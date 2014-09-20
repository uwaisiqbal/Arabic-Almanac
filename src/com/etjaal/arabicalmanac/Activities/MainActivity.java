package com.etjaal.arabicalmanac.Activities;

import java.io.File;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;
import com.etjaal.arabicalmanac.Services.DownloadService;
import com.etjaal.arabicalmanac.Tools.SearchInterface;

public class MainActivity extends FragmentActivity implements OnClickListener {

    private String TAG = "MainActivity";
    private PhotoView imdisplayImage;
    private Button topNextPageButton, topLastPageButton;
    private Dictionary dict;
    private SearchInterface searchInterface;
    private String path;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";
    private static final String REORGANISE_FILES_STRUCTURE_PREFS_KEY = "reorganiseFiles";
    private static final String IMAGES_IN_GALLERY_FIX_PREFS_KEY = "hideImagesInGallery";
    private static final String DOWNLOAD_SERVICE_RUNNING = "downloadServiceRunning";
    private static final String INDETERMINATE_STAGE = "indeterminateStage";
    private String hansWehrLink = "https://dl.dropboxusercontent.com/u/63542577/hw4.zip";
    private IntentFilter mFilter = new IntentFilter("download");
    private ProgressDialog progressDialog;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent;
    private SearchView searchView;
    private String searchIndex;

    // New Comment
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

	// Load views from XML
	referenceViews();

	dict = new Dictionary("hw4", "Hans Wehr");
	mFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
	searchInterface = new SearchInterface(getApplicationContext(), dict);
	// Setup Share Intent
	shareIntent = new Intent();
	shareIntent.setAction(Intent.ACTION_SEND);
	shareIntent.setType("image/png");

	// Force system to reload options menu after a rotation
	if (savedInstanceState != null) {
	    invalidateOptionsMenu();
	    searchIndex = savedInstanceState.getString("search_index");
	    Log.v(TAG, "Loading search index after rotation as: " + searchIndex);
	    searchInterface.setIndex(Integer.valueOf(searchIndex));
	    displayImageUsingIndex();
	    savedInstanceState = null;
	} else {
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
		setupProgressDialog(prefs
			.getBoolean(INDETERMINATE_STAGE, false));
	    }

	    handleIntent(getIntent());
	}

    }

    private String statusMessage(Cursor c) {
	String msg = "???";
	switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
	case DownloadManager.STATUS_FAILED:
	    msg = "Download failed!";
	    String failedReason = "";
	    switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON))) {
	    case DownloadManager.ERROR_CANNOT_RESUME:
		failedReason = "ERROR_CANNOT_RESUME";
		break;
	    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
		failedReason = "ERROR_DEVICE_NOT_FOUND";
		break;
	    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
		failedReason = "ERROR_FILE_ALREADY_EXISTS";
		break;
	    case DownloadManager.ERROR_FILE_ERROR:
		failedReason = "ERROR_FILE_ERROR";
		break;
	    case DownloadManager.ERROR_HTTP_DATA_ERROR:
		failedReason = "ERROR_HTTP_DATA_ERROR";
		break;
	    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
		failedReason = "ERROR_INSUFFICIENT_SPACE";
		break;
	    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
		failedReason = "ERROR_TOO_MANY_REDIRECTS";
		break;
	    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
		failedReason = "ERROR_UNHANDLED_HTTP_CODE";
		break;
	    case DownloadManager.ERROR_UNKNOWN:
		failedReason = "ERROR_UNKNOWN";
		break;
	    }

	    Log.v(TAG, failedReason);
	    break;

	case DownloadManager.STATUS_PAUSED:
	    msg = "Download paused!";
	    break;

	case DownloadManager.STATUS_PENDING:
	    msg = "Download pending!";
	    break;

	case DownloadManager.STATUS_RUNNING:
	    msg = "Download in progress!";
	    break;

	case DownloadManager.STATUS_SUCCESSFUL:
	    msg = "Download complete!";
	    break;

	default:
	    msg = "Download is nowhere in sight";
	    break;
	}

	return (msg);
    }

    private void setupDownloadManager() {
	DownloadManager.Request request = new DownloadManager.Request(
		Uri.parse(hansWehrLink));

	request.setDescription("Arabic Almanac");
	request.setTitle("Download");
	request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	request.setDestinationInExternalFilesDir(this,
		Environment.DIRECTORY_DOWNLOADS, "hw4.zip");

	final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

	final long downloadId = manager.enqueue(request);

	progressDialog = new ProgressDialog(MainActivity.this);
	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	progressDialog.setCancelable(false);
	progressDialog.setCanceledOnTouchOutside(false);
	progressDialog.setMax(100);
	progressDialog.setProgress(0);
	progressDialog.show();

	new Thread(new Runnable() {

	    @Override
	    public void run() {

		boolean downloading = true;

		while (downloading) {

		    DownloadManager.Query q = new DownloadManager.Query();
		    q.setFilterById(downloadId);

		    Cursor cursor = manager.query(q);
		    cursor.moveToFirst();
		    int bytes_downloaded = cursor.getInt(cursor
			    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
		    int bytes_total = cursor.getInt(cursor
			    .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

		    if (cursor.getInt(cursor
			    .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
			downloading = false;
		    }

		    final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);

		    runOnUiThread(new Runnable() {

			@Override
			public void run() {

			    progressDialog.setProgress((int) dl_progress);

			}
		    });
		    Log.d(TAG, statusMessage(cursor));
		    cursor.close();
		}

	    }
	}).start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	outState.putString("search_index",
		String.valueOf(searchInterface.getIndex()));
	Log.v(TAG,
		"Saving search index before rotation as: "
			+ searchInterface.getIndex());
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
	    if (searchInterface.getIndex() == 0) {
		searchInterface.setIndex(1);
	    }

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
				// Intent i = new
				// Intent(getApplicationContext(),
				// DownloadService.class);
				/*
				 * i.putExtra("url", hansWehrLink);
				 * i.putExtra("path", path);
				 * i.putExtra("fileName", dict.getReference());
				 * startService(i);
				 * setupProgressDialog(prefs.getBoolean(
				 * INDETERMINATE_STAGE, false));
				 */
				setupDownloadManager();
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
	imdisplayImage = (PhotoView) findViewById(R.id.imshow);
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
