/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "criteria")
public class Criterion implements Serializable {

	/**
	 * Criteria Types
	 */
	public static final String CRITERIA_TYPE_RESOURCE = "resource";
	public static final String CRITERIA_TYPE_CHECK = "check";
	public static final String CRITERIA_TYPE_RADIO = "radio";
	public static final String CRITERIA_TYPE_SELECT = "select";

	private static final long serialVersionUID = 1L;

	@DatabaseField(generatedId = true)
	public Integer /* NOT NULL AUTO_INCREMENT */id;

	public int amountVisibleOptions = 0;

	@DatabaseField
	public String /* NOT NULL */title;

	@DatabaseField
	public String /* NOT NULL */type;

	@DatabaseField
	public Boolean /* NOT NULL */deleted;

	@DatabaseField
	public String /* NOT NULL */description;

	@DatabaseField
	public Integer /* NOT NULL */orderindex;

	@DatabaseField
	public String /* DEFAULT NULL */discriminator;

	@DatabaseField
	public String /* DEFAULT NULL */article_types;

	@DatabaseField
	public Boolean /* NOT NULL */show_in_planner;

	@DatabaseField
	public String /* DEFAULT NULL */group_article_types;

	@DatabaseField
	public Boolean /* NOT NULL */filter_type_or;

	@ForeignCollectionField(eager = true, orderColumnName="orderindex")
	public ForeignCollection<CriteriaOption> options = null;

	public List<CriteriaOption> visibleOptions = new ArrayList<CriteriaOption>();

	public ArrayList<Article> articles = new ArrayList<Article>();

	public HashMap<Integer, Article> filteredArticles = new HashMap<Integer, Article>();
}