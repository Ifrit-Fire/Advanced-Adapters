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
package com.sawyer.advadapters.app.adapters.androidarrayadapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sawyer.advadapters.app.adapters.ListAdapterFragment;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AndroidAdapterFragment extends ListAdapterFragment<MovieItem> {
	private static final String STATE_LIST = "State List";

	public static AndroidAdapterFragment newInstance() {
		return new AndroidAdapterFragment();
	}

	@Override
	public MovieAdapter getListAdapter() {
		return (MovieAdapter) super.getListAdapter();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (adapter instanceof MovieAdapter) {
			super.setListAdapter(adapter);
		} else {
			throw new ClassCastException(
					"Adapter must be of type " +
					MovieAdapter.class.getSimpleName()
			);
		}
	}

	@Override
	protected boolean isRemoveItemsEnabled() {
		return true;
	}

	@Override
	protected boolean isRetainItemsEnabled() {
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			List<MovieItem> list = (ArrayList<MovieItem>) savedInstanceState
					.getSerializable(STATE_LIST);
			setListAdapter(new MovieAdapter(getActivity(), list));
		} else {
			setListAdapter(new MovieAdapter(getActivity()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
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
		getListAdapter().setNotifyOnChange(false);
		getListAdapter().remove(oldMovie);
		getListAdapter().insert(newMovie, position);
		getListAdapter().notifyDataSetChanged();
	}

	@Override
	protected void onRemoveItemsClicked(Set<MovieItem> items) {
		getListAdapter().setNotifyOnChange(false);
		for (MovieItem item : items) {
			getListAdapter().remove(item);
		}
		getListAdapter().notifyDataSetChanged();
	}

	@Override
	protected void onRetainItemsClicked(Set<MovieItem> items) {
		//Not supported
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ArrayList<MovieItem> list = new ArrayList<>();
		for (int index = 0; index < getListAdapter().getCount(); ++index) {
			list.add(getListAdapter().getItem(index));
		}
		outState.putSerializable(STATE_LIST, list);
	}
}
