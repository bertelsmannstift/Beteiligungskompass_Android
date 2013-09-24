/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.Article;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.CriterionManager;
import com.your.name.dao.helper.DataObserver;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.fragments.FilterList;
import com.your.name.fragments.SortArticle;
import com.your.name.fragments.dashboard.Navigation;
import com.your.name.service.API;

public class ArticleList extends NavigationActivity implements DataObserver {

	private static String logTag = ArticleList.class.toString();

	public static String type = "";

	private ExpandableListView listView = null;

	private Article singelArticle = null;

	private ArticleListAdapter articleAdapter = new ArticleListAdapter();

	protected NavigationItem item = null;

	private List<Article> articles = null;
	private ArrayList<Article> filteredArticles = null;
	private List<Criterion> criteria = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_articlelist);

		this.findViewById(R.id.tabArticleLayout).setBackgroundResource(
				R.color.background_dark_highlighted);

		try {
			item = (NavigationItem) getIntent().getExtras().getSerializable(
					Navigation.NAVIGATION_ITEM);

			type = item.type;

			((Button) this.findViewById(R.id.clearFilter)).setText(Util
					.getApplicationString("label.delete_filter", this));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArticleManager.observable.registerObserver(this);
		loadData();

		final EditText edit = (EditText) findViewById(R.id.search);
		edit.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					ArticleManager.searchText = "";
					ArticleManager.searchText = ((EditText) v).getText()
							.toString();
					ArticleManager.observable.notifyDatasetChanged();
					return true;
				}
				return false;
			}
		});

		/*
		 * Add the filterfragment in every case. But only if the activity is not restarted afterwards. 
		 * Example: The orientation changes, because fragments get automatically re-added by the FragmentManager
		 */
		if (null == savedInstanceState) {
			try {
				FilterList filterList = FilterList.newInstance(
						CriterionManager.getCriteriaByType(item.type), false);
				filterList.setType(item.type);
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();

				transaction.add(R.id.filterListFragmentContainer, filterList);

				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * In landscape we display the fragment. In portrait we don't.
		 */
		if (Util.isOrientationPortrait(this) || false == Util.isTablet(this)) {
			findViewById(R.id.filterListFragmentContainer).setVisibility(
					View.GONE);
		} else {
			findViewById(R.id.filterListFragmentContainer).setVisibility(
					View.VISIBLE);
		}

		((TextView) this.findViewById(R.id.navbarTitle)).setText(item.name);
		((TextView) this.findViewById(R.id.navbarTitle))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onNavbarClicked(null);

					}
				});
	}

	/**
	 * Build the actionbar on the top of the app. Because that app runs with
	 * Android Version 2.2.
	 */
	private void setupActionbar() {
		try {
			View filter = this.findViewById(R.id.filter);
			RelativeLayout.LayoutParams filterParams = new RelativeLayout.LayoutParams(
					Util.getPixels(150, this), Util.getPixels(40, this));
			filterParams.addRule(RelativeLayout.LEFT_OF, R.id.search);

			filter.setVisibility(View.VISIBLE);

			TextView title = ((TextView) this.findViewById(R.id.navbarTitle));
			if (filteredArticles != null
					|| !ArticleManager.searchText.equals("")) {
				filter.setBackgroundResource(R.color.background_interferer);
				title.setText(item.name
						+ " ("
						+ ((filteredArticles == null) ? "0" : filteredArticles
								.size()) + "/" + item.amountArticles + ")");
			} else {
				filter.setBackgroundResource(R.color.background_dark);
				title.setText(item.name + " (" + item.amountArticles + "/"
						+ item.amountArticles + ")");
			}
			this.findViewById(R.id.actionbarRight).setVisibility(View.VISIBLE);
			this.findViewById(R.id.navbar).setVisibility(View.VISIBLE);
			this.findViewById(R.id.search).setVisibility(View.VISIBLE);
			this.findViewById(R.id.sort).setVisibility(View.VISIBLE);

			View rightLayout = this.findViewById(R.id.RightLayout);
			rightLayout.setLayoutParams(filterParams);
			int orientation = getResources().getConfiguration().orientation;
			if (Util.isTablet(this)
					&& orientation == Configuration.ORIENTATION_LANDSCAPE) {
				this.findViewById(R.id.filter).setVisibility(View.GONE);
				this.findViewById(R.id.search).setVisibility(View.GONE);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						Util.getPixels(40, this), Util.getPixels(40, this));
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
				rightLayout.setLayoutParams(params);

			} else if (!Util.isTablet(this)
					&& Configuration.ORIENTATION_PORTRAIT == orientation) {
				RelativeLayout.LayoutParams textparams = new RelativeLayout.LayoutParams(
						Util.getPixels(150, this), Util.getPixels(40, this));
				textparams.addRule(RelativeLayout.RIGHT_OF, R.id.navbarlayout);
				title.setLayoutParams(textparams);
				this.findViewById(R.id.search).setVisibility(View.GONE);
				this.findViewById(R.id.share).setVisibility(View.VISIBLE);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						Util.getPixels(120, this), Util.getPixels(40, this));
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
				rightLayout.setLayoutParams(params);
				findViewById(R.id.tabContactLayout).setVisibility(View.GONE);
			} else if (!Util.isTablet(this)
					&& Configuration.ORIENTATION_LANDSCAPE == orientation) {
				this.findViewById(R.id.share).setVisibility(View.GONE);
				findViewById(R.id.tabContactLayout).setVisibility(View.VISIBLE);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onSortClicked(View v) {
		SortArticle sortArticle;

		sortArticle = SortArticle.newInstance(item.type, false);

		sortArticle.show(getSupportFragmentManager(), "Sort");
	}

	public void onFilterClicked(View v) {
		Util.logDebug(logTag, "filter clicked");

		try {
			FilterList filterList;
			Util.logDebug(logTag, "size of criteriaList: " + criteria.size());

			filterList = FilterList.newInstance(criteria, false);

			filterList.show(getSupportFragmentManager(), "Filter");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Building the path to the right URL, before we show the share link
	 */
	public void onShareClicked(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");

		String filterCriterion = "";
		String searchText = "";
		String sortBy = "";
		try {
			if (!ArticleManager.searchText.equals("")) {
				searchText += "search=" + ArticleManager.searchText;
			}

			if (ArticleManager.sortBy != -1) {
				switch (ArticleManager.sortBy) {
				case 0:
					if (type.equals(Article.EXPERT)) {
						sortBy = "lastname";
					} else {
						sortBy = "title";
					}
					break;
				case 1:
					sortBy = "lastname";
					break;
				case 2:
					if (type.equals(Article.STUDY)) {
						sortBy = "study_start";
					} else if (type.equals(Article.QA)) {
						sortBy = "year";
					} else {
						sortBy = "date";
					}
					break;
				case 3:
					sortBy = "created";
					break;
				case 4:
					sortBy = "institution";
					break;
				case 5:
					sortBy = "fav";
					break;
				case 6:
					sortBy = "author";
					break;
				default:

					break;

				}
			}

			Dao<CriteriaOption, Integer> options = this.helper
					.getDao(CriteriaOption.class);

			QueryBuilder<CriteriaOption, Integer> qb = options.queryBuilder();

			List<CriteriaOption> selectedOptions = qb.where()
					.eq("selected", true).and().ne("default_value", true)
					.query();

			for (int i = 0; i < selectedOptions.size(); i++) {
				CriteriaOption option = selectedOptions.get(i);

				if (i == (selectedOptions.size() - 1)) {
					filterCriterion += "criteria[" + i + "]=" + option.id;
				} else {
					filterCriterion += "criteria[" + i + "]=" + option.id + "&";
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (filterCriterion.equals("") && searchText.equals("")
				&& sortBy.equals("")) {
			intent.putExtra(Intent.EXTRA_TEXT, API.articleListURL + item.type);
		} else {
			intent.putExtra(Intent.EXTRA_TEXT, API.articleListURL + item.type
					+ "/?" + filterCriterion
					+ ((!searchText.equals("")) ? "&" + searchText : "")
					+ ((!sortBy.equals("")) ? "&sort=" + sortBy : ""));
		}

		startActivity(Intent.createChooser(intent, "Daten senden"));
	}

	public class ArticleListAdapter extends BaseExpandableListAdapter {

		public Object getChild(int groupPosition, int childPosition) {

			if (ArticleManager.filteredCriterion != null
					&& ArticleManager.filteredCriterion.get(item.type) != null) {

				CriteriaOption option = (CriteriaOption) ArticleManager.filteredCriterion
						.get(item.type).visibleOptions.get(groupPosition);

				return option.articles.get(childPosition);
			}
			if (filteredArticles != null && !item.type.equals(Article.EVENT)) {
				return filteredArticles.get(childPosition);
			}

			return articles.get(childPosition);
		}

		public long getChildId(int arg0, int arg1) {
			return 0;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean arg2, View v, ViewGroup vg) {

			if (v == null) {
				v = LayoutInflater.from(ArticleList.this).inflate(
						R.layout.articlelist_child_layout, null);
			}

			try {

				final Article article = (Article) getChild(groupPosition,
						childPosition);

				v.setTag(article);
				Article.ArticleBase specializedArticle = article
						.createSubType(ArticleList.this);

				((TextView) v.findViewById(R.id.articleTitle))
						.setText(specializedArticle.getTitle());

				((TextView) v.findViewById(R.id.articleDescription))
						.setText(article.listdescription_plaintext);

				if (0 == specializedArticle.videoURLs.size()
						|| !Util.isTablet(ArticleList.this)) {
					v.findViewById(R.id.articleVideo).setVisibility(View.GONE);
				}

				if (0 == specializedArticle.downloadFilenames.size()
						|| !Util.isTablet(ArticleList.this)) {
					v.findViewById(R.id.articleDownload).setVisibility(
							View.GONE);
				}

				if (null == article.city || article.city.equals("")
						|| !Util.isTablet(ArticleList.this)) {
					v.findViewById(R.id.articlePin).setVisibility(View.GONE);

				}

				((TextView) v.findViewById(R.id.articleSubTitle)).setText(Util
						.buildSubTitle(article, ArticleList.this));

				if (0 != specializedArticle.imageFilenames.size()) {
					String thumbnailFileName = Sync
							.getThumbnailsPath(ArticleList.this)
							+ "/"
							+ specializedArticle.imageFilenames.get(0);

					Util.logDebug(logTag, "Setting thumbnail: "
							+ thumbnailFileName);

					ImageView imageView = ((ImageView) v
							.findViewById(R.id.articleImage));

					LinearLayout.LayoutParams params = null;
					if (Util.isTablet(ArticleList.this)) {
						params = new LinearLayout.LayoutParams(Util.getPixels(
								150, ArticleList.this), Util.getPixels(150,
								ArticleList.this));
					} else {
						params = new LinearLayout.LayoutParams(Util.getPixels(
								80, ArticleList.this), Util.getPixels(80,
								ArticleList.this));
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
							Util.getPixels(1, ArticleList.this),
							Util.getPixels(80, ArticleList.this));

					imageView.setLayoutParams(params);

				}

				if (article.isFavorit
						|| (article.user != null && article.user.id
								.equals(getSharedPreferences("LOGIN",
										Context.MODE_PRIVATE).getInt("userID",
										-1)))) {
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
									if (Util.isUserAuthentificated(ArticleList.this)) {
										new ManageFavoritTask()
												.execute(article);
									} else {
										startActivity(new Intent(
												ArticleList.this, Login.class));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});

			} catch (Exception e) {
				e.printStackTrace();
				v = new TextView(ArticleList.this);
				((TextView) v).setText(""
						+ (null != e.getMessage() ? e.getMessage()
								: "No Message"));
			}

			return v;
		}

		public int getChildrenCount(int position) {

			if (ArticleManager.filteredCriterion != null
					&& ArticleManager.filteredCriterion.get(item.type) != null
					&& ArticleManager.filteredCriterion.get(item.type).visibleOptions
							.size() > 0) {
				CriteriaOption option = (CriteriaOption) ArticleManager.filteredCriterion
						.get(item.type).visibleOptions.get(position);
				return option.articles.size();
			}

			if (filteredArticles != null && !item.type.equals(Article.EVENT)) {
				return filteredArticles.size();
			}

			return articles.size();

		}

		public Object getGroup(int arg0) {

			return null;
		}

		public int getGroupCount() {
			try {
				if (ArticleManager.filteredCriterion != null
						&& ArticleManager.filteredCriterion.get(item.type) != null) {
					return ArticleManager.filteredCriterion.get(item.type).visibleOptions
							.size();
				}
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 1;
			}
		}

		public long getGroupId(int position) {
			return 0;
		}

		public View getGroupView(int groupPosition, boolean expanded,
				View convertView, ViewGroup vg) {

			View v = convertView;

			v = getLayoutInflater().inflate(R.layout.articlelist_group_layout,
					null);

			if (!expanded) {
				((ImageView) v.findViewById(R.id.imageViewArticleGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_down));
			} else {
				((ImageView) v.findViewById(R.id.imageViewArticleGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_up));
			}
			if ((ArticleManager.filteredCriterion != null && ArticleManager.filteredCriterion
					.get(item.type) != null)) {

				v.setVisibility(View.VISIBLE);

				CriteriaOption option = (CriteriaOption) ArticleManager.filteredCriterion
						.get(item.type).visibleOptions.toArray()[groupPosition];

				if (option.articles != null && option.articles.size() > 0) {
					v.setVisibility(View.VISIBLE);
					((TextView) v.findViewById(R.id.articleGroupTitle))
							.setText(option.title);

					v.setTag(option);
				} else {
					v = new FrameLayout(ArticleList.this);
				}

				v.setTag(option);

			} else {

				AbsListView.LayoutParams params = new AbsListView.LayoutParams(
						Util.getPixels(1, ArticleList.this), Util.getPixels(1,
								ArticleList.this));

				v.setLayoutParams(params);
				v.setVisibility(View.VISIBLE);
			}

			return v;
		}

		public boolean hasStableIds() {
			return false;
		}

		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		setupActionbar();

		View view = this.findViewById(R.id.navbarLayout);
		if (view != null && view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (findViewById(R.id.navbarLayout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.navbarLayout).setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setupActionbar();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if (findViewById(R.id.navbarLayout).getVisibility() == View.GONE) {
			ArticleDetails.articleType = "";
			ArticleList.type = "";
			Intent intent = new Intent(this, Dashboard.class);
			startActivity(intent);
			finish();
		}

		this.findViewById(R.id.navbarLayout).setVisibility(View.GONE);
	}

	private void loadData() {
		new ArticleLoaderTask().execute(item.filterId);

	}

	/*
	 * Load the necessary articles from the database and concatinate them with the
	 * matching criterion
	 */
	private class ArticleLoaderTask extends
			AsyncTask<Integer, Void, ArrayList<Article>> {

		private ProgressDialog dialog = null;

		@Override
		protected ArrayList<Article> doInBackground(Integer... params) {
			try {
				String criterionFilter = item.type;

				criteria = CriterionManager.getCriteriaByType(criterionFilter,
						true, ArticleList.this);

				articles = ArticleManager.getArticles(ArticleList.this,
						criterionFilter);

				filteredArticles = (ArrayList<Article>) ArticleManager
						.callArticlesByFilter(ArticleList.this,
								criterionFilter, true);

				if (criterionFilter.equals(Article.EVENT)) {
					Criterion criterion = new Criterion();

					String groupText = "";
					if (filteredArticles == null) {
						for (Article article : articles) {
							Calendar cal = new GregorianCalendar();
							Date date = article.start_date;
							cal.setTime(date);

							String dateString = "";

							dateString = Util.getMonth(cal.get(Calendar.MONTH))
									+ " " + cal.get(Calendar.YEAR);
							if (!groupText.equals(dateString)) {
								CriteriaOption option = new CriteriaOption();
								groupText = dateString;
								criterion.visibleOptions.add(option);
								option.articles = new ArrayList<Article>();
								option.title = dateString;
								option.articles.add(article);
							} else {
								criterion.visibleOptions
										.get((criterion.visibleOptions.size() - 1)).articles
										.add(article);
							}
						}
					}

					if (ArticleManager.sortBy == 5) {
						for (CriteriaOption opt : criterion.visibleOptions) {
							Collections.sort(opt.articles);
						}
					}

					if (ArticleManager.filteredCriterion == null) {
						ArticleManager.filteredCriterion = new HashMap<String, Criterion>();
					}
					ArticleManager.filteredCriterion.put(criterionFilter,
							criterion);

				} else {

					Criterion criterion = null;
					if (ArticleManager.filteredCriterion != null) {
						criterion = ArticleManager.filteredCriterion.get(type);
					}
					if (criterion != null) {
						if (filteredArticles == null
								|| filteredArticles.size() == 0) {
							for (CriteriaOption option : criterion.visibleOptions) {
								option.articles = (ArrayList<Article>) ArticleManager
										.getArticlesInCriterion(
												ArticleList.this, option.id,
												criterionFilter);
								if (ArticleManager.sortBy == 5) {
									Collections.sort(option.articles);
								}
							}
						}

						ArticleManager.filteredCriterion.put(type, criterion);
					} else {
						if (ArticleManager.sortBy == 5) {
							Collections.sort(articles);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				try {
					Toast.makeText(
							ArticleList.this,
							Util.getApplicationString("error_get_articles",
									ArticleList.this), Toast.LENGTH_SHORT)
							.show();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				articles = new ArrayList<Article>();
				criteria = new ArrayList<Criterion>();
			}

			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<Article> result) {
			if (dialog != null && !isCancelled()) {
				try {
					dialog.dismiss();
				} catch (Exception e) {

				}
			}

			if (filteredArticles != null
					|| !ArticleManager.searchText.equals("")) {
				((ImageView) ArticleList.this.findViewById(R.id.filter))
						.setBackgroundResource(R.color.background_interferer);

				if (filteredArticles == null || filteredArticles.size() == 0)
					Toast.makeText(ArticleList.this,
							getString(R.string.no_article_found),
							Toast.LENGTH_LONG).show();
				((TextView) ArticleList.this.findViewById(R.id.navbarTitle))
						.setText(item.name
								+ " ("
								+ ((filteredArticles == null) ? "0"
										: filteredArticles.size()) + "/"
								+ item.amountArticles + ")");
			} else {
				((ImageView) ArticleList.this.findViewById(R.id.filter))
						.setBackgroundResource(R.color.background_dark);
				((TextView) ArticleList.this.findViewById(R.id.navbarTitle))
						.setText(item.name + " (" + item.amountArticles + "/"
								+ item.amountArticles + ")");
			}

			if (!ArticleManager.isFilterSet(ArticleList.this)) {
				Button clearFilter = (Button) findViewById(R.id.clearFilter);
				if (!ArticleManager.searchText.equals("")) {
					try {
						clearFilter.setText(Util.getApplicationString(
								"label.delete_search", ArticleList.this));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						clearFilter.setText(Util.getApplicationString(
								"label.delete_filter", ArticleList.this));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				findViewById(R.id.clearFilter).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.clearFilter).setVisibility(View.GONE);
			}

			listView = (ExpandableListView) ArticleList.this
					.findViewById(R.id.articleView);

			listView.setGroupIndicator(null);
			articleAdapter = new ArticleListAdapter();
			listView.setAdapter(articleAdapter);

			if (articleAdapter.getGroupCount() == 0) {
				findViewById(R.id.empty_view_layout)
						.setVisibility(View.VISIBLE);
				findViewById(R.id.articleView).setVisibility(View.GONE);
			} else {
				findViewById(R.id.empty_view_layout).setVisibility(View.GONE);
				findViewById(R.id.articleView).setVisibility(View.VISIBLE);
			}

			for (int i = 0; i < listView.getExpandableListAdapter()
					.getGroupCount(); i++) {
				listView.expandGroup(i);
			}
			listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {

					try {
						com.your.name.dao.Article article = ((com.your.name.dao.Article) listView
								.getExpandableListAdapter().getChild(
										groupPosition, childPosition));
						singelArticle = article;
						Util.logDebug(logTag, "clicked on article with title: "
								+ article.title);

						new loadOneArticleTask().execute(groupPosition);

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
			if (Util.isTablet(ArticleList.this))
				setupActionbar();

		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(ArticleList.this, "", Util
						.getApplicationString("label.message_loading_articles",
								ArticleList.this));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

	}

	/*
	 * load the selected article from the list and start the ArticleDetails view.
	 */
	private class loadOneArticleTask extends AsyncTask<Integer, Void, Intent> {

		private ProgressDialog dialog = null;

		@Override
		protected Intent doInBackground(Integer... params) {
			Intent intent = new Intent(ArticleList.this, ArticleDetails.class);
			ArrayList<Integer> tmpArticles = new ArrayList<Integer>();
			if (ArticleManager.filteredCriterion != null
					&& ArticleManager.filteredCriterion.get(item.type) != null) {
				for (Article art : (ArrayList<Article>) ((CriteriaOption) ArticleManager.filteredCriterion
						.get(item.type).visibleOptions.toArray()[(params[0])]).articles) {
					tmpArticles.add(art.id);
				}
			} else if (filteredArticles != null) {
				for (int i = 0; i < filteredArticles.size(); i++) {
					Article tmpArt = filteredArticles.get(i);
					tmpArticles.add(tmpArt.id);
				}
			} else {
				for (Article art : articles) {
					tmpArticles.add(art.id);
				}
			}
			intent.putExtra(ArticleDetails.ARTICLE_SWITCH, true);
			intent.putExtra(
					com.your.name.activities.ArticleDetails.ARTICLE_LIST,
					tmpArticles);
			intent.putExtra(
					com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
					singelArticle.id);

			return intent;
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(ArticleList.this, "", Util
						.getApplicationString("label.message_loading_articles",
								ArticleList.this));
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

	/*
	 * On Press of the favorite star, the article will get a favorite from the
	 * user. De-selecting/Tapping again removes the article as a favorite from the user.
	 */
	public class ManageFavoritTask extends AsyncTask<Article, Void, Article> {
		private ProgressDialog dialog = null;

		@Override
		protected Article doInBackground(Article... params) {
			try {
				if (Util.isUserAuthentificated(ArticleList.this)) {
					boolean changed = false;

					SharedPreferences pref = ArticleList.this
							.getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

					if (!params[0].isFavorit) {
						changed = API.getInstance(ArticleList.this)
								.addFavorite(params[0].id, ArticleList.this);
						if (changed) {
							if (FavoritManager
									.addFavoritToDB(params[0].id,
											pref.getInt("userID", -1),
											ArticleList.this)) {
								params[0].isFavorit = true;
							}
						}
					} else {

						changed = API.getInstance(ArticleList.this)
								.removeFavorite(params[0].id, ArticleList.this);
						if (changed) {
							if (FavoritManager.removeFavoritFromDB(
									params[0].id, pref.getInt("userID", -1),
									ArticleList.this)) {
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

				articleAdapter.notifyDataSetChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(ArticleList.this, "", Util
						.getApplicationString("label.article_edit",
								ArticleList.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void onClearFilterClicked(View v) {
		try {
			ArticleManager.refreshFilter(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dataChanged() {
		Util.logDebug(logTag, "dataChanged");

		loadData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			ArticleManager.observable.unregisterObserver(this);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int resultCode, Intent arg2) {
		if (resultCode == RESULT_OK) {
			articleAdapter.notifyDataSetChanged();
		}
	}

}
