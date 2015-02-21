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
package com.sawyer.advadapters.app.adapters.jsonadapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.MovieViewHolder;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.JSONAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

class MovieJSONAdapter extends JSONAdapter {

	MovieJSONAdapter(Context activity) {
		super(activity);
	}

	MovieJSONAdapter(Context activity, JSONArray list) {
		super(activity, list);
	}

	@Override
	public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
		MovieViewHolder vh;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_movie1, parent, false);
			vh = new MovieViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (MovieViewHolder) convertView.getTag();
		}

		try {
			JSONObject movie = optItemJSONObject(position);
			if (movie != null) {
				vh.title.setText(movie.getString(MovieItem.JSON_TITLE));
				vh.subtitle.setText(movie.getString(MovieItem.JSON_YEAR));
				vh.icon.setImageResource((movie.getBoolean(MovieItem.JSON_IS_RECOMMENDED))
												 ? R.drawable.ic_rating_good : R.drawable.ic_rating_bad);
			}
		} catch (JSONException e) {
			Log.e(MovieJSONAdapter.class.getSimpleName(), "GetView error", e);
		}

		return convertView;
	}

	@Override
	protected boolean isFilteredOut(Object item, CharSequence constraint) {
		//We are expecting the adapter to only ever contain JSONObjects. So we are specifically letting
		//the JSONObject filter handle it.
		return false;
	}

	@SuppressWarnings("UnusedDeclaration")    //Reflexively called, hence the suppressed warning
	private boolean isFilteredOut(JSONObject item, CharSequence constraint) {
		String title = item.optString(MovieItem.JSON_TITLE).toLowerCase(Locale.US);
		return !title.contains(constraint.toString().toLowerCase(Locale.US));
	}
}