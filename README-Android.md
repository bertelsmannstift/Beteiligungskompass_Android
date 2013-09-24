Participationcompass Android -- A fully featured App for the Open Source Participationcompass web portal
========================================================================================================

## DESCRIPTION

Originally, the web platform "Beteiligungskompass.org" was initiated by the Bertelsmann Stiftung as a portal to help people in the public, private and not-for-profit sectors who need to involve a wider group of people in their work. The portal was created to provide information, advice, case studies and opportunities to share experiences with others helping to make public participation activities as effective as possible.

The portal is mainly aimed at people who are directly involved in planning, running or commissioning participation activities.

This is the Open Source release for the Android App that interacts with the portal's database and lets users browse through the platform's catalogue for offline use.

## SYSTEM REQUIREMENTS

* a recent version of Java and the Java SDK
* [Eclipse 3.6.2](www.eclipse.org)
* [Android Developer Tools ADT](http://developer.android.com/tools/sdk/eclipse-adt.html)

## DEVICE-COMPATIBILITY

* Android SDK API-Level 8 (Android 2.2)

## INSTALLATION/SETUP

### 1. Prerequisites

1. Open Eclipse with ADT installed
2. File --> Import
3. Choose "Android" and then "Existing Android Code into Workspace"
4. Choose the directory of the Open Source Beteiligungskompass Android App  

### 2. Settings

You will find all settings in the file `../service/API.java`

You will need the following parameters:

* `HOSTNAME`: This is the URL where your web portal is running.
* `API-KEY`: This key is needed to interface with the Web portal's API.
* `HTACCESS`: For access to a possible development environment where the web platform is running, a `username` and `password` is required.

### 3. Change the package name
The default package name is `com.your.name`. If you want to use an other one, you have to do the following steps:

* Change the package in your `AndroidManifest.xml`
* Refactor all files in your project with the new package name.

### 4. Generating the default Database

**VERY IMPORTANT:**

To get the current database, use the following API-Call:

* `HOSTNAME/api/export?api_key=API-KEY`

BEWARE! You have to change the sqlite file in a synced_database.mp3 file and then save that file in the res -> raw Folder.
The file beteligungskompass.sqlite has been renamed to synced_database.mp3 to trick the android packager into thinking it's already compressed.

### 4. Generating Assets

When you have successfully hooked up the connection to your web portal it is time to create initial Assets for the App with information from your web portal's database.

Right now the folder `/assets` contains only dummy data.
These have to be replaced with files containing the information from your web portal.

To get these information from your web portal use the `/api/` method in one of your favorite browsers.

You can use the following API-Calls:

* `HOSTNAME/api/get_thumbnails?api_key=API-KEY`

By the following calls you have to copy the result into the right file.

* `HOSTNAME/api/get_strings?api_key=API-KEY`
* `HOSTNAME/api/get_basic_config?api_key=API-KEY`
* `HOSTNAME/api/get_terms?api_key=API-KEY`

### Debug settings

To activate verbose logging, open the file **`src/com/your/name/Util.java`** and uncomment the following two methods:

* **logDebug**
* **logVerbose**


## FOLDER STRUCTURE
	
	PROJECTFOLDER
	   |--/assets
	   |--/libs
	   |--/res
	   |--/src/com
	     |--/googlecode/android
	     |--/YOUR/NAME
	       |--/activities
	       |--/dao
	       |--/fragments
	         |--/addArticle
	         |--/article
	         |--/dashboard
	       |--/service
	       |--/views
	

* **`/assets`**: The assets folder contains the initialize data for the first App install without any syncs.
	 
**IMPORTANT:** DON'T CHANGE THE NAMES OF THESE FILES!

* **`/assets/thumb.zip`**: Includes the images and videos from all articles.
* **`/assets/therms.html`**: Contains the Terms and Conditions.
* **`/assets/strings.json`**: This is a Localization file. The default is the German.
* **`/assets/about.html`**: This is a static page. Should be used for the "about" page on the website.
* **`/assets/basic_config.json`**: Defines the modules which are visible/active inside the app and the default sort parameter for the different article types.
	
* **`/libs`**: This folder contains the necessary libraries like ORM and the Android Support library.. 
* **`/res`**: This is the default Android folder containing the view definitions.
 
* **`/src/com`**: This folder contains the different packages that are used.

	* **`/googlecode/android`**: Contains different helpers e.g. a date picker.

	* **`/your/name`**: Contains the package contents

		* **`/activities`** contains all Activities / Tasks / Listeners and Adapters
			* Noteworthy files here are the `Splashscreen.java` and the `Sync.java`.
			* **`Splashscreen.java`** contains the **Importer Task**. This is called the very first time a user opens the app. The Importer takes all information from the folder **`/assets`** and initializes the database from the folder **`/res`**.
			* **`sync.java`** imports all data from the web platform through API calls and replaces them with the current data.
		* **`/dao`**: all items inside this folder are ORM objects. The folder **`/dao/helper`** contains classes which interact with the SQlite database. 
		* **`/fragments`**: contains all needed fragments
			* **`fragments/addArticle`**: contains all fragments for the **add article** view
			* **`fragments/article`**: contains all fragments for the **article detail** view
			* **`fragments/dashboard`**: contains all fragments for the **dashboard** view (Startpage)
		* **`/service`**: contains the API class. This class communicates with the website's API
		* **`/views`**: contains custom view implementation and view validation files