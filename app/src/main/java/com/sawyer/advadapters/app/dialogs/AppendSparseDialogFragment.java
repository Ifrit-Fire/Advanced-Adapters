/*
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.Random;

/**
 * Renders a dialog with all the options possible for appending items into a SparseArray. Implement
 * the {@link EventListener} in order to receive back dialog results.
 */
public class AppendSparseDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";

	private EventListener mEventListener;

	private SparseArray<MovieItem> mMovieItems;

	public static AppendSparseDialogFragment newInstance() {
		AppendSparseDialogFragment frag = new AppendSparseDialogFragment();

		SparseArray<MovieItem> movies = new SparseArray<>();
		while (movies.size() != 3) {
			int index = new Random().nextInt(MovieContent.ITEM_SPARSE.size());
			MovieItem movie = MovieContent.ITEM_SPARSE.valueAt(index);
			movies.put(movie.barcode(), movie);
		}
		Bundle bundle = new Bundle();
		bundle.putSparseParcelableArray(STATE_MOVIES, movies);
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = getArguments().getSparseParcelableArray(STATE_MOVIES);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_append_sparse);
		dialog.setTitle(R.string.title_dialog_append_movies);

		Button btn = (Button) dialog.findViewById(R.id.movie_append_id_btn);
		btn.setOnClickListener(new OnAppendMovieWithIdClickListener());
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.valueAt(0).title);

		btn = (Button) dialog.findViewById(R.id.movies_append_all_btn);
		btn.setOnClickListener(new OnAppendAllMoviesClickListener());
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.valueAt(1).title);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.valueAt(2).title);

		return dialog;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onAppendAllMoviesClick(SparseArray<MovieItem> movies);

		public void onAppendMovieWithIdClick(int barcode, MovieItem movieItem);
	}

	private class OnAppendAllMoviesClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mMovieItems.removeAt(0);
				mEventListener.onAppendAllMoviesClick(mMovieItems);
			}
		}
	}

	private class OnAppendMovieWithIdClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onAppendMovieWithIdClick(mMovieItems.keyAt(0),
														mMovieItems.valueAt(0));
			}
		}
	}
}
