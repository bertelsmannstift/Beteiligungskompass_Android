/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;

/**
 * this class contains all necessary methods which are used in the whole app.
 */
public class Util {
	private static String logTag = Util.class.toString();

	/*
	 * Array with the default month names. That will renamed on runtime
	 */
	private static String[] monthName = { "Januar", "Februar", "März", "April",
			"Mai", "Juni", "Juli", "August", "September", "Oktober",
			"November", "Dezember" };

	/*
	 * the default range of years for the add article selection box
	 */
	public static String[] years = { "", "1980", "1981", "1982", "1983",
			"1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991",
			"1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999",
			"2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007",
			"2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015",
			"2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023",
			"2024", "2025", "2026", "2027", "2028", "2030", "2031", "2032",
			"2033", "2034", "2035", "2036", "2037", "2038", "2039", "2040" };

	public Util() {
	}

	/**
	 * 
	 * @param con
	 * @return boolean
	 */
	public static boolean isWifiConnected(Context con) {
		ConnectivityManager connMgr = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * 
	 * @param map
	 * @return Bitmap
	 * 
	 *         scaled a bitmap for the articlelist view to a size of 200x200px
	 */
	public static Bitmap getScaledBitmap(Bitmap map) {
		try {
			int width = map.getWidth();
			int height = map.getHeight();
			int newWitdh = 200;
			int newheight = 200;

			float scaleWith = ((float) newWitdh) / width;
			float scaleheight = ((float) newheight) / height;

			Matrix matrix = new Matrix();

			// matrix.postRotate(90);
			matrix.postScale(scaleWith, scaleheight);

			Bitmap resizedBitmap = Bitmap.createBitmap(map, 0, 0, width,
					height, matrix, true);

			return resizedBitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isUserAuthentificated(Context context) {
		SharedPreferences pref = context.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);

		if (!pref.getString("token", "").equals("")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param contenUri
	 * @param mContext
	 * @return File
	 */
	public static com.your.name.dao.File getRealPathFromURI(
			Uri contenUri, Context mContext) {
		String[] proj = { MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE };
		CursorLoader loader = new CursorLoader(mContext, contenUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int data_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
		int display_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
		int image_size = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
		cursor.moveToFirst();
		String path = cursor.getString(data_index);

		String[] values = path.split("/");
		String imgName = values[(values.length - 1)];

		String[] neededValues = imgName.split("\\.");

		String mime = cursor.getString(column_index);
		String name = cursor.getString(display_index);
		int size = cursor.getInt(image_size);
		cursor.close();
		com.your.name.dao.File file = new com.your.name.dao.File();
		file.ext = neededValues[1];
		file.mime = mime;
		file.filename = name;
		file.size = size;
		file.uri = contenUri.toString();

		return file;
	}

	/**
	 * 
	 * @param context
	 * @return list of month strings
	 */
	public static String[] getMonths(Context context) {
		try {
			return monthName = new String[] {
					Util.getApplicationString("label.january", context),
					Util.getApplicationString("label.feburary", context),
					Util.getApplicationString("label.march", context),
					Util.getApplicationString("label.april", context),
					Util.getApplicationString("label.mai", context),
					Util.getApplicationString("label.june", context),
					Util.getApplicationString("label.july", context),
					Util.getApplicationString("label.august", context),
					Util.getApplicationString("label.september", context),
					Util.getApplicationString("label.october", context),
					Util.getApplicationString("label.november", context),
					Util.getApplicationString("label.december", context) };
		} catch (Exception e) {
			return monthName;
		}
	}

	/**
	 * 
	 * @param dipValue
	 * @param context
	 * @return int
	 * 
	 *         this method converts dp into px
	 */
	public static int getPixels(int dipValue, Context context) {
		Resources r = context.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dipValue, r.getDisplayMetrics());
		return px;
	}

	/**
	 * 
	 * @param context
	 * @return boolean
	 * 
	 *         checks if the device is a tablet or a smartphone
	 */
	public static boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		return (xlarge);
	}

	/**
	 * 
	 * @param startNewActivity
	 * @param closeActivity
	 * 
	 *            helper method to start a new activity
	 */
	public static void switchActivity(Intent startNewActivity,
			Activity closeActivity) {
		closeActivity.startActivity(startNewActivity);
		closeActivity.finish();
	}

	public static boolean isOrientationPortrait(Context context) {
		return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? true
				: false;
	}

	/**
	 * A utility function to lookup strings that come from the strings.json
	 * file..
	 */
	public static String getApplicationString(String key, Context context)
			throws Exception {

		String ret = context.getSharedPreferences(
				context.getString(R.string.strings_json), Context.MODE_PRIVATE)
				.getString(key, null);

		Util.logDebug(logTag, "Application string: key: \"" + key
				+ "\"  value: \"" + ret + "\"");

		if (null == ret) {
			throw new RuntimeException("string not found - bug!! key: " + key);
		}

		return ret;
	}

	public static boolean isModuleActive(String key, Context context) {
		boolean ret = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getBoolean(key, true);

		Util.logDebug(logTag, "Modal Section: key: \"" + key + "\"  value: \""
				+ ret + "\"");

		return ret;
	}

	public static String getSortString(String key, Context context) {

		String result = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getString(key, "");

		return result;

	}

	/**
	 * 
	 * @param context
	 * @return a list of strings, which the modules they should not be shown in
	 *         the app are returned
	 */
	public static ArrayList<String> getNotShownModules(Context context) {

		ArrayList<String> modules = new ArrayList<String>();

		boolean result = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getBoolean(BasicConfigValue.MODULE_EXPERT, true);

		if (!result) {
			modules.add("expert");
		}

		result = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getBoolean(BasicConfigValue.MODULE_QA, true);

		if (!result) {
			modules.add("qa");
		}

		result = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getBoolean(BasicConfigValue.MODULE_EVENT, true);

		if (!result) {
			modules.add("event");
		}

		result = context.getSharedPreferences(
				context.getString(R.string.basic_config), Context.MODE_PRIVATE)
				.getBoolean(BasicConfigValue.MODULE_NEWS, true);

		if (!result) {
			modules.add("news");
		}

		return modules;

	}

	public static String getMonth(int month) {
		return monthName[month];
	}

	/**
	 * 
	 * @param type
	 * @param context
	 * @return
	 * 
	 *         returns a list of strings based on the submitted type. The
	 *         strings are used in the sort view of the article list
	 */
	public static SparseArray<String> getOrderByType(String type,
			Context context) {
		SparseArray<String> sortBy = new SparseArray<String>();
		try {
			if (type.equals(com.your.name.dao.Article.EXPERT)) {
				sortBy.put(0,
						Util.getApplicationString("label.lastname", context));
				sortBy.put(4,
						Util.getApplicationString("label.institution", context));
				sortBy.put(3,
						Util.getApplicationString("label.added_last", context));
			} else if (type
					.equals(com.your.name.dao.Article.EVENT)) {
				sortBy.put(2, Util.getApplicationString("label.date", context));
			} else if (type
					.equals(com.your.name.dao.Article.METHOD)) {
				sortBy.put(0, Util.getApplicationString("label.title", context));
				sortBy.put(3,
						Util.getApplicationString("label.added_last", context));
			} else if (type
					.equals(com.your.name.dao.Article.NEWS)) {
				sortBy.put(2, Util.getApplicationString("label.date", context));
				sortBy.put(6,
						Util.getApplicationString("label.author", context));
			} else if (type
					.equals(com.your.name.dao.Article.QA)) {
				sortBy.put(0, Util.getApplicationString("label.title", context));
				sortBy.put(2, Util.getApplicationString("label.date", context));
				sortBy.put(3,
						Util.getApplicationString("label.added_last", context));
			} else if (type
					.equals(com.your.name.dao.Article.STUDY)) {
				sortBy.put(0, Util.getApplicationString("label.title", context));
				sortBy.put(2, Util.getApplicationString("label.date", context));
				sortBy.put(3,
						Util.getApplicationString("label.added_last", context));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sortBy;
	}

	/**
	 * 
	 * @param article
	 * @param context
	 * @return String
	 * 
	 *         This method returns the short title below the main title of
	 *         an article. It takes different information for the sub title
	 *         based on the article type
	 */
	public static String buildSubTitle(Article article, Context context) {
		String subTitle = "";
		try {
			if (article.type.equals(Article.STUDY)) {

				if (article.city != null && article.city.length() > 0) {
					subTitle += article.city + " | ";
				}
				Dao<CriteriaOption, Integer> optionDAO = ((DatabaseActivity) context).helper
						.getDao(CriteriaOption.class);

				Dao<ArticlesOption, Integer> artOptionDAO = ((DatabaseActivity) context).helper
						.getDao(ArticlesOption.class);
				;

				QueryBuilder<ArticlesOption, Integer> artQB = artOptionDAO
						.queryBuilder();
				artQB.selectColumns("model_criterion_option_id");
				artQB.where().eq("model_article_id", article.id);

				QueryBuilder<CriteriaOption, Integer> qb = optionDAO
						.queryBuilder();

				Dao<Criterion, Integer> criterionDAO = ((DatabaseActivity) context).helper
						.getDao(Criterion.class);

				QueryBuilder<Criterion, Integer> critQB = criterionDAO
						.queryBuilder();
				critQB.selectColumns("id");
				critQB.where().eq("discriminator", "country");

				Criterion crit = critQB.queryForFirst();

				qb.where().in("id", artQB).and().eq("criterion_id", crit.id);

				List<CriteriaOption> options = qb.query();

				for (CriteriaOption criteriaOption : options) {
					if (criteriaOption.default_value) {
						continue;
					}
					subTitle += criteriaOption.title + " | ";
					break;
				}

				if (article.start_year > 0 || article.start_month > 0
						|| article.end_month > 0 || article.end_year > 0) {
					SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy",
							Locale.getDefault());

					if (article.end_year == 0) {
						if (article.start_month > 0 && article.start_year > 0) {
							subTitle += "seit "
									+ format.format(new GregorianCalendar(
											article.start_year,
											article.start_month, 0).getTime())
									+ " | ";
						} else if (article.start_year > 0) {
							subTitle += "seit " + article.start_year + " | ";
						}

						subTitle += Util.getApplicationString(
								"label.state_ongoing", context);

					} else {
						String endyear = "";
						if (article.end_month > 0) {
							endyear = format.format(new GregorianCalendar(
									article.end_year, article.end_month, 0)
									.getTime());
						} else {
							endyear = article.end_year + "";
						}
						String startyear = "";
						if (article.start_year > 0) {
							if (article.start_month > 0) {
								startyear = format
										.format(new GregorianCalendar(
												article.start_year,
												article.start_month, 0)
												.getTime());
							} else {
								startyear = article.start_year + "";
							}
						}

						if (startyear.length() > 0) {
							subTitle += startyear + " - " + endyear + " | ";
						}

						GregorianCalendar today = new GregorianCalendar();
						GregorianCalendar enddate = new GregorianCalendar(
								article.end_year, article.end_month, 0);
						if (article.end_year == 0) {
							subTitle += Util.getApplicationString(
									"label.state_ongoing", context);
						} else if (today.getTimeInMillis() > enddate
								.getTimeInMillis()) {
							subTitle += Util.getApplicationString(
									"label.state_closed", context);
						} else {
							subTitle += Util.getApplicationString(
									"label.state_ongoing", context);
						}
					}
				}

			} else if (article.type.equals(Article.QA)) {
				if (article.author_answer != null
						&& article.author_answer.length() > 0) {
					subTitle = "Autor: " + article.author_answer;
				}
			} else if (article.type.equals(Article.EXPERT)) {
				if (article.city != null && article.city.length() > 0) {
					subTitle = article.city;
				}
			} else if (article.type.equals(Article.NEWS)) {
				if (article.date != null) {
					SimpleDateFormat format = new SimpleDateFormat(
							"dd.MM.yyyy", Locale.getDefault());
					subTitle = format.format(article.date);
				}

				if (article.author != null && article.author.length() > 0) {
					if (subTitle.length() > 0) {
						subTitle += " | ";
					}

					subTitle += article.author;
				}
			} else if (article.type.equals(Article.EVENT)) {
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",
						Locale.getDefault());
				if (article.start_date != null) {

					subTitle = format.format(article.start_date);
				}

				if (article.end_date != null) {
					if (subTitle.length() > 0) {
						subTitle += " - ";
					}

					subTitle += format.format(article.end_date);
				}

				if (article.city != null && article.city.length() > 0) {
					subTitle += " " + article.city;
				}
			}

			return subTitle;
		} catch (Exception e) {
			e.printStackTrace();
			return subTitle = "";
		}
	}

	/**
	 * 
	 * @param directory
	 * @return
	 * 
	 *         try to remove the directory
	 */
	public static boolean removeDirectory(File directory) {
		if (directory == null) {
			return false;
		}

		if (false == directory.exists()) {
			Util.logDebug(logTag, "Directory doesn't exist. Not removing: "
					+ directory.getPath());
			return true;
		}

		if (false == directory.isDirectory()) {
			Util.logDebug(logTag,
					"Not a directory. Not removing: " + directory.getPath());
			return false;
		}

		String[] list = directory.list();

		// Some JVMs return null for File.list() when the
		// directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {

				File entry = new File(directory, list[i]);

				if (entry.isDirectory()) {
					if (false == removeDirectory(entry))
						return false;
				} else {
					if (false == entry.delete())
						return false;
				}
			}
		}
		return directory.delete();
	}

	/**
	 * remove the target directory if it exists. returns FALSE if the 
	 * directory removal failed (if it was necessary) or if the mkdirs() failed
	 */
	public static boolean createDirectoryAnyways(File directory) {
		if (directory.exists()) {
			Util.logDebug(logTag,
					"directory exists: " + directory.getAbsolutePath());
			Util.logDebug(logTag,
					"removing directory" + directory.getAbsolutePath());

			if (false == removeDirectory(directory)) {
				return false;
			}
		}

		Util.logDebug(logTag,
				"creating directory: " + directory.getAbsolutePath());
		return directory.mkdirs();
	}

	public static void flow(InputStream is, OutputStream os, byte[] buf)
			throws IOException {
		int numRead;
		while ((numRead = is.read(buf)) >= 0) {
			os.write(buf, 0, numRead);
		}
	}

	/**
	 * 
	 * @param inputStream
	 * @param targetLocation
	 * @param listener
	 * @throws Exception
	 * 
	 *             unzips the files from the service and saves these in the
	 *             target location
	 */
	public static void unzipFromInputStream(InputStream inputStream,
			String targetLocation, FileNameListener listener) throws Exception {
		ZipInputStream zin = new ZipInputStream(inputStream);
		ZipEntry ze = null;
		while ((ze = zin.getNextEntry()) != null) {
			if (null != listener) {
				listener.updateFileName(ze.getName());
			}

			if (ze.isDirectory()) {
				if (false == dirChecker(targetLocation + "/" + ze.getName())) {
					throw new Exception("failed to create directory: "
							+ targetLocation + "/" + ze.getName());
				}
			} else {

				byte[] buffer = new byte[8192];

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						new FileOutputStream(targetLocation + "/"
								+ ze.getName()), buffer.length);
				int size = 0;
				while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
					bufferedOutputStream.write(buffer, 0, size);
				}

				bufferedOutputStream.flush();
				bufferedOutputStream.close();
				zin.closeEntry();
			}
		}
		zin.close();
	}

	public static void unzip(String zipFile, String targetLocation,
			FileNameListener listener) throws Exception {

		FileInputStream inputStream = new FileInputStream(zipFile);

		unzipFromInputStream(inputStream, targetLocation, listener);

		inputStream.close();
	}

	/**
	 * If the file doesn't exist, create it. At the moment we support two kinds
	 * of video hosts (vimeo / youtube).
	 */
	private static boolean dirChecker(String dir) {
		File f = new File(dir);

		if (!f.isDirectory()) {
			return f.mkdirs();
		}

		return true;
	}

	public static final String VIMEO = "vimeo";
	public static final String YOUTUBE = "youtube";

	/**
	 * get a pair describing the video from the URL
	 */
	public static Pair<String, String> getVideoIDFromURL(String urlString)
			throws Exception {
		Util.logDebug(logTag, "getting info from URL: " + urlString);

		URL url = new URL(urlString);

		String host = url.getHost();

		Util.logDebug(logTag, "host: " + host);

		if (host.equals("youtube.com") || host.equals("www.youtube.com")) {
			String id = "";

			/*
			 * find the position of the v= indicator for the video ID
			 */
			int vPos = url.getQuery().indexOf("v=");

			if (-1 == vPos) {
				throw new Exception("malformed query in URL: " + urlString
						+ " query: " + url.getQuery());
			}

			int additionalParamsPos = url.getQuery().indexOf("&", vPos);

			/*
			 * Finally get the substring skipping the "v="
			 */
			if (-1 == additionalParamsPos) {
				id = url.getQuery().substring(vPos + 2);
			} else {
				id = url.getQuery().substring(vPos + 2, additionalParamsPos);
			}

			Util.logDebug(logTag, "found " + YOUTUBE + " ID: " + id);

			return new Pair<String, String>(YOUTUBE, id);
		}

		/*
		 * In a vimeo URL the path part encodes the video URL. We strip the
		 * leading '/' from the path and return a corresponding tuple
		 */
		if (host.equals("vimeo.com") || host.equals("www.vimeo.com")) {
			String id = "";

			if (url.getPath().length() > 1
					&& url.getPath().substring(0, 1).equals("/")) {

				id = url.getPath().substring(1);

				Util.logDebug(logTag, "found " + VIMEO + " ID: " + id);

				return new Pair<String, String>(VIMEO, id);

			} else {
				throw new Exception("Malformed path in URL: " + urlString
						+ " path: " + url.getPath());
			}
		}

		throw new Exception("URL not handled: " + urlString);
	}

	/**
	 * Comment out Log.d if you don't want any console debug
	 */
	public static void logDebug(String tag, String log) {
		Log.d(tag, log);
	}
}
