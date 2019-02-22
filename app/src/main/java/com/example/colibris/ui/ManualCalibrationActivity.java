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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.colibris.configuration.Me;
import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.multi.hypergraph.HyperConnection;
import com.example.colibris.calib.Meeting;
import com.example.colibris.multi.graph.DirectedEdge;
import com.example.colibris.multi.hypergraph.HyperEdge;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.colibris.configuration.Configuration.REFERENCENB;
import static com.example.colibris.configuration.Configuration.VERTEXNB;

/**
 * Manual calibration is performed
 */
public class ManualCalibrationActivity extends AppCompatActivity {
    /**
     * text view usefull to display calibration related parameters
     */
    EditText interceptText, betaText, mean, std,r_square,sum; // get beta
    /**
     * local device
     */
    Me me;
    /**
     * log information
     */
    String TAG = "Manual Calibration Activity";

    /**
     * Activity creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_calibration);

        me = new Me(this);
        //Set the manual Calibration Parameters that are needed   to run a regression
        betaText = findViewById(R.id.editbetatext);// used to get the beta parameter
        betaText.setText(String.valueOf(Configuration.MANUAL_SLOPE));
        interceptText = findViewById(R.id.edittextintercept); // used to get the intercept parameter
        interceptText.setText(String.valueOf(Configuration.MANUAL_INTERCEPT));
        mean = findViewById(R.id.editmean);
        mean.setText(String.valueOf(Configuration.MANUAL_MEAN_RESIDUAL));
        std = findViewById(R.id.editstd);

        std.setText(String.valueOf(Configuration.MANUAL_STD_RESIUDAL));
        r_square = findViewById(R.id.edittextr2);

        r_square.setText(String.valueOf(Configuration.MANUAL_R_SQUARED));
        sum =findViewById(R.id.editsum);
        sum.setText(String.valueOf(Configuration.MANUAL_SUM_RESIDUAL));

        // determine if the check box is set or not
        CheckBox mCheckBox = findViewById(R.id.multiHopCalibration);
        mCheckBox.setChecked(Configuration.isMultiHopCalibration);
    }

    /**
     * activity get stop
     */
    protected void onStop() {
        appliConfiguration();
        super.onStop();
    }

    /**
     * store the calibration information
     */
    public void appliConfiguration() {
        /*extract the   Values related to the manual Calibration*/
        String beta_s = betaText.getText().toString(); // get the beta to apply to calibrate
        String intercept_s = interceptText.getText().toString(); // get the intercept to apply to calibrate

        String mean_s = mean.getText().toString();
        String std_s = std.getText().toString();
        String sum_s = sum.getText().toString();
        String r_squared_s = r_square.getText().toString();


        Configuration.MANUAL_SLOPE  = Double.parseDouble(beta_s);
        Configuration.MANUAL_INTERCEPT = Double.parseDouble(intercept_s);
        Configuration.IS_CALIBRATED= true;
        Configuration.MANUAL_MEAN_RESIDUAL = Double.parseDouble(mean_s);
        Configuration.MANUAL_STD_RESIUDAL = Double.parseDouble(std_s);
        Configuration.MANUAL_SUM_RESIDUAL = Double.parseDouble(sum_s);
        Configuration.MANUAL_R_SQUARED = Double.parseDouble(r_squared_s);
        /**
         * if that is a multi hop calibration then the hypergraphis updated  with
         * i add a hyperedge between the local device and the consolidated node
         * the slope and the intercept
         */
        if (Configuration.isMultiHopCalibration == true){
            // add an edge between the sensor and the consolidated sensor
            Date now = new Date();
            long time_now =  now.getTime();

            Meeting meeting = new Meeting(Configuration.MANUAL_INTERCEPT/*intercept*/, Configuration.MANUAL_SLOPE /*slope*/,
                    Configuration.MANUAL_STD_RESIUDAL ,Configuration.MANUAL_MEAN_RESIDUAL  ,
                    Configuration.recordDuration_ms /*meet duration*/,
                    Configuration.MANUAL_R_SQUARED , time_now,
                    Configuration.MANUAL_SUM_RESIDUAL
            );
            //todo this works but this should be cleaned
            DirectedEdge innerEdge = new DirectedEdge( me.localDevice.getVertexId(),VERTEXNB + REFERENCENB,
                    meeting ) ;
            Log.e(TAG, "add inner edge " +  innerEdge);
            HyperConnection connection = me.getLocalconnectionGraph();
            connection.getFromFile(this);
            List<DirectedEdge> innerEdges = new ArrayList<>();
            innerEdges.add(innerEdge);

            HyperEdge hyperEdge = new HyperEdge(innerEdges);

            connection.addHyperEdge(hyperEdge);
            Log.e(TAG, "my connection is" +  connection.toString());

            connection.toFile(this);
            Log.e(TAG, "my connection is" +  connection.toString());








        }


    }

    /**
     * Check the box that determine whether multi hop calibration should be performed in the future
     * @param view
     */
    public void multiHopCalibration_message(View view) {
        CheckBox cb = findViewById(R.id.multiHopCalibration);
        Configuration.isMultiHopCalibration = cb.isChecked();
        Log.e(TAG, "MultiHop Calibration Selected " + Configuration.isMultiHopCalibration);
    }


}
