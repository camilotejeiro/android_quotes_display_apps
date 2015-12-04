package com.cat.randomquoteswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/***********************************************************************
 * Random Quotes Widget.
 * Simply takes a text file(s) or multiple text files and displays them 
 * in random order.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class RandomQuotesWidget extends AppWidgetProvider
{    
    private static final String LOG_TAG = RandomQuotesWidget.class.getName();
        
    private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    /*
     * We are overriding onReceive because we need to access the actual 
     * intent to get the passed Ids, the default filters don't allow us 
     * to get the intent, so we have to do it manually.  
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(LOG_TAG, "onReceive");
        
        // to allow only supported onReceive actions.
        boolean actionFilter; 
        
        // get our received action so we can switch.
        String intentAction = intent.getAction();
        
        Log.d(LOG_TAG, "Intent Action: " + intentAction); 
        
        // receive the intent and get the unique key.
        Bundle extras = intent.getExtras();
                
        // filter on our allowed activities.
        actionFilter = ((intentAction == AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    || (intentAction == AppWidgetManager.ACTION_APPWIDGET_DELETED)
                    || (intentAction == AppWidgetManager.ACTION_APPWIDGET_ENABLED));
        
        if (extras != null)
        {
            myAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // if our action is not applicable or we didn't get a valid widget ID, 
        // just return.
        // either these is not one of our supported actions or the receiver 
        // intent has no extras or invalid ID. (e.g. the first time update 
        // is called - it shouldn't be called while on config; This is a 
        // reported Android Bug).
        if ((actionFilter == false) || (myAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID))  
        {
                Log.i(LOG_TAG, "Not applicable onReceive action or invalid widget ID"); 
        }
        else
        {
            Log.d(LOG_TAG, "App Widget ID: " + myAppWidgetId);
            
            // Create our stored preferences object
            PreferencesStorage storedPreferences = new PreferencesStorage(myAppWidgetId, context);
            
            if (intentAction == AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            {

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                
                String[] currentDataArray = storedPreferences.getCurrentDataPreferences();

                // check to see if we've reached the end then we need to re-process our files.
                if (currentDataArray == null)
                {
                    // Create our fileProcessing object.
                    FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
                    
                    // process our text files, generate current data and store it.
                    filesProcessor.processTextFiles();
                    
                    // now, we can get new data.
                    currentDataArray = storedPreferences.getCurrentDataPreferences();

                }
                
                // update the widget with our current quote.
                updateAppWidget(myAppWidgetId, context, appWidgetManager, currentDataArray); 
            }
            else if(intentAction == AppWidgetManager.ACTION_APPWIDGET_DELETED)
            {
                // make sure we delete our preferences files for this instance.
                storedPreferences.deleteAllPreferences();
            }
            else if (intentAction == AppWidgetManager.ACTION_APPWIDGET_ENABLED)
            {
                // app widget enabled need to do something.
            }
            else
            {
                Log.e(LOG_TAG, "Unsupported action, check your action filters");
            }
        }
    }    

    public static void updateAppWidget(int appWidgetId, Context context, AppWidgetManager appWidgetManager,
        String[] currentWidgetData) 
    {
        Log.d(LOG_TAG, "updateAppWidget, appWidgetId=" + appWidgetId 
            + " widgetTextElements = [" + currentWidgetData[0] + ", " 
            + currentWidgetData[1] + ", " + currentWidgetData[2] + ", " 
            + currentWidgetData[3] + ", " + currentWidgetData[4] + "]");
        
        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // set quotes queue: index/lenght
        remoteViews.setTextViewText(R.id.quotes_index, "queue number: " 
            + currentWidgetData[0] + "/" + currentWidgetData[1]);
        // set quote paragraph.
        remoteViews.setTextViewText(R.id.update, currentWidgetData[2]);
        // set file name (remove long path for display)
        String fileName = currentWidgetData[3].substring(currentWidgetData[3].lastIndexOf("/")+1);
        remoteViews.setTextViewText(R.id.file_name, fileName);
        // set line number
        remoteViews.setTextViewText(R.id.line_number, "line number: " + currentWidgetData[4]);
        
        // update widget everytime the user taps.
        Intent updateIntent = new Intent(context, RandomQuotesWidget.class);
        
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.update, updatePendingIntent);
        
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        
    }
}
