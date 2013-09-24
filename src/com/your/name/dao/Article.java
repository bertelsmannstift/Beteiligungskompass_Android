/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;

@DatabaseTable(tableName = "articles")
public class Article implements Serializable, Comparable<Article> {

	private static final long serialVersionUID = 7440614510171936517L;

	private static final String logTag = Article.class.toString();

	public static final String STUDY = "study";
	public static final String METHOD = "method";
	public static final String NEWS = "news";
	public static final String EVENT = "event";
	public static final String EXPERT = "expert";
	public static final String QA = "qa";
	public static final String INSTRUMENT = "instrument";

	@DatabaseField(generatedId = true)
	public Integer id;

	public boolean is_mine_article = false;

	@DatabaseField
	public String title = "";

	@DatabaseField
	public Boolean /* NOT NULL */deleted = false;

	@DatabaseField
	public String /* NOT NULL */type = "";

	@DatabaseField
	public String listdescription_plaintext;

	@DatabaseField
	public String description_plaintext = "";

	@DatabaseField
	public String country = "";

	@DatabaseField
	public Integer start_month = 0;

	@DatabaseField
	public Integer start_year = 0;

	@DatabaseField
	public Integer end_month = 0;

	@DatabaseField
	public Integer end_year = 0;

	@DatabaseField
	public String /* NOT NULL */short_description = "";

	@DatabaseField
	public String background = "";

	@DatabaseField
	public String aim;

	@DatabaseField
	public String process = "";

	@DatabaseField
	public String results = "";

	@DatabaseField
	public String contact = "";

	@DatabaseField
	public String used_for = "";

	@DatabaseField
	public String participants = "";

	@DatabaseField
	public String costs = "";

	@DatabaseField
	public String time_expense = "";

	@DatabaseField
	public String when_to_use = "";

	@DatabaseField
	public String when_not_to_use = "";

	@DatabaseField
	public String strengths = "";

	@DatabaseField
	public String origin = "";

	@DatabaseField
	public String restrictions = "";

	@DatabaseField
	public String /* NOT NULL */author = "";

	@DatabaseField
	public String listdescription = "";

	@DatabaseField
	public String description = "";

	@DatabaseField
	public String answer = "";

	@DatabaseField
	public String author_answer = "";

	@DatabaseField(foreign = true, columnName = "user_id")
	public User user = null;

	@DatabaseField
	public Boolean /* NOT NULL */active = false;

	@DatabaseField
	public String weaknesses = "";

	@DatabaseField
	public String external_links = "";

	@DatabaseField
	public String videos = "";

	@DatabaseField
	public String images = "";

	@DatabaseField
	public Boolean /* NOT NULL */ready_for_publish = false;

	@DatabaseField
	public String question = "";

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date /* datetime *//* NOT NULL */created = null;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date /* datetime *//* NOT NULL */updated = null;

	@DatabaseField
	public String /* NOT NULL */more_information = "";

	@DatabaseField
	public Integer involveid = null;

	@DatabaseField
	public String city = "";

	@DatabaseField
	public String projectstatus = "";

	@DatabaseField
	public String institution = "";

	@DatabaseField
	public String address = "";

	@DatabaseField
	public String lastname = "";

	@DatabaseField
	public String subtitle = "";

	@DatabaseField
	public String intro = "";

	@DatabaseField
	public String text = "";

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
	public Date date = null;

	@DatabaseField
	public String zip = "";

	@DatabaseField
	public String description_institution = "";

	@DatabaseField
	public String firstname = "";

	@DatabaseField
	public String email = "";

	@DatabaseField
	public String phone = "";

	@DatabaseField
	public String fax = "";

	@DatabaseField
	public String short_description_expert = "";

	@DatabaseField
	public String publisher;

	@DatabaseField
	public Integer year = null;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date /* datetime */start_date = null;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date /* datetime */end_date = null;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
	public Date deadline = null;

	@DatabaseField
	public String street = "";

	@DatabaseField
	public String street_nr = "";

	@DatabaseField
	public String organized_by = "";

	@DatabaseField
	public String participation = "";

	public int participatioN_id = 0;

