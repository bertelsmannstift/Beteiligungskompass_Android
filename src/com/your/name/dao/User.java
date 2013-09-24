/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User implements Serializable {
	
	private static final long serialVersionUID = 3140541594457589531L;

	@DatabaseField(generatedId = true)
	public Integer id;

	@DatabaseField
	String email;

	@DatabaseField
	public String first_name;

	@DatabaseField
	public String last_name;

	@DatabaseField
	String password;

	@DatabaseField
	String salt;

	@DatabaseField
	Boolean is_editor;

	@DatabaseField
	Boolean is_admin;

	@DatabaseField
	String api_token;

	@DatabaseField
	String hash;

	@DatabaseField
	Boolean dbloptin;

	@DatabaseField
	Boolean is_active;

	@DatabaseField
	Boolean is_deleted;
}