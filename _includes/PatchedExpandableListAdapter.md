#### Perks:

- **ExpandableListView** For use with only.
- **Replacement Foundation** Want to create your own custom adapter? Start by using this guy instead of `BaseExpandableListAdapter`
- **Choice Mode** Fixes and provides choice mode support for an `ExpandableListView`
- **Stop Group Collapse** Wouldn't it be nice to force an `ExpandableListView` to never collapse a group? Now possible.
- **Is Group Selectable** `BaseExpandableLisAdapter` provides an `isChildSelectable()` method but forgot about an `isGroupSelectable()`.  Now longer forgotten.
- **Convenience methods** To name just a few:
  - `collapseAll()`, `expandAll()`
  - `hasAutoExpandingGroups()`, `isGroupSelectable()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`

    <br/>

#### Code Examples

Already have a custom adapter extending `BaseExpandableListAdapter`? No sweat. Just switch out with the `PatchedExpandableListAdapter` and you're done.

    class MyCustomAdapter extends PatchedExpandableListAdapter {
        //No further work required to get working.  Just implement your custom adapter
        //as normal. However there are a couple methods available to override for
        //certain behavior changes.

        //Override and return true to force all groups to always render expanded
        @Override
        public boolean hasAutoExpandingGroups() {
            return true;
        }

        //Override and return true to toggle whether a group is clickable or not.
        @Override
        public boolean isGroupSelectable(int groupPosition) {
            return true;
        }
    }

To enable and use choice mode, there are a few key things to remember. All interactions relating to choice mode must go through the adapter instead of the `ExpandableListView`.  Here are some code examples to get you started.

    //If you need to set your own group or child click listeners, do so through the
    //adapter.
    mAdapter.setOnChildClickListener(new MyChildClickListener());
    mAdapter.setOnGroupClickListener(new MyGroupClickListener());

    //To enable choice mode, go through the adapter.
    mAdapter.setChoiceMode(PatchedExpandableListAdapter.ChoiceMode.MULITPLE);

    //If enabling one of the modal choice modes, don't forget to set the
    //ChoiceModeListener
    mAdapter.setMultiChoiceModeListener(new MyChoiceModeListener());

    private class MyChoiceModeListener implements
			PatchedExpandableListAdapter.ChoiceModeListener {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		}

		@Override
		public void onChildCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   int childPosition, long childId, boolean checked) {
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onGroupCheckedStateChanged(ActionMode mode, int groupPosition, long groupId,
											   boolean checked) {
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		}
	}

When choide mode is turned on, if you need to access any of the data revolving around which items are selected and so forth, do so through the adapter. Those returned via the `ExpandableListView` will not be valid.

        //Example list of available functions
        mAdapter.getCheckedChildCount();
        mAdapter.getCheckedChildIds();
        mAdapter.getCheckedChildPositions();
        mAdapter.getCheckedGroupCount();
        mAdapter.getCheckedGroupIds();
        mAdapter.getCheckedGroupPositions();

There are a bunch more available methods when using choice mode.  Just remember to work directly through the adapter instead of the `ExpandableListView`
