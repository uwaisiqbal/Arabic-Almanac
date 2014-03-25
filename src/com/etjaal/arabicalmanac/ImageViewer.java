package com.etjaal.arabicalmanac;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

public class ImageViewer extends Activity implements OnClickListener {

    private TouchImageView imdisplayImage;
    private String path;
    private ArrayList<String> indexes;
    private Button topNextPageButton, topLastPageButton, bottomNextPageButton,
	    bottomLastPageButton;
    private int index;
    private Dictionary dict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	indexes = new ArrayList<String>();
	imdisplayImage = (TouchImageView) findViewById(R.id.imshow);
	topNextPageButton = (Button) findViewById(R.id.bTopNextPage);
	topNextPageButton.setOnClickListener(this);
	bottomNextPageButton = (Button) findViewById(R.id.bBottomNextPage);
	bottomNextPageButton.setOnClickListener(this);
	topLastPageButton = (Button) findViewById(R.id.bTopPreviousPage);
	topLastPageButton.setOnClickListener(this);
	bottomLastPageButton = (Button) findViewById(R.id.bBottomPreviousPage);
	bottomLastPageButton.setOnClickListener(this);
	
	dict = new Dictionary("hw4", "Hans Wehr");
	imdisplayImage.setMaxZoom(4);
	imdisplayImage.maintainZoomAfterSetImage(false);
	path = Environment.getExternalStorageDirectory().toString() + "/"
		+ getResources().getString(R.string.app_name) + "/";

	index = 0;
	Bitmap bmp = BitmapFactory.decodeFile(path + "img/" + dict.getReference() + "/0/" + dict.getReference() + "-0001.png");
	imdisplayImage.setImageBitmap(bmp);
	parseFileToArrayList();
	handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
	// TODO Auto-generated method stub
	setIntent(intent);
	handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
	// TODO Auto-generated method stub
	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    String query = intent.getStringExtra(SearchManager.QUERY);
	    doMySearch(query);
	}
    }

    private void doMySearch(String query) {
	// TODO Auto-generated method stub

	query = convertFromRomanToArabic(query);
	index = Arrays.binarySearch(indexes.toArray(), query);
	if (index < 0) {
	    index = (index +1) * (-1);
	}
	DisplayImageUsingIndex();

    }

    private void DisplayImageUsingIndex() {
	// TODO Auto-generated method stub
	String indexString = Integer.toString(index);
	int folder = (int) Math.round(index / 100 - 0.5f);
	int length = String.valueOf(index).length();
	if (length < 4) {
	    for (int i = length; i < 4; i++) {
		indexString = "0" + indexString;
	    }
	}
	String location = path + "img/" + dict.getReference() + "/" + Integer.toString(folder)
		+ "/" + dict.getReference() + "-" + indexString + ".png";
	Log.v("location", location);
	Bitmap bmp = BitmapFactory.decodeFile(location);
	imdisplayImage.setImageBitmap(bmp);
    }

    private String convertFromRomanToArabic(String query) {
	// TODO Auto-generated method stub
	query = query.replaceAll("[إآٱأءﺀﺀﺁﺃﺅﺇﺉ]", "ا");
	query = query.replaceAll("[إآٱأءﺀﺀﺁﺃﺅﺇﺉ]", "ا");
	query = query.replaceAll("[ﻯ]", "ي");
	query = query.replaceAll("th", "ث");
	query = query.replaceAll("gh", "غ");
	query = query.replaceAll("[gG]", "غ");
	query = query.replaceAll("kh", "خ");
	query = query.replaceAll("sh", "ش");
	query = query.replaceAll("dh", "ذ");
	query = query.replaceAll("d", "د");
	query = query.replaceAll("D", "ض");
	query = query.replaceAll("z", "ز");
	query = query.replaceAll("Z", "ظ");
	query = query.replaceAll("s", "س");
	query = query.replaceAll("S", "ص");
	query = query.replaceAll("t", "ت");
	query = query.replaceAll("T", "ط");
	query = query.replaceAll("h", "ه");
	query = query.replaceAll("H", "ح");
	query = query.replaceAll("[xX]", "خ");
	query = query.replaceAll("[vV]", "ث");
	query = query.replaceAll("[aA]", "ا");
	query = query.replaceAll("[bB]", "ب");
	query = query.replaceAll("[jJ]", "ج");
	query = query.replaceAll("[7]", "ح");
	query = query.replaceAll("[rR]", "ر");
	query = query.replaceAll("[3]", "ع");
	query = query.replaceAll("[eE]", "ع");
	query = query.replaceAll("[fF]", "ف");
	query = query.replaceAll("[qQ]", "ق");
	query = query.replaceAll("[kK]", "ك");
	query = query.replaceAll("[lL]", "ل");
	query = query.replaceAll("[mM]", "م");
	query = query.replaceAll("[nN]", "ن");
	query = query.replaceAll("[wW]", "و");
	query = query.replaceAll("[yY]", "ي");
	return query;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// TODO Auto-generated method stub
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.imageviewer_activity_actions, menu);

	// Get the SearchView and set the searchable configuration
	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
		.getActionView();

	searchView.setSearchableInfo(searchManager
		.getSearchableInfo(getComponentName()));
	searchView.setIconifiedByDefault(true);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	switch (item.getItemId()) {
	case R.id.action_search:
	    return true;
	case R.id.action_about:
	    Intent i = new Intent(getApplicationContext(), AboutActivity.class);
	    startActivity(i);
	    return true;
	case R.id.action_settings:
	    Intent j = new Intent(getApplicationContext(),
		    SettingsActivity.class);
	    startActivity(j);
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}

    }

    public void parseFileToArrayList() {
	try {
	    JSONObject jb = new JSONObject(loadJSONFromAsset());
	    JSONArray hw4 = jb.getJSONArray("hw4");
	    for (int i = 0; i < hw4.length(); i++) {
		String index = (String) hw4.get(i);
		indexes.add(index);
	    }
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public String loadJSONFromAsset() {
	String json = null;
	try {

	    InputStream is = getAssets().open("hw4_indexes.json");

	    int size = is.available();

	    byte[] buffer = new byte[size];

	    is.read(buffer);

	    is.close();

	    json = new String(buffer, "UTF-8");

	} catch (IOException ex) {
	    ex.printStackTrace();
	    return null;
	}
	return json;
    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.bTopNextPage:
	case R.id.bBottomNextPage:
	    index +=1;
	    DisplayImageUsingIndex();
	    break;
	case R.id.bTopPreviousPage:
	case R.id.bBottomPreviousPage:
	    if(index!= 0){
		index -= 1;
	    }
	    DisplayImageUsingIndex();
	    break;

	}
    }

}
