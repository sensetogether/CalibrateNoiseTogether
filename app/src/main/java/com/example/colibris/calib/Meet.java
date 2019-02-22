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
package com.example.colibris.calib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;


import com.example.colibris.calib.sound.NoiseListener;
import com.example.colibris.calib.sound.Record;
import com.example.colibris.comtool.ContextData;
import com.example.colibris.comtool.Netandgps;
import com.example.colibris.comtool.Orientation;
import com.example.colibris.comtool.SleepListener;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.configuration.Me;
import com.example.colibris.multi.Calibration;
import com.example.colibris.multi.graph.DirectedEdge;
import com.example.colibris.multi.hypergraph.HyperConnection;
import com.example.colibris.multi.hypergraph.HyperEdge;
import com.example.colibris.multi.hypergraph.ShortestHyperPath;
import com.example.colibris.nsd.ProtocolState;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.colibris.calib.FileManager.SAVING_SOUND_HAS_COMPLETED;
import static com.example.colibris.calib.sound.Record.HAS_CANCELLED;
import static com.example.colibris.calib.sound.Record.HAS_COMPLETED;
import static com.example.colibris.calib.sound.Record.STILL_ADDING;

/**
 *
 * This class orchestrate the activities that need to be performed during a meeting
 * of several smartphones. this includes the following activities :
 *      find  devices to calibrate with
 *      exchange connection graphs
 *       merge local and remote connection graphs - on merge complete echange sound
 *       schedule the recording - send or receive when to record
 *       get the local sound that has been recorded (Thread) - on complete
 *       save the local sound in a file - on save completed :
 *       send the local sound (Thread) - on complete
 *     /  receive sound from remote device(s) - on receive completed
 *
 *      calibrate, save regression into the connection graph, find best regression (shortest path)
 *     calibrate again the phone with regards to the selected best device
 *
 *
 */
public class Meet /*extends Handler */ implements NoiseListener, FileListener,  Orientation.SensorCallBack, Netandgps.PositionSensorCallBack {


    /**
     * file name
     */
    public String nRecordTest;
    /**
     * round trip delay as provided by ntp
     */
     public  long roundTripDelay =0; //
    /**
     * determine if the device is synchronised
     */
    public  boolean isSynchronised =false;
    /**
     * time offset between the device and the ap
     */
    private double timeOffset =0; // time offset as established ?
    /**
     * log related information
     */
    private static final String LOG_TAG = "Meet";
    /**
     * context
     */
    private Context ctx;
    /**
     * context data
     */
    public ContextData ctxData = new ContextData();

    /**
     * thread used to get the local recording */
    private Thread _sleepingThread; // thread to save the sound into a file
    /**
     * listeners
     */
    private List<SleepListener> listeners = new ArrayList<SleepListener>();
    /**
     *  Handlers to the events to lead to the UI thread to the listener.
     *
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper()); // sleep event handler
    /**
     * when to start recording
     */
    private Date recordSchedule ;// when to start recording
    /**
     * sound measurements
     */
    public Record measurement;
    /**
     * orientation
     */
    public ArrayList<String> orientation = new ArrayList<String>();
    /**
     * position
     */
    public ArrayList<String> position = new ArrayList<String>();
    /**
     * time
     */
    private long orientationTime;
    /**
     * time
     */
    private long positionTime;

    /**
     * sounds recorded by the devices that meet
     */
    private List<FileManager> soundfileList;
    /**
     * inner edges that will constitute the hyperedge that reflect the calibration that
     * takes place during the meeting
     */
    private List<DirectedEdge> innerEdges= new ArrayList<DirectedEdge>();
    /**
     * inner edges that will constitute the hyperedge that reflect the robust calibration that
     * takes place during the meeting
     */
    private List<DirectedEdge>    inner_robust_Edges= new ArrayList<DirectedEdge>();

    /**
     * inner edges that will constitute the hyperedge that reflect the calibration that
     * takes place during the meeting with only the uncalibrated devices
     */

    private List<DirectedEdge> uncalibrated_innerEdges= new ArrayList<DirectedEdge>();
    /**
     * inner edges that will constitute the hyperedge that reflect the robust calibration that
     * takes place during the meeting with only the uncalibrated devices
     */
    private List<DirectedEdge>    uncalibrated_inner_robust_Edges= new ArrayList<DirectedEdge>();

    /**
     * regression related information
     */
    private   Meet2regress regressActive;
    /**
     * geographical regression information
     */
    private   Meet2regressGeo filteredRegressActive;
    private   Meet2regressGeo regressActiveGeoFilter ;
    /**
     * determine if the device is the time server
     */
    public boolean isTimeServer = false;

    /**
     *     ArrayList created so as to save the name of calibrated devices
     */
    public ArrayList<String> namesFiles4Noise = new ArrayList<String>();

    // deal with the meeting of several devices
    /**
     * list of devices we are meeting with
     */
    public  List<Device>  meetwithS   = new ArrayList<Device>(); //device we are meeting with
    /**
     * state of the protocol concerning the device we are meeting with
     */
    public List<ProtocolState>  meetwithProtocolState   = new ArrayList<ProtocolState>();
    /**
     * sounds recorded by the devices  we are meeting with
     */
    public  List<FileManager> files4noise = new ArrayList<FileManager>(); // set of file managers used to store noise

    /**
     * time offset with regards to the wifi direct ap
     * @return time offset
     */
    public double getTimeOffset(){return this.timeOffset;}

    /**
     * set the time offset
     * @param time time offset
     */
    public void setTimeOffset(double time){
        this.timeOffset = time;
    }


    /**
     * reset the stream of the file wherein sound will be saved
     */
    public void resetLocalFileManager4Noise(){

        new FileManager(getlocalFile4NoiseName(), false, ctx);
    }

    /**
     * get the file where the (local) sound is stored
     * @return file
     */
    public FileManager getLocalFileManager4Noise(){
        Log.e(LOG_TAG, "I am trying to get local file ");
        return this.files4noise.get(0);
    }


     /**
     *provide the position of the requested device in the list of devices meeting using the provided vertexid
     * @param vertex_id vertex id
     * @return position of the requested device in hte group
     */
    public int getPositionfromVertex(int vertex_id){
        for (int i = 0 ; i< meetwithS.size(); i++){
            if (meetwithS.get(i).getVertexId() == vertex_id)
                return i;
        }
        return -1;
    }

    /**/

    /**
     * provide the position of the requested device in the list of devices meeting
     *      * return -1 if the device is not found
     * @param device_id device id
     * @return position of the requested device
     */
    public int getPosition(int device_id){
        Log.e(LOG_TAG, "provided id is " + device_id);

        if(meetwithS == null){
            Log.e(LOG_TAG, "meetwith is null " );
        }
        Log.e(LOG_TAG, "meetwith isze is " + meetwithS.size());


        for (int i = 0 ; i< meetwithS.size(); i++){
            if(meetwithS.get(i) == null)
                Log.e(LOG_TAG, "meetwith device is null " );
            Log.e(LOG_TAG, "check device(" + i + ")= vertex:" +  meetwithS.get(i).getVertexId() + " name:"+ meetwithS.get(i).name + " id:" +  meetwithS.get(i).deviceId );
            if (meetwithS.get(i).deviceId == device_id)
                return i;
        }
        return -1;
    }

    /**
     * returns the name fof the file wherein the sound recorded locally is stored
     * @return name of the file
     */
    public String getlocalFile4NoiseName(){
        String toReturn = new String("");

        //get prefix of the file name
        if (Configuration.isAveraging == false)
            toReturn+= Configuration.LOCAL_FILE_PREFIX   ;
        else
            toReturn+= Configuration.AVG_LOCAL_FILE_PREFIX   ;

        for(int i = 0 ; i< meetwithS.size() ; i++){
            Log.e(LOG_TAG, "add " + String.valueOf(meetwithS.get(i)) + "-");
            toReturn+= String.valueOf(meetwithS.get(i).getVertexId()) + "-";
        }
        Log.e(LOG_TAG, "local file name:" + toReturn);
        return toReturn;
    }




