/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.your.name.AdapterUtils;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.Sync;
import com.your.name.activities.Viewer;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.service.API;

/*
 * this fragment displays the images in the article details but beware, only in
 * the portrait orientation. In Landscape mode, we have to take the
 * ImageWithText fragment
 */
public class Images extends Fragment {

	private static String logTag = Text.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Images newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Images ret = new Images();
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
			e1.printStackTrace();
		}

		return v;
	}

	protected void preparePreviews(final Article.ArticleBase article, View v)
			throws Exception {

		Util.logDebug(logTag, "Images.preparePreviews()");

		try {
			((TextView) v.findViewById(R.id.title)).setText(Util
					.getApplicationString("label.images", getActivity())
					+ " ("
					+ article.imageFilenames.size() + ")");
			if (article.imageFilenames != null
					&& article.imageFilenames.size() > 1) {
				((TextView) v.findViewById(R.id.moreImages))
						.setText("weitere Bilder nach Öffnen");
			} else {
				v.findViewById(R.id.moreImages).setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		DatabaseActivity activity = (DatabaseActivity) getActivity();

		if (article.imageFilenames.size() > 0) {
			v.setVisibility(View.VISIBLE);

			Util.logDebug(logTag, "There's images!!!");

			v.findViewById(R.id.bigPreview).setVisibility(View.VISIBLE);

			String thumbPath = Sync.getThumbnailsPath(activity)
					+ article.imageFilenames.get(0);

			Util.logDebug(logTag, "thumb path: " + thumbPath);

			ImageView bigPreviewView = (ImageView) v
					.findViewById(R.id.bigPreview);

			Bitmap tmpBitmap = BitmapFactory.decodeFile(thumbPath);

			bigPreviewView.setImageBitmap(tmpBitmap);

			bigPreviewView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (Util.isWifiConnected(getActivity())) {
						Intent intent = new Intent(getActivity(), Viewer.class);
						intent.putStringArrayListExtra(Viewer.LIST_FILES,
								(ArrayList<String>) article.imageFilenames);
						intent.putExtra(Viewer.CURRENT_POS, 0);
						intent.putExtra(Viewer.URL_EXTRA,
								API.getInstance(getActivity()).host + "/media/"
										+ article.imageFilenames.get(0));

						startActivity(intent);
					} else {
						Toast.makeText(
								getActivity(),
								getActivity().getString(
										R.string.internet_for_images),
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		BaseAdapter adapter = null;

		adapter = new ImagePreviewsAdapter(article);

		LinearLayout blockLayout = (LinearLayout) v
				.findViewById(R.id.previewsLayout);

		AdapterUtils.fillLinearLayoutFromAdapter(3, blockLayout, adapter,
				getActivity(), true, true);

		if (0 == article.imageFilenames.size()) {
			v.setVisibility(View.GONE);
		}
	}

	/*
	 * This adapter is a little "special" Since the first video URL encountered
	 * is displayed in the big preview it will not be repeated here
	 */
	private class ImagePreviewsAdapter extends BaseAdapter {
		Article.ArticleBase article = null;

		public ImagePreviewsAdapter(Article.ArticleBase article) {
			this.article = article;
		}

		@Override
		public int getCount() {

			return 0;
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
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			View v = convertView;
			if (null == v) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.thumbnail_view, null);
			}

			ImageView imageView = (ImageView) v
					.findViewById(R.id.thumbnailView);

			String thumbPath = Sync.getThumbnailsPath(getActivity())
					+ article.imageFilenames.get((position + 1));

			imageView.setImageBitmap(BitmapFactory.decodeFile(thumbPath));
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					Util.getPixels(50, getActivity()), Util.getPixels(50,
							getActivity()));
			imageView.setLayoutParams(params);
			final int pos = (position + 1);

			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getActivity(), Viewer.class);
					intent.putStringArrayListExtra(Viewer.LIST_FILES,
							(ArrayList<String>) article.imageFilenames);
					intent.putExtra(Viewer.CURRENT_POS, (pos));
					intent.putExtra(Viewer.URL_EXTRA,
							API.getInstance(getActivity()).host + "/media/"
									+ article.imageFilenames.get(pos));

					startActivity(intent);
				}
			});

			return v;
		}

	}

}