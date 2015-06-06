# Introduction #

This is a howto on getting the sofurry android app project running in eclipse.


# Details #

Prerequisites:
  * Latest version of Eclipse
  * Latest Android SDK with 1.6, 2.1, 2.2 and 2.3 support
  * Android SDK set up and running in eclipse.
  * Subversive SVN plugin for Eclipse (Add http://community.polarion.com/projects/subversive/download/eclipse/2.0/update-site/ to sources and install the SVN Connector and dependencies with subversion 1.6 compatibility)


First of all, go to http://code.google.com/p/sofurry-android-app/source/checkout and note the svn checkout path: https://sofurry-android-app.googlecode.com/svn/trunk/
Below that you can see a link that says "googlecode.com password". Click it, and note your SVN password. These two bits of information are what you need to get started.

Start Eclipse, right click in the package explorer and select "Import". A window will appear, in which you should select "SVN" and "Project from SVN":

<img src='http://hoxdna.org/svn/01.png'>

Click on "Create new repository location":<br>
<br>
<img src='http://hoxdna.org/svn/02.png'>

Enter the SVN path, username and password as follows. Do check "remember password", and note that the username does not contain "@gmail.com":<br>
<br>
<img src='http://hoxdna.org/svn/03.png'>

If you get a certicate warning, select "Trust Always":<br>
<br>
<img src='http://hoxdna.org/svn/04.png'>

On the next window append "/trunk" to the end so it looks like this. You will want to have "Head Revision" selected as shown.<br>
<br>
<img src='http://hoxdna.org/svn/05.png'>

On the next window, select "Check out as project configured using New Project Wizard"<br>
<br>
<img src='http://hoxdna.org/svn/06.png'>

On the next dialog enter the following: "SoFurry Android App" as project name, select "Create new project in workspace", select "Android 1.6" as target, Application name is "SoFurry Mobile", and package name is "com.sofurry". Uncheck "Create Activity"!<br>
<br>
<img src='http://hoxdna.org/svn/07.png'>

When asked to create a test project don't do anything but just press "Finish".<br>
<br>
<br>
Eclipse will now download the source and create the project. But there's one last step you have to do before you can start coding:<br>
<br>
Right click on "SoFurry Android App" in the package explorer, and select "Team" -> "Revert". Select all entries (they should be selected by default) and press OK. After this the project should work without any errors.<br>
<br>
<br>
To run, select "Run" from the menu bar, then "Run Configurations..." and create a run configuration for this android application. You may need to set up an emulator device (also within this window) or connect your phone to the USB cable.