package com.fasih.podcastr;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasih.podcastr.adapter.CategorySpinnerAdapter;
import com.fasih.podcastr.adapter.NavigationDrawerAdapter;
import com.fasih.podcastr.fragment.PodcastGridFragment;
import com.fasih.podcastr.fragment.PodcastGridFragment.OnPodcastClickedListener;
import com.fasih.podcastr.fragment.VideoPlayerFragment;
import com.fasih.podcastr.util.ActionBarUtil;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.FavoriteUtil;
import com.fasih.podcastr.util.PodcastUtil;
import com.fasih.podcastr.util.PrefUtils;

public class HomeActivity extends FragmentActivity implements OnPodcastClickedListener{
	private PodcastGridFragment fragment = null;
	private NavigationDrawerAdapter adapter = null;
	private ListView leftDrawer = null;
	private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    // The following view is for when the 
    // grid fragment is displayed
    private View customActionBarView = null;
    // The following view is for when the 
    // video fragment is displayed
    private View videoFragmentActionBarView = null;
    private Spinner categories = null;
    private CategorySpinnerAdapter spinnerAdapter = null;
    private ImageButton search = null;
    private ImageButton cancel = null;
    private LinearLayout root = null;
    private EditText searchString = null;
    // Used to know whether the user is in search mode
    private boolean searchModeEnabled = false;
    // Used to know if VideoPlayerFragment is currently being displayed
    private boolean videoPlayerFragmentShown = false;
    // Used to keep track of the spinner item
    private int spinnerPosition = -1;
    
    private static final String SPINNER_POSITION_KEY = "spinner_position_key";
    private static final String SEARCH_MODE_ENABLED_KEY = "search_mode_enabled_key";
    private static final String SEARCH_TEXT_KEY = "search_text_key";
    private static final String VIDEO_PLAYER_FRAGMENT_SHOWN_KEY = "video_fragment_shown";
    
    private Bundle argsToVideoPlayerFragment = null;
    
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
		new CountDownTimer(1000, 1000) {
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
		// Listen for keypresses
		setEditTextEditorActionListener();
		
		if(savedInstanceState != null){
			spinnerPosition = savedInstanceState.getInt(SPINNER_POSITION_KEY);
			String searchText = savedInstanceState.getString(SEARCH_TEXT_KEY);
			searchModeEnabled = savedInstanceState.getBoolean(SEARCH_MODE_ENABLED_KEY);
			videoPlayerFragmentShown = savedInstanceState.getBoolean(VIDEO_PLAYER_FRAGMENT_SHOWN_KEY);
			categories.setSelection(spinnerPosition);
			if(searchModeEnabled){
				search.performClick();
				searchString.setText(searchText);
			}
		}
		FavoriteUtil.loadFavoritesFromDatabase();
	}
	//------------------------------------------------------------------------------
	@Override
	public void onResume(){
		super.onResume();
		if(videoPlayerFragmentShown){
			setVideoFragmentActionBarView();
		}
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
		}
		if(!videoPlayerFragmentShown){
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
	private void setVideoFragmentActionBarView(){
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		if(videoFragmentActionBarView == null){
			LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			videoFragmentActionBarView = inflator.inflate(R.layout.fragment_video_player_action_bar,null);
		}
		ab.setCustomView(videoFragmentActionBarView);
		// No need to retrieve any references as 
		// we will let the video fragment do that
	}
	//------------------------------------------------------------------------------
	private void setSpinnerItemSelectionListener(){
		categories.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id) {
				spinnerPosition = position;
				String category = (String) PodcastUtil.getCategories().get(spinnerPosition);
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
				fragment.restoreAll();
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
	private void setEditTextEditorActionListener(){
		TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEARCH
						|| event != null){
					String text = searchString.getText().toString();
					if(!text.trim().isEmpty()){
						 fragment.searchFor(text.trim());
					}
				}
				return true;
			}
		};
		searchString.setOnEditorActionListener(editorListener);
	}
	//------------------------------------------------------------------------------
	@Override
	public void onSaveInstanceState(Bundle outState){
		outState.putBoolean(SEARCH_MODE_ENABLED_KEY, searchModeEnabled);
		outState.putBoolean(VIDEO_PLAYER_FRAGMENT_SHOWN_KEY, videoPlayerFragmentShown);
		outState.putInt(SPINNER_POSITION_KEY, spinnerPosition);
		outState.putString(SEARCH_TEXT_KEY, searchString.getText().toString());
		super.onSaveInstanceState(outState);
	}
	//------------------------------------------------------------------------------
	@Override
	public void onBackPressed(){
		
		// Reset Everything
		PrefUtils.setSeekTo(this, 0);
		PrefUtils.setVideoIndex(this, 0);
		System.out.println("VIDEO INDEX AFTER BACK: " + PrefUtils.getSeekTo(this));
		
		if(searchModeEnabled){
			searchModeEnabled = false;
			cancel.performClick();
		}else if(videoPlayerFragmentShown){
			// VideoPlayerFragment now officially hidden
			videoPlayerFragmentShown = false;
			cancel.performClick();
			setDrawerClosedCustomActionBarView();
			// Screen no longer needs to be on at all times
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			super.onBackPressed();
		}else{
			super.onBackPressed();
		}
	}
	//------------------------------------------------------------------------------
	@Override
	public void onPodcastClicked(int index) {
		fragment.restoreAll();
		// Set Arguments
		if(argsToVideoPlayerFragment == null){
			argsToVideoPlayerFragment = new Bundle();
			argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, index);
			VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
			videoPlayerFragment.setRetainInstance(true);
			videoPlayerFragment.setArguments(argsToVideoPlayerFragment);
		}else{
			argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, index);
		}
		// Show the custom view for the video fragment context, yo!
		setVideoFragmentActionBarView();
		// Display VideoPlayerFragment
		displayVideoPlayerFragment();
		// VideoPlayerFragment now officially shown
		videoPlayerFragmentShown = true;
		
	}
	//------------------------------------------------------------------------------
	private void displayVideoPlayerFragment(){
		VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
		// Save the fragment across configuration changes
		videoPlayerFragment.setRetainInstance(true);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.fragment_container, videoPlayerFragment);
		transaction.addToBackStack(null);
		// add some eye candy
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		// Commit the transaction
		transaction.commit();
	}
	//------------------------------------------------------------------------------
}
