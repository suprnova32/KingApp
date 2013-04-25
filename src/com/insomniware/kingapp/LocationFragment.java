package com.insomniware.kingapp;


import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocationFragment extends Fragment {
	
	public static GoogleMap mMap;
	private SupportMapFragment mMapFragment;
	static final int ONE_MINUTE = 1000 * 60;
	
	public static LocationListener localLocationListener = new LocationListener() {

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}

		@Override
		public void onLocationChanged(Location location) {
			updateMap(location);						
		}
	  };
	
	public LocationFragment() {
		
	}
	
		
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
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
		//MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE, 500, localLocationListener);
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
        
        markMap(mMap);       
    }
	
	private static void updateMap(Location location) {
		Location myLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
        		new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
	}
	
	private static void markMap(GoogleMap map) {
		
		mMap.addMarker(new MarkerOptions().position(new LatLng(48.7754181, 9.1817588)).title("First Marker"));
		
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_layout,
				container, false);
		setUpMapIfNeeded();
		MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
        MainPageActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE, 500, localLocationListener);
		
		return rootView;
	}

}
