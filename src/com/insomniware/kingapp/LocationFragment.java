package com.insomniware.kingapp;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocationFragment extends Fragment {
	
	public LocationFragment() {
		
	}
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
//        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
//		FragmentTransaction fragmentTransaction =
//		         getFragmentManager().beginTransaction();
//		fragmentTransaction.add(this.getId(), mMapFragment);
//		fragmentTransaction.commit();
//		mMapFragment.getMap();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_layout,
				container, false);
		
		return rootView;
	}

}
