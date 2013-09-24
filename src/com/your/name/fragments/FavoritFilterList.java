/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.your.name.AdapterUtils;
import com.your.name.R;
import com.your.name.ResultValues;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.FavoritList;
import com.your.name.activities.Planning;
import com.your.name.dao.FavoriteGroup;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.DataObserver;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.service.API;

/* 
 * this fragment shows the filter list of the favorite part. 
 */
public class FavoritFilterList extends DialogFragment implements DataObserver {

	private static String logTag = FavoritFilterList.class.toString();

	private static View v;

	FavoriteGroupsListAdapter favoriteGroupsListAdapter;

	/*
	 * if show_description == true, show the description instead of the title if
	 * it exists
	 */
	public static FavoritFilterList newInstance(int userID) {
		Bundle args = new Bundle();

		args.putInt("userID", userID);

		FavoritFilterList list = new FavoritFilterList();
		list.setArguments(args);

		return list;
	}

	@Override
	public void onStart() {
		super.onStart();
		FavoritManager.observable.registerObserver(this);
	}

	@Override
	public void onPause() {
		if (getDialog() != null) {
			getDialog().dismiss();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		if (getActivity().isFinishing()) {
			FavoritManager.observable.unregisterObserver(this);
			if (getDialog() != null) {
				getDialog().dismiss();
			}
			v = null;
		}
		super.onStop();
	}

	private View buildView() {
		FavoritManager.showOnlyMineInFavorites = false;
		final View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_favorite_filter_list, null);
		if ((Util.isTablet(getActivity()) && !Util
				.isOrientationPortrait(getActivity()))) {
			view.findViewById(R.id.filter_seperator)
					.setVisibility(View.VISIBLE);
		}

		TextView newCollection = (TextView) view
				.findViewById(R.id.newCollectionText);
		try {
			newCollection.setText(Util.getApplicationString("global.newgroup",
					getActivity()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		v = view;
		prepareGroupsView(view);
		RadioGroup group = ((RadioGroup) view.findViewById(R.id.radioGroup));
		try {
			RadioButton favoritGroup = ((RadioButton) group
					.findViewById(R.id.radioAllFavorites));
			favoritGroup.setText(Util.getApplicationString(
					"favorites.all_favourites", getActivity())
					+ " ("
					+ FavoritManager.countAllFavoritArticles(getArguments()
							.getInt("userID"), (FavoritList) getActivity())
					+ ")");

			try {
				((TextView) view.findViewById(R.id.favoriteGroupsText))
						.setText(Util.getApplicationString(
								"label.favorite_groups", getActivity()));
			} catch (Exception e) {
				((TextView) view.findViewById(R.id.favoriteGroupsText))
						.setText(Util.getApplicationString(
								"label.favorie_groups", getActivity()));
			}
			((TextView) view.findViewById(R.id.allFavoriteGroupsText))
					.setText(Util.getApplicationString("label.all_groups",
							getActivity()));

			((TextView) view.findViewById(R.id.allUnassignedFavoritesText))
					.setText(Util.getApplicationString(
							"favorites.not_assigned", getActivity()));

			((TextView) view.findViewById(R.id.newCollectionText)).setText(Util
					.getApplicationString("global.newgroup", getActivity()));

			RadioButton myArticleGroup = ((RadioButton) group
					.findViewById(R.id.radioOwnArticles));
			myArticleGroup.setText(Util.getApplicationString(
					"label.my_entries", getActivity())
					+ " ("
					+ ArticleManager.countAllMyArticles(
							getArguments().getInt("userID"),
							(FavoritList) getActivity()) + ")");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (getActivity().getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
				.getBoolean("showOnlyMyArticle", false) == true) {
			((RadioButton) group.findViewById(R.id.radioOwnArticles))
					.setChecked(true);
			FavoritManager.showOnlyMineInFavorites = true;
		} else {
			((RadioButton) group.findViewById(R.id.radioAllFavorites))
					.setChecked(true);
			FavoritManager.showOnlyMineInFavorites = false;
		}
		FavoritManager.showUnassignedInFavorites = false;
		FavoritManager.groupToShowInFavorites = -1;
		((RadioGroup) view.findViewById(R.id.radioGroup))
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						SharedPreferences pref = getActivity()
								.getSharedPreferences("LOGIN",
										Context.MODE_PRIVATE);
						Editor edit = pref.edit();
						FavoritManager.showOnlyLocaleArticle = false;
						FavoritManager.showUnassignedInFavorites = false;
						FavoritManager.groupToShowInFavorites = -1;
						if (checkedId == R.id.radioAllFavorites) {
							FavoritManager.showOnlyMineInFavorites = false;

							edit.putBoolean("showOnlyMyArticle", false);
						} else {
							FavoritManager.showOnlyMineInFavorites = true;
							edit.putBoolean("showOnlyMyArticle", true);
						}
						edit.commit();
						FavoritManager.observable.notifyDatasetChanged();
					}
				});
		TextView allUnassigend = ((TextView) view
				.findViewById(R.id.allUnassignedFavoritesText));

		TextView allLocalArticle = ((TextView) view
				.findViewById(R.id.allLocaleArticle));

		try {

			allLocalArticle.setText(Util.getApplicationString(
					"label.article_in_edit_modus", getActivity())
					+ " ("
					+ FavoritManager
							.countAllLocaleArticles(
									getArguments().getInt("userID"),
									(DatabaseActivity) getActivity()) + ")");

			allUnassigend.setText(Util.getApplicationString(
					"favorites.not_assigned", getActivity())
					+ " ("
					+ FavoritManager
							.countAllUnassignedArticles(
									getArguments().getInt("userID"),
									(DatabaseActivity) getActivity()) + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}

		allLocalArticle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FavoritManager.showOnlyLocaleArticle = true;
				FavoritManager.groupToShowInFavorites = 0;
				FavoritManager.showUnassignedInFavorites = false;
				FavoritManager.showOnlyMineInFavorites = false;
				FavoritList.group = null;
				FavoritManager.observable.notifyDatasetChanged();

			}
		});

