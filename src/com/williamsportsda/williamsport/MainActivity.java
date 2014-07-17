package com.williamsportsda.williamsport;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
	public static class PlaceholderFragment extends Fragment {

		GridView gridView;

		static final String[] MENU_OPTIONS = new String[] { "Audio", "Live", "Bulletin", "Contact", };

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			gridView = (GridView) rootView.findViewById(R.id.gridView_menu);

			gridView.setAdapter(new ImageAdapter(getActivity(), MENU_OPTIONS));

			gridView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					Toast.makeText(getActivity().getApplicationContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();

				}
			});

			return rootView;
		}

		public class ImageAdapter extends BaseAdapter {
			private Context context;
			private final String[] mobileValues;

			public ImageAdapter(Context context, String[] mobileValues) {
				this.context = context;
				this.mobileValues = mobileValues;
			}

			public View getView(int position, View convertView, ViewGroup parent) {

				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View gridView;
				
				if (convertView == null) {
					
					gridView = new View(context);

					// get layout from mobile.xml
					gridView = inflater.inflate(R.layout.menu, null);
					gridView.setTag(mobileValues[position]);
					
					RelativeLayout rel = (RelativeLayout) gridView.findViewById(R.id.relativeLayout_menu);
					ImageView image = (ImageView) rel.findViewById(R.id.imageView_icon);
					switch (mobileValues[position]) {
					case "Audio":
						image.setImageResource(R.drawable.icon_audio);
						break;
					case "Live":
						image.setImageResource(R.drawable.icon_live);
						break;
					case "Bulletin":
						image.setImageResource(R.drawable.icon_bulletin);
						break;
					case "Contact":
						image.setImageResource(R.drawable.icon_contact);
						break;
					default:
						break;
					}

					rel.setTag(mobileValues[position]);

					
					gridView.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {

							Intent intent = null;

							String tag = (String) v.getTag();
							if (tag.equals("Audio")) {
								intent = new Intent(getActivity(), AudioActivity.class);
							} else if (tag.equals("Live")) {
								intent = new Intent(getActivity(), WebViewActivity.class);
							} else if (tag.equals("Bulletin")) {
								intent = new Intent(getActivity(), BulletinActivity.class);
							} else if (tag.equals("Contact")) {
								intent = new Intent(getActivity(), ContactActivity.class);
							}

							if (intent != null)
								startActivity(intent);
						}
					});

				} else {
					gridView = (View) convertView;
				}

				return gridView;
			}

			@Override
			public int getCount() {
				return mobileValues.length;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}
		}
	}

}
