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
package com.sawyer.advadapters.app.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Random;

public class MovieItem implements Comparable<MovieItem>, Parcelable {
	public static final Creator<MovieItem> CREATOR = new MovieCreator();

	private static Random sRand = new Random();

	public String title;
	public int year;
	public boolean isRecommended;

	private int mBarcode;

	public MovieItem() {
		mBarcode = sRand.nextInt(Integer.MAX_VALUE);
	}

	public MovieItem(int barcode) {
		mBarcode = barcode;
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
		if (o instanceof MovieItem == false) return false;
		MovieItem other = (MovieItem) o;
		return mBarcode == other.mBarcode;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(mBarcode).hashCode();
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
			item.isRecommended = Boolean.getBoolean(source.readString());

			return item;
		}

		@Override
		public MovieItem[] newArray(int size) {
			return new MovieItem[size];
		}
	}
}
