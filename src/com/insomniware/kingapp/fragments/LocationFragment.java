package com.insomniware.kingapp.fragments;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.insomniware.kingapp.MainPageActivity;
import com.insomniware.kingapp.R;
import com.insomniware.kingapp.CheckInActivity.CheckInTask;
import com.insomniware.kingapp.R.id;
import com.insomniware.kingapp.R.layout;
import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.helpers.ConnectionHelper;
import com.insomniware.kingapp.helpers.LocationMarker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class LocationFragment extends Fragment {
	
	public static GoogleMap mMap;
	private SupportMapFragment mMapFragment;
	public static Location latestLocation = MainPageActivity.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	private static MarkTask mMarkTask = null;
	private static List<LocationMarker> mMarkers;
	private static Context myContext;
	private static int requestCode = 0;
	private static Activity myActivity;
	public static String INTENT_FILTER = "com.insomniware.kingapp.Notify";
	
	public LocationListener localLocationListener = new LocationListener() {

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}

		@Override
		public void onLocationChanged(Location location) {
			latestLocation = location;
			Toast.makeText(getActivity(), "Updated location", Toast.LENGTH_LONG).show();
			updateMap(location);
			markMap();
		}
	  };
	
	public LocationFragment() {
		
	}
	
		
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        // Register the listener with the Location Manager to receive location updates
        myContext = getActivity().getApplicationContext();
        myActivity = getActivity();
        //IntentFilter filter = new IntentFilter(INTENT_FILTER);
		//myActivity.registerReceiver(new ProximityIntentReceiver(), filter);
 		MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
 		MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MyConstants.FIVE_MINUTES, 500, localLocationListener);

    }
	
	@Override
	public void onPause() {
		super.onPause();
		MainPageActivity.locationManager.removeUpdates(localLocationListener);		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		forceMapReload(getFragmentManager());
		MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
		MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MyConstants.FIVE_MINUTES, 500, localLocationListener);
	}
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
        	mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        	mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap("First setup");
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
            }
        } else {
        	setUpMap("Another setup");
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));        	
        }
    }
	
	public void forceMapReload(FragmentManager manager) {
		mMapFragment = (SupportMapFragment) manager.findFragmentById(R.id.map);
		if(mMapFragment != null){
			mMap = mMapFragment.getMap();
	    	setUpMap("Forced reload");			
		}
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
		
	}
	
	public static void setUpMap(String mes) {
		Log.e("Map Activity", mes);
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
//        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
//			
//			@Override
//			public void onMyLocationChange(Location location) {
//				updateMap(location);				
//			}
//		});
        
        markMap();       
    }
	
	private static void updateMap(Location location) {
		Location myLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
        		new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
	}
	
	private static void markMap() {
		mMarkTask = new MarkTask();
		mMarkTask.execute((Void) null);		
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_layout,
				container, false);
		setUpMapIfNeeded();
		
		return rootView;
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public static class MarkTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONObject jsonobj = new JSONObject();
			ConnectionHelper conn;
			mMarkers = new ArrayList<LocationMarker>();
			
			try {				 
				jsonobj.put("latitude", latestLocation.getLatitude());
				jsonobj.put("longitude", latestLocation.getLongitude());
				conn = new ConnectionHelper("hidden_locations", jsonobj);
				JSONObject recvdjson = conn.performRequest();
				if (recvdjson.has("error")) {
					return false;				
				}
				JSONArray markers = recvdjson.getJSONArray("markers");
				for(int i=0;i<markers.length();i++) {
					JSONObject e = markers.getJSONObject(i);
					LocationMarker mark = new LocationMarker(e.getInt("id"), 
							e.getDouble("latitude"), e.getDouble("longitude"), e.getString("name"));
					mMarkers.add(mark);					
				}
				
			} catch (JSONException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean status) {
			mMarkTask = null;
			mMap.clear();
			for(LocationMarker lm : mMarkers) {
				mMap.addMarker(new MarkerOptions().position(lm.getCoordinates()).title(lm.getInfo()));
				mMap.addCircle(new CircleOptions().center(lm.getCoordinates()).radius(50f).strokeWidth(1).strokeColor(0x809fff).fillColor(0x358097f5));
//				Bundle extras = new Bundle();
//			    extras.putString("name", lm.getInfo());
//			    extras.putInt("id", requestCode);
//				Intent myIntent = new Intent(INTENT_FILTER);
//				myIntent.putExtra(INTENT_FILTER, extras);
//				PendingIntent proximityIntent = PendingIntent.getBroadcast(myContext, requestCode, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//				MainPageActivity.locationManager.addProximityAlert(lm.getCoordinates().latitude, lm.getCoordinates().longitude, 60f, 600000, proximityIntent);
				requestCode++;
			}
		}

		@Override
		protected void onCancelled() {
			mMarkTask = null;
		}
	}

}