/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.service.API;

/*
 * This WebView is quite specific to the Web platform API since it uses the 
 * htaccessUser and htaccessPassword parts to set the authentication headers
 */
public class Viewer extends Activity implements OnGestureListener {
	private static String logTag = Viewer.class.toString();

	private GestureDetector dector;

	public static String LIST_FILES = "LIST_FILES";
	public static String CURRENT_POS = "current_pos";
	public static String URL_EXTRA = "url_extra";

	private ArrayList<String> imageList = null;
	private int currentPos = 0;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		String URL = getIntent().getExtras().getString(URL_EXTRA);
		currentPos = getIntent().getExtras().getInt(CURRENT_POS);
		imageList = getIntent().getExtras().getStringArrayList(LIST_FILES);
		Util.logDebug(logTag, "Viewer URL: " + URL);
		setContentView(R.layout.activity_viewer);

		/*
		 * Check if the cache directory exists
		 */
		if (false == new File(getExternalCacheDir() + "/imageCache").exists()) {}

		dector = new GestureDetector(this, this);

		WebView webView = (WebView) findViewById(R.id.webView);

		webView.setWebViewClient(new MyWebViewClient());
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.setBackgroundColor(0x00000000);
		webView.getSettings().setBuiltInZoomControls(true);

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReachedMaxAppCacheSize(long spaceNeeded,
					long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(spaceNeeded * 2);
			}
		});

		webView.getSettings().setDomStorageEnabled(true);

		// Set cache size to 8MB by default. This should be more than enough.
		webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

		/*
		 * This next one is crazy. It's the DEFAULT location for the app's cache
		 * But it didn't work for me without this line.
		 * UPDATE: no hardcoded path. Thanks to Kevin Hawkins
		 * Source: http://alex.tapmania.org/2010/11/html5-cache-android-webview.html
		 */
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		webView.getSettings().setAppCachePath(appCachePath);
		webView.setInitialScale(1);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setAppCacheEnabled(true);

		webView.loadUrl(URL);
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {

			API api = API.getInstance(Viewer.this);
			handler.proceed(api.htaccessUser, api.htaccessPassword);
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		DisplayMetrics dm = getResources().getDisplayMetrics();

		Util.logDebug(logTag, velocityX + "");

		int threshold = (int) (200.0 * (dm.densityDpi / 160.0));

		if ((Math.sqrt(velocityX * velocityX + velocityY * velocityY) > threshold)
				&& (Math.abs(velocityX) > Math.abs(velocityY))) {
			if (velocityX < 0) {
				onNextImageClicked(null);
			} else {
				onPreviousImageClicked(null);
			}
			return true;
		}
		return true;
	}

	public void onNextImageClicked(View v) {
		if ((currentPos + 1) < imageList.size()) {
			WebView webView = (WebView) findViewById(R.id.webView);

			webView.setWebViewClient(new MyWebViewClient());
			webView.getSettings().setLoadWithOverviewMode(true);
			webView.getSettings().setUseWideViewPort(true);
			webView.setInitialScale(1);
			webView.setBackgroundColor(0x00000000);
			webView.getSettings().setBuiltInZoomControls(true);
			currentPos++;
			webView.setWebChromeClient(new WebChromeClient() {

				@Override
				public void onReachedMaxAppCacheSize(long spaceNeeded,
						long totalUsedQuota,
						WebStorage.QuotaUpdater quotaUpdater) {
					quotaUpdater.updateQuota(spaceNeeded * 2);
				}
			});

			webView.getSettings().setDomStorageEnabled(true);

			// Set cache size to 8MB by default. This should be more than enough.
			webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

			/*
			 * This next one is crazy. It's the DEFAULT location for the app's cache
			 * But it didn't work for me without this line.
			 * UPDATE: no hardcoded path. Thanks to Kevin Hawkins
			 * Source: http://alex.tapmania.org/2010/11/html5-cache-android-webview.html
			 */
			String appCachePath = getApplicationContext().getCacheDir()
					.getAbsolutePath();
			webView.getSettings().setAppCachePath(appCachePath);
			webView.getSettings().setAllowFileAccess(true);
			webView.getSettings().setAppCacheEnabled(true);

			Util.logDebug("LINK:", API.getInstance(this).host + "/media/"
					+ imageList.get(currentPos));
			webView.loadUrl(API.getInstance(this).host + "/media/"
					+ imageList.get(currentPos));
		}
	}

	public void onPreviousImageClicked(View v) {
		if (currentPos > 0) {
			WebView webView = (WebView) findViewById(R.id.webView);

			webView.setWebViewClient(new MyWebViewClient());
			webView.getSettings().setLoadWithOverviewMode(true);
			webView.getSettings().setUseWideViewPort(true);
			webView.setInitialScale(1);
			webView.setBackgroundColor(0x00000000);
			webView.getSettings().setBuiltInZoomControls(true);
			webView.setWebChromeClient(new WebChromeClient() {

				@Override
				public void onReachedMaxAppCacheSize(long spaceNeeded,
						long totalUsedQuota,
						WebStorage.QuotaUpdater quotaUpdater) {
					quotaUpdater.updateQuota(spaceNeeded * 2);
				}
			});

			webView.getSettings().setDomStorageEnabled(true);

			// Set cache size to 8MB by default. This should be more than enough.
			webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

			/*
			 * This next one is crazy. It's the DEFAULT location for the app's cache
			 * But it didn't work for me without this line.
			 * UPDATE: no hardcoded path. Thanks to Kevin Hawkins
			 * Source: http://alex.tapmania.org/2010/11/html5-cache-android-webview.html
			 */
			String appCachePath = getApplicationContext().getCacheDir()
					.getAbsolutePath();
			webView.getSettings().setAppCachePath(appCachePath);
			webView.getSettings().setAllowFileAccess(true);
			webView.getSettings().setAppCacheEnabled(true);
			currentPos--;
			Util.logDebug("LINK:", API.getInstance(this).host + "/media/"
					+ imageList.get(currentPos));
			webView.loadUrl(API.getInstance(this).host + "/media/"
					+ imageList.get(currentPos));
		}
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return dector.onTouchEvent(event);
	}

}