/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.colibris.ui;

 import com.example.colibris.R;
 import com.example.colibris.comtool.ContextData;
 import com.example.colibris.comtool.Netandgps;
 import com.example.colibris.comtool.Orientation;
 import com.example.colibris.comtool.PositionTime;
 import com.example.colibris.comtool.SleepListener;
 import com.example.colibris.comtool.TimeListener;
 import com.example.colibris.configuration.Configuration;
 import com.example.colibris.configuration.Me;
 import com.example.colibris.multi.Calibration;


 import android.Manifest;
import android.content.Intent;
 import java.text.SimpleDateFormat;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
//import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;

import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * The MainActivity class is an activity that is displayed to the end user.
 * In particular, it contains various buttons (e.g., manual calibration, automatic calibration,
 * optiosn, record, cruve) that can be selected by the end user to perform a calibration,
 * reset the calibration system, record sound or parametrise the application
 */
public class MainActivity extends AppCompatActivity implements TimeListener, SleepListener, Orientation.SensorCallBack, Netandgps.PositionSensorCallBack {
    /**
     * message used to identify a device
     */
    public final static String DEVICE_ID_MESSAGE = "device_id";
    /**
     * vertex id
     */
    public final static String VERTEX_MESSAGE = "vertex_id";
    /**
     *
     */
    public final static String FILE_MESSAGE = "file";
    /**
     * log related information
     */
    private static final String TAG = "Main";
    /**
     * number format
     */
    private static final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");
    /**
     *  GPS related text view
     */
    EditText warningGPSText;// warning : there is not gps
    /**
     * 3G related information
     */
    EditText warningNetText; // warning : there is no (wireless or 3G network)
    /**
     * local device
     */
    Me me;
    /**
     * message
     */
    public final static String ME_MESSAGE = "me";
    /**
     * text view displaying gps related information
     */
    private TextView t; // to display the gps calibration
    /**
     * progress bar
     */
    private ProgressBar p;
    /**
     * other gps information
     */
    private TextView gps_second_line_Text ;
    /**
     * gps text view
     */
    private TextView gpsText ;
    /**
     * orientation text view
     */
    private TextView orientationText ;
    //for sensors data how orientation, position and acelerometer
    /**
     * date related to context
     */
    public ContextData ctxData = new ContextData();
    /**
     *  is used to figure out how many  orientations have been loged so far
     */
    private int indexOrientation;
    /**
     * is used to figure out how many (GPS) position have been loged so far
     */
    private int indexPosition; //

    public Orientation testOrientation;


    // for localisation purpose
    /**
     * handler for localisation purpose
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * sleep listener
     */
    private List<SleepListener> listeners = new ArrayList<SleepListener>();
    /**
     * sleeping thread
     */
    private Thread _sleepingThread; // thread to save the sound into a file
    /**
     * permission (related to the manifest file)
     */
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    /**
     * permissions that are requested
     */
    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    /**
     * check if the permission are granted
     * @return true if the persmission are granted
     */
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }


    /**
     * activity creation
     * @param savedInstanceState instance state
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        // check if permissions are granted
/*        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted to record audio
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                // Request permission
                int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
*/









        testOrientation = new Orientation(this, this);
        warningGPSText = findViewById(R.id.gpswarning);
        warningNetText = findViewById(R.id.netwarning);

        t=new TextView(this); //get text view determining if the device is synchronised
        t= findViewById(R.id.text_message);

        p = new ProgressBar(this);
        p = findViewById(R.id.progress_message);
        p.setVisibility(View.INVISIBLE);// progress bar becomes invisible

        gpsText = findViewById(R.id.gps_text);
        gps_second_line_Text= findViewById(R.id.gpsnext_line_text);
        orientationText = findViewById(R.id.orientation_text_message);

