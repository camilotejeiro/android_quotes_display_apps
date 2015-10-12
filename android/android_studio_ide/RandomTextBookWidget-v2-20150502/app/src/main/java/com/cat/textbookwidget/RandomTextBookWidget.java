package com.cat.textbookwidget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**************************************************************************************************
 * Random Text Widget.
 * This widget app is very simple:
 * First it displays a configuration activity screen to select the desired file to view.
 * Then it loads the configuration on the widget. The widget simply breaks the text file into 
 * paragraphs shuffles the paragraphs and displays them in random order, periodically (12 hours) 
 * or upon tapping the screen. 
 * @author	C.A.T	,=,e
 ***************************************************************************************************/
public class RandomTextBookWidget extends AppWidgetProvider
{	
	private static final String LOG_TAG = RandomTextBookWidget.class.getName();
	
	// message to display for user to update configuration
	public static final String WIDGET_LOAD_CONFIGURATION_MSG = "Tap widget to load existent configuration";
	
	// The paragraph number used as an index to iterate over the paragraph array
	private static int paragraphsArrayIndex;
	
	// status flag to wait until configuration activity is finished
	private static boolean configurationComplete = false;
	
	// array we will use for our paragraphs.
	private static String[] paragraphsArray;
	
	@Override
	public void onEnabled (Context context)
	{
		Log.i(LOG_TAG, "Widget Enabled");

		super.onEnabled(context);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		Log.i(LOG_TAG, "Widget Deleted");
		
		// reset the configuration preferences.
		WidgetConfiguration.resetFilePathPrefs(context);
		WidgetConfiguration.resetConfigurationStatusPrefs(context);
		
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	      int[] appWidgetIds) 
	{	
		Log.i(LOG_TAG, "onUpdate");
		
		// if configuration screen is still active or configuration was lost (e.g. due to force stopping the widget)
		if (configurationComplete == false )
		{
			
			// check to see if there is a pre-existent configuration stored in preferences
			if (WidgetConfiguration.getConfigurationStatusPrefs(context) == true)
			{
				// if we do have it we can proceed safely
				configurationComplete = true;
			}
			else
			{
				// we'll set up a PendingIntent to open the Configure screen if the widget is not configured.
				Intent configureIntent = new Intent(context, WidgetConfiguration.class);	
				configureIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity (configureIntent);
				
				// configuration pending, display message in widget to inform user.
				updateWidgetParagraph(context, appWidgetManager, appWidgetIds, WIDGET_LOAD_CONFIGURATION_MSG, 0, 0);
			}
		}
		
		if (configurationComplete == true )
		{
			// status is clear, configuration is complete, now we can start the widget logic.
			
			// if this is the first paragraph
			if (paragraphsArrayIndex == 0)
			{
				// LOAD all of the paragraphs into our array only once, no need to read every time
				Log.i(LOG_TAG, "Reading text file, loading all paragraphs for display");
				
				String filePath = WidgetConfiguration.getFilePathPrefs(context);
				
				// routine to read from the SD card text file.
				File fileToRead = new File (filePath);
				
				List<String> paragraphsList = new ArrayList<String>();
				
				// logic to read and break up file into paragraphs list.
				try 
				{
					FileReader textFileReader = new FileReader(fileToRead);
					BufferedReader bufferedTextFileReader = new BufferedReader(textFileReader);
					String currentLine = "";
					String currentParagraph = "";
					StringBuilder paragraphBuilder = new StringBuilder();
					
					// as long as there are lines to read.
					while ((currentLine = bufferedTextFileReader.readLine()) != null)
					{
						// if it is the end of the paragraph and there is a paragraph to add (i.e. no empty paragraph)
						if (currentLine.contentEquals("") == true)
						{
							currentParagraph = paragraphBuilder.toString();

							// add it to array of paragraph Strings.
							paragraphsList.add(currentParagraph);

							//clear the paragraphs list
							paragraphBuilder.setLength(0);
						}
						else
						{
							// just add another line to the paragraph
							paragraphBuilder.append(currentLine); 
						}
					}
					
					// free up our resources.
					bufferedTextFileReader.close();
					textFileReader.close();
					paragraphBuilder.setLength(0);
				}
				catch (IOException e)
				{
					Log.e(LOG_TAG, "Error message: " + e.getMessage());
					
			        e.printStackTrace();
			        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
				// now that we know the total size add all of the elements from the list to an array.
				paragraphsArray = new String[paragraphsList.size()];
				paragraphsArray = paragraphsList.toArray(paragraphsArray);
				
				Log.i(LOG_TAG, "size of resulting paragraphs string array: " + paragraphsArray.length);
				
				// lets shuffle our array of paragraphs.
				shuffleArray(paragraphsArray);
				
				Log.i(LOG_TAG, "Array Shuffled Succesfully");
				
			}
			
			// now we only need to display the current Quote in the array and subscribe for new taps.
			updateWidgetParagraph(context, appWidgetManager, appWidgetIds, paragraphsArray[paragraphsArrayIndex], paragraphsArrayIndex, paragraphsArray.length);
			
			// increment the widget paragraph number
			paragraphsArrayIndex++;
			
			// and make sure we loop if we reach the end, this will trigger a new file read.
			if (paragraphsArrayIndex == paragraphsArray.length)
				paragraphsArrayIndex = 0;
		}
	}
	
	/***********************************************************************************************
	 * Update Widget Paragraph.
	 * This function simply updates the views to display the new paragraph provided and
	 * sets up a pending intent to call an update again if the user taps the widget.
	 * @param	context			The current context of the widget application
	 * @param	appWidgetManager	
	 * @param	appWidgetIds 	IDs for all the instances of this widget that are currently running
	 * @param	newQuote		the actual string paragraph we want to display in the widget.
	 * @param 	currentIndex	the current index for the paragraph array
	 * @param 	arraySize		The total number of paragraphs loaded.
	 ************************************************************************************************/
	private void updateWidgetParagraph(Context context, AppWidgetManager appWidgetManager,
		      int[] appWidgetIds, String newQuote, int currentIndex, int arraySize)
	{
		
		// Get the name for the widget from the current passed context.
		ComponentName myTextWidget = new ComponentName(context, RandomTextBookWidget.class);
		
		// get the IDs for all of the widgets of the same kind.
		int [] myTextWidgetIds = appWidgetManager.getAppWidgetIds(myTextWidget);
		
		// iterate over all of the different instances of our widget.
		for (int currentTextWidgetId : myTextWidgetIds)
		{
			// get a remoteviews object to be able to post changes to the UI.
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			
			// update the text in the widget, make sure we never post empty strings as the widget will stop refreshing
			// and miss click updates
			if ( newQuote.trim().isEmpty() == true )
			{
				remoteViews.setTextViewText(R.id.update, "Empty parragraph, check text file, tap for next paragraph");
			}
			else
			{
				remoteViews.setTextViewText(R.id.update, newQuote );
			}
			
			remoteViews.setTextViewText(R.id.paragraph_number, currentIndex + "/" + arraySize);
			
			Log.i(LOG_TAG, "Register Widget for on click updates");
			// update widget everytime the user taps.
			Intent updateIntent = new Intent(context, RandomTextBookWidget.class);
			
			updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
	
			PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, updatePendingIntent);
			
			appWidgetManager.updateAppWidget(currentTextWidgetId, remoteViews);
		}
	}
	
	/**********************************************
	 * Shuffle Array
	 * Function to shuffle the elements in an array. 
	 * @param	stringArr
	 *********************************************/
	private void shuffleArray(String [] stringArr)
	{
		Random rnd = new Random();
	    for (int i = stringArr.length - 1; i > 0; i--)
	    {
	      int randomIndex = rnd.nextInt(i + 1);
	      
	      // Simple swap
	      String a = stringArr[randomIndex];
	      stringArr[randomIndex] = stringArr[i];
	      stringArr[i] = a;
	    }
	}
}
