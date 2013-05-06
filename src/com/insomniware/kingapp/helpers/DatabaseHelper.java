package com.insomniware.kingapp.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_LOCATIONS = "hidden_locations";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_INFO = "info";
	public static final String COLUMN_EX_ID = "ex_id";

	private static final String DATABASE_NAME = "locations.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = "create table "
		      + TABLE_LOCATIONS + "(" + COLUMN_ID
		      + " integer primary key autoincrement, " + COLUMN_LATITUDE
		      + " decimal not null, " + COLUMN_LONGITUDE +
		      " decimal not null," + COLUMN_INFO + 
		      " text not null, +" + COLUMN_EX_ID +
		      " integer not null);";
	
	public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
        		+ newVersion + ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		onCreate(db);
	}

}
