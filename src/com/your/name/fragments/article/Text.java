/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.User;
import com.your.name.dao.helper.DatabaseHelper;

/**
 * This is the default fragment. Generally we use it if none of the other
 * fragments are required.
 */
public class Text extends Fragment {

	Article.PaneConfig config = null;
	Article article = null;

	private static String logTag = Text.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Text newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putSerializable(ARTICLE_ID_KEY, article);
		bundle.putSerializable(CONFIG_KEY, config);

		Text text = new Text();
		text.setArguments(bundle);

		return text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_text, container,
				false);

		Bundle args = getArguments();

		config = (PaneConfig) args.getSerializable(CONFIG_KEY);
		article = null;

		try {
			article = (Article) args.getSerializable(ARTICLE_ID_KEY);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		boolean hideSection = true;
		boolean hideTitle = true;

		try {
			String key = "";
			String text = "";

			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm",
					Locale.getDefault());
			/*
			 * based on the PaneConfig, the fragment takes several information
			 * from the article object. The config values could be found in the
			 * article class.
			 */
			switch (config) {
			case QUESTION:
				if (!Util.isTablet(getActivity())
						|| Util.isOrientationPortrait(getActivity())) {
					key = "label.question";
				}
				text = article.question;

				break;
			case GOOGLEMAP:
				key = "";
				text = "";
				break;
			case ANSWER:
				key = "label.answer";
				text = article.answer;

				break;
			case PUBLISHER:
				key = "label.publisher";
				text = article.publisher;

				break;

			case AUTHOR_ANSWER:
				key = "label.author_answer";
				text = article.author_answer;

				break;

			case USED_FOR:
				key = "label.used_for";
				text = article.used_for;

				break;

			case PARTICIPANTS:
				key = "label.participants";
				text = article.participants;

				break;

			case COSTS:
				key = "label.costs";
				text = article.costs;

				break;

			case TIME_EXPENSE:
				key = "label.time_expense";
				text = article.time_expense;

				break;

			case WHEN_TO_USE:
				key = "label.when_to_use";
				text = article.when_to_use;

				break;

			case WHEN_NOT_TO_USE:
				key = "label.when_not_to_use";
				text = article.when_not_to_use;

				break;

			case STRENGTHS:
				key = "label.strengths";
				text = article.strengths;

				break;

			case WEAKNESSES:
				key = "label.weaknesses";
				text = article.weaknesses;

				break;

			case ORIGIN:
				key = "label.origin";
				text = article.origin;

				break;

			case RESTRICTIONS:
				key = "label.restrictions";
				text = article.restrictions;

				break;

			case DESCRIPTION_INSTITUTION:
				key = "label.description_institution";
				text = article.description_institution;

				break;

			case SHORT_DESCRIPTION_EXPERT:
				if (!Util.isTablet(getActivity())
						|| Util.isOrientationPortrait(getActivity())) {
					key = "label.short_description_expert";
				}
				text = article.short_description_expert;

				break;

			case DURATION_FULL:
				key = "";

				text = "" + format.format(article.start_date) + " - <br/>"
						+ format.format(article.end_date) + " | "
						+ article.city;

				break;

			case TEXT:
				// key = "label.text";
				key = "";
				text = article.text;

				break;

			case DATE:
				key = "label.date";
				text = "" + format.format(article.date) + " Uhr";

				break;

			case AUTHOR:
				key = "label.author";
				Dao<User, Integer> userDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(User.class);
				List<User> users = userDAO.queryForEq("id", article.user.id);
				text = "Autor: " + users.get(0).first_name + " "
						+ users.get(0).last_name;

				break;

			case SUBTITLE:

				// key = "label.subtitle";
				text = article.subtitle;
				Log.d("TITLE", article.subtitle);
				break;

			case TITLE:
				key = "label.title";
				text = article.title;

				break;

			case UPDATED:
				SimpleDateFormat updatedFormat = new SimpleDateFormat(
						"dd.MM.yyyy HH:mm", Locale.getDefault());
				key = "label.updated";
				text = "" + updatedFormat.format(article.updated) + " Uhr";

				break;

			case CITY_AND_COUNTRY:
				if ((article.city == null || article.city.equals(""))
						&& (article.country == null || article.country
								.equals(""))) {
					key = "";
					text = "";
					break;
				}
				// key = "label.location";
				key = "";
				text = article.city;

				String country = getCountryName(article);

				if (country.length() > 0) {
					if (article.city != null && article.city.length() > 0) {
						text += ", " + country;
					} else {
						text = country;
					}
				}
				break;

			case CITY:
				key = "label.city";
				text = article.city;

				break;

			case COUNTRY:
				key = "label.country";
				text = getCountryName(article);

				break;

			case DURATION_MONTH_AND_YEAR:
				key = "";

				boolean hasEndDate = null != article.end_year
						&& article.end_year != 0;

				if (false == hasEndDate) {

					text = Util.getApplicationString("global.started",
							getActivity()) + " ";
				}

				format = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

				text += format.format(new GregorianCalendar(article.start_year,
						article.start_month, 0).getTime());

				if (true == hasEndDate) {
					text += " - "
							+ format.format(new GregorianCalendar(
									article.end_year, article.end_month, 0)
									.getTime());
				}

				break;

			case PROJECT_STATUS:
				key = "";
				text = article.projectstatus;

				break;

			case AIM:
				key = "label.aim";
				text = article.aim;

				break;

			case RESULTS:
				key = "label.results";
				text = article.results;

				break;

			case INTRO:
				// key = "label.intro";
				key = "";
				text = article.intro;

				break;
			case YEAR:
				key = "label.year";
				text = article.year + "";
			case PROCESS:
				if (article.type.equals(Article.METHOD)) {
					key = "label.description";
				} else {
					key = "label.process";
				}
				text = article.process;

				break;

			case DESCRIPTION:
				if (article.type.equals(Article.NEWS)) {
					key = "";
				} else {
					if (!article.type.equals(Article.EVENT)) {
						key = "label.description";
					} else {
						key = "label.description_event";
					}
				}

				text = article.description;
				break;

			case SHORT_DESCRIPTION:
				if (!Util.isTablet(getActivity())
						|| Util.isOrientationPortrait(getActivity())) {
					key = "label.short_description";
				}
				text = article.short_description;

				break;
			case MORE_INFORMATION:
				key = "label.more_information";
				text = article.more_information;

				break;

			case BACKGROUND:
				key = "label.background";
				text = article.background;

				break;

			case CONTACT:
				key = "label.contact";
				text = article.contact;

				break;

			case EXPERT_CONTACT:
				key = "label.contact_person";
				text = article.contact_person;

				break;

			default:
				key = "not yet handled";
				text = "DEFAULT";
				Util.logDebug(logTag, "ENUM is " + config);
				break;
			}

			WebView textView = ((WebView) v.findViewById(R.id.text));
			// textView.loadData(text, "text/html", "UTF-8",);
			textView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);

			// textView.setAutoLinkMask(Linkify.ALL);

			Util.logDebug(logTag, "adding text block with key: " + key
					+ "  and text: " + text);

			/*
			 * Let's check if there's a title to display
			 */
			if (false == key.equals("")) {
				((TextView) v.findViewById(R.id.title)).setText(Util
						.getApplicationString(key, getActivity()));
				hideTitle = false;
			} else {
			}

			if (false == text.equals("")) {
				hideSection = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			((WebView) v.findViewById(R.id.text)).loadData(
					null != e.getMessage() ? e.getMessage() : "No message",
					"text/html", "UTF-8");
		}

		if (true == hideSection) {
			v.findViewById(R.id.separator).setVisibility(View.GONE);
			v.setVisibility(View.GONE);
		}

		if (true == hideTitle) {
			v.findViewById(R.id.separator).setVisibility(View.GONE);
			v.findViewById(R.id.title).setVisibility(View.GONE);
		}

		return v;
	}

	/**
	 * returns null if the country is not set
	 */
	public String getCountryName(Article article) {
		try {
			DatabaseHelper helper = ((DatabaseActivity) getActivity()).helper;

			List<ArticlesOption> articlesOptions = helper.getDao(
					ArticlesOption.class).queryForEq("model_article_id",
					article.id);

			for (ArticlesOption articlesOption : articlesOptions) {

				Dao<CriteriaOption, Integer> criteriaOptionsDAO = helper
						.getDao(CriteriaOption.class);

				CriteriaOption criteriaOption = criteriaOptionsDAO
						.queryForId(articlesOption.model_criterion_option_id);

				Dao<Criterion, Integer> criteriaDAO = helper
						.getDao(Criterion.class);

				Criterion criterion = criteriaDAO
						.queryForId(criteriaOption.criterion_id.id);

				if (criterion.discriminator.equals("country")) {
					/*
					 * YAY, we have a country criterion
					 */
					Util.logDebug(logTag, "option with discriminator country");
					return criteriaOption.title;
				}

			}

			return "";
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
