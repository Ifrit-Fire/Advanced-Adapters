<p align="center"><img src="https://raw.githubusercontent.com/JaySoyer/Advanced-Adapters/master/app/src/main/logo.png" width="256px" height="256px"/></p>
# Advanced-Adapters
Advanced-Adapters is a suite of adapters to designed to provide better alternaties to Android's ArrayAdapter. For some time now, their adapter has been plagued with filtering issues, which was the primary drive to starting this opensource project. The following list are some of the advantages to using this suite:
- Additional methods for supporting more ArrayList APIs.
- Slightly smarter use of notifyDataSetChanged().
- LayoutInflater passed to all view creation methods.
- Filtering behaves correctly.
- No conflicting List instances during adapter construction

Currently, this suite provides two different adapters.  More will be coming in time. Head on over to the wiki *(coming soon)* to read more about them and learn how to use them.
- ArrayBaseAdapter
- SimpleArrayBaseAdapter

### Supports
- Ice Cream Sandwich
- Jelly Bean
- Kit Kat

There is no plan to specifically support Gingerbread at this time.  However, I don't believe there's any API specific calls being used in the adapter's themselves that aren't found on Gingerbread.  Meaning...it may work fine. Seeing that I lack a Gingerbread devices, maybe someone can test that for me?

### Example
Of course for those just skimming thru, here's a quick example of using the ArrayBaseAdapter.  For now, every adapter is just an abstract class that requires subclassing. This may or may not change in the future.  It's more or less just like using Android's ArrayAdapter.
```java
public class MovieAdapter extends ArrayBaseAdapter<MovieItem> {
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
	public boolean isFilteredBy(MovieItem movie, CharSequence constraint) {
		return movie.title.toLowerCase(Locale.US).contains(constraint.toString().toLowerCase(Locale.US));
	}
}
```
Thats it!

### Adding To Your Project
All adapters are found within the `lib` directory.  Each adapter is a standalone class file which you can copy and paste into your eclipse project.  If using Android Studio, you can import the module directly into your app instead.

Everything found within `app` is the source code for the Google Play store demo app *(coming soon)*. You can also use it for seeing how to use the adapters.
	
	
### License

    Copyright 2014 Jay Soyer

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
