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


import java.io.Serializable;

/**
 * GPS/positioning related parameters
 */
public class PositionTime implements Serializable {

    //location related infrmation
    /**
     * latitude in degrees
     */
    public double GPSlatitude ;
    /**
     * longitude in degrees
     */
    public double GPSlongitude;
    /**
     * GPS bearing is enabled or not
     */
    public boolean GPShasBearing;
    /**
     * horizontal direction of travel of this device (is not related to the device orientation). Is in the range (0.0, 360.0]
     */
    public float GPSbearing;
    /**
     * determine if Altitude is provided
     */
    public boolean GPShasAltitude;
    /**
     * Altitude in meters  above the WGS 84 reference ellipsoid.
     */
    public double GPSaltitude;
    /**
     * determine if accuracy is provided
     */
    public boolean GPShasAccuracy;
    /**
     * // with a radius = accuracy, then there is a 68% probability that the true location is inside the circle.
     *
     */
    public float GPSAccuracy;
    /**
     * determine if speed is supplied
     */
    public boolean GPShasSpeed;
    /**
     * Speed
     */
    public float GPSspeed; // speed if  available, in meters/second over ground. If this location does not have a speed then 0.0 is returned.

    /**
     * latitude provided by the 3G network
     */
    public double network_latitude ;
    /**
     * longitude provided by the 3G network
     */
    public double network_longitude;
    /**
     * determine if the 3G network has bearing
     */
    public boolean network_hasBearing;
    /**
     * horizontal direction of travel of this device (is not related to the device orientation). Is in the range (0.0, 360.0]
     */
    public float network_bearing;
    /**
     * determine if the 3G network provided altitude
     */
    public boolean network_hasAltitude;
    /**
     * Altitude as provided by3G
     */
    public double network_altitude; //in meters above the WGS 84 reference ellipsoid.
    /**
     *  determine if the 3G network provides accuracy
     */
    public boolean network_hasAccuracy;
    /**
     * with a radius = accuracy, then there is a 68% probability that the true location is inside the circle.
     *
     */
    public float network_Accuracy;
    /**
     * determine if the network supplied speed
     */
    public boolean network_hasSpeed;
    /**
     * speed
     */
    public float network_speed; // speed if  available, in meters/second over ground. If this location does not have a speed then 0.0 is returned.


    /**
     * time offset provided by the gps
     */
    public long GPSOffset = (long) Double.POSITIVE_INFINITY;
    /**
     * network offset
     */
    public long networkOffset = (long) Double.POSITIVE_INFINITY;
    /**
     * determine if we synchronised using gps
     */
    public boolean isGPSSynchronised = false;
    /**
     * determine if we synchronise using network
     */
    public boolean isNetworkSynchronised = false;
    /**
     * time provided by GPS
     */
    public long GPSTime = (long) Double.POSITIVE_INFINITY;
    /**
     * local time (as provided by gps
     */
    public long localGPSTime = (long) Double.POSITIVE_INFINITY;
    /**
     * time provided by 3G
     */
    public long NetworkTime = (long) Double.POSITIVE_INFINITY;
    /**
     * local time as provided by 3G
     */
    public long LocalNetworkTime = (long) Double.POSITIVE_INFINITY;

}