    /**
     * return the content of the file wherein the sound provided by
     * the other device, is stored
     * @param device device id
     * @return sound
     */
    public String getRemoteFile4NoiseName(Device device){
        String toReturn = new String("");
        // try to find the requested device in the list of devices
        for (int i =0; i < this.meetwithS.size(); i++){
            if(    this.meetwithS.get(i).getVertexId() == device.getVertexId()){// device found
                if (Configuration.isAveraging == true)
                    toReturn+=  Configuration.AVG_REMOTE_FILE_PREFIX + this.getName (i);
                else
                    toReturn+=  Configuration.REMOTE_FILE_PREFIX + this.getName (i);
                return toReturn;
            }
        }
        return toReturn;
    }

    /**
     *
     * @param actx context
     */
    public Meet(Context actx){
        this.ctx = actx;
        Orientation ort = new Orientation(this.ctx, this);
        Netandgps gpsData = new Netandgps(this.ctx, this, true);
        ort.start();
        gpsData.InitialiseGPSandNetworkTimeListener();
        this.setTimeOffset(0); // set that there is no time offset

    }

    /**
     * create a meeting (with the provided device)
     * @param device2meet device we are meeting with
     * @param actx context
     * @param fileMode file mode
     */
    public Meet(Device device2meet, Context actx, boolean fileMode ){
        Orientation ort = new Orientation(actx, this);
        Netandgps gpsData = new Netandgps(actx, this, true);
        ort.start();
        gpsData.InitialiseGPSandNetworkTimeListener();
        Log.e(LOG_TAG, "+++++++++++++++++++++\n+++++++++++++++++++++\n+++++++++++++++++++++\n" );
        Log.e(LOG_TAG, "+++++++++++++++++++++Create a new Meet for " + device2meet.getVertexId());
        this.meetwithS.add(device2meet);// add device we are meeting with
        for(int i =0; i< this.meetwithS.size() ; i++){
            this.meetwithProtocolState.add(new ProtocolState());
        }
        this.setTimeOffset(0); // set that there is no time offset
        init(actx);
    }

    /**
     * initialise the meeting
     * @param actx context
     */
    public void init(Context actx){
        this.ctx = actx; // used to save the connection graph into a file
        String localFileName4Noise = new String("");
        String remoteFileName4Noise = new String("");

        if(Configuration.isAveraging == true){
            localFileName4Noise = Configuration.AVG_LOCAL_FILE_PREFIX  ;
            remoteFileName4Noise = Configuration.AVG_REMOTE_FILE_PREFIX  ;
        }
        else{
            localFileName4Noise = Configuration.LOCAL_FILE_PREFIX ;
            remoteFileName4Noise = Configuration.REMOTE_FILE_PREFIX ;
        }

        Log.e(LOG_TAG, "add local file to the meeting: " + localFileName4Noise + getName(0)  );
        files4noise.add(new FileManager( localFileName4Noise + getName(0), true ,actx));
        for(int i =0; i< this.meetwithS.size() ; i++){
            Log.e(LOG_TAG, "add file to meeting: " + remoteFileName4Noise + getName(i)  );
            files4noise.add(new FileManager( remoteFileName4Noise + getName(i), true ,actx));
        }
    }

    /**
     * end of the meeting, release the resources
     */
    public void end (){

        if (files4noise.isEmpty()==false){
            for(int i =0; i< files4noise.size()  ; i++) {
                files4noise.get(i).close();
            }
            files4noise.clear(); //remove all the elements from the list
        }
    }


    /**
     * get the name of a device
     * @param i position of the device in the meeting group
     * @return name of the device
     */
    public String getName (int i){

        String toReturn =  String.valueOf (this.meetwithS.get(i).getVertexId()) + "-";

        for(int  j=0; j< this.meetwithS.size() ; j++){
            //  Log.e(LOG_TAG, "get Name(" + i + ")" + toReturn);
            if( i != j)
                toReturn += this.meetwithS.get(j).getVertexId() + "-";
        }
        Log.e(LOG_TAG, "****************get Name" + toReturn);
        return toReturn;
    }

    /**
     * add a device to the group that meet
     * @param device2meet device we are meeting with and we should add to the group
     * @param actx context
     * @param fileMode file mode
     */
    public void add(Device device2meet, Context actx, boolean fileMode){
        files4noise.clear();
        Log.e(LOG_TAG, "+++++++++++++++++++++\n+++++++++++++++++++++\n+++++++++++++++++++++" );
        Log.e(LOG_TAG, "add device " + device2meet.getVertexId() );
        boolean hasbeenadded = false;
        int i =0 ;
        Log.e(LOG_TAG, "add device " + device2meet.name + "to the meeting");
        while (  i< meetwithS.size() && hasbeenadded ==false ){
            // not need to add if the device is already present
            if(this.meetwithS.get(i).getVertexId() == device2meet.getVertexId())
                hasbeenadded = true;

            if(this.meetwithS.get(i).getVertexId() > device2meet.getVertexId()){
                this.meetwithS.add(this.meetwithS.get(i));// add device we are meeting with
                this.meetwithS.set(i, device2meet);
                this.meetwithProtocolState.add(this.meetwithProtocolState.get(i));
                this.meetwithProtocolState.set(i, new ProtocolState());
                hasbeenadded = true;
            }
            i++;
        }

        // if the device were not already present in the list, add it
        if(hasbeenadded == false){
            this.meetwithS.add(device2meet);// add device we are meeting with
            this.meetwithProtocolState.add(new ProtocolState());
        }

        Log.e(LOG_TAG, "Meeting is made of:");

        i =0 ;
        while (  i< meetwithS.size() && hasbeenadded ==false ) {
            Log.e(LOG_TAG, "device " + meetwithS.get(i).name);
            i ++;
        }

        String localFileName4Noise = new String("");
        String remoteFileName4Noise = new String("");
        if(Configuration.isAveraging == true){
            localFileName4Noise = Configuration.AVG_LOCAL_FILE_PREFIX  ;
            remoteFileName4Noise = Configuration.AVG_REMOTE_FILE_PREFIX  ;
        }
        else{
            localFileName4Noise = Configuration.LOCAL_FILE_PREFIX ; // + device2meet.vertexId ;
            remoteFileName4Noise = Configuration.REMOTE_FILE_PREFIX;// + device2meet.vertexId;
        }

        Log.e(LOG_TAG, "add the following file to meeting: " + localFileName4Noise + getName(0)  );
        files4noise.add(new FileManager( localFileName4Noise + getName(0), true /*append*/,actx));
        for( i =0; i< this.meetwithS.size() ; i++){
            Log.e(LOG_TAG, "add file to meeting: " + remoteFileName4Noise + getName(i)  );
            files4noise.add(new FileManager( remoteFileName4Noise + getName(i), true /*append*/,actx));
        }
    }









    @Override
/**
 * context changed
 */
    public void onSensorOrientationChanged(float axisX, float axisY, float axisZ, long timestamp) {
        //Log.d(TAG, "azimuth: " + axisX + "pitch: " + axisY + "roll: " + axisZ);
        //long offset = (long) Math.abs(axisX - axisY) * 100;
        //long offset = (long) timestamp ;

        // Log.e(LOG_TAG, "I am time server: " + me.configuration.isTimeServer);
        if(this.isTimeServer == true) {
            //orientationTime = measurement.syncNow.getTime();
            orientationTime = timestamp;
            this.ctxData.FLAG = "OrientationTimeServer";
        }else{
            //orientationTime = measurement.syncNow.getTime() + this.config.timeOffset;
            // Log.e(LOG_TAG, "offset: " + this.ctxData.timeOffsetCtxData);
            orientationTime = timestamp + ContextData.timeOffsetCtxData; //+ this.timeOffset;
            this.ctxData.FLAG = "OrientationTimeClient";
        }
        //Log.e(LOG_TAG, "Orientation Values :" + now.getTime() + "," + String.valueOf(axisX)+ "," + String.valueOf(axisY)+ "," +  String.valueOf(axisZ));
        ContextData.orientationData.add(ContextData.indexOrientation, orientationTime + "," + String.valueOf(axisX)+ "," + String.valueOf(axisY)+ "," +  String.valueOf(axisZ)) ;
        ContextData.indexOrientation = ContextData.indexOrientation + 1;
    }

