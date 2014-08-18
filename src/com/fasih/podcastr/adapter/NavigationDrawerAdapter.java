package com.fasih.podcastr.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.util.EffectsUtil;

public class NavigationDrawerAdapter extends BaseAdapter {
	private String[] options = {};
	private Context con = null;
	private Typeface roboto = null;
	//------------------------------------------------------------------------------
	public NavigationDrawerAdapter(Context con){
		this.con = con;
		options = con.getResources().getStringArray(R.array.options);
		roboto = PodcastrApplication.newInstance().getTypeface();
	}
	//------------------------------------------------------------------------------
	@Override
	public boolean isEnabled(int position){
		// Disable the banner. It should not be clickable.
		if(position == 0){
			return false;
		}
		return true;
	}
	//------------------------------------------------------------------------------
	@Override
	public int getViewTypeCount(){
		return 2;
	}
	//------------------------------------------------------------------------------
	@Override
	public int getItemViewType (int position){
		if(position == 0)
			return 0; // 0 = banner
		else
			return 1; // 1 = other list items
	}
	//------------------------------------------------------------------------------
	@Override
	public int getCount() {
		return options.length + 1;
	}
	//------------------------------------------------------------------------------
	@Override
	public Object getItem(int position) {
		return options[position];
	}
	//------------------------------------------------------------------------------
	@Override
	public long getItemId(int position) {
		return position;
	}
	//------------------------------------------------------------------------------
	@Override
	public View getView(int position, View convertView, ViewGroup root) {
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(con);
			if(position == 0){
				convertView = inflater.inflate(R.layout.banner, root, false);
				convertView.setBackgroundColor(Color.rgb(86, 78, 59));
			}else{
				int redId = android.R.layout.simple_list_item_1;
				convertView = inflater.inflate(redId, root, false);
			}
				
		}
		// If it is the banner we are dealing with
		if(position == 0){
			ImageView bannerImage = (ImageView) convertView.findViewById(R.id.bannerImage);
			TextView bannerText = (TextView) convertView.findViewById(R.id.bannerText);
			bannerText.setTypeface(roboto, Typeface.BOLD);
		}else{
			TextView simpleTextView = (TextView) convertView;
			simpleTextView.setText(options[position - 1]);
			simpleTextView.setTextSize(21);
			simpleTextView.setTypeface(roboto,Typeface.BOLD);
		}
		
		return convertView;
	}
	//------------------------------------------------------------------------------
}
