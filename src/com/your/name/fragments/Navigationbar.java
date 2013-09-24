/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.AdapterUtils;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleDetails;
import com.your.name.activities.ArticleList;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.NavigationActivity;
import com.your.name.activities.Tasks.LoadOneArticleTask;
import com.your.name.dao.Article;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.DataObserver;
import com.your.name.fragments.dashboard.Navigation;
import com.your.name.service.API;

/*
 * this fragment defines the navigation drawer. That drawer is self written,
 * because the navigation drawer from android is avaiable since API 14. This APP
 * supports API 8 too. 
 */
public class Navigationbar extends Fragment implements DataObserver {

	private ArrayList<NavigationItem> navigationItems = null;

	private View v = null;
	private TextView login = null;
	private ImageView loginImage = null;

	private TextView buttonNewArticle;
	private NavigationbarAdapter adapter;

	private String logTag = Navigationbar.class.toString();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_navigationbar, container, false);

		navigationItems = API.getInstance(getActivity()).getNavigationItems(
				(NavigationActivity) getActivity());

		buttonNewArticle = (TextView) v.findViewById(R.id.ownArticle);

		LinearLayout img = (LinearLayout) v
				.findViewById(R.id.headerNavigationbarLayout);

		img.setOnClickListener(new View.OnClickListener() {

			public void onClick(View tmp) {
				Animation fadeInAnimation = AnimationUtils.loadAnimation(
						getActivity(), R.anim.slide_out_left);
				v.setAnimation(fadeInAnimation);
				v.setVisibility(View.GONE);
			}
		});

		login = (TextView) v.findViewById(R.id.login);
		loginImage = (ImageView) v.findViewById(R.id.loginIcon);
		try {
			((TextView) v.findViewById(R.id.selectArea))
					.setText(Util.getApplicationString("label.select_section",
							getActivity()));

			((TextView) v.findViewById(R.id.textViewFilterText)).setText(Util
					.getApplicationString("label.filter_text", getActivity()));

			((Button) v.findViewById(R.id.clearFilter))
					.setText(Util.getApplicationString("label.delete_filter",
							getActivity()));

			((TextView) v.findViewById(R.id.info)).setText(Util
					.getApplicationString("label.info_contact", getActivity()));
			if (Util.isUserAuthentificated(getActivity())) {
				login.setText(Util.getApplicationString("label.logout",
						getActivity()));
				loginImage.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.icon_subnavi_logout));
			} else {
				login.setText(Util.getApplicationString("label.login",
						getActivity()));
				loginImage.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.icon_subnavi_login));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	private class InnerArticleAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ArticleDetails.navbarArticles.size();
		}

		@Override
		public Object getItem(int position) {
			return ArticleDetails.navbarArticles.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int postion, View v, ViewGroup vg) {

			View view = v;

			if (view == null) {
				view = LayoutInflater.from(getActivity()).inflate(
						R.layout.innerarticleadapter_listitem, null);
			}

			com.your.name.dao.Article article = ArticleDetails.navbarArticles
					.get(postion);
			TextView articleText = (TextView) view
					.findViewById(R.id.textViewArticle);
			if (article.type.equals(Article.EXPERT)) {
				String title = "";
				if (article.title != null && article.title.length() > 0) {
					if (article.institution != null
							&& article.institution.length() > 0) {
						title = article.firstname + " " + article.lastname
								+ ", " + article.institution;
					} else {
						title = article.firstname + " " + article.lastname;
					}
				} else {
					title = article.institution;
				}
				articleText.setText(" - " + title);
			} else {
				articleText.setText(" - " + article.title != null
						&& article.title.length() > 0 ? article.title
						: article.institution);
			}

			view.setTag(article);
			view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					com.your.name.dao.Article tmp = (com.your.name.dao.Article) v
							.getTag();
					ArticleDetails.articleID = tmp.id;
					ArticleDetails.articleType = tmp.type;

					new LoadOneArticleTask(getActivity()).execute(tmp.id);

				}
			});
			if (article.id.equals(ArticleDetails.articleID)) {
				try {
					if (ArticleDetails.navbarArticles.size() == 1) {
						getActivity().findViewById(R.id.previousArticle)
								.setVisibility(View.INVISIBLE);
						getActivity().findViewById(R.id.nextArticle)
								.setVisibility(View.INVISIBLE);
					} else if (postion == 0) {
						getActivity().findViewById(R.id.previousArticle)
								.setVisibility(View.INVISIBLE);
					} else if ((postion + 1) == ArticleDetails.navbarArticles
							.size()) {
						getActivity().findViewById(R.id.nextArticle)
								.setVisibility(View.INVISIBLE);
					}
					view.setBackgroundResource(R.color.background_dark_highlighted);
					articleText.setTextColor(getResources().getColor(
							android.R.color.white));
				} catch (Exception e) {

				}
			} else {
				view.setBackgroundResource(R.color.text_on_background_dark);
				articleText.setTextColor(getResources().getColor(
						android.R.color.black));
			}

			return view;
		}

	}

	private class NavigationbarAdapter extends BaseAdapter {

		public int getCount() {
			return navigationItems.size();
		}

		public Object getItem(int position) {
			return navigationItems.get(position);
		}

		public long getItemId(int arg0) {
			return 0;
		}

		private void setInnerArticleAdapter(String type, View v) {
			if (ArticleDetails.articleType.equals(type)) {
				v.findViewById(R.id.navigationItemBar).setBackgroundResource(
						R.color.background_interferer);
			} else {
				v.findViewById(R.id.navigationItemBar).setBackgroundResource(
						R.color.background_dark);
			}
			if (ArticleDetails.articleType.equals(type)
					&& ArticleDetails.articleID > -1) {

				LinearLayout inner = (LinearLayout) v
						.findViewById(R.id.innerArticleListView);
				inner.setVisibility(View.VISIBLE);
				AdapterUtils
						.fillLinearLayoutFromAdapter(1, inner,
								new InnerArticleAdapter(), getActivity(), true,
								true, 2);
			} else {
				LinearLayout inner = (LinearLayout) v
						.findViewById(R.id.innerArticleListView);
				inner.setVisibility(View.GONE);
			}
		}

		public View getView(int position, View convertView, ViewGroup vg) {
			Util.logDebug(logTag, "getting view");

			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.navigationbar_listitem, null);
			}

			final View view = convertView;
			final NavigationItem item = navigationItems.get(position);

			if (ArticleDetails.articleType.equals(item.type)) {
				view.findViewById(R.id.navigationItemBar)
						.setBackgroundResource(R.color.background_interferer);
			}

			view.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					try {
						Intent intent = new Intent(getActivity(),
								ArticleList.class);
						intent.putExtra(Navigation.NAVIGATION_ITEM, item);
						ArticleDetails.navbarArticles = null;
						ArticleDetails.articleID = -1;
						ArticleDetails.articleType = item.type;

						Util.switchActivity(intent, getActivity());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

			int filteredArticles = -1;
			boolean filtered = false;

			Util.logDebug(logTag, "counting articles");
			boolean filterNotSet = ArticleManager
					.isFilterSet((DatabaseActivity) getActivity());
			switch (item.filterId) {
			case API.ARTICLE_CASE_STUDIES:
				try {

					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.dashboard_icon_projects));

					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());
						filtered = true;
					}
				} catch (Exception e) {
					e.printStackTrace();

				}

				break;
			case API.ARTICLE_EVENTS:
				try {
					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.dashboard_icon_events));
					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());

						filtered = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case API.ARTICLE_EXPERTS:
				try {
					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.dashboard_icon_experts));
					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());
						filtered = true;
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				break;
			case API.ARTICLE_METHODS:
				try {
					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.dashboard_icon_methods));
					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());
						filtered = true;
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				break;
			case API.ARTICLE_NEWS:
				try {
					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources().getDrawable(
									R.drawable.dashboard_icon_news));
					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());
						filtered = true;
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
				break;
			case API.ARTICLE_QA:
				try {
					((ImageView) view.findViewById(R.id.navbarIcon))
							.setImageDrawable(getResources()
									.getDrawable(
											R.drawable.dashboard_icon_practical_knowledge));
					if (!filterNotSet) {
						filteredArticles = ArticleManager.countArticleByFilter(
								item.type, (DatabaseActivity) getActivity());
						filtered = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			if (filteredArticles == -1) {
				filtered = false;
			}
			if (!filtered) {
				Util.logDebug(logTag, "not filtered: " + item.type);
				((TextView) view.findViewById(R.id.navbarItemName))
						.setText(item.name + " (" + item.amountArticles + ")");
			} else {
				Util.logDebug(logTag, "filtered: " + item.type);
				v.findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
				((TextView) view.findViewById(R.id.navbarItemName))
						.setText(item.name + " ( " + filteredArticles + " / "
								+ item.amountArticles + " )");
			}
			if (getActivity().getClass().getSimpleName()
					.equals(ArticleDetails.class.getSimpleName())
					&& item.type.equals(ArticleDetails.articleType)
					&& ArticleDetails.articleSwitch) {
				Util.logDebug(logTag, "start innerArticle");
				setInnerArticleAdapter(item.type, view);
			}
			return view;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		outState.putString("tab", "Navigationbar");

		super.onSaveInstanceState(outState);

	}

	@Override
	public void dataChanged() {
		Util.logDebug(logTag, "dataChanged");
		v.findViewById(R.id.filterLayout).setVisibility(View.INVISIBLE);
		AdapterUtils.fillLinearLayoutFromAdapter(1,
				(LinearLayout) v.findViewById(R.id.dashboardDataListView),
				new NavigationbarAdapter(), getActivity(), true, true, 2);
		if (login != null) {
			try {
				if (Util.isUserAuthentificated(getActivity())) {
					login.setText(Util.getApplicationString("label.logout",
							getActivity()));
					loginImage.setImageDrawable(getActivity().getResources()
							.getDrawable(R.drawable.icon_subnavi_logout));
				} else {
					login.setText(Util.getApplicationString("label.login",
							getActivity()));
					loginImage.setImageDrawable(getActivity().getResources()
							.getDrawable(R.drawable.icon_subnavi_login));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		ArticleManager.observable.unregisterObserver(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		ArticleManager.observable.registerObserver(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!getActivity().getClass().getSimpleName()
				.equals(ArticleList.class.getSimpleName())) {
			buttonNewArticle.setVisibility(View.GONE);
		} else {
			buttonNewArticle.setVisibility(View.VISIBLE);
			String type = "";
			try {
				if (ArticleList.type.equals(Article.STUDY)) {
					type = Util.getApplicationString("label.add_project",
							getActivity());
				} else if (ArticleList.type.equals(Article.METHOD)) {
					type = Util.getApplicationString("label.add_method",
							getActivity());
				} else if (ArticleList.type.equals(Article.QA)) {
					type = Util.getApplicationString("label.add_qa",
							getActivity());
				} else if (ArticleList.type.equals(Article.EVENT)) {
					type = Util.getApplicationString("label.add_event",
							getActivity());
				} else if (ArticleList.type.equals(Article.EXPERT)) {
					type = Util.getApplicationString("label.add_expert",
							getActivity());
				} else if (ArticleList.type.equals(Article.NEWS)) {
					type = Util.getApplicationString("label.add_news",
							getActivity());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			buttonNewArticle.setText(type);
		}

		v.findViewById(R.id.filterLayout).setVisibility(View.INVISIBLE);
		adapter = new NavigationbarAdapter();
		AdapterUtils.fillLinearLayoutFromAdapter(1,
				(LinearLayout) v.findViewById(R.id.dashboardDataListView),
				adapt1er, getActivity(), true, true, 2);
	}
}
