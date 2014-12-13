package com.sawyer.advadapters.widget;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

public abstract class BaseRolodexAdapter extends BaseExpandableListAdapter {
	public static final int CHOICE_MODE_NONE = AbsListView.CHOICE_MODE_NONE;
	public static final int CHOICE_MODE_SINGLE = AbsListView.CHOICE_MODE_SINGLE;
	public static final int CHOICE_MODE_MULTIPLE = AbsListView.CHOICE_MODE_MULTIPLE;
	public static final int CHOICE_MODE_MULTIPLE_MODAL = AbsListView.CHOICE_MODE_MULTIPLE_MODAL;

	private static final String TAG = "BaseRolodexAdapter";
	private final OnDisableTouchListener mDisableTouchListener = new OnDisableTouchListener();
	private final OnChoiceModeClickListener mChoiceModeClickListener = new OnChoiceModeClickListener();

	/** Controls if/how the user may choose/check items in the list */
	int mChoiceMode;
	/** Flag indicating if the ActionMode CAB is active and being displayed */
	boolean mIsChoiceModeActive;
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

	BaseRolodexAdapter(Context activity) {
		init(activity);
	}

	/** Convenience method to log when we unexpectedly lost our ExpandableListView reference. */
	private static void logLostReference(String method) {
		Log.w(TAG, "Lost reference to ExpandableListView in " + method);
	}

	static <G, C> ArrayList<C> toArrayList(Map<G, ArrayList<C>> map) {
		ArrayList<C> joinedList = new ArrayList<>();
		for (Map.Entry<G, ArrayList<C>> entry : map.entrySet()) {
			joinedList.addAll(entry.getValue());
		}
		return joinedList;
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

	public abstract View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
									  boolean isLastChild,
									  View convertView, ViewGroup parent);

	@Override
	public final View getChildView(int groupPosition, int childPosition, boolean isLastChild,
								   View convertView, ViewGroup parent) {
		return getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							parent);
	}

	public int getChoiceMode() {
		return mChoiceMode;
	}

	/**
	 * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
	 * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
	 * List allows up to one item to  be in a chosen state. By setting the choiceMode to {@link
	 * #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
	 * <p/>
	 * Use this method instead of {@link AbsListView#setChoiceMode(int)}. By setting the behavior to
	 * anything but {@link #CHOICE_MODE_NONE} will have this adapter take ownership of the {@link
	 * ExpandableListView.OnChildClickListener} and {@link ExpandableListView.OnGroupClickListener}
	 * listeners.
	 *
	 * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or {@link
	 *                   #CHOICE_MODE_MULTIPLE}
	 */
	public void setChoiceMode(int choiceMode) {
		if (mChoiceMode == choiceMode) return;
		mChoiceMode = choiceMode;
		final ExpandableListView lv = mListView.get();
		if (lv == null) return;
		lv.setOnGroupClickListener(mOnGroupClickListener);
		lv.setOnChildClickListener(mOnChildClickListener);
		lv.setChoiceMode(choiceMode);
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
				lv.setOnGroupClickListener(mOnGroupClickListener);
				lv.setOnChildClickListener(mOnChildClickListener);
				lv.setChoiceMode(mChoiceMode);
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
		return false;
	}

	private void init(Context activity) {
		mContext = activity;
		mInflater = LayoutInflater.from(mContext);
		mListView = new WeakReference<>(null);
		mChoiceMode = CHOICE_MODE_NONE;
		mIsChoiceModeActive = false;
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

	public void onRestoreExpandableListViewState(Parcelable state) {
		if (state == null) return;
		ExpandableListView lv = mListView.get();
		if (lv == null) {
			mParcelState = state;
		} else {
			lv.onRestoreInstanceState(mParcelState);
		}
	}

	public Parcelable onSaveExpandableListViewState() {
		ExpandableListView lv = mListView.get();
		if (lv == null) return null;
		return lv.onSaveInstanceState();
	}

	/**
	 * Set a {@link MultiChoiceModeListener} that will manage the lifecycle of the selection {@link
	 * ActionMode}. Only used when the choice mode is set to {@link #CHOICE_MODE_MULTIPLE_MODAL}.
	 *
	 * @param listener Listener that will manage the selection mode
	 *
	 * @see #setChoiceMode(int)
	 */
	public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
		mMultiChoiceModeListener = listener;
	}

	public void setOnChildClickListener(
			ExpandableListView.OnChildClickListener onChildClickListener) {
		mOnChildClickListener = onChildClickListener;
		if (!mIsChoiceModeActive) {
			ExpandableListView lv = mListView.get();
			if (lv != null) lv.setOnChildClickListener(mOnChildClickListener);
		}
	}

	public void setOnGroupClickListener(
			ExpandableListView.OnGroupClickListener onGroupClickListener) {
		mOnGroupClickListener = onGroupClickListener;
		ExpandableListView lv = mListView.get();
		if (!mIsChoiceModeActive) {
			if (lv != null) lv.setOnGroupClickListener(mOnGroupClickListener);
		}
	}

	public static interface MultiChoiceModeListener {
		public boolean onActionItemClicked(ActionMode mode, MenuItem item);

		public void onChildCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   int childPosition, long childId, boolean checked);

		public boolean onCreateActionMode(ActionMode mode, Menu menu);

		public void onDestroyActionMode(ActionMode mode);

		public void onGroupCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   boolean checked);

		public boolean onPrepareActionMode(ActionMode mode, Menu menu);
	}

	private static class OnDisableTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;    //Do nothing but consume touch event
		}
	}

	/**
	 * Wraps around the {@link AbsListView.MultiChoiceModeListener} to provide the missing
	 * functionality to support choice mode with an {@link ExpandableListView}. This includes
	 * delegating the {@link #onItemCheckedStateChanged} to either a child or group event handler.
	 * It also handles switching out the group and child click listeners when the CAB (dis)appears.
	 */
	private class InternalMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return mMultiChoiceModeListener != null &&
				   mMultiChoiceModeListener.onActionItemClicked(mode, item);
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mIsChoiceModeActive = mMultiChoiceModeListener != null &&
								  mMultiChoiceModeListener.onCreateActionMode(mode, menu);
			if (mIsChoiceModeActive) {
				ExpandableListView lv = mListView.get();
				if (lv == null) {
					logLostReference("onCreateActionMode");
					mIsChoiceModeActive = false;
				} else {
					lv.setOnChildClickListener(mChoiceModeClickListener);
					lv.setOnGroupClickListener(mChoiceModeClickListener);
				}
			}
			return mIsChoiceModeActive;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (mMultiChoiceModeListener != null)
				mMultiChoiceModeListener.onDestroyActionMode(mode);
			mIsChoiceModeActive = false;
			ExpandableListView lv = mListView.get();
			if (lv == null) {
				logLostReference("onDestroyActionMode");
			} else {
				lv.setOnChildClickListener(mOnChildClickListener);
				lv.setOnGroupClickListener(mOnGroupClickListener);
			}
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

	private class OnChoiceModeClickListener implements ExpandableListView.OnChildClickListener,
			ExpandableListView.OnGroupClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
									int childPosition, long id) {
			if (mIsChoiceModeActive) {
				long packedPosition = ExpandableListView
						.getPackedPositionForChild(groupPosition, childPosition);
				int position = parent.getFlatListPosition(packedPosition);
				parent.setItemChecked(position, !parent.isItemChecked(position));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			if (mIsChoiceModeActive) {
				long packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
				int position = parent.getFlatListPosition(packedPosition);
				parent.setItemChecked(position, !parent.isItemChecked(position));
				return true;
			} else {
				return false;
			}
		}
	}
}
