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
package com.sawyer.advadapters.app.adapters.arraybaseadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.ArrayBaseAdapter;

import java.util.List;
import java.util.Locale;

class MovieArrayBaseAdapter extends ArrayBaseAdapter<MovieItem> {

	MovieArrayBaseAdapter(Context activity) {
		super(activity);
	}

	MovieArrayBaseAdapter(Context activity, List<MovieItem> list) {
		super(activity, list);
	}

	@Override
	public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_movie1, parent, false);
			vh = new ViewHolder();
			vh.title = (TextView) convertView.findViewById(R.id.title);
			vh.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
			vh.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		MovieItem movie = getItem(position);
		vh.title.setText(movie.title);
		vh.subtitle.setText(String.valueOf(movie.year));
		vh.icon.setImageResource(
				movie.isRecommended ? R.drawable.ic_rating_good : R.drawable.ic_rating_bad);

		return convertView;
	}

	@Override
	protected boolean isFilteredBy(MovieItem movie, CharSequence constraint) {
		return movie.title.toLowerCase(Locale.US)
						  .contains(constraint.toString().toLowerCase(Locale.US));
	}

	private static class ViewHolder {
		TextView title;
		TextView subtitle;
		ImageView icon;
	}
}