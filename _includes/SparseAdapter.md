#### Perks:

- **SparseArray Backed** Based on and designed to work only with Android's `SparseArray`
- **SparseArray Implementation** Unlike a typical array based adapter, all interactions with this adapter are akin to working with a `SparseArray`.
- **Filtering Logic** You provide the how, adapter takes care of the rest.
- **Active Filtering** Mutating adapter will re-filter data on the fly.
- **Convenience methods**
  - `getFilteredSparseArray()`
  - `getSparseArray()`, `setSparseArray()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`

    <br/>

#### Code Example

        public class MovieAdapter extends SparseAdapter<MovieItem> {
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
            protected boolean isFilteredOut(int keyId, MovieItem item, CharSequence constraint) {
                return !item.title.toLowerCase(Locale.US)
                .contains(constraint.toString().toLowerCase(Locale.US));
            }
        }
