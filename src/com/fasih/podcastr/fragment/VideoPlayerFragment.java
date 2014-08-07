package com.fasih.podcastr.fragment;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.adapter.EpisodeAdapter;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.Episode;
import com.fasih.podcastr.util.EpisodeUtil;
import com.fasih.podcastr.util.FavoriteUtil;
import com.fasih.podcastr.util.LoadEpisodesTask;
import com.fasih.podcastr.util.Podcast;
import com.fasih.podcastr.util.PodcastUtil;
import com.fasih.podcastr.util.PrefUtils;
import com.fasih.podcastr.view.VideoControllerView;
import com.parse.ParseObject;

public class VideoPlayerFragment extends Fragment implements 
													SurfaceHolder.Callback, 
													MediaPlayer.OnPreparedListener, 
													VideoControllerView.MediaPlayerControl,
													LoadEpisodesTask.DataArrivedListener{
	
	private View contentView = null;
	private Typeface roboto = PodcastrApplication.newInstance().getTypeface();
	
	private static VideoPlayerFragment singleton = null;
	private Bundle arguments = null;
	private Podcast podcast = null;
	
	private SurfaceView videoSurface;
    private MediaPlayer player;
    private VideoControllerView controller;
    private ListView listOfEpisodes;
    private TextView episodeTitle;
    private TextView episodeDescription;
    
    private ImageButton download;
    private ImageButton addToFavorites;
    private ImageButton share;
    private ImageButton refresh;
    private TextView podcastTitle;
    
    private LoadEpisodesTask loadEpisodes;
    private EpisodeAdapter adapter = new EpisodeAdapter();
    
  //------------------------------------------------------------------------------
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		View view = null;
		view = inflater.inflate(R.layout.fragment_video_player, container, false);
		contentView = view;
		return view;
	}
	//------------------------------------------------------------------------------
	public void onResume(){
		super.onResume();
		// get references
		init();
		// Because landscape mode has video player only.
		// Hence, ListView will be null after init()
		if(listOfEpisodes != null){
			setEpisodesListAdapter();
			setEpisodeOnItemClickListener();
		}
		retrieveArguments();
		getEpisodes();
		setTouchEventListener();
		setDownloadClickListener();
		setAddToFavoritesClickListener();
		setShareOnClickListener();
		setRefreshOnClickListener();
		setPodcastTitle();
	}
	//------------------------------------------------------------------------------
	@Override
	public void onStop(){
		super.onStop();
		loadEpisodes.cancel(true);
		player.stop();
		player.reset();
		// onStop, we keep the snapshot state. Not a complete reset
		PrefUtils.setSeekTo(getActivity(), player.getCurrentPosition());
		PrefUtils.setVideoIndex(getActivity(), PrefUtils.getVideoIndex(getActivity()));
	}
	//------------------------------------------------------------------------------
	private void init(){
		
		videoSurface = (SurfaceView) contentView.findViewById(R.id.videoSurface);
    	SurfaceHolder videoHolder = videoSurface.getHolder();
    	videoHolder.addCallback(this);
    	player = new MediaPlayer();
    	player.setOnPreparedListener(this); 
    	
    	// Because these are in the ActionBar,
    	// we get them from the Activity
    	download = (ImageButton) getActivity().findViewById(R.id.download);
    	addToFavorites = (ImageButton) getActivity().findViewById(R.id.add_to_favorite);
    	share = (ImageButton) getActivity().findViewById(R.id.share);
    	refresh = (ImageButton) getActivity().findViewById(R.id.refresh);
    	podcastTitle = (TextView) getActivity().findViewById(R.id.podcast_title);
    	podcastTitle.setTypeface(roboto, Typeface.BOLD);
    	
    	controller = new VideoControllerView(getActivity());
    	listOfEpisodes = (ListView) contentView.findViewById(R.id.listOfEpisodes);
    	episodeTitle = (TextView) contentView.findViewById(R.id.episode_title);
    	episodeDescription = (TextView) contentView.findViewById(R.id.episode_description);
    	// Because landscape mode only has a video player.
		// If one of the TextView is null, all are null
    	if(episodeTitle != null){
    		episodeTitle.setTypeface(roboto, Typeface.BOLD);
        	episodeDescription.setTypeface(roboto, Typeface.BOLD);
    	}
	}
	//------------------------------------------------------------------------------
	private void retrieveArguments(){
		arguments = getArguments();
		int index = arguments.getInt(Constants.PODCAST_INDEX);
		podcast = PodcastUtil.getPodcasts().get(index);
	}
	//------------------------------------------------------------------------------
	private void getEpisodes(){
		loadEpisodes = new LoadEpisodesTask(getActivity().getSupportFragmentManager(),
				              getActivity().getAssets(), 
				              adapter,
				              this);
		loadEpisodes.execute(podcast.getFeed());
	}
	//------------------------------------------------------------------------------
	public static VideoPlayerFragment newInstance(){
		if(singleton == null){
			singleton = new VideoPlayerFragment();
		}
		return singleton;
	}
	//------------------------------------------------------------------------------
