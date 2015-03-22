#### Compared to Android's `ArrayAdapter`:

- **Filtering Logic** You provide the how, adapter takes care of the rest.
- **Active Filtering** Mutating adapter will re-filters data on the fly.
- **Resolves Bugs** [9666][1], [69179][2]
- **More List Implementations**
  - `contains()`, `containsAll()`
  - `removeAll()`, `retainAll()`
  - `update()`
- **Convenience methods**
  - `getFilteredList()`
  - `getList()`, `setList()`
- **LayoutInflater** Passed down to both `getView()` and `getDropDownView()`
- **Slightly smarter** internal `notifyDataSetChanged()` invocations

    <br/>

#### Code Example

        public class MovieAdapter extends AbsArrayAdapter<MovieItem> {
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
            protected boolean isFilteredOut(MovieItem movie, CharSequence constraint) {
                return !movie.title.toLowerCase(Locale.US).contains(
                        constraint.toString().toLowerCase(Locale.US));
            }
        }


[1]: https://code.google.com/p/android/issues/detail?id=9666
[2]: https://code.google.com/p/android/issues/detail?id=69179
