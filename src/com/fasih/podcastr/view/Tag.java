package com.fasih.podcastr.view;



import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasih.podcastr.R;

public class Tag extends LinearLayout {
	// Used to hold a reference to the UI elements
	private View mDot = null;
	private TextView mTagText = null;
	
	// Default values for attributes
	private boolean dotVisibleAttr = true;
	private String tagTextAttr = null;
	private int dotColorAttr = 0; // to be initialized later in style();
	
	// Used to inflater a layout
	private static final String INF_SERVICE = Context.LAYOUT_INFLATER_SERVICE;
	private LayoutInflater inflater;
	//------------------------------------------------------------------------------
	public Tag(Context context, AttributeSet attr){
		super(context,attr);
		init();
		style(context, attr);
	}
	//------------------------------------------------------------------------------
	public Tag(Context context){
		super(context);
		init();
	}
	//------------------------------------------------------------------------------
	public Tag(Context context, AttributeSet attr, int defStyle){
		super(context, attr, defStyle);
		init();
		style(context, attr);
	}
	//------------------------------------------------------------------------------
	private void init() {
		// TODO Auto-generated method stub
		inflater = (LayoutInflater) getContext().getSystemService(INF_SERVICE);
		inflater.inflate(R.layout.tag, this, true);
		
		// Retrieve a reference to the UI elements
		mDot = (View) findViewById(R.id.dot);
		mTagText = (TextView) findViewById(R.id.tag_text);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to set the text of the tag
	 * @param text The text to display
	 */
	public void setText(String text){
		mTagText.setText(text);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to get the text of the tag
	 * @return
	 */
	public String getText(){
		return mTagText.getText().toString();
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to set the size of the text
	 * @param size
	 */
	public void setTextSize(float size) {
		mTagText.setTextSize(size);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to hide the text view
	 */
	public void hideText(){
		mTagText.setVisibility(View.GONE);
	}
	//------------------------------------------------------------------------------
	/**
	 * Show the tag text
	 */
	public void showText(){
		mTagText.setVisibility(View.VISIBLE);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to set the Typeface of the tag
	 * @param typeface
	 * @param style
	 */
	public void setTypeface(Typeface typeface, int style){
		mTagText.setTypeface(typeface, style);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to set the Typeface of the tag
	 * @param typeface
	 */
	public void setTypeface(Typeface typeface){
		mTagText.setTypeface(typeface);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to set the color of the dot
	 * @param color The new color of the dot
	 */
	public void setDotColor(int color){
		GradientDrawable shape = (GradientDrawable) mDot.getBackground();
		shape.setColor(color);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to hide the dot
	 */
	public void hideDot(){
		mDot.setVisibility(View.GONE);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to show the dot
	 */
	public void showDot(){
		mDot.setVisibility(View.VISIBLE);
	}
	//------------------------------------------------------------------------------
	private void style(Context context, AttributeSet attrs){
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs, 
				R.styleable.Tag, 
				0, 0);
		
		tagTextAttr = getResources().getString(android.R.string.untitled);
		dotColorAttr = getResources().getColor(R.color.google_io_blue);
		
		try{
			tagTextAttr = a.getString(R.styleable.Tag_tagText);
			dotColorAttr = a.getColor(R.styleable.Tag_dotColor, dotColorAttr);
			dotVisibleAttr = a.getBoolean(R.styleable.Tag_dotVisible, dotVisibleAttr);
		}finally{
			a.recycle();
		}
		
		if(tagTextAttr == null)
			// If the attribute has not been defined in XML, 
			// We set the default value
			tagTextAttr = getResources().getString(android.R.string.untitled);
		else
			setText(tagTextAttr);
		
		if(!dotVisibleAttr)
			// Default is visible so we only check for the 
			// condition when we have to hide the dot
			hideDot();
		
		// and we set the color and text size
		setDotColor(dotColorAttr);
	}
}
