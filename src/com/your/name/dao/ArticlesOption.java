/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "articles_options")
public class ArticlesOption implements Serializable {

	private static final long serialVersionUID = 1L;

	@DatabaseField(uniqueCombo = true, index = true)
	public Integer model_article_id = 0;

	@DatabaseField(uniqueCombo = true, index = true)
	public Integer model_criterion_option_id = 0;
}