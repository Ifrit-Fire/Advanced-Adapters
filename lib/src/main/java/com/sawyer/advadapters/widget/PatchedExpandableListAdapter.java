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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Checkable;
import android.widget.ExpandableListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Meant to replace the {@link android.widget.BaseExpandableListAdapter} as the starting point
 * for building a specialized adapter for the {@link android.widget.ExpandableListView}. It provides
 * convenient capabilities lacking in the ExpandableListView; as well as patching it's broken choice
 * mode functionality.</p>
 *
 * <b>New Capabilities:</b><ul><li>Expand/Collapse All Groups</li> <li>Auto Expanding
 * Groups</li><li>Define individual group selectability</li><li>Four Available Choice
 * Modes</li></ul>
 *
 * <p><b>Choice Mode:</b> This adapter provides a variety of {@link ChoiceMode ChoiceModes} which
 * may be enabled through {@link #setChoiceMode(ChoiceMode) setChoiceMode()}. When enabled, you must
 * also provide a callback via {@link #setMultiChoiceModeListener(ChoiceModeListener)
 * setMultiChoiceModeListener()}. Additionally, you'll need to store the adapter's saved state which
 * can be retrieved using {@link #onSaveInstanceState()}.  Then subsequently restore it during
 * Activity recreation with {@link #onRestoreInstanceState(Parcelable)}.</p>
 *
 * <p><b>Ownership:</b> This adapter will take ownership of an ExpandableListView's {@link
 * android.widget.ExpandableListView.OnGroupClickListener}, {@link android.widget.ExpandableListView.OnChildClickListener},
 * and {@link android.widget.AbsListView.MultiChoiceModeListener}. Attempting to set those listeners
 * directly through the ExpandableListView is an error and will fail to function correctly.
 * Additionally all choice mode interactions must be conducted through this adapter.</p>
 */
public abstract class PatchedExpandableListAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "PatchedExpand...Adapter";

	private final OnDisableTouchListener mDisableTouchListener = new OnDisableTouchListener();
	private final OnChoiceModeClickListener mChoiceModeClickListener = new OnChoiceModeClickListener();

	/** Controls if/how the user may activate items in the {@link android.widget.ExpandableListView}. */
	ChoiceMode mChoiceMode;
	/** The ActionMode CAB used during any choice mode MODALs. Null when inactive. */
	ActionMode mChoiceActionMode;
	/**
	 * Wrapper for the multiple choice mode callback; PatchedExpandableListAdapter needs to perform
	 * a few extra actions around what application code does.
	 */
	ChoiceModeWrapper mModalChoiceModeWrapper;
	/**
	 * Running state of which group/child are currently checked. If {@link #hasStableIds()} is
	 * enabled, this will track each item via it's ID. Otherwise, it'll track the packed position.
	 */
	CheckedState mCheckStates;

	/**
	 * {@link WeakReference} to {@link android.widget.ExpandableListView} which the adapter is
	 * currently attached.
	 */
	WeakReference<ExpandableListView> mExpandableListView;
	/** User defined callback to be invoked when a group view has been clicked. */
	ExpandableListView.OnGroupClickListener mOnGroupClickListener;
	/** User defined callback to be invoked when a child view has been clicked. */
	ExpandableListView.OnChildClickListener mOnChildClickListener;

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter */
	private Context mContext;
	/**
	 * Indicates if there are any pending actions that need to be performed when a reference to the
	 * {@link ExpandableListView} is established.
	 */
	private Set<QueueAction> mQueueAction;

	/**
	 * Defines the various behaviors supported for use with {@link android.widget.ExpandableListView}.
	 * While similar to those found within {@link android.widget.AbsListView} they are not
	 * equivalent.
	 *
	 * @see #setChoiceMode(ChoiceMode) setChoiceMode()
	 */
	public enum ChoiceMode {
		/** Normal ExpandableListView that does not indicate choices. */
		NONE,
		/** The ExpandableListView allows up to one choice. */
		SINGLE,
		/** The ExpandableListView allows up to one choice in a modal selection mode. */
		SINGLE_MODAL,
		/** The ExpandableListView allows multiple choices. */
		MULTIPLE,
		/** The ExpandableListView allows multiple choices in a modal selection mode. */
		MULTIPLE_MODAL;

		public boolean isDisabled() {
			return this == NONE;
		}

		public boolean isModal() {
			return this == SINGLE_MODAL || this == MULTIPLE_MODAL;
		}

		public boolean isMultiple() {
			return this == MULTIPLE || this == MULTIPLE_MODAL;
		}

		public boolean isSingle() {
			return this == SINGLE || this == SINGLE_MODAL;
		}
	}

	/**
	 * Defines actions which could be requested but fail to happen because the adapter has no
	 * reference to it's {@link ExpandableListView}. This may occur for various reasons since the
	 * ExpandableListView is not known until View generation.
	 */
	private enum QueueAction {
		DO_NOTHING,
		COLLAPSE_ALL,
		EXPAND_ALL,
		EXPAND_ALL_ANIMATE,
		START_ACTION_MODE
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	public PatchedExpandableListAdapter(Context activity) {
		init(activity);
	}

	/** Convenience method to log when we unexpectedly lost our ExpandableListView reference. */
	private static void logLostReference(@NonNull String method) {
		Log.w(TAG, "Lost reference to ExpandableListView in " + method);
	}

	/**
	 * Clear any choices previously set. This will only be valid if the choice mode is not {@link
	 * ChoiceMode#NONE NONE} (default).
	 */
	public void clearChoices() {
		if (mCheckStates != null) {
			mCheckStates.clear();
		}
	}

	/** Collapse all groups in the adapter. */
	public void collapseAll() {
		if (hasAutoExpandingGroups()) {
			return;
		}
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null) {
			mQueueAction.add(QueueAction.COLLAPSE_ALL);
			return;
		}

		mQueueAction.remove(QueueAction.COLLAPSE_ALL);
		for (int index = 0; index < getGroupCount(); ++index) {
			lv.collapseGroup(index);
		}
	}

	/** Perform any and all queued up actions. */
	private void doAction() {
		for (QueueAction action : mQueueAction) {
			switch (action) {
			case COLLAPSE_ALL:
				collapseAll();
				break;
			case EXPAND_ALL:
				expandAll(false);
				break;
			case EXPAND_ALL_ANIMATE:
				expandAll(true);
				break;
			case START_ACTION_MODE:
				startActionMode();
				break;
			default:
				//Do nothing
			}
		}
	}

	/**
	 * Expand all groups in the adapter with no animation. Convenience wrapper call for {@link
	 * #expandAll(boolean)} with false passed in.
	 */
	public void expandAll() {
		expandAll(false);
	}

	/**
	 * Expand all groups in the adapter.
	 *
	 * @param animate True if the expanding groups should be animated in
	 */
	public void expandAll(boolean animate) {
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null) {
			mQueueAction.add((animate) ? QueueAction.EXPAND_ALL_ANIMATE : QueueAction.EXPAND_ALL);
			return;
		}

		mQueueAction.remove(QueueAction.EXPAND_ALL);
		mQueueAction.remove(QueueAction.EXPAND_ALL_ANIMATE);
		for (int index = 0; index < getGroupCount(); ++index) {
			lv.expandGroup(index, animate);
		}
	}

	/**
	 * <p>Returns the number of child items currently checked. This will only be valid if the choice
	 * mode is not {@link ChoiceMode#NONE NONE} (default).</p>
	 *
	 * <p>To determine the specific items that are currently checked, use one of the {@code
	 * getChecked*} methods.</p>
	 *
	 * @return The number of children currently checked.
	 *
	 * @see #getCheckedChildIds()
	 * @see #getCheckedChildIds()
	 * @see #getCheckedGroupIds()
	 * @see #getCheckedGroupPositions()
	 * @see #getCheckedChildPositions()
	 */
	public int getCheckedChildCount() {
		return (mCheckStates == null) ? 0 : mCheckStates.getCheckedChildCount();
	}

	/**
	 * Returns the set of checked children item ids. The result is only valid if the choice mode has
	 * not been set to {@link ChoiceMode#NONE NONE} and the adapter enabled {@link
	 * #hasStableIds()}.
	 *
	 * @return A new array which contains the id of each checked item in the list. Will never
	 * contain a null value.
	 */
	@NonNull
	public Long[] getCheckedChildIds() {
		return (mCheckStates == null) ? new Long[]{0L} : mCheckStates.getCheckedChildIds();
	}

	/**
	 * Returns the set of child items in the list which are checked. The result is only valid if the
	 * choice mode has not been set to {@link ChoiceMode#NONE NONE}.
	 *
	 * @return A Long array which contains only those children positions which are checked. Note
	 * these are packed positions.
	 */
	@NonNull
	public Long[] getCheckedChildPositions() {
		return (mCheckStates == null) ? new Long[]{-1L} : mCheckStates.getCheckedChildPositions();
	}

	/**
	 * <p>Returns the number of group items currently checked. This will only be valid if the choice
	 * mode is not {@link ChoiceMode#NONE NONE} (default).</p>
	 *
	 * <p>To determine the specific items that are currently checked, use one of the {@code
	 * getChecked*} methods.</p>
	 *
	 * @return The number of items currently checked
	 *
	 * @see #getCheckedChildIds()
	 * @see #getCheckedGroupIds()
	 * @see #getCheckedGroupPositions()
	 * @see #getCheckedChildPositions()
	 */
	public int getCheckedGroupCount() {
		return (mCheckStates == null) ? 0 : mCheckStates.getCheckedGroupCount();
	}

	/**
	 * Returns the set of checked group item ids. The result is only valid if the choice mode has
	 * not been set to {@link ChoiceMode#NONE NONE} and the adapter enabled {@link #hasStableIds()}
	 *
	 * @return A new array which contains the id of each checked item in the list. Will never
	 * contain a null value.
	 */
	@NonNull
	public Long[] getCheckedGroupIds() {
		return (mCheckStates == null) ? new Long[]{0L} : mCheckStates.getCheckedGroupIds();
	}

	/**
	 * Returns the set of group items in the list which are checked. The result is only valid if the
	 * choice mode has not been set to {@link ChoiceMode#NONE NONE}.
	 *
	 * @return An Integer array which contains only those group positions which are checked.
	 */
	@NonNull
	public Integer[] getCheckedGroupPositions() {
		return (mCheckStates == null) ? new Integer[]{-1} : mCheckStates.getCheckedGroupPositions();
	}

	/**
	 * Gets a View that displays the data for the given child within the given group.
	 *
	 * @param inflater      The LayoutInflater object that can be used to inflate each view.
	 * @param groupPosition The position of the group that contains the child
	 * @param childPosition The position of the child (for which the View is returned) within the
	 *                      group
	 * @param isLastChild   Whether the child is the last child within the group
	 * @param convertView   The old view to reuse, if possible. You should check that this view is
	 *                      non-null and of an appropriate type before using. If it is not possible
	 *                      to convert this view to display the correct data, this method can create
	 *                      a new view. It is not guaranteed that the convertView will have been
	 *                      previously created by this method.
	 * @param parent        The parent that this view will eventually be attached to
	 *
	 * @return the View corresponding to the child at the specified position
	 */
	@NonNull
	public abstract View getChildView(@NonNull LayoutInflater inflater, int groupPosition,
									  int childPosition, boolean isLastChild,
									  @Nullable View convertView, @NonNull ViewGroup parent);

	@Override
	public final View getChildView(int groupPosition, int childPosition, boolean isLastChild,
								   View convertView, ViewGroup parent) {
		View v = getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							  parent);
		updateChildCheckedView(groupPosition, childPosition, v);
		return v;
	}

	/**
	 * @return The current choice mode to be applied to the attached {@link
	 * android.widget.ExpandableListView}
	 */
	@NonNull
	public ChoiceMode getChoiceMode() {
		return mChoiceMode;
	}

	/**
	 * <p>Defines the choice behavior for the attached {@link android.widget.ExpandableListView}. By
	 * default, this adapter does not have any choice behavior ({@link ChoiceMode#NONE NONE}) set.
	 * By setting the choiceMode to {@link ChoiceMode#SINGLE SINGLE}, the ExpandableListView allows
	 * up to one item to be in an activation state. By setting the choiceMode to {@link
	 * ChoiceMode#MULTIPLE MULTIPLE}, the ExpandableListView allows any number of items to be
	 * chosen. Any of the MODAL variants will show a custom CAB when an item is long pressed.</p>
	 *
	 * <p>Use this method instead of {@link android.widget.ExpandableListView#setChoiceMode(int)}.
	 * This adapter will take over and emulate the behavior instead.</p>
	 *
	 * @param choiceMode One of the {@link ChoiceMode NONE} options
	 */
	public void setChoiceMode(@NonNull ChoiceMode choiceMode) {
		if (mChoiceMode == choiceMode) {
			return;
		}
		mChoiceMode = choiceMode;
		if (mChoiceActionMode != null) {
			mChoiceActionMode.finish();
		} else {
			updateClickListeners(null);
			clearChoices();
		}

		if (mChoiceMode.isDisabled()) {
			mCheckStates = null;
		} else {
			if (mCheckStates == null) {
				mCheckStates = new CheckedState();
			}
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
	public final View getGroupView(int groupPosition, boolean isExpanded, View convertView,
								   ViewGroup parent) {
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null || parent != lv) {
			if (parent instanceof ExpandableListView) {
				lv = (ExpandableListView) parent;
				if (lv.getChoiceMode() != AbsListView.CHOICE_MODE_NONE) {
					throw new RuntimeException(
							"Set choiceMode through attached adapter, not on the ExpandableListView itself");
				}
				mExpandableListView = new WeakReference<>(lv);
				updateClickListeners("getGroupView");
				doAction();
				//Check now and expandAll for auto expanding. Really helps reduce clunky/jumping scrolling
				//experience seen when otherwise individually expanding a group as we come across it.
				if (hasAutoExpandingGroups()) {
					expandAll(false);
				}
			} else {
				throw new IllegalStateException(
						"Expecting ExpandableListView when refreshing referenced state. Instead found unsupported " +
						parent.getClass().getSimpleName());
			}
		}

		if (!isExpanded && hasAutoExpandingGroups()) {
			//Usually only occurs if an adapter is modified post rendering to the screen. ExpandAll
			//prevents a clunky/jumpy scrolling experience seen when otherwise individually expanding a
			//group.
			expandAll(false);
		}

		View v = getGroupView(mInflater, groupPosition, isExpanded, convertView, parent);
		if (!isGroupSelectable(groupPosition)) {
			v.setOnTouchListener(mDisableTouchListener);
		}
		updateGroupCheckedView(groupPosition, v);

		return v;
	}

	/**
	 * Gets a View that displays the given group. This View is only for the group--the Views for the
	 * group's children will be fetched using {@link #getChildView(LayoutInflater, int, int,
	 * boolean, View, ViewGroup) getChildView()}.
	 *
	 * @param inflater      The LayoutInflater object that can be used to inflate each view.
	 * @param groupPosition The position of the group for which the View is returned
	 * @param isExpanded    Whether the group is expanded or collapsed
	 * @param convertView   The old view to reuse, if possible. You should check that this view is
	 *                      non-null and of an appropriate type before using. If it is not possible
	 *                      to convert this view to display the correct data, this method can create
	 *                      a new view. It is not guaranteed that the convertView will have been
	 *                      previously created by this method.
	 * @param parent        The parent that this view will eventually be attached to
	 *
	 * @return The View corresponding to the group at the specified position
	 */
	@NonNull
	public abstract View getGroupView(@NonNull LayoutInflater inflater, int groupPosition,
									  boolean isExpanded, @Nullable View convertView,
									  @NonNull ViewGroup parent);

	/**
	 * @return Whether groups are always forced to render expanded. Default is false.
	 */
	public boolean hasAutoExpandingGroups() {
		return false;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	private void init(@NonNull Context activity) {
		mContext = activity;
		mInflater = LayoutInflater.from(mContext);
		mExpandableListView = new WeakReference<>(null);    //We'll obtain reference in getGroupView
		mChoiceMode = ChoiceMode.NONE;
		mQueueAction = new HashSet<>(QueueAction.values().length);
	}

	/**
	 * Returns the checked state of the specified child item position. Will always return false if
	 * choice mode is {@link ChoiceMode#NONE NONE}
	 *
	 * @param groupPosition The position of the group that contains the child.
	 * @param childPosition The position of the child.
	 *
	 * @return The child item's checked state or false if choice mode is disabled.
	 *
	 * @see #setChoiceMode(ChoiceMode) setChoiceMode()
	 */
	public boolean isChildChecked(int groupPosition, int childPosition) {
		return mCheckStates != null && mCheckStates.getChild(groupPosition, childPosition);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * Returns the checked state of the specified group item position. Will always return false if
	 * choice mode is {@link ChoiceMode#NONE NONE}
	 *
	 * @param groupPosition The position of the group item
	 *
	 * @return The group item's checked state or {@code false} if choice mode is disabled.
	 *
	 * @see #setChoiceMode(ChoiceMode) setChoiceMode
	 */
	public boolean isGroupChecked(int groupPosition) {
		return mCheckStates != null && mCheckStates.getGroup(groupPosition);
	}

	/**
	 * Whether the group at the specified position is selectable.
	 *
	 * @param groupPosition The position of the group that contains the child
	 *
	 * @return Whether the group is selectable. Default is true.
	 */
	public boolean isGroupSelectable(int groupPosition) {
		return true;
	}

	/**
	 * Re-apply a representation of its internal state that had previously been generated by {@link
	 * #onSaveInstanceState()}.  Specifically this restores the ChoiceMode, ActionMode and checked
	 * children/group items of the adapter. This method will not restore the internal data
	 * originally stored with the adapter. That must still be done manually as you would with any
	 * other adapter. If not using choice mode, there is no need to invoke this.
	 *
	 * @param state The frozen state that had previously been returned by {@link
	 *              #onSaveInstanceState}.
	 *
	 * @see #onSaveInstanceState()
	 */
	public void onRestoreInstanceState(@Nullable Parcelable state) {
		if (state instanceof SavedState) {
			SavedState ss = (SavedState) state;
			setChoiceMode(ss.choiceMode);
			if (mCheckStates != null) {
				mCheckStates.groupIds = ss.groupIds;
				mCheckStates.childIds = ss.childIds;
				mCheckStates.groupPositions = ss.groupPositions;
				mCheckStates.childPositions = ss.childPositions;
			}
			if (ss.inActionMode) {
				startActionMode();
			}
		}
	}

	/**
	 * Saves the internal state of the adapter for use when re-instantiating a new adapter.
	 * Primarily use this for restoring the ChoiceMode, ActionMode and checked children/group items
	 * of the adapter. This method will NOT persist the internal data stored with the adapter. That
	 * must still be done manually as you would with any other adapter. If not using choice mode,
	 * there is no need to invoke this.
	 *
	 * @return Returns a Parcelable object containing the ChoiceMode, ActionMode and selected
	 * children/group items. Will never return null.
	 *
	 * @see #onRestoreInstanceState(android.os.Parcelable)
	 */
	@NonNull
	public Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState();
		if (mCheckStates != null) {
			ss.groupIds = mCheckStates.groupIds;
			ss.childIds = mCheckStates.childIds;
			ss.groupPositions = mCheckStates.groupPositions;
			ss.childPositions = mCheckStates.childPositions;
		}
		ss.inActionMode = (mChoiceActionMode != null);
		ss.choiceMode = mChoiceMode;
		return ss;
	}

	/**
	 * Sets the checked state of the specified child. Will not work if choice mode is set to {@link
	 * ChoiceMode#NONE}.
	 *
	 * @param groupPosition The position of the group that contains the child.
	 * @param childPosition The position of the child.
	 * @param isChecked     The new checked state for the child item.
	 */
	public void setChildChecked(int groupPosition, int childPosition, boolean isChecked) {
		if (mChoiceMode.isDisabled()) {
			return;
		}
		boolean updateViews;
		boolean oldCheck;

		//Update child checked state
		long groupId = getGroupId(groupPosition);
		long childId = getChildId(groupPosition, childPosition);
		if (mChoiceMode.isSingle()) {
			oldCheck = mCheckStates.getChild(groupPosition, childPosition);
			mCheckStates.clear();
			mCheckStates.putChild(groupPosition, childPosition, childId, isChecked);
		} else {
			oldCheck = mCheckStates.putChild(groupPosition, childPosition, childId, isChecked);
		}
		updateViews = (oldCheck != isChecked);

		//If modal, verify ActionMode and invoke it's listener
		boolean treatAsModal = mChoiceMode.isModal();
		if (treatAsModal) {
			ExpandableListView lv = mExpandableListView.get();
			if (lv == null) {
				//In a bad modal state, reset and ignore modal behavior
				treatAsModal = false;
				updateViews = false;
				if (mChoiceActionMode != null) {
					mChoiceActionMode.finish();
				}
			} else {
				//We are activating for the first time, ensure ActionMode is launched
				if (mCheckStates.getCheckedChildCount() + mCheckStates.getCheckedGroupCount() > 0) {
					if (mChoiceActionMode == null) {
						startActionMode();
					}
				}
				//Notify ActionMode if change actually occurred
				if (updateViews) {
					mModalChoiceModeWrapper
							.onChildCheckedStateChanged(mChoiceActionMode, groupPosition, groupId,
														childPosition, childId, isChecked);
				}
			}
		}

		//Check if all children of a group have the same checked value. If so, ensure group matches
		if (mChoiceMode.isMultiple()) {
			int childrenCount = getChildrenCount(groupPosition);
			boolean expectedGroupState = isChecked;
			for (childPosition = 0; childPosition < childrenCount; ++childPosition) {
				if (mCheckStates.getChild(groupPosition, childPosition) != isChecked) {
					expectedGroupState = false;
					break;
				}
			}

			//Only if group check state is different from what we expect, then update
			if (mCheckStates.getGroup(groupPosition) != expectedGroupState) {
				updateViews = true;
				mCheckStates.putGroup(groupPosition, groupId, isChecked);
				//Notify ActionMode if change actually occurred
				if (treatAsModal) {
					mModalChoiceModeWrapper
							.onGroupCheckedStateChanged(mChoiceActionMode, groupPosition, groupId,
														isChecked);
				}
			}
		}

		if (updateViews) {
			updateOnScreenCheckedViews();
		}
	}

	/**
	 * Sets the checked state of the specified group. Will not work if the choice mode is set to
	 * {@link ChoiceMode#NONE NONE}. If choice mode is set to a multiple selector, Eg {@link
	 * ChoiceMode#MULTIPLE MULTIPLE} or {@link ChoiceMode#MULTIPLE_MODAL MULTIPLE_MODAL} then all
	 * child items will additionally have their check state updated.
	 *
	 * @param groupPosition The position of the group.
	 * @param isChecked     The new checked state for the child item.
	 */
	public void setGroupChecked(int groupPosition, boolean isChecked) {
		if (mChoiceMode.isDisabled()) {
			return;
		}
		boolean updateViews;
		boolean oldCheck;

		//Update group checked state
		long groupId = getGroupId(groupPosition);
		if (mChoiceMode.isSingle()) {
			oldCheck = mCheckStates.getGroup(groupPosition);
			mCheckStates.clear();
			mCheckStates.putGroup(groupPosition, groupId, isChecked);
		} else {
			oldCheck = mCheckStates.putGroup(groupPosition, groupId, isChecked);
		}
		updateViews = (oldCheck != isChecked);

		//If modal, verify ActionMode and invoke it's listener
		ExpandableListView lv = mExpandableListView.get();
		boolean treatAsModal = mChoiceMode.isModal();
		if (treatAsModal) {
			if (lv == null) {
				//In a bad modal state, reset and ignore modal behavior
				treatAsModal = false;
				if (mChoiceActionMode != null) {
					mChoiceActionMode.finish();
					updateViews = false;
				}
			} else {
				//We are activating for the first time, ensure ActionMode is launched
				if (mCheckStates.getCheckedChildCount() + mCheckStates.getCheckedGroupCount() > 0) {
					if (mChoiceActionMode == null) {
						startActionMode();
					}
				}
				//Notify ActionMode if a change actually occurred
				if (updateViews) {
					mModalChoiceModeWrapper
							.onGroupCheckedStateChanged(mChoiceActionMode, groupPosition, groupId,
														isChecked);
				}
			}
		}

		//Ensure all children have the same checked state and update accordingly
		if (mChoiceMode.isMultiple()) {
			int childrenCount = getChildrenCount(groupPosition);
			if (treatAsModal) {
				for (int childPosition = 0; childPosition < childrenCount; ++childPosition) {
					long childId = getChildId(groupPosition, childPosition);
					//Only notify ActionMode if there was an actual change
					if (mCheckStates.putChild(groupPosition, childPosition, childId, isChecked) !=
						isChecked) {
						updateViews = true;
						mModalChoiceModeWrapper
								.onChildCheckedStateChanged(mChoiceActionMode, groupPosition,
															groupId, childPosition, childId,
															isChecked);
					}
				}
			} else {
				for (int childPosition = 0; childPosition < childrenCount; ++childPosition) {
					long childId = getChildId(groupPosition, childPosition);
					if (mCheckStates.putChild(groupPosition, childPosition, childId, isChecked) !=
						isChecked) {
						updateViews = true;
					}
				}
			}
		}

		if (updateViews) {
			updateOnScreenCheckedViews();
		}
	}

	/**
	 * Set a {@link PatchedExpandableListAdapter.ChoiceModeListener ChoiceModeListener} that will
	 * manage the lifecycle of the selection {@link android.view.ActionMode}. Only used when the
	 * choice mode is set to modal variant of {@link ChoiceMode ChoiceMode}. Eg {@link
	 * ChoiceMode#MULTIPLE_MODAL MULITPLE_MODAL} or {@link ChoiceMode#SINGLE_MODAL SINGLE_MODAL}.
	 *
	 * @param listener Callback that will manage the selection mode
	 */
	public void setMultiChoiceModeListener(@Nullable ChoiceModeListener listener) {
		if (mModalChoiceModeWrapper == null) {
			mModalChoiceModeWrapper = new ChoiceModeWrapper();
		}
		mModalChoiceModeWrapper.setWrapped(listener);
	}

	/**
	 * <p>Register a callback to be invoked when a child item has been clicked. Whether a listener
	 * is registered or not, this adapter takes ownership of the {@link
	 * android.widget.ExpandableListView}'s equivalent listeners. Attempting to set the callback
	 * directly through the ExpandableListView will not work.</p>
	 *
	 * <p>When a modal {@link ChoiceMode ChoiceMode} CAB is activated, all children click events
	 * will be ignored by this callback. Instead use the {@link #setMultiChoiceModeListener(PatchedExpandableListAdapter.ChoiceModeListener)
	 * setMultiChoiceModeListener()} to handle that case.</p>
	 *
	 * @param onChildClickListener The callback that will be invoked.
	 */
	public void setOnChildClickListener(
			@Nullable ExpandableListView.OnChildClickListener onChildClickListener) {
		mOnChildClickListener = onChildClickListener;
		updateClickListeners(null);
	}

	/**
	 * <p>Register a callback to be invoked when a group item has been clicked. Whether a listener
	 * is registered or not, this adapter takes ownership of the {@link
	 * android.widget.ExpandableListView}'s equivalent listeners. Attempting to set the callback
	 * directly through the ExpandableListView will not work.</p>
	 *
	 * <p>When a modal {@link ChoiceMode ChoiceMode} CAB is activated, all group click events will
	 * be ignored by this callback. Instead use the {@link #setMultiChoiceModeListener(PatchedExpandableListAdapter.ChoiceModeListener)
	 * setMultiChoiceModeListener()} to handle that case.</p>
	 *
	 * @param onGroupClickListener The callback that will be invoked.
	 */
	public void setOnGroupClickListener(
			@Nullable ExpandableListView.OnGroupClickListener onGroupClickListener) {
		mOnGroupClickListener = onGroupClickListener;
		updateClickListeners(null);
	}

	/**
	 * Start an {@link android.view.ActionMode}. Use this method instead of {@link
	 * android.widget.ExpandableListView#startActionMode(android.view.ActionMode.Callback)
	 * ExpandableListView.startActionMode()}.
	 */
	public void startActionMode() {
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null) {
			mQueueAction.add(QueueAction.START_ACTION_MODE);
			return;
		}
		mQueueAction.remove(QueueAction.START_ACTION_MODE);
		mChoiceActionMode = lv.startActionMode(mModalChoiceModeWrapper);
		updateClickListeners(null);
	}

	private void updateChildCheckedView(int groupPosition, int childPosition, @NonNull View v) {
		if (mCheckStates == null) {
			return;
		}
		if (v instanceof Checkable) {
			((Checkable) v).setChecked(mCheckStates.getChild(groupPosition, childPosition));
		} else {
			v.setActivated(mCheckStates.getChild(groupPosition, childPosition));
		}
	}

	/**
	 * Helper method which ensures our click listeners are in the correct state.
	 *
	 * @param debugTag Text to output if we unexpectedly loose ExpandableListView referencing. Null
	 *                 if we don't care and nothing will be logged.
	 */
	private void updateClickListeners(@Nullable String debugTag) {
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null) {
			if (!TextUtils.isEmpty(debugTag)) {
				logLostReference(debugTag);
			}
			return;
		}

		switch (mChoiceMode) {
		case SINGLE:
		case MULTIPLE:
			lv.setOnChildClickListener(mChoiceModeClickListener);
			lv.setOnGroupClickListener(mChoiceModeClickListener);
			break;
		case SINGLE_MODAL:
		case MULTIPLE_MODAL:
			if (mChoiceActionMode != null) {
				lv.setLongClickable(false);
				lv.setOnChildClickListener(mChoiceModeClickListener);
				lv.setOnGroupClickListener(mChoiceModeClickListener);
			} else {
				lv.setOnItemLongClickListener(mChoiceModeClickListener);
				lv.setOnGroupClickListener(mOnGroupClickListener);
				lv.setOnChildClickListener(mOnChildClickListener);
			}
			break;
		case NONE:
			lv.setOnGroupClickListener(mOnGroupClickListener);
			lv.setOnChildClickListener(mOnChildClickListener);
			break;
		}
	}

	private void updateGroupCheckedView(int groupPosition, @NonNull View v) {
		if (mCheckStates == null) {
			return;
		}
		if (v instanceof Checkable) {
			((Checkable) v).setChecked(mCheckStates.getGroup(groupPosition));
		} else {
			v.setActivated(mCheckStates.getGroup(groupPosition));
		}
	}

	/**
	 * Perform a quick, in-place update of the checked or activated state on all visible item views.
	 * This should only be called when a valid choice mode is active.
	 */
	private void updateOnScreenCheckedViews() {
		ExpandableListView lv = mExpandableListView.get();
		if (lv == null) {
			return;
		}
		final int firstPos = lv.getFirstVisiblePosition();
		final int lastPos = lv.getChildCount();

		for (int index = 0; index < lastPos; ++index) {
			View child = lv.getChildAt(index);
			long packedPosition = lv.getExpandableListPosition(firstPos + index);
			int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
			boolean isChecked;
			switch (ExpandableListView.getPackedPositionType(packedPosition)) {
			case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
				isChecked = mCheckStates.getGroup(groupPosition);
				break;
			case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
				isChecked = mCheckStates.getChild(groupPosition, childPosition);
				break;
			default:
				Log.w(TAG, "updateOnScreenCheckedViews received unknown packed position?");
				continue;
			}
			if (child instanceof Checkable) {
				((Checkable) child).setChecked(isChecked);
			} else {
				child.setActivated(isChecked);
			}
		}
	}

	/**
	 * An interface definition for callbacks that receive events for {@link ChoiceMode ChoiceMode}
	 * modal variants. It acts as the {@link android.view.ActionMode.Callback} for the selection
	 * mode and also receives checked state change events when the user selects and deselects groups
	 * or children views.
	 */
	public interface ChoiceModeListener extends ActionMode.Callback {

		/**
		 * Called when a child item is checked or unchecked during selection mode.
		 *
		 * @param mode          The {@link android.view.ActionMode} providing the selection mode
		 * @param childPosition Adapter position of the child item that was checked or unchecked
		 * @param childId       Adapter ID of the child item that was checked or unchecked
		 * @param groupPosition Adapter position of the group this child belongs to.
		 * @param groupId       Adapter ID of the group this child belongs to.
		 * @param checked       {@code true} if the item is now checked, {@code false} if the item
		 *                      is now unchecked.
		 */
		void onChildCheckedStateChanged(@NonNull ActionMode mode, int groupPosition, long groupId,
										int childPosition, long childId, boolean checked);

		/**
		 * Called when a group item is checked or unchecked during selection mode. If the group is
		 * expanded, then all of it's children will have their checked state changed to match and
		 * {@link #onChildCheckedStateChanged(ActionMode, int, long, int, long, boolean)
		 * onChildCheckedStateChanged()} will be appropriately invoked for each.
		 *
		 * @param mode          The {@link android.view.ActionMode} providing the selection mode
		 * @param groupPosition Adapter position of the group item that was checked or unchecked
		 * @param groupId       Adapter ID of the group item that was checked or unchecked
		 * @param checked       {@code true} if the item is now checked, {@code false} if the item
		 *                      is now unchecked.
		 */
		void onGroupCheckedStateChanged(@NonNull ActionMode mode, int groupPosition, long groupId,
										boolean checked);
	}

	private static class OnDisableTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;    //Do nothing but consume touch event
		}
	}

	/**
	 * Handles reading from and writing to a {@link Parcel}.  The parcel is used for maintaining the
	 * adapter's state, specifically for the ChoiceMode, ActionMode, and checked group/children
	 * items.
	 */
	static class SavedState implements Parcelable {
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		Map<Long, Integer> groupIds;    //Id, group position.
		Map<Long, Long> childIds;    //Id, packed position
		Set<Integer> groupPositions;
		Set<Long> childPositions;    //packed position
		boolean inActionMode;
		ChoiceMode choiceMode;

		public SavedState() {
		}

		@SuppressWarnings("unchecked")
		public SavedState(@NonNull Parcel source) {
			int size = source.readInt();
			if (size >= 0) {
				groupIds = new HashMap<>(size);
				for (int index = 0; index < size; ++index) {
					source.readValue(Long.class.getClassLoader());
					source.readValue(Integer.class.getClassLoader());
				}
			}
			size = source.readInt();
			if (size >= 0) {
				childIds = new HashMap<>(size);
				for (int index = 0; index < size; ++index) {
					source.readValue(Long.class.getClassLoader());
					source.readValue(Long.class.getClassLoader());
				}
			}
			List list = source.readArrayList(Integer.class.getClassLoader());
			if (list != null) {
				groupPositions = new HashSet<>(list);
			}
			list = source.readArrayList(Long.class.getClassLoader());
			if (list != null) {
				childPositions = new HashSet<>(list);
			}
			inActionMode = (source.readByte() != 0);
			choiceMode = ChoiceMode.values()[source.readInt()];
		}

		//Be careful on what K and V are. Must be valid Object types for use with Parcel#writeValue()
		private static <K, V> void writeMap(@NonNull Parcel dest, @Nullable Map<K, V> map) {
			if (map == null) {
				dest.writeInt(-1);
			} else {
				dest.writeInt(map.size());
				for (Map.Entry<K, V> entry : map.entrySet()) {
					dest.writeValue(entry.getKey());
					dest.writeValue(entry.getValue());
				}
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			writeMap(dest, groupIds);
			writeMap(dest, childIds);
			dest.writeList((groupPositions == null) ? null : new ArrayList(groupPositions));
			dest.writeList((childPositions == null) ? null : new ArrayList(childPositions));
			dest.writeByte((byte) (inActionMode ? 1 : 0));
			dest.writeInt(choiceMode.ordinal());
		}
	}

	/**
	 * Tracks the checked state of all child and group items via either their IDs or positions. If
	 * stable IDs are enabled, then all items will be tracked via their IDs. If stable IDs are
	 * disabled, then all items will be tracked via their positions instead. Basically this class
	 * conveniently abstracts away those details for the utilizing parent class.
	 */
	private class CheckedState {
		//Inclusion in collections means checked. Exclusion from collections means false.
		public Map<Long, Integer> groupIds;    //Id, group position.
		public Map<Long, Long> childIds;    //Id, packed position
		public Set<Integer> groupPositions;
		public Set<Long> childPositions;    //packed position

		public CheckedState() {
			if (hasStableIds()) {
				groupIds = new HashMap<>();
				childIds = new HashMap<>();
			} else {
				groupPositions = new HashSet<>();
				childPositions = new HashSet<>();
			}
		}

		public void clear() {
			if (hasStableIds()) {
				groupIds.clear();
				childIds.clear();
			} else {
				groupPositions.clear();
				childPositions.clear();
			}
		}

		public int getCheckedChildCount() {
			return hasStableIds() ? childIds.size() : childPositions.size();
		}

		@NonNull
		public Long[] getCheckedChildIds() {
			if (hasStableIds()) {
				return childIds.keySet().toArray(new Long[childIds.size()]);
			} else {
				return new Long[]{0L};
			}
		}

		@NonNull
		public Long[] getCheckedChildPositions() {
			if (hasStableIds()) {
				return childIds.values().toArray(new Long[childIds.size()]);
			} else {
				return childPositions.toArray(new Long[childPositions.size()]);
			}
		}

		public int getCheckedGroupCount() {
			return hasStableIds() ? groupIds.size() : groupPositions.size();
		}

		@NonNull
		public Long[] getCheckedGroupIds() {
			if (hasStableIds()) {
				return groupIds.keySet().toArray(new Long[groupIds.size()]);
			} else {
				return new Long[]{0L};
			}
		}

		@NonNull
		public Integer[] getCheckedGroupPositions() {
			if (hasStableIds()) {
				return groupIds.values().toArray(new Integer[groupIds.size()]);
			} else {
				return groupPositions.toArray(new Integer[groupPositions.size()]);
			}
		}

		/**
		 * @return The checked state of a child with the given positions.
		 */
		public boolean getChild(int groupPosition, int childPosition) {
			if (hasStableIds()) {
				return childIds.containsKey(getChildId(groupPosition, childPosition));
			} else {
				long packedPosition = ExpandableListView
						.getPackedPositionForChild(groupPosition, childPosition);
				return childPositions.contains(packedPosition);
			}
		}

		/**
		 * @return The checked state of a group with the given group position
		 */
		public boolean getGroup(int groupPosition) {
			if (hasStableIds()) {
				return groupIds.containsKey(getGroupId(groupPosition));
			} else {
				return groupPositions.contains(groupPosition);
			}
		}

		/**
		 * Records the checked state of the given child positions.
		 *
		 * @return The previous checked state of the child position.
		 */
		public boolean putChild(int groupPosition, int childPosition, long childId,
								boolean isChecked) {
			long packedPosition = ExpandableListView
					.getPackedPositionForChild(groupPosition, childPosition);
			if (hasStableIds()) {
				Long result = (isChecked) ? childIds.put(childId, packedPosition) : childIds
						.remove(childId);
				return result != null;
			} else {
				return (isChecked) ? !childPositions.add(packedPosition) : childPositions
						.remove(packedPosition);
			}
		}

		/**
		 * Records the checked state of the given group position.
		 *
		 * @return The previous checked state of the group position.
		 */
		public boolean putGroup(int groupPosition, long groupId, boolean isChecked) {
			if (hasStableIds()) {
				Integer result = (isChecked) ? groupIds.put(groupId, groupPosition) : groupIds
						.remove(groupId);
				return result != null;
			} else {
				return (isChecked) ? !groupPositions.add(groupPosition) : groupPositions
						.remove(groupPosition);
			}
		}
	}

	/**
	 * Wraps around the {@link AbsListView.MultiChoiceModeListener} and converts it's item checked
	 * change callbacks to group or child checked changed events. Note, the user defined group and
	 * child click listeners will be bypassed when the CAB appears.
	 */
	private class ChoiceModeWrapper implements ChoiceModeListener {
		private ChoiceModeListener mWrapped;

		public boolean hasWrappedCallback() {
			return mWrapped != null;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return mWrapped.onActionItemClicked(mode, item);
		}

		@Override
		public void onChildCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, int childPosition, long childId,
											   boolean checked) {
			mWrapped.onChildCheckedStateChanged(mode, groupPosition, groupId, childPosition,
												childId, checked);
			if (mCheckStates.getCheckedChildCount() + mCheckStates.getCheckedGroupCount() == 0) {
				mode.finish();
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return mWrapped.onCreateActionMode(mode, menu);
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mWrapped.onDestroyActionMode(mode);
			mChoiceActionMode = null;
			updateClickListeners("onDestroyActionMode");
			if (mCheckStates.getCheckedChildCount() + mCheckStates.getCheckedGroupCount() != 0) {
				clearChoices();
				updateOnScreenCheckedViews();
			}
		}

		@Override
		public void onGroupCheckedStateChanged(@NonNull ActionMode mode, int groupPosition,
											   long groupId, boolean checked) {
			mWrapped.onGroupCheckedStateChanged(mode, groupPosition, groupId, checked);
			if (mCheckStates.getCheckedChildCount() + mCheckStates.getCheckedGroupCount() == 0) {
				mode.finish();
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return mWrapped.onPrepareActionMode(mode, menu);
		}

		public void setWrapped(@Nullable ChoiceModeListener wrapped) {
			mWrapped = wrapped;
		}
	}

	/**
	 * Special click listener attached to group and child items when the ActionMode starts and
	 * removed when it's destroyed. Handles activating the items that were clicked by the user
	 * during this period.
	 */
	private class OnChoiceModeClickListener implements ExpandableListView.OnChildClickListener,
			ExpandableListView.OnGroupClickListener, AdapterView.OnItemLongClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
									int childPosition, long id) {
			boolean isChecked = mCheckStates.getChild(groupPosition, childPosition);
			setChildChecked(groupPosition, childPosition, !isChecked);
			if (!mChoiceMode.isModal() && mOnChildClickListener != null) {
				mOnChildClickListener.onChildClick(parent, v, groupPosition, childPosition, id);
			}
			return true;
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			setGroupChecked(groupPosition, !mCheckStates.getGroup(groupPosition));
			if (!mChoiceMode.isModal() && mOnGroupClickListener != null) {
				mOnGroupClickListener.onGroupClick(parent, v, groupPosition, id);
			}
			return true;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (!mChoiceMode.isModal()) {
				return false;
			}
			if (mModalChoiceModeWrapper == null || !mModalChoiceModeWrapper.hasWrappedCallback()) {
				throw new IllegalStateException(
						"Attempted to start selection mode for " + mChoiceMode.toString() +
						" but no choice mode callback was supplied. Invoke #setMultiChoiceModeListener" +
						" to set a callback.");
			}
			mQueueAction.remove(QueueAction.START_ACTION_MODE);
			mChoiceActionMode = parent.startActionMode(mModalChoiceModeWrapper);
			updateClickListeners("onItemLongClick");

			ExpandableListView lv = (ExpandableListView) parent;
			long packedPosition = lv.getExpandableListPosition(position);
			int groupPosition;
			switch (ExpandableListView.getPackedPositionType(packedPosition)) {
			case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
				groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
				setGroupChecked(groupPosition, true);
				break;

			case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
				groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
				int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
				setChildChecked(groupPosition, childPosition, true);
				break;

			default:
				Log.w(TAG, "onItemCheckedStateChanged received unknown packed position?");
				return false;
			}
			return true;
		}
	}
}
