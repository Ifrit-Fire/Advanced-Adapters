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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A custom abstract {@link BaseAdapter} that is backed by an {@link ArrayList} of arbitrary
 * objects.  By default this class delegates view generation and defining the filtering logic to
 * subclasses.
 * <p/>
 * Designed to be a more flexible and customizable solution then Android's ArrayAdapter class. It
 * provides extra features such as: supporting additional {@link ArrayList} methods, resolves
 * outstanding filtering bugs, makes smarter use of {@link #notifyDataSetChanged()}, and
 * conveniently passes along a layout inflater for view creation.
 * <p/>
 * Because of the background filtering process, all methods which mutates the underlying data are
 * internally synchronized. This ensures a thread safe environment for internal write operations. If
 * filtering is not required, it's strongly recommended to use the {@link SimpleArrayBaseAdapter}
 * instead.
 */
public abstract class ArrayBaseAdapter<T> extends BaseAdapter implements Filterable {
	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation performed on the
	 * array should be synchronized on this lock. This lock is also used by the filter (see {@link
	 * #getFilter()} to make a synchronized copy of the original array of data.
	 */
	private final Object mLock = new Object();

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/**
	 * Contains the list of objects that represent the visible data of the adapter. It's contents
	 * will change as filtering occurs. All methods retrieving data about the adapter will always do
	 * so from this list.
	 */
	private ArrayList<T> mObjects;
	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever {@link
	 * #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;
	/**
	 * A copy of the original mObjects array, is not initialized until a filtering processing
	 * occurs. Once initialized, it'll track the entire unfiltered data. Once the filter process
	 * completes, it's contents are copied back over to mObjects and is set to null.
	 */
	private ArrayList<T> mOriginalValues;
	private ArrayFilter mFilter;

	/**
	 * Saves the constraint used during the last filtering operation. Used to re-filter the list
	 * following changes to the array of data
	 */
	private CharSequence mLastConstraint;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public ArrayBaseAdapter(Context activity) {
		init(activity, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param objects  The objects to represent within the adapter.
	 */
	public ArrayBaseAdapter(Context activity, T[] objects) {
		List<T> list = Arrays.asList(objects);
		if (list instanceof ArrayList) {    //Should always be true...but just in case implementation changes
			init(activity, (ArrayList<T>) list);
		} else {
			init(activity, new ArrayList<>(list));
		}
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param objects  The objects to represent within the adapter.
	 */
	public ArrayBaseAdapter(Context activity, Collection<T> objects) {
		init(activity, new ArrayList<>(objects));
	}

	/**
	 * Adds the specified object at the end of the adapter. Will repeat the last filtering request
	 * if invoked while filtered results are being displayed.
	 *
	 * @param object The object to add at the end of the adapter.
	 */
	public void add(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.add(object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the adapter. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 *
	 * @param collection The Collection to add at the end of the adapter.
	 */
	public void addAll(Collection<? extends T> collection) {
		boolean isModified;

		synchronized (mLock) {
			if (mOriginalValues != null) {
				isModified = mOriginalValues.addAll(collection);
				if (isModified) getFilter().filter(mLastConstraint);
			} else {
				isModified = mObjects.addAll(collection);
			}
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items The items to add at the end of the adapter.
	 */
	@SafeVarargs
	public final void addAll(T... items) {
		boolean isModified;

		synchronized (mLock) {
			if (mOriginalValues != null) {
				isModified = Collections.addAll(mOriginalValues, items);
				if (isModified) getFilter().filter(mLastConstraint);
			} else {
				isModified = Collections.addAll(mObjects, items);
			}
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Remove all elements from the adapter.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) mOriginalValues.clear();
			mObjects.clear();
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Tests whether this adapter contains the specified object. Be aware that this is a linear
	 * search.
	 *
	 * @param object The object to search for
	 *
	 * @return {@code true} if the object is an element of this adapter. {@code false} otherwise
	 */
	public boolean contains(T object) {
		return mObjects.contains(object);
	}

	/**
	 * Tests whether this adapter contains all objects contained in the specified collection.  Be
	 * aware that this performs a nested for loop search...eg O(n*m) complexity.
	 *
	 * @param collection The collection of objects
	 *
	 * @return {@code true} if all objects in the specified collection are elements of this adapter,
	 * {@code false} otherwise
	 */
	public boolean containsAll(Collection<?> collection) {
		return mObjects.containsAll(collection);
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
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * @return The shown filtered list. If no filter is applied, then the original list is returned.
	 */
	public ArrayList<T> getFilteredList() {
		ArrayList<T> objects;
		synchronized (mLock) {
			objects = new ArrayList<>(mObjects);
		}
		return objects;
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
	 * @return The original (unfiltered) list of objects stored within the Adapter
	 */
	public ArrayList<T> getList() {
		ArrayList<T> objects;
		synchronized (mLock) {
			if (mOriginalValues != null) {
				objects = new ArrayList<>(mOriginalValues);
			} else {
				objects = new ArrayList<>(mObjects);
			}
		}
		return objects;
	}

	/**
	 * Resets the adapter to store a new list of objects. Convenient way of calling {@link
	 * #clear()}, then {@link #addAll(java.util.Collection)} without having to worry about an extra
	 * {@link #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param objects New list of objects to store within the adapter.
	 */
	public void setList(Collection<? extends T> objects) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
				mOriginalValues.addAll(objects);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.clear();
				mObjects.addAll(objects);
			}
		}
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
	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	public abstract View getView(LayoutInflater inflater, int position, View convertView,
								 ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(Context context, ArrayList<T> objects) {
		mInflater = LayoutInflater.from(context);
		mObjects = objects;
	}

	/**
	 * Determines whether the provided constraint filters out the given object. Allows easy,
	 * customized filtering for subclasses. It's incorrect to modify the adapter or the contents of
	 * the object itself. Any alterations will lead to undefined behavior or crashes. Internally,
	 * this method is only ever invoked from a background thread.
	 *
	 * @param object     The object to compare against the constraint
	 * @param constraint The constraint used to filter the object
	 *
	 * @return True if the object is filtered out by the constraint. False if the object is not
	 * filtered and will continue to reside in the adapter.
	 */
	protected abstract boolean isFilteredBy(T object, CharSequence constraint);

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Removes the first occurrence of the specified object from the adapter. Will repeat the last
	 * filtering request if invoked while filtered results are being displayed.
	 *
	 * @param object The object to remove.
	 */
	public void remove(T object) {
		boolean isModified = false;

		synchronized (mLock) {
			if (mOriginalValues != null) isModified = mOriginalValues.remove(object);
			isModified |= mObjects.remove(object);
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all occurrences in the adapter of each object in the specified collection. Will
	 * repeat the last filter operation if one occurred.
	 *
	 * @param collection The collection of objects to remove
	 */
	public void removeAll(Collection<?> collection) {
		boolean isModified = false;
		synchronized (mLock) {
			if (mOriginalValues != null) isModified = mOriginalValues.removeAll(collection);
			isModified |= mObjects.removeAll(collection);
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all objects from this adapter that are not contained in the specified collection.
	 * Will repeat the last filtering request if invoked while filtered results are being
	 * displayed.
	 *
	 * @param collection The collection of objects to retain
	 */
	public void retainAll(Collection<?> collection) {
		boolean isModified = false;

		synchronized (mLock) {
			if (mOriginalValues != null) isModified = mOriginalValues.retainAll(collection);
			isModified |= mObjects.retainAll(collection);
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Control whether methods that change the list ({@link #add}, {@link #retainAll}, {@link
	 * #remove}, {@link #clear}) automatically call {@link #notifyDataSetChanged}.  If set to false,
	 * caller must manually call notifyDataSetChanged() to have the changes reflected in the
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
	 * Sorts the content of this adapter using the natural order of the stored objects themselves.
	 * This requires objects to have implemented {@link java.lang.Comparable} and is equivalent of
	 * passing null to {@link #sort(java.util.Comparator)}.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored objects do not
	 *                                      implement <code>Comparable</code> or if
	 *                                      <code>compareTo</code> throws for any pair of objects.
	 */
	public void sort() {
		sort(null);
	}

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator Used to sort the objects contained in this adapter. Null to use an object's
	 *                   <code>Comparable</code> interface.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored objects do not
	 *                                      implement <code>Comparable</code> or if
	 *                                      <code>compareTo</code> throws for any pair of objects.
	 */
	public void sort(Comparator<? super T> comparator) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.sort(mOriginalValues, comparator);
			}
			Collections.sort(mObjects, comparator);
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Updates the object at the specified position in the adapter with the specified object. This
	 * operation does not change the size of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed. Be-aware this method is only a constant
	 * time operation when the list is not filtered. Otherwise, the position must be converted to a
	 * unfiltered position; which requires traversing the original unfiltered list.
	 *
	 * @param position The location at which to put the specified object
	 * @param object   The new object to replace with the old
	 */
	public void update(int position, T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				int newPosition = mOriginalValues.indexOf(mObjects.get(position));
				mOriginalValues.set(newPosition, object);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.set(position, object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * An array filter constrains the content of the array adapter. Whether an item is constrained
	 * or not is delegated to subclasses through {@link ArrayBaseAdapter#isFilteredBy(Object,
	 * CharSequence)}
	 */
	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			final ArrayList<T> values;

			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = new ArrayList<>(mOriginalValues);
						mOriginalValues = null;
					}
					results.values = mObjects;
					results.count = mObjects.size();
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = new ArrayList<>(mObjects);
					}
					values = new ArrayList<>(mOriginalValues);
				}
			}

			final ArrayList<T> newValues = new ArrayList<>();
			for (T value : values) {
				if (isFilteredBy(value, constraint)) {
					newValues.add(value);
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
			mObjects = (ArrayList<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}