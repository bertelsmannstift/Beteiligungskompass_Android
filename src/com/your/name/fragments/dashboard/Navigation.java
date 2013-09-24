/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.dashboard;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleDetails;
import com.your.name.activities.ArticleList;
import com.your.name.activities.NavigationActivity;
import com.your.name.activities.Planning;
import com.your.name.service.API;

/*
 * this fragment displays the navigation list menu. It receives the navigation
 * items from the API. At the moment it contains "studies", "methods", "experts",
 * "events", "news", "qa" and "planning". 
 */
public class Navigation extends Fragment {

	public static final String NAVIGATION_ITEM = "navigationItem";

	private ArrayList<NavigationItem> navigationItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_dashboard_navigation,
				container, false);

		try {
			((TextView) v.findViewById(R.id.textViewOwnTransaction))
					.setText(Util.getApplicationString("label.planning",
							getActivity()));
			if (v.findViewById(R.id.itemDescription) != null) {
				((TextView) v.findViewById(R.id.itemDescription)).setText(Util
						.getApplicationString("global.content_filter",
								getActivity()));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		navigationItems = API.getInstance(getActivity()).getNavigationItems(
				(NavigationActivity) getActivity());

		NavigationAdapter adapter = new NavigationAdapter();

		int cols = 1;
		int currentCol = 0;
		LinearLayout tableRow = null;
		for (int position = 0; position < adapter.getCount(); ++position) {
			/*
			 * At the start of a line create a new linear layout
			 */
			if (0 == currentCol) {
				tableRow = new LinearLayout(getActivity());
			}

			View child = adapter.getView(position, null, null);

			tableRow.addView(child);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child
					.getLayoutParams();

			params.weight = 1.0f;

			params.bottomMargin = 2;

			child.setLayoutParams(params);
			++currentCol;
			currentCol %= cols;

			/*
			 * if we reached the end of a line add the linear layout to the
			 * parent layout
			 */
			if (0 == currentCol) {
				((LinearLayout) v.findViewById(R.id.navigationTable))
						.addView(tableRow);

				params = (LinearLayout.LayoutParams) tableRow.getLayoutParams();
				params.weight = 1.0f;
				tableRow.setLayoutParams(params);
			}
		}
		return v;
	}

	private class NavigationAdapter extends BaseAdapter {

		public int getCount() {
			return navigationItems.size();
		}

		public Object getItem(int pos) {
			return null;
		}

		public long getItemId(int id) {
			return 0;
		}

		public View getView(int position, View conView, ViewGroup vg) {
			final NavigationItem item = navigationItems.get(position);

			NavigationHolder holder = null;

			View v = conView;

			if (v == null) {
				holder = new NavigationHolder();

				v = LayoutInflater.from(Navigation.this.getActivity()).inflate(
						R.layout.dashboard_navigation_listitem, null);

				((ImageView) v.findViewById(R.id.itemImage))
						.setImageDrawable(getActivity().getResources()
								.getDrawable(item.iconResourceID));

				holder.text = (TextView) v.findViewById(R.id.itemName);
				holder.description = (TextView) v
						.findViewById(R.id.itemDescription);

				v.setTag(holder);

			} else {
				holder = (NavigationHolder) v.getTag();
			}

			v.findViewById(R.id.navigationItemlayout).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							Intent intent = new Intent(getActivity(),
									ArticleList.class);
							intent.putExtra(NAVIGATION_ITEM, item);
							ArticleDetails.articleType = item.type;
							Util.switchActivity(intent, getActivity());
						}
					});

			holder.text.setText(item.name);
			holder.description.setText(item.description);

			return v;
		}
	}

	public void onPlanningClicked(View v) {
		Util.switchActivity(new Intent(getActivity(), Planning.class),
				getActivity());
	}

	private static class NavigationHolder {
		private TextView text, description = null;
	}
}