#### Perks:

- **SparseArray Backed** Based on and designed to work only with Android's `SparseArray`
- **SparseArray Implementation** Unlike a typical array based adapter, all interactions with this adapter are akin to working with a `SparseArray`.
- **Filtering Removed** So no internal synchronized blocks to slow things down.
- **Convenience methods**
  - `getFilteredSparseArray()`
  - `getSparseArray()`, `setSparseArray()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`

    <br/>

#### Code Example

        public class MovieAdapter extends NFSparseAdapter<MovieItem> {
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
        }
