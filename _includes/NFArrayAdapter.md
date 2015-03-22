#### Compared to Android's `ArrayAdapter`:

- **Filtering Removed** So no internal synchronized blocks to slow things down.
- **More List Implementations**
  - `contains()`, `containsAll()`
  - `removeAll()`, `retainAll()`
  - `update()`, `insertAll()`
- **Convenience methods**
  - `getList()`, `setList()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`
- **Slightly smarter** internal `notifyDataSetChanged()` invocations

    <br/>

#### Code Example

        public class MovieAdapter extends NFArrayAdapter<MovieItem> {
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
