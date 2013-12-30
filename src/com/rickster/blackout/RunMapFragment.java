package com.rickster.blackout;

import java.text.DateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RunMapFragment extends SupportMapFragment {
	
	private static final String TAG = "RunMapFragment";
	private static final String ARG_RUN_ID = "RUN_ID";
	
	private GoogleMap mGoogleMap;
	private ArrayList<RunLocation> mRunLocations;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null){
			long runId = args.getLong(ARG_RUN_ID);
			if(runId != -1){				
				mRunLocations = RunManager.get(getActivity()).getRunLocations(runId);				
			}
		}		
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		mGoogleMap = getMap();
		mGoogleMap.setMyLocationEnabled(true);
		
		
		if(isNetworkAvailable())
			updateUI();
		
		return v;
	}
	

	public void updateUI(){	
		if(mGoogleMap == null || mRunLocations == null) return;
		
		PolylineOptions line = new PolylineOptions();
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		
		for(RunLocation rl : mRunLocations){
			LatLng latLng = new LatLng(rl.getLatitude(), rl.getLongitude());

			String date = DateFormat.getTimeInstance(DateFormat.SHORT).format(rl.getTime());
			String text = rl.getNearBy();
			MarkerOptions markerOption = new MarkerOptions().position(latLng).title(text).snippet(date);
			mGoogleMap.addMarker(markerOption).showInfoWindow();
			
			line.add(latLng);
			latLngBuilder.include(latLng);
		}
		
		mGoogleMap.addPolyline(line);
		
		LatLngBounds latLngBounds = latLngBuilder.build();	
		final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, 30);
		//mGoogleMap.moveCamera(cu);
		
		mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition arg0) {
				// TODO Auto-generated method stub
				mGoogleMap.animateCamera(cu);
				mGoogleMap.setOnCameraChangeListener(null);
			}
			
		});
	}
	
	private boolean isNetworkAvailable(){
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	public static RunMapFragment newInstance(long runId){
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunMapFragment fragment = new RunMapFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
}
