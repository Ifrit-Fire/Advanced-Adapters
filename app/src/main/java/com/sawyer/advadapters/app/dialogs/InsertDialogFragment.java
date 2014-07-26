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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.Arrays;
import java.util.List;

/**
 * Renders a dialog with all the options possible for inserting items into one of the adapters.
 * Implement the {@link EventListener} interface in order to receive back the dialog results.
 */
public class InsertDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIE_1 = "State Movie 1";
	private static final String STATE_MOVIE_2 = "State Movie 2";

	private EventListener mEventListener;

	private MovieItem[] mMovieItems;

	public static InsertDialogFragment newInstance(MovieItem movie1, MovieItem movie2) {
		InsertDialogFragment frag = new InsertDialogFragment();

		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_MOVIE_1, movie1);
		bundle.putParcelable(STATE_MOVIE_2, movie2);
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = new MovieItem[2];
		mMovieItems[0] = getArguments().getParcelable(STATE_MOVIE_1);
		mMovieItems[1] = getArguments().getParcelable(STATE_MOVIE_2);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_insert);
		dialog.setTitle(R.string.title_dialog_insert_movies);

		ViewGroup vg = (ViewGroup) dialog.findViewById(R.id.movie_single_button_bar);
		Button btn = (Button) vg.findViewById(R.id.movie_insert_start_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.START));
		btn = (Button) vg.findViewById(R.id.movie_insert_random_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.RANDOM));
		btn = (Button) vg.findViewById(R.id.movie_insert_end_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.END));
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems[0].title);

		vg = (ViewGroup) dialog.findViewById(R.id.movie_collection_button_bar);
		btn = (Button) vg.findViewById(R.id.movie_insert_start_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.START));
		btn = (Button) vg.findViewById(R.id.movie_insert_random_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.RANDOM));
		btn = (Button) vg.findViewById(R.id.movie_insert_end_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.END));
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems[0].title);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems[1].title);

		return dialog;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public enum InsertLocation {
		START,
		RANDOM,
		END
	}

	public interface EventListener {
		public void onInsertMovieCollectionClick(List<MovieItem> movies, InsertLocation insertLocation);

		public void onInsertSingleMovieClick(MovieItem movie, InsertLocation insertLocation);
	}

	private class OnInsertCollectionClickListener implements View.OnClickListener {
		private InsertLocation mInsertLocation;

		public OnInsertCollectionClickListener(InsertLocation insertLocation) {
			mInsertLocation = insertLocation;
		}

		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onInsertMovieCollectionClick(Arrays.asList(mMovieItems), mInsertLocation);
			}
		}
	}

	private class OnInsertSingleClickListener implements View.OnClickListener {
		private InsertLocation mInsertLocation;

		public OnInsertSingleClickListener(InsertLocation insertLocation) {
			mInsertLocation = insertLocation;
		}

		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener.onInsertSingleMovieClick(mMovieItems[0], mInsertLocation);
			}
		}
	}
}
