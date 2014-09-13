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
package com.sawyer.advadapters.app.adapters.jsonarraybaseadapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.ToastHelper;
import com.sawyer.advadapters.app.adapters.AdapterBaseActivity;
import com.sawyer.advadapters.app.data.MovieContent;
import com.sawyer.advadapters.widget.JSONArrayBaseAdapter;

import org.json.JSONException;

import java.util.Random;
import java.util.UUID;

public class UnitTestJSONArrayActivity extends AdapterBaseActivity implements
		UnitTestJSONArrayFragment.EventListener {
	private static final String TAG_BASE_ADAPTER_FRAG = "Tag Base Adapter Frag";
	private static final String TAG = UnitTestJSONArrayActivity.class.getSimpleName();

	private UnitTestJSONArrayFragment mListFragment;

	private static boolean getBoolean() {
		return new Random().nextBoolean();
	}

	private static byte getByte() {
		byte[] buf = new byte[1];
		new Random().nextBytes(buf);
		return buf[0];
	}

	private static char getChar() {
		return (char) (new Random().nextInt(26) + 'a');
	}

	private static double getDouble() {
		return new Random().nextDouble();
	}

	private static float getFloat() {
		return new Random().nextFloat();
	}

	private static int getInteger() {
		return new Random().nextInt();
	}

	private static long getLong() {
		return new Random().nextLong();
	}

	private static short getShort() {
		return (short) new Random().nextInt(Short.MAX_VALUE);
	}

	private static String getString() {
		return UUID.randomUUID().toString().substring(0, 6);
	}

	@Override
	protected void clear() {
		mListFragment.getListAdapter().clear();
		updateActionBar();
	}

	@Override
	protected void clearAdapterFilter() {
		mListFragment.getListAdapter().getFilter().filter("");
	}

	@Override
	protected String getInfoDialogMessage() {
		return getString(R.string.info_array_baseadapter_message);
	}

	@Override
	protected String getInfoDialogTitle() {
		return getString(R.string.info_array_baseadapter_title);
	}

	@Override
	protected int getListCount() {
		return mListFragment.getListAdapter().getCount();
	}

	@Override
	protected void initFrags() {
		super.initFrags();
		FragmentManager manager = getFragmentManager();
		mListFragment = (UnitTestJSONArrayFragment) manager
				.findFragmentByTag(TAG_BASE_ADAPTER_FRAG);
		if (mListFragment == null) {
			mListFragment = UnitTestJSONArrayFragment.newInstance();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.frag_container, mListFragment, TAG_BASE_ADAPTER_FRAG);
			transaction.commit();
		}
	}

	@Override
	protected boolean isSearchViewEnabled() {
		return true;
	}

	@Override
	public void onAdapterCountUpdated() {
		updateActionBar();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		if (fragment instanceof UnitTestJSONArrayFragment) {
			mListFragment = (UnitTestJSONArrayFragment) fragment;
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mListFragment.getListAdapter().getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mListFragment.getListAdapter().getFilter().filter(query);
		return true;
	}

	@Override
	protected void reset() {
		JSONArrayBaseAdapter adapter = mListFragment.getListAdapter();
		adapter.setNotifyOnChange(false);
		adapter.clear();
		adapter.add(getChar());    //JSONArray converts to Integer
		adapter.add(getString());
		adapter.add(getBoolean());
		adapter.add(getByte());    //JSONArray converts to Integer
		adapter.add(getShort());    //JSONArray converts to Integer
		adapter.add(getInteger());
		adapter.add(getLong());
		adapter.add(MovieContent.ITEM_LIST.get(0));
		try {
			adapter.add(getFloat());    //JSONArray converts to Double
			adapter.add(getDouble());
		} catch (JSONException e) {
			Log.e(TAG, "Error resetting float and/or double");
		}
		adapter.notifyDataSetChanged();
		updateActionBar();
	}

	@Override
	protected void sort() {
		ToastHelper.showSortNotSupported(this);
	}
}
