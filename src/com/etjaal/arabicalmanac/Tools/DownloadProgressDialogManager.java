package com.etjaal.arabicalmanac.Tools;

import java.util.ArrayList;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadProgressDialogManager {

    private Context context;
    private Dialog progressDialog;
    private ArrayList<Integer> mSelectedItems;
    private ArrayList<Dictionary> listOfDicts;
    private ArrayList<ProgressBar> pbList;
    private ArrayList<TextView> tvProgressList;
    private ArrayList<TextView> tvTitleList;

    public DownloadProgressDialogManager(Context context,
	    ArrayList<Dictionary> listOfDicts) {
	this.context = context;
	this.listOfDicts = listOfDicts;
	progressDialog = new Dialog(context);
    }
    
    public void setSelectedItems(ArrayList<Integer> mSelectedItems){
	this.mSelectedItems = mSelectedItems;
	setupProgressDialog();
	
    }

    public void setupIndeterminateProgressDialog(int pos) {
	pbList.get(pos).setIndeterminate(true);
	tvTitleList.get(pos).setText(
		context.getResources().getString(
			R.string.progress_bar_unzip_message)
			+ listOfDicts.get(mSelectedItems.get(pos)).getName());
	tvProgressList.get(pos).setText(null);
    }

    public void showProgressDialog() {
	progressDialog.show();
    }
    
    public boolean isShowing(){
	return progressDialog.isShowing();
    }

    public void dismissProgressDialog() {
	progressDialog.dismiss();
    }

    private void setupProgressDialog() {
	// TODO Auto-generated method stub
	progressDialog.setContentView(R.layout.download_dialog_layout);
	progressDialog.setTitle("Download In Progress");
	progressDialog.setCancelable(false);
	progressDialog.setCanceledOnTouchOutside(false);
	LinearLayout ll = (LinearLayout) progressDialog
		.findViewById(R.id.llProgressBars);
	pbList = new ArrayList<ProgressBar>();
	tvTitleList = new ArrayList<TextView>();
	tvProgressList = new ArrayList<TextView>();
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	for (int i = 0; i < mSelectedItems.size(); i++) {
	    View v = inflater.inflate(R.layout.progress_layout, null);
	    ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressBar);
	    pb.setProgress(0);
	    pb.setMax(100);
	    TextView tvTitle = (TextView) v.findViewById(R.id.tvpbTitle);
	    tvTitle.setText("Downloading "
		    + listOfDicts.get(mSelectedItems.get(i)).getName());
	    TextView tvProgress = (TextView) v.findViewById(R.id.tvpbProgress);
	    tvProgress.setText(String.valueOf(0));
	    tvTitleList.add(tvTitle);
	    tvProgressList.add(tvProgress);
	    pbList.add(pb);
	    ll.addView(v);
	}
    }

    public void setProgress(int pos, int progress) {
	pbList.get(pos).setProgress(progress);
	tvProgressList.get(pos).setText(String.valueOf(progress) + "%");
    }
    
    public void setProgressBarComplete(int pos){
	//pbList.get(pos).setIndeterminate(false);
	setProgress(pos, 100);
	tvTitleList.get(pos).setText(listOfDicts.get(mSelectedItems.get(pos)).getName() + " Downloaded");
    }

    public void setMessage(int pos, String message) {
	tvTitleList.get(pos).setText(message);
    }

    public void setTitle(String title) {
	progressDialog.setTitle(title);
    }

}
