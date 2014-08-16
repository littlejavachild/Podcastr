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
					if(favorites.isEmpty()){
						favorites.addAll(faves);
					}
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
			System.out.println("Added: " + favorite.getString(Constants.ENCLOSURE));
			System.out.println("***SIZE: " + favorites.size() + "***");
		}
	}
	//------------------------------------------------------------------------------
	public static void removeFromFavorites(ParseObject favorite){
		int index = -1;
		String _enclosure = (String) favorite.get(Constants.ENCLOSURE);
		for(int i = 0; i < favorites.size(); i++){
			ParseObject each = favorites.get(i);
			String enclosure = (String) each.get(Constants.ENCLOSURE);
			if(_enclosure.equals(enclosure)){
				index = i;
				break;
			}	
		}
		ParseObject remove = favorites.get(index);
		favorites.remove(remove);
		remove.unpinInBackground(delCallback);
		System.out.println("Removed: " + remove.getString(Constants.ENCLOSURE));
		System.out.println("***SIZE: " + favorites.size() + "***");
	}
	//------------------------------------------------------------------------------
	public static boolean containsFavorite(ParseObject favorite){
		boolean contains = false;
		String _enclosure = (String) favorite.get(Constants.ENCLOSURE);
		for(ParseObject each : favorites){
			String enclosure = (String) each.get(Constants.ENCLOSURE);
			if(_enclosure.equals(enclosure)){
				System.out.println("MATCH FOUND!");
				contains = true; 
				break;
			}
		}
		return contains;
	}
	//------------------------------------------------------------------------------
	public static List<Episode> getFavoritesAsEpisodes(){
		List<Episode> episodes = new ArrayList<Episode>();
		for(ParseObject fav : favorites){
			Episode ep = new Episode();
			ep.setTitle(fav.getString(Constants.TITLE));
			ep.setDescription(fav.getString(Constants.DESCRIPTION));
			ep.setGuid(fav.getString(Constants.GUID));
			ep.setEnclosureURL(fav.getString(Constants.ENCLOSURE));
			episodes.add(ep);
		}
		return episodes;
	}
	//------------------------------------------------------------------------------
}
