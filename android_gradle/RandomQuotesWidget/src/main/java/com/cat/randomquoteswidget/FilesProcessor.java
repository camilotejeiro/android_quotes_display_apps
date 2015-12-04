package com.cat.randomquoteswidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;

/***********************************************************************
 * Files Processor
 * This class is a library which provides all of the necessary text 
 * files manipulation routines. 
 * @author C.A.T    ,=,e
 **********************************************************************/
public class FilesProcessor
{
    private static final String LOG_TAG = FilesProcessor.class.getName();
    
    private PreferencesStorage storedPreferences;
    
    
    public FilesProcessor(PreferencesStorage myPreferencesStorage)
    {
        // initialize our underlying storage for our files processor.
        storedPreferences = myPreferencesStorage;
    }
    
    public boolean checkFilePaths(String[] filePathsArray)
    {
        
        // to create our log strings.
        String configMessageLog = "";
        
        // Now let's go ahead and check the individual paths 
        // for correctness and if so store them in our preferences 
        // file.
        for (int i = 0; i < filePathsArray.length; i++)
        {
            // now pass the string path to create the file.
            File desiredPath = new File (filePathsArray[i]);

            // display the current path being parsed.
            configMessageLog = "Checking Path[" + i + "]: " 
                + desiredPath;
            Log.d(LOG_TAG, configMessageLog);
            
            // now we want to do a very quick check to make sure the path exists.
            if (desiredPath.exists() == false)
            {
                configMessageLog = "desired path[" + i + "] does not exist.";
                Log.d(LOG_TAG, configMessageLog);
                return false;
            }
            
            // get only the filename with no preceding path.
            String desiredFile = desiredPath.getName();
            
            // display the actual file being parsed.
            configMessageLog = "Checking file[" + i + "]: " 
                + desiredFile;
            Log.d(LOG_TAG, configMessageLog);                 
            
            // check to make sure the filetype is .txt
            if ((desiredFile.substring((desiredFile.lastIndexOf(".") + 1), desiredFile.length())).equals("txt") == false)
            {
                configMessageLog = "desired file[" + i + "] is not a text file";
                Log.d(LOG_TAG, configMessageLog);
                return false;
            }
        }
        // this path and file were correct.
        return true;
    }   

    public void processTextFiles()
    {
        
        // LOAD all of the paragraphs into our array only once, no need to read every time
        Log.d(LOG_TAG, "Processing text files");
        
        // declare our array to hold our user checked filePaths.
        String[] userFilePaths;

        // array we will use for our paragraphs, files and line numbers
        String[] paragraphsArray;
        String[] filePathsArray;
        String[] lineNumbersArray;
                
        // lists we will use first, since we don't know the lenght yet.
        List<String> paragraphsList = new ArrayList<String>();
        List<String> filePathsList = new ArrayList<String>();
        List<String> lineNumbersList = new ArrayList<String>();
        
        userFilePaths = storedPreferences.getUserPreferences();
        
        for (int i = 0; i < userFilePaths.length; i++)
        {
            // LOAD all of the paragraphs into our array only once, no need to read every time
            Log.d(LOG_TAG, "Current Text file: " + userFilePaths[i]);
            
            // to count lines and track start of paragraph.
            // our file numbers start from one.
            int fileLineNumber = 1;
            // to count valid paragraph lines.
            int paragraphLineNumber = 0;
            
            // routine to read from the SD card text file.
            File fileToRead = new File (userFilePaths[i]);
            
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
                        // convert our built paragraph to a string.
                        currentParagraph = paragraphBuilder.toString();
                        // add it to list of paragraph Strings.
                        paragraphsList.add(currentParagraph);
                        //clear the paragraphs list
                        paragraphBuilder.setLength(0);
                        
                        // add our filePath to this quote.
                        filePathsList.add(userFilePaths[i]);
                        
                        // add our lineNumbers to this quote. 
                        // last file-line number - number of lines in paragraph.
                        lineNumbersList.add(Integer.toString(fileLineNumber-paragraphLineNumber));
                        
                        // reset our paragraph line number.
                        paragraphLineNumber = 0;
                    }
                    else
                    {
                        // just add another line to the paragraph
                        paragraphBuilder.append(currentLine); 
                    
                        paragraphLineNumber++;
                    }
                    
                    fileLineNumber++;
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
            }
            
        }

        // now that we know the total size add all of the elements from 
        // the lists to arrays.
        
        // convert paragraphs list to paragraphs array.
        paragraphsArray = new String[paragraphsList.size()];
        paragraphsArray = paragraphsList.toArray(paragraphsArray);
        
        // convert filepaths list to paragraphs array.
        filePathsArray = new String[filePathsList.size()];
        filePathsArray = filePathsList.toArray(filePathsArray);
        
        // convert line numbers list to paragraphs array.
        lineNumbersArray = new String[lineNumbersList.size()];
        lineNumbersArray = lineNumbersList.toArray(lineNumbersArray);
        
        Log.d(LOG_TAG, "size of resulting arrays: " + paragraphsArray.length 
                + ", " + filePathsArray.length + ", " + lineNumbersArray.length);
        
        // lets shuffle our array of paragraphs, keeping our indexes consistent.
        shuffleArray(paragraphsArray, filePathsArray, lineNumbersArray);
        
        Log.d(LOG_TAG, "Arrays Shuffled Succesfully");
        
        // commit our changes to a data preferences file.
        storedPreferences.updateDataPreferences(paragraphsArray, 
            filePathsArray, lineNumbersArray);
        
    }

   /**
     * Shuffle Array
     * Function to shuffle the elements in an array. 
     * @param paragraphsArray The array of 
     **/
    private void shuffleArray(String [] paragraphsArray, String [] filePathsArray, String [] lineNumbersArray)
    {
        Random rnd = new Random();
        
        // get a random index and swap, then repeat for previous index. 
        // to keep indexes consistent apply the operations for all arrays.
        for (int i = paragraphsArray.length - 1; i > 0; i--)
        {
            int randomIndex = rnd.nextInt(i + 1);

            // Simple swap for paragraphs array.
            String tempParagraph = paragraphsArray[randomIndex];
            paragraphsArray[randomIndex] = paragraphsArray[i];
            paragraphsArray[i] = tempParagraph;

            // Simple swap for filePathsArray Array.
            String tempFilePath = filePathsArray[randomIndex];
            filePathsArray[randomIndex] = filePathsArray[i];
            filePathsArray[i] = tempFilePath;

            // Simple swap for lineNumbers array.
            String tempLineNumber = lineNumbersArray[randomIndex];
            lineNumbersArray[randomIndex] = lineNumbersArray[i];
            lineNumbersArray[i] = tempLineNumber;
        }
    }

}