    // When change position, we save the data
    @Override
    /**
     * position changed
     */
    public void onSensorPositionChanged(double latitude, double longitude, long localGPStime) {
        //Log.d(TAG, "azimuth: " + axisX + "pitch: " + axisY + "roll: " + axisZ);
        //long offset = (long) Math.abs(axisX - axisY) * 100;
        //long offset = (long) timestamp ;
        //Log.e(LOG_TAG, "Orientation Values :" + now.getTime() + "," + String.valueOf(axisX)+ "," + String.valueOf(axisY)+ "," +  String.valueOf(axisZ));

        Log.d(LOG_TAG, "GPS is running");
        Me me = new Me(this.ctx);
        if(this.isTimeServer == true) {
            positionTime = localGPStime;
            this.ctxData.FLAG = "PositionTimeServer";
        }else{
            positionTime = localGPStime + ContextData.timeOffsetCtxData; // + this.timeOffset;
            this.ctxData.FLAG = "PositionTimeClient";
        }

        ContextData.positionData.add(ContextData.indexPosition, positionTime + "," + String.valueOf(latitude)+ "," + String.valueOf(longitude)) ;
        ContextData.indexPosition = ContextData.indexPosition + 1;
        Log.e(LOG_TAG, "Size Position File: " + ContextData.positionData.size());
    }


     /**
     * start recording the sound
     */
    public void getLocalSoundRecording(){
        Log.d(LOG_TAG, "start recording (duration: " + Configuration.recordDuration_ms +")\n");
        // we should not save in a file that is not empty
        //
        resetLocalFileManager4Noise();

        measurement = new Record(Configuration.recordDuration_ms/*nb of milliseonc*/);
        //add a listener to know when the sound will be completely recorded (complete)
        measurement.addNoiseListener(this);
        try {
            measurement.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not record the noise");
            e.printStackTrace();
        }
    }


