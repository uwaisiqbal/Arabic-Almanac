package com.etjaal.arabicalmanac.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.etjaal.arabicalmanac.Objects.Dictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseQueries {
	
	public static final int DATABASE_VERSION = 1;
	public static final String DICTIONARY_KEY_ID = "id";
	public static final String DICTIONARY_TABLE_NAME = "dictionaries";
	public static final String DICTIONARY_KEY_INDEX = "index";
	public static final String DICTIONARY_KEY_NAME = "name";
	public static final String DICTIONARY_KEY_LANGUAGE = "lang";
	public static final String DICTIONARY_KEY_INSTALLED = "installed";
	public static final String DICTIONARY_KEY_SIZE = "size";
	public final String TAG = "DatabaseQueries";

	
	private DatabaseHelper dbhelp;
	private SQLiteDatabase database;

	// Initiate a dbhelper to allow use of the DB
	public DatabaseQueries(Context context) {

		dbhelp = new DatabaseHelper(context);
		try {
			dbhelp.createDatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			dbhelp.openDatabase();
			database = dbhelp.getDatabase();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	public void closeDB(){
		dbhelp.closeDatabase();
	}
	
	//Database Operations
	public void deleteAllData() {
		database.delete(DICTIONARY_TABLE_NAME, null, null);
		database.close();
	}

	// Dictionaries Table CRUD Operations
	public void addDictionary(Dictionary dictionary) {
		ContentValues cv = putDictionaryIntoContentValues(dictionary);
		Log.v(TAG, dictionary.getReference() + " adding to db");
		long rowID = database.insert(DICTIONARY_TABLE_NAME, null, cv);
	}

	public List<Dictionary> getAllDictionaries() {
		Cursor cursor = database.query(DICTIONARY_TABLE_NAME, null, null, null, null,
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

	public List<Dictionary> getListOfDictionariesBasedOnInstallStatus(
			boolean isInstalled) {
		Cursor cursor = database.query(DICTIONARY_TABLE_NAME, null,
				DICTIONARY_KEY_INSTALLED + "=?",
				new String[] { String.valueOf(putBoolean(isInstalled)) }, null,
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

	public Dictionary getDictionaryUsingRef(String ref) {
		Cursor cursor = database.query(DICTIONARY_TABLE_NAME, null,
				DICTIONARY_KEY_INDEX + "=? ", new String[] { ref }, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		Dictionary dict = getDictionaryFromCursor(cursor);
		return dict;
	}

	public void setDictionaryAsInstalled(Dictionary dict, boolean installed) {
		dict.setInstalled(installed);
		ContentValues cv = putDictionaryIntoContentValues(dict);
		database.update(DICTIONARY_TABLE_NAME, cv, DICTIONARY_KEY_INDEX + " =?",
				new String[] { dict.getReference() });
		Log.v(TAG, dict.getReference() + " setting installed status to "
				+ String.valueOf(installed));

	}

	public int getDictionaryCount() {
		String countQuery = "SELECT  * FROM " + DICTIONARY_TABLE_NAME;
		Cursor cursor = database.rawQuery(countQuery, null);
		cursor.close();
		return cursor.getCount();
	}

	public Dictionary getDictionaryFromCursor(Cursor cursor) {
		Dictionary dict = new Dictionary();
		dict.setId(cursor.getInt(cursor.getColumnIndex(DICTIONARY_KEY_ID)));
		dict.setName(cursor.getString(cursor
				.getColumnIndex(DICTIONARY_KEY_NAME)));
		dict.setReference(cursor.getString(cursor
				.getColumnIndex(DICTIONARY_KEY_INDEX)));
		dict.setLanguage(cursor.getString(cursor
				.getColumnIndex(DICTIONARY_KEY_LANGUAGE)));
		dict.setSize(cursor.getInt(cursor.getColumnIndex(DICTIONARY_KEY_SIZE)));
		dict.setInstalled(getBoolean(cursor.getInt(cursor
				.getColumnIndex(DICTIONARY_KEY_INSTALLED))));
		return dict;
	}

	private ContentValues putDictionaryIntoContentValues(Dictionary dictionary) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put(DICTIONARY_KEY_NAME, dictionary.getName());
		cv.put(DICTIONARY_KEY_INDEX, dictionary.getReference());
		cv.put(DICTIONARY_KEY_LANGUAGE, dictionary.getLanguage());
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
