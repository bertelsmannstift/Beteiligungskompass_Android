/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.ArticleLink;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.FavoriteArticle;

/**
 * 
 * this class include all database transaction for articles
 */
public class ArticleManager extends AbstractManager {

	public ArticleManager() {}

	private static String logTag = ArticleManager.class.toString();

	/**
	 * 
	 * @param db
	 * @return list of locale article
	 * @throws Exception
	 * 
	 *             Returns all articles which were not synced yet and only on
	 *             the app are available
	 */
	public static List<Article> getLocalArticles(DatabaseActivity db)
			throws Exception {
		List<Article> localeArticles;

		Dao<Article, Integer> artDAO = db.helper.getDao(Article.class);

		localeArticles = artDAO.queryForEq("locale", true);

		return localeArticles;
	}

	/**
	 * 
	 * @param dbActivity
	 * @return list of article which contains videos
	 * @throws Exception
	 */
	private static List<Article> getVideoArticles(DatabaseActivity dbActivity)
			throws Exception {
		Dao<Article, Integer> articleDao = dbActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> articleQB = articleDao.queryBuilder();
		articleQB.where().eq("deleted", false).and().eq("active", true).and()
				.ne("type", "").and().isNotNull("videos");

		return articleQB.query();
	}

	/**
	 * 
	 * @param dbActivity
	 * @param artID
	 * @param option
	 * @throws Exception
	 * 
	 *             Add or remove an article to or from one criterion option.
	 *             this method will be used in the add article process
	 */
	public static void addOrRemoveOneArticleToOption(
			DatabaseActivity dbActivity, int artID, CriteriaOption option)
			throws Exception {
		Dao<ArticlesOption, Integer> dao = dbActivity.helper
				.getDao(ArticlesOption.class);

		if (option.isArticleAddedToFilter) {
			ArticlesOption artOption = new ArticlesOption();
			artOption.model_article_id = artID;
			artOption.model_criterion_option_id = option.id;

			dao.create(artOption);
		} else {
			DeleteBuilder<ArticlesOption, Integer> qb = dao.deleteBuilder();

			qb.where().eq("model_article_id", artID).and()
					.eq("model_criterion_option_id", option.id);

			dao.delete(qb.prepare());
		}

	}

	/**
	 * 
	 * @param dbActivity
	 * @param artID
	 * @param criterion
	 * @throws Exception
	 * 
	 *             add an Article to all options in a criterion if the article
	 *             is added to the selected filter
	 */
	public static void addArticleToOptions(DatabaseActivity dbActivity,
			int artID, Criterion criterion) throws Exception {
		Dao<ArticlesOption, Integer> dao = dbActivity.helper
				.getDao(ArticlesOption.class);

		for (CriteriaOption criteriaOption : criterion.options) {
			if (criteriaOption.isArticleAddedToFilter) {

				ArticlesOption option = new ArticlesOption();
				option.model_article_id = artID;
				option.model_criterion_option_id = criteriaOption.id;

				dao.create(option);

			} else {
				DeleteBuilder<ArticlesOption, Integer> qb = dao.deleteBuilder();
				SelectArg arg = new SelectArg();
				arg.setValue(artID);
				SelectArg arg_option_id = new SelectArg();
				arg_option_id.setValue(criteriaOption.id);
				qb.where().eq("model_article_id", arg).and()
						.eq("model_criterion_option_id", arg_option_id);
				try {
					dao.delete(qb.prepare());
				} catch (Exception e) {

				}
			}
		}

	}

	/**
	 * 
	 * @param dbActivity
	 * @return
	 * @throws Exception
	 * 
	 *             counts all available articles in the app under consideration
	 *             of the visible module sections
	 */
	public static int countAllArticles(DatabaseActivity dbActivity)
			throws Exception {
		Dao<Article, Integer> articleDAO = dbActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> articleQB = articleDAO.queryBuilder();
		articleQB.setCountOf(true);
		articleQB.selectColumns("id");
		Where<Article, Integer> where = articleQB.where();
		where.eq("deleted", false).eq("active", true);

		setDateRange(where);

		where.notIn("type", Util.getNotShownModules(dbActivity));
		where.and(5);
		Log.d("TAG", where.getStatement());
		return (int) articleDAO.countOf(articleQB.prepare());
	}

