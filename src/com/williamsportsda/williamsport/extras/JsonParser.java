package com.williamsportsda.williamsport.extras;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;

public class JsonParser {

	private static String resourcePath = "com.williamsportsda.williamsport.communications.";

	private static String UpperCase(String input) {

		return Character.toUpperCase(input.charAt(0)) + input.substring(1);

	}

	public static Object GetResponse(String str, String Action)
			throws JSONException {

		JSONObject obj = null;
		try {
			obj = new JSONObject(str);
		} catch (Exception e) {

		}

		Object oo = BuildObj(
				obj,
				resourcePath
						+ UpperCase(Action.substring(
								Action.lastIndexOf('/') > 0 ? Action
										.lastIndexOf('/') + 1 : 0).replace(
								".php", "")));

		return oo;

	}

	public static Object BuildObj(JSONObject json, String objClass)
			throws JSONException {

		Object parentObj = null;

		try {

			Class<?> parentClass = Class.forName(objClass);
			Constructor<?> constructor = parentClass.getConstructor();
			parentObj = constructor.newInstance();

			if (json != null) {

				Object obj;
				Iterator<?> keys = json.keys();
				while (keys.hasNext()) {
					obj = null;

					String myKey = (String) keys.next();
					Object jsonObj = json.get(myKey);

					if (jsonObj == null)
						continue;

					if (jsonObj instanceof String) {

						obj = json.getString(myKey);

					} else if (jsonObj instanceof JSONObject) {

						Field fld = null;
						try { // throws exception if field not found.

							fld = parentClass.getField(myKey.replace("@", "").replace(":", ""));
						} catch (NoSuchFieldException e) {
						}
						
						if (fld != null) {
							String temp = fld.getType().getName();
							//Class<?> type = fld.getType();

							obj = BuildObj(
									json.getJSONObject(myKey),
									temp);
						}

					} else if (jsonObj instanceof JSONArray) {

						obj = BuildArr(json.getJSONArray(myKey),
								myKey.replace("@", "").replace(":", ""));

					}

					try {

						if (obj != null) {
							Field field = parentClass.getField(myKey.replace(
									"@", "").replace(":", ""));
							field.set(parentObj, obj);
						}

					} catch (NoSuchFieldException e) {
					}

				}
			}

		} catch (ClassNotFoundException e) {

		} catch (Exception e) {
			Log.e("Error", e.getMessage());

		}

		return parentObj;
	}

	public static Object BuildArr(JSONArray json, String objClass)
			throws JSONException {

		ArrayList<Object> list = null;

		try {

			list = new ArrayList<Object>();

			if (json != null) {

				Object genObject;
				for (int i = 0; i < json.length(); i++) {
					genObject = null;

					Object obj = json.getJSONObject(i);

					if (obj == null)
						continue;

					if (obj instanceof String) {

						genObject = (String) obj;

					} else if (obj instanceof JSONObject) {

						genObject = BuildObj((JSONObject) obj, resourcePath
								+ UpperCase(objClass));
					}

					if (genObject != null) {
						list.add(genObject);
					}

				}
			}

		} catch (Exception e) {

		}

		return list;
	}

}
