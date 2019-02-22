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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.colibris.configuration.Me;
import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;

/*public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
*/

/**
 * The OptionActivity class corresponds to an activity used to parametrise the application
 */
public class OptionActivity extends AppCompatActivity {
   // TextView textView;
    /**
     * local device
     */
    Me me;
    /**
     * Edit text relating to the calibration duration
     */
    EditText durationText;
    /**
     * Edit text
     */
    EditText subwindowText;
    /**
     * Edit text relating to the number of measurements that should not be considered
     */
    EditText shiftText;
    /**
     * Edit text relating to the number of consecutive calibrations that should be performed
     */
    EditText testNbText; // get the number of tests
    /**
     * prefix of the file in which the test related file should be stored (that is a zip file)
     */
    EditText nameTest; // get the number of tests
    /**
     * Various editex
     */
    EditText numberOfNodes, numberOfReferenceNodes, ntpport, serverport,soundport;
    /**
     * Warning considering the shift parameter that has been entered
     */
    TextView warningShiftText;
    /**
     * log related parameter
     */
    String TAG = "OptionsActivity";
    /**
     * magnitude
     */
    private Double magnitude;
    /**
     * subwindow
     */
    private Double subwindow2double;
    /**
     * shift of the mesurments
     */
    private int shift;

    /**
     * Ativity creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        //initialize the controls
        final RadioGroup rgApproach = findViewById(R.id.rgApproachCalib);
        final RadioGroup rgWeight = findViewById(R.id.rgWeightCalib);

        ///Although the Calibation Type Radio Buttons, set default to false for all the following radio buttons
        for (int i = 0; i < rgWeight.getChildCount(); i++) {
            rgWeight.getChildAt(i).setEnabled(false);
        }

        for (int i = 0; i < rgApproach.getChildCount(); i++) {
            rgApproach.getChildAt(i).setEnabled(false);
        }

        findViewById(R.id.noWeightDistanceAsVariable).setEnabled(false);

        RadioButton rbLocationCalib = findViewById(R.id.rbLocationCalib);
        rbLocationCalib.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton radioButton, boolean checked) {
                //basically, since we will set enabled state to whatever state the checkbox is
                //therefore, we will only have to setEnabled(checked)
                for (int i = 0; i < rgApproach.getChildCount(); i++) {
                    rgApproach.getChildAt(i).setEnabled(checked);
                }

                for (int i = 0; i < rgWeight.getChildCount(); i++) {
                    rgWeight.getChildAt(i).setEnabled(checked);
                }
            }
        });

        RadioButton rbSimpleCalib = findViewById(R.id.rbSimpleCalib);
        rbSimpleCalib.setChecked(true);

        RadioButton rbLinearApproach = findViewById(R.id.linearApproach);
        rbLinearApproach.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton radioButton, boolean checked) {
                //basically, since we will set enabled state to whatever state the checkbox is
                //therefore, we will only have to setEnabled(checked)
                findViewById(R.id.noWeightDistanceAsVariable).setEnabled(checked);
            }
        });



        Log.e("Options", "create Option Activity");
        //   Log.e("Application path:  " , file.getAbsolutePath() + "\n");

        // initialise the device properties
        me = new Me(this);

        durationText = findViewById(R.id.edittextduration);// used to get the duration parameter
        durationText.setText(String.valueOf(Configuration.recordDuration_ms / 1000)); //setting the configuration value of Duration
        subwindowText = findViewById(R.id.edittextsubwindow);// used to get the duration parameter
        subwindowText.setText(String.valueOf(Configuration.samplingDurationSec)); //setting the configuration value of the sampling duration
        shiftText = findViewById(R.id.editshift);// used to get the duration parameter
        shiftText.setText(String.valueOf(Configuration.SHIFT_IN_SAMPLE));
        Log.e(TAG, "Duration Text: " + durationText.getText().toString() + " and subwindow: "
                + subwindowText.getText().toString() + " (ms)");
        //Test parameters so as to have a name and number of records
        nameTest = findViewById(R.id.editnamerecordtext);
        nameTest.setText(String.valueOf(Configuration.nameRecordTest)); //setting the configuration value of the name of the test
        testNbText = findViewById(R.id.editnumberrecordtext);
        testNbText.setText(String.valueOf(Configuration.numberofconsecutivecalibrationtocarry)); //setting the configuration value of the number of consecutive calibrations
        //Edit Values related to the MultiHopCalibration

        numberOfNodes = findViewById(R.id.editnumberofnodes);
        numberOfReferenceNodes = findViewById(R.id.editnumberofreferencenodes);
        CheckBox mCheckBox = findViewById(R.id.multiHopCalibration);
        mCheckBox.setChecked(Configuration.isMultiHopCalibration);

        ntpport = findViewById(R.id.editntpport);
        serverport = findViewById(R.id.editserverPort);
        soundport = findViewById(R.id.editfileport);
        ntpport.setText(String.valueOf(Configuration.ntp_ports));
        serverport.setText(String.valueOf(Configuration.BROADCAST_SERVER_PORT));
        soundport.setText(String.valueOf(Configuration.SERVER_PORT));


        subwindowText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                warningShiftText = findViewById(R.id.warningshift);
                String subwindowTextValue = subwindowText.getText().toString();

                if (subwindowTextValue.isEmpty())
                    shift = 0;
                else {
                    subwindow2double = Double.parseDouble(subwindowTextValue);
                    magnitude = Math.abs(Math.log10(subwindow2double));
                    if (magnitude > 1) {
                        shift = (int) (Math.pow(10, 2 * magnitude - 1) * Double.parseDouble(subwindowText.getText().toString()));
                    } else {
                        shift = 1; // ????
                    }
                }
                warningShiftText.append(String.valueOf(shift));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Activity get stoped
     */
    @Override
    protected void onStop() {

        appliConfiguration();
        super.onStop();
    }

