package com.fcbm.contentproviderstub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

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
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class TestContentProvider extends ContentProvider {

	private static final String TAG = "TestContentProvider";

    // Definition of the contract for the Movies table of our provider.
    public static final class MoviesTable implements BaseColumns {

        // This class cannot be instantiated
        private MoviesTable() {}

        // The table name offered by this provider
        public static final String TABLE_MOVIES = "tablemovies";

        // The content:// style URL for this table
        public static final Uri CONTENT_URI_MOVIES =  Uri.parse("content://" + AUTHORITY + "/" + TABLE_MOVIES);

        // The content URI base for a single row of data. Callers must, append a numeric row id to this Uri to retrieve a row
        public static final Uri CONTENT_ID_URI_MOVIES = Uri.parse("content://" + AUTHORITY + "/" + TABLE_MOVIES + "/");

    	// Mime Type declarations - used by ContentProvider.getType()
    	private static final String CONTENT_ANDROID_TYPE = "vnd.android.cursor.dir";
    	private static final String CONTENT_ANDROID_ITEM_TYPE = "vnd.android.cursor.item";
    	private static final String CONTENT_MOVIES_SUBTYPE = "vnd.contentproviderstub.movies";
        // The MIME type of DIR
    	public static final String CONTENT_MOVIES_MIMETYPE = CONTENT_ANDROID_TYPE + "/" + CONTENT_MOVIES_SUBTYPE;
        // The MIME type of a single row.
    	public static final String CONTENT_MOVIES_ITEM_MIMETYPE = CONTENT_ANDROID_ITEM_TYPE + "/" + CONTENT_MOVIES_SUBTYPE;
        
        // Column names
    	// public static final String COL_ID = "_id"; // _ID is inherited from BaseColumns
    	public static final String COL_TITLE = "title";
    	public static final String COL_DIRECTOR = "director";
    	public static final String COL_DESCRIPTION = "description";
    	public static final String COL_RATING = "rating";
    	public static final String COL_DATE = "date";
    	public static final String COL_BANNER = "_data";

        // The default sort order for this table
        public static final String MOVIES_DEFAULT_SORT_ORDER = COL_TITLE +"  COLLATE LOCALIZED ASC";
    }
	
	// Database metadata declarations
	public static final String AUTHORITY = "com.fcbm.contentproviderstub";
	
	
	
	// UriMatcher metadata declarations
	// Provide a mechanism to identify all incoming Uri patterns
	private static UriMatcher mMatcher;
	private static final String PATTERN_SINGLE_ITEM = MoviesTable.TABLE_MOVIES + "/#";
	private static final String PATTERN_DIRECTORY 	= MoviesTable.TABLE_MOVIES;
	private static final int TYPE_ID_SINGLE_ITEM 	= 1;
	private static final int TYPE_ID_DIRECTORY 		= 2;
	
	
    // A projection map used to select columns from the database
    private static final HashMap<String, String> mMoviesProjectionMap;
	
	static
	{
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI(AUTHORITY, PATTERN_SINGLE_ITEM, TYPE_ID_SINGLE_ITEM);
		mMatcher.addURI(AUTHORITY, PATTERN_DIRECTORY, TYPE_ID_DIRECTORY);
		
        // Create and initialize projection map for all columns.  This is
        // simply an identity mapping.
		mMoviesProjectionMap = new HashMap<String, String>();
		mMoviesProjectionMap.put(MoviesTable._ID, MoviesTable._ID);
		mMoviesProjectionMap.put(MoviesTable.COL_TITLE, MoviesTable.COL_TITLE);
		mMoviesProjectionMap.put(MoviesTable.COL_DIRECTOR, MoviesTable.COL_DIRECTOR);
		mMoviesProjectionMap.put(MoviesTable.COL_DESCRIPTION, MoviesTable.COL_DESCRIPTION);
		mMoviesProjectionMap.put(MoviesTable.COL_RATING, MoviesTable.COL_RATING);
		mMoviesProjectionMap.put(MoviesTable.COL_DATE, MoviesTable.COL_DATE);
		mMoviesProjectionMap.put(MoviesTable.COL_BANNER, MoviesTable.COL_BANNER);
	}
	
	// SQLiteOpenHelper implementation
	private static class DbHelper extends SQLiteOpenHelper
	{
		private static final String DATABASE_MOVIES = "movies.db";
		private static final int DATABASE_MOVIES_VERSION = 1;
		private static final String CREATE_STATEMENT = "CREATE TABLE " + MoviesTable.TABLE_MOVIES + " ("
				+ MoviesTable._ID + " INTEGER PRIMARY KEY," +
				MoviesTable.COL_TITLE + " TEXT NOT NULL," +
				MoviesTable.COL_DIRECTOR + " TEXT," +
				MoviesTable.COL_DESCRIPTION + " TEXT," +
				MoviesTable.COL_DATE + " INTEGER," +
				MoviesTable.COL_RATING + " INTEGER," +
				MoviesTable.COL_BANNER + " INTEGER)";
		
		private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + MoviesTable.TABLE_MOVIES;
		
		public DbHelper(Context context) {
			super(context, DATABASE_MOVIES, null, DATABASE_MOVIES_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL( DROP_STATEMENT );
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( CREATE_STATEMENT );
			// TODO: consider when it's the case to create an index
            //db.execSQL("CREATE INDEX moviesIndexTitle ON " + TABLE_MOVIES + "(" + COL_TITLE + ");");
		}		
	};
	
	private DbHelper mDbMoviesHelper;	
	

	// ContentProvider overridden methods
	
	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");
		mDbMoviesHelper = new DbHelper(getContext());
		// Assumes that any failures will be reported by a thrown exception.
		return true;
	}	
	
	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "getType");
		String aRetVal = null;
		
		switch (mMatcher.match(uri))
		{
			case TYPE_ID_DIRECTORY:
				aRetVal = MoviesTable.CONTENT_MOVIES_MIMETYPE;
				break;
			case TYPE_ID_SINGLE_ITEM:
				aRetVal = MoviesTable.CONTENT_MOVIES_ITEM_MIMETYPE;
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
				aNewSelection = MoviesTable._ID + "=" + uri.getPathSegments().get(1) + 
					(TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
				break;
			case TYPE_ID_DIRECTORY:
				aNewSelection = selection;
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		// To delete all rows we need to set "1" - TODO: Test this
		if (aNewSelection == null)
		{
			aNewSelection = "1";
		}
		
		SQLiteDatabase db = mDbMoviesHelper.getWritableDatabase();
		int aRetVal = db.delete(MoviesTable.TABLE_MOVIES, aNewSelection, selectionArgs);

		// Notify all observers that something has changed
		getContext().getContentResolver().notifyChange(uri, null);
		
		return aRetVal;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Log.d(TAG, "insert");
		Uri aRetUri = null;
		
		// Validate Uri
		if (mMatcher.match(uri) != TYPE_ID_DIRECTORY)
		{
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}

		// Create a new ContentValue
		ContentValues cv = null;
		if (values == null)
		{
			cv = new ContentValues();
		}
		else
		{
			// Note we do a copy of "values" because we may need to
			// change its content during the validation step below
			cv = new ContentValues(values);
		}
		
		// Validate ContentValues content
		if (cv.containsKey( MoviesTable.COL_TITLE ) == false)
		{
			throw new SQLException("Failed to insert row because Movie Title is needed " + uri);
		}
		if (cv.containsKey( MoviesTable.COL_DIRECTOR ) == false)
		{
			cv.put(MoviesTable.COL_DIRECTOR, "DefaultDirector");
		}
		// TODO: fill other default values..
		
		// TODO: Consider adding a method to check if an entry is already present, and return in case it is.
		// Check whether there are better alternatives, such as playing with keys in the DB
		
		// Insert data into the database
		SQLiteDatabase db = mDbMoviesHelper.getWritableDatabase();
		
		// To add empty rows to your database by passing in an empty 
		// Content Values object you must use the null column hack
		// parameter to specify the name of the column that can be 
		// set to null.
		String nullColumnHack = null;

		Log.d(TAG, "CV to insert: " + cv);
		long aNewRowId = db.insert(MoviesTable.TABLE_MOVIES, nullColumnHack, cv);
		if (aNewRowId <= 0)
		{
			throw new SQLException("Failed to insert row into: " + uri);
		}

		// Prepare return Uri
		aRetUri = ContentUris.withAppendedId(MoviesTable.CONTENT_ID_URI_MOVIES, // Note that we use CONTENT_ID_URI_MOVIES 
																				// and not CONTENT_URI_MOVIES - TODO: test this
				aNewRowId);
		
		// Notify all observers that something has changed
		getContext().getContentResolver().notifyChange(aRetUri, // Note that we use aRetUri and not uri 
				null);
		
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
				aNewSelection = MoviesTable._ID + "=" + uri.getPathSegments().get(1) + 
					(TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
				break;
			case TYPE_ID_DIRECTORY:
				aNewSelection = selection;
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		SQLiteDatabase db = mDbMoviesHelper.getWritableDatabase();
		aRetUpdatedRows = db.update(MoviesTable.TABLE_MOVIES, values, aNewSelection, selectionArgs);

		// Notify all observers that something has changed
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
			// case SEARCH:
			// 	TODO: add implementation to allow search queries
			//	break;
			case TYPE_ID_SINGLE_ITEM:
				queryBuilder.setTables( MoviesTable.TABLE_MOVIES );
				queryBuilder.setProjectionMap(mMoviesProjectionMap);			
				queryBuilder.appendWhere( MoviesTable._ID + "=" + uri.getPathSegments().get(1));
				break;
			case TYPE_ID_DIRECTORY:
				queryBuilder.setTables( MoviesTable.TABLE_MOVIES );
				queryBuilder.setProjectionMap(mMoviesProjectionMap);			
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = MoviesTable.MOVIES_DEFAULT_SORT_ORDER;
        }
	
		String groupBy = null;
		String having = null;
        
		SQLiteDatabase db = mDbMoviesHelper.getReadableDatabase();
		aRetCursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data changes
		// TODO: check if this is really needed, in PAD4 this is not present
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
		File f = new File(
				getContext().getExternalFilesDir(fDir),
				//getContext().getCacheDir()
				rowId);
		
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			} catch (IOException e)
			{
				Log.d(TAG, "Failed to create file: " + e.getMessage());
				// TODO: return null?
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
	
	
	// Methods to handle read/write from assets in the apk
	
    //@Override
    //public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
    //	super.openAssetFile(uri, mode);
    //}
    
    //@Override
    //public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType, Bundle opts, InputStream args) {
		// must implements PipeDataWriter<InputStream>
    //}
    

	
}
