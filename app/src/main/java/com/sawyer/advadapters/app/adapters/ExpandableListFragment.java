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
package com.sawyer.advadapters.app.adapters;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.sawyer.advadapters.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A quick implementation of a fragment that exposes the same required methods as a ListFragment,
 * expect for use as an ExpandableListView.
 */
public class ExpandableListFragment extends Fragment {

	@InjectView(android.R.id.list) ExpandableListView mList;
	private ExpandableListAdapter mAdapter;

	public ExpandableListFragment() {
	}

	/**
	 * Get the ListAdapter associated with this activity's ListView.
	 */
	public ExpandableListAdapter getListAdapter() {
		return mAdapter;
	}

	/**
	 * Provide the cursor for the list view.
	 */
	public void setListAdapter(ExpandableListAdapter adapter) {
		mAdapter = adapter;
		if (mList != null) mList.setAdapter(adapter);
	}

	public ExpandableListView getListView() {
		return mList;
	}

	/**
	 * Callback method to be invoked when a child in this expandable list has been clicked.
	 *
	 * @param parent        The ExpandableListView where the click happened
	 * @param v             The view within the expandable list/ListView that was clicked
	 * @param groupPosition The group position that contains the child that was clicked
	 * @param childPosition The child position within the group
	 * @param id            The row id of the child that was clicked
	 *
	 * @return True if the click was handled
	 */
	public boolean onChildItemClick(ExpandableListView parent, View v, int groupPosition,
									int childPosition, long id) {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_expandable_list, container, false);
		ButterKnife.inject(this, v);
		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}

	/**
	 * Callback method to be invoked when a group in this expandable list has been clicked.
	 *
	 * @param parent        The ExpandableListConnector where the click happened
	 * @param v             The view within the expandable list/ListView that was clicked
	 * @param groupPosition The group position that was clicked
	 * @param id            The row id of the group that was clicked
	 *
	 * @return True if the click was handled
	 */
	public boolean onGroupItemClick(ExpandableListView parent, View v, int groupPosition, long id) {
		return false;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//In case adapter set before ListView inflated
		if (mAdapter != null) mList.setAdapter(mAdapter);
		mList.setOnGroupClickListener(new OnInternalGroupClickListener());
		mList.setOnChildClickListener(new OnInternalChildClickListener());
	}

	private class OnInternalChildClickListener implements ExpandableListView.OnChildClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
									int childPosition, long id) {
			return onChildItemClick(parent, v, groupPosition, childPosition, id);
		}
	}

	private class OnInternalGroupClickListener implements ExpandableListView.OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
									long id) {
			return onGroupItemClick(parent, v, groupPosition, id);
		}
	}
}
