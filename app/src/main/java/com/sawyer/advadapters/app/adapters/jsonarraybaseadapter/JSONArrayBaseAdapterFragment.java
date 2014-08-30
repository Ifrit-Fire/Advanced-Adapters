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
package com.sawyer.advadapters.app.adapters.jsonarraybaseadapter;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
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
import android.widget.Toast;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JSONArrayBaseAdapterFragment extends ListFragment {
	private static final String STATE_CAB_CHECKED_ITEMS = "State Cab Checked Items";
	private static final String STATE_LIST = "State List";

	private Set<Object> mCheckedItems = new HashSet<>();
	private EventListener mEventListener;

	public static JSONArrayBaseAdapterFragment newInstance() {
		return new JSONArrayBaseAdapterFragment();
	}

	@Override
	public MovieJSONArrayBaseAdapter getListAdapter() {
		return (MovieJSONArrayBaseAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof MovieJSONArrayBaseAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " +
					MovieJSONArrayBaseAdapter.class.getSimpleName());
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
			//mCheckedItems.addAll(checkItems);
			try {
				JSONArray list = new JSONArray(savedInstanceState.getString(STATE_LIST));
				setListAdapter(new MovieJSONArrayBaseAdapter(getActivity(), list));
			} catch (JSONException e) {
				e.printStackTrace();
				mCheckedItems.clear();
				setListAdapter(new MovieJSONArrayBaseAdapter(getActivity()));
			}
		} else {
			setListAdapter(new MovieJSONArrayBaseAdapter(getActivity()));
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
		try {
			JSONObject movie = getListAdapter().getItemJSONObject(position);
			StringBuilder builder = new StringBuilder(movie.getString(MovieItem.JSON_TITLE));
			movie.put(MovieItem.JSON_TITLE, builder.reverse().toString());
			Boolean isRecommended = movie.getBoolean(MovieItem.JSON_IS_RECOMMENDED);
			movie.put(MovieItem.JSON_IS_RECOMMENDED, !isRecommended);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			getListAdapter().notifyDataSetChanged();
		}
	}

	private void onRemoveItemsClicked() {
		Toast.makeText(getActivity(), R.string.toast_remove_not_supported, Toast.LENGTH_SHORT)
			 .show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LIST, getListAdapter().getJSONArray().toString());
//		outState.putParcelableArrayList(STATE_CAB_CHECKED_ITEMS,
//										new ArrayList<Parcelable>(mCheckedItems));
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
				onRemoveItemsClicked();
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
			inflater.inflate(R.menu.cab_jsonarray, menu);
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
