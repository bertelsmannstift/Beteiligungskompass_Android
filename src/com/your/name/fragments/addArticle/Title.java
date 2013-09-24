/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.addArticle;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.your.name.activities.RemoveListener;
import com.your.name.dao.Article;
import com.your.name.dao.User;
import com.your.name.fragments.DatePickerDialog;
import com.your.name.views.Validator.EditTextWatcher;

/*
 * Title defines the first step in the "add a new articles" process. It
 * considers all different article types.
 */
public class Title extends Fragment {

	private static NavigationItem type = null;

	private EditText title, town, newsAuthor = null;

	private TextView newsDate, startDate, endDate;

	private Button nextStep, backButton = null;

	private Article article;

	public Title() {

	}

	public Title(NavigationItem type) {
		Title.type = type;
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
			article.locale = true;
			article.active = false;
			article.videos = null;
			article.type = type.type;
			article.user = new User();
			article.user.id = getActivity().getSharedPreferences("LOGIN",
					Context.MODE_PRIVATE).getInt("userID", -1);

			if (type.type.equals(Article.STUDY)) {
				final View v = inflater.inflate(
						R.layout.fragment_newarticle_title_project, container,
						false);
				((Button) v.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) v.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));
				// project Title

				title = (EditText) v.findViewById(R.id.NewArticleTitle);
				title.setText(article.title);
				((TextView) v.findViewById(R.id.textViewTitle)).setText(Util
						.getApplicationString("label.title", getActivity()));
				title.addTextChangedListener(new EditTextWatcher(title));
				// project Town
				town = (EditText) v.findViewById(R.id.NewArticleCity);
				town.setText(article.city);
				((TextView) v.findViewById(R.id.textViewCity)).setText(Util
						.getApplicationString("label.city", getActivity()));
				Spinner startMonth = (Spinner) v
						.findViewById(R.id.spinnerMonth);

				((TextView) v.findViewById(R.id.textViewStart)).setText(Util
						.getApplicationString("label.start_month",
								getActivity()));
				((CheckBox) v.findViewById(R.id.allTime)).setText(Util
						.getApplicationString("global.ongoing", getActivity()));

