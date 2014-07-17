package com.williamsportsda.williamsport;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.williamsportsda.williamsport.extras.IMediaPlayerServiceClient;
import com.williamsportsda.williamsport.extras.MediaItem;
import com.williamsportsda.williamsport.extras.MediaService;
import com.williamsportsda.williamsport.extras.MediaService.MediaPlayerBinder;

import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MediaPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_player);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == android.R.id.home) {
			shutdownFragmentMedia();
		}

		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		shutdownFragmentMedia();
	}

	private void shutdownFragmentMedia() {
		PlaceholderFragment fragment = (PlaceholderFragment) getFragmentManager().findFragmentById(R.id.container);
		fragment.onBackPressed();
	}

	public static class PlaceholderFragment extends Fragment implements IMediaPlayerServiceClient {

		private MediaItem mMediaItem;
		private MediaService mService;
		private boolean mBound;

		private ProgressDialog mProgressDialog;

		private MusicIntentReceiver musicIntentReceiver;

		private TextView media_position, media_duration;
		private SeekBar media_seek, volume_seek;
		private ImageView backButton, playPauseButton, forwardButton;
		private Handler handler;

		private SettingsContentObserver mSettingsContentObserver;

		Boolean phoneCallPause = false;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_media_player, container, false);

			mMediaItem = new MediaItem();
			mMediaItem.Title = (String) getActivity().getIntent().getExtras().getString("AUDIO_TITLE");
			mMediaItem.Url = (String) getActivity().getIntent().getExtras().getString("AUDIO_LINK");

			new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView_thumbnail)).execute((String) getActivity().getIntent().getExtras()
					.getString("AUDIO_MEDIATHUMBNAIL"));

			TextView t = (TextView) rootView.findViewById(R.id.textView_author);
			t.setText((String) getActivity().getIntent().getExtras().getString("AUDIO_AUTHOR"));
			t = (TextView) rootView.findViewById(R.id.textView_title);
			t.setText((String) getActivity().getIntent().getExtras().getString("AUDIO_TITLE"));
			t = (TextView) rootView.findViewById(R.id.textView_date);
			try {
				Date pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse((String) getActivity().getIntent().getExtras()
						.getString("AUDIO_DATE"));
				t.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(pubDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			
			mProgressDialog = new ProgressDialog(getActivity());

			media_position = (TextView) rootView.findViewById(R.id.textView_media_position);
			media_duration = (TextView) rootView.findViewById(R.id.textView_media_duration);
			media_seek = (SeekBar) rootView.findViewById(R.id.seekBar_media_position);

			volume_seek = (SeekBar) rootView.findViewById(R.id.seekBar_volume);

			playPauseButton = (ImageView) rootView.findViewById(R.id.imageView_playpause);
			playPauseButton.setTag(R.drawable.play);
			playPauseButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					phoneCallPause = false;// need to clear so phone going to
											// idle does not restart audio.

					Integer tag = (Integer) playPauseButton.getTag();
					tag = tag == null ? 0 : tag;

					if (tag == R.drawable.pause) {
						mService.pauseMediaPlayer();
						setToPlaying(false);
					} else {
						mService.startMediaPlayer();
						setToPlaying(true);
					}

				}
			});

			backButton = (ImageView) rootView.findViewById(R.id.imageView_back);
			backButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					int mediaPos = mService.getCurrentPosition();

					if (mediaPos != MediaService.NO_SEEK_POSITION && mService != null)

					{
						mService.seekMediaPlayer(mediaPos - 15000 > 0 ? mediaPos - 15000 : 0);
					}

				}
			});

			forwardButton = (ImageView) rootView.findViewById(R.id.imageView_forward);
			forwardButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					int mediaPos = mService.getCurrentPosition();
					int duration = mService.getDuration();

					if (mediaPos != MediaService.NO_SEEK_POSITION && mService != null)

					{
						mService.seekMediaPlayer(mediaPos + 15000 < duration ? mediaPos + 15000 : duration);
					}

				}
			});

			media_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// note onPlayerSeekComplete restarts seekbar/timer
					// thread.
					if (mService != null)
						mService.seekMediaPlayer(seekBar.getProgress());

				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					handler.removeCallbacks(moveSeekBarThread);
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				}
			});

			volume_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (mService != null)
						mService.setStreamVolume(progress);
				}

			});

			bindToService();
			
			handler = new Handler();

			mSettingsContentObserver = new SettingsContentObserver(getActivity(), new Handler());
			getActivity().getApplicationContext().getContentResolver()
					.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

			return rootView;
		}

		private void setToPlaying(Boolean playing) {

			if (playing) {
				playPauseButton.setImageResource(R.drawable.pause);
				playPauseButton.setTag(R.drawable.pause);
			} else {
				playPauseButton.setImageResource(R.drawable.play);
				playPauseButton.setTag(R.drawable.play);
			}

		}

		@Override
		public void onResume() {
			super.onResume();

			musicIntentReceiver = new MusicIntentReceiver();

			IntentFilter filter = new IntentFilter();
			filter.addAction("android.media.AUDIO_BECOMING_NOISY");
			filter.addAction("android.intent.action.HEADSET_PLUG");
			filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			filter.addAction("android.intent.action.PHONE_STATE");
			getActivity().registerReceiver(musicIntentReceiver, filter);

			handler.removeCallbacks(moveSeekBarThread);
			handler.postDelayed(moveSeekBarThread, 1000);
		}

		@Override
		public void onPause() {
			super.onPause();

			getActivity().unregisterReceiver(musicIntentReceiver);

			handler.removeCallbacks(moveSeekBarThread);

		}

		public void onBackPressed() {
			shutdownActivity();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();

			getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);

			shutdownActivity();
		}

		@Override
		public void onPlayerSeekComplete() {
			handler.removeCallbacks(moveSeekBarThread);
			handler.postDelayed(moveSeekBarThread, 1000);
		}

		public void bindToService() {
			Intent intent = new Intent(getActivity(), MediaService.class);

			if (MediaPlayerServiceRunning()) {
				// Bind to LocalService
				getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			} else {
				getActivity().startService(intent);
				getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			}
		}

		private ServiceConnection mConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className, IBinder serviceBinder) {

				// bound with Service. get Service instance
				MediaPlayerBinder binder = (MediaPlayerBinder) serviceBinder;
				mService = binder.getService();

				// send this instance to the service, so it can make callbacks
				// on this instance as a client
				mService.setClient(PlaceholderFragment.this);
				mBound = true;

				mService.initializePlayer(mMediaItem);

				setToPlaying(mService.getMediaPlayer().isPlaying());

			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				mBound = false;
			}
		};

		private boolean MediaPlayerServiceRunning() {

			ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);

			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if ("com.williamsportsda.williamsport.extras.MediaService".equals(service.service.getClassName())) {
					return true;
				}
			}

			return false;
		}

		public void onInitializePlayerSuccess() {

			// these values cannot be retrieved until the player has been
			// initialized.
			volume_seek.setMax(mService.getStreamMaxVolume());

			// if starting stream, volume seems to always set to zero. Set to 8
			// instead.
			// ***** would be better to "remember" volume and set that value
			// whenever starting. ******
			if (mService != null && mService.getStreamVolume() == 0)
				mService.setStreamVolume(8);

			volume_seek.setProgress(mService.getStreamVolume());

			mProgressDialog.dismiss();

			setToPlaying(mService.getMediaPlayer().isPlaying());

		}

		public void onInitializePlayerStart(String message) {

			mProgressDialog = ProgressDialog.show(getActivity(), "", message, true);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialogInterface) {
					MediaPlayerActivity.PlaceholderFragment.this.mService.resetMediaPlayer();

					setToPlaying(mService.getMediaPlayer().isPlaying());
				}

			});

		}

		public void onPlayerPause() {
			if (mService != null)
				setToPlaying(!mService.getMediaPlayer().isPlaying());
		}

		public void onPlayerStop() {
			shutdownActivity();
		}

		@Override
		public void onError() {
			mProgressDialog.cancel();
		}

		/**
		 * Closes unbinds from service, stops the service, and calls finish()
		 */
		public void shutdownActivity() {

			if (mBound) {
				if (mService != null)
					mService.stopMediaPlayer();
				// Detach existing connection.
				getActivity().unbindService(mConnection);
				mBound = false;
			}

			Intent intent = new Intent(getActivity(), MediaService.class);
			getActivity().stopService(intent);
			getActivity().finish();

		}

		public class MusicIntentReceiver extends android.content.BroadcastReceiver {

			@Override
			public void onReceive(Context ctx, Intent intent) {
				if (mService == null)
					return;

				if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
					String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

					if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
						mService.pauseMediaPlayer();
						phoneCallPause = true;
					} else if (phoneCallPause && TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
						mService.startMediaPlayer();
						phoneCallPause = false;
					}

				} else if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)
						|| intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) || intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {

					mService.pauseMediaPlayer();

				}
			}

		}

		private Runnable moveSeekBarThread = new Runnable() {

			public void run() {
				if (mService != null) {
					int mediaPos = mService.getCurrentPosition();
					int mediaMax = mService.getDuration();

					if (mediaPos != MediaService.NO_SEEK_POSITION && mService.getMediaPlayer().isPlaying()) {

						media_seek.setMax(mediaMax);
						media_seek.setProgress(mediaPos);

						mediaMax /= 1000;
						int hours = mediaMax / 3600;
						int minutes = (mediaMax % 3600) / 60;
						int seconds = mediaMax % 60;

						media_duration.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));

						mediaPos /= 1000;
						hours = mediaPos / 3600;
						minutes = (mediaPos % 3600) / 60;
						seconds = mediaPos % 60;

						media_position.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));

					}
				}

				handler.postDelayed(this, 300);

			}
		};

		public class SettingsContentObserver extends ContentObserver {

			private Context context;

			public SettingsContentObserver(Context c, Handler handler) {

				super(handler);
				context = c;

			}

			@Override
			public boolean deliverSelfNotifications() {
				return super.deliverSelfNotifications();
			}

			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);

				AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

				volume_seek.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));

			}
		}

		private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
			ImageView bmImage;

			public DownloadImageTask(ImageView bmImage) {
				this.bmImage = bmImage;
			}

			protected Bitmap doInBackground(String... urls) {
				String urldisplay = urls[0];

				if (urldisplay == null || urldisplay.equals(""))
					return null;

				Bitmap mIcon11 = null;
				try {
					InputStream in = new java.net.URL(urldisplay).openStream();
					mIcon11 = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return mIcon11;
			}

			protected void onPostExecute(Bitmap result) {
				if (result != null)
					bmImage.setImageBitmap(result);
			}
		}

	}

}
