package com.williamsportsda.williamsport.extras;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import org.json.JSONException;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyApplication extends Application  {

	private static Context Context;

	public static Context getAppContext() {
		return MyApplication.Context;
	}

	@Override
	public void onCreate() {

		super.onCreate();
		MyApplication.Context = getApplicationContext();
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				if (Context != null && MyFragment.isOnline(Context)) {
					try {
						Intent loginIntent = new Intent(Context, CommunicationsIntentService.class);
						loginIntent.putExtra(CommunicationsIntentService.PARAM_ACTION, "ReportError");
						loginIntent.putExtra(CommunicationsIntentService.PARAM_IN_JSON,
								JsonHandler.ReportError(Context, (new Date()).toString(), Log.getStackTraceString(ex)).toString());
						Context.startService(loginIntent);
					} catch (JSONException e) {

					}
				}
			}
		});

		super.onCreate();
	}



}

