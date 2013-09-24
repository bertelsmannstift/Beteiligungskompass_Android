/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;

/*
 * this fragment shows the venue from the event article 
 */
public class Event_Venue extends Fragment {

	Article.PaneConfig config = null;
	Article article = null;

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Event_Venue newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Event_Venue text = new Event_Venue();
		text.setArguments(bundle);

		return text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_venue, container,
				false);

		Bundle args = getArguments();

		config = (PaneConfig) args.getSerializable(CONFIG_KEY);
		article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

			((TextView) v.findViewById(R.id.title)).setText(Util
					.getApplicationString("label.venue", getActivity()));

			((TextView) v.findViewById(R.id.eventVenue)).setText(article.venue);

			String address = "";

			if (article.street != null && article.street.length() > 0) {
				address += article.street;
			}

			if (article.street_nr != null && article.street_nr.length() > 0) {
				address += " " + article.street_nr + "\n";
			}

			if (article.zip != null && article.zip.length() > 0) {
				address += article.zip;
			}

			if (article.city != null && article.city.length() > 0) {
				address += " " + article.city;
			}

			((TextView) v.findViewById(R.id.eventAddress)).setText(address
					.trim());

			((TextView) v.findViewById(R.id.googlemaps)).setText(Util
					.getApplicationString("label.show_venue_in_maps",
							getActivity()));

			((TextView) v.findViewById(R.id.addtocalendar)).setText(Util
					.getApplicationString("label.add_event_to_cal",
							getActivity()));

			((TextView) v.findViewById(R.id.addtocalendar))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							ContentValues values = new ContentValues();
							values.put("dtstart", article.start_date.getTime());
							values.put("dtend", article.end_date.getTime());
							values.put("title", article.title);
							values.put("eventLocation", article.venue);
							values.put("description",
									article.listdescription_plaintext);
							values.put("eventTimezone", TimeZone.getDefault()
									.getDisplayName());
							values.put("calendar_id", 1);

							try {
								Toast.makeText(
										getActivity(),
										Util.getApplicationString(
												"label.event_saved_message",
												getActivity()),
										Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					});

			((TextView) v.findViewById(R.id.googlemaps))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(
									android.content.Intent.ACTION_VIEW, Uri
											.parse("geo:0,0?q="
													+ article.street + " "
													+ article.street_nr + " "
													+ article.zip + " "
													+ article.city));
							startActivity(intent);
						}
					});

			if (article.number_of_participants != null
					&& article.number_of_participants.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewMaxMember))
						.setText(Util.getApplicationString(
								"label.number_of_participants", getActivity())
								+ ": ");
				((TextView) v.findViewById(R.id.eventMaxMember))
						.setText(article.number_of_participants);
			} else {
				v.findViewById(R.id.layoutMaxMember).setVisibility(View.GONE);
			}

			if (article.fee != null && article.fee.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewTeilnahmeGebuehr))
						.setText(Util.getApplicationString("label.fee",
								getActivity()) + ": ");
				((TextView) v.findViewById(R.id.eventTeilnahemGebuehr))
						.setText(article.fee);
			} else {
				v.findViewById(R.id.layoutTeilnahmegebuehr).setVisibility(
						View.GONE);
			}

			if (article.link != null && article.link.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewLink)).setText(Util
						.getApplicationString("label.link", getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventLink))
						.setText(article.link);
			} else {
				v.findViewById(R.id.layoutLink).setVisibility(View.GONE);
			}

			if (article.deadline != null) {
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",
						Locale.getDefault());
				((TextView) v.findViewById(R.id.TextViewDeadline)).setText(Util
						.getApplicationString("label.deadline", getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventDeadline)).setText(format
						.format(article.deadline));
			} else {
				v.findViewById(R.id.layoutDeadline).setVisibility(View.GONE);
			}

			if (article.contact_person != null
					&& article.contact_person.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewPartner)).setText(Util
						.getApplicationString("label.contact_person",
								getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventPartner))
						.setText(article.contact_person);
			} else {
				v.findViewById(R.id.layoutPartner).setVisibility(View.GONE);
			}

			if (article.email != null && article.email.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewEmail)).setText(Util
						.getApplicationString("label.email", getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventEmail))
						.setText(article.email);
			} else {
				v.findViewById(R.id.layoutEmail).setVisibility(View.GONE);
			}

			if (article.phone != null && article.phone.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewTelefon)).setText(Util
						.getApplicationString("label.phone", getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventTelefon))
						.setText(article.phone);
			} else {
				v.findViewById(R.id.layoutTelefon).setVisibility(View.GONE);
			}

			if (article.fax != null && article.fax.length() > 0) {
				((TextView) v.findViewById(R.id.TextViewFax)).setText(Util
						.getApplicationString("label.fax", getActivity())
						+ ": ");
				((TextView) v.findViewById(R.id.eventFax)).setText(article.fax);
			} else {
				v.findViewById(R.id.layoutFax).setVisibility(View.GONE);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return v;
	}
}
