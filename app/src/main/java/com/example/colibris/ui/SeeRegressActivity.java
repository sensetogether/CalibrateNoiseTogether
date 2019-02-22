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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;


import com.example.colibris.R;
 import com.example.colibris.comtool.ContextData;
 import com.example.colibris.configuration.Configuration;

import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.Meet;
import com.example.colibris.calib.Meet2regress;
import com.example.colibris.calib.Meet2regressGeo;


import java.util.Arrays;

/**
 * The SeeRegressActivity class is an activity used to display regression related information
 */
public class SeeRegressActivity extends AppCompatActivity {
    /**
     * Text view
     */
    private TextView text ;
    /**
     * log related information
     */
    private static final String TAG = "Regress activity";
    /**
     * meeting information
     */
    public Meet meet = null;
    /**
     * context data
     */
    public ContextData ctxData = new ContextData();

    /**
     * Activity gets created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_regress);

        /* Taking each devices in a meeting, extracting the noise data, so as to call a calibration regression
         */
        Log.d(TAG, "Beginning Regress Activity");
        text = new TextView(this);
        text = findViewById(R.id.regress_message);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());

        // new meeting is created in order to proceed the calibration beetween the devices
        Log.d(TAG, "Running meet class and taking record data from File Manager");
        this.meet = new Meet(this);
        Bundle extras = getIntent().getExtras();
        if(Configuration.isLocationAwareCalibration == false) {
            Meet2regress regress = new Meet2regress(extras, this);
            text.append(regress.MultivariateRegressionoutput);
            text.append(regress.robustRegressionoutput);
            text.append(regress.geoMeanRegressionoutput);

            //text.append(regressGeo.geoWeightedRegressionOutput);

            FileManager outliersFile = new FileManager("outliersRobust", false, this);
            for (int i = 1; i < regress.multivariateregression.outliersX.size(); i = i + 2) {
                Log.d(TAG, "Value of each line: " + Arrays.toString(regress.multivariateregression.outliersX.get(i)));
                outliersFile.write_txt(Arrays.toString(regress.multivariateregression.outliersX.get(i)) + "," + regress.multivariateregression.outliersY.get(i) + " ; ");
            }
            outliersFile.close();

            int j;
            FileManager xyfile = new FileManager("xyRegressData", false, this);

            Log.d(TAG, "Saving Data for more than two devices: " + regress.predicted.length);
            if (regress.features.size() > 1) {
                String header = new String();
                String headerTemp = new String();
                header = "Y ";
                for (int col = 0; col < regress.features.size(); col++) {
                    headerTemp = "X" + String.valueOf(col) + " ";
                    header += headerTemp;
                }
                xyfile.write_txt(header + "\n");
                for (j = 0; j < regress.predicted.length; j++) {
                    String lineTemp = new String();
                    for(int col = 0; col < regress.features.size(); col++) {
                        lineTemp += String.valueOf(regress.xfeatures[j][col]) + ",";
                    }
                    xyfile.write_txt(String.valueOf(regress.predicted[j]) + "," + lineTemp + "\n");
                    //xyfile.close();
                }
                xyfile.write_txt("\n" + text.getText().toString());
                xyfile.close();
            } else {
                Log.e(TAG, "Saving Data for only two devices");
                xyfile.write_txt("#" + "Y " + "X" + "\n");
                for (j = 0; j < regress.predicted.length; j++) {
                    xyfile.write_txt(String.valueOf(regress.predicted[j]) + " " + String.valueOf(regress.xfeatures[j][0]) + "\n");
                    xyfile.close();
                }
                xyfile.write_txt("\n" + text.getText().toString());
            }
            xyfile.close();
            Log.d(TAG, "End of Saving Finishing SeeRegressActivity");
        }


        if(Configuration.isLocationAwareCalibration == true)
        {
            if(Configuration.isFilteredData2LinearRegression == true){
                if(Configuration.isFilteredData2GeoRegression == false) {
                    Meet2regressGeo regressSimpleFilter = new Meet2regressGeo(extras, this);
                    Configuration.isNotFilteredMatrix = true;
                    Meet2regressGeo regressSimple = new Meet2regressGeo(extras, this);
                    text.append(regressSimpleFilter.MultivariateFilteredRegressionoutput);
                    text.append(regressSimple.MultivariateFilteredRegressionoutput);
                    text.append(regressSimpleFilter.filteredRobustRegressionoutput);
                    text.append(regressSimple.filteredRobustRegressionoutput);

                    //Multivariate Case using the Raw Matrix
                    FileManager outliersFile = new FileManager("outliersRawMatrix2Robust", false, this);
                    for (int i = 1; i < regressSimple.filteredmultiregression.outliersX.size(); i = i + 2) {
                        Log.d(TAG, "Value of each line: " + Arrays.toString(regressSimple.filteredmultiregression.outliersX.get(i)));
                        outliersFile.write_txt(Arrays.toString(regressSimple.filteredmultiregression.outliersX.get(i)) + "," + regressSimple.filteredmultiregression.outliersY.get(i) + " ; ");
                    }
                    outliersFile.close();

                    int j;
                    FileManager xyfile = new FileManager("xRawyRegressData", false, this);

                    Log.d(TAG, "Saving Data for more than two devices: " + regressSimple.predicted.length);
                    if (regressSimple.features.size() > 1) {
                        String header = new String();
                        String headerTemp = new String();
                        header = "Y ";
                        for (int col = 0; col < regressSimple.features.size(); col++) {
                            headerTemp = "X" + String.valueOf(col) + " ";
                            header += headerTemp;
                        }
                        xyfile.write_txt(header + "\n");
                        for (j = 0; j < regressSimple.predicted.length; j++) {
                            String lineTemp = new String();
                            for(int col = 0; col < regressSimple.features.size(); col++) {
                                lineTemp += String.valueOf(regressSimple.xfeatures[j][col]) + ",";
                            }
                            xyfile.write_txt(String.valueOf(regressSimple.predicted[j]) + "," + lineTemp + "\n");
                            //xyfile.close();
                        }
                        xyfile.write_txt("\n" + text.getText().toString());
                        xyfile.close();
                    } else {
                        Log.e(TAG, "Saving Data for only two devices");
                        xyfile.write_txt("#" + "Y " + "X" + "\n");
                        for (j = 0; j < regressSimple.predicted.length; j++) {
                            xyfile.write_txt(String.valueOf(regressSimple.predicted[j]) + " " + String.valueOf(regressSimple.xfeatures[j][0]) + "\n");
                            xyfile.close();
                        }
                        xyfile.write_txt("\n" + text.getText().toString());
                    }
                    xyfile.close();

                    //Multivariate Case using the Filtered Matrix

                    FileManager outliersFilteredFile = new FileManager("outliersFilteredMatrix2Robust", false, this);
                    for (int i = 1; i < regressSimpleFilter.filteredmultiregression.outliersX.size(); i = i + 2) {
                        Log.d(TAG, "Value of each line: " + Arrays.toString(regressSimpleFilter.filteredmultiregression.outliersX.get(i)));
                        outliersFilteredFile.write_txt(Arrays.toString(regressSimpleFilter.filteredmultiregression.outliersX.get(i)) + "," + regressSimpleFilter.filteredmultiregression.outliersY.get(i) + " ; ");
                    }
                    outliersFilteredFile.close();

                    int k;
                    FileManager xyFilteredfile = new FileManager("xFilteredyRegressData", false, this);

                    Log.d(TAG, "Saving Data for more than two devices: " + regressSimpleFilter.predicted.length);
                    if (regressSimpleFilter.features.size() > 1) {
                        String header = new String();
                        String headerTemp = new String();
                        header = "Y ";
                        for (int col = 0; col < regressSimpleFilter.features.size(); col++) {
                            headerTemp = "X" + String.valueOf(col) + " ";
                            header += headerTemp;
                        }
                        xyFilteredfile.write_txt(header + "\n");
                        for (k = 0; k < regressSimpleFilter.predicted.length; k++) {
                            String lineTemp = new String();
                            for(int col = 0; col < regressSimpleFilter.features.size(); col++) {
                                lineTemp += String.valueOf(regressSimpleFilter.xfeatures[k][col]) + ",";
                            }
                            xyFilteredfile.write_txt(String.valueOf(regressSimpleFilter.predicted[k]) + "," + lineTemp + "\n");
                            //xyfile.close();
                        }
                        xyFilteredfile.write_txt("\n" + text.getText().toString());
                        xyFilteredfile.close();
                    } else {
                        Log.e(TAG, "Saving Data for only two devices");
                        xyFilteredfile.write_txt("#" + "Y " + "X" + "\n");
                        for (k = 0; k < regressSimpleFilter.predicted.length; k++) {
                            xyFilteredfile.write_txt(String.valueOf(regressSimpleFilter.predicted[k]) + " " + String.valueOf(regressSimpleFilter.xfeatures[k][0]) + "\n");
                            xyFilteredfile.close();
                        }
                        xyFilteredfile.write_txt("\n" + text.getText().toString());
                    }
                    xyFilteredfile.close();
                    Log.d(TAG, "End of Saving Finishing SeeRegressActivity");
                }


                if(Configuration.isFilteredData2GeoRegression == true) {
                    Meet2regressGeo regressGeoFilter = new Meet2regressGeo(extras, this);
                    Configuration.isNotFilteredMatrix = true;
                    Meet2regressGeo regressGeo = new Meet2regressGeo(extras, this);
                    text.append(regressGeoFilter.filteredGeoWeightedRegressionOutput);
                    text.append(regressGeo.filteredGeoWeightedRegressionOutput);
                }
            }else {
                //todo apply the button for when is geo and when is simple case
                //todo apply filter for the geo case
                Meet2regressGeo regressGeo = new Meet2regressGeo(extras, this);
                text.append(regressGeo.geoWeightedRegressionOutput);
            }
        }
    }
}
