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

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;

import org.json.JSONArray;
import org.json.JSONException;

public class UnitTestJSONArrayFragment extends ListFragment {
	private static final String STATE_CAB_CHECKED_COUNT = "State Cab Checked Count";
	private static final String STATE_LIST = "State List";

	private int mCheckedCount;
	private EventListener mEventListener;

	public static UnitTestJSONArrayFragment newInstance() {
		return new UnitTestJSONArrayFragment();
	}

	@Override
	public UnitTestMovieAdapter getListAdapter() {
		return (UnitTestMovieAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof UnitTestMovieAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " +
					UnitTestMovieAdapter.class.getSimpleName());
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
			mCheckedCount = savedInstanceState.getInt(STATE_CAB_CHECKED_COUNT);
			try {
				JSONArray list = new JSONArray(savedInstanceState.getString(STATE_LIST));
				setListAdapter(new UnitTestMovieAdapter(getActivity(), list));
			} catch (JSONException e) {
				e.printStackTrace();
				mCheckedCount = 0;
				setListAdapter(new UnitTestMovieAdapter(getActivity()));
			}
		} else {
			setListAdapter(new UnitTestMovieAdapter(getActivity()));
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
		ToastHelper.showItemUpdatesNotSupported(getActivity());
	}

	private void onRemoveItemsClicked() {
		ToastHelper.showRemoveNotSupported(getActivity());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LIST, getListAdapter().getJSONArray().toString());
		outState.putInt(STATE_CAB_CHECKED_COUNT, mCheckedCount);
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
			mode.setTitle(mCheckedCount + " Selected");
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCheckedCount = 0;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
			if (checked) {
				++mCheckedCount;
			} else {
				--mCheckedCount;
			}
			mode.setTitle(mCheckedCount + " Selected");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
