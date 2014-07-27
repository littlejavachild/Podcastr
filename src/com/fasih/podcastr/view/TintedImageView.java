package com.fasih.podcastr.view;



import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasih.podcastr.R;
import com.squareup.picasso.Picasso;

public class TintedImageView extends LinearLayout{
	// Used to inflater a layout
	private static final String INF_SERVICE = Context.LAYOUT_INFLATER_SERVICE;
	private LayoutInflater inflater;
	
	// Used to hold references to the UI elements
	private ImageView image = null;
	private View tint = null;
	private TextView title = null;
	private FrameLayout root = null;
	
	// Default values for the attributes
	private int tintColorAttr = 0; // to be initialized in style();
	
	//------------------------------------------------------------------------------
	public TintedImageView(Context context, AttributeSet attr){
		super(context,attr);
		init();
		style(context, attr);
	}
	//------------------------------------------------------------------------------
	public TintedImageView(Context context){
		super(context);
		init();
	}
	//------------------------------------------------------------------------------
	public TintedImageView(Context context, AttributeSet attr, int defStyle){
		super(context, attr, defStyle);
		init();
		style(context, attr);
	}
	//------------------------------------------------------------------------------
	private void init(){
		inflater = (LayoutInflater) getContext().getSystemService(INF_SERVICE);
		inflater.inflate(R.layout.tinted_image_view, this, true);
		
		image = (ImageView) findViewById(R.id.image);
		tint = (View) findViewById(R.id.tint);
		title = (TextView) findViewById(R.id.title);
		root = (FrameLayout) findViewById(R.id.root);
	}
	//------------------------------------------------------------------------------
	private void style(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs, 
				R.styleable.TintedImageView, 
				0, 0);
		
	}
	//------------------------------------------------------------------------------
	public void setTintColor(int color){
		tint.setBackgroundColor(color);
	}
	//------------------------------------------------------------------------------
	public void setNoTint(){
		int transparent = getResources().getColor(android.R.color.transparent);
		tint.setBackgroundColor(transparent);
	}
	//------------------------------------------------------------------------------
	public void setTitle(String newTitle){
		title.setText(newTitle);
	}
	//------------------------------------------------------------------------------
	public String getTitle(){
		return title.getText().toString();
	}
	//------------------------------------------------------------------------------
	public void setTextColor(int color){
		title.setTextColor(color);
	}
	//------------------------------------------------------------------------------
	public void setTypeface(Typeface typeface, int style){
		title.setTypeface(typeface, style);
	}
	//------------------------------------------------------------------------------
	public void setTypeface(Typeface typeface){
		title.setTypeface(typeface);
	}
	//------------------------------------------------------------------------------
	public void setImage(int resId){
		Picasso.with(getContext()).load(resId).error(R.drawable.ic_launcher).into(image);
	}
	//------------------------------------------------------------------------------
	public void setImage(File file){
		Picasso.with(getContext()).load(file).error(R.drawable.ic_launcher).into(image);
	}
	//------------------------------------------------------------------------------
	public void setImage(String url){
		Uri uri = Uri.parse(url);
		Picasso.with(getContext()).load(uri).error(R.drawable.ic_launcher).into(image);
	}
	//------------------------------------------------------------------------------
}
