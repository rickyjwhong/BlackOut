package com.example.blackout;



import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RunListFragment extends ListFragment {
	
	private static final String TAG = "RunListFragment";
	private RunManager sRunManager;
	private ArrayList<Run> mRuns;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		sRunManager = RunManager.get(getActivity());
		mRuns = sRunManager.getRuns();
		ArrayAdapter<Run> adapter = new ArrayAdapter<Run>(getActivity(), android.R.layout.simple_list_item_1, mRuns);
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id){
		Run run = (Run) getListAdapter().getItem(pos);
		Intent i = new Intent(getActivity(), RunActivity.class);
		i.putExtra(RunFragment.RUN_EXTRA_ID, run.getId());
		startActivity(i);
		//click for run
	}
	
}
