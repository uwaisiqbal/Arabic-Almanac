package com.etjaal.arabicalmanac.Services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Activities.MainActivity;

public class UnzipService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String ZIP_FILE = "zipFile";
    public static final String UNZIP_PATH = "unzipPath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.etjaal.arabicalmanac.Services";
    public static final String UNZIP_RUNNING = "UnzipRunning";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";

    public UnzipService() {
	super("UnzipService");
	// TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	// TODO Auto-generated method stub
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	editor = prefs.edit();
	editor.putBoolean(UNZIP_RUNNING, true);
	editor.commit();

	setupNotification();

	ArrayList<String> zipFiles = intent.getStringArrayListExtra(ZIP_FILE);
	String path = intent.getStringExtra(UNZIP_PATH);
	// Unzip the files
	for (String zipFile : zipFiles) {
	    Unzip(zipFile, path);
	    DeleteRecursive(new File(zipFile));
	}
	result = Activity.RESULT_OK;
	publishResults(result);
    }

    private void setupNotification() {
	// TODO Auto-generated method stub
	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
		new Intent(getBaseContext(), MainActivity.class),
		PendingIntent.FLAG_ONE_SHOT);
	mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	mBuilder = new NotificationCompat.Builder(this);
	mBuilder.setContentTitle(getResources().getString(R.string.app_name));
	mBuilder.setContentText(
		getResources().getString(R.string.progress_bar_unzip_title))
		.setSmallIcon(android.R.drawable.stat_sys_download)
		.setContentIntent(pendingIntent);
	mBuilder.setProgress(0, 0, true);
	mNotifyManager.notify(0, mBuilder.build());
    }

    private void publishResults(int result) {
	// TODO Auto-generated method stub
	editor.putBoolean(FIRST_TIME_PREFS_KEY, true);
	editor.putBoolean(UNZIP_RUNNING, false);
	editor.commit();

	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
		new Intent(getBaseContext(), MainActivity.class),
		PendingIntent.FLAG_ONE_SHOT);
	mBuilder.setContentTitle(getResources().getString(R.string.app_name))
		.setContentText("All files have been successfully installed")
		.setProgress(0, 0, false)
		.setSmallIcon(android.R.drawable.stat_sys_download_done)
		.setContentIntent(pendingIntent);
	mNotifyManager.notify(0, mBuilder.build());

	Intent intent = new Intent(NOTIFICATION);
	intent.putExtra(RESULT, result);
	sendBroadcast(intent);
    }

    public static boolean[] loadBooleanArray(String prefsKey, Context context) {
	SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(context);
	int size = prefs.getInt(prefsKey + "_size", 0);
	boolean[] array = new boolean[size];
	for (int i = 0; i < size; i++) {
	    array[i] = prefs.getBoolean(prefsKey + i, false);
	}
	return array;
    }

    public static void saveBooleanArray(String prefsKey, boolean[] array,
	    Context context) {
	SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(context);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putInt(prefsKey + "_size", array.length);
	for (int i = 0; i < array.length; i++) {
	    editor.remove(prefsKey + i);
	    editor.putBoolean(prefsKey + i, array[i]);
	}
	editor.commit();
    }

    private void DeleteRecursive(File fileOrDirectory) {
	if (fileOrDirectory.isDirectory())
	    for (File child : fileOrDirectory.listFiles())
		DeleteRecursive(child);
	fileOrDirectory.delete();

    }

    public static void Unzip(String zipFile, String location) {
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

}
