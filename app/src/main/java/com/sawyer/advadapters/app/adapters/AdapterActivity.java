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
import android.widget.Toast;

import com.sawyer.advadapters.app.Prefs;
import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.app.dialogs.AddDialogFragment;
import com.sawyer.advadapters.app.dialogs.ContainsDialogFragment;
import com.sawyer.advadapters.app.dialogs.InfoDialogFragment;
import com.sawyer.advadapters.app.dialogs.InsertDialogFragment;

import java.util.List;
import java.util.Random;

public abstract class AdapterActivity extends Activity implements AddDialogFragment.EventListener, SearchView.OnQueryTextListener {
	protected static final String TAG_BASE_ADAPTER_FRAG = "Tag Base Adapter Frag";

	private static final String TAG_ADD_DIALOG_FRAG = "Tag Add Dialog Frag";
	private static final String TAG_CONTAINS_DIALOG_FRAG = "Tag Contains Dialog Frag";
	private static final String TAG_INSERT_DIALOG_FRAG = "Tag Insert Dialog Frag";
	private static final String TAG_INFO_DIALOG_FRAG = "Tag Info Dialog Frag";

	private AddDialogFragment mAddDialog;
	private ContainsDialogFragment mContainsDialog;
	private InsertDialogFragment mInsertDialog;
	private InfoDialogFragment mInfoDialogFragment;

	protected void clear() {
	}

	protected boolean containsMovie(MovieItem movie) {
		return false;
	}

	protected boolean containsMovieCollection(List<MovieItem> movies) {
		return false;
	}

	protected abstract String getInfoDialogMessage();

	protected abstract String getInfoDialogTitle();

	protected abstract int getListCount();

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	protected void initFrags() {
		FragmentManager manager = getFragmentManager();
		if (isAddDialogEnabled()) {
			mAddDialog = (AddDialogFragment) manager.findFragmentByTag(TAG_ADD_DIALOG_FRAG);
			if (mAddDialog != null) {
				mAddDialog.setEventListener(this);
			}
		}

		if (isContainsDialogEnabled()) {
			mContainsDialog = (ContainsDialogFragment) manager
					.findFragmentByTag(TAG_CONTAINS_DIALOG_FRAG);
			if (mContainsDialog != null) {
				mContainsDialog.setEventListener(new ContainsDialogFragEventListener());
			}
		}

		if (isInsertDialogEnabled()) {
			mInsertDialog = (InsertDialogFragment) manager
					.findFragmentByTag(TAG_INSERT_DIALOG_FRAG);
			if (mInsertDialog != null) {
				mInsertDialog.setEventListener(new InsertDialogFragEventListener());
			}
		}

		mInfoDialogFragment = (InfoDialogFragment) manager.findFragmentByTag(TAG_INFO_DIALOG_FRAG);
	}

