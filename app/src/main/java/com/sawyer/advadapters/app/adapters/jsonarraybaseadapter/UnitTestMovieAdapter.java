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
package com.sawyer.advadapters.app.adapters.jsonarraybaseadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.JSONArrayBaseAdapter;

import org.json.JSONArray;

import java.util.Locale;

class UnitTestMovieAdapter extends JSONArrayBaseAdapter {

	UnitTestMovieAdapter(Context activity) {
		super(activity);
	}

	UnitTestMovieAdapter(Context activity, JSONArray list) {
		super(activity, list);
	}

	@Override
	public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
			vh = new ViewHolder();
			vh.title = (TextView) convertView.findViewById(android.R.id.text1);
			vh.subtitle = (TextView) convertView.findViewById(android.R.id.text2);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Object item = getItem(position);
		vh.title.setText(item.getClass().getSimpleName());
		vh.subtitle.setText(item.toString());

		return convertView;
	}

	@Override
	protected boolean isFilteredBy(Object item, CharSequence constraint) {
		return item.toString().toLowerCase(Locale.US)
				   .contains(constraint.toString().toLowerCase(Locale.US));
	}

	@Override
	protected boolean isFilteredBy(Long item, CharSequence constraint) {
		return String.valueOf(item).toLowerCase(Locale.US)
					 .contains(constraint.toString().toLowerCase(Locale.US));
	}

	boolean isFilteredBy(MovieItem movie, CharSequence constraint) {
		return movie.title.toLowerCase(Locale.US)
						  .contains(constraint.toString().toLowerCase(Locale.US));
	}

	private static class ViewHolder {
		TextView title;
		TextView subtitle;
	}
}