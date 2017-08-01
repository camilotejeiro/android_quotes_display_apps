**Note: These applications are not maintained.**

_Feel free to fork/clone this repository if you would like to implement new features/functionality to fit your purposes._

Quotes Display Applications
===========================

_A quote a day? That would be nice, wouldn't it?_

That's what we thought too, and that's why we created these apps. 

You can use these _open source_ apps to display personal/book quotes 
in your Android device. The quotes are read from text file(s) and 
displayed (one by one) in random order at periodic intervals (or on 
screen-tap). 

## Random Quotes App

Application to turn an Android device into a "picture frame" for 
displaying quotes.

* [Google Play Store Download Link](https://play.google.com/store/apps/details?id=org.osohm.randomquotesapp&hl=en)
* [More Details - Source Code](android_gradle/RandomQuotes/RandomQuotesApp)

## Random Quotes Widget

Application to display your quotes in a small widget in the Android device front screen.

* [Google Play Store Download Link](https://play.google.com/store/apps/details?id=org.osohm.randomquoteswidget&hl=en)
* [More Details - Source Code](android_gradle/RandomQuotes/RandomQuotesWidget)

## Documentation

For documentation, see the 
[user documentation](documentation/user_functional_specification.md).  

## Development

All the development was done using Gradle and the android tools 
directly from the command line (No bloated IDEs). It's actually pretty 
straightforward after you get the hold of it.

* [Get Android SDK](https://developer.android.com/studio/index.html) -- just the command line tools.
    - Scroll to the bottom of the page.
    - Avoid the bloated RAM consuming monster (Android Studio): 3GB RAM minimum, 8 GB recommended... No thanks.

* [Installing Gradle](https://gradle.org/install/): note these applications were built using **Gradle 2.10**.
    - Dependencies might differ for newer Gradle versions.

* [Gradle user docs](https://docs.gradle.org/current/userguide/userguide.html)

The Gradle build process mostly involves two commands:  

- Build Debug:  
    > gradle assembleDebug -- just the command line tools
- Build Release (signed apps with your own keys):  
    > gradle assembleRelease

Once the application builds you can use the [following bash scripts](android_gradle/bin) 
to install and debug your application.

If you need more, you can write your own little scripts, it's actually 
quite nice.

## License

These applications were created to meet our needs, we are sharing them 
under the assumption that they can provide some value to others, but 
with no warranties of any kind; without even the implied warranty of 
merchantability or fitness for any particular purpose. See the 
[GPLv3 license file](license.md) for more details. 
