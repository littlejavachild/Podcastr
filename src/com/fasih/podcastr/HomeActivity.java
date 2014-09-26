package com.fasih.podcastr;

import java.io.File;
import java.util.List;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.fasih.podcastr.adapter.CategorySpinnerAdapter;
import com.fasih.podcastr.adapter.NavigationDrawerAdapter;
import com.fasih.podcastr.fragment.PodcastGridFragment;
import com.fasih.podcastr.fragment.PodcastGridFragment.OnPodcastClickedListener;
import com.fasih.podcastr.fragment.VideoPlayerFragment;
import com.fasih.podcastr.service.MusicService;
import com.fasih.podcastr.util.ActionBarUtil;
import com.fasih.podcastr.util.Constants;
import com.fasih.podcastr.util.FavoriteUtil;
import com.fasih.podcastr.util.PodcastUtil;
import com.fasih.podcastr.util.PrefUtils;
import com.fasih.podcastr.util.RecentUtil;

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
    private boolean favoritesFragmentShown = false;
    private boolean recentsFragmentShown = false;
    
    // Used to keep track of the spinner item
    private int spinnerPosition = -1;
    
    private static final String SPINNER_POSITION_KEY = "spinner_position_key";
    private static final String SEARCH_MODE_ENABLED_KEY = "search_mode_enabled_key";
    private static final String SEARCH_TEXT_KEY = "search_text_key";
    private static final String VIDEO_PLAYER_FRAGMENT_SHOWN_KEY = "video_fragment_shown";
    private static final String FAVORITES_FRAGMENT_SHOWN_KEY = "favorites_fragment_shown";
    private static final String RECENTS_FRAGMENT_SHOWN_KEY = "recents_fragment_shown";
    
    private Bundle argsToVideoPlayerFragment = null;
    
	//------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
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
		// Listen for nav drawer events
		setNavigationDrawerItemClickedListener();
		if(savedInstanceState != null){
			spinnerPosition = savedInstanceState.getInt(SPINNER_POSITION_KEY);
			String searchText = savedInstanceState.getString(SEARCH_TEXT_KEY);
			searchModeEnabled = savedInstanceState.getBoolean(SEARCH_MODE_ENABLED_KEY);
			videoPlayerFragmentShown = savedInstanceState.getBoolean(VIDEO_PLAYER_FRAGMENT_SHOWN_KEY);
			favoritesFragmentShown = savedInstanceState.getBoolean(FAVORITES_FRAGMENT_SHOWN_KEY);
			recentsFragmentShown = savedInstanceState.getBoolean(RECENTS_FRAGMENT_SHOWN_KEY);
			categories.setSelection(spinnerPosition);
			if(searchModeEnabled){
				search.performClick();
				searchString.setText(searchText);
			}
		}
	}
	//------------------------------------------------------------------------------
	@Override
	public void onResume(){
		super.onResume();
		if(videoPlayerFragmentShown || favoritesFragmentShown
				|| recentsFragmentShown){
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
	public void onStop(){
		super.onStop();
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
                if(favoritesFragmentShown || recentsFragmentShown){
                	setVideoFragmentActionBarView();
                }else{
                	setDrawerClosedCustomActionBarView();
                }
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
				fragment.restoreAll();
			}
		});
	}
	//------------------------------------------------------------------------------
