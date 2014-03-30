package com.etjaal.arabicalmanac;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onReceive(Context context, Intent intent) {
	// TODO Implement action when activity is not in the foreground
	// Create and update notification with the progress

	int progress = intent.getExtras().getInt("progress");
	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		new Intent(context, MainActivity.class),
		PendingIntent.FLAG_ONE_SHOT);
	mNotifyManager = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);
	mBuilder = new NotificationCompat.Builder(context);
	PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(),
		0);
	mBuilder.setContentTitle(context.getResources().getString(
		R.string.app_name));
	mBuilder.setContentText(
		context.getResources().getString(
			R.string.progress_bar_download_message))
		.setSmallIcon(android.R.drawable.stat_sys_download)
		.setContentIntent(pi);
	mBuilder.setProgress(100, progress, false);

	if (progress == 101) {
	    mBuilder.setProgress(0, 0, true);
	    mBuilder.setContentText(context.getResources().getString(
		    R.string.progress_bar_unzip_title));
	}
	mNotifyManager.notify(0, mBuilder.build());

	if (progress == -1) {
	    // download failed
	    mBuilder.setContentTitle("Download Failed")
		    .setContentText(
			    "Click to download the necessary files again.")
		    .setProgress(0, 0, false)
		    .setSmallIcon(android.R.drawable.stat_sys_download_done)
		    .setContentIntent(pendingIntent);
	    mNotifyManager.notify(0, mBuilder.build());
	    Toast.makeText(context, "Download failed. Please try again.",
		    Toast.LENGTH_SHORT).show();
	} else if (progress == -2) {
	    // download succeeded
	    mBuilder.setContentTitle("Download Complete")
		    .setContentText(
			    "All files have been downloaded successfully")
		    .setProgress(0, 0, false)
		    .setSmallIcon(android.R.drawable.stat_sys_download_done)
		    .setContentIntent(pendingIntent);
	    mNotifyManager.notify(0, mBuilder.build());
	    Toast.makeText(context,
		    "All files have been successfully downloaded",
		    Toast.LENGTH_SHORT).show();

	}

    }

}
