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

    /**Converts Roman characters in the string to their corresponding Arabic counterparts*/
   private String convertFromRomanToArabic(String query) {
	query = query.replaceAll("[Ø¥Ø¢Ù±Ø£Ø¡ïº€ïº€ïº�ïºƒïº…ïº‡ïº‰]", "Ø§");
	query = query.replaceAll("[Ø¥Ø¢Ù±Ø£Ø¡ïº€ïº€ïº�ïºƒïº…ïº‡ïº‰]", "Ø§");
	query = query.replaceAll("[ï»¯]", "ÙŠ");
	query = query.replaceAll("th", "Ø«");
	query = query.replaceAll("gh", "Øº");
	query = query.replaceAll("[gG]", "Øº");
	query = query.replaceAll("kh", "Ø®");
	query = query.replaceAll("sh", "Ø´");
	query = query.replaceAll("dh", "Ø°");
	query = query.replaceAll("d", "Ø¯");
	query = query.replaceAll("D", "Ø¶");
	query = query.replaceAll("z", "Ø²");
	query = query.replaceAll("Z", "Ø¸");
	query = query.replaceAll("s", "Ø³");
	query = query.replaceAll("S", "Øµ");
	query = query.replaceAll("t", "Øª");
	query = query.replaceAll("T", "Ø·");
	query = query.replaceAll("h", "Ù‡");
	query = query.replaceAll("H", "Ø­");
	query = query.replaceAll("[xX]", "Ø®");
	query = query.replaceAll("[vV]", "Ø«");
	query = query.replaceAll("[aA]", "Ø§");
	query = query.replaceAll("[bB]", "Ø¨");
	query = query.replaceAll("[jJ]", "Ø¬");
	query = query.replaceAll("[7]", "Ø­");
	query = query.replaceAll("[rR]", "Ø±");
	query = query.replaceAll("[3]", "Ø¹");
	query = query.replaceAll("[eE]", "Ø¹");
	query = query.replaceAll("[fF]", "Ù�");
	query = query.replaceAll("[qQ]", "Ù‚");
	query = query.replaceAll("[kK]", "Ùƒ");
	query = query.replaceAll("[lL]", "Ù„");
	query = query.replaceAll("[mM]", "Ù…");
	query = query.replaceAll("[nN]", "Ù†");
	query = query.replaceAll("[wW]", "Ùˆ");
	query = query.replaceAll("[yY]", "ÙŠ");
	return query;
    }


}
