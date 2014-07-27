package com.fasih.podcastr.util;

import java.util.ArrayList;
import java.util.List;

public class Response {
	private List<Podcast> podcasts = new ArrayList<Podcast>();
	
	public List<Podcast> getPodcasts() {
		return podcasts;
	}
	public void setPodcasts(List<Podcast> podcasts) {
		this.podcasts = podcasts;
	}
}
