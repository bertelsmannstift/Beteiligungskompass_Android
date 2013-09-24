/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.Planning;
import com.your.name.dao.Article;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.CriterionManager;
import com.your.name.dao.helper.DataObserver;
import com.your.name.dao.helper.FavoritManager;

/*
 * this fragment defines the filter settings for a special article type. 
 */
public class FilterList extends DialogFragment implements DataObserver {

	private static String logTag = FilterList.class.toString();
	FilterListAdapter adapter = null;
	private List<Criterion> criteria = null;
	private static String type = "";

	private boolean isPlannerView = false;

	private int count, methodCount, exampleCount = 0;
	private EditText search;
	private ListView filterList;

	private boolean showDescription = false;

	private static String CRITERIA_KEY = "criteria";
	private static String SHOW_DESCRIPTION_KEY = "show_description";

	/*
	 * if show_description == true, show the description instead of the title if
	 * it exists
	 */
	public static FilterList newInstance(List<Criterion> criteria,
			boolean show_description) {
		Bundle args = new Bundle();

		ArrayList<Criterion> criterialList = new ArrayList<Criterion>();
		criterialList.addAll(criteria);

		Util.logDebug(logTag, "size of criteriaList: " + criterialList.size());

		args.putSerializable(CRITERIA_KEY, criterialList);
		args.putBoolean(SHOW_DESCRIPTION_KEY, show_description);

		FilterList list = new FilterList();
		list.setArguments(args);

		return list;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (!getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())
				|| (Util.isTablet(getActivity())
						&& Util.isOrientationPortrait(getActivity()) || !Util
							.isTablet(getActivity()))) {
			final View view = getActivity().getLayoutInflater().inflate(
					R.layout.fragment_filter_list, null);

			buildView(view);

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					getActivity());
			try {
				dialogBuilder.setTitle(Util.getApplicationString(
						"label.filter_criteria", getActivity()));
				dialogBuilder.setView(view);
				dialogBuilder
						.setNeutralButton(Util.getApplicationString(
								"label.close", getActivity()),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (dialog != null) {
											dialog.dismiss();
										}

									}
								});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return dialogBuilder.show();
		} else {
			return super.onCreateDialog(savedInstanceState);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		isPlannerView = getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName()) ? true : false;
		if (getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())
				|| (Util.isTablet(getActivity()) && !Util
						.isOrientationPortrait(getActivity()))) {

			final View view = inflater.inflate(R.layout.fragment_filter_list,
					container, false);

			buildView(view);

			return view;
		} else {
			return super.onCreateView(inflater, container, savedInstanceState);
		}

	}

	@SuppressWarnings("unchecked")
	private void buildView(View view) {
		this.criteria = (ArrayList<Criterion>) getArguments().getSerializable(
				CRITERIA_KEY);

		showDescription = getArguments().getBoolean(SHOW_DESCRIPTION_KEY);

		try {
			((Button) view.findViewById(R.id.clearFilter))
					.setText(Util.getApplicationString("label.delete_filter",
							getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())) {
			if (!FavoritManager.isFilterSet((DatabaseActivity) getActivity())) {
				view.findViewById(R.id.clearFilter).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.clearFilter).setVisibility(View.GONE);
			}
		}
		search = (EditText) view.findViewById(R.id.freitextsuche);

		final TextView clearFilter = (TextView) view
				.findViewById(R.id.clearSearch);
		clearFilter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				search.setText("");
				FavoritManager.searchText = "";
				FavoritManager.observable.notifyDatasetChanged();

			}
		});

		search.setText(FavoritManager.searchText);

		search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					FavoritManager.searchText = "";
					FavoritManager.searchText = ((EditText) v).getText()
							.toString();
					FavoritManager.observable.notifyDatasetChanged();
					InputMethodManager inputManager = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(
							search.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		view.findViewById(R.id.filter_seperator).setVisibility(View.VISIBLE);

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		adapter = new FilterListAdapter();

		filterList = ((ListView) view.findViewById(R.id.filterList));

		filterList.setAdapter(adapter);

		filterList.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);

		if (getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())) {
			getActivity().findViewById(R.id.navbartext).setVisibility(
					View.VISIBLE);

			getActivity().findViewById(R.id.navbar).setVisibility(
					View.INVISIBLE);

			dataChanged();
		}
	}

	public void setType(String type) {
		try {
			FilterList.type = type;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private int getLastSelectedOption(Criterion criterion) throws SQLException {
		int index = -1;
		for (int i = 0; i < criterion.visibleOptions.size(); i++) {
			CriteriaOption option = (CriteriaOption) criterion.visibleOptions
					.get(i);

			if (option.selected) {
				index = i;
			}
		}

		return index;
	}

	private String getSelectedOptionsAsString(Criterion criterion)
			throws SQLException {
		int numberOfSelectedItems = 0;

		String result = "";
		for (CriteriaOption option : criterion.visibleOptions) {
			if (null != option.selected && true == option.selected) {
				if (numberOfSelectedItems++ > 0) {
					result += ", ";
				}
				result += option.title;
			}
		}

		Util.logDebug(logTag, "Selected options: " + result
				+ " for criterion with title: " + criterion.title);

		if (criterion.type.equals("check") && numberOfSelectedItems == 0) {
			try {
				result = Util.getApplicationString(
						"global.select_mone_selected_text", getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ((criterion.type.equals("radio") || criterion.type
				.equals("select")) && numberOfSelectedItems == 0) {
			if (result.length() == 0) {
				try {

					for (CriteriaOption option : criterion.options) {
						if (option.default_value) {
							result = option.title;
							break;
						}
					}

					if (result.length() == 0) {
						result = criterion.visibleOptions.get(0).title;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	/*
	 * The items getting displayed in a Dialog that enable choice of options
	 */
	private class CriterionOptionListAdapter extends BaseAdapter {
		Dialog dialog;
		Criterion criterion;

		public CriterionOptionListAdapter(Criterion criterion, Dialog dialog) {
			this.criterion = criterion;
			this.dialog = dialog;
		}

		@Override
		public int getCount() {
			return criterion.visibleOptions.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			final int pos = position;

			CriteriaOption option = (CriteriaOption) criterion.visibleOptions
					.get(pos);
			View v = arg1;

			CriterionOptionHolder holder;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.filter_list_item_dialog_item_with_checkbox,
						null);

				holder = new CriterionOptionHolder();

				holder.box = ((CheckBox) v.findViewById(R.id.checkbox));
				holder.box.setTextColor(getResources().getColor(
						android.R.color.black));
				holder.text = ((TextView) v.findViewById(R.id.title));
				holder.text.setTextColor(getResources().getColor(
						android.R.color.black));

				v.setTag(holder);
			} else {
				holder = (CriterionOptionHolder) v.getTag();
			}

			if (false == criterion.type.equals("check")) {
				holder.box.setVisibility(View.GONE);
				if (option.parentOption_id > 0) {
					holder.text.setText(" - " + option.title);
				} else {
					holder.text.setText(option.title);
				}

				v.setFocusable(true);
				v.setClickable(true);

				v.setOnClickListener(new AdapterView.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							Util.logDebug(logTag, "somone pressed me");
							CriterionManager.setSelectedOption(criterion, pos,
									(DatabaseActivity) getActivity());
						} catch (Exception e) {
							Util.logDebug(logTag,
									"something went wrong updating the selected option");
							e.printStackTrace();
						}
						// adapter.notifyDataSetChanged();
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				});

			} else {
				v.setClickable(false);
				holder.text.setVisibility(View.GONE);
				holder.box.setText(option.title);
				holder.box.setOnCheckedChangeListener(null);
				holder.box
						.setChecked((option.selected != null && option.selected) ? true
								: false);

				holder.box
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {

								try {
									Util.logDebug(logTag, "did we get checked?");
									CriteriaOption option = (CriteriaOption) criterion.visibleOptions
											.toArray()[pos];
									option.selected = isChecked;
									criterion.visibleOptions.get(pos).selected = isChecked;
									CriterionManager.setSelectedOption(option,
											isChecked,
											(DatabaseActivity) getActivity());

									ArticleManager.observable
											.notifyDatasetChanged();

								} catch (Exception e) {
									Util.logDebug(logTag,
											"something went wrong updating the selected options");
									e.printStackTrace();
								}

							}
						});

			}

			return v;
		}
	}

	class FilterListHolder {
		Criterion criterion;
		TextView text, currenValue;
		SeekBar bar;
		Button methodButton, exampleButton;
	}

	class CriterionOptionHolder {
		TextView text;
		CheckBox box;
	}

	class FilterListAdapter extends BaseAdapter {

		private static final int VIEW_TYPE_CHECK = 1;
		private static final int VIEW_TYPE_RESOURCE = 2;
		private static final int VIEW_TYPE_BUTTON = 3;

		public FilterListAdapter() {
		}

		@Override
		public int getItemViewType(int position) {

			if (criteria.size() == position
					&& getActivity().getClass().getSimpleName()
							.equals(Planning.class.getSimpleName())) {
				return 3;
			}

			if (criteria.get(position).type.equals("resource")) {
				return 2;
			} else {
				return 1;
			}
		}

		@Override
		public int getViewTypeCount() {
			if (getActivity().getClass().getSimpleName()
					.equals(Planning.class.getSimpleName())) {
				return 4;
			}
			return 3;
		}

		@Override
		public int getCount() {
			if (getActivity().getClass().getSimpleName()
					.equals(Planning.class.getSimpleName())) {
				return (criteria.size() + 1);
			}
			return (criteria.size());
		}

		@Override
		public Object getItem(int position) {
			return criteria.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			try {

				final int pos = position;

				int type = getItemViewType(position);

				View v = arg1;

				FilterListHolder holder;
				holder = new FilterListHolder();

				switch (type) {
				case VIEW_TYPE_BUTTON:
					v = getActivity().getLayoutInflater().inflate(
							R.layout.filter_list_item_type_button, null);

					holder.methodButton = ((Button) v
							.findViewById(R.id.methodResults));
					holder.exampleButton = ((Button) v
							.findViewById(R.id.exampleResults));
					break;
				case VIEW_TYPE_CHECK:

					v = getActivity().getLayoutInflater().inflate(
							R.layout.filter_list_item_type_check, null);

					holder.text = ((TextView) v.findViewById(R.id.title));
					holder.currenValue = ((TextView) v
							.findViewById(R.id.selectionButton));

					break;
				case VIEW_TYPE_RESOURCE:
					v = getActivity().getLayoutInflater().inflate(
							R.layout.filter_list_item_type_resource, null);

					holder.bar = ((SeekBar) v.findViewById(R.id.seekBar));
					holder.text = ((TextView) v.findViewById(R.id.title));
					holder.currenValue = ((TextView) v
							.findViewById(R.id.currentValue));
					break;
				}

				if (pos == (criteria.size())
						&& getActivity().getClass().getSimpleName()
								.equals(Planning.class.getSimpleName())) {
					holder.methodButton.setText(Util.getApplicationString(
							"module.methods.title",
							FilterList.this.getActivity())
							+ ": " + methodCount);

					holder.exampleButton.setText(Util.getApplicationString(
							"module.studies.title",
							FilterList.this.getActivity())
							+ ": " + exampleCount);

					return v;
				}

				final Criterion criterion = criteria.get(pos);

				/*
				 * we have to hide all criteria which are shown in the planer
				 * view
				 */
				if (!isPlannerView && criterion.show_in_planner) {
					return new FrameLayout(getActivity());
				}

				/*
				 * Only the resource filter type needs a special treatment
				 */
				if (criterion.type.equals("resource")) {
					if (true == getArguments().getBoolean(SHOW_DESCRIPTION_KEY)) {

						holder.text.setText(CriterionManager
								.getCriterionDescription(criterion));

					} else {
						holder.text.setText(criterion.title);
					}
					int critIndex = getLastSelectedOption(criterion);
					holder.bar.setMax((criterion.visibleOptions.size()-1));

					String text = "";
					final CriteriaOption option = (CriteriaOption) criterion.visibleOptions
							.get((critIndex < 0) ? 0 : critIndex);
					if (critIndex >= 0) {
						holder.bar.setProgress(critIndex);

						text = option.title;
						holder.currenValue.setText(text);
					}
					if (critIndex == -1) {
						holder.currenValue.setText(Util.getApplicationString(
								"global.select_mone_selected_text",
								getActivity()));
						holder.bar.setProgress(0);
						holder.currenValue.setTextColor(getResources()
								.getColor(android.R.color.black));
					} else if (!text.equals(Util.getApplicationString(
							"global.select_mone_selected_text", getActivity()))) {
						holder.currenValue.setTextColor(getResources()
								.getColor(R.color.background_interferer));
					} else {
						holder.currenValue.setTextColor(getResources()
								.getColor(android.R.color.black));
					}

					final View parentView = v;

					holder.bar
							.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

								private int progress = 0;

								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {
									try {

										CriterionManager
												.setSelectedResourceOption(
														criterion,
														progress,
														(DatabaseActivity) getActivity());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

								@Override
								public void onStartTrackingTouch(SeekBar seekBar) {

								}

								@Override
								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									this.progress = progress;
									if (true == fromUser) {
										((TextView) parentView
												.findViewById(R.id.currentValue))
												.setText(option.title);

									}
								}
							});

					return v;
				}

				if (showDescription) {
					holder.text.setText(criterion.description);
				} else {
					holder.text.setText(criterion.title);
				}

				String text = getSelectedOptionsAsString(criterion);

				boolean onlyDefaultValue = true;

				for (CriteriaOption option : criterion.visibleOptions) {
					if (null != option.selected && true == option.selected
							&& !option.default_value) {
						onlyDefaultValue = false;
						break;
					}
				}

				holder.currenValue.setText(text);

				if (onlyDefaultValue) {
					holder.currenValue.setTextColor(getResources().getColor(
							android.R.color.black));
				} else if (!text.equals(Util.getApplicationString(
						"global.select_mone_selected_text", getActivity()))) {
					holder.currenValue.setTextColor(getResources().getColor(
							R.color.background_interferer));
				} else {
					holder.currenValue.setTextColor(getResources().getColor(
							android.R.color.black));
				}

				v.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Util.logDebug(logTag, "we should show a dialog now");
						Dialog selectionDialog;

						AlertDialog.Builder dialog = new AlertDialog.Builder(
								getActivity());

						{
							View dialogContentView = getActivity()
									.getLayoutInflater()
									.inflate(
											R.layout.filter_list_item_choices_dialog,
											null);

							ListView choicesListView = (ListView) dialogContentView
									.findViewById(R.id.choicesList);
							try {
								dialog.setView(dialogContentView);
								dialog.setTitle(criterion.title);
								dialog.setNeutralButton(Util
										.getApplicationString("label.close",
												FilterList.this.getActivity()),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												if (dialog != null) {
													dialog.dismiss();
												}

											}
										});
							} catch (Exception e) {
								e.printStackTrace();
							}
							selectionDialog = dialog.create();

							choicesListView.setAdapter(new CriterionOptionListAdapter(
									criteria.get(pos), selectionDialog));

						}

						selectionDialog.show();
					}
				});

				return v;
			} catch (Exception e) {
				e.printStackTrace();
				return new View(getActivity());
			}
		}

		@Override
		public void notifyDataSetChanged() {

			if (Util.isTablet(getActivity())
					&& !getActivity().getClass().getSimpleName()
							.equals(Planning.class.getSimpleName())) {
				try {
					criteria = CriterionManager.getCriteriaByType(type);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.notifyDataSetChanged();
		}
	}

	@Override
	public void dataChanged() {
		Util.logDebug(logTag, "datachanged");
		boolean selectionFound = false;
		count = 0;
		try {

			if (getActivity().getClass().getSimpleName()
					.equals(Planning.class.getSimpleName())) {

				int tmp = ArticleManager.countArticleByFilter(Article.EVENT,
						(Planning) getActivity(), false, true);

				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				tmp = ArticleManager.countArticleByFilter(Article.EXPERT,
						(Planning) getActivity(), false, true);

				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				tmp = ArticleManager.countArticleByFilter(Article.METHOD,
						(Planning) getActivity(), false, true);
				methodCount = (tmp == -1) ? ArticleManager.countArticlesByType(
						Article.METHOD, (DatabaseActivity) getActivity()) : tmp;
				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				tmp = ArticleManager.countArticleByFilter(Article.NEWS,
						(Planning) getActivity(), false, true);
				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				tmp = ArticleManager.countArticleByFilter(Article.QA,
						(Planning) getActivity(), false, true);
				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				tmp = ArticleManager.countArticleByFilter(Article.STUDY,
						(Planning) getActivity(), false, true);
				exampleCount = (tmp == -1) ? ArticleManager
						.countArticlesByType(Article.STUDY,
								(DatabaseActivity) getActivity()) : tmp;
				selectionFound = (tmp > -1) ? true : false;
				count += (tmp == -1) ? 0 : tmp;

				Button clearFilter = (Button) getActivity().findViewById(
						R.id.clearFilter);
				if (!selectionFound) {
					search.setText("");
					count = ArticleManager
							.countAllArticles((Planning) getActivity());
					clearFilter.setVisibility(View.GONE);
				} else {
					clearFilter.setVisibility(View.VISIBLE);
					if (!ArticleManager.searchText.equals("")) {
						clearFilter.setText(Util.getApplicationString(
								"label.delete_search", getActivity()));
					} else {
						search.setText("");
						clearFilter.setText(Util.getApplicationString(
								"label.delete_filter", getActivity()));
					}
				}

				this.criteria = CriterionManager.getCriteriaByType("", true,
						false, null);
				if ((methodCount + exampleCount) == 0) {
					Toast.makeText(getActivity(),
							getString(R.string.no_article_found),
							Toast.LENGTH_LONG).show();
				}
				((TextView) getActivity().findViewById(R.id.navbartext))
						.setText(Util.getApplicationString("label.results",
								FilterList.this.getActivity()) + ": " + count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		ArticleManager.observable.registerObserver(this);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStop() {
		ArticleManager.observable.unregisterObserver(this);
		super.onStop();
	}

}