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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.colibris.R;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.calib.Meet;

/**
 * The RecordMessageActivity class is an activity that permits to record sound
 */
public class RecordMessageActivity extends AppCompatActivity /*implements NoiseListener, FileListener*/ {
    /**
     * log related information
     */
    private static final String LOG_TAG = "AudioRecordTest";
    /**
     * Meeting
     */
    Meet meet ;
    /**
     * button
     */
    private RecordButton mRecordButton = null;
    /**
     * button
     */
    private MediaRecorder mRecorder = null;
    /**
     * media player
     */
    private MediaPlayer mPlayer = null;
    /**
     * text view to display data to the end user
     */
    public TextView tv = null;

    /**
     * Activity creation
     * @param savedInstanceState instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_record_message);
        Device device2meet  = new Device(1,"device1", -1);
        meet = new Meet(device2meet,this, true /*append*/);


        LinearLayout LL_Outer = (LinearLayout) findViewById(R.id.layoutRecord);
        tv = new TextView(this);
        LL_Outer.addView(tv);

        tv.setText("Initialise the recording\n" );
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    /**
     * activity is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.d(LOG_TAG, "stop the recording activity");
        meet.measurement.stop();
        meet.end();
    }

    /**
     * start/stop the recording
     * @param start determine if we should start or stop the recording
     */
    private void onRecord(boolean start) {
        if (start) {
            startMeetRecording();
        }
    }

    /**
     * simulate a meeting where only one device record
     */
    private void startMeetRecording() {
        tv.append("Start recording for "  + Configuration.recordDuration_ms +" ms, ");
        tv.append("sampling  duration: " +  Configuration.samplingDurationSec + " sec \n");
        Configuration.isRecordActivity = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                meet.getLocalSoundRecording2ConsecTests(Configuration.numberofconsecutivecalibrationtocarry); // record the sound nTests
            }
        };
        new Thread(runnable).start();
        tv.append("Add listener to record noise and save it \n");
        //tv.append("Wait for add listener to record noise  and save it \n");
        tv.append("Please wait until the sound is recorded, i.e., "  + Configuration.recordDuration_ms +" ms \n");
    }

    /**
     * button pressed to stard recording
     * @param view view
     */
    public void recordMessage(View view) {
        //send the time that has been obtained)
        startMeetRecording();

    }


    /**
     * Button to start/stop recording
     */
    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                } else {
                    setText("Start");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start");
            setOnClickListener(clicker);
        }
    }

    /**
     * Activity is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
