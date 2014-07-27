package com.fasih.podcastr.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;

public class PleaseWaitDialogFragment extends DialogFragment{
	
	public PleaseWaitDialogFragment(){
		
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflateTextView(inflater, container);
	}
	
	private View inflateTextView(LayoutInflater inflater,ViewGroup container){
		View view = inflater.inflate(R.layout.fragment_please_wait_text, container, false);
//		TextView loading = (TextView) view.findViewById(R.id.loading);
//		loading.setTypeface(PodcastrApplication.newInstance().getTypeface());
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		return view;
	}
	
}
