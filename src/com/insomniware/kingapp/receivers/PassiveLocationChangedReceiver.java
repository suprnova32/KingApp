package com.insomniware.kingapp.receivers;

import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.services.LocationsUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class PassiveLocationChangedReceiver extends BroadcastReceiver {
	
	protected static String TAG = "PassiveLocationChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String key = LocationManager.KEY_LOCATION_CHANGED;
	    Location location = null;
	    if (intent.hasExtra(key)) {
	        // This update came from Passive provider, so we can extract the location
	        // directly.
	        location = (Location) intent.getExtras().get(key);
	        Log.d(TAG, "Passively got location.");
	        if (location != null) {
	        	Log.d(TAG, "Passively updating place list.");
	        	Intent updateServiceIntent = new Intent(context, LocationsUpdateService.class);
		        updateServiceIntent.putExtra(MyConstants.EXTRA_KEY_LOCATION, location);
		        updateServiceIntent.putExtra(MyConstants.EXTRA_KEY_RADIUS, MyConstants.DEFAULT_RADIUS);
		        context.startService(updateServiceIntent);	        	
	        }
	    }
		
	}

}
