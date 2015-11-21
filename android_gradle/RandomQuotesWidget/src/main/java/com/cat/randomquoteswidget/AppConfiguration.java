package com.cat.randomquoteswidget;

import java.io.File;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/***********************************************************************
 * App Configuration
 * This class simply displays a configure screen 
 * where the user can input the text file he wants the widget to read 
 * from.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class AppConfiguration extends Activity
{
    private static final String LOG_TAG = AppConfiguration.class.getName();
    
    // The file stored in memory to keep the selected user preferences.
    public static final String QUOTES_WIDGET_CONF_FILE = "quotes_widget_config";

    // view objects.
    private EditText inputEditText;
    private TextView configMessageText;
    
    private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG, "Configuration Screen loaded");
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // reset the configuration preferences as soon as we start.
        updateConfigurationStatusPrefs(false);
        updateFilePathPrefs(null);
        updateWidgetID(myAppWidgetId);
        
        // now we will render the UI for the configure Activity.
        setContentView(R.layout.configure_layout);

        // we will have a file name input that can be configured in the widget.
        inputEditText = (EditText) findViewById(R.id.file_name);
        
        // Where we will display messages regarding configuration details.
        configMessageText = (TextView) findViewById(R.id.message_textView);

        // receive the intent and get the unique key.
        Intent receivedIntent = getIntent();
        Bundle extras = receivedIntent.getExtras();
        if (extras != null)
        {
            myAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                            AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (myAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 
        {
            finish();
        }
        
        // display our id for reference.
        configMessageText.setText("Widget Id:" + myAppWidgetId);
        
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
                
                // The configuration is set and checked for correctness, 
                // create a preferences file the widgetId sufix and 
                // store our configuration in memory.
                updateSharedPreferences(myAppWidgetId, filePath)
                
                Log.i(LOG_TAG, "Configuration complete");
                
                // end the activity.
                finish();
            }
        });
    }

    /**
     * updateSharedPreferences
     * This private method is used to create a new shared preferences with 
     * our widget id as a suffix and add the parsed filePath.
     * @param configurationStatus   the value to store in preferences.
     **/
    private void updateSharedPreferences(int myAppWidgetId, String filePath)
    {
        // create our dynamic file name.
        String fileName =  QUOTES_WIDGET_CONF_FILE + myAppWidgetId;
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = getSharedPreferences(fileName, 0);
        // SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
    }

    /**
     * Update Configuration Status Preferences
     * This private method is used to store the configuration status 
     * settings in memory.
     * @param configurationStatus   the value to store in preferences.
     **/
    private void updateConfigurationStatusPrefs(boolean configurationStatus)
    {
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putBoolean("configurationStatus", configurationStatus);

        // Commit the edits!
        configurationEditor.commit();
    }
    
    /**
     * Update File Path Preferences
     * This private method is used to store the file path user settings 
     * in memory.
     * @param filePath    The filePath to use to open the text file.
     **/
    private void updateFilePathPrefs(String filePath)
    {
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putString("filePath", filePath);

        // Commit the edits!
        configurationEditor.commit();
    }

    /**
     * Update Widget ID Preferences
     * This private method is used to store the specific widget ID in 
     * memory
     * @param widgetId    The widget id used to identify the current instance.
     **/
    private void updateWidgetIdPrefs(int widgetId)
    {
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putInt("widgetId", widgetId);

        // Commit the edits!
        configurationEditor.commit();
    }

    /**
     * Get Configuration Status Preferences
     * @param context   the context passed by the widget to have access 
     *                      to preferences.
     * @return boolean  the current status of the widget configuration 
     *                      from the preferences file.
     *                      false: not configured
     *                      true: configuration complete
     **/
    public static boolean getConfigurationStatusPrefs(Context context)
    {
        // routine to get value by key in the widget configuration file
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        return widgetConfiguration.getBoolean("configurationStatus", false);
    }
    
    /**
     * Get File Path Preferences.
     * @param context   the current context passed by the widget
     *                      to have access to preferences.
     * @return String   The path to the text file to read from, 
     *                      fetched from preferences
     **/
    public static String getFilePathPrefs(Context context)
    {
        // routine to get value by key in the widget configuration file
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        return widgetConfiguration.getString("filePath", null);
    }

    /**
     * Get Widget ID Preferences.
     * @param context   the current context passed by the widget
     *                      to have access to preferences.
     * @return int The widget ID that identifies this instance.
     **/
    public static int getWidgetIdPrefs(Context context)
    {
        // routine to get value by key in the widget configuration file
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        return widgetConfiguration.getInt("widgetId", null);
    }
    
    /**
     * Reset Configuration Status Preferences
     * Simply resets the configuration status flag in the configuration 
     * file.
     * @param context   the current context passed by the widget. 
     **/
    public static void resetConfigurationStatusPrefs(Context context)
    {

        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putBoolean("configurationStatus", false);

        // Commit the edits!
        configurationEditor.commit();
    }
    
    /**
     * Reset File Path Preferences
     * Simply resets the stored file path in the configuration file preferences.
     * @param context   the current context passed by the widget.
     **/
    public static void resetFilePathPrefs(Context context)
    {
        
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putString("filePath", null);

        // Commit the edits!
        configurationEditor.commit();
    }
    
    /**
     * Reset Widget ID Preferences
     * Simply resets the stored Widget Id in the configuration file preferences.
     * @param context   the current context passed by the widget.
     **/
    public static void resetWidgetIdPrefs(Context context)
    {
        
        // routine to modify value by key in the widget configuration file.
        SharedPreferences widgetConfiguration = context.getSharedPreferences(QUOTES_WIDGET_CONF_FILE, 0);
        SharedPreferences.Editor configurationEditor = widgetConfiguration.edit();
        configurationEditor.putString("widgetId", AppWidgetManager.INVALID_APPWIDGET_ID);

        // Commit the edits!
        configurationEditor.commit();
    }
}
