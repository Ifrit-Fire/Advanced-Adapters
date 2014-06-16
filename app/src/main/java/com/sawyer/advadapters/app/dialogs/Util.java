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
package com.sawyer.advadapters.app.dialogs;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;

class Util {
	/**
	 * Will apply an orange color to a dialog's title bar divider. Yes this is quite hackish and may
	 * not work on all devices. However its crash safe. Just make sure the dialog has already set
	 * it's content view.
	 *
	 * @param dialog Holo dialog with an already instantiate view hierarchy to style title divider
	 */
	static void setTitleBarColor(Dialog dialog) {
		View v = dialog.findViewById(android.R.id.title);
		if (v != null && v.getParent() instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v.getParent();
			for (int index = 0; index < vg.getChildCount(); ++index) {
				View child = vg.getChildAt(index);
				if (child.getClass() == View.class) {
					child.setBackgroundColor(child.getContext().getResources().getColor(android.R.color.holo_orange_dark));
					break;
				}
			}
		}
	}
}
