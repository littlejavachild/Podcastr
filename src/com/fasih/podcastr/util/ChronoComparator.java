package com.fasih.podcastr.util;

import java.util.Comparator;

import com.parse.ParseObject;

public class ChronoComparator implements Comparator<ParseObject> {
	@Override
	public int compare(ParseObject obj1, ParseObject obj2) {
		return -1 * obj1.getDate(Constants.MODIFIED_TIME).compareTo(obj2.getDate(Constants.MODIFIED_TIME));
	}
}
