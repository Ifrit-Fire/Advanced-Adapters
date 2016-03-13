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

package com.sawyer.advadapters.app.adapters.nfrolodexarrayadapter.advancedemo;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.NFRolodexArrayAdapter;

import java.util.List;

/**
 * Demonstration on how to use a {@link android.widget.Checkable} view with the rolodex adapter for
 * a multi-select choice mode.  In order to check the views, choice mode must be set. When using the
 * rolodex adapter, the adapter itself takes ownership of setting choice mode. Setting it on the
 * ExpandableListView itself will cause the app to crash.
 */
public class MultiSelectActivity extends ExpandableListActivity {
	private static final String STATE_ADAPTER_SAVED_STATE = "State Adapter Saved State";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DemoAdapter adapter = new DemoAdapter(this, MovieContent.ITEM_LIST);
		adapter.setChoiceMode(DemoAdapter.ChoiceMode.MULTIPLE);
		setListAdapter(adapter);

		if (savedInstanceState != null) {
			//Restore choice mode state and the activated items
			adapter.onRestoreInstanceState(
					savedInstanceState.getParcelable(STATE_ADAPTER_SAVED_STATE));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
		DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
		if (adapter != null) {
			outState.putParcelable(STATE_ADAPTER_SAVED_STATE, adapter.onSaveInstanceState());
		}

		//Because this demo doesn't ever modify the adapter once it's loaded...there's no need
		//to save the state of the data in the adapter. Only the activation state as done above is
		//needed.
	}

	private class DemoAdapter extends NFRolodexArrayAdapter<Integer, MovieItem> {
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
				convertView = inflater.inflate(R.layout.item_expandable_child2, parent, false);
			}
			CheckedTextView tv = (CheckedTextView) convertView;
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
				convertView = inflater.inflate(R.layout.item_expandable_group3, parent, false);
			}
			CheckedTextView tv = (CheckedTextView) convertView;
			tv.setText(getGroup(groupPosition).toString());
			return convertView;
		}

		@Override
		public boolean hasAutoExpandingGroups() {
			//Can be false as well.  Depends on the behavior you want.
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
	}
}
