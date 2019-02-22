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

 import android.app.Fragment;
import android.content.Context;

import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.colibris.configuration.Me;
import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.calib.FileListener;
import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.sound.Filter;
import com.example.colibris.calib.Meet;
import com.example.colibris.calib.sound.Record;
 import com.example.colibris.nsd.FileTransferManager;
 import com.example.colibris.nsd.ProtocolState;

 import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
/**
 * This fragment handles the UI used to display information about the calibration. this  includes a list view user for the messages
 * that are exchanged during the calibration. All the protocol is implemented here
 */

public class WifiDirectCalibrationFragment extends Fragment implements FileListener,  com.example.colibris.comtool.SleepListener {
    /**
     * log information
     */
    private static final String TAG = "Calibration fragment";
    /**
     * view
     */
    private View view;
    /**
     * list view used to display calibration related information
     */
    private ListView listView;
    /**
     * adapter used to handle message
     */
    CalibrationMessageAdapter adapter = null;
    /**
     * activity used to display service discovery information
     */
    private WiFiServiceDiscoveryActivity discoveryActivity;
    /**
     * tcp transfer manager used to exchange calibration rleated parameters (e.g. sound) with other devices
     */
    private boolean setedtcptransferManager;
    /**
     * list of socket manager used to commmunicate with other devices
     */
    public  List< FileTransferManager>  tcptransferManagerList   = new ArrayList<FileTransferManager>();
    /**
     * is set to true as soon as we stop recording locally
     */
    public boolean hasAlreadyFinishedRecording = false;
    /**
     * determine if the device can calibrate
     */
    public boolean canCalibrate = false;
    /**
     * buttons
     */
    Button        cancelButton, calibrateButton;
    /**
     * radio group
     */
    public RadioGroup rgApproach;
    /**
     * radio buttons to determine which type of calibration should be performed with regards to their performance
     */
    public RadioButton simple_calibrate_radio, robust_calibrate_radio;

