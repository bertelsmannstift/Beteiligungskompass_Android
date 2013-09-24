/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.your.name.dao.helper.DatabaseHelper;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * This is an abstract activity. This class initializes the database connection for all
 * the other activities.
 */
public class DatabaseActivity extends FragmentActivity {

	public DatabaseHelper helper = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		helper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	}

	public DatabaseHelper getHelper() {
		if (helper == null) {
			helper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}

		return helper;
	}
}