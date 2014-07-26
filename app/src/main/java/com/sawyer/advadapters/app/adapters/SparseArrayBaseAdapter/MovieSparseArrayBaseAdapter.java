package com.sawyer.advadapters.app.adapters.SparseArrayBaseAdapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sawyer.advadapters.app.R;
import com.sawyer.advadapters.app.data.MovieItem;
import com.sawyer.advadapters.widget.SparseArrayBaseAdapter;

class MovieSparseArrayBaseAdapter extends SparseArrayBaseAdapter<MovieItem> {
	public MovieSparseArrayBaseAdapter(Context activity) {
		super(activity);
	}

	public MovieSparseArrayBaseAdapter(Context activity, SparseArray<MovieItem> items) {
		super(activity, items);
	}

	@Override
	public View getView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_movie1, parent, false);
			convertView.setTag(R.id.title, convertView.findViewById(R.id.title));
			convertView.setTag(R.id.subtitle, convertView.findViewById(R.id.subtitle));
			convertView.setTag(R.id.icon, convertView.findViewById(R.id.icon));
		}

		MovieItem movie = getItem(position);
		TextView title = (TextView) convertView.getTag(R.id.title);
		title.setText(movie.title);

		TextView subtitle = (TextView) convertView.getTag(R.id.subtitle);
		subtitle.setText(String.valueOf(movie.year));

		ImageView icon = (ImageView) convertView.getTag(R.id.icon);
		icon.setImageResource(
				(movie.isRecommended) ? R.drawable.ic_rating_good : R.drawable.ic_rating_bad);

		return convertView;
	}

	@Override
	protected boolean isFilteredBy(int keyId, MovieItem item, CharSequence constraint) {
		return false;
	}
}
