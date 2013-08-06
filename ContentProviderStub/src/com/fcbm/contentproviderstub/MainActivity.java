package com.fcbm.contentproviderstub;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

// This project is inspired to code from: PAD4, ProA4, LoaderThrottle.java

public class MainActivity extends FragmentActivity { // We extend FragmentActivity to have getSupportLoaderManager
													 // This is the base class for activities that want to use the support-based Fragment and Loader APIs
	private static final String TAG = "MainActivity";
	private static final String KEY_PROJECTION = "projection";
	private ItemSimpleCursorAdapter mSca = null;
	private int mIdLoaderTitles = 0;
	private AsyncTask<Void, Void, Void> mPopulateTask = null;
	
	LoaderManager.LoaderCallbacks<Cursor> mLoaderCb = new LoaderManager.LoaderCallbacks<Cursor>() {
		// --- Loader<D> ---
		// are available to any Activity or Fragment throug the LoaderManager
		// They are designed to asynchronously load data and monitor the underlying 
		// data source for changes. 
		
		// --- CursorLoader ---
		// is an indirect subclass of Loader<Cursor>, and are designed to perform
		// asynchronous query to ContentProviders returning a result Cursor and notifications
		// of any updates on the underlying provider
		
		// --- LoaderManager.LoaderCallbacks<Cursor> ---
		// Callback interface for a client to interact with the LoaderManager.

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) 
		{
			// This method is called when the Loader is initialized
			// Instantiate and return a new Loader for the given ID.
			
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			String aIncomingProjection[] = null;
			String projection[] = null; //new String[]{
					//TestContentProvider.MoviesTable.COL_TITLE,
					//TestContentProvider.MoviesTable.COL_DIRECTOR,
					//TestContentProvider.MoviesTable._ID, 	// Note that _id *must* be present in the projection in order to work with CursorLoader
					//};

			if (args != null && args.containsKey(KEY_PROJECTION))
			{
				aIncomingProjection = args.getStringArray(KEY_PROJECTION);
				projection = new String[aIncomingProjection.length + 1];
			}
			else
			{
				projection = new String[1];
			}
			if (aIncomingProjection != null)
			{
				for(int i = 0; i < aIncomingProjection.length; i++)
				{
					projection[i] = aIncomingProjection[i];
				}
			}
			projection[ projection.length - 1] = TestContentProvider.MoviesTable._ID;
			
			CursorLoader cl = new CursorLoader(
					MainActivity.this, 
					TestContentProvider.MoviesTable.CONTENT_URI_MOVIES, 
					projection, 
					selection, 
					selectionArgs, 
					sortOrder);
			
			// update at most every 2 seconds
			// This method is inherited from AsyncTaskLoader<D>
			cl.setUpdateThrottle(2000); 
			
			return cl;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// This method is called when the LoaderManager completes the asynchronous query
			// Note: "loader" parameter is the object we have created above
			//       "cursor" contains the result of the query
			
			// Note: this call is not synchronized with the UI thread
			mSca.swapCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// This method is called when the LoaderManager resets the CursorLoader			
			// Note: this call is not synchronized with the UI thread
			mSca.swapCursor(null);
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		String projection[] = new String[]{
				TestContentProvider.MoviesTable.COL_TITLE,
				TestContentProvider.MoviesTable.COL_DIRECTOR};
        
        
        //mSca = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
        //		null, projection, new int[] { android.R.id.text1}, 0);
		int aFlags = 0;
		Cursor aCursor = null;
		mSca = new ItemSimpleCursorAdapter(this, 
				R.layout.row, 
				aCursor, 
				projection, 
				new int[] { R.id.txtMovieTitle, R.id.txtMovieTitle}, 
				aFlags);
        
        ListView lv = (ListView) findViewById( R.id.listView );
        lv.setAdapter(mSca);
        
        registerForContextMenu(lv);
        
        Bundle aLoaderArgs = new Bundle();
        aLoaderArgs.putStringArray(KEY_PROJECTION, projection);
        android.support.v4.app.LoaderManager lm = getSupportLoaderManager();
        lm.initLoader( mIdLoaderTitles, aLoaderArgs, mLoaderCb);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
    	if (menuInfo instanceof AdapterContextMenuInfo)
    	{
    		Log.d(TAG, "AdapterContectMenuInfo");
    	}
    	else if (menuInfo instanceof ExpandableListContextMenuInfo)
    	{
    		Log.d(TAG, "ExpandableListContextMenuInfo");
    	}
    	else
    	{
    		Log.d(TAG, "ClassName " + menuInfo.getClass().getCanonicalName());
    	}
    	if (v.getId() == R.id.listView)
    	{
    		Log.d(TAG, "selectedListView");
    		AdapterContextMenuInfo aCmi = (AdapterContextMenuInfo) menuInfo;
    		Log.d(TAG, "position " + aCmi.position);
    		Cursor c = (Cursor)mSca.getItem( aCmi.position );
    		String title = c.getString( c.getColumnIndex( TestContentProvider.MoviesTable.COL_TITLE));
    		Log.d(TAG, "Director: " + title);
    		
    		menu.setHeaderTitle( title );
    		menu.add(Menu.NONE, 0, 0, "Show");
    		menu.add(Menu.NONE, 1, 1, "Update");
    		menu.add(Menu.NONE, 2, 2, "Delete");
    	}
    	else if (v.getId() == R.id.imgMovieBanner)
    	{
    		Log.d(TAG, "selectedImageView");
    	}
    	else if (v.getId() == R.id.txtMovieTitle)
    	{
    		Log.d(TAG, "selectedTitle");
    	}
    	else if (v.getId() == R.id.txtMovieDirector)
    	{
    		Log.d(TAG, "selectedDirector");
    	}
    	else
    	{
    		Log.d(TAG, "selected id " + v.getId());
    	}
    	
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	if (item.getItemId() == 0)
    	{
    		AdapterContextMenuInfo aCmi = (AdapterContextMenuInfo) item.getMenuInfo();
    		Cursor c = (Cursor)mSca.getItem( aCmi.position );
    		String title = c.getString( c.getColumnIndex( TestContentProvider.MoviesTable.COL_TITLE));    		

    		Toast.makeText(this , "Show Item " + title, Toast.LENGTH_LONG).show();
    	}
    	else if (item.getItemId() == 1)
    	{
    		Toast.makeText(this , "Update Item", Toast.LENGTH_LONG).show();
    	}
    	else if (item.getItemId() == 2)
    	{
    		Toast.makeText(this , "Delete Item", Toast.LENGTH_LONG).show();

    		AdapterContextMenuInfo aCmi = (AdapterContextMenuInfo) item.getMenuInfo();
    		Cursor c = (Cursor)mSca.getItem( aCmi.position );
    		String movieId = c.getString( c.getColumnIndex( TestContentProvider.MoviesTable._ID));    		

    		doDeleteItem(movieId);
    	}
    	else
    	{
    		return super.onContextItemSelected(item);	
    	}

    	return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case R.id.action_clear_db:
    			doClearDb();
    			return true;
    		case R.id.action_populate_db:
    			doPopulateDb();
    			return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    private void doPopulateDb()
    {
    	if (mPopulateTask != null)
    	{
    		mPopulateTask.cancel(false);
    	}
    	mPopulateTask = new AsyncTask<Void, Void, Void> ()
    	{
			@Override
			protected Void doInBackground(Void... params) {
				
				for (char c = 'Z' ; c>= 'A'; c--)
				{
					if (isCancelled())
					{
						Log.i(TAG, "AsyncTask interrupted");
						break;
					}
					String aTitle = "Movie " + c;
					String aDirector = "Director " + c;
					
					if (movieExists(aTitle, aDirector))
						continue;
					
					ContentValues cv = new ContentValues();
					cv.put(TestContentProvider.MoviesTable.COL_TITLE, aTitle);
					cv.put(TestContentProvider.MoviesTable.COL_DIRECTOR, aDirector);
					
					getContentResolver().insert(TestContentProvider.MoviesTable.CONTENT_URI_MOVIES, cv);
					
                    // Wait a bit between each insert
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) { }
				}
				
				return null;
			}
    	}.execute();
    }

