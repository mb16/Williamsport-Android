package com.williamsportsda.williamsport.extras;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnCompletionListener;

public class StatefulMediaPlayer extends android.media.MediaPlayer implements OnSeekCompleteListener, OnCompletionListener {
    /**
     * Set of states for StatefulMediaPlayer:<br>
     * EMPTY, CREATED, PREPARED, STARTED, PAUSED, STOPPED, ERROR
     * http://www.speakingcode.com/2012/02/22/creating-a-streaming-audio-app-for-android-with-android-media-mediaplayer-android-media-audiomanager-and-android-app-service/
     */
    public enum MPStates {
        EMPTY, CREATED, PREPARED, STARTED, PAUSED, STOPPED, ERROR, COMPLETED
    }
 
    private MPStates mState;
    private MediaItem mMediaItem;
 
    public MediaItem getMediaItem() {
        return mMediaItem;
    }
 
    /**
     * Sets a StatefulMediaPlayer's data source as the provided StreamStation
     * @param streamStation the StreamStation to set as the data source
     */
    public void setMediaItem(MediaItem mediaItem) {
        this.mMediaItem = mediaItem;
        try {
            setDataSource(mediaItem.Url);
            setState(MPStates.CREATED);
        }
        catch (Exception e) {
            Log.e("StatefulMediaPlayer", "setDataSource failed");
            setState(MPStates.ERROR);
        }
    }
 
    /**
     * Instantiates a StatefulMediaPlayer object.
     */
    public StatefulMediaPlayer() {
        super();
        setState(MPStates.CREATED);
        
        this.setOnSeekCompleteListener(this);
        this.setOnCompletionListener(this);
    }
 
    /**
     * Instantiates a StatefulMediaPlayer object with the Audio Stream Type
     * set to STREAM_MUSIC and the provided StreamStation's URL as the data source.
     * @param streamStation The StreamStation to use as the data source
     */
    public StatefulMediaPlayer(MediaItem streamStation) {
        super();
        this.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mMediaItem = streamStation;
        try {
            setDataSource(mMediaItem.Url);
            setState(MPStates.CREATED);
        }
        catch (Exception e) {
            Log.e("StatefulMediaPlayer", "setDataSourceFailed");
            setState(MPStates.ERROR);
        }
    }
 
    @Override
    public void reset() throws IllegalStateException {
        super.reset();
        this.mState = MPStates.EMPTY;
    }
 
    @Override
    public void start() throws IllegalStateException {
        super.start();
        setState(MPStates.STARTED);
    }
 
    @Override
    public void pause() throws IllegalStateException { 
        super.pause();
        setState(MPStates.PAUSED);
 
    }
 
    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        setState(MPStates.STOPPED);
    }
 
    @Override
    public void release() throws IllegalStateException {
        super.release();
        setState(MPStates.EMPTY);
    }
 
    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
        setState(MPStates.PREPARED);
    }
 
    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
        setState(MPStates.PREPARED);
    }
 
    public MPStates getState() {
        return mState;
    }
 
    /**
     * @param state the state to set
     */
    public void setState(MPStates state) {
        this.mState = state;
    }
 
    public boolean isCreated() {
        return (mState == MPStates.CREATED);
    }
 
    public boolean isEmpty() {
        return (mState == MPStates.EMPTY);
    }
 
    public boolean isStopped() {
        return (mState == MPStates.STOPPED);
    }
 
    public boolean isStarted() {
        return (mState == MPStates.STARTED || this.isPlaying());
    }
 
    public boolean isPaused() {
        return (mState == MPStates.PAUSED);
    }
 
    public boolean isPrepared() {
        return (mState == MPStates.PREPARED);
    }
    
    public boolean isCompleted() {
        return (mState == MPStates.COMPLETED);
    }

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		mState = MPStates.COMPLETED;		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		setState(MPStates.STOPPED);		
	}
}

