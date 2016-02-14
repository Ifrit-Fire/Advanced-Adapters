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
package com.sawyer.advadapters.app.adapters.android.simpleexpandablelistadapter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;

public class AndroidExpandableActivity extends AdapterBaseActivity {
	private static final String TAG_ADAPTER_FRAG = "Tag Adapter Frag";

	private AndroidExpandableFragment mListFragment;

	@Override
	protected void clear() {
		//Only way to clear the adapter is by creating a new one with no data
		mListFragment.setListAdapter(mListFragment.createEmptyAdapter());
	}

	private void collapseAll() {
		int groupCount = mListFragment.getListAdapter().getGroupCount();
		for (int index = 0; index < groupCount; ++index) {
			mListFragment.getExpandableListView().collapseGroup(index);
		}
	}

	private void expandAll() {
		int groupCount = mListFragment.getListAdapter().getGroupCount();
		for (int index = 0; index < groupCount; ++index) {
			mListFragment.getExpandableListView().expandGroup(index);
		}
	}

	@Override
	protected String getInfoDialogMessage() {
		return getString(R.string.info_android_simpleexpandable_message);
	}

	@Override
	protected String getInfoDialogTitle() {
		return getString(R.string.info_android_simpleexpandable_title);
	}

	@Override
	protected int getListCount() {
		int groupCount = mListFragment.getListAdapter().getGroupCount();
		int totalChildCount = 0;
		for (int index = 0; index < groupCount; ++index) {
			totalChildCount += mListFragment.getListAdapter().getChildrenCount(index);
		}
		return totalChildCount;
	}

	@Override
	protected void initFrags() {
		super.initFrags();
		FragmentManager manager = getFragmentManager();
		mListFragment = (AndroidExpandableFragment) manager.findFragmentByTag(TAG_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = AndroidExpandableFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_ADAPTER_FRAG);
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.rolodex, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_collapse:
			collapseAll();
			return true;

		case R.id.menu_action_expand:
			expandAll();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void reset() {
		mListFragment.reset();
	}

	@Override
	protected void sort() {
		ToastHelper.showSortNotSupported(this);
	}
}