    /**
     * get the parameters given by the end user
     */
    public void appliConfiguration() {
        /*Option Values related to the Configuration file of the App*/
        String duration_s = durationText.getText().toString(); // get the duration to save in the config file
        String subwindow_s = subwindowText.getText().toString(); // get the subwindow duration to save in the config file
        String shiftTime = shiftText.getText().toString(); // get the shift time to save in the config file
        /*Option Edit values related to Record Tests Activity*/
        String testNb = testNbText.getText().toString(); // get the number of consecutive records tests to apply
        String nameTest_s = nameTest.getText().toString(); // get the name of the test to apply to record tests
        /*Option Edit values related to Record Tests Activity*/
        String numberOfNodes_s = numberOfNodes.getText().toString(); // get the number of consecutive records tests to apply
        String numberOfReferencesNodes_s = numberOfReferenceNodes.getText().toString(); // get the name of the test to apply to record tests

        String ntport_s = ntpport.getText().toString();
        String server_port_s = serverport.getText().toString();
        String soundport_s = soundport.getText().toString();
        Configuration.ntp_ports = Integer.parseInt(ntport_s);
        Configuration.BROADCAST_SERVER_PORT = Integer.parseInt(server_port_s);
        Configuration.SERVER_PORT = Integer.parseInt(soundport_s);

        Configuration.numberofconsecutivecalibrationtocarry = Integer.parseInt(testNb);
        Configuration.nameRecordTest = nameTest_s;
        Configuration.recordDuration_ms = Long.parseLong(duration_s) * 1000;
        Configuration.samplingDurationSec = Double.parseDouble(subwindow_s);
        Configuration.SHIFT_IN_SAMPLE = Integer.parseInt(shiftTime);

        Configuration.VERTEXNB = Integer.valueOf(numberOfNodes_s);
        Configuration.REFERENCENB = Integer.valueOf(numberOfReferencesNodes_s);




        Log.e(TAG, "Duration Test: " + durationText.getText().toString() + " and Subwindow of: " + subwindowText.getText().toString() + " (ms)");
    }




    /**
     * extract the parameters when the button set is pressed
     * @param view
     */
    public void set_Message(View view) {
        appliConfiguration();
    }




    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void linear_weight_CalibMessage(View view) {
        Configuration.isFilteredData2LinearRegression = true;
        Configuration.isFilteredData2GeoRegression = false;
        Log.e(TAG, "Filter Error and Distance Button value: " + Configuration.isFilteredData2LinearRegression + "Geo Button: " + Configuration.isFilteredData2GeoRegression);
    }


    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void location_weight_CalibMessage(View view) {
        Configuration.isFilteredData2GeoRegression = true;
        Configuration.isNotFilteringDistanceIsVariable = false;
        Log.e(TAG, "Geo Filter Error and Distance Button value: " + Configuration.isFilteredData2GeoRegression);
    }


    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void weight_LogMessage(View view) {
        Configuration.weightingFunction = 1;
        Configuration.isNotFilteringDistanceIsVariable = false;
        Log.e(TAG, "The weight function is logarithmic: " + Configuration.weightingFunction);

    }

    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void weight_ExpMessage(View view) {
        Configuration.weightingFunction = 0;
        Configuration.isNotFilteringDistanceIsVariable = false;
        Log.e(TAG, "The weight function is exponential: " + Configuration.weightingFunction);
    }

    /**
     * extract the information when the button set is pressed
     * @param view
     */
     public void noWeight_DistanceAsVariable(View view) {
        Configuration.isNotFilteringDistanceIsVariable = true;
        Log.e(TAG, "The weight function is logarithmic: " + Configuration.isNotFilteringDistanceIsVariable);

    }

    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void location_Message(View view) {
        Configuration.isLocationAwareCalibration = true;
        Log.e(TAG, "Location-aware Calibration: " + Configuration.isLocationAwareCalibration);
    }

    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void weighting_message(View view) {
        CheckBox cb = findViewById(R.id.recordandweight);

        Configuration.isRawSoundData = cb.isChecked() != true;

        Log.e(TAG, "Raw sound traited ?  " + Configuration.isRawSoundData);
    }

    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void multiHopCalibration_message(View view) {
        CheckBox cb = findViewById(R.id.multiHopCalibration);
        Configuration.isMultiHopCalibration = cb.isChecked();
        Log.e(TAG, "MultiHop Calibration Selected " + Configuration.isMultiHopCalibration);
    }


    /**
     * extract the information when the button set is pressed
     * @param view
     */
    public void zip_message(View view) {
        CheckBox cb = findViewById(R.id.zipCalibration);
        Configuration.SoundFileAreZipped = cb.isChecked();
        Log.e(TAG, "Calibration are zipped ?   " + Configuration.SoundFileAreZipped);
    }

    /**
     * start pressing the button to start recording noise
     * @param view
     */
    public void record_Message(View view) {
        set_Message(view);

        Intent intent = new Intent(this, RecordMessageActivity.class);
        startActivity(intent);
    }

}