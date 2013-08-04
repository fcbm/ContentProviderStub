package com.example.feedreaderstub;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;


public class ItemCursorAdapter extends CursorAdapter {

	public ItemCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, c, true);


	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		return null;
	}

}
