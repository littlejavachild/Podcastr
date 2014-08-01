package com.fasih.podcastr.fragment;

import java.io.IOException;

import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.adapter.EpisodeAdapter;
import com.fasih.podcastr.util.ActionBarUtil;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.Episode;
import com.fasih.podcastr.util.EpisodeUtil;
import com.fasih.podcastr.util.LoadEpisodesTask;
import com.fasih.podcastr.util.Podcast;
import com.fasih.podcastr.util.PodcastUtil;
import com.fasih.podcastr.util.PrefUtils;
import com.fasih.podcastr.view.VideoControllerView;

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
    
    private LoadEpisodesTask loadEpisodes;
    private EpisodeAdapter adapter = new EpisodeAdapter();
    
    private int videoIndex = 0;
    
  //------------------------------------------------------------------------------
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		ActionBarUtil.hideActionBar(getActivity().getActionBar());
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
		setTouchEventListener();
		if(listOfEpisodes != null){
			setEpisodesListAdapter();
			setEpisodeOnItemClickListener();
		}
		retrieveArguments();
		getEpisodes();
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
		PrefUtils.setVideoIndex(getActivity(), videoIndex);
	}
	//------------------------------------------------------------------------------
	private void init(){
		
		videoSurface = (SurfaceView) contentView.findViewById(R.id.videoSurface);
    	SurfaceHolder videoHolder = videoSurface.getHolder();
    	videoHolder.addCallback(this);
    	player = new MediaPlayer();
    	player.setOnPreparedListener(this); 
    	
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
		videoIndex = position;
		if(EpisodeUtil.getEpisodes().size() == 0){
			return;
		}
		Episode episode = EpisodeUtil.getEpisodes().get(position);
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
		}
		
		System.out.println(source);
		
		try {
			player.stop();
			player.reset();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC); 
			player.setDataSource(getActivity(), Uri.parse(source));
			player.prepareAsync();
	    } catch (IllegalArgumentException e) { e.printStackTrace(); }
	      catch (SecurityException e) { e.printStackTrace(); } 
		  catch (IllegalStateException e) { e.printStackTrace(); } 
		  catch (IOException e) { e.printStackTrace(); } 
	}
	//------------------------------------------------------------------------------
	@Override
	public void onDetach(){
		super.onDetach();
		player.stop();
		player.reset();
		// onDetach also, everything is reset to 0
		PrefUtils.setSeekTo(getActivity(), 0);
		PrefUtils.setVideoIndex(getActivity(), 0);
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
}