	@DatabaseField
	public String link = "";

	@DatabaseField
	public String contact_person = "";

	@DatabaseField
	public String venue = "";

	@DatabaseField
	public String number_of_participants = "";

	@DatabaseField
	public boolean locale = false;

	@DatabaseField
	public String fee = "";

	public boolean isFavorit = false;

	public List<Criterion> critera = null;

	public List<File> imageFiles = new ArrayList<File>();

	public List<String> externalLinks = new ArrayList<String>();

	public List<FavoriteGroup> groups = new ArrayList<FavoriteGroup>();

	public List<Article> linkedArticles = new ArrayList<Article>();

	public List<ArticlesOption> options = null;

	public int favID;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date /* datetime */sticky = null;

	List<CriteriaOption> matchingOptions = new ArrayList<CriteriaOption>();

	public ArticleBase createSubType(DatabaseActivity databaseActivity)
			throws Exception {
		if (type.equals(STUDY)) {
			return new Study(databaseActivity);
		}
		if (type.equals(METHOD)) {
			return new Method(databaseActivity);
		}
		if (type.equals(QA)) {
			return new QA(databaseActivity);
		}
		if (type.equals(NEWS)) {
			return new News(databaseActivity);
		}
		if (type.equals(EXPERT)) {
			return new Expert(databaseActivity);
		}
		if (type.equals(EVENT)) {
			return new Event(databaseActivity);
		}
		throw new Exception("This article is not one of the supported types");
	}

	/*
	 * Do not remove the comments at the end of the lines unless the eclipse
	 * code formatter now knows how to handle long enums correctly
	 */
	public enum PaneConfig {
		QUESTION, ANSWER, PUBLISHER, YEAR, AUTHOR_ANSWER, USED_FOR, //
		PARTICIPANTS, COSTS, TIME_EXPENSE, WHEN_TO_USE, WHEN_NOT_TO_USE, //
		STRENGTHS, WEAKNESSES, ORIGIN, RESTRICTIONS, CONTACT, EXPERT_CONTACT, //
		DESCRIPTION_INSTITUTION, SHORT_DESCRIPTION_EXPERT, DESCRIPTION, //
		GOOGLEMAP, CALENDER, DURATION_FULL, CRITERIA, TEXT, INTRO, DATE, //
		AUTHOR, SUBTITLE, TITLE, UPDATED, SHORT_DESCRIPTION, CITY, COUNTRY, //
		CITY_AND_COUNTRY, DURATION_MONTH_AND_YEAR, PROJECT_STATUS, VIDEOS, //
		IMAGES, FILES, BACKGROUND, AIM, PROCESS, RESULTS, MORE_INFORMATION, //
		EXTERNAL_LINKS, LINKED_ARTICLES, VENUE, IMAGE_AND_EXPERT_DESCRIPTION, PROJECT_DETAIL
	}

	/*
	 * This class represents functionality that is needed to be common among all
	 * article types
	 */
	public class ArticleBase {
		protected void assertType(String typeToCheck) throws Exception {
			if (false == type.equals(typeToCheck)) {
				throw new RuntimeException("The article is not a "
						+ typeToCheck + " item");
			}
		}

		public List<String> imageFilenames = new ArrayList<String>();
		public List<String> downloadFilenames = new ArrayList<String>();
		public List<String> videoURLs = new ArrayList<String>();

		public String getListDescription() {
			return description;
		}

		/*
		 * This gets overridden by e.g. Experts to get the relevant title
		 */
		public String getTitle() {
			return title;
		}

		/*
		 * Subclasses need to implement these.
		 */
		public List<PaneConfig> getLeftPaneConfig() {
			return new ArrayList<PaneConfig>();
		}

		/*
		 * Subclasses need to implement these.
		 */
		public List<PaneConfig> getRightPaneConfig() {
			return new ArrayList<PaneConfig>();
		}

		/*
		 * All criteria options that match this article. This is filled by the
		 * Data class, since a single pass over the join table is quicker than
		 * iterating through the join table for every article
		 */
		public List<CriteriaOption> matchingOptions = Article.this.matchingOptions;
		protected DatabaseActivity db;

