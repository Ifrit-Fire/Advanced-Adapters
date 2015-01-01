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
package com.sawyer.advadapters.app.adapters.rolodexarrayadapter.fulldemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.sawyer.advadapters.widget.RolodexBaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FullDemoFragment extends ExpandableListFragment {
	private static final String STATE_CAB_CHECKED_ITEMS = "State Cab Checked Items";
	private static final String STATE_LIST = "State List";

	private Set<MovieItem> mCheckedItems = new HashSet<>();
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
			ArrayList<MovieItem> checkItems = savedInstanceState
					.getParcelableArrayList(STATE_CAB_CHECKED_ITEMS);
			mCheckedItems.addAll(checkItems);
			ArrayList<MovieItem> list = savedInstanceState.getParcelableArrayList(STATE_LIST);
			setListAdapter(new FullDemoAdapter(getActivity(), list));
		} else {
			setListAdapter(new FullDemoAdapter(getActivity()));
		}
		switch (mEventListener.getChoiceMode()) {
		case SINGLE_MODAL:
		case MULTIPLE_MODAL:
			getListAdapter().setMultiChoiceModeListener(new OnCabModalChoiceModeListener());
		default:
			//Do nothing else
		}
		getListAdapter().setChoiceMode(mEventListener.getChoiceMode());
		return v;
	}

	private void onRemoveItemsClicked(Set<MovieItem> items) {
		if (items.size() == 1) {
			getListAdapter().remove(items.iterator().next());
		} else {
			getListAdapter().removeAll(items);
		}
	}

	private void onRetainItemsClicked(Set<MovieItem> items) {
		getListAdapter().retainAll(items);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(STATE_LIST, getListAdapter().getList());
		outState.putParcelableArrayList(STATE_CAB_CHECKED_ITEMS,
										new ArrayList<Parcelable>(mCheckedItems));
	}

	public interface EventListener {
		public RolodexBaseAdapter.ChoiceMode getChoiceMode();

		public void onAdapterCountUpdated();
	}

	private class OnCabModalChoiceModeListener implements
			RolodexBaseAdapter.ModalChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;
			switch (item.getItemId()) {
			case R.id.menu_context_remove:
				onRemoveItemsClicked(mCheckedItems);
				mode.finish();
				result = true;
				break;

			case R.id.menu_context_retain:
				onRetainItemsClicked(mCheckedItems);
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
		public void onChildCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   int childPosition, long childId, boolean checked) {
			if (checked) {
				mCheckedItems.add(getListAdapter().getChild(groupPosition, childPosition));
			} else {
				mCheckedItems.remove(getListAdapter().getChild(groupPosition, childPosition));
			}
			mode.setTitle(mCheckedItems.size() + " Selected");
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_array, menu);
			mode.setTitle(mCheckedItems.size() + " Selected");
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCheckedItems.clear();
		}

		@Override
		public void onGroupCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   boolean checked) {
			//If group is expanded, then the onChildCheckedStateChanged method will be invoked. Which
			//means it'll safely take care of updating our screen.
			if (getExpandableListView().isGroupExpanded(groupPosition)) return;

			int childCount = getListAdapter().getChildrenCount(groupPosition);
			if (checked) {
				for (int index = 0; index < childCount; ++index) {
					mCheckedItems.add(getListAdapter().getChild(groupPosition, index));
				}
			} else {
				for (int index = 0; index < childCount; ++index) {
					mCheckedItems.remove(getListAdapter().getChild(groupPosition, index));
				}
			}
			mode.setTitle(mCheckedItems.size() + " Selected");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
