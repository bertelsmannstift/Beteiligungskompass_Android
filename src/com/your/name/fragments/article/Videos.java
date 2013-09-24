/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.AdapterUtils;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.Sync;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;

/*
 * this fragment will be used to show videos in the article details
 */
public class Videos extends Fragment {

	private static String logTag = Text.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Videos newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Videos ret = new Videos();
		ret.setArguments(bundle);

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_thumbnails,
				container, false);

		Bundle args = getArguments();

		Article article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			preparePreviews(article.new ArticleBase(
					(DatabaseActivity) getActivity()), v);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return v;
	}

	protected void preparePreviews(final Article.ArticleBase article, View v)
			throws Exception {

		/*
		 * TODO: replace with string from i18n
		 */
		((TextView) v.findViewById(R.id.title)).setText("Videos");

		DatabaseActivity activity = (DatabaseActivity) getActivity();

		if (article.videoURLs.size() > 0) {
			v.setVisibility(View.VISIBLE);

			Util.logDebug(logTag, "There's videos!!!");

			v.findViewById(R.id.bigPreview).setVisibility(View.VISIBLE);

			/*
			 * find thumbnail for the video
			 */
			Pair<String, String> video = Util
					.getVideoIDFromURL(article.videoURLs.get(0));

			String thumbPath = Sync.getVideoThumbnailsPath(activity)
					+ video.second + "_" + video.first + ".jpg";

			Util.logDebug(logTag, "thumb path: " + thumbPath);

			ImageView bigPreviewView = (ImageView) v
					.findViewById(R.id.bigPreview);

			bigPreviewView.setImageBitmap(BitmapFactory.decodeFile(thumbPath));

			bigPreviewView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(article.videoURLs.get(0)));
					startActivity(intent);
				}
			});
		}

		BaseAdapter adapter = null;

		adapter = new VideoPreviewsAdapter(article);

		LinearLayout blockLayout = (LinearLayout) v
				.findViewById(R.id.previewsLayout);

		AdapterUtils.fillLinearLayoutFromAdapter(3, blockLayout, adapter,
				getActivity(), true, true);

		if (0 == article.videoURLs.size()) {
			v.setVisibility(View.GONE);
		}
	}

	/*
	 * This adapter is a little "special" since the first video URL encountered
	 * is displayed in the big preview it will not be repeated here
	 */
	private class VideoPreviewsAdapter extends BaseAdapter {
		Article.ArticleBase article = null;

		// LayoutInflater inflater;

		public VideoPreviewsAdapter(Article.ArticleBase article) {
			this.article = article;
			// inflater = ((LayoutInflater) getActivity().getSystemService(
			// Activity.LAYOUT_INFLATER_SERVICE));
		}

		@Override
		public int getCount() {
			if (article.videoURLs.size() > 1) {
				return article.videoURLs.size() - 1;
			}

			return 0;
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
			ImageView v = new ImageView(getActivity());

			final int pos = position + 1;

			try {
				Pair<String, String> video;

				video = Util.getVideoIDFromURL(article.videoURLs.get(pos));

				String thumbPath = Sync
						.getVideoThumbnailsPath(((DatabaseActivity) getActivity()))
						+ video.second + "_" + video.first + ".jpg";

				v.setImageBitmap(BitmapFactory.decodeFile(thumbPath));

				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(article.videoURLs.get(pos)));
						startActivity(intent);
					}
				});

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return v;
		}

	}

}
