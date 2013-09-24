/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "files")
public class File implements Serializable {
	
	private static final long serialVersionUID = 1835431411975044626L;
	
	@DatabaseField
	public Integer id;

	@DatabaseField
	public String md5;

	@DatabaseField
	public String filename;

	@DatabaseField
	public String mime;

	@DatabaseField
	public String ext;

	@DatabaseField
	public Integer size;

	@DatabaseField
	public String uri;
}