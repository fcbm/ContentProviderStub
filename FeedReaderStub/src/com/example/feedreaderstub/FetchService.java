package com.example.feedreaderstub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class FetchService extends IntentService{

	private static long instanceCounter = 1;
	private static long intentCounter = 1;
	private static final String TAG = "FetchService";
	

	public FetchService() {
		super(TAG + " " + instanceCounter);
		instanceCounter++;
	}
	
	public FetchService(String name) {
		super(TAG + " " + instanceCounter);
		instanceCounter++;
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		Log.d(TAG, "instance " + instanceCounter + " intent " + intentCounter);
		intentCounter++;
		
		List<ValueElement> values = null;
		
		try {
			values = fetchData();
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException", e);
		} catch (XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		
		if (values == null)
		{
			Log.d(TAG, "Nothing to add: empty values");
			return;
		}
		
		try {
			updateDataBase(values);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException", e);
		}
		
	}

	private static final class ValueElement
	{
		ContentValues cv;
		Bitmap bmp;
	}
	
	private List<ValueElement> fetchData() throws IOException, XmlPullParserException
	{
		URL feedUrl = new URL(
				//"http://feeds.feedburner.com/Link2universe"
				"http://rss.slashdot.org/Slashdot/slashdot"
				);
		
		Log.d(TAG, "fetchData: start");
		
		URLConnection connection = feedUrl.openConnection();
		InputStream is = connection.getInputStream();
		Log.d(TAG, "fetchData: connected");
		
		XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
		xppf.setNamespaceAware(false);
		XmlPullParser xpp = xppf.newPullParser();
		
		Log.d(TAG, "fetchData: xml parser init");
		
		xpp.setInput(is, "UTF_8");

		boolean insideItem = false;
		
		List<ValueElement> contentValues = new ArrayList<ValueElement>();
		
		Log.d(TAG, "fetchData: start xml parsing");
		
		ValueElement currentContentValues = null;;
		for (int eventType = xpp.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next())
		{
			
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equalsIgnoreCase("item"))
				{
					currentContentValues = new ValueElement();
					currentContentValues.cv = new ContentValues();
					Log.d(TAG, "NewItem : " );
					insideItem = true;
				}
				else if (xpp.getName().equalsIgnoreCase("title") && insideItem)
				{
					String title = xpp.nextText();
					Log.d(TAG, "\t title : " + title);
					currentContentValues.cv.put(FeedProvider.FEED_TITLE, title);
				}
				else if (xpp.getName().equalsIgnoreCase("link") && insideItem)
				{
					String link = xpp.nextText();
					Log.d(TAG, "\t link : " + link);
					currentContentValues.cv.put(FeedProvider.FEED_LINK, link);
				}
				else if (xpp.getName().equalsIgnoreCase("description") && insideItem)
				{
					String description = xpp.nextText();
					Log.d(TAG, "\t description : " + description);
					currentContentValues.cv.put(FeedProvider.FEED_CONTENT, description);
				}
				else if (xpp.getName().equalsIgnoreCase("dc:subject") && insideItem)
				{
					String subject = xpp.nextText();
					Log.d(TAG, "\t subject : " + subject);
					currentContentValues.cv.put(FeedProvider.FEED_SUBJECT, subject);
					currentContentValues.bmp = fetchBitmap(subject);
				}
			}
			else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
			{
				if (currentContentValues != null)
				{
					contentValues.add(currentContentValues);
					Log.d(TAG, "--------------------" );
				}
				insideItem = false;
			}
		}
		return contentValues;
	}

	public Bitmap fetchBitmap(String description) throws MalformedURLException
	{
		URL url = new URL("http://a.fsdn.com/sd/topics/"+description+"_64.png");

		Bitmap bmp = null;
		try {
			URLConnection connection = url.openConnection();
			bmp = BitmapFactory.decodeStream(connection.getInputStream());
		} catch (IOException e) {
		}
		
		return bmp;
		
	}
	
	private void updateDataBase(List<ValueElement> values) throws FileNotFoundException
	{
		ContentResolver cr = getContentResolver();
		
		
		for(ValueElement contentValues : values) {
			
			Uri aNewUri = cr.insert(FeedProvider.authority, contentValues.cv);
			if (aNewUri != null && contentValues.bmp != null)
			{
				Log.d(TAG, "updateDataBase aNewUri is " + aNewUri);
				OutputStream os = cr.openOutputStream(aNewUri);
				contentValues.bmp.compress(Bitmap.CompressFormat.PNG, 80, os);
			}
		}
		 
		Log.d(TAG, "updateDataBase result");
	}
}
