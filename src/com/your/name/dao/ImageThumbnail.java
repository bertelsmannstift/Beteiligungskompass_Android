/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "image_thumbnails")
public class ImageThumbnail {
	@DatabaseField
	String thumbnail;

	@DatabaseField(generatedId = true)
	Integer id;

	@DatabaseField
	Integer article_id;
}