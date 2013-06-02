package com.insomniware.kingapp.helpers;

import com.google.android.gms.maps.model.LatLng;

public class LocationMarker {

    private String points;
    private long id;
    private double latitude;
    private double longitude;
    private String name;
    private int ex_id;
    private String hint;

    public LocationMarker(int ex_id, double latitude, double longitude, String name, String hint, String points) {
		this.ex_id = ex_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
        this.hint = hint;
        this.points = points;
	}
	
	public long getId() {
		return id;
	}

    public String getPoints() {
        return points;
    }

    public String getHint() {
        return hint;
    }

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public LatLng getCoordinates() {
		return new LatLng(latitude, longitude);		
	}
	
	public int getExId() {
		return ex_id;
	}

}
