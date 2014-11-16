/**
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sawyer.advadapters.app.adapters.rolodexadapter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;
import com.sawyer.advadapters.app.data.MovieContent;

public class RolodexAdapterActivity extends AdapterBaseActivity implements
		RolodexAdapterFragment.EventListener {
	private static final String TAG_ADAPTER_FRAG = "Tag Adapter Frag";

	private RolodexAdapterFragment mListFragment;

	@Override
	protected void clear() {
		mListFragment.getListAdapter().clear();
		updateActionBar();
	}

	@Override
	protected String getInfoDialogMessage() {
		return "";    //TODO: Implement
	}

	@Override
	protected String getInfoDialogTitle() {
		return "";    //TODO: Implement
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
		mListFragment = (RolodexAdapterFragment) manager
				.findFragmentByTag(TAG_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = RolodexAdapterFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_ADAPTER_FRAG);
			transaction.commit();
		}
	}

	@Override
	public void onAdapterCountUpdated() {
		updateActionBar();
	}

	@Override
	protected void reset() {
		mListFragment.getListAdapter().setList(MovieContent.ITEM_LIST);
		updateActionBar();
	}

	@Override
	protected void sort() {
		//TODO: Implement
	}
}
