package com.cat.randomquoteswidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/***********************************************************************
 * Preferences Storage
 * This class simply provides a library interface to read and write to 
 * android xml storage preferences.
 * Note: while sqlite might seem like a better approach for implementing 
 * our data storage, it was not used because the amount of data we are 
 * handling is quite small and the number of I/Os is tiny. It would have 
 * been an unnecessary feature (perhaps this will change in the future).
 * @author C.A.T    ,=,e
 **********************************************************************/
public class PreferencesStorage
{
    private static final String LOG_TAG = PreferencesStorage.class.getName();
    
    // The file stored in memory to keep the selected user preferences.
    private static final String QUOTES_USER_CONF_FILE = "quotes_user_conf";
    // The file stored in memory to keep the processed data.
    private static final String QUOTES_DATA_FILE = "quotes_data";
    
    // our class properties.
    private int appInstanceId;
    private Context context;
    
    /**
     * Preferences Storage Constructor
     * Every new PreferencesStorage object should be identified by an 
     * specific passed instanceId (for example in the case of multiple 
     * widget instances) and a context.
     * @param myAppInstanceId
     * @param myContext
     **/
    public PreferencesStorage(Context myContext, int myAppInstanceId)
    {
        // initialize our class properties, we need specific parameters 
        // for every instance.
        context = myContext;
        appInstanceId = myAppInstanceId;
    }
    
    /**
     * Update User Preferences
     * Put our validated user preferences in storage for easy retrieval 
     * later.
     * @param filePathsArray The array of user chosen file paths.  
     **/
    public void updateUserPreferences(String[] filePathsArray)
    {
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
     * Get User Preferences
     * Get the stored user-chosen preferences, get out all of our stored 
     * file paths.
     * @return String[] An array holding all of the user-chosen file paths.
     **/
    public String[] getUserPreferences()
    {
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
    
    /**
     * Update Data Preferences
     * Put our new updated processed data in storage, for easy retrieval 
     * later.
     * @param paragraphsArray An array holding all of the user paragrapsh(quotes)
     * @param filePathsArray An array holding the user filePaths for display.
     * @param lineNumsArray An array holding the files' line numbers.
     **/
    public void updateDataPreferences(String[] paragraphsArray, 
        String[] filePathsArray, String[] lineNumsArray)
    {  
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

    /**
     * Get Current Data Preferences
     * Get our current quote and quote-info, then update our index for next 
     * time.
     * Output data Format:
     * String[0]: current quotes array index
     * String[1]: total quotes array length
     * String[2]: quotes array current element.
     * String[3]: filePaths array current element.
     * String[4]: lineNums array current element.
     * @return String[] The array of data to update our view.
     **/
    public String[] getCurrentDataPreferences()
    {
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
    
    /**
     * Delete All Preferences
     * Convenience method to clear any user or data preferences with our 
     * specific class properties.
     **/
    public void deleteAllPreferences()
    {
        deleteUserPreferences();
        deleteDataPreferences();        
    }
    
    /**
     * Delete User Preferences
     * Convenience method to delete any user preferences with our specific 
     * class properties.
     **/
    private void deleteUserPreferences()
    {
        SharedPreferences userPreferences = openUserPreferencesFile();
        SharedPreferences.Editor configurationEditor = userPreferences.edit();
        configurationEditor.clear();

        // Commit the edits!
        configurationEditor.commit();
    }
    
    /**
     * Delete Data Preferences
     * Convenience method to delete any data preferences with our specific 
     * class properties.
     **/
    private void deleteDataPreferences()
    {
        SharedPreferences dataPreferences = openDataPreferencesFile();
        SharedPreferences.Editor configurationEditor = dataPreferences.edit();
        configurationEditor.clear();

        // Commit the edits!
        configurationEditor.commit();        
    }
    
    /**
     * Open User Preferences File
     * Convenience method to open/create a user xml preferences file and pass 
     * the SharedPreferences handle to client code for file read/write.
     * @return SharedPreferences The handle to the open preferences file. 
     **/
    private SharedPreferences openUserPreferencesFile()
    {
        String prefsFileName = QUOTES_USER_CONF_FILE + "_" + appInstanceId;
        
        // open or create if non-existent.
        return context.getSharedPreferences(prefsFileName, 0);
    }
    
    /**
     * Open Data Preferences File
     * Convenience method to open/create a data xml preferences file and pass 
     * the SharedPreferences handle to client code for read/write.
     * @return SharedPreferences The handle to the open preferences file. 
     **/
    private SharedPreferences openDataPreferencesFile()
    {
        String prefsFileName = QUOTES_DATA_FILE + "_" + appInstanceId;
        
        // open or create if non-existent.
        return context.getSharedPreferences(prefsFileName, 0);
    }    

}
