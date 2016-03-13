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

package com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Demonstration on how to remove items from our adapter. There are two ways to do so; Via an
 * individual item or collection. Groups are forced to expand to help visually see the item removal.
 * The adapter's list is persisted/restored accordingly to ensure things like orientation change
 * doesn't wipe our list of items.
 */
public class RemoveItemsActivity extends ExpandableListActivity {
	private static final String STATE_LIST = "State List";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rolodex_two_buttons);
		ButterKnife.inject(this);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views

		//Set the button text
		Button button = ButterKnife.findById(this, android.R.id.button1);
		button.setText(R.string.btn_remove_first);
		button = ButterKnife.findById(this, android.R.id.button2);
		button.setText(R.string.btn_remove_first2);

		//Create our adapter and set it. By default groups will be sorted
		if (savedInstanceState == null) {
			setListAdapter(new DemoAdapter(this, MovieContent.ITEM_LIST));
		} else {
			ArrayList<MovieItem> list = savedInstanceState.getParcelableArrayList(STATE_LIST);
			setListAdapter(new DemoAdapter(this, list));
		}
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
	}

	@OnClick(android.R.id.button1)
	public void onRemoveFirst(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		if (adapter.getGroupCount() == 0) {
			return;    //No more items to remove
		}
		MovieItem movie = adapter.getChild(0, 0);
		adapter.remove(movie);
	}

	@OnClick(android.R.id.button2)
	public void onRemoveFirstTwo(View v) {
		//Lets pick the first two movies from the list, and remove them
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		List<MovieItem> movies = adapter.getList();
		if (movies.size() < 2) {
			return;    //No more items to remove
		}
		movies = movies.subList(0, 2);
		adapter.removeAll(movies);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		outState.putParcelableArrayList(STATE_LIST, adapter.getList());
	}

	private class DemoAdapter extends RolodexArrayAdapter<Integer, MovieItem> {
		public DemoAdapter(Context activity, List<MovieItem> movies) {
			super(activity, movies);
		}

		@NonNull
		@Override
		public Integer createGroupFor(MovieItem childItem) {
			return childItem.year;
		}

		@NonNull
		@Override
		public View getChildView(@NonNull LayoutInflater inflater, int groupPosition,
								 int childPosition, boolean isLastChild, View convertView,
								 @NonNull ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_child1, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getChild(groupPosition, childPosition).title);
			return convertView;
		}

		@NonNull
		@Override
		public View getGroupView(@NonNull LayoutInflater inflater, int groupPosition,
								 boolean isExpanded, View convertView, @NonNull ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_group1, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getGroup(groupPosition).toString());
			return convertView;
		}

		@Override
		public boolean hasAutoExpandingGroups() {
			//Auto expand so user can more easily see what's happening
			return true;
		}

		@Override
		protected boolean isChildFilteredOut(MovieItem childItem,
											 @NonNull CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}

		@Override
		protected boolean isGroupFilteredOut(Integer groupItem, @NonNull CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}
	}
}
