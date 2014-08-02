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
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
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
 * Renders a dialog with all the options possible for inserting items into one of the adapters.
 * Implement the {@link EventListener} interface in order to receive back the dialog results.
 */
public class InsertArrayDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";
	private static final String STATE_ENABLE_INSERT_ALL = "State Enable Insert All";

	private EventListener mEventListener;

	private boolean mIsInsertAllEnabled;
	private List<MovieItem> mMovieItems;

	public static InsertArrayDialogFragment newInstance() {
		InsertArrayDialogFragment frag = new InsertArrayDialogFragment();

		Set<MovieItem> movies = new HashSet<>();
		while (movies.size() != 3) {
			int index = new Random().nextInt(MovieContent.ITEM_LIST.size());
			movies.add(MovieContent.ITEM_LIST.get(index));
		}
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(STATE_MOVIES, new ArrayList<Parcelable>(movies));
		frag.setArguments(bundle);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = getArguments().getParcelableArrayList(STATE_MOVIES);
		mIsInsertAllEnabled = getArguments().getBoolean(STATE_ENABLE_INSERT_ALL, true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_insert_array);
		dialog.setTitle(R.string.title_dialog_insert_movies);

		ViewGroup vg = (ViewGroup) dialog.findViewById(R.id.movie_single_button_bar);
		Button btn = (Button) vg.findViewById(R.id.movie_insert_start_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.START));
		btn = (Button) vg.findViewById(R.id.movie_insert_random_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.RANDOM));
		btn = (Button) vg.findViewById(R.id.movie_insert_end_btn);
		btn.setOnClickListener(new OnInsertSingleClickListener(InsertLocation.END));
		TextView tv = (TextView) dialog.findViewById(R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.get(0).title);

		int visibility = mIsInsertAllEnabled ? View.VISIBLE : View.GONE;
		vg = (ViewGroup) dialog.findViewById(R.id.movie_collection_button_bar);
		vg.setVisibility(visibility);
		btn = (Button) vg.findViewById(R.id.movie_insert_start_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.START));
		btn = (Button) vg.findViewById(R.id.movie_insert_random_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.RANDOM));
		btn = (Button) vg.findViewById(R.id.movie_insert_end_btn);
		btn.setOnClickListener(new OnInsertCollectionClickListener(InsertLocation.END));
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.get(1).title);
		tv = (TextView) dialog.findViewById(R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.get(2).title);
		((View) tv.getParent()).setVisibility(visibility);


		return dialog;
	}

	public void setEnableInsertAll(boolean enable) {
		getArguments().putBoolean(STATE_ENABLE_INSERT_ALL, enable);
		mIsInsertAllEnabled = enable;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public enum InsertLocation {
		START,
		RANDOM,
		END;

		public int toListPosition(int listCount) {
			int position;

			switch (this) {
			case RANDOM:
				//Trying to Random 0 will crash
				position = (listCount == 0 ? 0 : new Random().nextInt(listCount));
				break;
			case END:
				position = listCount;
				break;
			case START:
			default:
				position = 0;
				break;
			}
			return position;
		}
	}

	public interface EventListener {
		public void onInsertMultipleMoviesClick(List<MovieItem> movies, InsertLocation location);

		public void onInsertSingleMovieClick(MovieItem movie, InsertLocation location);
	}

	private class OnInsertCollectionClickListener implements View.OnClickListener {
		private InsertLocation mInsertLocation;

		public OnInsertCollectionClickListener(InsertLocation insertLocation) {
			mInsertLocation = insertLocation;
		}

		@Override
		public void onClick(View v) {
			if (mEventListener != null) {
				mEventListener
						.onInsertMultipleMoviesClick(mMovieItems.subList(1, 2), mInsertLocation);
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
				mEventListener.onInsertSingleMovieClick(mMovieItems.get(0), mInsertLocation);
			}
		}
	}
}
