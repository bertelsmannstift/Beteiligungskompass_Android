/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities.Tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.your.name.Util;
import com.your.name.activities.ArticleDetails;

public class LoadOneArticleTask extends AsyncTask<Integer, Void, Void> {

	private Activity _activity;

	public LoadOneArticleTask(Activity activity) {
		_activity = activity;
	}

	private ProgressDialog dialog = null;

	@Override
	protected void onPreExecute() {
		try {
			dialog = ProgressDialog.show(_activity, "", Util
					.getApplicationString("label.message_loading_articles",
							_activity));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		if (!isCancelled()) {
			dialog.dismiss();
		}
	}

	@Override
	protected Void doInBackground(Integer... params) {
		Intent intent = new Intent(_activity, ArticleDetails.class);
		intent.putExtra(
				com.your.name.activities.ArticleDetails.ARTICLE_LIST,
				ArticleDetails.ids);
		intent.putExtra(
				com.your.name.activities.ArticleDetails.ARTICLE_ID_EXTRA,
				params[0]);
		intent.putExtra(ArticleDetails.ARTICLE_SWITCH, true);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		_activity.startActivity(intent);

		return null;
	}
}
