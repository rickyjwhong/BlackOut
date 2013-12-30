package com.rickster.blackout;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import com.rickster.blackout.R;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RunFragment extends Fragment {
	
	private static final String TAG = "RunFragment";
	private static final int START_SETTING_REQUEST = 1;
	private static final String START_SETTING_DIALOG = "start_setting_dialog";
	public static final String RUN_EXTRA_ID = "run_extra_id";	
	
	private Run mRun;
	private RunManager sRunManager;
	private Button mToggleButton;
	private TextView mLatLonAlt, mTime, mTitle, mLastKnownLocationText, mDuration;
	private RunLocation mLastLocation;
	private TableLayout mAllLocationTable;
	private boolean mExistingRun = false;
	
	private BroadcastReceiver mUIReceiver = new LocationReceiver(){
		@Override
		protected void onLocationChanged(Context c, Location loc){
			Log.i(TAG, "Online Location Received: " + loc.getLatitude() + " and " + loc.getAltitude());
			updateUI();
		}
		
		@Override
		protected void onProviderEnabled(boolean enabled){
			int text = (enabled) ? R.string.gps_enabled : R.string.gps_disabled;
			Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
		}
	};
	
	@Override
	public void onStart(){
		super.onStart();
		getActivity().registerReceiver(mUIReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
	}
	
	@Override
	public void onStop(){
		getActivity().unregisterReceiver(mUIReceiver);
		super.onStop();
	}	
	
	@SuppressLint("ResourceAsColor")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		sRunManager = RunManager.get(getActivity());
		setRetainInstance(true);
		setHasOptionsMenu(true);		
		
		long mId = (Long) getArguments().getSerializable(RUN_EXTRA_ID);
		if(mId != -1){
			mRun = sRunManager.getRun(mId);
			mLastLocation = sRunManager.getLastKnownRunLocation(mRun.getId());
			mExistingRun = true;
		}else{
			mRun = new Run();
		}		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode != Activity.RESULT_OK) return;
		switch(requestCode){
			case START_SETTING_REQUEST:								
				mRun.setText(data.getStringExtra(RunDialog.TITLE_EXTRA));	
				mRun.setFrequency(data.getLongExtra(RunDialog.DURATION_EXTRA, RunDialog.DEFAULT_DURATION));
				sRunManager.startNewRun(mRun);
				mExistingRun = true;
		}
	}
	
	//MENU
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case android.R.id.home:
				Intent i = new Intent(getActivity(), RunListActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_list:
				i = new Intent(getActivity(), RunListActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_add:				
				if(sRunManager.isLocationUpdateOn()) sRunManager.stopLocationUpdates(sRunManager.getCurrentRun());				
				i = new Intent(getActivity(), MainActivity.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
		
	@SuppressLint("ResourceAsColor")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = inflater.inflate(R.layout.fragment_run, parent, false);	
		
		mTitle = (TextView) v.findViewById(R.id.run_title);
		mTime = (TextView) v.findViewById(R.id.ll_time);
		mDuration = (TextView) v.findViewById(R.id.ll_duration);
		mLatLonAlt = (TextView) v.findViewById(R.id.ll_lat_lon_alt);
		mLastKnownLocationText = (TextView) v.findViewById(R.id.ll_known_location);
		
		mAllLocationTable = (TableLayout) v.findViewById(R.id.all_location_table);
		
		mToggleButton = (Button) v.findViewById(R.id.toggleButton);	
		if(mRun != null){
			if(mRun.isClosed()) {
				mToggleButton.setText(R.string.closed_text);
				mToggleButton.setClickable(false);
				mToggleButton.setFocusable(false);
			}else{
				mToggleButton.setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub					
						updateTracker();
						updateUI();
					}
				});
			}
		}	
		
		
		
		updateUI();
		
		return v;
		
	}
	
	public void updateTracker(){
		boolean started = sRunManager.isLocationUpdateOn();
		if(started) {
			sRunManager.stopLocationUpdates(mRun);
			mRun.setClosed(true);
			RunManager.get(getActivity()).updateRun(mRun);
		}else if(mExistingRun){
			Log.i(TAG, "Run with " + mRun.getFrequency() + " has been restarted");
			sRunManager.startLocationUpdates(mRun.getFrequency());
		}else {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			RunDialog dialog = new RunDialog();
			dialog.setTargetFragment(RunFragment.this, START_SETTING_REQUEST);
			dialog.show(fm, START_SETTING_DIALOG);				
		}
	}
	
	@SuppressLint({ "ResourceAsColor", "DefaultLocale" })
	public void updateUI(){		
		
		boolean started = sRunManager.isLocationUpdateOn();
		if(mRun.isClosed()) mToggleButton.setText(R.string.closed_text);
		else mToggleButton.setText((started) ? R.string.stop_text : R.string.start_text);	
		if(started) getActivity().getActionBar().setTitle(R.string.blackout_current);
		else getActivity().getActionBar().setTitle(R.string.app_name);
		
		if(mRun != null)
			if(mRun.getText() != null)
				mTitle.setText(mRun.getText().toUpperCase());
		
		//The Last Location Section
		updateLastLocation();
		
		//The All Location Section
		updateAllLocation();
		
		//map
		displayMap();

	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public void updateLastLocation(){	
		mLastLocation = RunManager.get(getActivity()).getLastKnownRunLocation(mRun.getId());
		int durationSeconds = 0;
		if(mLastLocation != null){
			String latitude = Double.toString(round(mLastLocation.getLatitude(), 2));
			String longitude = Double.toString(round(mLastLocation.getLongitude(), 2));
			String altitude = Double.toString(round(mLastLocation.getAltitude(), 2));
			String string = latitude + " | " + longitude + " | " + altitude;
			mLatLonAlt.setText(string);
			mLastKnownLocationText.setText((CharSequence) ((mLastLocation.getNearBy() != null) ? mLastLocation.getNearBy() : R.string.location_unknown));
			durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
		}		
		mDuration.setText(Run.getFormattedTime(durationSeconds));
		mTime.setText(DateFormat.getDateTimeInstance().format(mRun.getDate()));
	}
	
	@SuppressWarnings("deprecation")
	public void updateAllLocation(){	
		mAllLocationTable.removeAllViews();
		ArrayList<RunLocation> mLocations = sRunManager.getRunLocations(mRun.getId());
		
		for(RunLocation l: mLocations){
			Log.i(TAG, "Location!! " + l.getLongitude() + " - " + new Date(l.getTime()).toString());
			View v = getActivity().getLayoutInflater().inflate(R.layout.all_location_row, null);
			//CHANGE THE NAMES
			TextView tv1 = (TextView) v.findViewById(R.id.tv1);
			TextView tv2 = (TextView) v.findViewById(R.id.tv2);
			
			tv1.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(l.getTime()));
			tv2.setText((CharSequence) ((l.getNearBy() != null) ? l.getNearBy() : R.string.location_unknown));
			
			mAllLocationTable.addView(v, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
		}				
	}
	
	public void displayMap(){		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
		Fragment newFragment = RunMapFragment.newInstance(mRun.getId());
		if(oldFragment != null)
			fm.beginTransaction().replace(R.id.mapFragmentContainer, newFragment).commit();
		else
			fm.beginTransaction().add(R.id.mapFragmentContainer, newFragment).commit();		
	}
	
	public static RunFragment newInstance(long id){
		Bundle args = new Bundle();
		args.putSerializable(RUN_EXTRA_ID, id);		
		RunFragment fragment = new RunFragment();
		fragment.setArguments(args);		
		return fragment;
	}
	
}
