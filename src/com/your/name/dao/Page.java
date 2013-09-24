/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "pages")
public class Page {
	@DatabaseField(generatedId = true)
	Integer id;

	@DatabaseField
	String title;

	@DatabaseField
	String content;

	@DatabaseField
	String type;

	@DatabaseField
	String short_title;

	@DatabaseField
	String fields;
}