package com.cat.randomquoteswidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/***********************************************************************
 * Preferences Storage
 * This class simply provides a library interface to read and write to 
 * android xml storage preferences.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class PreferencesStorage
{
    private static final String LOG_TAG = PreferencesStorage.class.getName();
    
    // The file stored in memory to keep the selected user preferences.
    private static final String QUOTES_WIDGET_CONF_FILE = "quotes_widget_conf";
    // The file stored in memory to keep the processed data.
    private static final String QUOTES_WIDGET_DATA_FILE = "quotes_widget_data";
    
    // our class properties.
    private int appWidgetId;
    private Context context;
    
    
    public PreferencesStorage(int myAppWidgetId, Context myContext)
    {
        // initialize our class properties, we need widget specific parameters.
        appWidgetId = myAppWidgetId;
        context = myContext;
    }
    
    /**
     * updateUserPreferences
     * This private method is used to create a new shared preferences with 
     * our widget id as a suffix and add the parsed filePath.
     * @param appWidgetId The id of our widget instance.
     * @param filePath The chosen text file.  
     **/
    public void updateUserPreferences(String[] filePathsArray)
    {
        Log.d(LOG_TAG, "UpdateUserPreferences");
        
        // clear all of our current user preferences if any.
        deleteUserPreferences();
        
        // open shared preferences file for edit.
        SharedPreferences userPreferences = openUserPreferencesFile();
        SharedPreferences.Editor configurationEditor = userPreferences.edit();
        
        // put the number of filePaths
        configurationEditor.putInt("arrayLength", filePathsArray.length);
        
        // write our user chosen filePaths.
        for(int i = 0 ; i < filePathsArray.length ; i++)
            configurationEditor.putString("filePathsArray_" + i, filePathsArray[i]);
        
        // Commit the edits!
        configurationEditor.commit();
    }
    
    /**
     * Get User Preferences.
     * @param myAppWidgetID The current id of our widget instance.
     * @param context The current context passed by the widget.
     * @return String The path to the text file to read from.
     **/
    public String[] getUserPreferences()
    {
        Log.d(LOG_TAG, "GetUserPreferences");
        
        int arrayLength = 0;
        String[] filePathsArray;
        
        SharedPreferences userPreferences = openUserPreferencesFile();
        
        //get the number of filePaths
        arrayLength = userPreferences.getInt("arrayLength", 0);
        
        // initialize our array now that we know the size.
        filePathsArray = new String[arrayLength];
        
        // get our user chosen filePaths.
        for(int i = 0 ; i < arrayLength ; i++)
            filePathsArray[i] = userPreferences.getString("filePathsArray_" + i, null);
            
        return filePathsArray;

    }
    
    public void updateDataPreferences(String[] paragraphsArray, 
        String[] filePathsArray, String[] lineNumsArray)
    {
        Log.d(LOG_TAG, "updateDataPreferences");   
             
        // clear all of our current data preferences if any.
        deleteDataPreferences();
        
        // open the sharedpreferences for edit.
        SharedPreferences dataPreferences = openDataPreferencesFile();
        SharedPreferences.Editor configurationEditor = dataPreferences.edit();
        
        // set the array index to start position.
        configurationEditor.putInt("arrayIndex", 0);
        
        // set the array lenght.
        configurationEditor.putInt("arrayLength", paragraphsArray.length);
        
        // then let's go ahead and actually put the data in.
        for (int i = 0 ; i < paragraphsArray.length ; i++)
        {
            configurationEditor.putString("paragraphsArray_" + i, paragraphsArray[i]);
            configurationEditor.putString("filePathsArray_" + i, filePathsArray[i]);
            configurationEditor.putString("lineNumsArray_" + i, lineNumsArray[i]);            
        }
        
        // Commit the edits!
        configurationEditor.commit();
    }

    public String[] getCurrentDataPreferences()
    {
        Log.d(LOG_TAG, "getCurrentDataPreferences"); 
        int currentArrayIndex = 0;
        int arrayLength = 0;
        String[] currentDataArray = new String[5];
        
        // open the sharedpreferences for read/write
        SharedPreferences dataPreferences = openDataPreferencesFile();
        SharedPreferences.Editor configurationEditor = dataPreferences.edit();
        
        //get our array lenght.
        arrayLength  = dataPreferences.getInt("arrayLength", 0);
        
        // get our current Index, if it fails default to our lenght for error handling.
        currentArrayIndex = dataPreferences.getInt("arrayIndex", arrayLength);

        // if we have reached the end of our array, 
        if (currentArrayIndex >= arrayLength)
        {
            // we've reached the end, we have no more current data.
            // client code should re-process text files, i.e. get new current data
            return null;
        }
        else
        {

            // return current quotes index
            currentDataArray[0] = Integer.toString(currentArrayIndex); 
            // return total quotes
            currentDataArray[1] = Integer.toString(arrayLength);                        
            // return the current paragraph (current quote)
            currentDataArray[2] = dataPreferences.getString("paragraphsArray_" + currentArrayIndex, null);
            // return the current filePath
            currentDataArray[3] = dataPreferences.getString("filePathsArray_" + currentArrayIndex, null);
            // return the line number
            currentDataArray[4] = dataPreferences.getString("lineNumsArray_" + currentArrayIndex, null);
            
            // increment our index.
            configurationEditor.putInt("arrayIndex", currentArrayIndex + 1);
            
            // Commit the edits!
            configurationEditor.commit();
            
            // fetched current data succesfully.
            return currentDataArray;
        }
    }
    
    public void deleteAllPreferences()
    {
        Log.d(LOG_TAG, "deleteAllPreferences");
        deleteUserPreferences();
        deleteDataPreferences();        
    }
    
    private void deleteUserPreferences()
    {
        Log.d(LOG_TAG, "deleteUserPreferences");
        SharedPreferences userPreferences = openUserPreferencesFile();
        SharedPreferences.Editor configurationEditor = userPreferences.edit();
        configurationEditor.clear();

        // Commit the edits!
        configurationEditor.commit();
    }
    
    private void deleteDataPreferences()
    {
        Log.d(LOG_TAG, "deleteDataPreferences");
        SharedPreferences dataPreferences = openDataPreferencesFile();
        SharedPreferences.Editor configurationEditor = dataPreferences.edit();
        configurationEditor.clear();

        // Commit the edits!
        configurationEditor.commit();        
    }
    
    private SharedPreferences openUserPreferencesFile()
    {
        Log.d(LOG_TAG, "openUserPreferencesFile");
        String prefsFileName = QUOTES_WIDGET_CONF_FILE + "_" + appWidgetId;
        
        // open or create if non-existent.
        return context.getSharedPreferences(prefsFileName, 0);
    }
    
    private SharedPreferences openDataPreferencesFile()
    {
        Log.d(LOG_TAG, "openDataPreferencesFile");
        String prefsFileName = QUOTES_WIDGET_DATA_FILE + "_" + appWidgetId;
        
        // open or create if non-existent.
        return context.getSharedPreferences(prefsFileName, 0);
    }    

}
