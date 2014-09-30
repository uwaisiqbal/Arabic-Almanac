package com.etjaal.arabicalmanac.Activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;
import com.etjaal.arabicalmanac.Services.UnzipService;
import com.etjaal.arabicalmanac.Tools.DatabaseHelper;
import com.etjaal.arabicalmanac.Tools.ProgressDialogManager;

public class DownloadActivity extends Activity {

    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private ProgressDialogManager progressDialogManager;
    private Context context;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";
    private static final String IMAGES_IN_GALLERY_FIX_PREFS_KEY = "hideImagesInGallery";
    private static final String DOWNLOAD_RUNNING = "downloadRunning";
    private static final String DOWNLOAD_ID = "downloadId";
    private DownloadManager downloadManager;
    private String hansWehrLink = "https://dl.dropboxusercontent.com/u/63542577/hw4.zip";
    private String path;
    private ArrayList<Integer> mSelectedItems;
    private DatabaseHelper dbHelp;
    private ArrayList<Dictionary> listOfDicts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	context = this;

	dbHelp = new DatabaseHelper(this);
	listOfDicts = (ArrayList<Dictionary>) dbHelp.getListOfDictionariesBasedOnInstallStatus(false);

	// Initialise Prefs
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	editor = prefs.edit();
	path = Environment.getExternalStorageDirectory().toString() + "/"
		+ getResources().getString(R.string.app_name) + "/";
	progressDialogManager = new ProgressDialogManager(this,listOfDicts);
	//Need to save mSelectedItems in prefs so I can pass it to the progressDialog manager
	downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

	
	if (!prefs.getBoolean(DOWNLOAD_RUNNING, false)) {
	    
	    if (!prefs.getBoolean(UnzipService.UNZIP_RUNNING, false)) {
		//If neither download nor unzip service is running then run the app
		run();
	    } else {
		//If download is complete and unzip is still running
		
		//Need two arrays of boolean variables
		//One to tell which of the downloads is still running
		//Another to tell which of the downloads is unzipping
		//Then cycle through each element of the two arrays and setup the dialog accordingly
		progressDialogManager.setupIndeterminateProgressDialog();
		progressDialogManager.showProgressDialog();
	    }
	} else {
	    //If download is still runnning
	    onDownloadCompleteReceiver.onReceive(context, null);
	}

    }

    private void setupDownloadManager() {
	ArrayList<Long> downloadIds = new ArrayList<Long>();
	//Register selected dictionaries with the download manager
	for (Integer item : mSelectedItems) {
	    String link = listOfDicts.get(item).getDownloadLink();
	    DownloadManager.Request request = new DownloadManager.Request(
		    Uri.parse(link));
	    request.setDescription(getResources().getString(
		    R.string.progress_bar_download_message));
	    request.setTitle(getResources().getString(R.string.app_name) + "-"
		    + listOfDicts.get(item).getName());
	    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	    request.setDestinationInExternalFilesDir(this,
		    Environment.DIRECTORY_DOWNLOADS, listOfDicts.get(item)
			    .getReference() + ".zip");
	    request.setVisibleInDownloadsUi(false);
	    downloadIds.add(downloadManager.enqueue(request));
	    //Save array to prefs so If user leaves app the data can be retreived
	    saveArrayList(downloadIds);
	}
	updateProgressDialog();

    }

    public void saveArrayList(ArrayList<Long> list) {

	editor.putInt("Status_size", list.size());

	for (int i = 0; i < list.size(); i++) {
	    editor.remove("Status_" + i);
	    editor.putLong("Status_" + i, list.get(i));
	}

	editor.commit();
    }

    public ArrayList<Long> loadArrayList() {
	ArrayList<Long> list = new ArrayList<Long>();
	int size = prefs.getInt("Status_size", 0);
	for (int i = 0; i < size; i++) {
	    list.add(prefs.getLong("Status_" + i, 0));
	}
	return list;
    }

    public long[] getArrayFromArrayList(ArrayList<Long> list) {
	final long[] array = new long[list.size()];
	int count = 0;
	for (Long id : list) {
	    array[count] = id;
	    count++;
	}
	return array;
    }


    private void updateProgressDialog() {
	progressDialogManager.showProgressDialog();

	new Thread(new Runnable() {

	    @Override
	    public void run() {

		boolean downloading = true;

		while (downloading) {
		    final ArrayList<Long> downloadIds = loadArrayList();
		    long[] array = getArrayFromArrayList(downloadIds);
		    DownloadManager.Query q = new DownloadManager.Query();
		    q.setFilterById(array);

		    Cursor cursor = downloadManager.query(q);
		    if (cursor.moveToFirst()) {

			do {
			    int bytes_downloaded = cursor.getInt(cursor
				    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
			    int bytes_total = cursor.getInt(cursor
				    .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			    long downloadId = cursor.getLong(cursor
				    .getColumnIndex(DownloadManager.COLUMN_ID));
			    final int pos = downloadIds.indexOf(downloadId);
			    if (cursor.getInt(cursor
				    .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
				// If download complete remove from arraylist
	
				
				downloadIds.remove(downloadId);
				saveArrayList(downloadIds);
				
				// If list is empty ie all downloads complete,
				// set
				// downloading false
				if (downloadIds.size() == 0) {
				    downloading = false;
				}

			    }

			    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

			    runOnUiThread(new Runnable() {

				@Override
				public void run() {
				    // Update during download
				    progressDialogManager.setProgress(pos, dl_progress);

				}
			    });
			} while (cursor.moveToNext());
		    }

		    // Log.d(TAG, statusMessage(cursor));
		    cursor.close();
		}

	    }
	}).start();
    }

    BroadcastReceiver onDownloadCompleteReceiver = new BroadcastReceiver() {
	public void onReceive(Context ctxt, Intent intent) {
	    DownloadManager.Query query = new DownloadManager.Query();
	    ArrayList<Long> downloadIds = loadArrayList();
	    query.setFilterById(getArrayFromArrayList(downloadIds));
	    Cursor cursor = downloadManager.query(query);

	    if (cursor.moveToFirst()) {

		do {
		    int columnIndex = cursor
			    .getColumnIndex(DownloadManager.COLUMN_STATUS);
		    int status = cursor.getInt(columnIndex);
		    int columnReason = cursor
			    .getColumnIndex(DownloadManager.COLUMN_REASON);
		    long downloadId = cursor.getLong(cursor
			    .getColumnIndex(DownloadManager.COLUMN_ID));
		    final int pos = downloadIds.indexOf(downloadId);
		    int reason = cursor.getInt(columnReason);

		    if (status == DownloadManager.STATUS_SUCCESSFUL) {

			// Post Download
			String zipFile = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
				+ "/"
				+ listOfDicts.get(mSelectedItems.get(pos))
					.getReference() + ".zip";
			// Change progressDialog to Indeterminate State
			progressDialogManager.setupIndeterminateProgressDialog(pos);
			downloadManager.remove(downloadId);
			downloadIds.remove(downloadId);
			saveArrayList(downloadIds);

			Intent i = new Intent(getBaseContext(),
				UnzipService.class);
			i.putExtra(UnzipService.ZIP_FILE, zipFile);
			i.putExtra(UnzipService.UNZIP_PATH, path);
			startService(i);

		    } else if (status == DownloadManager.STATUS_FAILED) {
			// Download Failed
			editor.putBoolean(FIRST_TIME_PREFS_KEY, false);
			editor.putBoolean(DOWNLOAD_RUNNING, false);
			editor.commit();
			Toast.makeText(
				getBaseContext(),
				"Download Of"
					+ listOfDicts.get(
						mSelectedItems.get(pos))
						.getName()
					+ " Failed. Please try again!",
				Toast.LENGTH_SHORT).show();
			downloadManager.remove(downloadId);
			run();
		    } else if (status == DownloadManager.STATUS_RUNNING) {
			updateProgressDialog();
		    }

		} while (cursor.moveToNext());

	    }
	}
    };

    BroadcastReceiver UnzipServiceReceiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context arg0, Intent intent) {
	    Bundle bundle = intent.getExtras();
	    if (bundle != null) {
		int resultCode = bundle.getInt(UnzipService.RESULT);
		if (resultCode == RESULT_OK) {
		    // If Unzipped Correctly
		    // progressDialogManager.dismissProgressDialog();
		    //progressDialog.dismiss();
		    Toast.makeText(context,
			    "All files have been successfully downloaded",
			    Toast.LENGTH_SHORT).show();
		    for (Integer item : mSelectedItems) {
			dbHelp.setDictionaryAsInstalled(listOfDicts.get(item),
				true);
			Log.v("Set Dict as Installed", listOfDicts.get(item)
				.getName());
		    }
		    // editor.putBoolean(FIRST_TIME_PREFS_KEY, true);
		    // editor.commit();
		    run();
		}
	    }
	}
    };

    private void run() {
	// If first time running app
	if (!prefs.getBoolean(FIRST_TIME_PREFS_KEY, false)) {
	    // If no connection open settings otherwise commence download
	    if (!isNetworkAvailable()) {
		showInternetConnectionDialog();
	    } else {
		showWarningDialog();
	    }
	} else {
	    // Fix to prevent images from showing in gallery
	    if (!prefs.getBoolean(IMAGES_IN_GALLERY_FIX_PREFS_KEY, false)) {
		File noMediaFile = new File(path, ".nomedia");
		noMediaFile.mkdir();
		editor.putBoolean(IMAGES_IN_GALLERY_FIX_PREFS_KEY, true);
		editor.commit();
	    }

	    Intent i = new Intent();
	    setResult(RESULT_OK, i);
	    finish();
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == 0) {
	    run();
	}
    }

    public void showWarningDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog
		.setTitle(
			getResources().getString(R.string.dialog_warning_title))
		.setMessage(getResources().getString(R.string.dialog_message))
		.setCancelable(false)
		.setNegativeButton(
			getResources().getString(R.string.dialog_cancel_button),
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				dialog.cancel();
				Intent i = new Intent();
				setResult(RESULT_CANCELED, i);
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
				dialog.dismiss();
				showDownloadDialog();
			    }
			}).create();
	alertDialog.show();
    }

    public void showDownloadDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	mSelectedItems = new ArrayList<Integer>();

	final CharSequence[] items = new CharSequence[listOfDicts.size()];
	int count = 0;
	for (Dictionary dict : listOfDicts) {
	    items[count] = dict.toString();
	    count++;
	}
	alertDialog
		.setTitle(
			getResources().getString(R.string.dialog_choose_title))
		.setCancelable(false)
		.setMultiChoiceItems(items, null,
			new DialogInterface.OnMultiChoiceClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which, boolean isChecked) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),
					listOfDicts.get(which).toString(),
					Toast.LENGTH_SHORT).show();

				if (isChecked) {
				    // If the user checked the item, add it to
				    // the selected items
				    mSelectedItems.add(which);
				} else if (mSelectedItems.contains(which)) {
				    // Else, if the item is already in the
				    // array, remove it
				    mSelectedItems.remove(Integer
					    .valueOf(which));
				}

			    }
			})
		.setNegativeButton(
			getResources().getString(R.string.dialog_cancel_button),
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				dialog.cancel();
				Intent i = new Intent();
				setResult(RESULT_CANCELED, i);
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
				progressDialogManager.setSelectedItems(mSelectedItems);
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

    @Override
    protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	registerReceiver(onDownloadCompleteReceiver, new IntentFilter(
		DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	registerReceiver(UnzipServiceReceiver, new IntentFilter(
		UnzipService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
	super.onPause();
	overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
	unregisterReceiver(UnzipServiceReceiver);
	unregisterReceiver(onDownloadCompleteReceiver);
    }
}