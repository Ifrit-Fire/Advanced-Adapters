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
package com.sawyer.advadapters.widget;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * A <i>trimmed</i> down version of the {@link SparseArrayBaseAdapter} which is similarly backed by
 * an {@link SparseArray} of arbitrary objects. By default this class will delegate view generation
 * to subclasses.
 * <p/>
 * Designed to be a simple version of it's cousin SparseArrayBaseAdapter, it removes support for
 * filtering. As a result, there is no need for <code>synchronized</code> blocks which may help
 * those worried about performance. An adapter's row ID maps to the SparseArray's key and vice
 * versa. Any method requiring a key will have <i>"withId"</i> in the name.
 * <p/>
 * If filtering is required, it's strongly recommended to use the {@link ArrayBaseAdapter} instead.
 */
public abstract class SimpleSparseArrayBaseAdapter<T> extends BaseAdapter {

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/**
	 * Contains the sparse array of objects that represent the visible data of the adapter. It's
	 * contents will change as filtering occurs. All methods retrieving data about the adapter will
	 * always do so from this sparse array.
	 */
	private SparseArray<T> mObjects;
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
	public SimpleSparseArrayBaseAdapter(Context activity) {
		init(activity, null);
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public SimpleSparseArrayBaseAdapter(Context activity, SparseArray<T> items) {
		init(activity, items);
	}

	/**
	 * Appends the specified SparseArray at the end of the adapter, optimizing for the case where
	 * all the keys are greater then all existing keys in the adapter. This includes the given items
	 * having it's keys in sequential order as well.
	 *
	 * @param items The SparseArray items to add at the end of the adapter.
	 */
	public void appendAll(SparseArray<T> items) {
		for (int index = 0; index < items.size(); ++index) {
			mObjects.append(items.keyAt(index), items.valueAt(index));
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Appends the specified key and item pair to the end of the adapter, optimizing for the case
	 * where the key is greater then all existing keys in the adapter.
	 *
	 * @param keyId The keyId to append with
	 * @param item  The item to append with
	 */
	public void appendWithId(int keyId, T item) {
		mObjects.append(keyId, item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all items from the adapter.
	 */
	public void clear() {
		mObjects.clear();
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Determines if the specified keyId exists within the adapter.
	 *
	 * @param keyId The keyId to search for
	 *
	 * @return {@code true} if the keyId is found within the adapter. {@code false} otherwise.
	 */
	public boolean containsId(int keyId) {
		return mObjects.indexOfKey(keyId) >= 0;
	}

	/**
	 * Determines if the specified item exists within the adapter. Be aware that this is a linear
	 * search, unlike look-ups by key, and that multiple keys can map to the same value and this
	 * will find only one of them.
	 * <p/>
	 * Note also that unlike most collections this method compares values using == rather than
	 * equals...a result of how SparseArrays are implemented.
	 *
	 * @param item The item to search for
	 *
	 * @return {@code true} if the item is an element of this adapter. {@code false} otherwise
	 */
	public boolean containsItem(T item) {
		return mObjects.indexOfValue(item) >= 0;
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
	public View getDropDownView(LayoutInflater inflater, int position, View convertView,
								ViewGroup parent) {
		return getView(inflater, position, convertView, parent);
	}

	@Override
	public final View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getDropDownView(mInflater, position, convertView, parent);
	}

	@Override
	public T getItem(int position) {
		return mObjects.valueAt(position);
	}

	@Override
	public long getItemId(int position) {
		return mObjects.keyAt(position);
	}

	/**
	 * @return The Item mapped by the keyId or null if no such mapping has been made
	 */
	public T getItemWithId(int keyId) {
		return mObjects.get(keyId);
	}

	/**
	 * Returns the position of the specified item in the sparse array. Be aware that this is a
	 * linear search, unlike look-ups by key,and that multiple keys can map to the same value and
	 * this will find only one of them.
	 * <p/>
	 * Note also that unlike most collections, this method compares values using == rather than
	 * equals.
	 *
	 * @param item The item to retrieve the position of.
	 *
	 * @return The position of the specified item, or a negative number if not found.
	 */
	public int getPosition(T item) {
		return mObjects.indexOfValue(item);
	}

	/**
	 * @param keyId The keyId to search for
	 *
	 * @return the position for which a keyId is found, or a negative number if not found.
	 */
	public int getPosition(int keyId) {
		return mObjects.indexOfKey(keyId);
	}

	/**
	 * @return The SparseArray of items stored within the adapter.
	 */
	public SparseArray<T> getSparseArray() {
		return mObjects.clone();
	}

	/**
	 * Get a View that displays the data at the specified position in the data set.  You can either
	 * create a View manually or inflate it from an XML layout file.  When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you {@link
	 * android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean) to specifiy a root
	 * view and to prevent attachment to the root.}
	 *
	 * @param inflater    the LayoutInflater object that can be used to inflate each view.
	 * @param position    The position of the item within the adapter's data set of the item whose
	 *                    view we want
	 * @param convertView the old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible
	 *                    to convert this view to display the correct data, this method can create a
	 *                    new view.
	 * @param parent      the parent that this view will eventually be attached to
	 *
	 * @return A View corresponding to the data at the specified position
	 */
	public abstract View getView(LayoutInflater inflater, int position, View convertView,
								 ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(Context context, SparseArray<T> objects) {
		mInflater = LayoutInflater.from(context);
		if (objects == null) {
			mObjects = new SparseArray<>();
		} else {
			mObjects = objects.clone();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Given a position in the range of 0...{@link #getCount()} - 1, sets a new value for the
	 * key-value stored at that position.
	 *
	 * @param position The position of the item to update
	 * @param item     The item to update with
	 */
	public void put(int position, T item) {
		mObjects.setValueAt(position, item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified SparseArray to the adapter, replacing any existing mappings from the
	 * specified keys if there were any.
	 *
	 * @param items The SparseArray items to add to the adapter.
	 */
	public void putAll(SparseArray<T> items) {
		for (int index = 0; index < items.size(); ++index) {
			mObjects.put(items.keyAt(index), items.valueAt(index));
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified key and item to the adapter, replacing the previous mapping from the
	 * specified key if there was one.
	 */
	public void putWithId(int keyId, T item) {
		mObjects.put(keyId, item);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes the mapping at the specified position in the adapter.
	 *
	 * @param position The position of the item to remove
	 */
	public void remove(int position) {
		mObjects.removeAt(position);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all items from the adapter that are found within the specified SparseArray.
	 *
	 * @param items The SparseArray items to remove from the adapter.
	 */
	public void removeAll(SparseArray<T> items) {
		for (int index = 0; index < items.size(); ++index) {
			mObjects.delete(items.keyAt(index));
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes the mapping with the specified keyId from the adapter.
	 *
	 * @param keyId The keyId to remove.
	 */
	public void removeWithId(int keyId) {
		mObjects.delete(keyId);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Controls whether methods that change the list ({@link #appendWithId}, {@link #putWithId},
	 * {@link #remove}, {@link #clear}) automatically call {@link #notifyDataSetChanged}.  If set to
	 * false, caller must manually call notifyDataSetChanged() to have the changes reflected in the
	 * attached view.
	 * <p/>
	 * The default is true, and calling notifyDataSetChanged() resets the flag to true.
	 *
	 * @param notifyOnChange if true, modifications to the list will automatically call {@link
	 *                       #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}
}