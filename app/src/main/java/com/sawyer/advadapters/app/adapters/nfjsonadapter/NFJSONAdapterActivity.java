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
package com.sawyer.advadapters.app.adapters.nfjsonadapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.dialogs.AddJSONArrayDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

public class NFJSONAdapterActivity extends AdapterBaseActivity implements
		AddJSONArrayDialogFragment.EventListener, NFJSONAdapterFragment.EventListener {
	private static final String TAG_ADD_DIALOG_FRAG = "Tag Add Dialog Frag";
	private static final String TAG_ADAPTER_FRAG = "Tag Adapter Frag";

	private AddJSONArrayDialogFragment mAddDialogFragment;
	private NFJSONAdapterFragment mListFragment;

	@Override
	protected void clear() {
		mListFragment.getListAdapter().clear();
		updateActionBar();
	}

	@Override
	protected String getInfoDialogMessage() {
		return getString(R.string.info_nfjsonadapter_message);
	}

	@Override
	protected String getInfoDialogTitle() {
		return getString(R.string.info_nfjsonadapter_title);
	}

	@Override
	protected int getListCount() {
		return mListFragment.getListAdapter().getCount();
	}

	@Override
	protected void initFrags() {
		super.initFrags();
		FragmentManager manager = getFragmentManager();
		mListFragment = (NFJSONAdapterFragment) manager
				.findFragmentByTag(TAG_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = NFJSONAdapterFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_ADAPTER_FRAG);
			transaction.commit();
		}

		mAddDialogFragment = (AddJSONArrayDialogFragment) manager
				.findFragmentByTag(TAG_ADD_DIALOG_FRAG);
		if (mAddDialogFragment != null) {
			mAddDialogFragment.setEventListener(this);
		}
	}

	@Override
	protected boolean isAddDialogEnabled() {
		return true;
	}

	@Override
	public void onAdapterCountUpdated() {
		updateActionBar();
	}

	@Override
	public void onAddMultipleMoviesClick(JSONArray movies) {
		mListFragment.getListAdapter().addAll(movies);
		updateActionBar();
		mAddDialogFragment.dismiss();
	}

	@Override
	public void onAddSingleMovieClick(JSONObject movie) {
		mListFragment.getListAdapter().add(movie);
		updateActionBar();
		mAddDialogFragment.dismiss();
	}

	@Override
	public void onAddVarargsMovieClick(JSONObject... movies) {
		//Not supported
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		if (fragment instanceof NFJSONAdapterFragment) {
			mListFragment = (NFJSONAdapterFragment) fragment;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.jsonarray, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_unit_test:
			Intent intent = new Intent(this, UnitTestJSONArrayActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void reset() {
		mListFragment.getListAdapter().setJSONArray(MovieContent.ITEM_JSON);
		updateActionBar();
	}

	@Override
	protected void sort() {
		ToastHelper.showSortNotSupported(this);
	}

	@Override
	protected void startAddDialog() {
		mAddDialogFragment = AddJSONArrayDialogFragment.newInstance();
		mAddDialogFragment.setEventListener(this);
		mAddDialogFragment.show(getFragmentManager(), TAG_ADD_DIALOG_FRAG);
		mAddDialogFragment.setEnableArgvargs(false);
	}
}
