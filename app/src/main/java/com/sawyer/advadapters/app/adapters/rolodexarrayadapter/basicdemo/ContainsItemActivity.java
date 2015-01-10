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
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Demonstration on how to test if a given item exists within our adapter. Groups are forced to
 * expand to help visually see our list of movies. Shows test conditions where one item is found and
 * another is not.
 */
public class ContainsItemActivity extends ExpandableListActivity {
	MovieItem mContainsMovie = MovieContent.ITEM_LIST.get(1);
	MovieItem mMissingMovie = MovieContent.ITEM_LIST.get(4);

	@OnClick(R.id.button_contain)
	public void containsItem(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		if (adapter.contains(mContainsMovie)) {    //We are expecting a true result here
			ToastHelper.showContainsTrue(this, mContainsMovie.title);
		} else {
			ToastHelper.showContainsFalse(this, mContainsMovie.title);
		}
	}

	@OnClick(R.id.button_missing)
	public void missingItem(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		if (adapter.contains(mMissingMovie)) {
			ToastHelper.showContainsTrue(this, mMissingMovie.title);
		} else {    //We are expecting a false result here
			ToastHelper.showContainsFalse(this, mMissingMovie.title);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contains_item);
		ButterKnife.inject(this);
		setListAdapter(new DemoAdapter(this, MovieContent.ITEM_LIST.subList(0, 3)));

		//Set the button text with the items we are testing with
		Button button = ButterKnife.findById(this, R.id.button_contain);
		button.setText(getString(R.string.btn_contains_item1, mContainsMovie.title));
		button = ButterKnife.findById(this, R.id.button_missing);
		button.setText(getString(R.string.btn_contains_item1, mMissingMovie.title));
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
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
