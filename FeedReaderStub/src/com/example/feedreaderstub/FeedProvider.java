package com.example.feedreaderstub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class FeedProvider extends ContentProvider {

	private static final String TAG = "FeedProvider"; 
	
	public static final Uri authority = Uri.parse("content://com.example.feedreaderstub/FeedTable");
	private static final UriMatcher matcher;

	private static final String FEEDCONTENT_DB = "feedcontent.db";
	private static int DB_VERSION = 3; 
	
	public static final String FEED_ID = "_id";
	public static final String FEED_TITLE = "title";
	public static final String FEED_CONTENT = "content";
	public static final String FEED_LINK = "link";
	private static final String FEED_LINK_HASH = "link_hash";
	public static final String FEED_SUBJECT = "subject";
	public static final String FEED_DATE = "date";
	public static final String FEED_AUTHOR = "author";
	public static final String FEED_THUMBNAIL = "_data";
	
	private static final String TABLE_NAME = "FeedTable";
	
	private static final int SINGLE_ROW = 1;
	private static final int ALL_ROWS = 2;
	
	static
	{
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(authority.getAuthority(), TABLE_NAME, ALL_ROWS);
		matcher.addURI(authority.getAuthority(), TABLE_NAME + "/#", SINGLE_ROW);
	}
	
	private static final class DbHelper extends SQLiteOpenHelper
	{
		private static final String CREATE_FEED_STMT = "create table " + TABLE_NAME + " (" +
				FEED_ID + " integer primary key autoincrement, " +
				FEED_TITLE + " text not null, " +
				FEED_CONTENT + " text not null, " +
				FEED_LINK + " text not null, " +
				FEED_DATE + " text, " +
				FEED_AUTHOR + " text, " +
				FEED_SUBJECT + " text, " +
				FEED_LINK_HASH + " integer not null, " +
				FEED_THUMBNAIL + " text)";
		private static final String DROP_FEED_STMT = "drop table if exists " + TABLE_NAME;
		
		public DbHelper(Context context, String name, CursorFactory factory, int version) 
		{
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			Log.d(TAG, "DbHelper: creating table " + CREATE_FEED_STMT);
			db.execSQL(CREATE_FEED_STMT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Log.d(TAG, "DbHelper: upgrading table " + DROP_FEED_STMT);
			db.execSQL(DROP_FEED_STMT);
			onCreate(db);
		}
	}
	
	private DbHelper dbHelper;

	@Override
	public boolean onCreate() 
	{
		dbHelper = new DbHelper(getContext(), FEEDCONTENT_DB, null, DB_VERSION);
		return true;
	}

	@Override
	public String getType(Uri uri) 
	{
		String result = null;
		switch(matcher.match(uri))
		{
		case SINGLE_ROW:
			result = "vnd.android.cursor.item/vnd.feedreaderstub.item";
			break;
		case ALL_ROWS:
			result = "vnd.android.cursor.dir/vnd.feedreaderstub.dir";
			break;
		}
		
		Log.d(TAG, "getType result " + result);
		
		return result;
	}

	private boolean hasHashLink(Uri uri, int hashLink)
	{
		boolean aResult = false;
		String whereClause = FEED_LINK_HASH + " = \'" + hashLink + "\'";
		Cursor c = query(uri, new String[] {FEED_LINK_HASH}, whereClause, null, null);
		if (c.getCount() > 0)
		{
			aResult = true;
		}
		return aResult;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Uri retVal = null;
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		{
			String link = values.getAsString(FEED_LINK);
			Log.d(TAG, "insert link : " + link);
			Uri linkUri = Uri.parse(link);
			Log.d(TAG, "insert linkUri : " + linkUri.toString());
			int linkHash = linkUri.hashCode();
			Log.d(TAG, "insert linkHash : " + linkHash);
			values.put(FEED_LINK_HASH, linkHash);
			
			if (hasHashLink(uri, linkHash))
			{
				Log.d(TAG, "insert linkHash " + linkHash + " - skip insert");
				return retVal;
			}
		}
		
		long rowId = db.insert(TABLE_NAME, null, values);
		
		if (rowId > -1)
		{
			retVal = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(retVal, null);
			
		}
		
		Log.d(TAG, "insert rowId : " + rowId + " retVal " + uri.toString());
		
		return retVal;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		
		switch(matcher.match(uri))
		{
		case SINGLE_ROW:
			String rowId = uri.getPathSegments().get(1);
			selection = FEED_ID + " = " +  rowId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
			break;
		}
		
		if (selection == null)
		{
			selection = "1";
		}
		
		Log.d(TAG, "update selection : " + selection);
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int retVal = db.update(TABLE_NAME, values, selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		Log.d(TAG, "update retVal : " + retVal);
		
		return retVal;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		switch(matcher.match(uri))
		{
		case SINGLE_ROW:
			String rowId = uri.getPathSegments().get(1);
			selection = FEED_ID + " = " + rowId + (TextUtils.isEmpty(selection) ? "" :  (" AND (" + selection + ")"));
			break;
		}
		
		Log.d(TAG, "delete selection : " + selection);
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int retVal = db.delete(TABLE_NAME, selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		Log.d(TAG, "delete retVal : " + retVal); 
		return retVal;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		
		sqb.setTables(TABLE_NAME);
		{
			for (String s : uri.getPathSegments()) 
			{
				Log.d(TAG, "PathSegments: " + s);
			}
		}
		
		switch(matcher.match(uri))
		{
		case SINGLE_ROW:
			String rowId = uri.getPathSegments().get(1);
			sqb.appendWhere(FEED_ID + " = " + rowId);
			break;
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String groupBy = null;
		String having = null;
		
		Log.d(TAG, "SQLiteQueryBuilder query: " + sqb.buildQuery(projection, selection, groupBy, having, sortOrder, null));
		
		Cursor c = sqb.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		Log.d(TAG, "query count : " + c.getCount());
		
		return c;
	}
	
	private String getSubjectname(Uri uri, String rowId)
	{
		String aResult = "unknown";
		String whereClause = null;//FEED_ID + " = \'" + rowId + "\'";
		Cursor c = query(uri, new String[] {FEED_SUBJECT}, whereClause, null, null);
		
		if (c.getCount() > 0)
		{
			c.moveToFirst();
			Log.d(TAG, "Got result " + c.getColumnIndex(FEED_SUBJECT));
			aResult = c.getString(c.getColumnIndex(FEED_SUBJECT));
			Log.d(TAG, "Got result " + aResult);
		}
		return aResult;
	}	
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
	{
		String rowId = uri.getPathSegments().get(1);
		String fName = getSubjectname(uri, rowId);
		File f = new File(
				//getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
				getContext().getCacheDir(), fName);
		
		if (!f.exists())
		{
			try {
				f.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "File creation failed", e);
				e.printStackTrace();
			}
		}
		
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
 
		return ParcelFileDescriptor.open(f, fileMode);
	}
}
