package com.etjaal.arabicalmanac;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class ImageViewer extends Activity {

    private TouchImageView imdisplayImage;
    private String path;
    private ArrayList<String> indexes;

    /*TODO: For Search:
     * 		Setup Action Bar
     * 		Add in a edit box so users can search
     * 		Validate search parameters
     * 		Convert from roman letters to Arabic
     * 		Search using supplied parameter
     * 		If not found load the closest page
     * 		
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	indexes = new ArrayList<String>();
	imdisplayImage = (TouchImageView) findViewById(R.id.imshow);
	imdisplayImage.setMaxZoom(4);
	path = Environment.getExternalStorageDirectory().toString() + "/"
		+ getResources().getString(R.string.app_name) + "/";

	Bitmap bmp = BitmapFactory.decodeFile(path + "img/hw4/1/hw4-0101.png");
	imdisplayImage.setImageBitmap(bmp);
	parseFileToArrayList();
	int id = indexes.indexOf("وهم");
	
	Log.v("AA", "" + Integer.toString(id));
	Bitmap bmp2 = BitmapFactory.decodeFile(path + "img/hw4/13/" +"/hw4-"+Integer.toString(id) + ".png");
	imdisplayImage.setImageBitmap(bmp2);
	
    }

    public void parseFileToArrayList(){
	try {
	    JSONObject jb = new JSONObject(loadJSONFromAsset());
	    JSONArray hw4 = jb.getJSONArray("hw4");
	    for(int i=0; i<hw4.length(); i++){
		String index = (String) hw4.get(i);
		Log.i("AA", "" + index);
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
    
    

}
