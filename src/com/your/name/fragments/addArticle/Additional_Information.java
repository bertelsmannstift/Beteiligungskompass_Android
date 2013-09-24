/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.j256.ormlite.dao.Dao;
import com.your.name.NavigationItem;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.ArticleList;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.NewArticle;
import com.your.name.dao.Article;
import com.your.name.fragments.DatePickerDialog;

/**
 * this fragment will be used to add additional informations to the new article
 * like description, costs etc.
 */
public class Additional_Information extends Fragment {

	private static NavigationItem type = null;
	private Article article;

	private View view;

	public Additional_Information() {

	}

	public Additional_Information(NavigationItem type) {
		Additional_Information.type = type;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			article = (Article) getActivity().getIntent().getSerializableExtra(
					NewArticle.ARTICLE);
			if (article == null) {
				article = NewArticle.article;
			}
			if (type.type.equals(Article.STUDY)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_project,
						container, false);
				((TextView) view.findViewById(R.id.textViewProjectDesc))
						.setText(Util.getApplicationString(
								"label.short_description", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectDesc))
						.setText(article.listdescription_plaintext);

				((TextView) view.findViewById(R.id.textViewProjectBackground))
						.setText(Util.getApplicationString("label.background",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectBackground))
						.setText(article.background);

				((TextView) view.findViewById(R.id.textViewProjectProzess))
						.setText(Util.getApplicationString("label.process",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectProzess))
						.setText(article.process);

				((TextView) view.findViewById(R.id.textViewProjectAim))
						.setText(Util.getApplicationString("label.aim",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectAim))
						.setText(article.aim);

				((TextView) view.findViewById(R.id.textViewProjectResult))
						.setText(Util.getApplicationString("label.results",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectResult))
						.setText(article.results);

				((TextView) view.findViewById(R.id.textViewProjectAddInfo))
						.setText(Util.getApplicationString(
								"label.description_institution", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectAddInfo))
						.setText(article.description_institution);

				((TextView) view.findViewById(R.id.textViewProjectContact))
						.setText(Util.getApplicationString("label.contact",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleProjectContact))
						.setText(article.contact);

			} else if (type.type.equals(Article.EXPERT)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_expert,
						container, false);

