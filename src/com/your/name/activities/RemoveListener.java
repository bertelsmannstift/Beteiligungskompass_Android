/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.your.name.NavigationItem;
import com.your.name.dao.Article;
import com.your.name.fragments.addArticle.Images;
import com.your.name.fragments.dashboard.Navigation;

public class RemoveListener implements OnClickListener {

	private Activity _activity;
	private NavigationItem _item;

	public RemoveListener(Activity activity, Article article,
			NavigationItem item) {
		this._activity = activity;
		this._item = item;
	}

	@Override
	public void onClick(View v) {

		Images.mMemoryCache = null;
		NewArticle.article = null;
		Intent intent = new Intent(_activity, ArticleList.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Navigation.NAVIGATION_ITEM, _item);
		_activity.startActivity(intent);
		_activity.finish();

	}
}
