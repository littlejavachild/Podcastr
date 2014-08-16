package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class RecentUtil {
	
	private static List<ParseObject> recents = new ArrayList<ParseObject>();
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
	private static ChronoComparator comparator = new ChronoComparator();
	//------------------------------------------------------------------------------
	public static void loadRecentsFromDatabase(){
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Constants.RECENTS_CLASS_NAME);
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>(){
			@Override
			public void done(List<ParseObject> rec, ParseException e) {
				if( e == null){
					if(recents.isEmpty()){
						recents.addAll(rec);
						Collections.sort(recents, comparator);
					}
					System.out.println("***RECENTS: " + recents.size() + "***");
				}else{
					System.out.println(e.getMessage());
				}
			}
		});
	}
	//------------------------------------------------------------------------------
	public static boolean containsRecent(ParseObject recent){
		boolean contains = false;
		String _enclosure = (String) recent.get(Constants.ENCLOSURE);
		for(ParseObject each : recents){
			String enclosure = (String) each.get(Constants.ENCLOSURE);
			if(_enclosure.equals(enclosure)){
				contains = true; 
				break;
			}
		}
		return contains;
	}
	//------------------------------------------------------------------------------
	public static void addToRecents(ParseObject recent){
		recent.put(Constants.MODIFIED_TIME, new Date());
		if(!containsRecent(recent)){
			recents.add(recent);
			Collections.sort(recents, comparator);
			recent.pinInBackground(callback);
			System.out.println("Added TO RECENTS: " + recent.getString(Constants.ENCLOSURE));
			System.out.println("***SIZE: " + recents.size() + "***");
		}else{
			String _enclosure = (String) recent.getString(Constants.ENCLOSURE);
			for(ParseObject each : recents){
				String enclosure = (String) each.getString(Constants.ENCLOSURE);
				if(_enclosure.equals(enclosure)){
					each.put(Constants.MODIFIED_TIME, new Date());
					break;
				}
			}
			Collections.sort(recents, comparator);
		}
	}
	//------------------------------------------------------------------------------
	public static List<Episode> getRecentsAsEpisodes(){
		List<Episode> episodes = new ArrayList<Episode>();
		for(ParseObject rec : recents){
			Episode ep = new Episode();
			ep.setTitle(rec.getString(Constants.TITLE));
			ep.setDescription(rec.getString(Constants.DESCRIPTION));
			ep.setGuid(rec.getString(Constants.GUID));
			ep.setEnclosureURL(rec.getString(Constants.ENCLOSURE));
			episodes.add(ep);
		}
		return episodes;
	}
	//------------------------------------------------------------------------------
}
