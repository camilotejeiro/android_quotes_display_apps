package com.cat.randomquoteswidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/***********************************************************************
 * App Configuration
 * This class simply displays a configure screen 
 * where the user can input the text file(s) he wants the widget to read 
 * from.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class AppConfiguration extends Activity
{
    private static final String LOG_TAG = AppConfiguration.class.getName();
    
    private static final int PICKFILE_REQUEST_CODE = 1;
    private static final int PICKFILE_SUCCESS_RESULT_CODE = 1;
                
    // view objects.
    private EditText fileEditText;
    private EditText logEditText;
                
    // declare and reset our AppwidgetID.
    private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG, "Configuration Screen loaded");
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // receive the intent and get the unique key.
        Intent receivedIntent = getIntent();
        Bundle extras = receivedIntent.getExtras();
        if (extras != null)
        {
            myAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                            AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Finish if they gave us an intent without a widget ID, we can't proceed.
        if (myAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 
        {
            finish();
        }
        
        // now we will render the UI for the configure Activity.
        setContentView(R.layout.configure_layout);

        // we will have a file name input that can be configured in the widget.
        fileEditText = (EditText) findViewById(R.id.file_edit_text);
        
        // Where we will display messages regarding configuration details.
        logEditText = (EditText) findViewById(R.id.log_edit_text);
    }
    
    public void saveUserConfiguration(View view) 
    {
        Log.i(LOG_TAG, "onClick: Save App configuration");
        
        final Context context = AppConfiguration.this;
        
        // To keep track of errors to return.
        boolean configStatus = false;
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        // create our preferences storage object, pass our widget specific properties.
        PreferencesStorage storedPreferences = new PreferencesStorage(myAppWidgetId, context);
        
        // create our filesProcessor Object, pass our underlying preferences storage. 
        FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
        
        configMessageLog = "Our Widget Instance ID: " + myAppWidgetId;
        Log.d(LOG_TAG, configMessageLog);
        logEditText.setText("* " +  configMessageLog + "\n");
        
        // get the text from the text input view
        String userInput = fileEditText.getText().toString();
        
        // display what the user typed.
        configMessageLog = "User Input: " + userInput;
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");                

        // now lets break the string by our separator.
        String[] filePathsArray = userInput.split(";"); 
        
        // check our files to make sure everything is correct.
        configStatus = filesProcessor.checkFilePaths(filePathsArray); 
        
        if (configStatus == false)
        {
            configMessageLog = "Incorrect file path or no .txt file extension";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("* " + configMessageLog + ", please re-submit" + "\n"); 
            
            // exit, let the user correct entries.                    
            return;
        }

        configMessageLog = "Storing user preferences";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");

        // otherwise  let's store the valid user preferences.
        storedPreferences.updateUserPreferences(filePathsArray);
                        
        configMessageLog = "Processing Text Files to Quotes";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("* " + configMessageLog + "\n");
        
        // and let's actually process the files.
        filesProcessor.processTextFiles();
        
        // End log info
        configMessageLog = "Configuration Complete";
        Log.d(LOG_TAG, configMessageLog);
        logEditText.append("***" + configMessageLog + "*** \n");
        
        //When an App Widget uses a configuration Activity, it is 
        //the responsibility of the Activity to update the App 
        //the first time when the Widget when configuration is complete.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        // pass back the current processed data.
        String[] currentDataArray = storedPreferences.getCurrentDataPreferences();
        
        RandomQuotesWidget.updateAppWidget(myAppWidgetId, context, appWidgetManager,
            currentDataArray);
        
        // the configuration Activity should always return a result. 
        // The result should include the App Widget ID passed by 
        // the Intent that launched the Activity.
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
        setResult(RESULT_OK, resultValue);
        
        // end the activity.
        finish();
        
    }
    
    public void browseFileDirectories(View view) 
    {
        Log.i(LOG_TAG, "onClick: browse file directories");
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
        fileintent.setType("file/*");
        
        try 
        {
            startActivityForResult(fileintent, PICKFILE_SUCCESS_RESULT_CODE);
        } 
        catch (ActivityNotFoundException e) 
        {            
            configMessageLog = "No 3rd party file explorer application found";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.setText("* " + configMessageLog + "\n"); 
        }
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        Log.d(LOG_TAG, "onActivityResult"); 
        Log.d(LOG_TAG, "Request code: " + requestCode + ", Result Code: " + resultCode);
        
        // check to make sure we received data.
        if (data == null || data.getData() == null)
        {
            // received no data, mmmph
            Log.d(LOG_TAG, "Null intent: Received no data");
        }
        else if (requestCode == PICKFILE_REQUEST_CODE)
        {
            // file has been selected.
            String filePath = data.getData().getPath();
            Log.d(LOG_TAG, "got filePath: " + filePath);  

            Log.d(LOG_TAG, "File Picked Successfully");                   
            // is the editText empty, then don't prefix ';' just set.
            if (fileEditText.getText().toString().trim().length() == 0)
                fileEditText.setText(filePath);
            else
                fileEditText.append(";" + filePath);
        }
        else
        {
            // This is not our request (different request code)
            Log.d(LOG_TAG, "Different request code");
            return;
        }
    }
    // now we will get the ok button view and put a click listener to it.
/*    Button ok = (Button) findViewById(R.id.ok_button);
    ok.setOnClickListener( new OnClickListener()
    {
        @Override
        public void onClick(View v) 
        {
            final Context context = AppConfiguration.this;
            
            // To keep track of errors to return.
            boolean configStatus = false;
            
            // We will use this var for displaying our results
            String configMessageLog = "";
            
            // create our preferences storage object, pass our widget specific properties.
            PreferencesStorage storedPreferences = new PreferencesStorage(myAppWidgetId, context);
            
            // create our filesProcessor Object, pass our underlying preferences storage. 
            FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
            
            configMessageLog = "Our Widget Instance ID: " + myAppWidgetId;
            Log.d(LOG_TAG, configMessageLog);
            logEditText.setText("* " +  configMessageLog + "\n");
            
            // get the text from the text input view
            String userInput = fileEditText.getText().toString();
            
            // display what the user typed.
            configMessageLog = "User Input: " + userInput;
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("* " + configMessageLog + "\n");                

            // now lets break the string by our separator.
            String[] filePathsArray = userInput.split(";"); 
            
            // check our files to make sure everything is correct.
            configStatus = filesProcessor.checkFilePaths(filePathsArray); 
            
            if (configStatus == false)
            {
                configMessageLog = "Incorrect file path or no .txt file extension";
                Log.d(LOG_TAG, configMessageLog);
                logEditText.append("* " + configMessageLog + ", please re-submit" + "\n"); 
                
                // exit, let the user correct entries.                    
                return;
            }

            configMessageLog = "Storing user preferences";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("* " + configMessageLog + "\n");

            // otherwise  let's store the valid user preferences.
            storedPreferences.updateUserPreferences(filePathsArray);
                            
            configMessageLog = "Processing Text Files to Quotes";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("* " + configMessageLog + "\n");
            
            // and let's actually process the files.
            filesProcessor.processTextFiles();
            
            // End log info
            configMessageLog = "Configuration Complete";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.append("***" + configMessageLog + "*** \n");
            
            //When an App Widget uses a configuration Activity, it is 
            //the responsibility of the Activity to update the App 
            //the first time when the Widget when configuration is complete.
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            
            // pass back the current processed data.
            String[] currentDataArray = storedPreferences.getCurrentDataPreferences();
            
            RandomQuotesWidget.updateAppWidget(myAppWidgetId, context, appWidgetManager,
                currentDataArray);
            
            // the configuration Activity should always return a result. 
            // The result should include the App Widget ID passed by 
            // the Intent that launched the Activity.
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
            setResult(RESULT_OK, resultValue);
            
            // end the activity.
            finish();
        }
    });
    */
}
