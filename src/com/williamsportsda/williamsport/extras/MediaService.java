package com.williamsportsda.williamsport.extras;



import com.williamsportsda.williamsport.MediaPlayerActivity;
import com.williamsportsda.williamsport.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

//http://www.speakingcode.com/2012/02/22/creating-a-streaming-audio-app-for-android-with-android-media-mediaplayer-android-media-audiomanager-and-android-app-service/

public class MediaService extends Service implements OnBufferingUpdateListener, OnInfoListener, OnCompletionListener, OnPreparedListener, OnErrorListener,
		OnAudioFocusChangeListener, OnSeekCompleteListener {

	public static final int NO_SEEK_POSITION = -1;

	private static final int MP_NOTIFICATION_ID = 100;
	private WifiLock wifiLock;

	private String title = "";

	private StatefulMediaPlayer mMediaPlayer;
	private final Binder mBinder;
	private IMediaPlayerServiceClient mClient;

	private AudioManager audioManager;

	Boolean requestPlayerPaused = false;

	public class MediaPlayerBinder extends Binder {
		/**
		 * Returns the instance of this service for a client to make method
		 * calls on it.
		 * 
		 * @return the instance of this service.
		 */
		public MediaService getService() {
			return MediaService.this;
		}
	}

	public StatefulMediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}

	public void initializePlayer(MediaItem mediaItem) {
		mClient.onInitializePlayerStart("Connecting...");

		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
		wifiLock.acquire();

		if (mediaItem.Title != null)
			title = mediaItem.Title;

		try {
			mMediaPlayer.setDataSource(mediaItem.Url);
		} catch (Exception e) {
			Log.e("MediaPlayerService", "error setting data source");
			mMediaPlayer.setState(StatefulMediaPlayer.MPStates.ERROR);
		}

		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnInfoListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
		mMediaPlayer.prepareAsync();

	}

	public void setClient(IMediaPlayerServiceClient client) {
		this.mClient = client;
	}

	public MediaService() {
		mMediaPlayer = new StatefulMediaPlayer();
		mBinder = new MediaPlayerBinder();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer player, int percent) {

	}

	@Override
	public boolean onError(MediaPlayer player, int what, int extra) {
		// if (arg1 == MediaPlayer.MEDIA_ERROR_SERVER_DIED)
		// else if (arg1 == MediaPlayer.MEDIA_ERROR_UNKNOWN)

		mMediaPlayer.reset();
		mClient.onError();
		return true;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return false;
	}

	/** Called when MediaPlayer is ready */
	public void onPrepared(MediaPlayer player) {

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// Toast, audio failure.
		} else {
			if (!mMediaPlayer.isPlaying())
				startMediaPlayer();
			mMediaPlayer.setVolume(1.0f, 1.0f);
		}

		// setup ui after everything is prepared.
		mClient.onInitializePlayerSuccess();
	}

	public int getStreamMaxVolume() {
		return audioManager != null ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 1;
	}

	public int getStreamVolume() {
		return audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0;
	}

	public void setStreamVolume(int volume) {
		if (audioManager != null)
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		try {
			if (mMediaPlayer != null)
				mMediaPlayer.release();
		} catch (Exception e) {
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (wifiLock.isHeld())
			wifiLock.release();

		// set play button to paused.
		mClient.onPlayerPause();
		stopForeground(true);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			if (mMediaPlayer == null)
				startMediaPlayer();
			break;

		case AudioManager.AUDIOFOCUS_LOSS: // can happen if another app plays
											// audio.
			// Lost focus for an unbounded amount of time: stop playback and
			// release media player
			try {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					mClient.onPlayerPause();
				}
				// if (mMediaPlayer.isPlaying())
				// mMediaPlayer.stop();
				// mMediaPlayer.release();
				// mClient.onPlayerStop();

			} catch (Exception e) {
			}
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			try {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					mClient.onPlayerPause();
				}
			} catch (Exception e) {
			}
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.setVolume(0.1f, 0.1f);
			break;
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		mClient.onPlayerSeekComplete();

	}

	public int getDuration() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying())
			return mMediaPlayer.getDuration();

		return 0;
	}

	public int getCurrentPosition() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying())
			return mMediaPlayer.getCurrentPosition();

		return NO_SEEK_POSITION;
	}

	public void pauseMediaPlayer() {
		Log.d("MediaPlayerService", "pauseMediaPlayer() called");
		try {
			mMediaPlayer.pause();
			if (wifiLock.isHeld())
				wifiLock.release();
			stopForeground(true);
			removeNotification();
		} catch (Exception e) {
		}
	}

	public void stopMediaPlayer() {
		try {
			if (wifiLock.isHeld())
				wifiLock.release();
			stopForeground(true);
			mMediaPlayer.stop();
			mMediaPlayer.release();
			// mMediaPlayer = null;
			removeNotification();
			if (audioManager != null)
				audioManager.abandonAudioFocus(this);
		} catch (Exception e) {
		}
	}

	public void resetMediaPlayer() {
		try {
			if (wifiLock.isHeld())
				wifiLock.release();
			stopForeground(true);
			mMediaPlayer.reset();
			removeNotification();
		} catch (Exception e) {
		}
	}

	public void startMediaPlayer() {
		showNotification();

		if (mMediaPlayer.isStopped())
			mMediaPlayer.prepareAsync(); // when prepare completes, it will automatically start.  But stopped cannot go to Start, but must go to Prepared first.
		else
			mMediaPlayer.start();
	}

	public void seekMediaPlayer(int msec) {
		if (mMediaPlayer.isPrepared() || mMediaPlayer.isStarted() || mMediaPlayer.isPaused() || mMediaPlayer.isCompleted())
			mMediaPlayer.seekTo(msec);
	}

	private void showNotification() {

		Intent notificationIntent = new Intent(this, MediaPlayerActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = buildNotification(contentIntent);

		// NotificationManager manager = (NotificationManager)
		// this.getSystemService(Context.NOTIFICATION_SERVICE);
		// manager.notify(MP_NOTIFICATION_ID, notification);

		startForeground(MP_NOTIFICATION_ID, notification);

	}

	private void removeNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(MP_NOTIFICATION_ID);
	}

	private Notification buildNotification(PendingIntent contentIntent) {

		Notification.Builder b = new Notification.Builder(getApplicationContext());

		return b.setAutoCancel(true)
		// .setDefaults(Notification.DEFAULT_ALL)
				.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher)
				// .setTicker("Optional ticker")
				.setContentTitle(title)
				// .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
				// .setDefaults(Notification.DEFAULT_LIGHTS|
				// Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND)
				.setContentIntent(contentIntent).setContentInfo("Info").build();

	}

}