//	private void hideKeybaord(){
//		if(searchString != null){
//			InputMethodManager imm = (InputMethodManager)getSystemService(
//				      Context.INPUT_METHOD_SERVICE);
//			imm.hideSoftInputFromWindow(searchString.getWindowToken(), 0);
//		}
//	}
//	//------------------------------------------------------------------------------
//	private void showKeyboard(){
//		InputMethodManager imm = (InputMethodManager)getSystemService(
//			      Context.INPUT_METHOD_SERVICE);
//		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//	}
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
	private void setNavigationDrawerItemClickedListener(){
		leftDrawer.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int index,
					long id) {
				if(index == 1){
					// TODO
					displayPodcastFragment();
				}else if(index == 2){
					// Open downloads directory
					File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
					Uri selectedUri = Uri.fromFile(file.getAbsoluteFile());
					//Start Activity to view the selected file 
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(selectedUri, "*/*"); 
					startActivity(Intent.createChooser(intent, "Open File..."));
				}else if(index == 3){
					boolean temp = recentsFragmentShown;
					PrefUtils.setVideoIndex(HomeActivity.this, 0);
					PrefUtils.setSeekTo(HomeActivity.this, 0);
//					PodcastrApplication.newInstance().getMediaPlayer().reset();
					// Show the custom view for the video fragment context, yo!
					setVideoFragmentActionBarView();
					// we are showing the recent fragment
					recentsFragmentShown = true;
					// and not the favorite fragment
					favoritesFragmentShown = false;
					// although they are part of video fragment
					// videoPlayerFragmentShown is a different flag altogether
					videoPlayerFragmentShown = false;
					// Set the arguments because the user may be directly clicking
					// the recents directly from nav drawer
					VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
					if(argsToVideoPlayerFragment == null){
						Bundle existingArgs = videoPlayerFragment.getArguments();
						if(existingArgs == null){
							argsToVideoPlayerFragment = new Bundle();
						}else{
							argsToVideoPlayerFragment = existingArgs;
						}
						
						argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, 0);
						
						argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
						argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
						
						videoPlayerFragment.setRetainInstance(true);
						if(existingArgs == null){
							videoPlayerFragment.setArguments(argsToVideoPlayerFragment);
						}
					}else{
						argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
						argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
					}
					
					if(temp == false){
						if(!isInBackStack(videoPlayerFragment)){
							displayVideoPlayerFragment();
						}
						videoPlayerFragment.retrieveArgumentsAgain();
					}
					
				}else if(index == 4){
					boolean temp = favoritesFragmentShown;
					PrefUtils.setVideoIndex(HomeActivity.this, 0);
					PrefUtils.setSeekTo(HomeActivity.this, 0);
//					PodcastrApplication.newInstance().getMediaPlayer().reset();
					// Show the custom view for the video fragment context, yo!
					setVideoFragmentActionBarView();
					
					// we are not showing the recent fragment
					recentsFragmentShown = false;
					// we are showing the favorites fragment
					favoritesFragmentShown = true;
					// although they are part of video fragment
					// videoPlayerFragmentShown is a different flag altogether
					videoPlayerFragmentShown = false;
					VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
					// Set the arguments because the user may be directly clicking
					// the recents directly from nav drawer
					if(argsToVideoPlayerFragment == null){
						Bundle existingArgs = videoPlayerFragment.getArguments();
						if(existingArgs == null){
							argsToVideoPlayerFragment = new Bundle();
						}else{
							argsToVideoPlayerFragment = existingArgs;
						}
						
						argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, 0);
						
						argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
						argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
						
						videoPlayerFragment.setRetainInstance(true);
						if(existingArgs == null){
							videoPlayerFragment.setArguments(argsToVideoPlayerFragment);
						}
					}else{
						argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
						argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
					}
					if(temp == false){
						if(!isInBackStack(videoPlayerFragment)){
							displayVideoPlayerFragment();
						}
						videoPlayerFragment.retrieveArgumentsAgain();
					}
				}else if(index == 5){
					final String packageName = getPackageName();
					Uri market = Uri.parse("market://details?id=" + packageName);
					try{
						startActivity(new Intent(Intent.ACTION_VIEW,market));
					}catch(ActivityNotFoundException e){
						Uri browser = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
						startActivity(new Intent(Intent.ACTION_VIEW,browser));
					}
				}
