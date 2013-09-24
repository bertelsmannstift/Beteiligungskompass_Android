/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.Criterion;
import com.your.name.dao.helper.CriterionManager;
import com.your.name.fragments.FilterList;

public class Planning extends NavigationActivity {
	public static String logTag = NavigationActivity.class.toString();

	private List<Criterion> getPlanningCriteria() throws Exception {
		List<Criterion> criteraInPlanner = CriterionManager.getCriteriaByType(
				"", true, false, null);

		return criteraInPlanner;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.setContentView(R.layout.activity_planning);

		this.findViewById(R.id.tabPlanningLayout).setBackgroundResource(
				R.color.background_dark_highlighted);

		try {
			/*
			 * only on first activity start add the fragment
			 */
			if (null == arg0) {
				FilterList filterList = FilterList.newInstance(
						getPlanningCriteria(), true);

				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();

				transaction.add(R.id.fragmentContainer, filterList);

				transaction.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
					.getApplicationString("module.planning.title",
							Planning.this));

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {

		if (findViewById(R.id.navbarLayout).getVisibility() == View.GONE) {
			Intent intent = new Intent(this, Dashboard.class);
			startActivity(intent);
			finish();
		}

		this.findViewById(R.id.navbarLayout).setVisibility(View.GONE);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
