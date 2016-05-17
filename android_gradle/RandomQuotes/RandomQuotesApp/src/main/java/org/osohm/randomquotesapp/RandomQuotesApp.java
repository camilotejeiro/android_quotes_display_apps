package org.osohm.randomquotesapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Calendar;

// import our shared common library classes.
import org.osohm.randomquoteslib.PreferencesStorage;
import org.osohm.randomquoteslib.FilesProcessor;

/***********************************************************************
 * Random Quotes App.
 * Simply takes a text file(s) or multiple text files and displays them 
 * in random order.
 * @author Camilo Tejeiro ,=,e for Osohm
 **********************************************************************/
public class RandomQuotesApp extends Activity
{    
    private static final String LOG_TAG = RandomQuotesApp.class.getName();

    // we only support one, app instance.
    private static final int APP_UNIQUE_INSTANCE_ID = 1;
    
    // our app configuration activity request code.
    private static final int APP_CONFIG_REQUEST_CODE = 2;
    
    // our views.
    private static TextView fileNameView;
    private static TextView updateView;
    private static TextView lineNumberView;
    private static TextView quotesIndexView;
    
    // handler for our periodic updates.
    private Handler periodicUpdatesHandler;
    
    // wake lock to keep the device on as a picture frame as necessary.
    private WakeLock wakeLock;
    
    // calendar for getting time of day (to wake up frame at AM and sleep at PM)
    private Calendar calendar;
    
    // called when our activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {      
        // when "creating": Call super on create first (prevents nullPointers).
        super.onCreate(savedInstanceState);
         
        Log.i(LOG_TAG, "onCreate");  

        // Window properties: Show app activity in front of lockscreen 
        // and show app activity in fullscreen mode.
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED 
            | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Creating our wakelock (I know it's deprecated).
        // Use Wakelocks to turn screen ON until they become obsolete! 
        // Why? Setting/clearing the layoutParameter FLAG_TURN_SCREEN_ON 
        // does NOT work. It can only be set once at the start of the 
        // activity. Furthemore the adjusting screen brightness hack 
        // does not turn the screen fully OFF and also if the 
        // user locks the screen, which everyone does, we can not 
        // really turn the screen ON again, it's useless. Stick to your wakelock. 
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, 
            "Picture Frame Device Wake Lock");

        setContentView(R.layout.app_layout);
        
        // get a handle of our view objects.
        fileNameView = (TextView) findViewById(R.id.app_file_name_textview);        
        updateView = (TextView) findViewById(R.id.app_update_textview);  
        lineNumberView = (TextView) findViewById(R.id.app_line_number_textview); 
        quotesIndexView = (TextView) findViewById(R.id.app_queue_number_textview); 
        
        // create the handler we will use to execute our runnable at periodic 
        // intervals.
        periodicUpdatesHandler = new Handler();

        String [] currentQuote = getCurrentQuote();