		allUnassigend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FavoritManager.groupToShowInFavorites = 0;
				FavoritManager.showOnlyLocaleArticle = false;
				FavoritManager.showUnassignedInFavorites = true;
				FavoritList.group = null;
				FavoritManager.observable.notifyDatasetChanged();
			}
		});
		TextView allFavs = ((TextView) view
				.findViewById(R.id.allFavoriteGroupsText));
		try {
			allFavs.setText(allFavs.getText().toString()
					+ " ("
					+ FavoritManager
							.countAllArticlesWithGroup(
									getArguments().getInt("userID"),
									(DatabaseActivity) getActivity()) + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}
		view.findViewById(R.id.allFavoriteGroupsText).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						FavoritManager.groupToShowInFavorites = 0;
						FavoritManager.showOnlyLocaleArticle = false;
						FavoritManager.showUnassignedInFavorites = false;
						FavoritList.group = null;
						FavoritManager.observable.notifyDatasetChanged();
					}
				});

		((EditText) view.findViewById(R.id.addGroupNameEdit))
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (EditorInfo.IME_ACTION_DONE == actionId) {
							Util.logDebug(logTag, "add group");
							addGroup();
							return true;
						}

						return false;
					}
				});

		view.findViewById(R.id.addGroupButton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						addGroup();
					}
				});

		prepareGroupsView(view);

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (!getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())
				|| (Util.isTablet(getActivity())
						&& Util.isOrientationPortrait(getActivity()) || !Util
							.isTablet(getActivity()))) {

			getActivity().getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					getActivity());

			try {
				dialogBuilder.setTitle(Util.getApplicationString(
						"global.sortfav", getActivity()));

				dialogBuilder.setView(buildView());
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

		}
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getActivity().getClass().getSimpleName()
				.equals(Planning.class.getSimpleName())
				|| (Util.isTablet(getActivity()) && !Util
						.isOrientationPortrait(getActivity()))) {

			return buildView();
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void prepareGroupsView(View view) {
		favoriteGroupsListAdapter = new FavoriteGroupsListAdapter();

		if (!isRemoving()) {
			try {
				AdapterUtils.fillLinearLayoutFromAdapter(1,
						((LinearLayout) view.findViewById(R.id.filterList)),
						favoriteGroupsListAdapter, getActivity(), true, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addGroup() {
		EditText groupNameEdit = (EditText) v
				.findViewById(R.id.addGroupNameEdit);

		String groupName = groupNameEdit.getText().toString();

		groupNameEdit.setText("");

		/*
		 * simple sanity check.
		 */
		if (groupName.equals("")) {
			Util.logDebug(logTag, "empty groupname - not adding");
			return;
		}

		FavoriteGroup favoriteGroup = new FavoriteGroup();
		favoriteGroup.name = groupName;
		favoriteGroup.user_id = getArguments().getInt("userID");
		favoriteGroup.created = new Date();

		try {
			new AddOrRemoveFavoriteGroup(favoriteGroup, false, getActivity())
					.execute(true);
		} catch (Exception e) {
			Util.logDebug(logTag, "adding the group failed");
			e.printStackTrace();
		}
	}

	@Override
	public void dataChanged() {
		Util.logDebug(logTag, "dataChanged");
		// buildView();
		if (!Util.isUserAuthentificated(getActivity())) {

		} else if (getActivity() != null && v != null) {
			prepareGroupsView(v);

			TextView allUnassigend = ((TextView) v
					.findViewById(R.id.allUnassignedFavoritesText));

			TextView allLocalArticle = ((TextView) v
					.findViewById(R.id.allLocaleArticle));

			try {

				allLocalArticle.setText(Util.getApplicationString(
						"label.article_in_edit_modus", getActivity())
						+ " ("
						+ FavoritManager.countAllLocaleArticles(getArguments()
								.getInt("userID"),
								(DatabaseActivity) getActivity()) + ")");

				allUnassigend.setText(Util.getApplicationString(
						"favorites.not_assigned", getActivity())
						+ " ("
						+ FavoritManager.countAllUnassignedArticles(
								getArguments().getInt("userID"),
								(DatabaseActivity) getActivity()) + ")");
			} catch (Exception e) {
				e.printStackTrace();
			}

			TextView allFavs = ((TextView) v
					.findViewById(R.id.allFavoriteGroupsText));
			try {
				allFavs.setText(Util.getApplicationString("label.all_groups",
						getActivity())
						+ " ("
						+ FavoritManager.countAllArticlesWithGroup(
								getArguments().getInt("userID"),
								(DatabaseActivity) getActivity()) + ")");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private class FavoriteGroupsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			try {
				return FavoritManager.getFavoriteGroups(
						getArguments().getInt("userID"),
						((DatabaseActivity) getActivity())).size();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		@Override
		public Object getItem(int arg0) {
			try {
				return FavoritManager.getFavoriteGroups(
						getArguments().getInt("userID"),
						((DatabaseActivity) getActivity())).get(arg0);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertview, ViewGroup arg2) {
			View v = convertview;

			if (null == v) {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.favorite_filter_list_child, null);
			}

			final FavoriteGroup group = (FavoriteGroup) getItem(position);

			Util.logDebug(logTag, "group id: " + group.id + " name: "
					+ group.name);

			((TextView) v.findViewById(R.id.selectionButton))
					.setText(group.name + " (" + group.amountFavGroups + ")");

			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					FavoritList.group = group;
					FavoritManager.showOnlyLocaleArticle = false;
					FavoritManager.showUnassignedInFavorites = false;
					FavoritManager.groupToShowInFavorites = (group.extern_id == 0) ? group.id
							: group.extern_id;
					FavoritManager.observable.notifyDatasetChanged();
				}
			});

			final int pos = position;

			v.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					FavoriteGroup group = (FavoriteGroup) getItem(pos);
					if (null == group) {
						Util.logDebug(logTag, "group is null");
						return false;
					}
					try {

						new AddOrRemoveFavoriteGroup(group, true, getActivity())
								.execute(false);

					} catch (Exception e) {

						Util.logDebug(logTag,
								"something went wrong removing the group");
						e.printStackTrace();
						return false;
					}

					return true;
				}
			});

			return v;
		}
	}

	public class AddOrRemoveFavoriteGroup extends
			AsyncTask<Boolean, ResultValues, Void> {
		private ProgressDialog dialog = null;

		private boolean delete = false;
		private Context con;

		private FavoriteGroup _externGroup;

		public AddOrRemoveFavoriteGroup(FavoriteGroup group, boolean delete,
				Context con) {
			this._externGroup = group;
			this.delete = delete;
			this.con = con;
		}

		@Override
		protected Void doInBackground(Boolean... params) {
			if (params[0]) {
				int groupID = API.getInstance(getActivity()).addFavoriteGroup(
						_externGroup.name, con);

				if (groupID > -1) {
					_externGroup.extern_id = groupID;
					try {
						FavoritManager.addFavoritGroup(_externGroup,
								(DatabaseActivity) con);
						API.getInstance(getActivity()).callFavoritesFromServer(
								(DatabaseActivity) con);
						publishProgress(ResultValues.RESULT_OK);
					} catch (Exception e) {
						publishProgress(ResultValues.RESULT_EXCEPTION);
						e.printStackTrace();
					}

				} else {
					publishProgress(ResultValues.RESULT_NO_INTERNET);
				}
			} else {
				try {

					if (API.getInstance(getActivity()).removeFavoriteGroup(
							_externGroup.extern_id, con)) {
						FavoritManager.removeFavoriteGroup(_externGroup,
								((DatabaseActivity) con));

						publishProgress(ResultValues.RESULT_OK);
					} else {
						publishProgress(ResultValues.RESULT_NO_INTERNET);
					}
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(ResultValues.RESULT_EXCEPTION);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(ResultValues... values) {

			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
			try {
				switch (values[0]) {
				case RESULT_NO_INTERNET:
					if (delete) {
						Toast.makeText(
								getActivity(),
								Util.getApplicationString(
										"label.error_remove_group",
										getActivity()), Toast.LENGTH_LONG)
								.show();
					} else {
						Toast.makeText(
								getActivity(),
								Util.getApplicationString(
										"label.error_add_group", getActivity()),
								Toast.LENGTH_LONG).show();
					}

					break;
				case RESULT_EXCEPTION:
					break;
				case RESULT_OK:
					FavoritManager.observable.notifyDatasetChanged();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			try {
				if (delete) {
					dialog = ProgressDialog.show(con, "", Util
							.getApplicationString("label.message_delete_group",
									con));
				} else {
					dialog = ProgressDialog.show(con, "", Util
							.getApplicationString("label.message_save_group",
									con));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
