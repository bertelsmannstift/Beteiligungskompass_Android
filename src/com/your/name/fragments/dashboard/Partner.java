/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.dashboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.AdapterUtils;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.PartnerLink;

/* 
 * shows the partner images on the dashboard screen.
 */
public class Partner extends Fragment {

	private static String logTag = Partner.class.toString();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_dashboard_partner,
				container, false);

		AdapterUtils.fillLinearLayoutFromAdapter(3,
				(LinearLayout) v.findViewById(R.id.partnerLayout),
				new PartnerAdapter(), getActivity(), true, true);
		try {
			((TextView) v.findViewById(R.id.partner)).setText(Util
					.getApplicationString("global.partner_headline",
							getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	private class PartnerAdapter extends BaseAdapter {

		List<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();

		public PartnerAdapter() {
			try {
				Dao<PartnerLink, Integer> partnerDAO = ((DatabaseActivity) getActivity()).helper
						.getDao(PartnerLink.class);

				partnerLinks = partnerDAO.queryForAll();

				Util.logDebug(logTag,
						"number of partners: " + partnerLinks.size());
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		public int getCount() {
			/*
			 * We make use of the fact that integer arithmetic is useful:
			 * 
			 * We take the number of partnerlinks and add 1. Then divide by two.
			 * And multiply by two again: If the number is even we get the same
			 * number back:
			 * 
			 * ((6 + 1) / 2) * 2 = 6
			 * 
			 * But if it's odd we get:
			 * 
			 * (7 + 1) / 2) * 2 = 8
			 * 
			 * And then we fill the layout with an empty view. So the last
			 * view doesn't span the whole width
			 */
			return ((partnerLinks.size() + 1) / 2) * 2;
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup vg) {

			if (position >= partnerLinks.size()) {
				return new View(getActivity());
			}

			View v = convertView;

			if (v == null) {
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.dashboard_partner_listitem, null);
			}

			String content = partnerLinks.get(position).content.trim();

			Util.logDebug(logTag,
					"partnerlink: title: " + partnerLinks.get(position).title);

			Util.logDebug(logTag,
					"partnerlinl: title: " + partnerLinks.get(position).title);
			content = content.replace("<img ", "<img width=\"100%\" ");

			WebView contentView = ((WebView) v.findViewById(R.id.webView));

			contentView.loadDataWithBaseURL(null, content, "text/html",
					"UTF-8", null);

			return v;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		outState.putString("tab", "Partner");

		super.onSaveInstanceState(outState);

	}

}