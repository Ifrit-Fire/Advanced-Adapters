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
package com.sawyer.advadapters.app.adapters.arraybaseadapter;

import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sawyer.advadapters.app.adapters.ListAdapterFragment;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.ArrayList;
import java.util.Set;

public class ArrayBaseAdapterFragment extends ListAdapterFragment<MovieItem> {
	private static final String STATE_LIST = "State List";

	public static ArrayBaseAdapterFragment newInstance() {
		return new ArrayBaseAdapterFragment();
	}

	@Override
	public MovieArrayBaseAdapter getListAdapter() {
		return (MovieArrayBaseAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof MovieArrayBaseAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " + MovieArrayBaseAdapter.class.getSimpleName());
		}
	}

	@Override
	protected boolean isRemoveItemsEnabled() {
		return true;
	}

	@Override
	protected boolean isRetainItemsEnabled() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			ArrayList<MovieItem> list = (ArrayList<MovieItem>) savedInstanceState
					.getSerializable(STATE_LIST);
			setListAdapter(new MovieArrayBaseAdapter(getActivity(), list));
		} else {
			setListAdapter(new MovieArrayBaseAdapter(getActivity()));
		}
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
		getListAdapter().update(position, newMovie);
	}

	@Override
	protected void onRemoveItemsClicked(Set<MovieItem> items) {
		if (items.size() == 1) {
			getListAdapter().remove(items.iterator().next());
		} else {
			getListAdapter().removeAll(items);
		}
	}

	@Override
	protected void onRetainItemsClicked(Set<MovieItem> items) {
		getListAdapter().retainAll(items);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_LIST, getListAdapter().getList());
	}
}
