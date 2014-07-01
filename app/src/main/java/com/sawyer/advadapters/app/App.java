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

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class App extends Application {
	private static final String TAG = App.class.getPackage().getName();

	private static Context sContext;

	public static Context context() {
		return sContext;
	}

	public static boolean getBoolean(int resId) {
		return sContext.getResources().getBoolean(resId);
	}

	public static int getVersionCode() {
		int versionCode;

		try {
			versionCode = sContext.getPackageManager()
								  .getPackageInfo(sContext.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "No package info found");
			versionCode = -1;
		}
		return versionCode;
	}

	public static String getVersionName() {
		String versionName;
		try {
			versionName = sContext.getPackageManager()
								  .getPackageInfo(sContext.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "No version name found");
			versionName = "Error";
		}
		return versionName;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();
	}
}
