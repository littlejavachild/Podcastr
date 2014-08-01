package com.fasih.podcastr.util;

import java.util.Comparator;

public class AlphaComparator implements Comparator<Podcast>{

	@Override
	public int compare(Podcast p1, Podcast p2) {
		return p1.getTitle().compareTo(p2.getTitle());
	}
}
