package com.fasih.podcastr.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {
	
	public static int getSeekTo(Context con){
		SharedPreferences prefs = con.getSharedPreferences(Constants.PREFS_FILE, 0);
		int seekTo = prefs.getInt(Constants.SEEK_TO, 0);
		return seekTo;
	}
	//----------------------------------------------------------------------------------------
	public static void setSeekTo(Context con, int seekTo){
		SharedPreferences prefs = con.getSharedPreferences(Constants.PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Constants.SEEK_TO, seekTo);
		editor.commit();
	}
	//----------------------------------------------------------------------------------------
	public static int getVideoIndex(Context con){
		SharedPreferences prefs = con.getSharedPreferences(Constants.PREFS_FILE, 0);
		int videoIndex = prefs.getInt(Constants.VIDEO_INDEX, 0);
		return videoIndex;
	}
	//----------------------------------------------------------------------------------------
	public static void setVideoIndex(Context con, int index){
		SharedPreferences prefs = con.getSharedPreferences(Constants.PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Constants.VIDEO_INDEX, index);
		editor.commit();
	}
	//----------------------------------------------------------------------------------------
}
