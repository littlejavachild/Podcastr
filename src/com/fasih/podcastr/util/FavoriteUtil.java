package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.fasih.podcastr.HomeActivity;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class FavoriteUtil {
	
	private static List<ParseObject> favorites = new ArrayList<ParseObject>();
	private static SaveCallback callback = new SaveCallback() {
		@Override
		public void done(ParseException e) {
			if(e != null){
				e.printStackTrace();
			}
		}
	};
	private static DeleteCallback delCallback = new DeleteCallback() {
		@Override
		public void done(ParseException e) {
			if(e != null){
				e.printStackTrace();
			}
		}
	};
	//------------------------------------------------------------------------------
	public static void loadFavoritesFromDatabase(){
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Constants.FAVORITES_CLASS_NAME);
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>(){
			@Override
			public void done(List<ParseObject> faves, ParseException e) {
				if( e == null){
					favorites.addAll(faves);
					System.out.println("***FAVORITES: " + faves.size() + "***");
				}else{
					System.out.println(e.getMessage());
				}
			}
		});
	}
	//------------------------------------------------------------------------------
	public static void addToFavorites(ParseObject favorite){
		if(!containsFavorite(favorite)){
			favorites.add(favorite);
			favorite.pinInBackground(callback);
			System.out.println("Added: " + favorite.getString(Constants.EPISODE));
			System.out.println("***SIZE: " + favorites.size() + "***");
		}
	}
	//------------------------------------------------------------------------------
	public static void removeFromFavorites(ParseObject favorite){
		int index = -1;
		String _podcast = (String) favorite.get(Constants.PODCAST);
		String _episode = (String) favorite.get(Constants.EPISODE);
		for(int i = 0; i < favorites.size(); i++){
			ParseObject each = favorites.get(i);
			String episode = (String) each.get(Constants.EPISODE);
			String podcast = (String) each.get(Constants.PODCAST);
			if(_podcast.equals(podcast) && _episode.equals(episode)){
				index = i;
				break;
			}	
		}
		ParseObject remove = favorites.get(index);
		favorites.remove(remove);
		remove.unpinInBackground(delCallback);
		System.out.println("Removed: " + remove.getString(Constants.EPISODE));
		System.out.println("***SIZE: " + favorites.size() + "***");
	}
	//------------------------------------------------------------------------------
	public static boolean containsFavorite(ParseObject favorite){
		boolean contains = false;
		String _podcast = (String) favorite.get(Constants.PODCAST);
		String _episode = (String) favorite.get(Constants.EPISODE);
		for(ParseObject each : favorites){
			String episode = (String) each.get(Constants.EPISODE);
			String podcast = (String) each.get(Constants.PODCAST);
			System.out.println("Comparing: " + episode + " With: " + episode);
			if(_podcast.equals(podcast) && _episode.equals(episode)){
				System.out.println("MATCH FOUND!");
				contains = true; 
				break;
			}
		}
		return contains;
	}
	//------------------------------------------------------------------------------
}
