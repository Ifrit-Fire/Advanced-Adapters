#### Perks:

- **Section Adapter** Easily create sections for your data without manually organizing it yourself.
- **ExpandableListView** For use with only.
- **ArrayList & Map Backing** Designed to work only with Android's `ArrayList`. Data is organized in a `Map` of `ArrayLists`.
- **Uses PatchedExpandableListAdapter** Which fixes broken `ExpandableListView` features and provides additional conveniences. Learn more here.
- **Organizing Data** You provide the how, adapter takes care of the rest.
- **Filtering Logic** You provide the how, adapter takes care of the rest. Sound familiar?
- **Active Filtering** Mutating adapter will re-filter data on the fly.
- **Convenience methods**
  - `getFilteredList()`, `getList()`, `setList()`
  - `sortGroup()`, `sortAllChildren()`
  - `collapseAll()`, `expandAll()`, `hasAutoExpandingGroups()`, `isGroupSelectable()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`

    <br/>

#### Code Example

    class MovieAdapter extends RolodexArrayAdapter<Integer, MovieItem> {

        MovieAdapter(Context activity) {
            super(activity);
        }

        @Override
        public Integer createGroupFor(MovieItem childItem) {
            return childItem.year;
        }

        @Override
        public View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                //Inflate your view
            }

            //Fill your view with data
            return convertView;
        }

        @Override
        public View getGroupView(LayoutInflater inflater, int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                //Inflate your view
            }

            //Fill your view with data
            return convertView;
        }

        @Override
        protected boolean isChildFilteredOut(MovieItem movie, CharSequence constraint) {
            //Make sure we aren't checking against a constraint containing a movie year, then filter by movie title
            return !TextUtils.isDigitsOnly(constraint) && !movie.title.toLowerCase(Locale.US).contains(
                    constraint.toString().toLowerCase(Locale.US));
        }

        @Override
        protected boolean isGroupFilteredOut(Integer year, CharSequence constraint) {
            //Lets filter out everything whose year does not the numeric values in constraint.
            return TextUtils.isDigitsOnly(constraint) && !year.toString().contains(constraint);
        }
    }
