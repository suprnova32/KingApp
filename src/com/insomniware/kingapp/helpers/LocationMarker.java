package com.insomniware.kingapp.helpers;

import com.google.android.gms.maps.model.LatLng;

public class LocationMarker {
	
	public LocationMarker(int ex_id, double latitude, double longitude, String info) {
		this.ex_id = ex_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.info = info;		
	}
	
	private long id;
	private double latitude;
	private double longitude;
	private String info;
	private int ex_id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getInfo() {
		return info;
	}
	
	public LatLng getCoordinates() {
		return new LatLng(latitude, longitude);		
	}
	
	public int getExId() {
		return ex_id;
	}

}
