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
package com.example.colibris.comtool;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.location.LocationManager;

import android.os.Looper;

/**
 * This Netandgps class is used to obtain the position
 * of the devices and the time. These data can be provided by
 * the GPS or by the network operator.
 */
public class Netandgps {
    /**
     * poistion and time
     */
    private PositionTime positionTime;
    /**
     * location manager
     */
    android.location.LocationManager locationManager;
    /**
     * location listener
     */
    android.location.LocationListener locationListener;
    /**
     * context
     */
    private Context ctx;
    /**
     * log information
     */
    private static final String TAG = "NetandGPS"; // for debugging purpose
    /**
     * time listeners
     */
    private List<TimeListener> listeners = new ArrayList<TimeListener>();
    /**
     * handle for time listeners
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper()); //
    /**
     * Position call backs
     */
    private PositionSensorCallBack callBackPosition;
    /**
     * meet
     */
    public boolean meetGPS = false;


    /**
     * init
     * @param ctx context
     * @param callback call back
     * @param isMeet determine if there is a calibration meeting
     */
    public Netandgps(Context ctx, PositionSensorCallBack callback, boolean isMeet){
        this.ctx = ctx;
        this.meetGPS = isMeet;
        this.callBackPosition = callback;
    }
    //old version

    /**
     *
     */
    public void InitialiseTimeListener() {
        positionTime = new PositionTime();

        Log.e("Location", "Initialise location listener: " );
        locationManager = (android.location.LocationManager)
                ctx.getSystemService(android.content.Context.LOCATION_SERVICE);

        Log.e("Location", "Initialise location listener: " );
        android.location.LocationListener locationListener = new android.location.LocationListener() {

            public void onLocationChanged(android.location.Location location) {
                // calculate the offset between the time provided by the device and the one
                // provided by the GPS/Cellular network
                Date now = new Date();// get local time
                long offset =  now.getTime() - location.getTime() ;

                String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());

                if( location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
                    positionTime.GPSTime = location.getTime();
                    positionTime.localGPSTime = now.getTime();
                    positionTime.isGPSSynchronised = true;
                    positionTime.GPSOffset = offset;

                    android.util.Log.d("Location", "Time GPS: " + time); // This is what we want!
                }
                else {
                    android.util.Log.d("Location", "Time Device (" + location.getProvider() + "): " + time);
                    positionTime.isNetworkSynchronised = true;
                    positionTime.networkOffset = offset;
                    positionTime.LocalNetworkTime = now.getTime();
                    positionTime.NetworkTime = location.getTime();
                }

                timeandPositionChangedSendEvent(positionTime); //report to listeners   that a new time is established
            }

            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        android.util.Log.e("Location", "Request Location updates ");
        if(this.isNetworkEnabled()) { locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);}
        if(this.isNetworkEnabled()) {
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
        // todo Stop listening use: locationManager.removeUpdates(locationListener)
    }

