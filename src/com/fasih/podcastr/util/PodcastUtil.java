package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.List;

public class PodcastUtil {
	private static List<Podcast> podcasts = new ArrayList<Podcast>();
	private static List<CharSequence> categories = new ArrayList<CharSequence>();
	// Used to hold all those podcasts that do not match a given search text
	private static List<Podcast> exclude = new ArrayList<Podcast>();
	//------------------------------------------------------------------------------
	/**
	 * Used to add a list of retrieved podcasts after parsing the JSON
	 * @param newPodcasts The newly fetched podcasts
	 */
	public static void addPodcasts(List<Podcast> newPodcasts){
		if(podcasts.size() == 0){
			podcasts.addAll(newPodcasts);
		}
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to retrieve the list of podcasts
	 * @return The list of podcasts
	 */
	public static List<Podcast> getPodcasts(){
		return podcasts;
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to add a category to a list of categories
	 * @param category The newly fetched category
	 */
	public static void addCategory(String category){
		if(!categories.contains(category)){
			categories.add(category);
		}
	}
	//------------------------------------------------------------------------------
	public static List<CharSequence> getCategories(){
		return categories;
	}
	//------------------------------------------------------------------------------
	public static void sortByCategory(String category){
		System.out.println("Sorting by: " + category);
		List<Podcast> matches = new ArrayList<Podcast>();
		for(int i = podcasts.size()-1; i >= 0; i--){
			Podcast podcast = podcasts.get(i);
			// If a podcast matches the given category
			if(podcast.getCategory().trim().equals(category)){
				// We remove it from the main list
				podcasts.remove(podcast);
				// and add it to matches list
				matches.add(podcast);
				System.out.println(podcast.getTitle() + " is " + category);
			}
		}
		// We then add the matches to the front of the list
		podcasts.addAll(0, matches);
	}
	//------------------------------------------------------------------------------
	/**
	 * used to search for podcasts that match a given criteria
	 * @param text
	 */
	public static void searchFor(String text){
		restoreAll();
		text = text.toLowerCase();
		// We first add the results of the previosu searches
		// to the main list of podcasts
		podcasts.addAll(exclude);
		// Then we look for "excludes"
		for(int i = podcasts.size()-1; i >= 0; i--){
			Podcast podcast = podcasts.get(i);
			if(podcast.getTitle().toLowerCase().contains(text) ||
			   podcast.getDescription().toLowerCase().contains(text) ||
			   podcast.getHost().toLowerCase().contains(text) ||
			   podcast.getCategory().toLowerCase().contains(text)){
				// IGNORE
			}else{
				podcasts.remove(podcast);
				if(!exclude.contains(podcast))
					exclude.add(podcast);
			}
		}
	}
	//------------------------------------------------------------------------------
	public static void restoreAll(){
		podcasts.addAll(exclude);
		exclude.clear();
	}
}
