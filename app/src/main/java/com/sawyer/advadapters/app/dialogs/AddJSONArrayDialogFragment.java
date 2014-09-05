/**
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sawyer.advadapters.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Renders a dialog with all the options possible for adding movie items to a list. Implement the
 * {@link EventListener} in order to receive back dialog results. Toggling the Varargs option on and
 * off is possible through the listener.
 */
public class AddJSONArrayDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";
	private static final String STATE_ENABLE_ARGVARGS = "State Enable Argvargs";

	private EventListener mEventListener;

	private boolean mIsArgvargsEnabled;
	private JSONArray mMovieItems;

	public static AddJSONArrayDialogFragment newInstance() {
		AddJSONArrayDialogFragment frag = new AddJSONArrayDialogFragment();

		//Generate unique movie listing
		Set<MovieItem> tempMovies = new HashSet<>();
		while (tempMovies.size() != 3) {
			int index = new Random().nextInt(MovieContent.ITEM_LIST.size());
			tempMovies.add(MovieContent.ITEM_LIST.get(index));
		}

		//Convert over to JSONArray
		JSONArray movies = new JSONArray();
		for (MovieItem movie : tempMovies) {
			movies.put(movie.toJSONObject());
		}

		Bundle bundle = new Bundle();
		bundle.putString(STATE_MOVIES, movies.toString());
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mMovieItems = new JSONArray(getArguments().getString(STATE_MOVIES));
		} catch (JSONException e) {
			e.printStackTrace();
			mMovieItems = new JSONArray();
		}
		mIsArgvargsEnabled = getArguments().getBoolean(STATE_ENABLE_ARGVARGS, true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_add_array);
		dialog.setTitle(R.string.title_dialog_add_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_single_btn);
		btn.setOnClickListener(new OnAddSingleClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.optJSONObject(0).optString(MovieItem.JSON_TITLE));

		btn = (Button) dialog.findViewById(R.id.movies_collection_btn);
		btn.setOnClickListener(new OnAddJSONArrayClickListener());
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.optJSONObject(1).optString(MovieItem.JSON_TITLE));
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.optJSONObject(2).optString(MovieItem.JSON_TITLE));

		btn = (Button) dialog.findViewById(R.id.movies_vararg_btn);
		btn.setOnClickListener(new OnAddVarargsClickListener());
		btn.setVisibility(mIsArgvargsEnabled ? View.VISIBLE : View.GONE);
		return dialog;
	}

	public void setEnableArgvargs(boolean enable) {
		getArguments().putBoolean(STATE_ENABLE_ARGVARGS, enable);
		mIsArgvargsEnabled = enable;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onAddMultipleMoviesClick(JSONArray movies);

		public void onAddSingleMovieClick(JSONObject movie);

		public void onAddVarargsMovieClick(JSONObject... movies);
	}

	private class OnAddJSONArrayClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				JSONArray newArray = new JSONArray();
				newArray.put(mMovieItems.optJSONObject(1));
				newArray.put(mMovieItems.optJSONObject(2));
				mEventListener.onAddMultipleMoviesClick(newArray);
			}
		}
	}

	private class OnAddSingleClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAddSingleMovieClick(mMovieItems.optJSONObject(0));
			}
		}
	}

	private class OnAddVarargsClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAddVarargsMovieClick(mMovieItems.optJSONObject(1),
													  mMovieItems.optJSONObject(2));
			}
		}
	}
}
