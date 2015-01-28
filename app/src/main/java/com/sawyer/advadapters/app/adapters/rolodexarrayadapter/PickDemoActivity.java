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
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.advancedemo.ActionModeActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.advancedemo.MultiSelectActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.advancedemo.NavigationDrawerActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.AddItemsActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.ClickListenerActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.ContainsItemActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.ExpandCollapseAllActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.FilterGroupsOnlyActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.NeverCollapseGroupActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.NeverCollapseGroupUnsortedActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.RemoveItemsActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.RetainAndSetListActivity;
import com.sawyer.advadapters.app.adapters.rolodexarrayadapter.basicdemo.SortAllChildrenActivity;
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
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_full_demos));
		intent.putExtra(EXTRA_CHOICE_MODE, RolodexBaseAdapter.ChoiceMode.NONE);
		intents.add(intent);

		/* Basic Examples - This group will be sorted. */
		intent = new Intent(this, NeverCollapseGroupActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_rolodex_never_collapse_groups));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, NeverCollapseGroupUnsortedActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_rolodex_never_collapse_groups_unsorted));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, AddItemsActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_add_items));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, RemoveItemsActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_remove_items));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, RetainAndSetListActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_retain_set_list));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, SortAllChildrenActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_sort_all_children));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, ContainsItemActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_contains_item));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, ExpandCollapseAllActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_rolodex_expand_collapse_all));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, ClickListenerActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_click_listener));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		intent = new Intent(this, FilterGroupsOnlyActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_filter_groups_only));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_basic_demos));
		intents.add(intent);

		/* Advanced Examples */
		intent = new Intent(this, MultiSelectActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_multiselect));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_advanced_demos));
		intents.add(intent);

		intent = new Intent(this, ActionModeActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_action_mode));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_advanced_demos));
		intents.add(intent);

		intent = new Intent(this, NavigationDrawerActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_rolodex_navigation_drawer));
		intent.putExtra(EXTRA_GROUP_NAME, getString(R.string.title_group_advanced_demos));
		intents.add(intent);

		return intents;
	}
}
