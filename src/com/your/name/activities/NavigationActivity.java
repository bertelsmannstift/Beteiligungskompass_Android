/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.Article;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.fragments.dashboard.Navigation;
import com.your.name.service.API;

/*
 * This class contains all common methods for the different activities. 
 * It's an abstract class, because this class should never be used alone. 
 */
public abstract class NavigationActivity extends DatabaseActivity {

	public static final String ITEM_TPE = "TYPE";

	public void onArticleClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(ArticleList.class.getSimpleName())) {
			Intent intent = new Intent(this, ArticleList.class);
			ArticleDetails.navbarArticles = null;
			NavigationItem item = API.navigationItems.get(0);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Navigation.NAVIGATION_ITEM, item);
			ArticleDetails.articleType = item.type;
			Util.switchActivity(intent, this);
		}
	}

	@Override
	public void onBackPressed() {
		View view = this.findViewById(R.id.navbarLayout);
		if (view != null && view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
		}
		super.onBackPressed();
	}

	public void onNewArticleClicked(View v) {
		if (this.getClass().getSimpleName()
				.equals(ArticleList.class.getSimpleName())) {
			if (!Util.isUserAuthentificated(this)) {
				Intent intent = new Intent(this, Login.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, NewArticle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(ITEM_TPE, ((ArticleList) this).item);
				intent.putExtra(NewArticle.NEXT_STEP, 1);
				startActivity(intent);
			}
		}
	}

	public void onShareClicked(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, API.getInstance(this).host);
		startActivity(Intent.createChooser(intent, "Daten senden"));
	}

	public void onDashboardClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(Dashboard.class.getSimpleName())) {
			ArticleList.type = "";
			ArticleDetails.articleType = "";
			Util.switchActivity(new Intent(this, Dashboard.class), this);
		}
	}

	public void onPlanningClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())) {
			ArticleList.type = "";
			ArticleDetails.articleType = "";
			Util.switchActivity(new Intent(this, Planning.class), this);
		}
	}

	public void onAboutClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(About.class.getSimpleName())) {
			Util.switchActivity(new Intent(this, About.class), this);
		}
	}

	public void onMyDataClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(FavoritList.class.getSimpleName())) {
			ArticleList.type = "";
			ArticleDetails.articleType = "";
		} else {
			Intent intent = new Intent(this, FavoritList.class);
			startActivity(intent);
			finish();
		}

	}

	public void onFavoritClicked(View v) {
		ArticleList.type = "";
		ArticleDetails.articleType = "";
		if (!Util.isUserAuthentificated(this)) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, FavoritList.class);
			startActivity(intent);
			finish();
		}
	}

	public void onNavbarClicked(View v) {
		View view = this.findViewById(R.id.navbarLayout);
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		view.startAnimation(fadeInAnimation);
		view.setVisibility(View.VISIBLE);
	}

	public void onExampleClicked(View v) {

		ArrayList<NavigationItem> navigationItems = API.getInstance(this)
				.getNavigationItems((NavigationActivity) this);

		NavigationItem item = null;

		for (NavigationItem navigationItem : navigationItems) {
			if (navigationItem.type.equals(Article.STUDY)) {
				item = navigationItem;
				break;
			}
		}

		Intent intent = new Intent(this, ArticleList.class);
		intent.putExtra(Navigation.NAVIGATION_ITEM, item);
		ArticleDetails.navbarArticles = null;
		ArticleDetails.articleID = -1;
		ArticleDetails.articleType = item.type;

		Util.switchActivity(intent, this);
	}

	public void onMethodClicked(View v) {

		ArrayList<NavigationItem> navigationItems = API.getInstance(this)
				.getNavigationItems((NavigationActivity) this);

		NavigationItem item = null;

		for (NavigationItem navigationItem : navigationItems) {
			if (navigationItem.type.equals(Article.METHOD)) {
				item = navigationItem;
				break;
			}
		}

		Intent intent = new Intent(this, ArticleList.class);
		intent.putExtra(Navigation.NAVIGATION_ITEM, item);
		ArticleDetails.navbarArticles = null;
		ArticleDetails.articleID = -1;
		ArticleDetails.articleType = item.type;

		Util.switchActivity(intent, this);
	}

	public void onContactClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(Contact.class.getSimpleName())) {
			Util.switchActivity(new Intent(this, Contact.class), this);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!getClass().getSimpleName().equals(
				SplashScreen.class.getSimpleName())) {
			View view = this.findViewById(R.id.tabLayout);
			if (view != null) {
				try {
					((TextView) view.findViewById(R.id.tabArticleName))
							.setText(Util.getApplicationString(
									"label.categories", this));
					((TextView) view.findViewById(R.id.tabContactName))
							.setText(Util.getApplicationString("share.title",
									this));
					((TextView) view.findViewById(R.id.tabDashboardName))
							.setText(Util.getApplicationString(
									"label.dashboard", this));
					((TextView) view.findViewById(R.id.tabPlanName))
							.setText(Util.getApplicationString(
									"label.planning", this));
					((TextView) view.findViewById(R.id.tabDataName))
							.setText(Util.getApplicationString(
									"global.sortfav", this));

					((TextView) this.findViewById(R.id.navbarTitle))
							.setText(Util.getApplicationString(
									"label.dashboard_name", this));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (Util.isOrientationPortrait(this)
					&& !Util.isTablet(this)
					&& !this.getClass().getSimpleName()
							.equals(Login.class.getSimpleName())) {
				findViewById(R.id.tabContactLayout).setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		overridePendingTransition(0, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!Util.isTablet(this) && Util.isOrientationPortrait(this)) {
			getMenuInflater().inflate(R.menu.options_menu, menu);
			try {
				menu.findItem(R.id.icInfo).setTitle(
						Util.getApplicationString("label.info_contact", this));

				menu.findItem(R.id.icontext).setTitle(
						Util.getApplicationString("share.title", this));
			} catch (Exception e) {

			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.icontext:
			onShareClicked(null);
			break;
		case R.id.icInfo:
			onInfoClicked(null);
			break;
		}
		return true;
	}

	public void onClearFilterClicked(View v) {
		try {
			ArticleManager.refreshFilter(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onInfoClicked(View v) {
		if (!this.getClass().getSimpleName()
				.equals(Contact.class.getSimpleName())) {

			Intent intent = new Intent(this, Contact.class);
			Util.switchActivity(intent, this);
		}

	}

	public void onLoginClicked(View v) {
		if (Util.isUserAuthentificated(this)) {
			getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit()
					.putString("token", "").commit();
			try {
				FavoritManager.removeAllFavorites(
						getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
								.getInt("userID", -1), this);
				getSharedPreferences("LOGIN", Context.MODE_PRIVATE).edit()
						.putInt("userID", -1).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Toast.makeText(
						this,
						Util.getApplicationString(
								"label.message_successfully_logout", this),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			FavoritManager.observable.notifyDatasetChanged();
		} else {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
		}
	}

}
