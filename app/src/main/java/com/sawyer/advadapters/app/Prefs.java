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
package com.sawyer.advadapters.app;

import android.app.Activity;
import android.content.SharedPreferences;

public class Prefs {
	private static final String KEY_VIEW_ADAPTER_INFO = "Key View Adapter Info";
	private static final boolean DEFAULT_VIEW_ADAPTER_INFO = false;

	public static boolean hasViewedAdapterInfo(Activity activity) {
		SharedPreferences pref = activity.getPreferences(Activity.MODE_PRIVATE);
		return pref.getBoolean(KEY_VIEW_ADAPTER_INFO, DEFAULT_VIEW_ADAPTER_INFO);
	}

	public static void setViewedAdapterInfo(Activity activity, boolean hasViewed) {
		SharedPreferences pref = activity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(KEY_VIEW_ADAPTER_INFO, hasViewed);
		editor.apply();
	}
}