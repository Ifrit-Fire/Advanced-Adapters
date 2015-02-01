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

package com.sawyer.advadapters.app.adapters.nfrolodexarrayadapter.basicdemo;

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
import com.sawyer.advadapters.widget.NFRolodexArrayAdapter;

import java.util.Collection;

/**
 * Demonstration on how to make a our ExpandableListView show as always expanded. This would be the
 * similar look one sees with setting screens. Note, rolodex adapters will always auto sort groups.
 */
public class NeverCollapseGroupActivity extends ExpandableListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views

		//Create our adapter and set it. By default groups will be sorted
		DemoAdapter adapter = new DemoAdapter(this, MovieContent.ITEM_LIST);
		setListAdapter(adapter);
	}

	private class DemoAdapter extends NFRolodexArrayAdapter<Integer, MovieItem> {

		public DemoAdapter(Context activity, Collection<MovieItem> items) {
			super(activity, items);
		}

		@Override
		public Integer createGroupFor(MovieItem childItem) {
			return childItem.year;
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
		public boolean isGroupSelectable(int groupPosition) {
			//This prevents a user from seeing any touch feedback when a group is clicked.
			//Even if this method returned true, selecting a group will not force it closed.
			return false;
		}
	}
}
