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
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AndroidExpandableFragment extends ExpandableListFragment {
	private static final String STATE_GROUP_LIST = "State Group List";
	private static final String STATE_CHILD_LIST = "State Child List";

	private String[] mGroupKeys = {"KEY_YEAR"};
	private String[] mChildKeys = {MovieItem.MAP_KEYS[0], MovieItem.MAP_KEYS[1]};
	private int[] mGroupIds = {android.R.id.text1};
	private int[] mChildIds = {android.R.id.text1, android.R.id.text2};

	public static AndroidExpandableFragment newInstance() {
		return new AndroidExpandableFragment();
	}

	/**
	 * Just when you thought the {@link #getGroupData(List)} was getting complicated, we go one step
	 * further. This converts our List of movies into the data structure required by the {@link
	 * android.widget.SimpleExpandableListAdapter}. Totally makes sense right?
	 */
	private List<List<Map<String, String>>> getChildData(List<MovieItem> movies) {
		//Build a mapping of movies that belong to each year
		Map<Integer, List<Map<String, String>>> yearToMovies = new HashMap<>();
		for (MovieItem movie : movies) {
			List<Map<String, String>> movieList = yearToMovies.get(movie.year);
			if (movieList == null) {
				movieList = new ArrayList<>();
				yearToMovies.put(movie.year, movieList);
			}
			movieList.add(movie.toMap());
		}

		//Build our final list which groups lists of movies by year within a greater list.
		//Are you confused yet?
		List<List<Map<String, String>>> childData = new ArrayList<>();
		for (List<Map<String, String>> movieList : yearToMovies.values()) {
			childData.add(movieList);
		}
		return childData;
	}

	/**
	 * Given a list of movies, builds the required List of Maps based on the found movie years. This
	 * will go on to act as our group data required by the {@link android.widget.SimpleExpandableListAdapter}.
	 */
	private List<Map<String, String>> getGroupData(List<MovieItem> movies) {
		//Lets first find all the movie years we need
		Set<String> years = new HashSet<>();
		for (MovieItem movie : movies) {
			years.add(String.valueOf(movie.year));
		}

		//Build a map for each year, then add to group listing
		List<Map<String, String>> groupData = new ArrayList<>();
		for (String year : years) {
			Map<String, String> map = new HashMap<>();
			map.put(mGroupKeys[0], year);
			groupData.add(map);
		}

		return groupData;
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
								int childPosition, long id) {
		//While this will modify the adapter's data...it's not really recommended.
		Map<String, String> movieMap = (Map<String, String>) getListAdapter()
				.getChild(groupPosition, childPosition);
		String newTitle = new StringBuilder(movieMap.get(MovieItem.KEY_TITLE)).reverse().toString();
		movieMap.put(MovieItem.KEY_TITLE, newTitle);
		getListAdapter().notifyDataSetChanged();
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		List<Map<String, String>> groupData;
		List<List<Map<String, String>>> childData;
		if (savedInstanceState != null) {
			groupData = (List<Map<String, String>>) savedInstanceState.getSerializable(
					STATE_GROUP_LIST);
			childData = (List<List<Map<String, String>>>) savedInstanceState.getSerializable(
					STATE_CHILD_LIST);
		} else {
			groupData = getGroupData(MovieContent.ITEM_LIST);
			childData = getChildData(MovieContent.ITEM_LIST);
		}
		//Yup this isn't confusing or complicated. Not one bit.
		setListAdapter(new AndroidExpandableAdapter(getActivity(),
													groupData,
													android.R.layout.simple_expandable_list_item_1,
													mGroupKeys, mGroupIds,
													childData,
													android.R.layout.simple_expandable_list_item_2,
													mChildKeys, mChildIds));
		getExpandableListView().setChoiceMode(RolodexAdapter.CHOICE_MODE_MULTIPLE_MODAL);
		getExpandableListView().setMultiChoiceModeListener(new OnCabMultiChoiceModeListener());
		return v;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		List<Map<String, String>> groupData = new ArrayList<>();
		List<List<Map<String, String>>> childData = new ArrayList<>();
		int groupCount = getListAdapter().getGroupCount();
		for (int index = 0; index < groupCount; ++index) {
			groupData.add((Map<String, String>) getListAdapter().getGroup(index));
			int childCount = getListAdapter().getChildrenCount(index);
			List<Map<String, String>> groupChildren = new ArrayList<>();
			for (int ind = 0; ind < childCount; ++ind) {
				groupChildren.add((Map<String, String>) getListAdapter().getChild(index, ind));
			}
			childData.add(groupChildren);
		}
		outState.putSerializable(STATE_GROUP_LIST, (Serializable) groupData);
		outState.putSerializable(STATE_CHILD_LIST, (Serializable) childData);
	}

	public void reset() {
		//Can anyone say, convoluted constructor?
		setListAdapter(new AndroidExpandableAdapter(getActivity(),
													getGroupData(MovieContent.ITEM_LIST),
													android.R.layout.simple_expandable_list_item_1,
													mGroupKeys, mGroupIds,
													getChildData(MovieContent.ITEM_LIST),
													android.R.layout.simple_expandable_list_item_2,
													mChildKeys, mChildIds));
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
