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
import android.widget.Button;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Demonstration on how to conveniently expand and collapse all groups in the ExpandableListView.
 * Though expanding/collapsing is normally done through the ExpandableListView, these convenience
 * methods are in fact found in the adapter.
 */
public class ExpandCollapseAllActivity extends ExpandableListActivity {

	@OnClick(android.R.id.button2)
	public void onCollapseAll(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.collapseAll();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rolodex_two_buttons);
		ButterKnife.inject(this);

		//Set the button text
		Button button = ButterKnife.findById(this, android.R.id.button1);
		button.setText(R.string.btn_expand);
		button = ButterKnife.findById(this, android.R.id.button2);
		button.setText(R.string.btn_collapse);

		//Create our adapter and set it. By default groups will be sorted
		DemoAdapter adapter = new DemoAdapter(this, MovieContent.ITEM_LIST);
		setListAdapter(adapter);

		//Only on first start up, expand all groups. Otherwise, allow ExpandableListView to
		//appropriately reload which groups were expanded or collapsed
		if (savedInstanceState == null) adapter.expandAll();
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
	}

	@OnClick(android.R.id.button1)
	public void onExpandAll(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.expandAll();
	}

	private class DemoAdapter extends RolodexArrayAdapter<Integer, MovieItem> {

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
				convertView = inflater.inflate(R.layout.item_expandable_group2, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getGroup(groupPosition).toString());
			return convertView;
		}

		@Override
		protected boolean isChildFilteredOut(MovieItem childItem, CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}

		@Override
		protected boolean isGroupFilteredOut(Integer groupItem, CharSequence constraint) {
			//Not worried about filtering for this demo
			return false;
		}
	}
}
