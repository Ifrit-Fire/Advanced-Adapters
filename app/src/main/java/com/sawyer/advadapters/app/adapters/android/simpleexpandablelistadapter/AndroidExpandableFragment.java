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
package com.sawyer.advadapters.app.adapters.android.simpleexpandablelistadapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.sawyer.advadapters.app.adapters.ExpandableListFragment;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexAdapter;

import java.util.ArrayList;

public class AndroidExpandableFragment extends ExpandableListFragment {
	private static final String STATE_LIST = "State List";

	private EventListener mEventListener;

	public static AndroidExpandableFragment newInstance() {
		return new AndroidExpandableFragment();
	}

	@Override
	public AndroidExpandableAdapter getListAdapter() {
		return (AndroidExpandableAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ExpandableListAdapter adapter) {
		if (adapter instanceof AndroidExpandableAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " + AndroidExpandableAdapter.class.getSimpleName());
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof EventListener) {
			mEventListener = (EventListener) activity;
		} else {
			throw new ClassCastException(
					"Activity must implement " + EventListener.class.getSimpleName());
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
								int childPosition, long id) {
		//TODO: implement
//		MovieItem oldMovie = getListAdapter().getChild(groupPosition, childPosition);
//		MovieItem newMovie = new MovieItem(oldMovie.barcode());
//		newMovie.title = new StringBuilder(oldMovie.title).reverse().toString();
//		newMovie.year = oldMovie.year;
//		newMovie.isRecommended = !oldMovie.isRecommended;
//		getListAdapter().update(position, newMovie);	//TODO: Implement
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (savedInstanceState != null) {
			ArrayList<MovieItem> list = savedInstanceState.getParcelableArrayList(STATE_LIST);
			//TODO: Figure out how to get MovieData into here
//			setListAdapter(new MovieAdapter(list));
		} else {
			//TODO: Figure out how to get MovieData into here
//			setListAdapter(new MovieAdapter());
		}
		getExpandableListView().setChoiceMode(RolodexAdapter.CHOICE_MODE_MULTIPLE_MODAL);
		getExpandableListView().setMultiChoiceModeListener(new OnCabMultiChoiceModeListener());
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//TODO: Create loop to pull all data out?
//		outState.putParcelableArrayList(STATE_LIST, getListAdapter().getList());
	}

	public interface EventListener {
		//TODO: Do I even need this?
	}

	private class OnCabMultiChoiceModeListener implements ListView.MultiChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//TODO: Show unsupported toast
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