		public ArticleBase(DatabaseActivity databaseActivity) throws Exception {
			db = databaseActivity;
			if (null != images) {
				try {
					JSONArray imagesArray = new JSONArray(images);

					Dao<File, Integer> filesDAO = databaseActivity.helper
							.getDao(File.class);

					for (int index = 0, max = imagesArray.length(); index < max; ++index) {

						int fileID = imagesArray.getJSONObject(index).getInt(
								"id");

						QueryBuilder<File, Integer> qb = filesDAO
								.queryBuilder();

						qb.where().eq("id", fileID);

						File file = qb.queryForFirst();

						/*
						 * The mime field of the files table contains a mime
						 * type. Only if the first part of the mime type is
						 * "image" we add the image to the image list
						 */
						if (file.mime.split("/")[0].equals("image")) {

							Util.logDebug(logTag,
									"adding image filename to list: "
											+ file.filename);
							imageFilenames.add(fileID + "-" + file.filename);
						} else {

							downloadFilenames.add(fileID + "-" + file.filename);
						}
					}
				} catch (Exception e) {

				}
			}

			if (null != videos) {
				JSONArray videosArray = new JSONArray(videos);
				for (int index = 0, max = videosArray.length(); index < max; ++index) {
					videoURLs.add(videosArray.getJSONObject(index).getString(
							"url"));
				}
			}
		}
	}

	public class News extends ArticleBase {
		public News(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.NEWS);

		}

