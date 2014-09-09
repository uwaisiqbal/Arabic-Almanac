package com.etjaal.arabicalmanac.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.util.IabHelper;
import com.etjaal.arabicalmanac.util.IabResult;
import com.etjaal.arabicalmanac.util.Inventory;
import com.etjaal.arabicalmanac.util.Purchase;

public class AboutActivity extends PreferenceActivity {

    private static final String SKU_DONATION = "0";
    Context context;
    IabHelper mHelper;
    String TAG = "AboutActivity";
    String intentAction;
    Preference email_pref, donate_pref, feedback_email_pref;
    IInAppBillingService mService;
    boolean donationPurchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// setContentView(R.layout.activity_about);
	context = this;
	getActionBar().setDisplayHomeAsUpEnabled(true);
	overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);
	SetupInAppBilling();

	// Load prefs from xml
	addPreferencesFromResource(R.xml.about);
	email_pref = (Preference) findPreference("email_app_pref");
	donate_pref = (Preference) findPreference("donate_app_pref");
	feedback_email_pref = (Preference) findPreference("feedback_email_app_pref");


	// Handle email pref click event
	email_pref
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

		    @Override
		    public boolean onPreferenceClick(Preference arg0) {
			Intent email = new Intent(Intent.ACTION_SENDTO, Uri
				.fromParts("mailto",
					"arabicalmanacandroid@gmail.com", null));
			email.putExtra(Intent.EXTRA_SUBJECT,
				"Technical Support");
			startActivity(Intent.createChooser(email,
				"Choose an Email client :"));
			return true;
		    }
		});

	// Handle Donate pref click event
	donate_pref
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

		    @Override
		    public boolean onPreferenceClick(Preference preference) {
			mHelper.launchPurchaseFlow((Activity) context,
				SKU_DONATION, 10001, mPurchaseFinishedListener,
				null);

			return true;
		    }

		});
	
	feedback_email_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

	    @Override
	    public boolean onPreferenceClick(Preference arg0) {
		Intent email = new Intent(Intent.ACTION_SENDTO, Uri
			.fromParts("mailto",
				"arabicalmanacandroid@gmail.com", null));
		email.putExtra(Intent.EXTRA_SUBJECT,
			"Feedback and Suggestions");
		startActivity(Intent.createChooser(email,
			"Choose an Email client :"));
		return true;
	    }
	    
	});

    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
	public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
	    if (result.isFailure()) {
		Log.d(TAG, "Error purchasing: " + result);
		return;
	    } else if (purchase.getSku().equals(SKU_DONATION)) {
		// consume the donation and update the UI
		// Query Purchased Item
		donate_pref.setEnabled(false);
		mHelper.queryInventoryAsync(mGotInventoryListener);
	    }
	}
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
	public void onQueryInventoryFinished(IabResult result,
		Inventory inventory) {

	    if (result.isFailure()) {
		// handle error here
		Log.d(TAG, "Get Inventory Failure");
	    } else {
		// does the user have the donation purchased?
		donationPurchased = inventory.hasPurchase(SKU_DONATION);
		// Consume Purchased Donation
		if (donationPurchased) {
		    mHelper.consumeAsync(inventory.getPurchase(SKU_DONATION),
			    mConsumeFinishedListener);
		}
	    }
	}
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
	public void onConsumeFinished(Purchase purchase, IabResult result) {
	    if (result.isSuccess()) {
		// provision the in-app purchase to the user
		donate_pref.setEnabled(true);
		Toast.makeText(getApplicationContext(),
			"Donation Successful! Thank you for your support.",
			Toast.LENGTH_SHORT).show();
		Log.v(TAG, "Purchase Successful");
	    } else {
		Log.v(TAG, "Purchase Failed");
		Toast.makeText(getApplicationContext(),
			"Donation Failed! Please try again!",
			Toast.LENGTH_SHORT).show();
	    }
	}
    };

    private void SetupInAppBilling() {
	String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6ECYlk9FDoWQL8XSDuEwzLWa+"
		+ "5EU+4CDNIhoq7WMZFY3Nj+ewHN/ylwbBtS7/N2MmOuqX/LzA9iqpRTlbLlQZxoO5dPotRh+zIkcxLyssh1YCWMbNnyv5Iv"
		+ "chIFBEun7AxZhkaTE0FyocVzd+jClfEJUbFa2op5jWcl8tkCceYi6PGJNC0ogcn2p2+kddjo8VJWX4qdMxykccXm5l"
		+ "+ShDNFbcsX/1gFYzIQQ6QFGkAXRrxNEBqsiuVdo2wPNbBOHrFE8q4B3PCXs1413Ol8ykvFRdTJENMA2l2VFlEfms"
		+ "2IhAU7Zhl/osaX/M/672zjBgWsp8Ob70GfMZC6VCGpXkQIDAQAB";

	// compute your public key and store it in base64EncodedPublicKey
	mHelper = new IabHelper(this, base64EncodedPublicKey);
	mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
	    public void onIabSetupFinished(IabResult result) {
		if (!result.isSuccess()) {
		    // Oh noes, there was a problem.
		    Log.d(TAG, "Problem setting up In-app Billing: " + result);
		} else {
		    // Hooray, IAB is fully set up!
		    mHelper.queryInventoryAsync(mGotInventoryListener);
		    Log.d(TAG, "In-app Billing is set up OK");
		}
	    }

	});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
         Intent data) 
    {
          if (!mHelper.handleActivityResult(requestCode, 
                  resultCode, data)) {     
        	super.onActivityResult(requestCode, resultCode, data);
          }
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	if (mHelper != null)
	    mHelper.dispose();
	mHelper = null;
    }
    
    @Override
    protected void onPause() {
	overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
	super.onPause();
    }

}