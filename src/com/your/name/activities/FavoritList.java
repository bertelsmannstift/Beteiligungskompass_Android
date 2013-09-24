/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.ResultValues;
import com.your.name.Util;
import com.your.name.dao.Article;
import com.your.name.dao.Criterion;
import com.your.name.dao.FavoriteGroup;
import com.your.name.dao.helper.DataObserver;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.fragments.FavoritFilterList;
import com.your.name.service.API;

public class FavoritList extends NavigationActivity implements DataObserver {
	private static String logTag = FavoritList.class.toString();

	public static FavoriteGroup group = null;

	private List<Criterion> criteria = null;

	private List<FavoriteGroup> favGroups = null;

	private ExpandableListView listView = null;

	private FavoritListAdapter adapter = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_favoritlist);

		FavoritManager.observable.registerObserver(this);

		this.findViewById(R.id.tabDataLayout).setBackgroundResource(
				R.color.background_dark_highlighted);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		/*
		 * Add the filterfragment in every case. But only if the activity is not restarted afterwards. 
		 * Example: The orientation changes, because fragments get automatically re-added by the FragmentManager
		 */
		if (null == icicle) {
			try {

				SharedPreferences pref = FavoritList.this.getSharedPreferences(
						"LOGIN", Context.MODE_PRIVATE);
				pref.edit().putBoolean("showOnlyMyArticle", false).commit();

				FavoritFilterList filterList = FavoritFilterList
						.newInstance(pref.getInt("userID", -1));

				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();

				transaction.add(R.id.filterListFragmentContainer, filterList);

				transaction.commit();

			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 * In the landscape case we display the fragment. In the other case
			 * we don't
			 */
			if (Util.isOrientationPortrait(this)
					|| false == Util.isTablet(this)) {
				findViewById(R.id.filterListFragmentContainer).setVisibility(
						View.GONE);
			} else {
				findViewById(R.id.filterListFragmentContainer).setVisibility(
						View.VISIBLE);
			}
		}

		/*
		 * In the landscape case we display the fragment. In the other case we
		 * don't
		 */
		if (Util.isOrientationPortrait(this) || false == Util.isTablet(this)) {
			findViewById(R.id.filterListFragmentContainer).setVisibility(
					View.GONE);
		} else {
			findViewById(R.id.filterListFragmentContainer).setVisibility(
					View.VISIBLE);
		}

		dataChanged();
	}

	@Override
	public void dataChanged() {
		Util.logDebug(logTag, "datachange - foo");
		new LoadFavoritTask().execute();

	}

	public void addGroup(View v, ListView listView) {
		EditText groupNameEdit = (EditText) v
				.findViewById(R.id.addGroupNameEdit);

		String groupName = groupNameEdit.getText().toString();

		groupNameEdit.setText("");

		/*
		 * simple sanity check.
		 */
		if (groupName.equals("")) {
			Util.logDebug(logTag, "empty groupname - not adding");
			return;
		}

		FavoriteGroup favoriteGroup = new FavoriteGroup();
		favoriteGroup.name = groupName;
		favoriteGroup.user_id = getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE).getInt("userID", -1);
		favoriteGroup.created = new Date();
		((FavoriteGroupAdapter) listView.getAdapter()).favGroup
				.add(favoriteGroup);
		((FavoriteGroupAdapter) listView.getAdapter()).notifyDataSetChanged();
		listView.refreshDrawableState();

		try {
			FavoritFilterList list = new FavoritFilterList();
			list.new AddOrRemoveFavoriteGroup(favoriteGroup, false, this)
					.execute(true);
		} catch (Exception e) {
			Util.logDebug(logTag, "adding the group failed");
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if (findViewById(R.id.navbarLayout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.navbarLayout).setVisibility(View.GONE);
		} else {
			getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit()
					.putBoolean("showOnlyMyArticle", false).commit();
			FavoritManager.showOnlyMineInFavorites = false;
			FavoritManager.showUnassignedInFavorites = false;
			FavoritManager.showOnlyLocaleArticle = false;
			FavoritManager.groupToShowInFavorites = -1;
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setupActionbar();
		((TextView) this.findViewById(R.id.navbarTitle)).setText("Favoriten");
	}

	/*
	 * This adapter shows the Favorite groups
	 */
	private class FavoriteGroupAdapter extends BaseAdapter implements
			SpinnerAdapter {

		private Article article;
		private List<FavoriteGroup> favGroup;

		public FavoriteGroupAdapter(List<FavoriteGroup> favGroup,
				Article article, FavoritListAdapter favAdapter) {
			this.favGroup = favGroup;
			this.article = article;
		}

		@Override
		public int getCount() {
			return this.favGroup.size();
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
			convertView = getLayoutInflater().inflate(
					R.layout.filter_list_item_spinner_item, null);
			final int pos = position;
			TextView textView = ((TextView) convertView
					.findViewById(R.id.title));
			textView.setText(favGroup.get(position).name);
			textView.setTextColor(getResources().getColor(
					R.color.background_dark));
			convertView.setFocusable(true);
			convertView.setClickable(true);

			convertView.setOnClickListener(new AdapterView.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						boolean isAdded = false;
						for (FavoriteGroup group : article.groups) {
							if (group.id.equals(favGroup.get(pos).id)) {
								isAdded = true;
							}
						}
						Util.logDebug("is Added:", isAdded + "");
						if (!isAdded) {
							new addOrRemoveArticleTask(article, favGroup
									.get(pos)).execute(true);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			return convertView;
		}

	}

	/*
	 * Add or remove an article as a favorite
	 */
	private class addOrRemoveArticleTask extends
			AsyncTask<Boolean, ResultValues, Boolean> {

		private ProgressDialog dialog = null;

		private Article _article;
		private FavoriteGroup _externGroup;

		public addOrRemoveArticleTask(Article article, FavoriteGroup externgroup) {
			this._article = article;
			this._externGroup = externgroup;
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			if (params[0]) {
				if (_article.user.id.equals(getSharedPreferences("LOGIN",
						Context.MODE_PRIVATE).getInt("userID", -1))) {
					try {
						if (API.getInstance(FavoritList.this).addFavorite(
								_article.id, FavoritList.this)) {
							if (API.getInstance(FavoritList.this)
									.add_article_from_favoriteGroup(
											_article.id,
											_externGroup.extern_id,
											FavoritList.this)) {
								FavoritManager.addArticleToGroup(_article.id,
										_externGroup, FavoritList.this);
								publishProgress(ResultValues.RESULT_OK);
							} else {
								publishProgress(ResultValues.RESULT_NO_INTERNET);
							}
						} else {
							publishProgress(ResultValues.RESULT_NO_INTERNET);
						}
					} catch (Exception e) {
						publishProgress(ResultValues.RESULT_EXCEPTION);
					}

				} else if (API.getInstance(FavoritList.this)
						.add_article_from_favoriteGroup(_article.id,
								_externGroup.extern_id, FavoritList.this)) {
					try {
						FavoritManager.addArticleToGroup(_article.id,
								_externGroup, FavoritList.this);
						publishProgress(ResultValues.RESULT_OK);
					} catch (Exception e) {
						e.printStackTrace();
						publishProgress(ResultValues.RESULT_EXCEPTION);
					}
					_article.groups.add(_externGroup);
				} else {
					publishProgress(ResultValues.RESULT_NO_INTERNET);
				}
			} else {
				try {
					if (API.getInstance(FavoritList.this)
							.remove_article_from_favoriteGroup(_article.id,
									_externGroup.extern_id, FavoritList.this)) {
						FavoritManager.removeArticleFromGroup(_article.id,
								_externGroup, FavoritList.this);
						_article.groups.remove(_externGroup);
						publishProgress(ResultValues.RESULT_OK);
					} else {
						publishProgress(ResultValues.RESULT_NO_INTERNET);
					}
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(ResultValues.RESULT_EXCEPTION);
				}
			}
			return false;
		}

		@Override
		protected void onProgressUpdate(ResultValues... values) {

			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
			try {
				switch (values[0]) {
				case RESULT_NO_INTERNET:
					Toast.makeText(
							FavoritList.this,
							Util.getApplicationString(
									"label.error_add_favorite_state",
									FavoritList.this), Toast.LENGTH_LONG)
							.show();
					break;
				case RESULT_EXCEPTION:
					break;
				case RESULT_OK:
					FavoritManager.observable.notifyDatasetChanged();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(FavoritList.this, "", Util
						.getApplicationString("label.edit_favorite",
								FavoritList.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * This class shows all favorite articles in a list. They will all group in 6
	 * special types like "event", "expert", "qa", "studies", "news", "methods"
	 */
	private class FavoritListAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return criteria.get(groupPosition).articles.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				v = LayoutInflater.from(FavoritList.this).inflate(
						R.layout.favoritlist_child_layout, null);
			}

			try {

				final Article article = (Article) getChild(groupPosition,
						childPosition);

				Article.ArticleBase specializedArticle = article
						.createSubType(FavoritList.this);

				((TextView) v.findViewById(R.id.favoritTitle))
						.setText(specializedArticle.getTitle());

				((TextView) v.findViewById(R.id.favoritDescription))
						.setText(article.listdescription_plaintext);

				((TextView) v.findViewById(R.id.favoritSubTitle)).setText(Util
						.buildSubTitle(article, FavoritList.this));

				if (false == Util.isTablet(FavoritList.this)) {
					v.findViewById(R.id.indicatorLayout).setVisibility(
							View.GONE);
				}

				if (0 == specializedArticle.videoURLs.size()) {
					v.findViewById(R.id.favoritVideo).setVisibility(View.GONE);
				}

				if (0 == specializedArticle.downloadFilenames.size()) {
					v.findViewById(R.id.favoritDownload).setVisibility(
							View.GONE);
				}

				if (null == article.city || article.city.equals("")) {
					v.findViewById(R.id.favoritPin).setVisibility(View.GONE);

				}

				if (0 != specializedArticle.imageFilenames.size()) {
					String thumbnailFileName = Sync
							.getThumbnailsPath(FavoritList.this)
							+ "/"
							+ specializedArticle.imageFilenames.get(0);

					Util.logDebug(logTag, "Setting thumbnail: "
							+ thumbnailFileName);

					ImageView imageView = ((ImageView) v
							.findViewById(R.id.favoritImage));
					LinearLayout.LayoutParams params = null;
					if (Util.isTablet(FavoritList.this)) {
						params = new LinearLayout.LayoutParams(Util.getPixels(
								150, FavoritList.this), Util.getPixels(150,
								FavoritList.this));
					} else {
						params = new LinearLayout.LayoutParams(Util.getPixels(
								80, FavoritList.this), Util.getPixels(80,
								FavoritList.this));
					}

					imageView.setLayoutParams(params);
					imageView.setImageBitmap(BitmapFactory
							.decodeFile(thumbnailFileName));
					imageView.refreshDrawableState();

				} else {
					int id = -1;

					id = R.drawable.placeholder;

					ImageView imageView = ((ImageView) v
							.findViewById(R.id.favoritImage));

					imageView.setImageResource(id);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							Util.getPixels(1, FavoritList.this),
							Util.getPixels(80, FavoritList.this));

					imageView.setLayoutParams(params);

				}

				/*
				 *  Show the favorite star only, if those are not the user's articles
				 */
				if (!FavoritManager.showOnlyMineInFavorites) {
					((ImageView) v.findViewById(R.id.articleFavorit))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.icon_fav_oranje));
				} else {
					((ImageView) v.findViewById(R.id.articleFavorit))
							.setVisibility(View.GONE);
				}

				LinearLayout localeLayout = (LinearLayout) v
						.findViewById(R.id.selectionLayout);

				if (article.locale) {
					localeLayout.removeAllViews();

					LinearLayout layout = (LinearLayout) FavoritList.this
							.getLayoutInflater().inflate(
									R.layout.layout_edit_article, null);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);

					layout.setLayoutParams(params);

					localeLayout.addView(layout);

					localeLayout
							.setBackgroundResource(android.R.color.transparent);

					((Button) localeLayout.findViewById(R.id.editButton))
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											FavoritList.this, NewArticle.class);

									intent.putExtra(NewArticle.ARTICLE, article);
									intent.putExtra(NewArticle.NEXT_STEP, 1);
									startActivity(intent);

								}
							});

					((Button) localeLayout.findViewById(R.id.deleteButton))
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									try {
										Dao<Article, Integer> articleDAO = ((DatabaseActivity) FavoritList.this).helper
												.getDao(Article.class);

										articleDAO.delete(article);

										FavoritManager.observable
												.notifyDatasetChanged();

									} catch (SQLException e) {
										e.printStackTrace();
									}

								}
							});

				} else {

					final TextView spinner = (TextView) v
							.findViewById(R.id.selectGroupSpinner);

					/*
					 *  Show the favorite star only, if those are not the user's articles
					 */
					if (FavoritManager.showOnlyMineInFavorites) {
						spinner.setVisibility(View.GONE);
						v.findViewById(R.id.selectionLayout).setVisibility(
								View.GONE);
					} else {
						spinner.setVisibility(View.VISIBLE);
						v.findViewById(R.id.selectionLayout).setVisibility(
								View.VISIBLE);
					}

					spinner.setText(Util.getApplicationString(
							"label.select_favorite_group", FavoritList.this));
					(v.findViewById(R.id.selectionLayout))
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									Util.logDebug(logTag,
											"we should show a dialog now");
									Dialog selectionDialog;

									AlertDialog.Builder dialog = new AlertDialog.Builder(
											FavoritList.this);

									{
										final View dialogContentView = FavoritList.this
												.getLayoutInflater()
												.inflate(
														R.layout.filter_list_item_choices_dialog,
														null);

										final ListView choicesListView = (ListView) dialogContentView
												.findViewById(R.id.choicesList);
										dialogContentView.findViewById(
												R.id.newCollectionText)
												.setVisibility(View.VISIBLE);
										dialogContentView.findViewById(
												R.id.newCollectionLayout)
												.setVisibility(View.VISIBLE);

										((EditText) dialogContentView
												.findViewById(R.id.addGroupNameEdit))
												.setOnEditorActionListener(new TextView.OnEditorActionListener() {

													@Override
													public boolean onEditorAction(
															TextView v,
															int actionId,
															KeyEvent event) {
														if (EditorInfo.IME_ACTION_DONE == actionId) {
															Util.logDebug(
																	logTag,
																	"add group");
															addGroup(
																	dialogContentView,
																	choicesListView);
															return true;
														}

														return false;
													}
												});
										try {
											((TextView) dialogContentView
													.findViewById(R.id.newCollectionText)).setText(Util
													.getApplicationString(
															"global.newgroup",
															FavoritList.this));
										} catch (Exception e1) {
											e1.printStackTrace();
										}
										dialogContentView
												.findViewById(
														R.id.addGroupButton)
												.setOnClickListener(
														new View.OnClickListener() {

															@Override
															public void onClick(
																	View v) {
																addGroup(
																		dialogContentView,
																		choicesListView);
															}
														});

										dialog.setView(dialogContentView);
										try {
											dialog.setTitle(Util
													.getApplicationString(
															"label.select_favorite_group",
															FavoritList.this));

											dialog.setNeutralButton(
													Util.getApplicationString(
															"label.close",
															FavoritList.this),
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

										choicesListView
												.setAdapter(new FavoriteGroupAdapter(
														favGroups, article,
														FavoritListAdapter.this));

									}
									selectionDialog.show();
								}
							});
				}

				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							Util.logDebug(logTag,
									"clicked on article with title: "
											+ article.title);

							Intent intent = new Intent(FavoritList.this,
									ArticleDetails.class);
							intent.putExtra(ArticleDetails.ARTICLE_SWITCH,
									false);
							intent.putExtra(
									com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
									article.id);

							startActivity(intent);

						} catch (Exception e) {
							Toast.makeText(getApplicationContext(),
									"Error - Please inform the developer",
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}

					}
				});

				LinearLayout layout = (LinearLayout) v
						.findViewById(R.id.favoritGroupLayout);
				layout.removeAllViews();
				for (FavoriteGroup group : article.groups) {
					final FavoriteGroup tmp = group;
					View child = getLayoutInflater().inflate(
							R.layout.selected_group_child_view, null);

					((TextView) child.findViewById(R.id.favoritGroup))
							.setText(group.name);
					child.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							new addOrRemoveArticleTask(article, tmp)
									.execute(false);

						}
					});
					layout.addView(child);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if ((null != criteria) && 0 != criteria.size()
					&& (null != criteria.get(groupPosition))
					&& (null != criteria.get(groupPosition).articles)
					&& (0 != criteria.get(groupPosition).articles.size())) {
				return criteria.get(groupPosition).articles.size();
			}
			return 0;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return criteria.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			if (null == criteria) {
				return 0;
			}
			return criteria.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				view = getLayoutInflater().inflate(
						R.layout.favoritlist_group_layout, null);
			}
			if (!isExpanded) {
				((ImageView) view.findViewById(R.id.imageViewFavoritGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_down));
			} else {
				((ImageView) view.findViewById(R.id.imageViewFavoritGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_up));
			}

			if (null != criteria && 0 != criteria.size()
					&& null != criteria.get(groupPosition)) {
				((TextView) view.findViewById(R.id.favoritGroupTitle))
						.setText(criteria.get(groupPosition).title);
			}
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}

	/*
	 * Set the Actionbar for the favorites list-view.
	 */
	private void setupActionbar() {
		this.findViewById(R.id.actionbarRight).setVisibility(View.VISIBLE);
		this.findViewById(R.id.navbar).setVisibility(View.VISIBLE);
		View filter = this.findViewById(R.id.filter);
		this.findViewById(R.id.search).setVisibility(View.VISIBLE);
		this.findViewById(R.id.sort).setVisibility(View.GONE);
		filter.setVisibility(View.VISIBLE);

		View rightLayout = this.findViewById(R.id.RightLayout);

		int orientation = getResources().getConfiguration().orientation;
		if (Util.isTablet(this)
				&& orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.findViewById(R.id.filter).setVisibility(View.GONE);
			this.findViewById(R.id.search).setVisibility(View.GONE);

		} else if (!Util.isTablet(this)
				&& Configuration.ORIENTATION_PORTRAIT == orientation) {
			RelativeLayout.LayoutParams textparams = new RelativeLayout.LayoutParams(
					Util.getPixels(150, this), Util.getPixels(40, this));
			textparams.addRule(RelativeLayout.RIGHT_OF, R.id.navbarlayout);
			TextView title = ((TextView) this.findViewById(R.id.navbarTitle));
			title.setLayoutParams(textparams);
			this.findViewById(R.id.search).setVisibility(View.GONE);
			this.findViewById(R.id.share).setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					Util.getPixels(120, this), Util.getPixels(40, this));
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
			rightLayout.setLayoutParams(params);
			rightLayout.setLayoutParams(params);
		}

	}

	private class LoadFavoritTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog dialog = null;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				SharedPreferences pref = FavoritList.this.getSharedPreferences(
						"LOGIN", Context.MODE_PRIVATE);
				criteria = null;
				criteria = FavoritManager.getFavoritesByUser(
						pref.getInt("userID", -1), FavoritList.this);
				favGroups = null;
				favGroups = FavoritManager.getFavoriteGroups(
						pref.getInt("userID", -1), FavoritList.this);

			} catch (Exception e) {
				Util.logDebug(logTag,
						"Something went wrong getting the favorite groups: "
								+ (null != e.getMessage() ? e.getMessage()
										: "no message"));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
			listView = (ExpandableListView) findViewById(R.id.favoritView);

			adapter = new FavoritListAdapter();

			listView.setAdapter(adapter);
			for (int i = 0; i < listView.getExpandableListAdapter()
					.getGroupCount(); i++) {
				listView.expandGroup(i);
			}

			listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {

					try {
						com.your.name.dao.Article article = ((com.your.name.dao.Article) listView
								.getExpandableListAdapter().getChild(
										groupPosition, childPosition));
						Util.logDebug(logTag, "clicked on article with title: "
								+ article.title);

						Intent intent = new Intent(FavoritList.this,
								ArticleDetails.class);
						intent.putExtra(ArticleDetails.ARTICLE_SWITCH, false);
						intent.putExtra(
								com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
								article.id);

						startActivity(intent);

						return true;
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(),
								"Error - Please inform the developer",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}

					return false;
				}
			});

		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(FavoritList.this, "", Util
						.getApplicationString("label.message_loading_favorite",
								FavoritList.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onFilterClicked(View v) {
		SharedPreferences pref = FavoritList.this.getSharedPreferences("LOGIN",
				Context.MODE_PRIVATE);

		FavoritFilterList filterList;

		filterList = FavoritFilterList.newInstance(pref.getInt("userID", -1));

		filterList.show(getSupportFragmentManager(), "Filter");

	}

	@Override
	protected void onStop() {
		if (isFinishing()) {
			FavoritManager.observable.unregisterObserver(this);
			getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit()
					.putBoolean("showOnlyMyArticle", false).commit();
			FavoritManager.showOnlyMineInFavorites = false;
			FavoritManager.showOnlyLocaleArticle = false;
			FavoritManager.showUnassignedInFavorites = false;
			FavoritManager.groupToShowInFavorites = -1;
		}
		super.onStop();
	}

	public void onShareClicked(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");
		if (group == null) {
			intent.putExtra(Intent.EXTRA_TEXT, API.shareHost + "favorites");
		} else {
			intent.putExtra(Intent.EXTRA_TEXT, API.shareHost + ""
					+ group.sharelink);
		}

		startActivity(Intent.createChooser(intent, "Daten senden"));
	}

}
