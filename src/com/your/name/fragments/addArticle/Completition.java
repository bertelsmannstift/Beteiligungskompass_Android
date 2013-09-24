/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleList;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.FavoritList;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;
import com.your.name.dao.ArticleLink;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.File;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.fragments.dashboard.Navigation;
import com.your.name.service.API;

/**
 * this fragment handles the final step of the "add new article" process. This is the
 * last possibility to make changes or remove all. After that the article will be
 * sent to the server and the article will be saved to the database.
 */
public class Completition extends Fragment {

	private Article article;
	private NavigationItem item;

	public Completition() {
	}

	public Completition(NavigationItem item) {
		this.item = item;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			article = (Article) getActivity().getIntent().getSerializableExtra(
					NewArticle.ARTICLE);
			if (article == null) {
				article = NewArticle.article;
			}

			View v = inflater
					.inflate(R.layout.fragment_newarticle_completition,
							container, false);
			((TextView) v.findViewById(R.id.saveButton)).setText(Util
					.getApplicationString("label.article_save", getActivity()));

			((TextView) v.findViewById(R.id.deleteButton))
					.setText(Util.getApplicationString("label.article_delete",
							getActivity()));
			RadioGroup group = ((RadioGroup) v.findViewById(R.id.radioGroup));

			RadioButton inactiveArticle = (RadioButton) group
					.findViewById(R.id.radioArticleInactiv);
			inactiveArticle.setText(Util.getApplicationString(
					"article.inactive", getActivity()));
			RadioButton activButton = (RadioButton) group
					.findViewById(R.id.radioArticlePublish);
			activButton.setText(Util.getApplicationString("article.active",
					getActivity()));

			group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					if (checkedId == R.id.radioArticleInactiv) {
						article.active = false;
						article.ready_for_publish = false;

					} else if (checkedId == R.id.radioArticlePublish) {
						article.active = false;
						article.ready_for_publish = true;
					}
				}
			});

			Button save = (Button) v.findViewById(R.id.saveButton);

			save.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Images.mMemoryCache = null;
					new saveArticleTask().execute();
				}
			});

			Button delete = (Button) v.findViewById(R.id.deleteButton);

			delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Images.mMemoryCache = null;
					new deleteArticleTask().execute();
				}
			});

			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return super.onCreateView(inflater, container, savedInstanceState);
		}

	}

	private class saveArticleTask extends AsyncTask<Void, Article, Article> {

		private ProgressDialog dialog = null;

		@Override
		protected void onProgressUpdate(Article... values) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(getActivity(),
									FavoritList.class);
							ArticleManager.showOnlyLocaleArticle = true;
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra(Navigation.NAVIGATION_ITEM, item);
							startActivity(intent);

							item = null;
							article = null;
							NewArticle.article = null;
							getActivity().finish();

							if (dialog != null)

								dialog.dismiss();
						}
					});

			try {
				if (!article.locale) {

					String message = "";
					if (article.ready_for_publish) {
						try {
							message = Util.getApplicationString(
									"label.dialog_send_article_for_publishing",
									getActivity());
						} catch (Exception ex) {
							message = "Der Artikel wurde zur Freigabe versendet.\nBis zur Freigabe wird dieser Artikel nicht angezeigt";
						}
					} else {
						try {
							message = Util.getApplicationString(
									"label.dialog_send_article_for_editing",
									getActivity());
						} catch (Exception ex) {
							message = "Der Artikel wurde versendet.\nBis zur Veröffentlichung wird dieser nicht auf dem Gerät angezeigt.";
						}
					}

					builder.setMessage(message);
				} else {
					try {
						builder.setMessage(Util.getApplicationString(
								"label.error_send_article", getActivity()));
					} catch (Exception e) {
						builder.setMessage("Der Artikel konnte nicht versendet werden.\nIhr Artikel wurde Lokal gespeichert und es wird versucht beim nächsten Synchronisieren diesen zu versenden.");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			builder.create().show();
		}

		@Override
		protected void onPostExecute(Article result) {
			if (!isCancelled() && dialog != null) {
				dialog.dismiss();
			}

			try {
				JSONArray imageArray = new JSONArray();
				for (File f : article.imageFiles) {

					Dao<File, Integer> fileDAO = ((DatabaseActivity) getActivity()).helper
							.getDao(File.class);

					fileDAO.create(f);
					String uri = f.uri;
					f = fileDAO.queryForMatching(f).get(0);
					f.uri = uri;
					JSONObject object = new JSONObject();

					object.put("id", f.id);
					object.put("description", "");

					imageArray.put(object);

					Bitmap scaledBitmap = Util.getScaledBitmap(BitmapFactory
							.decodeStream(getActivity().getContentResolver()
									.openInputStream(Uri.parse(f.uri))));

					// Save image in folder
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();

					scaledBitmap
							.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

					java.io.File thumbDir = new java.io.File(getActivity()
							.getExternalCacheDir().getAbsolutePath()
							+ "/thumbs/200x/" + f.id + "-" + f.filename);

					thumbDir.createNewFile();

					FileOutputStream stream = new FileOutputStream(thumbDir);

					stream.write(bytes.toByteArray());

					stream.close();
					bytes.close();
					thumbDir = null;

				}
				article.images = imageArray.toString();

				Dao<Article, Integer> articleDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(Article.class);

				articleDAO.createOrUpdate(article);

			} catch (Exception e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			if (!isCancelled()) {
				try {
					dialog = ProgressDialog.show(getActivity(), "", Util
							.getApplicationString("label.message_save_article",
									getActivity()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.onPreExecute();
		}

		@Override
		protected Article doInBackground(Void... params) {

			try {
				if (!API.getInstance(getActivity()).addArticle(article,
						getActivity())) {
					article.locale = true;
				} else {
					article.locale = false;
				}

				JSONArray links = new JSONArray();

				for (String string : article.externalLinks) {
					if (!string.equals("")) {
						continue;
					}
					JSONObject object = new JSONObject();

					object.put("url", string);
					object.put("show_link", false);
					links.put(object);
				}

				article.external_links = links.toString();
				Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
						.getDao(Article.class);
				article.active = false;
				dao.createOrUpdate(article);

				publishProgress(article);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return article;
		}
	}

	private class deleteArticleTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(Void result) {
			if (!isCancelled() && dialog != null) {
				dialog.dismiss();
			}

			Intent intent = new Intent(getActivity(), ArticleList.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Navigation.NAVIGATION_ITEM, item);
			startActivity(intent);

			item = null;
			article = null;
			NewArticle.article = null;

			getActivity().finish();

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			if (!isCancelled()) {
				try {
					dialog = ProgressDialog.show(getActivity(), "", Util
							.getApplicationString(
									"label.message_delete_article",
									getActivity()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Dao<ArticleLink, Integer> linkDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(ArticleLink.class);

				DeleteBuilder<ArticleLink, Integer> deleteBuilder = linkDAO
						.deleteBuilder();

				deleteBuilder.where().eq("article_id", article.id);

				linkDAO.delete(deleteBuilder.prepare());

				Dao<ArticlesOption, Integer> optionDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(ArticleLink.class);

				DeleteBuilder<ArticlesOption, Integer> option = optionDAO
						.deleteBuilder();

				optionDAO.delete(option.prepare());

				Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
						.getDao(Article.class);

				dao.delete(article);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
