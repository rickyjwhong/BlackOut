package com.rickster.blackout;



import java.text.DateFormat;
import java.util.ArrayList;

import com.rickster.blackout.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RunListFragment extends ListFragment {
	
	private static final String TAG = "RunListFragment";
	private RunManager sRunManager;
	private ArrayList<Run> mRuns;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		sRunManager = RunManager.get(getActivity());
		mRuns = sRunManager.getRuns();
		BlackOutListAdapter adapter = new BlackOutListAdapter(mRuns);
		setListAdapter(adapter);
		
		boolean started = sRunManager.isLocationUpdateOn();
		if(started) getActivity().getActionBar().setTitle(R.string.blackout_current);
		else getActivity().getActionBar().setTitle(R.string.app_name);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.menu_add:
				if(sRunManager.isLocationUpdateOn()) sRunManager.stopLocationUpdates(sRunManager.getCurrentRun());		
				Intent i = new Intent(getActivity(), MainActivity.class);
				startActivity(i);
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id){
		Run run = (Run) getListAdapter().getItem(pos);
		Intent i = new Intent(getActivity(), RunActivity.class);
		i.putExtra(RunFragment.RUN_EXTRA_ID, run.getId());
		startActivity(i);
		//click for run
	}
	
	@SuppressLint("DefaultLocale")
	private class BlackOutListAdapter extends ArrayAdapter<Run> {
		
		public BlackOutListAdapter(ArrayList<Run> runs){
			super(getActivity(), 0, runs);
		}
		
		@Override
		public View getView(int position, View view , ViewGroup parent){
			
			View v = getActivity().getLayoutInflater().inflate(R.layout.run_list_row, parent, false);
			
			Run mRun = getItem(position);
			
			TextView mTitle = (TextView) v.findViewById(R.id.rr_title);
			mTitle.setText(mRun.getText().toUpperCase());
			
			TextView mDate = (TextView) v.findViewById(R.id.rr_date);
			mDate.setText(DateFormat.getDateTimeInstance().format(mRun.getDate()));
			
			ImageView mImage = (ImageView) v.findViewById(R.id.rr_active);
			mImage.setImageResource((mRun.isClosed()) ? R.drawable.empty_drink : R.drawable.full_drink);
			
			return v;
			
		}
		
	}
	
}
