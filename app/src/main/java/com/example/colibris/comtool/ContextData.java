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

import android.widget.TextView;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Random;

/**
 * This ContextData class store context related information
 */
public class ContextData {

    /**
     * GPS data recorded
     */
    public static ArrayList<String> positionData = new ArrayList<String>();
    /**
     * index of position
     */
    public static int indexPosition = 0;
    /**
     *     Recorded Orientation data
     */
    public static ArrayList<String> orientationData = new ArrayList<String>();
    /**
     * accumulated error
     */
    public static DescriptiveStatistics accumulatedErrorData = new DescriptiveStatistics();

    /**
     * Weight distance Data
     */
    public static ArrayList<Double> filterWeight = new ArrayList<Double>();
    /**
     * Standard Deviation of the filterWeight
     */

    public static double filterSD = 0;
    /**
     * index for orientation
     */
     public static int indexOrientation = 0;

     /**
     * calibration test
     */
    public static int currentRecordTest = 0;

    /**
     * array with the parameters for the filtered case
     */
    public static double[] betaFiltered;
    /**
     * robust beta parameters
     */
    public static double[] betaRobustFiltered;
    /**
     * residual error for the robust case
     */
    public static double[] residualRobustFiltered;
    /**
     * residual error for the simple case
     */
    public static double[] residualFiltered;
    /**
     * fitlered sum
     */
    public static double sumFiltered = 0;
    /**
     * filtered sum for the robust case
     */
    public static double sumRobustFiltered = 0;
    //public static ArrayList<Double> rawBuffer = new ArrayList<Double>();
    /**
     * raw measurements
     */
    public static double[] rawBuffer;

    /**
     * show if the device is the server or the client for providing or receiving the time.
     *      This is import beacuse all the data here are based upon these measure of time
     */
    public String FLAG;

    /**
     * time offset as provided by ntp
     */
    public static long timeOffsetCtxData = 0 ; //

    /**
     * random number
     */
    private Random random = new Random();

    /**
     * return an array containing the various parameters that are given
     * @param startTime start time to record
     * @param size size
     * @param slot slot
     * @param isOrientation correspond to orientation
     * @param isRandom correspond ti random
     * @return array composed of the arguments
     */
    public ArrayList<String>  creatingArrs(Long startTime,int size, double slot, boolean isOrientation, boolean isRandom){

        ArrayList<String> arrs = new ArrayList<String>();
        Long timeis = startTime;
        for(int i = 0; i < size; i++){
            if(isOrientation) {
                arrs.add(i, timeis + "," + random.nextInt() + "," + random.nextInt() + "," + random.nextInt());
            }else{
                arrs.add(i, timeis + "," + random.nextInt() + "," + random.nextInt());
            }
            if(isRandom)
                timeis =(long)(timeis + slot *  (1 + Math.random()));
            else
                timeis = (long)(timeis + slot);
        }
        return arrs;
    }
    public static Long startBufferTime = (long) 10000;
    public static Long startOrtTime = (long) 3;
    public static Long startGPSTime = (long) 7;

}


