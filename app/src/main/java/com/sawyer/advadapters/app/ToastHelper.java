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
package com.sawyer.advadapters.app;

import android.content.Context;
import android.widget.Toast;

import com.sawyer.advadapters.app.data.MovieItem;

import static android.widget.Toast.LENGTH_SHORT;

public class ToastHelper {

	public static void showChoiceModeNotSupported(Context activity) {
		Toast.makeText(activity, R.string.toast_choice_mode_not_supported, LENGTH_SHORT).show();
	}

	public static void showContainsFalse(Context activity, String text) {
		Toast.makeText(activity, activity.getString(R.string.toast_contains_movie_false) + text,
					   LENGTH_SHORT).show();
	}

	public static void showContainsTrue(Context activity, String text) {
		Toast.makeText(activity, activity.getString(R.string.toast_contains_movie_true) + text,
					   LENGTH_SHORT).show();
	}

	public static void showGroupClicked(Context activity, String text) {
		Toast.makeText(activity, activity.getString(R.string.toast_group_clicked) + text,
					   LENGTH_SHORT).show();
	}

	public static void showItemUpdatesNotSupported(Context activity) {
		Toast.makeText(activity, R.string.toast_item_updates_not_supported, LENGTH_SHORT).show();
	}

	public static void showMovieClicked(Context activity, MovieItem movie) {
		Toast.makeText(activity, activity.getString(R.string.toast_movie_clicked) + movie.title,
					   LENGTH_SHORT).show();
	}

	public static void showRemoveNotSupported(Context activity) {
		Toast.makeText(activity, R.string.toast_remove_not_supported, LENGTH_SHORT).show();
	}

	public static void showRetainAllNotSupported(Context activity) {
		Toast.makeText(activity, R.string.toast_retain_all_not_supported, LENGTH_SHORT).show();
	}

	public static void showSortNotSupported(Context activity) {
		Toast.makeText(activity, R.string.toast_sort_not_supported, LENGTH_SHORT).show();
	}
}
