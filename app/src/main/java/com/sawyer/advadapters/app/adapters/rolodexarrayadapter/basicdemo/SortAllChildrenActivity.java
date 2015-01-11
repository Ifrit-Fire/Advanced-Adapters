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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Demonstration on how to sort all child items in our adapter. Groups are forced to expand to help
 * visually see the sorting change. Orientation changes will reset the unsorted list so as to retest
 * the sort feature. Aka, savedInstanceState is ignored. The list of items are purposely shuffled to
 * show how groups themselves are not sorted.
 */
public class SortAllChildrenActivity extends ExpandableListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rolodex_text_button);
		ButterKnife.inject(this);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views

		//Set the widget text
		TextView tv = ButterKnife.findById(this, android.R.id.button1);
		tv.setText(R.string.btn_sort_children);
		tv = ButterKnife.findById(this, android.R.id.text1);
		tv.setText(R.string.desc_sortall_ignores_groups);

		//Create our adapter and set it. By default groups will be sorted.
		//Shuffle to help demonstrate how groups will not sort.
		List<MovieItem> movies = new ArrayList<>(MovieContent.ITEM_LIST);
		Collections.shuffle(movies);
		setListAdapter(new DemoAdapter(this, movies));
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
	}

	@OnClick(android.R.id.button1)
	public void onSortAllChildren(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.sort();    //MovieItem implements it's own Comparable interface.
	}

	private class DemoAdapter extends RolodexArrayAdapter<String, MovieItem> {
		public DemoAdapter(Context activity, List<MovieItem> movies) {
			super(activity, movies);
		}

		@Override
		public boolean areGroupsSorted() {
			//Prevents our groups from being sorted. Will show in insertion order instead.
			return false;
		}

		@Override
		public String createGroupFor(MovieItem childItem) {
			return String.valueOf(childItem.year);
		}

		@Override
		public View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_child1, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getChild(groupPosition, childPosition).title);
			return convertView;
		}

		@Override
		public View getGroupView(LayoutInflater inflater, int groupPosition, boolean isExpanded,
								 View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_group1, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getGroup(groupPosition));
			return convertView;
		}

		@Override
		public boolean hasAutoExpandingGroups() {
			//Auto expand so user can more easily see what's happening
			return true;
		}

		@Override
		protected boolean isChildFilteredOut(MovieItem childItem, CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}

		@Override
		protected boolean isGroupFilteredOut(String groupItem, CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}
	}
}
