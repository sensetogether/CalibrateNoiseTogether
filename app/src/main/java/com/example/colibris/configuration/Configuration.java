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
package com.example.colibris.configuration;

import java.io.Serializable;



/**
 * The Configuration class contains  all the configuration parameters
 */
public class Configuration implements Serializable {

    /**
     * we are averaging the samples over a period of time
     */
    public static boolean isAveraging = true; //
    /**
     *duration of the record in millecond _> 1000 equal one sec
     */
    public static long recordDuration_ms =5000;  //

    /**
     *duration of the sample in millisecong 0.1=100ms //0.002;  //0.125 = 125 ms   0.001 = 1ms
     */
    public static double samplingDurationSec = 0.1; //
    /**
     * dtermine if we  are ziping the files related to calibration
     */
    public static boolean SoundFileAreZipped = false; //
    /**
     * name of the test to carry
     */
    public static String nameRecordTest = "-1";  /*Name of Record Tests to carry on*/
    /**
     *number of successive calibrations to performs
     */
    public static int numberofconsecutivecalibrationtocarry =1; //

    //
    /**
     *period of averaging is expressed in second
     */
    public static   int SHIFT_IN_SAMPLE = 2;  // number of samples removed at the beginning of the record  (please use a number that is divisble
    /**
     * chunk size
     */

    public static double chunksize_Sec =   recordDuration_ms /1000/4;

        /**
     *sleeping permits to wait a little bit before recording. This is expressed in ms (1000ms is equal to 1 second
     */

    public static final int SLEEPDURBEFORESCHEDULERECORD_MS = 15000;
    // sleep for 20 secondes
    // duration to wait during two consecutive tests
    /**
     * sleeping duraction during consecutive tests (expressed in second
     */

    public static long duration_beetween_test_SEC = 15; // is expressed in second

    /**/
    /**
     * Dtermine that the end used pressed the MultiHop option
     */

    public static boolean isMultiHopCalibration = false; // should we perform a multi hop calibration


    /**
     * determine if the device is calibrated
     */

    public static boolean IS_CALIBRATED  = false;
    /**
     * intercept that is set during the manual calibration (if any)
     */

    public static double MANUAL_INTERCEPT =0; //calibration intercept
    /**
     * slope that is set during the manual calibration (if any)
     */

    public static double MANUAL_SLOPE  =1; //calibration slope to apply
    /**
     * mean of the residual that is set during the manual calibration (if any)
     */

    public static double MANUAL_MEAN_RESIDUAL = 0;// these parameters are entered manually
    /**
     * standard deviation of the residuals that is set during the manual calibration (if any)
     */

    public static double MANUAL_STD_RESIUDAL = 0;// these parameters are entered manually
    /**
     * R squared that is set during the manual calibration (if any)
     */

    public static double MANUAL_R_SQUARED = 1;// these parameters are entered manually
    /**
     * sum of the residuals that is set during the manual calibration (if any)
     */

    public static double MANUAL_SUM_RESIDUAL   =0 ;// these parameters are entered manually

    /**
     * calibration slope that is automatically computed
     */

    public static double SLOPE = 0;// these parameters are entered manually
    /**
     *intercept that is automatically computed
     */

    public static double INTERCEPT  = 0;// these parameters are entered manually
    /**
     * weighted and accumulated error that is automatically computed
     */

    public static double WEIGHTED_CUMULATED_ERROR = 0;// these parameters are entered manually
    /**
     * cumulated error that is automatically computed
     */

    public static double CUMULATED_ERRORS  = 0;// these parameters are entered manually



    // do not touch, these paprameters are used
    /**
     * determine that the user wants to apply a simple regression
     */

    public static boolean SHOULD_SIMPLY_CALIBRATE = false;
    /**
     *determine that the user wants to apply a robust calibration
     */

    public static boolean SHOULD_ROBUSTLY_CALIBRATE = false;
    /**
     * calibration parameters should be saved (in the hypergraph)
     */

    public static boolean SHOULD_BE_SAVED = false;


    /*Called when not weighted sound for option record is pressed*/
    /**
     *
     */

    public static boolean isRawSoundData = false;

    //--------------------- different criteria (i.e. metrics) that can be used for multi hops calibration
    /**
     * metric to apply = standard error
     */

    public static final int STD_ERROR_CRITERIA = 1;
    /**
     *metric to apply = mean squarred error
     */

    public static final int MEAN_SQUARE_ERROR_CRITERIA = 2 ;
    /**
     * metric to apply = meeting duration
     */

    public static final int  MEETING_DURATION_CRITERIA= 3 ;
    /**
     * metric to apply = r squared
      */

    public static final int MEETING_RSQUARED = 4;

    /**
     * metric to apply = sum of the squared residuals
     */
    public static final int MEETING_RESIDUAL_SUM_OF_SQUARRE =5;

    // SELECT the metric used for multi hops calibration
    /**
     * metric to apply = mean of the squared error
     */
    public static int shortest_path_criteria =   MEAN_SQUARE_ERROR_CRITERIA;
    /**
     * when to start recording from now (is given in number of second)
     */
    public static final int SCHEDULE_TIME = 3; //
    //this is used to let devices join a group

