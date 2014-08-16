package com.fasih.podcastr.util;

public class Episode {
	private String title;
	private String description;
	private String guid;
	private String enclosureURL;
	//------------------------------------------------------------------------------
	public String getTitle() {
		return title;
	}
	//------------------------------------------------------------------------------
	public void setTitle(String title) {
		this.title = title;
	}
	//------------------------------------------------------------------------------
	public String getDescription() {
		return description;
	}
	//------------------------------------------------------------------------------
	public void setDescription(String description) {
		this.description = description;
	}
	//------------------------------------------------------------------------------
	public String getGuid() {
		return guid;
	}
	//------------------------------------------------------------------------------
	public void setGuid(String guid) {
		this.guid = guid;
	}
	//------------------------------------------------------------------------------
	public String getEnclosureURL() {
		return enclosureURL;
	}
	//------------------------------------------------------------------------------
	public void setEnclosureURL(String enclosureURL) {
		this.enclosureURL = enclosureURL;
	}
}
