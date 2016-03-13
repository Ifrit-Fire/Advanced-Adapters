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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Collection;

/**
 * A non-filterable custom abstract {@link BaseAdapter} that is backed by a {@link JSONArray} of
 * arbitrary objects. By default this class delegates view generation to subclasses.
 * <p/>
 * Designed to be a flexible and customizable solution for using JSONArray with an adapter but
 * without the filtering mechanism. As a result, there is no need for {@code synchronized} blocks
 * which may help those worried about performance. It exposes most of the JSONArray methods and
 * conveniently passes along a layout inflater for view creation. Keep in mind JSONArray itself has
 * limited capabilities which restricts what this adapter can do.
 * <p/>
 * If filtering is required, it's strongly recommended to use the {@link JSONAdapter} instead.
 */
public abstract class NFJSONArrayAdapter extends BaseAdapter {
	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter * */
	private Context mContext;
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
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public NFJSONArrayAdapter(@NonNull Context activity) {
		init(activity, new JSONArray());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param readFrom a tokener whose nextValue() method will yield a {@code JSONArray} to be
	 *                 stored in the adapter.
	 *
	 * @throws org.json.JSONException if the parse fails or doesn't yield a {@code JSONArray}.
	 */
	public NFJSONArrayAdapter(@NonNull Context activity,
							  @NonNull JSONTokener readFrom) throws JSONException {
		init(activity, new JSONArray(readFrom));
	}

	/**
	 * * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param json     a JSON-encoded string containing an array to be stored in the adapter
	 *
	 * @throws org.json.JSONException if the parse fails or doesn't yield a {@code JSONArray}.
	 */
	public NFJSONArrayAdapter(@NonNull Context activity,
							  @NonNull String json) throws JSONException {
		init(activity, new JSONArray(json));
	}

	public NFJSONArrayAdapter(@NonNull Context activity, @NonNull JSONArray array) {
		init(activity, generateCopy(array));
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public NFJSONArrayAdapter(@NonNull Context activity, @NonNull Collection items) {
		init(activity, new JSONArray(items));
	}

	/**
	 * Creates a new {@code JSONArray} with values from another.
	 */
	@NonNull
	private static JSONArray generateCopy(@NonNull JSONArray array) {
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
	 * Adds the specified items at the end of the adapter. May not be NaNs or infinities which will
	 * cause the adapter to be in an inconsistent state.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(@Nullable Object item) {
		mObjects.put(item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified items at the end of the adapter. May not be NaNs or infinities.
	 *
	 * @param item The item to add at the end of the adapter.
	 *
	 * @throws org.json.JSONException If item is NaN or infinity
	 */
	public void add(double item) throws JSONException {
		mObjects.put(item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified items at the end of the adapter
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(long item) {
		mObjects.put(item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified items at the end of the adapter.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(boolean item) {
		mObjects.put(item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified items at the end of the adapter.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(int item) {
		mObjects.put(item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified JSONArray at the end of the adapter. May not contain NaNs or infinities
	 * which will cause the adapter to be in an inconsistent state.
	 *
	 * @param items The JSONArray to add at the end of the adapter.
	 */
	public void addAll(@NonNull JSONArray items) {
		for (int index = 0; index < items.length(); ++index) {
			mObjects.put(items.opt(index));
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Remove all elements from the adapter.
	 */
	public void clear() {
		mObjects = new JSONArray();
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
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
	@NonNull
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
	 * @throws org.json.JSONException If the value at position doesn't exit or cannot be coerced to
	 *                                a boolean.
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
	 * @throws org.json.JSONException If the value at position doesn't exit or cannot be coerced to
	 *                                a double.
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
	 * @throws org.json.JSONException If the value at position doesn't exit or cannot be coerced to
	 *                                a int.
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
	 * @throws org.json.JSONException If the value at position doesn't exit or is not a JSONArray.
	 */
	@NonNull
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
	 * @throws org.json.JSONException If the value at position doesn't exit or is not a JSONObject.
	 */
	@NonNull
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
	 * @throws org.json.JSONException If the value at position doesn't exit or cannot be coerced to
	 *                                a long.
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
	 * @throws org.json.JSONException If no such value exists.
	 */
	@NonNull
	public String getItemString(int position) throws JSONException {
		return mObjects.getString(position);
	}

	/**
	 * @return The original list of items stored within the Adapter
	 */
	@NonNull
	public JSONArray getJSONArray() {
		return generateCopy(mObjects);
	}

	/**
	 * Resets the adapter to store a new JSONArray of items. Convenient way of calling {@link
	 * #clear()}, then {@link #addAll(org.json.JSONArray)} without having to worry about an extra
	 * {@link #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items New JSONArray of items to store within the adapter.
	 */
	public void setJSONArray(@NonNull JSONArray items) {
		mObjects = generateCopy(items);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
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
	@NonNull
	public abstract View getView(@NonNull LayoutInflater inflater, int position,
								 @Nullable View convertView, @Nullable ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(@NonNull Context context, @NonNull JSONArray objects) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mObjects = objects;
	}

	/**
	 * @return True if this adapter has no value at position, or if it's value is the {@code null}
	 * reference or {@link org.json.JSONObject#NULL NULL}.
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
	@Nullable
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
	@Nullable
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
	@Nullable
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
	@Nullable
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
	@Nullable
	public String optItemString(int position, @Nullable String fallback) {
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
	 * Updates the item at the specified position in the adapter with the specified item. This
	 * operation does not change the size of the adapter.
	 *
	 * @param position The location at which to put the specified item
	 * @param item     The new item to replace with the old
	 *
	 * @throws org.json.JSONException If item is NaN or infinity
	 */
	public void update(int position, @Nullable Object item) throws JSONException {
		mObjects.put(position, item);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}
}