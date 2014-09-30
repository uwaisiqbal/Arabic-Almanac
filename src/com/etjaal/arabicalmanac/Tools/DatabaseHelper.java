package com.etjaal.arabicalmanac.Tools;

import java.util.ArrayList;
import java.util.List;

import com.etjaal.arabicalmanac.Objects.Dictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "arabicalmanac";
    public static final String DICTIONARY_KEY_ID = "id";
    public static final String DICTIONARY_TABLE_NAME = "dictionaries";
    public static final String DICTIONARY_KEY_REF = "ref";
    public static final String DICTIONARY_KEY_NAME = "name";
    public static final String DICTIONARY_KEY_LANGUAGE = "lang";
    public static final String DICTIONARY_KEY_DOWNLOAD_LINK = "downloadLink";
    public static final String DICTIONARY_KEY_INSTALLED = "installed";
    public static final String DICTIONARY_KEY_SIZE = "size";

    public String TAG = "DbHelper";

    public DatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	// TODO Auto-generated method stub
	String CREATE_DICTIONARY_TABLE = "CREATE TABLE "
		+ DICTIONARY_TABLE_NAME + "(" + DICTIONARY_KEY_ID
		+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
		+ DICTIONARY_KEY_REF + " TEXT," + DICTIONARY_KEY_NAME
		+ " TEXT," + DICTIONARY_KEY_LANGUAGE + " TEXT,"
		+ DICTIONARY_KEY_DOWNLOAD_LINK + " TEXT,"
		+ DICTIONARY_KEY_INSTALLED + " SMALLINT NOT NULL DEFAULT "
		+ String.valueOf(0) + " ," + DICTIONARY_KEY_SIZE + " INTEGER"
		+ ")";
	db.execSQL(CREATE_DICTIONARY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub

    }

    public void deleteAllData() {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(DICTIONARY_TABLE_NAME, null, null);
	db.close();
    }

    // Dictionaries Table CRUD Operations
    public void addDictionary(Dictionary dictionary) {
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues cv = putDictionaryIntoContentValues(dictionary);
	Log.v(TAG, dictionary.getReference() + " adding to db");
	long rowID = db.insert(DICTIONARY_TABLE_NAME, null, cv);
    }

    public List<Dictionary> getAllDictionaries() {
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.query(DICTIONARY_TABLE_NAME, null, null, null, null,
		null, null);
	List<Dictionary> list = new ArrayList<Dictionary>();
	if (cursor.moveToFirst()) {
	    do {
		Dictionary dict = getDictionaryFromCursor(cursor);
		list.add(dict);
	    } while (cursor.moveToNext());
	}
	return list;
    }

    public List<Dictionary> getListOfDictionariesBasedOnInstallStatus(boolean installed) {
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.query(DICTIONARY_TABLE_NAME, null,
		DICTIONARY_KEY_INSTALLED + "=?",
		new String[] { String.valueOf(putBoolean(installed)) }, null, null, null);
	List<Dictionary> list = new ArrayList<Dictionary>();
	if (cursor.moveToFirst()) {
	    do {
		Dictionary dict = getDictionaryFromCursor(cursor);
		list.add(dict);
	    } while (cursor.moveToNext());
	}
	return list;
    }

    public Dictionary getDictionaryUsingRef(String ref) {
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.query(DICTIONARY_TABLE_NAME, null,
		DICTIONARY_KEY_REF + "=? ", new String[] { ref }, null, null,
		null);
	if (cursor != null) {
	    cursor.moveToFirst();
	}
	Dictionary dict = getDictionaryFromCursor(cursor);
	return dict;
    }

    public void setDictionaryAsInstalled(Dictionary dict, boolean installed) {
	dict.setInstalled(installed);
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues cv = putDictionaryIntoContentValues(dict);
	db.update(DICTIONARY_TABLE_NAME, cv, DICTIONARY_KEY_REF + " =?",
		new String[] { dict.getReference() });
	Log.v(TAG, dict.getReference() + " setting installed status to "
		+ String.valueOf(installed));

    }

    public int getDictionaryCount() {
	String countQuery = "SELECT  * FROM " + DICTIONARY_TABLE_NAME;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(countQuery, null);
	cursor.close();
	return cursor.getCount();
    }

    public Dictionary getDictionaryFromCursor(Cursor cursor) {
	Dictionary dict = new Dictionary();
	dict.setId(cursor.getInt(cursor.getColumnIndex(DICTIONARY_KEY_ID)));
	dict.setName(cursor.getString(cursor
		.getColumnIndex(DICTIONARY_KEY_NAME)));
	dict.setReference(cursor.getString(cursor
		.getColumnIndex(DICTIONARY_KEY_REF)));
	dict.setLanguage(cursor.getString(cursor
		.getColumnIndex(DICTIONARY_KEY_LANGUAGE)));
	dict.setDownloadLink(cursor.getString(cursor
		.getColumnIndex(DICTIONARY_KEY_DOWNLOAD_LINK)));
	dict.setSize(cursor.getInt(cursor.getColumnIndex(DICTIONARY_KEY_SIZE)));
	dict.setInstalled(getBoolean(cursor.getInt(cursor
		.getColumnIndex(DICTIONARY_KEY_INSTALLED))));
	return dict;
    }

    private ContentValues putDictionaryIntoContentValues(Dictionary dictionary) {
	// TODO Auto-generated method stub
	ContentValues cv = new ContentValues();
	cv.put(DICTIONARY_KEY_NAME, dictionary.getName());
	cv.put(DICTIONARY_KEY_REF, dictionary.getReference());
	cv.put(DICTIONARY_KEY_LANGUAGE, dictionary.getLanguage());
	cv.put(DICTIONARY_KEY_DOWNLOAD_LINK, dictionary.getDownloadLink());
	cv.put(DICTIONARY_KEY_SIZE, dictionary.getSize());
	cv.put(DICTIONARY_KEY_INSTALLED, putBoolean(dictionary.isInstalled()));
	return cv;
    }

    /**
     * Since SQLite does not support boolean, will use 1 as true and 0 as false
     */
    private boolean getBoolean(int num) {
	if (num == 1) {
	    return true;
	}
	return false;
    }

    private int putBoolean(boolean installed) {
	if (installed) {
	    return 1;
	}
	return 0;
    }

}