		public String getListDescription() {
			if (null != subtitle && false == subtitle.equals("")) {
				return subtitle;
			} else if (null != intro) {
				return intro;
			} else if (null != text) {
				return text;
			}

			return "";
		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}

			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				config.add(PaneConfig.SUBTITLE);
				if (!Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.INTRO);
				}
				config.add(PaneConfig.TEXT);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.EXTERNAL_LINKS);

			} else {
				config.add(PaneConfig.TITLE);

				config.add(PaneConfig.AUTHOR);
				config.add(PaneConfig.SUBTITLE);
				if (Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.INTRO);
				} else {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				}
				config.add(PaneConfig.TEXT);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}

			return config;
		}
	}

	public class Event extends ArticleBase {
		public Event(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.EVENT);

		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.CALENDER);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				config.add(PaneConfig.IMAGES);
				config.add(PaneConfig.DURATION_FULL);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DESCRIPTION);
				config.add(PaneConfig.VENUE);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.GOOGLEMAP);
			} else {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				config.add(PaneConfig.IMAGES);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DURATION_FULL);
				config.add(PaneConfig.DESCRIPTION);
				config.add(PaneConfig.VENUE);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.GOOGLEMAP);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}
	}

	public class Expert extends ArticleBase {
		public Expert(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.EXPERT);

		}

		@Override
		public String getTitle() {
			if (null == title || title.equals("")) {
				return (null != institution) ? institution : "";
			} else {
				title = "";
				if (firstname != null && firstname.length() > 0) {
					title = firstname;
				}

				if (lastname != null && lastname.length() > 0) {
					if (title.length() > 0) {
						title += " " + lastname;
					} else {
						title = lastname;
					}
				}

				if (institution != null && institution.length() > 0) {
					if (title.length() > 0) {
						title += ", " + institution;
					} else {
						title = institution;
					}
				}
			}
			return title;
		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				if (!Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION);
				}
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DESCRIPTION_INSTITUTION);
				config.add(PaneConfig.EXPERT_CONTACT);
			} else {
				config.add(PaneConfig.TITLE);
				if (Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION_EXPERT);
				} else {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				}
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DESCRIPTION_INSTITUTION);
				config.add(PaneConfig.EXPERT_CONTACT);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}
	}

	public class Method extends ArticleBase {
		public Method(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.METHOD);
		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();
			if (Util.isTablet(db)) {
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();
			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				if (!Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION);
				}
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DESCRIPTION);
				config.add(PaneConfig.USED_FOR);
				config.add(PaneConfig.PARTICIPANTS);
				config.add(PaneConfig.COSTS);
				config.add(PaneConfig.TIME_EXPENSE);
				config.add(PaneConfig.WHEN_TO_USE);
				config.add(PaneConfig.WHEN_NOT_TO_USE);
				config.add(PaneConfig.STRENGTHS);
				config.add(PaneConfig.WEAKNESSES);
				config.add(PaneConfig.ORIGIN);
				config.add(PaneConfig.RESTRICTIONS);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.CONTACT);
			} else {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				config.add(PaneConfig.TITLE);
				if (Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				}
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.DESCRIPTION);
				config.add(PaneConfig.USED_FOR);
				config.add(PaneConfig.PARTICIPANTS);
				config.add(PaneConfig.COSTS);
				config.add(PaneConfig.TIME_EXPENSE);
				config.add(PaneConfig.WHEN_TO_USE);
				config.add(PaneConfig.WHEN_NOT_TO_USE);
				config.add(PaneConfig.STRENGTHS);
				config.add(PaneConfig.WEAKNESSES);
				config.add(PaneConfig.ORIGIN);
				config.add(PaneConfig.RESTRICTIONS);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.CONTACT);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

	}

	public class QA extends ArticleBase {
		public QA(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.QA);
		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				// config.add(PaneConfig.IMAGES);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				if (!Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.QUESTION);
				}

				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.ANSWER);
				config.add(PaneConfig.PUBLISHER);
				config.add(PaneConfig.YEAR);
				config.add(PaneConfig.AUTHOR_ANSWER);
				config.add(PaneConfig.EXTERNAL_LINKS);
			} else {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				if (Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.QUESTION);
				} else {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				}
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.ANSWER);
				config.add(PaneConfig.PUBLISHER);
				config.add(PaneConfig.YEAR);
				config.add(PaneConfig.AUTHOR_ANSWER);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
				;
			}
			return config;
		}
	}

	public class Study extends ArticleBase {
		public Study(DatabaseActivity databaseActivity) throws Exception {
			super(databaseActivity);
			assertType(Article.STUDY);

		}

		@Override
		public String getListDescription() {
			return Article.this.short_description;
		}

		@Override
		public List<PaneConfig> getRightPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();

			if (Util.isTablet(db)) {

				config.add(PaneConfig.FILES);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.CRITERIA);
			}
			return config;
		}

		@Override
		public List<PaneConfig> getLeftPaneConfig() {
			List<PaneConfig> config = new ArrayList<Article.PaneConfig>();
			if (Util.isTablet(db)) {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				if (!Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION);
				}
				config.add(PaneConfig.PROJECT_DETAIL);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.BACKGROUND);
				config.add(PaneConfig.AIM);
				config.add(PaneConfig.PROCESS);
				config.add(PaneConfig.RESULTS);
				config.add(PaneConfig.MORE_INFORMATION);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.CONTACT);
			} else {
				config.add(PaneConfig.TITLE);
				config.add(PaneConfig.AUTHOR);
				if (Util.isOrientationPortrait(db)) {
					config.add(PaneConfig.IMAGES);
					config.add(PaneConfig.SHORT_DESCRIPTION);
				} else {
					config.add(PaneConfig.IMAGE_AND_EXPERT_DESCRIPTION);
				}
				config.add(PaneConfig.PROJECT_DETAIL);
				config.add(PaneConfig.VIDEOS);
				config.add(PaneConfig.BACKGROUND);
				config.add(PaneConfig.AIM);
				config.add(PaneConfig.PROCESS);
				config.add(PaneConfig.RESULTS);
				config.add(PaneConfig.MORE_INFORMATION);
				config.add(PaneConfig.EXTERNAL_LINKS);
				config.add(PaneConfig.LINKED_ARTICLES);
				config.add(PaneConfig.FILES);
				config.add(PaneConfig.CONTACT);
			}
			return config;
		}
	}

	@Override
	public int compareTo(Article another) {
		if ((another.isFavorit && this.isFavorit)
				|| (another.is_mine_article && this.is_mine_article)) {
			return 0;
		}

		if (this.is_mine_article || this.isFavorit) {
			return -1;
		}

		if (another.isFavorit || another.is_mine_article) {
			return 1;
		}

		return 0;
	}
}