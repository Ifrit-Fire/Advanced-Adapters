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
package com.sawyer.advadapters.app.adapters.androidarrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;

import java.util.List;

class MovieAdapter extends ArrayAdapter<MovieItem> {
	private LayoutInflater mInflater;

	MovieAdapter(Context activity) {
		super(activity, R.layout.item_movie1);
		init();
	}

	MovieAdapter(Context activity, List<MovieItem> list) {
		super(activity, R.layout.item_movie1, list);
		init();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_movie1, parent, false);
			convertView.setTag(R.id.title, convertView.findViewById(R.id.title));
			convertView.setTag(R.id.subtitle, convertView.findViewById(R.id.subtitle));
			convertView.setTag(R.id.icon, convertView.findViewById(R.id.icon));
		}

		MovieItem movie = getItem(position);
		TextView title = (TextView) convertView.getTag(R.id.title);
		title.setText(movie.title);

		TextView subtitle = (TextView) convertView.getTag(R.id.subtitle);
		subtitle.setText(String.valueOf(movie.year));

		ImageView icon = (ImageView) convertView.getTag(R.id.icon);
		icon.setImageResource(
				(movie.isRecommended) ? R.drawable.ic_rating_good : R.drawable.ic_rating_bad);

		return convertView;
	}

	private void init() {
		mInflater = LayoutInflater.from(getContext());
	}
}