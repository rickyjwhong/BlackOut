package com.rickster.blackout;

import java.util.ArrayList;
import java.util.Date;

import android.location.Location;

public class Run {
	
	private ArrayList<Location> mLocations;
	private long mId;
	private Date mDate;
	private String mText;
	private boolean mClosed;
	private long mFrequency;
	
	public Run(){
		mId = -1;
		mDate = new Date();
		mLocations = new ArrayList<Location>();
		mClosed = false;
	}
	
	@Override
	public String toString(){
		return mText;
	}	
	
	public long getFrequency() {
		return mFrequency;
	}

	public void setFrequency(long frequency) {
		mFrequency = frequency;
	}
	
	public boolean isClosed() {
		return mClosed;
	}

	public void setClosed(boolean closed) {
		mClosed = closed;
	}

	public int getDurationSeconds(long end){
		return (int) (end - mDate.getTime()) / 1000;
	}
	
	public static String getFormattedTime(int seconds){
		int s = seconds % 60;
		int m = (seconds - s) / 60 % 60;
		int h = (seconds - m * 60 - s) / 3600;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
	}

	public void addLocation(Location loc){
		mLocations.add(loc);
	}
	
	public void removeLocation(Location loc){
		mLocations.remove(loc);
	}
	
	public ArrayList<Location> getLocations() {
		return mLocations;
	}

	public void setLocations(ArrayList<Location> locations) {
		mLocations = locations;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}
	
}