	/**
	 * 
	 * @param type
	 * @param dbActivity
	 * @return Integer
	 * @throws Exception
	 * 
	 *             counts all articles by the special article type like 'event',
	 *             'expert', 'method', 'study', 'qa', 'news'. These types are
	 *             defined in Article.java
	 */
	public static int countArticlesByType(String type,
			DatabaseActivity dbActivity) throws Exception {
		Dao<Article, Integer> articleDAO = dbActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> articleQB = articleDAO.queryBuilder();
		articleQB.setCountOf(true);
		articleQB.selectColumns("id");
		Where<Article, Integer> where = articleQB.where();
		where.eq("type", type).eq("deleted", false).eq("active", true);

		setDateRange(where);

		where.and(5);
		Log.d(logTag, where.getStatement());

		return (int) articleDAO.countOf(articleQB.prepare());
	}

	/**
	 * 
	 * @param type
	 * @param dbActivity
	 * @return counts all articles which matched to the filter parameter
	 * @throws Exception
	 */
	public static int countArticleByFilter(String type,
			DatabaseActivity dbActivity) throws Exception {
		return countArticleByFilter(type, dbActivity, false, false);
	}

	/**
	 * 
	 * @param type
	 * @param dbActivity
	 * @return counts all articles which matched to the filter parameter
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static int countArticleByFilter(String type,
			DatabaseActivity dbActivity, boolean alwaysCount,
			boolean showInPlanner) throws Exception {

		List<Criterion> tmpCriteria = CriterionManager.getCriteriaByType(type);

		if (dbActivity.helper == null) {
			dbActivity.helper = OpenHelperManager.getHelper(dbActivity,
					DatabaseHelper.class);
		}
		Dao<Article, Integer> articleDAO = dbActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> articleQB = articleDAO.queryBuilder();
		articleQB.setCountOf(true);
		articleQB.selectColumns("id");
		Where<Article, Integer> where = articleQB.where();

		Dao<ArticlesOption, Integer> articleOptionDAO = dbActivity.helper
				.getDao(ArticlesOption.class);

		boolean selectionFound = false;

		int amountAnd = 0;

		for (Criterion criterion : tmpCriteria) {
			List<Integer> optionIds = new ArrayList<Integer>();
			for (CriteriaOption articlesOption : criterion.options) {
				if (articlesOption.selected != null && articlesOption.selected
						&& articlesOption.default_value != null
						&& articlesOption.default_value == false) {
					optionIds.add(articlesOption.id);
				}
			}

			if (optionIds.size() == 0) {
				continue;
			} else {
				Util.logDebug(logTag, "tmpSize != 0");
				selectionFound = true;
				filterCleared = false;
			}

			QueryBuilder<ArticlesOption, Integer> artOptionQB = articleOptionDAO
					.queryBuilder();

			Where<ArticlesOption, Integer> completeStmt = artOptionQB.where();

			/*
			 * if the criterion have to filter with an or statement or the type
			 * is "resource" like Spinner
			 */
			if (criterion.filter_type_or
					|| criterion.type.equals(Criterion.CRITERIA_TYPE_RESOURCE)) {
				completeStmt.eq("model_article_id", "articles.id").and()
						.in("model_criterion_option_id", optionIds);

				where.exists(artOptionQB);
				amountAnd++;
				/*
				 * In all other cases, we have to select over all cirteria
				 * Options
				 */
			} else {
				for (int id : optionIds) {
					artOptionQB.where().eq("model_article_id", "articles.id")
							.and().eq("model_criterion_option_id", id);
					where.exists(artOptionQB);
					amountAnd++;
				}
			}
		}

		Util.logDebug(logTag, "selectionFound: " + selectionFound
				+ " filterReset: " + filterReset);

		if ((selectionFound || searchText.length() > 0) || alwaysCount) {

			where.like("type", "%" + type + "%").eq("active", true);
			amountAnd++;
			amountAnd++;
			where.or(where.like("title", "%" + searchText + "%"),
					where.like("short_description", "%" + searchText + "%"));

			setDateRange(where);
			amountAnd++;
			amountAnd++;

			where.and((amountAnd + 1));
			Util.logDebug(logTag, where.getStatement());
			return (int) articleDAO.countOf(articleQB.prepare());

		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param type
	 * @param dbActivity
	 * @return filtered articles
	 * @throws Exception
	 * 
	 * this method filtered all article based on the type like 'expert' or 'event' and only get the articles which are based on the filter list.
	 * Beware! all event articles have no criterion like "September" or "March". That's the reason why we have to create on criteria options for it.
	 */
	private static List<Article> filterArticleByType(String type,
			DatabaseActivity dbActivity) throws Exception {
		List<Article> filteredArticles = new ArrayList<Article>();
		if (filteredCriterion == null) {
			filteredCriterion = new HashMap<String, Criterion>();
		}
		SharedPreferences pref = dbActivity.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);
		List<Criterion> criteriaType = CriterionManager.getCriteriaByType(type);

		Dao<Article, Integer> articleDAO = dbActivity.helper
				.getDao(Article.class);
		QueryBuilder<Article, Integer> articleQB = articleDAO.queryBuilder();

		Where<Article, Integer> where = articleQB.where();

		Dao<ArticlesOption, Integer> articleOptionDAO = dbActivity.helper
				.getDao(ArticlesOption.class);

		boolean selectionFound = false;
		int amountAnd = 0;
		if (type.equals(Article.EVENT)) {
			if (filteredCriterion.get(type) != null) {
				filteredCriterion.remove(type);
			}
			Criterion mCriterion = new Criterion();
			mCriterion.filter_type_or = false;

			String groupText = "";

			for (Criterion criterion : criteriaType) {

				for (CriteriaOption option : criterion.options) {
					if (option.selected != null && option.selected
							&& option.default_value != null
							&& option.default_value == false) {
						selectionFound = true;
						QueryBuilder<ArticlesOption, Integer> artOptionQB = articleOptionDAO
								.queryBuilder();

						artOptionQB.where()
								.eq("model_article_id", "articles.id").and()
								.eq("model_criterion_option_id", option.id);

						where.exists(artOptionQB);

						amountAnd++;
					}
				}
			}

			if (selectionFound || searchText.length() > 0) {
				filteredCriterion.put(type, mCriterion);
				where.like("type", "%" + type + "%").eq("active", true);
				amountAnd++;
				amountAnd++;
				where.or(where.like("title", "%" + searchText + "%"),
						where.like("short_description", "%" + searchText + "%"));

				GregorianCalendar cal = new GregorianCalendar();

				java.sql.Date d = new java.sql.Date(cal.getTimeInMillis());
				where.ge("end_date", d);
				amountAnd++;
				where.and((amountAnd + 1));
				Util.logDebug("STATEMENT", where.getStatement());

				sortBy(articleQB, type, dbActivity);

				filteredArticles = articleQB.query();
			}

			for (Article article : filteredArticles) {
				if (Util.isUserAuthentificated(dbActivity)) {
					article.isFavorit = isArticleFavorit(article.id, dbActivity);
					if (article.user.id.equals(pref.getInt("userID", -1))) {
						article.is_mine_article = true;
					}
				}
				Calendar cal = new GregorianCalendar();
				Date date = article.start_date;
				cal.setTime(date);

				String dateString = "";

				dateString = Util.getMonth(cal.get(Calendar.MONTH)) + " "
						+ cal.get(Calendar.YEAR);
				if (!groupText.equals(dateString)) {
					CriteriaOption option = new CriteriaOption();
					groupText = dateString;
					mCriterion.options.add(option);
					option.articles = new ArrayList<Article>();
					option.title = dateString;
					option.articles.add(article);
				} else {
					((CriteriaOption) mCriterion.options.toArray()[((mCriterion.options
							.size() - 1))]).articles.add(article);
				}
			}
			if (sortBy == 5) {
				for (CriteriaOption opt : mCriterion.options) {
					Collections.sort(opt.articles);
				}
			}

		} else {
			ArrayList<Integer> selectedFilterIDs = new ArrayList<Integer>();
			for (Criterion criterion : criteriaType) {
				selectedFilterIDs.clear();
				for (CriteriaOption articlesOption : criterion.options) {
					if (articlesOption.selected != null
							&& articlesOption.selected
							&& articlesOption.default_value != null
							&& articlesOption.default_value == false) {
						selectionFound = true;
						selectedFilterIDs.add(articlesOption.id);
					}
				}

				if (selectedFilterIDs.size() == 0) {
					if (criterion.group_article_types != null
							&& criterion.group_article_types.contains(type)) {
						if (filteredCriterion.get(type) == null)
							filteredCriterion.put(type, criterion);
					}
					continue;
				}

				Object[] ids = selectedFilterIDs.toArray();
				QueryBuilder<ArticlesOption, Integer> artOptionQB = articleOptionDAO
						.queryBuilder();
				if (criterion.type.equals(Criterion.CRITERIA_TYPE_RESOURCE)
						|| criterion.filter_type_or) {
					Where<ArticlesOption, Integer> completeStmt = artOptionQB
							.where();

					completeStmt.eq("model_article_id", "articles.id").and()
							.in("model_criterion_option_id", ids);
					where.exists(artOptionQB);
					amountAnd++;

				} else {
					for (Object id : ids) {
						artOptionQB.where()
								.eq("model_article_id", "articles.id").and()
								.eq("model_criterion_option_id", id);

						where.exists(artOptionQB);
						amountAnd++;
					}
				}
			}
			if (selectionFound || searchText.length() > 0) {

				where.like("type", "%" + type + "%").eq("active", true);
				amountAnd++;
				amountAnd++;
				where.or(where.like("title", "%" + searchText + "%"),
						where.like("short_description", "%" + searchText + "%"));
				setDateRange(where);
				// setDateRange includes two where clauses, that the reason why
				// we count the amountAnd variable
				amountAnd++;
				amountAnd++;
				where.and((amountAnd + 1));
				Util.logDebug("STATEMENT", where.getStatement());

				sortBy(articleQB, type, dbActivity);

				filteredArticles = articleQB.query();

				if (filteredCriterion != null
						&& filteredCriterion.get(type) != null) {

					Criterion crit = filteredCriterion.get(type);
					for (CriteriaOption option : crit.visibleOptions) {
						option.articles = (ArrayList<Article>) getArticlesInCriterion(
								dbActivity, option.id, type);
						ArrayList<Article> tmpArticles = new ArrayList<Article>();
						for (Article article : option.articles) {
							for (Article art : filteredArticles) {
								if (article.id.equals(art.id)) {
									if (Util.isUserAuthentificated(dbActivity)) {
										article.isFavorit = isArticleFavorit(
												article.id, dbActivity);
										art.isFavorit = article.isFavorit;
									}
									if (article.user.id.equals(pref.getInt(
											"userID", -1))) {
										article.is_mine_article = true;
										art.is_mine_article = true;
									}
									tmpArticles.add(article);
								}
							}
						}
						if (tmpArticles.size() > 0) {
							option.articles = tmpArticles;
							if (sortBy == 5) {
								Collections.sort(option.articles);
								Collections.sort(filteredArticles);
							}
						} else {
							option.articles = tmpArticles;
						}
					}
					filteredCriterion.put(type, crit);
				}
			}
		}

		return filteredArticles;

	}

	/**
	 * 
	 * @param id
	 * @param baseActivity
	 * @return boolean
	 * @throws Exception
	 * 
	 *             Check if the article is a favorite article of the current
	 *             user
	 */
	public static boolean isArticleFavorit(int id, DatabaseActivity baseActivity)
			throws Exception {
		Dao<FavoriteArticle, Integer> favoritDao = baseActivity.helper
				.getDao(FavoriteArticle.class);

		QueryBuilder<FavoriteArticle, Integer> favroitBuilder = favoritDao
				.queryBuilder();
		favroitBuilder.setCountOf(true);
		favroitBuilder
				.where()
				.eq("article_id", id)
				.and()
				.eq("user_id",
						baseActivity.getSharedPreferences("LOGIN",
								Context.MODE_PRIVATE).getInt("userID", -1));
		int value = (int) favoritDao.countOf(favroitBuilder.prepare());

		if (value > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static void callArticlesByFilter(DatabaseActivity dbActivity,
			String type) throws Exception {
		callArticlesByFilter(dbActivity, type, false);
	}

	/**
	 * 
	 * @param dbActivity
	 * @return
	 * @throws SQLException
	 * 
	 *             counts all selected criterion options
	 */
	private static long countSelectedCriterionOptions(
			DatabaseActivity dbActivity) throws SQLException {
		Dao<CriteriaOption, Integer> options = dbActivity.helper
				.getDao(CriteriaOption.class);

		QueryBuilder<CriteriaOption, Integer> optionQB = options.queryBuilder();

		optionQB.selectColumns("selected");
		optionQB.setCountOf(true);

		optionQB.where().eq("selected", true).and().ne("default_value", true);

		long amountSelectedOptions = options.countOf(optionQB.prepare());

		return amountSelectedOptions;
	}

	public static List<Article> callArticlesByFilter(
			DatabaseActivity dbActivity, String type, boolean firstLoad)
			throws Exception {
		long amountSelectedOptions = countSelectedCriterionOptions(dbActivity);

		if (amountSelectedOptions > 0 || !searchText.equals("")) {
			filterReset = false;
			filterCleared = false;

			return filterArticleByType(type, dbActivity);

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param baseActivity
	 * @param criterionID
	 * @param type
	 * @return List of articles
	 * @throws Exception
	 * 
	 * get all articles which are in no criterion
	 */
	public static List<Article> getArticlesNotInCriterion(
			DatabaseActivity baseActivity, int criterionID, String type)
			throws Exception {
		List<Article> selectedArticles = new ArrayList<Article>();

		Dao<CriteriaOption, Integer> optins = baseActivity.helper
				.getDao(CriteriaOption.class);
		QueryBuilder<CriteriaOption, Integer> qb = optins.queryBuilder();

		qb.selectColumns("id").where().eq("criterion_id", criterionID);

		Dao<ArticlesOption, Integer> optionOptionDao = baseActivity.helper
				.getDao(ArticlesOption.class);

		QueryBuilder<ArticlesOption, Integer> builder = optionOptionDao
				.queryBuilder();
		builder.selectColumns("model_article_id").where()
				.in("model_criterion_option_id", qb);

		Dao<Article, Integer> filteredArticleDao = baseActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> filteredArticle = filteredArticleDao
				.queryBuilder();

		Where<Article, Integer> where = filteredArticle.where();

		where.notIn("id", builder).eq("type", type).eq("active", true);
		where.or(where.like("title", "%" + searchText + "%"),
				where.like("short_description", "%" + searchText + "%"));
		int and = 4;

		setDateRange(where);
		// setDateRange includes two where clauses, that the reason why
		// we count the amountAnd variable
		and++;
		and++;

		where.and(and);
		sortBy(filteredArticle, type, baseActivity);

		selectedArticles = filteredArticle.query();
		SharedPreferences pref = baseActivity.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);
		for (Article article : selectedArticles) {
			if (Util.isUserAuthentificated(baseActivity)) {
				article.isFavorit = isArticleFavorit(article.id, baseActivity);
				if (article.user.id.equals(pref.getInt("userID", -1))) {
					article.is_mine_article = true;
				}
			}
		}

		if (!type.equals(Article.EVENT)) {
			if (sortBy == 5) {
				Collections.sort(selectedArticles);
			}
		}
		return selectedArticles;
	}

	/**
	 * 
	 * @param ids
	 * @param baseActivity
	 * @param type
	 * @return List of articles
	 * @throws SQLException
	 * 
	 *             Returns the list of the articles based on the ids
	 */
	public static List<Article> getArticlesByIds(ArrayList<Integer> ids,
			DatabaseActivity baseActivity, String type) throws SQLException {

		Dao<Article, Integer> articleDAO;
		articleDAO = baseActivity.helper.getDao(Article.class);

		QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

		artQB.where().in("id", ids);

		sortBy(artQB, type, baseActivity);

		ArrayList<Article> navbarArticles = (ArrayList<Article>) articleDAO
				.query(artQB.prepare());

		if (ArticleManager.sortBy == 5) {
			Collections.sort(navbarArticles);
		}

		return navbarArticles;
	}

	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param baseActivity
	 * @param criterionID
	 * @param type
	 * @return list of articles
	 * @throws Exception
	 * 
	 * get all articles which are in the submitted criterionID
	 */
	public static List<Article> getArticlesInCriterion(
			DatabaseActivity baseActivity, int criterionID, String type)
			throws Exception {

		List<Article> selectedArticles = new ArrayList<Article>();

		Dao<ArticlesOption, Integer> optionOptionDao = baseActivity.helper
				.getDao(ArticlesOption.class);

		QueryBuilder<ArticlesOption, Integer> builder = optionOptionDao
				.queryBuilder();
		builder.selectColumns("model_article_id").where()
				.eq("model_criterion_option_id", criterionID);

		Dao<Article, Integer> filteredArticleDao = baseActivity.helper
				.getDao(Article.class);

		QueryBuilder<Article, Integer> filteredArticle = filteredArticleDao
				.queryBuilder();

		Where<Article, Integer> where = filteredArticle.where();

		where.in("id", builder).eq("type", type).eq("active", true);
		where.or(where.like("title", "%" + searchText + "%"),
				where.like("short_description", "%" + searchText + "%"));

		int and = 4;

		setDateRange(where);
		// setDateRange includes two where clauses, that the reason why
		// we count the amountAnd variable
		and++;
		and++;

		where.and(and);
		sortBy(filteredArticle, type, baseActivity);

		selectedArticles = filteredArticle.query();
		SharedPreferences pref = baseActivity.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);
		for (Article article : selectedArticles) {
			if (Util.isUserAuthentificated(baseActivity)) {
				article.isFavorit = isArticleFavorit(article.id, baseActivity);
				if (article.user.id.equals(pref.getInt("userID", -1))) {
					article.is_mine_article = true;
				}
			}
		}

		if (!type.equals(Article.EVENT)) {
			if (sortBy == 5) {
				Collections.sort(selectedArticles);
			}
		}
		return selectedArticles;
	}

	/**
	 * 
	 * @param baseActivity
	 * @return list of Event article
	 * 
	 *         this method is only needed in the dashboard section, cause we
	 *         need for the news_event fragment the last two events
	 */
	public static List<Article> getNewestEvents(DatabaseActivity baseActivity) {

		List<Article> tmpArticles = new ArrayList<Article>();
		try {
			Dao<Article, Integer> articleDao = baseActivity.helper
					.getDao(Article.class);

			QueryBuilder<Article, Integer> articleQB = articleDao
					.queryBuilder();

			Where<Article, Integer> articleWhere = articleQB.where();
			articleWhere.eq("deleted", false).eq("active", true)
					.eq("type", "event");

			int and = 3;
			setDateRange(articleWhere);
			and++;
			and++;
			articleWhere.and(and);
			sortBy(articleQB, "event", baseActivity);
			articleQB.limit(2L);

			List<Article> articles = articleQB.query();
			SharedPreferences pref = baseActivity.getSharedPreferences("LOGIN",
					Activity.MODE_PRIVATE);
			for (Article article : articles) {
				if (Util.isUserAuthentificated(baseActivity)) {
					try {
						article.isFavorit = isArticleFavorit(article.id,
								baseActivity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (article.user.id.equals(pref.getInt("userID", -1))) {
					article.is_mine_article = true;
				}
				tmpArticles.add(article);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tmpArticles;

	}

	/**
	 * 
	 * @param baseActivity
	 * @return list of News article
	 * 
	 *         this method is only needed in the dashboard section, cause we
	 *         need for the news_event fragment the last two news
	 */
	public static List<Article> getNewestNews(DatabaseActivity baseActivity) {

		List<Article> tmpArticles = new ArrayList<Article>();
		try {
			Dao<Article, Integer> articleDao = baseActivity.helper
					.getDao(Article.class);

			QueryBuilder<Article, Integer> articleQB = articleDao
					.queryBuilder();

			Where<Article, Integer> articleWhere = articleQB.where();
			articleWhere.eq("deleted", false).eq("active", true)
					.eq("type", "news");

			int and = 3;

			articleWhere.and(and);
			sortBy(articleQB, "news", baseActivity);
			articleQB.limit(2L);

			List<Article> articles = articleQB.query();
			SharedPreferences pref = baseActivity.getSharedPreferences("LOGIN",
					Activity.MODE_PRIVATE);
			for (Article article : articles) {
				if (Util.isUserAuthentificated(baseActivity)) {
					try {
						article.isFavorit = isArticleFavorit(article.id,
								baseActivity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (article.user.id.equals(pref.getInt("userID", -1))) {
					article.is_mine_article = true;
				}
				tmpArticles.add(article);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tmpArticles;

	}

	@SuppressWarnings("unchecked")
	public static List<Article> getArticles(DatabaseActivity baseActivity,
			String filter) throws Exception {
		Dao<Article, Integer> articleDao = baseActivity.helper
				.getDao(Article.class);

		List<Article> tmpArticles = new ArrayList<Article>();

		QueryBuilder<Article, Integer> articleQB = articleDao.queryBuilder();

		Where<Article, Integer> articleWhere = articleQB.where();
		articleWhere.eq("deleted", false).eq("active", true).eq("type", filter);
		articleWhere.or(articleWhere.like("title", "%" + searchText + "%"),
				articleWhere.like("short_description", "%" + searchText + "%"));

		int and = 4;

		setDateRange(articleWhere);
		and++;
		and++;

		articleWhere.and(and);
		sortBy(articleQB, filter, baseActivity);

		List<Article> articles = articleQB.query();
		SharedPreferences pref = baseActivity.getSharedPreferences("LOGIN",
				Activity.MODE_PRIVATE);
		for (Article article : articles) {
			if (Util.isUserAuthentificated(baseActivity)) {
				article.isFavorit = isArticleFavorit(article.id, baseActivity);
			}
			if (article.user.id.equals(pref.getInt("userID", -1))) {
				article.is_mine_article = true;
			}
			tmpArticles.add(article);
		}

		if (sortBy == 5 && !filter.equals(Article.EVENT)) {
			Collections.sort(tmpArticles);
		}

		return tmpArticles;
	}

	public static List<Integer> getArticleIdsByType(String type,
			DatabaseActivity db) throws Exception {

		Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

		List<Article> arts = articleDAO.queryBuilder().selectColumns("id")
				.where().eq("type", type).and().eq("deleted", false).query();

		List<Integer> ids = new ArrayList<Integer>();

		for (Article article : arts) {
			ids.add(article.id);
		}

		return ids;
	}

	public static List<Article> getFilteredArticles(String type,
			DatabaseActivity activity) throws Exception {

		getArticles(activity, type);

		throw new Exception("the type argument does not name a valid type: "
				+ type);
	}

	public static void dumpCreateStatements(DatabaseActivity baseActivity) {
		baseActivity.helper.dumpCreateStatements();
	}

	/**
	 * 
	 * @param activity
	 * @return list of featured videos
	 * @throws Exception
	 * 
	 *             this method extracts all articles with videos and build a
	 *             list of FeatureVideo Objects. These objects are needed in the
	 *             VideoPlayer fragment
	 */
	public static List<FeaturedVideo> getFeaturedVideos(
			DatabaseActivity activity) throws Exception {

		if (null != featuredVideos) {
			return featuredVideos;
		}

		featuredVideos = new ArrayList<FeaturedVideo>();

		for (Article article : getVideoArticles(activity)) {
			if (null != article.videos) {
				Article.ArticleBase articleBase = article
						.createSubType(activity);

				JSONArray jsonArray = new JSONArray(article.videos);

				for (int index = 0; index < jsonArray.length(); ++index) {
					JSONObject jsonObject = jsonArray.getJSONObject(index);
					if (jsonObject.has("featured")
							&& jsonObject.getBoolean("featured")
							&& jsonObject.has("url")) {

						FeaturedVideo featuredVideo = new FeaturedVideo();
						featuredVideo.videoURL = jsonObject.getString("url");
						featuredVideo.article = articleBase;
						featuredVideos.add(featuredVideo);
					}
				}
			}
		}

		return featuredVideos;
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return amount of my created articles
	 * @throws Exception
	 */
	public static int countAllMyArticles(int userID, DatabaseActivity db)
			throws Exception {

		Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

		QueryBuilder<Article, Integer> qb = articleDAO.queryBuilder();
		qb.setCountOf(true);
		Where<Article, Integer> where = qb.where();
		where.eq("user_id", userID).and().eq("deleted", false);
		return (int) articleDAO.countOf(qb.prepare());
	}

	public static List<Article> getLinkedArticles(int articleID,
			DatabaseActivity db) {
		Dao<Article, Integer> articlesDAO;
		List<Article> articleLinks = new ArrayList<Article>();
		try {
			articlesDAO = db.helper.getDao(Article.class);

			Dao<ArticleLink, Integer> articleLinkDao = db.helper
					.getDao(ArticleLink.class);

			QueryBuilder<ArticleLink, Integer> qb = articleLinkDao
					.queryBuilder();
			qb.selectColumns("article_linked_id").where()
					.eq("article_id", articleID);

			articleLinks = articlesDAO.queryBuilder().where().in("id", qb)
					.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return articleLinks;
	}

	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param where
	 * @throws SQLException
	 * 
	 * set the range of the news and event dates. If you want to see all news, you have to change the filter. Default is all news since 30 days
	 */
	private static void setDateRange(Where<Article, Integer> where)
			throws SQLException {
		GregorianCalendar cal = new GregorianCalendar();

		java.sql.Date d = new java.sql.Date(cal.getTimeInMillis());

		where.or(where.ge("end_date", d), where.ne("type", Article.EVENT));

		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				(cal.get(Calendar.DAY_OF_MONTH) - 30));
		d = new java.sql.Date(cal.getTimeInMillis());

		where.or(where.ge("date", d), where.isNull("date"));
	}

}
