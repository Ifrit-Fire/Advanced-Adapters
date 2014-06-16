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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.adapters.arraybaseadapter.ArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.simplearraybaseadapter.SimpleArrayBaseAdapterActivity;
import com.sawyer.advadapters.widget.SimpleArrayBaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends ListActivity {
	private static final String EXTRA_INTENT_NAME = "Extra Intent Name";

	private List<Intent> createIntentList() {
		List<Intent> intents = new ArrayList<>();

		Intent intent = new Intent(this, ArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_array_baseadapter));
		intents.add(intent);

		intent = new Intent(this, SimpleArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_simplearray_baseadapter));
		intents.add(intent);

		return intents;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new DemoAdapter(this, createIntentList()));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = (Intent) getListAdapter().getItem(position);
		startActivity(intent);
	}

	private class DemoAdapter extends SimpleArrayBaseAdapter<Intent> {
		public DemoAdapter(Context activity, Collection<Intent> objects) {
			super(activity, objects);
		}

		@Override
		public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			}

			TextView tv = (TextView) convertView;
			tv.setText(getItem(position).getStringExtra(EXTRA_INTENT_NAME));

			return convertView;
		}
	}
}
