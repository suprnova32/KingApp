package com.insomniware.kingapp.fragments;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.insomniware.kingapp.MainPageActivity;
import com.insomniware.kingapp.R;
import com.insomniware.kingapp.extras.MyConstants;
import com.insomniware.kingapp.helpers.ConnectionHelper;
import com.insomniware.kingapp.helpers.LocationMarker;
import com.insomniware.kingapp.helpers.GeofenceUtils;
import com.insomniware.kingapp.helpers.LocationUtils;
import com.insomniware.kingapp.helpers.SimpleGeofence;

import android.content.IntentSender;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class LocationFragment extends Fragment implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener  {
	
	public static GoogleMap mMap;
	private SupportMapFragment mMapFragment;
	public static Location latestLocation = MainPageActivity.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	private static MarkTask mMarkTask = null;
	private static List<LocationMarker> mMarkers;
    private static Context context;

    //Location Services
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    // Stores the current instantiation of the location client in this object
    public static LocationClient mLocationClient;


    //Geofencing
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
	
//	public LocationListener localLocationListener = new LocationListener() {
//
//	    public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//	    public void onProviderEnabled(String provider) {}
//
//	    public void onProviderDisabled(String provider) {}
//
//		@Override
//		public void onLocationChanged(Location location) {
//			latestLocation = location;
//			Toast.makeText(getActivity(), "Updated location", Toast.LENGTH_LONG).show();
//			updateMap(location);
//			markMap();
//		}
//	  };
	
	public LocationFragment() {

	}

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */

        mLocationClient.connect();

    }
	
		
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        // Register the listener with the Location Manager to receive location updates
        context = getActivity();

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        mLocationClient = new LocationClient(context, this, this);

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        //mLocationClient = new LocationClient(context, this, this);
 		//MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
 		//MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MyConstants.FIVE_MINUTES, 500, localLocationListener);

    }

    @Override
	public void onPause() {
		super.onPause();
		//MainPageActivity.locationManager.removeUpdates(localLocationListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		forceMapReload(getFragmentManager());
		//MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
		//MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MyConstants.FIVE_MINUTES, 500, localLocationListener);
	}

    /*
     * Methods for the Play Location Services
     */
    @Override
    public void onConnected(Bundle bundle) {
        startPeriodicUpdates();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        LocationFragment.latestLocation = location;
        Toast.makeText(context, "Updated location", Toast.LENGTH_LONG).show();
        LocationFragment.updateMap(location);
        LocationFragment.markMap();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            //showErrorDialog(connectionResult.getErrorCode());
        }

    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        //mConnectionState.setText(R.string.location_requested);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        //mConnectionState.setText(R.string.location_updates_stopped);
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
        
        markMap();       
    }
	
	public static void updateMap(Location location) {
		Location myLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
        		new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
	}
	
	public static void markMap() {
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
	 * Represents an asynchronous task used to mark the map.
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
				JSONObject recvdjson = conn.performRequest(context);
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
            int fence_id = MainPageActivity.settings.getInt("fences_num", 0);
            MainPageActivity.mGeofenceIdsToRemove = new ArrayList<String>();
            if (fence_id > 0) {
                for (int i = 1; i <= fence_id; i++) {
                    MainPageActivity.mPrefs.clearGeofence(String.valueOf(i));
                    MainPageActivity.mGeofenceIdsToRemove.add(String.valueOf(i));
                }
                MainPageActivity.mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;
                try {
                    MainPageActivity.mGeofenceRemover.removeGeofencesById(MainPageActivity.mGeofenceIdsToRemove);
                    fence_id = 1;
                    MainPageActivity.editor.putInt("fences_num", 0);
                    MainPageActivity.editor.commit();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (UnsupportedOperationException e) {
                    // Notify user that previous request hasn't finished.
                }
            }
            MainPageActivity.mCurrentGeofences = new ArrayList<Geofence>();
			mMarkTask = null;
			mMap.clear();
			for(LocationMarker lm : mMarkers) {
                SimpleGeofence gf = new SimpleGeofence(
                        String.valueOf(fence_id),
                        lm.getCoordinates().latitude,
                        lm.getCoordinates().longitude,
                        Float.valueOf(MyConstants.DEFAULT_RADIUS),
                        // Set the expiration time
                        GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                        // Detect only entry and exit transitions
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                );
                // Store this flat version in SharedPreferences
                MainPageActivity.mPrefs.setGeofence(String.valueOf(fence_id), gf);
                MainPageActivity.mCurrentGeofences.add(gf.toGeofence());
				mMap.addMarker(new MarkerOptions().position(lm.getCoordinates()).title(lm.getInfo()));
				mMap.addCircle(new CircleOptions().center(lm.getCoordinates()).radius(50f).strokeWidth(1).strokeColor(0x809fff).fillColor(0x358097f5));
                fence_id++;
			}
            // Start the request. Fail if there's already a request in progress
            MainPageActivity.mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;
            try {
                // Try to add geofences
                if (!MainPageActivity.mCurrentGeofences.isEmpty())
                    MainPageActivity.mGeofenceRequester.addGeofences(MainPageActivity.mCurrentGeofences);
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.

            }
            MainPageActivity.editor.putInt("fences_num", fence_id);
            MainPageActivity.editor.commit();
		}

		@Override
		protected void onCancelled() {
			mMarkTask = null;
		}
	}

}
