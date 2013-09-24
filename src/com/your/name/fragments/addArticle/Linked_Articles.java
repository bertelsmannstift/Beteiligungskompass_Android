/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleList;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.NavigationActivity;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;
import com.your.name.dao.ArticleLink;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.service.API;

/*
 * links the new article with other articles 
 */
public class Linked_Articles extends Fragment {

	private LinkedArticleAdapter adapter;

	private static ArrayList<NavigationItem> items;
	private static NavigationItem item;
	private Article article;

	public Linked_Articles() {
	}

	public Linked_Articles(NavigationItem item) {
		Linked_Articles.item = item;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		article = (Article) getActivity().getIntent().getSerializableExtra(
				NewArticle.ARTICLE);
		if (article == null) {
			article = NewArticle.article;
		}

		// no linked article found in the object, now we check if an article is
		// available
		if (article.linkedArticles.size() == 0) {
			try {
				Dao<ArticleLink, Integer> linkedArticles = ((DatabaseActivity) getActivity()).helper
						.getDao(ArticleLink.class);

				QueryBuilder<ArticleLink, Integer> linkBuilder = linkedArticles
						.queryBuilder();

				linkBuilder.selectColumns("article_linked_id").where()
						.eq("article_id", article.id);

				Dao<Article, Integer> articleDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(Article.class);

				QueryBuilder<Article, Integer> builder = articleDAO
						.queryBuilder();
				article.linkedArticles = builder.selectColumns("id", "type")
						.where().in("id", linkBuilder).query();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		items = API.getInstance(getActivity()).getNavigationItems(
				(NavigationActivity) getActivity());

		View v = inflater.inflate(R.layout.fragment_newarticle_linked_articles,
				container, false);
		try {
			((Button) v.findViewById(R.id.nextStepButton))
					.setText(Util.getApplicationString("label.save_and_next",
							getActivity()));

			((Button) v.findViewById(R.id.backButton)).setText(Util
					.getApplicationString("label.back", getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ListView listView = (ListView) v.findViewById(R.id.articleList);

		adapter = new LinkedArticleAdapter();

		listView.setAdapter(adapter);

		listView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);

		Button backButton = (Button) v.findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NewArticle.article = article;
				Intent intent = new Intent(getActivity(), NewArticle.class);
				intent.putExtra(NewArticle.NEXT_STEP, 3);
				intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
				intent.putExtra(ArticleList.ITEM_TPE, item);
				startActivity(intent);
				getActivity().finish();

			}
		});

		Button nextStep = (Button) v.findViewById(R.id.nextStepButton);

		nextStep.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NewArticle.article = article;
				Intent intent = new Intent(getActivity(), NewArticle.class);
				intent.putExtra(NewArticle.NEXT_STEP, 5);
				intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
				intent.putExtra(ArticleList.ITEM_TPE, item);
				startActivity(intent);
			}
		});

		return v;
	}

	private class LinkedArticleHolder {
		TextView title, selectedText;
	}

	private class LinkedArticleAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return items.size();
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

			LinkedArticleHolder holder;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.filter_list_item_type_check, null);
				holder = new LinkedArticleHolder();

				holder.title = ((TextView) v.findViewById(R.id.title));
				holder.selectedText = ((TextView) v
						.findViewById(R.id.selectionButton));

				v.setTag(holder);
			} else {
				holder = (LinkedArticleHolder) v.getTag();
			}
			try {

				final int pos = position;

				holder.title.setText(items.get(position).name);

				String text = Util.getApplicationString(
						"global.select_mone_selected_text", getActivity());
				int counter = 0;
				for (Article art : article.linkedArticles) {
					if (art.type.equals(items.get(position).type)) {
						counter++;
					}

				}
				if (counter > 0) {
					text = counter + " ausgewählt";
				}
				holder.selectedText.setText(text);

				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Dialog selectionDialog;

						AlertDialog.Builder dialog = new AlertDialog.Builder(
								getActivity());

						{
							View dialogContentView = getActivity()
									.getLayoutInflater()
									.inflate(
											R.layout.filter_list_item_choices_dialog,
											null);

							ListView choicesListView = (ListView) dialogContentView
									.findViewById(R.id.choicesList);

							List<Article> listOfArticles = null;
							try {
								listOfArticles = ArticleManager.getArticles(
										(DatabaseActivity) getActivity(),
										items.get(pos).type);
							} catch (Exception e) {
								e.printStackTrace();
							}

							try {
								dialog.setView(dialogContentView);
								dialog.setTitle(items.get(pos).name);
								dialog.setNeutralButton(Util
										.getApplicationString("label.close",
												Linked_Articles.this
														.getActivity()),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												if (dialog != null) {
													dialog.dismiss();
												}

											}
										});
							} catch (Exception e) {
								e.printStackTrace();
							}
							selectionDialog = dialog.create();

							LinkedArticleOptionAdapter optionAdapter = new LinkedArticleOptionAdapter(
									listOfArticles, selectionDialog);

							choicesListView.setAdapter(optionAdapter);

						}

						selectionDialog.show();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			return v;
		}
	}

	private class LinkedArticleOptionHolder {
		CheckBox box;
		TextView text;
	}

	/*
	 * The items getting displayed in a Dialog that enables choice of options
	 */
	class LinkedArticleOptionAdapter extends BaseAdapter {
		Dialog dialog;
		ArrayList<Article> articles;

		public LinkedArticleOptionAdapter(List<Article> articles, Dialog dialog) {
			this.articles = (ArrayList<Article>) articles;
			this.dialog = dialog;
		}

		@Override
		public int getCount() {
			return articles.size();
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
		public View getView(int position, View arg1, ViewGroup arg2) {
			Article linkedArticle = articles.get(position);

			LinkedArticleOptionHolder holder;

			View v = arg1;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.filter_list_item_dialog_item_with_checkbox,
						null);

				holder = new LinkedArticleOptionHolder();

				holder.box = ((CheckBox) v.findViewById(R.id.checkbox));
				holder.text = ((TextView) v.findViewById(R.id.title));
				holder.text.setTextColor(getResources().getColor(
						android.R.color.black));

				holder.text.setVisibility(View.GONE);

				v.setTag(holder);
			} else {
				holder = (LinkedArticleOptionHolder) v.getTag();
			}
			int articleisLinked = 0;
			try {
				Dao<ArticleLink, Integer> linkdao = ((DatabaseActivity) getActivity()).helper
						.getDao(ArticleLink.class);

				QueryBuilder<ArticleLink, Integer> qb = linkdao.queryBuilder();

				qb.setCountOf(true);

				qb.where().eq("article_id", article.id).and()
						.eq("article_linked_id", linkedArticle.id);

				articleisLinked = (int) linkdao.countOf(qb.prepare());
				holder.box.setTag(linkedArticle);
				holder.box.setOnCheckedChangeListener(null);
				holder.box.setChecked((articleisLinked == 0) ? false : true);

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			holder.box
					.setText((linkedArticle.title == null || linkedArticle.title
							.length() == 0) ? linkedArticle.institution
							: linkedArticle.title);
			holder.box.setTextColor(getResources().getColor(
					android.R.color.black));
			holder.box
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							try {
								Article tmp = ((Article) buttonView.getTag());

								Dao<ArticleLink, Integer> dao = ((DatabaseActivity) getActivity()).helper
										.getDao(ArticleLink.class);
								ArticleLink link = new ArticleLink();
								link.article_id = article.id;
								link.article_linked_id = tmp.id;

								if (isChecked) {

									if (dao.create(link) == 1) {
										article.linkedArticles.add(tmp);
									} else {
										Util.logDebug(
												"LINKED ARTICLE INSERT ERROR",
												"ERRROR");
									}
								} else {

									DeleteBuilder<ArticleLink, Integer> qb = dao
											.deleteBuilder();

									qb.where().eq("article_id", article.id)
											.and()
											.eq("article_linked_id", tmp.id);

									if (dao.delete(qb.prepare()) == 1) {

									} else {
										Util.logDebug(
												"LINKED ARTICLE DELETE ERROR",
												"ERRROR");
									}

									for (Article art : article.linkedArticles) {
										if (art.id.equals(tmp.id)) {
											article.linkedArticles.remove(art);
										}
									}

								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							adapter.notifyDataSetChanged();
						}
					});
			return v;
		}
	}
}
