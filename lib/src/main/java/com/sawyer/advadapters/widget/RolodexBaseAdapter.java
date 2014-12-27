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
import android.os.Parcelable;
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
import java.util.HashMap;

/**
 * TODO: Write this
 */
public abstract class RolodexBaseAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "RolodexBaseAdapter";

	private final OnDisableTouchListener mDisableTouchListener = new OnDisableTouchListener();
	private final OnChoiceModeClickListener mChoiceModeClickListener = new OnChoiceModeClickListener();

	/** Controls if/how the user may activate items in the {@link ExpandableListView}. */
	ChoiceMode mChoiceMode;
	/** The ActionMode CAB used during any choice mode MODALs. Null when inactive. */
	ActionMode mChoiceActionMode;
	/**
	 * Wrapper for the multiple choice mode callback; RolodexBaseAdapter needs to perform a few
	 * extra actions around what application code does.
	 */
	ModalChoiceModeWrapper mModalChoiceModeWrapper;
	/**
	 * Running state of which group/child are currently checked. If {@link #hasStableIds()} is
	 * enabled, this will track each item via it's ID. Otherwise, it'll track the packed position.
	 */
	HashMapWrapper mCheckStates;

	/** {@link WeakReference} to {@link ExpandableListView} which the adapter is currently attached. */
	WeakReference<ExpandableListView> mListView;
	/** User defined callback to be invoked when a group view has been clicked. */
	ExpandableListView.OnGroupClickListener mOnGroupClickListener;
	/** User defined callback to be invoked when a child view has been clicked. */
	ExpandableListView.OnChildClickListener mOnChildClickListener;

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter */
	private Context mContext;
	/**
	 * Indicates if there are any pending actions that need to be performed. Certain actions will
	 * get queued up when they fail to complete due to having no reference to the attached {@link
	 * ExpandableListView}
	 */
	private QueueAction mQueueAction;
	/**
	 * The saved state of the previously attached {@link ExpandableListView}. This is used solely
	 * for restoring the state of the CAB when setChoiceMode is turned on.
	 */
	private Parcelable mParcelState;

	/**
	 * TODO: Write this
	 */
	public static enum ChoiceMode {
		NONE,
		SINGLE,
		SINGLE_MODAL,
		MULTIPLE,
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
	 * reference to it's {@link ExpandableListView}. These actions then must be queued up to occur
	 * once a new reference has been re-established.
	 * <p/>
	 * This mainly solves the edge case where an adapter is attached to a ExpandableListView and
	 * told to expand/collapse all before any View's are drawn. Since the ExpandableListView is not
	 * known until View generation, the request would fail to do anything.
	 */
	private static enum QueueAction {
		EXPAND_ALL,
		EXPAND_ALL_ANIMATE,
		COLLAPSE_ALL,
		DO_NOTHING
	}

	/**
	 * Constructor
	 *
	 * @param activity Context used for inflating views
	 */
	RolodexBaseAdapter(Context activity) {
		init(activity);
	}

	/** Convenience method to log when we unexpectedly lost our ExpandableListView reference. */
	private static void logLostReference(String method) {
		Log.w(TAG, "Lost reference to ExpandableListView in " + method);
	}

	/**
	 * Clear any choices previously set
	 */
	public void clearChoices() {
		if (mCheckStates != null) {
			mCheckStates.clear();
		}
	}

	/** Collapse all groups in the adapter. */
	public void collapseAll() {
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			mQueueAction = QueueAction.COLLAPSE_ALL;
			return;
		}

		mQueueAction = QueueAction.DO_NOTHING;
		if (hasAutoExpandingGroups()) return;
		for (int index = 0; index < getGroupCount(); ++index) {
			lv.collapseGroup(index);
		}
	}

	private void doAction() {
		switch (mQueueAction) {
		case COLLAPSE_ALL:
			collapseAll();
			break;
		case EXPAND_ALL:
			expandAll(false);
			break;
		case EXPAND_ALL_ANIMATE:
			expandAll(true);
			break;
		default:
			//Do nothing
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
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			mQueueAction = animate ? QueueAction.EXPAND_ALL_ANIMATE : QueueAction.EXPAND_ALL;
			return;
		}

		mQueueAction = QueueAction.DO_NOTHING;
		if (hasAutoExpandingGroups()) return;
		for (int index = 0; index < getGroupCount(); ++index) {
			lv.expandGroup(index, animate);
		}
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
	public abstract View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
									  boolean isLastChild,
									  View convertView, ViewGroup parent);

	@Override
	public final View getChildView(int groupPosition, int childPosition, boolean isLastChild,
								   View convertView, ViewGroup parent) {
		View v = getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							  parent);
		updateChildCheckedView(groupPosition, childPosition, v);
		return v;
	}

	/**
	 * @return The current choice mode to be applied to the attached {@link ExpandableListView}
	 */
	public ChoiceMode getChoiceMode() {
		return mChoiceMode;
	}

	/**
	 * Defines the choice behavior for the attached {@link ExpandableListView}. By default, this
	 * adapter does not have any choice behavior ({@link ChoiceMode#NONE}) set. By setting the
	 * choiceMode to {@link ChoiceMode#SINGLE}, the ExpandableListView allows up to one item to be
	 * in an activation state. By setting the choiceMode to {@link ChoiceMode#MULTIPLE}, the
	 * ExpandableListView allows any number of items to be chosen. Any of the MODEL variants will
	 * show a custom CAB when an item is long pressed.
	 * <p/>
	 * Use this method instead of {@link ExpandableListView#setChoiceMode(int)}. This adapter will
	 * take over and emulate the behavior instead.  By setting the behavior to anything but {@link
	 * ChoiceMode#NONE} will have this adapter take ownership of the {@link
	 * ExpandableListView.OnChildClickListener} and {@link ExpandableListView.OnGroupClickListener}
	 * listeners.
	 *
	 * @param choiceMode One of the {@link ChoiceMode} options
	 */
	public void setChoiceMode(ChoiceMode choiceMode) {
		if (mChoiceMode == choiceMode) return;
		mChoiceMode = choiceMode;
		if (mChoiceActionMode != null) {
			mChoiceActionMode.finish();
		} else {
			updateClickListeners(null);
			clearChoices();
		}

		if (mChoiceMode.isDisabled())
			mCheckStates = null;
		else if (mCheckStates == null)
			mCheckStates = new HashMapWrapper();
	}

	/**
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	@Override
	public final View getGroupView(int groupPosition, boolean isExpanded, View convertView,
								   ViewGroup parent) {
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			if (parent instanceof ExpandableListView) {
				lv = (ExpandableListView) parent;
				if (lv.getChoiceMode() != AbsListView.CHOICE_MODE_NONE) {
					throw new RuntimeException(
							"Set choiceMode through attached adapter, not on the ExpandableListView itself");
				}
				mListView = new WeakReference<>(lv);
				updateClickListeners("getGroupView");
				if (mParcelState != null) lv.onRestoreInstanceState(mParcelState);
				doAction();
			} else {
				throw new IllegalStateException(
						"Expecting ExpandableListView when refreshing referenced state. Instead found unsupported " +
						parent.getClass().getSimpleName());
			}
		}
		if (!isExpanded && hasAutoExpandingGroups()) {
			lv.expandGroup(groupPosition);
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
	 * boolean, View, ViewGroup)}.
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
	public abstract View getGroupView(LayoutInflater inflater, int groupPosition,
									  boolean isExpanded, View convertView,
									  ViewGroup parent);

	/**
	 * @return Whether groups are always forced to render expanded. Default is false.
	 */
	public boolean hasAutoExpandingGroups() {
		return false;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	private void init(Context activity) {
		mContext = activity;
		mInflater = LayoutInflater.from(mContext);
		mListView = new WeakReference<>(null);    //We'll obtain reference in getGroupView
		mChoiceMode = ChoiceMode.NONE;
		mQueueAction = QueueAction.DO_NOTHING;
	}

	/**
	 * Returns the checked state of the specified child item position. Will always return false if
	 * choice mode is {@link ChoiceMode#NONE}
	 *
	 * @param groupPosition The position of the group that contains the child.
	 * @param childPosition The position of the child.
	 *
	 * @return The child item's checked state or <code>false</code> if choice mode is disabled.
	 *
	 * @see #setChoiceMode(ChoiceMode)
	 */
	public boolean isChildChecked(int groupPosition, int childPosition) {
		if (mCheckStates == null) return false;
		long key = hasStableIds() ? getChildId(groupPosition, childPosition) : ExpandableListView
				.getPackedPositionForChild(groupPosition, childPosition);
		return mCheckStates.get(key);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * Returns the checked state of the specified group item position. Will always return false if
	 * choice mode is {@link ChoiceMode#NONE}
	 *
	 * @param groupPosition The position of the group item
	 *
	 * @return The group item's checked state or <code>false</code> if choice mode is disabled.
	 *
	 * @see #setChoiceMode(ChoiceMode)
	 */
	public boolean isGroupChecked(int groupPosition) {
		if (mCheckStates == null) return false;
		long key = hasStableIds() ? getGroupId(groupPosition) : ExpandableListView
				.getPackedPositionForGroup(groupPosition);
		return mCheckStates.get(key);
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

	//TODO: Investigate how to handle areAllItemsEnabled

	/**
	 * TODO: Replace
	 */
	public void onRestoreExpandableListViewState(Parcelable state) {
		if (state == null) return;
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			mParcelState = state;
		} else {
			lv.onRestoreInstanceState(mParcelState);
		}
	}

	/**
	 * TODO: Replace
	 */
	public Parcelable onSaveExpandableListViewState() {
		ExpandableListView lv = mListView.get();
		if (lv == null) return null;
		return lv.onSaveInstanceState();
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
		if (mChoiceMode.isDisabled()) return;
		boolean updateViews;
		boolean oldCheck;

		//Update child checked state
		long groupId = getGroupId(groupPosition);
		long childId = getChildId(groupPosition, childPosition);
		long key = hasStableIds() ? childId : ExpandableListView.getPackedPositionForChild(
				groupPosition, childPosition);
		if (mChoiceMode.isSingle()) {
			oldCheck = mCheckStates.get(key);
			mCheckStates.clear();
			mCheckStates.put(key, isChecked);
		} else {
			oldCheck = mCheckStates.put(key, isChecked);
		}
		updateViews = (oldCheck != isChecked);

		//If modal, verify ActionMode and invoke it's listener
		boolean treatAsModal = mChoiceMode.isModal();
		if (treatAsModal) {
			ExpandableListView lv = mListView.get();
			if (lv == null) {
				//In a bad modal state, reset and ignore modal behavior
				treatAsModal = false;
				updateViews = false;
				if (mChoiceActionMode != null) {
					mChoiceActionMode.finish();
				}
			} else {
				//We are activating for the first time, ensure ActionMode is launched
				if (mCheckStates.checkedCount > 0) {
					if (mChoiceActionMode == null) {
						mChoiceActionMode = lv.startActionMode(mModalChoiceModeWrapper);
						updateClickListeners(null);
					}
				}
				//Notify ActionMode if change actually occurred
				if (updateViews)
					mModalChoiceModeWrapper.onChildCheckedStateChanged(mChoiceActionMode,
																	   groupPosition, groupId,
																	   childPosition, childId,
																	   isChecked);
			}
		}

		//Check if all children of a group have the same checked value. If so, ensure group matches
		if (mChoiceMode.isMultiple()) {
			int childrenCount = getChildrenCount(groupPosition);
			boolean expectedGroupState = isChecked;
			for (childPosition = 0; childPosition < childrenCount; ++childPosition) {
				key = hasStableIds() ? getChildId(groupPosition, childPosition) :
						ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
				if (!mCheckStates.get(key).equals(isChecked)) {
					expectedGroupState = false;
					break;
				}
			}

			//Only if group check state is different from what we expect, then update
			key = hasStableIds() ? groupId : ExpandableListView.getPackedPositionForGroup(
					groupPosition);
			if (!mCheckStates.get(key).equals(expectedGroupState)) {
				updateViews = true;
				mCheckStates.put(key, isChecked);
				//Notify ActionMode if change actually occurred
				if (treatAsModal) {
					mModalChoiceModeWrapper.onGroupCheckedStateChanged(mChoiceActionMode,
																	   groupPosition, groupId,
																	   isChecked);
				}
			}
		}

		if (updateViews) updateOnScreenCheckedViews();
	}

	/**
	 * Sets the checked state of the specified group. Will not work if the choice mode is set to
	 * (@link ChoiceMode#NONE}. If choice mode is set to a multiple selector, Eg {@link
	 * ChoiceMode#MULTIPLE} or {@link ChoiceMode#MULTIPLE_MODAL} then all child items will
	 * additionally have their check state updated.
	 *
	 * @param groupPosition The position of the group.
	 * @param isChecked     The new checked state for the child item.
	 */
	@SuppressWarnings("UnnecessaryReturnStatement")
	public void setGroupChecked(int groupPosition, boolean isChecked) {
		if (mChoiceMode.isDisabled()) return;
		boolean updateViews;
		boolean oldCheck;

		//Update group checked state
		long groupId = getGroupId(groupPosition);
		long key = hasStableIds() ? groupId : ExpandableListView.getPackedPositionForGroup(
				groupPosition);
		if (mChoiceMode.isSingle()) {
			oldCheck = mCheckStates.get(key);
			mCheckStates.clear();
			mCheckStates.put(key, isChecked);
		} else {
			oldCheck = mCheckStates.put(key, isChecked);
		}
		updateViews = (oldCheck != isChecked);

		//If modal, verify ActionMode and invoke it's listener
		ExpandableListView lv = mListView.get();
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
				if (mCheckStates.checkedCount > 0) {
					if (mChoiceActionMode == null) {
						mChoiceActionMode = lv.startActionMode(mModalChoiceModeWrapper);
						updateClickListeners(null);
					}
				}
				//Notify ActionMode if a change actually occurred
				if (updateViews)
					mModalChoiceModeWrapper.onGroupCheckedStateChanged(mChoiceActionMode,
																	   groupPosition, groupId,
																	   isChecked);
			}
		}

		//Ensure all children have the same checked state and update accordingly
		if (mChoiceMode.isMultiple()) {
			int childrenCount = getChildrenCount(groupPosition);
			if (treatAsModal) {
				for (int childPosition = 0; childPosition < childrenCount; ++childPosition) {
					long childId = getChildId(groupPosition, childPosition);
					key = hasStableIds() ? childId : ExpandableListView
							.getPackedPositionForChild(groupPosition, childPosition);
					//Only notify ActionMode if there was an actual change
					if (!mCheckStates.put(key, isChecked).equals(isChecked)) {
						updateViews = true;
						mModalChoiceModeWrapper
								.onChildCheckedStateChanged(mChoiceActionMode, groupPosition,
															groupId, childPosition, childId,
															isChecked);
					}
				}
			} else {
				for (int childPosition = 0; childPosition < childrenCount; ++childPosition) {
					key = hasStableIds() ? getChildId(groupPosition, childPosition) :
							ExpandableListView.getPackedPositionForChild(groupPosition,
																		 childPosition);
					if (!mCheckStates.put(key, isChecked).equals(isChecked)) updateViews = true;
				}
			}
		}

		if (updateViews) updateOnScreenCheckedViews();
	}

	/**
	 * Set a {@link com.sawyer.advadapters.widget.RolodexBaseAdapter.ModalChoiceModeListener} that
	 * will manage the lifecycle of the selection {@link ActionMode}. Only used when the choice mode
	 * is set to {@link android.widget.ExpandableListView#CHOICE_MODE_MULTIPLE_MODAL}.
	 *
	 * @param listener Listener that will manage the selection mode
	 */

	public void setMultiChoiceModeListener(ModalChoiceModeListener listener) {
		if (mModalChoiceModeWrapper == null) {
			mModalChoiceModeWrapper = new ModalChoiceModeWrapper();
		}
		mModalChoiceModeWrapper.setWrapped(listener);
	}

	/**
	 * TODO: Write doc once all choice modes have been tested
	 */
	public void setOnChildClickListener(
			ExpandableListView.OnChildClickListener onChildClickListener) {
		mOnChildClickListener = onChildClickListener;
		updateClickListeners(null);
	}

	/**
	 * TODO: Write doc once all choice modes have been tested
	 */
	public void setOnGroupClickListener(
			ExpandableListView.OnGroupClickListener onGroupClickListener) {
		mOnGroupClickListener = onGroupClickListener;
		updateClickListeners(null);
	}

	private void updateChildCheckedView(int groupPosition, int childPosition, View v) {
		if (mCheckStates == null) return;
		long key = hasStableIds() ? getChildId(groupPosition, childPosition) :
				ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
		if (v instanceof Checkable)
			((Checkable) v).setChecked(mCheckStates.get(key));
		else
			v.setActivated(mCheckStates.get(key));
	}

	/**
	 * Helper method which ensures our click listeners are in the correct state.
	 *
	 * @param debugTag Text to output if we unexpectedly loose ExpandableListView referencing. Null
	 *                 if we don't care and nothing will be logged.
	 */
	private void updateClickListeners(String debugTag) {
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			if (!TextUtils.isEmpty(debugTag)) logLostReference(debugTag);
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

	private void updateGroupCheckedView(int groupPosition, View v) {
		if (mCheckStates == null) return;
		long key = hasStableIds() ? getGroupId(groupPosition) :
				ExpandableListView.getPackedPositionForGroup(groupPosition);
		if (v instanceof Checkable)
			((Checkable) v).setChecked(mCheckStates.get(key));
		else
			v.setActivated(mCheckStates.get(key));
	}

	/**
	 * Perform a quick, in-place update of the checked or activated state on all visible item views.
	 * This should only be called when a valid choice mode is active.
	 */
	private void updateOnScreenCheckedViews() {
		ExpandableListView lv = mListView.get();
		if (lv == null) return;
		final int firstPos = lv.getFirstVisiblePosition();
		final int lastPos = lv.getChildCount();

		for (int index = 0; index < lastPos; ++index) {
			View child = lv.getChildAt(index);
			long packedPosition = lv.getExpandableListPosition(firstPos + index);
			long key;
			if (hasStableIds()) {
				switch (ExpandableListView.getPackedPositionType(packedPosition)) {
				case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
					key = getGroupId(ExpandableListView.getPackedPositionGroup(packedPosition));
					break;
				case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
					key = getChildId(ExpandableListView.getPackedPositionGroup(packedPosition),
									 ExpandableListView.getPackedPositionChild(packedPosition));
					break;
				default:
					Log.w(TAG, "updateOnScreenCheckedViews received unknown packed position?");
					continue;
				}
			} else {
				key = packedPosition;
			}

			if (child instanceof Checkable) {
				((Checkable) child).setChecked(mCheckStates.get(key));
			} else {
				child.setActivated(mCheckStates.get(key));
			}
		}
	}

	//TODO: Implement getCheckedItem (from AbsListView)
	//TODO: Implement getCheckedItems (from AbsListView)
	//TODO: Implement getCheckedItemIds (from AbsListView)

	/**
	 * An interface definition for callbacks that receive events for {@link
	 * AbsListView#CHOICE_MODE_MULTIPLE_MODAL}. It acts as the {@link ActionMode.Callback} for the
	 * selection mode and also receives checked state change events when the user selects and
	 * deselects groups or children views.
	 */
	public static interface ModalChoiceModeListener extends ActionMode.Callback {

		/**
		 * Called when a child item is checked or unchecked during selection mode.
		 *
		 * @param mode          The {@link ActionMode} providing the selection mode
		 * @param childPosition Adapter position of the child item that was checked or unchecked
		 * @param childId       Adapter ID of the child item that was checked or unchecked
		 * @param groupPosition Adapter position of the group this child belongs to.
		 * @param groupId       Adapter ID of the group this child belongs to.
		 * @param checked       <code>true</code> if the item is now checked, <code>false</code> if
		 *                      the item is now unchecked.
		 */
		public void onChildCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   int childPosition, long childId, boolean checked);

		/**
		 * Called when a group item is checked or unchecked during selection mode. If the group is
		 * expanded, then all of it's children will have their checked state changed to match and
		 * {@link #onChildCheckedStateChanged(ActionMode, int, long, int, long, boolean)} will be
		 * appropriately invoked for each.
		 *
		 * @param mode          The {@link ActionMode} providing the selection mode
		 * @param groupPosition Adapter position of the group item that was checked or unchecked
		 * @param groupId       Adapter ID of the group item that was checked or unchecked
		 * @param checked       <code>true</code> if the item is now checked, <code>false</code> if
		 *                      the item is now unchecked.
		 */
		public void onGroupCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   boolean checked);
	}

	private static class HashMapWrapper extends HashMap<Long, Boolean> {
		/** Running count of how many items are currently checked. */
		public int checkedCount;

		@Override
		public void clear() {
			super.clear();
			checkedCount = 0;
		}

		@Override
		public Boolean get(Object key) {
			return Boolean.TRUE.equals(super.get(key));    //Never return null
		}

		@Override
		public Boolean put(Long key, Boolean newValue) {
			Boolean oldValue = Boolean.TRUE.equals(super.put(key, newValue)); //Never return null
			if (oldValue && !newValue) //Went from True to False
				--checkedCount;
			else if (!oldValue && newValue)    //Went from False to True
				++checkedCount;
			return oldValue;
		}
	}

	private static class OnDisableTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;    //Do nothing but consume touch event
		}
	}

	/**
	 * Wraps around the {@link AbsListView.MultiChoiceModeListener} and converts it's item checked
	 * change callbacks to group or child checked changed events. In addition, the user defined
	 * group and child click listeners will be bypassed when the CAB appears.
	 */
	private class ModalChoiceModeWrapper implements ModalChoiceModeListener {
		private ModalChoiceModeListener mWrapped;

		public boolean hasWrappedCallback() {
			return mWrapped != null;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return mWrapped.onActionItemClicked(mode, item);
		}

		@Override
		public void onChildCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   int childPosition, long childId, boolean checked) {
			mWrapped.onChildCheckedStateChanged(mode, groupPosition, groupId, childPosition,
												childId, checked);
			if (mCheckStates.checkedCount == 0) mode.finish();
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
			if (mCheckStates.checkedCount != 0) {
				clearChoices();
				updateOnScreenCheckedViews();
			}
		}

		@Override
		public void onGroupCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   boolean checked) {
			mWrapped.onGroupCheckedStateChanged(mode, groupPosition, groupId, checked);
			if (mCheckStates.checkedCount == 0) mode.finish();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return mWrapped.onPrepareActionMode(mode, menu);
		}

		public void setWrapped(ModalChoiceModeListener wrapped) {
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
			long key = hasStableIds() ? id : ExpandableListView
					.getPackedPositionForChild(groupPosition, childPosition);
			setChildChecked(groupPosition, childPosition, !mCheckStates.get(key));
			return true;
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			long key = hasStableIds() ? id : ExpandableListView
					.getPackedPositionForGroup(groupPosition);
			setGroupChecked(groupPosition, !mCheckStates.get(key));
			return true;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (!mChoiceMode.isModal()) return false;
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
