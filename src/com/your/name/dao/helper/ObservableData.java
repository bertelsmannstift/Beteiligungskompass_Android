/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao.helper;

import java.util.ArrayList;
import java.util.List;

public class ObservableData {

	List<DataObserver> observers = new ArrayList<DataObserver>();

	/*
	 * notifies (via callback) all registered observers, that the dataset has
	 * changed. This includes changed filter settings and newly synced data
	 */
	public void notifyDatasetChanged() {
		for (DataObserver observer : observers) {
			observer.dataChanged();
		}
	}

	/*
	 * registers every unique observer at mostly one time
	 */
	public void registerObserver(DataObserver observer) {
		DataObserver removeObserver = null;
		for (DataObserver observ : observers) {
			if (observ.getClass().getSimpleName()
					.equals(observer.getClass().getSimpleName())) {
				removeObserver = observ;
				break;
			}
		}

		if (removeObserver != null) {
			observers.remove(removeObserver);
		}
		if (false == observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void removeObserver() {
		observers.clear();
	}

	/*
	 * removes a data observer from the observers list.
	 */
	public void unregisterObserver(DataObserver observer) {
		while (true == observers.remove(observer))
			;
	}
}