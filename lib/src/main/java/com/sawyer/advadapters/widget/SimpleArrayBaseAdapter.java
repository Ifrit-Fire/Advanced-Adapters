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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public abstract class SimpleArrayBaseAdapter<T> extends BaseAdapter {
	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
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
	public SimpleArrayBaseAdapter(Context activity) {
		init(activity, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param objects  The objects to represent within the adapter.
	 */
	public SimpleArrayBaseAdapter(Context activity, Collection<T> objects) {
		init(activity, new ArrayList<>(objects));
	}

	/**
	 * Adds the specified object at the end of the adapter.
	 *
	 * @param object The object to add at the end of the adapter.
	 */
	public void add(T object) {
		mObjects.add(object);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the adapter.
	 *
	 * @param collection The Collection to add at the end of the adapter.
	 */
	public void addAll(Collection<? extends T> collection) {
		boolean isModified = mObjects.addAll(collection);
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
	 * Tests whether this adapter contains the specified object
	 *
	 * @param object The object to search for
	 *
	 * @return {@code true} if the object is an element of this adapter. {@code false} otherwise
	 */
	public boolean contains(T object) {
		return mObjects.contains(object);
	}

	/**
	 * Tests whether this adapter contains all objects contained in the specified collection.
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
	public View getDropDownView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
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
	 * @return The  list of objects stored within the Adapter
	 */
	public ArrayList<T> getList() {
		return new ArrayList<>(mObjects);
	}

	/**
	 * Returns the position of the specified item in the array.
	 *
	 * @param item The item to retrieve the position of.
	 *
	 * @return The position of the specified item.
	 */
	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	public abstract View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent);

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		return this.getView(mInflater, position, convertView, parent);
	}

	private void init(Context context, ArrayList<T> objects) {
		mInflater = LayoutInflater.from(context);
		mObjects = objects;
	}

	/**
	 * Inserts the specified object at the specified index in the array.
	 *
	 * @param index  The index at which the object must be inserted.
	 * @param object The object to insert into the adapter.
	 */
	public void insert(int index, T object) {
		mObjects.add(index, object);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Inserts the objects in the specified collection at the specified location in this adapter.
	 * The objects are added in the order they are returned from the collection's iterator.
	 *
	 * @param index      The index at which the object must be inserted.
	 * @param collection The collection of objects to be inserted.
	 */
	public void insertAll(int index, Collection<? extends T> collection) {
		boolean isModified = mObjects.addAll(index, collection);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Removes the first occurrence of the specified object from the adapter
	 *
	 * @param object The object to remove.
	 */
	public void remove(T object) {
		boolean isModified = mObjects.remove(object);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all occurrences in the adapter of each object in the specified collection.
	 *
	 * @param collection The collection of objects to remove
	 */
	public void removeAll(Collection<?> collection) {
		boolean isModified = mObjects.removeAll(collection);
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all objects from this adapter that are not contained in the specified collection.
	 *
	 * @param collection The collection of objects to retain
	 */
	public void retainAll(Collection<?> collection) {
		boolean isModified = mObjects.retainAll(collection);
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
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator Used to sort the objects contained in this adapter. Null to use an object's
	 *                   <code>Comparable</code> interface.
	 *
	 * @throws ClassCastException If the comparator is null and the stored objects do not implement
	 *                            <code>Comparable</code> or if <code>compareTo</code> throws for
	 *                            any pair of objects.
	 */
	public void sort(Comparator<? super T> comparator) {
		Collections.sort(mObjects, comparator);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Updates the object at the specified position in the adapter with the specified object. This
	 * operation does not change the size of the adapter.
	 *
	 * @param position The location at which to put the specified object
	 * @param object   The new object to replace with the old
	 */
	public void update(int position, T object) {
		mObjects.set(position, object);
		if (mNotifyOnChange) notifyDataSetChanged();
	}
}