	private void initViews() {
		Button btn = (Button) findViewById(R.id.button_add_random);
		btn.setOnClickListener(new OnAddRandomClickListener());
		btn.setVisibility(isAddDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = (Button) findViewById(R.id.button_contain);
		btn.setOnClickListener(new OnContainsClickListener());
		btn.setVisibility(isContainsDialogEnabled() ? View.VISIBLE : View.GONE);

		btn = (Button) findViewById(R.id.button_insert_random);
		btn.setOnClickListener(new OnInsertClickListener());
		btn.setVisibility(isInsertDialogEnabled() ? View.VISIBLE : View.GONE);
	}

	protected void insertMovieCollection(List<MovieItem> movies, int position) {
	}

	protected void insertSingleMovie(MovieItem movie, int position) {
	}

	protected boolean isAddDialogEnabled() {
		return false;
	}

	@Override
	public boolean isAddVarargsEnabled() {
		return false;
	}

	protected boolean isContainsDialogEnabled() {
		return false;
	}

	protected boolean isInsertDialogEnabled() {
		return false;
	}

	protected boolean isSearchViewEnabled() {
		return false;
	}

	@Override
	public void onAddMovieCollectionClick(List<MovieItem> movies) {
		mAddDialog.dismiss();
	}

	@Override
	public void onAddSingleMovieClick(MovieItem movie) {
		mAddDialog.dismiss();
	}

	@Override
	public void onAddVarargsMovieClick(MovieItem... movies) {
		mAddDialog.dismiss();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adapter);

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

		return true;
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

	protected void reset() {
	}

	private void showInfoDialog() {
		if (mInfoDialogFragment != null) {
			mInfoDialogFragment.dismiss();
		}
		mInfoDialogFragment = InfoDialogFragment
				.newInstance(getInfoDialogTitle(), getInfoDialogMessage());
		mInfoDialogFragment.show(getFragmentManager(), TAG_INFO_DIALOG_FRAG);
	}

	protected void sort() {
	}

	@SuppressWarnings("ConstantConditions")
	protected void updateActionBar() {
		String subtitle = (getListCount()) + " movies listed";
		getActionBar().setSubtitle(subtitle);
	}

	private class ContainsDialogFragEventListener implements ContainsDialogFragment.EventListener {
		@Override
		public void onContainsMovieClick(MovieItem movie) {
			mContainsDialog.dismiss();
			boolean result = containsMovie(movie);
			String message =
					(result ? "Adapter contains movie:" : "Adapter does not contain:") + "\n" +
					movie.title;
			Toast.makeText(AdapterActivity.this, message, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onContainsMovieCollectionClick(List<MovieItem> movies) {
			mContainsDialog.dismiss();
			boolean result = containsMovieCollection(movies);
			StringBuilder builder = new StringBuilder();
			builder.append(
					result ? "Adapter contains all movies:" : "Adapter does not contain all movies:");
			for (MovieItem movie : movies) {
				builder.append("\n").append(movie.title);
			}
			Toast.makeText(AdapterActivity.this, builder.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private class InsertDialogFragEventListener implements InsertDialogFragment.EventListener {
		private int getPosition(InsertDialogFragment.InsertLocation location) {
			int position;

			switch (location) {
			case RANDOM:
				int count = getListCount();
				position = (count == 0 ? 0 : new Random().nextInt(count));    //Random 0 crashes
				break;
			case END:
				position = getListCount();
				break;
			case START:
			default:
				position = 0;
				break;
			}
			return position;
		}

		@Override
		public void onInsertMovieCollectionClick(List<MovieItem> movies,
												 InsertDialogFragment.InsertLocation insertLocation) {
			mInsertDialog.dismiss();
			insertMovieCollection(movies, getPosition(insertLocation));
		}

		@Override
		public void onInsertSingleMovieClick(MovieItem movie,
											 InsertDialogFragment.InsertLocation insertLocation) {
			mInsertDialog.dismiss();
			insertSingleMovie(movie, getPosition(insertLocation));
		}
	}

	private class OnAddRandomClickListener extends OnStartDialogClickListener {
		@Override
		public void startDialog(MovieItem movie1, MovieItem movie2) {
			mAddDialog = AddDialogFragment.newInstance(movie1, movie2);
			mAddDialog.setEventListener(AdapterActivity.this);
			mAddDialog.show(getFragmentManager(), TAG_ADD_DIALOG_FRAG);
		}
	}

	private class OnContainsClickListener extends OnStartDialogClickListener {
		@Override
		public void startDialog(MovieItem movie1, MovieItem movie2) {
			mContainsDialog = ContainsDialogFragment.newInstance(movie1, movie2);
			mContainsDialog.setEventListener(new ContainsDialogFragEventListener());
			mContainsDialog.show(getFragmentManager(), TAG_CONTAINS_DIALOG_FRAG);
		}
	}

	private class OnInsertClickListener extends OnStartDialogClickListener {
		@Override
		public void startDialog(MovieItem movie1, MovieItem movie2) {
			mInsertDialog = InsertDialogFragment.newInstance(movie1, movie2);
			mInsertDialog.setEventListener(new InsertDialogFragEventListener());
			mInsertDialog.show(getFragmentManager(), TAG_INSERT_DIALOG_FRAG);
		}
	}

	private class OnSearchActionExpandListener implements MenuItem.OnActionExpandListener {
		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
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

	private abstract class OnStartDialogClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int index1 = new Random().nextInt(MovieContent.ITEM_LIST.size());
			int index2 = new Random().nextInt(MovieContent.ITEM_LIST.size());
			while (index2 == index1) index2 = new Random().nextInt(MovieContent.ITEM_LIST.size());

			MovieItem movie1 = MovieContent.ITEM_LIST.get(index1);
			MovieItem movie2 = MovieContent.ITEM_LIST.get(index2);
			startDialog(movie1, movie2);
		}

		public abstract void startDialog(MovieItem movie1, MovieItem movie2);
	}
}
