package com.fasih.podcastr.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Xml;

import com.fasih.podcastr.adapter.EpisodeAdapter;

public class LoadEpisodesTask extends AsyncTask<String, Void, Void> {
	private FragmentManager fm = null;
	private AssetManager asset = null;
	private EpisodeAdapter adapter = null;
	private DataArrivedListener listener = null;
	private List<Episode> episodes = new ArrayList<Episode>();
	// We don't use namespaces
	final String ns = null;
	//------------------------------------------------------------------------------
	public LoadEpisodesTask(FragmentManager fm, 
			                AssetManager asset, 
			                EpisodeAdapter adapter,
			                DataArrivedListener listener){
		this.fm = fm;
		this.asset = asset;
		this.adapter = adapter;
		this.listener = listener;
	}
	//------------------------------------------------------------------------------
	@Override
	protected void onPreExecute(){
		DialogUtil.showLoadingDialog(fm);
	}
	//------------------------------------------------------------------------------
	@Override
	protected Void doInBackground(String... arg0) {
		EpisodeUtil.clearEpisodes();
		EpisodeUtil.setEpisodesFor(arg0[0]);
		String fileName = arg0[1] + ".xml";
		File xmlFile = new File(Constants.DIR_XML, fileName);
		
		if(xmlFile.exists()){
			System.out.println("XML File Exists");
			System.out.println("Stored File: " + xmlFile.getAbsolutePath());
			// If file exists, we will parse the XML from the file
			// instead from over the network
			try {
				FileInputStream fis = new FileInputStream(xmlFile.getAbsolutePath());
				parse(fis);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				EpisodeUtil.setEpisodesFor("");
			}
		}else{
			// Else we first read the file from the network ,save it locally,
			// and then parse the XML
			System.out.println("XML File Does Not Exist");
			System.out.println("Stored File: " + xmlFile.getAbsolutePath());
			try{
				xmlFile.createNewFile();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(readXmlFromUrl(arg0[0],xmlFile)){
				FileInputStream fis;
				try {
					fis = new FileInputStream(xmlFile.getAbsolutePath());
					parse(fis);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					EpisodeUtil.setEpisodesFor("");
				}
			}else{
				// We delete the partially written file
				xmlFile.delete();
				// We were unable to fetch episodes for this 
				// link and hence we will have to parsing later
				EpisodeUtil.setEpisodesFor("");
			}
		}
		return null;
	}
	//------------------------------------------------------------------------------
	private String getFileNameFromUrl(String url){
		int lastIndexOfSlash = url.lastIndexOf("/") + 1; // because we do not want to include the slash
		String fileName = url.substring(lastIndexOfSlash);
		return fileName;
	}
	//------------------------------------------------------------------------------
	private void parse(InputStream in){
		boolean insideItemTag = false;
		String title = null;
		String description = null;
		String guid = null;
		String enclosureURL = null;
		try{
			// Set up the parser
			XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            // Parsing begins with the first tag
            parser.nextTag();
            // Read the feed, like a boss
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
            	if (parser.getEventType() != XmlPullParser.START_TAG) {
            		continue;
                }
            	String tag = parser.getName();
            	if(tag.equals("item")){
            		insideItemTag = true;
            	}
            	if(insideItemTag){
            		if(tag.equals("title")){
            			title = readTitle(parser);
            		}
            		if(tag.equals("description")){
            			description = readDescription(parser);
            		}
            		if(tag.equals("guid")){
            			guid = readGuid(parser);
            		}
            		if(tag.equals("enclosure")){
            			enclosureURL = readEnclosure(parser);
            		}
            		// If we have found both the title and description
            		if(title != null && 
            				description != null && 
            				guid != null &&
            				enclosureURL != null){
            			Episode episode = new Episode();
            			episode.setTitle(title);
            			episode.setDescription(description);
            			episode.setGuid(guid);
            			episode.setEnclosureURL(enclosureURL);
            			// Add to the list
            			episodes.add(episode);
            			// Time for the next iteration
            			// Set them to null
            			title = null;
            			description = null;
            			guid = null;
            			enclosureURL = null;
            		}
            	}
            	// End the parsing if cancel requested
            	if(isCancelled()){
            		break;
            	}
            }
		} catch (Exception e) {
			EpisodeUtil.setEpisodesFor("");
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//------------------------------------------------------------------------------
	private String readGuid(XmlPullParser parser) {
		String _guid = "";
		try{
			parser.require(XmlPullParser.START_TAG, ns, "guid");
		    _guid = readText(parser);
		    parser.require(XmlPullParser.END_TAG, ns, "guid");
		}catch(Exception e){
			e.printStackTrace();
		}
	    return _guid;
	}
	//------------------------------------------------------------------------------
	private boolean readXmlFromUrl(String url, File dest){
		boolean success = false;
		try {
			String line = "";
			FileWriter fw = new FileWriter(dest);
			URL server = new URL(url);
			BufferedReader reader =
					new BufferedReader(new InputStreamReader(server.openStream()));
			BufferedWriter writer = 
					new BufferedWriter(fw);
			
			line = reader.readLine();
			while(line != null){
				writer.write(line);
				if(isCancelled()){
					success = false;
					break;
				}
				line = reader.readLine();
			}
			success = true;
			reader.close();
			writer.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	//------------------------------------------------------------------------------
	private String readTitle(XmlPullParser parser){
		String _title = "";
		try{
			parser.require(XmlPullParser.START_TAG, ns, "title");
		    _title = readText(parser);
		    parser.require(XmlPullParser.END_TAG, ns, "title");
		}catch(Exception e){
			e.printStackTrace();
		}
	    return _title;
	}
	//------------------------------------------------------------------------------
	private String readEnclosure(XmlPullParser parser){
		String _enclosure = "";
		try{
//			parser.require(XmlPullParser.START_TAG, ns, "enclosure");
		    _enclosure = parser.getAttributeValue(ns, "url");
//		    parser.require(XmlPullParser.END_TAG, ns, "enclosure");
		}catch(Exception e){
			e.printStackTrace();
		}
	    return _enclosure;
	}
	//------------------------------------------------------------------------------
	private String readDescription(XmlPullParser parser){
		String _description = "";
		try{
			parser.require(XmlPullParser.START_TAG, ns, "description");
			_description = readText(parser);
		    parser.require(XmlPullParser.END_TAG, ns, "description");
		}catch(Exception e){
			e.printStackTrace();
		}
	    return Jsoup.parse(_description).text();
	}
	//------------------------------------------------------------------------------
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	//------------------------------------------------------------------------------
	@Override
	protected void onPostExecute(Void v){
		DialogUtil.hideLoadingDialog();
		EpisodeUtil.getEpisodes().addAll(episodes);
		adapter.notifyDataSetChanged();
		listener.onDataArrived();
	}
	//------------------------------------------------------------------------------
	public static interface DataArrivedListener{
		void onDataArrived();
	}
	//------------------------------------------------------------------------------
}
