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
package com.sawyer.advadapters.app.adapters.jsonadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sawyer.advadapters.app.adapters.UnitTestViewHolder;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.JSONAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

class UnitTestMovieAdapter extends JSONAdapter {

	UnitTestMovieAdapter(Context activity) {
		super(activity);
	}

	UnitTestMovieAdapter(Context activity, JSONArray list) {
		super(activity, list);
	}

	@Override
	public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
		UnitTestViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
			vh = new UnitTestViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (UnitTestViewHolder) convertView.getTag();
		}

		Object item = getItem(position);
		vh.title.setText(item.getClass().getSimpleName());
		vh.subtitle.setText(item.toString());

		return convertView;
	}

	@Override    //Default isFilteredBy, required by all subclasses to implement
	protected boolean isFilteredBy(Object item, CharSequence constraint) {
		return item.toString().toLowerCase(Locale.US)
				   .contains(constraint.toString().toLowerCase(Locale.US));
	}

	@Override    //Predefined isFilteredBy, can optionally override to change the built in logic
	protected boolean isFilteredBy(Long item, CharSequence constraint) {
		return String.valueOf(item).toLowerCase(Locale.US)
					 .contains(constraint.toString().toLowerCase(Locale.US));
	}

	//Custom isFilteredBy, found on adapter construction and called reflexively for any MovieItem
	//object found in adapter while filtering.
	protected boolean isFilteredBy(MovieItem movie, CharSequence constraint) {
		return movie.title.toLowerCase(Locale.US)
						  .contains(constraint.toString().toLowerCase(Locale.US));
	}

	//Another custom isFilteredBy method. Be it private, public, protected or no modifier at all.
	//JSONAdapter will still find it and invoke it appropriately
	@SuppressWarnings("UnusedDeclaration")
	private boolean isFilteredBy(JSONObject item, CharSequence constraint) {
		String title = item.optString(MovieItem.JSON_TITLE).toLowerCase(Locale.US);
		return title.contains(constraint.toString().toLowerCase(Locale.US));
	}
}