/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Criterion;
import com.your.name.dao.FavoriteArticle;
import com.your.name.dao.FavoriteGroup;
import com.your.name.dao.FavoritesGroup;

/**
 * 
 * This class includes all database transactions for favorite Articles
 */
public class FavoritManager extends AbstractManager {

	public static boolean showOnlyMineInFavorites = false;
	public static int groupToShowInFavorites = -1;
	public static boolean showUnassignedInFavorites = false;
	public static boolean showOnlyLocaleArticle = false;

	public FavoritManager() {

	}

	/**
	 * 
	 * @param id
	 * @param group
	 * @param dbActivity
	 * @return FavoriteGroup
	 * @throws Exception
	 * 
	 *             this method add an article to a favorite group
	 */
	public static FavoritesGroup addArticleToGroup(int id, FavoriteGroup group,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoriteArticle, Integer> articleDAO = dbActivity.helper
				.getDao(FavoriteArticle.class);

		QueryBuilder<FavoriteArticle, Integer> artQB = articleDAO
				.queryBuilder();

		SharedPreferences pref = dbActivity.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);

		artQB.selectColumns("id").where().eq("article_id", id).and()
				.eq("user_id", pref.getInt("userID", -1));

		FavoriteArticle art = articleDAO.queryForFirst(artQB.prepare());

		if (art == null || art.id == null) {

			art = new FavoriteArticle();
			art.article_id = id;
			art.user_id = pref.getInt("userID", -1);
			articleDAO.create(art);

			art = articleDAO.queryBuilder().selectColumns("id").where()
					.eq("article_id", id).and().eq("user_id", art.user_id)
					.query().get(0);
		}

		Dao<FavoritesGroup, Integer> favgroups = dbActivity.helper
				.getDao(FavoritesGroup.class);

		FavoritesGroup groups = new FavoritesGroup();

		groups.favorite_id = art.id;
		groups.favoritegroup_id = (group.extern_id == 0) ? group.id
				: group.extern_id;

		favgroups.create(groups);

