package eu.mysmartline.appv3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import eu.mysmartline.appv3.models.MyKeys;

public class Activity7ShowDisplayPanel extends Activity {
	
	ListView msgList;
	ArrayList<DisplayModel> details;
	DisplayAdapter displayAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity7_show_display_panel);
		
		msgList = (ListView) findViewById(R.id.MessageList);
		details = new ArrayList<DisplayModel>();
		
		
		
		for(DisplayModel model:details){
			Log.d("tag","after: "+ model.getMessage());
		}
		DisplayCounter couner = DisplayCounter.getInstance();
		List<String>items = couner.getCoutner();
		for (String item:items){
			Log.i("tag", "value = " + item);
			details.add(buildDisplayModel(item));
		}
		displayAdapter = new DisplayAdapter(details, this);
		msgList.setAdapter(displayAdapter);
		setNavigationIntention();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity7_show_display_panel, menu);
		return true;
	}
	private DisplayModel buildDisplayModel(String theMessage){
		DisplayModel model = new DisplayModel();
		model.setIcon(R.drawable.ic_launcher);
		model.setMessage(theMessage);
		model.setTime(new Date().toString());
		return model;
	}
	private void setNavigationIntention() {
		SharedPreferences prefs = getSharedPreferences(
				MyKeys.MY_SMARTLINE_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_INTENTION,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL);
		editor.putString(MyKeys.PROPERTY_NAVIGATETO_VALUE,
				MyKeys.PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL);
		editor.commit();
	}

}
