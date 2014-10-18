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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom abstract {@link BaseAdapter} that is backed by a {@link JSONArray} of arbitrary objects.
 * By default this class delegates view generation and defining the filtering logic to subclasses.
 * <p/>
 * Designed to be a flexible and customizable solution for using JSONArray with an adapter. It
 * exposes most of the JSONArray methods, provides active filtering support, and conveniently passes
 * along a layout inflater for view creation. Keep in mind JSONArray itself has limited capabilities
 * which restricts what this adapter can do.
 * <p/>
 * Because of the background filtering process, all methods which mutates the underlying data are
 * internally synchronized. This ensures a thread safe environment for internal write operations. If
 * filtering is not required, it's strongly recommended to use the {@link NFJSONArrayAdapter}
 * instead. In order to support the multitude of data types a JSONArray can store, the filtering
 * mechanism is built to be very robust.
 * <p/>
 * Only the {@link #isFilteredOut(Object, CharSequence)} is required to be implemented by
 * subclasses. Pre-built methods for handing the logic for Boolean, Double, Integer, Long, and
 * String already exist. You may override them to provide an alternate behavior. In addition, you
 * can provide your own filtered methods for custom data types. During adapter construction, any
 * methods which match the {@code isFilteredOut} method signature are cached.  Then during a filter
 * operation, will be invoked via reflection when required.
 * <p/>
 * For example, lets say you created your own object type called Foo. You've stored several
 * instances of Foo within the adapter along with JSONObjects, Integers, and Booleans. In order to
 * specifically support a {@code isFilteredOut} method for your Foo object, have your subclass
 * implement a {@code isFilteredOut(Foo, CharSequence)}. Then during a filter operation, any Foo
 * object detected will have that method invoke to determine whether it passes the filter or not.
 */
public abstract class JSONAdapter extends BaseAdapter implements Filterable {
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
	private JSONArray mObjects;
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
	private JSONArray mOriginalValues;
	/**
	 * Saves the constraint used during the last filtering operation. Used to re-filter the list
	 * following changes to the array of data
	 */
	private CharSequence mLastConstraint;
	/**
	 * Initialized at startup, caches all the Method's which can be used for filtering data. Key is
	 * based on the object's name (including package). Eg, java.lang.Integer. Will also store the
	 * filtered methods defined in this class.
	 */
	private Map<String, Method> mFilterMethods;
	private JSONArrayFilter mFilter;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public JSONAdapter(Context activity) {
		init(activity, new JSONArray());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param readFrom a tokener whose nextValue() method will yield a {@code JSONArray} to be
	 *                 stored in the adapter.
	 *
	 * @throws JSONException if the parse fails or doesn't yield a {@code JSONArray}.
	 */
	public JSONAdapter(Context activity, JSONTokener readFrom) throws JSONException {
		init(activity, new JSONArray(readFrom));
	}

	/**
	 * * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param json     a JSON-encoded string containing an array to be stored in the adapter
	 *
	 * @throws JSONException if the parse fails or doesn't yield a {@code JSONArray}.
	 */
	public JSONAdapter(Context activity, String json) throws JSONException {
		init(activity, new JSONArray(json));
	}

	public JSONAdapter(Context activity, JSONArray array) {
		init(activity, generateCopy(array));
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public JSONAdapter(Context activity, Collection items) {
		init(activity, new JSONArray(items));
	}

	/**
	 * Creates a new {@code JSONArray} with values from another. Adds backward support as this only
	 * exists in API19.
	 */
	private static JSONArray generateCopy(JSONArray array) {
		JSONArray copy = new JSONArray();
		for (int i = 0; i < array.length(); ++i) {
			Object object = array.opt(i);
			if (object != null) {
				copy.put(object);
			}
		}
		return copy;
	}

	/**
	 * Determines whether the given method has he proper signature of a isFiltered method.
	 * Specifically looking for the following: <ul><li>Name equals <i>"isFilteredOut"</i></li>
	 * <li>Returns a primitive boolean</li> <li>Has exactly 2 parameters</li> <li>The 2nd param is a
	 * CharSequence</li> </ul> If the method matches the criteria, the first parameter is extracted
	 * and returned as a string to be used as a key in the filter cache.
	 *
	 * @param m Method to check signature of.
	 *
	 * @return String value of a filtered methods 1st parameter. Null if the method does not have
	 * the proper signature.
	 */
	private static String getFilterMethodKey(Method m) {
		if ("isFilteredOut".equals(m.getName()) && m.getGenericReturnType().equals(boolean.class)) {
			Type[] params = m.getGenericParameterTypes();
			if (params.length == 2 && params[1].equals(CharSequence.class)) {
				String[] split = params[0].toString().split("\\s+");
				return split[split.length - 1];
			}
		}
		return null;
	}

	/**
	 * Adds the specified items at the end of the adapter. May not be NaNs or infinities which will
	 * cause the adapter to be in an inconsistent state. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(Object item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. May not be NaNs or infinities. Will
	 * repeat the last filtering request if invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 *
	 * @throws JSONException If item is NaN or infinity
	 */
	public void add(double item) throws JSONException {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(long item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(boolean item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(int item) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.put(item);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.put(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified JSONArray at the end of the adapter. May not contain NaNs or infinities
	 * which will cause the adapter to be in an inconsistent state. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 *
	 * @param items The JSONArray to add at the end of the adapter.
	 */
	public void addAll(JSONArray items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (int index = 0; index < items.length(); ++index) {
					mOriginalValues.put(items.opt(index));
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (int index = 0; index < items.length(); ++index) {
					mObjects.put(items.opt(index));
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. May not contain NaNs or infinities which
	 * will cause the adapter to be in an inconsistent state. Will repeat the last filtering request
	 * if invoked while filtered results are being displayed.
	 *
	 * @param items The items to add at the end of the adapter.
	 */
	public void addAll(Object... items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (Object object : items) {
					mOriginalValues.put(object);
				}
				getFilter().filter(mLastConstraint);
			} else {
				for (Object object : items) {
					mObjects.put(object);
				}
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Caches the predefined isFilteredOut methods for later invocation.
	 */
	private void cacheKnownFilteredMethods() {
		try {
			Method m;
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", Boolean.class, CharSequence.class);
			mFilterMethods.put(Boolean.class.getName(), m);
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", Double.class, CharSequence.class);
			mFilterMethods.put(Double.class.getName(), m);
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", Integer.class, CharSequence.class);
			mFilterMethods.put(Integer.class.getName(), m);
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", Long.class, CharSequence.class);
			mFilterMethods.put(Long.class.getName(), m);
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", String.class, CharSequence.class);
			mFilterMethods.put(String.class.getName(), m);
			m = JSONAdapter.class
					.getDeclaredMethod("isFilteredOut", Object.class, CharSequence.class);
			mFilterMethods.put(Object.class.getName(), m);
		} catch (NoSuchMethodException e) {
			Log.e(JSONAdapter.this.getClass().getSimpleName(),
				  "Error attempting to cache known filtered methods. Did you configure proguard?");
		}
	}

	/**
	 * Scans all subclasses of this instance for any isFilteredOut methods and caches them for later
	 * invocation.
	 */
	private void cacheSubclassFilteredMethods() {
		//Scan public methods first
		Class<?> c = this.getClass();
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			String key = getFilterMethodKey(m);
			if (key != null) mFilterMethods.put(key, m);
		}

		//Scan non-public methods next
		while (!c.equals(JSONAdapter.class)) {
			methods = c.getDeclaredMethods();
			for (Method m : methods) {
				String key = getFilterMethodKey(m);
				if (key != null) {
					m.setAccessible(true);
					mFilterMethods.put(key, m);
				}
			}
			c = c.getSuperclass();
		}
	}

	/**
	 * Remove all elements from the adapter.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) mOriginalValues = new JSONArray();
			mObjects = new JSONArray();
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mObjects.length();
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
			mFilter = new JSONArrayFilter();
		}
		return mFilter;
	}

	/**
	 * @return The shown filtered JSONArray. If no filter is applied, then the original JSONArray is
	 * returned.
	 */
	public JSONArray getFilteredJSONArray() {
		JSONArray objects;
		synchronized (mLock) {
			objects = generateCopy(mObjects);
		}
		return objects;
	}

	@Override
	public Object getItem(int position) {
		Object object = mObjects.opt(position);
		if (object == null) {
			//A pain but can't add throws to this overridden method
			if (position < 0 || position >= mObjects.length()) {
				throw new IndexOutOfBoundsException();
			} else {
				throw new NullPointerException();
			}
		}
		return object;
	}

	/**
	 * Gets the boolean data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a boolean.
	 *
	 * @throws JSONException If the value at position doesn't exit or cannot be coerced to a
	 *                       boolean.
	 */
	public boolean getItemBoolean(int position) throws JSONException {
		return mObjects.getBoolean(position);
	}

	/**
	 * Gets the double data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a double.
	 *
	 * @throws JSONException If the value at position doesn't exit or cannot be coerced to a
	 *                       double.
	 */
	public double getItemDouble(int position) throws JSONException {
		return mObjects.getDouble(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Gets the int data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a int.
	 *
	 * @throws JSONException If the value at position doesn't exit or cannot be coerced to a int.
	 */
	public int getItemInt(int position) throws JSONException {
		return mObjects.getInt(position);
	}

	/**
	 * Gets the JSONArray data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a JSONArray.
	 *
	 * @throws JSONException If the value at position doesn't exit or is not a JSONArray.
	 */
	public JSONArray getItemJSONArray(int position) throws JSONException {
		return mObjects.getJSONArray(position);
	}

	/**
	 * Gets the JSONObject data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a JSONObject.
	 *
	 * @throws JSONException If the value at position doesn't exit or is not a JSONObject.
	 */
	public JSONObject getItemJSONObject(int position) throws JSONException {
		return mObjects.getJSONObject(position);
	}

	/**
	 * Gets the long data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a long.
	 *
	 * @throws JSONException If the value at position doesn't exit or cannot be coerced to a long.
	 */
	public long getItemLong(int position) throws JSONException {
		return mObjects.getLong(position);
	}

	/**
	 * Gets the String data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position as a String.
	 *
	 * @throws JSONException If no such value exists.
	 */
	public String getItemString(int position) throws JSONException {
		return mObjects.getString(position);
	}

	/**
	 * @return The original (unfiltered) list of items stored within the Adapter
	 */
	public JSONArray getJSONArray() {
		JSONArray objects;
		synchronized (mLock) {
			if (mOriginalValues != null) {
				objects = generateCopy(mOriginalValues);
			} else {
				objects = generateCopy(mObjects);
			}
		}
		return objects;
	}

	/**
	 * Resets the adapter to store a new JSONArray of items. Convenient way of calling {@link
	 * #clear()}, then {@link #addAll(org.json.JSONArray)} without having to worry about an extra
	 * {@link #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items New JSONArray of items to store within the adapter.
	 */
	public void setJSONArray(JSONArray items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues = generateCopy(items);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects = generateCopy(items);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
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
	public abstract View getView(LayoutInflater inflater, int position, View convertView,
								 ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(Context context, JSONArray objects) {
		mInflater = LayoutInflater.from(context);
		mObjects = objects;
		mFilterMethods = new HashMap<>();
		cacheKnownFilteredMethods();
		cacheSubclassFilteredMethods();
	}

	/**
	 * Determines whether the provided constraint filters out the given item. Subclass to provide
	 * you're own logic. It's incorrect to modify the adapter or the contents of the item itself.
	 * Any alterations will lead to undefined behavior or crashes. Internally, this method is only
	 * ever invoked from a background thread.
	 *
	 * @param item       The Boolean item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected boolean isFilteredOut(Boolean item, CharSequence constraint) {
		return !item.toString().equalsIgnoreCase(constraint.toString());
	}

	/**
	 * Determines whether the provided constraint filters out the given item. Subclass to provide
	 * you're own logic. It's incorrect to modify the adapter or the contents of the item itself.
	 * Any alterations will lead to undefined behavior or crashes. Internally, this method is only
	 * ever invoked from a background thread.
	 *
	 * @param item       The Double item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected boolean isFilteredOut(Double item, CharSequence constraint) {
		try {
			return !item.equals(Double.valueOf(constraint.toString()));
		} catch (NumberFormatException e) {
			return true;
		}
	}

	/**
	 * Determines whether the provided constraint filters out the given item. Subclass to provide
	 * you're own logic. It's incorrect to modify the adapter or the contents of the item itself.
	 * Any alterations will lead to undefined behavior or crashes. Internally, this method is only
	 * ever invoked from a background thread.
	 *
	 * @param item       The Integer item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected boolean isFilteredOut(Integer item, CharSequence constraint) {
		try {
			return !item.equals(Integer.valueOf(constraint.toString()));
		} catch (NumberFormatException e) {
			return true;
		}
	}

	/**
	 * Determines whether the provided constraint filters out the given item. Subclass to provide
	 * you're own logic. It's incorrect to modify the adapter or the contents of the item itself.
	 * Any alterations will lead to undefined behavior or crashes. Internally, this method is only
	 * ever invoked from a background thread.
	 *
	 * @param item       The Long item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected boolean isFilteredOut(Long item, CharSequence constraint) {
		try {
			return !item.equals(Long.valueOf(constraint.toString()));
		} catch (NumberFormatException e) {
			return true;
		}
	}

	/**
	 * Determines whether the provided constraint filters out the given item. Allows easy,
	 * customized filtering for subclasses. It's incorrect to modify the adapter or the contents of
	 * the item itself. Any alterations will lead to undefined behavior or crashes. Internally, this
	 * method is only ever invoked from a background thread.
	 *
	 * @param item       The Object item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected abstract boolean isFilteredOut(Object item, CharSequence constraint);

	/**
	 * Determines whether the provided constraint filters out the given item. Subclass to provide
	 * you're own logic. It's incorrect to modify the adapter or the contents of the item itself.
	 * Any alterations will lead to undefined behavior or crashes. Internally, this method is only
	 * ever invoked from a background thread.
	 *
	 * @param item       The String item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the item is filtered out by the given constraint. False if the item will
	 * continue to display in the adapter.
	 */
	protected boolean isFilteredOut(String item, CharSequence constraint) {
		return !item.toLowerCase().contains(constraint.toString().toLowerCase());
	}

	/**
	 * @return True if this adapter has no value at position, or if it's value is the {@code null}
	 * reference or {@link JSONObject#NULL NULL}.
	 */
	public boolean isNull(int position) {
		return mObjects.isNull(position);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Gets the data item associated with the specified position in the adapter or null if there is
	 * no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The data at the specified position or null.
	 */
	public Object optItem(int position) {
		return mObjects.opt(position);
	}

	/**
	 * Gets the boolean data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The boolean data at the specified position or null.
	 */
	public boolean optItemBoolean(int position) {
		return mObjects.optBoolean(position);
	}

	/**
	 * Gets the boolean data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 * @param fallback Value to return if no data is found.
	 *
	 * @return The boolean data at the specified position or otherwise the fallback.
	 */
	public boolean optItemBoolean(int position, boolean fallback) {
		return mObjects.optBoolean(position, fallback);
	}

	/**
	 * Gets the double data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The double data at the specified position or null.
	 */
	public double optItemDouble(int position) {
		return mObjects.optDouble(position);
	}

	/**
	 * Gets the double data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 * @param fallback Value to return if no data is found.
	 *
	 * @return The double data at the specified position or otherwise the fallback.
	 */
	public double optItemDouble(int position, double fallback) {
		return mObjects.optDouble(position, fallback);
	}

	/**
	 * Gets the int data item associated with the specified position in the adapter or null if there
	 * is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The int data at the specified position or null.
	 */
	public int optItemInt(int position) {
		return mObjects.optInt(position);
	}

	/**
	 * Gets the int data item associated with the specified position in the adapter or null if there
	 * is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 * @param fallback Value to return if no data is found.
	 *
	 * @return The int data at the specified position or otherwise the fallback.
	 */
	public int optItemInt(int position, int fallback) {
		return mObjects.optInt(position, fallback);
	}

	/**
	 * Gets the JSONArray data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The JSONArray data at the specified position or null.
	 */
	public JSONArray optItemJSONArray(int position) {
		return mObjects.optJSONArray(position);
	}

	/**
	 * Gets the JSONObject data item associated with the specified position in the adapter or null
	 * if there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The JSONObject data at the specified position or null.
	 */
	public JSONObject optItemJSONObject(int position) {
		return mObjects.optJSONObject(position);
	}

	/**
	 * Gets the long data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The long data at the specified position or null.
	 */
	public long optItemLong(int position) {
		return mObjects.optLong(position);
	}

	/**
	 * Gets the long data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 * @param fallback Value to return if no data is found.
	 *
	 * @return The long data at the specified position or otherwise the fallback.
	 */
	public long optItemLong(int position, long fallback) {
		return mObjects.optLong(position, fallback);
	}

	/**
	 * Gets the String data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 *
	 * @return The String data at the specified position or null.
	 */
	public String optItemString(int position) {
		return mObjects.optString(position);
	}

	/**
	 * Gets the String data item associated with the specified position in the adapter or null if
	 * there is no value at position.
	 *
	 * @param position Position of the item whose data we want within the adapter's data set.
	 * @param fallback Value to return if no data is found.
	 *
	 * @return The String data at the specified position or otherwise the fallback.
	 */
	public String optItemString(int position, String fallback) {
		return mObjects.optString(position, fallback);
	}

	/**
	 * Control whether methods that change the list ({@link #add}, {@link #clear}) automatically
	 * call {@link #notifyDataSetChanged}.  If set to false, caller must manually call
	 * notifyDataSetChanged() to have the changes reflected in the attached view.
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
	 * A JSONArray filter constrains the content of the adapter. Whether an item is constrained or
	 * not is delegated to subclasses through the default {@link JSONAdapter#isFilteredOut(Object,
	 * CharSequence)}. However the filter will first attempt to find the best matching {@code
	 * isFilteredOut} method based on the type of object stored at a given location. For instance,
	 * if the object is an Integer, then the {@link JSONAdapter#isFilteredOut(Integer,
	 * CharSequence)} will be invoked instead.
	 * <p/>
	 * It's possible for subclasses to define their own {@code isFilteredOut} methods for their own
	 * custom classes. So if the object stored is of type {@code Foo} and the subclassed adapter has
	 * defined an {@code isFilteredOut(Foo, CharSequence)}} method, then that  will be invoked
	 * instead of the default {@link JSONAdapter#isFilteredOut(Object, CharSequence)}.
	 */
	private class JSONArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			final JSONArray values;

			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = generateCopy(mOriginalValues);
						mOriginalValues = null;
					}
					results.values = mObjects;
					results.count = mObjects.length();
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = generateCopy(mObjects);
					}
					values = generateCopy(mOriginalValues);
				}
			}

			final JSONArray newValues = new JSONArray();
			if (values != null) {
				Object[] varargs = new Object[2];
				varargs[1] = constraint;
				for (int index = 0; index < values.length(); ++index) {
					Object value = values.opt(index);
					Method m = mFilterMethods.get(value.getClass().getName());
					if (m != null) {
						varargs[0] = value;
						try {
							boolean result = (boolean) m
									.invoke(JSONAdapter.this, varargs);
							if (!result) newValues.put(value);
						} catch (IllegalAccessException e) {
							Log.w(m.getName(),
								  "Method not accessible. Using `isFilteredOut(Object)` instead");
							if (!isFilteredOut(value, constraint)) newValues.put(value);
						} catch (InvocationTargetException e) {
							Log.w(m.getName(), "Exception thrown by method. Gracefully skipping " +
											   mObjects.toString());
						}
					} else {
						Log.v("No method defined for", value.getClass().getName());
						if (!isFilteredOut(value, constraint)) newValues.put(value);
					}
				}
			}

			results.values = newValues;
			results.count = newValues.length();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mLastConstraint = constraint;
			mObjects = (JSONArray) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}