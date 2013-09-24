/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.listener.OnStepSwitchListener;
import com.your.name.dao.Article;
import com.your.name.fragments.addArticle.Additional_Information;
import com.your.name.fragments.addArticle.Completition;
import com.your.name.fragments.addArticle.Filter;
import com.your.name.fragments.addArticle.Images;
import com.your.name.fragments.addArticle.Linked_Articles;
import com.your.name.fragments.addArticle.Title;
import com.your.name.service.API;

/* 
 * This class shows all steps in the "add article" process. 
 */
public class NewArticle extends NavigationActivity {

	public final static String ARTICLE = "article";
	public final static String NEXT_STEP = "step";

	private NavigationItem item = null;
	public static Article article = null;

	private int step = 1;

	public NewArticle() {
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			switch (item.filterId) {
			case API.ARTICLE_CASE_STUDIES:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_project", this));

				break;
			case API.ARTICLE_EVENTS:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_event", this));
				break;
			case API.ARTICLE_EXPERTS:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_expert", this));
				break;
			case API.ARTICLE_METHODS:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_method", this));
				break;
			case API.ARTICLE_NEWS:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_news", this));
				break;
			case API.ARTICLE_QA:
				((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
						.getApplicationString("label.add_qa", this));
				break;
			}
		} catch (Exception e) {

		}
	}

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_newarticle);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		item = (NavigationItem) getIntent().getExtras().getSerializable(
				ArticleList.ITEM_TPE);

		step = getIntent().getExtras().getInt(NEXT_STEP);

		article = (Article) getIntent().getExtras().getSerializable(ARTICLE);

		if (article == null) {
			article = new Article();
			article.created = new Date();
		}

		if (item == null) {
			item = new NavigationItem();
			item.type = article.type;

			if (item.type.equals(Article.STUDY)) {
				item.filterId = API.ARTICLE_CASE_STUDIES;
			} else if (item.type.equals(Article.METHOD)) {
				item.filterId = API.ARTICLE_METHODS;
			} else if (item.type.equals(Article.QA)) {
				item.filterId = API.ARTICLE_QA;
			} else if (item.type.equals(Article.EXPERT)) {
				item.filterId = API.ARTICLE_EXPERTS;
			} else if (item.type.equals(Article.EVENT)) {
				item.filterId = API.ARTICLE_EVENTS;
			} else if (item.type.equals(Article.NEWS)) {
				item.filterId = API.ARTICLE_NEWS;
			}
		}

		try {
			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep1);
				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step1", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step1", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step1", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step1", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step1", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step1", this));
					break;
				}
				view.setOnClickListener(new OnStepSwitchListener(1, step, item,
						this, article));

				view = (TextView) this.findViewById(R.id.textViewleftbarStep2);

				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step2", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step2", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step2", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step2", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step2", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step2", this));
					break;
				}

				view.setOnClickListener(new OnStepSwitchListener(2, step, item,
						this, article));

				view = (TextView) this.findViewById(R.id.textViewleftbarStep3);

				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step3", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step3", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step3", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step3", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step3", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step3", this));
					break;
				}

				view.setOnClickListener(new OnStepSwitchListener(3, step, item,
						this, article));

				view = (TextView) this.findViewById(R.id.textViewleftbarStep4);

				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step4", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step4", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step4", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step4", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step4", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step4", this));
					break;
				}

				view.setOnClickListener(new OnStepSwitchListener(4, step, item,
						this, article));

				view = (TextView) this.findViewById(R.id.textViewleftbarStep5);

				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step5", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step5", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step5", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step5", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step5", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step5", this));
					break;
				}

				view.setOnClickListener(new OnStepSwitchListener(5, step, item,
						this, article));

				view = (TextView) this.findViewById(R.id.textViewleftbarStep6);

				switch (item.filterId) {
				case API.ARTICLE_CASE_STUDIES:
					view.setText(Util.getApplicationString(
							"label.new_article_project_step6", this));
					break;
				case API.ARTICLE_EVENTS:
					view.setText(Util.getApplicationString(
							"label.new_article_event_step6", this));
					break;
				case API.ARTICLE_EXPERTS:
					view.setText(Util.getApplicationString(
							"label.new_article_expert_step6", this));
					break;
				case API.ARTICLE_METHODS:
					view.setText(Util.getApplicationString(
							"label.new_article_method_step6", this));
					break;
				case API.ARTICLE_NEWS:
					view.setText(Util.getApplicationString(
							"label.new_article_news_step6", this));
					break;
				case API.ARTICLE_QA:
					view.setText(Util.getApplicationString(
							"label.new_article_qa_step6", this));
					break;
				}

				view.setOnClickListener(new OnStepSwitchListener(6, step, item,
						this, article));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		FragmentManager manager = getSupportFragmentManager();

		FragmentTransaction transaction = manager.beginTransaction();

		switch (step) {
		case 1:
			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_linked_articles);

			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep1);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));
			}

			transaction.add(R.id.contentLayout, new Title(item));
			transaction.commit();
			break;
		case 2:
			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_linked_articles);

			if (Util.isTablet(this)) {
				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep2);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));

				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
			}

			transaction.add(R.id.contentLayout,
					new Additional_Information(item));
			transaction.commit();
			break;
		case 3:

			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_linked_articles);

			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep3);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));

				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep2).setVisibility(View.VISIBLE);
			}

			transaction.add(R.id.contentLayout, Images.newInstance(item));
			transaction.commit();
			break;

		case 4:
			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_linked_articles);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_linked_articles);

			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep4);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));

				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep2).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep3).setVisibility(View.VISIBLE);
			}

			transaction.add(R.id.contentLayout, new Linked_Articles(item));
			transaction.commit();
			break;

		case 5:
			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_linked_articles);

			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep5);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));
				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep2).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep3).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep4).setVisibility(View.VISIBLE);
			}

			transaction.add(R.id.contentLayout, new Filter(item));
			transaction.commit();
			break;

		case 6:
			this.findViewById(R.id.step1).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step2).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step3).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step4).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step5).setBackgroundResource(
					R.color.background_dark_highlighted);
			this.findViewById(R.id.step6).setBackgroundResource(
					R.color.background_dark_highlighted);

			if (Util.isTablet(this)) {
				TextView view = (TextView) this
						.findViewById(R.id.textViewleftbarStep6);
				view.setBackgroundResource(R.color.background_interferer);
				view.setTextColor(getResources()
						.getColor(android.R.color.white));
				this.findViewById(R.id.iconStep1).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep2).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep3).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep4).setVisibility(View.VISIBLE);
				this.findViewById(R.id.iconStep5).setVisibility(View.VISIBLE);
			}

			transaction.add(R.id.contentLayout, new Completition(item));
			transaction.commit();
			break;

		}
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.contentLayout);
		if (fragment != null) {
			if (fragment.getClass().getSimpleName()
					.equals(Completition.class.getSimpleName())) {

			} else {
				View v = fragment.getView().findViewById(R.id.backButton);

				if (v != null) {
					v.performClick();
				} else {
					super.onBackPressed();
				}
			}
		}
	}

}
