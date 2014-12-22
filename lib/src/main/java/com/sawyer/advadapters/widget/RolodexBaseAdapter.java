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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.lang.ref.WeakReference;

/**
 * TODO: Write this
 */
public abstract class RolodexBaseAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "RolodexBaseAdapter";

	private final OnDisableTouchListener mDisableTouchListener = new OnDisableTouchListener();
	private final OnChoiceModeClickListener mChoiceModeClickListener = new OnChoiceModeClickListener();

	/** Flag indicating if the ActionMode CAB is active and being displayed */
	boolean mIsActionModeActive;
	/** {@link WeakReference} to {@link ExpandableListView} which the adapter is currently attached. */
	WeakReference<ExpandableListView> mListView;
	/** User defined callback to be invoked when a group view has been clicked. */
	ExpandableListView.OnGroupClickListener mOnGroupClickListener;
	/** User defined callback to be invoked when a child view has been clicked. */
	ExpandableListView.OnChildClickListener mOnChildClickListener;
	/** TODO: Write this */
	MultiChoiceModeListener mMultiChoiceModeListener;
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
		return getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							parent);
	}

	/**
	 * @return The current choice mode of the attached {@link ExpandableListView}. This may return
	 * null if the adapter has no reference to it's ExpandableListView.
	 */
	public Integer getChoiceMode() {
		ExpandableListView lv = mListView.get();
		if (lv != null)
			return lv.getChoiceMode();
		else
			return null;
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
				mListView = new WeakReference<>(lv);
				updateClickListeners("getGroupView");
				lv.setMultiChoiceModeListener(new InternalMultiChoiceModeListener());
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
		mIsActionModeActive = false;
		mQueueAction = QueueAction.DO_NOTHING;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
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
	 * TODO: Write doc once all the choice modes are tested
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
	 * TODO: Write doc once all the choice modes are tested
	 */
	public Parcelable onSaveExpandableListViewState() {
		ExpandableListView lv = mListView.get();
		if (lv == null) return null;
		return lv.onSaveInstanceState();
	}

	/**
	 * Set a {@link MultiChoiceModeListener} that will manage the lifecycle of the selection {@link
	 * ActionMode}. Only used when the choice mode is set to {@link android.widget.ExpandableListView#CHOICE_MODE_MULTIPLE_MODAL}.
	 *
	 * @param listener Listener that will manage the selection mode
	 */
	public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
		mMultiChoiceModeListener = listener;
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

	private void updateClickListeners(String debugTag) {
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			if (!TextUtils.isEmpty(debugTag)) logLostReference(debugTag);
			mIsActionModeActive = false;
			return;
		}
		switch (lv.getChoiceMode()) {
		case ExpandableListView.CHOICE_MODE_SINGLE:
		case ExpandableListView.CHOICE_MODE_MULTIPLE:
			lv.setOnChildClickListener(mChoiceModeClickListener);
			lv.setOnGroupClickListener(mChoiceModeClickListener);
			break;
		case ExpandableListView.CHOICE_MODE_MULTIPLE_MODAL:
			if (mIsActionModeActive) {
				lv.setOnChildClickListener(mChoiceModeClickListener);
				lv.setOnGroupClickListener(mChoiceModeClickListener);
			} else {
				lv.setOnGroupClickListener(mOnGroupClickListener);
				lv.setOnChildClickListener(mOnChildClickListener);
			}
			break;
		case ExpandableListView.CHOICE_MODE_NONE:
		default:
			lv.setOnGroupClickListener(mOnGroupClickListener);
			lv.setOnChildClickListener(mOnChildClickListener);
			break;
		}
	}

	/**
	 * An interface definition for callbacks that receive events for {@link
	 * ExpandableListView#CHOICE_MODE_MULTIPLE_MODAL}. It acts as the {@link ActionMode.Callback}
	 * for the selection mode and also receives checked state change events when the user selects
	 * and deselects groups or children views.
	 */
	public static interface MultiChoiceModeListener extends ActionMode.Callback {

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

	private static class OnDisableTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;    //Do nothing but consume touch event
		}
	}

	/**
	 * Wraps around the {@link ExpandableListView.MultiChoiceModeListener} and converts it's item
	 * checked change callbacks to group or child checked changed events. In addition, the user
	 * defined group and child click listeners will be bypassed when the CAB appears.
	 */
	private class InternalMultiChoiceModeListener implements
			ExpandableListView.MultiChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return mMultiChoiceModeListener != null &&
				   mMultiChoiceModeListener.onActionItemClicked(mode, item);
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mIsActionModeActive = mMultiChoiceModeListener != null &&
								  mMultiChoiceModeListener.onCreateActionMode(mode, menu);
			updateClickListeners("onCreateActionMode");
			return mIsActionModeActive;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (mMultiChoiceModeListener != null)
				mMultiChoiceModeListener.onDestroyActionMode(mode);
			mIsActionModeActive = false;
			updateClickListeners("onDestroyActionMode");
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
			ExpandableListView lv = mListView.get();
			if (lv == null) {
				logLostReference("onItemCheckedStateChanged");
				return;
			}
			long packedPosition = lv.getExpandableListPosition(position);
			switch (ExpandableListView.getPackedPositionType(packedPosition)) {
			case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
				if (mMultiChoiceModeListener != null) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
					long groupId = getGroupId(groupPosition);
					mMultiChoiceModeListener
							.onGroupCheckedStateChanged(mode, groupPosition, groupId, checked);

					//Gotta check expansion first, else we'll crash if changing checked state for a non-visible child
					if (lv.isGroupExpanded(groupPosition)) {
						int childCount = getChildrenCount(groupPosition);
						for (int index = 0; index < childCount; ++index) {
							packedPosition = ExpandableListView.getPackedPositionForChild(
									groupPosition, index);
							int flatPosition = lv.getFlatListPosition(packedPosition);
							lv.setItemChecked(flatPosition, checked);
						}
					}
				}
				break;

			case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
				if (mMultiChoiceModeListener != null) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
					int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
					long groupId = getGroupId(groupPosition);
					long childId = getChildId(groupPosition, childPosition);
					mMultiChoiceModeListener
							.onChildCheckedStateChanged(mode, groupPosition, groupId, childPosition,
														childId, checked);
				}
				break;
			default:
				Log.w(TAG, "onItemCheckedStateChanged received unknown packed position?");
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return mMultiChoiceModeListener != null &&
				   mMultiChoiceModeListener.onPrepareActionMode(mode, menu);
		}
	}

	/**
	 * Special click listener attached to group and child items when the ActionMode starts and
	 * removed when it's destroyed. Handles activating the items that were clicked by the user
	 * during this period.
	 */
	private class OnChoiceModeClickListener implements ExpandableListView.OnChildClickListener,
			ExpandableListView.OnGroupClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
									int childPosition, long id) {
			long packedPosition = ExpandableListView
					.getPackedPositionForChild(groupPosition, childPosition);
			int position = parent.getFlatListPosition(packedPosition);
			parent.setItemChecked(position, !parent.isItemChecked(position));
			return true;
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			long packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
			int position = parent.getFlatListPosition(packedPosition);
			parent.setItemChecked(position, !parent.isItemChecked(position));
			return true;
		}
	}
}
