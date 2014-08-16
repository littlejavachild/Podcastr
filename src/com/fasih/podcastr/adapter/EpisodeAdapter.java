package com.fasih.podcastr.adapter;

import java.util.List;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.fasih.podcastr.PodcastrApplication;
import android.widget.TextView;

import com.fasih.podcastr.util.Episode;
import com.fasih.podcastr.util.EpisodeUtil;

public class EpisodeAdapter extends BaseAdapter {
	
	private List<Episode> episodes = EpisodeUtil.getEpisodes();
	private Typeface roboto = PodcastrApplication.newInstance().getTypeface();
	
	
	public EpisodeAdapter(){
		
	}
	//------------------------------------------------------------------------------
	public EpisodeAdapter(List<Episode> newEpisodes){
		episodes = newEpisodes;
	}
	//------------------------------------------------------------------------------
	@Override
	public int getCount() {
		return episodes.size();
	}
	//------------------------------------------------------------------------------
	@Override
	public Object getItem(int position) {
		if(episodes.size() > 0){
			return episodes.get(position);
		}else{
			return null;
		}
	}
	//------------------------------------------------------------------------------
	@Override
	public long getItemId(int position) {
		return position;
	}
	//------------------------------------------------------------------------------
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			TextView tv = (TextView) convertView;
			tv.setTypeface(roboto, Typeface.BOLD);
			tv.setSingleLine();
			tv.setEllipsize(TextUtils.TruncateAt.END);
		}
		TextView tv = (TextView) convertView;
		if(!episodes.isEmpty()){
			String _title = episodes.get(position).getTitle();
			tv.setText(_title);
		}
		return convertView;
	}
	//------------------------------------------------------------------------------
	public void setDataSource(List<Episode> ep){
		this.episodes = ep;
		notifyDataSetChanged();
	}
	//------------------------------------------------------------------------------
}
