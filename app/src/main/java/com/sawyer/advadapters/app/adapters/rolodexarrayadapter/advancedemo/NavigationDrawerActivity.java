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

package com.sawyer.advadapters.app.adapters.rolodexarrayadapter.advancedemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Demonstration on how to use a rolodex adapter with the navigation drawer. When using the rolodex
 * adapter, the adapter itself takes ownership of the click listeners...even when using the
 * navigation drawer. That means you must set your group and child listeners on the adapter instead
 * of the ExpandableListView.
 * <p/>
 * In order to keep the selected navigation drawer item highlighted, choice mode must be set. When
 * using the rolodex adapter, the adapter itself takes ownership of setting choice mode. Setting it
 * on the ExpandableListView itself will cause the app to crash.
 * <p/>
 * ActionBarDrawerToggle v7 is used as the v4 equivalent was recently deprecated. In order to
 * support ICS and JB MR1 correctly, this activity must be an ActionBarActivity. Otherwise, using a
 * standard Activity will work just fine for JB MR2 and up.
 */
public class NavigationDrawerActivity extends ActionBarActivity implements
		ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

	@InjectView(android.R.id.list)
	ExpandableListView mDrawerExpandableList;
	@InjectView(R.id.nav_drawer)
	DrawerLayout mDrawerLayout;
	@InjectView(android.R.id.text1)
	TextView mTextView;

	private DemoAdapter mDrawerAdapter;
	private ActionBarDrawerToggle mDrawerToggle;

	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar == null) return;
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}

	private void initNavDrawer() {
		mDrawerAdapter = new DemoAdapter(this, MovieContent.ITEM_LIST.subList(0, 9));
		mDrawerAdapter.setOnChildClickListener(this);
		mDrawerAdapter.setOnGroupClickListener(this);
		mDrawerAdapter.setChoiceMode(DemoAdapter.ChoiceMode.SINGLE);
		mDrawerExpandableList.setAdapter(mDrawerAdapter);
		mDrawerToggle = new DrawerToggle();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
								int childPosition, long id) {
		mDrawerLayout.closeDrawers();
		MovieItem movie = mDrawerAdapter.getChild(groupPosition, childPosition);
		mTextView.setText(movie.title);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation_drawer);
		ButterKnife.inject(this);
		initActionBar();
		initNavDrawer();
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		//Lets show a toast of the group clicked.
		String group = mDrawerAdapter.getGroup(groupPosition);
		ToastHelper.showGroupClicked(this, group);
		return false;    //Returning true prevents group expansion.  Returning false still allows groups to expand.
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	private class DemoAdapter extends RolodexArrayAdapter<String, MovieItem> {
		public DemoAdapter(Context activity, List<MovieItem> movies) {
			super(activity, movies);
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
				convertView = inflater.inflate(R.layout.item_expandable_group2, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getGroup(groupPosition));
			return convertView;
		}

		@Override
		public boolean hasAutoExpandingGroups() {
			//Can be false as well.  Depends on how you want your nav drawer items to behave
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

	private class DrawerToggle extends ActionBarDrawerToggle {

		public DrawerToggle() {
			super(NavigationDrawerActivity.this, mDrawerLayout, R.string.title_group_advanced_demos,
				  R.string.activity_rolodex_navigation_drawer);
		}
	}
}