				((TextView) view.findViewById(R.id.textViewExpertDesc))
						.setText(Util
								.getApplicationString(
										"label.short_description_expert",
										getActivity()));
				((EditText) view.findViewById(R.id.NewArticleExpertDesc))
						.setText(article.listdescription_plaintext);
				((TextView) view.findViewById(R.id.textViewExpertAddInfo))
						.setText(Util.getApplicationString(
								"label.description_institution", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleExpertAddInfo))
						.setText(article.description_institution);

			} else if (type.type.equals(Article.METHOD)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_method,
						container, false);

				((TextView) view.findViewById(R.id.textViewMethodAim))
						.setText(Util.getApplicationString("label.used_for",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodAim))
						.setText(article.used_for);

				((TextView) view.findViewById(R.id.textViewMethodInfo))
						.setText(Util.getApplicationString(
								"label.participants", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodInfo))
						.setText(article.participants);
				((TextView) view.findViewById(R.id.textViewMethodCost))
						.setText(Util.getApplicationString("label.costs",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodCost))
						.setText(article.costs);

				((TextView) view.findViewById(R.id.textViewMethodTimeExpense))
						.setText(Util.getApplicationString(
								"label.time_expense", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodTimeExpense))
						.setText(article.time_expense);

				((TextView) view.findViewById(R.id.textViewMethodUsefulIf))
						.setText(Util.getApplicationString("label.when_to_use",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodeUsefulIf))
						.setText(article.when_to_use);

				((TextView) view.findViewById(R.id.textViewMethodNotUsefulIf))
						.setText(Util.getApplicationString(
								"label.when_not_to_use", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodNotUsefulIf))
						.setText(article.when_not_to_use);

				((TextView) view.findViewById(R.id.textViewMethodStrength))
						.setText(Util.getApplicationString("label.strengths",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodStrength))
						.setText(article.strengths);

				((TextView) view.findViewById(R.id.textViewMethodWeakness))
						.setText(Util.getApplicationString("label.weaknesses",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodWeakness))
						.setText(article.weaknesses);

				((TextView) view.findViewById(R.id.textViewMethodOrigin))
						.setText(Util.getApplicationString("label.origin",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodOrigin))
						.setText(article.origin);

				((TextView) view.findViewById(R.id.textViewMethodRights))
						.setText(Util.getApplicationString(
								"label.restrictions", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodRights))
						.setText(article.restrictions);

				((TextView) view.findViewById(R.id.textViewMethodContact))
						.setText(Util.getApplicationString("label.contact",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodContact))
						.setText(article.contact);

			} else if (type.type.equals(Article.QA)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_qa, container,
						false);

				((TextView) view.findViewById(R.id.textViewQADescription))
						.setText(Util.getApplicationString(
								"module.qa.short_description", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleQADescription))
						.setText(article.answer);

				((TextView) view.findViewById(R.id.textViewAuthor))
						.setText(Util.getApplicationString(
								"label.author_answer", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleQAAuthor))
						.setText(article.author_answer);

				((TextView) view.findViewById(R.id.textViewPublisher))
						.setText(Util.getApplicationString("label.publisher",
								getActivity()));
				((EditText) view.findViewById(R.id.NewArticleQAPublisher))
						.setText(article.publisher);

				((TextView) view.findViewById(R.id.textViewQAYear))
						.setText(Util.getApplicationString("label.year",
								getActivity()));

				Spinner year = (Spinner) view
						.findViewById(R.id.NewArticleQAYear);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				adapter.setDropDownViewResource(R.layout.simple_spinner_item);
				int selectedPos = 0;
				if (Build.VERSION.SDK_INT >= 11) {
					adapter.addAll(Util.years);
				} else {

					for (String string : Util.years) {
						adapter.add(string);
					}
				}
				int pos = 0;
				for (String string : Util.years) {

					if (article.year != null && article.year > 0) {
						if (string.equals(article.year + "")) {
							selectedPos = pos;
						}
					}
					pos++;
				}
				year.setAdapter(adapter);
				year.setSelection(selectedPos);
				year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						article.year = (arg2 == 0) ? 0 : Integer
								.parseInt(((String) arg0.getSelectedItem()));

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

			} else if (type.type.equals(Article.NEWS)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_news,
						container, false);

				((TextView) view.findViewById(R.id.textViewNewsIntro))
						.setText(Util.getApplicationString("label.intro",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleNewsIntro))
						.setText(article.intro);

				((TextView) view.findViewById(R.id.textViewNewsText))
						.setText(Util.getApplicationString("label.text",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleNewsText))
						.setText(article.text);

			} else if (type.type.equals(Article.EVENT)) {
				view = inflater.inflate(
						R.layout.fragment_newarticle_description_event,
						container, false);

				((TextView) view.findViewById(R.id.textViewEventDescription))
						.setText(Util.getApplicationString(
								"label.description_event", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEVENTDescription))
						.setText(article.listdescription_plaintext);

				((EditText) view.findViewById(R.id.NewArticleEventMaxPart))
						.setText(article.number_of_participants);

				((TextView) view.findViewById(R.id.textViewEventFee))
						.setText(Util.getApplicationString("label.fee",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEventFee))
						.setText(article.fee);

				((TextView) view.findViewById(R.id.textViewEventContactPerson))
						.setText(Util.getApplicationString(
								"label.contact_person", getActivity()));

				((EditText) view
						.findViewById(R.id.NewArticleEventContactPerson))
						.setText(article.contact_person);

				((TextView) view.findViewById(R.id.textViewEventEmail))
						.setText(Util.getApplicationString("label.email",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEventEmail))
						.setText(article.email);

				((TextView) view.findViewById(R.id.textViewEventPhone))
						.setText(Util.getApplicationString("label.phone",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEventPhone))
						.setText(article.phone);

				((TextView) view.findViewById(R.id.textViewEventFax))
						.setText(Util.getApplicationString("label.fax",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEventFax))
						.setText(article.fax);

				((TextView) view.findViewById(R.id.textViewEventLink))
						.setText(Util.getApplicationString("label.link",
								getActivity()));

				((EditText) view.findViewById(R.id.NewArticleEventLink))
						.setText(article.link);

				((TextView) view.findViewById(R.id.textViewEventdeadline))
						.setText(Util.getApplicationString("label.deadline",
								getActivity()));

				Spinner participation = (Spinner) view
						.findViewById(R.id.NewArticleEventParticipation);
				String[] participationArray = new String[] { "" };
				try {
					participationArray = new String[] {
							"",
							Util.getApplicationString("participation.open",
									getActivity()),
							Util.getApplicationString("participation.register",
									getActivity()),
							Util.getApplicationString(
									"participation.invitation", getActivity()) };
				} catch (Exception e) {
					e.printStackTrace();
				}

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				adapter.setDropDownViewResource(R.layout.simple_spinner_item);
				if (Build.VERSION.SDK_INT >= 11) {
					adapter.addAll(participationArray);
				} else {
					for (String string : participationArray) {
						adapter.add(string);
					}
				}
				int pos = 0;
				for (String string : participationArray) {
					if (string.equals(article.participation)) {
						article.participatioN_id = pos;
						break;
					}

					pos++;
				}
				participation.setAdapter(adapter);
				participation.setSelection((article.participatioN_id));
				participation
						.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
								article.participation = (String) arg0
										.getSelectedItem();
								article.participatioN_id = (pos + 1);
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}
						});

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",
						Locale.getDefault());

				final EditText deadline = ((EditText) view
						.findViewById(R.id.NewArticleEventDeadline));
				deadline.setText((article.deadline != null) ? format.format(
						article.deadline).toString() : "");
				deadline.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						FragmentManager manager = getActivity()
								.getSupportFragmentManager();

						Fragment fragment = manager.findFragmentByTag("dialog");
						FragmentTransaction transaction = manager
								.beginTransaction();
						if (fragment != null) {
							transaction.remove(fragment);
						}

						transaction.addToBackStack(null);

						DatePickerDialog dialog = DatePickerDialog.newInstance(
								1, new DateSlider.OnDateSetListener() {

									@Override
									public void onDateSet(DateSlider view,
											Calendar selectedDate) {

										SimpleDateFormat format = new SimpleDateFormat(
												"dd.MM.yyyy", Locale
														.getDefault());
										deadline.setText(format
												.format(selectedDate.getTime()));
									}
								});

						dialog.show(manager, "dialog");
						transaction.commit();

					}
				});
			}

			Button backButton = (Button) view.findViewById(R.id.backButton);
			backButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					NewArticle.article = article;
					Intent intent = new Intent(getActivity(), NewArticle.class);
					intent.putExtra(NewArticle.NEXT_STEP, 1);
					intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
					intent.putExtra(ArticleList.ITEM_TPE, type);
					startActivity(intent);
					getActivity().finish();

				}
			});
		} catch (Exception e) {

			e.printStackTrace();
		}
		try {
			((Button) view.findViewById(R.id.nextStepButton))
					.setText(Util.getApplicationString("label.save_and_next",
							getActivity()));

			((Button) view.findViewById(R.id.backButton)).setText(Util
					.getApplicationString("label.back", getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Button nextStep = (Button) view.findViewById(R.id.nextStepButton);

		nextStep.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (type.type.equals(Article.STUDY)) {
					String description = ((EditText) view
							.findViewById(R.id.NewArticleProjectDesc))
							.getText().toString();
					article.description_plaintext = description;
					article.listdescription_plaintext = description;
					article.listdescription = description;
					article.short_description = description;
					article.description = description;
					article.background = ((EditText) view
							.findViewById(R.id.NewArticleProjectBackground))
							.getText().toString();
					article.aim = ((EditText) view
							.findViewById(R.id.NewArticleProjectAim)).getText()
							.toString();
					article.process = ((EditText) view
							.findViewById(R.id.NewArticleProjectProzess))
							.getText().toString();
					article.results = ((EditText) view
							.findViewById(R.id.NewArticleProjectResult))
							.getText().toString();
					article.more_information = ((EditText) view
							.findViewById(R.id.NewArticleProjectAddInfo))
							.getText().toString();
					article.contact = ((EditText) view
							.findViewById(R.id.NewArticleProjectContact))
							.getText().toString();
				} else if (type.type.equals(Article.METHOD)) {
					article.used_for = ((EditText) view
							.findViewById(R.id.NewArticleMethodAim)).getText()
							.toString();
					article.participants = ((EditText) view
							.findViewById(R.id.NewArticleMethodInfo)).getText()
							.toString();
					article.costs = ((EditText) view
							.findViewById(R.id.NewArticleMethodCost)).getText()
							.toString();
					article.time_expense = ((EditText) view
							.findViewById(R.id.NewArticleMethodTimeExpense))
							.getText().toString();
					article.when_to_use = ((EditText) view
							.findViewById(R.id.NewArticleMethodeUsefulIf))
							.getText().toString();
					article.when_not_to_use = ((EditText) view
							.findViewById(R.id.NewArticleMethodNotUsefulIf))
							.getText().toString();
					article.strengths = ((EditText) view
							.findViewById(R.id.NewArticleMethodStrength))
							.getText().toString();
					article.weaknesses = ((EditText) view
							.findViewById(R.id.NewArticleMethodWeakness))
							.getText().toString();
					article.origin = ((EditText) view
							.findViewById(R.id.NewArticleMethodOrigin))
							.getText().toString();
					article.restrictions = ((EditText) view
							.findViewById(R.id.NewArticleMethodRights))
							.getText().toString();
					article.contact = ((EditText) view
							.findViewById(R.id.NewArticleMethodContact))
							.getText().toString();
				} else if (type.type.equals(Article.QA)) {
					String description = ((EditText) view
							.findViewById(R.id.NewArticleQADescription))
							.getText().toString();
					article.answer = description;
					article.publisher = ((EditText) view
							.findViewById(R.id.NewArticleQAPublisher))
							.getText().toString();
					article.author_answer = ((EditText) view
							.findViewById(R.id.NewArticleQAAuthor)).getText()
							.toString();
				} else if (type.type.equals(Article.NEWS)) {
					article.intro = ((EditText) view
							.findViewById(R.id.NewArticleNewsIntro)).getText()
							.toString();
					article.text = ((EditText) view
							.findViewById(R.id.NewArticleNewsText)).getText()
							.toString();
					article.description = ((EditText) view
							.findViewById(R.id.NewArticleNewsText)).getText()
							.toString();

					article.listdescription = ((EditText) view
							.findViewById(R.id.NewArticleNewsText)).getText()
							.toString();

					article.listdescription_plaintext = ((EditText) view
							.findViewById(R.id.NewArticleNewsText)).getText()
							.toString();

					article.description_plaintext = ((EditText) view
							.findViewById(R.id.NewArticleNewsText)).getText()
							.toString();

				} else if (type.type.equals(Article.EVENT)) {
					article.number_of_participants = ((EditText) view
							.findViewById(R.id.NewArticleEventMaxPart))
							.getText().toString();
					String desc = ((EditText) view
							.findViewById(R.id.NewArticleEVENTDescription))
							.getText().toString();
					article.listdescription = desc;
					article.listdescription_plaintext = desc;
					article.description_plaintext = desc;
					article.description = desc;
					article.short_description = desc;

					article.fee = ((EditText) view
							.findViewById(R.id.NewArticleEventFee)).getText()
							.toString();
					article.contact_person = ((EditText) view
							.findViewById(R.id.NewArticleEventContactPerson))
							.getText().toString();
					article.email = ((EditText) view
							.findViewById(R.id.NewArticleEventEmail)).getText()
							.toString();
					article.phone = ((EditText) view
							.findViewById(R.id.NewArticleEventPhone)).getText()
							.toString();
					article.fax = ((EditText) view
							.findViewById(R.id.NewArticleEventFax)).getText()
							.toString();
					article.link = ((EditText) view
							.findViewById(R.id.NewArticleEventLink)).getText()
							.toString();

					if (!URLUtil.isValidUrl(article.link)
							&& article.link.length() > 0) {
						((EditText) view.findViewById(R.id.NewArticleEventLink))
								.setError("Bitte eine gültige Url eintragen.");
						return;
					}

					SimpleDateFormat format = new SimpleDateFormat(
							"dd.MM.yyyy", Locale.getDefault());

					try {
						article.deadline = format.parse(((EditText) view
								.findViewById(R.id.NewArticleEventDeadline))
								.getText().toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (type.type.equals(Article.EXPERT)) {
					String desc = ((EditText) view
							.findViewById(R.id.NewArticleExpertDesc)).getText()
							.toString();

					article.short_description_expert = desc;
					article.description_plaintext = desc;
					article.listdescription_plaintext = desc;
					article.listdescription = desc;
					article.description_institution = ((EditText) view
							.findViewById(R.id.NewArticleExpertAddInfo))
							.getText().toString();
				}

				try {
					Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
							.getDao(Article.class);

					dao.createOrUpdate(article);
					NewArticle.article = article;

				} catch (SQLException e) {
					e.printStackTrace();
				}

				Intent intent = new Intent(getActivity(), NewArticle.class);
				intent.putExtra(NewArticle.NEXT_STEP, 3);
				intent.putExtra(NewArticle.ARTICLE, NewArticle.article);
				intent.putExtra(ArticleList.ITEM_TPE, type);
				startActivity(intent);
			}
		});

		return view;

	}
}
