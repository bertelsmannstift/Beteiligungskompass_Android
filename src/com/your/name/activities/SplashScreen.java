/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.service.API;

public class SplashScreen extends NavigationActivity {
	private static String logTag = SplashScreen.class.toString();

	/*
	 * Bump this integer whenever the initial data has changed to make devices
	 * renew their cache
	 */
	private static int INITIAL_SYNC_VERSION = 21;

	static InitialImport initialImport = null;

	volatile boolean quitWatcherThread;
	Thread watcherThread = null;

	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_splashscreen);

		if (null == initialImport) {
			Util.logDebug(logTag, "starting initial import");
			initialImport = new InitialImport();
			initialImport.execute();
		} else {
			try {
				ArticleManager.initAll(SplashScreen.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Util.logDebug(logTag, "not starting initial import");
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		quitWatcherThread = false;
		watcherThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (false == quitWatcherThread) {
					try {
						Thread.sleep(100);
						if (initialImport == null
								|| true == initialImport.finished) {
							runOnUiThread(new Runnable() {
								public void run() {
									initialImport = null;
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									startActivity(new Intent(SplashScreen.this,
											Dashboard.class));
									finish();
								}
							});

							break;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		watcherThread.start();
	}

	@Override
	public void onPause() {
		quitWatcherThread = true;
		boolean watcherReallyQuit = false;
		while (false == watcherReallyQuit) {
			try {
				watcherThread.join();
				watcherReallyQuit = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		super.onPause();
	}

	private class InitialImport extends AsyncTask<Void, Void, Boolean> {
		volatile boolean finished = false;

		@Override
		protected void onPostExecute(Boolean result) {
			if (false == result) {
				ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

				if (manager.getActiveNetworkInfo() == null
						|| !manager.getActiveNetworkInfo()
								.isConnectedOrConnecting()) {
					Toast.makeText(
							SplashScreen.this,
							"Import failed. You need  an active Network Connection.",
							Toast.LENGTH_SHORT).show();
				}

			}
			finished = true;
		}

		private void copyInitialData() throws Exception {

			Util.logDebug(logTag, "dumping SQL CREATE statements");

			Util.logDebug(logTag, "copying initial data packet");

			if (getSharedPreferences(getString(R.string.strings_json),
					MODE_PRIVATE).getInt(
					getString(R.string.initial_sync_version), 0) >= INITIAL_SYNC_VERSION) {

				Util.logDebug(logTag, "data up to date, not importing anew");

				return;
			}

			String tmpDBPath = getExternalCacheDir() + "/"
					+ getString(R.string.temp_sqlite_db_name);

			if (true == new File(tmpDBPath).exists()) {

				new File(tmpDBPath).delete();
			}

			Util.logDebug(logTag, "first the initial database");

			BufferedInputStream bis = new BufferedInputStream(getResources()
					.openRawResource(R.raw.synced_database));

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpDBPath));

			byte[] bytes = new byte[8192];
			Util.flow(bis, bos, bytes);

			bos.close();
			bis.close();

			{
				SQLiteDatabase db = SQLiteDatabase.openDatabase(tmpDBPath,
						null, SQLiteDatabase.OPEN_READONLY
								| SQLiteDatabase.NO_LOCALIZED_COLLATORS);

				Sync.import_from_sqlite(db, SplashScreen.this);

				db.close();
			}

			Util.logDebug(logTag, "then the strings.json");

			bis = new BufferedInputStream(getAssets().open(
					getString(R.string.strings_json)));

			JSONObject responseJSON = new JSONObject(IOUtils.toString(bis,
					"UTF-8"));

			JSONObject stringsJSON = responseJSON.getJSONObject("response");

			SharedPreferences.Editor editor = getSharedPreferences(
					getString(R.string.strings_json), MODE_PRIVATE).edit();

			@SuppressWarnings("unchecked")
			Iterator<String> it = stringsJSON.keys();
			while (it.hasNext()) {
				String key = it.next();

				editor.putString(key, stringsJSON.getString(key));
			}

			editor.commit();
			responseJSON = null;
			stringsJSON = null;
			bis.close();

			getSharedPreferences(getString(R.string.strings_json), MODE_PRIVATE)
					.edit()
					.putInt(getString(R.string.initial_sync_version),
							INITIAL_SYNC_VERSION).commit();

			Util.logDebug(logTag, "Now, the basic config  values");

			bis = new BufferedInputStream(getAssets().open(
					getString(R.string.basic_config)));

			responseJSON = new JSONObject(IOUtils.toString(bis, "UTF-8"));

			stringsJSON = responseJSON.getJSONObject("response");

			editor = getSharedPreferences(getString(R.string.basic_config),
					MODE_PRIVATE).edit();

			@SuppressWarnings("unchecked")
			Iterator<String> sortKeys = stringsJSON.keys();
			while (sortKeys.hasNext()) {
				String key = sortKeys.next();

				Object value = stringsJSON.get(key);

				if ("true".equals(value) || "false".equals(value)) {
					editor.putBoolean(key, stringsJSON.getBoolean(key));
				} else {
					editor.putString(key, stringsJSON.getString(key));
				}
			}

			editor.commit();
			responseJSON = null;
			stringsJSON = null;
			bis.close();

			getSharedPreferences(getString(R.string.basic_config), MODE_PRIVATE)
					.edit()
					.putInt(getString(R.string.initial_sync_version),
							INITIAL_SYNC_VERSION).commit();

			Util.logDebug(logTag, "Terms of Use, too");

			bis = new BufferedInputStream(getAssets().open("terms.html"));

			String terms = IOUtils.toString(bis);

			editor = getSharedPreferences(getString(R.string.termsHTML),
					MODE_PRIVATE).edit();

			editor.putString(getString(R.string.termsHTML), terms);

			editor.commit();

			bis.close();

			File thumbDir = new File(
					API.getInstance(SplashScreen.this).dlCachePath + "/thumbs/");

			if (false == Util.createDirectoryAnyways(thumbDir)) {
				throw new Exception(
						"Could not create directory to unpack thumbnails");
			}

			Util.unzipFromInputStream(
					getAssets().open("thumb.zip"),
					API.getInstance(SplashScreen.this).dlCachePath + "/thumbs/",
					null);

			/*
			 * If the thumbnails directory in the externalCacheDir already
			 * exists, remove it.
			 */
			String targetThumbsPath = SplashScreen.this.getExternalCacheDir()
					+ "/thumbs";

			if (true == new File(targetThumbsPath).exists()) {
				if (false == Util.removeDirectory(new File(targetThumbsPath)))
					throw new Exception(
							"Something went wrong removing the target thumbs dir");
			}

			if (false == thumbDir.renameTo(new File(targetThumbsPath))) {
				throw new Exception(
						"Something went wrong moving the thumbnails to the target location");
			}

			new File(tmpDBPath).delete();

			Util.logDebug(logTag, "copying initial data packet - done");

			ArticleManager.observable.notifyDatasetChanged();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Util.logDebug(logTag, "Importing data from DB");

			try {
				copyInitialData();

				ArticleManager.initAll(SplashScreen.this);

			} catch (Exception e) {
				e.printStackTrace();
				Util.logDebug(logTag, "Exception message: "
						+ ((e.getMessage() != null) ? e.getMessage()
								: "no message"));
				return false;
			}

			return true;
		}
	}

}
