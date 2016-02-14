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
package com.sawyer.advadapters.app.adapters.nfjsonadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sawyer.advadapters.app.adapters.UnitTestViewHolder;
import com.sawyer.advadapters.widget.NFJSONArrayAdapter;

import org.json.JSONArray;

class UnitTestMovieAdapter extends NFJSONArrayAdapter {

	UnitTestMovieAdapter(Context activity) {
		super(activity);
	}

	UnitTestMovieAdapter(Context activity, JSONArray list) {
		super(activity, list);
	}

	@NonNull
	@Override
	public View getView(@NonNull LayoutInflater inflater, int position, View convertView,
						ViewGroup parent) {
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
}