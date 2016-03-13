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
import android.text.TextUtils;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;

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
 * For use with an {@link ExpandableListView}, The rolodex adapters are specifically designed to
 * tackle the problem of organizing existing data on the fly without the need to pre-compute the
 * groupings nor actually store the grouping data itself. Instead of having to organize your data
 * ahead of time, you can simply pass in a list of arbitrary data and provide one simple method
 * which determines the groupings it belongs to. Though not required, ideally this relationship
 * would be derived from the data itself. For example, populating the adapter with a list of Person
 * objects could derive it's groupings based on the last name. Just like an old-school rolodex.
 * <p/>
 * The RolodexArrayAdapter uses a {@link Map} to organize the data under each group. The children
 * within each grouping are backed by an {@link ArrayList}. The data can be easily modified and
 * filtered in various ways and allows numerous display and sorting options.  Additionally full
 * support for {@link ChoiceMode ChoiceMode} is available. By default this class delegates view
 * generation and defining the filtering logic to subclasses.
 * <p/>
 * Because of the background filtering process, all methods which mutates the underlying data are
 * internally synchronized. This ensures a thread safe environment for internal write operations. If
 * filtering is not required, it's strongly recommended to use the {@link NFRolodexArrayAdapter}
 * instead.
 */
public abstract class RolodexArrayAdapter<G, C> extends PatchedExpandableListAdapter implements
		Filterable {
	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation performed on the
	 * map should be synchronized on this lock. This lock is also used by the filter (see {@link
	 * #getFilter()} to make a synchronized copy of the original map of data.
	 */
	private final Object mLock = new Object();
	/**
	 * Contains the map of objects that represent the visible data of the adapter. It's contents
	 * will change as filtering occurs. All methods retrieving data about the adapter will always do
	 * so from this list.
	 */
	private Map<G, ArrayList<C>> mObjects;
	private ArrayList<G> mGroupObjects;
	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever {@link
	 * #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;
	/**
	 * A copy of the original {@link #mObjects} map, is not initialized until a filtering processing
	 * occurs. Once initialized, it'll track the entire unfiltered data. Once the filter process
	 * completes, it's contents are copied back over to mObjects and is set to null.
	 */
	private Map<G, ArrayList<C>> mOriginalValues;
	private RolodexFilter mFilter;
	/**
	 * Saves the constraint used during the last filtering operation. Used to re-filter the map
	 * following changes to the array of data
	 */
	private CharSequence mLastConstraint;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public RolodexArrayAdapter(@NonNull Context activity) {
		super(activity);
		init(new ArrayList<C>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public RolodexArrayAdapter(@NonNull Context activity, @NonNull C[] items) {
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
	public RolodexArrayAdapter(@NonNull Context activity, @NonNull Collection<C> items) {
		super(activity);
		init(items);
	}

	@NonNull
	private static <G, C> Map<G, ArrayList<C>> createNewMap(boolean areGroupsSorted,
															@Nullable Map<G, ArrayList<C>> dataToCopy) {
		if (dataToCopy == null) {
			return areGroupsSorted ? new TreeMap<G, ArrayList<C>>() :
					new LinkedHashMap<G, ArrayList<C>>();
		} else {
			return areGroupsSorted ? new TreeMap<>(dataToCopy) : new LinkedHashMap<>(dataToCopy);
		}
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
	@NonNull
	static <G, C> ArrayList<C> toArrayList(@NonNull Map<G, ArrayList<C>> map) {
		ArrayList<C> joinedList = new ArrayList<>();
		for (Map.Entry<G, ArrayList<C>> entry : map.entrySet()) {
			joinedList.addAll(entry.getValue());
		}
		return joinedList;
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param childItem The child item to add at the end of the adapter.
	 */
	public void add(@Nullable C childItem) {
		synchronized (mLock) {
			G group = getGroupFor(childItem);
			if (mOriginalValues != null) {
				ArrayList<C> children = mOriginalValues.get(group);
				if (children == null) {
					children = new ArrayList<>();
					mOriginalValues.put(group, children);
				}
				children.add(childItem);
				getFilter().filter(mLastConstraint);
			} else {
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
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified Collection at the end of the adapter. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 *
	 * @param childItems The Collection of children items to add at the end of the adapter.
	 */
	public void addAll(@NonNull Collection<? extends C> childItems) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				addAllToOriginalValues(childItems);
				getFilter().filter(mLastConstraint);
			} else {
				addAllToObjects(childItems);
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified child items at the end of the adapter. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 *
	 * @param childItems The child items to add at the end of the adapter.
	 */
	@SafeVarargs
	public final void addAll(@NonNull C... childItems) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				addAllToOriginalValues(Arrays.asList(childItems));
				getFilter().filter(mLastConstraint);
			} else {
				addAllToObjects(Arrays.asList(childItems));
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	private void addAllToObjects(@NonNull Collection<? extends C> childItems) {
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

	private void addAllToOriginalValues(@NonNull Collection<? extends C> childItems) {
		for (C item : childItems) {
			G group = getGroupFor(item);
			ArrayList<C> children = mOriginalValues.get(group);
			if (children == null) {
				children = new ArrayList<>();
				mOriginalValues.put(group, children);
			}
			children.add(item);
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
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
			}
			mObjects.clear();
			mGroupObjects.clear();
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Tests whether this adapter contains the specified child item.
	 *
	 * @param childItem The child item to search for
	 *
	 * @return {@code true} if the child item is an element of this adapter. {@code false} otherwise
	 */
	public boolean contains(@Nullable C childItem) {
		G group = getGroupFor(childItem);
		return mObjects.get(group) != null && mObjects.get(group).contains(childItem);
	}

	/**
	 * Creates a new group object which represents the parent of the given child item. This is used
	 * to determine what group the child item will fall under. Do not attempt to return a cached
	 * group object here. See {@link #getGroupFromCacheFor(Object)} for that behavior.
	 * <p/>
	 * It's recommended that the group object returned is immutable, or whose hashcode is based on
	 * an immutable field(s). A mutable object is fine so long as it's not modified during the
	 * lifespan of this adapter.
	 *
	 * @param childItem The child item for which a group instance will be created for.
	 *
	 * @return An immutable group class object which represents the given child item. Do not return
	 * null.
	 */
	@NonNull
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

	@NonNull
	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new RolodexFilter();
		}
		return mFilter;
	}

	/**
	 * @return The shown filtered list. If no filter is applied, then the original list is returned.
	 */
	@NonNull
	public ArrayList<C> getFilteredList() {
		ArrayList<C> objects;
		synchronized (mLock) {
			objects = toArrayList(mObjects);
		}
		return objects;
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
	@NonNull
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
	@NonNull
	public final G getGroupFor(@Nullable C childItem) {
		G group = getGroupFromCacheFor(childItem);
		if (group == null) {
			group = createGroupFor(childItem);
			//Subclasses may choose to ignore @NonNull, so we suppress inspection and verify for null
			//noinspection ConstantConditions
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
	 * <p/>
	 * Additionally, the group object returned should be immutable, or whose hashcode is based on an
	 * immutable field(s). A mutable object is fine so long as it's not modified during the lifespan
	 * of this adapter.
	 *
	 * @param childItem The child item for which a group object will be returned for.
	 *
	 * @return The group object from cache which represents the given child item. Null if the group
	 * is not found in cache.
	 */
	@SuppressWarnings("UnusedParameters")
	@Nullable
	public G getGroupFromCacheFor(C childItem) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * @return The original (unfiltered) list of items stored within the Adapter
	 */
	@NonNull
	public ArrayList<C> getList() {
		ArrayList<C> objects;
		synchronized (mLock) {
			if (mOriginalValues != null) {
				objects = toArrayList(mOriginalValues);
			} else {
				objects = toArrayList(mObjects);
			}
		}
		return objects;
	}

	/**
	 * Resets the adapter to store a new list of children items. Convenient way of calling {@link
	 * #clear()}, then {@link #addAll(Collection)} without having to worry about an extra {@link
	 * #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param childItems New list of children items to store within the adapter.
	 */
	public void setList(@NonNull Collection<? extends C> childItems) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
				addAllToOriginalValues(childItems);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.clear();
				mGroupObjects.clear();
				addAllToObjects(childItems);
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	private void init(@NonNull Collection<C> objects) {
		mObjects = createNewMap(areGroupsSorted(), null);
		mGroupObjects = new ArrayList<>();
		addAllToObjects(objects);
	}

	/**
	 * Determines whether the provided constraint filters out the given child item. Allows easy,
	 * customized filtering for subclasses. It's incorrect to modify the adapter or the contents of
	 * the item itself. Any alterations will lead to undefined behavior or crashes. Internally, this
	 * method is only ever invoked from a background thread.
	 *
	 * @param childItem  The child item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the child item is filtered out by the given constraint. False if the item
	 * will continue to display in the adapter.
	 */
	protected abstract boolean isChildFilteredOut(C childItem, @NonNull CharSequence constraint);

	/**
	 * Determines whether the provided constraint filters out the given group item. If filtered out,
	 * all it's children will automatically be filtered out as well and their associating {@link
	 * #isChildFilteredOut(Object, CharSequence) isChildFilteredOut()} method calls will not occur.
	 * This method allows easy, customized filtering for subclasses. It's incorrect to modify the
	 * adapter or the contents of the item itself. Any alterations will lead to undefined behavior
	 * or crashes. Internally, this method is only ever invoked from a background thread.
	 *
	 * @param groupItem  The group item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the group item (and subsequently all it's children) is filtered out by the
	 * given constraint. False if the item will continue to display in the adapter.
	 */
	protected abstract boolean isGroupFilteredOut(G groupItem, @NonNull CharSequence constraint);

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Removes the first occurrence of the specified child item from the adapter.
	 *
	 * @param childItem The child item to remove.
	 */
	public void remove(@Nullable C childItem) {
		boolean isModified = false;

		SYNC_BLOCK:
		synchronized (mLock) {
			G group = getGroupFor(childItem);
			if (mOriginalValues != null) {
				ArrayList<C> children = mOriginalValues.get(group);
				if (children == null) {
					return; //Can't find group, assume item doesn't exist
				}
				isModified = children.remove(childItem);
				if (children.isEmpty()) {
					mOriginalValues.remove(group);
					if (mObjects.remove(group) != null) {
						mGroupObjects.remove(group);
					}
					break SYNC_BLOCK;
				}
			}
			//No matter what, remove from mObjects. This avoids having to re-filter the data when
			//mOriginalValues != null, then our group object will be correct. Otherwise, we may need
			//to do a manual search.
			ArrayList<C> children = mObjects.get(group);
			if (children == null) {
				return; //Can't find group, item already removed or doesn't exist
			}
			isModified |= children.remove(childItem);
			if (children.isEmpty()) {
				mObjects.remove(group);
				mGroupObjects.remove(group);
			}
		}
		if (isModified && mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Convenience method which removes all occurrences in the adapter of each item in the specified
	 * collection.
	 *
	 * @param childItems The collection of child items to remove
	 */
	public void removeAll(@NonNull Collection<? extends C> childItems) {
		boolean isModified = false;

		synchronized (mLock) {
			for (C item : childItems) {
				G group = getGroupFor(item);
				if (mOriginalValues != null) {
					ArrayList<C> children = mOriginalValues.get(group);
					if (children == null) {
						return; //Can't find group, assume item doesn't exist
					}
					isModified = children.remove(item);
					if (children.isEmpty()) {
						mOriginalValues.remove(group);
						mObjects.remove(group);
						continue;
					}
				}
				//No matter what, remove from mObjects. This avoids having to re-filter the data when
				//mOriginalValues != null, then our group object will be correct. Otherwise, we may need
				//to do a manual search.
				ArrayList<C> children = mObjects.get(group);
				if (children == null) {
					return; //Can't find group, already removed or doesn't exist
				}
				isModified |= children.remove(item);
				if (children.isEmpty()) {
					mObjects.remove(group);
				}
			}
			mGroupObjects = new ArrayList<>(mObjects.keySet());
		}
		if (isModified && mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Removes all children items from this adapter that are not contained in the specified
	 * collection.
	 *
	 * @param childItems The collection of children items to retain
	 */
	public void retainAll(@NonNull Collection<?> childItems) {
		boolean isModified = false;

		synchronized (mLock) {
			if (mOriginalValues != null) {
				Iterator<Map.Entry<G, ArrayList<C>>> it = mOriginalValues.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<G, ArrayList<C>> entry = it.next();
					isModified |= entry.getValue().retainAll(childItems);
					if (entry.getValue().isEmpty()) {
						mObjects.remove(entry.getKey());
						it.remove();
					}
				}
			}

			//No matter what, remove from mObjects so as to avoid re-filtering when mOriginalValues != null
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
			}
		}
		if (isModified && mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Controls whether methods that change the list ({@link #add}, {@link #remove}, {@link #clear})
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
	 * @throws java.lang.ClassCastException If the comparator is null and the stored items do not
	 *                                      implement {@code Comparable}, or if {@code compareTo}
	 *                                      throws for any pair of items.
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
	 * @throws java.lang.ClassCastException If the comparator is null and the stored items do not
	 *                                      implement {@code Comparable}, or if {@code compareTo}
	 *                                      throws for any pair of items.
	 */
	public void sortAllChildren(@Nullable Comparator<? super C> comparator) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (Map.Entry<G, ArrayList<C>> entry : mOriginalValues.entrySet()) {
					Collections.sort(entry.getValue(), comparator);
				}
			}
			for (Map.Entry<G, ArrayList<C>> entry : mObjects.entrySet()) {
				Collections.sort(entry.getValue(), comparator);
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Sorts the children of the specified group using the natural order of the children themselves.
	 * This requires the child items to have implemented {@link Comparable} and is equivalent of
	 * passing null to {@link #sortGroup(int, Comparator)}. This will not sort groups themselves.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored children do not
	 *                                      implement {@code Comparable}, or if {@code compareTo}
	 *                                      throws for any pair of items.
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
	 * @throws java.lang.ClassCastException If the comparator is null and the stored children do not
	 *                                      implement {@code Comparable}, or if {@code compareTo}
	 *                                      throws for any pair of items.
	 */
	public void sortGroup(int groupPosition, @Nullable Comparator<? super C> comparator) {
		synchronized (mLock) {
			G group = mGroupObjects.get(groupPosition);
			if (mOriginalValues != null) {
				Collections.sort(mOriginalValues.get(group), comparator);
			}
			Collections.sort(mObjects.get(group), comparator);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Updates the item at the specified position in the adapter with the specified item. This
	 * operation does not change the overall size of the adapter but will relocate the item to a new
	 * group if needed. Will repeat the last filtering request if invoked while filtered results are
	 * being displayed.
	 *
	 * @param groupPosition The group location at which to put the specified child
	 * @param childPosition The child location at which to put the specified item
	 * @param childItem     The new item to replace with the old
	 */
	public void update(int groupPosition, int childPosition, @Nullable C childItem) {
		synchronized (mLock) {
			G oldGroup = mGroupObjects.get(groupPosition);
			G newGroup = createGroupFor(childItem);    //Can't rely on cache.

			//Easy case, group hasn't changed
			if (oldGroup.equals(newGroup)) {
				if (mOriginalValues != null) {
					C child = mObjects.get(oldGroup).get(childPosition);
					ArrayList<C> children = mOriginalValues.get(oldGroup);
					children.set(children.indexOf(child), childItem);
					getFilter().filter(mLastConstraint);
				} else {
					mObjects.get(oldGroup).set(childPosition, childItem);
				}

				//Hard case, group has changed. Must remove and re-add appropriately
			} else {
				if (mOriginalValues != null) {
					//Remove old item
					C child = mObjects.get(oldGroup).get(childPosition);
					ArrayList<C> children = mOriginalValues.get(oldGroup);
					children.remove(child);
					if (children.isEmpty()) {
						mOriginalValues.remove(oldGroup);
					}

					//Add new item
					children = mOriginalValues.get(newGroup);
					if (children == null) {
						children = new ArrayList<>();
						mOriginalValues.put(newGroup, children);
					}
					children.add(childItem);
					getFilter().filter(mLastConstraint);
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
			}
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	private class RolodexFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			Map<G, ArrayList<C>> values;
			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = createNewMap(areGroupsSorted(), mOriginalValues);
						mGroupObjects = new ArrayList<>(mObjects.keySet());
						mOriginalValues = null;
					}
					results.values = mObjects;
					results.count = mObjects.size();
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = createNewMap(areGroupsSorted(), mObjects);
					}
					values = createNewMap(areGroupsSorted(), mOriginalValues);
				}
			}
			Map<G, ArrayList<C>> newValues = createNewMap(areGroupsSorted(), null);
			for (Map.Entry<G, ArrayList<C>> entry : values.entrySet()) {
				if (!isGroupFilteredOut(entry.getKey(), constraint)) {
					ArrayList<C> children = new ArrayList<>();
					for (C child : entry.getValue()) {
						if (!isChildFilteredOut(child, constraint)) {
							children.add(child);
						}
					}
					if (!children.isEmpty()) {
						newValues.put(entry.getKey(), children);
					}
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
			synchronized (mLock) {
				mObjects = (Map<G, ArrayList<C>>) results.values;
				mGroupObjects = new ArrayList<>(mObjects.keySet());
			}
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
