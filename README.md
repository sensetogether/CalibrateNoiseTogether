# CalibrateNoiseTogether
Key Features 
------------
#CalibrateNoiseTogether is an Android application that supports the distributed and opportunistic calibration of the microphone embedded in -or connected to- a mobile phone.   
#CalibrateNoiseTogether is intended to operate in a fully decentralized way and thereby leverages the presence of the nearby phones 
that monitor the same noise level so as to perform a multi-party calibration while enhancing the overall accuracy gain. 
For this purpose, the mobile phones exchange measurements to determine a calibration function, which describes the relationship between the non-calibrated measurements
and the calibrated one(s) and thereby enables correcting the measurements of the non-calibrated phones(s). 
Once calibrated, the phone can, in turn, be used to calibrate others. Such a dynamic calibration, which is also known as multi-hop blind calibration, allows calibrating sensors under their deployment conditions and without involving the end-user. 

Getting started 
-----------------
Step 1 - Download or clone the source code of #CalibrateNoiseTogether.  
 
 Step 2 - Download the following java libraries:
*  [common Lang](https://commons.apache.org/proper/commons-lang/) (version 3.5 is preferred) 
*  [commons-math ](http://commons.apache.org/proper/commons-math/download_math.cgi)(version 3.6.1 is preferred)
*  [commons-net](https://commons.apache.org/proper/commons-net/)(version: 3.5 is preferred)
*  [Jama ](https://mvnrepository.com/artifact/gov.nist.math/jama/1.0.3)(version: 1.0.3 is preferred)

Place the jar files that just have been downloaded into the app/libs folder of the #CalibrateNoiseTogether.

Step 3 - Download[Android Studio](https://developer.android.com/studio)  
Start android Studio and open the #CalibrateNoiseTogether project by selecting the directory wherein is placed 
#CalibrateNoiseTogether. 
Before running #CalibrateNoiseTogether on a mobile phone using Android Studio, you need to set up the phone. 

Setting up the mobile phone: 
----------------------------------

Step 1 - Enable Wifi Direct 
#CalibrateNoiseTogether leverages Wifi Direct to support the discovery of the nearby phones that offer the calibration service. 
A prerequisite is to enable/switch on Wifi-direct, using the settings app. For this purpose, Wifi should be switched on. 
Then, Wifi Direct should also be enabled: usually, the configuration of Wifi Direct can be done using Wifi Properties.   

Step 2 - Change permissions 
#CalibrateNoiseTogether requires that permissions are granted to access: 
* Microphone,
* Storage,
* Location. 

In order to grant permission, open the Settings app and tap Apps or Application Manager (depending on your phone, this may look different).
Tap the #CalibrateNoiseTogether app and then, tap Permissions and turn all the permissions on.


Contributors
-------------

* Françoise Sailhan: design, architect, coding 
* Valérie Issarny: design 
* Yifan Du: design 
* Otto Tavares Nacimiento: design, architect, coding
