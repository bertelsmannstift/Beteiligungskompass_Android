/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.your.name.Base64;
import com.your.name.BasicConfigValue;
import com.your.name.NavigationItem;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.activities.NavigationActivity;
import com.your.name.dao.Article;
import com.your.name.dao.ArticlesOption;
import com.your.name.dao.CriteriaOption;
import com.your.name.dao.Criterion;
import com.your.name.dao.FavoriteGroup;
import com.your.name.dao.FavoritesGroup;
import com.your.name.dao.helper.FavoritManager;

/* 
 * this class defines the whole communication with the website API. You have to
 * set the host URL / API KEY and the htaccessUser with Password if necessary
 * (in case you are using HTACCESS protection during development).
 * The API is a singleton class. You have to initialize it with the
 * getInstance() Method.
 */
public class API {
	public static final String logTag = API.class.toString();

	public static final int ARTICLE_CASE_STUDIES = 0;
	public static final int ARTICLE_METHODS = 1;
	public static final int ARTICLE_QA = 2;
	public static final int ARTICLE_NEWS = 3;
	public static final int ARTICLE_EVENTS = 4;
	public static final int ARTICLE_EXPERTS = 5;

	// the hostname for all api calls
	private static String hostName = "";

	// Htaccess Data
	public String htaccessUser = "";
	public String htaccessPassword = "";

	public String host = "http://" + htaccessUser + ":" + htaccessPassword
			+ "@" + hostName;

	// public static String shareHost = "http://platformaddress.org/";
	// the host name for article sharing if that is an another url like the
	// normal host name
	public static String shareHost = hostName;
	
	// the url part to show one article (only for share necessary)
	public static String articleURL = shareHost + "article/show/";
	
	// the url part to show all articles in the current list (only for share necessary)
	public static String articleListURL = shareHost + "article/index/";

	// API URL Path
	public String apiUrl = host + "/api/";

	// API Key
	public String apiKey = "";

	// the path where all items will be cached
	public String dlCachePath;
	public File dlCacheDir;

	private Context context;

	protected static API api = null;

	public static ArrayList<NavigationItem> navigationItems = null;

	/*
	 * If null != apiUrl use the passed in apiUrl instead of the existing one
	 */
	protected API(Context context, String apiUrl) {
		if (null != apiUrl) {
			this.apiUrl = apiUrl;
		}
		this.context = context;
		dlCachePath = context.getExternalCacheDir().getAbsolutePath()
				+ "/dlcache";
		new File(dlCachePath).mkdirs();
		dlCacheDir = new File(dlCachePath);

		Authenticator.setDefault(new MyAuthenticator());
	}

	public static API getInstance(Context context) {
		if (api == null) {
			api = new API(context, null);
		}

		return api;
	}

	private HttpClient setHttpClient() {
		HttpClient client = new DefaultHttpClient();

		((DefaultHttpClient) client).getCredentialsProvider()
				.setCredentials(
						new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
						new UsernamePasswordCredentials(htaccessUser,
								htaccessPassword));

		return client;
	}

