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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.colibris.configuration.Me;
import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.calib.FileManager;
import com.example.colibris.multi.hypergraph.HyperConnection;
import com.example.colibris.calib.Meet;
import com.example.colibris.calib.Meet2regress;
import com.example.colibris.calib.Meet2regressGeo;
import com.example.colibris.calib.Meeting;
import com.example.colibris.multi.graph.DirectedEdge;
import com.example.colibris.multi.hypergraph.HyperEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * CalibrateActivity activity
 */
public class CalibrateActivity extends AppCompatActivity {
    /**
     * Meeting that take place to calibrate
     */
    Meet meets;
    /**
     * Text veiw of the UI
     */
    private TextView text ;
    /**
     * Log related information
     */
    private static final String TAG = "Calibrate  activity";

    /**
     * Activity creation
     * @param savedInstanceState instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* Taking each devices in a meeting, extracting the noise data, so as to call a calibration regression
         */
        Log.d(TAG, "Beginning Regress Activity");
        text = new TextView(this);
        text = findViewById(R.id.calibrate2_message);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());


        // new meeting is created in order to proceed the calibration beetween the devices
        Log.d(TAG, "Run meet class and take record data from File Manager");
        this.meets = new Meet(this);
        Bundle extras = getIntent().getExtras();

        /*todo extract the number of devices we are calibrating with
         * for each device, add the related regression parameter
         * in the hyperconnection, add the hyeredge */


        //////////////////////////////

        /* Take each devices in a meeting */

        // new meeting is created in order to call proceed the calibration beetween the devices
        //   Log.d(TAG, "Running meet class and taking record data from File Manager");

        Device device2meet;
        if (extras != null) {
            String vertexId = extras.getString(Configuration.VERTEX_MESSAGE);
            //     Log.e(TAG, "Indexes :" + Arrays.toString(vertexId.split("-")));
            String[] index = vertexId.split("-");
            //  Log.e(TAG, "Vertex id Meet :" + vertexId);
            String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE);
            for (int i = 0; i < index.length; i++) {
                Integer index_i = new Integer(index[i]);
                device2meet = new Device(index_i, device_id, 0);
                this.meets.add(device2meet, this /*cyx*/, false /*append*/); // add the device if the device is not already in
                // display
            }
        }
        ////////////////////////////////
        text.append( " *********************\n");


        HyperEdge hyperEdge= new HyperEdge();
        List<DirectedEdge> directedEdges = new ArrayList<>();
        Me me = new Me(this);


        if(Configuration.isLocationAwareCalibration == false) {
            Meet2regress regress = new Meet2regress(extras, this);
            text.append(regress.MultivariateRegressionoutput);
            text.append(regress.robustRegressionoutput);
            text.append(regress.geoMeanRegressionoutput);



            // add an hyperedge for this regression

            List<FileManager> remoteFilelist = new ArrayList<FileManager>();
            String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE);
            for (int j = 0; j < this.meets.meetwithS.size(); j++) {
                //   Log.e(TAG, "Devices in a meeting calibration: " + Arrays.toString(this.meet.meetwithS.toArray()));
                remoteFilelist.add(j, regress.device2data(this.meets, device_id, j, this));
            }

            for (int i = 0 ; i < this.meets.meetwithS.size(); i++){
                Log.e(TAG, " add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );
                text.append( " \n add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );


                double startTime =  meets.files4noise.get(i).getStartTime();// i+1
                Meeting meeting;
                //todo get start time
                if (Configuration.APPLY_ROBUST_REGRESSION_IN_HYPERGRAPH == false){
                    meeting = new Meeting(regress.multivariateregression.beta[0]/*intercept*/,regress.multivariateregression.beta[i+1] /*slope*/,
                            regress.multivariateregression.parametersStdErrors[i]  /*std error*/ ,
                            regress.getMean( regress.multivariateregression.residuals) /*mean squarred error*/ ,
                            Configuration.recordDuration_ms /*meet duration*/,
                            regress.multivariateregression.OLSMultiReg.calculateRSquared() /*R²*/,
                            startTime,
                            regress.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares());

                }else{
                    meeting = new Meeting(regress.multivariateregression.cleanedMultivariateRegression.beta[0]/*intercept*/,
                            regress.multivariateregression.cleanedMultivariateRegression.beta[i+1] /*slope*/,
                            regress.std(regress.multivariateregression.cleanedMultivariateRegression.residuals ) /*std error*/ ,
                            regress.getMean( regress.multivariateregression.cleanedMultivariateRegression.residuals) /*mean squarred error*/ ,
                            Configuration.recordDuration_ms /*meet duration*/,
                            regress.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateRSquared() /*R²*/,
                            startTime,
                            regress.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateTotalSumOfSquares()
                    );
                }
                DirectedEdge innerEdge = new DirectedEdge(this.meets.meetwithS.get(i).getVertexId(), me.localDevice.getVertexId(),meeting ) ;

                text.append( " \n add hyperedge " +  innerEdge.toString());
                directedEdges.add(innerEdge);
            }
        }


        if(Configuration.isLocationAwareCalibration == true)
        {
            if(Configuration.isFilteredData2LinearRegression == true){
                if(Configuration.isFilteredData2GeoRegression == false) {
                    Meet2regressGeo regressSimpleFilter = new Meet2regressGeo(extras, this);
                    Configuration.isNotFilteredMatrix = true;
                    Meet2regressGeo regressSimple = new Meet2regressGeo(extras, this);
                    text.append(regressSimple.MultivariateFilteredRegressionoutput);
                    text.append(regressSimpleFilter.filteredRobustRegressionoutput);
                    text.append(regressSimple.filteredRobustRegressionoutput);


                    //////////////////////////////////////////////////////////////////////////////////////////////

                    // add an hyperedge for this regression

                    List<FileManager> remoteFilelist = new ArrayList<FileManager>();
                    String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE);
                    for (int j = 0; j < this.meets.meetwithS.size(); j++) {
                        //   Log.e(TAG, "Devices in a meeting calibration: " + Arrays.toString(this.meet.meetwithS.toArray()));
                        remoteFilelist.add(j, regressSimpleFilter.device2data(this.meets, device_id, j, this));
                    }

                    for (int i = 0 ; i < this.meets.meetwithS.size(); i++){
                        Log.e(TAG, " add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );
                        text.append( " \n add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );


                        double startTime =  meets.files4noise.get(i).getStartTime();// i+1
                        Meeting meeting;
                        //todo get start time
                        if (Configuration.APPLY_ROBUST_REGRESSION_IN_HYPERGRAPH == false){
                            meeting = new Meeting(regressSimple.multivariateregression.beta[0]/*intercept*/,regressSimple.multivariateregression.beta[i+1] /*slope*/,
                                    regressSimple.std(regressSimple.multivariateregression.residuals ) /*std error*/ ,
                                    regressSimple.getMean( regressSimple.multivariateregression.residuals) /*mean squarred error*/ ,
                                    Configuration.recordDuration_ms /*meet duration*/,
                                    regressSimple.multivariateregression.OLSMultiReg.calculateRSquared() /*R²*/,
                                    startTime,
                                    regressSimple.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares()
                            );
                        }else{
                            meeting = new Meeting(regressSimple.multivariateregression.cleanedMultivariateRegression.beta[0]/*intercept*/,
                                    regressSimple.multivariateregression.cleanedMultivariateRegression.beta[i+1] /*slope*/,
                                    regressSimple.std(regressSimple.multivariateregression.cleanedMultivariateRegression.residuals ) /*std error*/ ,
                                    regressSimpleFilter.getMean( regressSimple.multivariateregression.cleanedMultivariateRegression.residuals) /*mean squarred error*/ ,
                                    Configuration.recordDuration_ms /*meet duration*/,
                                    regressSimple.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateRSquared() /*R²*/,
                                    startTime,
                                    regressSimple.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateTotalSumOfSquares()
                            );
                        }
                        DirectedEdge innerEdge = new DirectedEdge(this.meets.meetwithS.get(i).getVertexId(), me.localDevice.getVertexId(),meeting ) ;
                        text.append( " \n add inner Edge " +  innerEdge.toString());
                        directedEdges.add(innerEdge);
                    }
                    //////////////////////////////////////////////////////////////////////////////////////////////
                }


                if(Configuration.isFilteredData2GeoRegression == true) {
                    Meet2regressGeo regressGeoFilter = new Meet2regressGeo(extras, this);
                    Configuration.isNotFilteredMatrix = true;
                    Meet2regressGeo regressGeo = new Meet2regressGeo(extras, this);
                    text.append(regressGeoFilter.filteredGeoWeightedRegressionOutput);
                    text.append(regressGeo.filteredGeoWeightedRegressionOutput);


                    // add an hyperedge for this regression

                    List<FileManager> remoteFilelist = new ArrayList<FileManager>();
                    String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE);
                    for (int j = 0; j < this.meets.meetwithS.size(); j++) {
                        //   Log.e(TAG, "Devices in a meeting calibration: " + Arrays.toString(this.meet.meetwithS.toArray()));
                        remoteFilelist.add(j, regressGeo.device2data(this.meets, device_id, j, this));
                    }

                    for (int i = 0 ; i < this.meets.meetwithS.size(); i++){
                        Log.e(TAG, " add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );
                        text.append( " \n add inner Edge " +  this.meets.meetwithS.get(i).getVertexId() + " -> " + me.localDevice.getVertexId() );


                        double startTime =  meets.files4noise.get(i).getStartTime();// i+1
                        Meeting meeting;

                        if (Configuration.APPLY_ROBUST_REGRESSION_IN_HYPERGRAPH == false){
                            meeting = new Meeting(regressGeo.multivariateregression.beta[0]/*intercept*/,regressGeo.multivariateregression.beta[i+1] /*slope*/,
                                    regressGeo.std(regressGeo.multivariateregression.residuals ) /*std error*/ ,
                                    regressGeo.getMean( regressGeo.multivariateregression.residuals) /*mean squarred error*/ ,
                                    Configuration.recordDuration_ms /*meet duration*/,
                                    regressGeo.multivariateregression.OLSMultiReg.calculateRSquared() /*R²*/,
                                    startTime,
                                    regressGeo.multivariateregression.OLSMultiReg.calculateTotalSumOfSquares()
                            );
                        }else{
                            meeting = new Meeting(regressGeo.multivariateregression.cleanedMultivariateRegression.beta[0]/*intercept*/,
                                    regressGeo.multivariateregression.cleanedMultivariateRegression.beta[i+1] /*slope*/,
                                    regressGeo.std(regressGeo.multivariateregression.cleanedMultivariateRegression.residuals ) /*std error*/ ,
                                    regressGeo.getMean( regressGeo.multivariateregression.cleanedMultivariateRegression.residuals) /*mean squarred error*/ ,
                                    Configuration.recordDuration_ms /*meet duration*/,
                                    regressGeo.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateRSquared() /*R²*/,
                                    startTime,
                                    regressGeo.multivariateregression.cleanedMultivariateRegression.OLSMultiReg.calculateTotalSumOfSquares()
                            );
                        }
                        DirectedEdge innerEdge = new DirectedEdge(this.meets.meetwithS.get(i).getVertexId(), me.localDevice.getVertexId(),meeting ) ;
                        text.append( " \n add inner Edge " +  innerEdge.toString());
                        directedEdges.add(innerEdge);
                    }
///////////////////////////////////////////////

                }
            }else {
                //todo apply the button for when is geo and when is simple case
                //todo apply filter for the geo case
                Meet2regressGeo regressGeo = new Meet2regressGeo(extras, this);
                text.append(regressGeo.geoWeightedRegressionOutput);
            }
        }


        if (Configuration.isMultiHopCalibration){
            HyperEdge h = new HyperEdge(directedEdges);
            HyperConnection hyperConnection = me.getLocalconnectionGraph();
            hyperConnection.addHyperEdge(h);
            hyperConnection.toFile(this);
        }
    }




}

