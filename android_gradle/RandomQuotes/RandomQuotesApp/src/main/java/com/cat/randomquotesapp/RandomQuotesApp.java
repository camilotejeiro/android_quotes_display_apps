package com.cat.randomquotesapp;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

// import our shared common library classes.
import com.cat.randomquoteslib.PreferencesStorage;
import com.cat.randomquoteslib.FilesProcessor;

/***********************************************************************
 * Random Quotes App.
 * Simply takes a text file(s) or multiple text files and displays them 
 * in random order.
 * @author C.A.T    ,=,e
 **********************************************************************/
public class RandomQuotesApp extends Activity
{    
    private static final String LOG_TAG = RandomQuotesApp.class.getName();

    // we only support one, app instance.
    private static final int APP_UNIQUE_INSTANCE_ID = 1;
    
    // our app configuration activity request code.
    private static final int APP_CONFIG_REQUEST_CODE = 2;
    
    // constants to define our daytime to keep the device display ON.
    private static final int DAYTIME_START_HOUR = 7;
    private static final int DAYTIME_END_HOUR = 19;
    
    // update every 1 hour (3600000 mS)
    private static final int APP_UPDATE_PERIOD = 3600000; 
    
    // our views.
    private static TextView fileNameView;
    private static TextView updateView;
    private static TextView lineNumberView;
    private static TextView quotesIndexView;
    
    // handler for our periodic updates.
    private Handler periodicUpdatesHandler;
    
    // wake lock to keep the device on as a picture frame as necessary.
    private WakeLock wakeLock;
    
    private KeyguardLock keyguardLock;
    
    // calendar for getting time of day (to wake up frame at AM and sleep at PM)
    private Calendar calendar;
    
    // called when our activity is created.
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {      
        // when "creating": Call super on create first (prevents nullPointers).
        super.onCreate(savedInstanceState);
         
        Log.i(LOG_TAG, "onCreate");        

        // instantiating our keyguardlock.
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
        keyguardLock = keyguardManager.newKeyguardLock("Picture Frame Keyguard Lock"); 
        
        // make sure the keyguardLock is disabled when this app is active.
        keyguardLock.disableKeyguard(); 
        
        // instantiating our wakelock.
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
            
            // handle wakelock depending on time of day.
            handleWakeLock(checkDaytime());
            
            // update the app with our current quote.
            updateAppView(currentQuote); 
        
            // Schedule our runnable to handle periodic updates from now on.
            periodicUpdatesHandler.postDelayed(periodicUpdatesRunnable, APP_UPDATE_PERIOD);
        }
        
    }
    
    // called when our app is closed.
    @Override
    protected void onDestroy()
    {          
        Log.i(LOG_TAG, "onDestroy"); 
    
        Log.d(LOG_TAG, "Releasing full wakelock");     
        
        // release wake lock if necessary.
        if (wakeLock.isHeld() == true)
            wakeLock.release();
        
        // activate lock screen after app is done.
        keyguardLock.reenableKeyguard(); 
        
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
            
            // handle wakelock depending on time of day.
            handleWakeLock(checkDaytime());

            // get our first quote.
            String [] currentQuote = getCurrentQuote();

            // update the app with our current quote.
            updateAppView(currentQuote); 
            
            // Schedule our runnable to handle periodic updates from now on.
            periodicUpdatesHandler.postDelayed(periodicUpdatesRunnable, APP_UPDATE_PERIOD);
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
            
            // check to see if it is daytime.
            boolean daytime = checkDaytime();
            
            // acquire or release wakelock as applicable.
            handleWakeLock(daytime);
            
            // only update quotes if on daytime (otherwise is futile)
            if (daytime == true)
            {
                String [] currentQuote = getCurrentQuote(); 
                
                // update the app with our current quote.
                updateAppView(currentQuote);  
            }
            
            // re schedule the runnable for our next update.           
            periodicUpdatesHandler.postDelayed(periodicUpdatesRunnable, APP_UPDATE_PERIOD);
        }
    };
    
    
    /**
     * Check Day Time
     * Method called for client code to know whether is day time.
     * @return boolean The flag indicating whether is daytime or not.
     **/
    private boolean checkDaytime()
    {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        Log.i(LOG_TAG, "checkDayTime, current time (24hr): " + hourOfDay);
        
        if ((hourOfDay >= DAYTIME_START_HOUR) && (hourOfDay < DAYTIME_END_HOUR))
            return true;
        else
            return false;
    }

    /**
     * Handle Wake Lock
     * Method called to acquire or release WakeLock based on time of day.
     * @param dayTime The flag indicating whether is daytime or not.
     **/    
    private void handleWakeLock(boolean dayTime)
    {
        Log.i(LOG_TAG, "handleWakeLock, daytime: " + dayTime);
        
        if (dayTime == true)
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
     * @param context The specific context we use to grab a handle of the remoteViews.
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
