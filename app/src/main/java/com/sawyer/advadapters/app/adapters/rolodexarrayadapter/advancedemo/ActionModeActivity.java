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

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.PatchedExpandableListAdapter;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;

import java.util.List;

/**
 * Demonstration on how to use modal {@link PatchedExpandableListAdapter.ChoiceMode} with the
 * rolodex adapter. When using the rolodex adapter, the adapter itself takes ownership of setting
 * choice mode. Setting it on the ExpandableListView itself will cause the app to crash.
 * <p/>
 * The PatchedExpandableListAdapter implemented it's own solution which behaves exactly as the
 * {@link AbsListView#setChoiceMode(int)} would. Ensure you not only set the ChoiceMode with the
 * adapter but also set the custom {@link PatchedExpandableListAdapter.ChoiceModeListener}.
 */
public class ActionModeActivity extends ExpandableListActivity {
	private static final String STATE_ADAPTER_SAVED_STATE = "State Adapter Saved State";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DemoAdapter adapter = new DemoAdapter(this, MovieContent.ITEM_LIST);
		adapter.setChoiceMode(DemoAdapter.ChoiceMode.MULTIPLE_MODAL);
		adapter.setMultiChoiceModeListener(new DemoChoiceModeListener());
		setListAdapter(adapter);

		if (savedInstanceState != null) {
			//Restore choice mode state and the activated items
			adapter.onRestoreInstanceState(
					savedInstanceState.getParcelable(STATE_ADAPTER_SAVED_STATE));
		}
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
			return getGroup(groupPosition);
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

	/**
	 * When enabling a modal choiceMode, a custom {@link PatchedExpandableListAdapter.ChoiceModeListener}
	 * is required to be set on the adapter. This usage is identical to that of {@link
	 * AbsListView.MultiChoiceModeListener} with the difference of having child and group checked
	 * state callbacks.
	 * <p/>
	 * This is a simple example of tracking the number of checked children items. While groups can
	 * be checked, we are ignoring their count. Some MenuItems are inflated into the CAB for visual
	 * reference only.
	 */
	private class DemoChoiceModeListener implements
			PatchedExpandableListAdapter.ChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_context_remove:
				ToastHelper.showRemoveNotSupported(ActionModeActivity.this);
				return true;
			case R.id.menu_context_retain:
				ToastHelper.showRetainAllNotSupported(ActionModeActivity.this);
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onChildCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, int childPosition, long childId,
											   boolean checked) {
			DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
			mode.setTitle(adapter.getCheckedChildCount() + getString(R.string.desc_selected));
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_array, menu);
			DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
			mode.setTitle(adapter.getCheckedChildCount() + getString(R.string.desc_selected));
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onGroupCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, boolean checked) {
			//If group is expanded, then the onChildCheckedStateChanged method will be invoked. Which
			//means it'll safely take care of updating our screen.
			if (getExpandableListView().isGroupExpanded(groupPosition)) {
				return;
			}

			//If group is NOT expanded, then the onChildCheckedStateChanged method will NOT be invoked.
			//which means we need to take care of updating our screen here.
			DemoAdapter adapter = (DemoAdapter) getExpandableListAdapter();
			mode.setTitle(adapter.getCheckedChildCount() + getString(R.string.desc_selected));
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
