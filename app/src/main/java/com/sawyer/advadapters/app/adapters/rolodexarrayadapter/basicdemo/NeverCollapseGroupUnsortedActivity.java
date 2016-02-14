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
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Demonstration on how to make a our ExpandableListView show as always expanded. This would be the
 * similar look one sees with setting screens. Rolodex adapters will always auto sort groups, this
 * demo specifically shows how to change that behavior.
 */
public class NeverCollapseGroupUnsortedActivity extends ExpandableListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views

		//Shuffle the list to help show how groups are no longer being sorted
		List<MovieItem> movies = new ArrayList<>(MovieContent.ITEM_LIST);
		Collections.shuffle(movies);

		//Create our adapter and set it.
		DemoAdapter adapter = new DemoAdapter(this, movies);
		setListAdapter(adapter);
	}

	private class DemoAdapter extends RolodexArrayAdapter<Integer, MovieItem> {

		public DemoAdapter(Context activity, Collection<MovieItem> items) {
			super(activity, items);
		}

		@Override
		public boolean areGroupsSorted() {
			//Prevents our groups from being sorted. Will show in insertion order instead.
			return false;
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
			//This forces our group views to always render expanded.
			//Even attempting to programmatically collapse a group will not work.
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

		@Override
		public boolean isGroupSelectable(int groupPosition) {
			//This prevents a user from seeing any touch feedback when a group is clicked.
			//Even if this method returned true, selecting a group will not force it closed.
			return false;
		}
	}
}