    /**
     * is called before recording hte local sound,
     * we sleep for a little of time (to permit other devices to join the group
     * @param nTests number of tests that should be performed (i.e. , number of calibrations)
     */
    public void getLocalSoundRecording2ConsecTests(int nTests){
        Log.d(LOG_TAG, "start the recording (duration: " + Configuration.recordDuration_ms + ")\n");
        //add a listener to know when the sound will be completely recorded (complete)
        //Orientation ort = new Orientation(this.ctx, this);
        //Netandgps gpsData = new Netandgps(this.ctx, this, true);
        try {
            for(int i = 0; i < nTests; i++) {
                //Log.e(LOG_TAG, "Sum of GPS and Orientation Key: " + this.HAS_ORT + " " + this.HAS_GPS);
                measurement = new Record(Configuration.recordDuration_ms/*nb of milliseconds*/);
                measurement.addNoiseListener(this);
                ContextData.currentRecordTest = i;
                measurement.start();
                Thread.sleep((Configuration.duration_beetween_test_SEC ) * 1000);
                //ort.start();
                //gpsData.InitialiseGPSandNetworkTimeListener();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not record the noise");
            e.printStackTrace();
        }
    }



    /**
     * save the sound that is provided b y the given remote device
     * @param buf buffer storing the sound
     * @param who device we are meeting with and that collected the sound
     */
    public void saveRemoteRecord(String buf,Device who){
        // find in the list the device we are speaking about
        for (int i =0 ; i< meetwithS.size() ; i++){
            if(meetwithS.get(i).getVertexId() == who.getVertexId()){
                files4noise.get(i+1).write_txt(buf);
                Log.e(LOG_TAG, "write the remote sound in file:" + files4noise.get(i+1).getFilename());
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * we remove a number of samples. The samples are given by a shifted time
     * @param buffer sound buffer
     * @param shift delay / nn of measurements to shift
     * @param slope slope used to weight the measurements
     * @param intercept intercept used to weight the measurements
     * @return shifted sound
     */
    public DoubleBuffer shift(DoubleBuffer buffer ,  int  shift, double slope , double intercept){
        double tempb[] =buffer.array();
        DoubleBuffer shiftedBuffer = DoubleBuffer.allocate(buffer.capacity()  - shift);
        Log.e(LOG_TAG, "We Start from shift, i.e., line: " + buffer.get(shift));
        for (int i =shift  ; i < tempb.length   /* buffer.capacity()*/ ; i++){
            shiftedBuffer.put(tempb[i] * slope + intercept);
            //    shiftedBuffer.put(buffer.get(i) * slope + intercept);
        }
        return shiftedBuffer ;
    }




    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    /**
     * save the raw sound recorded locally and provided in the buffer in a file
     * considering the time slot between two recording
     * @param buffer soundbuffer
     * @param slot delay between two consecurive measurements
     * @return sound
     */
    public DoubleBuffer savelocalrecord(DoubleBuffer buffer, double slot ){
        Log.d(LOG_TAG , "save local noise record into a file");
        this.files4noise.get(0).addFileListener(this); // add a listener to tfigure out when the thread finishes to write in the file
        DoubleBuffer shiftedBuffer;

        if(Configuration.isRawSoundData == true) {
            double[] arrayRaw = measurement.rawNoiseBuffer.array();
            ContextData.rawBuffer = arrayRaw;
        }

        try {// we remove from the buffer a number of samples (nb sample = shift)

            Log.e(LOG_TAG, "SHIFT IS " + Configuration.SHIFT_IN_SAMPLE);

            if(Configuration.IS_CALIBRATED == false  ){
                shiftedBuffer = shift(buffer, Configuration.SHIFT_IN_SAMPLE , /*slope*/1  , 0 /*intercept*/) ;
            }
            else{
                shiftedBuffer = shift(buffer, Configuration.SHIFT_IN_SAMPLE , Configuration.SLOPE  , Configuration.INTERCEPT) ;
            }

            long shiftDuration;// is expressing the period of time related to the number of samples
            if (slot == 0){
                // 1ms = 44 100/1000 samples
                shiftDuration = ((long) 1000/44100) * ((long) Configuration.SHIFT_IN_SAMPLE);
            }else {
                shiftDuration = ((long) slot) * ((long) Configuration.SHIFT_IN_SAMPLE);
            }
            Log.e(LOG_TAG, "THE TIME RELATED TO SHIFT IS " + shiftDuration);
            Log.e(LOG_TAG, " Save the sound in the file called " + this.files4noise.get(0).fileName);
            // we store with the shifted time in a file

            ///Log.e(LOG_TAG, "time offset is" + this.timeOffset);
            resetLocalFileManager4Noise();

            this.files4noise.get(0).write_calibration_bufferThread( shiftedBuffer,
                    shiftDuration + this.timeOffset + measurement.startRecord /* synchronised time*/,
                    slot /* delay between two noise record*/);


            if(Configuration.isRecordActivity == true){
                if(Configuration.SoundFileAreZipped == true) {
                    this.files4noise.get(0).zipFileAtPath("test" + ContextData.currentRecordTest, this.nRecordTest);
                    Log.d(LOG_TAG, "Test: zip the files");
                } else
                    Log.d(LOG_TAG,"Test: do not zip the files"  );
            }

            this.files4noise.get(0).removeFileListener(this);
            return shiftedBuffer;
        } catch (Exception e) {
            Log.d(LOG_TAG , "Exception in the save local record");
            e.printStackTrace();
        }
        // this.localFile4Noise.removeFileListener(this);//todo remove
        return null;
    }

    /**
     * add a sleep time
     * @param toadd sleep timer to add
     */
    public void addSleepListener(SleepListener toadd){ listeners.add(toadd);}

    /**
     * remove a sleep time
     * @param toremove sleep time to remove
     */
    public void removeSleepListener(SleepListener toremove){
        listeners.remove(toremove);
    }

    /**
     * time to wake up
     */
    private void sleepChangedSendEvent() {
        Log.d(LOG_TAG , "file changed event :" );

        mainHandler.post(new Runnable() {
            public void run() {
                for (SleepListener listener : listeners) {
                    Log.d(LOG_TAG , "report to listener " );
                    listener.someoneReportedAwake();
                }
            }
        });
    }

    /**
     * returns if a sleeping thread is already running
     * @returnd
     */
    private boolean sleepingThreadAlive() {
        return (_sleepingThread != null && _sleepingThread.isAlive());
    }

    /**
     * start a sleep thread
     * @param millisec duration of the sleep
     * @throws Exception could not run the thread
     */
    public synchronized void sleepingThread(final long millisec ) throws Exception {
        Log.d(LOG_TAG , "launching sleeping thread");
        if (!sleepingThreadAlive()) {
            //Create a new thread only if it does not exist or has been terminated.
            _sleepingThread = new Thread() {
                @Override
                public void run() {
                    Log.d(LOG_TAG , "sleeping thread launched");
                    sleepFor( millisec);
                }
            };
            _sleepingThread.start();
        }else{
            Log.d(LOG_TAG , "cannot launch a sleeping thread ");
        }
    }

    /**
     * sleep during the amount of millisecs that is provided
     * @param millisec sleep duration
     */
    public void sleepFor(long millisec){ // 5000 = 5 sec
        Log.d(LOG_TAG, "schedule the sound recording \n");
        Date now = new Date();// get local time
        long now_long = now.getTime() + millisec;
        this.recordSchedule = new Date(now_long);
        SystemClock.sleep(millisec);
        sleepChangedSendEvent();
        Log.d(LOG_TAG , "record scheduled at :::::::: " + this.recordSchedule.getTime() );
    }


    /**
     * schedule a meeting in 5 sec
     */
    public void scheduleMeetingin5sec(){
        Log.d(LOG_TAG, "start scheduling the recording \n");
        Date now = new Date();// get local time
        long now_long = now.getTime() + 5000;//start recording in 5 sec from now
        this.recordSchedule = new Date(now_long);
        SystemClock.sleep(5000);
    }

    //
    @Override
    /**
     * is used to determine of   the record of the sound is completed
     */
    public void someoneReportedNoiseChange(int type) {
        switch(type){
            case STILL_ADDING  :
                Log.e(LOG_TAG, "Still recording");
                break;
            case HAS_COMPLETED : // record is completed
                Log.e(LOG_TAG, "Recording completed ");
                //save the raw sound  into a file
                if(Configuration.isAveraging == false){
                    double slot = (double) 1000 / (double) Record._rate ; // compute the delay between two recording
                    this.savelocalrecord(measurement.noiseBuffer, slot);
                }
                else{// save the avg recording into a file
                    double slot = measurement.getSubwindowDuration() * 1000; // 0.125 second = 0.125 *1000 = 125 ms
                    // should we record the average ?
                    this.savelocalrecord(measurement.avgNoiseBuffer, slot);
                }

                // remove noise listener
                measurement.removeNoiseListener(this);

                break;
            case HAS_CANCELLED : // cancelled
                //remove the noise listener
                Log.e(LOG_TAG, "Recording cancelled");
                measurement.removeNoiseListener(this);
                break;
        }
    }
    //

    /**
     * is used to determine whether there is an error
     */
    @Override
    public void someoneReportedNoiseError() {
        measurement.removeNoiseListener(this);
    }

    @Override
    /**
     * is used to determine that the file has been updated
     */
    public void someoneReportedFileChange(int type) {
        switch(type){
            case SAVING_SOUND_HAS_COMPLETED  :
                Log.e(LOG_TAG, "Saving local sound completed");

                //
                break;
        }
    }




     /**
     * configure the parameters that are necessary to run a regression (a.k.a calibration)
     * @return some text that can be used as log
     */
    public String configureRegression() {
        String toReturn = new String();

        Me me = new Me(ctx);

        Log.e(LOG_TAG, "start calibration" );

        // display what are the calibration parameters that are applied to the device
        HyperConnection hyperConnect = new HyperConnection();
        hyperConnect.getFromFile(ctx);
        //compute shortest path from this  device
        Log.e(LOG_TAG, "Look for the shortest hyperpath from local device " + me.localDevice.getVertexId() );
        ShortestHyperPath shortHyperPath = new ShortestHyperPath();
        HyperConnection bestHyperConnex = shortHyperPath.getShortestHypergraph(me.localDevice.getVertexId() , hyperConnect  );
        Log.e(LOG_TAG, "\n Best Shortest hyper path: \n "+ bestHyperConnex.toString());


         me.getLocalconnectionGraph();
        Calibration multiHopCalibration = me.getMultiHopCalibration();
        me.getSingleHopCalibration();//determine if the device is calibrated or not

        /*
         apply the parameters
         */

        if(multiHopCalibration.slope ==1 && multiHopCalibration.intercept == 0 && multiHopCalibration.cumulated_errror ==0){//
            toReturn +="The device is not yet calibrated";
            Configuration.IS_CALIBRATED = false;
        }else{
            Calibration singleHopCalibration = me.getSingleHopCalibration();
            Configuration.IS_CALIBRATED = true;
            Configuration.SLOPE = singleHopCalibration.slope;
            Configuration.INTERCEPT = singleHopCalibration.intercept;
            Configuration.CUMULATED_ERRORS = multiHopCalibration.cumulated_errror;
            Configuration.WEIGHTED_CUMULATED_ERROR = multiHopCalibration.weighted_cumulated_error;
            toReturn += "The device is already calibrated \n ";
            toReturn += "   Slope: " + Configuration.SLOPE + ", intercept: " + Configuration.INTERCEPT +
                    ", calibration error " + Configuration.CUMULATED_ERRORS + " \n ";
        }



        toReturn += "Calibration group is composed of " + meetwithS.size() + " device";
        if (meetwithS.size() > 1) toReturn += "s";
        toReturn += ":";

        // list the device that form the calibration group
        for (int i = 0; i < meetwithS.size(); i++) {
            toReturn += " " + meetwithS.get(i).getVertexId() ;
        }
        toReturn +="\n";


        // decompose the group of devices with which we calibrate into 2 groups:
        HyperConnection connexion_graph = me.getLocalconnectionGraph();

        List<Device>  meetwithCalibrated   = new ArrayList<Device>(); //device we are meeting with and that are calibrated
        List<Device>  meetwithUncalibrated   = new ArrayList<Device>(); //device we are meeting with and that are not calibrated

        // put in list  the local and remote sounds that will be used for regression
        soundfileList = new ArrayList<FileManager>();
        List<FileManager> calibratedSoundfileList  =new ArrayList<FileManager>();
        List<FileManager> notCalibratedsoundfileList =   new ArrayList<FileManager>();
        //save into a file the remote sound and state if the device is calibrated
        for (int i = 0; i < this.meetwithS.size(); i++) {
            Device device_i = meetwithS.get(i);
            FileManager remoteFile4Noise = new FileManager(getRemoteFile4NoiseName(device_i), true, ctx);
            soundfileList.add(i, remoteFile4Noise);
            // determine if this device is calibrated
            // extract connection graph
            HyperConnection hyperConnection = new HyperConnection();
            hyperConnection.getFromFile(ctx);
            //compute shortest path from this  device
            Log.e(LOG_TAG, "Look for the shortest hyperpath for device " + device_i.vertexId);
            ShortestHyperPath shortestHyperPath = new ShortestHyperPath();
            HyperConnection bestHyperConnexion = shortestHyperPath.getShortestHypergraph(device_i.vertexId, hyperConnection );
            Log.e(LOG_TAG, "\n Best Shortest hyper path: \n "+ bestHyperConnexion.toString());
            device_i.calibration   = shortestHyperPath.multi_hop_calibrate( device_i.vertexId ,bestHyperConnexion);
            Log.e(LOG_TAG, device_i.calibration.toString() );
            meetwithS.set(i,device_i);
            // decompose the group of devices with which we calibrate into 2 groups:
            // those that are calibrated and those that are not
            if (device_i.calibration.iscalibred()){
                meetwithCalibrated.add(device_i);
                calibratedSoundfileList.add(remoteFile4Noise);
//                calibratedSoundfileList.add(i, remoteFile4Noise);
            }else{
                meetwithUncalibrated.add(device_i);
                notCalibratedsoundfileList.add(remoteFile4Noise);
              //  notCalibratedsoundfileList.add(i, remoteFile4Noise);
            }
        }

        // ask if we should had an hyperedge for the calibrated device

        if (calibratedSoundfileList.size()>0){
            toReturn += "   - The device may calibrate with " + meetwithCalibrated.size() + " device(s): ";
            for (int j=0; j< meetwithCalibrated.size(); j++){
                toReturn+= " " + meetwithCalibrated.get(j).vertexId;
            }
            toReturn+="\n"; //display the result of the regression
            //do the regression
            regress(calibratedSoundfileList);
            toReturn+= printRegression();
            // display the parameters that
            double cummulatederror =0; double robust_error = 0;

            for (int j=0; j< meetwithCalibrated.size(); j++){



                Meeting edge_caracteristic = new Meeting(   regressActive.multivariateregression.beta[0] /*intercept*/,
                        regressActive.multivariateregression.beta[j+1]  /*slope*/,
                        regressActive.std(regressActive.multivariateregression.residuals)/(meetwithCalibrated.size() )/*regressStandard_error*/,
                        regressActive.getMean(regressActive.multivariateregression.residuals) /(meetwithCalibrated.size() ) /* regressMeans_square_error*/
                        , Configuration.recordDuration_ms,
                        regressActive.multivariateregression.rSquared /(meetwithCalibrated.size() ) /* _Rsquared*/,
                        files4noise.get(0).getStartTime(), regressActive.sum(regressActive.multivariateregression.residuals)/(meetwithCalibrated.size() )  );

                DirectedEdge e = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(j).getVertexId(), edge_caracteristic);
                innerEdges.add(e);


                Meeting edge_robust_caracteristic = new Meeting(   regressActive.multivariateregression.cleanedMultivariateRegression.beta[0] /*intercept*/,
                        regressActive.multivariateregression.cleanedMultivariateRegression.beta[j+1]  /*slope*/,
                        regressActive.std(regressActive.multivariateregression.cleanedMultivariateRegression.residuals)/ (meetwithCalibrated.size() ) /* regressStandard_error*/,
                        regressActive.getMean(regressActive.multivariateregression.cleanedMultivariateRegression.residuals) /(meetwithCalibrated.size() ) /* regressMeans_square_error*/
                        , Configuration.recordDuration_ms,
                        regressActive.multivariateregression.cleanedMultivariateRegression.rSquared /(meetwithCalibrated.size() ) /* _Rsquared*/,
                        files4noise.get(0).getStartTime(), regressActive.sum(regressActive.multivariateregression.cleanedMultivariateRegression.residuals)/(meetwithCalibrated.size() )  );

                DirectedEdge robust_edge = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(j).getVertexId(), edge_robust_caracteristic);
                inner_robust_Edges.add(robust_edge);

                // if the cumulated error of the calibrated device is bigger than the one we get here

                // look in the connexion graph for the cumulated error and weighted cumulated error
                Calibration calib=  me.getMultiHopCalibration(meetwithCalibrated.get(j).getVertexId());

                cummulatederror+= calib.cumulated_errror  ;
                robust_error += calib.cumulated_errror  ;

                Log.e(LOG_TAG, " computed error for " + j + "gets " + cummulatederror);
                Log.e(LOG_TAG, " computed robust error for  " + j + "gets " + robust_error);

                cummulatederror+= e.weight();
                robust_error+=robust_edge.weight();
            }

            toReturn+= "\n-----------------------------";
            toReturn += "\nSimple Calibration:\nbeta: [";
            for (int k =0; k< regressActive.multivariateregression.beta.length ; k++)
                toReturn += regressActive.multivariateregression.beta[k] + " "  ;
            toReturn+="] \nerror: " + cummulatederror;
            toReturn+= "-----------------------------";
            toReturn += "\nRobust Calibration: \nbeta: [" ;
            for (int k =0; k< regressActive.multivariateregression.cleanedMultivariateRegression.beta.length ; k++)
                toReturn += regressActive.multivariateregression.cleanedMultivariateRegression.beta[k] + " "  ;
            toReturn+="] \nerror: " + robust_error;
            toReturn+= "\n-----------------------------\n\n";


            //check if this calibration is the best one so far
            if (Configuration.IS_CALIBRATED == false || (Math.abs(Configuration.CUMULATED_ERRORS)>Math.abs(cummulatederror)) ||  Math.abs(Configuration.CUMULATED_ERRORS)> Math.abs(robust_error))
            {
                toReturn += "The device should calibrate now  !!! \n";
                if (Math.abs(cummulatederror)> Math.abs(robust_error)){
                    toReturn+= "\nCalibration using robust regression should be priviledged";
                    Configuration.SHOULD_BE_SAVED = true;
                    Configuration.SHOULD_SIMPLY_CALIBRATE = false;
                    Configuration.SHOULD_ROBUSTLY_CALIBRATE = true;
                    // do not offer the option to calibrate
                    // make the button visible
                }else{
                    toReturn+= " \nCalibration using simple regression should be priviledged";
                    Configuration.SHOULD_ROBUSTLY_CALIBRATE = false;
                    Configuration.SHOULD_SIMPLY_CALIBRATE = true;
                }
            }else{ toReturn +="The device should not calibrate";
            }

            ///////////////////////////////
            //we could display the calibration properties of any device
        }else{
            toReturn += "   - But, the device cannot calibrate with any device\n";
        }


        // add an hyperedge for uncalibrated devices
        if (notCalibratedsoundfileList.size()>0){
            toReturn += "   - " + meetwithUncalibrated.size() + " device(s) are not yet calibrated\n\n";
            regress(notCalibratedsoundfileList);
            //to do add it to the history
            ///////////////////////////////////////////////////////////////////

            Log.e(LOG_TAG,  printRegression());
            // display the parameters that
            double cummulatederror =0; double robust_error = 0;

            for (int j=0; j< meetwithUncalibrated.size(); j++){

                Meeting edge_caracteristic = new Meeting(   regressActive.multivariateregression.beta[0] /*intercept*/,
                        regressActive.multivariateregression.beta[j+1]  /*slope*/,
                        regressActive.std(regressActive.multivariateregression.residuals)/(meetwithUncalibrated.size() )/*regressStandard_error*/,
                        regressActive.getMean(regressActive.multivariateregression.residuals) /(meetwithUncalibrated.size() ) /* regressMeans_square_error*/
                        , Configuration.recordDuration_ms,
                        regressActive.multivariateregression.rSquared /(meetwithUncalibrated.size() ) /* _Rsquared*/,
                        files4noise.get(0).getStartTime(), regressActive.sum(regressActive.multivariateregression.residuals)/(meetwithUncalibrated.size() )  );

                DirectedEdge e = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(j).getVertexId(), edge_caracteristic);
                innerEdges.add(e);

                Meeting edge_robust_caracteristic = new Meeting(   regressActive.multivariateregression.cleanedMultivariateRegression.beta[0] /*intercept*/,
                        regressActive.multivariateregression.cleanedMultivariateRegression.beta[j+1]  /*slope*/,
                        regressActive.std(regressActive.multivariateregression.cleanedMultivariateRegression.residuals)/ (meetwithUncalibrated.size() ) /* regressStandard_error*/,
                        regressActive.getMean(regressActive.multivariateregression.cleanedMultivariateRegression.residuals) /(meetwithUncalibrated.size() ) /* regressMeans_square_error*/
                        , Configuration.recordDuration_ms,
                        regressActive.multivariateregression.cleanedMultivariateRegression.rSquared /(meetwithUncalibrated.size() ) /* _Rsquared*/,
                        files4noise.get(0).getStartTime(), regressActive.sum(regressActive.multivariateregression.cleanedMultivariateRegression.residuals)/(meetwithUncalibrated.size() )  );

                DirectedEdge robust_edge = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(j).getVertexId(), edge_robust_caracteristic);
                inner_robust_Edges.add(robust_edge);
                toReturn += "robust edge " + robust_edge.toString();
                toReturn += "robust edge " + e.toString();

                // if the cumulated error of the calibrated device is bigger than the one we get here

                // look in the connexion graph for the cumulated error and weighted cumulated error
                Calibration calib=  me.getMultiHopCalibration(meetwithUncalibrated.get(j).getVertexId());

                cummulatederror+= calib.cumulated_errror  ;
                robust_error += calib.cumulated_errror  ;

                Log.e(LOG_TAG, " computed error for " + j + "gets " + cummulatederror);
                Log.e(LOG_TAG, " computed robust error for  " + j + "gets " + robust_error);

                cummulatederror+= e.weight();
                robust_error+=robust_edge.weight();
            }


/////////////////////////////////////////////
        }

        return toReturn;
    }


