package com.fasih.podcastr.fragment;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fasih.podcastr.R;
import com.fasih.podcastr.adapter.GridAdapter;
import com.fasih.podcastr.util.LoadPodcastsTask;
import com.fasih.podcastr.util.PodcastUtil;

public class PodcastGridFragment extends Fragment {
	private GridView podcastGrid = null;
	private GridAdapter adapter = new GridAdapter();
	private AssetManager am;
	private FragmentManager fm;
	private LoadPodcastsTask loader = null;
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_podcast_grid, container, false);
		podcastGrid = (GridView) view.findViewById(R.id.podcastGrid);
		podcastGrid.setAdapter(adapter);
		setRetainInstance(true);
		return view;
	}
	
	public void setLoaderResources(FragmentManager fm, AssetManager am){
		this.fm = fm;
		this.am = am;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		loader = new LoadPodcastsTask(fm, am, adapter);
		loader.execute("txt/podcasts.txt");
	}
	
	public void sortByCategory(String category){
		PodcastUtil.sortByCategory(category);
		adapter.notifyDataSetChanged();
	}
}
