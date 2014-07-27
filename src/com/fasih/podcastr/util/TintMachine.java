package com.fasih.podcastr.util;

import java.util.Random;

import android.content.Context;

import com.fasih.podcastr.R;



public class TintMachine {
	private static int lastSelectedColor = 0;
	private static int[] colorResources = {R.color.translucent_google_io_blue,
									R.color.translucent_google_io_green,
									R.color.translucent_google_io_purple,
									R.color.translucent_google_io_turquoise};
	
	private static Random random = new Random();
	
	public static int getTint(Context context){
		int currentColor = random.nextInt(colorResources.length);
		while(currentColor == lastSelectedColor){
			currentColor = random.nextInt(colorResources.length);
		}
		int color = context.getResources().getColor(colorResources[currentColor]);
		lastSelectedColor = currentColor;
		return color;
	}
}
