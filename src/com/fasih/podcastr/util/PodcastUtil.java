package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.List;

public class PodcastUtil {
	private static List<Podcast> podcasts = new ArrayList<Podcast>();
	private static List<CharSequence> categories = new ArrayList<CharSequence>();
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
}
