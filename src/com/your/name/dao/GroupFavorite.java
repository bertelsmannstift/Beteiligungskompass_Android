/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "group_favorites")
public class GroupFavorite {
	@DatabaseField(uniqueCombo = true)
	public Integer model_favoritegroup_id;

	@DatabaseField(uniqueCombo = true)
	public Integer model_article_id;
}