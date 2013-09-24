/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.Article;
import com.your.name.dao.ArticleLink;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.FavoriteArticle;
import com.your.name.dao.FavoriteGroup;
import com.your.name.dao.File;
import com.your.name.dao.FavoritesGroup;
import com.your.name.dao.Page;
import com.your.name.dao.PartnerLink;
import com.your.name.dao.User;

/**
 * This class create or update the database
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static String logTag = DatabaseHelper.class.toString();

	private static final int DATABASE_VERSION = 17;

	public DatabaseHelper(Context context) throws SQLiteException {
		super(context, sqlite_db_path(context), null, DATABASE_VERSION);

		Util.logDebug(logTag, "setting context");
	}

	public static String sqlite_db_path(Context context) throws SQLiteException {
		java.io.File dir = context.getExternalCacheDir();

		if (null == dir) {
			throw new SQLiteException(
					"something went wrong getting the external cache dir");
		}

		String db_name = context.getString(R.string.sqlite_db_name);

		if (null == db_name) {
			throw new SQLiteException(
					"something went wrong getting the db name");

		}

		return dir.getAbsolutePath() + "/" + db_name;
	}

	@SuppressWarnings("rawtypes")
	private List<Class> getClasses() {
		List<Class> classes = new ArrayList<Class>();

		classes.add(Article.class);
		classes.add(ArticlesOption.class);
		classes.add(ArticleLink.class);
		classes.add(CriteriaOption.class);
		classes.add(Criterion.class);
		classes.add(FavoriteArticle.class);
		classes.add(FavoriteGroup.class);
		classes.add(File.class);
		classes.add(FavoritesGroup.class);
		classes.add(Page.class);
		classes.add(User.class);
		classes.add(PartnerLink.class);

		return classes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Util.logDebug(DatabaseHelper.class.getName(), "onCreate");

			@SuppressWarnings("rawtypes")
			List<Class> classes = getClasses();

			for (@SuppressWarnings("rawtypes")
			Class c : classes) {
				Util.logDebug(logTag, "creating table for: " + c.toString());
				TableUtils.createTable(connectionSource, c);
			}

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource cs,
			int oldVersion, int newVersion) {

		String[] tables = { "articles", "articles_options", "article_links",
				"criteria", "criteria_options", "favorite_articles",
				"favoritegroup", "favorites", "favorites_groups", "files",
				"images", "links", "pages", "partnerlinks", "rss_feeds",
				"users", "videos" };

		for (String table : tables) {
			try {
				Util.logDebug(logTag, "dropping table: " + table);

				db.execSQL("DROP TABLE  IF EXISTS " + table + ";");
			} catch (Exception e) {
				Util.logDebug(logTag, e.getMessage() != null ? e.getMessage()
						: "no message");
			}
		}

		onCreate(db, cs);
	}

	@Override
	public void close() {
		super.close();
	}

	public void dumpCreateStatements() {
		try {
			Util.logDebug(logTag, "dumping create statements");

			for (@SuppressWarnings("rawtypes")
			Class c : getClasses()) {
				@SuppressWarnings("unchecked")
				List<String> statements = TableUtils.getCreateTableStatements(
						getConnectionSource(), c);

				for (String s : statements) {
					Util.logDebug(logTag, s);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}