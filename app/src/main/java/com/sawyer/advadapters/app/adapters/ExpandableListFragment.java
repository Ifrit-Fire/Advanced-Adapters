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
import com.sawyer.advadapters.widget.BaseRolodexAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A quick implementation of a fragment that exposes the same required methods as a ListFragment,
 * expect for use as an ExpandableListView.
 */
public class ExpandableListFragment extends Fragment implements
		ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

	@InjectView(android.R.id.list) ExpandableListView mExpandableListView;
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
		if (mExpandableListView != null) mExpandableListView.setAdapter(adapter);
	}

	public ExpandableListView getListView() {
		return mExpandableListView;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
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

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		return false;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//In case adapter set before ListView inflated
		if (mAdapter != null) mExpandableListView.setAdapter(mAdapter);
		if (mAdapter instanceof BaseRolodexAdapter) {
			BaseRolodexAdapter adapter = (BaseRolodexAdapter) mAdapter;
			adapter.setOnGroupClickListener(this);
			adapter.setOnChildClickListener(this);
		} else {
			mExpandableListView.setOnGroupClickListener(this);
			mExpandableListView.setOnChildClickListener(this);
		}
	}
}
