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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sawyer.advadapters.app.adapters.androidarrayadapter.AndroidAdapterActivity;
import com.sawyer.advadapters.app.adapters.arraybaseadapter.ArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.jsonarraybaseadapter.JSONArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.simplearraybaseadapter.SimpleArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.simplejsonarraybaseadapter.SimpleJSONArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.simplesparsearraybaseadapter.SimpleSparseArrayBaseAdapterActivity;
import com.sawyer.advadapters.app.adapters.sparsearraybaseadapter.SparseArrayBaseAdapterActivity;
import com.sawyer.advadapters.widget.SimpleArrayBaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends ListActivity {
	private static final String EXTRA_INTENT_NAME = "Extra Intent Name";

	private List<Intent> createIntentList() {
		List<Intent> intents = new ArrayList<>();
		Intent intent;

		intent = new Intent(this, ArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_array_baseadapter));
		intents.add(intent);

		intent = new Intent(this, SimpleArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_simplearray_baseadapter));
		intents.add(intent);

		intent = new Intent(this, SparseArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_sparsearray_baseadapter));
		intents.add(intent);

		intent = new Intent(this, SimpleSparseArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_simplesparsearray_baseadapter));
		intents.add(intent);

		intent = new Intent(this, JSONArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_jsonarray_baseadapter));
		intents.add(intent);

		intent = new Intent(this, SimpleJSONArrayBaseAdapterActivity.class);
		intent.putExtra(EXTRA_INTENT_NAME,
						getString(R.string.activity_simplejsonarray_baseadapter));
		intents.add(intent);

		return intents;
	}

	private void initHeaders() {
		ListView lv = getListView();
		View v = getLayoutInflater().inflate(R.layout.item_simple_list_header_1, lv, false);
		TextView tv = (TextView) v.findViewById(android.R.id.text1);
		tv.setText(R.string.activity_android_arrayadapter);
		v.setOnClickListener(new OnAndroidHeaderClickListener());
		lv.addHeaderView(v);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initHeaders();
		setListAdapter(new DemoAdapter(this, createIntentList()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		position -= l.getHeaderViewsCount();
		if (position >= 0) {
			Intent intent = (Intent) getListAdapter().getItem(position);
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_about:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DemoAdapter extends SimpleArrayBaseAdapter<Intent> {
		public DemoAdapter(Context activity, Collection<Intent> objects) {
			super(activity, objects);
		}

		@Override
		public View getView(LayoutInflater inflater, int position, View convertView,
							ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			}

			TextView tv = (TextView) convertView;
			tv.setText(getItem(position).getStringExtra(EXTRA_INTENT_NAME));

			return convertView;
		}
	}

	private class OnAndroidHeaderClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, AndroidAdapterActivity.class);
			intent.putExtra(EXTRA_INTENT_NAME, getString(R.string.activity_android_arrayadapter));
			startActivity(intent);
		}
	}
}
