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
 * A custom abstract {@link android.widget.BaseAdapter} that is backed by an {@link
 * java.util.ArrayList} of arbitrary objects.  By default this class delegates view generation and
 * defining the filtering logic to subclasses.
 * <p/>
 * Designed to be a more flexible and customizable solution then Android's ArrayAdapter class. It
 * provides extra features such as: supporting additional {@link java.util.ArrayList} methods,
 * resolves outstanding filtering bugs, makes smarter use of {@link #notifyDataSetChanged()}, and
 * conveniently passes along a layout inflater for view creation.
 * <p/>
 * Because of the background filtering process, all methods which mutates the underlying data are
 * internally synchronized. This ensures a thread safe environment for internal write operations. If
 * filtering is not required, it's strongly recommended to use the {@link
 * com.sawyer.advadapters.widget.SimpleArrayBaseAdapter} instead.
 */
public abstract class SparseArrayBaseAdapter<T> extends BaseAdapter implements Filterable {
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
	private SparseArray<T> mObjects;
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
	private SparseArray<T> mOriginalValues;
	private SparseArrayFilter mFilter;

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
	public SparseArrayBaseAdapter(Context activity) {
		init(activity, new SparseArray<T>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param objects  The objects to represent within the adapter.
	 */
	public SparseArrayBaseAdapter(Context activity, SparseArray<T> objects) {
		init(activity, objects);
	}

	/**
	 * Adds the specified object at the end of the adapter. Will repeat the last filtering request
	 * if invoked while filtered results are being displayed.
	 *
	 * @param object The object to add at the end of the adapter.
	 */
	public void append(int keyId, T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.append(keyId, object);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.append(keyId, object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	public void appendAll(SparseArray<T> objects) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < objects.size(); ++index) {
					mOriginalValues.append(objects.keyAt(index), objects.valueAt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < objects.size(); ++index) {
					mObjects.append(objects.keyAt(index), objects.valueAt(index));
				}
			}
			if (mNotifyOnChange) notifyDataSetChanged();
		}
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

	public boolean containsId(int keyId) {
		return mObjects.indexOfKey(keyId) >= 0;
	}

	/**
	 * Tests whether this adapter contains the specified object
	 *
	 * @param object The object to search for
	 *
	 * @return {@code true} if the object is an element of this adapter. {@code false} otherwise
	 */
	public boolean containsItem(T object) {
		return mObjects.indexOfValue(object) >= 0;
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
	 * @return The shown filtered list. If no filter is applied, then the original list is returned.
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

	public T getItemWithId(int keyId) {
		return mObjects.get(keyId);
	}

	/**
	 * @return The original (unfiltered) list of objects stored within the Adapter
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
	 * Resets the adapter to store a new list of objects. Convenient way of calling {@link
	 * #clear()}, then {@link # (java.util.Collection)} without having to worry about an extra
	 * {@link #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param objects New list of objects to store within the adapter.
	 */
	public void setSparseArray(SparseArray<T> objects) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
				mOriginalValues = objects.clone();
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.clear();
				mObjects = objects.clone();
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	public abstract View getView(LayoutInflater inflater, int position, View convertView,
								 ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(Context context, SparseArray<T> objects) {
		mInflater = LayoutInflater.from(context);
		mObjects = objects.clone();
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
	protected abstract boolean isFilteredBy(int keyId, T object, CharSequence constraint);

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Adds the specified Collection at the end of the adapter. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 */
	public void put(int keyId, T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(keyId, object);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(keyId, object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	public void putAll(SparseArray<T> objects) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < objects.size(); ++index) {
					mOriginalValues.put(objects.keyAt(index), objects.valueAt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < objects.size(); ++index) {
					mObjects.put(objects.keyAt(index), objects.valueAt(index));
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	public void remove(int position) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.removeAt(position);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.removeAt(position);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	public void removeAll(SparseArray<T> objects) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < objects.size(); ++index) {
					mOriginalValues.delete(objects.keyAt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < objects.size(); ++index) {
					mObjects.delete(objects.keyAt(index));
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes the first occurrence of the specified object from the adapter. Will repeat the last
	 * filtering request if invoked while filtered results are being displayed.
	 *
	 * @param keyId The object to remove.
	 */
	public void removeWithId(int keyId) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.delete(keyId);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.delete(keyId);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Control whether methods that change the list ({@link #append}, {@link #put}, {@link #remove},
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
	 * An array filter constrains the content of the array adapter. Whether an item is constrained
	 * or not is delegated to subclasses through {@link com.sawyer.advadapters.widget.SparseArrayBaseAdapter#isFilteredBy}
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