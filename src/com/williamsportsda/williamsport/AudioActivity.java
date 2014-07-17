package com.williamsportsda.williamsport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

import com.williamsportsda.williamsport.communications.Feed;
import com.williamsportsda.williamsport.communications.Item;
import com.williamsportsda.williamsport.communications.Rss;
import com.williamsportsda.williamsport.extras.CommunicationsIntentService;
import com.williamsportsda.williamsport.extras.JsonHandler;
import com.williamsportsda.williamsport.extras.JsonParser;
import com.williamsportsda.williamsport.extras.MyFragment;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AudioActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends MyFragment {

		private ListView audioListView;

		AudioReceiver receiver = null;
		protected static final String COMMUNICATIONS_AUDIO_QUERY = "feed.php";

		private MyPerformanceArrayAdapter adapter;
				
		private ProgressDialog dialog = null;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_audio,
					container, false);

			audioListView = (ListView) rootView
					.findViewById(R.id.listView_media);


			adapter = new MyPerformanceArrayAdapter(
					getActivity(),R.id.listView_media, new ArrayList<Item>());
			audioListView.setAdapter(adapter);

			
			IntentFilter filter = new IntentFilter(AudioReceiver.ACTION_RESPONSE);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new AudioReceiver();
			getActivity().registerReceiver(receiver, filter);
			
			dialog = new ProgressDialog(getActivity());
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			
			LoadData();

			return rootView;
		}

		protected void LoadData() {

			if (isOnline(getActivity().getApplicationContext())) {

				try {
					
					Intent commIntent = new Intent(getActivity(), CommunicationsIntentService.class);
					commIntent.putExtra(CommunicationsIntentService.PARAM_RECEIVER, AudioReceiver.ACTION_RESPONSE);
					commIntent.putExtra(CommunicationsIntentService.PARAM_ACTION, COMMUNICATIONS_AUDIO_QUERY);
					getActivity().startService(commIntent);

					dialog.setMessage("Please wait");
			        dialog.show();
			        
				} catch (Exception e) {

				}
			}

		}
		
		@Override
		public void onDestroy() {

			if (receiver != null)
				getActivity().unregisterReceiver(receiver);

			super.onDestroy();
		}

		public class AudioReceiver extends BroadcastReceiver {
			public static final String ACTION_RESPONSE = "com.williamsportsda.android.AudioActivity.AUDIO_QUERY";

			@Override
			public void onReceive(Context context, Intent intent) {

				try {

					String errorMessage = intent
							.getStringExtra(CommunicationsIntentService.PARAM_OUT_ERROR_MESSAGE);

					if (errorMessage != null && !errorMessage.equals("")) {
					}

					else {

						 Feed response = (Feed) JsonParser.GetResponse(intent.getStringExtra(CommunicationsIntentService.PARAM_OUT_JSON), COMMUNICATIONS_AUDIO_QUERY);

						 if (response != null && response.rss != null && response.rss.channel != null  && response.rss.channel.item != null  && !response.rss.channel.item.isEmpty() ) {
							 
								adapter.clear();
								adapter.addAll(response.rss.channel.item);
								adapter.notifyDataSetChanged();
						 }
					}

				} catch (JSONException e) {
					
					
				}
				finally{
					  if (dialog.isShowing()) {
				            dialog.dismiss();
				        }
				}
			}

		}

		public class MyPerformanceArrayAdapter extends ArrayAdapter<Item> {
			private final Activity context;
	
			class ViewHolder {
				public TextView title;
				public TextView author;
				public TextView day;
				public TextView month;
				//public ImageView playIcon;
				public int position;
			}

			public MyPerformanceArrayAdapter(Activity context, int layoutID, ArrayList<Item> items) {
				super(context, layoutID, items);
				this.context = context;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = convertView;
				// reuse views
				if (rowView == null) {
					LayoutInflater inflater = context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.audio_item, null);
					// configure view holder
					ViewHolder viewHolder = new ViewHolder();
					viewHolder.title = (TextView) rowView
							.findViewById(R.id.textView_title);
					viewHolder.author = (TextView) rowView
							.findViewById(R.id.textView_author);
					viewHolder.day = (TextView) rowView
							.findViewById(R.id.textView_day);
					viewHolder.month = (TextView) rowView
							.findViewById(R.id.textView_month);
					//viewHolder.playIcon = (ImageView) rowView
					//		.findViewById(R.id.play_icon);
					rowView.setTag(viewHolder);
				}

				
				// fill data
				ViewHolder holder = (ViewHolder) rowView.getTag();
				Item item = this.getItem(position);
				holder.title.setText(item.title);
				holder.author.setText(item.author);
				
				
				try {  
				    Date pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(item.pubDate);  
				    holder.day.setText(new SimpleDateFormat("dd", Locale.US).format(pubDate));
				    holder.month.setText(new SimpleDateFormat("MMM", Locale.US).format(pubDate));
				} catch (ParseException e) {  
				    e.printStackTrace();  
				}				
				
				//holder.playIcon.setImageResource(R.drawable.audio_small);
				holder.position = position;

				rowView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						ViewHolder holder = (ViewHolder) v.getTag();
						Item item = adapter.getItem(holder.position);
						
						Intent intent = new Intent(getActivity(),
								MediaPlayerActivity.class);
						intent.putExtra("AUDIO_TITLE", item.title);
						intent.putExtra("AUDIO_AUTHOR", item.author);
						intent.putExtra("AUDIO_LINK", item.link);
						intent.putExtra("AUDIO_DATE", item.pubDate);
						intent.putExtra("AUDIO_MEDIATHUMBNAIL", item.mediathumbnail.url);
						startActivity(intent);
					}
				});

				return rowView;
			}
		}

	}

}
