package com.etjaal.arabicalmanac;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private String baseLink = "http://ia600803.us.archive.org/2/items/ArabicAlmanac/base.zip";
    private String hansWehrLink = "http://ia600803.us.archive.org/2/items/ArabicAlmanac/hw4.zip";
    private String path;
    private int noOfTasksRemaining;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	path = Environment.getExternalStorageDirectory().toString() + "/"
		+ getResources().getString(R.string.app_name) + "/";
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	editor = prefs.edit();
	run();
    }

    public void showDownloadDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog
		.setTitle(getResources().getString(R.string.dialog_title))
		.setMessage(getResources().getString(R.string.dialog_message))
		.setCancelable(false)
		.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				finish();
			    }
			})
		.setPositiveButton("Download",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// TODO Auto-generated method stub

				noOfTasksRemaining = 2;
				DownloadInBackground task = new DownloadInBackground();
				task.execute(new String[] { baseLink, path,
					"aa" });
				DownloadInBackground task1 = new DownloadInBackground();
				task1.execute(new String[] { hansWehrLink,
					path, "hw4" });
				finish();
			    }
			}).create();
	alertDialog.show();
    }

    public void run() {
	if (!prefs.getBoolean(FIRST_TIME_PREFS_KEY, false)) {
	    if (!isNetworkAvailable()) {
		showInternetConnectionDialog();
	    } else {
		showDownloadDialog();
	    }
	} else {
	    //Fix to prevent images from showing in gallery
	    File noMediaFile = new File(path, ".nomedia");
	    if (!noMediaFile.exists()) {
		noMediaFile.mkdir();
	    }
	    openInBrowser();

	}

    }

    public void showInternetConnectionDialog() {
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	alertDialog
		.setTitle("No network connection")
		.setMessage(
			"Internet connection required. Please connect to the Internet.")
		.setNeutralButton("Go to Settings",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(
					Settings.ACTION_WIRELESS_SETTINGS), 0);
			    }
			}).setCancelable(false).create();
	alertDialog.show();
    }

    /**Once the user returns from the Settings, run the app process again */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == 0) {
	    run();
	}
    }
    
    /**Check if an active network connection is available*/
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

    /**Overwrite the Javascript Configuration file that tells the web app 
     * which dictionaries are available to be used*/
    public void OverwriteConfigFile() {
	String content = getResources().getString(R.string.config_file_base)
		+ getResources().getString(R.string.config_file_var_open)
		+ getResources().getString(R.string.config_file_hw4)
		+ getResources().getString(R.string.config_file_var_close);

	File file = new File(path, "mawrid-conf.js");

	if (file.exists()) {
	    file.delete();
	}

	try {
	    FileOutputStream fos = new FileOutputStream(file);
	    fos.write(content.getBytes());
	    fos.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void openInBrowser() {
	File file = new File(path, "aa.html");
	// Above Android 4.0, Chrome is supported so use that
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	    try {
		Intent i = new Intent("android.intent.action.MAIN");
		i.setComponent(ComponentName
			.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
		i.addCategory("android.intent.category.LAUNCHER");
		i.setData(Uri.fromFile(file));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(i);
		finish();
	    } catch (ActivityNotFoundException e) {
		Toast.makeText(getApplicationContext(),
			"Please install Google Chrome first",
			Toast.LENGTH_SHORT).show();

	    }
	} else {
	    // Below Android 4.0, use default browser instead
	    Intent i = new Intent(Intent.ACTION_VIEW);
	    i.setClassName("com.android.browser",
		    "com.android.browser.BrowserActivity");
	    i.setData(Uri.fromFile(file));
	    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    getApplicationContext().startActivity(i);
	    finish();
	}

    }

    private class DownloadInBackground extends
	    AsyncTask<String, Integer, String> {

	private NotificationManager mNotifyManager;
	private Notification.Builder mBuilder;

	@Override
	protected void onPreExecute() {
	    // TODO Auto-generated method stub
	    mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    mBuilder = new Notification.Builder(getBaseContext());
	    PendingIntent pi = PendingIntent.getActivity(
		    getApplicationContext(), 0, new Intent(), 0);
	    mBuilder.setContentTitle("Downloading")
		    .setContentText("Download in progress")
		    .setSmallIcon(android.R.drawable.stat_sys_download)
		    .setContentIntent(pi);
	    mNotifyManager.notify(0, mBuilder.build());
	}

	@Override
	protected String doInBackground(String... params) {
	    // TODO Auto-generated method stub
	    try {
		URL url = new URL(params[0]);
		URLConnection connection = url.openConnection();
		connection.connect();
		int fileLength = connection.getContentLength();
		String path = params[1];
		String fileName = params[2];
		File myDir = new File(path);
		myDir.mkdirs();
		File file = new File(myDir, fileName);
		if (file.exists())
		    file.delete();
		// Download the file
		InputStream input = new BufferedInputStream(url.openStream());
		OutputStream output = new FileOutputStream(file);

		byte data[] = new byte[1024];
		long total = 0;
		int count;
		int percentComplete = 0;
		while ((count = input.read(data)) != -1) {
		    total += count;
		    if (percentComplete + 1 == (int) (total * 100 / fileLength)) {
			percentComplete = (int) (total * 100 / fileLength);
			publishProgress(percentComplete);
		    }
		    output.write(data, 0, count);
		}
		output.flush();
		output.close();
		input.close();

		publishProgress(101);
		Unzip(path + fileName, path);
		file.delete();
		File noMediaFile = new File(path, ".nomedia");
		noMediaFile.mkdir();
		return null;
	    } catch (Exception e) {
		e.printStackTrace();
		return e.toString();
	    }

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	    // TODO Auto-generated method stub
	    mBuilder.setProgress(100, values[0], false);
	    if (values[0] == 101) {
		mBuilder.setProgress(0, 0, true);
		mBuilder.setContentTitle("Unzipping").setContentText(
			"Unzip In Progress");
	    }
	    mNotifyManager.notify(0, mBuilder.build());
	}

	@Override
	protected void onPostExecute(String result) {
	    // TODO Auto-generated method stub
	    PendingIntent pendingIntent = PendingIntent.getActivity(
		    getApplicationContext(), 0, new Intent(
			    getApplicationContext(), MainActivity.class),
		    PendingIntent.FLAG_ONE_SHOT);
	    // If download failed
	    if (result != null) {
		mBuilder.setContentTitle("Download Failed")
			.setContentText(
				"Click to download the necessary files again.")
			.setProgress(0, 0, false)
			.setSmallIcon(android.R.drawable.stat_sys_download_done)
			.setContentIntent(pendingIntent);
		mNotifyManager.notify(0, mBuilder.build());

		Toast.makeText(getApplicationContext(),
			"Download failed. Please try again.",
			Toast.LENGTH_SHORT).show();

	    } else {
		// If download succeeded
		noOfTasksRemaining--;
		if (noOfTasksRemaining == 0) {

		    mBuilder.setContentTitle("Download Complete")
			    .setContentText(
				    "All files have been downloaded successfully")
			    .setProgress(0, 0, false)
			    .setSmallIcon(
				    android.R.drawable.stat_sys_download_done)
			    .setContentIntent(pendingIntent);
		    mNotifyManager.notify(0, mBuilder.build());

		    Toast.makeText(getApplicationContext(),
			    "All files have been successfully downloaded",
			    Toast.LENGTH_SHORT).show();

		    OverwriteConfigFile();
		    editor.putBoolean(FIRST_TIME_PREFS_KEY, true);
		    editor.commit();
		    openInBrowser();
		}
	    }

	}
    }

    public void Unzip(String zipFile, String location) {
	int BUFFER_SIZE = 1024;
	int count = 0;
	byte[] buffer = new byte[BUFFER_SIZE];

	try {
	    if (!location.endsWith("/")) {
		location += "/";
	    }
	    File f = new File(location);
	    if (!f.isDirectory()) {
		f.mkdirs();
	    }

	    ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
		    new FileInputStream(zipFile), BUFFER_SIZE));
	    try {
		ZipEntry ze = null;
		while ((ze = zin.getNextEntry()) != null) {
		    String path = location + ze.getName();
		    File unzipFile = new File(path);

		    if (ze.isDirectory()) {
			if (!unzipFile.isDirectory()) {
			    unzipFile.mkdirs();
			}
		    } else {
			// check for and create parent directories if
			// they don't
			// exist
			File parentDir = unzipFile.getParentFile();
			if (null != parentDir) {
			    if (!parentDir.isDirectory()) {
				parentDir.mkdirs();
			    }
			}

			// unzip the file
			FileOutputStream out = new FileOutputStream(unzipFile,
				false);
			BufferedOutputStream fout = new BufferedOutputStream(
				out, BUFFER_SIZE);
			try {
			    while ((count = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
				fout.write(buffer, 0, count);
			    }

			    zin.closeEntry();
			} finally {
			    fout.flush();
			    fout.close();

			}
		    }
		}
	    } finally {
		zin.close();
	    }
	} catch (Exception e) {
	    Log.e("Unzip", "Unzip exception", e);
	}

    }

    public static double remainingLocalStorage() {
	final double SIZE_KB = 1024.0;

	final double SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
		.getPath());
	stat.restat(Environment.getExternalStorageDirectory().getPath());
	double bytesAvailable = (double) stat.getBlockSize()
		* (double) stat.getAvailableBlocks();
	return bytesAvailable / SIZE_GB;
    }
}
