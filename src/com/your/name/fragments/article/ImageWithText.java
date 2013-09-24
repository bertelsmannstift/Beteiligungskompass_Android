/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
import com.your.name.dao.Article.ArticleBase;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.service.API;

/*
 * This fragment will be use by the Article Detail view. It's only visible in the
 * landscape mode. In Portrait mode we use the Image.java fragment. 
 */
public class ImageWithText extends Fragment {

	private static String logTag = ImageWithText.class.toString();

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static ImageWithText newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		ImageWithText ret = new ImageWithText();
		ret.setArguments(bundle);

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_image_with_text,
				container, false);
		Bundle args = getArguments();

		Article article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			preparePreviews(article, v);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return v;
	}

	/**
	 * 
	 * @param article
	 * @param v
	 * @throws Exception
	 * 
	 *             checks which articles have to show up in the fragment. First we
	 *             take a look at the type of the article. This type defines the
	 *             text that must be shown as text. After that, it loads the
	 *             image preview
	 */
	protected void preparePreviews(final Article article, View v)
			throws Exception {

		Util.logDebug(logTag, "Images.preparePreviews()");

		boolean noText = true;
		WebView textView = ((WebView) v.findViewById(R.id.text));
		if (article.type.equals(Article.EXPERT)) {
			textView.loadDataWithBaseURL(null,
					article.short_description_expert, "text/html", "UTF-8",
					null);
			if (article.short_description != null
					&& !article.short_description_expert.equals("")) {
				noText = false;
			}
		} else if (article.type.equals(Article.QA)) {
			textView.loadDataWithBaseURL(null, article.question, "text/html",
					"UTF-8", null);

			if (article.question != null && !article.question.equals("")) {
				noText = false;
			}
		} else if (article.type.equals(Article.NEWS)) {
			textView.loadDataWithBaseURL(null, article.intro, "text/html",
					"UTF-8", null);

			if (article.intro != null && !article.intro.equals("")) {
				noText = false;
			}
		} else {
			textView.loadDataWithBaseURL(null, article.short_description,
					"text/html", "UTF-8", null);

			if (article.short_description != null
					&& !article.short_description.equals("")) {
				noText = false;
			}
		}

		DatabaseActivity activity = (DatabaseActivity) getActivity();

		final ArticleBase base = article.new ArticleBase(activity);
		if (base.imageFilenames.size() > 0) {
			v.setVisibility(View.VISIBLE);

			try {

				if (base.imageFilenames != null
						&& base.imageFilenames.size() > 1) {
					((TextView) v.findViewById(R.id.moreImages)).setText(Util
							.getApplicationString("label.show_more_images",
									getActivity()));
				} else {
					v.findViewById(R.id.moreImages).setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Util.logDebug(logTag, "There's images!!!");

			v.findViewById(R.id.image).setVisibility(View.VISIBLE);

			String thumbPath = Sync.getThumbnailsPath(activity)
					+ base.imageFilenames.get(0);

			Util.logDebug(logTag, "thumb path: " + thumbPath);

			ImageView bigPreviewView = (ImageView) v.findViewById(R.id.image);

			bigPreviewView.setImageBitmap(BitmapFactory.decodeFile(thumbPath));

			bigPreviewView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (Util.isWifiConnected(getActivity())) {
						Intent intent = new Intent(getActivity(), Viewer.class);
						intent.putStringArrayListExtra(Viewer.LIST_FILES,
								(ArrayList<String>) base.imageFilenames);
						intent.putExtra(Viewer.CURRENT_POS, 0);
						intent.putExtra(Viewer.URL_EXTRA,
								API.getInstance(getActivity()).host + "/media/"
										+ base.imageFilenames.get(0));

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

		adapter = new ImagePreviewsAdapter(base);

		LinearLayout blockLayout = (LinearLayout) v
				.findViewById(R.id.previewsLayout);

		AdapterUtils.fillLinearLayoutFromAdapter(3, blockLayout, adapter,
				getActivity(), true, true);

		if (0 == base.imageFilenames.size() && noText) {
			v.setVisibility(View.GONE);
		}
	}

	/**
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
