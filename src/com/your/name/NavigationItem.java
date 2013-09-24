/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name;

import java.io.Serializable;

import com.your.name.activities.NavigationActivity;
import com.your.name.dao.Article;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.service.API;
/**
 * this class communicates with the getNavigationItems method in the API.java
 * There is nothing to change here. Only if you want to expand the navigation with additional items
 */
public class NavigationItem implements Serializable {
	private static final long serialVersionUID = 8512867947852856944L;

	public String name = "";
	public String description = "";
	public int filterId = 0;
	public String type = "";
	public boolean isActiv = true;
	public int amountArticles = 0;
	public int iconResourceID = 0;

	public NavigationItem() {

	}

	public NavigationItem(String name, String description, int filterId,
			boolean isActiv, NavigationActivity context) {
		this.name = name;
		this.description = description;
		this.filterId = filterId;
		this.isActiv = isActiv;
		try {
			switch (filterId) {
			case API.ARTICLE_CASE_STUDIES:
				amountArticles = ArticleManager.countArticlesByType(Article.STUDY,
						context);
				iconResourceID = R.drawable.dashboard_icon_projects;
				type = Article.STUDY;
				break;
			case API.ARTICLE_EVENTS:
				amountArticles = ArticleManager.countArticlesByType(Article.EVENT,
						context);
				iconResourceID = R.drawable.dashboard_icon_events;
				type = Article.EVENT;
				break;
			case API.ARTICLE_EXPERTS:
				amountArticles = ArticleManager.countArticlesByType(Article.EXPERT,
						context);
				iconResourceID = R.drawable.dashboard_icon_experts;
				type = Article.EXPERT;
				break;
			case API.ARTICLE_METHODS:
				amountArticles = ArticleManager.countArticlesByType(Article.METHOD,
						context);
				iconResourceID = R.drawable.dashboard_icon_methods;
				type = Article.METHOD;
				break;
			case API.ARTICLE_NEWS:
				amountArticles = ArticleManager.countArticlesByType(Article.NEWS,
						context);
				iconResourceID = R.drawable.dashboard_icon_news;
				type = Article.NEWS;
				break;
			case API.ARTICLE_QA:
				amountArticles = ArticleManager.countArticlesByType(Article.QA,
						context);
				iconResourceID = R.drawable.dashboard_icon_practical_knowledge;
				type = Article.QA;
				break;
			default:
				break;
			}
		} catch (Exception e) {

		}
	}
}
