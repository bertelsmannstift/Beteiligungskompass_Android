/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;

/**
 * this class include all database transaction for criteria
 */
public class CriterionManager extends AbstractManager {

	private static String logTag = CriterionManager.class.toString();

	public CriterionManager() {
	}

	/**
	 * 
	 * @param baseActivity
	 * @param type
	 * @param showInPlanner
	 * @return list of criteria
	 * @throws Exception
	 *             returns a list of criteria and fills their options member
	 */
	public static List<Criterion> getCriteria(DatabaseActivity baseActivity,
			String type, boolean showInPlanner) throws Exception {
		Dao<Criterion, Integer> criterionDao = baseActivity.helper
				.getDao(Criterion.class);

		QueryBuilder<Criterion, Integer> criterionBuilder = criterionDao
				.queryBuilder();

		Where<Criterion, Integer> where = criterionBuilder.where();
		where.eq("deleted", false);
		criterionBuilder.orderBy("orderindex", true);

		allCritera = criterionBuilder.query();
		return allCritera;

	}

	/**
	 * 
	 * @param type
	 * @param articleID
	 * @param dbActivity
	 * @return list of criteria
	 * @throws Exception
	 * 
	 *             returns a list of cirtiera based on the submitted article id
	 */
	public static List<Criterion> getCriteriaByArticle(String type,
			int articleID, DatabaseActivity dbActivity) throws Exception {
		Dao<Criterion, Integer> articleCritera = dbActivity.helper
				.getDao(Criterion.class);

		QueryBuilder<Criterion, Integer> builder = articleCritera
				.queryBuilder();

		builder.where().like("article_types", "%" + type + "%");

		List<Criterion> tmpCriteria = builder.query();

		for (Criterion criterion : tmpCriteria) {
			Dao<CriteriaOption, Integer> options = dbActivity.helper
					.getDao(CriteriaOption.class);

			QueryBuilder<CriteriaOption, Integer> qb = options.queryBuilder();

			Dao<ArticlesOption, Integer> artOptions = dbActivity.helper
					.getDao(ArticlesOption.class);

			QueryBuilder<ArticlesOption, Integer> artQB = artOptions
					.queryBuilder();

			artQB.selectColumns("model_criterion_option_id").where()
					.eq("model_article_id", articleID);

			qb.where().eq("criterion_id", criterion.id).and().in("id", artQB);

			criterion.visibleOptions = new ArrayList<CriteriaOption>();
			criterion.visibleOptions.addAll(qb.query());

		}

		return tmpCriteria;

	}

	public static List<Criterion> getCriteriaByType(String type)
			throws Exception {
		return getCriteriaByType(type, false, false, null);
	}

	public static List<Criterion> getCriteriaByType(String type,
			boolean filterOptions, DatabaseActivity db) throws Exception {
		return getCriteriaByType(type, false, true, db);
	}

	/**
	 * 
	 * @param type
	 * @param showInPlaner
	 * @param filterOptions
	 * @param db
	 * @return list of criteria
	 * @throws Exception
	 * 
	 *             get a list of all criteria. if showInPlaner is set to true,
	 *             only all criteria which the flag show_in_planer have the
	 *             value true are taken.
	 */
	public static List<Criterion> getCriteriaByType(String type,
			boolean showInPlaner, boolean filterOptions, DatabaseActivity db)
			throws Exception {

		if (allCritera == null) {
			allCritera = CriterionManager.getCriteria(db, "", true);
		}
		List<Criterion> filtered = new ArrayList<Criterion>();
		if (showInPlaner) {
			for (Criterion criterion : allCritera) {
				criterion.visibleOptions.clear();
				if (criterion.show_in_planner) {
					for (CriteriaOption option : criterion.options) {
						criterion.visibleOptions.add(option);
					}
					filtered.add(criterion);
				}
			}
		} else {
			for (Criterion criterion : allCritera) {

				if (filterOptions && criterion.article_types.contains(type)) {
					criterion.visibleOptions.clear();
				}
				if (criterion.article_types.contains(type)) {

					for (CriteriaOption option : criterion.options) {
						if (option.isArticleAddedToFilter) {
							option.isArticleAddedToFilter = false;
						}
						if (filterOptions) {
							if (option.hasChilds || option.default_value) {
								continue;
							}
							Dao<Article, Integer> articleDAO = db.helper
									.getDao(Article.class);

							QueryBuilder<Article, Integer> qb = articleDAO
									.queryBuilder();
							qb.setCountOf(true);
							Dao<ArticlesOption, Integer> artOptionDAO = db.helper
									.getDao(ArticlesOption.class);

							QueryBuilder<ArticlesOption, Integer> artQB = artOptionDAO
									.queryBuilder();

							artQB.selectColumns("model_article_id");
							artQB.where().in("model_criterion_option_id",
									option.id);

							Where<Article, Integer> where = qb.where();

							where.in("id", artQB).and().eq("type", type);

							if (type.equals("event")) {
								GregorianCalendar cal = new GregorianCalendar();

								java.sql.Date d = new java.sql.Date(
										cal.getTimeInMillis());
								where.and().ge("end_date", d);
							}

							if (articleDAO.countOf(qb.prepare()) > 0) {

								criterion.visibleOptions.add(option);
							}

						}
					}
					if (criterion.group_article_types != null
							&& criterion.group_article_types.contains(type)) {
						if (filteredCriterion == null) {
							filteredCriterion = new HashMap<String, Criterion>();
						}
						filteredCriterion.put(type, criterion);
					}
					filtered.add(criterion);
				}
			}
		}

		return filtered;
	}