////////// display some information related to GPS and 3G
        Netandgps netandgps = new Netandgps(this, this, false);
        //Netandgps netandgpsMeet = new Netandgps(this, this);

        // determine if GPS or 3G network is enabled
        if (!netandgps.isGPSenabled()){// if there is no GPS
            warningGPSText.setTextColor(getResources().getColor(R.color.colorAccent));
            //   warningGPSText.setTextColor(Color.parseColor(R.color.colorAccent));
            warningGPSText.setText("No GPS");
        }else{
            warningGPSText.setTextColor(Color.parseColor("black"));
            warningGPSText.setText("GPS enabled");
        }

        if (!netandgps.isNetworkEnabled()){// if there is no GPS
            warningGPSText.setTextColor(getResources().getColor(R.color.colorAccent));
//            warningNetText.setTextColor(Color.parseColor("red"));
            warningNetText.setText("No 3G");
        }else{
            warningGPSText.setTextColor(Color.parseColor("black"));
            warningNetText.setText("3G enabled");
        }

        gps_second_line_Text.setTextColor(Color.parseColor("black"));

        // display the time provided by GPS and network
        netandgps.addTimeAndPositionListener(this);
        netandgps.InitialiseGPSandNetworkTimeListener();

        Log.e(TAG, "BEST CALIBRATION IS HERE");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        // compute the calibration parameters


        me = new Me(this);
        me.getLocalconnectionGraph();
        Calibration multiHopCalibration = me.getMultiHopCalibration();
        me.getSingleHopCalibration();//determine if the device is calibrated or not

        /*
         apply the parameters
         */

        if(multiHopCalibration.slope ==1 && multiHopCalibration.intercept == 0 && multiHopCalibration.cumulated_errror ==0){//
            Log.e(TAG, "the local device is not calibrated");
            Configuration.IS_CALIBRATED = false;
        }else{
            Calibration singleHopCalibration = me.getSingleHopCalibration();
             Configuration.IS_CALIBRATED = true;
             Configuration.SLOPE = singleHopCalibration.slope;
             Configuration.INTERCEPT = singleHopCalibration.intercept;
             Configuration.CUMULATED_ERRORS = multiHopCalibration.cumulated_errror;
             Configuration.WEIGHTED_CUMULATED_ERROR = multiHopCalibration.weighted_cumulated_error;
             Log.e(TAG, "the local device is calibrated \n slope: " + Configuration.SLOPE + " intercept " + Configuration.INTERCEPT
                     + " error:" + Configuration.CUMULATED_ERRORS + "weighted error" + Configuration.WEIGHTED_CUMULATED_ERROR);

        }






    }

    /**
     * Activity resume
     */
    @Override
    public void onResume(){
        super.onResume();
        testOrientation.start();

        Calibration multiHopCalibration = me.getMultiHopCalibration();

        /*
         apply the parameters
         */

        if(multiHopCalibration.slope ==1 && multiHopCalibration.intercept == 0 && multiHopCalibration.cumulated_errror ==0){
             Log.e(TAG, "the local device is not calibrated");
            Configuration.IS_CALIBRATED = false;
         }else{
             Calibration singleHopCalibration = me.getSingleHopCalibration();
             Configuration.IS_CALIBRATED = true;
             Configuration.SLOPE = singleHopCalibration.slope;
             Configuration.INTERCEPT = singleHopCalibration.intercept;
             Configuration.CUMULATED_ERRORS = multiHopCalibration.cumulated_errror;
             Configuration.WEIGHTED_CUMULATED_ERROR = multiHopCalibration.weighted_cumulated_error;
             Log.e(TAG, "the local device is calibrated \n slope: " + Configuration.SLOPE + " intercept " + Configuration.INTERCEPT
                     + " error: " + Configuration.CUMULATED_ERRORS + " weighted error " + Configuration.WEIGHTED_CUMULATED_ERROR);

        }






    }

    /**
     * Pause
     */
    @Override
    public void onPause(){
        super.onPause();
        testOrientation.stop();
    }







    /**
     * print information provided by ntp
     * @param info information provided by ntp
     */
    public static void processResponse(TimeInfo info)
    {
        NtpV3Packet message = info.getMessage();
        int stratum = message.getStratum();
        String refType;
        if (stratum <= 0) {
            refType = "(Unspecified or Unavailable)";
        } else if (stratum == 1) {
            refType = "(Primary Reference; e.g., GPS)"; // GPS, radio clock, etc.
        } else {
            refType = "(Secondary Reference; e.g. via NTP or SNTP)";
        }
        // stratum should be 0..15...
        Log.e(TAG," Stratum: " + stratum + " " + refType);
        int version = message.getVersion();
        int li = message.getLeapIndicator();
        Log.e(TAG," leap=" + li + ", version="
                + version + ", precision=" + message.getPrecision());

        Log.e(TAG," mode: " + message.getModeName() + " (" + message.getMode() + ")");
        int poll = message.getPoll();
        // poll value typically btwn MINPOLL (4) and MAXPOLL (14)
        Log.e(TAG," poll: " + (poll <= 0 ? 1 : (int) Math.pow(2, poll))
                + " seconds" + " (2 ** " + poll + ")");
        double disp = message.getRootDispersionInMillisDouble();
        Log.e(TAG," rootdelay=" + numberFormat.format(message.getRootDelayInMillisDouble())
                + ", rootdispersion(ms): " + numberFormat.format(disp));

        int refId = message.getReferenceId();
        String refAddr = NtpUtils.getHostAddress(refId);
        String refName = null;
        if (refId != 0) {
            if (refAddr.equals("127.127.1.0")) {
                refName = "LOCAL"; // This is the ref address for the Local Clock
            } else if (stratum >= 2) {
                // If reference id has 127.127 prefix then it uses its own reference clock
                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
                if (!refAddr.startsWith("127.127")) {
                    try {
                        InetAddress addr = InetAddress.getByName(refAddr);
                        String name = addr.getHostName();
                        if (name != null && !name.equals(refAddr)) {
                            refName = name;
                        }
                    } catch (UnknownHostException e) {
                        // some stratum-2 servers sync to ref clock device but fudge stratum level higher... (e.g. 2)
                        // ref not valid host maybe it's a reference clock name?
                        // otherwise just show the ref IP address.
                        refName = NtpUtils.getReferenceClock(message);
                    }
                }
            } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
                refName = NtpUtils.getReferenceClock(message);
                // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
            }
            // otherwise give up on naming the beast...
        }
        if (refName != null && refName.length() > 1) {
            refAddr += " (" + refName + ")";
        }
        Log.e(TAG," Reference Identifier:\t" + refAddr);

        TimeStamp refNtpTime = message.getReferenceTimeStamp();
        Log.e(TAG," Reference Timestamp:\t"  + "  " + refNtpTime.toDateString());

        // Originate Time is time request sent by client (t1)
        TimeStamp origNtpTime = message.getOriginateTimeStamp();
        Log.e(TAG," Originate Timestamp:\t"  + "  " + origNtpTime.toDateString());

        long destTime = info.getReturnTime();
        // Receive Time is time request received by server (t2)
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        Log.e(TAG," Receive Timestamp:\t"  + "  " + rcvNtpTime.toDateString());

        // Transmit time is time reply sent by server (t3)
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        Log.e(TAG," Transmit Timestamp:\t" + xmitNtpTime + "  " + xmitNtpTime.toDateString());

        // Destination time is time reply received by client (t4)
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
        Log.e(TAG," Destination Timestamp:\t" + destNtpTime + "  " + destNtpTime.toDateString());


        info.computeDetails(); // compute offset/delay if not already done
        Long offsetValue = info.getOffset();
        Long delayValue = info.getDelay();
        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

        Log.e(TAG," Roundtrip delay(ms)=" + delay
                + ", clock offset(ms)=" + offset); // offset in ms
    }

    /**
     * click on the button to carry manual calibration
     * @param view view
     */
    public void manualMessage(View view){
        Intent intent = new Intent(this,    ManualCalibrationActivity.class);
        startActivity(intent);
    }

    /**
     * click on the button to get configuration related information and reset the device if needed
     * @param view view
     */
    public void initMessage(View view) {
        //send the time that has been obtained)
        Intent intent = new Intent(this,    InitActivity.class);
        startActivity(intent);
    }

    /**
     * click on the button to perform an automatic calibration
     * @param view view
     */
    public void discoMessage(View view) {
        //send the time that has been obtained by GPS or by the network
        Intent intent = new Intent(this, WiFiServiceDiscoveryActivity.class);
        startActivity(intent);
    }

    /**
     * click on the button to plot information
     * @param view view
     */
    public void fileMessage(View view) {
        Intent intent = new Intent(this, FileActivity.class);
        startActivity(intent);
    }

    /**
     * click on the button to start recording noise
     * @param view view
     */
    public void record_Message(View view) {
        Intent intent = new Intent(this, RecordMessageActivity.class);
        startActivity(intent);
    }

    /**
     * sleep during the amount of milli seconds that is provided
     * @param millisec amount of milli seconds to sleep for
     */

    public void sleepFor(long millisec){ // 5000 = 5 sec
        Log.d(TAG, "schedule the ntp time \n");
        Date now = new Date();// get local time
        long now_long = now.getTime() + millisec;
        SystemClock.sleep(millisec);
        sleepChangedSendEvent();
    }

    /**
     * time to wake up
     */
    private void sleepChangedSendEvent() {
        Log.d(TAG , "wake up event :" );

        mainHandler.post(new Runnable() {
            public void run() {
                for (SleepListener listener : listeners) {
                    Log.d(TAG , "report to listener " );
                    listener.someoneReportedAwake();
                }
            }
        });
    }

    /**
     * add a sleep listener
     * @param toadd
     */
    public void addSleepListener(SleepListener toadd){ listeners.add(  toadd);}

    /**
     * remove the sleep listener
     * @param toremove
     */
    public void removeSleepListener(SleepListener toremove){
        listeners.remove(toremove);
    }

    /**
     * start a sleeping thread
     * @return if the thread could be create
     */
    private boolean sleepingThreadAlive() {
        return (_sleepingThread != null && _sleepingThread.isAlive());
    }

    /**
     * create and start a sleeping thread
     * @param millisec sleep period
     */
    public synchronized void sleepingThread(final long millisec ) {

        Log.d(TAG , "launching sleeping thread");
        if (!sleepingThreadAlive()) {
            //Create a new thread only if it does not exist or has been terminated.
            _sleepingThread = new Thread() {
                @Override
                public void run() {
                    Log.d(TAG , "sleeping thread launched");
                    sleepFor( millisec);
                }
            };
            _sleepingThread.start();
        }else{
            Log.d(TAG , "cannot launch a sleeping thread ");
        }
    }

    /**
     * time to wake up
     */
    @Override
    public void someoneReportedAwake() {

    }


    /**
     * the orientation of the sensor changed
     * @param axisX x
     * @param axisY y
     * @param axisZ z
     * @param timestamp time
     */
    @Override
    public void onSensorOrientationChanged(float axisX, float axisY, float axisZ, long timestamp) {
        indexOrientation += 1;
        long offset = timestamp ;
        if(offset != 0) {
            orientationText.setTextColor(Color.parseColor("black"));
            orientationText.setText("azimuth: " + axisX + " pitch: " + axisY + " roll: " + axisZ);
        }
        else {
            orientationText.setTextColor(getResources().getColor(R.color.colorAccent));
            orientationText.setText("azimuth: " + 0 + " pitch: " + 0 + " roll: " + 0);
        }
    }

    /**
     * the orientation of the sensor has changed
     * @param latitude latitude
     * @param longitude longitude
     * @param localGPStime time
     */
    @Override
    public void onSensorPositionChanged(double latitude, double longitude, long localGPStime) {
        Log.e(TAG, "get position from  GPS or network");
        //display local time
        long timeis = me.getLocalTimeShifterALongTimeAgo();

        ContextData.positionData.add(ContextData.indexPosition, timeis + "," + latitude + "," + longitude);
        ContextData.indexPosition = ContextData.indexPosition + 1;
        Log.e(TAG, "Size of Position File: " + ContextData.positionData.size());
        Log.d(TAG, "latitude: "+ latitude  + "longitude: " + longitude );
    }

    /**
     * click on the option button
     * @param view
     */
    public void optionMessage(View view) {
        //send the time that has been obtained by GPS or by the network
        Intent intent = new Intent(this, OptionActivity.class);
        startActivity(intent);
    }

    /**
     * position changed
     * @param timeposition
     */
    @Override
    public void someoneReportedTimeChange(PositionTime timeposition) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());

        // display local time
        gpsText.setText(" " + sdf.format(new Date()));
        gps_second_line_Text.setText("                 ");


        if (timeposition.isGPSSynchronised ) {

            gpsText.append(" gps latitude: " + timeposition.GPSlatitude  + " longitude: " + timeposition.GPSlongitude );// System.getProperty(\"line.separator\");");// + System.getProperty("line.separator"));
            if (timeposition.GPShasAltitude)                gps_second_line_Text.setText(timeposition.GPSaltitude+"m");
//            if(timeposition.GPShasBearing)//              gps_second_line_Text.append(timeposition.GPSbearing + "rad\n");
            if(timeposition.GPShasAccuracy  )
                gps_second_line_Text.setText(" accuracy: " + timeposition.GPSAccuracy +"%");

            if(timeposition.GPShasSpeed)
                gps_second_line_Text.append(" speed: " + timeposition.GPSspeed +"m/s");

            if(timeposition.GPShasAccuracy && timeposition.GPShasSpeed )
                gps_second_line_Text.setText(" accuracy: " + timeposition.GPSAccuracy +"%" + " speed: " + timeposition.GPSspeed +"m/s");

        }
        if ( timeposition.isNetworkSynchronised) {
            gpsText.append(" latitude: " +  timeposition.network_latitude + " longitude: " + timeposition.network_longitude ); //+ System.getProperty("line.separator"));

            if (timeposition.network_hasAltitude)
                gps_second_line_Text.append(" altitude: " + timeposition.network_altitude+"m");


            if(timeposition.GPShasAccuracy  )
                gps_second_line_Text.setText(" accuracy: " + timeposition.GPSAccuracy +"%");

            if(timeposition.GPShasSpeed)
                gps_second_line_Text.append(" speed: " + timeposition.GPSspeed +"m/s");

            if(timeposition.GPShasAccuracy && timeposition.GPShasSpeed )
                gps_second_line_Text.setText(" accuracy: " + timeposition.GPSAccuracy +"%" + " speed: " + timeposition.GPSspeed +"m/s");




        }
        p.setVisibility(View.INVISIBLE);// progress bar becomes invisible
    }
}
