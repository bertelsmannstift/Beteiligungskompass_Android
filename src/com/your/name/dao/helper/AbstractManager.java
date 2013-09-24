/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.your.name.BasicConfigValue;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;


public abstract class AbstractManager {
	public static List<Criterion> allCritera = null;
	public static HashMap<String, Criterion> filteredCriterion = new HashMap<String, Criterion>();
	public static List<FeaturedVideo> featuredVideos = null;
	public static ObservableData observable = new ObservableData();

	/*
	 * Favorite list Parameter
	 */
	public static boolean showOnlyMineInFavorites = false;
	public static int groupToShowInFavorites = -1;
	public static boolean showUnassignedInFavorites = false;
	public static boolean showOnlyLocaleArticle = false;

	/*
	 * Simplistic caching scheme. After a sync this should be set to NULL
	 * again, so that the list is created again
	 */
	public static boolean filterReset = false;
	public static boolean filterCleared = true;
	public static int sortBy = -1;
	public static String searchText = "";
	public AbstractManager() {}
	
	/**
	 * @param baseActivity
	 * @throws Exception
	 * 
	 * This method refreshes all database data and notifies the views
	 * to refresh their UI
	 */
	public static void refreshAll(DatabaseActivity baseActivity)
			throws Exception {

		initAll(baseActivity);
		observable.notifyDatasetChanged();
	}

	/**
	 * @param baseActivity
	 * @throws Exception
	 * This method removes all cached filter criteria and featured videos.
	 * Calls the criteria from the database and gets the new featured videos.
	 */
	public static void initAll(DatabaseActivity baseActivity) throws Exception {
		featuredVideos = null;
		filteredCriterion = null;
		allCritera = CriterionManager.getCriteria(baseActivity, "", true);
		ArticleManager.getFeaturedVideos(baseActivity);
	}
	/**
	 * @param baseActivity
	 * @throws Exception
	 * Clears all filter settings which the user has selected
	 */
	public static void refreshFilter(DatabaseActivity baseActivity)
			throws Exception {

		if (!searchText.equals("")) {
			searchText = "";
			observable.notifyDatasetChanged();
			return;
		}
		filterCleared = true;
		Dao<CriteriaOption, Integer> criterioNDAO = baseActivity.helper
				.getDao(CriteriaOption.class);

		UpdateBuilder<CriteriaOption, Integer> builder = criterioNDAO
				.updateBuilder();

		builder.updateColumnValue("selected", false).where()
				.eq("selected", true);

		criterioNDAO.update(builder.prepare());
		observable.notifyDatasetChanged();
		filteredCriterion = null;

		allCritera = CriterionManager.getCriteria(baseActivity, "", true);
		observable.notifyDatasetChanged();
	}

	/**
	 * @param activity
	 * @return
	 * Checks if a filter is set.
	 */
	public static boolean isFilterSet(DatabaseActivity activity) {
		try {
			Dao<CriteriaOption, Integer> dao = activity.helper
					.getDao(CriteriaOption.class);
			QueryBuilder<CriteriaOption, Integer> articleQB = dao
					.queryBuilder();

			articleQB.setCountOf(true);
			articleQB.selectColumns("id");
			articleQB.where().eq("selected", true).and()
					.eq("default_value", false);

			long res = dao.countOf(articleQB.prepare());

			filterCleared = (res > 0) ? false : true;

			return filterCleared;

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param articleQB
	 * @param type
	 * 
	 * To understand this method, you have to take a look at the
	 * getOrderByType() method in the Util class. The variable "sortBy"
	 * defines one of the key values in the list of orderTypes.
	 * 
	 * For Example: if you have a list of expert articles and the
	 * user sorts the articles after institution, the sortBy variable
	 * gets the value 4.
	 */
	protected static void sortBy(QueryBuilder<Article, Integer> articleQB,
			String type, Context context) {
		switch (sortBy) {
		case 0:
			if (type.equals(Article.EXPERT)) {
				articleQB.orderByRaw("lastname ASC, institution asc");
			} else {
				articleQB.orderBy("title", true);
			}
			break;
		case 1:
			articleQB.orderBy("lastname", true);
			break;
		case 2:
			if (type.equals(Article.EVENT)) {
				articleQB.orderBy("start_date", true);
			} else if (type.equals(Article.STUDY)) {
				articleQB.orderByRaw("start_year desc, start_month desc");
			} else if (type.equals(Article.QA)) {
				articleQB.orderBy("year", true);
			} else {
				articleQB.orderBy("date", false);
			}
			break;
		case 3:
			articleQB.orderBy("created", false);
			break;
		case 4:
			articleQB.orderByRaw("institution ASC, lastname asc");
			break;
		case 5:
			if (type.equals(Article.EVENT)) {
				articleQB.orderBy("start_date", true);
			}
			break;
		case 6:
			articleQB.orderBy("author", true);
			break;
		default:
			/**
			 * if no sort parameter was selected, we take the default value from
			 * the shared preferences
			 */
			if (type.equals(Article.EXPERT)) {
				String sortValue = Util.getSortString(
						BasicConfigValue.SORT_EXPERT, context);
				boolean ascending = true;
				if (sortValue.equals("created")) {
					ascending = false;
				}

				articleQB.orderBy(sortValue, ascending);
			} else if (type.equals(Article.STUDY)) {
				String sortValue = Util.getSortString(
						BasicConfigValue.SORT_STUDY, context);
				boolean ascending = false;
				if (sortValue.equals("title")) {
					ascending = true;
				}
				articleQB.orderBy(sortValue, ascending);

			} else if (type.equals(Article.METHOD)) {
				String sortValue = Util.getSortString(
						BasicConfigValue.SORT_METHOD, context);
				boolean ascending = false;
				if (sortValue.equals("title")) {
					ascending = true;
				}
				articleQB.orderBy(sortValue, ascending);
			} else if (type.equals(Article.EVENT)) {
				articleQB.orderBy(Util.getSortString(
						BasicConfigValue.SORT_EVENT, context), true);
			} else if (type.equals(Article.NEWS)) {
				articleQB
						.orderBy(Util.getSortString(BasicConfigValue.SORT_NEWS,
								context), false);
			} else if (type.equals(Article.QA)) {
				String sortValue = Util.getSortString(BasicConfigValue.SORT_QA,
						context);
				boolean ascending = false;
				if (sortValue.equals("title")) {
					ascending = true;
				}
				articleQB.orderBy(sortValue, ascending);
			}
			break;
		}
	}
}
