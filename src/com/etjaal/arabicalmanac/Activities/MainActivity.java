package com.etjaal.arabicalmanac.Activities;

import java.io.File;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;
import com.etjaal.arabicalmanac.Services.DownloadService;
import com.etjaal.arabicalmanac.Tools.SearchInterface;
import com.etjaal.arabicalmanac.Tools.TouchImageView;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {

    private String TAG = "MainActivity";
    private TouchImageView imdisplayImage;
    private Button topNextPageButton, topLastPageButton;
    private Dictionary dict;
    private SearchInterface searchInterface;
    private String path;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";
    private static final String REORGANISE_FILES_STRUCTURE_PREFS_KEY = "reorganiseFiles";
    private static final String IMAGES_IN_GALLERY_FIX_PREFS_KEY = "galleryfix";
    private static final String DOWNLOAD_SERVICE_RUNNING = "downloadServiceRunning";
    private static final String INDETERMINATE_STAGE = "indeterminateStage";
    // private String hansWehrLink =
    // "http://ia600803.us.archive.org/2/items/ArabicAlmanac/hw4.zip";
    private String hansWehrLink = "https://dl.dropboxusercontent.com/u/63542577/hw4.zip";
    // private String hansWehrLink =
    // "https://www.dropbox.com/s/cy9qu1pgsa39b2z/hw4.zip?dl=1";
    private IntentFilter mFilter = new IntentFilter("download");
    private ProgressDialog progressDialog;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
	setContentView(R.layout.activity_main);
	path = Environment.getExternalStorageDirectory().toString() + "/"
		+ getResources().getString(R.string.app_name) + "/";
	// Initialise Prefs
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	editor = prefs.edit();

	mFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
	// Load views from XML
	referenceViews();

	dict = new Dictionary("hw4", "Hans Wehr");

	searchInterface = new SearchInterface(getApplicationContext(), dict);
	handleIntent(getIntent());

	// If download service is not running then run app
	// Otherwise manage notifications and setup Progress Dialog
	if (!prefs.getBoolean(DOWNLOAD_SERVICE_RUNNING, false)) {
	    run();
	} else {
	    // Dismiss notification bar
	    NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
		    this);
	    mNotifyManager.cancel(0);
	    setupProgressDialog(prefs.getBoolean(INDETERMINATE_STAGE, false));
	}
    }

    private void setupProgressDialog(boolean indeterminate) {
	progressDialog = new ProgressDialog(MainActivity.this);
	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	progressDialog.setCancelable(false);
	progressDialog.setCanceledOnTouchOutside(false);
	// Close app on press
	progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Minimise",
		new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// close the activity
			finish();
		    }
		});

	if (indeterminate) {
	    // If unzipping files
	    progressDialog.setIndeterminate(true);
	    progressDialog.setProgressNumberFormat(null);
	    progressDialog.setProgressPercentFormat(null);
	    progressDialog.setTitle(getResources().getString(
		    R.string.progress_bar_unzip_title));
	    progressDialog.setMessage(getResources().getString(
		    R.string.progress_bar_unzip_message));

	} else {
	    // If still downloading
	    progressDialog.setTitle(getResources().getString(
		    R.string.progress_bar_download_title));
	    progressDialog.setMessage(getResources().getString(
		    R.string.progress_bar_download_message));
	    progressDialog.setProgress(0);
	    progressDialog.setMax(100);
	}

	progressDialog.show();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
	    // Update progress bar with progress data
	    if (intent.getAction().equals("download")) {
		int progress = intent.getExtras().getInt("progress");
		if (progress >= 0) {
		    progressDialog.setProgress(progress);
		    // If unzipping
		    if (progress == 101) {
			progressDialog.setIndeterminate(true);
			progressDialog.setProgressNumberFormat(null);
			progressDialog.setProgressPercentFormat(null);
			progressDialog.setTitle(getResources().getString(
				R.string.progress_bar_unzip_title));
			progressDialog.setMessage(getResources().getString(
				R.string.progress_bar_unzip_message));
		    }
		} else if (progress == -1) {
		    // download failed
		    progressDialog.dismiss();
		    Toast.makeText(context,
			    "Download failed. Please try again.",
			    Toast.LENGTH_SHORT).show();
		    run();
		} else if (progress == -2) {
		    // download succeeded
		    progressDialog.dismiss();
		    Toast.makeText(context,
			    "All files have been successfully downloaded",
			    Toast.LENGTH_SHORT).show();
		    run();
		}
		abortBroadcast();
	    }

	}

    };

    @Override
    protected void onStart() {
	registerReceiver(mReceiver, mFilter);
	super.onStart();
    }

    @Override
    protected void onStop() {
	unregisterReceiver(mReceiver);
	super.onStop();
    }

    @Override
    protected void onPause() {
	overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
	super.onPause();
    }

    private void run() {
	// If first time running app
	if (!prefs.getBoolean(FIRST_TIME_PREFS_KEY, false)) {
	    // If no connection open settings otherwise commence download
	    if (!isNetworkAvailable()) {
		showInternetConnectionDialog();
	    } else {
		showDownloadDialog();
	    }
	} else {
	    // if file structure hasn't been re-organised yet
	    if (!prefs.getBoolean(REORGANISE_FILES_STRUCTURE_PREFS_KEY, false)) {
		organiseFileStructure();
	    }
	    // TODO: Check if this is actually working
	    // Fix to prevent images from showing in gallery
	    if (!prefs.getBoolean(IMAGES_IN_GALLERY_FIX_PREFS_KEY, false)) {
		File noMediaFile = new File(path, ".nomedia");
		noMediaFile.mkdir();
		editor.putBoolean(IMAGES_IN_GALLERY_FIX_PREFS_KEY, true);
		editor.commit();
	    }
	    // run app
	    searchInterface.setIndex(1);
	    displayImageUsingIndex();

	}
    }

    public void showDownloadDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog
		.setTitle(getResources().getString(R.string.dialog_title))
		.setMessage(getResources().getString(R.string.dialog_message))
		.setCancelable(false)
		.setNegativeButton(
			getResources().getString(R.string.dialog_cancel_button),
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				dialog.cancel();
				finish();
			    }
			})
		.setPositiveButton(
			getResources().getString(
				R.string.dialog_download_button),
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// Start download
				Intent i = new Intent(getApplicationContext(),
					DownloadService.class);
				i.putExtra("url", hansWehrLink);
				i.putExtra("path", path);
				i.putExtra("fileName", dict.getReference());
				startService(i);
				setupProgressDialog(prefs.getBoolean(
					INDETERMINATE_STAGE, false));
			    }
			}).create();
	alertDialog.show();
    }

    public void showInternetConnectionDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog
		.setTitle("No network connection")
		.setMessage(
			"Internet connection required. Please connect to the Internet.")
		.setNeutralButton("Go to Device Settings",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// Open settings so user can enable network
				// access
				startActivityForResult(new Intent(
					Settings.ACTION_WIRELESS_SETTINGS), 0);
			    }
			}).setCancelable(false).create();
	alertDialog.show();
    }

    /** Once the user returns from the Settings, run the app process again */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == 0) {
	    run();
	}
    }

    /** Check if an active network connection is available */
    public boolean isNetworkAvailable() {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	// if no network is available networkInfo will be null
	// otherwise check if we are connected
	if (networkInfo != null && networkInfo.isConnected()) {
	    return true;
	}
	return false;
    }

    /** Load the views from XML */
    private void referenceViews() {
	imdisplayImage = (TouchImageView) findViewById(R.id.imshow);
	imdisplayImage.setMaxZoom(4);
	imdisplayImage.maintainZoomAfterSetImage(false);

	topNextPageButton = (Button) findViewById(R.id.bTopNextPage);
	topNextPageButton.setOnClickListener(this);
	topLastPageButton = (Button) findViewById(R.id.bTopPreviousPage);
	topLastPageButton.setOnClickListener(this);

    }

    private void organiseFileStructure() {
	// TODO: Check if this is needed and what it actually does
	// Delete all other files except for img/hw4/ folder
	File file = new File(path);
	File imgFile = new File(path + "img/");
	for (File mFile : file.listFiles()) {
	    if (!mFile.getPath().toString()
		    .equals(imgFile.getPath().toString())) {
		DeleteRecursive(mFile);
		Log.v("file", mFile.getPath().toString());
	    }
	}

	editor.putBoolean(REORGANISE_FILES_STRUCTURE_PREFS_KEY, true);
	editor.commit();
    }

    private void DeleteRecursive(File fileOrDirectory) {
	if (fileOrDirectory.isDirectory())
	    for (File child : fileOrDirectory.listFiles())
		DeleteRecursive(child);
	fileOrDirectory.delete();

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
	    linkShareIntentWithShareProvider();
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.imageviewer_activity_actions, menu);

	// Link ShareActionProvider with menu item
	MenuItem item = menu.findItem(R.id.action_share);
	mShareActionProvider = (ShareActionProvider) item.getActionProvider();
	if(mShareActionProvider != null){
	    linkShareIntentWithShareProvider();
	}

	// Get the SearchView and set the search-able configuration
	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
		.getActionView();

	searchView.setSearchableInfo(searchManager
		.getSearchableInfo(getComponentName()));
	searchView.setIconifiedByDefault(true);
	return true;
    }

    private void linkShareIntentWithShareProvider() {
	// TODO Auto-generated method stub
	    setupShareIntent();
	    mShareActionProvider.setShareIntent(shareIntent);
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
	case R.id.action_share:
	    setupShareIntent();
	    startActivity(Intent.createChooser(shareIntent, "Send to"));
	case R.id.action_settings:
	    // Add in Settings Activity when update for other dictionaries is
	    // added
	    /*
	     * Intent j = new Intent(getApplicationContext(),
	     * SettingsActivity.class); startActivity(j);
	     */
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}

    }

    private void setupShareIntent() {
	// TODO Auto-generated method stub
	String filePath = searchInterface.getImagePathForIndex();
	shareIntent = new Intent();
	shareIntent.setAction(Intent.ACTION_SEND);
	shareIntent.putExtra(Intent.EXTRA_STREAM,
		Uri.fromFile(new File(filePath)));
	shareIntent.setType("image/png");

    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.bTopNextPage:
	    // case R.id.bBottomNextPage:
	    searchInterface.setIndex(searchInterface.getIndex() + 1);
	    displayImageUsingIndex();
	    linkShareIntentWithShareProvider();
	    break;
	case R.id.bTopPreviousPage:
	    // case R.id.bBottomPreviousPage:
	    if (searchInterface.getIndex() > 1) {
		searchInterface.setIndex(searchInterface.getIndex() - 1);
	    }
	    displayImageUsingIndex();
	    linkShareIntentWithShareProvider();
	    break;

	}
    }

    /** Display the image in the Image view */
    private void displayImageUsingIndex() {
	Bitmap bmp = BitmapFactory.decodeFile(searchInterface
		.getImagePathForIndex());
	imdisplayImage.setImageBitmap(bmp);
    }
}
