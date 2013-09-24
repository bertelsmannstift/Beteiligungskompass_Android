/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.dashboard;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.Sync;
import com.your.name.dao.helper.ArticleManager;
import com.your.name.dao.helper.DataObserver;
import com.your.name.dao.helper.FeaturedVideo;

/* 
 * shows article videos on the dashboard. 
 */
public class VideoPlayer extends Fragment implements OnGestureListener,
		DataObserver {

	private static String logTag = VideoPlayer.class.toString();

	private GestureDetector gestureDetector;

	private LinearLayout arrowLeft, arrowRight;

	private int currentVideo = 0;

	DatabaseActivity activity;

	private List<FeaturedVideo> featuredVideos = new ArrayList<FeaturedVideo>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activity = (DatabaseActivity) getActivity();

		View v = inflater.inflate(R.layout.fragment_dashboard_videoplayer,
				container, false);

		arrowLeft = (LinearLayout) v.findViewById(R.id.arrowLeft);
		arrowLeft.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				movePrevious();
			}
		});

		arrowRight = (LinearLayout) v.findViewById(R.id.arrowRight);
		arrowLeft.setVisibility(View.INVISIBLE);
		arrowRight.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				moveNext();
			}
		});

		gestureDetector = new GestureDetector(this.getActivity(), this);
		v.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		v.findViewById(R.id.videoView).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse(featuredVideos.get(currentVideo).videoURL));
						startActivity(intent);
					}
				});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		ArticleManager.observable.registerObserver(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		ArticleManager.observable.unregisterObserver(this);
	}

	private void init() {
		try {
			featuredVideos = ArticleManager.getFeaturedVideos(activity);
			Util.logDebug(logTag,
					"size of featuredVideos: " + featuredVideos.size());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		showVideo(currentVideo, getView());
	}

	private void showVideo(int currentVideoIndex, View v) {
		if (featuredVideos.size() == 0) {
			Util.logDebug(logTag, "no videos to show");
			return;
		}

		try {
			Pair<String, String> video;

			video = Util.getVideoIDFromURL(featuredVideos
					.get(currentVideoIndex).videoURL);

			String thumbPath = Sync.getVideoThumbnailsPath(getActivity())
					+ video.second + "_" + video.first + ".jpg";

			ImageView videoView = (ImageView) v.findViewById(R.id.videoView);

			Util.logDebug(logTag, "thumb path: " + thumbPath);

			Bitmap bitmap = BitmapFactory.decodeFile(thumbPath);

			videoView.setImageBitmap(bitmap);

			((TextView) v.findViewById(R.id.amountVideoTextView))
					.setText((currentVideoIndex + 1) + " von "
							+ featuredVideos.size());

			((TextView) v.findViewById(R.id.videoplayerDescription))
					.setText(Html.fromHtml(Html
							.fromHtml(
									featuredVideos.get(currentVideoIndex).article
											.getListDescription()).toString()
							.trim()));

			((TextView) v.findViewById(R.id.videoplayerTitle))
					.setText(featuredVideos.get(currentVideoIndex).article
							.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void moveNext() {
		currentVideo = currentVideo < (featuredVideos.size() - 1) ? ++currentVideo
				: currentVideo;
		if (currentVideo == (featuredVideos.size() - 1)) {
			getActivity().findViewById(R.id.arrowRight).setVisibility(
					View.INVISIBLE);
		} else if (currentVideo == 0) {
			getActivity().findViewById(R.id.arrowLeft).setVisibility(
					View.INVISIBLE);
		} else {
			getActivity().findViewById(R.id.arrowRight).setVisibility(
					View.VISIBLE);
			getActivity().findViewById(R.id.arrowLeft).setVisibility(
					View.VISIBLE);
		}
		showVideo(currentVideo, getView());
	}

	private void movePrevious() {
		currentVideo = currentVideo > 0 ? --currentVideo : currentVideo;

		if (currentVideo == (featuredVideos.size() - 1)) {
			getActivity().findViewById(R.id.arrowRight).setVisibility(
					View.INVISIBLE);
		} else if (currentVideo == 0) {
			getActivity().findViewById(R.id.arrowLeft).setVisibility(
					View.INVISIBLE);
		} else {
			getActivity().findViewById(R.id.arrowRight).setVisibility(
					View.VISIBLE);
			getActivity().findViewById(R.id.arrowLeft).setVisibility(
					View.VISIBLE);
		}

		showVideo(currentVideo, getView());
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int dx = (int) (e2.getX() - e1.getX());

		if (Math.abs(dx) > 120 && Math.abs(velocityX) > Math.abs(velocityY)) {
			if (velocityX > 0) {
				movePrevious();
			} else {
				moveNext();
			}
			return true;
		} else {
			return false;
		}
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {

	}

	public boolean onSingleTapUp(MotionEvent e) {

		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		outState.putString("tab", "VideoPlayer");

		super.onSaveInstanceState(outState);

	}

	@Override
	public void dataChanged() {
		init();
	}

}
