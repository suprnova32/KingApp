package com.insomniware.kingapp;


import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocationFragment extends Fragment {
	
	private static GoogleMap mMap;
	private SupportMapFragment mMapFragment;
	private static final int ONE_MINUTE = 1000 * 60;
	
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
	public void onResume() {
		super.onResume();
		MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
		
	}
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
        	mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        	mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(); 
            }
        }
    }
	
	private void setUpMap() {		
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
			
			@Override
			public void onMyLocationChange(Location location) {
				updateMap(location);				
			}
		});
        
        MainPageActivity.locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localLocationListener, null);
        MainPageActivity.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_MINUTE, 500, localLocationListener);
    }
	
	private static void updateMap(Location location){
		Location myLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
        		new LatLng( myLocation.getLatitude(), myLocation.getLongitude())));		
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_layout,
				container, false);
		setUpMapIfNeeded();
		
		return rootView;
	}

}
