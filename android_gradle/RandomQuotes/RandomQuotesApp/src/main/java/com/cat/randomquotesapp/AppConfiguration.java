package com.cat.randomquotesapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

// import our shared common library classes.
import com.cat.randomquoteslib.PreferencesStorage;
import com.cat.randomquoteslib.FilesProcessor;

/***********************************************************************
 * App Configuration
 * This class simply displays a configure screen 
 * where the user can input the text file(s) he wants the app to read 
 * from.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class AppConfiguration extends Activity
{
    private static final String LOG_TAG = AppConfiguration.class.getName();
    
    private static final int PICKFILE_REQUEST_CODE = 1;
        
    // we only support one, app instance.
    private static final int APP_UNIQUE_INSTANCE_ID = 1;
                
    // view objects.
    private EditText fileEditText;
    private EditText logEditText;
                
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG, "onCreate");  
        
        // now we will render the UI for the configure Activity.
        setContentView(R.layout.configure_layout);

        // we will have a file name input that can be configured.
        fileEditText = (EditText) findViewById(R.id.config_files_edittext);
        
        // Where we will display messages regarding configuration details.
        logEditText = (EditText) findViewById(R.id.config_log_edittext);

    }
    
    /**
     * Save User Configuration
     * Method called by Android onClick "OK" in our app configuration screen. 
     * See the onClick property in the configure_layout.xml button element.
     * @param View  
     **/
    public void saveUserConfiguration(View view) 
    {
        Log.i(LOG_TAG, "saveUserConfiguration");
        
        final Context context = AppConfiguration.this;
        
        // To keep track of errors to return.
        boolean configStatus = false;
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        // create our preferences storage object, pass our app specific properties.
        final PreferencesStorage storedPreferences = new PreferencesStorage(context, APP_UNIQUE_INSTANCE_ID);
        
        // create our filesProcessor Object, pass our underlying preferences storage. 
        FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
        
        // reset our edit text.
        logEditText.setText("");
        
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
        

        // it is the responsibility of the Activity to update the App 
        // the first time when the configuration is complete.
        
        String [] currentDataArray = filesProcessor.getCurrentParagraph();  
        // update the app with our current quote.
        RandomQuotesApp.updateAppView(currentDataArray);
        
        setResult(RESULT_OK);
        
        // end the activity.
        finish();
        
    }

    /**
     * Browse File Directories
     * Method called by Android onClick "Browse" in our app configuration screen. 
     * See the onClick property in the config   ure_layout.xml button element.
     * @param View  
     **/    
    public void browseFileDirectories(View view) 
    {
        Log.i(LOG_TAG, "browseFileDirectories");
        
        // We will use this var for displaying our results
        String configMessageLog = "";
        
        Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
        fileintent.setType("file/*");
        
        try 
        {
            startActivityForResult(fileintent, PICKFILE_REQUEST_CODE);
        } 
        catch (ActivityNotFoundException e) 
        {            
            configMessageLog = "No 3rd party file explorer application found";
            Log.d(LOG_TAG, configMessageLog);
            logEditText.setText("* " + configMessageLog + "\n"); 
        }
        
    }

    // called after the browser file picker returns.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    { 
        Log.i(LOG_TAG, "onActivityResult, Request code: " + requestCode 
            + ", Result Code: " + resultCode);
        
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
        }
    }
}
