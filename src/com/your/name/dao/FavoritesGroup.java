/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "favorites_groups")
public class FavoritesGroup {
	@DatabaseField(uniqueCombo = true)
	public Integer favoritegroup_id;

	@DatabaseField(uniqueCombo = true)
	public Integer favorite_id;
}