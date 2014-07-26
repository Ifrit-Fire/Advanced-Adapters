package com.sawyer.advadapters.app.adapters.SparseArrayBaseAdapter;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;

public class SparseArrayBaseFragment extends ListFragment {
	private static final String STATE_CAB_CHECKED_ITEMS = "State Cab Checked Items";
	private static final String STATE_LIST = "State List";

	private SparseArray<MovieItem> mCheckedItems = new SparseArray<>();

	@Override
	public MovieSparseArrayBaseAdapter getListAdapter() {
		return (MovieSparseArrayBaseAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof MovieSparseArrayBaseAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " + MovieSparseArrayBaseAdapter.class.getSimpleName());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setAdapter(getListAdapter());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCheckedItems = savedInstanceState.getSparseParcelableArray(STATE_CAB_CHECKED_ITEMS);
			SparseArray<MovieItem> list = savedInstanceState
					.getSparseParcelableArray(STATE_LIST);
			setListAdapter(new MovieSparseArrayBaseAdapter(getActivity(), list));
		} else {
			setListAdapter(new MovieSparseArrayBaseAdapter(getActivity()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		ListView lv = (ListView) inflater.inflate(R.layout.listview, container, false);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(new OnCabMultiChoiceModeListener());
		return lv;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//Granted, making a whole new instance is not even necessary here.
		//However I wanted to demonstrate updating with an entirely different instance.
		MovieItem oldMovie = getListAdapter().getItem(position);
		MovieItem newMovie = new MovieItem(oldMovie.barcode());
		newMovie.title = new StringBuilder(oldMovie.title).reverse().toString();
		newMovie.year = oldMovie.year;
		newMovie.isRecommended = !oldMovie.isRecommended;
		getListAdapter().put((int) id, newMovie);
	}

	protected void onRemoveItemsClicked(SparseArray<MovieItem> items) {
		//Want to test each type of remove
		if (items.size() == 1) {
			getListAdapter().remove(0);
		} else if (items.size() == 2) {
			getListAdapter().removeWithId(items.keyAt(0));
			getListAdapter().removeWithId(items.keyAt(1));
		} else {
			getListAdapter().removeAll(items);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSparseParcelableArray(STATE_CAB_CHECKED_ITEMS, mCheckedItems);
		outState.putSparseParcelableArray(STATE_LIST, getListAdapter().getSparseArray());
	}

	private class OnCabMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;
			switch (item.getItemId()) {
			case R.id.menu_context_remove:
				onRemoveItemsClicked(mCheckedItems);
				mode.finish();
				result = true;
				break;

			default:
				result = false;
				break;
			}

			//Quick and easy way to force activity actionbar list count to update
			if (result) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						getActivity().invalidateOptionsMenu();
					}
				});
			}
			return result;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_sparsearray, menu);
			mode.setTitle(mCheckedItems.size() + " Selected");
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCheckedItems.clear();
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
			if (checked) {
				mCheckedItems.put((int) id, getListAdapter().getItem(position));
			} else {
				mCheckedItems.remove((int) id);
			}
			mode.setTitle(mCheckedItems.size() + " Selected");
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}
}
