package com.sawyer.advadapters.widget;

import android.content.Context;
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
	private final OnChoiceModeTouchListener mChoiceModeTouchListener = new OnChoiceModeTouchListener();
	/** Controls if/how the user may choose/check items in the list */
	int mChoiceMode;
	boolean mIsChoiceModeActive;
	WeakReference<ExpandableListView> mListView;
	ExpandableListView.OnGroupClickListener mOnGroupClickListener;
	ExpandableListView.OnChildClickListener mOnChildClickListener;
	MultiChoiceModeListener mMultiChoiceModeListener;

	/** LayoutInflater created from the constructing context */
	private LayoutInflater mInflater;
	/** Activity Context used to construct this adapter * */
	private Context mContext;

	BaseRolodexAdapter(ExpandableListView listView) {
		init(listView);
	}

	static <G, C> ArrayList<C> toArrayList(Map<G, ArrayList<C>> map) {
		ArrayList<C> joinedList = new ArrayList<>();
		for (Map.Entry<G, ArrayList<C>> entry : map.entrySet()) {
			joinedList.addAll(entry.getValue());
		}
		return joinedList;
	}

	/**
	 * Collapse all groups in the adapter.
	 *
	 * @return False if adapter failed to attempt collapsing. Otherwise True.
	 */
	public boolean collapseAll() {
		ExpandableListView lv = mListView.get();
		if (lv == null) return false;

		for (int index = 0; index < getGroupCount(); ++index) {
			lv.collapseGroup(index);
		}
		return true;
	}

	/**
	 * Expand all groups in the adapter.
	 *
	 * @param animate True if the expanding groups should be animated in
	 *
	 * @return False if adapter failed to attempt an expansion. Otherwise True.
	 */
	public boolean expandAll(boolean animate) {
		ExpandableListView lv = mListView.get();
		if (lv == null) return false;

		for (int index = 0; index < getGroupCount(); ++index) {
			lv.expandGroup(index, animate);
		}
		return true;
	}

	public abstract View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
									  boolean isLastChild,
									  View convertView, ViewGroup parent);

	@Override
	public final View getChildView(int groupPosition, int childPosition, boolean isLastChild,
								   View convertView, ViewGroup parent) {
		View v = getChildView(mInflater, groupPosition, childPosition, isLastChild, convertView,
							  parent);
		if (mChoiceMode != ExpandableListView.CHOICE_MODE_NONE) {
			v.setOnTouchListener(mChoiceModeTouchListener);
		}
		return v;
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
				initChoiceMode(lv);
			} else {
				//TODO: Consider supporting custom implementations of ListView or AbsListView
			}
		}

		if (!isExpanded && hasAutoExpandingGroups()) {
			lv.expandGroup(groupPosition);
		}

		View v = getGroupView(mInflater, groupPosition, isExpanded, convertView, parent);
		if (!isGroupSelectable(groupPosition)) {
			v.setOnTouchListener(mDisableTouchListener);
		} else {
			if (mChoiceMode != ExpandableListView.CHOICE_MODE_NONE) {
				v.setOnTouchListener(mChoiceModeTouchListener);
			}
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
		return true;
	}

	private void init(ExpandableListView lv) {
		mContext = lv.getContext();
		mInflater = LayoutInflater.from(mContext);
		mListView = new WeakReference<>(lv);
		mChoiceMode = ExpandableListView.CHOICE_MODE_NONE;
		mIsChoiceModeActive = false;
		initChoiceMode(lv);
	}

	private void initChoiceMode(ExpandableListView lv) {
		if (mChoiceMode != lv.getChoiceMode()) {
			lv.setChoiceMode(mChoiceMode);
			if (mChoiceMode == CHOICE_MODE_NONE) {
				//TODO: Test setting this while mIsChoiceModeActive is true
				mIsChoiceModeActive = false;
				lv.setMultiChoiceModeListener(null);
			} else {
				mIsChoiceModeActive = false;
				lv.setMultiChoiceModeListener(new InternalMultiChoiceModeListener());
			}
		}
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
	 * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
	 * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
	 * List allows up to one item to  be in a chosen state. By setting the choiceMode to {@link
	 * #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
	 * <p/>
	 * Use this method instead of {@link AbsListView#setChoiceMode(int)}. By setting to the behavior
	 * to anything but {@link #CHOICE_MODE_NONE} will have this adapter take owner ship of the
	 * {@link ExpandableListView.OnChildClickListener} and {@link ExpandableListView.OnGroupClickListener}
	 * listeners.
	 *
	 * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or {@link
	 *                   #CHOICE_MODE_MULTIPLE}
	 */
	public void setChoiceMode(int choiceMode) {
		mChoiceMode = choiceMode;
		ExpandableListView lv = mListView.get();
		if (lv != null) initChoiceMode(lv);
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
	}

	public void setOnGroupClickListener(
			ExpandableListView.OnGroupClickListener onGroupClickListener) {
		mOnGroupClickListener = onGroupClickListener;
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
			return mIsChoiceModeActive;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (mMultiChoiceModeListener != null)
				mMultiChoiceModeListener.onDestroyActionMode(mode);
			mIsChoiceModeActive = false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
											  boolean checked) {
			ExpandableListView lv = mListView.get();
			if (lv == null) {
				Log.w(TAG, "Lost ExpandableListView reference in onItemCheckedStateChanged");
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

	private class OnChoiceModeTouchListener implements View.OnTouchListener {
		private OnChoiceModeClickListener mListener = new OnChoiceModeClickListener();

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ExpandableListView lv = mListView.get();
			if (lv == null) {
				Log.w(TAG, "Lost ExpandableListView reference in OnChoiceModeTouchListener");
				return false;
			}

			if (mIsChoiceModeActive) {
				lv.setOnChildClickListener(mListener);
				lv.setOnGroupClickListener(mListener);
			} else {
				lv.setOnChildClickListener(mOnChildClickListener);
				lv.setOnGroupClickListener(mOnGroupClickListener);
			}
			return false;
		}
	}
}
