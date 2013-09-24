/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.BasicConfigValue;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleDetails;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.dao.helper.ArticleManager;

/*
 * this fragment shows the linked articles of a selected article in the detail
 * view.
 */
public class LinkedArticles extends Fragment {
	private static String logTag = LinkedArticles.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static LinkedArticles newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		LinkedArticles LinkedArticles = new LinkedArticles();
		LinkedArticles.setArguments(bundle);

		return LinkedArticles;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_linked_articles,
				container, false);

		DatabaseActivity databaseActivity = ((DatabaseActivity) getActivity());

		Bundle args = getArguments();

		boolean foundLinks = false;

		try {
			((TextView) v.findViewById(R.id.title)).setText(Util
					.getApplicationString("global.linked_articles",
							databaseActivity));

			List<Article> articleLinks = ArticleManager.getLinkedArticles(
					args.getInt(ARTICLE_ID_KEY), databaseActivity);

			for (Article linkedArticle : articleLinks) {

				foundLinks = true;

				LinearLayout layout = (LinearLayout) getActivity()
						.getLayoutInflater().inflate(
								R.layout.linked_article_link, null);

				TextView linkTextView = (TextView) layout
						.findViewById(R.id.linkText);

				// TextView linkTextView = new TextView(getActivity());

				if (linkedArticle.type.equals(Article.STUDY)
						&& Util.isModuleActive(BasicConfigValue.MODULE_STUDY,
								getActivity())) {
					v.findViewById(R.id.studiesTitle).setVisibility(
							View.VISIBLE);

					((TextView) v.findViewById(R.id.studiesTitle)).setText(Util
							.getApplicationString("module.studies.title",
									databaseActivity));

					v.findViewById(R.id.studiesList)
							.setVisibility(View.VISIBLE);

					linkTextView.setText(linkedArticle.title);

					((LinearLayout) v.findViewById(R.id.studiesList))
							.addView(layout);
				}

				if (linkedArticle.type.equals(Article.NEWS)
						&& Util.isModuleActive(BasicConfigValue.MODULE_NEWS,
								getActivity())) {
					v.findViewById(R.id.newsTitle).setVisibility(View.VISIBLE);

					((TextView) v.findViewById(R.id.newsTitle)).setText(Util
							.getApplicationString("module.news.title",
									databaseActivity));

					v.findViewById(R.id.newsList).setVisibility(View.VISIBLE);

					linkTextView.setText(linkedArticle.title);

					((LinearLayout) v.findViewById(R.id.newsList))
							.addView(layout);
				}

				if (linkedArticle.type.equals(Article.METHOD)
						&& Util.isModuleActive(BasicConfigValue.MODULE_METHOD,
								getActivity())) {
					v.findViewById(R.id.methodsTitle).setVisibility(
							View.VISIBLE);

					((TextView) v.findViewById(R.id.methodsTitle)).setText(Util
							.getApplicationString("module.methods.title",
									databaseActivity));

					v.findViewById(R.id.methodsList)
							.setVisibility(View.VISIBLE);

					linkTextView.setText(linkedArticle.title);

					((LinearLayout) v.findViewById(R.id.methodsList))
							.addView(layout);
				}

				if (linkedArticle.type.equals(Article.EXPERT)
						&& Util.isModuleActive(BasicConfigValue.MODULE_EXPERT,
								getActivity())) {
					v.findViewById(R.id.expertsTitle).setVisibility(
							View.VISIBLE);

					((TextView) v.findViewById(R.id.expertsTitle)).setText(Util
							.getApplicationString("module.experts.title",
									databaseActivity));

					v.findViewById(R.id.expertsList)
							.setVisibility(View.VISIBLE);

					Article.ArticleBase art = linkedArticle
							.createSubType((DatabaseActivity) getActivity());

					linkTextView.setText(art.getTitle());

					((LinearLayout) v.findViewById(R.id.expertsList))
							.addView(layout);
				}

				if (linkedArticle.type.equals(Article.EVENT)
						&& Util.isModuleActive(BasicConfigValue.MODULE_EVENT,
								getActivity())) {
					v.findViewById(R.id.eventsTitle)
							.setVisibility(View.VISIBLE);

					((TextView) v.findViewById(R.id.eventsTitle)).setText(Util
							.getApplicationString("module.events.title",
									databaseActivity));

					v.findViewById(R.id.eventsList).setVisibility(View.VISIBLE);

					linkTextView.setText(linkedArticle.title);

					((LinearLayout) v.findViewById(R.id.eventsList))
							.addView(layout);
				}

				if (linkedArticle.type.equals(Article.QA)
						&& Util.isModuleActive(BasicConfigValue.MODULE_QA,
								getActivity())) {
					v.findViewById(R.id.qasTitle).setVisibility(View.VISIBLE);

					((TextView) v.findViewById(R.id.qasTitle)).setText(Util
							.getApplicationString("module.qa.title",
									databaseActivity));
					v.findViewById(R.id.qasList).setVisibility(View.VISIBLE);

					linkTextView.setText(linkedArticle.title);

					((LinearLayout) v.findViewById(R.id.qasList))
							.addView(layout);
				}

				final DatabaseActivity activity = databaseActivity;
				final Article fArticle = linkedArticle;
				linkTextView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							Intent intent = new Intent(
									activity,
									com.your.name.activities.ArticleDetails.class);

							intent.putExtra(
									com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
									fArticle.id);

							ArrayList<Integer> articleList = new ArrayList<Integer>();
							articleList.addAll(ArticleManager
									.getArticleIdsByType(fArticle.type,
											activity));

							intent.putExtra(ArticleDetails.ARTICLE_SWITCH,
									false);

							intent.putExtra(
									com.your.name.activities.ArticleDetails.ARTICLE_LIST,
									articleList);

							startActivity(intent);
						} catch (Exception e) {
							Util.logDebug(logTag, "something went wrong");
						}
					}
				});

				Util.logDebug(logTag, "found a linked article: "
						+ linkedArticle.title + "  type: " + linkedArticle.type);
			}
			if (false == foundLinks) {
				v.findViewById(R.id.separator).setVisibility(View.GONE);
				v.setVisibility(View.GONE);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return v;
	}
}