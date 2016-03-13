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
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
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
public class NavigationDrawerActivity extends AppCompatActivity implements
		ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
	private static String STATE_ADAPTER_SAVED_STATE = "State Adapter Saved State";

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
		if (actionBar == null)
			return;
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

		//In case user reselected the same child...let's re-activate it.
		if (mDrawerAdapter.getCheckedChildCount() == 0)
			mDrawerAdapter.setChildChecked(groupPosition, childPosition, true);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation_drawer);
		ButterKnife.inject(this);
		initActionBar();
		initNavDrawer();

		if (savedInstanceState != null) {
			//Restore choice mode state and the activated item
			mDrawerAdapter.onRestoreInstanceState(
					savedInstanceState.getParcelable(STATE_ADAPTER_SAVED_STATE));

			//Lets figure out what item was activated and update our UI accordingly
			//Is a child item activated?
			Long[] checkedChildren = mDrawerAdapter.getCheckedChildPositions();
			if (checkedChildren.length == 1) {
				int childPosition = ExpandableListView.getPackedPositionChild(checkedChildren[0]);
				int groupPosition = ExpandableListView.getPackedPositionGroup(checkedChildren[0]);
				MovieItem movie = mDrawerAdapter.getChild(groupPosition, childPosition);
				mTextView.setText(movie.title);
			} else {
				//Is a group item activated?
				Integer[] checkedGroups = mDrawerAdapter.getCheckedGroupPositions();
				if (checkedGroups.length == 1) {
					String groupTitle = mDrawerAdapter.getGroup(checkedGroups[0]).toString();
					mTextView.setText(groupTitle);
				}
			}
		} else {
			//Lets pre-select a navigation drawer item.
			mDrawerAdapter.setGroupChecked(0, true);
			String groupTitle = mDrawerAdapter.getGroup(0).toString();
			mTextView.setText(groupTitle);
		}
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		mDrawerLayout.closeDrawers();
		Integer group = mDrawerAdapter.getGroup(groupPosition);
		mTextView.setText(group.toString());

		//In case user reselected the same group...let's re-activate it.
		if (mDrawerAdapter.getCheckedGroupCount() == 0)
			mDrawerAdapter.setGroupChecked(groupPosition, true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mDrawerToggle.syncState();
		return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		//DON"T FORGET TO PUT THIS GUY HERE!!! Or things will not work correctly.
		mDrawerToggle.syncState();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		/*
		In order to properly restore the activated items in the drawer, we must call into the adapter
		to save it's state. The adapter will return a parcelable for us to place in the bundle.
		It's important to note that the adapter will NOT place it's internal item data into this
		parcelable. You must still manually call and save the ArrayList returned with getList().
		*/
		if (mDrawerAdapter != null)
			outState.putParcelable(STATE_ADAPTER_SAVED_STATE, mDrawerAdapter.onSaveInstanceState());

		//Because this demo doesn't ever modify the adapter once it's loaded...there's no need
		//To save the state of the data in the adapter. Only the activation state as done above is
		//needed.
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

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			/*
			Returning the childPosition "could" be a stable & unique value to return here in certain
			situations.  However the safest and recommended approach is to always return a value
			which remains constant (stable) despite positional changes.

			Since each MovieItem has it's own unique barcode, utilizing that satisfies the stable
			and unique requirement. No two children will ever have the same barcode. Eg: no
			matter where the movie "I, Robot" is displayed...be it the 2nd child Position or 10th or
			in groupPosition == 1 or groupPosition == 10...it'll always return the same barcode.
			*/
			return getChild(groupPosition, childPosition).barcode();
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

		@Override
		public long getGroupId(int groupPosition) {
			/*
			Returning groupPosition "could" be a stable & unique value to return here in certain
			situations. However the safest and recommended approach is to always return a value
			which remains constant (stable) despite positional changes.

			Since our groups are storing years, returning the year is stable and unique.  No two
			groups will ever have the same year displayed. Eg: no matter where the year "2004"
			is displayed...be it groupPosition == 1 or groupPosition == 10...it'll always return
			2004.
			*/
			return Long.valueOf(getGroup(groupPosition));
		}

		@NonNull
		@Override
		public View getGroupView(@NonNull LayoutInflater inflater, int groupPosition,
								 boolean isExpanded, View convertView, @NonNull ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_group2, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getGroup(groupPosition).toString());
			return convertView;
		}

		@Override
		public boolean hasAutoExpandingGroups() {
			//Can be false as well.  Depends on how you want your navigation drawer items to behave
			return true;
		}

		@Override
		public boolean hasStableIds() {
			/*
			Any time choice mode is enabled, stable IDs should also be enabled. Otherwise, restoring
			the activity from saved state may activate the incorrect item in the adapter. Additionally,
			don't forget to have getGroupId() and getChildId() actually return unique and stable ids.
			Else it defeats the purpose of enabling this feature.
			*/
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

	private class DrawerToggle extends ActionBarDrawerToggle {

		public DrawerToggle() {
			//String resId here are for accessibility devices. It is not used for changing ActionBar title.
			//That must be manually done in the onDrawerClosed/Opened methods.
			super(NavigationDrawerActivity.this, mDrawerLayout,
				  R.string.title_navigation_drawer_open,
				  R.string.activity_rolodex_navigation_drawer);
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			super.onDrawerClosed(drawerView);
			ActionBar ab = getSupportActionBar();
			if (ab != null) {
				ab.setTitle(R.string.activity_rolodex_navigation_drawer);
			}
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			ActionBar ab = getSupportActionBar();
			if (ab != null) {
				ab.setTitle(R.string.title_navigation_drawer_open);
			}
		}
	}
}
