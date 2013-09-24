/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.dao.helper.FavoritManager;
import com.your.name.service.API;

public class Login extends NavigationActivity {

	private EditText emailText, pwdText = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.setContentView(R.layout.activity_login);
		try {
			((TextView) this.findViewById(R.id.textViewEmailText)).setText(Util
					.getApplicationString("label.email_text", this));
			emailText = (EditText) this.findViewById(R.id.edittextEmail);
			emailText.setHint(Util.getApplicationString("label.insert_email",
					this));
			pwdText = (EditText) this.findViewById(R.id.edittextPwd);
			pwdText.setHint(Util.getApplicationString("label.enter_password",
					this));

			((Button) this.findViewById(R.id.loginCancelButton)).setText(Util
					.getApplicationString("label.cancel", this));

			((Button) this.findViewById(R.id.loginAddButton)).setText(Util
					.getApplicationString("label.login", this));

			((TextView) this.findViewById(R.id.textViewPWDText)).setText(Util
					.getApplicationString("label.enter_password", this));
		} catch (Exception e) {

		}
	}

	public void onCancelClicked(View v) {
		this.finish();
	}

	public void onLoginClicked(View v) {

		boolean emailNotValid = false;
		boolean pwdNotValid = false;

		String emailValue = emailText.getText().toString();
		String pwdValue = pwdText.getText().toString();

		if (emailValue.equals("")) {
			emailText.setError("Dieses Feld muss ausgefüllt sein");
			emailNotValid = true;
		}

		if (pwdValue.equals("")) {
			pwdText.setError("Dieses Feld muss ausgefüllt sein");
			pwdNotValid = true;
		}

		if (!emailNotValid && !pwdNotValid) {
			new LoginTask().execute();
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog dialog = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean logIn = true;
			try {
				if (!API.getInstance(Login.this).authUser(
						emailText.getText().toString(),
						pwdText.getText().toString())) {
					logIn = false;
					publishProgress();
				} else {
					API.getInstance(Login.this).callFavoritesFromServer(
							Login.this);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return logIn;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isCancelled()) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}

			if (result == true) {
				InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				manager.hideSoftInputFromWindow(pwdText.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				FavoritManager.observable.notifyDatasetChanged();
				setResult(RESULT_OK);
				Login.this.finish();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				dialog = ProgressDialog.show(Login.this, "",
						Util.getApplicationString("label.login_process",
								Login.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			try {
				Toast.makeText(
						Login.this,
						Util.getApplicationString("label.error_login_error",
								Login.this), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
