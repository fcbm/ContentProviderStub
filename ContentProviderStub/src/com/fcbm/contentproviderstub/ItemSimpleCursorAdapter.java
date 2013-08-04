package com.fcbm.contentproviderstub;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemSimpleCursorAdapter extends SimpleCursorAdapter{

	private static final String TAG = "ItemSimpleCursorAdapter";
	private final Context mCtx;
	
	public ItemSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mCtx = context;
	}

	@Override
	public View getView(int position, View currentView, ViewGroup parent)
	{
		RelativeLayout newView = null;

		if (currentView == null)
		{
			// Create the new layout to insert in the item
			newView = new RelativeLayout( mCtx );
			
			// Get the LayoutInflater, and inflate the layout
			LayoutInflater li = (LayoutInflater) mCtx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			li.inflate(R.layout.row, newView, true);
		}
		else
		{
			// Sometimes we may receive a not-null View to be recycled
			newView = (RelativeLayout) currentView;
		}
		
		Cursor c = (Cursor) getItem(position);
		//ImageView ivBanner = (ImageView)newView.findViewById(R.id.imgMovieBanner);
		TextView tvTitle = (TextView) newView.findViewById(R.id.txtMovieTitle);
		TextView tvDirector = (TextView) newView.findViewById(R.id.txtMovieDirector);
		
		tvTitle.setText(c.getString(c.getColumnIndex(TestContentProvider.MoviesTable.COL_TITLE)));
		tvDirector.setText(c.getString(c.getColumnIndex(TestContentProvider.MoviesTable.COL_DIRECTOR)));
		
		/*
		try {
			InputStream is = mCtx.getContentResolver().openInputStream(
					Uri.parse( TestContentProvider.MoviesTable.CONTENT_URI_MOVIES.toString() + "/" + 
					c.getString( c.getColumnIndex(TestContentProvider.MoviesTable._ID)) ));
			Bitmap bmp = BitmapFactory.decodeStream(is);
			ivBanner.setImageBitmap(bmp);
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		*/
		
		return newView;
	}


}
