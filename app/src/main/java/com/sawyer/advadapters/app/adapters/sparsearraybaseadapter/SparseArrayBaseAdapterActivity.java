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
package com.sawyer.advadapters.app.adapters.sparsearraybaseadapter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.SparseArray;
import android.widget.Toast;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.app.dialogs.AppendSparseDialogFragment;
import com.sawyer.advadapters.app.dialogs.ContainsSparseDialogFragment;
import com.sawyer.advadapters.app.dialogs.PutSparseDialogFragment;

public class SparseArrayBaseAdapterActivity extends AdapterBaseActivity implements
		ContainsSparseDialogFragment.EventListener, PutSparseDialogFragment.EventListener,
		AppendSparseDialogFragment.EventListener {
	private static final String TAG_APPEND_ADAPTER_FRAG = "Tag Append Adapter Frag";
	private static final String TAG_BASE_ADAPTER_FRAG = "Tag Base Adapter Frag";
	private static final String TAG_CONTAINS_DIALOG_FRAG = "Tag Contains Dialog Frag";
	private static final String TAG_PUT_DIALOG_FRAG = "Tag Put Dialog Frag";

	private SparseArrayBaseFragment mListFragment;
	private ContainsSparseDialogFragment mContainsDialogFragment;
	private PutSparseDialogFragment mPutDialogFragment;
	private AppendSparseDialogFragment mAppendDialogFragment;

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
		return getString(R.string.info_sparsearray_baseadapter_message);
	}

	@Override
	protected String getInfoDialogTitle() {
		return getString(R.string.info_sparsearray_baseadapter_title);
	}

	@Override
	protected int getListCount() {
		return mListFragment.getListAdapter().getCount();
	}

	@Override
	protected void initFrags() {
		super.initFrags();
		FragmentManager manager = getFragmentManager();

		mListFragment = (SparseArrayBaseFragment) manager
				.findFragmentByTag(TAG_BASE_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = SparseArrayBaseFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_BASE_ADAPTER_FRAG);
			transaction.commit();
		}

		mContainsDialogFragment = (ContainsSparseDialogFragment) manager
				.findFragmentByTag(TAG_CONTAINS_DIALOG_FRAG);
		if (mContainsDialogFragment != null) {
			mContainsDialogFragment.setEventListener(this);
		}

		mPutDialogFragment = (PutSparseDialogFragment) manager
				.findFragmentByTag(TAG_PUT_DIALOG_FRAG);
		if (mPutDialogFragment != null) {
			mPutDialogFragment.setEventListener(this);
		}

		mAppendDialogFragment = (AppendSparseDialogFragment) manager
				.findFragmentByTag(TAG_APPEND_ADAPTER_FRAG);
		if (mAppendDialogFragment != null) {
			mAppendDialogFragment.setEventListener(this);
		}
	}

	@Override
	protected boolean isAppendDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isContainsDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isPutDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isSearchViewEnabled() {
		return true;
	}

	@Override
	public void onAppendAllMoviesClick(SparseArray<MovieItem> movies) {
		mListFragment.getListAdapter().appendAll(movies);
		updateActionBar();
		mAppendDialogFragment.dismiss();
	}

	@Override
	public void onAppendMovieWithIdClick(int barcode, MovieItem movieItem) {
		mListFragment.getListAdapter().appendWithId(barcode, movieItem);
		updateActionBar();
		mAppendDialogFragment.dismiss();
	}

	@Override
	public void onContainsIdClick(int barcode) {
		StringBuilder text = new StringBuilder();
		if (mListFragment.getListAdapter().containsId(barcode)) {
			text.append(getString(R.string.toast_contains_movie_true));
		} else {
			text.append(getString(R.string.toast_contains_movie_false));
		}
		text.append(MovieContent.ITEM_SPARSE.get(barcode).title);
		Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show();
		mContainsDialogFragment.dismiss();
	}

	@Override
	public void onContainsItemClick(MovieItem movie) {
		StringBuilder text = new StringBuilder();
		if (mListFragment.getListAdapter().containsItem(movie)) {
			text.append(getString(R.string.toast_contains_movie_true));
		} else {
			text.append(getString(R.string.toast_contains_movie_false));
		}
		text.append(movie.title);
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		mContainsDialogFragment.dismiss();
	}

	@Override
	public void onPutAllMoviesClick(SparseArray<MovieItem> movies) {
		mListFragment.getListAdapter().putAll(movies);
		updateActionBar();
		mPutDialogFragment.dismiss();
	}

	@Override
	public void onPutMovieClick(MovieItem movie) {
		int position = mListFragment.getListAdapter().getPosition(movie);

		//Bug fix, can't find movie. SparseArrays use == to search instead of equals()
		if (position < 0) {
			mListFragment.getListAdapter().putWithId(movie.barcode(), movie);
		} else {
			mListFragment.getListAdapter().put(position, movie);
		}
		updateActionBar();
		mPutDialogFragment.dismiss();
	}

	@Override
	public void onPutMovieWithIdClick(int barcode, MovieItem movie) {
		mListFragment.getListAdapter().putWithId(barcode, movie);
		updateActionBar();
		mPutDialogFragment.dismiss();
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
		mListFragment.getListAdapter().setSparseArray(MovieContent.ITEM_SPARSE);
		updateActionBar();
	}

	@Override
	protected void sort() {
		Toast.makeText(this, R.string.toast_sort_not_supported, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void startAppendDialog() {
		mAppendDialogFragment = AppendSparseDialogFragment.newInstance();
		mAppendDialogFragment.setEventListener(this);
		mAppendDialogFragment.show(getFragmentManager(), TAG_APPEND_ADAPTER_FRAG);
	}

	@Override
	protected void startContainsDialog() {
		mContainsDialogFragment = ContainsSparseDialogFragment.newInstance();
		mContainsDialogFragment.setEventListener(this);
		mContainsDialogFragment.show(getFragmentManager(), TAG_CONTAINS_DIALOG_FRAG);
	}

	@Override
	protected void startPutDialog() {
		mPutDialogFragment = PutSparseDialogFragment.newInstance();
		mPutDialogFragment.setEventListener(this);
		mPutDialogFragment.show(getFragmentManager(), TAG_PUT_DIALOG_FRAG);
	}
}
