/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.AdapterUtils;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleList;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;

/**
 * this fragment will be used to add images to the new article. The image is
 * taken from the file system. After that, all images are saved in the
 * LruCache. That improves the performance because the images have not be loaded
 * again from the file system.
 */
public class Images extends Fragment {

	private Article article;

	public static LruCache<Integer, Bitmap> mMemoryCache;

	private ImageListAdapter adapter;

	private NavigationItem item;
	private Button nextStepButton;
	private View view;

	public static Images newInstance(NavigationItem item) {
		Images img = new Images();

		Bundle bunlde = new Bundle();
		bunlde.putSerializable(ArticleList.ITEM_TPE, item);
		img.setArguments(bunlde);

		return img;
	}

	public Images() {}

	private void updateImageView(Article article) {
		if (adapter == null) {
			adapter = new ImageListAdapter();
		}
		AdapterUtils.fillLinearLayoutFromAdapter(1,
				(LinearLayout) view.findViewById(R.id.listOfImages), adapter,
				getActivity(), true, true);
	}

	private void updateLinkView(Article article) {
		AdapterUtils.fillLinearLayoutFromAdapter(1,
				(LinearLayout) view.findViewById(R.id.listOfLinks),
				new LinkListAdapter(), getActivity(), true, true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Uri targetUri = data.getData();
			// Util.getRealPathFromURI(targetUri, getActivity());
			article.imageFiles.add(Util.getRealPathFromURI(targetUri,
					getActivity()));
			updateImageView(article);
			Util.logDebug("TAG", targetUri.toString());
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			// Get memory class of this device, exceeding this amount will throw
			// an OutOfMemory exception.
			if (mMemoryCache == null) {
				final int memClass = ((ActivityManager) getActivity()
						.getSystemService(Context.ACTIVITY_SERVICE))
						.getMemoryClass();

				// Use 1/8th of the available memory for this memory cache.
				final int cacheSize = 1024 * 1024 * memClass / 8;

				mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
					@Override
					protected int sizeOf(Integer key, Bitmap bitmap) {
						// The cache size will be measured in bytes rather than
						// number of items.
						if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 12)
							return bitmap.getByteCount();
						else
							return (bitmap.getRowBytes() * bitmap.getHeight());
					}
				};
			}

			View v = inflater.inflate(R.layout.fragment_newarticle_images,
					container, false);
			view = v;

			try {
				((Button) v.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) v.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));

				((TextView) v.findViewById(R.id.newArticleImageDesc))
						.setText(Util.getApplicationString(
								"label.newarticle_images", getActivity()));

				((TextView) v.findViewById(R.id.newArticleImageText))
						.setText(Util.getApplicationString(
								"label.newarticle_addImage", getActivity()));

				((TextView) v.findViewById(R.id.newArticleExternalLinkDesc))
						.setText(Util.getApplicationString(
								"label.newarticle_externallink", getActivity()));

				((TextView) v.findViewById(R.id.newArticleExternalLinkText))
						.setText(Util.getApplicationString(
								"label.newarticle_addLink", getActivity()));

			} catch (Exception e) {
				e.printStackTrace();
			}

			article = (Article) getActivity().getIntent().getSerializableExtra(
					NewArticle.ARTICLE);
			updateLinkView(article);
			updateImageView(article);
			item = (NavigationItem) getArguments().getSerializable(
					ArticleList.ITEM_TPE);

			v.findViewById(R.id.imagesLayout).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(intent, 0);

						}
					});

			v.findViewById(R.id.externalLinkLayout).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							article.externalLinks.add("");
							updateLinkView(article);

						}
					});

			if (article == null) {
				article = NewArticle.article;
			}

			Button backButton = (Button) v.findViewById(R.id.backButton);
			backButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mMemoryCache = null;
					NewArticle.article = article;
					Intent intent = new Intent(getActivity(), NewArticle.class);
					intent.putExtra(NewArticle.NEXT_STEP, 2);
					intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
					intent.putExtra(ArticleList.ITEM_TPE, item);
					startActivity(intent);
					getActivity().finish();

				}
			});

			nextStepButton = (Button) v.findViewById(R.id.nextStepButton);
			nextStepButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					NewArticle.article = article;
					Intent intent = new Intent(getActivity(), NewArticle.class);
					intent.putExtra(NewArticle.NEXT_STEP, 4);
					intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
					intent.putExtra(ArticleList.ITEM_TPE, item);
					startActivity(intent);

				}
			});

			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	public void addBitmapToMemoryCache(int key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(int key) {
		return mMemoryCache.get(key);
	}

	private class ImageListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return article.imageFiles.size();
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
			final int pos = position;
			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.layout_newarticle_image_view, null);

				Bitmap scaledBitmap = null;
				try {
					scaledBitmap = getBitmapFromMemCache(pos);

					if (scaledBitmap == null) {
						scaledBitmap = Util.getScaledBitmap(BitmapFactory
								.decodeStream(getActivity()
										.getContentResolver().openInputStream(
												Uri.parse(article.imageFiles
														.get(position).uri))));

						addBitmapToMemoryCache(pos, scaledBitmap);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					((TextView) v.findViewById(R.id.removeImage)).setText(Util
							.getApplicationString(
									"label.newarticle_removeImage",
									getActivity()));
				} catch (Exception e) {
					e.printStackTrace();
				}

				ImageView image = (ImageView) v.findViewById(R.id.image);

				image.setImageBitmap(scaledBitmap);

				TextView removeImage = (TextView) v
						.findViewById(R.id.removeImage);
				removeImage.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						article.imageFiles.remove(pos);
						mMemoryCache.remove(pos);
						updateImageView(article);
					}
				});

			}

			return v;
		}
	}

	private class LinkListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return article.externalLinks.size();
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
			final int pos = position;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.layout_newarticle_externallinks, null);
			}
			try {
				((TextView) v.findViewById(R.id.removeLink)).setText(Util
						.getApplicationString("label.newarticle_removeLink",
								getActivity()));
			} catch (Exception e) {
				e.printStackTrace();
			}

			final EditText link = (EditText) v.findViewById(R.id.externalLink);
			link.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if (URLUtil.isValidUrl(s.toString())) {
						article.externalLinks.set(pos, s.toString());
					} else {
						link.setError("Is not a valid URL");
					}

				}
			});
			link.setText(article.externalLinks.get(pos));

			TextView removeLink = (TextView) v.findViewById(R.id.removeLink);
			removeLink.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					article.externalLinks.remove(pos);
					updateLinkView(article);
				}
			});
			return v;
		}
	}

}
