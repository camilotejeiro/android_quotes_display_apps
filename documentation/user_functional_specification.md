Quotes Display Apps Functional Specification V2.1
=================================================

## Project Goal
---

Develop two applications: One simple application for picture frames (e.g. 
old android devices) and one widget application for cell-phones. The 
general purpose of the application is to process multiple text-files and 
break and randomize paragraphs (each paragraph is a quote), then save the 
processed data on non-volatile memory, finally on-click or at periodic 
intervals an UI element can read the next quote from the processed text 
file and update the view.

## Use Cases.
---

A person has a text file with some paragraphs(quotes) or notes he/she has 
made (life quotes, book summaries ...etc).

### A house picture frame

He would like to put his notes in a digital picture frame for displaying 
them in the house/apt ...etc. He uses an old android phone as a picture 
frame and installs the Random Quotes Application to display a random quote 
at periodic intervals (every hour, every day ...etc).

### A personal reminder.

She would like to keep her notes in her cell-phone and display a new quote 
on Tap or at periodic Intervals. She installs the Random Quotes Widget to 
display a random quote in her phone home screen at periodic intervals. 


## Features & Issues to Solve
---

### Random Quotes Widget

* Change project Ownership.  
    org.osohm  
    > Done.

* Add menu of different periodic intervals.  
    > Done.

* Scan Directories.  
    Support for scanning diretories and populating the book summaries list,  
    so that you dont have to select file by file.  
    > Done.

* Add support for variable widget updates.  
    
    
### Random Quotes Libraries.
* Integrated file/dir choser library.  
    aFileDialog  
    > Done.

### Random Quotes Application.

* Change project Ownership.  
    org.osohm  
    > Done.

* Fix deprecated functionality.  
    Wakelock and KeyguardLock.  
    > Done.

* Add menu of different periodic intervals.  
    > Done.

* Scan Directories.
    Support for scanning diretories and populating the book summaries list, 
    so that you dont have to select file by file.  
    > Done.
    
* Select Start & End Time.
    When the picture frame turns on. When the frame turns off.  
    > Done.

## Posponed/Removed Features.
---

* Open text file directly from view. (v3.0)  
    Could be nice (not necessary) when integrated with a nice text 
    editor and server synchronization.
 
* Change backend storage to a databse. (v3.0)  
    Could be nice (not necessary) with large number of text files.  

* Make daytime check based on alarm events, not tied to periodic updates. (v3.0)  
    Why? is it necessary? It's not! It works well. 
    Keep it Tied to the runnable update. You don't need it. KISS.

* Put app in f-droid (removed)  
    Their instructions are terribly outdated. Had to troubleshoot 
    all their commands and in the end it didn't work, spent a day 
    on this, let's not do it again.
    
