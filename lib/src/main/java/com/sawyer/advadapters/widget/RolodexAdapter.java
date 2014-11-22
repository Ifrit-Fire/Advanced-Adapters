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
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//TODO: Implement
public abstract class RolodexAdapter<G, C> extends BaseExpandableListAdapter implements Filterable {
	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation performed on the
	 * map should be synchronized on this lock. This lock is also used by the filter (see {@link
	 * #getFilter()} to make a synchronized copy of the original map of data.
	 */
	private final Object mLock = new Object();

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter * */
	private Context mContext;
	/**
	 * Contains the map of objects that represent the visible data of the adapter. It's contents
	 * will change as filtering occurs. All methods retrieving data about the adapter will always do
	 * so from this list.
	 */
	private Map<G, ArrayList<C>> mObjects;
	private ArrayList<G> mGroupObjects;
	private Map<C, G> mChild2Group;
	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever {@link
	 * #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;
	/**
	 * A copy of the original mObjects map, is not initialized until a filtering processing occurs.
	 * Once initialized, it'll track the entire unfiltered data. Once the filter process completes,
	 * it's contents are copied back over to mObjects and is set to null.
	 */
	private Map<G, ArrayList<C>> mOriginalValues;
	private RolodexFilter mFilter;
	/**
	 * Saves the constraint used during the last filtering operation. Used to re-filter the map
	 * following changes to the array of data
	 */
	private CharSequence mLastConstraint;
	/**
	 * Determines if groups will be auto sorted. Basically determines if the Map storing all the
	 * data will be a TreeMap or LinkHashMap.
	 */
	private boolean mIsAutoSortEnabled = false;

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public RolodexAdapter(Context activity) {
		init(activity, new ArrayList<C>());
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public RolodexAdapter(Context activity, C[] items) {
		List<C> list = Arrays.asList(items);
		init(activity, list);
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 * @param items    The items to represent within the adapter.
	 */
	public RolodexAdapter(Context activity, Collection<C> items) {
		init(activity, items);
	}

	private static <G, C> ArrayList<C> toArrayList(Map<G, ArrayList<C>> map) {
		ArrayList<C> joinedList = new ArrayList<>();
		//TODO: Ensure this returns in the same order
		for (Map.Entry<G, ArrayList<C>> entry : map.entrySet()) {
			joinedList.addAll(entry.getValue());
		}
		return joinedList;
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param item The item to add at the end of the adapter.
	 */
	public void add(C item) {
		synchronized (mLock) {
			G group = getGroupFor(item);
			if (mOriginalValues != null) {
				ArrayList<C> children = mOriginalValues.get(group);
				if (children == null) {
					children = new ArrayList<>();
					mOriginalValues.put(group, children);
				}
				children.add(item);
				getFilter().filter(mLastConstraint);
			} else {
				ArrayList<C> children = mObjects.get(group);
				if (children == null) {
					children = new ArrayList<>();
					mObjects.put(group, children);
					if (mIsAutoSortEnabled) {
						mGroupObjects = new ArrayList<>(mObjects.keySet());
					} else {
						mGroupObjects.add(group);
					}
				}
				children.add(item);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified Collection at the end of the adapter. Will repeat the last filtering
	 * request if invoked while filtered results are being displayed.
	 *
	 * @param items The Collection to add at the end of the adapter.
	 */
	public void addAll(Collection<? extends C> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				addAllToOriginalValues(items);
				getFilter().filter(mLastConstraint);
			} else {
				addAllToObjects(items);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	/**
	 * Adds the specified items at the end of the adapter. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items The items to add at the end of the adapter.
	 */
	@SafeVarargs
	public final void addAll(C... items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				for (C item : items) {
					G group = getGroupFor(item);
					ArrayList<C> children = mOriginalValues.get(group);
					if (children == null) {
						children = new ArrayList<>();
						mOriginalValues.put(group, children);
					}
					children.add(item);
				}
				getFilter().filter(mLastConstraint);
			} else {
				if (mIsAutoSortEnabled) {
					for (C item : items) {
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
					for (C item : items) {
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
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	private void addAllToObjects(Collection<? extends C> items) {
		if (mIsAutoSortEnabled) {
			for (C item : items) {
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
			for (C item : items) {
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

	private void addAllToOriginalValues(Collection<? extends C> items) {
		for (C item : items) {
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
	public boolean contains(C item) {
		G group = mChild2Group.get(item);
		return group != null && mObjects.get(group) != null && mObjects.get(group).contains(item);
	}

	/**
	 * Creates a new group object which represents the parent of the given child item. This is used
	 * to determine what group item the child item will fall under. Internally this relationship is
	 * cached to optimize how often this method is invoked. Ideally only once per child. However any
	 * changes to the child or it's parent group will result in additional invocations of this
	 * method.
	 *
	 * @param childItem The child item for which a group instance will be created for.
	 *
	 * @return The group class object which represents the give child. Do not return null.
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
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
							 View convertView, ViewGroup parent) {
		return getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							parent);
	}

	public abstract View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
									  boolean isLastChild,
									  View convertView, ViewGroup parent);

	@Override
	public int getChildrenCount(int groupPosition) {
		return mObjects.get(mGroupObjects.get(groupPosition)).size();
	}

	/**
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

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

	@Override
	public int getGroupCount() {
		return mGroupObjects.size();
	}

	private G getGroupFor(C child) {
		G group = mChild2Group.get(child);
		if (group == null) {
			group = createGroupFor(child);
			if (group == null) {
				throw new NullPointerException(
						"createGroupFor(child) must return a non-null value");
			}
			mChild2Group.put(child, group);
		}
		return group;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public final View getGroupView(int groupPosition, boolean isExpanded, View convertView,
								   ViewGroup parent) {
		return getGroupView(mInflater, groupPosition, isExpanded, convertView, parent);
	}

	public abstract View getGroupView(LayoutInflater inflater, int groupPosition,
									  boolean isExpanded, View convertView,
									  ViewGroup parent);

	/**
	 * @return The original (unfiltered) list of items stored within the Adapter
	 */
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
	 * Resets the adapter to store a new list of items. Convenient way of calling {@link #clear()},
	 * then {@link #addAll(java.util.Collection)} without having to worry about an extra {@link
	 * #notifyDataSetChanged()} invoked in between. Will repeat the last filtering request if
	 * invoked while filtered results are being displayed.
	 *
	 * @param items New list of items to store within the adapter.
	 */
	public void setList(Collection<? extends C> items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
				addAllToOriginalValues(items);
				getFilter().filter(mLastConstraint);
			} else {
				mObjects.clear();
				mGroupObjects.clear();
				addAllToObjects(items);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	private void init(Context context, Collection<C> objects) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mObjects = mIsAutoSortEnabled ? new TreeMap<G, ArrayList<C>>() : new LinkedHashMap<G, ArrayList<C>>();
		mGroupObjects = new ArrayList<>();
		mChild2Group = new HashMap<>(objects.size());
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
	protected abstract boolean isChildFilteredOut(C childItem, CharSequence constraint);

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * Determines whether the provided constraint filters out the given group item. If filtered out,
	 * all it's children will automatically be filtered out as well. This method allows easy,
	 * customized filtering for subclasses. It's incorrect to modify the adapter or the contents of
	 * the item itself. Any alterations will lead to undefined behavior or crashes. Internally, this
	 * method is only ever invoked from a background thread.
	 *
	 * @param groupItem  The group item to compare against the constraint
	 * @param constraint The constraint used to filter the item
	 *
	 * @return True if the group item (and subsequently all it's children) is filtered out by the
	 * given constraint. False if the item will continue to display in the adapter.
	 */
	protected abstract boolean isGroupFilteredOut(G groupItem, CharSequence constraint);

	public boolean isHeaderAutoSortEnabled() {
		return mIsAutoSortEnabled;
	}

	public void setEnableHeaderAutoSort(boolean isEnabled) {
		if (mIsAutoSortEnabled == isEnabled) return;
		mIsAutoSortEnabled = isEnabled;
		synchronized (mLock) {
			if (mIsAutoSortEnabled) {
				if (mOriginalValues != null) {
					mOriginalValues = new TreeMap<>(mOriginalValues);
				}
				mObjects = new TreeMap<>(mObjects);
			} else {
				if (mOriginalValues != null) {
					mOriginalValues = new LinkedHashMap<>(mOriginalValues);
				}
				mObjects = new LinkedHashMap<>(mObjects);
			}
			// Only need to update when switching to TreeMap
			if (mIsAutoSortEnabled) {
				mGroupObjects = new ArrayList<>(mObjects.keySet());
				if (mNotifyOnChange) notifyDataSetChanged();
			}
		}
	}

	/**
	 * Sorts the children of each grouping using the natural order of the stored children items
	 * themselves. This requires the items to have implemented {@link java.lang.Comparable} and is
	 * equivalent of passing null to {@link #sort(java.util.Comparator)}.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored items do not
	 *                                      implement {@code Comparable} or if {@code compareTo}
	 *                                      throws for any pair of items.
	 */
	public void sort() {
		sort(null);
	}

	/**
	 * Sorts the children of each grouping using the specified comparator. This will not sort the
	 * groups themselves. Use {@link #setEnableHeaderAutoSort(boolean)} for group sorting.
	 *
	 * @param comparator Used to sort the child items contained in this adapter. Null to use an
	 *                   item's {@code Comparable} interface.
	 *
	 * @throws java.lang.ClassCastException If the comparator is null and the stored items do not
	 *                                      implement {@code Comparable} or if {@code compareTo}
	 *                                      throws for any pair of items.
	 */
	public void sort(Comparator<? super C> comparator) {
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
		if (mNotifyOnChange) notifyDataSetChanged();
	}

	private class RolodexFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			Map<G, ArrayList<C>> values;

			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = new LinkedHashMap<>(mOriginalValues);
						mOriginalValues = null;
					}
					results.values = mObjects;
					results.count = mObjects.size();
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = new LinkedHashMap<>(mObjects);
					}
					values = new LinkedHashMap<>(mOriginalValues);
				}
			}
			Map<G, ArrayList<C>> newValues = mIsAutoSortEnabled ? new TreeMap<G, ArrayList<C>>() : new LinkedHashMap<G, ArrayList<C>>();
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
			synchronized (mLock) {
				mLastConstraint = constraint;
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
