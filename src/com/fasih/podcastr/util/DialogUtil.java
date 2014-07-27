package com.fasih.podcastr.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.fasih.podcastr.fragment.PleaseWaitDialogFragment;

public class DialogUtil {
	
	private static final DialogFragment pleaseWaitDialog = new PleaseWaitDialogFragment();
	
	/**
	 * Used to show a loading screen
	 * @param fm
	 */
	public static void showLoadingDialog(FragmentManager fm){
		pleaseWaitDialog.setCancelable(false);
		if( !pleaseWaitDialog.isVisible() ){
			pleaseWaitDialog.show(fm,"");
		}
	}
	
	/**
	 * Used to hide the loading screen
	 */
	public static void hideLoadingDialog(){
		if(pleaseWaitDialog.isVisible()){
			pleaseWaitDialog.dismiss();
		}
	}
}
