/**
 * Copyright 2014 Jay Soyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sawyer.advadapters.app.adapters.rolodexadapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.MovieNoSubViewHolder;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.RolodexAdapter;

import java.util.List;
import java.util.Locale;

class MovieRolodexAdapter extends RolodexAdapter<Integer, MovieItem> {

	MovieRolodexAdapter(Context activity) {
		super(activity);
	}

	MovieRolodexAdapter(Context activity, List<MovieItem> list) {
		super(activity, list);
	}

	@Override
	public Integer createGroupFor(MovieItem childItem) {
		return childItem.year;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getChild(groupPosition, childPosition).barcodeLong();
	}

	@Override
	public View getChildView(LayoutInflater inflater, int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {
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
		return getGroup(groupPosition);
	}

	@Override
	public View getGroupView(LayoutInflater inflater, int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_expandable_group2, parent, false);
		}
		TextView tv = (TextView) convertView;
		tv.setText(getGroup(groupPosition).toString());

		return convertView;
	}

	@Override
	protected boolean isChildFilteredOut(MovieItem movie, CharSequence constraint) {
		return !TextUtils.isDigitsOnly(constraint) && !movie.title.toLowerCase(Locale.US).contains(
				constraint.toString().toLowerCase(Locale.US));
	}

	@Override
	protected boolean isGroupFilteredOut(Integer year, CharSequence constraint) {
		return TextUtils.isDigitsOnly(constraint) && !year.toString().contains(constraint);
	}
}