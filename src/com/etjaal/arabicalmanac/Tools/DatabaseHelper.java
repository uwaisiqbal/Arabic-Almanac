package com.etjaal.arabicalmanac.Tools;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "arabicalmanac";
    public static final String DICTIONARY_KEY_ID = "id";
    public static final String DICTIONARY_TABLE_NAME = "dictionaries";
    public static final String DICTIONARY_KEY_REF = "ref";
    public static final String DICTIONARY_KEY_NAME = "name";
    public static final String DICTIONARY_KEY_LANGUAGE = "lang";
    public static final String DICTIONARY_KEY_CODE = "code";
    public static final String DICTIONARY_KEY_INSTALLED = "installed";

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
		+ DICTIONARY_KEY_CODE + " TEXT," + DICTIONARY_KEY_INSTALLED
		+ "BOOLEAN" + ")";
	db.execSQL(CREATE_DICTIONARY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub

    }

}