		return groups;

	}

	/**
	 * 
	 * @param articleID
	 * @param group
	 * @param dbActivity
	 * @return true / false
	 * @throws Exception
	 * 
	 *             removes an article from a favorite group
	 */
	public static boolean removeArticleFromGroup(int articleID,
			FavoriteGroup group, DatabaseActivity dbActivity) throws Exception {

		Dao<FavoriteArticle, Integer> favArticle = dbActivity.helper
				.getDao(FavoriteArticle.class);

		QueryBuilder<FavoriteArticle, Integer> favArtQB = favArticle
				.queryBuilder();

		SharedPreferences pref = dbActivity.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);

		favArtQB.where().eq("article_id", articleID).and()
				.eq("user_id", pref.getInt("userID", -1));

		FavoriteArticle art = favArticle.queryForFirst(favArtQB.prepare());

		if (art != null) {
			Dao<FavoritesGroup, Integer> groups = dbActivity.helper
					.getDao(FavoritesGroup.class);

			DeleteBuilder<FavoritesGroup, Integer> delete = groups
					.deleteBuilder();

			delete.where()
					.eq("favorite_id", art.id)
					.and()
					.eq("favoritegroup_id",
							(group.id == 0) ? group.id : group.extern_id);

			groups.delete(delete.prepare());
		}

		return true;
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return
	 * @throws Exception
	 * 
	 *             counts all article which are in a favorite group
	 */
	public static int countAllArticlesWithGroup(int userID, DatabaseActivity db)
			throws Exception {
		if (!showOnlyMineInFavorites) {
			Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

			QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

			artQB.setCountOf(true);

			Dao<FavoriteArticle, Integer> favArticles = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> favArticleQB = favArticles
					.queryBuilder();

			favArticleQB.selectColumns("article_id");

			favArticleQB.where().eq("user_id", userID);

			artQB.where().in("id", favArticleQB).and().ne("user_id", userID)
					.and().notIn("type", Util.getNotShownModules(db));

			return (int) articleDAO.countOf(artQB.prepare());

		} else {
			Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

			QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

			artQB.setCountOf(true);

			Dao<FavoriteArticle, Integer> favArticles = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> favArticleQB = favArticles
					.queryBuilder();

			favArticleQB.selectColumns("article_id");

			artQB.where().eq("user_id", userID);

			return (int) articleDAO.countOf(artQB.prepare());
		}
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return
	 * @throws Exception
	 * 
	 *             counts all articles which are created but not synced with the
	 *             server
	 */
	public static int countAllLocaleArticles(int userID, DatabaseActivity db)
			throws Exception {
		Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

		QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

		artQB.setCountOf(true);

		artQB.where().eq("user_id", userID).and().eq("locale", true);

		return (int) articleDAO.countOf(artQB.prepare());
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return
	 * @throws Exception
	 * 
	 *             counts all articles which are in no favorite groups
	 */
	public static int countAllUnassignedArticles(int userID, DatabaseActivity db)
			throws Exception {

		if (!showOnlyMineInFavorites) {
			Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

			QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

			artQB.setCountOf(true);

			Dao<FavoriteArticle, Integer> favArticles = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> favArticleQB = favArticles
					.queryBuilder();

			favArticleQB.selectColumns("article_id");

			Dao<FavoritesGroup, Integer> favGroupsDAO = db.helper
					.getDao(FavoritesGroup.class);
			QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favGroupsDAO
					.queryBuilder();

			Dao<FavoriteGroup, Integer> favGroup = db.helper
					.getDao(FavoriteGroup.class);

			QueryBuilder<FavoriteGroup, Integer> favGroupQB = favGroup
					.queryBuilder();

			favGroupQB.selectColumns("extern_id");

			favGroupsQB.where().in("favoritegroup_id", favGroupQB);

			favGroupsQB.selectColumns("favorite_id");
			favArticleQB.where().notIn("id", favGroupsQB).and()
					.eq("user_id", userID);

			artQB.where().in("id", favArticleQB).and().ne("user_id", userID)
					.and().notIn("type", Util.getNotShownModules(db));

			return (int) articleDAO.countOf(artQB.prepare());

		} else {
			Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

			QueryBuilder<Article, Integer> artQB = articleDAO.queryBuilder();

			artQB.setCountOf(true);

			Dao<FavoriteArticle, Integer> favArticles = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> favArticleQB = favArticles
					.queryBuilder();

			favArticleQB.selectColumns("article_id");

			Dao<FavoritesGroup, Integer> favGroupsDAO = db.helper
					.getDao(FavoritesGroup.class);
			QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favGroupsDAO
					.queryBuilder();
			favGroupsQB.selectColumns("favorite_id");

			Dao<FavoriteGroup, Integer> favGroup = db.helper
					.getDao(FavoriteGroup.class);

			QueryBuilder<FavoriteGroup, Integer> favGroupQB = favGroup
					.queryBuilder();

			favGroupQB.selectColumns("extern_id");

			favGroupsQB.where().in("favoritegroup_id", favGroupQB);

			favArticleQB.where().in("id", favGroupsQB).and()
					.eq("user_id", userID);

			artQB.where().notIn("id", favArticleQB).and().eq("user_id", userID)
					.and().eq("deleted", false).and()
					.notIn("type", Util.getNotShownModules(db));

			return (int) articleDAO.countOf(artQB.prepare());
		}
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return
	 * @throws Exception
	 * 
	 *             counts all article which are favorites from the registered
	 *             user
	 */
	public static int countAllFavoritArticles(int userID, DatabaseActivity db)
			throws Exception {

		Dao<FavoriteArticle, Integer> favoriteArticleDAO = db.helper
				.getDao(FavoriteArticle.class);

		QueryBuilder<FavoriteArticle, Integer> faQB = favoriteArticleDAO
				.queryBuilder();

		Where<FavoriteArticle, Integer> favaWhere = faQB.selectColumns(
				"article_id").where();

		favaWhere.eq("user_id", userID);

		Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

		QueryBuilder<Article, Integer> qb = articleDAO.queryBuilder();
		qb.setCountOf(true);
		Where<Article, Integer> where = qb.where();
		where.in("id", faQB).and().ne("user_id", userID).and()
				.eq("deleted", false).and()
				.notIn("type", Util.getNotShownModules(db));

		return (int) articleDAO.countOf(qb.prepare());
	}

	@SuppressWarnings("unchecked")
	/**
	 * 
	 * @param userID
	 * @param db
	 * @return list of criteria
	 * @throws Exception
	 * 
	 * get list of criteria with the favorite articles
	 */
	public static List<Criterion> getFavoritesByUser(int userID,
			DatabaseActivity db) throws Exception {

		Dao<Article, Integer> articleDAO = db.helper.getDao(Article.class);

		QueryBuilder<Article, Integer> articleQB = articleDAO.queryBuilder();

		Where<Article, Integer> where = articleQB.where();

		List<Article> tmpArt = new ArrayList<Article>();
		int ands = 0;
		// all locale articles
		if (showOnlyLocaleArticle) {
			where.eq("locale", true);
		} else if (!showOnlyMineInFavorites) {
			Dao<FavoriteArticle, Integer> favoriteArticleDAO = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> faQB = favoriteArticleDAO
					.queryBuilder();

			Where<FavoriteArticle, Integer> favaWhere = faQB.selectColumns(
					"article_id").where();

			favaWhere.eq("user_id", userID);

			Dao<FavoritesGroup, Integer> favsGroups = db.helper
					.getDao(FavoritesGroup.class);

			// all favorites in a group
			if (showUnassignedInFavorites && groupToShowInFavorites == 0) {
				QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favsGroups
						.queryBuilder();

				favGroupsQB.selectColumns("favorite_id");

				favaWhere.and().notIn("id", favGroupsQB);

			} else if (groupToShowInFavorites > 0) {
				QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favsGroups
						.queryBuilder();

				favGroupsQB.selectColumns("favorite_id");
				favGroupsQB.where().eq("favoritegroup_id",
						groupToShowInFavorites);
				favaWhere.and().in("id", favGroupsQB);
			}
			where.in("id", faQB).and().ne("user_id", userID);
		} // own articles
		else {
			where.eq("user_id", userID);
			ands++;
			Dao<FavoriteArticle, Integer> favoriteArticleDAO = db.helper
					.getDao(FavoriteArticle.class);

			QueryBuilder<FavoriteArticle, Integer> faQB = favoriteArticleDAO
					.queryBuilder();

			Where<FavoriteArticle, Integer> favaWhere = faQB.selectColumns(
					"article_id").where();

			favaWhere.eq("user_id", userID);
			Dao<FavoritesGroup, Integer> favsGroups = db.helper
					.getDao(FavoritesGroup.class);
			if (showUnassignedInFavorites) {

				QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favsGroups
						.queryBuilder();

				favGroupsQB.selectColumns("favorite_id");

				favaWhere.and().in("id", favGroupsQB).and()
						.eq("user_id", userID);

				where.notIn("id", faQB);

			} else if (groupToShowInFavorites > 0) {

				QueryBuilder<FavoritesGroup, Integer> favGroupsQB = favsGroups
						.queryBuilder();

				favGroupsQB.selectColumns("favorite_id");
				favGroupsQB.where().eq("favoritegroup_id",
						groupToShowInFavorites);
				favaWhere.and().in("id", favGroupsQB);

				where.in("id", faQB);
			} else {
				ands--;
			}
		}
		ands++;
		where.eq("deleted", false);
		ands++;
		where.notIn("type", Util.getNotShownModules(db));
		ands++;
		if (!showOnlyLocaleArticle) {
			where.or(where.eq("locale", false), where.isNull("locale"));
			ands++;
		}
		where.or(where.like("title", "%" + searchText + "%"),
				where.like("short_description", "%" + searchText + "%"));
		ands++;
		where.and(ands);
		articleQB.orderBy("type", false);
		Util.logDebug("STATEMENT", where.getStatement());
		tmpArt = articleQB.query();

		List<Criterion> tmpCriteria = new ArrayList<Criterion>();

		String type = "";
		SharedPreferences prefs = db.getSharedPreferences(
				db.getString(R.string.strings_json), Activity.MODE_PRIVATE);
		Criterion crit = null;
		for (Article article : tmpArt) {
			/*
			 * The favorite list are grouped by the modules. To Group the
			 * favorites, we abuse the criterion object
			 */
			if (!type.equals(article.type)) {
				type = article.type;
				crit = new Criterion();
				if (article.type.equals(Article.EVENT)) {
					crit.title = prefs.getString("module.events.title", "");
				}
				if (article.type.equals(Article.EXPERT)) {
					crit.title = prefs.getString("module.experts.title", "");
				}
				if (article.type.equals(Article.METHOD)) {
					crit.title = prefs.getString("module.methods.title", "");
				}
				if (article.type.equals(Article.STUDY)) {
					crit.title = prefs.getString("module.studies.title", "");
				}
				if (article.type.equals(Article.NEWS)) {
					crit.title = prefs.getString("module.news.title", "");
				}
				if (article.type.equals(Article.QA)) {
					crit.title = prefs.getString("module.qa.title", "");
				}
				tmpCriteria.add(crit);
			}

			Dao<FavoriteArticle, Integer> favoriteArticle = db.helper
					.getDao(FavoriteArticle.class);
			QueryBuilder<FavoriteArticle, Integer> favoriteArticleQB = favoriteArticle
					.queryBuilder();

			favoriteArticleQB.selectColumns("id").where()
					.eq("article_id", article.id);

			Dao<FavoritesGroup, Integer> favoritesGroups = db.helper
					.getDao(FavoritesGroup.class);

			QueryBuilder<FavoritesGroup, Integer> favoritesGroupQB = favoritesGroups
					.queryBuilder();

			favoritesGroupQB.selectColumns("favoritegroup_id").where()
					.in("favorite_id", favoriteArticleQB);

			Dao<FavoriteGroup, Integer> fg = db.helper
					.getDao(FavoriteGroup.class);

			article.groups.addAll(fg.queryBuilder().where()
					.in("extern_id", favoritesGroupQB).query());

			crit.articles.add(article);
		}

		return tmpCriteria;

	}

	/**
	 * 
	 * @param group
	 * @param dbActivity
	 * @return boolean
	 * @throws Exception
	 * 
	 *             saves a new favorites group in the database
	 */
	public static boolean addFavoritesGroups(FavoritesGroup group,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoritesGroup, Integer> favoriteDAO = dbActivity.helper
				.getDao(FavoritesGroup.class);

		favoriteDAO.create(group);

		return true;
	}

	/**
	 * 
	 * @param group
	 * @param dbActivity
	 * @return FavoriteGroup
	 * @throws Exception
	 * 
	 *             save a new favorite group in the database.
	 */
	public static FavoriteGroup addFavoritGroup(FavoriteGroup group,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoriteGroup, Integer> favoriteDAO = dbActivity.helper
				.getDao(FavoriteGroup.class);

		List<FavoriteGroup> list = favoriteDAO.queryForEq("extern_id",
				group.extern_id);

		if (list != null && list.size() > 0) {
			UpdateBuilder<FavoriteGroup, Integer> builder = favoriteDAO
					.updateBuilder();
			builder.updateColumnValue("sharelink", group.sharelink);
			builder.where().eq("extern_id", group.extern_id);

			favoriteDAO.update(builder.prepare());
		} else {
			favoriteDAO.create(group);
		}

		/*
		 * This logic is probably buggy as the name is not necessarily unique
		 */
		List<FavoriteGroup> groups = favoriteDAO.queryForEq("extern_id",
				group.extern_id);

		return groups.get(0);
	}

	/**
	 * 
	 * @param userID
	 * @param db
	 * @return boolean
	 * @throws Exception
	 * 
	 *             removes all favorites from the database. this method will
	 *             called if the user starts a new sync process. We must clean
	 *             the database, before we can save the newer one
	 */
	public static boolean removeAllFavorites(int userID, DatabaseActivity db)
			throws Exception {

		Dao<FavoritesGroup, Integer> favoritesGroupDAO = db.helper
				.getDao(FavoritesGroup.class);

		DeleteBuilder<FavoritesGroup, Integer> favoritesGroupDelete = favoritesGroupDAO
				.deleteBuilder();

		favoritesGroupDelete.delete();

		Dao<FavoriteGroup, Integer> favoriteGroupDAO = db.helper
				.getDao(FavoriteGroup.class);

		DeleteBuilder<FavoriteGroup, Integer> favoriteGroupDelete = favoriteGroupDAO
				.deleteBuilder();

		favoriteGroupDelete.delete();

		Dao<FavoriteArticle, Integer> favoriteArticleDAO = db.helper
				.getDao(FavoriteArticle.class);

		DeleteBuilder<FavoriteArticle, Integer> favoriteArticleDelete = favoriteArticleDAO
				.deleteBuilder();

		favoriteArticleDelete.delete();

		return true;
	}

	/**
	 * 
	 * @param articleID
	 * @param userID
	 * @param dbActivity
	 * @return boolean
	 * @throws Exception
	 * 
	 *             adds an article as a favorite to the database
	 */
	public static boolean addFavoritToDB(int articleID, int userID,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoriteArticle, Integer> favoriteDAO = dbActivity.helper
				.getDao(FavoriteArticle.class);

		FavoriteArticle fav = new FavoriteArticle();
		fav.article_id = articleID;
		fav.created = new Date();
		fav.user_id = userID;

		CreateOrUpdateStatus status = favoriteDAO.createOrUpdate(fav);
		if (status.isCreated() || status.isUpdated()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param articleID
	 * @param userID
	 * @param dbActivity
	 * @return database identifier
	 * @throws Exception
	 * 
	 *             returns the favorite id from the article based on the
	 *             registered user
	 */
	public static int getFavoritID(int articleID, int userID,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoriteArticle, Integer> favoriteDAO = dbActivity.helper
				.getDao(FavoriteArticle.class);

		return favoriteDAO.queryBuilder().selectColumns("id").where()
				.eq("article_id", articleID).and().eq("user_id", userID)
				.query().get(0).id;
	}

	public static void removeFavoriteGroup(FavoriteGroup group,
			DatabaseActivity activity) throws Exception {
		Dao<FavoriteGroup, Integer> favgroupDAO = activity.helper
				.getDao(FavoriteGroup.class);

		favgroupDAO.delete(group);
	}

	/**
	 * 
	 * @param userID
	 * @param dbActivity
	 * @return list of FavoriteGroup objects
	 * @throws Exception
	 */
	public static List<FavoriteGroup> getFavoriteGroups(int userID,
			DatabaseActivity dbActivity) throws Exception {

		Dao<FavoriteGroup, Integer> favGroupDAO = dbActivity.getHelper()
				.getDao(FavoriteGroup.class);

		List<FavoriteGroup> groups = favGroupDAO.queryForEq("user_id", userID);

		for (FavoriteGroup favoriteGroup : groups) {
			Dao<FavoritesGroup, Integer> favoritesGroupsDAO = dbActivity.helper
					.getDao(FavoritesGroup.class);
			if (showOnlyMineInFavorites) {
				QueryBuilder<FavoritesGroup, Integer> groupsQB = favoritesGroupsDAO
						.queryBuilder();
				groupsQB.selectColumns("favorite_id");
				groupsQB.where().eq(
						"favoritegroup_id",
						(favoriteGroup.extern_id == 0) ? favoriteGroup.id
								: favoriteGroup.extern_id);

				Dao<FavoriteArticle, Integer> favDao = dbActivity.helper
						.getDao(FavoriteArticle.class);

				QueryBuilder<FavoriteArticle, Integer> qb = favDao
						.queryBuilder();
				qb.selectColumns("article_id");

				qb.where().in("id", groupsQB).and().eq("user_id", userID);

				Dao<Article, Integer> articleDAO = dbActivity.helper
						.getDao(Article.class);

				QueryBuilder<Article, Integer> articleQB = articleDAO
						.queryBuilder();

				articleQB.setCountOf(true);

				articleQB.where().in("id", qb).and().eq("user_id", userID);

				favoriteGroup.amountFavGroups = (int) articleDAO
						.countOf(articleQB.prepare());

			} else {

				QueryBuilder<FavoritesGroup, Integer> groupsQB = favoritesGroupsDAO
						.queryBuilder();
				groupsQB.selectColumns("favorite_id");
				groupsQB.where().eq(
						"favoritegroup_id",
						(favoriteGroup.extern_id == 0) ? favoriteGroup.id
								: favoriteGroup.extern_id);

				Dao<FavoriteArticle, Integer> favDao = dbActivity.helper
						.getDao(FavoriteArticle.class);

				QueryBuilder<FavoriteArticle, Integer> qb = favDao
						.queryBuilder();
				qb.selectColumns("article_id");

				qb.where().in("id", groupsQB).and().eq("user_id", userID);

				Dao<Article, Integer> articleDAO = dbActivity.helper
						.getDao(Article.class);

				QueryBuilder<Article, Integer> articleQB = articleDAO
						.queryBuilder();

				articleQB.setCountOf(true);

				articleQB.where().in("id", qb).and().ne("user_id", userID);

				favoriteGroup.amountFavGroups = (int) articleDAO
						.countOf(articleQB.prepare());

			}
		}

		return groups;
	}

	/**
	 * 
	 * @param articleID
	 * @param userID
	 * @param dbActivity
	 * @return boolean
	 * @throws Exception
	 * 
	 *             removes an article from the user favorite list
	 */
	public static boolean removeFavoritFromDB(int articleID, int userID,
			DatabaseActivity dbActivity) throws Exception {
		Dao<FavoriteArticle, Integer> favoriteDAO = dbActivity.helper
				.getDao(FavoriteArticle.class);

		DeleteBuilder<FavoriteArticle, Integer> deleteBuilder = favoriteDAO
				.deleteBuilder();

		deleteBuilder.where().eq("article_id", articleID).and()
				.eq("user_id", userID);

		int removed = favoriteDAO.delete(deleteBuilder.prepare());
		if (removed > 0) {
			return true;
		}
		return false;
	}

}
