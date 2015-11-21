Quotes Display Apps User Requirements
=====================================
We will have two applications we should complete. We will develop them 
serially, i.e. first complete one, then the other one.
- Android random quotes widget.
- Android random quotes application.

The following are our required features, in order of necessity/priority.

### Version 1.1 required features. (widget only)
* Minimal Android Graddle workspace
* Widget application supports multiple instances.

### Version 1.2 required features. (widget only)
* Common Configuration screen(library), add support for multiple input fields.
* Add support for parsing multiple input fields and shuffling multiple text files.
* Display file source, line number and queue number in the app display screen.
* Barebones widget complete and functional

### Version 1.3 required features. (application only)
* Creation of quotes application (non-widget).
* Configure screen wakelocks and application sleep times (non-widget)
* Barebones application complete and functional.

### Version 1.4 features. (application only)
* Group common functionality into a shared android library.
* Setup gradle workspace to set dependencies.
* Widget utilizes shared library.
* application utilizes shared library.

### Version 1.5 features
* Add support for dynamic zoom with different text and window sizes. 

### Version 1.6 features
* Add a browsee button in the common configuration screen, utilize a file browser.
* Add support for modifying quotes, open text editor at exact line number.
* Add support for appending quotes, open text editor at the end of file.

----
### Wish-List of Features.
----

V2 of the display apps will have the following features (order of priority)
* 1 random text widget application wich can have multiple instances.
* 1 random text standard application which can have multiple instances.
* both applications have a common library dependency (library) that is maintained, 
    all that changes is the app vs widget frontend, differences should be 
    minimal.
* setup screen (common library)
    - Starts with a single input field.
    - user can type the text file paht.,
    - user can press the browsee button (which will bring a simple explorer). 
    - user can press the + button and add an additional input field will 
        show below.
* Display screen
    - Dynamically increasing or decreasing font to fit the size of the 
    application or widget container, such that small quotes are zoomed 
    and large quotes are zoomed out.
    - Source info, line number and queue.
    The widget or app display will have at the boundary of the screen, the 
    source file, the line number and the current quote over total.
    - Modify quote.
    a small button will be displayed to allow the user to modify the current 
    quote, e.g. to fix a grammar error, a spelling error etc. The app should 
    open the editor exactly at the specific line number.
    - Add new quote.
    A small button will be displayed. It will allow the user to select 
    among the loaded files and add a new quote at the end of the file.
* Text processing    
    - Files paragraph merge: all the paragraphs from all the files are aggregated, 
    randomized and displayed on tap and at periodic times, every quote has 
    a link to file kept in memory for easy editting. 
    - Given that explorer applications are so common, Open source applications 
    will be used to implement our file chooser dialog.
    - Given that text editor applications are so common, open source code 
    will be used to implement our text file editor.
