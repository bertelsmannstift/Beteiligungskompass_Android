/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments;

import java.util.Calendar;

import com.googlecode.android.widgets.DateSlider.DateSlider.OnDateSetListener;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerDialog extends DialogFragment {

	private static int mdialogID;
	private static OnDateSetListener ml;

	public static DatePickerDialog newInstance(int dialogID, OnDateSetListener l) {
		mdialogID = dialogID;
		ml = l;

		return new DatePickerDialog();
	}

	public DatePickerDialog() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar cal = Calendar.getInstance();
		switch (mdialogID) {
		case 1:
			DefaultDateSlider slider = new DefaultDateSlider(getActivity(), ml,
					cal);

			return slider;
		case 2:
			DateTimeSlider dtSlider = new DateTimeSlider(getActivity(), ml, cal);

			return dtSlider;
		}
		return super.onCreateDialog(savedInstanceState);
	}

}
