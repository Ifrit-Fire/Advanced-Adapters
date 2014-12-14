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
package com.sawyer.advadapters.app.adapters.rolodexarrayadapter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.app.dialogs.AddArrayDialogFragment;
import com.sawyer.advadapters.app.dialogs.ContainsArrayDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RolodexArrayAdapterActivity extends AdapterBaseActivity implements
		RolodexArrayAdapterFragment.EventListener, AddArrayDialogFragment.EventListener,
		ContainsArrayDialogFragment.EventListener {
	private static final String TAG_ADAPTER_FRAG = "Tag Adapter Frag";
	private static final String TAG_ADD_DIALOG_FRAG = "Tag Add Dialog Frag";
	private static final String TAG_CONTAINS_DIALOG_FRAG = "Tag Contains Dialog Frag";

	private AddArrayDialogFragment mAddDialogFragment;
	private ContainsArrayDialogFragment mContainsDialogFragment;
	private RolodexArrayAdapterFragment mListFragment;

	@Override
	protected void clear() {
		mListFragment.getListAdapter().clear();
		updateActionBar();
	}

	@Override
	protected void clearAdapterFilter() {
		mListFragment.getListAdapter().getFilter().filter("");
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
		mListFragment = (RolodexArrayAdapterFragment) manager
				.findFragmentByTag(TAG_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = RolodexArrayAdapterFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_ADAPTER_FRAG);
			transaction.commit();
		}

		mAddDialogFragment = (AddArrayDialogFragment) manager
				.findFragmentByTag(TAG_ADD_DIALOG_FRAG);
		if (mAddDialogFragment != null) {
			mAddDialogFragment.setEventListener(this);
		}

		mContainsDialogFragment = (ContainsArrayDialogFragment) manager
				.findFragmentByTag(TAG_CONTAINS_DIALOG_FRAG);
		if (mContainsDialogFragment != null) {
			mContainsDialogFragment.setEventListener(this);
		}
	}

	@Override
	protected boolean isAddDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isContainsDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isSearchViewEnabled() {
		return true;
	}

	@Override
	public void onAdapterCountUpdated() {
		updateActionBar();
	}

	@Override
	public void onAddMultipleMoviesClick(List<MovieItem> movies) {
		mListFragment.getListAdapter().addAll(movies);
		updateActionBar();
		mAddDialogFragment.dismiss();
	}

	@Override
	public void onAddSingleMovieClick(MovieItem movie) {
		mListFragment.getListAdapter().add(movie);
		updateActionBar();
		mAddDialogFragment.dismiss();
	}

	@Override
	public void onAddVarargsMovieClick(MovieItem... movies) {
		mListFragment.getListAdapter().addAll(movies);
		updateActionBar();
		mAddDialogFragment.dismiss();
	}

	@Override
	public void onContainsMultipleMovieClick(List<MovieItem> movies) {
		//Not supported
	}

	@Override
	public void onContainsSingleMovieClick(MovieItem movie) {
		if (mListFragment.getListAdapter().contains(movie)) {
			ToastHelper.showContainsTrue(this, movie.title);
		} else {
			ToastHelper.showContainsFalse(this, movie.title);
		}
		mContainsDialogFragment.dismiss();
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
			mListFragment.getListAdapter().collapseAll();
			return true;

		case R.id.menu_action_expand:
			mListFragment.getListAdapter().expandAll();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mListFragment.getListAdapter().getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mListFragment.getListAdapter().getFilter().filter(query);
		return true;
	}

	@Override
	protected void reset() {
		List<MovieItem> movies = new ArrayList<>(MovieContent.ITEM_LIST);
		Collections.shuffle(movies);    //Test to ensure group ordering is working
		movies = movies.subList(0, movies.size() / 2);
		mListFragment.getListAdapter().setList(movies);
		updateActionBar();
	}

	@Override
	protected void sort() {
		mListFragment.getListAdapter().sort();
	}

	@Override
	protected void startAddDialog() {
		mAddDialogFragment = AddArrayDialogFragment.newInstance();
		mAddDialogFragment.setEventListener(this);
		mAddDialogFragment.show(getFragmentManager(), TAG_ADD_DIALOG_FRAG);
	}

	@Override
	protected void startContainsDialog() {
		mContainsDialogFragment = ContainsArrayDialogFragment.newInstance();
		mContainsDialogFragment.setEventListener(this);
		mContainsDialogFragment.show(getFragmentManager(), TAG_CONTAINS_DIALOG_FRAG);
		mContainsDialogFragment.setEnableContainsAll(false);
	}
}
