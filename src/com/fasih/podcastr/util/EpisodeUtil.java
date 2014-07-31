package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.List;

public class EpisodeUtil {
	// Used to know for which podcast the episodes have been loaded
	// This is to prevent repeated loading of episodes
	private static String episodesFor = "";
	private static List<Episode> episodes = new ArrayList<Episode>();
	//------------------------------------------------------------------------------
	public static void setEpisodesFor(String url){
		episodesFor = url;
	}
	//------------------------------------------------------------------------------
	public static String getEpisodesFor(){
		return episodesFor;
	}
	//------------------------------------------------------------------------------
	public static void addEpisode(Episode episode){
		episodes.add(episode);
	}
	//------------------------------------------------------------------------------
	public static void clearEpisodes(){
		episodes.clear();
	}
	//------------------------------------------------------------------------------
	public static List<Episode> getEpisodes(){
		return episodes;
	}
	//------------------------------------------------------------------------------
}