//	  Works only in Activities
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        controller.show();
//        return false;
//    }
	//------------------------------------------------------------------------------
	private void setTouchEventListener(){
		FrameLayout videoSurfaceContainer = (FrameLayout) contentView.findViewById(R.id.videoSurfaceContainer);
		videoSurfaceContainer.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				controller.show();
				return true;
			}
		});
	}
	//------------------------------------------------------------------------------
	private void setEpisodesListAdapter(){
		listOfEpisodes.setAdapter(adapter);
	}
	//------------------------------------------------------------------------------
	private void playEpisode(int position){
		if(EpisodeUtil.getEpisodes().size() == 0){
			return;
		}
		
		// Keep the screen on.
		// This is a video app. Screen stays on at all times.
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Episode episode = EpisodeUtil.getEpisodes().get(PrefUtils.getVideoIndex(getActivity()));
		String title = episode.getTitle();
		String description = episode.getDescription();
		String guid = episode.getGuid();
		String enclosure = episode.getEnclosureURL();
		
		String source = guid;
		
		if(!enclosure.isEmpty()){
			source = enclosure;
		}
		
		// Because landscape mode only has a video player.
		// If one of the TextView is null, all are null
		if(episodeTitle != null){
			episodeTitle.setText(title);
			episodeDescription.setText(description);
			episodeTitle.setSelected(true);
		}
		
		try {
			player.stop();
			player.reset();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			
			if(existsLocally(getFileNameFromEnclosure(enclosure))){
		        System.out.println("Exists Locally");
		        String fileName = getFileNameFromEnclosure(enclosure);
		        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
		        File localFile = new File(path,fileName);
		        Uri localSource = Uri.parse(localFile.getAbsolutePath());
		        player.setDataSource(getActivity(), localSource);
				player.prepare();
			}else{
		        System.out.println("Does Not Exist Locally");
		        player.setDataSource(getActivity(), Uri.parse(source));
				player.prepareAsync();
			}
	    } catch (IllegalArgumentException e) { e.printStackTrace(); }
	      catch (SecurityException e) { e.printStackTrace(); } 
		  catch (IllegalStateException e) { e.printStackTrace(); } 
		  catch (IOException e) { e.printStackTrace(); }
		
		setInitialButtonColor();
	}
	//------------------------------------------------------------------------------
	@Override
	public void onDetach(){
		super.onDetach();
		player.stop();
		player.reset();
	}
	//------------------------------------------------------------------------------
    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback
    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
    	player.start();
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) contentView.findViewById(R.id.videoSurfaceContainer));
    }
    // End MediaPlayer.OnPreparedListener
    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }
    // End VideoMediaController.MediaPlayerControl
    //------------------------------------------------------------------------------
	@Override
	public void onDataArrived() {
		adapter.notifyDataSetChanged();
		if(adapter.getCount() > 0){
			playEpisode(PrefUtils.getVideoIndex(getActivity()));
			// Because listOfEpisodes is not available in
			// landscape mode
			if(listOfEpisodes != null){
				listOfEpisodes.smoothScrollToPosition(PrefUtils.getVideoIndex(getActivity()));
			}
		}
	}
	//------------------------------------------------------------------------------
	private void setEpisodeOnItemClickListener(){
		listOfEpisodes.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long id) {
				// On clicking a new episode, everything is reset
				PrefUtils.setVideoIndex(getActivity(), index);
				PrefUtils.setSeekTo(getActivity(), 0);
				// Then we play the desired episode
				playEpisode(index);
			}
		});
	}
	//------------------------------------------------------------------------------
	private void setDownloadClickListener(){
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Episode episode = EpisodeUtil.getEpisodes().get(PrefUtils.getVideoIndex(getActivity()));
				String uri = episode.getEnclosureURL();
				if(uri == null || uri.isEmpty()){
					uri = episode.getGuid();
				}
				final String _uri = uri; // cheating :P
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override 
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            download(_uri);
				            break; 
				 
				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked, ignore
				            break; 
				        } 
				    } 
				};
				buildAndShowDownloadYesNoDialog(dialogClickListener);
			}
		});
	}
	//------------------------------------------------------------------------------
	private void download(String uri){
		String fileName = uri.substring(uri.lastIndexOf("/") + 1);
		DownloadManager.Request r = new DownloadManager.Request(Uri.parse(uri));
		
		// This put the download in the same Download dir the browser uses 
		r.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS,fileName);
		 
		// TODO delete later
		System.out.println("DOWNLOAD LOCATION: " + 
				new File(Environment.DIRECTORY_PODCASTS,fileName).getAbsolutePath());
		
		// When downloading music and videos they will be listed in the player 
		// (Seems to be available since Honeycomb only) 
		r.allowScanningByMediaScanner();
		
		// Notify user when download is completed 
		// (Seems to be available since Honeycomb only)
		r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		 
		// Start download 
		DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		
		dm.enqueue(r);
	}
	//------------------------------------------------------------------------------
	private boolean isWifiConnected(){
		ConnectivityManager m = (ConnectivityManager) 
				getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = m.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi.isConnected();
	}
	//------------------------------------------------------------------------------
	private boolean isMobileDataConnected(){
		ConnectivityManager m = (ConnectivityManager) 
				getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = m.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return net.isConnectedOrConnecting();
	}
	//------------------------------------------------------------------------------
	private void buildAndShowDownloadYesNoDialog(DialogInterface.OnClickListener listener){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		Resources res = getActivity().getResources();
		String proceed = res.getString(R.string.proceed_to_download);
		String yes = res.getString(R.string.yes_download);
		String no = res.getString(R.string.no_download);
		builder.setMessage(proceed);
		builder.setPositiveButton(yes, listener);
		builder.setNegativeButton(no, listener);
		builder.show();
	}
	//------------------------------------------------------------------------------
	private void setPodcastTitle(){
		podcastTitle.setText(podcast.getTitle());
	}
	//------------------------------------------------------------------------------
	private void setAddToFavoritesClickListener(){
		addToFavorites.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Episode episode = EpisodeUtil.getEpisodes().get(PrefUtils.getVideoIndex(getActivity()));
				ParseObject favorite = new ParseObject(Constants.FAVORITES_CLASS_NAME);
				favorite.put(Constants.PODCAST, podcast.getFeed());
				favorite.put(Constants.EPISODE, episode.getEnclosureURL());
				if(FavoriteUtil.containsFavorite(favorite)){
					// If it contains it already,
					// we remove
					FavoriteUtil.removeFromFavorites(favorite);
					// and set the logo to normal
					addToFavorites.setImageResource(R.drawable.ic_action_important);
				}else{
					// else, we add
					FavoriteUtil.addToFavorites(favorite);
					// and set the logo to highlighted
					addToFavorites.setImageResource(R.drawable.ic_action_important_yellow);
				}
			}
		});
	}
	//------------------------------------------------------------------------------
	private void setInitialButtonColor(){
		if(EpisodeUtil.getEpisodes().size() == 0 ){
			return;
		}
		Episode episode = EpisodeUtil.getEpisodes().get(PrefUtils.getVideoIndex(getActivity()));
		ParseObject favorite = new ParseObject(Constants.FAVORITES_CLASS_NAME);
		favorite.put(Constants.PODCAST, podcast.getFeed());
		favorite.put(Constants.EPISODE, episode.getEnclosureURL());
		if(FavoriteUtil.containsFavorite(favorite)){
			// If it is already in favorites,
			// we show the highlighted logo
			addToFavorites.setImageResource(R.drawable.ic_action_important_yellow);
		}else{
			// else we show the normal logo
			addToFavorites.setImageResource(R.drawable.ic_action_important);
		}
	}
	//------------------------------------------------------------------------------
	private void setShareOnClickListener(){
		share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareEpisode();
			}
		});
	}
	//------------------------------------------------------------------------------
	private void setRefreshOnClickListener(){
		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override 
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            refresh();
				            break; 
				 
				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked, ignore
				            break; 
				        } 
				    } 
				};
				
				buildAndShowRefreshYesNoDialog(dialogClickListener);
			}
		});
	}
	//------------------------------------------------------------------------------
	private void shareEpisode(){
		Episode episode = EpisodeUtil.getEpisodes().get(PrefUtils.getVideoIndex(getActivity()));
		String message = "Watch " + episode.getTitle() + " [" + episode.getEnclosureURL() + "] " + "on Podcastr";
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(Intent.EXTRA_TEXT, message);
		try{
			startActivity(Intent.createChooser(sharingIntent, "Share Using"));
		}catch(ActivityNotFoundException e){
			Toast.makeText(getActivity(), 
					"Unable To Share Event. No Suitable Application Found", 
					Toast.LENGTH_SHORT).show();
		}
	}
	//------------------------------------------------------------------------------
	private void refresh(){
		// Step 1. Delete the locally stored XML file
		String xmlFile = getXmlFileFromFeedUrl();
		File f = new File(Constants.DIR_XML, xmlFile);
		if(f.delete())
			System.out.println("Deleted: " + f.getAbsolutePath());
		else
			System.out.println("Could Not Delete: " + f.getAbsolutePath());
		// Step 2. Execute the LoadEpisodesTask
		getEpisodes();
	}
	//------------------------------------------------------------------------------
	private void buildAndShowRefreshYesNoDialog(DialogInterface.OnClickListener listener){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		Resources res = getActivity().getResources();
		String proceed = res.getString(R.string.check_for_update);
		String yes = res.getString(R.string.yes_check_for_update);
		String no = res.getString(R.string.no_check_for_updates);
		builder.setMessage(proceed);
		builder.setPositiveButton(yes, listener);
		builder.setNegativeButton(no, listener);
		builder.show();
	}
	//------------------------------------------------------------------------------
	private String getFileNameFromEnclosure(String enclosureUrl){
		int indexOfSlash = enclosureUrl.lastIndexOf("/");
		String fileName = enclosureUrl.substring(indexOfSlash + 1);
		return fileName;
	}
	//------------------------------------------------------------------------------
	private boolean existsLocally(String fileName){
		File path = Environment.getExternalStoragePublicDirectory(
		        Environment.DIRECTORY_PODCASTS);
		File f = new File(path, fileName);
		System.out.println("CHECKED LOCATION: " + f.getAbsolutePath());
		return f.exists();
	}
	//------------------------------------------------------------------------------
	private String getXmlFileFromFeedUrl(){
		String feed = podcast.getFeed();
		int indexOfSlash = feed.lastIndexOf("/");
		String fileName = feed.substring(indexOfSlash) + ".xml";
		return fileName;
	}
	//------------------------------------------------------------------------------
}