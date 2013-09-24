/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.your.name.R;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;

/* 
 * this fragment shows the criteria in an article detail view. based on these
 * criteria that article could be filtered.
 * 
 */
public class ShowCriteria extends DialogFragment {

	private ArrayList<Criterion> criteria = null;

	private ArrayList<Criterion> tmpCriterion = null;

	@SuppressWarnings("unchecked")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_show_criteria,
				container, false);
		getDialog().setTitle("Kriterien");
		criteria = (ArrayList<Criterion>) getArguments().getSerializable(
				"Filter");
		tmpCriterion = new ArrayList<Criterion>();
		for (Criterion criterion : criteria) {
			if (criterion.options != null && criterion.options.size() > 0) {
				tmpCriterion.add(criterion);
			}
		}

		ExpandableListView lv = (ExpandableListView) view
				.findViewById(R.id.expandableListViewCriteria);

		ShowCriteriaAdapter adapter = new ShowCriteriaAdapter();
		lv.setAdapter(adapter);

		lv.expandGroup(0);
		return view;
	}

	public static ShowCriteria newInstance(List<Criterion> criteria,
			boolean show_description) {
		Bundle args = new Bundle();

		ArrayList<Criterion> tmp = new ArrayList<Criterion>();
		tmp.addAll(criteria);

		args.putSerializable("Filter", tmp);

		ShowCriteria list = new ShowCriteria();
		list.setArguments(args);

		return list;
	}

	private class ShowCriteriaAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return tmpCriterion.get(groupPosition).visibleOptions
					.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return tmpCriterion.get(groupPosition).visibleOptions.size();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;

			if (null == v) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.showcriteria_child_layout, null);
			}

			((TextView) v.findViewById(R.id.showCriteriaTitle))
					.setText(((CriteriaOption) getChild(groupPosition,
							childPosition)).title);

			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return tmpCriterion.get(groupPosition).visibleOptions.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return tmpCriterion.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return tmpCriterion.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View v = convertView;

			if (null == v) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.articlelist_group_layout, null);
			}

			if (!isExpanded) {
				((ImageView) v.findViewById(R.id.imageViewArticleGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_down));
			} else {
				((ImageView) v.findViewById(R.id.imageViewArticleGroup))
						.setImageDrawable(getResources().getDrawable(
								R.drawable.icon_arr_up));
			}

			((TextView) v.findViewById(R.id.articleGroupTitle))
					.setText(((Criterion) getGroup(groupPosition)).title);

			ExpandableListView elv = (ExpandableListView) parent;

			elv.expandGroup(groupPosition);

			return v;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}
}
