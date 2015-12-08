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
    
    @Override 
    public void onUpdate (Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.i(LOG_TAG, "onUpdate: " + appWidgetIds.length);
        
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            Log.d(LOG_TAG, "widgetId: " + appWidgetIds[i]);
            
            String[] currentQuote = getCurrentQuote(context, appWidgetIds[i]);

            // check to see if we've an underlying preferences file.
            if (currentQuote == null)
            {
                Log.d(LOG_TAG, "No current data preferences found");
                
                // build our widget text.
                String[] loading_message = {"0", "0", "No user/data preferences found", "no text file(s)", "0"};
                // update the app with our widget information.
                updateAppWidget(context, appWidgetManager, appWidgetIds[i], loading_message);
            }
            else
            {     
                Log.d(LOG_TAG, "Found prior stored user/data preferences");
                       
                // update the app with our current quote.
                updateAppWidget(context, appWidgetManager, appWidgetIds[i], currentQuote); 
            }  
            
        }
    }
        
    /*
     * We are overriding onReceive because we need to access the actual 
     * intent to get the passed Ids, the default filters don't allow us 
     * to get the intent, so we have to do it manually.  
     */
    /* @Override
    public void onReceive(Context context, Intent intent)
    {

        super.onReceive(context, intent);
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
        // intent has no extras or invalid ID. (e.g. the first time widget_file_name_textview 
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
                
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                
                // widget_file_name_textview the widget with our current quote.
                updateAppWidget(context, appWidgetManager, myAppWidgetId, currentDataArray); 
            }
            else if(intentAction == AppWidgetManager.ACTION_APPWIDGET_DELETED)
            {
                // make sure we delete our preferences files for this instance.
                storedPreferences.deleteAllPreferences();
            }
            else
            {
                Log.e(LOG_TAG, "Unsupported action, check your action filters");
            }
        }
        
    }*/

    /**
     * Get Current Quote
     * Use our filesProcessor to read, validate and get our current paragraph 
     * from storage. 
     * @param context The current application context.
     * @param appWidgetId The id of the current widget instance.
     * @return String[] Our current quote and quote extra info (file, line num ...etc)
     **/ 
    private String[] getCurrentQuote(Context context, int appWidgetId)
    {
        Log.i(LOG_TAG, "getCurrentQuote"); 
        
        // Create our stored preferences object
        PreferencesStorage storedPreferences = new PreferencesStorage(appWidgetId, context);
        
        // get our current data if available.
        FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
        String [] currentParagraph = filesProcessor.getCurrentParagraph();
        
        return currentParagraph;
    }

    /**
     * Update App Widget
     * Method called by elements wishing to update our widget current view.
     * @param context The specific context we use to grab a handle of the remoteViews.
     * @param appWidgetManager To post changes to our widget.
     * @param appWidgetId The widget id, that identifies the instance we want to update
     * @param currentWidgetData Array with data to display in the widget view.
     **/ 
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, 
        int appWidgetId,  String[] currentWidgetData) 
    {
        Log.i(LOG_TAG, "updateAppWidget, appWidgetId=" + appWidgetId 
            + " widgetTextElements = [" + currentWidgetData[0] + ", " 
            + currentWidgetData[1] + ", " + currentWidgetData[2] + ", " 
            + currentWidgetData[3] + ", " + currentWidgetData[4] + "]");
        
        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the widget_layout from our package).
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // set quotes queue: index/lenght
        remoteViews.setTextViewText(R.id.widget_queue_number_textview, "queue number: " 
            + currentWidgetData[0] + "/" + currentWidgetData[1]);
        // set quote paragraph.
        remoteViews.setTextViewText(R.id.widget_update_textview, currentWidgetData[2]);
        // set file name (remove long path for display)
        String fileName = currentWidgetData[3].substring(currentWidgetData[3].lastIndexOf("/")+1);
        remoteViews.setTextViewText(R.id.widget_file_name_textview, fileName);
        // set line number
        remoteViews.setTextViewText(R.id.widget_line_number_textview, "line number: " + currentWidgetData[4]);
        
        // update widget everytime the user taps.
        Intent updateIntent = new Intent(context, RandomQuotesWidget.class);
        
        // set our intent action such that we cen trigger the onUpdate callback.
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        
        // we need to specify and pass an array of IDs, even if we are only passing one. 
        // i.e. DO NOT use EXTRA_APPWIDGET_ID use EXTRA_APPWIDGET_IDS and 
        // do not pass a single int pass a one item arraym, otherwise 
        // the onUpdate callback will not get called!
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});

        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_update_textview, updatePendingIntent);
        
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        
    }
}
