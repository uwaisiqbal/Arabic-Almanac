package com.etjaal.arabicalmanac.Activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.etjaal.arabicalmanac.R;

public class AboutExtensionActivity extends Activity {

    String intentAction;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	getActionBar().setDisplayHomeAsUpEnabled(true);
	setContentView(R.layout.activity_about_extension);
        overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
        tv = (TextView) findViewById(R.id.tv_about_extenstion);
        
        //Make any links in the text view clickable
        if(tv != null){
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
        
	intentAction = getIntent().getAction();
	setupViewBasedOnIntent();
    }
 
    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
        super.onPause();
    }

    private void setupViewBasedOnIntent() {
	Log.v("About", intentAction);
	switch (intentAction) {
	case "Activities.FAQActivity":
	    String text = getResources().getString(R.string.faq_activity_text);
	    tv.setText(Html.fromHtml(text));
	    tv.setTextSize(18);
	    break;
	}
    }

}
