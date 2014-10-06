package com.etjaal.arabicalmanac.Tools;

import com.etjaal.arabicalmanac.R;

import android.app.ProgressDialog;
import android.content.Context;

public class UnzipProgressDialogManager {
    
    private ProgressDialog progressDialog;
    private Context context;
    
    
    public UnzipProgressDialogManager(Context context){
	this.context = context;
	progressDialog = new ProgressDialog(context);
	setupProgressDialog();
	
    }
    
    private void setupProgressDialog() {
	// TODO Auto-generated method stub
	progressDialog.setIndeterminate(true);
	progressDialog.setProgressNumberFormat(null);
	progressDialog.setProgressPercentFormat(null);
	progressDialog.setTitle(context.getResources().getString(R.string.progress_bar_unzip_title));
	progressDialog.setTitle(context.getResources().getString(R.string.progress_bar_unzip_message));
    }

    public void showUnzipProgressDialog(){
	progressDialog.show();
    }
    
    public void dismissUnzipProgressDialog(){
	progressDialog.dismiss();
    }

}