        // check to see if we've an underlying preferences file.
        if (currentQuote == null)
        {
            Log.d(LOG_TAG, "No current data preferences found");
              
            // launch the configuration screen
            // periodic updates will be scheduled after configuration is complete.
            launchAppConfiguration(null);
        }
        else
        {     
            Log.d(LOG_TAG, "Found prior stored data preferences");
            
            // Turn Screen ON, depending on time of day.
            handleWakeLock(checkDaytime());
            
            // update the app with our current quote.
            updateAppView(currentQuote); 
            
            schedulePeriodicUpdates();
        }
        
    }
    
    // called when our app is closed.
    @Override
    protected void onDestroy()
    {          
        Log.i(LOG_TAG, "onDestroy"); 

        // release wake lock if necessary.
        if (wakeLock.isHeld() == true)
            wakeLock.release();

        Log.d(LOG_TAG, "Disabling periodic updates"); 
        periodicUpdatesHandler.removeCallbacks(periodicUpdatesRunnable);
    
        // When "destroying": call super onDestroy last (prevents nullPointers).
        super.onDestroy();
    }

    /**
     * Get Current Quote
     * Use our filesProcessor to read, validate and get our current paragraph 
     * from storage. 
     * @return String[] Our current quote and quote extra info (file, line num ...etc)
     **/ 
    private String[] getCurrentQuote()
    {
        Log.i(LOG_TAG, "getCurrentQuote"); 
        
        final Context context = RandomQuotesApp.this;
        
        // Create our stored preferences object
        PreferencesStorage storedPreferences = new PreferencesStorage(context, APP_UNIQUE_INSTANCE_ID);
        
        // get our current data if available.
        FilesProcessor filesProcessor = new FilesProcessor(storedPreferences);
        String [] currentParagraph = filesProcessor.getCurrentParagraph();
        
        return currentParagraph;
    }

    /**
     * On Tap Next Quote
     * Method called by clicking the quotes screen textView.
     * @param View  
     **/ 
    public void onTapNextQuote(View view) 
    {
        Log.i(LOG_TAG, "onTapNextQuote");
        
        String [] currentQuote = getCurrentQuote(); 
        
        // update the app with our current quote.
        updateAppView(currentQuote); 
              
    }

    /**
     * Launch App Configuration
     * Method called by clicking the info textViews (file name, line number 
     * or queue) or to launch the appConfiguration activity.
     * @param View  
     **/     
    public void launchAppConfiguration(View view)
    {
        Log.i(LOG_TAG, "launchAppConfiguration");
        
        final Context context = RandomQuotesApp.this;
        
        Log.d(LOG_TAG, "Disabling periodic updates"); 
        
        // disable any timedSchedule tasks if any.
        periodicUpdatesHandler.removeCallbacks(periodicUpdatesRunnable);
        
        // we'll set up an Intent to open the Configure screen.
        Intent configureIntent = new Intent(context, AppConfiguration.class);    
        // start our configuration activity.
        startActivityForResult(configureIntent, APP_CONFIG_REQUEST_CODE);
    }
    
    // called after the appConfiguration returns. 
    // we need to do it manually, to handle our runnable start and our cancel.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        Log.i(LOG_TAG, "onActivityResult, " + "Request code: " + requestCode 
            + ", Result Code: " + resultCode); 
        
        if ((requestCode == APP_CONFIG_REQUEST_CODE) && (resultCode == RESULT_OK))
        {
            Log.d(LOG_TAG, "periodicUdatesRunnable: Scheduling periodic updates");
            
            // Turn Screen ON, depending on time of day.
            handleWakeLock(checkDaytime());

            // get our first quote.
            String [] currentQuote = getCurrentQuote();

            // update the app with our current quote.
            updateAppView(currentQuote); 
            
            schedulePeriodicUpdates();
        }
        else
        {
            Log.d(LOG_TAG, "Different Request Code or Incorrect Result code"); 
            finish();
        }
    }

    // runnable for executing periodic updates.
    private Runnable periodicUpdatesRunnable = new Runnable() 
    {
        @Override 
        public void run() 
        {
            Log.i(LOG_TAG, "periodicUdatesRunnable: Executing periodic update");
            
            boolean daytime = checkDaytime();
            
            // Turn Screen ON, depending on time of day.
            handleWakeLock(daytime);
            
            // only update quotes if on daytime (otherwise is futile)
            if (daytime == true)
            {
                String [] currentQuote = getCurrentQuote(); 
                
                // update the app with our current quote.
                updateAppView(currentQuote);  
            }
            
            // re-schedule our updates.
            schedulePeriodicUpdates();
        }
    };
    
    /**
     * Schedule Periodic Updates
     * Convenience method for reading the user-desired update period 
     * and scheduling our updates.
     **/ 
    private void schedulePeriodicUpdates()
    {
        
        final Context context = RandomQuotesApp.this;
        
        // Create our stored preferences object
        PreferencesStorage storedPreferences = new PreferencesStorage(context, APP_UNIQUE_INSTANCE_ID);
        
        // schedule the runnable for our next update.           
        periodicUpdatesHandler.postDelayed(periodicUpdatesRunnable, storedPreferences.getUserTimePeriod());
    
    }
    
    /**
     * Check Day Time
     * Method called for client code to know whether is day time.
     * @return boolean The flag indicating whether is daytime or not.
     **/
    private boolean checkDaytime()
    {
        final Context context = RandomQuotesApp.this;
        
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        Log.i(LOG_TAG, "checkDaytime, current time (24hr): " + hourOfDay);
        
        // Create our stored preferences object
        PreferencesStorage storedPreferences = new PreferencesStorage(context, APP_UNIQUE_INSTANCE_ID);
        int[] daytime = storedPreferences.getUserDaytime();
        
        if ((hourOfDay >= daytime[0]) && (hourOfDay < daytime[1]))
            return true;
        else
            return false;
    }

    /**
     * Handle Wake Lock
     * Set wakeLock properties to set screen state based on time of the 
     * day (I know it's deprecated see comments above).
     * @param daytime The flag indicating whether is daytime or not.
     **/    
    private void handleWakeLock(boolean daytime)
    {
        Log.i(LOG_TAG, "handleWakeLock, daytime: " + daytime);
        
        if (daytime == true)
        {
            // acquire wakelock to keep screen ON if necessary.
            if (wakeLock.isHeld() == false)
                wakeLock.acquire();
                            
        }
        else
        {
            // release wakelock to let phone go to sleep as necessary.
            if (wakeLock.isHeld() == true)
                wakeLock.release();
        }
    }
    
    /**
     * Update App View
     * Method called by elements wishing to update our app current view.
     * @param currentAppData Array with data to display in the app view.
     **/ 
    public static void updateAppView(String[] currentAppData) 
    {
        Log.i(LOG_TAG, "updateApp,  textElements = [" 
            + currentAppData[0] + ", " + currentAppData[1] + ", " 
            + currentAppData[2] + ", " + currentAppData[3] + ", " 
            + currentAppData[4] + "]");
        
        // set quotes queue: index/lenght
        quotesIndexView.setText("queue number: " 
            + currentAppData[0] + "/" + currentAppData[1]);
        // set quote paragraph.
        updateView.setText(currentAppData[2]);
        // set file name (remove long path for display)
        String fileName = currentAppData[3].substring(currentAppData[3].lastIndexOf("/")+1);
        fileNameView.setText(fileName);
        // set line number
        lineNumberView.setText("line number: " + currentAppData[4]);
        
        
        
    }
}
