package com.etjaal.arabicalmanac.Services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadService extends Service {

    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String FIRST_TIME_PREFS_KEY = "firstTime";
    private static final String DOWNLOAD_SERVICE_RUNNING = "downloadServiceRunning";
    private static final String INDETERMINATE_STAGE = "indeterminateStage";
    String TAG = "Download Service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// TODO Auto-generated method stub
	String url = (String) intent.getExtras().get("url");
	String path = (String) intent.getExtras().get("path");
	String fileName = (String) intent.getExtras().get("fileName");
	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	editor = prefs.edit();
	editor.putBoolean(DOWNLOAD_SERVICE_RUNNING, true);
	editor.putBoolean(INDETERMINATE_STAGE, false);
	editor.commit();
	new DoBackgroundDownloadTask().execute(new String[] { url, path,
		fileName });
	return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
    }

    private class DoBackgroundDownloadTask extends
	    AsyncTask<String, Integer, String> {

	@Override
	protected String doInBackground(String... params) {
	    try {
		// TODO Auto-generated method stub

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
		editor.putBoolean(INDETERMINATE_STAGE, true);
		editor.commit();
		publishProgress(101);
		Unzip(path + fileName, path);
		file.delete();
		return null;
	    } catch (Exception e) {
		e.printStackTrace();
		return e.toString();
	    }
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	    // TODO Auto-generated method stub
	    Intent i = new Intent();
	    i.setAction("download");
	    i.putExtra("progress", values[0]);
	    sendOrderedBroadcast(i, null);
	}

	@Override
	protected void onPostExecute(String result) {
	    // TODO Auto-generated method stub
	    editor.putBoolean(DOWNLOAD_SERVICE_RUNNING, false);
	    editor.commit();
	    Intent j = new Intent();
	    j.setAction("download");
	    int resultCode;
	    if (result != null) {
		// download failed
		resultCode = -1;
		editor.putBoolean(FIRST_TIME_PREFS_KEY, false);
		editor.commit();
	    } else {
		// download succeeded
		resultCode = -2;
		editor.putBoolean(FIRST_TIME_PREFS_KEY, true);
		editor.commit();
	    }

	    j.putExtra("progress", resultCode);
	    sendOrderedBroadcast(j, null);
	    stopSelf();
	}

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
