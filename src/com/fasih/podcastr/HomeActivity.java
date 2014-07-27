package com.fasih.podcastr;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.fasih.podcastr.adapter.CategorySpinnerAdapter;
import com.fasih.podcastr.adapter.NavigationDrawerAdapter;
import com.fasih.podcastr.fragment.PodcastGridFragment;
import com.fasih.podcastr.util.ActionBarUtil;
import com.fasih.podcastr.util.PodcastUtil;

public class HomeActivity extends FragmentActivity {
	private PodcastGridFragment fragment = null;
	private NavigationDrawerAdapter adapter = null;
	private ListView leftDrawer = null;
	private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private View customActionBarView = null;
    private Spinner categories = null;
    private CategorySpinnerAdapter spinnerAdapter = null;
    private ImageButton search = null;
    private ImageButton cancel = null;
    private LinearLayout root = null;
    private EditText searchString = null;
    // Used to know whether the user is in search mode
    private boolean searchModeEnabled = false;
    
    private int spinnerPosition = -1;
	//------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		// Set the typeface for ActionBar
		ActionBarUtil.setActionBarTypeface(HomeActivity.this);
		// Set the custom view for ActionBar when drawer is closed
		setDrawerClosedCustomActionBarView();
		// Retrieve reference, create instances, etc.
		init();
		// Set the adapter for the sliding menu
		setLeftDrawerAdapter();
		// Set the drawer listener
		setDrawerListener();
		// Because if we don't do this,
		// we will end up with a timing issue.
		// Ideally, notifying the data set change should happen
		// in the AsyncTask. But I am too lazy to code
		// and thus this work around
		new CountDownTimer(4000, 4000) {
		     public void onTick(long millisUntilFinished) { /** NOTHING */ }
		     public void onFinish() {
		    	 spinnerAdapter.notifyDataSetChanged(); 
		     }
		  }.start();
		// Set the listener for the spinner
		setSpinnerItemSelectionListener();
		// Set the listener for the search button
		setSearchButtonListener();
		// Set the listener for the cancel button
		setCancelButtonListener();
		
	}
	//------------------------------------------------------------------------------
	@Override
	public void onResume(){
		super.onResume();
	}
	//------------------------------------------------------------------------------
	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
	}
	//------------------------------------------------------------------------------
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
	//------------------------------------------------------------------------------
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
	//------------------------------------------------------------------------------
	private void init(){
		// Get the fragment
		fragment = (PodcastGridFragment) getSupportFragmentManager().
										 findFragmentById(R.id.gridFragment);
		fragment.setLoaderResources(getSupportFragmentManager(), getAssets());
		
		// Retrieve the sliding menu and create its adapter
		leftDrawer = (ListView) findViewById(R.id.left_drawer);
		adapter = new NavigationDrawerAdapter(HomeActivity.this);
		// Retrieve the drawer layout and create a drawer toggle for it
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ){
			
			/** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setDrawerClosedCustomActionBarView();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setDrawerOpenedCustomActionBarView();
            }
		};
		// Set the adapter for the spinner
		categories = (Spinner) customActionBarView.findViewById(R.id.categories);
		spinnerAdapter = new CategorySpinnerAdapter();
		categories.setAdapter(spinnerAdapter);
		// Retrieve the references to the buttons in ActionBar
		// and the LinearLayout holder
		search = (ImageButton) findViewById(R.id.search);
		cancel = (ImageButton) findViewById(R.id.cancel);
		root = (LinearLayout) findViewById(R.id.root);
		searchString = (EditText) findViewById(R.id.searchString);
		// Set the font
		searchString.setTypeface(PodcastrApplication.newInstance().getTypeface(),Typeface.BOLD);
	}
	//------------------------------------------------------------------------------
	private void setLeftDrawerAdapter(){
		leftDrawer.setAdapter(adapter);
	}
	//------------------------------------------------------------------------------
	private void setDrawerListener(){
		drawerLayout.setDrawerListener(drawerToggle);
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to show the view when the sliding menu is completely closed
	 */
	private void setDrawerClosedCustomActionBarView(){
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		if(customActionBarView == null){
			LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			customActionBarView = inflator.inflate(R.layout.activity_home_action_bar,null);
			ab.setCustomView(customActionBarView);
		}
		// If the user was in search mode when the drawer was opened,
		// we should make the EditText get the focus
		if(searchString != null){
			if(searchString.isFocusable()){
				searchString.requestFocus();
			}
		}
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to display the view when the drawer is opened
	 */
	private void setDrawerOpenedCustomActionBarView(){
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setDisplayShowCustomEnabled(false);
		ActionBarUtil.setActionBarTypeface(HomeActivity.this);
		cancel.performClick();
	}
	//------------------------------------------------------------------------------
	private void setSpinnerItemSelectionListener(){
		categories.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id) {
				spinnerPosition = position;
				String category = (String) PodcastUtil.getCategories().get(position);
				fragment.sortByCategory(category);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) { /** NOTHING */ }
		});
	}
	//------------------------------------------------------------------------------
	private void setSearchButtonListener(){
		search.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// We are officially in search mode
				searchModeEnabled = true;
				// Hide the search button and spinner
				search.setVisibility(View.GONE);
				categories.setVisibility(View.GONE);
				// Load the animation
				Animation growInLength = AnimationUtils.loadAnimation(HomeActivity.this, 
						  R.anim.grow_in_length);
				// Show the linear layout
				root.setVisibility(View.VISIBLE);
				// Animate the EditText
				searchString.startAnimation(growInLength);
				// Set focus on the EditText
				searchString.requestFocus();
				// Show the keyboard
				showKeyboard();
			}
		});
	}
	//------------------------------------------------------------------------------
	private void setCancelButtonListener(){
		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// We are officially out of search mode
				searchModeEnabled = false;
				// Hide the linear layout
				root.setVisibility(View.GONE);
				// Show the spinner and search button
				search.setVisibility(View.VISIBLE);
				categories.setVisibility(View.VISIBLE);
				// Clear the search string
				searchString.setText("");
				// Hide keyboard
				hideKeybaord();
			}
		});
	}
	//------------------------------------------------------------------------------
	private void hideKeybaord(){
		if(searchString != null){
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(searchString.getWindowToken(), 0);
		}
	}
	//------------------------------------------------------------------------------
	private void showKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
	}
	//------------------------------------------------------------------------------
	@Override
	public void onBackPressed(){
		if(searchModeEnabled){
			// Programatically click the cancel button
			cancel.performClick();
		}else{
			super.onBackPressed();
		}
	}
	//------------------------------------------------------------------------------
}
