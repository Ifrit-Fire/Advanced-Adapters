#### Perks:

- **JSONArray Backed** Based on and designed to work only with Android's `JSONArray`
- **JSONArray Implementation** Unlike a typical array based adapter, all interactions with this adapter are akin to working with a `JSONArray`.
- **Filtering Logic** You provide the how, adapter takes care of the rest.
- **Manage Type Filtering** Storing multiple types of objects? No problem, you can create a specific filtering logic for each data type.  Even for your own custom classes.
- **Built-in Filters** Default filtering logic supplied for `Boolean`, `Double`, `Integer`, `Long` and `String` data types. Can optionally override these with your own behavior.
- **Active Filtering** Mutating adapter will re-filter data on the fly.
- **Convenience methods**
  - `getFilteredJSONArray()`
  - `getJSONArray()`, `setJSONArray()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`

    <br/>

#### Code Example

        public class MovieAdapter extends JSONAdapter<MovieItem> {
            MovieAdapter(Context activity) {
                super(activity);
            }

            @Override
            public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    //Inflate your view
                }

                //Fill your view with data
                return convertView;
            }

            //Easily customize your filtered results here.  Too easy!
            @Override
            protected boolean isFilteredOut(Object item, CharSequence constraint) {
                return !item.toString().toLowerCase(Locale.US).
                        contains(constraint.toString().toLowerCase(Locale.US));
            }
        }