	/**
	 * 
	 * @param criterion
	 * @param index
	 * @param activity
	 * @throws Exception
	 * 
	 *             if a "Resource" criterion filter was selected or deselected,
	 *             this method will called and set the selected state.
	 */
	public static void setSelectedResourceOption(Criterion criterion,
			int index, DatabaseActivity activity) throws Exception {
		for (int i = 0; i < criterion.visibleOptions.size(); i++) {
			CriteriaOption option = (CriteriaOption) criterion.options
					.toArray()[i];
			if (index == 0) {

				if (i == 0) {
					option.selected = true;
				} else {
					option.selected = false;
				}
			} else if (index == (criterion.visibleOptions.size() - 1)) {
				option.selected = true;
			} else {
				if (i <= index) {
					option.selected = true;
				} else {
					option.selected = false;
				}
			}

			criterion.options.update(option);

		}
		criterion.options.refreshAll();
		observable.notifyDatasetChanged();

	}

	/**
	 * 
	 * @param criterion
	 * @param index
	 * @param activity
	 * @return
	 * @throws Exception
	 *             set the selection sate from a criteria option
	 */
	public static List<CriteriaOption> setSelectedOption(Criterion criterion,
			int index, DatabaseActivity activity) throws Exception {

		int currentIndex = 0;
		for (CriteriaOption option : criterion.visibleOptions) {
			/*
			 * Keep track of whether we need to really change the DB at all
			 */
			boolean changed = false;
			if (currentIndex == index) {
				Util.logDebug(logTag, "setting selected to true: "
						+ option.title);

				changed = (null == option.selected)
						|| (false == option.selected);

				option.selected = true;

			} else {
				changed = (null == option.selected)
						|| (true == option.selected);

				option.selected = false;

			}

			if (true == changed) {
				Util.logDebug(logTag, "selection actually did change");
				criterion.options.update(option);
			}
			++currentIndex;
		}

		observable.notifyDatasetChanged();
		return criterion.visibleOptions;
	}

	/**
	 * 
	 * @param criterion
	 * @param index
	 * @param activity
	 * @return
	 * @throws Exception
	 *             set the selection sate from a criteria option
	 */
	public static CriteriaOption setSelectedOption(CriteriaOption option,
			boolean isChecked, DatabaseActivity activity) throws Exception {

		Dao<CriteriaOption, Integer> dao = activity.helper
				.getDao(CriteriaOption.class);

		option.selected = isChecked;
		CreateOrUpdateStatus status = dao.createOrUpdate(option);

		Util.logDebug(logTag,
				"status: " + status.isUpdated() + " " + status.isCreated());

		observable.notifyDatasetChanged();
		return option;
	}

	public static String getCriterionDescription(Criterion criterion) {
		if (null != criterion.description
				&& false == criterion.description.equals("")) {
			return criterion.description;
		}

		return criterion.title;
	}

}
