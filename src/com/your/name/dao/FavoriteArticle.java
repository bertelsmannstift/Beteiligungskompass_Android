/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "favorite_articles")
public class FavoriteArticle {
	@DatabaseField(id = true)
	public Integer id;

	@DatabaseField
	public Integer article_id;

	@DatabaseField
	public Integer user_id;

	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date created;
}