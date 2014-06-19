/**
 * By: JaySoyer
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
package com.sawyer.advadapters.app.adapters.simplearraybaseadapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.AdapterActivity;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.List;

public class SimpleArrayBaseAdapterActivity extends AdapterActivity {
	private MovieSimpleArrayAdapterFragment mListFragment;

	@Override
	protected void clear() {
		mListFragment.getListAdapter().clear();
		updateActionBar();
	}

	@Override
	protected boolean containsMovie(MovieItem movie) {
		return mListFragment.getListAdapter().contains(movie);
	}

	@Override
	protected boolean containsMovieCollection(List<MovieItem> movies) {
		return mListFragment.getListAdapter().containsAll(movies);
	}

	@Override
	protected String getInfoDialogMessage() {
		return getString(R.string.info_simplearray_baseadapter_message);
	}

	@Override
	protected String getInfoDialogTitle() {
		return getString(R.string.info_simplearray_baseadapter_title);
	}

	@Override
	protected int getListCount() {
		return mListFragment.getListAdapter().getCount();
	}

	@Override
	protected void initFrags() {
		super.initFrags();
		FragmentManager manager = getFragmentManager();
		mListFragment = (MovieSimpleArrayAdapterFragment) manager
				.findFragmentByTag(TAG_BASE_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = MovieSimpleArrayAdapterFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_BASE_ADAPTER_FRAG);
			transaction.commit();
		}
	}

	@Override
	protected void insertMovieCollection(List<MovieItem> movies, int position) {
		mListFragment.getListAdapter().insertAll(position, movies);
	}

	@Override
	protected void insertSingleMovie(MovieItem movie, int position) {
		mListFragment.getListAdapter().insert(position, movie);
	}

	@Override
	protected boolean isAddDialogEnabled() {
		return true;
	}

	@Override
	public boolean isAddVarargsEnabled() {
		return false;
	}

	@Override
	protected boolean isContainsDialogEnabled() {
		return true;
	}

	@Override
	protected boolean isInsertDialogEnabled() {
		return true;
	}

	@Override
	public void onAddMovieCollectionClick(List<MovieItem> movies) {
		super.onAddMovieCollectionClick(movies);
		mListFragment.getListAdapter().addAll(movies);
		updateActionBar();
	}

	@Override
	public void onAddSingleMovieClick(MovieItem movie) {
		super.onAddSingleMovieClick(movie);
		mListFragment.getListAdapter().add(movie);
		updateActionBar();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		if (fragment instanceof MovieSimpleArrayAdapterFragment) {
			mListFragment = (MovieSimpleArrayAdapterFragment) fragment;
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	protected void reset() {
		mListFragment.getListAdapter().setNotifyOnChange(false);
		mListFragment.getListAdapter().clear();
		mListFragment.getListAdapter().addAll(MovieContent.ITEM_LIST);
		mListFragment.getListAdapter().notifyDataSetChanged();
		updateActionBar();
	}

	@Override
	protected void sort() {
		mListFragment.getListAdapter().sort(null);
	}
}
