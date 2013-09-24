/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.views.Validator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextWatcher implements TextWatcher {
	private EditText text;

	public EditTextWatcher(EditText text) {
		this.text = text;
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			text.setError(null);
		}
	}

}