    //determine the number of vertex in the hypergraph
    /**
     * number of vertex in the hypergraph
     */
    public   static int VERTEXNB = 100; // max number of vertex that can be added to the graph
    /**
     * number of references in the references in the hypergraph
     */
    public   static int REFERENCENB = 3; // number of reference nodes
    /**
     * number of node in the hypergraph
     */
    public static int HYPERGRAPH_SIZE = VERTEXNB + REFERENCENB  +1 ;/*one for the consolidated node*/


    /**
     * determine that the end user requires location aware calibration
     */
    public static boolean isLocationAwareCalibration = false;
     /**
     * determine that the end user requires linear regression
     */
    public static boolean isFilteredData2LinearRegression = false;
    /**
     * determine that the end user requires Geographically Regression
     */
    public static boolean isFilteredData2GeoRegression = false;

    /**
     *determine that the end user requires Weighting Function (based on distance) is Logarithmic and 0 when this function is chosen to be exponential
     */
    public static int weightingFunction = 0;
     /**
     * determines that the end user requires to filter the data (based on distance)
     */
    public static boolean isNotFilteringDistanceIsVariable = false;
     /**
     *determines that the end user requires  to fitler the data to regress in the linear case
     */
    public static boolean isNotFilteredMatrix = false;


     /**
     * determines if the device is recording
     */
    public static boolean isRecordActivity = false;

     /**
     * regression has been halted
     */
    public static boolean stopRegressionEmptyFiles = false;

    /**
     * DO NOT UPDATE ANY OF THE VARIABLE BELLOW
     */

    /**
     * time offset with the AP
     */
    public static int   offset ;



    // TXT RECORD properties
    /**
     * message type = handle
     */
    public static final int MY_BROADCAST_HANDLE = 0x100 + 0;//
    /**
     *message type =  tcp traffic
     */
    public static final int MY_TCP_HANDLE = 0x100 + 1;//
    /**
     * message type =  file transmited
     */
    public static final int FILE_READ = 0x100 + 2;//


    /**
     * message type =
     */
    public final static String DEVICE_ID_MESSAGE = "device_id";
    /**
     *message type =
     */
    public final static String VERTEX_MESSAGE = "vertex_id";
    /**
     *message type =
     */
    public final static String FILE_MESSAGE = "file";

    /**
     * time that takes place a very long time ago
     */
    public static final long VERY_LONG_TIME_AGO  = 100000000000L;
    /**
     * ip adress the the AP with wifi direct
     */
    public static final String calibration_leader = "192.168.49.1"; // IP adress of the WifiDirect access point
    /**
     * name of the file that stores the connexion graph
     */
    public final static String connectFileName = "local_connection_graphs.txt"; // name of the file that stores the connexion graph

    /**
     *  port we are listening to send sound and so one during a meeting
     */
    public static  int SERVER_PORT = 4545; //
    /**
     * ntp port
     */
    public static  int ntp_ports = 4002;
    /**
     *  port we are listening to send sound and so one during a meeting when we broadcast
     */
    public static  int BROADCAST_SERVER_PORT = 4546;
    /**
     * name of the calibration service
     */
    public static final String SERVICE_INSTANCE = "calibration service";
    /**
     * service
     */
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    /**
     * prefix of the file used to store the sound recorded locally
     */
    public static final String LOCAL_FILE_PREFIX = "local_noise"; // file prefix used to store  the sound recorded by the device itself
    /**
     * prefix of the file used to store the sound recorded by a remote device
     */
    public static final String REMOTE_FILE_PREFIX = "remote_noise";
    /**
     * prefix of the file used to store the averaged sound recorded locally
     */
    public static final String AVG_LOCAL_FILE_PREFIX = "avg_local_noise";
    /**
     * prefix of the file used to store the averaged sound recorded locally
     */
    public static final String AVG_REMOTE_FILE_PREFIX = "avg_remote_noise";



    /**
     * option that state that robust regression is applied
     */
    public static boolean APPLY_ROBUST_REGRESSION_IN_HYPERGRAPH = false;



    /* Message types and delimiters of the messages*/
    /**
     * message delimited
     */
    public static final String MSG_DELIMIT = "*";
    /**
     * delimited of the end of a file
     */
    public static final String MSG_FILE_END = "+";
    /**
     * message that identifies a device
     */
    public static final String MSG_ID = MSG_DELIMIT + "ID=";
    /**
     * message that is used to send a connexion graph
     */
    public static final String MSG_CONNEXION = MSG_DELIMIT + "CONNEXION=";
    /**
     *message used to send the sound
     */
    public static final String MSG_RECORD = MSG_DELIMIT + "RECORD=";
    /**
     * message include time related parameters
     */
    public static final String MSG_TIME = MSG_DELIMIT + "TIME=";
    /**
     * message including the MD5 of a file
     */
    public static final String MSG_MD5 = "MD5=";
    /**
     * testing message
     */
    public static final String MSG_TEST = "TEST=";

    /**
     * it seems that the network provided by wifidirect is 192.168.49.0
     */
    public static String broadcast_addr = "192.168.49.255"; //
    /**
     *it seems that the  prefix of the network provided by wifidirect is 192.168.49
     */
    public static String prefix = "192.168.49";


 /* CROSSCALL
    public static double INTERCEPT = 8.26816568;
    public static double SLOPE = 1.26845418;
    */
    /* NEXUS Red
    public static double INTERCEPT = 17.77684948;
    public static double SLOPE = 0.66158333;
    */
    /* NEXUS Yellow
    public static double INTERCEPT = 16.48970672;
    public static double SLOPE = 0.73145984;
    */

}




