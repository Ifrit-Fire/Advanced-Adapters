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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Renders a dialog with all the options possible for adding movie items to a list. Implement the
 * {@link EventListener} in order to receive back dialog results. Toggling the Varargs option on and
 * off is possible through the listener.
 */
public class AddArrayDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";

	private EventListener mEventListener;

	private List<MovieItem> mMovieItems;

	public static AddArrayDialogFragment newInstance() {
		AddArrayDialogFragment frag = new AddArrayDialogFragment();

		Set<MovieItem> movies = new HashSet<>();
		while (movies.size() != 3) {
			int index = new Random().nextInt(MovieContent.ITEM_LIST.size());
			movies.add(MovieContent.ITEM_LIST.get(index));
		}
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(STATE_MOVIES, new ArrayList<>(movies));
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = getArguments().getParcelableArrayList(STATE_MOVIES);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_add_array);
		dialog.setTitle(R.string.title_dialog_add_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_single_btn);
		btn.setOnClickListener(new OnAddSingleClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.get(0).title);

		btn = (Button) dialog.findViewById(R.id.movies_collection_btn);
		btn.setOnClickListener(new OnAddCollectionClickListener());
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.get(1).title);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.get(2).title);

		btn = (Button) dialog.findViewById(R.id.movies_vararg_btn);
		btn.setOnClickListener(new OnAddVarargsClickListener());
		if (mEventListener != null) {
			btn.setVisibility(mEventListener.isAddVarargsEnabled() ? View.VISIBLE : View.GONE);
		}
		return dialog;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public boolean isAddVarargsEnabled();

		public void onAddMovieCollectionClick(List<MovieItem> movies);

		public void onAddSingleMovieClick(MovieItem movie);

		public void onAddVarargsMovieClick(MovieItem... movies);
	}

	private class OnAddCollectionClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAddMovieCollectionClick(mMovieItems);
			}
		}
	}

	private class OnAddSingleClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAddSingleMovieClick(mMovieItems.get(0));
			}
		}
	}

	private class OnAddVarargsClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAddVarargsMovieClick(mMovieItems.get(1), mMovieItems.get(2));
			}
		}
	}
}
