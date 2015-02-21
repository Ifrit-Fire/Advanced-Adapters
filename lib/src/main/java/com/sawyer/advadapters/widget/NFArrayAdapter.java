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
package com.sawyer.advadapters.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * A non-filterable custom abstract {@link BaseAdapter} that is backed by an {@link ArrayList} of
 * arbitrary objects.  By default this class delegates view generation to subclasses.
 * <p/>
 * Designed to be a more flexible and customizable solution then Android's ArrayAdapter class but
 * without the filtering mechanism. As a result, there is no need for {@code synchronized} blocks
 * which may help those worried about performance. It provides extra features such as: supporting
 * additional {@link ArrayList} methods, makes smarter use of {@link #notifyDataSetChanged()}, and
 * conveniently passes along a layout inflater for view creation.
 * <p/>
 * If filtering is required, it's strongly recommended to use the {@link AbsArrayAdapter} instead.
 */
public abstract class NFArrayAdapter<T> extends BaseAdapter {
	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter * */
	private Context mContext;
	/**
	 * Contains the list of objects that represent the data of the adapter.
	 */
	private ArrayList<T> mObjects;
	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever {@link
	 * #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public NFArrayAdapter(@NonNull Context activity) {
		init(activity, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public NFArrayAdapter(@NonNull Context activity, @NonNull Collection<T> items) {
		init(activity, new ArrayList<>(items));
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public NFArrayAdapter(@NonNull Context activity, @NonNull T[] items) {
		ArrayList<T> list = new ArrayList<>();
		Collections.addAll(list, items);
		init(activity, list);
	}

	/**
	 * Adds the specified item at the end of the adapter.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(@Nullable T item) {
		mObjects.add(item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the adapter.
	 *
	 * @param items The Collection to add at the end of the adapter.
	 */
	public void addAll(@NonNull Collection<? extends T> items) {
		boolean isModified = mObjects.addAll(items);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter.
	 *
	 * @param items The items to add at the end of the adapter.
	 */
	@SafeVarargs
	public final void addAll(@NonNull T... items) {
		boolean isModified = Collections.addAll(mObjects, items);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Remove all elements from the adapter.
	 */
	public void clear() {
		mObjects.clear();
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Tests whether this adapter contains the specified item. Be aware that this is a linear
	 * search.
	 *
	 * @param item The item to search for
	 *
	 * @return {@code true} if the item is an element of this adapter. {@code false} otherwise
	 */
	public boolean contains(@Nullable T item) {
		return mObjects.contains(item);
	}

	/**
	 * Tests whether this adapter contains all items contained in the specified collection.  Be
	 * aware that this performs a nested for loop search...eg O(n*m) complexity.
	 *
	 * @param items The collection of items
	 *
	 * @return {@code true} if all items in the specified collection are elements of this adapter,
	 * {@code false} otherwise
	 */
	public boolean containsAll(@NonNull Collection<?> items) {
		return mObjects.containsAll(items);
	}

	/**
	 * @return The Context associated with this adapter.
	 */
	@NonNull
	public Context getContext() {
		return mContext;
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	/**
	 * <p>Get a {@link android.view.View} that displays in the drop down popup the data at the
	 * specified position in the data set.</p>
	 *
	 * @param inflater    the LayoutInflater object that can be used to inflate each view.
	 * @param position    index of the item whose view we want.
	 * @param convertView the old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible
	 *                    to convert this view to display the correct data, this method can create a
	 *                    new view.
	 * @param parent      the parent that this view will eventually be attached to
	 *
	 * @return a {@link android.view.View} corresponding to the data at the specified position.
	 */
	@NonNull
	public View getDropDownView(@NonNull LayoutInflater inflater, int position,
								@Nullable View convertView, @NonNull ViewGroup parent) {
		return getView(inflater, position, convertView, parent);
	}

	@Override
	public final View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getDropDownView(mInflater, position, convertView, parent);
	}

	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * @return The  list of items stored within the Adapter
	 */
	@NonNull
	public ArrayList<T> getList() {
		return new ArrayList<>(mObjects);
	}

	/**
	 * Resets the adapter to store a new list of items. Convenient way of calling {@link #clear()},
	 * then {@link #addAll(java.util.Collection)} without having to worry about an extra {@link
	 * #notifyDataSetChanged()} invoked in between.
	 *
	 * @param items New list of items to store within the adapter.
	 */
	public void setList(@NonNull Collection<? extends T> items) {
		mObjects.clear();
		mObjects.addAll(items);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Returns the position of the specified item in the array.  Be aware that this is a linear
	 * search.
	 *
	 * @param item The item to retrieve the position of.
	 *
	 * @return The position of the specified item.
	 */
	public int getPosition(@Nullable T item) {
		return mObjects.indexOf(item);
	}

	/**
	 * Get a View that displays the data at the specified position in the data set. You can either
	 * create a View manually or inflate it from an XML layout file. When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)} to specify
	 * a root view and to prevent attachment to the root.
	 *
	 * @param inflater    the LayoutInflater object that can be used to inflate each view.
	 * @param position    The position of the item within the adapter's data set of the item whose
	 *                    view we want.
	 * @param convertView The old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible
	 *                    to convert this view to display the correct data, this method can create a
	 *                    new view. Heterogeneous lists can specify their number of view types, so
	 *                    that this View is always of the right type (see {@link
	 *                    #getViewTypeCount()} and {@link #getItemViewType(int)}).
	 * @param parent      The parent that this view will eventually be attached to
	 *
	 * @return A View corresponding to the data at the specified position.
	 */
	public abstract View getView(@NonNull LayoutInflater inflater, int position,
								 @Nullable View convertView, @NonNull ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(@NonNull Context context, @NonNull ArrayList<T> items) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mObjects = items;
	}

	/**
	 * Inserts the specified item at the specified index in the array.
	 *
	 * @param index The index at which the item must be inserted.
	 * @param item  The item to insert into the adapter.
	 */
	public void insert(int index, @Nullable T item) {
		mObjects.add(index, item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Inserts the item in the specified collection at the specified location in this adapter. The
	 * items are added in the order they are returned from the collection's iterator.
	 *
	 * @param index The index at which the items must be inserted.
	 * @param items The collection of items to be inserted.
	 */
	public void insertAll(int index, @NonNull Collection<? extends T> items) {
		boolean isModified = mObjects.addAll(index, items);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Removes the first occurrence of the specified item from the adapter
	 *
	 * @param item The item to remove.
	 */
	public void remove(@Nullable T item) {
		boolean isModified = mObjects.remove(item);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all occurrences in the adapter of each item in the specified collection.
	 *
	 * @param items The collection of items to remove
	 */
	public void removeAll(@NonNull Collection<?> items) {
		boolean isModified = mObjects.removeAll(items);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all items from this adapter that are not contained in the specified collection.
	 *
	 * @param items The collection of items to retain
	 */
	public void retainAll(@NonNull Collection<?> items) {
		boolean isModified = mObjects.retainAll(items);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Control whether methods that change the list ({@link #add}, {@link #insert}, {@link #remove},
	 * {@link #clear}) automatically call {@link #notifyDataSetChanged}.  If set to false, caller
	 * must manually call notifyDataSetChanged() to have the changes reflected in the attached
	 * view.
	 * <p/>
	 * The default is true, and calling notifyDataSetChanged() resets the flag to true.
	 *
	 * @param notifyOnChange if true, modifications to the list will automatically call {@link
	 *                       #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	/**
	 * Sorts the content of this adapter using the natural order of the stored items themselves.
	 * This requires items to have implemented {@link java.lang.Comparable} and is equivalent of
	 * passing null to {@link #sort(java.util.Comparator)}.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored items do not
	 *                                      implement {@code Comparable} or if {@code compareTo}
	 *                                      throws for any pair of items.
	 */
	public void sort() {
		sort(null);
	}

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator Used to sort the items contained in this adapter. Null to use an item's
	 *                   {@code Comparable} interface.
	 *
	 * @throws ClassCastException If the comparator is null and the stored items do not implement
	 *                            {@code Comparable} or if {@code compareTo} throws for any pair of
	 *                            items.
	 */
	public void sort(@Nullable Comparator<? super T> comparator) {
		Collections.sort(mObjects, comparator);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Updates the item at the specified position in the adapter with the specified item. This
	 * operation does not change the size of the adapter.
	 *
	 * @param position The location at which to put the specified item
	 * @param item     The new item to replace with the old
	 */
	public void update(int position, @Nullable T item) {
		mObjects.set(position, item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}
}