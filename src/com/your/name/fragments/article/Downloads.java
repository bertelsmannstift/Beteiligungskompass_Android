/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.service.API;

/*
 * this fragment displays the download section in the article details
 */
public class Downloads extends Fragment {

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Downloads newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Downloads ret = new Downloads();
		ret.setArguments(bundle);

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_downloads,
				container, false);

		Bundle args = getArguments();
		Article article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			prepareDownloadViews(article.new ArticleBase(
					(DatabaseActivity) getActivity()), v);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return v;
	}

	protected void prepareDownloadViews(Article.ArticleBase article, View v)
			throws Exception {

		if (article.downloadFilenames.size() > 0) {
			v.setVisibility(View.VISIBLE);
		} else {
			// v.findViewById(R.id.separator).setVisibility(View.GONE);
			v.setVisibility(View.GONE);
		}

		DownloadsAdapter adapter = new DownloadsAdapter(article);

		LinearLayout blockLayout = (LinearLayout) v
				.findViewById(R.id.downloadsLayout);

		blockLayout.removeAllViews();

		for (int index = 0, max = adapter.getCount(); index < max; ++index) {
			View view = adapter.getView(index, null, null);

			blockLayout.addView(view);
		}
	}

	private class DownloadsAdapter extends BaseAdapter {
		Article.ArticleBase article = null;

		LayoutInflater inflater;

		public DownloadsAdapter(Article.ArticleBase article) {
			this.article = article;
			inflater = ((LayoutInflater) getActivity().getSystemService(
					Activity.LAYOUT_INFLATER_SERVICE));
		}

		@Override
		public int getCount() {
			return article.downloadFilenames.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			View v = inflater
					.inflate(R.layout.listitem_article_downloads, null);

			((TextView) v.findViewById(R.id.text))
					.setText(article.downloadFilenames.get(position));

			final int pos = position;
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(API.getInstance(getActivity()).host
									+ "/media/"
									+ article.downloadFilenames.get(pos)));

					startActivity(intent);
				}
			});

			return v;
		}

	}
}
