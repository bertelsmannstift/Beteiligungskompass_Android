/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "article_links")
public class ArticleLink implements Serializable {

	private static final long serialVersionUID = 1L;

	@DatabaseField(uniqueCombo = true, columnName = "article_id")
	public Integer article_id = null;

	@DatabaseField(uniqueCombo = true, columnName = "article_linked_id")
	public Integer article_linked_id = null;
}