    /**
     * what to do with uncalibrated devices that are not usefull to calibrate the device
     * @return some text to display
     */
    public String add_uncalibrated_devices(){

        String toReturn = new String();
        Me me = new Me(ctx);
        HyperConnection connexion_graph = me.getLocalconnectionGraph();

        // is there any hyperedge to create to join the uncalibrated devices
        if (uncalibrated_inner_robust_Edges.size()>0 && uncalibrated_innerEdges.size()>0){
            HyperEdge uncalibratedHyperedge2add = new HyperEdge(uncalibrated_innerEdges);
            HyperEdge uncalibratedrobustHyperedge2add = new HyperEdge(uncalibrated_inner_robust_Edges);

            if (uncalibratedHyperedge2add.weight() > uncalibratedrobustHyperedge2add.weight()){
                toReturn += "add uncalibrated hyper edge " + uncalibratedrobustHyperedge2add;
                connexion_graph.addHyperEdge(uncalibratedrobustHyperedge2add);

            }else{
                toReturn += "add uncalibrated hyper edge " + uncalibratedHyperedge2add;
                connexion_graph.addHyperEdge(uncalibratedHyperedge2add);
            }
        }

        connexion_graph.toFile(ctx);//write connexion
        return toReturn;
    }

    /**
     * robust calibration is performed during this meeting
     * @return some text to display
     */
    public String follow_to_robustly_calibrate(){

        String toReturn = new String();
        Me me = new Me(ctx);
        HyperConnection connexion_graph = me.getLocalconnectionGraph();



        HyperEdge hyperEdge2add = new HyperEdge(inner_robust_Edges/**list iner e dges*/);
        toReturn += "add hyper edge " + hyperEdge2add;

        Log.e(LOG_TAG, " add hyperedge" + hyperEdge2add.toString());
        connexion_graph.addHyperEdge(hyperEdge2add);


        connexion_graph.toFile(ctx);//write connexion
        toReturn += "Local connexion graph:" + me.getLocalconnectionGraph().toString();
        me.localconnectionGraph = me.getLocalconnectionGraph();
        //compute shortest path from the local device
        HyperConnection bestHyperConnexion = me.getShortestPath(me.localDevice.getVertexId() );
        toReturn +=  "\n Best Shortest path: \n";
        toReturn +=  bestHyperConnexion.toString();
        toReturn += "CALIBRATION********************";
        Calibration multiHopCalibration = me.getMultiHopCalibration();
        Calibration singleHopCalibration = me.getSingleHopCalibration();

        /*
         apply the parameters
         */
        Configuration.IS_CALIBRATED = true;
        Configuration.SLOPE = singleHopCalibration.slope;
        Configuration.INTERCEPT = singleHopCalibration.intercept;
        Configuration.CUMULATED_ERRORS = multiHopCalibration.cumulated_errror;
        Configuration.WEIGHTED_CUMULATED_ERROR = multiHopCalibration.weighted_cumulated_error;
        Calibration calibration = new Calibration(Configuration.SLOPE, Configuration.INTERCEPT, Configuration.CUMULATED_ERRORS, Configuration.WEIGHTED_CUMULATED_ERROR);
        toReturn += calibration.toString();
        /////////////////////////////
        return toReturn;
    }








