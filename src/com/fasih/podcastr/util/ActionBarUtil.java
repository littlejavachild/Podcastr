package com.fasih.podcastr.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;

public class ActionBarUtil {
	
	public static void setActionBarColor(ActionBar bar, Context context){
		// Set ActionBar color
		bar.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.google_io_blue)));	
	}
	
	public static void setActionBarTypeface(Activity activity){
		int titleId = activity.getResources().getIdentifier("action_bar_title", "id",
	            "android");
	    TextView yourTextView = (TextView) activity.findViewById(titleId);
	    yourTextView.setTypeface(PodcastrApplication.newInstance().getTypeface(),Typeface.BOLD);
	}
}
