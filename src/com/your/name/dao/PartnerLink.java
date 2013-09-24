/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "partnerlinks")
public class PartnerLink {
	@DatabaseField(index = true)
	Integer id;

	@DatabaseField
	public String title;

	@DatabaseField
	public String content;
}