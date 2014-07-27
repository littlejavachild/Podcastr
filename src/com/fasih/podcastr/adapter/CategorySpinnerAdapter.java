package com.fasih.podcastr.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.util.PodcastUtil;

public class CategorySpinnerAdapter extends BaseAdapter {
	
	private List<CharSequence> categories = PodcastUtil.getCategories();
	private Typeface roboto = PodcastrApplication.newInstance().getTypeface();
	private int fontColor = -1;
	@Override
	public int getCount() {
		return categories.size();
	}

	@Override
	public Object getItem(int position) {
		return categories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		TextView tv = (TextView) convertView;
		tv.setText(categories.get(position));
		tv.setTypeface(roboto,Typeface.BOLD);
		
		if(fontColor == -1){
			Context c = convertView.getContext();
			int fontColor = c.getResources().getColor(android.R.color.darker_gray);
			tv.setTextColor(fontColor);
		}
		
		return convertView;
	}
}
