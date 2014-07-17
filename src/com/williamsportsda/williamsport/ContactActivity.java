package com.williamsportsda.williamsport;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.williamsportsda.williamsport.MainActivity.PlaceholderFragment.ImageAdapter;

public class ContactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);

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
	public static class PlaceholderFragment extends Fragment {




		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact, container,
					false);

			TextView t = (TextView)rootView.findViewById(R.id.textView_phone);
			t.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:3012238125"));
					startActivity(intent);
			    }
			});	
			
			t = (TextView)rootView.findViewById(R.id.textView_website);
			t.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.williamsportsda.com"));
			    	startActivity(browserIntent);
			    }
			});		

			t = (TextView)rootView.findViewById(R.id.textView_maplink);
			t.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?q=16421 Lappans Road Williamsport, MD 21795"));
			    	startActivity(browserIntent);
			    }
			});	
			
			return rootView;
		}

		
	}

}
