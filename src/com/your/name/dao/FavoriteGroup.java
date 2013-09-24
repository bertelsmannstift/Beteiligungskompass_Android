/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "favoritegroup")
public class FavoriteGroup {

	@DatabaseField(generatedId = true)
	public Integer id;

	@DatabaseField
	public Integer user_id;

	@DatabaseField
	public String name;
	
	public int amountFavGroups;

	@DatabaseField
	public String sharelink;
	
	@DatabaseField
	public int extern_id;
	
	@DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
	public Date created;

	@DatabaseField
	public Integer shared;
}
