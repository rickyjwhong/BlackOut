package com.rickster.blackout;

import android.location.Location;

public class RunLocation extends Location{
	
	private long mId;
	private long mRunId;
	private String mNearBy;
	
	public RunLocation(Location loc){
		super(loc);
		mId = -1;
		mRunId = -1;
	}	
	
	public long getRunId() {
		return mRunId;
	}
	
	public void setRunId(long runId) {
		mRunId = runId;
	}

	public long getId() {
		return mId;
	}	
	
	public void setId(long id) {
		mId = id;
	}	
	
	public String getNearBy() {
		return mNearBy;
	}

	public void setNearBy(String nearBy) {
		mNearBy = nearBy;
	}
	
}
