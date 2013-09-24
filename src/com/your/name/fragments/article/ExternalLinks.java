/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.AdapterUtils;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;

/*
 * this fragment show the external links in the article details 
 */
public class ExternalLinks extends Fragment {

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	private ArrayList<String> externalLink = new ArrayList<String>();

	public static ExternalLinks newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		ExternalLinks ret = new ExternalLinks();
		ret.setArguments(bundle);

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_external_links,
				container, false);

		Bundle args = getArguments();

		Article article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			if (article.external_links != null
					&& article.external_links.length() > 0) {
				JSONArray array = new JSONArray(article.external_links);

				for (int i = 0; i < array.length(); i++) {
					// if (array.getJSONObject(i).getBoolean("show_link")) {
					externalLink.add(array.getJSONObject(i).getString("url"));
					// }
				}
			}

			prepareExternalLinks(v);

			if (externalLink.size() == 0) {
				v.findViewById(R.id.separator).setVisibility(View.GONE);
				v.setVisibility(View.GONE);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return v;
	}

	private void prepareExternalLinks(View v) {
		try {
			((TextView) v.findViewById(R.id.title))
					.setText(Util.getApplicationString("label.external_links",
							getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		AdapterUtils.fillLinearLayoutFromAdapter(1,
				(LinearLayout) v.findViewById(R.id.externalLinkLayout),
				new ExternalLinkAdapter(), (DatabaseActivity) getActivity(),
				true, false);
	}

	private class ExternalLinkAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return externalLink.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.external_link_text, null);
			}

			((TextView) v.findViewById(R.id.linkText)).setText(externalLink
					.get(position));

			return v;
		}
	}
}