package com.sawyer.advadapters.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private Map<G, List<C>> mObjects;
	private List<G> mGroupObjects;
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
	private Map<G, List<C>> mOriginalValues;
	private List<G> mGroupOriginalValues;
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

	/**
	 * Creates a new group class object which represents the given child item. This is used to
	 * determine what group item the child item will fall under. Internally this relationship is
	 * cached to optimize how often this method is invoked. Ideally only once per child item.
	 * However any changes to the child or it's parent group will result in additional invocations
	 * of this method.
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
		return (long) groupPosition << 32 | childPosition & 0xFFFFFFFFL;
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
		return mFilter;
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
				throw new NullPointerException("createGroupFor() must return a non-null value");
			}
		}
		mChild2Group.put(child, group);
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

	@Override
	public boolean hasStableIds() {
		return false;
	}

	private void init(Context context, Collection<C> objects) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mObjects = new LinkedHashMap<>();
		mGroupObjects = new ArrayList<>();
		mChild2Group = new HashMap<>(objects.size());

		for (C object : objects) {
			G group = getGroupFor(object);
			List<C> children = mObjects.get(group);
			if (children == null) {
				children = new ArrayList<>();
				mObjects.put(group, children);
			}
			children.add(object);
		}
		mGroupObjects.addAll(mObjects.keySet());
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

	private class RolodexFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			final Map<G, List<C>> values;
			final List<G> groups;

			synchronized (mLock) {
				if (TextUtils.isEmpty(constraint)) {    //Clearing out filtered results
					if (mOriginalValues != null) {
						mObjects = new LinkedHashMap<>(mOriginalValues);
						mGroupObjects = new ArrayList<>(mGroupOriginalValues);
						mOriginalValues = null;
						mGroupOriginalValues = null;
					}
					results.values = new Pair<>(mObjects, mGroupObjects);
					results.count = Math.max(mObjects.size(), mGroupObjects.size());
					return results;
				} else {    //Ready for filtering
					if (mOriginalValues == null) {
						mOriginalValues = new LinkedHashMap<>(mObjects);
						mGroupOriginalValues = new ArrayList<>(mGroupObjects);
					}
					values = new LinkedHashMap<>(mOriginalValues);
					groups = new ArrayList<>(mGroupOriginalValues);
				}
			}

			final Map<G, List<C>> newValues = new LinkedHashMap<>();
			final List<G> newGroups = new ArrayList<>();
			for (G group : groups) {
				if (!isGroupFilteredOut(group, constraint)) {
					newGroups.add(group);
					List<C> children = new ArrayList<>();
					for (C child : values.get(group)) {
						if (!isChildFilteredOut(child, constraint)) {
							children.add(child);
						}
					}
					newValues.put(group, children);
				}
			}

			results.values = new Pair<>(newValues, newGroups);
			results.count = newValues.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mLastConstraint = constraint;
			Pair<Map<G, List<C>>, List<G>> pair = (Pair<Map<G, List<C>>, List<G>>) results.values;
			mObjects = pair.first;
			mGroupObjects = pair.second;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
