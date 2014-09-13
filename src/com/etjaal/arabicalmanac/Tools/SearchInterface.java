package com.etjaal.arabicalmanac.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.etjaal.arabicalmanac.R;
import com.etjaal.arabicalmanac.Objects.Dictionary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class SearchInterface {

    private Dictionary dict;
    private ArrayList<String> searchIndexes;
    private int index;
    private Context context;
    private String DIRECTORY_PATH;

    public SearchInterface(Context context, Dictionary dict) {
	this.dict = dict;
	this.context = context;
	searchIndexes = new ArrayList<String>();
	index = 0;
	DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString() + "/"
		+ context.getResources().getString(R.string.app_name) + "/"; 
	parseFileToArrayList();
    }
    
    public void setIndex(int index){
	this.index = index;
    }
    
    public int getIndex(){
	return index;
    }
    
    public void search(String query) {
	// TODO Auto-generated method stub
	query = convertFromRomanToArabic(query);
	index = Arrays.binarySearch(searchIndexes.toArray(), query);
	if (index < 0) {
	    index = (index + 1) * (-1);
	}
    }
    
    /**Returns the path to the image file whose index has been specified*/
    public String getImagePathForIndex() {
	String indexString = Integer.toString(index);
	int folder = (int) Math.round(index / 100 - 0.5f);
	int length = String.valueOf(index).length();
	if (length < 4) {
	    for (int i = length; i < 4; i++) {
		indexString = "0" + indexString;
	    }
	}
	String location = DIRECTORY_PATH +"img/"+ dict.getReference() + "/"
		+ Integer.toString(folder) + "/" + dict.getReference() + "-"
		+ indexString + ".png";
	Log.v("location", location);
	return location;
    }

    private void parseFileToArrayList() {
	try {
	    //Load json file and convert it to an Array List
	    JSONObject jb = new JSONObject(loadJSONFromAsset());
	    JSONArray dictIndexes = jb.getJSONArray(dict.getReference());
	    for (int i = 0; i < dictIndexes.length(); i++) {
		String index = (String) dictIndexes.get(i);
		searchIndexes.add(index);
	    }
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    
    /**Loads the json file located in assests into a json object*/
    private String loadJSONFromAsset() {
	String json = null;
	try {
	    InputStream is = context.getAssets().open(
		    dict.getReference() + "_indexes.json");
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
//Comment
    /**Converts Roman characters in the string to their corresponding Arabic counterparts*/
    private String convertFromRomanToArabic(String query) {
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


}
