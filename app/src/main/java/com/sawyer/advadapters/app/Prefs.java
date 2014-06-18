/**
 * By: JaySoyer
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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
	private static final SharedPreferences PREF = PreferenceManager
			.getDefaultSharedPreferences(App.context());

	public static boolean hasViewedArrayBaseAdapterInfo() {
		return PREF.getBoolean(App.context().getString(R.string.key_pref_viewed_array_baseadapter),
							   App.getBoolean(R.bool.default_pref_viewed_array_baseadapter));
	}

	public static void setViewedArrayBaseAdapterInfo(boolean hasViewed) {
		SharedPreferences.Editor editor = PREF.edit();
		editor.putBoolean(App.context().getString(R.string.key_pref_viewed_array_baseadapter),
						  hasViewed);
		editor.apply();
	}
}