//				else if(index == 6){
//				    Actually, we should be serializaing a Response object
//					However, since we lose it when we retrieve podcasts in the background,
//					we have to do a little workaround
//					String json = new Gson().toJson(PodcastUtil.getPodcasts());
//					json = "{podcasts : " + json + "}";
//					Response response = new Gson().fromJson(json, Response.class);
//					System.out.println(response.getPodcasts().size());
//				}
				drawerLayout.closeDrawer(Gravity.LEFT);
			}
		});
	}
	//------------------------------------------------------------------------------
	@Override
	public void onSaveInstanceState(Bundle outState){
		outState.putBoolean(SEARCH_MODE_ENABLED_KEY, searchModeEnabled);
		outState.putBoolean(VIDEO_PLAYER_FRAGMENT_SHOWN_KEY, videoPlayerFragmentShown);
		outState.putInt(SPINNER_POSITION_KEY, spinnerPosition);
		outState.putString(SEARCH_TEXT_KEY, searchString.getText().toString());
		outState.putBoolean(FAVORITES_FRAGMENT_SHOWN_KEY, favoritesFragmentShown);
		outState.putBoolean(RECENTS_FRAGMENT_SHOWN_KEY, recentsFragmentShown);
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
		}else if(videoPlayerFragmentShown || favoritesFragmentShown || recentsFragmentShown){
			// VideoPlayerFragment now officially hidden
			videoPlayerFragmentShown = false;
			// Other fragments also officially hidden
			favoritesFragmentShown = false;
			recentsFragmentShown = false;
			
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
		PrefUtils.setSeekTo(this, 0);
		PrefUtils.setVideoIndex(this, 0);
		
		fragment.restoreAll();
		
		// We arent showing the favorites or recents so we set them to false
		favoritesFragmentShown = false;
		recentsFragmentShown = false;
		
		VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
		// Set Arguments
		if(argsToVideoPlayerFragment == null){
			argsToVideoPlayerFragment = new Bundle();
			argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, index);
			
			argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
			argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
			
			videoPlayerFragment.setRetainInstance(true);
			videoPlayerFragment.setArguments(argsToVideoPlayerFragment);
		}else{
			argsToVideoPlayerFragment.putInt(Constants.PODCAST_INDEX, index);
			argsToVideoPlayerFragment.putBoolean(Constants.FAVORITES_FRAGMENT_SHOWN, favoritesFragmentShown);
			argsToVideoPlayerFragment.putBoolean(Constants.RECENTS_FRAGMENT_SHOWN, recentsFragmentShown);
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
		
		// First check if the fragment is already in the backstack
		// We do not want a "fragment already shown" exception
		List<Fragment> alreadyShown = getSupportFragmentManager().getFragments();
		if(alreadyShown != null){
			for(Fragment each : alreadyShown){
				if(each instanceof VideoPlayerFragment){
					return;
				}
			}
		}
		
		VideoPlayerFragment videoPlayerFragment = VideoPlayerFragment.newInstance();
		// Save the fragment across configuration changes
		videoPlayerFragment.setRetainInstance(true);
		
		FragmentManager manager  = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.add(R.id.fragment_container, videoPlayerFragment,null);
		transaction.addToBackStack(null);
		// add some eye candy
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		// Commit the transaction
		transaction.commit();
	}
	//------------------------------------------------------------------------------
	private void displayPodcastFragment(){
		// TODO
		// We only pop if we are not already showing the PodcastGridFragment
		if(videoPlayerFragmentShown == true || favoritesFragmentShown == true ||
				recentsFragmentShown == true){
			super.onBackPressed();
			
			videoPlayerFragmentShown = false;
			favoritesFragmentShown = false;
			recentsFragmentShown = false;
		}
	}
	//------------------------------------------------------------------------------
	private boolean isInBackStack(Fragment frag){
		
		boolean contains = false;
		FragmentManager manager = getSupportFragmentManager();
		List<Fragment> backstack = manager.getFragments();
		for(Fragment each : backstack){
			if(each != null){
				if(each.equals(frag)){
					contains = true;
					break;
				}
			}
		}
		return contains;
	}
	//------------------------------------------------------------------------------
	// never remove
	// http://stackoverflow.com/questions/23443400/android-fragment-exists-from-previous-state-and-cannot-be-removed
}
