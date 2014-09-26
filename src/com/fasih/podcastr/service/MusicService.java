package com.fasih.podcastr.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.PrefUtils;

public class MusicService extends Service implements 
										  MediaPlayer.OnCompletionListener{
	
	final static String NAME = "MusicService";
	private NotificationManager mgr;
	private final int NOTIFICATION_ID = 6969;
	private RemoteViews rm;
	
	// Constants to know what type of request was made
	final String REQUEST_CODE = "play_pause";
	final int PAUSE = 0;
	final int DISMISS = 1;
	
	// Bitmap Images
	Bitmap play;
	Bitmap pause;
	
	private int request;
	private String title;
	
	// 0 = stopped
	// 1 = running
	public static int status = 0;
	
	//------------------------------------------------------------------------------
	@Override
	public void onCreate(){
		Log.d(NAME, "Starting Music Service in BG");
		super.onCreate();
		showMediaPlayerNotification();
		status = 1;
	}
	//------------------------------------------------------------------------------
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d(NAME, "Making Request To Music Service in BG");
		MediaPlayer player = PodcastrApplication.newInstance().getMediaPlayer();
		player.setOnCompletionListener(this);
		if(intent != null){
			request = intent.getIntExtra(REQUEST_CODE, 0);
			title = intent.getStringExtra(Constants.TITLE);
			updateNotification();
			if(request == PAUSE){
				// Toggle Play and Pause
				if(player.isPlaying()){
					player.pause();
				}else{
					player.start();
				}
			}else if(request == DISMISS){
				hideNotification();
				stopSelf();
			}
		}
		return START_STICKY;
	}
	//------------------------------------------------------------------------------
	@Override 
    public void onDestroy() {
		status = 0;
		super.onDestroy();
		Log.d(NAME, "Stopping Music Service in BG");
		MediaPlayer player = PodcastrApplication.newInstance().getMediaPlayer();
		player.reset();
		// Cancel the persistent notification. 
		hideNotification();
	}
	//------------------------------------------------------------------------------
	private void showMediaPlayerNotification(){
		if(rm == null){
			rm = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notif_media_player);
		}
		
		Intent pauseIntent = new Intent(getApplicationContext(), MusicService.class);
		Intent dismissIntent = new Intent(getApplicationContext(), MusicService.class);
		
		pauseIntent.putExtra(REQUEST_CODE, PAUSE);
		dismissIntent.putExtra(REQUEST_CODE, DISMISS);
		
		PendingIntent pause = PendingIntent.getService(getApplicationContext(), PAUSE, pauseIntent, 0);
		PendingIntent dismiss = PendingIntent.getService(getApplicationContext(), DISMISS, dismissIntent, 0);
		
		rm.setOnClickPendingIntent(R.id.pause, pause);
		rm.setOnClickPendingIntent(R.id.dismiss, dismiss);
		
		Notification notif =  new NotificationCompat.Builder(getApplicationContext())
						         .setOngoing(true)
						         .setSmallIcon(R.drawable.ic_launcher)
						         .setContent(rm)
						         .build();
		
		if(mgr == null){
			mgr = (NotificationManager) getApplicationContext()
					  .getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mgr.notify(NOTIFICATION_ID, notif);
	}
	//------------------------------------------------------------------------------
	private void hideNotification(){
		if(mgr == null){
			mgr = (NotificationManager) getApplicationContext()
	   			    .getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mgr.cancel(NOTIFICATION_ID);
	}
	//------------------------------------------------------------------------------
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	//------------------------------------------------------------------------------
	private void updateNotification(){
		NotificationManager mgr = (NotificationManager) getApplicationContext()
				  .getSystemService(Context.NOTIFICATION_SERVICE);
		MediaPlayer player = PodcastrApplication.newInstance().getMediaPlayer();
		if(title != null){
			rm.setTextViewText(R.id.episode_title, title);
		}
		if(player.isPlaying()){
			rm.setImageViewResource(R.id.pause, R.drawable.ic_action_play);
		}else{
			rm.setImageViewResource(R.id.pause, R.drawable.ic_action_pause);
		}
		
		Notification notif =  new NotificationCompat.Builder(getApplicationContext())
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContent(rm)
        .build();
		
		mgr.notify(NOTIFICATION_ID, notif);
	}
	//------------------------------------------------------------------------------
	@Override
	public void onCompletion(MediaPlayer mp) {
		rm.setImageViewResource(R.id.pause, R.drawable.ic_action_play);
		
		Notification notif =  new NotificationCompat.Builder(getApplicationContext())
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContent(rm)
        .build();
		
		mgr.notify(NOTIFICATION_ID, notif);
		
		PrefUtils.setSeekTo(getApplicationContext(), 0);
	}
	//------------------------------------------------------------------------------
}
