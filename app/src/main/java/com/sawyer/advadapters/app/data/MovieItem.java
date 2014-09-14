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
package com.sawyer.advadapters.app.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

public class MovieItem implements Comparable<MovieItem>, Parcelable {
	public static final Creator<MovieItem> CREATOR = new MovieCreator();
	public static final String JSON_TITLE = "title";
	public static final String JSON_YEAR = "year";
	public static final String JSON_IS_RECOMMENDED = "recommended";
	public static final String JSON_BARCODE = "barcode";

	private static Random sRand = new Random();

	public String title;
	public int year;
	public boolean isRecommended;
	private int mBarcode;
	private long mBarcodeLong;

	public MovieItem() {
		mBarcode = sRand.nextInt();
		mBarcodeLong = mBarcode;
	}

	public MovieItem(int barcode) {
		mBarcode = barcode;
		mBarcodeLong = barcode;
	}

	private static String getStringToCompare(String text) {
		if (TextUtils.isEmpty(text)) return "";

		String compare = text.toLowerCase(Locale.US);
		compare = compare.replace("the ", "").replace("a ", "").replace("an ", "");
		return compare;
	}

	public int barcode() {
		return mBarcode;
	}

	public long barcodeLong() {
		return mBarcodeLong;
	}

	@Override
	public int compareTo(MovieItem another) {
		if (TextUtils.isEmpty(title)) {
			return (TextUtils.isEmpty(another.title)) ? Integer.valueOf(year).compareTo(year) : 1;
		}

		return getStringToCompare(title).compareTo(getStringToCompare(another.title));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MovieItem)) return false;
		MovieItem other = (MovieItem) o;
		return mBarcode == other.mBarcode;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(mBarcode).hashCode();
	}

	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		try {
			object.put(JSON_TITLE, title);
			object.put(JSON_YEAR, year);
			object.put(JSON_IS_RECOMMENDED, isRecommended);
			object.put(JSON_BARCODE, barcode());
		} catch (JSONException e) {
			Log.e(MovieItem.class.getSimpleName(), "Error converting to JSON", e);
		}

		return object;
	}

	@Override
	public String toString() {
		return (title == null ? "" : title) + " " + Integer.toString(year) + " " +
			   Boolean.toString(isRecommended);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeInt(year);
		dest.writeInt(mBarcode);
		dest.writeString(Boolean.toString(isRecommended));
	}

	private static class MovieCreator implements Creator<MovieItem> {
		@Override
		public MovieItem createFromParcel(Parcel source) {
			MovieItem item = new MovieItem();
			item.title = source.readString();
			item.year = source.readInt();
			item.mBarcode = source.readInt();
			item.mBarcodeLong = item.mBarcode;
			item.isRecommended = Boolean.getBoolean(source.readString());

			return item;
		}

		@Override
		public MovieItem[] newArray(int size) {
			return new MovieItem[size];
		}
	}
}
