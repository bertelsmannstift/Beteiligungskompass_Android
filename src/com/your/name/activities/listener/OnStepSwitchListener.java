/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;

public class OnStepSwitchListener implements OnClickListener {

	private int mStepId;
	private Article mArticle;
	private int mCurrentStep = mStepId;
	private NavigationItem mItem;
	private Activity mActivity;

	public OnStepSwitchListener(int stepid, int currentStep,
			NavigationItem item, Activity activity, Article article) {
		this.mStepId = stepid;
		this.mCurrentStep = currentStep;
		this.mItem = item;
		this.mActivity = activity;
		this.mArticle = article;
	}

	@Override
	public void onClick(View v) {

		FragmentManager manager = ((NewArticle) mActivity)
				.getSupportFragmentManager();

		android.support.v4.app.Fragment fragment = manager
				.findFragmentById(R.id.contentLayout);

		if (fragment != null) {
			if (mCurrentStep > 1 && (mStepId - mCurrentStep) > 1) {
				Intent intent = new Intent(mActivity, NewArticle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(NewArticle.ITEM_TPE, mItem);
				intent.putExtra(NewArticle.ARTICLE, mArticle);
				intent.putExtra(NewArticle.NEXT_STEP, mStepId);
				mActivity.startActivity(intent);
				mActivity.finish();
			} else if (mStepId > mCurrentStep) {
				fragment.getView().findViewById(R.id.nextStepButton)
						.performClick();
			} else if (mCurrentStep == 6) {
				Intent intent = new Intent(mActivity, NewArticle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(NewArticle.ITEM_TPE, mItem);
				intent.putExtra(NewArticle.ARTICLE, mArticle);
				intent.putExtra(NewArticle.NEXT_STEP, mStepId);
				mActivity.startActivity(intent);
				mActivity.finish();

			} else if ((mStepId - mCurrentStep) < -1) {
				Intent intent = new Intent(mActivity, NewArticle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(NewArticle.ITEM_TPE, mItem);
				intent.putExtra(NewArticle.ARTICLE, mArticle);
				intent.putExtra(NewArticle.NEXT_STEP, mStepId);
				mActivity.startActivity(intent);
				mActivity.finish();

			} else if (mStepId < mCurrentStep) {
				fragment.getView().findViewById(R.id.backButton).performClick();

			}
		}
	}
}