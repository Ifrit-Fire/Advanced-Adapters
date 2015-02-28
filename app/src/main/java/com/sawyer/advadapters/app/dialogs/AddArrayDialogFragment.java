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

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Renders a dialog with all the options possible for adding movie items to a list. Implement the
 * {@link EventListener} in order to receive back dialog results. Toggling the Varargs option on and
 * off is possible through the listener. For use specifically with array based adapters.
 */
public class AddArrayDialogFragment extends CustomDialogFragment {
	private static final String STATE_MOVIES = "State Movies";
	private static final String STATE_ENABLE_ARGVARGS = "State Enable Argvargs";

	private EventListener mEventListener;

	private boolean mIsArgvargsEnabled;
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

	@OnClick(R.id.movies_collection_btn)
	public void onAddCollectionClick(View v) {
		if (mEventListener != null) {
			mEventListener.onAddMultipleMoviesClick(mMovieItems.subList(1, 3));
		}
	}

	@OnClick(R.id.movie_single_btn)
	public void onAddSingleMovieClick(View v) {
		if (mEventListener != null) {
			mEventListener.onAddSingleMovieClick(mMovieItems.get(0));
		}
	}

	@OnClick(R.id.movies_vararg_btn)
	public void onAddVarargsClick(View v) {
		if (mEventListener != null) {
			mEventListener.onAddVarargsMovieClick(mMovieItems.get(1), mMovieItems.get(2));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMovieItems = getArguments().getParcelableArrayList(STATE_MOVIES);
		mIsArgvargsEnabled = getArguments().getBoolean(STATE_ENABLE_ARGVARGS, true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.dialog_add_array);
		dialog.setTitle(R.string.title_dialog_add_movies);
		ButterKnife.inject(this, dialog);

		TextView tv = ButterKnife.findById(dialog, R.id.movie_single_txt);
		tv.setText("- " + mMovieItems.get(0).title);
		tv = ButterKnife.findById(dialog, R.id.movie_multi_txt1);
		tv.setText("- " + mMovieItems.get(1).title);
		tv = ButterKnife.findById(dialog, R.id.movie_multi_txt2);
		tv.setText("- " + mMovieItems.get(2).title);

		Button btn = ButterKnife.findById(dialog, R.id.movies_vararg_btn);
		btn.setVisibility(mIsArgvargsEnabled ? View.VISIBLE : View.GONE);
		return dialog;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setEnableArgvargs(boolean enable) {
		getArguments().putBoolean(STATE_ENABLE_ARGVARGS, enable);
		mIsArgvargsEnabled = enable;
	}

	public void setEventListener(EventListener listener) {
		mEventListener = listener;
	}

	public interface EventListener {
		public void onAddMultipleMoviesClick(List<MovieItem> movies);

		public void onAddSingleMovieClick(MovieItem movie);

		public void onAddVarargsMovieClick(MovieItem... movies);
	}
}
