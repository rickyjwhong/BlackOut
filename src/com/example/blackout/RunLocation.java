package com.example.blackout;

import android.location.Location;

public class RunLocation extends Location{
	
	private String mNearBy;	

	public RunLocation(Location loc){
		super(loc);
	}
	
	public String getNearBy() {
		return mNearBy;
	}

	public void setNearBy(String nearBy) {
		mNearBy = nearBy;
	}
	
}
