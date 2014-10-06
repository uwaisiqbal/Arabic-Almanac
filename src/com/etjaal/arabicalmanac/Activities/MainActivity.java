package com.etjaal.arabicalmanac.Activities;

import java.io.File;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.ShareActionProvider;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;
import com.etjaal.arabicalmanac.Tools.Constants;
import com.etjaal.arabicalmanac.Tools.DatabaseHelper;
import com.etjaal.arabicalmanac.Tools.SearchInterface;
import com.samsung.spen.lib.multiwindow.SMultiWindowManager;


public class MainActivity extends FragmentActivity implements OnClickListener {

    private String TAG = "MainActivity";
    private PhotoView imdisplayImage;
    private Button topNextPageButton, topLastPageButton;
    private Dictionary dict;
    private SearchInterface searchInterface;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";
    private static final String ADD_DICTS_TO_DB = "addToDb";
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent;
    private SearchView searchView;
    private String searchIndex;
    private SMultiWindowManager mMWM;

    // New Comment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
	setContentView(R.layout.activity_main);
	// mMWM = new SMultiWindowManager(this);

	// Initialise Prefs
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	editor = prefs.edit();

	DatabaseHelper dbHelp = new DatabaseHelper(this);

	if (prefs.getBoolean(ADD_DICTS_TO_DB, true)) {
	    // Add dictionaries to db on first run of app
	    Dictionary hans = new Dictionary(Constants.HANS_WEHR_REF,
		    "Hans Wehr", Constants.LANG_ENG, false, 60);
	    Dictionary lisan = new Dictionary(Constants.LISAN_REF, "Lisan al-Arab", 
		    Constants.LANG_ARABIC, false, 671);
	    Dictionary mukhtaar = new Dictionary("ums", "Mukhtaar as-Sahih", Constants.LANG_URDU, false, 50);
	    Dictionary mufradaat = new Dictionary("umr", "Mufradaat Alfaaz al-Quran", Constants.LANG_URDU, false, 47);
	    dbHelp.addDictionary(mufradaat);
	    dbHelp.addDictionary(mukhtaar);
	    dbHelp.addDictionary(lisan);
	    dbHelp.addDictionary(hans);
	    
	    editor.putBoolean(ADD_DICTS_TO_DB, false);
	    editor.commit();
	}

	// Load views from XML
	referenceViews();

	dict = new Dictionary("hw4", "Hans Wehr");
	searchInterface = new SearchInterface(getApplicationContext(), dict);

	// Setup Share Intent
	shareIntent = new Intent();
	shareIntent.setAction(Intent.ACTION_SEND);
	shareIntent.setType("image/png");

	// Force system to reload options menu after a rotation
	if (savedInstanceState != null) {
	    searchIndex = savedInstanceState.getString("search_index");
	    searchInterface.setIndex(Integer.valueOf(searchIndex));
	    displayImageUsingIndex();
	    savedInstanceState = null;
	} else {
	    if (!prefs.getBoolean(FIRST_TIME_PREFS_KEY, false)) {
		Intent i = new Intent(this, DownloadActivity.class);
		startActivityForResult(i, 1);
	    } else {
		displayImageUsingIndex();
	    }

	    handleIntent(getIntent());
	}

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	outState.putString("search_index",
		String.valueOf(searchInterface.getIndex()));
    }

    @Override
    protected void onPause() {
	super.onPause();
	overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == 1) {
	    if (resultCode == Activity.RESULT_OK) {
		displayImageUsingIndex();
	    } else {
		finish();
	    }
	}
    }

    /** Load the views from XML */
    private void referenceViews() {
	imdisplayImage = (PhotoView) findViewById(R.id.imshow);
	topNextPageButton = (Button) findViewById(R.id.bTopNextPage);
	topNextPageButton.setOnClickListener(this);
	topLastPageButton = (Button) findViewById(R.id.bTopPreviousPage);
	topLastPageButton.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
	setIntent(intent);
	handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
	// Explicitly handle search intent
	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    String query = intent.getStringExtra(SearchManager.QUERY);
	    searchInterface.search(query);
	    displayImageUsingIndex();
	}
    }

    // Called only once when the menu is first created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.imageviewer_activity_actions, menu);

	// Link ShareActionProvider with menu item
	mShareActionProvider = (ShareActionProvider) menu.findItem(
		R.id.action_share).getActionProvider();

	if (mShareActionProvider != null) {
	    mShareActionProvider.setShareIntent(shareIntent);
	}

	// Get the SearchView and set the search-able configuration
	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	searchView = (SearchView) menu.findItem(R.id.action_search)
		.getActionView();
	searchView.setSearchableInfo(searchManager
		.getSearchableInfo(getComponentName()));
	searchView.setIconifiedByDefault(true);

	if (searchIndex != null) {
	    searchInterface.setIndex(Integer.valueOf(searchIndex));
	    displayImageUsingIndex();
	}

	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_search:
	    return true;
	case R.id.action_help:
	    Intent i = new Intent(getBaseContext(), AboutActivity.class);
	    startActivity(i);
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}

    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.bTopNextPage:
	    searchInterface.setIndex(searchInterface.getIndex() + 1);
	    displayImageUsingIndex();
	    break;
	case R.id.bTopPreviousPage:
	    if (searchInterface.getIndex() > 1) {
		searchInterface.setIndex(searchInterface.getIndex() - 1);
	    }
	    displayImageUsingIndex();
	    break;

	}
    }

    /** Display the image in the Image view */
    private void displayImageUsingIndex() {

	if (searchInterface.getIndex() == 0) {
	    searchInterface.setIndex(1);
	}
	Drawable bmp = new BitmapDrawable(
		BitmapFactory.decodeFile(searchInterface.getImagePathForIndex()));
	imdisplayImage.setImageDrawable(bmp);
	PhotoViewAttacher mAttacher = new PhotoViewAttacher(imdisplayImage);
	imdisplayImage.setScaleType(PhotoView.ScaleType.CENTER_CROP);
	mAttacher.setScaleType(PhotoView.ScaleType.CENTER_CROP);
	updateShareIntent();
    }

    private void updateShareIntent() {
	shareIntent.putExtra(Intent.EXTRA_STREAM,
		Uri.fromFile(new File(searchInterface.getImagePathForIndex())));
	if (mShareActionProvider != null) {
	    mShareActionProvider.setShareIntent(shareIntent);
	}
    }

}
