/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.your.name.BasicConfigValue;
import com.your.name.R;
import com.your.name.Util;

public class Dashboard extends NavigationActivity {

	private static String logTag = Dashboard.class.toString();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Util.logDebug(logTag, "Dashboard onCreate()");

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_dashboard);

		setDashboardView();

		this.findViewById(R.id.tabDashboardLayout).setBackgroundResource(
				R.color.background_dark_highlighted);

		((ImageView) this.findViewById(R.id.sync)).setVisibility(View.VISIBLE);
	}

	/**
	 * based on the config parameter from the web platform, we have to set the
	 * visibility of the different fragments. Mainly for tablets.
	 */
	private void setDashboardView() {
		boolean showVideoBox = Util.getSortString(BasicConfigValue.MODULE_VIDEOBOX_NEWSBOX_EVENTBOX, this).equals("video") ? true : false;
 		
		boolean showNewsBox = Util.isModuleActive(
				BasicConfigValue.MODULE_NEWSBOX, this);
		boolean showEventBox = Util.isModuleActive(
				BasicConfigValue.MODULE_EVENTBOX, this);
		
		if(Util.isTablet(this) && showVideoBox){
			this.findViewById(R.id.eventFragment).setVisibility(View.GONE);
			boolean videoBox = Util.isModuleActive(BasicConfigValue.MODULE_VIDEOBOX, this);
			
			if(!videoBox && Util.isOrientationPortrait(this)){
				this.findViewById(R.id.videoplayerFragment).setVisibility(View.GONE);
			}
		}else if(Util.isTablet(this)){
			this.findViewById(R.id.videoplayerFragment).setVisibility(View.GONE);
		}
		// if (!showVideoBox && !showNewsBox && !showEventBox
		// && !Util.isOrientationPortrait(this) && Util.isTablet(this)) {
		if (!showNewsBox && !showEventBox && !Util.isOrientationPortrait(this)
				&& Util.isTablet(this)) {
			this.findViewById(R.id.newsEventScrollView)
					.setVisibility(View.GONE);
		} else {
			// if (!showVideoBox) {
			// this.findViewById(R.id.videoplayerFragment).setVisibility(
			// View.GONE);
			// }

			if (Util.isTablet(this)) {

				try {
					((TextView) this.findViewById(R.id.textView1)).setText(Html
							.fromHtml(
									Util.getApplicationString(
											"home.project_description.text",
											this)).toString().trim()
							.replace(":nl", "").trim());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if ((!showEventBox && !showNewsBox)) {
					this.findViewById(R.id.eventFragment).setVisibility(
							View.GONE);

				}
			}
		}
	}

	public void onSyncClicked(View v) {
		getSharedPreferences(getString(R.string.strings_json), MODE_PRIVATE)
				.edit().putLong("synctime", System.currentTimeMillis())
				.commit();
		Intent intent = new Intent(this, Sync.class);
		startActivity(intent);
		this.finish();
	}

	@Override
	public void onBackPressed() {
		if (findViewById(R.id.navbarLayout).getVisibility() == View.VISIBLE) {
			findViewById(R.id.navbarLayout).setVisibility(View.GONE);
		} else {
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		final SharedPreferences prefs = getSharedPreferences(
				getString(R.string.strings_json), MODE_PRIVATE);

		long syncTime = prefs.getLong("synctime", 0);

		long lastSyncSince = (System.currentTimeMillis() - syncTime) / 3600000;

		if ((syncTime == 0 || lastSyncSince >= 24)
				&& Util.isWifiConnected(this)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			try {
				builder.setTitle(Util.getApplicationString("updatealert.title",
						this));

				builder.setMessage(Util.getApplicationString(
						"updatealert.text", this));
				builder.setPositiveButton(
						Util.getApplicationString("updatealert.update", this),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								Intent intent = new Intent(Dashboard.this,
										Sync.class);
								startActivity(intent);
								finish();

							}
						});
				builder.setNegativeButton(
						Util.getApplicationString("updatealert.cancel", this),
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

			builder.create().show();
			prefs.edit().putLong("synctime", System.currentTimeMillis())
					.commit();
		}
	}
}
