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

package com.sawyer.advadapters.app.adapters.nfrolodexarrayadapter.fulldemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.ExpandableListFragment;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.PatchedExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FullDemoFragment extends ExpandableListFragment {
	private static final String STATE_LIST = "State List";

	private EventListener mEventListener;

	public static FullDemoFragment newInstance() {
		return new FullDemoFragment();
	}

	@Override
	public FullDemoAdapter getListAdapter() {
		return (FullDemoAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ExpandableListAdapter adapter) {
		if (adapter instanceof FullDemoAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " + FullDemoAdapter.class.getSimpleName());
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
		//Only modal should update child click.  All others should just cause activation state change
		if (!mEventListener.getChoiceMode().isModal())
			return false;

		//Granted, making a whole new instance is not even necessary here.
		//However I wanted to demonstrate updating with an entirely different instance.
		MovieItem oldMovie = getListAdapter().getChild(groupPosition, childPosition);
		MovieItem newMovie = new MovieItem(oldMovie.barcode());
		newMovie.title = new StringBuilder(oldMovie.title).reverse().toString();

		//Test relocating to a new group
		//String year = new StringBuilder(String.valueOf(oldMovie.year)).reverse().toString();
		//if (year.charAt(0) == '0') year += "0";  //In case there are leading zeroes
		//newMovie.year = Integer.valueOf(year);

		//Test keeping in same group
		newMovie.year = oldMovie.year;

		newMovie.isRecommended = !oldMovie.isRecommended;
		getListAdapter().update(groupPosition, childPosition, newMovie);
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (savedInstanceState != null) {
			ArrayList<MovieItem> list = savedInstanceState.getParcelableArrayList(STATE_LIST);
			setListAdapter(new FullDemoAdapter(getActivity(), list));
		} else {
			setListAdapter(new FullDemoAdapter(getActivity()));
		}
		PatchedExpandableListAdapter.ChoiceMode choiceMode = mEventListener.getChoiceMode();
		if (choiceMode.isModal())
			getListAdapter().setMultiChoiceModeListener(new DemoChoiceModeListener());
		getListAdapter().setChoiceMode(choiceMode);
		return v;
	}

	private void onRemoveItemsClicked(List<MovieItem> items) {
		//Easy way to test both remove methods
		if (items.size() == 1) {
			getListAdapter().remove(items.iterator().next());
		} else {
			getListAdapter().removeAll(items);
		}
	}

	private void onRetainItemsClicked(List<MovieItem> items) {
		getListAdapter().retainAll(items);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(STATE_LIST, getListAdapter().getList());
	}

	public interface EventListener {
		PatchedExpandableListAdapter.ChoiceMode getChoiceMode();

		void onAdapterCountUpdated();
	}

	/**
	 * Demonstration on how to use modal {@link com.sawyer.advadapters.widget.PatchedExpandableListAdapter.ChoiceMode}
	 * with the rolodex adapter. When using the rolodex adapter, the adapter itself takes ownership
	 * of setting choice mode. Setting it on the ExpandableListView itself will cause the app to
	 * crash.
	 * <p/>
	 * The PatchedExpandableListAdapter implemented it's own solution which behaves exactly as the
	 * {@link android.widget.AbsListView#setChoiceMode(int)} would. Ensure you not only set the
	 * ChoiceMode with the adapter but also set the custom {@link com.sawyer.advadapters.widget.PatchedExpandableListAdapter.ChoiceModeListener}.
	 */
	private class DemoChoiceModeListener implements
			PatchedExpandableListAdapter.ChoiceModeListener {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;
			Integer[] groupPositions;
			Long[] packedPositions;
			List<MovieItem> movies;

			switch (item.getItemId()) {
			case R.id.menu_context_remove:
				packedPositions = getListAdapter().getCheckedChildPositions();
				movies = new ArrayList<>(packedPositions.length);
				for (Long packedPos : packedPositions) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
					int childPosition = ExpandableListView.getPackedPositionChild(packedPos);
					movies.add(getListAdapter().getChild(groupPosition, childPosition));
				}
				if (movies.size() == 0) {    //Will only occur for single_modal mode
					groupPositions = getListAdapter().getCheckedGroupPositions();
					for (Integer groupPos : groupPositions) {
						movies.addAll(getListAdapter().getGroupChildren(groupPos));
					}
				}
				onRemoveItemsClicked(movies);
				mode.finish();
				result = true;
				break;

			case R.id.menu_context_retain:
				packedPositions = getListAdapter().getCheckedChildPositions();
				movies = new ArrayList<>(packedPositions.length);
				for (Long packedPos : packedPositions) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
					int childPosition = ExpandableListView.getPackedPositionChild(packedPos);
					movies.add(getListAdapter().getChild(groupPosition, childPosition));
				}
				if (movies.size() == 0) {    //Will only occur for single_modal mode
					groupPositions = getListAdapter().getCheckedGroupPositions();
					for (Integer groupPos : groupPositions) {
						movies.addAll(getListAdapter().getGroupChildren(groupPos));
					}
				}
				onRetainItemsClicked(movies);
				mode.finish();
				result = true;
				break;

			default:
				result = false;
				break;
			}

			if (result && mEventListener != null) {
				mEventListener.onAdapterCountUpdated();
			}
			return result;
		}

		@Override
		public void onChildCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, int childPosition, long childId,
											   boolean checked) {
			mode.setTitle(
					getListAdapter().getCheckedChildCount() + getString(R.string.desc_selected));
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_array, menu);
			mode.setTitle(
					getListAdapter().getCheckedChildCount() + getString(R.string.desc_selected));
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onGroupCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, boolean checked) {
			//If group is expanded, then the onChildCheckedStateChanged method will be invoked. Which
			//means it'll safely take care of updating our screen.
			if (getExpandableListView().isGroupExpanded(groupPosition))
				return;
			mode.setTitle(
					getListAdapter().getCheckedChildCount() + getString(R.string.desc_selected));
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
