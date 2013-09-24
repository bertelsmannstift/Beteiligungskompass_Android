/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.adapter.SimpleSpinnerAdapter;
import com.your.name.dao.helper.ArticleManager;

/*
 * this fragment shows the sort criteria for the article list.
 */
public class SortArticle extends DialogFragment {

	SparseArray<String> sortBy = new SparseArray<String>();

	public static SortArticle newInstance(String type, boolean show_description) {
		Bundle args = new Bundle();

		args.putSerializable("Filter", type);

		SortArticle list = new SortArticle();
		list.setArguments(args);

		return list;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_sort_article,
				container, false);
		try {
			getDialog().setTitle(
					Util.getApplicationString("label.article_sort",
							getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String filter = getArguments().getString("Filter");

		sortBy = Util.getOrderByType(filter, getActivity());

		final String[] array = new String[sortBy.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = sortBy.valueAt(i);
		}
		try {
			((TextView) view.findViewById(R.id.spinnerSortBy)).setText(Util
					.getApplicationString("label.select_sort", getActivity()));

			((TextView) view.findViewById(R.id.textViewSortBy)).setText(Util
					.getApplicationString("label.sort_by", getActivity()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		View layout = view.findViewById(R.id.spinnerSelection);

		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog selectionDialog = new Dialog(getActivity());
				{
					View dialogContentView = getActivity().getLayoutInflater()
							.inflate(R.layout.filter_list_item_choices_dialog,
									null);

					ListView choicesListView = (ListView) dialogContentView
							.findViewById(R.id.choicesList);

					choicesListView
							.setBackgroundResource(android.R.color.white);

					choicesListView.setAdapter(new SimpleSpinnerAdapter(
							SortArticle.this.getActivity(), array));
					choicesListView
							.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
					choicesListView
							.setOnItemClickListener(new AdapterView.OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0,
										View arg1, int position, long arg3) {

									ArticleManager.sortBy = sortBy
											.keyAt(position);
									ArticleManager.observable
											.notifyDatasetChanged();
									if (selectionDialog != null) {
										selectionDialog.dismiss();
									}
									if (SortArticle.this.getDialog() != null) {
										SortArticle.this.getDialog().dismiss();
									}

								}
							});

					selectionDialog.setContentView(dialogContentView);
					try {
						selectionDialog.setTitle(Util.getApplicationString(
								"label.select_sort", getActivity()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				selectionDialog.show();

			}
		});

		return view;
	}
}
