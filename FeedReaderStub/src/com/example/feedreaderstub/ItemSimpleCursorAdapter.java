package com.example.feedreaderstub;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ItemSimpleCursorAdapter extends SimpleCursorAdapter{

	private final Context ctx;
	
	public ItemSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		ctx = context;
	}
	
	@Override
	public View getView(int position, View currentView, ViewGroup parent)
	{
		LinearLayout newView = null;
		if (currentView == null)
		{
			newView = new LinearLayout(ctx);
			
			LayoutInflater li = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			li.inflate(R.layout.row, newView, true);
			
		}
		else
		{
			newView = (LinearLayout) currentView;
		}
		
		Cursor c = (Cursor) getItem(position);
		
		ImageView iv = (ImageView)newView.findViewById(R.id.imgItemId);
		TextView tv = (TextView) newView.findViewById(R.id.txtItemTitle);
		
		tv.setText(c.getString(c.getColumnIndex(FeedProvider.FEED_TITLE)));
		
		
		try {
			InputStream is = ctx.getContentResolver().openInputStream(Uri.parse( FeedProvider.authority.toString() + "/" + 
				c.getString( c.getColumnIndex(FeedProvider.FEED_ID)) ));
			Bitmap bmp = BitmapFactory.decodeStream(is);
			iv.setImageBitmap(bmp);
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		
		
		return newView;
	}

}
