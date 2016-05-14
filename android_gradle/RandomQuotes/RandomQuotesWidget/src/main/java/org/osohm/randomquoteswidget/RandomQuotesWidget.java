package org.osohm.randomquoteswidget;

import android.app.PendingIntent;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

// import our shared common library classes.
import org.osohm.randomquoteslib.PreferencesStorage;
import org.osohm.randomquoteslib.FilesProcessor;

/***********************************************************************
 * Random Quotes Widget.
 * Simply takes a text file(s) or multiple text files and displays them 
 * in random order in a widget view.
 * @author Camilo Tejeiro ,=,e for Osohm
 **********************************************************************/
public class RandomQuotesWidget extends AppWidgetProvider
{    
    private static final String LOG_TAG = RandomQuotesWidget.class.getName();
    
    // called for periodic or onTap updates.
    @Override 
    public void onUpdate (Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {    
        // when "creating": Call super onUpdate first (prevents nullPointers).
        super.onUpdate(context, appWidgetManager, appWidgetIds);
            
        // push updates to every applicable widget instance.
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            Log.i(LOG_TAG, "onUpdate, widgetId: " + appWidgetIds[i]);
            
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

    // called when a widget instance is removed from the screen.
    @Override
    public void onDeleted (Context context, int[] appWidgetIds)
    {        
        // clean up all resources for every applicable widget instance.
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            Log.i(LOG_TAG, "onDeleted, widgetId: " + appWidgetIds[i]);
            
            // Create our stored preferences object
            PreferencesStorage storedPreferences = new PreferencesStorage(context, appWidgetIds[i]);
            
            storedPreferences.deleteAllPreferences();
        }
        
        // When "destroying" call super onDeleted last (prevents nullPointers).
        super.onDeleted(context, appWidgetIds);
    }
    
    // called when we have no more remaining widget instances.
    @Override
    public void onDisabled (Context context)
    {        
        Log.i(LOG_TAG, "onDisabled: Deleting all user data, everything");
        
        // here we want to remove every single bit of data we created, 
        // basically get the widget app back to install state.
        ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE))
            .clearApplicationUserData();
            
        // When "destroying" call super onDisabled last (prevents nullPointers).
        super.onDisabled(context);
            
    }
        

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
        PreferencesStorage storedPreferences = new PreferencesStorage(context, appWidgetId);
        
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
        // do not pass a single int pass a one item array, otherwise 
        // the onUpdate callback will not get called!
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});

        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_update_textview, updatePendingIntent);
        
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}