    /*
     */

    /**
     * perform a simple calibration (find the best simple calibration
     *
     * @return some text that can be displayed
     */
    public String   follow_to_simply_calibrate(){
        String toReturn = new String();
        Me me = new Me(ctx);
        HyperConnection connexion_graph = me.getLocalconnectionGraph();

        //create an hyperedge to join with the calibrated devices
        HyperEdge hyperEdge2add = new HyperEdge(innerEdges/**list iner edges*/);
        toReturn += "add hyper edge " + hyperEdge2add;
        Log.e(LOG_TAG, " add hyperedge" + hyperEdge2add.toString());
        connexion_graph.addHyperEdge(hyperEdge2add);

        connexion_graph.toFile(ctx);//write connexion
        toReturn += "Local connexion graph:" + me.getLocalconnectionGraph().toString();
        me.localconnectionGraph = me.getLocalconnectionGraph();
        //compute shortest path from the local device
        HyperConnection bestHyperConnexion = me.getShortestPath(me.localDevice.getVertexId() );
        toReturn +=  "\n Best Shortest path: \n";
        toReturn +=  bestHyperConnexion.toString();
        toReturn += "CALIBRATION********************";
        Calibration multiHopCalibration = me.getMultiHopCalibration();
        Calibration singleHopCalibration = me.getSingleHopCalibration();

        /*
         apply the parameters
         */
        Configuration.SLOPE = singleHopCalibration.slope;
        Configuration.INTERCEPT = singleHopCalibration.intercept;
        Configuration.CUMULATED_ERRORS = multiHopCalibration.cumulated_errror;
        Configuration.WEIGHTED_CUMULATED_ERROR = multiHopCalibration.weighted_cumulated_error;
        Calibration calibration = new Calibration(Configuration.SLOPE, Configuration.INTERCEPT, Configuration.CUMULATED_ERRORS, Configuration.WEIGHTED_CUMULATED_ERROR);
        toReturn += calibration.toString();

        /////////////////////////////
        return toReturn;

    }


    /**
     * perform a regression using the measurements that are stored in the files list
     * @param asoundfileList list of files used to store hte measurements that will be used to regress
     */
    public void regress( List<FileManager> asoundfileList) {
        // save into a file the sound recorded locally

        FileManager localFile4Noise = new FileManager(getlocalFile4NoiseName(), true, ctx);

        localFile4Noise.getSound();
      FileManager f=  localFile4Noise.getWeightedSound(1/Configuration.SLOPE, Configuration.INTERCEPT, "raw_local_noise" ,  ctx);


        //   if(Configuration.isLocationAwareCalibration == false) {// perform the simple, multivariate, geometric regression
        regressActive = new Meet2regress(/*localFile4Noise*/f, asoundfileList, ctx);
        //  }

    /*    if(Configuration.isLocationAwareCalibration && Configuration.isFilteredData2LinearRegression  && !Configuration.isFilteredData2GeoRegression) {
            filteredRegressActive = new Meet2regressGeo(localFile4Noise, asoundfileList, ctx);
        }

        if(Configuration.isLocationAwareCalibration &&  Configuration.isFilteredData2LinearRegression && Configuration.isFilteredData2GeoRegression) {
            regressActiveGeoFilter = new Meet2regressGeo(localFile4Noise, asoundfileList, ctx);
        }
        */
    }


