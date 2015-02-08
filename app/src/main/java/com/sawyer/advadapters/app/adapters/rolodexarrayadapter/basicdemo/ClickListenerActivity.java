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
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.List;

/**
 * Demonstration on how to set an ExpandableListView's child and group listeners. When using the
 * rolodex adapter, the adapter itself takes ownership of those listeners. That means you must set
 * your group and child listeners on the adapter instead of the ExpandableListView.
 */
public class ClickListenerActivity extends ExpandableListActivity implements
		ExpandableListView.OnGroupClickListener {
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
								int childPosition, long id) {
		//Lets show a toast of the movie clicked.
		MovieItem movie = (MovieItem) getExpandableListAdapter()
				.getChild(groupPosition, childPosition);
		ToastHelper.showMovieClicked(this, movie);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		Create our adapter and set it. By default groups will be sorted.
		Rolodex adapters will take ownership of the child and group listeners. That means manually
		setting the listeners on the ExpandableListView will not work. You must instead set the
		listeners with the adapter.
		*/
		DemoAdapter adapter = new DemoAdapter(this, MovieContent.ITEM_LIST);
		adapter.setOnChildClickListener(this);
		adapter.setOnGroupClickListener(this);
		setListAdapter(adapter);
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		//Lets show a toast of the group clicked.
		String group = getExpandableListAdapter().getGroup(groupPosition).toString();
		ToastHelper.showGroupClicked(this, group);
		return false;    //Returning true prevents group expansion.  Returning false still allows groups to expand.
	}

	private class DemoAdapter extends RolodexArrayAdapter<Integer, MovieItem> {
		public DemoAdapter(Context activity, List<MovieItem> movies) {
			super(activity, movies);
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
