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
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * A custom abstract {@link BaseAdapter} that is backed by a {@link SparseArray} of arbitrary
 * objects.  By default this class delegates view generation and defining the filtering logic to
 * subclasses.
 * <p/>
 * Designed to be a flexible and customizable solution for using SparseArray with an adapter. It
 * exposes most of the SparseArray methods, provides active filtering support, and conveniently
 * passes along a layout inflater for view creation. An adapter's row ID maps to the SparseArray's
 * key and vice versa. Any method requiring a key will have <i>"withId"</i> in the name.
 * <p/>
 * Because of the background filtering process, all methods which mutates the underlying data are
 * internally synchronized. This ensures a thread safe environment for internal write operations. If
 * filtering is not required, it's strongly recommended to use the {@link
 * NFSparseAdapter} instead.
 */
public abstract class SparseAdapter<T> extends BaseAdapter implements Filterable {
	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation performed on the
	 * sparse array should be synchronized on this lock. This lock is also used by the filter (see
	 * {@link #getFilter()} to make a synchronized copy of the original array of data.
	 */
	private final Object mLock = new Object();

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
	 * A copy of the original mObjects sparse array, is not initialized until a filtering processing
	 * occurs. Once initialized, it'll track the entire unfiltered data. Once the filter process
	 * completes, it's contents are copied back over to mObjects and is set to null.
	 */
	private SparseArray<T> mOriginalValues;
	private SparseArrayFilter mFilter;

	/**
	 * Saves the constraint used during the last filtering operation. Used to re-filter the sparse
	 * array following changes to the data
	 */
	private CharSequence mLastConstraint;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public SparseAdapter(Context activity) {
		init(activity, null);
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public SparseAdapter(Context activity, SparseArray<T> items) {
		init(activity, items);
	}

	/**
	 * Appends the specified SparseArray at the end of the adapter, optimizing for the case where
	 * all the keys are greater then all existing keys in the adapter. This includes the given items
	 * having it's keys in sequential order as well. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items The SparseArray items to add at the end of the adapter.
	 */
	public void appendAll(SparseArray<T> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < items.size(); ++index) {
					mOriginalValues.append(items.keyAt(index), items.valueAt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < items.size(); ++index) {
					mObjects.append(items.keyAt(index), items.valueAt(index));
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Appends the specified key and item pair to the end of the adapter, optimizing for the case
	 * where the key is greater then all existing keys in the adapter. Will repeat the last
	 * filtering request if invoked while filtered results are being displayed.
	 *
	 * @param keyId The keyId to append with
	 * @param item  The item to append with
	 */
	public void appendWithId(int keyId, T item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.append(keyId, item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.append(keyId, item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all items from the adapter.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) mOriginalValues.clear();
			mObjects.clear();
		}
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
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new SparseArrayFilter();
		}
		return mFilter;
	}

	/**
	 * @return The shown filtered data as a SparseArray. If no filter is applied, then the original
	 * (unfiltered) data is returned instead.
	 */
	public SparseArray<T> getFilteredSparseArray() {
		SparseArray<T> objects;
		synchronized (mLock) {
			objects = mObjects.clone();
		}
		return objects;
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
	 * @return The original (unfiltered) SparseArray of items stored within the adapter.
	 */
	public SparseArray<T> getSparseArray() {
		SparseArray<T> objects;
		synchronized (mLock) {
			if (mOriginalValues != null) {
				objects = mOriginalValues.clone();
			} else {
				objects = mObjects.clone();
			}
		}
		return objects;
	}

	/**
	 * Resets the adapter to store a new SparseArray of items. Though equivalent to calling {@link
	 * #clear()}, followed by {@link #putAll}; this method is more efficient. It performs a straight
	 * clone of the items instead of having to perform a binary search before putting each item
	 * within the adapter. Will repeat the last filtering request if invoked while filtered results
	 * are being displayed.
	 *
	 * @param items New SparseArray of items to store within the adapter.
	 */
	public void setSparseArray(SparseArray<T> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
				mOriginalValues = items.clone();
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.clear();
				mObjects = items.clone();
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
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

	/**
	 * Determines whether the provided constraint filters out the given item. Allows easy,
	 * customized filtering for subclasses. It's incorrect to modify the adapter or the contents of
	 * the item itself. Any alterations will lead to undefined behavior or crashes. Internally, this
	 * method is only ever invoked from a background thread. Do not make UI changes from here!
	 *
	 * @param item       The item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item passes the filtered constraint and continues to display. False if
	 * the item does not pass the filter check and is not displayed.
	 */
	protected abstract boolean isFilteredBy(int keyId, T item, CharSequence constraint);

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Given a position in the range of 0...{@link #getCount()} - 1, sets a new value for the
	 * key-value stored at that position. Be aware this method is only a constant amortised time
	 * operation when the adapter is not filtered. Otherwise, the position must be converted to a
	 * unfiltered position; which requires a binary search on the original unfiltered data.
	 *
	 * @param position The position of the item to update
	 * @param item     The item to update with
	 */
	public void put(int position, T item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				int newPosition = mOriginalValues.indexOfKey(mObjects.keyAt(position));
				mOriginalValues.setValueAt(newPosition, item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.setValueAt(position, item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified SparseArray to the adapter, replacing any existing mappings from the
	 * specified keys if there were any.  Will repeat the last filtering request if invoked while
	 * filtered results are being displayed.
	 *
	 * @param items The SparseArray items to add to the adapter.
	 */
	public void putAll(SparseArray<T> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < items.size(); ++index) {
					mOriginalValues.put(items.keyAt(index), items.valueAt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < items.size(); ++index) {
					mObjects.put(items.keyAt(index), items.valueAt(index));
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified key and item to the adapter, replacing the previous mapping from the
	 * specified key if there was one. Will repeat the last filtering request if invoked while
	 * filtered results are being displayed.
	 */
	public void putWithId(int keyId, T item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(keyId, item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(keyId, item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes the mapping at the specified position in the adapter.  Be aware this method is only a
	 * constant amortised time operation when the adapter is not filtered. Otherwise, the position
	 * must be converted to a unfiltered position; which requires a binary search on the original
	 * unfiltered sparse array.
	 *
	 * @param position The position of the item to remove
	 */
	public void remove(int position) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				int newPosition = mOriginalValues.indexOfKey(mObjects.keyAt(position));
				mOriginalValues.removeAt(newPosition);
			}
			mObjects.removeAt(position);
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all items from the adapter that are found within the specified SparseArray.
	 *
	 * @param items The SparseArray items to remove from the adapter.
	 */
	public void removeAll(SparseArray<T> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < items.size(); ++index) {
					mOriginalValues.delete(items.keyAt(index));
				}
			}
			for (int index = 0; index < items.size(); ++index) {
				mObjects.delete(items.keyAt(index));
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes the mapping with the specified keyId from the adapter.
	 *
	 * @param keyId The keyId to remove.
	 */
	public void removeWithId(int keyId) {
		synchronized (mLock) {
			if (mOriginalValues != null) mOriginalValues.delete(keyId);
			mObjects.delete(keyId);
		}
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

	/**
	 * A SparseArray filter constrains the content of the adapter. Whether an item is constrained or
	 * not is delegated to subclasses through {@link SparseAdapter#isFilteredBy}
	 */
	private class SparseArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			final SparseArray<T> values;

			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = mOriginalValues.clone();
						mOriginalValues = null;
					}
					results.values = mObjects;
					results.count = mObjects.size();
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = mObjects.clone();
					}
					values = mOriginalValues.clone();
				}
			}

			final SparseArray<T> newValues = new SparseArray<>();
			for (int index = 0; index < values.size(); ++index) {
				if (isFilteredBy(values.keyAt(index), values.valueAt(index), constraint)) {
					newValues.put(values.keyAt(index), values.valueAt(index));
				}
			}

			results.values = newValues;
			results.count = newValues.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mLastConstraint = constraint;
			mObjects = (SparseArray<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}