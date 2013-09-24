/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;

/*
 * this fragment displays the part where the location / duration and state will
 * be shown
 */
public class Study_Detail extends Fragment {

	Article.PaneConfig config = null;
	Article article = null;

	private static String logTag = Author.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Study_Detail newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Study_Detail text = new Study_Detail();
		text.setArguments(bundle);

		return text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_project_country,
				container, false);

		Bundle args = getArguments();

		config = (PaneConfig) args.getSerializable(CONFIG_KEY);
		article = null;

		Dao<Article, Integer> articlesDAO;
		String country = "";
		String duration = "";
		String progress = "";
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			if (article.city != null && article.city.length() > 0) {
				country += article.city;
			}
			Dao<CriteriaOption, Integer> optionDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(CriteriaOption.class);

			Dao<ArticlesOption, Integer> artOptionDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(ArticlesOption.class);
			;

			QueryBuilder<ArticlesOption, Integer> artQB = artOptionDAO
					.queryBuilder();
			artQB.selectColumns("model_criterion_option_id");
			artQB.where().eq("model_article_id", article.id);

			QueryBuilder<CriteriaOption, Integer> qb = optionDAO.queryBuilder();

			Dao<Criterion, Integer> criterionDAO = ((DatabaseActivity) getActivity()).helper
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
				if (country.length() > 0) {
					country += ", ";
					country += criteriaOption.title;
					break;
				}
			}

			if (article.start_year > 0 || article.start_month > 0
					|| article.end_month > 0 || article.end_year > 0) {
				SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy",
						Locale.getDefault());

				if (article.end_year == 0) {
					if (article.start_month > 0 && article.start_year > 0) {
						duration += Util.getApplicationString("global.since",
								getActivity())
								+ " "
								+ format.format(new GregorianCalendar(
										article.start_year,
										article.start_month, 0).getTime());
					} else if (article.start_year > 0) {
						duration += Util.getApplicationString("global.since",
								getActivity()) + " " + article.start_year;
					}

					progress += Util.getApplicationString(
							"label.state_ongoing", getActivity());

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
							startyear = format.format(new GregorianCalendar(
									article.start_year, article.start_month, 0)
									.getTime());
						} else {
							startyear = article.start_year + "";
						}
					}

					if (startyear.length() > 0) {
						duration += startyear + " - " + endyear + "";
					}

					GregorianCalendar today = new GregorianCalendar();
					GregorianCalendar enddate = new GregorianCalendar(
							article.end_year, article.end_month, 0);
					if (article.end_year == 0) {
						progress += Util.getApplicationString(
								"label.state_ongoing", getActivity());
					} else if (today.getTimeInMillis() > enddate
							.getTimeInMillis()) {
						progress += Util.getApplicationString(
								"label.state_closed", getActivity());
					} else {
						progress += Util.getApplicationString(
								"label.state_ongoing", getActivity());
					}
				}
			}
			((TextView) v.findViewById(R.id.ort)).setText(Util
					.getApplicationString("label.location", getActivity()));
			((TextView) v.findViewById(R.id.dauer)).setText(Util
					.getApplicationString("global.duration", getActivity()));
			((TextView) v.findViewById(R.id.State))
					.setText(Util.getApplicationString("label.projectstatus",
							getActivity()));

			WebView textView = ((WebView) v.findViewById(R.id.textOrt));
			textView.loadDataWithBaseURL(null, country, "text/html", "UTF-8",
					null);

			WebView textState = ((WebView) v.findViewById(R.id.textDauer));
			textState.loadDataWithBaseURL(null, duration, "text/html", "UTF-8",
					null);

			WebView textDuration = ((WebView) v.findViewById(R.id.textState));
			textDuration.loadDataWithBaseURL(null, progress, "text/html",
					"UTF-8", null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return v;
	}

}
