/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;

/*
 * this fragment shows the contact part in the article details of an expert
 */
public class Expert_Contact extends Fragment {

	Article.PaneConfig config = null;
	Article article = null;

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Expert_Contact newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Expert_Contact text = new Expert_Contact();
		text.setArguments(bundle);

		return text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_contact, container,
				false);

		Bundle args = getArguments();

		config = (PaneConfig) args.getSerializable(CONFIG_KEY);
		article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));
			String key = "label.contact";
			boolean hideSelection = true;
			if (false == key.equals("")) {
				((TextView) v.findViewById(R.id.title)).setText(Util
						.getApplicationString(key, getActivity()));
			}

			if (article.address != null && article.address.length() > 0) {
				((TextView) v.findViewById(R.id.expertAdress))
						.setText(article.address);
				hideSelection = false;
			} else {
				v.findViewById(R.id.expertAdress).setVisibility(View.GONE);
			}

			if ((article.city != null && article.city.length() > 0)
					|| (article.zip != null && article.zip.length() > 0)) {
				hideSelection = false;
				((TextView) v.findViewById(R.id.expertCity))
						.setText(article.zip + " " + article.city);
			} else {
				v.findViewById(R.id.expertCity).setVisibility(View.GONE);
			}

			if (article.email != null && article.email.length() > 0) {
				hideSelection = false;
				((TextView) v.findViewById(R.id.expertEmail))
						.setText(article.email);
			} else {
				v.findViewById(R.id.layoutEmail).setVisibility(View.GONE);
			}

			if (article.phone != null & article.phone.length() > 0) {
				hideSelection = false;
				((TextView) v.findViewById(R.id.expertTel))
						.setText(article.phone);
			} else {
				v.findViewById(R.id.layoutTel).setVisibility(View.GONE);
			}

			if (article.fax != null && article.fax.length() > 0) {
				hideSelection = false;
				((TextView) v.findViewById(R.id.expertFax))
						.setText(article.fax);
			} else {
				v.findViewById(R.id.layoutFax).setVisibility(View.GONE);
			}

			if (hideSelection) {
				v.setVisibility(View.GONE);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

		return v;
	}
}
