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
package com.sawyer.advadapters.app.adapters.android.simpleexpandablelistadapter;

import android.content.Context;
import android.widget.SimpleExpandableListAdapter;

import java.util.List;
import java.util.Map;

/**
 * Does this seem useless? You bet. Its here in for solely for quick testing purposes during
 * development or whenever I need to quickly test a using a custom version of the
 * SimpleExpandableListAdapter for whatever reason. Otherwise, for purposes of the Demo App, this
 * class is not needed.
 */
public class AndroidExpandableAdapter extends SimpleExpandableListAdapter {

	public AndroidExpandableAdapter(Context context, List<? extends Map<String, ?>> groupData,
									int groupLayout, String[] groupFrom, int[] groupTo,
									List<? extends List<? extends Map<String, ?>>> childData,
									int childLayout, String[] childFrom, int[] childTo) {
		super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout,
			  childFrom, childTo);
	}
}