    // TODO: pass this parameter in AsyncTask.execute()
    private void doDeleteItem(final String movieId)
    {
    	new AsyncTask<Void, Void, Void> ()
    	{
    		@Override
    		protected Void doInBackground(Void... params)
    		{
    			String where = TestContentProvider.MoviesTable._ID + "=?";
    			String selectionArgs[] = new String[] { movieId};
    			getContentResolver().delete(TestContentProvider.MoviesTable.CONTENT_URI_MOVIES, where, selectionArgs);
    			return null;
    		}
    	}.execute();
    }
    
    private void doClearDb()
    {
    	if (mPopulateTask != null)
    	{
    		mPopulateTask.cancel(false);
    		mPopulateTask = null;
    	}
    	new AsyncTask<Void, Void, Void> ()
    	{
			@Override
			protected Void doInBackground(Void... params) {
				String where = null;
				String selectionArgs[] = null;
				getContentResolver().delete(TestContentProvider.MoviesTable.CONTENT_URI_MOVIES, where, selectionArgs);
				return null;
			}
    	}.execute();
    }
    
    private boolean movieExists(String title, String director)
    {
    	String sortOrder = null;
    	String selection = TestContentProvider.MoviesTable.COL_TITLE + "=? AND " + 
    			TestContentProvider.MoviesTable.COL_DIRECTOR + "=?";
    	String selectionArgs[] = new String[] {title, director};
    	Cursor c = null;
    	boolean aBookFound = false;
    	
    	try
    	{
    		c = getContentResolver().query(
    			TestContentProvider.MoviesTable.CONTENT_URI_MOVIES, 
    			new String[] {TestContentProvider.MoviesTable._ID}, 
    			selection, selectionArgs, sortOrder);
        } finally {
            if (c != null)
            {
            	aBookFound = c.getCount() > 0;
            	Log.d(TAG, "Movie " + title + " found: " + aBookFound);
            	c.close();
            }
        }

    	return aBookFound;
    }
    
    
}
