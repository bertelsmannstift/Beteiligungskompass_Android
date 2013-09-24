/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

import com.your.name.R;
import com.your.name.Util;

public class Contact extends NavigationActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_contact);
		try {
			((TextView) this.findViewById(R.id.navbarTitle)).setText(Util
					.getApplicationString("label.info_contact", this));

			((TextView) this.findViewById(R.id.responsableHeader))
					.setText(Util.getApplicationString(
							"label.responsible_for_content", this));
			
			((TextView)this.findViewById(R.id.contactHeader)).setText(Util.getApplicationString("label.contact", this));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String termsHTML = getSharedPreferences(getString(R.string.termsHTML),
				MODE_PRIVATE).getString(getString(R.string.termsHTML), "");

		((WebView) findViewById(R.id.termsView)).loadDataWithBaseURL("",
				termsHTML, "text/html", "UTF-8", null);

	}

	@Override
	public void onBackPressed() {
		if (findViewById(R.id.navbarLayout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.navbarLayout).setVisibility(View.GONE);
			return;
		}

		Intent intent = new Intent(this, Dashboard.class);
		startActivity(intent);

		finish();
	}
}
