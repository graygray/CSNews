package com.graylin.csnews;

import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class PlayVideoService extends IntentService {

	public MediaPlayer mp = null;
	public static boolean getBroadcastToPlay = false;
	public static boolean getBroadcastToPause = false;
	public static boolean getBroadcastToNext = false;
	public static boolean getBroadcastToPreviious = false;
	public static boolean getBroadcastToFFaward = false;
	public static boolean getBroadcastToRewind = false;
	public boolean isMediaPlayerRun = false;
	public boolean isReplay = false;
	public int maxPlayCounter = 0;
	public int playConunter = 0;
	
	public PlayVideoService() {
		super("PlayVideoService");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent arg0) {
		
		if (MainActivity.isDebug) {
			Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "");
		}
		
		// set to foreground service
		Notification note = new Notification(R.drawable.ic_launcher,
				"10minNews", System.currentTimeMillis());
		Intent i = new Intent(this, PlayActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0,
                        i, 0);
		note.setLatestEventInfo(this, "10minNews",
				"Service is running", pi);
		note.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(1337, note);
		
		// initial variables
		isReplay = false;
		playConunter = 0;
		if ( MainActivity.bgStopTimes == 6) {
			maxPlayCounter = 99;
		} else {
			maxPlayCounter = MainActivity.bgStopTimes+1;
		}

		playVideo();
		
		// end while will go to onDestroy()
		while (!PlayActivity.isStopService ) {
			
			if (getBroadcastToPlay && !isMediaPlayerRun ) {
				getBroadcastToPlay = false;
				playVideo();
			}
			
			if (getBroadcastToPause && isMediaPlayerRun) {
				getBroadcastToPause = false;
				stopVideo();
			}
			
			if ( mp != null ) {
				
				if (!mp.isPlaying()) {
					stopVideo();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (MainActivity.isDebug) {
						Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "is not playing");
						Log.e("gray", "PlayVideoService.java:onHandleIntent, playConunter:" + playConunter);
						Log.e("gray", "PlayVideoService.java:onHandleIntent, maxPlayCounter:" + maxPlayCounter);
					}
					
					if (playConunter < maxPlayCounter) {
						isReplay = true;
						playVideo();
					} else {
						break;
					}
				}
			}
			
			if ( mp != null) {
				PlayActivity.stopPosition = mp.getCurrentPosition();
				
				if (getBroadcastToPreviious || getBroadcastToRewind) {
					getBroadcastToPreviious = false;
					getBroadcastToRewind = false;
					PlayActivity.stopPosition -= MainActivity.swipeTime*1000;
					mp.seekTo(PlayActivity.stopPosition);
				}
				// don't do fast forward
//				else if (getBroadcastToNext || getBroadcastToFFaward) {
//					getBroadcastToNext = false;
//					getBroadcastToFFaward = false;
//					PlayActivity.stopPosition += 2000;
//					mp.seekTo(PlayActivity.stopPosition);
//				} 
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void playVideo() {
		
		if (MainActivity.isDebug) {
			Log.e("gray", "PlayVideoService.java:playVideo, " + "");
		}
		mp = new MediaPlayer();
		mp.setLooping(false);
		playConunter++;
		
		try {
			
			if (isReplay) {
				PlayActivity.stopPosition = 0;
				isReplay = false;
			}
			mp.setDataSource(PlayActivity.cnnVideoPath);
			mp.prepare();
			mp.seekTo(PlayActivity.stopPosition);
			mp.start();
			
			PlayActivity.isVideoPlaying = true;
			isMediaPlayerRun = true;
			
		} catch (IllegalArgumentException e) {
			Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "IllegalArgumentException");
			e.printStackTrace();
		} catch (SecurityException e) {
			Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "SecurityException");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "IllegalStateException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("gray", "PlayVideoService.java:onHandleIntent, " + "IOException");
			e.printStackTrace();
		}
	}
	
	public void stopVideo() {
		
		if (MainActivity.isDebug) {
			Log.e("gray", "PlayVideoService.java:stopVideo, " + "");
		}
		if (mp !=  null) {
			
			mp.stop();
			mp.release();
			mp = null;
			
			PlayActivity.isVideoPlaying = false;
			isMediaPlayerRun = false;
		}
	}
	
	@Override
    public void onDestroy() {
		
		if (MainActivity.isDebug) {
			Log.e("gray", "PlayVideoService.java:onDestroy, " + "");
		}
		stopVideo();
    }
	
}

