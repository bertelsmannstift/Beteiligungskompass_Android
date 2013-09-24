/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.dashboard;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.AdapterUtils;
import com.your.name.BasicConfigValue;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleDetails;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.Login;
import com.your.name.activities.Sync;
import com.your.name.dao.Article;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.service.API;

/* 
 * this fragment contains the news event section part on the tablet screen. 
 */
public class News_Event_Section extends Fragment {

	private List<Article> newsArticles, eventArticles = null;
	private LinearLayout eventList, newsList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_news_event, container,
				false);
		try {
			if (Util.isModuleActive(BasicConfigValue.MODULE_EVENTBOX,
					getActivity())) {
				TextView eventTextView = (TextView) v
						.findViewById(R.id.EventsSectionheader);
				eventTextView.setText(Util.getApplicationString(
						"module.events.title.dashheader", getActivity()));
				eventTextView.setVisibility(View.VISIBLE);
				eventList = (LinearLayout) v.findViewById(R.id.listViewEvent);
				eventArticles = ArticleManager
						.getNewestEvents((DatabaseActivity) getActivity());

				AdapterUtils.fillLinearLayoutFromAdapter(1, eventList,
						new EventAdapter(), getActivity(), true, true, 2);
			}

			if (Util.isModuleActive(BasicConfigValue.MODULE_NEWSBOX,
					getActivity())) {
				TextView newsTextView = ((TextView) v
						.findViewById(R.id.NewsSectionHeader));
				newsTextView.setText(Util.getApplicationString(
						"module.news.title.dashheader", getActivity()));
				newsTextView.setVisibility(View.VISIBLE);
				newsList = (LinearLayout) v.findViewById(R.id.listViewNews);
				newsArticles = ArticleManager
						.getNewestNews((DatabaseActivity) getActivity());

				AdapterUtils.fillLinearLayoutFromAdapter(1, newsList,
						new NewsAdapter(), getActivity(), true, true, 2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	private class NewsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return newsArticles.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View v, ViewGroup arg2) {
			if (v == null) {
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.articlelist_child_layout, null);
			}

			final Article article = (Article) newsArticles.get(pos);

			return buildView(v, article);
		}

	}

	private class EventAdapter extends BaseAdapter {

		public EventAdapter() {

		}

		@Override
		public int getCount() {
			return eventArticles.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View v, ViewGroup arg2) {
			if (v == null) {
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.articlelist_child_layout, null);
			}
			final Article article = (Article) eventArticles.get(pos);

			return buildView(v, article);
		}

	}

	private View buildView(View v, final Article article) {
		try {
			article.isFavorit = ArticleManager.isArticleFavorit(article.id,
					(DatabaseActivity) getActivity());

			v.setTag(article);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new loadOneArticleTask(
							(article.type.equals(Article.EVENT) ? 1 : 0))
							.execute(article);

				}
			});
			Article.ArticleBase specializedArticle = article
					.createSubType((DatabaseActivity) getActivity());

			((TextView) v.findViewById(R.id.articleTitle))
					.setText(specializedArticle.getTitle());

			((TextView) v.findViewById(R.id.articleDescription))
					.setText(article.listdescription_plaintext);
			((TextView) v.findViewById(R.id.articleDescription)).setLines(3);

			if (0 == specializedArticle.videoURLs.size()
					|| !Util.isTablet((DatabaseActivity) getActivity())) {
				v.findViewById(R.id.articleVideo).setVisibility(View.GONE);
			}

			if (0 == specializedArticle.downloadFilenames.size()
					|| !Util.isTablet((DatabaseActivity) getActivity())) {
				v.findViewById(R.id.articleDownload).setVisibility(View.GONE);
			}

			if (null == article.city || article.city.equals("")
					|| !Util.isTablet((DatabaseActivity) getActivity())) {
				v.findViewById(R.id.articlePin).setVisibility(View.GONE);

			}

			((TextView) v.findViewById(R.id.articleSubTitle)).setText(Util
					.buildSubTitle(article, (DatabaseActivity) getActivity()));

			if (0 != specializedArticle.imageFilenames.size()) {
				String thumbnailFileName = Sync
						.getThumbnailsPath((DatabaseActivity) getActivity())
						+ "/" + specializedArticle.imageFilenames.get(0);

				Util.logDebug("", "Setting thumbnail: " + thumbnailFileName);

				ImageView imageView = ((ImageView) v
						.findViewById(R.id.articleImage));

				LinearLayout.LayoutParams params = null;
				if (Util.isTablet((DatabaseActivity) getActivity())) {
					params = new LinearLayout.LayoutParams(Util.getPixels(150,
							(DatabaseActivity) getActivity()), Util.getPixels(
							150, (DatabaseActivity) getActivity()));
				} else {
					params = new LinearLayout.LayoutParams(Util.getPixels(80,
							(DatabaseActivity) getActivity()), Util.getPixels(
							80, (DatabaseActivity) getActivity()));
				}

				imageView.setLayoutParams(params);
				imageView.setImageBitmap(BitmapFactory
						.decodeFile(thumbnailFileName));
				imageView.refreshDrawableState();

			} else {
				int id = -1;

				id = R.drawable.placeholder;

				ImageView imageView = ((ImageView) v
						.findViewById(R.id.articleImage));

				imageView.setImageResource(id);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						Util.getPixels(1, (DatabaseActivity) getActivity()),
						Util.getPixels(80, (DatabaseActivity) getActivity()));

				imageView.setLayoutParams(params);

			}

			if (article.isFavorit
					|| (article.user != null && article.user.id
							.equals(getActivity().getSharedPreferences("LOGIN",
									Context.MODE_PRIVATE).getInt("userID", -1)))) {
				((ImageView) v.findViewById(R.id.articleFavorit))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_fav_oranje));
			} else {
				((ImageView) v.findViewById(R.id.articleFavorit))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_fav_blue));
			}

			((LinearLayout) v.findViewById(R.id.articleFavoritLayout))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								if (Util.isUserAuthentificated(getActivity())) {
									new ManageFavoritTask().execute(article);
								} else {
									startActivity(new Intent(
											(DatabaseActivity) getActivity(),
											Login.class));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
			v = new TextView((DatabaseActivity) getActivity());
			((TextView) v).setText("AAARGHH!!!: "
					+ (null != e.getMessage() ? e.getMessage() : "No Message"));
		}

		return v;
	}

	private class loadOneArticleTask extends AsyncTask<Article, Void, Intent> {

		private ProgressDialog dialog = null;

		private int type = 0;

		public loadOneArticleTask(int type) {
			this.type = type;
		}

		@Override
		protected Intent doInBackground(Article... params) {
			Intent intent = new Intent(getActivity(), ArticleDetails.class);

			ArrayList<Article> tmpArticles = (ArrayList<Article>) ((type == 0) ? newsArticles
					: eventArticles);

			intent.putExtra(ArticleDetails.ARTICLE_SWITCH, false);
			intent.putExtra(
					com.your.name.activities.ArticleDetails.ARTICLE_LIST,
					tmpArticles);
			intent.putExtra(
					com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
					params[0].id);
			return intent;
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(getActivity(), "", Util
						.getApplicationString("label.message_loading_articles",
								getActivity()));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		protected void onPostExecute(Intent result) {
			startActivityForResult(result, 100);
			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}

	}

	public class ManageFavoritTask extends AsyncTask<Article, Void, Article> {
		private ProgressDialog dialog = null;

		@Override
		protected Article doInBackground(Article... params) {
			try {
				if (Util.isUserAuthentificated(getActivity())) {
					boolean changed = false;

					SharedPreferences pref = getActivity()
							.getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

					if (!params[0].isFavorit) {
						changed = API.getInstance(getActivity()).addFavorite(
								params[0].id, getActivity());
						if (changed) {
							if (FavoritManager.addFavoritToDB(params[0].id,
									pref.getInt("userID", -1),
									(DatabaseActivity) getActivity())) {
								params[0].isFavorit = true;
							}
						}
					} else {

						changed = API.getInstance(getActivity())
								.removeFavorite(params[0].id, getActivity());
						if (changed) {
							if (FavoritManager.removeFavoritFromDB(
									params[0].id, pref.getInt("userID", -1),
									(DatabaseActivity) getActivity())) {
								params[0].isFavorit = false;
							}
						}
					}
				} else {
					publishProgress();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return params[0];
		}

		@Override
		protected void onPostExecute(Article result) {
			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
			try {

				AdapterUtils.fillLinearLayoutFromAdapter(1, eventList,
						new EventAdapter(), getActivity(), true, true, 2);

				AdapterUtils.fillLinearLayoutFromAdapter(1, newsList,
						new NewsAdapter(), getActivity(), true, true, 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(getActivity(), "", Util
						.getApplicationString("label.article_edit",
								getActivity()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}
}
