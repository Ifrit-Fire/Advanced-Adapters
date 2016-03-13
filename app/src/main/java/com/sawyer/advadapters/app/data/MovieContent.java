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
package com.sawyer.advadapters.app.data;

import android.util.SparseArray;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for sample lists
 */
public class MovieContent {

	/** An array of sample (movie) items. */
	public static final List<MovieItem> ITEM_LIST = new ArrayList<>();

	/** A sparse array of sample (movie) items */
	public static final SparseArray<MovieItem> ITEM_SPARSE = new SparseArray<>();

	/** A JSON array of sample (movie) items */
	public static final JSONArray ITEM_JSON = new JSONArray();

	static {
		addItem(newMovie("Primer", 2004, true));
		addItem(newMovie("I, Robot", 2004, true));
		addItem(newMovie("The Day After Tomorrow", 2004, false));

		addItem(newMovie("V For Vendetta", 2005, true));
		addItem(newMovie("War of the Worlds", 2005, true));
		addItem(newMovie("Doom", 2005, true));

		addItem(newMovie("X-Men: The Last Stand", 2006, false));
		addItem(newMovie("Superman Returns", 2006, false));
		addItem(newMovie("Ultraviolet", 2006, false));

		addItem(newMovie("I Am Legend", 2007, true));
		addItem(newMovie("Transformers", 2007, true));
		addItem(newMovie("The Nines", 2007, true));

		addItem(newMovie("Cloverfield", 2008, true));
		addItem(newMovie("Jumper", 2008, true));
		addItem(newMovie("Iron Man", 2008, true));

		addItem(newMovie("Avatar", 2009, true));
		addItem(newMovie("District 9", 2009, true));
		addItem(newMovie("Terminator Salvation", 2009, false));

		addItem(newMovie("Inception", 2010, true));
		addItem(newMovie("Tron: Legacy", 2010, true));
		addItem(newMovie("Resident Evil: Afterlife", 2010, false));

		addItem(newMovie("Source Code", 2011, true));
		addItem(newMovie("Green Lantern", 2011, false));
		addItem(newMovie("The Adjustment Bureau", 2011, true));

		addItem(newMovie("Prometheus", 2012, true));
		addItem(newMovie("Men in Black 3", 2012, false));
		addItem(newMovie("Looper", 2012, true));

		addItem(newMovie("Ender's Game", 2013, false));
		addItem(newMovie("Elysium", 2013, true));
		addItem(newMovie("Her", 2013, false));
	}

	private static void addItem(MovieItem item) {
		ITEM_LIST.add(item);
		ITEM_SPARSE.put(item.barcode(), item);
		ITEM_JSON.put(item.toJSONObject());
	}

	private static MovieItem newMovie(String title, int year, boolean isRecommended) {
		MovieItem item = new MovieItem();
		item.title = title;
		item.year = year;
		item.isRecommended = isRecommended;
		return item;
	}
}
