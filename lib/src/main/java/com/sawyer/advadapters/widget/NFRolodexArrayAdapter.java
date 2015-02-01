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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * For use with an {@link android.widget.ExpandableListView}, The rolodex adapters are specifically
 * designed to tackle the problem of organizing existing data on the fly without the need to
 * pre-compute the groupings nor actually store the grouping data itself. Instead of having to
 * organize your data ahead of time, you can simply pass in a list of arbitrary data and provide one
 * simple method which determines the groupings it belongs to. Though not required, ideally this
 * relationship would be derived from the data itself. For example, populating the adapter with a
 * list of Person objects could derive it's groupings based on the last name. Just like an
 * old-school rolodex.
 * <p/>
 * The NFRolodexArrayAdapter uses a Map to organize the data under each group. The children within
 * each grouping are backed by an {@link ArrayList}. The data can be easily modified in various ways
 * and allows numerous display and sorting options.  Additionally full support for {@link
 * ChoiceMode} is available. By default this class delegates view generation logic to subclasses.
 * <p/>
 * If filtering is required, it's strongly recommended to use the {@link RolodexArrayAdapter}
 * instead.
 */
public abstract class NFRolodexArrayAdapter<G, C> extends PatchedExpandableListAdapter {
	/** Contains the map of objects that represent the visible data of the adapter. */
	private Map<G, ArrayList<C>> mObjects;
	private ArrayList<G> mGroupObjects;
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
	public NFRolodexArrayAdapter(Context activity) {
		super(activity);
		init(new ArrayList<C>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public NFRolodexArrayAdapter(Context activity, C[] items) {
		super(activity);
		List<C> list = Arrays.asList(items);
		init(list);
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public NFRolodexArrayAdapter(Context activity, Collection<C> items) {
		super(activity);
		init(items);
	}

	private static <G, C> Map<G, ArrayList<C>> createNewMap(boolean areGroupsSorted,
															Map<G, ArrayList<C>> dataToCopy) {
		if (dataToCopy == null)
			return areGroupsSorted ? new TreeMap<G, ArrayList<C>>() : new LinkedHashMap<G, ArrayList<C>>();
		else
			return areGroupsSorted ? new TreeMap<>(dataToCopy) : new LinkedHashMap<>(dataToCopy);
	}

	/**
	 * Convenience method which joins all {@link ArrayList} values of a {@link Map} into one giant
	 * ArrayList. Order of the newly generated list will match the iteration order of the Map.
	 *
	 * @param map {@link Map} which stores an {@link ArrayList} of values to be joined.
	 * @param <G> Key class used with Map
	 * @param <C> Value class used with ArrayList
	 *
	 * @return All values of the given Map joined together. Will never return null.
	 */
	static <G, C> ArrayList<C> toArrayList(Map<G, ArrayList<C>> map) {
		ArrayList<C> joinedList = new ArrayList<>();
		for (Map.Entry<G, ArrayList<C>> entry : map.entrySet()) {
			joinedList.addAll(entry.getValue());
		}
		return joinedList;
	}

	/**
	 * Adds the specified items at the end of the adapter.
	 *
	 * @param childItem The child item to add at the end of the adapter.
	 */
	public void add(C childItem) {
		G group = getGroupFor(childItem);
		ArrayList<C> children = mObjects.get(group);
		if (children == null) {
			children = new ArrayList<>();
			mObjects.put(group, children);
			if (areGroupsSorted()) {
				mGroupObjects = new ArrayList<>(mObjects.keySet());
			} else {
				mGroupObjects.add(group);
			}
		}
		children.add(childItem);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the adapter.
	 *
	 * @param childItems The Collection of children items to add at the end of the adapter.
	 */
	public void addAll(Collection<? extends C> childItems) {
		addAllToObjects(childItems);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified child items at the end of the adapter.
	 *
	 * @param childItems The child items to add at the end of the adapter.
	 */
	@SafeVarargs
	public final void addAll(C... childItems) {
		addAllToObjects(Arrays.asList(childItems));
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	private void addAllToObjects(Collection<? extends C> childItems) {
		if (areGroupsSorted()) {
			for (C item : childItems) {
				G group = getGroupFor(item);
				ArrayList<C> children = mObjects.get(group);
				if (children == null) {
					children = new ArrayList<>();
					mObjects.put(group, children);
				}
				children.add(item);
			}
			mGroupObjects = new ArrayList<>(mObjects.keySet());
		} else {
			for (C item : childItems) {
				G group = getGroupFor(item);
				ArrayList<C> children = mObjects.get(group);
				if (children == null) {
					children = new ArrayList<>();
					mObjects.put(group, children);
					mGroupObjects.add(group);
				}
				children.add(item);
			}
		}
	}

	/**
	 * @return Whether groups are automatically sorted. Default is true.
	 */
	public boolean areGroupsSorted() {
		return true;
	}

	/**
	 * Remove all elements from the adapter.
	 */
	public void clear() {
		mObjects.clear();
		mGroupObjects.clear();
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Tests whether this adapter contains the specified child item. Be aware that this is a linear
	 * search.
	 *
	 * @param childItem The child item to search for
	 *
	 * @return {@code true} if the child item is an element of this adapter. {@code false} otherwise
	 */
	public boolean contains(C childItem) {
		G group = getGroupFor(childItem);
		return mObjects.get(group) != null && mObjects.get(group).contains(childItem);
	}

	/**
	 * Creates a new group object which represents the parent of the given child item. This is used
	 * to determine what group the child item will fall under. Do not attempt to return a cached
	 * group object here. See {@link #getGroupFromCacheFor(Object)} for that behavior.
	 * <p/>
	 * It's highly recommended that the group object returned is immutable, or whose hashcode is
	 * based on an immutable field(s). A mutable object is fine so long as it's not modified during
	 * the lifespan of this adapter.
	 *
	 * @param childItem The child item for which a group instance will be created for.
	 *
	 * @return An immutable group class object which represents the give child. Do not return null.
	 */
	public abstract G createGroupFor(C childItem);

	@Override
	public C getChild(int groupPosition, int childPosition) {
		return mObjects.get(mGroupObjects.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mObjects.get(mGroupObjects.get(groupPosition)).size();
	}

	@Override
	public G getGroup(int groupPosition) {
		return mGroupObjects.get(groupPosition);
	}

	/**
	 * Gets all the children associated with the given group.
	 *
	 * @param groupPosition The position of the group.
	 *
	 * @return the ArrayList of children found within the specified group.
	 */
	public ArrayList<C> getGroupChildren(int groupPosition) {
		return new ArrayList<>(mObjects.get(mGroupObjects.get(groupPosition)));
	}

	@Override
	public int getGroupCount() {
		return mGroupObjects.size();
	}

	/**
	 * Retrieves a group object for the given child. Attempts to look in cache before requesting to
	 * construct one. By default nothing is cached. Override {@link #getGroupFromCacheFor(Object)}
	 * if you wish to provide your own cache implementation.
	 *
	 * @param childItem Child item to look for
	 *
	 * @return Group associated with child. Will never return null.
	 */
	public final G getGroupFor(C childItem) {
		G group = getGroupFromCacheFor(childItem);
		if (group == null) {
			group = createGroupFor(childItem);
			if (group == null) {
				throw new NullPointerException(
						"createGroupFor(child) must return a non-null value");
			}
		}
		return group;
	}

	/**
	 * Override to provide a caching mechanism for retrieving a group item. Caching can help reduce
	 * the number of {@link #createGroupFor(Object)} invocations. By default, no caching is provided
	 * by the adapter. This method normally returns null.
	 * <p/>
	 * It's only recommended to implement this method if one of the following are true: <ul> <li>You
	 * can pre-populate the cache with all the child to group relations.</li> <li>The cache will be
	 * lazy-loaded and saved for later re-use.</li> <li>Instantiating your group object is a pretty
	 * hefty call.</li> <li>You are constantly mutating the adapter. </li> </ul>
	 * <p/>
	 * Pulling from cache is primarily used when mutating the adapter. It is never used nor needed
	 * by any of the getters.
	 *
	 * @param childItem The child item for which a group object will be returned for.
	 *
	 * @return The group object from cache which represents the given child item. Null if the group
	 * is not found in cache.
	 */
	@SuppressWarnings("UnusedParameters")
	public G getGroupFromCacheFor(C childItem) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * @return The original list of items stored within the Adapter
	 */
	public ArrayList<C> getList() {
		return toArrayList(mObjects);
	}

	/**
	 * Resets the adapter to store a new list of children items. Convenient way of calling {@link
	 * #clear()}, then {@link #addAll(Collection)} without having to worry about an extra {@link
	 * #notifyDataSetChanged()} invoked in between.
	 *
	 * @param childItems New list of children items to store within the adapter.
	 */
	public void setList(Collection<? extends C> childItems) {
		mObjects.clear();
		mGroupObjects.clear();
		addAllToObjects(childItems);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	private void init(Collection<C> objects) {
		mObjects = createNewMap(areGroupsSorted(), null);
		mGroupObjects = new ArrayList<>();
		addAllToObjects(objects);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Removes all occurrences in the adapter of each child item in the specified collection.
	 *
	 * @param childItem The child item to remove.
	 */
	public void remove(C childItem) {
		G group = getGroupFor(childItem);
		ArrayList<C> children = mObjects.get(group);
		if (children == null) return; //Can't find group, item already removed or doesn't exist
		boolean isModified = children.remove(childItem);
		if (children.isEmpty()) {
			mObjects.remove(group);
			mGroupObjects.remove(group);
		}
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Convenience method which removes all occurrences in the adapter of each item in the specified
	 * collection.
	 *
	 * @param childItems The collection of child items to remove
	 */
	public void removeAll(Collection<? extends C> childItems) {
		boolean isModified = false;
		for (C item : childItems) {
			G group = getGroupFor(item);
			ArrayList<C> children = mObjects.get(group);
			if (children == null) return; //Can't find group, already removed or doesn't exist
			isModified |= children.remove(item);
			if (children.isEmpty()) mObjects.remove(group);
		}
		mGroupObjects = new ArrayList<>(mObjects.keySet());
		if (isModified && mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Removes all children items from this adapter that are not contained in the specified
	 * collection.
	 *
	 * @param childItems The collection of children items to retain
	 */
	public void retainAll(Collection<?> childItems) {
		boolean isModified = false;
		Iterator<Map.Entry<G, ArrayList<C>>> it = mObjects.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<G, ArrayList<C>> entry = it.next();
			isModified |= entry.getValue().retainAll(childItems);
			if (entry.getValue().isEmpty()) {
				it.remove();
			}
		}
		if (isModified) {
			mGroupObjects = new ArrayList<>(mObjects.keySet());
			if (mNotifyOnChange) notifyDataSetChanged();
		}
	}

	/**
	 * Control whether methods that change the list ({@link #add}, {@link #remove}, {@link #clear})
	 * automatically call {@link #notifyDataSetChanged}.  If set to false, caller must manually call
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
	 * Sorts the children of each grouping using the natural order of the items themselves. This
	 * requires the items to have implemented {@link Comparable} and is equivalent of passing null
	 * to {@link #sortAllChildren(Comparator)}. This will not sort groups themselves.
	 *
	 * @throws ClassCastException If the comparator is null and the stored items do not implement
	 *                            {@code Comparable} or if {@code compareTo} throws for any pair of
	 *                            items.
	 */
	public void sortAllChildren() {
		sortAllChildren(null);
	}

	/**
	 * Sorts the children of each grouping using the specified comparator. This will not sort groups
	 * themselves.
	 *
	 * @param comparator Used to sort the child items contained in this adapter. Null to use an
	 *                   item's {@code Comparable} interface.
	 *
	 * @throws ClassCastException If the comparator is null and the stored items do not implement
	 *                            {@code Comparable} or if {@code compareTo} throws for any pair of
	 *                            items.
	 */
	public void sortAllChildren(Comparator<? super C> comparator) {
		for (Map.Entry<G, ArrayList<C>> entry : mObjects.entrySet()) {
			Collections.sort(entry.getValue(), comparator);
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Sorts the children of the specified group using the natural order of the children themselves.
	 * This requires the child items to have implemented {@link Comparable} and is equivalent of
	 * passing null to {@link #sortGroup(int, Comparator)}. This will not sort groups themselves.
	 *
	 * @throws ClassCastException If the comparator is null and the stored children do not implement
	 *                            {@code Comparable} or if {@code compareTo} throws for any pair of
	 *                            items.
	 */
	public void sortGroup(int groupPosition) {
		sortGroup(groupPosition, null);
	}

	/**
	 * Sorts the children of the specified group using the specified comparator. This will not sort
	 * groups themselves.
	 *
	 * @param comparator Used to sort the child items contained within a group. Null to use an
	 *                   item's {@code Comparable} interface.
	 *
	 * @throws ClassCastException If the comparator is null and the stored children do not implement
	 *                            {@code Comparable} or if {@code compareTo} throws for any pair of
	 *                            items.
	 */
	public void sortGroup(int groupPosition, Comparator<? super C> comparator) {
		G group = mGroupObjects.get(groupPosition);
		Collections.sort(mObjects.get(group), comparator);
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Updates the item at the specified position in the adapter with the specified item. This
	 * operation does not change the overall size of the adapter but will relocate the item to a new
	 * group if needed.
	 *
	 * @param groupPosition The group location at which to put the specified child
	 * @param childPosition The child location at which to put the specified item
	 * @param childItem     The new item to replace with the old
	 */
	public void update(int groupPosition, int childPosition, C childItem) {
		G oldGroup = mGroupObjects.get(groupPosition);
		G newGroup = createGroupFor(childItem);    //Can't rely on cache.

		//Easy case, group hasn't changed
		if (oldGroup.equals(newGroup)) {
			mObjects.get(oldGroup).set(childPosition, childItem);

			//Hard case, group has changed. Must remove and re-add appropriately
		} else {
			//Remove old item
			ArrayList<C> children = mObjects.get(oldGroup);
			children.remove(childItem);
			if (children.isEmpty()) {
				mObjects.remove(oldGroup);
				mGroupObjects.remove(groupPosition);
			}

			//Add new item
			children = mObjects.get(newGroup);
			if (children == null) {
				children = new ArrayList<>();
				mObjects.put(newGroup, children);
				if (areGroupsSorted()) {
					mGroupObjects = new ArrayList<>(mObjects.keySet());
				} else {
					mGroupObjects.add(newGroup);
				}
			}
			children.add(childItem);
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
}
