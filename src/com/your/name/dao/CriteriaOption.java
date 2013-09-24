/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;
import java.util.ArrayList;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "criteria_options")
public class CriteriaOption implements Serializable {

	private static final long serialVersionUID = -2030312589342263294L;

	@DatabaseField(generatedId = true)
	public Integer /* NOT NULL AUTO_INCREMENT */id;
	
	public boolean hasChilds = false;
	
	@DatabaseField
	public String /* NOT NULL */title;

	@DatabaseField
	public Boolean /* NOT NULL */deleted;

	@DatabaseField(foreign = true, columnName = "criterion_id")
	public Criterion /* DEFAULT NULL */criterion_id;

	@DatabaseField
	public Integer /* DEFAULT NULL */involveid;

	@DatabaseField
	public String /* NOT NULL */description;

	@DatabaseField
	public Integer /* NOT NULL */orderindex;

	@DatabaseField
	public Boolean /* NOT NULL */default_value;

	@DatabaseField
	public Integer /* DEFAULT NULL */parentOption_id;

	@DatabaseField
	public Boolean selected = false;
	
	public Boolean isArticleAddedToFilter = false;
	
	public ArrayList<CriteriaOption> childOptions;

	public ArrayList<Article> articles = new ArrayList<Article>();
}