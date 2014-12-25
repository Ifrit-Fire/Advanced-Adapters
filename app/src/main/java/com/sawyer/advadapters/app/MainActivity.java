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
package com.sawyer.advadapters.app;

import android.app.ExpandableListActivity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.adapters.absarrayadapter.ArrayAdapterActivity;
import com.sawyer.advadapters.app.adapters.android.arrayadapter.AndroidAdapterActivity;
import com.sawyer.advadapters.app.adapters.android.simpleexpandablelistadapter.AndroidExpandableActivity;
import com.sawyer.advadapters.app.adapters.jsonadapter.JSONAdapterActivity;
import com.sawyer.advadapters.app.adapters.nfarrayadapter.NFArrayAdapterActivity;
import com.sawyer.advadapters.app.adapters.nfjsonadapter.NFJSONAdapterActivity;
import com.sawyer.advadapters.app.adapters.nfsparseadapter.NFSparseAdapterActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.RolodexArrayAdapterActivity;
import com.sawyer.advadapters.app.adapters.sparseadapter.SparseAdapterActivity;
import com.sawyer.advadapters.app.dialogs.ChoiceModeDialogFragment;
import com.sawyer.advadapters.widget.RolodexArrayAdapter;
import com.sawyer.advadapters.widget.RolodexBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ExpandableListActivity implements
		ChoiceModeDialogFragment.EventListener {
	public static final String EXTRA_CHOICE_MODE = "Extra Choice Mode";

	private static final String EXTRA_INTENT_NAME = "Extra Intent Name";
	private static final String EXTRA_GROUP_NAME = "Extra Group Name";
	private static final String TAG_CHOICE_MODE_DIALOG_FRAG = "Choice Mode Dialog Frag";

	private List<Intent> createIntentList() {
		List<Intent> intents = new ArrayList<>();
		Intent intent;

		/* Android Demos */
		intent = new Intent(this, AndroidAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_android_arrayadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_android));
		intents.add(intent);

		intent = new Intent(this, AndroidExpandableActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_android_simpleexpandable));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_android));
		intents.add(intent);

		/* Array Based Demos */
		intent = new Intent(this, ArrayAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_absarrayadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_arrays));
		intents.add(intent);

		intent = new Intent(this, NFArrayAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_nfarrayadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_arrays));
		intents.add(intent);

		intent = new Intent(this, RolodexArrayAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_arrayadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_arrays));
		intent.putExtra(EXTRA_CHOICE_MODE, RolodexBaseAdapter.ChoiceMode.NONE);
		intents.add(intent);

		/* SparseArray Based Demos */
		intent = new Intent(this, SparseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_sparseadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_sparsearrays));
		intents.add(intent);

		intent = new Intent(this, NFSparseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_nfsparseadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_sparsearrays));
		intents.add(intent);

		/* JSONArray Based Demos */
		intent = new Intent(this, JSONAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_jsonadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_jsonarrays));
		intents.add(intent);

		intent = new Intent(this, NFJSONAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_nfjsonadapter));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_jsonarrays));
		intents.add(intent);

		return intents;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment instanceof ChoiceModeDialogFragment) {
			ChoiceModeDialogFragment frag = (ChoiceModeDialogFragment) fragment;
			frag.setEventListener(this);
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
								int childPosition, long id) {
		Intent intent = (Intent) getExpandableListAdapter().getChild(groupPosition, childPosition);
		if (intent.getSerializableExtra(EXTRA_CHOICE_MODE) != null) {
			ChoiceModeDialogFragment frag = ChoiceModeDialogFragment.newInstance(intent);
			frag.show(getFragmentManager(), TAG_CHOICE_MODE_DIALOG_FRAG);
		} else {
			startActivity(intent);
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getExpandableListView().setGroupIndicator(null);
		DemoAdapter adapter = new DemoAdapter(this, createIntentList());
		adapter.setOnChildClickListener(this);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_about:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSelectedChoiceMode(RolodexBaseAdapter.ChoiceMode choiceMode, Intent intent) {
		intent.putExtra(EXTRA_CHOICE_MODE, choiceMode);
		startActivity(intent);
	}

	private class DemoAdapter extends RolodexArrayAdapter<String, Intent> {
		public DemoAdapter(Context activity, List<Intent> objects) {
			super(activity, objects);
		}

		@Override
		public String createGroupFor(Intent child) {
			return child.getStringExtra(EXTRA_GROUP_NAME);
		}

		@Override
		public View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_expandable_child1, parent, false);
			}
			TextView tv = (TextView) convertView;
			tv.setText(getChild(groupPosition, childPosition).getStringExtra(EXTRA_INTENT_NAME));

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
			return true;
		}

		@Override
		protected boolean isChildFilteredOut(Intent childItem, CharSequence constraint) {
			return false;
		}

		@Override
		protected boolean isGroupFilteredOut(String groupItem, CharSequence constraint) {
			return false;
		}

		@Override
		public boolean isGroupSelectable(int groupPosition) {
			return false;
		}
	}
}
