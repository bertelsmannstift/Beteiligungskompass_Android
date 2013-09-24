/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.your.name.R;

public class SimpleSpinnerAdapter extends BaseAdapter {

	private Activity _acitivty;
	private String[] _values;

	public SimpleSpinnerAdapter(Activity activity, String[] values) {
		this._acitivty = activity;
		this._values = values;
	}

	@Override
	public int getCount() {
		return _values.length;
	}

	@Override
	public Object getItem(int position) {
		return _values[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			v = _acitivty.getLayoutInflater().inflate(
					R.layout.simple_spinner_item, null);
		}

		((TextView) v.findViewById(R.id.selectionText))
				.setText(_values[position]);

		return v;
	}

}
