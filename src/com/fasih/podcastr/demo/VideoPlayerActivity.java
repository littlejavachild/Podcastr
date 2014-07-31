package com.fasih.podcastr.demo;

import java.io.IOException;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.fasih.podcastr.R;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.PrefUtils;
import com.fasih.podcastr.view.VideoControllerView;

public class VideoPlayerActivity extends Activity 
								 implements 
								 		SurfaceHolder.Callback, 
								 		MediaPlayer.OnPreparedListener, 
								 		VideoControllerView.MediaPlayerControl {
	//------------------------------------------------------------------------------
	private SurfaceView videoSurface;
    private MediaPlayer player;
    private VideoControllerView controller;
    private int seekTo = 0;
    private String SEEK_TO_KEY = "seek_to_key";
    //------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_player);
        seekTo = PrefUtils.getSeekTo(getApplicationContext());
        init();
    }
    //------------------------------------------------------------------------------
    @Override
    public void onResume(){
    	super.onResume();
    	playVideo();
    }
    //------------------------------------------------------------------------------
    private void init(){
    	videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
    	SurfaceHolder videoHolder = videoSurface.getHolder();
    	videoHolder.addCallback(this);
    	if(player == null){
    		player = new MediaPlayer();
    	}
    	controller = new VideoControllerView(this);
    }
    //------------------------------------------------------------------------------
    private void playVideo(){
    	try {
    		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	    player.setDataSource(this, Uri.parse("http://www.podtrac.com/pts/redirect.mp4/twit.cachefly.net/video/aaa/aaa0171/aaa0171_h264b_640x368_256.mp4"));
    	    player.setOnPreparedListener(this);
    	    player.prepareAsync();
    	    player.start();
        	player.seekTo(seekTo);
    	} catch (IllegalArgumentException e) {
    	    e.printStackTrace();
    	} catch (SecurityException e) {
    	    e.printStackTrace();
    	} catch (IllegalStateException e) {
    	    e.printStackTrace();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    }
    //------------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }
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
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
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
    
    @Override
    public void onSaveInstanceState(Bundle outState){
    	seekTo = player.getCurrentPosition();
    	player.pause();
    	PrefUtils.setSeekTo(getApplicationContext(), seekTo);
    	outState.putInt(SEEK_TO_KEY, seekTo);
    }
}