	/**
	 * 
	 * @param dbActivity
	 * @return boolean
	 * @throws Exception
	 * 
	 *             calls all favorite articles from the current user from off
	 *             server
	 */
	public boolean callFavoritesFromServer(DatabaseActivity dbActivity)
			throws Exception {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();
		HttpGet httpGet = new HttpGet(apiUrl
				+ "get_favorites"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", ""));

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();
				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				JSONObject favoritInfo = tokenObject
						.getJSONObject("favoriteInfo");

				SharedPreferences pref = context.getSharedPreferences("LOGIN",
						Context.MODE_PRIVATE);

				for (int i = 0; i < favoritInfo.length(); i++) {
					if (i == 0) {
						JSONArray allFavorites = favoritInfo
								.getJSONArray("allFavorites");
						for (int j = 0; j < allFavorites.length(); j++) {
							int favID = allFavorites.getInt(j);

							FavoritManager.addFavoritToDB(favID,
									pref.getInt("userID", -1), dbActivity);

						}
					} else {
						JSONObject favoriteGroups = favoritInfo
								.getJSONObject("favoriteGroups");

						if (favoriteGroups.names().length() > 0) {
							for (int j = 0; j < favoriteGroups.names().length(); j++) {
								JSONObject favArray = favoriteGroups
										.getJSONObject(favoriteGroups.names()
												.getString(j));
								FavoriteGroup group = new FavoriteGroup();
								group.name = favoriteGroups.names()
										.getString(j);
								group.extern_id = favArray.getInt("id");
								group.user_id = pref.getInt("userID", -1);
								group.sharelink = favArray
										.getString("sharelink");

								group = FavoritManager.addFavoritGroup(group,
										dbActivity);

								JSONArray articleArray = favArray
										.getJSONArray("articles");

								for (int k = 0; k < articleArray.length(); k++) {
									FavoritesGroup fav = new FavoritesGroup();
									fav.favoritegroup_id = group.extern_id;
									fav.favorite_id = FavoritManager
											.getFavoritID(
													articleArray.getInt(k),
													pref.getInt("userID", -1),
													dbActivity);

									FavoritManager.addFavoritesGroups(fav,
											dbActivity);
								}

							}
						}
					}
				}
				return true;
			} else if (statusCode == 400) {
				return false;
			} else {
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (client != null) {
				client = null;
			}
		}
	}

	/**
	 * 
	 * @param articleID
	 * @param context
	 * @return true
	 * @throws Exception
	 * 
	 *             sends the information to the server which articles were
	 *             removed from the favorites list
	 */
	public boolean removeFavorite(int articleID, Context context)
			throws Exception {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();
		HttpGet httpGet = new HttpGet(apiUrl
				+ "remove_favorite"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", "") + "&article_id=" + articleID);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();
				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isRemoved = tokenObject.getBoolean("success");

				if (isRemoved) {
					return true;
				} else {
					Toast.makeText(
							context,
							Util.getApplicationString(
									"label.error_remove_favorite_state",
									context), Toast.LENGTH_LONG).show();
					return false;
				}

			} else if (statusCode == 400) {
				return false;
			} else {
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (client != null) {
				client = null;
			}
		}
	}

