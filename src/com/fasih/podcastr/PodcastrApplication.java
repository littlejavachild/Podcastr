package com.fasih.podcastr;


import android.app.Application;
import android.graphics.Typeface;

public class PodcastrApplication extends Application {
	private static PodcastrApplication singleton = null;
	private Typeface roboto = null;
	private boolean homeActivityVisible = false;
	//--------------------------------------------------------------------------------------------------
	@Override
	public void onCreate(){
		singleton = this;
		createTypeface();
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
}
