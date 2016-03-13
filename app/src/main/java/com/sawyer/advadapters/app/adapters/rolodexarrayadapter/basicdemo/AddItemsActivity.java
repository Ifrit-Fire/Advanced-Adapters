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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Demonstration on how to add items to our adapter. There are three ways to do so; Via an
 * individual item, an array, or collection. Groups are forced to expand to help visually see the
 * item addition. The adapter's list is persisted/restored accordingly to ensure things like
 * orientation change doesn't wipe our list of items.
 */
public class AddItemsActivity extends ExpandableListActivity {
	private static final String STATE_LIST = "State List";

	@OnClick(android.R.id.button3)
	public void onAddArray(View v) {
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		//Lets shuffle our list and pick three
		List<MovieItem> randList = new ArrayList<>(MovieContent.ITEM_LIST);
		Collections.shuffle(randList);
		randList = randList.subList(0, 3);

		//Convert to normal array and add to adapter
		MovieItem[] movies = new MovieItem[3];
		movies = randList.toArray(movies);
		adapter.addAll(movies);
	}

	@OnClick(android.R.id.button2)
	public void onAddCollection(View v) {
		//Lets shuffle our list, pick three, and add to adapter
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		List<MovieItem> randList = new ArrayList<>(MovieContent.ITEM_LIST);
		Collections.shuffle(randList);
		adapter.addAll(randList.subList(0, 3));
	}

	@OnClick(android.R.id.button1)
	public void onAddOne(View v) {
		//Pick a random movie and add to adapter
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		int ranIndex = new Random().nextInt(MovieContent.ITEM_LIST.size());
		adapter.add(MovieContent.ITEM_LIST.get(ranIndex));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rolodex_three_buttons);
		ButterKnife.inject(this);
		getExpandableListView().setGroupIndicator(null); //Removes group indicator from group views

		//Set the button text
		Button button = ButterKnife.findById(this, android.R.id.button1);
		button.setText(R.string.btn_add);
		button = ButterKnife.findById(this, android.R.id.button2);
		button.setText(R.string.btn_add_all_col);
		button = ButterKnife.findById(this, android.R.id.button3);
		button.setText(R.string.btn_add_all_var);

		//Create our adapter. By default groups will be sorted. onRestore handles setting adapter.
		setListAdapter(new DemoAdapter(this));
	}

	@Override
	protected void onDestroy() {
		ButterKnife.reset(this);
		super.onDestroy();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		ArrayList<MovieItem> list = state.getParcelableArrayList(STATE_LIST);
		if (list != null) {
			adapter.setList(list);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		outState.putParcelableArrayList(STATE_LIST, adapter.getList());
	}

	private class DemoAdapter extends RolodexArrayAdapter<Integer, MovieItem> {
		public DemoAdapter(Context activity) {
			super(activity);
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