	/**
	 * 
	 * @param articleId
	 * @param groupId
	 * @param context
	 * @return boolean
	 * 
	 *         Sends the information to the server which articles were added to
	 *         a favorite group
	 */
	public boolean add_article_from_favoriteGroup(int articleId, int groupId,
			Context context) {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = new HttpGet(apiUrl
				+ "add_article_to_favorite_group"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", "") + "&group_id=" + groupId
				+ "&article_id=" + articleId);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isAdded = tokenObject.getBoolean("success");

				if (isAdded) {
					return true;
				}

				return false;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param articleId
	 * @param groupId
	 * @param context
	 * @return boolean
	 * 
	 *         Sends the information to the server which article were removed
	 *         from a favorite group
	 */
	public boolean remove_article_from_favoriteGroup(int articleId,
			int groupId, Context context) {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = new HttpGet(apiUrl
				+ "remove_article_from_favorite_group"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", "") + "&group_id=" + groupId
				+ "&article_id=" + articleId);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isRemoved = tokenObject.getBoolean("success");

				if (isRemoved) {
					return true;
				}

				return false;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param groupID
	 * @param context
	 * @return boolean
	 * 
	 *         sends the group id, which group can be removed on server site
	 */
	public boolean removeFavoriteGroup(int groupID, Context context) {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = new HttpGet(apiUrl
				+ "remove_favorite_group"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", "") + "&group_id=" + groupID);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isAdded = tokenObject.getBoolean("success");

				Util.logDebug("remove Favorite Group: ", isAdded + "");

				if (isAdded) {
					return true;
				}

				return false;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param groupname
	 * @param context
	 * @return group_id on success else -1
	 * 
	 *         Send a new Favorite Group to the server
	 */
	public int addFavoriteGroup(String groupname, Context context) {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(apiUrl
					+ "add_favorite_group"
					+ "?api_key="
					+ apiKey
					+ "&token="
					+ context.getSharedPreferences("LOGIN",
							Context.MODE_PRIVATE).getString("token", "")
					+ "&title=" + URLEncoder.encode(groupname, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isAdded = tokenObject.getBoolean("success");

				Util.logDebug("add Favorite Group: ", isAdded + "");

				if (isAdded) {
					return tokenObject.getInt("group_id");
				}

				return -1;
			} else {
				return -1;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 
	 * @param article
	 * @param context
	 * @return boolean
	 * @throws Exception
	 * 
	 *             generate a json format with all article information and send
	 *             that format to the server
	 */
	public boolean addArticle(Article article, Context context)
			throws Exception {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpPost post = new HttpPost(apiUrl
				+ "add_article?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", ""));

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");

		JSONObject articleObj = new JSONObject();

		JSONObject articlebaseData = new JSONObject();
		articlebaseData.put("active", article.active);
		articlebaseData.put("address", article.address);
		articlebaseData.put("aim", article.answer);
		articlebaseData.put("author", article.author);
		articlebaseData.put("author_anwser", article.author_answer);
		articlebaseData.put("background", article.background);
		articlebaseData.put("city", article.city);
		articlebaseData.put("contact", article.contact);
		articlebaseData.put("contact_person", article.contact_person);
		articlebaseData.put("country", article.country);
		if (article.date != null) {

			articlebaseData.put("date", article.date.getTime());
		} else {
			articlebaseData.put("date", "");
		}

		if (article.deadline != null) {
			articlebaseData.put("deadline", article.deadline.getTime());
		} else {
			articlebaseData.put("deadline", "");
		}
		articlebaseData.put("deleted", false);
		articlebaseData.put("description", article.description);
		articlebaseData.put("description_institution",
				article.description_institution);
		articlebaseData.put("description_plaintext",
				article.description_plaintext);
		articlebaseData.put("email", article.email);
		if (article.end_date != null) {
			articlebaseData.put("end_date", article.end_date.getTime());
		} else {
			articlebaseData.put("end_date", "");
		}
		articlebaseData.put("end_month", article.end_month);
		articlebaseData.put("end_year", article.end_year);
		JSONArray linkArray = new JSONArray();
		for (String link : article.externalLinks) {
			if (link.equals("")) {
				continue;
			}
			linkArray.put(link);
		}
		articlebaseData.put("external_links", linkArray);
		articlebaseData.put("zip", article.zip);
		articlebaseData.put("fax", article.fax);
		articlebaseData.put("fee", article.fee);
		articlebaseData.put("firstname", article.firstname);

		JSONArray images = new JSONArray();
		for (com.your.name.dao.File file : article.imageFiles) {
			JSONObject image = new JSONObject();
			Bitmap tmp = BitmapFactory.decodeStream(context
					.getContentResolver().openInputStream(Uri.parse(file.uri)));

			ByteArrayOutputStream bao = new ByteArrayOutputStream();

			tmp.compress(Bitmap.CompressFormat.JPEG, 90, bao);

			byte[] ba = bao.toByteArray();

			String bytes = Base64.encodeBytes(ba);

			image.put("name", file.filename);
			image.put("content", bytes);
			images.put(image);
			ba = null;
			tmp = null;
			bao.close();

		}
		articlebaseData.put("images", images);
		articlebaseData.put("institution", article.institution);
		articlebaseData.put("intro", article.intro);
		articlebaseData.put("involveid", "");
		articlebaseData.put("year", article.year);
		articlebaseData.put("lastname", article.lastname);
		articlebaseData.put("link", article.link);
		articlebaseData.put("more_information", article.more_information);
		articlebaseData.put("number_of_participants",
				article.number_of_participants);
		articlebaseData.put("organized_by", article.organized_by);
		articlebaseData.put("origin", article.origin);
		articlebaseData.put("participants", article.participants);
		articlebaseData.put("participation", article.participatioN_id);
		articlebaseData.put("phone", article.phone);
		articlebaseData.put("process", article.process);
		articlebaseData.put("projectstatus", article.projectstatus);
		articlebaseData.put("publisher", article.publisher);
		articlebaseData.put("question", article.question);
		articlebaseData.put("ready_for_publish", article.ready_for_publish);
		articlebaseData.put("restrictions", article.restrictions);
		articlebaseData.put("results", article.results);
		articlebaseData.put("short_description", article.short_description);
		articlebaseData.put("short_description_expert",
				article.short_description_expert);
		if (article.start_date != null) {
			articlebaseData.put("start_date", article.start_date.getTime());
		} else {
			articlebaseData.put("start_date", "");
		}
		articlebaseData.put("start_month", article.start_month);
		articlebaseData.put("start_year", article.start_year);
		articlebaseData.put("sticky", null);
		articlebaseData.put("street", article.street);
		articlebaseData.put("street_nr", article.street_nr);
		articlebaseData.put("strengths", article.strengths);
		articlebaseData.put("subtitle", article.subtitle);
		articlebaseData.put("text", article.text);
		articlebaseData.put("time_expense", article.time_expense);
		articlebaseData.put("title", article.title);
		articlebaseData.put("type", article.type);
		articlebaseData.put("updated", "");
		articlebaseData.put("used_for", article.used_for);

		articlebaseData.put("venue", article.venue);
		articlebaseData.put("street_nr", article.street_nr);
		articlebaseData.put("videos", null);
		articlebaseData.put("weaknesses", article.weaknesses);
		articlebaseData.put("when_not_to_use", article.when_not_to_use);
		articlebaseData.put("when_to_use", article.when_to_use);

		JSONArray linkedArticles = new JSONArray();

		for (Article tmp : article.linkedArticles) {
			linkedArticles.put(tmp.id);
		}

		articlebaseData.put("linked_articles", linkedArticles);
		JSONArray criteriaOptions = new JSONArray();
		if (article.options == null) {
			if (article.critera != null) {
				for (Criterion criterion : article.critera) {
					for (CriteriaOption option : criterion.options) {
						if (option.isArticleAddedToFilter) {
							criteriaOptions.put(option.id);
						}
					}
				}
			}
		} else {
			for (ArticlesOption option : article.options) {
				criteriaOptions.put(option.model_criterion_option_id);
			}
		}
		articlebaseData.put("criteriaOptions", criteriaOptions);
		articleObj.put("article", articlebaseData);
		Util.logDebug("ARTICLE", articleObj.toString());
		StringEntity StringEntity = new StringEntity(articleObj.toString());

		post.setEntity(StringEntity);
		try {

			HttpResponse response = client.execute(post);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();

				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isAdded = tokenObject.getBoolean("success");

				if (isAdded) {

					JSONArray array = tokenObject.getJSONArray("images");
					for (int i = 0; i < array.length(); i++) {
						int fileID = array.getInt(i);
						com.your.name.dao.File file = article.imageFiles
								.get(i);
						Util.logDebug("GETTING IMAGES IDS", "FILE ID: "
								+ fileID);
						file.id = fileID;
						article.imageFiles.set(i, file);
					}

					return true;
				} else {

					return false;
				}
			} else if (statusCode == 400) {
				return false;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	/**
	 * 
	 * @param articleID
	 * @param context
	 * @return boolean
	 * @throws Exception
	 * 
	 *             send the article id as a new favorite to the server.
	 */
	public boolean addFavorite(int articleID, Context context) throws Exception {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = new HttpGet(apiUrl
				+ "add_favorite"
				+ "?api_key="
				+ apiKey
				+ "&token="
				+ context.getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
						.getString("token", "") + "&article_id=" + articleID);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				boolean isAdded = tokenObject.getBoolean("success");

				if (isAdded) {

					return true;
				} else {

					return false;
				}

			} else if (statusCode == 400) {
				return false;
			} else {
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 
	 * @param email
	 * @param pwd
	 * @return boolean
	 * @throws Exception
	 * 
	 *             try to authenticate the user with the server. If an active
	 *             account is available, the method returns TRUE otherwise FALSE
	 */
	public boolean authUser(String email, String pwd) throws Exception {
		StringBuilder builder = new StringBuilder();

		HttpClient client = setHttpClient();

		HttpGet httpGet = new HttpGet(apiUrl + "login" + "?api_key=" + apiKey
				+ "&email=" + email + "&password=" + pwd);

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content), (8 * 1024));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				content.close();
				reader.close();

				JSONObject jsonObject = new JSONObject(builder.toString());

				JSONObject tokenObject = jsonObject.getJSONObject("response");

				String token = tokenObject.getString("token");
				int userID = tokenObject.getInt("userid");
				if (token != null && token.length() > 0) {
					SharedPreferences pref = context.getSharedPreferences(
							"LOGIN", Context.MODE_PRIVATE);
					Editor edit = pref.edit();
					edit.putInt("userID", userID);
					edit.putString("token", token);
					edit.putString("email", email);
					edit.putString("pwd", pwd);
					edit.commit();
				}
				return true;

			} else {
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param context
	 * @return
	 * 
	 *         Currently it doesn't work with the server. But if you want to
	 *         implement an API call that gets all NavigationItem information,
	 *         feel free to do that here.
	 */
	public ArrayList<NavigationItem> getNavigationItems(
			NavigationActivity context) {

		if (navigationItems == null) {
			navigationItems = new ArrayList<NavigationItem>();
		} else {
			navigationItems.clear();
		}
		try {
			/*
			 * Studies and methods are not modal. They must always be shown
			 */
			NavigationItem item = new NavigationItem(Util.getApplicationString(
					"module.studies.title", context),
					Util.getApplicationString(
							"module.studies.short_description", context),
					ARTICLE_CASE_STUDIES, true, context);
			navigationItems.add(item);
			item = new NavigationItem(Util.getApplicationString(
					"module.methods.title", context),
					Util.getApplicationString(
							"module.methods.short_description", context),
					ARTICLE_METHODS, true, context);
			navigationItems.add(item);
			if (Util.isModuleActive(BasicConfigValue.MODULE_QA, context)) {
				item = new NavigationItem(Util.getApplicationString(
						"module.qa.title", context), Util.getApplicationString(
						"module.qa.short_description", context), ARTICLE_QA,
						true, context);
				navigationItems.add(item);
			}
			if (Util.isModuleActive(BasicConfigValue.MODULE_NEWS, context)) {
				item = new NavigationItem(Util.getApplicationString(
						"module.news.title", context),
						Util.getApplicationString(
								"module.news.short_description", context),
						ARTICLE_NEWS, true, context);

				navigationItems.add(item);
			}
			if (Util.isModuleActive(BasicConfigValue.MODULE_EVENT, context)) {
				item = new NavigationItem(Util.getApplicationString(
						"module.events.title", context),
						Util.getApplicationString(
								"module.events.short_description", context),
						ARTICLE_EVENTS, true, context);
				navigationItems.add(item);
			}
			if (Util.isModuleActive(BasicConfigValue.MODULE_EXPERT, context)) {
				item = new NavigationItem(Util.getApplicationString(
						"module.experts.title", context),
						Util.getApplicationString(
								"module.experts.short_description", context),
						ARTICLE_EXPERTS, true, context);
				navigationItems.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return navigationItems;
	}

	/**
	 * 
	 * @throws Exception
	 * 
	 *             if the download cache dir is not available, this will be created
	 *             on the android device
	 */
	public void createDLCacheDir() throws Exception {
		if (false == Util.createDirectoryAnyways(dlCacheDir)) {
			throw new Exception(
					"Something went wrong creating the DL cache dir");
		}
	}

	/**
	 * 
	 * @throws Exception
	 *             if the sync is completed, we have to destroy the download
	 *             cache Dir
	 */
	public void deleteDLCacheDir() throws Exception {

		if (false == Util.removeDirectory(dlCacheDir)) {
			throw new Exception(
					"Something went wrong removing the DL cache dir");
		}
	}

	/**
	 * downloads the new sqlite db and stores it in the dlcache. Returns the
	 * path to the downloaded file if sucessfull
	 */
	public String getNewDB() throws Exception {
		Util.logDebug(logTag, "getting new DB");

		URL callURL = new URL(apiUrl + "export" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream dbStream = connection.getInputStream();

		if (null == dbStream) {
			throw new Exception("Something went wrong trying to get the DB");
		}

		String outputPath = dlCachePath + "/" + "db.sqlite";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(dbStream, out, new byte[8192]);

		out.close();
		dbStream.close();

		Util.logDebug(logTag, "getting new DB - done");

		return outputPath;
	}

	/**
	 * 
	 * @return output cache path
	 * @throws Exception
	 * 
	 *             Download the videos and files from the server and save the
	 *             thumb.ip in the download cache path
	 */
	public String getThumbnails() throws Exception {
		Util.logDebug(logTag, "getting thumbnails");
		URL callURL = new URL(apiUrl + "get_thumbnails" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream dbStream = connection.getInputStream();

		if (null == dbStream) {
			throw new Exception(
					"Something went wrong trying to get the thumbnails");
		}

		String outputPath = dlCachePath + "/" + "thumb.zip";
		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(dbStream, out, new byte[8192]);

		out.close();
		dbStream.close();

		Util.logDebug(logTag, "getting thumbnails - done");

		return outputPath;
	}

	/**
	 * 
	 * @return String
	 * @throws Exception
	 * 
	 *             Download the Application strings. The strings are used for
	 *             localization
	 */
	public String getApplicationStrings() throws Exception {
		Util.logDebug(logTag, "getting string");
		URL callURL = new URL(apiUrl + "get_strings" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream stream = connection.getInputStream();

		String outputPath = dlCachePath + "/" + "application_strings.json";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(stream, out, new byte[8192]);

		out.close();
		stream.close();

		Util.logDebug(logTag, "getting strings - done");

		return outputPath;
	}

	/**
	 * 
	 * @return String
	 * @throws Exception
	 * 
	 *             Download the Module State Settings, the settings will be used
	 *             to decide which module will be visible or not
	 */
	public String getModuleStateStrings() throws Exception {
		Util.logDebug(logTag, "getting modal sections");
		URL callURL = new URL(apiUrl + "get_module_state" + "?api_key="
				+ apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream stream = connection.getInputStream();

		String outputPath = dlCachePath + "/" + "module_state.json";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(stream, out, new byte[8192]);

		out.close();
		stream.close();

		Util.logDebug(logTag, "getting modal sections - done");

		return outputPath;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 * 
	 *             Read the basic config from the website.
	 */
	public String getBasicConfig() throws Exception {
		Util.logDebug(logTag, "getting basic config");
		URL callURL = new URL(apiUrl + "get_base_config" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream stream = connection.getInputStream();

		String outputPath = dlCachePath + "/" + "sort_settings.json";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(stream, out, new byte[8192]);

		out.close();
		stream.close();

		Util.logDebug(logTag, "getting basic config - done");

		return outputPath;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 *             Download the About.html content from the server
	 */
	public String getAbout() throws Exception {
		Util.logDebug(logTag, "getting About");
		URL callURL = new URL(apiUrl + "get_static_page" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream stream = connection.getInputStream();

		String outputPath = dlCachePath + "/" + "about_page";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(stream, out, new byte[8192]);

		out.close();
		stream.close();

		Util.logDebug(logTag, "getting about - done");

		return outputPath;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 *             Download the terms.html from the Server
	 */
	public String getTermsOfUse() throws Exception {
		Util.logDebug(logTag, "getting terms");
		URL callURL = new URL(apiUrl + "get_terms" + "?api_key=" + apiKey);

		URLConnection connection = callURL.openConnection();
		InputStream stream = connection.getInputStream();

		String outputPath = dlCachePath + "/" + "terms_of_use";

		FileOutputStream out = new FileOutputStream(new File(outputPath));

		Util.flow(stream, out, new byte[8192]);

		out.close();
		stream.close();

		Util.logDebug(logTag, "getting terms - done");

		return outputPath;
	}

	public class MyAuthenticator extends Authenticator {
		// This method is called when a password-protected URL is accessed
		protected PasswordAuthentication getPasswordAuthentication() {

			// Get the username from the user...
			String username = "BK";

			// Get the password from the user...
			String password = "Kompass2012";

			// Return the information
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}
