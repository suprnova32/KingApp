package com.insomniware.kingapp.services;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.helpers.ConnectionHelper;
import com.insomniware.kingapp.receivers.PassiveLocationChangedReceiver;
import com.insomniware.kingapp.receivers.ProximityIntentReceiver;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;

public class LocationsUpdateService extends IntentService {
	
	 protected static String TAG = "PlacesUpdateService";
	 protected ConnectivityManager cm;
	 protected boolean lowBattery = false;
	 protected boolean mobileData = false;

	public LocationsUpdateService() {
		super(TAG);
		
	}
	
	/**
	 * Returns battery status. True if less than 10% remaining.
	 * @param battery Battery Intent
	 * @return Battery is low
	 */
	protected boolean getIsLowBattery(Intent battery) {
		float pctLevel = (float)battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1) / 
				battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		return pctLevel < 0.15;
	}
  
	@Override
	public void onCreate() {
		super.onCreate();
		cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Location location = new Location("CONSTRUCTED_LOCATION_PROVIDER");
	    int radius = 50;
		
		Bundle extras = intent.getExtras();
	    if (intent.hasExtra(MyConstants.EXTRA_KEY_LOCATION)) {
	    	location = (Location)(extras.get(MyConstants.EXTRA_KEY_LOCATION));
	    	radius = extras.getInt(MyConstants.EXTRA_KEY_RADIUS, 50);
	    }
	    
	    // Check if we're in a low battery situation.
	    IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    Intent battery = registerReceiver(null, batIntentFilter);
	    lowBattery = getIsLowBattery(battery);
	    
	    // Check if we're connected to a data network, and if so - if it's a mobile network.
	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	    mobileData = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
	    
	    // If we're not connected, enable the connectivity receiver and disable the location receiver.
	    // There's no point trying to poll the server for updates if we're not connected, and the 
	    // connectivity receiver will turn the location-based updates back on once we have a connection.
	    if (!isConnected) {
	    	PackageManager pm = getPackageManager();
	      
	    	//ComponentName connectivityReceiver = new ComponentName(this, ConnectivityChangedReceiver.class);
	    	//ComponentName locationReceiver = new ComponentName(this, LocationChangedReceiver.class);
	    	ComponentName passiveLocationReceiver = new ComponentName(this, PassiveLocationChangedReceiver.class);

//	    	pm.setComponentEnabledSetting(connectivityReceiver,
//	    			PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
//	    			PackageManager.DONT_KILL_APP);
//	            
//	    	pm.setComponentEnabledSetting(locationReceiver,
//	    			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
//	    			PackageManager.DONT_KILL_APP);
	      
	    	pm.setComponentEnabledSetting(passiveLocationReceiver,
	    			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
	    			PackageManager.DONT_KILL_APP);
	    } else {
	    	JSONObject jsonobj = new JSONObject();
			ConnectionHelper conn;
			
			try {				 
				jsonobj.put("latitude", location.getLatitude());
				jsonobj.put("longitude", location.getLongitude());
				conn = new ConnectionHelper("hidden_locations", jsonobj);
				JSONObject recvdjson = conn.performRequest();
				if (recvdjson.has("error")) {
					return;				
				}
				JSONArray markers = recvdjson.getJSONArray("markers");
				for(int i=0;i<markers.length();i++) {
					JSONObject e = markers.getJSONObject(i);
					double distance = e.getDouble("distance") * 1000;
					if (distance <= radius) {
						new ProximityIntentReceiver().launchNotify(e.getString("name"), getApplicationContext());
					}				
				}
				
			} catch (JSONException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
	}

}