				((TextView) v.findViewById(R.id.textViewEnd))
						.setText(Util.getApplicationString("label.end_month",
								getActivity()));

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				adapter.setDropDownViewResource(R.layout.simple_spinner_item);
				if (Build.VERSION.SDK_INT >= 11) {
					adapter.addAll(Util.getMonths(getActivity()));
				} else {
					for (String month : Util.getMonths(getActivity())) {
						adapter.add(month);
					}
				}
				startMonth.setSelection((article.start_month == 0) ? 0
						: (article.start_month - 1));
				startMonth
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int position, long arg3) {
								article.start_month = (position + 1);

							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}
						});
				startMonth.setAdapter(adapter);
				if (article.start_month == 0) {
					startMonth.setSelection(0);
				} else {
					startMonth.setSelection((article.start_month - 1));
				}
				ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				adapterYear
						.setDropDownViewResource(R.layout.simple_spinner_item);

				if (Build.VERSION.SDK_INT >= 11) {
					adapterYear.addAll(Util.years);
				} else {
					for (String year : Util.years) {
						adapterYear.add(year);
					}
				}

				Spinner startYear = (Spinner) v.findViewById(R.id.spinnerYear);

				if (article.start_year > 0) {
					int pos = 0;
					for (String year : Util.years) {
						if (year.equals(article.start_year + "")) {
							startYear.setSelection(pos);
							break;
						}
						pos++;
					}
				}

				startYear.setAdapter(adapterYear);

				if (article.start_year == 0) {
					startYear.setSelection(0);
				} else {
					for (int i = 0; i < Util.years.length; i++) {
						if (Util.years[i].equals(article.start_year + "")) {
							startYear.setSelection(i);
							break;
						}
					}
				}

				startYear
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int position, long arg3) {
								article.start_year = (position == 0) ? 0
										: Integer.parseInt((String) arg0
												.getSelectedItem());

							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}
						});
				CheckBox allTime = (CheckBox) v.findViewById(R.id.allTime);

				allTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							article.end_year = 0;
							article.end_month = 0;
							v.findViewById(R.id.textViewEnd).setVisibility(
									View.GONE);
							v.findViewById(R.id.end).setVisibility(View.GONE);
						} else {
							v.findViewById(R.id.end)
									.setVisibility(View.VISIBLE);
							v.findViewById(R.id.textViewEnd).setVisibility(
									View.VISIBLE);
						}

					}
				});

				if (article.end_year == 0) {
					if (article.end_month == 0) {
						allTime.setChecked(true);
					}
				}

				Spinner endMonth = (Spinner) v
						.findViewById(R.id.spinnerEndMonth);
				endMonth.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						article.end_month = (position + 1);

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
				ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				endAdapter
						.setDropDownViewResource(R.layout.simple_spinner_item);
				if (Build.VERSION.SDK_INT >= 11) {
					endAdapter.addAll(Util.getMonths(getActivity()));
				} else {
					for (String year : Util.getMonths(getActivity())) {
						endAdapter.add(year);
					}
				}

				endMonth.setAdapter(endAdapter);
				if (article.end_month == 0) {
					endMonth.setSelection(0);
				} else {
					endMonth.setSelection((article.end_month - 1));
				}
				ArrayAdapter<String> endYearAdapter = new ArrayAdapter<String>(
						getActivity(), R.layout.simple_spinner_layout);
				endYearAdapter
						.setDropDownViewResource(R.layout.simple_spinner_item);
				if (Build.VERSION.SDK_INT >= 11) {
					endYearAdapter.addAll(Util.years);
				} else {
					for (String year : Util.years) {
						endYearAdapter.add(year);
					}
				}

				Spinner endYear = (Spinner) v.findViewById(R.id.spinnerEndYear);
				endYear.setAdapter(endYearAdapter);
				if (article.end_year == 0) {
					endYear.setSelection(0);
				} else {
					for (int i = 0; i < Util.years.length; i++) {
						if (Util.years[i].equals(article.end_year + "")) {
							endYear.setSelection(i);
							break;
						}
					}
				}
				if (article.end_year == 0) {
					startYear.setSelection(0);
				} else {
					for (int i = 0; i < Util.years.length; i++) {
						if (Util.years[i].equals(article.end_year + "")) {
							startYear.setSelection(i);
							break;
						}
					}
				}

				endYear.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						article.end_year = (position == 0) ? 0 : Integer
								.parseInt((String) arg0.getSelectedItem());

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

				backButton = (Button) v.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));

				nextStep = (Button) v.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (title.getText().toString().length() == 0) {
							try {
								title.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							article.title = title.getText().toString();
							if (type.type.equals(Article.STUDY)) {
								article.city = town.getText().toString();
							}

							try {
								Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
										.getDao(Article.class);

								dao.createOrUpdate(article);

								article = dao.queryForMatching(article).get(0);

							} catch (SQLException e) {
								e.printStackTrace();
							}

							Intent intent = new Intent(getActivity(),
									NewArticle.class);
							intent.putExtra(NewArticle.NEXT_STEP, 2);
							intent.putExtra(NewArticle.ARTICLE, article);
							intent.putExtra(ArticleList.ITEM_TPE, type);
							startActivity(intent);
						}

					}
				});

				return v;

			} else if (type.type.equals(Article.METHOD)) {
				final View view = inflater.inflate(
						R.layout.fragment_newarticle_title_method, container,
						false);
				((Button) view.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) view.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));
				// Method title
				title = (EditText) view
						.findViewById(R.id.NewArticleMethodTitle);
				title.setText(article.title);

				((EditText) view.findViewById(R.id.NewArticleMethodEckpunkte))
						.setText(article.description_plaintext);

				((TextView) view.findViewById(R.id.textViewTitle)).setText(Util
						.getApplicationString("label.title", getActivity()));
				title.addTextChangedListener(new EditTextWatcher(title));

				((TextView) view.findViewById(R.id.textViewDescription))
						.setText(Util.getApplicationString(
								"label.short_description", getActivity()));

				((EditText) view.findViewById(R.id.NewArticleMethodDescription))
						.setText(article.short_description);

				((TextView) view.findViewById(R.id.textViewEckpunte))
						.setText(Util.getApplicationString("label.description",
								getActivity()));

				backButton = (Button) view.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));

				// go to next step
				nextStep = (Button) view.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (title.getText().toString().length() == 0) {
							try {
								title.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							article.title = title.getText().toString();

							String description = ((EditText) view
									.findViewById(R.id.NewArticleMethodDescription))
									.getText().toString();

							article.short_description = description;
							article.description_plaintext = ((EditText) view
									.findViewById(R.id.NewArticleMethodEckpunkte))
									.getText().toString();
							article.description = ((EditText) view
									.findViewById(R.id.NewArticleMethodEckpunkte))
									.getText().toString();

							try {
								Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
										.getDao(Article.class);

								dao.createOrUpdate(article);

								article = dao.queryForMatching(article).get(0);

							} catch (SQLException e) {
								e.printStackTrace();
							}

							Intent intent = new Intent(getActivity(),
									NewArticle.class);
							intent.putExtra(NewArticle.NEXT_STEP, 2);
							intent.putExtra(NewArticle.ARTICLE, article);
							intent.putExtra(ArticleList.ITEM_TPE, type);
							startActivity(intent);
						}

					}
				});

				return view;
			} else if (type.type.equals(Article.QA)) {

				final View view = inflater
						.inflate(R.layout.fragment_newarticle_title_qa,
								container, false);
				((Button) view.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) view.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));
				// QA title
				title = (EditText) view.findViewById(R.id.NewArticleQATitle);
				title.setText(article.title);
				((TextView) view.findViewById(R.id.textViewTitle)).setText(Util
						.getApplicationString("label.title", getActivity()));
				title.addTextChangedListener(new EditTextWatcher(title));

				((EditText) view.findViewById(R.id.NewArticleQAQuestion))
						.setText(article.question);

				((TextView) view.findViewById(R.id.textViewQuestion))
						.setText(Util.getApplicationString("label.question",
								getActivity()));

				backButton = (Button) view.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));
				// go to next step
				nextStep = (Button) view.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (title.getText().toString().length() == 0) {
							try {
								title.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							article.title = title.getText().toString();

							String question = ((EditText) view
									.findViewById(R.id.NewArticleQAQuestion))
									.getText().toString();

							article.question = question;
							article.description = question;
							article.description_plaintext = question;
							article.listdescription = question;
							article.listdescription_plaintext = question;
							try {
								Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
										.getDao(Article.class);

								dao.createOrUpdate(article);

								article = dao.queryForMatching(article).get(0);

							} catch (SQLException e) {

								e.printStackTrace();
							}

							Intent intent = new Intent(getActivity(),
									NewArticle.class);
							intent.putExtra(NewArticle.NEXT_STEP, 2);
							intent.putExtra(NewArticle.ARTICLE, article);
							intent.putExtra(ArticleList.ITEM_TPE, type);
							startActivity(intent);
						}

					}
				});

				return view;
			} else if (type.type.equals(Article.NEWS)) {
				final View view = inflater.inflate(
						R.layout.fragment_newarticle_title_news, container,
						false);
				((Button) view.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) view.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));
				// NEWS title
				title = (EditText) view.findViewById(R.id.NewArticleNewsTitle);
				((TextView) view.findViewById(R.id.textViewTitle)).setText(Util
						.getApplicationString("label.title", getActivity()));
				title.addTextChangedListener(new EditTextWatcher(title));
				title.setText(article.title);
				// NEWS Author
				newsAuthor = (EditText) view
						.findViewById(R.id.NewArticleNewsAuthor);

				newsAuthor.setText(article.author);

				((TextView) view.findViewById(R.id.textViewAutor)).setText(Util
						.getApplicationString("label.author_answer",
								getActivity()));

				((TextView) view.findViewById(R.id.textViewSubtitle))
						.setText(Util.getApplicationString("label.subtitle",
								getActivity()));

				((TextView) view.findViewById(R.id.textViewDate)).setText(Util
						.getApplicationString("label.date", getActivity()));

				newsAuthor.addTextChangedListener(new EditTextWatcher(
						newsAuthor));
				// NEWS Date
				newsDate = (TextView) view
						.findViewById(R.id.NewArticleNewsDate);

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",
						Locale.getDefault());

				newsDate.setText((article.date == null) ? "" : format
						.format(article.date));
				newsDate.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						final TextView edit = (TextView) v;
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
										edit.setText(format.format(selectedDate
												.getTime()));
									}
								});

						dialog.show(manager, "dialog");
						transaction.commit();
					}
				});

				backButton = (Button) view.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));

				// go to next step
				nextStep = (Button) view.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						boolean errorFound = false;
						String newsTitle = title.getText().toString();
						if (newsTitle.length() == 0) {
							try {
								title.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						}
						String author = newsAuthor.getText().toString();
						if (author.length() == 0) {
							try {
								newsAuthor.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						}
						String date = newsDate.getText().toString();
						if (date.length() == 0) {
							try {
								newsDate.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						}

						if (errorFound) {
							return;
						}
						SimpleDateFormat format = new SimpleDateFormat(
								"dd.MM.yyyy", Locale.getDefault());
						article.title = newsTitle;
						article.author = author;
						try {
							article.date = format.parse(date);
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						try {
							Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
									.getDao(Article.class);

							dao.createOrUpdate(article);

							article = dao.queryForMatching(article).get(0);

						} catch (SQLException e) {
							e.printStackTrace();
						}

						Intent intent = new Intent(getActivity(),
								NewArticle.class);
						intent.putExtra(NewArticle.NEXT_STEP, 2);
						intent.putExtra(NewArticle.ARTICLE, article);
						intent.putExtra(ArticleList.ITEM_TPE, type);
						startActivity(intent);

					}
				});

				return view;

			} else if (type.type.equals(Article.EVENT)) {
				final View view = inflater.inflate(
						R.layout.fragment_newarticle_title_event, container,
						false);
				((Button) view.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) view.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));
				// NEWS title
				title = (EditText) view.findViewById(R.id.NewArticleEventTitle);
				title.setText(article.title);
				((TextView) view.findViewById(R.id.textViewTitle)).setText(Util
						.getApplicationString("label.title", getActivity()));
				((TextView) view.findViewById(R.id.textViewStartDate))
						.setText(Util.getApplicationString("label.start_date",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewEnddate))
						.setText(Util.getApplicationString("label.end_date",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewCity)).setText(Util
						.getApplicationString("label.city", getActivity()));
				((TextView) view.findViewById(R.id.textViewPlz)).setText(Util
						.getApplicationString("label.zip", getActivity()));
				((TextView) view.findViewById(R.id.textViewStreet))
						.setText(Util.getApplicationString("label.street",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewEventLocation))
						.setText(Util.getApplicationString("label.venue",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewAuthor))
						.setText(Util.getApplicationString(
								"label.organized_by", getActivity()));

				OnClickListener listener = new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						final TextView edit = (TextView) v;
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
								2, new DateSlider.OnDateSetListener() {

									@Override
									public void onDateSet(DateSlider view,
											Calendar selectedDate) {

										SimpleDateFormat format = new SimpleDateFormat(
												"dd.MM.yyyy HH:mm", Locale
														.getDefault());
										edit.setText(format.format(selectedDate
												.getTime()));
									}
								});

						dialog.show(manager, "dialog");
						transaction.commit();
						// DateSlider slider = new DateSlider(getActivity(),
						// layoutID, l, initialTime);
					}
				};

				// Event Date
				startDate = (TextView) view.findViewById(R.id.eventStartDate);

				SimpleDateFormat format = new SimpleDateFormat(
						"dd.MM.yyyy HH:mm", Locale.getDefault());

				startDate.setText((article.start_date == null) ? "" : format
						.format(article.start_date));
				startDate.setOnClickListener(listener);

				endDate = (TextView) view
						.findViewById(R.id.eventEndDateDisplay);
				endDate.setOnClickListener(listener);
				endDate.setText((article.end_date == null) ? "" : format
						.format(article.end_date));

				title.addTextChangedListener(new EditTextWatcher(title));

				((EditText) view.findViewById(R.id.NewArticleEventStreet))
						.setText(article.street);

				((EditText) view.findViewById(R.id.NewArticleEventNr))
						.setText(article.street_nr);

				((EditText) view.findViewById(R.id.NewArticleEventCity))
						.setText(article.city);

				((EditText) view.findViewById(R.id.NewArticleEventPLZ))
						.setText(article.zip);

				((EditText) view.findViewById(R.id.NewArticleEventLocation))
						.setText(article.venue);

				((EditText) view.findViewById(R.id.NewArticleEventAuthor))
						.setText(article.organized_by);

				backButton = (Button) view.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));

				// go to next step
				nextStep = (Button) view.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						boolean errorFound = false;
						String newsTitle = title.getText().toString();
						if (newsTitle.length() == 0) {
							try {
								title.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						}
						String sDate = startDate.getText().toString();
						if (sDate.length() == 0) {
							try {
								startDate.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						} else {
							startDate.setError(null);
						}
						String eDate = endDate.getText().toString();
						if (eDate.length() == 0) {
							try {
								endDate.setError(Util.getApplicationString(
										"label.error_message_empty_field",
										getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							errorFound = true;
						} else {
							endDate.setError(null);
						}
						SimpleDateFormat format = new SimpleDateFormat(
								"dd.MM.yyyy HH:mm", Locale.getDefault());
						try {
							if (format.parse(sDate).getTime() > format.parse(
									eDate).getTime()) {
								startDate.setError(Util
										.getApplicationString(
												"label.error_message_startdate_gt_enddate",
												getActivity()));
								errorFound = true;
							} else {
								startDate.setError(null);
							}

							article.title = newsTitle;

							article.start_date = format.parse(sDate);
							article.end_date = format.parse(eDate);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (errorFound) {
							return;
						}
						String street = ((EditText) view
								.findViewById(R.id.NewArticleEventStreet))
								.getText().toString();
						article.street = street;
						String hnr = ((EditText) view
								.findViewById(R.id.NewArticleEventNr))
								.getText().toString();

						article.street_nr = hnr;

						String city = ((EditText) view
								.findViewById(R.id.NewArticleEventCity))
								.getText().toString();
						article.city = city;

						String plz = ((EditText) view
								.findViewById(R.id.NewArticleEventPLZ))
								.getText().toString();
						article.zip = plz;

						String venue = ((EditText) view
								.findViewById(R.id.NewArticleEventLocation))
								.getText().toString();
						article.venue = venue;

						String organized_by = ((EditText) view
								.findViewById(R.id.NewArticleEventAuthor))
								.getText().toString();
						article.organized_by = organized_by;

						try {
							Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
									.getDao(Article.class);

							dao.createOrUpdate(article);

							article = dao.queryForMatching(article).get(0);

						} catch (SQLException e) {
							e.printStackTrace();
						}

						Intent intent = new Intent(getActivity(),
								NewArticle.class);
						intent.putExtra(NewArticle.NEXT_STEP, 2);
						intent.putExtra(NewArticle.ARTICLE, article);
						intent.putExtra(ArticleList.ITEM_TPE, type);
						startActivity(intent);

					}
				});

				return view;
			} else if (type.type.equals(Article.EXPERT)) {
				final View view = inflater.inflate(
						R.layout.fragment_newarticle_title_expert, container,
						false);

				((Button) view.findViewById(R.id.nextStepButton)).setText(Util
						.getApplicationString("label.save_and_next",
								getActivity()));

				((Button) view.findViewById(R.id.backButton)).setText(Util
						.getApplicationString("label.back", getActivity()));

				((TextView) view.findViewById(R.id.textViewExpertInstitution))
						.setText(Util.getApplicationString("label.institution",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertFirstname))
						.setText(Util.getApplicationString("label.firstname",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertLastname))
						.setText(Util.getApplicationString("label.lastname",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertaddress))
						.setText(Util.getApplicationString("label.address",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertPLZ))
						.setText(Util.getApplicationString("label.zip",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertCity))
						.setText(Util.getApplicationString("label.city",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertEmail))
						.setText(Util.getApplicationString("label.email",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertPhone))
						.setText(Util.getApplicationString("label.phone",
								getActivity()));
				((TextView) view.findViewById(R.id.textViewExpertFax))
						.setText(Util.getApplicationString("label.fax",
								getActivity()));
				EditText insitution = ((EditText) view
						.findViewById(R.id.NewArticleExpertInstitution));
				insitution.addTextChangedListener(new EditTextWatcher(
						insitution));
				insitution.setText(article.institution);

				EditText first = ((EditText) view
						.findViewById(R.id.NewArticleExpertfirstname));
				first.setText(article.firstname);
				first.addTextChangedListener(new EditTextWatcher(first));

				EditText last = ((EditText) view
						.findViewById(R.id.NewArticleExpertLastname));

				last.setText(article.lastname);

				last.addTextChangedListener(new EditTextWatcher(last));

				((EditText) view.findViewById(R.id.NewArticleExpertaddress))
						.setText(article.address);

				((EditText) view.findViewById(R.id.NewArticleExpertPLZ))
						.setText(article.zip);

				((EditText) view.findViewById(R.id.NewArticleExpertCity))
						.setText(article.city);

				((EditText) view.findViewById(R.id.NewArticleExpertEmail))
						.setText(article.email);

				((EditText) view.findViewById(R.id.NewArticleExpertPhone))
						.setText(article.phone);

				((EditText) view.findViewById(R.id.NewArticleExpertFax))
						.setText(article.fax);

				backButton = (Button) view.findViewById(R.id.backButton);
				backButton.setOnClickListener(new RemoveListener(getActivity(),
						article, type));

				// go to next step
				nextStep = (Button) view.findViewById(R.id.nextStepButton);

				nextStep.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						String expertInstitution = ((EditText) view
								.findViewById(R.id.NewArticleExpertInstitution))
								.getText().toString();
						String expertFirstname = ((EditText) view
								.findViewById(R.id.NewArticleExpertfirstname))
								.getText().toString();
						String expertLastname = ((EditText) view
								.findViewById(R.id.NewArticleExpertLastname))
								.getText().toString();
						if (expertInstitution.length() == 0
								&& expertFirstname.length() == 0
								&& expertLastname.length() == 0) {

							try {
								((EditText) view
										.findViewById(R.id.NewArticleExpertInstitution)).setError(Util
										.getApplicationString(
												"label.error_message_empty_field",
												getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}

							try {
								((EditText) view
										.findViewById(R.id.NewArticleExpertfirstname)).setError(Util
										.getApplicationString(
												"label.error_message_empty_field",
												getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}

							try {
								((EditText) view
										.findViewById(R.id.NewArticleExpertLastname)).setError(Util
										.getApplicationString(
												"label.error_message_empty_field",
												getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}

							return;
						}

						if (expertInstitution.length() == 0
								&& (expertFirstname.length() == 0 || expertLastname
										.length() == 0)) {

							try {
								((EditText) view
										.findViewById(R.id.NewArticleExpertInstitution)).setError(Util
										.getApplicationString(
												"label.error_message_empty_field",
												getActivity()));
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (expertFirstname.length() == 0) {
								try {
									((EditText) view
											.findViewById(R.id.NewArticleExpertfirstname)).setError(Util
											.getApplicationString(
													"label.error_message_empty_field",
													getActivity()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							if (expertLastname.length() == 0) {
								try {
									((EditText) view
											.findViewById(R.id.NewArticleExpertLastname)).setError(Util
											.getApplicationString(
													"label.error_message_empty_field",
													getActivity()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							return;
						}

						article.institution = expertInstitution;
						article.firstname = expertFirstname;
						article.lastname = expertLastname;
						article.title = expertLastname;
						article.address = ((EditText) view
								.findViewById(R.id.NewArticleExpertaddress))
								.getText().toString();
						article.zip = ((EditText) view
								.findViewById(R.id.NewArticleExpertPLZ))
								.getText().toString();

						article.city = ((EditText) view
								.findViewById(R.id.NewArticleExpertCity))
								.getText().toString();

						article.email = ((EditText) view
								.findViewById(R.id.NewArticleExpertEmail))
								.getText().toString();

						article.phone = ((EditText) view
								.findViewById(R.id.NewArticleExpertPhone))
								.getText().toString();

						article.fax = ((EditText) view
								.findViewById(R.id.NewArticleExpertFax))
								.getText().toString();

						try {
							Dao<Article, Integer> dao = ((DatabaseActivity) getActivity()).helper
									.getDao(Article.class);

							dao.createOrUpdate(article);

							article = dao.queryForMatching(article).get(0);

						} catch (SQLException e) {
							e.printStackTrace();
						}

						Intent intent = new Intent(getActivity(),
								NewArticle.class);
						intent.putExtra(NewArticle.NEXT_STEP, 2);
						intent.putExtra(NewArticle.ARTICLE, article);
						intent.putExtra(ArticleList.ITEM_TPE, type);
						startActivity(intent);

					}
				});

				return view;
			} else {
				final View v = super.onCreateView(inflater, container,
						savedInstanceState);

				return v;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return super.onCreateView(inflater, container, savedInstanceState);
		}

	}
}
