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
package com.sawyer.advadapters.app.adapters;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.sawyer.advadapters.app.Prefs;
import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.dialogs.InfoDialogFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Abstract class that establishes a structure for supporting all sorts of Adapter functionality.
 * Will adjust UI based on what subclasses enable/disable.  Subclasses should only need worrying
 * about hooking up the adapter to the required methods needing implementing.
 */
public abstract class AdapterBaseActivity extends Activity implements
		SearchView.OnQueryTextListener {
	private static final String TAG_INFO_DIALOG_FRAG = "Tag Info Dialog Frag";

	private InfoDialogFragment mInfoDialogFragment;

	protected abstract void clear();

	protected void clearAdapterFilter() {
	}

	protected abstract String getInfoDialogMessage();

	protected abstract String getInfoDialogTitle();

	protected abstract int getListCount();

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar == null) return;
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	protected void initFrags() {
		FragmentManager manager = getFragmentManager();
		mInfoDialogFragment = (InfoDialogFragment) manager.findFragmentByTag(TAG_INFO_DIALOG_FRAG);
	}

	private void initViews() {
		Button btn = ButterKnife.findById(this, R.id.button_add_random);
		btn.setVisibility(isAddDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = ButterKnife.findById(this, R.id.button_append_random);
		btn.setVisibility(isAppendDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = ButterKnife.findById(this, R.id.button_contain);
		btn.setVisibility(isContainsDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = ButterKnife.findById(this, R.id.button_put);
		btn.setVisibility(isPutDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = ButterKnife.findById(this, R.id.button_insert_random);
		btn.setVisibility(isInsertDialogEnabled() ? View.VISIBLE : View.GONE);
	}

	protected boolean isAddDialogEnabled() {
		return false;
	}

	protected boolean isAppendDialogEnabled() {
		return false;
	}

	protected boolean isContainsDialogEnabled() {
		return false;
	}

	protected boolean isInsertDialogEnabled() {
		return false;
	}

	protected boolean isPutDialogEnabled() {
		return false;
	}

	protected boolean isSearchViewEnabled() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adapter);
		ButterKnife.inject(this);

		initFrags();
		initViews();
		initActionBar();

		if (!Prefs.hasViewedAdapterInfo(this)) {
			showInfoDialog();
			Prefs.setViewedAdapterInfo(this, true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.adapters, menu);

		MenuItem item = menu.findItem(R.id.menu_action_search);
		if (item != null) {
			SearchView searchView = (SearchView) item.getActionView();
			searchView.setOnQueryTextListener(this);
			item.setOnActionExpandListener(new OnSearchActionExpandListener());
			item.setVisible(isSearchViewEnabled());
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_action_clear:
			clear();
			return true;

		case R.id.menu_action_reset:
			reset();
			return true;

		case R.id.menu_action_search:
			return true;

		case R.id.menu_action_sort:
			sort();
			return true;

		case R.id.menu_action_info:
			showInfoDialog();
			return true;

		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		//Bug Fix: Placing in onCreate() caused reset() to be invoked before the attached fragments could finish their onCreate()
		//		This caused an NPE because the adapter hasn't been set yet. Does not occur with XML embedded fragments.
		if (savedInstanceState == null) {
			reset();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		updateActionBar();
		return result;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	protected abstract void reset();

	private void showInfoDialog() {
		if (mInfoDialogFragment != null) {
			mInfoDialogFragment.dismiss();
		}
		mInfoDialogFragment = InfoDialogFragment
				.newInstance(getInfoDialogTitle(), getInfoDialogMessage());
		mInfoDialogFragment.show(getFragmentManager(), TAG_INFO_DIALOG_FRAG);
	}

	protected abstract void sort();

	@OnClick(R.id.button_add_random)
	protected void startAddDialog() {
	}

	@OnClick(R.id.button_append_random)
	protected void startAppendDialog() {
	}

	@OnClick(R.id.button_contain)
	protected void startContainsDialog() {
	}

	@OnClick(R.id.button_insert_random)
	protected void startInsertDialog() {
	}

	@OnClick(R.id.button_put)
	protected void startPutDialog() {
	}

	protected void updateActionBar() {
		String subtitle = (getListCount()) + " movies listed";
		if (getActionBar() != null) getActionBar().setSubtitle(subtitle);
	}

	private class OnSearchActionExpandListener implements MenuItem.OnActionExpandListener {
		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			clearAdapterFilter();
			item.getActionView().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateActionBar();
				}
			}, 66);    //Have to wait for adapter to update with the unfiltered list

			return true;
		}

		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			return true;
		}
	}
}
