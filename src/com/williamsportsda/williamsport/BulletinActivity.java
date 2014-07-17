package com.williamsportsda.williamsport;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.williamsportsda.williamsport.communications.Bulletinjson;
import com.williamsportsda.williamsport.communications.Bulletin;
import com.williamsportsda.williamsport.extras.CommunicationsIntentService;
import com.williamsportsda.williamsport.extras.FileIntentService;
import com.williamsportsda.williamsport.extras.JsonParser;
import com.williamsportsda.williamsport.extras.MyFragment;

public class BulletinActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bulletin);

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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends MyFragment {

		private ListView bulletinListView;

		BulletinReceiver receiver = null;
		protected static final String COMMUNICATIONS_BULLETIN_QUERY = "bulletinjson.php";

		private MyPerformanceArrayAdapter adapter;

		private ProgressDialog dialog = null;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_bulletin, container, false);

			bulletinListView = (ListView) rootView.findViewById(R.id.listView_bulletin);

			adapter = new MyPerformanceArrayAdapter(getActivity(), R.id.listView_bulletin, new ArrayList<Bulletin>());
			bulletinListView.setAdapter(adapter);

			IntentFilter filter = new IntentFilter(BulletinReceiver.ACTION_RESPONSE);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new BulletinReceiver();
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
					commIntent.putExtra(CommunicationsIntentService.PARAM_RECEIVER, BulletinReceiver.ACTION_RESPONSE);
					commIntent.putExtra(CommunicationsIntentService.PARAM_ACTION, COMMUNICATIONS_BULLETIN_QUERY);
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

		
		public class BulletinReceiver extends BroadcastReceiver {
			public static final String ACTION_RESPONSE = "com.williamsportsda.android.BulletinActivity.BULLETIN_QUERY";

			@Override
			public void onReceive(Context context, Intent intent) {

				try {

					String errorMessage = intent.getStringExtra(CommunicationsIntentService.PARAM_OUT_ERROR_MESSAGE);

					if (errorMessage != null && !errorMessage.equals("")) {
					}

					else {

						Bulletinjson response = (Bulletinjson) JsonParser.GetResponse(intent.getStringExtra(CommunicationsIntentService.PARAM_OUT_JSON),
								COMMUNICATIONS_BULLETIN_QUERY);

						if (response != null && response.bulletin != null && response.bulletin.size() > 0) {

							adapter.clear();
							adapter.addAll(response.bulletin);
							adapter.notifyDataSetChanged();
						}
					}

				} catch (JSONException e) {

				} finally {
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
				}

			}

		}

		private class FileReceiver extends ResultReceiver {
			public static final String ACTION_RESPONSE = "com.bmic.android.policyholderapp.BulletinActivity.PDF_DOWNLOAD";

			public FileReceiver(Handler handler) {
				super(handler);
			}

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);

				if (dialog.isShowing()) {
					dialog.dismiss();
				}

				if (resultCode == FileIntentService.DOWNLOAD_COMPLETE) {

					Boolean success = resultData.getBoolean(FileIntentService.DOWNLOAD_SUCCESS, false);
					String filename = resultData.getString(FileIntentService.PARAM_FILE);

					if (success && filename != null && filename != "") {
						File file = new File(filename);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(file), "application/pdf");
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);

					}
				}
			}
		}

		public class MyPerformanceArrayAdapter extends ArrayAdapter<Bulletin> {
			private final Activity context;
			public static final String MIME_TYPE_PDF = "application/pdf";

			class ViewHolder {
				public TextView title;
				public TextView day;
				public TextView month;
				//public ImageView viewIcon;
				public int position;
			}

			public MyPerformanceArrayAdapter(Activity context, int layoutID, ArrayList<Bulletin> items) {
				super(context, layoutID, items);
				this.context = context;
			}

			public boolean canDisplayPdf(Context context) {
				PackageManager packageManager = context.getPackageManager();
				Intent testIntent = new Intent(Intent.ACTION_VIEW);
				testIntent.setType(MIME_TYPE_PDF);
				if (packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
					return true;
				} else {
					return false;
				}
			}

			public boolean isExternalStorageWritable() {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					return true;
				}
				return false;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = convertView;
				// reuse views
				if (rowView == null) {
					LayoutInflater inflater = context.getLayoutInflater();
					rowView = inflater.inflate(R.layout.bulletin_item, null);
					// configure view holder
					ViewHolder viewHolder = new ViewHolder();
					viewHolder.title = (TextView) rowView.findViewById(R.id.textView_title);
					viewHolder.day = (TextView) rowView.findViewById(R.id.textView_day);
					viewHolder.month = (TextView) rowView.findViewById(R.id.textView_month);
					//viewHolder.viewIcon = (ImageView) rowView.findViewById(R.id.play_icon);
					rowView.setTag(viewHolder);
				}

				// fill data
				ViewHolder holder = (ViewHolder) rowView.getTag();
				Bulletin bulletin = this.getItem(position);
				holder.title.setText("Bulletin");
			
				try {  
				    Date pubDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(bulletin.title);  
				    holder.day.setText(new SimpleDateFormat("dd", Locale.US).format(pubDate));
				    holder.month.setText(new SimpleDateFormat("MMM", Locale.US).format(pubDate));
				} catch (ParseException e) {  
				    e.printStackTrace();  
				}	
				
				//holder.viewIcon.setImageResource(R.drawable.bulletin_small);
				holder.position = position;

				rowView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						ViewHolder holder = (ViewHolder) v.getTag();
						Bulletin bulletin = adapter.getItem(holder.position);

						if (canDisplayPdf(context)) {
							if (isExternalStorageWritable()) {
								try {

									if (bulletin.url != null && !bulletin.url.equals("")) {
										Intent intent = new Intent(getActivity(), FileIntentService.class);
										intent.putExtra(FileIntentService.PARAM_URL, bulletin.url);
										intent.putExtra(FileIntentService.PARAM_FILE, Environment.getExternalStorageDirectory().getAbsolutePath()
												+ "/bulletin.pdf");
										intent.putExtra("receiver", new FileReceiver(new Handler()));
										getActivity().startService(intent);

										dialog.setMessage("Please wait");
										dialog.show();

									} else {
										Toast.makeText(getActivity(), "Failed to locate file.", Toast.LENGTH_LONG).show();
									}
								} catch (Exception e) {

								}
							} else {
								Toast.makeText(getActivity(), "Unable to save download.", Toast.LENGTH_LONG).show();
							}
						} else {
							Toast.makeText(getActivity(), "No PDF Viewer found.", Toast.LENGTH_LONG).show();
						}

					}
				});

				return rowView;
			}
		}

	}

}
