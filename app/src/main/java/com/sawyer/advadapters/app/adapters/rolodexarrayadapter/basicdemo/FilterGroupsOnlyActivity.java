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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.List;

/**
 * Demonstration on how to filter only groups with a rolodex adapter. Children items will be
 * ignored. The SearchView is inflated in the ActionBar. A query listener is implemented by the
 * Activity in order to send filter requests to the adapter when the user enters any. An action
 * expand listener is also required to clear out any filter constraints before closing the
 * SearchView.
 * <p/>
 * Note, When a group is filtered out, all it's children will automatically be filtered out. Only
 * when a group is not filtered out, can we decide on whether to filter out it's children.
 */
public class FilterGroupsOnlyActivity extends ExpandableListActivity implements
		SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views
		setListAdapter(new DemoAdapter(this, MovieContent.ITEM_LIST));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		MenuItem item = menu.findItem(R.id.menu_action_search);
		if (item != null) {
			SearchView searchView = (SearchView) item.getActionView();
			searchView.setOnQueryTextListener(this);
			item.setOnActionExpandListener(this);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		//When the searchview closes, we must clear out any previous filter operations so that
		//our adapter can reload all data to display into the ExpandableListView
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.getFilter().filter("");
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		adapter.getFilter().filter(query);
		return true;
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
			//We are purposely ignoring children for filtering in this demo.
			return false;
		}

		@Override
		protected boolean isGroupFilteredOut(Integer year, @NonNull CharSequence constraint) {
			//Lets filter out everything whose year does not match the numeric values in constraint.
			return !TextUtils.isDigitsOnly(constraint) || !year.toString().contains(constraint);
		}
	}
}
