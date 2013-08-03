package com.fcbm.contentproviderstub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class TestContentProvider extends ContentProvider {

	private static final String TAG = "TestContentProvider";

	// TODO: create "contract" inner class
	
	// Database metadata declarations
	public static final String AUTHORITY = "com.fcbm.contentproviderstub";
	public static final String DATABASE_MOVIE = "dbmovie";
	private static final int DATABASE_MOVIE_VERSION = 1;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DATABASE_MOVIE);
	
	// Mime Type declarations - used by ContentProvider.getType()
	private static final String CONTENT_ANDROID_TYPE = "vnd.android.cursor.dir";
	private static final String CONTENT_ANDROID_ITEM_TYPE = "vnd.android.cursor.item";
	private static final String CONTENT_MOVIE_SUBTYPE = "vnd.contentproviderstub.movie";
	public static final String CONTENT_MOVIE_MIMETYPE = CONTENT_ANDROID_TYPE + "/" + CONTENT_MOVIE_SUBTYPE;
	public static final String CONTENT_MOVIE_ITEM_MIMETYPE = CONTENT_ANDROID_ITEM_TYPE + "/" + CONTENT_MOVIE_SUBTYPE;
	
	// Table specific metadata declarations
	public static final String TABLE_MOVIE = "tablemovie";
	public static final String COL_MOVIE_ID = "_id";
	public static final String COL_MOVIE_TITLE = "title";
	public static final String COL_MOVIE_DIRECTOR = "director";
	public static final String COL_MOVIE_DESCRIPTION = "description";
	public static final String COL_MOVIE_RATING = "rating";
	public static final String COL_MOVIE_DATE = "date";
	public static final String COL_MOVIE_BANNER = "_data";
	
	// UriMatcher metadata declarations
	// Provide a mechanism to identify all incoming Uri patterns
	private static UriMatcher mMatcher;
	private static final String PATTERN_SINGLE_ITEM = "movies";
	private static final String PATTERN_DIRECTORY 	= "movies/#";
	private static final int TYPE_ID_SINGLE_ITEM 	= 1;
	private static final int TYPE_ID_DIRECTORY 		= 2;
	
	static
	{
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI(AUTHORITY, PATTERN_SINGLE_ITEM, TYPE_ID_SINGLE_ITEM);;
		mMatcher.addURI(AUTHORITY, PATTERN_DIRECTORY, TYPE_ID_DIRECTORY);;
	}
	
	// SQLiteOpenHelper implementation
	private static class DbHelper extends SQLiteOpenHelper
	{
		private static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE_MOVIE + " ("
				+ COL_MOVIE_ID + " INTEGER PRIMARY KEY," +
				COL_MOVIE_TITLE + " TEXT," +
				COL_MOVIE_DIRECTOR + " TEXT," +
				COL_MOVIE_DESCRIPTION + " TEXT," +
				COL_MOVIE_DATE + " INTEGER," +
				COL_MOVIE_RATING + " INTEGER," +
				COL_MOVIE_BANNER + " INTEGER)";
		
		private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_MOVIE;
		
		public DbHelper(Context context) {
			super(context, DATABASE_MOVIE, null, DATABASE_MOVIE_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL( DROP_STATEMENT );
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( CREATE_STATEMENT );
		}		
	};
	
	private DbHelper mDbMovieHelper;	
	

	// ContentProvider overridden methods
	
	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");
		mDbMovieHelper = new DbHelper(getContext());
		return true;
	}	
	
	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "getType");
		String aRetVal = null;
		
		switch (mMatcher.match(uri))
		{
		case TYPE_ID_DIRECTORY:
			aRetVal = CONTENT_MOVIE_MIMETYPE;
			break;
		case TYPE_ID_SINGLE_ITEM:
			aRetVal = CONTENT_MOVIE_ITEM_MIMETYPE;
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		return aRetVal;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		Log.d(TAG, "delete");
		String aNewSelection;
		switch ( mMatcher.match(uri)) {
		case TYPE_ID_SINGLE_ITEM:
			aNewSelection = COL_MOVIE_ID + "=" + uri.getPathSegments().get(1) + 
				(TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
			break;
		case TYPE_ID_DIRECTORY:
			aNewSelection = selection;
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		// To delete all rows we need to set "1" - Test this
		if (aNewSelection == null)
		{
			aNewSelection = "1";
		}
		
		SQLiteDatabase db = mDbMovieHelper.getWritableDatabase();
		int aRetVal = db.delete(TABLE_MOVIE, aNewSelection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		
		return aRetVal;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Log.d(TAG, "insert");
		Uri aRetUri = null;
		
		// Validate Uri
		if (mMatcher.match(uri) != TYPE_ID_SINGLE_ITEM)
		{
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		// Validate ContentValues : create it if null, and fill with default values
		if (values == null)
		{
			values = new ContentValues();
		}
		if (values.containsKey( COL_MOVIE_TITLE ) == false)
		{
			throw new SQLException("Failed to insert row because Movie Title is needed " + uri);
		}
		if (values.containsKey( COL_MOVIE_DIRECTOR ) == false)
		{
			values.put(COL_MOVIE_TITLE, "DefaultDirector");
		}
		// TODO: fill other default values..
		
		// Insert data into the database
		SQLiteDatabase db = mDbMovieHelper.getWritableDatabase();
		
		// Note the null value for the "null column hack"
		long aNewRowId = db.insert(TABLE_MOVIE, null, values);
		if (aNewRowId <= 0)
		{
			throw new SQLException("Failed to insert row into: " + uri);
		}

		// Prepare return Uri
		aRetUri = ContentUris.withAppendedId(CONTENT_URI, aNewRowId);
		
		// Notify that something has changed
		getContext().getContentResolver().notifyChange(aRetUri, null);
		
		return aRetUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		Log.d(TAG, "update");
		int aRetUpdatedRows = 0;
		
		String aNewSelection;
		switch ( mMatcher.match(uri)) {
		case TYPE_ID_SINGLE_ITEM:
			aNewSelection = COL_MOVIE_ID + "=" + uri.getPathSegments().get(1) + 
				(TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
			break;
		case TYPE_ID_DIRECTORY:
			aNewSelection = selection;
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		SQLiteDatabase db = mDbMovieHelper.getWritableDatabase();
		aRetUpdatedRows = db.update(TABLE_MOVIE, values, aNewSelection, selectionArgs);

		// Notify that something has changed
		getContext().getContentResolver().notifyChange(uri, null);

		return aRetUpdatedRows;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		Log.d(TAG, "query");
		Cursor aRetCursor = null;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		switch ( mMatcher.match(uri)) {
		case TYPE_ID_SINGLE_ITEM:
			queryBuilder.setTables( TABLE_MOVIE );
			queryBuilder.appendWhere( COL_MOVIE_ID + "=" + uri.getPathSegments().get(1) );
			break;
		case TYPE_ID_DIRECTORY:
			queryBuilder.setTables( TABLE_MOVIE );
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		SQLiteDatabase db = mDbMovieHelper.getReadableDatabase();
		aRetCursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		// TODO: check if this is really needed, in PA4 this is not present
		aRetCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return aRetCursor;
	}
	
	@Override 
	// TODO: consider what the effect of deleting a row should have on the underlying file
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
	{
		// Find the row id and use it as filename
		String rowId = uri.getPathSegments().get(1);
		
		// Create a file object in the application's external files directory
		String fDir = Environment.DIRECTORY_PICTURES;
		File f = new File(getContext().getExternalFilesDir(fDir), rowId);
		
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			} catch (IOException e)
			{
				Log.d(TAG, "Failed to create file: " + e.getMessage());
			}
		}
		
		// Translate the mode parameter to the corresponding Parcel File Descriptor open mode
		int fileMode = 0;
		if (mode.contains("w"))
		{
			fileMode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
		}
		if (mode.contains("r"))
		{
			fileMode |= ParcelFileDescriptor.MODE_READ_ONLY; 
		}
		if (mode.contains("+"))
		{
			fileMode |= ParcelFileDescriptor.MODE_APPEND;
		}
		
		ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, fileMode);

		// Return a Parcel File Descriptor that represents the file
		return pfd;
	}

}
