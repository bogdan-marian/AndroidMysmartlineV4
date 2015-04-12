package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import eu.mysmartline.appv3.models.DeviceRegistrationResponceModel;
import eu.mysmartline.appv3.models.MyKeys;

public class MainActivity extends Activity {
	/**
	 * This is the project number from the API Console, as described in
	 * "Getting Started."
	 */
	String SENDER_ID = MyKeys.PROPERTY_GCM_PROJECT_NUMBER;

	public static final String MY_SMARTLINE_PREFS = "MY_SMARTLINE_PREFS";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	TextView statusView;
	Context context;
	GoogleCloudMessaging gcm;
	String gcmRegid;

	// Tags use on log messages
	static final String TAG = "mysmartlineV3";

	@SuppressLint("Wakelock")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// get the views that will have content modified
		statusView = (TextView) findViewById(R.id.textView1);
		context = getApplicationContext();
		
		//SharedPreferences prefs = getGcmPreferences();
		
		//use wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My tag");
		wl.acquire();
		
		
		/*boolean deviceActivated;
		deviceActivated = prefs.getBoolean(MyKeys.PROPERTY_DEVICE_ACTIVATED, false);*/
		/**
		 *The follwoing code is part of the original 
		 */
		/*if (deviceActivated)
		{
			*//**
			 *  
			 * If device is active then open the Activity3ListActivityes
			 *//*
			Intent intent = new Intent (this, Activity3ListActivityes.class);
			startActivity(intent);
			return;
		}*/
		// check device for Play Services APK
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			gcmRegid = getRegistrationId(context);
			if (gcmRegid.isEmpty()) {
				registerInBackground();
			}
			else{
				startConnectToServer();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		SharedPreferences prefs = getGcmPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences() {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		
		return getSharedPreferences(MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					gcmRegid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + gcmRegid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				/**
				 */
			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. 
	 */

	private void sendRegistrationIdToBackend() {
		DownloadWebPageTask accesWeb = new DownloadWebPageTask();
		String registrationUrl = MyKeys.PROPERTY_HOME + "DeviceEntryPoint/registerPost";
		accesWeb.execute(new String[] { registrationUrl });
	}

	// http://www.mysmartline.eu/deviceEntryPoint/registerPost
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";

			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				Log.d("mysmartlineV3", "bogdan post url = " + url);
				HttpPost httppost = new HttpPost(url);
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							1);
					nameValuePairs.add(new BasicNameValuePair("gcmRegId",
							gcmRegid));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse execute = client.execute(httppost);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			/**
			 * decodeJsonAndShowContent(result); send the result to activation
			 * view
			 * */
			Log.d("mysmartlineV3", "bogdan result from server: " + result);
			
			DeviceRegistrationResponceModel regModel = decodeResponseFromServer(result);
			if (regModel.isDatabaseError()) {
				statusView.setText("Back end eror");
			}
			// Log.i(TAG, "start save data");
			if (!regModel.isActive() && gcmRegid != null) {
				statusView
						.setText("Registered with the back end. \nShort id = "
								+ regModel.getShortId());
				SharedPreferences prefs = getGcmPreferences();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(MyKeys.PROPERTY_REG_ID, gcmRegid);
				editor.putString(MyKeys.PROPERTY_CLOUD_ACTIVATION_ID,
						regModel.getShortId());
				editor.commit();
				statusView.setText(statusView.getText()
						+ "\n"
						+ "Result from the server = "
						+ result
						+ "\n short id = "
						+ prefs.getString("PROPERTY_CLOUD_ACTIVATION_ID",
								"note served this is an error")
						+ "\nYou should redirect to actiation view");
				//startActivateActivity();
				startConnectToServer();
				// Log.i(TAG, "end save data");
			}
		}
	}

	@SuppressWarnings("unused")
	private void startActivateActivity() {
		Intent intent = new Intent(this, Activity4ActivateDevice.class);
		startActivity(intent);
	}

	private DeviceRegistrationResponceModel decodeResponseFromServer(
			String response) {
		Gson gson = new Gson();
		DeviceRegistrationResponceModel model = gson.fromJson(response,
				DeviceRegistrationResponceModel.class);
		return model;
	}
	private void startConnectToServer(){
		Intent intent = new Intent(this, Activity11ConnectToServer.class);
		startActivity(intent);
	}
}
