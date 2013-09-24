/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.Article;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.service.API;

/*
 * This class is used to "refresh" the internal DB with data from the 
 * synced DB. This is necessary because the Android DB format differs
 * from a vanilla sqlite database.
 */
public class Sync extends DatabaseActivity {
	private static String logTag = Sync.class.toString();

	private static SyncTask syncTask = null;

	/*
	 * We use this flag to communicate to the progressUpdater Task that it's
	 * time to go home.
	 */
	volatile boolean quitProgressThread;
	Thread progressUpdaterThread = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_sync);

		((ProgressBar) findViewById(R.id.progressBar)).setMax(0);

		/*
		 * The maximum setting gets lost when the activity is restarted due to a
		 * config change
		 */
		((ProgressBar) findViewById(R.id.progressBar))
				.setMax(SyncTask.max_progress);

		try {
			((TextView) this.findViewById(R.id.syncText)).setText(Util
					.getApplicationString("label.sync", this));
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Safeguard from restarting the synctask on configuration changes
		 */
		if (null == syncTask) {
			syncTask = new SyncTask(this);
			syncTask.execute();
		}

		findViewById(R.id.syncIcon).startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.rotate));

	}

	@Override
	public void onStart() {
		super.onStart();

		/*
		 * We start a thread that is bound to the lifetime of this activity to
		 * be able to regularly check for the progress of the SyncTask (which
		 * might live longer than the activity)
		 */
		Util.logDebug(logTag, "starting progress update task");

		quitProgressThread = false;

		progressUpdaterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (false == quitProgressThread) {
					try {
						Thread.sleep(100);
						if (syncTask.current_progress == SyncTask.max_progress) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									syncTask = null;
									startActivity(new Intent(Sync.this,
											SplashScreen.class));
									finish();
								}
							});
							break;
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((ProgressBar) findViewById(R.id.progressBar))
										.setProgress(syncTask.current_progress);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		progressUpdaterThread.start();
	}

	@Override
	public void onPause() {
		Util.logDebug(logTag, "cancelling progress update task");

		/*
		 * Signal to the progress updater thread that we are done.
		 */
		quitProgressThread = true;
		boolean reallyQuit = false;

		while (false == reallyQuit) {
			try {
				progressUpdaterThread.join();
				reallyQuit = true;
			} catch (InterruptedException e) {
				/*
				 * We jump here if we got interrupted. In that case try to join
				 * again until success!
				 */
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		/*
		 * We simply ignore a back press. This activity only gets finished when
		 * the sync has either finished successfully or failed.
		 */
	}

	/*
	 * This AsyncTask lives longer than the activity that started it (due to the
	 * possibility of configuration changes which cause an activity to be
	 * restarted
	 */
	private static class SyncTask extends AsyncTask<Void, Integer, Void> {

		private DatabaseActivity context;

		/*
		 * volatile to allow the ProgressUpdaterTask to read the values across
		 * thread boundaries
		 */
		public volatile int current_progress = 0;

		/*
		 * The contract is here to set current_progress to max_progress when a]
		 * the sync task is done or b] it has failed
		 */
		public volatile static int max_progress = 12;

		public SyncTask(DatabaseActivity context) {
			this.context = context;
		}

		/*
		 * Using the progress publishing mechanism might not be necessary here
		 * since the variables are already volatile, but better be safe than
		 * sorry
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			if (null != values) {
				current_progress = values[0];
			}
		}

		/*
		 * In the case that the task has not been cancelled we set the progress
		 * to max progress
		 */
		@Override
		protected void onPostExecute(Void v) {
			current_progress = max_progress;
			ConnectivityManager manager = (ConnectivityManager) this.context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			boolean noInetAvailable = false;

			if ((info == null || !info.isConnectedOrConnecting())
					&& (manager.getActiveNetworkInfo() == null || !manager
							.getActiveNetworkInfo().isConnectedOrConnecting())) {
				noInetAvailable = true;
			}

			if (noInetAvailable) {
				Toast.makeText(
						context,
						"Import failed. You need  an active Network Connection.",
						Toast.LENGTH_SHORT).show();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... params) {
			try {
				int progress = 0;

				/*
				 * first Sync all locale article to BK
				 */
				List<Article> articles = ArticleManager
						.getLocalArticles(context);
				Dao<com.your.name.dao.File, Integer> fileDAO = ((DatabaseActivity) context).helper
						.getDao(com.your.name.dao.File.class);
				for (Article article2 : articles) {
					if (article2.images != null && article2.images.length() > 0) {
						JSONArray imageArray = new JSONArray(article2.images);

						for (int i = 0; i < imageArray.length(); i++) {
							JSONObject obj = imageArray.getJSONObject(i);

							int fileId = obj.getInt("id");

							com.your.name.dao.File file = fileDAO
									.queryForId(fileId);
							article2.imageFiles.add(file);

						}

					}

					if (article2.external_links != null
							&& article2.external_links.length() > 0) {
						JSONArray linkArray = new JSONArray(
								article2.external_links);

						for (int i = 0; i < linkArray.length(); i++) {
							JSONObject links = linkArray.getJSONObject(i);

							String link = links.getString("url");

							article2.externalLinks.add(link);
						}

					}

					if (API.getInstance(context).addArticle(article2, context)) {
						article2.locale = false;
					}

					for (int i = 0; i < article2.imageFiles.size(); i++) {
						fileDAO.createOrUpdate(article2.imageFiles.get(i));
					}

				}

				publishProgress(progress++);

				API.getInstance(context).createDLCacheDir();

				publishProgress(progress++);

				/*
				 * Download new DB file and store its path in downloadedDBPath
				 */
				String downloadedDBPath = API.getInstance(context).getNewDB();

				publishProgress(progress++);

				/*
				 * Download thumbs.zip, unzip it and store the path of the
				 * directory where they got unzipped to in downloadedThumbPath
				 */
				String downloadedThumbPath = API.getInstance(context)
						.getThumbnails();

				publishProgress(progress++);

				File thumbDir = new File(API.getInstance(context).dlCachePath
						+ "/thumbs/");
				if (false == Util.createDirectoryAnyways(thumbDir)) {
					throw new Exception(
							"Could not create directory to unpack thumbnails");
				}

				Util.unzip(downloadedThumbPath, thumbDir.getAbsolutePath(),
						null);

				publishProgress(progress++);

				SQLiteDatabase db = SQLiteDatabase.openDatabase(
						downloadedDBPath, null, SQLiteDatabase.OPEN_READONLY
								| SQLiteDatabase.NO_LOCALIZED_COLLATORS);

				import_from_sqlite(db, context);
				db.close();
				publishProgress(progress++);

				/*
				 * If the thumbnails directory in the externalCacheDir already
				 * exists, remove it.
				 */
				String targetThumbsPath = context.getExternalCacheDir()
						+ "/thumbs";

				if (true == new File(targetThumbsPath).exists()) {
					if (false == Util
							.removeDirectory(new File(targetThumbsPath)))
						throw new Exception(
								"Something went wrong removing the target thumbs dir");
				}

				if (false == thumbDir.renameTo(new File(targetThumbsPath))) {
					throw new Exception(
							"Something went wrong moving the thumbnails to the target location");
				}

				publishProgress(progress++);

				/**
				 * Read all Applications Strings
				 */
				String jsonFile = API.getInstance(context)
						.getApplicationStrings();

				publishProgress(progress++);

				InputStream bis = new FileInputStream(jsonFile);

				JSONObject responseJSON = new JSONObject(IOUtils.toString(bis,
						"UTF-8"));

				JSONObject stringsJSON = responseJSON.getJSONObject("response");

				SharedPreferences.Editor editor = context.getSharedPreferences(
						context.getString(R.string.strings_json),
						Activity.MODE_PRIVATE).edit();
				Iterator<String> it = stringsJSON.keys();
				while (it.hasNext()) {
					String key = it.next();

					editor.putString(key, stringsJSON.getString(key));
				}

				editor.commit();
				bis.close();
				publishProgress(progress++);

				/*
				 * Now, sync the sort settings sections
				 */

				jsonFile = API.getInstance(context).getBasicConfig();
				publishProgress(progress++);
				bis = new FileInputStream(jsonFile);
				responseJSON = new JSONObject(IOUtils.toString(bis, "UTF-8"));
				stringsJSON = responseJSON.getJSONObject("response");

				editor = context.getSharedPreferences(
						context.getString(R.string.basic_config),
						Activity.MODE_PRIVATE).edit();

				it = stringsJSON.keys();
				while (it.hasNext()) {
					String key = it.next();
					Object value = stringsJSON.get(key);
					if ("true".equals(value) || "false".equals(value)) {
						editor.putBoolean(key, stringsJSON.getBoolean(key));
					} else {
						editor.putString(key, stringsJSON.getString(key));
					}
				}

				editor.commit();
				bis.close();
				publishProgress(progress++);

				/*
				 * Read the Terms of Use
				 */
				String htmlPath = API.getInstance(context).getTermsOfUse();

				publishProgress(progress++);

				bis = new FileInputStream(htmlPath);

				JSONObject termsJSON = new JSONObject(IOUtils.toString(bis));

				if (termsJSON.has("response")) {
					if (termsJSON.getJSONObject("response").has("pagecontent")) {
						String termsHTML = termsJSON.getJSONObject("response")
								.getString("pagecontent");

						context.getSharedPreferences(
								context.getString(R.string.termsHTML),
								Context.MODE_PRIVATE)
								.edit()
								.putString(
										context.getString(R.string.termsHTML),
										termsHTML).commit();
					}
				}
				bis.close();

				publishProgress(progress++);

				/*
				 * If the user is authenticated, call all selected favorites
				 * from the server
				 */
				if (Util.isUserAuthentificated(context)) {
					API.getInstance(context).callFavoritesFromServer(context);
				}

				/*
				 * And finally remove the data in the DL cache
				 */
				API.getInstance(context).deleteDLCacheDir();

				publishProgress(progress++);

				publishProgress(max_progress);

				return null;
			} catch (Exception e) {
				Util.logDebug(logTag, "Something went wrong during the sync: "
						+ ((null != e.getMessage()) ? e.getMessage()
								: "no message"));
				e.printStackTrace();

				/*
				 * In the error case we also publish the max value (as the
				 * ProgressUpdaterTasks uses this as criterion to finish the
				 * activity)
				 */
				publishProgress(max_progress);
				return null;
			}
		}
	}

	public static String getVideoThumbnailsPath(Context context) {
		return context.getExternalCacheDir() + "/thumbs/video/";
	}

	public static String getThumbnailsPath(Context context) {
		return context.getExternalCacheDir() + "/thumbs/200x/";
	}

	private static void dropTables(SQLiteDatabase db, Context context) {

		db.beginTransaction();
		{
			Cursor tableCursor = db.rawQuery(
					"SELECT name from sqlite_master WHERE type='table';", null);

			Util.logDebug(logTag, "There are " + tableCursor.getCount()
					+ " tables");
			tableCursor.moveToFirst();

			while (false == tableCursor.isAfterLast()) {
				String tableName = tableCursor.getString(0);

				if (tableName.equals("android_metadata")) {
					tableCursor.moveToNext();
					continue;
				}
				Util.logDebug(logTag, "table name: " + tableName);
				{
					Util.logDebug(logTag, "dropping all data in: " + tableName);
					/*
					 * Delete all data in the table
					 */
					db.delete(tableName, null, null);

				}
				tableCursor.moveToNext();
			}
			tableCursor.close();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/*
	 * transfers data from newDB to db.
	 */
	private static void transferData(SQLiteDatabase db, SQLiteDatabase newDB,
			Context context) throws Exception {

		db.beginTransaction();
		{
			Cursor tableCursor = db.rawQuery(
					"SELECT name from sqlite_master WHERE type='table';", null);
			Util.logDebug(logTag, "There are " + tableCursor.getCount()
					+ " tables");
			tableCursor.moveToFirst();

			while (false == tableCursor.isAfterLast()) {
				String tableName = tableCursor.getString(0);

				if (tableName.equals("android_metadata")
						|| tableName.equals("sqlite_sequence")
						|| tableName.equals("group_favorites")) {
					tableCursor.moveToNext();
					continue;
				}

				Util.logDebug(logTag, "table name: " + tableName);
				Cursor columnNameCursor = db.rawQuery("PRAGMA table_info( "
						+ tableName + " );", null);
				Set<String> existingColumnNames = new HashSet<String>();
				columnNameCursor.moveToFirst();

				while (false == columnNameCursor.isAfterLast()) {
					String name = columnNameCursor.getString(columnNameCursor
							.getColumnIndex("name"));

					Util.logDebug(logTag, "target table has column: " + name);
					existingColumnNames.add(name);
					columnNameCursor.moveToNext();
				}
				columnNameCursor.close();
				{
					Cursor rowCursor = newDB.rawQuery("SELECT distinct * FROM "
							+ tableName + ";", null);

					String[] columnNames = rowCursor.getColumnNames();

					Util.logDebug(logTag, "filling DB with data - table: "
							+ tableName);

					rowCursor.moveToFirst();

					while (false == rowCursor.isAfterLast()) {

						ContentValues values = new ContentValues();
						for (String columnName : columnNames) {
							if (false == existingColumnNames
									.contains(columnName)) {
								continue;
							}

							int columnIndex = rowCursor
									.getColumnIndex(columnName);
							boolean handled = false;

							if (rowCursor.isNull(columnIndex)) {
								values.putNull(columnName);
								handled = true;
							}
							/*
							 * Nasty workaround logic for hacking around missing
							 * getType function in API level 8
							 */
							if (false == handled) {
								try {
									String value = rowCursor
											.getString(columnIndex);
									values.put(columnName, value);
									handled = true;
								} catch (Exception e) {
									// We do nothing here purposefully
								}
							}

							if (false == handled) {
								try {
									int value = rowCursor.getInt(columnIndex);
									values.put(columnName, value);
									handled = true;
								} catch (Exception e) {
									// We do nothing here purposefully
								}
							}

							if (false == handled) {

								try {
									float value = rowCursor
											.getFloat(columnIndex);

									values.put(columnName, value);
									handled = true;
								} catch (Exception e) {
									// We do nothing here purposefully
								}
							}
							if (false == handled) {
								try {
									byte[] value = rowCursor
											.getBlob(columnIndex);

									values.put(columnName, value);
									handled = true;
								} catch (Exception e) {
									// We do nothing here purposefully
								}
							}
							if (false == handled) {
								throw new Exception(
										"type not handled - this should not happen!!!");
							}
						}

						/*
						 * Here we have all values of the row copied into the
						 * ContentValues thingie. So we can insert it into our
						 * internal DB
						 */
						db.insert(tableName, null, values);

						rowCursor.moveToNext();
					}
					rowCursor.close();
				}
				tableCursor.moveToNext();
			}
			tableCursor.close();
		}

		db.setTransactionSuccessful();
		db.endTransaction();

	}

	public static void import_from_sqlite(SQLiteDatabase newDB,
			DatabaseActivity activity) throws Exception {

		Util.logDebug(logTag, "import_from_sqlite");

		SQLiteDatabase db = activity.helper.getWritableDatabase();
		{
			db.execSQL("PRAGMA foreign_keys = false;");
			{
				dropTables(db, activity);
				transferData(db, newDB, activity);
			}
			db.execSQL("PRAGMA foreign_keys = true;");
		}
		// db.close();

		Util.logDebug(logTag, "db: " + db.toString());
		Util.logDebug(logTag, "setting all filters to null");

		ContentValues values = new ContentValues();
		values.put("selected", 0);

		int rowsAffected = db.update("criteria_options", values, null, null);
		Util.logDebug(logTag, "updated: " + rowsAffected + "rows");
	}
}