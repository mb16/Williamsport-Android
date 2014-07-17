package com.williamsportsda.williamsport.extras;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class JsonHandler {

	
	public static JSONObject ReportError(Context context, String date, String stack) throws JSONException {
			
		JSONObject data = new JSONObject();
		data.put("date", date);
		data.put("stack", stack);
		
		
		JSONObject app = new JSONObject();
		app.put("Android_CODENAME", Build.VERSION.CODENAME);
		app.put("Android_INCREMENTAL", Build.VERSION.INCREMENTAL);
		app.put("Android_RELEASE", Build.VERSION.RELEASE);
		app.put("Android_SDK_INT", Build.VERSION.SDK_INT);

		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo("com.bmic.android.policyholderapp", PackageManager.GET_META_DATA);
			app.put("Android_versionCode", pi.versionCode);
			app.put("Android_versionName", pi.versionName);
			app.put("Android_packageName", pi.packageName);
			app.put("Android_firstInstallTime", pi.firstInstallTime);
			app.put("Android_lastUpdateTime", pi.lastUpdateTime);
			app.put("IOS_systemVersion", 0.0f);
			
			
		} catch (NameNotFoundException e) {			
		}
		
		JSONObject parent = new JSONObject();
		parent.put("App", app);
		parent.put("ReportError", data);
		
		return parent;
	}
	
}
