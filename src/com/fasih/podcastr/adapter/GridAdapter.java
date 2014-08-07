package com.fasih.podcastr.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.util.Podcast;
import com.fasih.podcastr.util.PodcastUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class GridAdapter extends BaseAdapter {
	private List<Podcast> podcasts = PodcastUtil.getPodcasts();
	@Override
	public int getCount() {
		return podcasts.size();
	}
	//------------------------------------------------------------------------------
	@Override
	public Object getItem(int position) {
		return podcasts.get(position);
	}
	//------------------------------------------------------------------------------
	@Override
	public long getItemId(int position) {
		return position;
	}
	//------------------------------------------------------------------------------
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Podcast podcast = podcasts.get(position);
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.grid_item, parent, false);
		}
		
		ImageView logo = (ImageView) convertView.findViewById(R.id.logo);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		Typeface roboto = PodcastrApplication.newInstance().getTypeface();
		
		Uri uri = Uri.parse(podcast.getLogo());
		Context c = convertView.getContext();
		title.setText(podcast.getTitle());
		title.setTypeface(roboto, Typeface.BOLD);
		
//		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		int width = size.x;
//		Picasso.with(c).load(uri).resize(width/2, width/2).into(logo);
//		title.setWidth(width/2);
		
		Picasso.with(c).load(uri).error(R.drawable.headphones).into(logo);
		
		return convertView;
	}
	//------------------------------------------------------------------------------
}