    /**
     * init the gps
     */
    public void InitialiseGPSandNetworkTimeListener() {
        positionTime = new PositionTime();
        android.util.Log.e("Location", "Initialise location listener: " );
        locationManager = (android.location.LocationManager)
                ctx.getSystemService(android.content.Context.LOCATION_SERVICE);

        android.util.Log.e("Location", "Initialise location listener: " );
        final android.location.LocationListener locationListener = new android.location.LocationListener() {

            public void onLocationChanged(android.location.Location location) {
                // calculate the offset between the time provided by the device and the one
                // provided by the GPS/Cellular network

                Date now = new Date();// get local time
                long offset =  now.getTime() - location.getTime() ; // compute the time offset
                Log.e("Location", "Location changed offset: " + now.getTime() + "-"  + location.getTime() + "= " + offset);
                String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());

                if( location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
                    Log.e("Location", "Time GPS: " + time);
                    positionTime.isGPSSynchronised = true;
                    positionTime.GPSOffset = offset;
                    positionTime.LocalNetworkTime = now.getTime();
                    positionTime.localGPSTime = now.getTime();


                    positionTime.GPSlatitude = location.getLatitude();
                    positionTime.GPSlongitude = location.getLongitude();

                    positionTime.GPShasAltitude = location.hasAltitude();
                    if (positionTime.GPShasAltitude)
                        positionTime.GPSaltitude = location.getAltitude(); // Get the altitude if available, in meters above the WGS 84 reference ellipsoid.

                    positionTime.GPShasAccuracy = location.hasAccuracy();
                    if( positionTime.GPShasAccuracy)
                        positionTime.GPSAccuracy =  location.getAccuracy(); //horizontal accuracy = radius of 68% confidence. draw a circle centered at location (latitude+longitude)
                    // with a radius = accuracy, then there is a 68% probability that the true location is inside the circle.

                    positionTime.GPShasSpeed = location.hasSpeed();
                    if(positionTime.GPShasSpeed)
                        positionTime.GPSspeed = location.getSpeed();// / Get speed in meters/second over ground. If this location does not have a speed then return 0.0

                    positionTime.GPShasBearing = location.hasBearing();
                    if(positionTime.GPShasBearing)
                        positionTime.GPSbearing =location.getBearing();

                    if(meetGPS = true) {
                        callBackPosition.onSensorPositionChanged(positionTime.GPSlatitude, positionTime.GPSlongitude, positionTime.localGPSTime);
                    }

                }
                else {
                    Log.e("Location", "Time Device (" + location.getProvider() + "): " + time);
                    positionTime.isNetworkSynchronised = true;
                    positionTime.networkOffset = offset;
                    positionTime.LocalNetworkTime = now.getTime();
                    Log.e("Location", "Time Device (" + location.getProvider() + "): " + time);


                    positionTime.network_latitude = location.getLatitude();
                    positionTime.network_longitude = location.getLongitude();

                    positionTime.network_hasAltitude = location.hasAltitude();
                    if (positionTime.network_hasAltitude)
                        positionTime.network_altitude = location.getAltitude(); // Get the altitude if available, in meters above the WGS 84 reference ellipsoid.

                    positionTime.network_hasAccuracy = location.hasAccuracy();
                    if( positionTime.network_hasAccuracy)
                        positionTime.network_Accuracy =  location.getAccuracy(); //horizontal accuracy = radius of 68% confidence. draw a circle centered at location (latitude+longitude)
                    // with a radius = accuracy, then there is a 68% probability that the true location is inside the circle.

                    positionTime.network_hasSpeed = location.hasSpeed();
                    if(positionTime.network_hasSpeed)
                        positionTime.network_speed = location.getSpeed();// / Get speed in meters/second over ground. If this location does not have a speed then return 0.0

                    positionTime.network_hasBearing = location.hasBearing();
                    if(positionTime.network_hasBearing)
                        positionTime.network_bearing =location.getBearing();

                    if(meetGPS = true) {
                        callBackPosition.onSensorPositionChanged(positionTime.network_latitude, positionTime.network_longitude, positionTime.LocalNetworkTime);
                    }
                }
                timeandPositionChangedSendEvent(positionTime); //report to listeners the fact that a new time is established
            }

            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        android.util.Log.e("Location", "Request Location updates ");
        if(this.isNetworkEnabled()) { locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000,(float) 0.1, locationListener);}
        if(this.isGPSenabled()) {
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000,(float) 0.1, locationListener);
        }
        // todo To Stop listening use: locationManager.removeUpdates(locationListener)
    }


    /**
     * the device changed its position
     * @param timeposition position and time
     */
    private void timeandPositionChangedSendEvent(final PositionTime timeposition) {
        Log.d(TAG , "Time changed event :" );

        mainHandler.post(new Runnable() {
            public void run() {
                for (TimeListener listener : listeners) {
                    Log.d(TAG , "report to time listener " );
                    listener.someoneReportedTimeChange(timeposition);
                }
            }
        });
    }

    //todo deal with the removal of GPS/network updates

    /**
     * remove the movement listener
     */
    public void removeTimeListener(){
        locationManager.removeUpdates(locationListener);
    }

    /**
     * add a movement listener
     * @param toadd
     */
    public void addTimeAndPositionListener(TimeListener toadd){ listeners.add(toadd);}

    /**
     * remove time and movement listener
     * @param toremove
     */
    public void removeTimeandPositionListener(TimeListener toremove){
        listeners.remove(toremove);
    }



    /**
     * return if the GPS is available
     * @return if the GPS is available
     */
    public boolean isGPSenabled(){
        LocationManager locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
        // determine if the synchronisation can come from the GPS
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //

    /**
     * determine if we can get from the 3G operator some location/ time
     * @return determine if we can get from the 3G operator some location/ time
     */
    public boolean isNetworkEnabled(){
        LocationManager locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);

        // determine if the synchronisation can come from  from the cellular network
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * callback for position
     */
    public interface PositionSensorCallBack {
        void onSensorPositionChanged(double latitude, double longitude, long localGPStime);
    }


}