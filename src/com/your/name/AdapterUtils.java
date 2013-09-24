/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class AdapterUtils {
	private static String logTag = AdapterUtils.class.toString();

	/**
	 * This is used in places where one cannot use a ListView, since it's part
	 * of a greater layout that scrolls
	 */

	public static void fillLinearLayoutFromAdapter(int columns,
			LinearLayout layout, BaseAdapter adapter, Context context,
			boolean equalWeights, boolean clearLayout) {
		fillLinearLayoutFromAdapter(columns, layout, adapter, context,
				equalWeights, clearLayout, 0);
	}

	public static void fillLinearLayoutFromAdapter(int columns,
			LinearLayout layout, BaseAdapter adapter, Context context,
			boolean equalWeights, boolean clearLayout, int margin) {

		if (true == clearLayout) {
			layout.removeAllViews();
		}

		LinearLayout rowLayout = null;

		for (int index = 0, maxIndex = adapter.getCount(); index < maxIndex; ++index) {
			if (0 == (index % columns)) {
				Util.logDebug(logTag, "adding a row");
				rowLayout = new LinearLayout(context);
				layout.addView(rowLayout);
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rowLayout
						.getLayoutParams();
				layoutParams.bottomMargin = margin;
				rowLayout.setLayoutParams(layoutParams);
				rowLayout.setOrientation(LinearLayout.HORIZONTAL);
			}

			View v = adapter.getView(index, null, null);
			rowLayout.addView(v);

			if (true == equalWeights) {

				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) (v
						.getLayoutParams());

				params.weight = 1.0f;
				params.width = 0;

				v.setLayoutParams(params);
			}
		}
	}
}