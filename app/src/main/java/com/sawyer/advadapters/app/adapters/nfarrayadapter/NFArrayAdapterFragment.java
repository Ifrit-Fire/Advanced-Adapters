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
package com.sawyer.advadapters.app.adapters.nfarrayadapter;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NFArrayAdapterFragment extends ListFragment {
	private static final String STATE_CAB_CHECKED_ITEMS = "State Cab Checked Items";
	private static final String STATE_LIST = "State List";

	private Set<MovieItem> mCheckedItems = new HashSet<>();
	private EventListener mEventListener;

	public static NFArrayAdapterFragment newInstance() {
		return new NFArrayAdapterFragment();
	}

	@Override
	public MovieNFArrayAdapter getListAdapter() {
		return (MovieNFArrayAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof MovieNFArrayAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " + MovieNFArrayAdapter.class.getSimpleName());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setAdapter(getListAdapter());
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			ArrayList<MovieItem> checkItems = savedInstanceState
					.getParcelableArrayList(STATE_CAB_CHECKED_ITEMS);
			mCheckedItems.addAll(checkItems);
			ArrayList<MovieItem> list = savedInstanceState.getParcelableArrayList(STATE_LIST);
			setListAdapter(new MovieNFArrayAdapter(getActivity(), list));
		} else {
			setListAdapter(new MovieNFArrayAdapter(getActivity()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ListView lv = (ListView) inflater.inflate(R.layout.listview, container, false);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(new OnCabMultiChoiceModeListener());
		return lv;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//Granted, making a whole new instance is not even necessary here.
		//However I wanted to demonstrate updating with an entirely different instance.
		MovieItem oldMovie = getListAdapter().getItem(position);
		MovieItem newMovie = new MovieItem(oldMovie.barcode());
		newMovie.title = new StringBuilder(oldMovie.title).reverse().toString();
		newMovie.year = oldMovie.year;
		newMovie.isRecommended = !oldMovie.isRecommended;
		getListAdapter().update(position, newMovie);
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
		outState.putSerializable(STATE_LIST, getListAdapter().getList());
		outState.putParcelableArrayList(STATE_CAB_CHECKED_ITEMS,
										new ArrayList<Parcelable>(mCheckedItems));
	}

	public interface EventListener {
		public void onAdapterCountUpdated();
	}

	private class OnCabMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
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
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
			if (checked) {
				mCheckedItems.add(getListAdapter().getItem(position));
			} else {
				mCheckedItems.remove(getListAdapter().getItem(position));
			}
			mode.setTitle(mCheckedItems.size() + " Selected");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
