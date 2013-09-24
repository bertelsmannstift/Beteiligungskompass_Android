/**
 * Copyright (C) 2013 Bertelsmann Stiftung
 */
package com.your.name.fragments.article;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.your.name.R;
import com.your.name.Util;
import com.your.name.activities.DatabaseActivity;
import com.your.name.dao.Article;
import com.your.name.dao.Article.PaneConfig;
import com.your.name.dao.User;

/*
 * this fragment displays the author and creation date in the headline of the
 * article details
 */
public class Author extends Fragment {

	Article.PaneConfig config = null;
	Article article = null;

	private static String CONFIG_KEY = "config";
	private static String ARTICLE_ID_KEY = "articleid";

	public static Author newInstance(Article article, PaneConfig config) {
		Bundle bundle = new Bundle();

		bundle.putInt(ARTICLE_ID_KEY, article.id);
		bundle.putSerializable(CONFIG_KEY, config);

		Author text = new Author();
		text.setArguments(bundle);

		return text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_article_author, container,
				false);

		Bundle args = getArguments();

		config = (PaneConfig) args.getSerializable(CONFIG_KEY);
		article = null;

		Dao<Article, Integer> articlesDAO;
		try {
			articlesDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(Article.class);

			article = articlesDAO.queryForId(args.getInt(ARTICLE_ID_KEY));

		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		TextView lastEdit = (TextView) v.findViewById(R.id.textViewLastUpdated);

		try {
			Dao<User, Integer> userDAO = ((DatabaseActivity) getActivity()).helper
					.getDao(User.class);

			SimpleDateFormat updatedFormat = new SimpleDateFormat("dd.MM.yyyy",
					Locale.getDefault());
			if (article.type.equals(Article.NEWS)) {
				((TextView) v.findViewById(R.id.textViewAutor))
						.setText(article.author);

				lastEdit.setText(", " + updatedFormat.format(article.date));

			} else {
				List<User> users = userDAO.queryForEq("id", article.user.id);

				if (users.size() > 0) {
					if (Util.isTablet(getActivity())) {
						((TextView) v.findViewById(R.id.textViewAutor))
								.setText(Util.getApplicationString(
										"global.author", getActivity())
										+ ": "
										+ users.get(0).first_name
										+ " "
										+ users.get(0).last_name + ", ");
					} else {
						((TextView) v.findViewById(R.id.textViewAutor))
								.setText(users.get(0).first_name + " "
										+ users.get(0).last_name + ", ");
					}
				}

				if (Util.isTablet(getActivity())) {
					lastEdit.setText(Util.getApplicationString("label.updated",
							getActivity())
							+ ": "
							+ updatedFormat.format(article.updated));
				} else {
					lastEdit.setText(updatedFormat.format(article.updated));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}
}
