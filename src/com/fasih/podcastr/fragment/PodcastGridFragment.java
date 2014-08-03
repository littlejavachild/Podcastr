package com.fasih.podcastr.fragment;

import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasih.podcastr.PodcastrApplication;
import com.fasih.podcastr.R;
import com.fasih.podcastr.adapter.GridAdapter;
import com.fasih.podcastr.util.LoadPodcastsTask;
import com.fasih.podcastr.util.Podcast;
import com.fasih.podcastr.util.PodcastUtil;
import com.squareup.picasso.Picasso;

public class PodcastGridFragment extends Fragment {
	private PodcastGridFragment singleton = null;
	private GridView podcastGrid = null;
	private GridAdapter adapter = new GridAdapter();
	private AssetManager am;
	private FragmentManager fm;
	private LoadPodcastsTask loader = null;
	private OnPodcastClickedListener listener = null;
	
	private Typeface roboto = null;
	private View dialogView = null;
	private Button watch = null;
	private ImageView logo = null;
	private TextView description = null;
	private TextView title = null;
	private TextView background = null;
	//------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		singleton = this;
	}
	//------------------------------------------------------------------------------
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_podcast_grid, container, false);
		
		podcastGrid = (GridView) view.findViewById(R.id.podcastGrid);
		podcastGrid.setAdapter(adapter);
		setGridViewOnItemClickListener();
		
		roboto = PodcastrApplication.newInstance().getTypeface();
		
		setRetainInstance(true);
		return view;
	}
	//------------------------------------------------------------------------------
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		try{
			listener = (OnPodcastClickedListener) getActivity();
		}catch(ClassCastException e){
			throw new ClassCastException
					("Hosting Activity Must Implement PodcastSelectionListener");
		}
	}
	//------------------------------------------------------------------------------
	public void setLoaderResources(FragmentManager fm, AssetManager am){
		this.fm = fm;
		this.am = am;
	}
	//------------------------------------------------------------------------------
	@Override
	public void onResume(){
		super.onResume();
		loader = new LoadPodcastsTask(fm, am, adapter);
		loader.execute("txt/podcasts.txt");
	}
	//------------------------------------------------------------------------------
	public PodcastGridFragment newInstance(){
		if(singleton == null){
			singleton = new PodcastGridFragment();
		}
		return singleton;
	}
	//------------------------------------------------------------------------------
	public void sortByCategory(String category){
		PodcastUtil.sortByCategory(category);
		adapter.notifyDataSetChanged();
	}
	//------------------------------------------------------------------------------
	public void searchFor(String text){
		PodcastUtil.searchFor(text);
		adapter.notifyDataSetChanged();
	}
	//------------------------------------------------------------------------------
	public void restoreAll(){
		PodcastUtil.restoreAll();
		adapter.notifyDataSetChanged();
	}
	//------------------------------------------------------------------------------
	private void setGridViewOnItemClickListener(){
		podcastGrid.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long index) {
				// TODO Display A Dialog Box
				buildAndShowDialog(position);
			}
		});
	}
	//------------------------------------------------------------------------------
	private void buildAndShowDialog(final int position){
		Podcast podcast = PodcastUtil.getPodcasts().get(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		// Inflate the View
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		dialogView = inflater.inflate(R.layout.dialog_podcast_description, podcastGrid, false);
		builder.setView(dialogView);
		// get references
		watch = (Button) dialogView.findViewById(R.id.watch);
		logo = (ImageView) dialogView.findViewById(R.id.logo);
		description = (TextView) dialogView.findViewById(R.id.description);
		title = (TextView) dialogView.findViewById(R.id.title);
		// fill them in
		synchronized (podcast) {
			title.setText(podcast.getTitle());
			description.setText(podcast.getDescription());
			Uri uri = Uri.parse(podcast.getLogo());
			Picasso.with(getActivity()).load(uri).error(R.color.google_io_blue).into(logo);
			title.setTypeface(roboto, Typeface.BOLD);
			description.setTypeface(roboto, Typeface.BOLD);
			watch.setTypeface(roboto, Typeface.BOLD);
		}
		final AlertDialog dialog = builder.create();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		watch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				listener.onPodcastClicked(position);
			}
		});
		dialog.show();
		
	}
	//------------------------------------------------------------------------------
	/**
	 * Used to convey the index of the select podcast to the hosting
	 * Activity
	 * @author Fasih
	 *
	 */
	public static interface OnPodcastClickedListener{
		void onPodcastClicked(int index);
	}
}
