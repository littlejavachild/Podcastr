package com.fasih.podcastr;


import java.io.File;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.fasih.podcastr.service.MusicService;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.FavoriteUtil;
import com.fasih.podcastr.util.RecentUtil;
import com.parse.Parse;
import com.squareup.picasso.Picasso;

public class PodcastrApplication extends Application {
	private static PodcastrApplication singleton = null;
	private static Typeface roboto = null;
	private boolean homeActivityVisible = false;
	private static MediaPlayer player = null;
	private static Intent musicService = null;
	//--------------------------------------------------------------------------------------------------
	@Override
	public void onCreate(){
		singleton = this;
		// Create a single media player
		// that will be used everywhere
		player = new MediaPlayer();
		
		// The following line enables localdatastore
		Parse.enableLocalDatastore(this);
		// The following line initialized Parse
		Parse.initialize(this, "juecnERSHJiESim4for8CWwT5o7LUWCvgyOzYdx7", "VeJvVnI7B9llgRrc72ywYK14kI6C47aMeeY9nbqg");
		
		// Intent to use the MusicService
		musicService = new Intent(getApplicationContext(), MusicService.class);
		
		FavoriteUtil.loadFavoritesFromDatabase();
		RecentUtil.loadRecentsFromDatabase();
		
		createTypeface();
		createDirectories();

	}
	//--------------------------------------------------------------------------------------------------
	/**
	 * Used to lead the Roboto-Thin font from the assets
	 */
	private void createTypeface(){
		roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
	}
	//--------------------------------------------------------------------------------------------------
	/**
	 * Used to obtain a reference to the Application object
	 * @return a reference to the Application singleton
	 */
	public static PodcastrApplication newInstance(){
		return singleton;
	}
	//--------------------------------------------------------------------------------------------------
	/**
	 * Used to obtain a reference to the Typeface
	 * @return a reference to the Typeface
	 */
	public Typeface getTypeface(){
		return roboto;
	}
	//--------------------------------------------------------------------------------------------------
	synchronized public void setHomeActivityVisible(boolean visibility){
		homeActivityVisible = visibility;
	}
	//--------------------------------------------------------------------------------------------------
	synchronized public boolean getHomeActivityVisible(){
		return homeActivityVisible;
	}
	//--------------------------------------------------------------------------------------------------
	private void createDirectories(){
		File xml = new File(Constants.DIR_XML);
		File downloads = new File(Constants.DIR_DOWNLOADS);
		
		if(!xml.exists()){
			xml.mkdirs();
		}
		
		if(!downloads.exists()){
			downloads.mkdirs();
		}
	}
	//--------------------------------------------------------------------------------------------------
	public MediaPlayer getMediaPlayer(){
		return player;
	}
	//--------------------------------------------------------------------------------------------------
	public void setMusicServiceIntent(Intent music){
		musicService = music;
	}
	//--------------------------------------------------------------------------------------------------
	public Intent getMusicServiceIntent(){
		return musicService;
	}
	//--------------------------------------------------------------------------------------------------
	@Override
	public void onTrimMemory(int memory){
		
		
	}
	//--------------------------------------------------------------------------------------------------
}
