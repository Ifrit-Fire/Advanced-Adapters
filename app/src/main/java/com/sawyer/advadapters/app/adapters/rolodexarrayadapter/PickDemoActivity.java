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

package com.sawyer.advadapters.app.adapters.rolodexarrayadapter;

import android.content.Intent;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.adapters.BasePickDemoActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.NeverCollapseHeaderActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.fulldemo.FullDemoActivity;
import com.sawyer.advadapters.widget.RolodexBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class PickDemoActivity extends BasePickDemoActivity {
	@Override
	public List<Intent> createIntentList() {
		List<Intent> intents = new ArrayList<>();
		Intent intent;

		/* Full Demos */
		intent = new Intent(this, FullDemoActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_movie_demo));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_fulldemos));
		intent.putExtra(EXTRA_CHOICE_MODE, RolodexBaseAdapter.ChoiceMode.NONE);
		intents.add(intent);

		/* Partial Examples */
		intent = new Intent(this, NeverCollapseHeaderActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_rolodex_never_collapse_header));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basicdemos));
		intents.add(intent);

		return intents;
	}
}
