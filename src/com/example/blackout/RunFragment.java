package com.example.blackout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private TextView mLatitude, mLongitude, mTime, mAltitude, mTitle;
	private Location mLastLocation;
	private TableLayout mAllLocationTable;
	private boolean mExistingRun = false;
	
	private BroadcastReceiver mUIReceiver = new LocationReceiver(){
		@Override
		protected void onLocationChanged(Context c, Location loc){
			Log.i(TAG, "Online Location Received: " + loc.getLatitude() + " and " + loc.getAltitude());
			mLastLocation = loc;
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
		//make sure to change this later
		super.onStop();
	}	
	
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
			sRunManager.setCurrentRun(mRun);
			mExistingRun = true;
		}else{
			mRun = new Run();
		}
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
				case R.id.menu_list:
					Intent i = new Intent(getActivity(), RunListActivity.class);
					startActivity(i);
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = inflater.inflate(R.layout.fragment_run, parent, false);	
		
		mTitle = (TextView) v.findViewById(R.id.run_title);
		mTime = (TextView) v.findViewById(R.id.ll_time);
		mLatitude = (TextView) v.findViewById(R.id.ll_latitude);
		mLongitude = (TextView) v.findViewById(R.id.ll_longitude);
		mAltitude = (TextView) v.findViewById(R.id.ll_altitude);
		mAllLocationTable = (TableLayout) v.findViewById(R.id.all_location_table);
		
		mToggleButton = (Button) v.findViewById(R.id.toggleButton);	
		mToggleButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub					
				updateTracker();
				updateUI();
			}
		});
		updateUI();
		
		return v;
		
	}
	
	public void updateTracker(){
		boolean started = sRunManager.isLocationUpdateOn();
		if(started) {
			sRunManager.stopLocationUpdates(mRun);
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
	
	public void updateUI(){
		boolean started = sRunManager.isLocationUpdateOn();
		mToggleButton.setText((started) ? R.string.stop_text : R.string.start_text);	
		
		if(mRun != null)
			mTitle.setText(mRun.getText());
		
		//The Last Location Section
		updateLastLocation();
		
		//The All Location Section
		updateAllLocation();

	}
	
	public void updateLastLocation(){
		
		
		int durationSeconds = 0;
		if(mLastLocation != null){
			mLatitude.setText(Double.toString(mLastLocation.getLatitude()));
			mLongitude.setText(Double.toString(mLastLocation.getLongitude()));
			mAltitude.setText(Double.toString(mLastLocation.getAltitude()));
			durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
		}		
		mTime.setText(Run.getFormattedTime(durationSeconds));
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
			tv2.setText(l.getNearBy());
			
			mAllLocationTable.addView(v, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
		}				
	}
	
	public static RunFragment newInstance(long id){
		Bundle args = new Bundle();
		args.putSerializable(RUN_EXTRA_ID, id);
		
		RunFragment fragment = new RunFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
}
