/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleList;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.CriterionManager;

/**
 * this fragment will be used to add filter criteria to the new article.
 */
public class Filter extends Fragment {

	private static NavigationItem item;
	private FilterAdapter adapter;
	private static Article article;
	private List<Criterion> criteria = null;

	public Filter() {
	}

	public Filter(NavigationItem item) {
		Filter.item = item;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		article = (Article) getActivity().getIntent().getSerializableExtra(
				NewArticle.ARTICLE);
		if (article == null) {
			article = NewArticle.article;
		}

		View v = inflater.inflate(R.layout.fragment_newarticle_filter,
				container, false);

		try {
			if (article.critera == null || article.critera.size() == 0) {

				criteria = CriterionManager.getCriteriaByType(item.type);
				article.critera = criteria;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			((Button) v.findViewById(R.id.nextStepButton))
					.setText(Util.getApplicationString("label.save_and_next",
							getActivity()));

			((Button) v.findViewById(R.id.backButton)).setText(Util
					.getApplicationString("label.back", getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ListView listView = (ListView) v.findViewById(R.id.filterList);

		adapter = new FilterAdapter();

		listView.setAdapter(adapter);

		listView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);

		Button backButton = (Button) v.findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();

			}
		});

		Button nextStep = (Button) v.findViewById(R.id.nextStepButton);

		nextStep.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				adapter = null;
				NewArticle.article = article;
				Intent intent = new Intent(getActivity(), NewArticle.class);
				intent.putExtra(NewArticle.NEXT_STEP, 6);
				intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
				intent.putExtra(ArticleList.ITEM_TPE, item);
				startActivity(intent);
				getActivity().finish();
			}
		});

		return v;
	}

	private int getSelectedOptionIndex(Criterion criterion) {
		int index = 0;

		int defaultIndex = -1;

		for (CriteriaOption option : criterion.options) {
			if (true == option.default_value) {
				defaultIndex = index;
			}
			if (null != option.isArticleAddedToFilter
					&& true == option.isArticleAddedToFilter) {

				return index;
			}
			++index;
		}

		return defaultIndex;
	}

	class FilterListHolder {
		Criterion criterion;
		TextView text, currenValue;
		SeekBar bar;
		Button button;
	}

	class FilterOptionHolder {
		TextView text;
		CheckBox box;
	}

	private String getSelectedOptionsAsString(Criterion criterion) {
		int numberOfSelectedItems = 0;

		String result = "";

		for (CriteriaOption option : criterion.options) {
			if (null != option.isArticleAddedToFilter
					&& true == option.isArticleAddedToFilter) {
				if (numberOfSelectedItems++ > 0) {
					result += ", ";
				}
				result += option.title;
			}
		}

		if (criterion.type.equals("check") && numberOfSelectedItems == 0) {
			try {
				result = Util.getApplicationString(
						"global.select_mone_selected_text", getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private class FilterAdapter extends BaseAdapter {

		private static final int VIEW_TYPE_CHECK = 1;
		private static final int VIEW_TYPE_RESOURCE = 2;

		@Override
		public int getItemViewType(int position) {

			if (article.critera.get(position).type.equals("resource")) {
				return 2;
			} else {
				return 1;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public int getCount() {
			return article.critera.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {

				final int pos = position;

				View v = convertView;

				final Criterion crit = article.critera.get(position);

				FilterListHolder holder;

				int type = getItemViewType(position);

				if (v == null) {
					holder = new FilterListHolder();
					switch (type) {
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

					v.setTag(holder);

				} else {
					holder = (FilterListHolder) v.getTag();
				}

				/*
				 * Only the resource filter type needs a special treatment
				 */
				if (crit.type.equals("resource")) {

					holder.bar.setMax((crit.options.size() - 1));
					holder.bar.setProgress(getSelectedOptionIndex(crit));

					holder.text.setText(crit.title);
					CriteriaOption option = (CriteriaOption) crit.options
							.toArray()[getSelectedOptionIndex(crit)];
					String text = option.title;
					holder.currenValue.setText(text);

					if (!text.equals(Util.getApplicationString(
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

								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {
									try {
										ArticleManager
												.addArticleToOptions(
														(DatabaseActivity) getActivity(),
														article.id,
														article.critera
																.get(pos));

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
									if (true == fromUser) {

										CriteriaOption option = (CriteriaOption) article.critera
												.get(pos).options.toArray()[progress];

										option.isArticleAddedToFilter = true;
										boolean isArticleAddedToFilter = ((CriteriaOption) article.critera
												.get(pos).options.toArray()[((progress + 1))]).isArticleAddedToFilter;
										if (isArticleAddedToFilter) {
											((CriteriaOption) article.critera
													.get(pos).options.toArray()[((progress + 1))]).isArticleAddedToFilter = false;
										}
										((TextView) parentView
												.findViewById(R.id.currentValue))
												.setText(option.title);

									}
								}
							});

					return v;
				}

				holder.text.setText(crit.title);

				String text = getSelectedOptionsAsString(crit);

				if (text.equals("")) {
					text = Util.getApplicationString(
							"global.select_mone_selected_text", getActivity());
				}

				holder.currenValue.setText(text);

				v.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {

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
								dialog.setNeutralButton(Util
										.getApplicationString("label.close",
												Filter.this.getActivity()),
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

							choicesListView.setAdapter(new FilterOptionAdapter(
									article.critera.get(pos), selectionDialog));

						}

						selectionDialog.show();
					}
				});

				return v;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/*
	 * The items getting displayed in a Dialog that enables choice of options
	 */
	class FilterOptionAdapter extends BaseAdapter {
		Dialog dialog;
		Criterion criterion;

		public FilterOptionAdapter(Criterion criterion, Dialog dialog) {
			this.criterion = criterion;
			this.dialog = dialog;
		}

		@Override
		public int getCount() {
			return criterion.options.size();
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

			View v = arg1;

			FilterOptionHolder holder;

			if (v == null) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.filter_list_item_dialog_item_with_checkbox,
						null);

				holder = new FilterOptionHolder();
				holder.box = ((CheckBox) v.findViewById(R.id.checkbox));
				holder.text = ((TextView) v.findViewById(R.id.title));

				v.setTag(holder);
			} else {
				holder = (FilterOptionHolder) v.getTag();
			}

			holder.text.setTextColor(getResources().getColor(
					android.R.color.black));

			final CriteriaOption option = (CriteriaOption) criterion.options
					.toArray()[position];

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
							if (option.isArticleAddedToFilter == false) {

								for (CriteriaOption opt : criterion.options) {
									opt.isArticleAddedToFilter = false;
								}
								option.isArticleAddedToFilter = true;
								ArticleManager.addArticleToOptions(
										(DatabaseActivity) getActivity(),
										article.id, criterion);
							}
						} catch (Exception e) {

							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				});

			} else {
				v.setClickable(false);
				holder.text.setVisibility(View.GONE);
				holder.box.setText(option.title);
				holder.box.setTextColor(getResources().getColor(
						android.R.color.black));
				holder.box.setOnCheckedChangeListener(null);
				holder.box.setChecked(option.isArticleAddedToFilter);
				holder.box
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {

								try {

									option.isArticleAddedToFilter = isChecked;
									ArticleManager
											.addOrRemoveOneArticleToOption(
													(DatabaseActivity) getActivity(),
													article.id, option);

									criterion.options.update(option);
								} catch (Exception e) {
									e.printStackTrace();
								}
								adapter.notifyDataSetChanged();
							}
						});
			}
			return v;
		}
	}
}
