/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.Tasks.LoadOneArticleTask;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.dao.Criterion;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.CriterionManager;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.fragments.ShowCriteria;
import com.your.name.fragments.article.Author;
import com.your.name.fragments.article.Downloads;
import com.your.name.fragments.article.Event_Venue;
import com.your.name.fragments.article.Expert_Contact;
import com.your.name.fragments.article.ExternalLinks;
import com.your.name.fragments.article.ImageWithText;
import com.your.name.fragments.article.Images;
import com.your.name.fragments.article.LinkedArticles;
import com.your.name.fragments.article.Study_Detail;
import com.your.name.fragments.article.Text;
import com.your.name.fragments.article.Videos;
import com.your.name.service.API;

public class ArticleDetails extends NavigationActivity implements
		OnGestureListener {

	private static final String logTag = ArticleDetails.class.toString();

	public static final String ARTICLE_ID_EXTRA = "article_id_extra";
	public static final String ARTICLE_LIST = "article_list";
	public static final String ARTICLE_SWITCH = "article_switch";

	private GestureDetector gestureDetector;

	public static boolean articleSwitch = true;

	private com.your.name.dao.Article article = null;
	public static int articleID = -1;
	public static ArrayList<Integer> ids = new ArrayList<Integer>();
	public static String articleType = "";
	public static ArrayList<com.your.name.dao.Article> navbarArticles = new ArrayList<com.your.name.dao.Article>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_article);

		((TextView) this.findViewById(R.id.navbarTitle)).setText("");

		Intent intent = getIntent();

		articleID = intent.getIntExtra(ARTICLE_ID_EXTRA, -1);

		articleSwitch = intent.getExtras().getBoolean(ARTICLE_SWITCH);

		ids = (ArrayList<Integer>) intent.getSerializableExtra(ARTICLE_LIST);

		if (!articleSwitch) {
			this.findViewById(R.id.previousArticle).setVisibility(
					View.INVISIBLE);
			this.findViewById(R.id.nextArticle).setVisibility(View.INVISIBLE);
		}

		if (-1 == articleID) {
			throw new RuntimeException("no article: id: " + articleID);
		}

		gestureDetector = new GestureDetector(this, this);
		this.findViewById(R.id.articleDetailLayout).setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return gestureDetector.onTouchEvent(event);
					}
				});

		/*
		 * Disable fling motion - Smooth vertical scrolling is more important
		 * and the fling detection disturbs that
		 */

		try {

			Dao<Article, Integer> articleDAO;
			articleDAO = this.helper.getDao(Article.class);

			article = articleDAO.queryForId(articleID);

			articleType = article.type;
			if (articleSwitch) {
				navbarArticles = (ArrayList<Article>) ArticleManager
						.getArticlesByIds(ids, this, articleType);

			}

			article.isFavorit = ArticleManager
					.isArticleFavorit(articleID, this);

			((TextView) findViewById(R.id.articleInfoView))
					.setText(article.author != null ? article.author : "");

			com.your.name.dao.Article.ArticleBase articleProxy = article
					.createSubType(this);

			List<PaneConfig> leftPaneConfig = articleProxy.getLeftPaneConfig();
			List<PaneConfig> rightPaneConfig = articleProxy
					.getRightPaneConfig();

			/*
			 * The title needs special treatment as it's not a section in itself
			 */
			if (leftPaneConfig.contains(PaneConfig.TITLE)) {
				if (!articleType.equals(Article.EXPERT)) {
					((TextView) findViewById(R.id.titleView))
							.setText((article.title != null && article.title
									.length() > 0) ? article.title
									: article.institution);
				} else {
					String name = "";
					if (article.firstname != null
							&& article.firstname.length() > 0) {
						name += article.firstname;
					}
					if (article.lastname != null
							&& article.lastname.length() > 0) {
						name += " " + article.lastname;
					}

					if (name.length() > 0 && !name.equals("")
							&& article.institution != null
							&& article.institution.length() > 0) {
						name += ", " + article.institution;
					} else if (name.length() == 0) {
						name = article.institution;
					}
					((TextView) findViewById(R.id.titleView)).setText(name);
				}
			}

			/*
			 * add fragments only on the first activity start
			 */

			if (null == icicle) {
				FragmentTransaction fragmentTransaction = getSupportFragmentManager()
						.beginTransaction();
				for (PaneConfig config : leftPaneConfig) {
					Util.logDebug(logTag, "adding fragment to left pane");

					Fragment fragment = createFragment(article, config);

					if (null != fragment) {
						fragmentTransaction.add(R.id.leftArticlePane, fragment,
								config.name());
					}
				}
				for (PaneConfig config : rightPaneConfig) {
					Util.logDebug(logTag, "adding fragment to right pane");

					Fragment fragment = createFragment(article, config);

					if (null != fragment) {
						/*
						 * The right article pane only exists in the large
						 * layout
						 */
						if (null != findViewById(R.id.rightArticlePane)) {
							fragmentTransaction.add(R.id.rightArticlePane,
									fragment);
						} else {

							fragmentTransaction.add(R.id.leftArticlePane,
									fragment);
						}
					}
				}

				fragmentTransaction.commit();

			} else {

				if (!Util.isTablet(this)) {
					getSupportFragmentManager().popBackStack(
							R.id.leftArticlePane,
							FragmentManager.POP_BACK_STACK_INCLUSIVE);
					FragmentTransaction fragmentTransaction = getSupportFragmentManager()
							.beginTransaction();

					for (PaneConfig config : leftPaneConfig) {
						Util.logDebug(logTag, "adding fragment to left pane");

						Fragment fragment = createFragment(article, config);

						if (null != fragment) {
							fragmentTransaction.add(R.id.leftArticlePane,
									fragment);
						}
					}

					fragmentTransaction.commit();
				}
			}
			if (article != null) {
				if (article.isFavorit
						|| article.user.id.equals(getSharedPreferences("LOGIN",
								Context.MODE_PRIVATE).getInt("userID", -1))) {
					((ImageView) this.findViewById(R.id.favorit))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.icon_fav_oranje));
				} else {
					((ImageView) this.findViewById(R.id.favorit))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.icon_tabbar_meine_daten));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			((TextView) this.findViewById(R.id.navbarTitle)).setText("");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * create one of the fragments, which is needed to show it
	 * in the Article Details view
	 */
	public Fragment createFragment(
			com.your.name.dao.Article article, PaneConfig config)
			throws Exception {

		switch (config) {
		case AUTHOR:
			return Author.newInstance(article, config);
		case PROJECT_DETAIL:
			return Study_Detail.newInstance(article, config);
		case FILES:
			Util.logDebug(logTag, "adding Downloads");
			return Downloads.newInstance(article, config);
		case CRITERIA:
			return null;
		case EXPERT_CONTACT:
			return Expert_Contact.newInstance(article, config);
		case TITLE:
			return null;
		case LINKED_ARTICLES:
			return LinkedArticles.newInstance(article, config);
		case VIDEOS:
			Util.logDebug(logTag, "adding Videos");
			return Videos.newInstance(article, config);
		case VENUE:
			return Event_Venue.newInstance(article, config);
		case IMAGES:
			Util.logDebug(logTag, "adding Images");
			return Images.newInstance(article, config);
		case IMAGE_AND_EXPERT_DESCRIPTION:
			return ImageWithText.newInstance(article, config);
		case EXTERNAL_LINKS:
			return ExternalLinks.newInstance(article, config);

		default:
			/*
			 * Everything else is handled by the Text.java class
			 */
			Util.logDebug(logTag, "adding Text");
			return Text.newInstance(article, config);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (!Util.isTablet(this))
			outState = new Bundle();
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		articleType = "";
		if (this.isFinishing() == true) {
			View frag = findViewById(R.id.navigationbarFragment);
			if (frag != null) {
				frag.findViewById(R.id.innerArticleListView).setVisibility(
						View.GONE);
			}
		}
		super.onStop();
	}

	public void onPreviousArticleClicked(View v) {
		for (int i = 0; i < navbarArticles.size(); i++) {
			if (navbarArticles.get(i).id.equals(articleID)) {
				if (i == 0) {
					return;
				}
				com.your.name.dao.Article tmp = navbarArticles
						.get((i - 1));
				ArticleDetails.articleID = tmp.id;
				new LoadOneArticleTask(this).execute(tmp.id);
			}
		}
	}

	public void onClearFilterClicked(View v) {
		try {
			ArticleDetails.navbarArticles = (ArrayList<com.your.name.dao.Article>) ArticleManager
					.getArticles((DatabaseActivity) this, articleType);
			ArticleManager.refreshFilter(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onFavoritStateChangeClicked(View v) {
		if (!Util.isUserAuthentificated(this)) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
		} else {
			new ManageFavoritTask().execute(article);
		}
	}

	private class ManageFavoritTask extends AsyncTask<Article, Void, Article> {
		private ProgressDialog dialog = null;

		@Override
		protected Article doInBackground(Article... params) {
			try {
				if (Util.isUserAuthentificated(ArticleDetails.this)) {
					boolean changed = false;

					SharedPreferences pref = ArticleDetails.this
							.getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

					if (!params[0].isFavorit) {
						changed = API.getInstance(ArticleDetails.this)
								.addFavorite(params[0].id, ArticleDetails.this);
						if (changed) {
							if (FavoritManager.addFavoritToDB(params[0].id,
									pref.getInt("userID", -1),
									ArticleDetails.this)) {
								params[0].isFavorit = true;
							}
						}
					} else {

						changed = API.getInstance(ArticleDetails.this)
								.removeFavorite(params[0].id,
										ArticleDetails.this);
						if (changed) {
							if (FavoritManager.removeFavoritFromDB(
									params[0].id, pref.getInt("userID", -1),
									ArticleDetails.this)) {
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

			if (result.isFavorit
					|| result.user.id.equals(getSharedPreferences("LOGIN",
							Context.MODE_PRIVATE).getInt("userID", -1))) {
				((ImageView) findViewById(R.id.favorit))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_fav_oranje));
			} else {
				((ImageView) findViewById(R.id.favorit))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_tabbar_meine_daten));
			}
		}

		@Override
		protected void onPreExecute() {
			try {
				dialog = ProgressDialog.show(ArticleDetails.this, "", Util
						.getApplicationString("label.article_edit",
								ArticleDetails.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			;
		}

	}

	public void onShareClicked(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, API.articleURL + articleID);
		startActivity(Intent.createChooser(intent, "Daten senden"));
	}

	public void onFilterClicked(View v) {
		try {
			List<Criterion> criteriaTypes = CriterionManager
					.getCriteriaByArticle(articleType, articleID, this);

			try {
				ShowCriteria filterList;

				filterList = ShowCriteria.newInstance(criteriaTypes, false);

				filterList.show(getSupportFragmentManager(), "Filter");

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onNextArticleClicked(View v) {

		for (int i = 0; i < navbarArticles.size(); i++) {
			if (i == navbarArticles.size() - 1) {
				return;
			}
			if (navbarArticles.get(i).id.equals(articleID)) {
				com.your.name.dao.Article tmp = navbarArticles
						.get((i + 1));
				new LoadOneArticleTask(this).execute(tmp.id);
			}
		}
	}

	@Override
	public void onBackPressed() {
		this.setResult(RESULT_OK);
		navbarArticles = null;
		super.onBackPressed();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		DisplayMetrics dm = getResources().getDisplayMetrics();

		Util.logDebug(logTag, velocityX + "");

		int threshold = (int) (1500.0 * (dm.densityDpi / 160.0));

		if ((Math.sqrt(velocityX * velocityX + velocityY * velocityY) > threshold)
				&& (Math.abs(velocityX) > Math.abs(velocityY))) {
			if (velocityX < 0) {
				onNextArticleClicked(null);
			} else {
				onPreviousArticleClicked(null);
			}

			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
}