    /**
     * list of items that are displayed
     */
    private List<String> items = new ArrayList<String>();
    /**
     * View creation
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wifi, container, false);
        listView = view.findViewById(android.R.id.list);
        adapter = new CalibrationMessageAdapter(getActivity(), android.R.id.text1, items);
        listView.setAdapter(adapter);
        cancelButton = view.findViewById(R.id.buttoncancel);
        simple_calibrate_radio = view.findViewById(R.id.rbSimpleCalib);
        robust_calibrate_radio = view.findViewById(R.id.robustCalib);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button cancel
                cancelButton.setVisibility(View.INVISIBLE);
                calibrateButton.setVisibility(View.INVISIBLE);
                simple_calibrate_radio.setVisibility(View.INVISIBLE);
                robust_calibrate_radio.setVisibility(View.INVISIBLE);
            }
        });

        calibrateButton = view.findViewById(R.id.buttonshouldCalibrate);

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button cancel
                cancelButton.setVisibility(View.INVISIBLE);
                calibrateButton.setVisibility(View.INVISIBLE);
                simple_calibrate_radio.setVisibility(View.INVISIBLE);
                robust_calibrate_radio.setVisibility(View.INVISIBLE);
                int selectedId = rgApproach.getCheckedRadioButtonId();
                // find which radioButton is checked by id
                if(selectedId == R.id.rbSimpleCalib) {
                    pushMessage("Simple calibration is performed");

                    String todisplay = discoveryActivity.meet.follow_to_simply_calibrate();
                    pushMessage(todisplay);

                } else if(selectedId == R.id.robustCalib) {
                    pushMessage("Robust  calibration is performed");
                    String todisplay = discoveryActivity.meet.follow_to_robustly_calibrate();
                    pushMessage(todisplay);
                }
                Configuration.SHOULD_SIMPLY_CALIBRATE = false;
                Configuration.SHOULD_ROBUSTLY_CALIBRATE = false;
            }
        });



        rgApproach = view.findViewById(R.id.approachCalib);


        return view;
    }


    /**
     * send a message identifying the device and send the connexion graph
     */
    public void sendHelloandConnexion() {
        Log.e(TAG, "======================start exchanging sending hello and connexion");
        // send to the ap a tcp message describing who I am to the ap
        if (tcptransferManagerList != null){
            //  who I am = my vertex id , my name , my ip address (i.e., computer id)
            // extract the computed id from my ip address
            String local_id = tcptransferManagerList.get(0).socket.getLocalAddress().getHostAddress().substring(
                  Configuration.prefix.length()+1);

            String whoIam = Configuration.MSG_ID +  Me.getVertexId() + "," + Me.getDeviceName()  + "," + local_id  + "#";

            // add to the message the devices I know I am meeting with
            if (discoveryActivity.meet.meetwithS.size() ==0)
                pushMessage("- " +"Advertise the presence of "+Me.getVertexId() + "=" + Me.getDeviceName()
                        + "=" + local_id + " because noone knowns");
            else
                pushMessage("- " +"Advertise the presence of "+Me.getVertexId() + "=" + Me.getDeviceName() + "=" + local_id );

            // send the list of devices I am aware of
            for (int i = 0 ; i< discoveryActivity.meet.meetwithS.size() ; i++){
                whoIam+= discoveryActivity.meet.meetwithS.get(i).getVertexId() + "," + discoveryActivity.meet.meetwithS.get(i).name + "," + discoveryActivity.meet.meetwithS.get(i).deviceId   + "#";
                pushMessage("- "  + "Device already  discovered: " + discoveryActivity.meet.meetwithS.get(i).name + " =" +discoveryActivity.meet.meetwithS.get(i).getVertexId());
            }

            // if I am not the AP, send the message who i am to the ap as well as my connection graph to the ap
            if( Integer.parseInt(local_id ) != 1){
                for (int i=0 ;i< tcptransferManagerList.size(); i++){// try to find the ap client interface
                    pushMessage("- " + "size of the calibration group: " + (tcptransferManagerList.size() +1) );//local ui
                    if(  tcptransferManagerList.get(i).isAPclient == true ) {
                        Log.e(TAG, "- " + "Device is configured as AP client");
                        pushMessage("- " + "Device is configured as  AP client");//
                        tcptransferManagerList.get(i).write(whoIam.getBytes());
                        tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_CONNEXION, Configuration.connectFileName, Configuration.MSG_FILE_END);
                        pushMessage("- " + "Send Connection file to AP ");
                        Log.e(TAG, "Send connection file to AP");
                    }
                }
            }

            // if I am the AP, broadcast the message to all the peers
            if( Integer.parseInt(local_id ) == 1){
                pushMessage("- " + "AP starts broadcasting connection graph");
                Log.e(TAG, "AP Broadcast connection file to " + tcptransferManagerList.size() + " devices (group size is "  );
                for (int i = 0; i< tcptransferManagerList.size() ; i++){
                    pushMessage("- " + "AP broadcasts connection graph to " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());//local ui
                    tcptransferManagerList.get(i).write(whoIam.getBytes());
                    tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_CONNEXION, Configuration.connectFileName, Configuration.MSG_FILE_END);
                }
            }
        }
        else{
            Log.e(TAG, "tcp transfer is null ");
            pushMessage("Warning: " + "FILE TRANSFER IS NULL ");
            //todo get out of the fragment or better: realocate the filetransfermanager when there is a pause
        }
    }

    /**
     * Send a message containing only the connexion graph
     */
    public void sendOnlyConnexions()  {
        Log.e(TAG, "======================start exchanging   connexion");
        // send to the ap a tcp message describing who I am to the ap
        if (tcptransferManagerList != null){

            String local_id = tcptransferManagerList.get(0).socket.getLocalAddress().getHostAddress().substring(
                    Configuration.prefix.length()+1);

            // if I am not the AP, send the message who i am to the ap as well as my connection graph to the ap
            if( Integer.parseInt(local_id ) != 1){
                for (int i=0 ;i< tcptransferManagerList.size(); i++){// try to find the ap client interface
                    pushMessage("- " + "size " + tcptransferManagerList.size());//local ui
                    if(  tcptransferManagerList.get(i).isAPclient == true ) {
                        tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_CONNEXION, Configuration.connectFileName, Configuration.MSG_FILE_END);
                        pushMessage("- " + "send the connection graph to AP");//local ui
                        Log.e(TAG, "send the connection graph to AP");
                    }
                }
            }

            // if I am the AP, broadcast the message to all the peers
            if( Integer.parseInt(local_id ) == 1){
                pushMessage("- " + "start broadcasting connection graph (as an AP)");//local ui
                Log.e(TAG, "Broadcast connection file to " + tcptransferManagerList.size() + " devices" );
                for (int i = 0; i< tcptransferManagerList.size() ; i++){
                    pushMessage("- " + "broadcast connection graph to" + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());//local ui
                    tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_CONNEXION, Configuration.connectFileName, Configuration.MSG_FILE_END);
                }
            }
        }
        else{
            Log.e(TAG, "tcp transfer is null ");
            pushMessage("Warning: " + "FILE TRANSFER IS NULL ");//local ui
        }
    }
    /**
     * start another calibration
     */
    public void sendAnotherCalibration() {
        Log.e(TAG, "======================start exchanging sending hello and connexion");
        // send to the ap a tcp message describing who I am to the ap
        if (tcptransferManagerList != null){

            String local_id = tcptransferManagerList.get(0).socket.getLocalAddress().getHostAddress().substring(
                   Configuration.prefix.length()+1);


            String calibrationOrder = Configuration.MSG_TEST;

            // if I am the AP, broadcast the message to all the peers
            if( Integer.parseInt(local_id ) == 1){
                pushMessage("Test: " + "broadcast Calibration order (as AP)");//local ui
                Log.e(TAG, "Broadcast calibration order to " + tcptransferManagerList.size() + " devices" );
                for (int i = 0; i< tcptransferManagerList.size() ; i++){
                    pushMessage("Test: " + "broadcast calibration order to " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());//local ui
                    tcptransferManagerList.get(i).write(calibrationOrder.getBytes());
                    tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_CONNEXION, Configuration.connectFileName, Configuration.MSG_FILE_END);
                }
            }
        }
        else{
            Log.e(TAG, "tcp transfer is null ");
            pushMessage("Warning: " + "Calibration test IS NULL ");//local ui
            //todo get out of the fragment or better: realocate the filetransfermanager when there is a pause
        }
    }


    /**
     * send the order to start recording into sec seconds and return the date when it is scheduled
     * @param sec delay used before recording
     * @param offsetms delay offset before recording
     * @return when to start recording
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public  long sendSchedulingInfo(int sec, long offsetms /*offset in millisec*/){
        pushMessage("- " + "AP starts broadcasting a scheduling order ");//local ui
        Calendar cal = Calendar.getInstance();//get current time
        // define in how many millisecond from now the SYNCHRONISED recording will be scheduled
        int scheduleTime = sec * 1000 + (int) offsetms ;
        cal.add(Calendar.MILLISECOND, scheduleTime); // add few millisecond to current time

        // create a message that schedules the record
        String scheduleOrder = Configuration.MSG_TIME + String.valueOf(cal.getTimeInMillis());
        // send the order using tcp

        for(int i =0; i< tcptransferManagerList.size() ; i++){
            if(tcptransferManagerList.get(i)!= null) {
                tcptransferManagerList.get(i).write(scheduleOrder.getBytes());
                pushMessage("- " + "send scheduling order to " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());
            }
        }


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());
        String currentDateandTime = sdf.format(new Date(cal.getTimeInMillis()));
        return cal.getTimeInMillis();
    }

    /**
     * schedule the sound recording that has to be performed locally
     * @param meet meeting information
     * @param synchronised_when when to record
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void scheduleRecord(Meet meet, long synchronised_when /*synchronised time in millisecond*/){

        Date now = new Date();// get local time
        //when = local time
        long waitDuring = synchronised_when   - now.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());

        Log.e(TAG, "at "+ sdf.format(new Date()) +  " schedule the recording at " + synchronised_when +
                "=" +sdf.format(new Date(synchronised_when))+ " => wait for " + waitDuring  + " ms" );
        pushMessage("DO: " + "date: "+ sdf.format(new Date()) +"=" + now.getTime() + " schedule at " + synchronised_when + " = "
                +sdf.format(new Date(synchronised_when))+ " wait for " + waitDuring + " ms");


        if(waitDuring <= 0){
            Log.e(TAG, "PROBLEM SLEEPING DURATION IS NEGATIVE");
            pushMessage("PROBLEM SLEEPING DURATION IS NEGATIVE");
            //start recording now
            try {
                discoveryActivity.meet.sleepingThread(0);
                discoveryActivity.meet.addSleepListener(this);//discoveryActivity
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                discoveryActivity.meet.sleepingThread(waitDuring);//discoveryActivity
                discoveryActivity.meet.addSleepListener(this);//discoveryActivity
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * is called when the  recorded sound has been stored in a file, then the file is sent
     * @param type
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void someoneReportedFileChange(int type) {
        hasAlreadyFinishedRecording = true; // tells that we have finished to record (it is written in a file)
        Date now = new Date();// get local time

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());//        String currentDateandTime = sdf.format(new Date());

        pushMessage("- " + "at " + sdf.format(new Date()) + "=" + now.getTime() + " sound is recorded");
        Log.e(TAG, "awake from writing the record" + discoveryActivity.meet.getlocalFile4NoiseName());
        //send the local sound using  tcp to all the nearby devices
        canCalibrate = true; // state that we recorded localy the file (and if we receive all the other file, we may calibrate
        // if i am the ap, send to all the clients i am connected with
        if (this.discoveryActivity.isGroupOwner == true) {
            for (int i = 0; i < tcptransferManagerList.size(); i++) {
                if (tcptransferManagerList.get(i) != null) {
                    pushMessage("- " + "AP sends the sound file " + discoveryActivity.meet.getlocalFile4NoiseName()
                            + " to " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());
                    String toprint = tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_RECORD, discoveryActivity.meet.getlocalFile4NoiseName(),
                            Configuration.MSG_FILE_END);
                    pushMessage("- " + toprint);
                }
            }
        }

        discoveryActivity.meet.getLocalFileManager4Noise().removeFileListener(this);

        //todo check if i already received the sound for the ap
        if (this.discoveryActivity.isGroupOwner == false) {// not an ap
            ProtocolState pr = discoveryActivity.meet.meetwithProtocolState.get(discoveryActivity.meet.getPosition(1));
            if (pr == null) {
                pushMessage("Warning: "+ "Cannot find the protocol state");
                Log.e(TAG, "Cannot find the protocol state of the AP ");
            } else {
                if (pr.receivingSoundCompleted == true) {
                    pushMessage("- send local sound");
                    Log.e(TAG, "Send the local sound");
                    clientSendSound();
                } else {
                    pushMessage("- cannot send local sound yet (AP did not sent it yet)");
                    Log.e(TAG, "cannot send the local sound yet (AP did not sent it yet)");
                }
            }
        }


        //establish the nunmber of sounds we have received and that is not corrupted
        int nb_of_remote_not_corrupted_sound = 0;
        int nb_of_remote_corrupted_sound = 0;

        // count the number of  remote sounds that have been received and that are not corrupted
        for (int i = 0; i < discoveryActivity.meet.meetwithS.size(); i++) {
            if (discoveryActivity.meet.meetwithProtocolState.get(i).receivingSoundCompleted && !discoveryActivity.meet.meetwithProtocolState.get(i).isSoundCorrupted)
                nb_of_remote_not_corrupted_sound++;
            if (discoveryActivity.meet.meetwithProtocolState.get(i).receivingSoundCompleted && discoveryActivity.meet.meetwithProtocolState.get(i).isSoundCorrupted)
                nb_of_remote_corrupted_sound++;
        }



        // if we did not recorded the sound locally, we cannot calibrate
        if (hasAlreadyFinishedRecording == false)
        {
            pushMessage("- " + "cannot calibrate yet because the sound is not yet recorded locally");
        }
        else
        {//we recorded the sound locally
            if (nb_of_remote_not_corrupted_sound == this.discoveryActivity.meet.meetwithS.size()) {
                pushMessage("- " + "calibration starts");
                Log.e(TAG, "I CAN CALIBRATE");

                String printing = this.discoveryActivity.meet.calibrate();
                pushMessage("- " + printing);

                //draw a button that ask if we save this calibration
                // determine if the device should calibrate or not, or if the calibration should be save
                if (Configuration.SHOULD_SIMPLY_CALIBRATE || Configuration.SHOULD_ROBUSTLY_CALIBRATE || Configuration.SHOULD_BE_SAVED)
                {
                    //display a button : calibrate/proceed and a cancel button
                    cancelButton.setVisibility(View.VISIBLE);
                    calibrateButton.setVisibility(View.VISIBLE);

                    // the device should calibrate (using robust calibration)
                    if (Configuration.SHOULD_ROBUSTLY_CALIBRATE || Configuration.SHOULD_SIMPLY_CALIBRATE ) {
                        calibrateButton.setText("Calibrate");
                        if (Configuration.SHOULD_ROBUSTLY_CALIBRATE){
                            //radio button set to robust calibration
                            simple_calibrate_radio.setChecked(false);
                            simple_calibrate_radio.setVisibility(View.VISIBLE);
                            robust_calibrate_radio.setVisibility(View.VISIBLE);
                            robust_calibrate_radio.setChecked(true);
                        }
                        if (Configuration.SHOULD_SIMPLY_CALIBRATE){
                            simple_calibrate_radio.setChecked(true);
                            robust_calibrate_radio.setChecked(false);
                            simple_calibrate_radio.setVisibility(View.VISIBLE);
                            robust_calibrate_radio.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if (Configuration.SHOULD_BE_SAVED == true){
                            calibrateButton.setText("Proceed");
                        }
                    }
                }

                Configuration.SHOULD_SIMPLY_CALIBRATE = false;
                Configuration.SHOULD_ROBUSTLY_CALIBRATE = false;
                Configuration.SHOULD_BE_SAVED = false;


                canCalibrate = false;



                this.discoveryActivity.prepareNextCalibration();
            }
            else {//we already recorded the local sound but there Are 2 options : one file at least is corrupted or we did not received all
                ////////////////////////

                if ( nb_of_remote_corrupted_sound ==0 ){
                    pushMessage("Cannot calibrate yet, no corrupted sound but  number of sound received  " +
                            nb_of_remote_not_corrupted_sound  +   " <  meeting size: " + discoveryActivity.meet.meetwithS.size() );
                }else{// we will never calibrate but I go to next calibration only if I received all the file (corrupted or not)

                    if ( nb_of_remote_corrupted_sound + nb_of_remote_not_corrupted_sound ==  discoveryActivity.meet.meetwithS.size()){
                        pushMessage("- " + "Calibration is aborted because "+ nb_of_remote_corrupted_sound +
                                "  file(s) is/are corrupted");
                        discoveryActivity.prepareNextCalibration();
                    }
                }

                //////////////////////////

                pushMessage("- " + "cannot calibrate yet:  the sound has been provided by " +
                        nb_of_remote_not_corrupted_sound + " (remote) devices while meeting size is " + this.discoveryActivity.meet.meetwithS.size());
            }
        }









        if (hasAlreadyFinishedRecording)
        {
            if (nb_of_remote_not_corrupted_sound == this.discoveryActivity.meet.meetwithS.size()) {
                pushMessage("- " + "calibration starts");
                Log.e(TAG, "I CAN CALIBRATE");
                String printing = this.discoveryActivity.meet.calibrate();
                pushMessage("- " + printing);
                canCalibrate = false;
                this.discoveryActivity.prepareNextCalibration();
                // we calibrate -> we cannot calibrate anymore
            } else {
                pushMessage("- " + "cannot calibrate yet:  the sound has been provided by " +
                        nb_of_remote_not_corrupted_sound + " (remote) devices while meeting size is " + this.discoveryActivity.meet.meetwithS.size());
            }
        } else {
            pushMessage("- " + "cannot calibrate yet because the sound is not yet recorded locally");
        }
    }


    /**
     * Send the sound (the dvice is a client and not the wifi direct AP)
     * so the sound should be sent to the ap
     */
    public void clientSendSound(){
        // todo deal with the removal of a group member during rdv

        // i am not an ap, so I send to all the clients i am connected with
        //todo try to synchronise sound with the ap
        Log.e (TAG,"start synchronising sound with ap ");
        synchroniseSoundWithAP();

        //  start the socket clients to send the sound files to all the other group members
        Log.e(TAG, "start the clients sockets to send the local sound file to all the group members ");
        // send the local sound to the ap and to the other devices (using the client socket i started purposely
        // i should not use the server socket if i am not the ap

        pushMessage("- " + " size of tcp " + tcptransferManagerList.size());
        Log.e(TAG, " Size of tcp " + tcptransferManagerList.size());

        for (int i = 0 ; i< tcptransferManagerList.size() ; i++){
            Log.e(TAG, "explore interface " + i);
            pushMessage("- " + "find interface " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());
            Log.e(TAG, "find interface " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());

            // send to the ap the sound
            if(tcptransferManagerList.get(i).isAPclient == true ){
                tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_RECORD ,discoveryActivity.meet.getlocalFile4NoiseName(), Configuration.MSG_FILE_END );
                pushMessage("- " + " send sound to AP " + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());
            }
            //send the sound to the other devices using only client socket
            if(tcptransferManagerList.get(i).isAPclient == false && tcptransferManagerList.get(i).isProvidedByServer == false){
                tcptransferManagerList.get(i).writeChunkFile(Configuration.MSG_RECORD ,discoveryActivity.meet.getlocalFile4NoiseName(), Configuration.MSG_FILE_END );
                pushMessage("- " + "AP sends sound to" + tcptransferManagerList.get(i).socket.getRemoteSocketAddress().toString());
                // todo remove all the client socket
                //tcptransferManagerList.remove(i); i= i-1;  // tcptransferManagerList.get(i).close(); // close the client socket
            }
        }
//        pushMessage("Me: " + "todo : send to other peers  "  );

    }

    /**
     * Synchronise the sound with the sound that has been provided by te ap
     */
    public void synchroniseSoundWithAP(){
        //todo check if i received the sound from the ap and if I recorded the sound
        //////////////////////

        if (this.discoveryActivity.isGroupOwner == false) {
            ProtocolState pr = discoveryActivity.meet.meetwithProtocolState.get(discoveryActivity.meet.getPosition(1));
            if (pr == null) {
                pushMessage("Cannot find the protocol state of the AP ");
                Log.e(TAG, "Cannot find the protocol state of the AP ");
            } else {
                if (pr.receivingSoundCompleted == true) {
                    pushMessage("Synchronise local sound with AP sound");
                    Log.e(TAG, "Synchronise local sound with AP sound");

                    Log.e (TAG,"extract local file ");
                    //get the local sound
                    DoubleBuffer localSound = discoveryActivity.meet.getLocalFileManager4Noise().getSound();
                    // correlate the sample to determine the time offset between the two sounds
                    for (int i = 0; i < discoveryActivity.meet.meetwithS.size(); i++) {
                        //check if i am the ap
                        if (discoveryActivity.meet.meetwithS.get(i).deviceId == 1 ) {
                            pushMessage("for device " + discoveryActivity.meet.meetwithS.get(i).getVertexId()
                                    + " id: " + discoveryActivity.meet.meetwithS.get(i).deviceId + ", round trip delay: " +
                                    discoveryActivity.meet.roundTripDelay + "ms" +
                                    ", offset: " +  discoveryActivity.meet.getTimeOffset());

                            Log.e(TAG,"for device " + discoveryActivity.meet.meetwithS.get(i).getVertexId()
                                    + " id: " + discoveryActivity.meet.meetwithS.get(i).deviceId + ", round trip delay: " + discoveryActivity.meet.roundTripDelay + "ms" +
                                    ", offset: " +  discoveryActivity.meet.getTimeOffset());

                            //get the sound for the AP
                            DoubleBuffer remoteSound = discoveryActivity.meet.files4noise.get(i + 1).getSound();

                            Log.e(TAG, "Create a filter delaying at max with " + discoveryActivity.meet.roundTripDelay);
                            pushMessage("Create a filter delaying at max with " + discoveryActivity.meet.roundTripDelay);

                            Filter filter = new Filter(localSound, remoteSound, discoveryActivity.meet.roundTripDelay);
                            pushMessage("for " + discoveryActivity.meet.meetwithS.get(i).getVertexId() +
                                    " offset (#samples): " + filter.offset + "\n normalised offset (#samples):" + filter.norm_offset);

                            Log.e(TAG,"for " + discoveryActivity.meet.meetwithS.get(i).getVertexId() +
                                    " offset (#samples): " + filter.offset + "\n normalised offset (#samples):" + filter.norm_offset);

                            // shift the starting time
                            double delay;
                            if (Configuration.isAveraging = true) {
                                delay = filter.norm_offset * Configuration.samplingDurationSec * 1000;
                            } else {
                                delay = Record._rate * Configuration.samplingDurationSec * filter.norm_offset;
                            }

                            pushMessage("for " + discoveryActivity.meet.meetwithS.get(i).getVertexId() +
                                    "normalised offset (ms) : " + delay);

                            // rewrite local sound file using the normalised delay
                            //compute the delay in milisecond
                            double slot_millisec;
                            if (Configuration.isAveraging == false)
                                slot_millisec = (double) 1000 / (double) Record._rate; // compute the delay between two recording
                            else // save the avg recording into a file
                                slot_millisec = Configuration.samplingDurationSec * 1000; // 0.125 second = 0.125 *1000 = 125 ms

                            double startTime = discoveryActivity.meet.getLocalFileManager4Noise().getStartTime();
                            Log.e(TAG, "before delaying, local start time is : " + startTime);
                            pushMessage("before delaying local start time is : " + startTime);

                            Log.e(TAG, "before delaying, local start time should be after : " + startTime+ " + " + delay + "=" + (startTime + delay));
                            pushMessage("before delaying local start time should be after: " + startTime + " + " + delay + "=" +  (startTime + delay));

                            FileManager localFileManager = new FileManager( discoveryActivity.meet.getlocalFile4NoiseName(), false, discoveryActivity);
                            localFileManager.write_calibration_buffer(localSound,  startTime + delay , slot_millisec);

                            Log.e(TAG, "after delaying, local start time is : " + discoveryActivity.meet.getLocalFileManager4Noise().getStartTime());
                            pushMessage("before delaying local start time is : " + discoveryActivity.meet.getLocalFileManager4Noise().getStartTime());

                            localSound.clear();
                        }



                    }
                }
            }
        }
    }




    /**
     * is called when the sound has to be recorded, it starts recording the sound
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void someoneReportedAwake() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());
        String currentDateandTime = sdf.format(new Date());
        pushMessage("- " +"at "+currentDateandTime +"=" +new Date().getTime() +"start recording for " + Configuration.recordDuration_ms + " ms");
        discoveryActivity.meet.getLocalSoundRecording(); // record the sound
        //add a listener to know when the sound recording is complete (i.e. stored in a file)
        discoveryActivity.meet.getLocalFileManager4Noise().addFileListener(this);//discoveryActivity
        discoveryActivity.meet.removeSleepListener(this); // remove the sleep listener //discoveryActivity
    }

    /**
     * interface dertermining the target
     */
    public interface MessageTarget {
        Handler getHandler();
    }

    /**
     * init method
     * @param obj discovery activity
     */
    public void setdiscoveryActivity(WiFiServiceDiscoveryActivity obj) {this.discoveryActivity  = obj; }

    /**
     * init the transffer manager
     * @param obj file transfer manager
     */
    public void setTCPTransferManager (FileTransferManager obj){
        // if i am  not the ap and I want to speak with ap , send to the ap  who i am and my connexion graph
        //when the ap will have received that, he will send to all who I am and his updated connexion graph
        Log.e(TAG, "set tcp handler, AP client= " + obj.isAPclient + " is server: " + obj.isProvidedByServer);
        //  pushMessage("SET Tcp handler: client= " + obj.isAPclient + " AP/server: " + obj.isProvidedByServer);
        this.tcptransferManagerList.add(obj); // this manager is either started to deal with the ap server or to deal with the client discussing with ap

        // pushMessage("Me: already set the tcp transfer manager: " + setedtcptransferManager + " is ap ? " + this.discoveryActivity.isGroupOwner);

        // i just started a client and i want to communicate to ap to say hello
        if (this.setedtcptransferManager == false /* at the beginning*/ && this.discoveryActivity.isGroupOwner == false) {
            sendHelloandConnexion();
        }

        // I am not connecting to the ap, i am not a server that is just started
        // I am  a client connecting to another peer
        if (this.discoveryActivity.isGroupOwner == false && this.setedtcptransferManager == true /*no begining */ && obj.isProvidedByServer == false) {
            //    this.tcptransferManagerList.add(obj);
        }

        this.setedtcptransferManager = true;
    }

    /* message displayed */
    public void pushMessage(String readMessage) {
        adapter.add(readMessage);
        adapter.notifyDataSetChanged();

    }


    /**
     * ArrayAdapter to manage the messages that are displayed in different colors depending
     *    on how the message start (blue if the message start with - , red if it begin by warning ect...
     */
    public class CalibrationMessageAdapter extends ArrayAdapter<String> {
        List<String> messages = null;
        public CalibrationMessageAdapter(Context context, int textViewResourceId, List<String> items) {
            super(context, textViewResourceId, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            String message = items.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText = v.findViewById(android.R.id.text1);
                //  nameText.setTextAppearance(getActivity(), R.style.testText);

                nameText.setTextSize(11);

                if (nameText != null) {
                    nameText.setText(message);
                    if (message.startsWith("- ")) {
                        nameText.setTextAppearance(getActivity(), R.style.normalText);
                    }
                    if(message.startsWith("Warning: ")){
                        nameText.setTextAppearance(getActivity(), R.style.boldText);
                    }

                    if(message.startsWith("Test: ")){
                        nameText.setTextAppearance(getActivity(), R.style.testText);
                    }


                    if (  message.startsWith("- ") == false &&  message.startsWith("Warning: ") == false  && message.startsWith("Test: ") == false)  {
                        nameText.setTextAppearance(getActivity(), R.style.boldText);
                    }
                }
            }
            return v;
        }
    }
}