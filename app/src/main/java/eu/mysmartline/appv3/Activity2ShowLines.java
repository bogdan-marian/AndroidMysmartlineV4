package eu.mysmartline.appv3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import eu.mysmartline.appv3.models.LineDetailsModel;
import eu.mysmartline.appv3.models.MyKeys;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Activity2ShowLines extends Activity {

	public static final String PROPERTY_REG_ID = "registration_id";
	private List<LineDetailsModel> lines;
	private ListView listView;
	private Context context;
	/*private String logTag = MyKeys.PROPERTY_LOGTAG;
	Log.i(logTag, result);*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity2_show_lines);
		// Show the Up button in the action bar.
		setupActionBar();
		lines = new ArrayList<LineDetailsModel>();
		listView = (ListView) findViewById(R.id.listView);
		context = this;

		setNavigationIntention();

		GetLinesTask getLines = new GetLinesTask();
		String getLinesUrl = MyKeys.PROPERTY_HOME + "Api/getLines";
		getLines.execute(new String[] { getLinesUrl });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity2_show_lines, menu);
		return true;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private class GetLinesTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			StringBuilder response = new StringBuilder();
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							1);
					// get saved gcmId
					Context context = getApplicationContext();
					SharedPreferences sharedPref = context
							.getSharedPreferences(MyKeys.MY_SMARTLINE_PREFS,
									Context.MODE_PRIVATE);
					String deviceGcmId = sharedPref.getString(
							MyKeys.PROPERTY_REG_ID, "no default");
					// set form parameters
					nameValuePairs.add(new BasicNameValuePair("deviceGcmId",
							deviceGcmId));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse execute = client.execute(httpPost);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = new String();
					while ((s = buffer.readLine()) != null) {
						response.append(s);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("nodata")) {
				// /then no data
			} else {
				// decode json
				
				Gson gson = new Gson();
				LineDetailsModel[] linesArey = gson.fromJson(result,
						LineDetailsModel[].class);
				List<Map<String, String>> items = new ArrayList<Map<String, String>>();
				// create object list and view list
				for (LineDetailsModel item : linesArey) {
					// set the registration url
					item.setRegistrationUrl(MyKeys.PROPERTY_HOME
							+ item.getRegistrationUrl());
					lines.add(item);
					items.add(createItem("item", item.getName()));
				}
				// react to click
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parentAdapter,
							View view, int position, long id) {
						int myId = (int) id;
						LineDetailsModel details = lines.get(myId);
						Intent intent = new Intent();
						intent.setClass(Activity2ShowLines.this,
								Activity6ShowLineDetails.class);
						intent.putExtra("LineDetailsModel", details);
						startActivity(intent);
					}

				});

				SimpleAdapter simpleAdpt = new SimpleAdapter(context, items,
						android.R.layout.simple_list_item_1,
						new String[] { "item" },
						new int[] { android.R.id.text1 });
				listView.setAdapter(simpleAdpt);
			}
		}

		private HashMap<String, String> createItem(String key, String name) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put(key, name);
			return item;
		}
	}

	private void setNavigationIntention() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_INTENTION,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES);
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_LINES);
		editor.commit();
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE, "paused");
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		setNavigationIntention();
	}
}