    /**
     * apply a regression
     */
    public void regress_all() {
        // save into a file the sound recorded locally

        FileManager localFile4Noise = new FileManager(getlocalFile4NoiseName(), true, ctx);

        if(Configuration.isLocationAwareCalibration == false) {// perform the simple, multivariate, geometric regression
            regressActive = new Meet2regress(localFile4Noise, soundfileList, ctx);
        }

        if(Configuration.isLocationAwareCalibration && Configuration.isFilteredData2LinearRegression  && !Configuration.isFilteredData2GeoRegression) {
            filteredRegressActive = new Meet2regressGeo(localFile4Noise, soundfileList, ctx);
        }

        if(Configuration.isLocationAwareCalibration &&  Configuration.isFilteredData2LinearRegression && Configuration.isFilteredData2GeoRegression) {
            regressActiveGeoFilter = new Meet2regressGeo(localFile4Noise, soundfileList, ctx);
        }
    }


    /**
     * calibrate
     * @return some text to display
     */

    public String calibrate() {

        String toReturn = new String(); // is used for printing informations

        toReturn+= configureRegression();
        return toReturn; }

    /**
     * return some string to display and provide insight on the regression
     * @return string to display
     */
    String  printRegression (){
        String toReturn = new String(); // is used for printing informations

//        toReturn += "\n Calibrate  with device(s): ";
        for (int i = 0; i < meetwithS.size(); i++) {
            toReturn += " " + meetwithS.get(i).getVertexId() ;
        }

        if(Configuration.isLocationAwareCalibration == false) {
            String toprint = regressActive.MultivariateRegressionoutput;
            String torobustprint = regressActive.robustRegressionoutput;
            String togeometricrint = regressActive.geoMeanRegressionoutput;

            toReturn +=  toprint + "\n" + torobustprint+ "\n";
//                    +"********Geometric Regression:\n" + togeometricrint+ "\n";

            double[] beta = regressActive.multivariateregression.beta;
            double[] regressstd = regressActive.multivariateregression.parametersStdErrors;
            //toReturn += "\n beta lenght " + beta.length+ "\n";
            //toReturn += "beta (0): " + beta[0]+ "\n";
            // toReturn +=  "tot sum of squarre: " + regressActive.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares()+ "\n";
            // toReturn += "record duration: " + recordDuration_ms;
            //toReturn += "\nR²: " + regressActive.multivariateregression.OLSMultiReg.calculateRSquared()+ "\n";
            // toReturn +="\nresidu:" + regressActive.multivariateregression.OLSMultiReg.calculateResidualSumOfSquares()+ "\n";

            for (int i = 0; i < meetwithS.size(); i++) {
                double startTime = files4noise.get(i).getStartTime();
                //   toReturn += "beta (" + (i + 1) + "): " +beta[i+1] ;
                //   toReturn += "regressstd[" + i + "]=" + regressstd[i] + "\n";
                //  toReturn += "start time" + startTime+ "\n";
            }
        }

        if(Configuration.isLocationAwareCalibration == true) {
            if(Configuration.isFilteredData2LinearRegression ==  true){
                if(Configuration.isFilteredData2GeoRegression == false) {
                    String filteredtoprint = filteredRegressActive.MultivariateFilteredRegressionoutput;
                    String filteredtorobustprint = filteredRegressActive.filteredRobustRegressionoutput;
                    //String filteredtogeometricrint = filteredRegressActive.geoMeanRegressionoutput.toString();
                    // Log.e(TAG, "multiple Regress" + toprint);
                    toReturn +=   filteredtoprint + filteredtorobustprint;
                    double[] filteredbeta = filteredRegressActive.filteredmultiregression.beta;
                    double[] filteredregressstd = filteredRegressActive.filteredmultiregression.parametersStdErrors;

                    for (int i = 0; i < meetwithS.size(); i++) {
                        double startTime = files4noise.get(i).getStartTime();// i+1
                        toReturn +="beta lenght " + filteredbeta.length + "\n";
                        toReturn +="beta0:" + filteredbeta[0]+ "\n";
                        toReturn += "beta(" + (i + 1) + ")=" + filteredbeta[i + 1]+ "\n";
                        toReturn +=" regressstd[" + i + "]=" + filteredregressstd[i]+ "\n";
                        toReturn +="tot sum of squarre" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateTotalSumOfSquares()+ "\n";
                        toReturn += "record duration " + Configuration.recordDuration_ms+ "\n";
                        toReturn += "R²" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateRSquared()+ "\n";
                        toReturn +="start time" + startTime+ "\n";
                        toReturn += "residu" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateResidualSumOfSquares()+ "\n";

                    }
                }
                if(Configuration.isFilteredData2GeoRegression ==  true){
                    String toprintGeo = regressActiveGeoFilter.filteredgeoweightedregression.toString();
                    toReturn += "********Filtered Geographically Multiple Regression:\n" + toprintGeo;

                    toReturn +="\n beta lenght " + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.size()+"\n";
                    //Log.e(TAG, "beta0:" + beta[0]);
                    toReturn += "beta (0)" + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(0)+"\n";
                    toReturn +="tot sum of squarre" + regressActiveGeoFilter.filteredgeoweightedregression.GLSMultiReg.estimateRegressionStandardError()+"\n";
                    toReturn += "R²" + regressActiveGeoFilter.filteredgeoweightedregression.GLSMultiReg.estimateRegressandVariance()+"\n";

                    for (int i = 0; i < meetwithS.size(); i++) {
                        double startTime = files4noise.get(i).getStartTime();
                        int betaSize = regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.size();
                        //double[] beta = new double[betaSize];
                        //for (int j = 0; i < betaSize; i++) {
                        //  Log.e(TAG, "beta mean: " + "(" + j +") " +  regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(j));
                        // beta[j] = regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(j);
                        //double[] regressstd = regressActiveGeo.geoweightedregression.;

                        //  toReturn += "beta (" + (i + 1) + ")"+"\n";
                        //  toReturn += "beta(" + (i + 1) + ")=" + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(i+1)+"\n";
                        // toReturn += " regressstd[" + i +"]=" +  regressstd[i];
                        //(chatFragment).pushMessage("regressstd[" + i +"]=" +  regressstd[i]);
                        toReturn += "start time" + startTime+"\n";
                        //Log.e(TAG, "residu" + regressActiveGeo.geoweightedregression.GLSMultiReg.);

                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * calibration
     * @return some text
     */
    public String old_calibrates() {
        Log.e(LOG_TAG, "start calibration" );

        String toReturn = new String(); // is used for printing informations
        List<FileManager> fileList = new ArrayList<FileManager>();


        // extract the sound
        for (int i = 0; i < this.meetwithS.size(); i++) {
            FileManager remoteFile4Noise = new FileManager(getRemoteFile4NoiseName(meetwithS.get(i)), true, ctx);
            Log.e(LOG_TAG, "remote file: " + remoteFile4Noise.read_txt().toString());
            Log.e(LOG_TAG, "File Name: " + remoteFile4Noise.getFilename());
            fileList.add(i, remoteFile4Noise);
        }

        if (meetwithS.size() == 1)
            toReturn += "**************\n\n Calibrate with " + meetwithS.size() + "device";
        else
            toReturn += "**************\n\n Calibrate with " + meetwithS.size() + "devices";

        toReturn += "\n Calibrate  with device(s): ";
        for (int i = 0; i < meetwithS.size(); i++) {
            toReturn += " " + meetwithS.get(i).getVertexId() ;
        }

        FileManager localFile4Noise = new FileManager(getlocalFile4NoiseName(), true, ctx);
        Log.e(LOG_TAG, "local file: " + localFile4Noise.read_txt().toString());
        ContextData ctxData = new ContextData();

        if(Configuration.isLocationAwareCalibration == false) {
            Meet2regress regressActive = new Meet2regress(localFile4Noise, fileList, ctx);

            String toprint = regressActive.MultivariateRegressionoutput;
            String torobustprint = regressActive.robustRegressionoutput;
            String togeometricrint = regressActive.geoMeanRegressionoutput;

            toReturn += "********Multiple Regression:\n" + toprint + "\n"+
                    "********Robust Regression:\n" + torobustprint+ "\n"
                    +"********Geometric Regression:\n" + togeometricrint+ "\n";

            for (int i = 0; i < meetwithS.size(); i++) {
                double startTime = files4noise.get(i).getStartTime();// i+1
                double[] beta = regressActive.multivariateregression.beta;
                double[] regressstd = regressActive.multivariateregression.parametersStdErrors;
                toReturn += "\n beta lenght " + beta.length+ "\n";
                toReturn += "beta (0): " + beta[0]+ "\n";
                toReturn += "beta (" + (i + 1) + "): " +beta[i+1] ;
                toReturn += "regressstd[" + i + "]=" + regressstd[i] + "\n";
                toReturn +=  "tot sum of squarre: " + regressActive.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares()+ "\n";
                toReturn += "record duration: " + Configuration.recordDuration_ms;
                toReturn += "\nR²: " + regressActive.multivariateregression.OLSMultiReg.calculateRSquared()+ "\n";
                toReturn += "start time" + startTime+ "\n";
                toReturn +="\nresidu:" + regressActive.multivariateregression.OLSMultiReg.calculateResidualSumOfSquares()+ "\n";

                Meeting edge_caracteristics  =    new Meeting(  beta[0] /*intercept*/, beta[i+1] /*slope*/,
                        regressstd[i] /* regressStandard_error*/,  regressActive.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares() /* regressMeans_square_error*/, Configuration.recordDuration_ms
                        , regressActive.multivariateregression.OLSMultiReg.calculateRSquared() /* _Rsquared*/,
                        startTime , regressActive.multivariateregression.OLSMultiReg.calculateResidualSumOfSquares());
                Me me = new Me(ctx);
                DirectedEdge e = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(i).getVertexId(), edge_caracteristics);

                Log.e(LOG_TAG, "I add an edge " + e.toString());
                Log.e("RRRRRRR", "my name is" + me.localDevice.getVertexId() );
                Log.e("RRRR", "other mobile" +  meetwithS.get(i).getVertexId());
                innerEdges.add(e);
            }
        }

        if(Configuration.isLocationAwareCalibration == true) {
            if(Configuration.isFilteredData2LinearRegression ==  true){
                if(Configuration.isFilteredData2GeoRegression == false) {
                    Meet2regressGeo filteredRegressActive = new Meet2regressGeo(localFile4Noise, fileList, ctx);

                    String filteredtoprint = filteredRegressActive.MultivariateFilteredRegressionoutput;
                    String filteredtorobustprint = filteredRegressActive.filteredRobustRegressionoutput;
                    //String filteredtogeometricrint = filteredRegressActive.geoMeanRegressionoutput.toString();
                    // Log.e(TAG, "multiple Regress" + toprint);
                    toReturn +=  "********Filtered Multiple Regression:\n" + filteredtoprint;
                    toReturn +=  "********Filtered Robust Regression:\n" + filteredtorobustprint;

                    for (int i = 0; i < meetwithS.size(); i++) {
                        double startTime = files4noise.get(i).getStartTime();// i+1
                        double[] filteredbeta = filteredRegressActive.filteredmultiregression.beta;
                        double[] filteredregressstd = filteredRegressActive.filteredmultiregression.parametersStdErrors;
                        toReturn +="beta lenght " + filteredbeta.length + "\n";
                        toReturn +="beta0:" + filteredbeta[0]+ "\n";
                        toReturn += "beta(" + (i + 1) + ")=" + filteredbeta[i + 1]+ "\n";
                        toReturn +=" regressstd[" + i + "]=" + filteredregressstd[i]+ "\n";
                        toReturn +="tot sum of squarre" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateTotalSumOfSquares()+ "\n";
                        toReturn += "record duration " + Configuration.recordDuration_ms+ "\n";
                        toReturn += "R²" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateRSquared()+ "\n";
                        toReturn +="start time" + startTime+ "\n";
                        toReturn += "residu" + filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateResidualSumOfSquares()+ "\n";

                        Meeting edge_caracteristics  =    new Meeting(  filteredbeta[i+1] /*intercept*/, filteredbeta[0] /*slope*/,
                                filteredregressstd[i] /* regressStandard_error*/,
                                filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateTotalSumOfSquares()  /* regressMeans_square_error*/, Configuration.recordDuration_ms
                                , filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateRSquared()    /* _Rsquared*/,
                                startTime , filteredRegressActive.filteredmultiregression.OLSMultiReg.calculateResidualSumOfSquares());
                        Me me = new Me(ctx);
                        DirectedEdge e = new DirectedEdge(me.localDevice.getVertexId(), meetwithS.get(i).getVertexId(), edge_caracteristics);

//                        DirectedEdge e = new DirectedEdge(me.getVertexId(), meetwithS.get(i).deviceId, edge_caracteristics);
                        innerEdges.add(e);
                    }
                }
                if(Configuration.isFilteredData2GeoRegression ==  true){
                    Meet2regressGeo regressActiveGeoFilter = new Meet2regressGeo(localFile4Noise, fileList, ctx);

                    String toprintGeo = regressActiveGeoFilter.filteredgeoweightedregression.toString();
                    toReturn += "********Filtered Geographically Multiple Regression:\n" + toprintGeo;


                    for (int i = 0; i < meetwithS.size(); i++) {
                        double startTime = files4noise.get(i).getStartTime();// i+1
                        int betaSize = regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.size();
                        //double[] beta = new double[betaSize];
                        //for (int j = 0; i < betaSize; i++) {
                        //  Log.e(TAG, "beta mean: " + "(" + j +") " +  regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(j));
                        // beta[j] = regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(j);
                        // }
                        //double[] regressstd = regressActiveGeo.geoweightedregression.;

                        toReturn +="\n beta lenght " + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.size()+"\n";
                        //Log.e(TAG, "beta0:" + beta[0]);
                        toReturn += "beta (0)" + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(0)+"\n";
                        toReturn += "beta (" + (i + 1) + ")"+"\n";
                        toReturn += "beta(" + (i + 1) + ")=" + regressActiveGeoFilter.filteredgeoweightedregression.betaSummaryMean.get(i+1)+"\n";
                        //                       toReturn += " regressstd[" + i +"]=" +  regressstd[i];
                        //(chatFragment).pushMessage("regressstd[" + i +"]=" +  regressstd[i]);
                        toReturn +="tot sum of squarre" + regressActiveGeoFilter.filteredgeoweightedregression.GLSMultiReg.estimateRegressionStandardError()+"\n";
                        toReturn += "R²" + regressActiveGeoFilter.filteredgeoweightedregression.GLSMultiReg.estimateRegressandVariance()+"\n";
                        toReturn += "start time" + startTime+"\n";
                        //Log.e(TAG, "residu" + regressActiveGeo.geoweightedregression.GLSMultiReg.);
                        //todo look with Otto

                    }
                }
            }

        }


        if(Configuration.isMultiHopCalibration ==true){
            //extract the connexion graph
            Me me = new Me(ctx);
            HyperConnection connexion_graph = me.getLocalconnectionGraph();
            HyperEdge hyperEdge2add = new HyperEdge(innerEdges/**list ineredges*/);
            Log.e(LOG_TAG, " add hyperedge" + hyperEdge2add.toString());
            connexion_graph.addHyperEdge(hyperEdge2add);
            me.localconnectionGraph.toFile(ctx);//write connexion
            toReturn += "Local connexion graph:" + me.localconnectionGraph.toString();
            toReturn += "Again" +  me.getLocalconnectionGraph().toString();

            //compute shortest path from the local device
            HyperConnection bestHyperConnexion = me.getShortestPath(me.localDevice.getVertexId() );
            toReturn +=  "\n Best Shortest path: \n";
            toReturn +=  bestHyperConnexion.toString();
            toReturn += "CALIBRATION********************";
            Calibration calibration = me.getMultiHopCalibration();
            toReturn += calibration.toString();
            /////////////////////////////
        }
        return toReturn;
    }


}
