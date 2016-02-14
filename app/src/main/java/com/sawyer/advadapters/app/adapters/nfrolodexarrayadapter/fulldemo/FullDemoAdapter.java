/*
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sawyer.advadapters.app.adapters.nfrolodexarrayadapter.fulldemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.MovieNoSubViewHolder;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.NFRolodexArrayAdapter;

import java.util.List;

class FullDemoAdapter extends NFRolodexArrayAdapter<Integer, MovieItem> {

	FullDemoAdapter(Context activity) {
		super(activity);
	}

	FullDemoAdapter(Context activity, List<MovieItem> list) {
		super(activity, list);
	}

	@NonNull
	@Override
	public Integer createGroupFor(MovieItem childItem) {
		return childItem.year;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		/*
		 Returning the childPosition "could" be a stable & unique value to return here in certain
		 situations.  However the safest and recommended approach is to always return a value
		 which remains constant (stable) despite positional changes.

		 Since each MovieItem has it's own unique barcode, utilizing that satisfies the stable
		 and unique requirement. No two children will ever have the same barcode. Eg: no
		 matter where the movie "I, Robot" is displayed...be it the 2nd child Position or 10th or
		 in groupPosition == 1 or groupPosition == 10...it'll always return the same barcode.
		 */
		return getChild(groupPosition, childPosition).barcodeLong();
	}

	@NonNull
	@Override
	public View getChildView(@NonNull LayoutInflater inflater, int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, @NonNull ViewGroup parent) {
		MovieNoSubViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_movie2, parent, false);
			vh = new MovieNoSubViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (MovieNoSubViewHolder) convertView.getTag();
		}

		MovieItem movie = getChild(groupPosition, childPosition);
		vh.title.setText(movie.title);
		vh.icon.setImageResource(
				movie.isRecommended ? R.drawable.ic_rating_good : R.drawable.ic_rating_bad);

		return convertView;
	}

	@Override
	public long getGroupId(int groupPosition) {
		/*
		 Returning groupPosition "could" be a stable & unique value to return here in certain
		 situations. However the safest and recommended approach is to always return a value
		 which remains constant (stable) despite positional changes.

		 Since our groups are storing years, returning the year is stable and unique.  No two
		 groups will ever have the same year displayed. Eg: no matter where the year "2004"
		 is displayed...be it groupPosition == 1 or groupPosition == 10...it'll always return
		 2004.
		 */
		return getGroup(groupPosition);
	}

	@NonNull
	@Override
	public View getGroupView(@NonNull LayoutInflater inflater, int groupPosition,
							 boolean isExpanded, View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_expandable_group2, parent, false);
		}
		TextView tv = (TextView) convertView;
		tv.setText(getGroup(groupPosition).toString());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		/*
		 Any time choice mode is enabled, stable IDs should also be enabled. Otherwise, restoring
		 the activity from saved state may activate the incorrect item in the adapter. Additionally,
		 don't forget to have getGroupId() and getChildId() actually return unique and stable ids.
		 Else it defeats the purpose of enabling this feature.
		 */
		return true;
	}
}