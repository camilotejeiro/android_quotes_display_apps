package com.cat.textnoteswidget;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*************************************************************************
 * Widget Configuration
 * This class simply displays a configure screen 
 * where the user can input the text file he wants the widget to read from.
 * @author	C.A.T	,=,e
 *************************************************************************/
public class WidgetConfiguration extends Activity 
{
	private static final String LOG_TAG = WidgetConfiguration.class.getName();
	
	// The file stored in memory to keep the selected user preferences.
	public static final String BOOK_WIDGET_CONF_FILE = "Book_Widget_conf";

	// view objects.
	private EditText inputEditText;
	private TextView configMessageText;
		
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Log.i(LOG_TAG, "Configuration Screen loaded");
		
		// reset the configuration preferences as soon as we start.
		updateConfigurationStatusPrefs(false);
		updateFilePathPrefs(null);
		
		// now we will render the UI for the configure Activity.
		setContentView(R.layout.configure_layout);	

		// we will have a file name input that can be configured in the widget.
		inputEditText = (EditText) findViewById(R.id.file_name);
		
		// Where we will display messages regarding configuration details.
		configMessageText = (TextView) findViewById(R.id.message_textView);
		
		// now we will get the ok button view and put a click listener to it.
		Button ok = (Button) findViewById(R.id.ok_button);
		ok.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
                // get the text from the text input view
                String filePath = inputEditText.getText().toString();

                Log.i(LOG_TAG, "User subdirectory: " + filePath);

                // now pass the string path to create the file.
                File desiredPath = new File (filePath);

                Log.i(LOG_TAG, "Complete Path: " + desiredPath);
				
				// now we want to do a very quick check to make sure the file exists.
				if (desiredPath.exists() == false)
				{
					Log.i(LOG_TAG, "desired path does not exist. ");
					configMessageText.setText("Desired path or file does not exist, please correct or press back to exit");
					return;
				}
				
				// get only the filename with no preceding path.
				String desiredFile = desiredPath.getName();
				
				Log.i(LOG_TAG, "desired File: " + desiredFile);
				
				// check to make sure the filetype is .txt
				if ((desiredFile.substring((desiredFile.lastIndexOf(".") + 1), desiredFile.length())).equals("txt") == false)
				{
					Log.i(LOG_TAG, "desired file is not a text file.");
					configMessageText.setText("Desired file is not a .txt file, please correct and reinstall the widget");
					return;
				}
				
				// The configuration is set and checked for correctness, store the preferences in memory.
				updateFilePathPrefs(filePath);
				updateConfigurationStatusPrefs(true);
				
				Log.i(LOG_TAG, "Configuration complete");
				
				// end the activity.
				finish();
			}
		});
	}
	
    /*********************************************************************
     * Update Configuration Status Preferences
     * This private method is used to store the configuration status 
     * settings in memory.
     * @param	configurationStatus	the value to store in preferences.
     **********************************************************************/
	private void updateConfigurationStatusPrefs(boolean configurationStatus)
	{
		// routine to modify value by key in the widget configuration file.
		SharedPreferences widgetConfiguration = getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
		configurationEditor.putBoolean("configurationStatus", configurationStatus);

		// Commit the edits!
		configurationEditor.commit();
	}
	
    /******************************************************************************
     * Update File Path Preferences
     * This private method is used to store the file path user settings in memory.
     * @param	filePath	The filePath to use to open the text file.
     ******************************************************************************/
	private void updateFilePathPrefs(String filePath)
	{
		// routine to modify value by key in the widget configuration file.
		SharedPreferences widgetConfiguration = getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
		configurationEditor.putString("filePath", filePath);

		// Commit the edits!
		configurationEditor.commit();
	}
	
    /*********************************************************************
     * Get Configuration Status Preferences
     * @param	context	the context passed by the widget to have 
     * 					access to preferences.
     * @return 	boolean the current status of the widget configuration 
     * 					from the preferences file.
     * 					false: not configured
     * 					true: configuration complete
     **********************************************************************/
	public static boolean getConfigurationStatusPrefs(Context context)
	{
		// routine to get value by key in the widget configuration file
		SharedPreferences widgetConfiguration = context.getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		return widgetConfiguration.getBoolean("configurationStatus", false);
	}
	
    /*********************************************************************
     * Reset Configuration Status Preferences
     * Simply resets the configuration status flag in the configuration file.
     * @param	context, the current context passed by the widget. 
     **********************************************************************/
	public static void resetConfigurationStatusPrefs(Context context)
	{

		// routine to modify value by key in the widget configuration file.
		SharedPreferences widgetConfiguration = context.getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
		configurationEditor.putBoolean("configurationStatus", false);

		// Commit the edits!
		configurationEditor.commit();
	}
	
    /************************************************************
     * Get File Path Preferences.
     * @param	context	the current context passed by the widget
     * 					to have access to preferences.
     * @return	String	The path to the text file to read from, 
     * 					fetched from preferences
     ******************************************************************/
	public static String getFilePathPrefs(Context context)
	{
		// routine to get value by key in the widget configuration file
		SharedPreferences widgetConfiguration = context.getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		return widgetConfiguration.getString("filePath", null);
	}
	
    /*********************************************************************
     * Reset File Path Preferences
     * Simply resets the stored file path in the configuration file preferences.
     * @param	context	the current context passed by the widget.
     **********************************************************************/
	public static void resetFilePathPrefs(Context context)
	{
		
		// routine to modify value by key in the widget configuration file.
		SharedPreferences widgetConfiguration = context.getSharedPreferences(BOOK_WIDGET_CONF_FILE, 0);
		SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
		configurationEditor.putString("filePath", null);

		// Commit the edits!
		configurationEditor.commit();
	}
}
