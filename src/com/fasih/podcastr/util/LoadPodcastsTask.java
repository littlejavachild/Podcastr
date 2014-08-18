package com.fasih.podcastr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;

import com.fasih.podcastr.adapter.GridAdapter;
import com.google.gson.Gson;

public class LoadPodcastsTask extends AsyncTask<String, Void, Void> {
	private FragmentManager fm = null;
	private AssetManager asset = null;
	private GridAdapter adapter = null;
	private StringBuffer json = new StringBuffer();
	//------------------------------------------------------------------------------
	public LoadPodcastsTask(FragmentManager fm, AssetManager asset, GridAdapter adapter){
		this.fm = fm;
		this.asset = asset;
		this.adapter = adapter;
	}
	//------------------------------------------------------------------------------
	@Override
	protected void onPreExecute(){
		
	}
	//------------------------------------------------------------------------------
	@Override
	protected Void doInBackground(String... arg0) {
		String fileName = arg0[0];
		String line = "";
		BufferedReader reader = null;
		try{
			// Read the JSON file
			reader = new BufferedReader(new InputStreamReader(asset.open(fileName)));
			line = reader.readLine();
			while( line!= null ){
				json.append(line);
				line = reader.readLine();
				if(isCancelled()){
					break;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			// Close the reader
			if(reader != null){
				try{
					reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	//------------------------------------------------------------------------------
	@Override
	protected void onPostExecute(Void v){
		// If not cancelled, deserialize
		System.out.println(json);
		Gson gson = new Gson();
		Response response = gson.fromJson(json.toString(), Response.class);
		if(!isCancelled()){
			if(response != null){
				PodcastUtil.addPodcasts(response.getPodcasts());
				for(Podcast podcast : response.getPodcasts()){
					PodcastUtil.addCategory(podcast.getCategory());
				}
				adapter.notifyDataSetChanged();
			}
		}
	}
}
