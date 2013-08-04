package com.example.feedreaderstub;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {

	private static final String TAG = "ListActivity";
	private SimpleCursorAdapter ca = null;
	private final String[] projection = new String[] {FeedProvider.FEED_ID, FeedProvider.FEED_TITLE};
	
	// TODO: implement this as static
	private final LoaderManager.LoaderCallbacks<Cursor> loaderCb = new LoaderManager.LoaderCallbacks<Cursor>() 
	{
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) 
		{
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			Log.d(TAG, "LoaderCallbacks onCreateLoader");
			return new CursorLoader(MainActivity.this, FeedProvider.authority, projection, selection, selectionArgs, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) 
		{
			Log.d(TAG, "LoaderCallbacks onLoaderFinished");
			ca.swapCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) 
		{
			Log.d(TAG, "LoaderCallbacks onLoaderReset");
			ca.swapCursor(null);
		}
	};
	
	public void onClickUpdate(View v)
	{
		Log.d(TAG, "onClickUpdate startNewService");
		startService(new Intent(this, FetchService.class));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ca = new ItemSimpleCursorAdapter(this, 
				R.layout.row, 
				null, 
				projection, 
				new int[] {R.id.imgItemId, R.id.txtItemTitle}, 0);
		
		int loaderId = 101;
		Bundle bundleId = new Bundle();
		bundleId.putString("TESTKEY", "TESTVALUE");
		
		getLoaderManager().initLoader(loaderId, bundleId, loaderCb);
		
		setListAdapter(ca);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
