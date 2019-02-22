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
import android.os.Bundle;
import android.util.Log;

import com.example.colibris.calib.regression.GeographicallyWeightedRegression;
import com.example.colibris.calib.regression.MultivariateRegression;
import com.example.colibris.comtool.ContextData;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.ui.MainActivity;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

/**
 * This Meet2regressGeo class is used when a meeting/calibration takes place.
 * In particular, this class permits to handle the calibration. For this
 * purpose, various type of rgeographically weighted regressions can be  performed
 */
public class Meet2regressGeo {
    /**
     * Log related information
     */
    private static final String TAG = "Meet2RegressGeo";
    /**
     * features
     */
    public  ArrayList features = new ArrayList();
    /**
     * features (raw sound considered
     */
    public  ArrayList featuresRaw = new ArrayList();
    /**
     * predicted fatures
     */
    public double[] predicted = new double[]{};
    /**
     * latitude
     */
    public double[] latitude = new double[]{};
    /**
     * longitude
     */
    public double[] longitude = new double[]{};

    /**
     * latide of a remote device
     */
     public double[] latitudeRemote = new double[]{};
    /**
     * longitude of remote device
     */
    private double[] longitudeRemote = new double[]{};
    /**
     * equal distance is obtained
     */
    private boolean flagEqualDistance = true;
    /**
     * x
     */
    public double[][] xfeatures;
    /**
     * parameters related to the meeting
     */
    public Meet meet = null;
    /**
     * characteristics of the regression
     */
    public String geoWeightedRegressionOutput;
    /**
     * is a linear case
     */
    public boolean flagLinearCase = false;
    /**
     * bd
     */
    public DescriptiveStatistics bandwith = new DescriptiveStatistics();
    /**
     * context
     */
    public ContextData ctxData = new ContextData();
    /**
     * prefix
     */
    private String prefix = "";
    /**
     * minimum
     */
    private int min = Integer.MAX_VALUE;
    /**
     * distance
     */
    private double dist;
    /**
     * file status
     */
    public String fileStatus;

    /**
     * characteristics (string) reflecting the filtered  multivariate regression
     */
    public String MultivariateFilteredRegressionoutput;
    /**
     * characteristics (string) reflecting the robust and multivariate regression
     */
    public String filteredRobustRegressionoutput;
    /**
     * characteristics (string) reflecting the geographical-aware and multivariate regression
     */
    public String filteredGeoWeightedRegressionOutput;

    /**
     * geo regression
     */
    public GeographicallyWeightedRegression geoweightedregression;
    /**
     * filtered geo-regression
     */
    public GeographicallyWeightedRegression filteredgeoweightedregression;
    /**
     * filtered regression
     */
    public MultivariateRegression filteredmultiregression;
    /**
     * multivariate regression
     */
    public MultivariateRegression multivariateregression;
    /**
     * characteristics of the multivariate regression
     */
    public String MultivariateRegressionoutput;
    /**
     * characteristics of the robust regression
     */
    public String robustRegressionoutput;

    private int sizeCalibFiltered = 0;

    /**
     * random number
     */
    private Random random = new Random();

    /**
     * Init the regression
     * @param extras2regress regression parameters
     * @param ctx2regress context
     */
    public Meet2regressGeo(Bundle extras2regress, Context ctx2regress) {
    /* Taking each devices in a meeting, extracting the noise data, so as to call a calibration regression
    We can this class as an extension of the Meet Class. Here, we take every variable and function related to the Location aware Calibration Approach*/
        Log.d(TAG, "Beginning Meet 2 Regress Geo");
        // new meeting is created in order to call proceed the calibration beetween the devices
        this.meet = new Meet(ctx2regress);
        Device device2meet;
        if (extras2regress != null) {
            String vertexId = extras2regress.getString(MainActivity.VERTEX_MESSAGE);
            String[] index = vertexId.split("-");
            String device_id = extras2regress.getString(MainActivity.DEVICE_ID_MESSAGE);
            List<FileManager> remoteFilelist = new ArrayList<FileManager>();
            for (int i = 0; i < index.length; i++) {
                Integer index_i = new Integer(index[i]);
                device2meet = new Device(index_i, device_id, 0);
                this.meet.add(device2meet, ctx2regress /*cyx*/, false /*append*/); // add the device if the device is not already in
                // display
                for (int j = 0; j < this.meet.meetwithS.size(); j++) {
                    remoteFilelist.add(j, device2data(this.meet, device_id, j, ctx2regress));
                }
            }
            //The key argument here must match the names from discovery activity
            Log.e(TAG, "local Name File 4 Noise : " + meet.getlocalFile4NoiseName());
            Log.e(TAG, "Size local data: " + remoteFilelist.size());

            //Calling the file manager for each device, so as to take the noise data
            FileManager localFile4Noise;
            if (device_id.startsWith("cross"))
                localFile4Noise = new FileManager("cross" + meet.getlocalFile4NoiseName(), true, ctx2regress);
            else
                localFile4Noise = new FileManager(meet.getlocalFile4NoiseName(), true, ctx2regress);
            if(localFile4Noise.isEmpty()) {
                fileStatus = localFile4Noise.getFilename() + "is empty \n";
                /*We should stop the regression, since the Local file is empty.*/
                Configuration.stopRegressionEmptyFiles = true;
            }
            localFile4Noise.close();
            regressMatrixGeo(localFile4Noise, remoteFilelist, ctx2regress);
        }
    }

    /**
     * initialise the regression
     * @param localFile4Noise local measurements
     * @param remoteFilelist  remote measurements
     * @param ctx2regress context
     */
    public Meet2regressGeo(FileManager localFile4Noise, List<FileManager> remoteFilelist, Context ctx2regress){
        regressMatrixGeo(localFile4Noise, remoteFilelist, ctx2regress);
    }

    /**
     * initialise the regression
     * @param localFile4Noise local measurements
     * @param remoteFilelist remote measurements
     * @param ctx2regress context
     */

    public void regressMatrixGeo(FileManager localFile4Noise, List<FileManager> remoteFilelist, Context ctx2regress) {
        //Creating a matrix with data recorded. This matrix will be used to run a calibrate regression
        ArrayList<ArrayList<Double>> matrix = localFile4Noise.extractCommontimeSeries(remoteFilelist, ctxData);
        processData2regress(matrix, ctx2regress, ctxData);
    }

    /**
     *
     * We need this function, since when dealing with Location data (GPS coordinates) we have three options of models to carry.
     *          Consider the distance between the devices uncalibrate - calibrate
     *            1- We take this distance as variable, which means sound_uncalibrate = beta0 + beta1 * sound_calibrate + beta2 * distance_uc + erro ]
     *          1.1 - The button related to this option is Configuration.isNotFilteringDistanceIsVariable == 1
     *            2- We apply a propagation sound function of the distance (delta = Lu - Lc = 20Log10(distance_uc)) on the sound of the calibrated device, which means sound_uncalibrate = beta0 + beta1 * (sound_calibrate + delta) + erro*
     *          2.1 The button related to this option is isFilteredData2LinearRegression == 1 and Configuration.weightingFunction = 1;
     *          *
     * @param matrix
     * @param ctx2regress
     * @param ctx2Data
     */
    public void processData2regress(ArrayList<ArrayList<Double>> matrix, Context ctx2regress, ContextData ctx2Data) {

        /*  */



        //We take the minimum size of the noise vectors. With this value, we create a matrix for regression.
        for (int i = 1; i < matrix.size(); i++) {
            Log.e(TAG, ">>>>>> size");
            Log.d(TAG, "********** matrix size" + " = " + matrix.size());
            Log.d(TAG, "**********\n ***********\n matrix(" + i + ") = " + matrix.get(i));
            if ((matrix.get(i).size() < min) && (0 < matrix.get(i).size()))
                min = matrix.get(i).size();
        }

        Log.e(TAG, "Size of the regression is" + min);
        Log.d(TAG, "Taking the data recorded and saving in a features list, so as to regress");
        Log.d(TAG, "**********\n ***********\n matrix(" + 0 + ") = " + matrix.get(0));
        //Here we save each feature data
        //We print each column of the regression here to be sure that we dont have a singular matrix
        if (Configuration.isNotFilteringDistanceIsVariable == true) {
            Log.e(TAG, "Beginning the distance as variable" + matrix.size());
            int j = 4;
            for (int i = 1; i < matrix.size(); i++) {
                Log.e(TAG, "Beggining the matrix for");
                Log.d(TAG, "********** matrix size" + " = " + matrix.size());
                Log.d(TAG, "**********\n ***********\n matrix(" + i + ") = " + matrix.get(i));
                //min = 200;
                if (matrix.get(i).size() != 0) {
                    if (i < 4) {
                        latitude = cast_data2regress(matrix.get(0), min);
                        if (i == 1)
                            longitude = cast_data2regress(matrix.get(i), min);
                        if (i == 3)
                            predicted = cast_data2regress(matrix.get(i), min);
                        Log.e(TAG, "predicted LENGTH:" + predicted.length);
                    } else {
                        double[] c = cast_data2regress(matrix.get(j + 2), min);
                        longitudeRemote = cast_data2regress(matrix.get(j), min);
                        latitudeRemote = cast_data2regress(matrix.get(j + 1), min);
                        double[] distanceVector = new double[latitudeRemote.length];
                        distanceVector = buildDistanceFeature(latitude, longitude, longitudeRemote, latitudeRemote);
                        Log.e(TAG, "DistanceVector: " + Arrays.toString(distanceVector));
                        if (!this.flagEqualDistance) {
                            features.add(j - 4, c);
                            features.add(j - 3, distanceVector);
                        } else {
                            features.add(j - 4, c);
                        }
                        Log.e(TAG, "Flag Distance: " + this.flagEqualDistance);
                        j = j + 3;
                        if (j > matrix.size()) ;
                        break;
                    }
                }
                Log.e(TAG, "Array Feature: " + Arrays.deepToString(features.toArray()));
            }
            regressActionGeo(features, predicted, ctx2regress, ctx2Data);
        } else {
            for (int i = 1; i < matrix.size(); i++) {
                Log.d(TAG, "********** matrix size" + " = " + matrix.size());
                Log.d(TAG, "**********\n ***********\n matrix(" + i + ") = " + matrix.get(i));
                //min = 200;
                if (matrix.get(i).size() != 0) {
                    if (i < 4) {
                        latitude = cast_data2regress(matrix.get(0), min);
                        if (i == 1)
                            longitude = cast_data2regress(matrix.get(i), min);
                        if (i == 3)
                            predicted = cast_data2regress(matrix.get(i), min);
                        Log.e(TAG, "predicted LENGTH:" + predicted.length);
                    } else {
                        double[] c = cast_data2regress(matrix.get(i), min);
                        features.add(i - 4, c);
                    }
                }
            }
            regressActionGeo(features, predicted, ctx2regress, ctx2Data);
        }

        //Random Values saved as accumulated error of each device, so that we can implement and make some simple tests..
        if(Configuration.isMultiHopCalibration == true) {
            ContextData.accumulatedErrorData.clear();
            Random testAccumulatedError = new Random();
            for (int k = 0; k < (features.size() + 1); k++) {
                Double accError = 10 * testAccumulatedError.nextDouble();
                ContextData.accumulatedErrorData.addValue(accError);
                Log.e(TAG, "Double2AccumulatedError: " + accError);
            }
        }
    }

    /**
     * multivatiriate regression
     * @param matrix2regress regression parameters
     * @param predicted2regress  regression parameters
     * @param ctx2regress context
     * @param ctx2Data context
     */

    public void regressActionGeo(ArrayList matrix2regress, double[] predicted2regress, Context ctx2regress, ContextData ctx2Data) {

        /* Begin the multivariate regression approach for more than two devices
         */

        Log.e(TAG, "Features Matrix" + Arrays.deepToString(matrix2regress.toArray()));
        //Creating fake geographically data to create a spacial regression
        double[] lat = new double[min];
        double[] longi = new double[min];
        //for (int k = 0; k < min; k++) {
        //  int j = (int) (random.nextDouble() * 10);
        // int i = (int) (random.nextDouble() * 10);
       /* int featSize = features.size();
        lat = (double[]) features.get(0);
        longi = (double[]) features.get(1);
        */
        //}

        //int numFeat = features.size();
        //features.add(numFeat, lat);
        //features.add(numFeat + 1, longi);


        Log.d(TAG, "beggining of the regression");

        this.xfeatures = features2regress(matrix2regress, ctx2Data);
        Log.e(TAG, "xfeatures values: " + Arrays.deepToString(xfeatures));
        Log.e(TAG, "predicted values: " + Arrays.toString(predicted));
        FileManager regressFile = new FileManager("Regressoutput", false, ctx2regress);

        // double[][] weights2regress2 = buildGeoWeight(lat, longi);
        if (Configuration.isFilteredData2LinearRegression == true) {
            Log.e(TAG, "Begging the Filtered Regression");
            /* Begin the multivariate regression approach for more than two devices
             */
            if (Configuration.isFilteredData2GeoRegression == false) {
                Log.d(TAG, "beggining of the regression");
                this.xfeatures = features2regress(matrix2regress, ctx2Data);
                Log.e(TAG, "xfeatures LENGTH:" + xfeatures.length);
                FileManager filteredRegressFile = new FileManager("Regressoutput", false, ctx2regress);

                this.filteredmultiregression = new MultivariateRegression(predicted2regress, this.xfeatures);
                if (Configuration.isNotFilteredMatrix == true) {
                    prefix = "";
                } else {
                    prefix = "Filtered";
                    ContextData.betaFiltered = this.filteredmultiregression.beta;
                    ContextData.residualFiltered = this.filteredmultiregression.residuals;
                }
                this.MultivariateFilteredRegressionoutput = "\n********" + prefix + " Multivariate Simple Regression **********\n" +
                        fileStatus +
                        "beta: " + Arrays.toString(filteredmultiregression.beta) +
                        //"\nresiduals: " + Arrays.toString(multivariateregression.residuals) +
                        // "\nParameters Variance:" + Arrays.deepToString(multivariateregression.parametersVariance) +
                        "\nVariance: " + filteredmultiregression.regressandVariance +
                        "\nrSquared: " + filteredmultiregression.rSquared +
                        "\nAdjusted rSquared: " + filteredmultiregression.AdjustedrSquared +
                        "\n mean residuals: " + this.getMean(filteredmultiregression.residuals) +
                        "\n N. Obs.: " + predicted.length +
                        "\n median residuals: " + this.getMedian(filteredmultiregression.residuals) +
                        "\n std residuals: " + this.std(filteredmultiregression.residuals);


                /* Taking the single values in order to run a simple regression beetween the features */
                // Case when X2 = alfa0 + alfa1 X1
                if (matrix2regress.size() > 1) {
                    ArrayList featureObj1 = new ArrayList();
                    featureObj1.add(0, matrix2regress.get(0));
                    double[][] featureX1 = features2regress(featureObj1, ctx2Data);
                    double[] predictedX2 = (double[]) matrix2regress.get(1);

                    // Simple Regression to explain X2 with X1 values.
                    MultivariateRegression simpleregressionX2 = new MultivariateRegression(predictedX2, featureX1);


                    // Case when X1 = alfa0 + alfa1 X2
                    ArrayList featureObj2 = new ArrayList();
                    featureObj2.add(0, matrix2regress.get(1));
                    double[][] featureX2 = features2regress(featureObj2, ctx2Data);
                    double[] predictedX1 = (double[]) matrix2regress.get(0);

                    //// Simple Regression to explain X1 with X2 values.
                    MultivariateRegression simpleregressionX1 = new MultivariateRegression(predictedX1, featureX2);

                    MultivariateFilteredRegressionoutput += "\n X2 =" + simpleregressionX2.beta[0] + "+" + simpleregressionX2.beta[1] + " * X1 " +
                            "\n with rsquared " + simpleregressionX2.rSquared +
                            "\n X1 = " + simpleregressionX1.beta[0] + simpleregressionX1.beta[1] + " * X2 " //+  Arrays.toString(simpleregressionX1.beta
                            + "\n with rsquared " + simpleregressionX1.rSquared;
                }


                //Calculating the difference between the variables before calibration
                for (int i = 0; i < matrix2regress.size(); i++) {
                    double diff = diff(predicted, (double[]) matrix2regress.get(i));
                    MultivariateFilteredRegressionoutput += "\n Y - X" + i + " = " + diff;

                    for (int k = 0; k < matrix2regress.size(); k++) {
                        if (i != k) {
                            double diffx = diff((double[]) matrix2regress.get(i), (double[]) matrix2regress.get(k));
                            MultivariateFilteredRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                        }
                    }
                }
                double[] val = (double[]) matrix2regress.get(0);
                // apply the calibration to the values and get the diff
                double calibrated[] = new double[predicted.length];
                double calibratedResidual[] = new double[predicted.length];
                double calibratedFiltered[] = new double[ContextData.residualFiltered.length];
                double calibratedResidualFiltered[] = new double[ContextData.residualFiltered.length];
                double alternativeCalibratedResidual[] = new double[predicted.length];
                double alternativeCalibratedSimple[] = new double[predicted.length];

                //Exploring different approachs for calibrating
                for (int i = 0; i < predicted.length; i++) {
                    /*approach for calibration including/excluding residuals*/

                    calibrated[i] = predicted[i] - filteredmultiregression.beta[0];
                    calibratedResidual[i] = predicted[i] - filteredmultiregression.beta[0] - filteredmultiregression.residuals[i];
                    /*approach for multivariate regression, when we consider the difference between the calibration
                    value and the weighted mean (regression parameters) of the features*/
                    alternativeCalibratedResidual[i] = predicted[i] - filteredmultiregression.beta[0] - filteredmultiregression.residuals[i];
                    alternativeCalibratedSimple[i] = predicted[i] - filteredmultiregression.beta[0];

                    double sum = 0;
                    for (int j = 1; j < filteredmultiregression.beta.length; j++) {
                        sum += filteredmultiregression.beta[j];
                    }
                    calibrated[i] = calibrated[i] / sum;
                    calibratedResidual[i] = calibratedResidual[i] / sum;


                    if (Configuration.isNotFilteredMatrix == false) {
                        sizeCalibFiltered = predicted.length;
                    } else {
                        sizeCalibFiltered = ContextData.residualRobustFiltered.length;
                    }
                    if (sizeCalibFiltered > predicted.length)
                        sizeCalibFiltered = predicted.length;

                    if (Configuration.isNotFilteredMatrix == false) {
                        for (int j = 1; j < ContextData.betaFiltered.length; j++) {
                            ContextData.sumFiltered += ContextData.betaFiltered[j];
                        }
                        calibratedFiltered[i] = predicted[i] - filteredmultiregression.beta[0];
                        calibratedResidualFiltered[i] = predicted[i] - filteredmultiregression.beta[0] - filteredmultiregression.residuals[i];
                        calibratedFiltered[i] = calibratedFiltered[i] / ContextData.sumFiltered;
                        calibratedResidualFiltered[i] = calibratedResidualFiltered[i] / ContextData.sumFiltered;
                    } else {
                        for (int k = 0; k < ContextData.residualFiltered.length; k++) {
                            calibratedFiltered[k] = predicted[k] - ContextData.betaFiltered[0];
                            calibratedResidualFiltered[k] = predicted[k] - ContextData.betaFiltered[0] - ContextData.residualFiltered[k];
                            calibratedFiltered[k] = calibratedFiltered[k] / ContextData.sumFiltered;
                            calibratedResidualFiltered[k] = calibratedResidualFiltered[k] / ContextData.sumFiltered;
                        }
                    }
                }

                //Calculating the weighted mean between the regression parameters and the feature variables to test calibrated values
                if (matrix2regress.size() > 1) {
                    double[] sumFeatBeta = new double[((double[]) matrix2regress.get(0)).length];
                    for (int i = 0; i < ((double[]) matrix2regress.get(0)).length; i++) {
                        for (int j = 0; j < matrix2regress.size(); j++) {
                            sumFeatBeta[j] = filteredmultiregression.beta[j] * ((double[]) matrix2regress.get(j))[i];
                        }
                    }

                    double diffResidualOttoCalib = diff(alternativeCalibratedResidual, sumFeatBeta);
                    double diffSimpleOttoCalib = diff(alternativeCalibratedSimple, sumFeatBeta);
                    MultivariateFilteredRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diffSimpleOttoCalib;
                    MultivariateFilteredRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diffResidualOttoCalib;

                }

                for (int i = 0; i < matrix2regress.size(); i++) {
                    double diff = diff(calibrated, (double[]) matrix2regress.get(i));
                    double diffResidual = diff(calibratedResidual, (double[]) matrix2regress.get(i));
                    MultivariateFilteredRegressionoutput += "\n Ycalib - X" + i + " = " + diff;
                    MultivariateFilteredRegressionoutput += "\n YcalibResidual - X" + i + " = " + diffResidual;
                    if (Configuration.isNotFilteredMatrix == true) {
                        double diffFiltered = diff(calibratedFiltered, (double[]) matrix2regress.get(i));
                        double diffResidualFiltered = diff(calibratedResidualFiltered, (double[]) matrix2regress.get(i));
                        MultivariateFilteredRegressionoutput += "\n YcalibFiltered - X" + i + " = " + diffFiltered;
                        MultivariateFilteredRegressionoutput += "\n YcalibResidualFiltered - X" + i + " = " + diffResidualFiltered;
                    } else {
                        MultivariateFilteredRegressionoutput += "\n Ycalib - X" + i + " = " + diff;
                        MultivariateFilteredRegressionoutput += "\n YcalibResidual - X" + i + " = " + diffResidual;
                    }
                }
                //text.append(MultivariateRegressionoutput);
                filteredRegressFile.write_txt(MultivariateFilteredRegressionoutput);

                filteredmultiregression.findBestMultipleRegression();
                filteredmultiregression.buildRLSRegression();

                //Testing the Geometric Mean Approach

                filteredmultiregression.geometricMeanRegression();

                Log.d(TAG, "End of regression");
                if (Configuration.isNotFilteredMatrix == false) {
                    prefix = "Filtered";
                    ContextData.betaRobustFiltered = this.filteredmultiregression.cleanedMultivariateRegression.beta;
                    ContextData.residualRobustFiltered = this.filteredmultiregression.cleanedMultivariateRegression.residuals;
                }
                this.filteredRobustRegressionoutput = "\n********" + prefix + " Multivariate Robust Regression **********\n" +
                        fileStatus +
                        "Parameters: " + Arrays.toString(filteredmultiregression.cleanedMultivariateRegression.beta) +
                        //  "\nresiduals: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.residuals) +
                        //       "\nParameters Variance:" + Arrays.deepToString(multivariateregression.cleanedMultivariateRegression.parametersVariance) +
                        "\nVariance: " + filteredmultiregression.cleanedMultivariateRegression.regressandVariance +
                        "\nrSquared: " + filteredmultiregression.cleanedMultivariateRegression.rSquared +
                        "\nAdjusted rSquared:" + filteredmultiregression.cleanedMultivariateRegression.AdjustedrSquared +
                        "\n mean residuals: " + this.getMean(filteredmultiregression.cleanedMultivariateRegression.residuals) +
                        "\n N. Obs. Robust: " + filteredmultiregression.y2regressClean.length +
                        "\n median residuals: " + this.getMedian(filteredmultiregression.cleanedMultivariateRegression.residuals) +
                        "\n std residuals: " + this.std(filteredmultiregression.cleanedMultivariateRegression.residuals);


                // Taking the clean values from the robust regression

                Log.d(TAG, "Size of Y Cleaned: " + filteredmultiregression.y2regressClean.length);
                ArrayList<double[]> featuresCleaned = new ArrayList<>();
                for (int k = 0; k < matrix2regress.size(); k++) {
                    double[] featureTemp = new double[filteredmultiregression.x2regressClean.length];
                    for (int j = 0; j < filteredmultiregression.x2regressClean.length; j++) {
                        featureTemp[j] = filteredmultiregression.x2regressClean[j][k];
                    }
                    featuresCleaned.add(k, featureTemp);
                }
                Log.e(TAG, "Size of features Cleaned: " + featuresCleaned.size());
                Log.e(TAG, "Values of first feature Cleaned: " + Arrays.toString(featuresCleaned.get(0)));

                // Making the difference beetween the values in robust regression before calibration

                for (int i = 0; i < featuresCleaned.size(); i++) {
                    //  Log.d(TAG, "Making the difference for each cleaned feature and each predicted variable");
                    double diff = diff(filteredmultiregression.y2regressClean, featuresCleaned.get(i));
                    filteredRobustRegressionoutput += "\n Y - X" + i + " = " + diff;
                    for (int k = 0; k < featuresCleaned.size(); k++) {
                        if (i != k) {
                            //    Log.d(TAG, "Making the difference for each cleaned feature");
                            double diffx = diff(featuresCleaned.get(i), featuresCleaned.get(k));
                            filteredRobustRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                        }
                    }
                }


                //Defining the variables and plotting the evaluating values for the robust case

                double calibratedRobust[] = new double[filteredmultiregression.y2regressClean.length];
                double calibratedRobustResidual[] = new double[filteredmultiregression.y2regressClean.length];
                double alternativeCalibratedRobustResidual[] = new double[filteredmultiregression.y2regressClean.length];
                double alternativeCalibratedRobustSimple[] = new double[filteredmultiregression.y2regressClean.length];

                if (Configuration.isNotFilteredMatrix == false) {
                    sizeCalibFiltered = filteredmultiregression.y2regressClean.length;
                } else {
                    sizeCalibFiltered = ContextData.residualRobustFiltered.length;
                }
                if (sizeCalibFiltered > filteredmultiregression.y2regressClean.length)
                    sizeCalibFiltered = filteredmultiregression.y2regressClean.length;

                double calibratedRobustFiltered[] = new double[sizeCalibFiltered];
                double calibratedRobustResidualFiltered[] = new double[sizeCalibFiltered];

                for (int i = 0; i < filteredmultiregression.y2regressClean.length; i++) {

                    calibratedRobust[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0];

                    calibratedRobustResidual[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0] -
                            filteredmultiregression.cleanedMultivariateRegression.residuals[i];

                    alternativeCalibratedRobustResidual[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0] -
                            filteredmultiregression.cleanedMultivariateRegression.residuals[i];

                    alternativeCalibratedRobustSimple[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0];
                    double sum = 0;
                    for (int j = 1; j < filteredmultiregression.cleanedMultivariateRegression.beta.length; j++) {
                        sum += filteredmultiregression.cleanedMultivariateRegression.beta[j];
                    }

                    if (Configuration.isNotFilteredMatrix == false) {
                        for (int j = 1; j < filteredmultiregression.cleanedMultivariateRegression.beta.length; j++) {
                            ContextData.sumRobustFiltered += filteredmultiregression.cleanedMultivariateRegression.beta[j];
                        }
                        calibratedRobustFiltered[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0];
                        calibratedRobustResidualFiltered[i] = filteredmultiregression.y2regressClean[i] - filteredmultiregression.cleanedMultivariateRegression.beta[0] - filteredmultiregression.cleanedMultivariateRegression.residuals[i];
                        calibratedRobustFiltered[i] = calibratedRobustFiltered[i] / ContextData.sumRobustFiltered;
                        calibratedRobustResidualFiltered[i] = calibratedRobustResidualFiltered[i] / ContextData.sumRobustFiltered;
                    }
                    if (Configuration.isNotFilteredMatrix == true) {
                        for (int k = 0; k < sizeCalibFiltered; k++) {
                            Log.e(TAG, "Size of cleaned Y values: " + filteredmultiregression.y2regressClean.length + " residual Size: " + ContextData.residualRobustFiltered.length);
                            calibratedRobustFiltered[k] = filteredmultiregression.y2regressClean[k] - ContextData.betaRobustFiltered[0];
                            calibratedRobustResidualFiltered[k] = filteredmultiregression.y2regressClean[k] - ContextData.betaRobustFiltered[0] - ContextData.residualRobustFiltered[k];
                            calibratedRobustFiltered[k] = calibratedRobustFiltered[k] / ContextData.sumRobustFiltered;
                            calibratedRobustResidualFiltered[k] = calibratedRobustResidualFiltered[k] / ContextData.sumRobustFiltered;
                        }
                    }

                    calibratedRobust[i] = calibratedRobust[i] / sum;
                    calibratedRobustResidual[i] = calibratedRobustResidual[i] / sum;
                }


                if (featuresCleaned.size() > 1) {

                    double[] sumFeatBetaRobust = new double[featuresCleaned.get(0).length];
                    for (int i = 0; i < featuresCleaned.get(0).length; i++) {
                        for (int j = 0; j < featuresCleaned.size(); j++) {
                            sumFeatBetaRobust[j] = filteredmultiregression.cleanedMultivariateRegression.beta[j] * featuresCleaned.get(j)[i];
                        }
                    }

                    double diff2Robust = diff(alternativeCalibratedRobustResidual, sumFeatBetaRobust);
                    filteredRobustRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diff2Robust;

                    double diff3Robust = diff(alternativeCalibratedRobustSimple, sumFeatBetaRobust);
                    filteredRobustRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diff3Robust;


                }

                Log.e(TAG, "Size of features Cleanded: " + featuresCleaned.size());

                for (int i = 0; i < featuresCleaned.size(); i++) {
                    Log.d(TAG, "Calculating the diff for each calibrated value");
                    double diff = diff(calibratedRobust, featuresCleaned.get(i));
                    double diffResidualRobust = diff(calibratedRobustResidual, featuresCleaned.get(i));
                    filteredRobustRegressionoutput += "\n Ycalibrobust - X" + i + " = " + diff;
                    filteredRobustRegressionoutput += "\n Ycalibresidualrobust  - X" + i + " = " + diffResidualRobust;

                }

                for (int j = 1; j < ContextData.betaRobustFiltered.length; j++) {
                    Log.e(TAG, "Beta Filtered Length" + ContextData.betaRobustFiltered.length);
                    int indexDiff = j - 1;
                    double diffRobustFiltered = diff(calibratedRobustFiltered, featuresCleaned.get(indexDiff));
                    double diffResidualRobustFiltered = diff(calibratedRobustResidualFiltered, featuresCleaned.get(indexDiff));
                    filteredRobustRegressionoutput += "\n YcalibrobustFiltered - X" + indexDiff + " = " + diffRobustFiltered;
                    filteredRobustRegressionoutput += "\n YcalibresidualrobustFiltered  - X" + indexDiff + " = " + diffResidualRobustFiltered;
                }

                //text.append(robustRegressionoutput);
                filteredRegressFile.write_txt(filteredRobustRegressionoutput);
            }
            if (Configuration.isFilteredData2GeoRegression == true) {
                //ArrayList<double[][]> weights2regress = new ArrayList<>();
                HashMap<Integer, double[][]> weights2regress = new HashMap<>();

                if (Configuration.isNotFilteredMatrix)
                    prefix = "";
                else
                    prefix = "Filtered";

                for (int j = 0; j < min/*lat.length*/; j++) {
                    HashMap<Integer, Double> latitude2weight = getCoord(latitude, longitude, true);
                    HashMap<Integer, Double> longitude2weight = getCoord(latitude, longitude, false);
                    weights2regress.put(j, buildGeoWeight(latitude2weight, longitude2weight, j));
                }

                this.filteredgeoweightedregression = new GeographicallyWeightedRegression(predicted, this.xfeatures, weights2regress, (matrix2regress.size() - 2), min);//lat.length);
                this.filteredGeoWeightedRegressionOutput = "\n********" + prefix + "Multivariate Geographically Regression **********\n" +
                        fileStatus +
                        //"beta: " + Arrays.deepToString(geoweightedregression.beta.toArray()) +
                        "beta Max: " + Arrays.deepToString(filteredgeoweightedregression.betaSummaryMax.toArray()) +
                        "beta Mean: " + Arrays.deepToString(filteredgeoweightedregression.betaSummaryMean.toArray()) +
                        "beta Min: " + Arrays.deepToString(filteredgeoweightedregression.betaSummaryMin.toArray()) +
                        //"\nresiduals: " + Arrays.toString(multivariateregression.residuals) +
//                 "\nParameters Variance:" + geoweightedregression.parametersVariance.toString() +
                        "\nVariance: " + Arrays.toString(filteredgeoweightedregression.regressandVariance.toArray()) +
                        //  "\nF: " + geoweightedregression.fValue +
                        //"\nrSquared: " + geoweightedregression.rSquared +
                        //"\nAdjusted rSquared: " + geoweightedregression.AdjustedrSquared +
                        //"\n mean residuals: " + this.getMean(geoweightedregression.residuals) +
                        "\n N. Obs.: " + predicted.length;
                //"\n median residuals: " + this.getMedian(geoweightedregression.residuals) +
                //"\n std residuals: " + this.std(geoweightedregression.residuals);
            }
        } else {
            if (Configuration.isNotFilteringDistanceIsVariable == true) {
                FileManager regressFileDistance = new FileManager("Regressoutput", false, ctx2regress);
                try {
                    this.xfeatures = features2regress(features, ctx2Data);
                    Log.e(TAG, "xfeatures LENGTH:" + xfeatures.length);
                    this.multivariateregression = new MultivariateRegression(predicted, this.xfeatures);
                }catch(Exception matrixSing){
                    ArrayList featuresSing = new ArrayList();
                    for(int i = 0; i < (features.size() - 1); i++){
                        featuresSing.set(i,features.get(i));
                    }
                    this.xfeatures = features2regress(featuresSing, ctx2Data);
                    Log.e(TAG, "xfeatures LENGTH:" + xfeatures.length);
                    this.multivariateregression = new MultivariateRegression(predicted, this.xfeatures);
                }
                this.MultivariateRegressionoutput = "\n********Multivariate Simple Regression **********\n" +
                        fileStatus +
                        "beta: " + Arrays.toString(multivariateregression.beta) +
                        //"\nresiduals: " + Arrays.toString(multivariateregression.residuals) +
                        // "\nParameters Variance:" + Arrays.deepToString(multivariateregression.parametersVariance) +
                        "\nVariance: " + multivariateregression.regressandVariance +
                        "\nrSquared: " + multivariateregression.rSquared +
                        "\nAdjusted rSquared: " + multivariateregression.AdjustedrSquared +
                        "\n mean residuals: " + this.getMean(multivariateregression.residuals) +
                        "\n N. Obs.: " + predicted.length +
                        "\n median residuals: " +  this.getMedian(multivariateregression.residuals)  +
                        "\n std residuals: " + this.std(multivariateregression.residuals);


                /* Taking the single values in order to run a simple regression beetween the features */
                // Case when X2 = alfa0 + alfa1 X1
                if (features.size() > 1) {
                    ArrayList featureObj1 = new ArrayList();
                    featureObj1.add(0, features.get(0));
                    double[][] featureX1 = features2regress(featureObj1, ctx2Data);
                    double[] predictedX2 = (double[]) features.get(1);

                    // Simple Regression to explain X2 with X1 values.
                    MultivariateRegression simpleregressionX2 = new MultivariateRegression(predictedX2, featureX1);


                    // Case when X1 = alfa0 + alfa1 X2
                    ArrayList featureObj2 = new ArrayList();
                    featureObj2.add(0, features.get(1));
                    double[][] featureX2 = features2regress(featureObj2, ctx2Data);
                    double[] predictedX1 = (double[]) features.get(0);

                    //// Simple Regression to explain X1 with X2 values.
                    MultivariateRegression simpleregressionX1 = new MultivariateRegression(predictedX1, featureX2);

                    MultivariateRegressionoutput += "\n X2 =" + simpleregressionX2.beta[0] + "+" + simpleregressionX2.beta[1] + " * X1 " +
                            "\n with rsquared " + simpleregressionX2.rSquared +
                            "\n X1 = " + simpleregressionX1.beta[0] + simpleregressionX1.beta[1] + " * X2 " //+  Arrays.toString(simpleregressionX1.beta
                            + "\n with rsquared " + simpleregressionX1.rSquared;
                }

                //Calculating the difference between the variables before calibration
                for (int i = 0; i < features.size(); i++) {
                    double diff = diff(predicted, (double[]) features.get(i));
                    MultivariateRegressionoutput += "\n Y - X" + i + " = " + diff;

                    for (int k = 0; k < features.size(); k++) {
                        if (i != k) {
                            double diffx = diff((double[]) features.get(i), (double[]) features.get(k));
                            MultivariateRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                        }
                    }
                }
                double[] val = (double[]) features.get(0);
                // apply the calibration to the values and get the diff
                double calibrated[] = new double[predicted.length];
                double calibratedResidual[] = new double[predicted.length];
                double alternativeCalibratedResidual[] = new double[predicted.length];
                double alternativeCalibratedSimple[] = new double[predicted.length];

                //Exploring different approachs for calibrating
                for (int i = 0; i < predicted.length; i++) {
                    /*approach for calibration including/excluding residuals*/

                    calibrated[i] = predicted[i] - multivariateregression.beta[0];
                    calibratedResidual[i] = predicted[i] - multivariateregression.beta[0] - multivariateregression.residuals[i];

                /*approach for multivariate regression, when we consider the difference between the calibration
                 value and the weighted mean (regression parameters) of the features*/

                    alternativeCalibratedResidual[i] = predicted[i] - multivariateregression.beta[0] - multivariateregression.residuals[i];
                    alternativeCalibratedSimple[i] = predicted[i] - multivariateregression.beta[0];

                    double sum = 0;
                    for (int j = 1; j < multivariateregression.beta.length; j++) {
                        sum += multivariateregression.beta[j];
                    }
                    calibrated[i] = calibrated[i] / sum;
                    calibratedResidual[i] = calibratedResidual[i] / sum;
                }

                //Calculating the weighted mean between the regression parameters and the feature variables to test calibrated values
                if (features.size() > 1)
                {
                    double[] sumFeatBeta = new double[((double[])features.get(0)).length];
                    for(int i = 0; i < ((double[])features.get(0)).length; i++)
                    {
                        for (int j = 0; j < features.size(); j++)
                        {
                            sumFeatBeta[j] =  multivariateregression.beta[j] * ((double[])features.get(j))[i];
                        }
                    }

                    double diffResidualOttoCalib = diff(alternativeCalibratedResidual, sumFeatBeta);
                    double diffSimpleOttoCalib = diff(alternativeCalibratedSimple, sumFeatBeta);
                    MultivariateRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diffSimpleOttoCalib;
                    MultivariateRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diffResidualOttoCalib;

                }

                for (int i = 0; i < features.size(); i++)
                {
                    double diff = diff(calibrated, (double[]) features.get(i));
                    double diffResidual = diff(calibratedResidual, (double[]) features.get(i));
                    MultivariateRegressionoutput += "\n Ycalib - X" + i + " = " + diff;
                    MultivariateRegressionoutput += "\n YcalibResidual - X" + i + " = " + diffResidual;
                }
                //text.append(MultivariateRegressionoutput);
                regressFileDistance.write_txt(MultivariateRegressionoutput);

                multivariateregression.findBestMultipleRegression();
                multivariateregression.buildRLSRegression();

                //Testing the Geometric Mean Approach

                multivariateregression.geometricMeanRegression();

                Log.d(TAG, "End of regression");

                this.robustRegressionoutput = "\n********Multivariate Robust Regression **********\n" +
                        "Parameters: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.beta) +
                        fileStatus +
                        //  "\nresiduals: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.residuals) +
                        //       "\nParameters Variance:" + Arrays.deepToString(multivariateregression.cleanedMultivariateRegression.parametersVariance) +
                        "\nVariance: " + multivariateregression.cleanedMultivariateRegression.regressandVariance +
                        "\nrSquared: " + multivariateregression.cleanedMultivariateRegression.rSquared +
                        "\nAdjusted rSquared:" + multivariateregression.cleanedMultivariateRegression.AdjustedrSquared +
                        "\n mean residuals: " + this.getMean(multivariateregression.cleanedMultivariateRegression.residuals) +
                        "\n N. Obs. Robust: " + multivariateregression.y2regressClean.length +
                        "\n median residuals: " +  this.getMedian(multivariateregression.cleanedMultivariateRegression.residuals)  +
                        "\n std residuals: " + this.std(multivariateregression.cleanedMultivariateRegression.residuals);


                // Taking the clean values from the robust regression

                Log.d(TAG, "Size of Y Cleaned: " + multivariateregression.y2regressClean.length);
                ArrayList<double[]> featuresCleaned = new ArrayList<>();
                for (int k = 0; k < features.size(); k++)
                {
                    double[] featureTemp = new double[multivariateregression.x2regressClean.length];
                    for (int j = 0; j < multivariateregression.x2regressClean.length; j++)
                    {
                        featureTemp[j] = multivariateregression.x2regressClean[j][k];
                    }
                    featuresCleaned.add(k, featureTemp);
                }
                Log.e(TAG, "Size of features Cleaned: " + featuresCleaned.size());
                Log.e(TAG, "Values of first feature Cleaned: " + Arrays.toString(featuresCleaned.get(0)));

                // Making the difference beetween the values in robust regression before calibration

                for (int i = 0; i < featuresCleaned.size(); i++)
                {
                    //  Log.d(TAG, "Making the difference for each cleaned feature and each predicted variable");
                    double diff = diff(multivariateregression.y2regressClean, featuresCleaned.get(i));
                    robustRegressionoutput += "\n Y - X" + i + " = " + diff;
                    for (int k = 0; k < featuresCleaned.size(); k++)
                    {
                        if (i != k)
                        {
                            //    Log.d(TAG, "Making the difference for each cleaned feature");
                            double diffx = diff(featuresCleaned.get(i), featuresCleaned.get(k));
                            robustRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                        }
                    }
                }


                //Defining the variables and plotting the evaluating values for the robust case

                double calibratedRobust[] = new double[multivariateregression.y2regressClean.length];
                double calibratedRobustResidual[] = new double[multivariateregression.y2regressClean.length];
                double alternativeCalibratedRobustResidual[] = new double[multivariateregression.y2regressClean.length];
                double alternativeCalibratedRobustSimple[] = new double[multivariateregression.y2regressClean.length];


                for (int i = 0; i < multivariateregression.y2regressClean.length; i++)
                {
                    calibratedRobust[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0];

                    calibratedRobustResidual[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0] -
                            multivariateregression.cleanedMultivariateRegression.residuals[i];

                    alternativeCalibratedRobustResidual[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0] -
                            multivariateregression.cleanedMultivariateRegression.residuals[i];

                    alternativeCalibratedRobustSimple[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0];
                    double sum = 0;
                    for (int j = 1; j < multivariateregression.cleanedMultivariateRegression.beta.length; j++)
                    {
                        sum += multivariateregression.cleanedMultivariateRegression.beta[j];
                    }
                    calibratedRobust[i] = calibratedRobust[i] / sum;
                    calibratedRobustResidual[i] = calibratedRobustResidual[i] / sum;
                }


                if (featuresCleaned.size() > 1)
                {

                    double[] sumFeatBetaRobust = new double[featuresCleaned.get(0).length];
                    for(int i = 0; i < featuresCleaned.get(0).length; i++)
                    {
                        for (int j = 0; j < featuresCleaned.size(); j++)
                        {
                            sumFeatBetaRobust[j] = multivariateregression.cleanedMultivariateRegression.beta[j] * featuresCleaned.get(j)[i];
                        }
                    }

                    double diff2Robust = diff(alternativeCalibratedRobustResidual, sumFeatBetaRobust);
                    robustRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diff2Robust;

                    double diff3Robust = diff(alternativeCalibratedRobustSimple, sumFeatBetaRobust);
                    robustRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diff3Robust;


                }

                Log.e(TAG, "Size of features Cleanded: " + featuresCleaned.size());

                for (int i = 0; i < featuresCleaned.size(); i++)
                {
                    Log.d(TAG, "Calculating the diff for each calibrated value");
                    double diff = diff(calibratedRobust, featuresCleaned.get(i));
                    double diffResidualRobust = diff(calibratedRobustResidual, featuresCleaned.get(i));
                    robustRegressionoutput += "\n Ycalibrobust - X" + i + " = " + diff;
                    robustRegressionoutput += "\n Ycalibresidualrobust  - X" + i + " = " + diffResidualRobust;
                }

                //text.append(robustRegressionoutput);
                regressFileDistance.write_txt(robustRegressionoutput);

                //Taking the parameters from the Geometric Mean Regression

                Log.d(TAG, "End of the calibration regression");
                Log.d(TAG, "Saving Data");



            } else {
                //ArrayList<double[][]> weights2regress = new ArrayList<>();
                HashMap<Integer, double[][]> weights2regress = new HashMap<>();

                for (int j = 0; j < min/*lat.length*/; j++) {
                    HashMap<Integer, Double> latitude2weight = getCoord(latitude, longitude, true);
                    HashMap<Integer, Double> longitude2weight = getCoord(latitude, longitude, false);
                    weights2regress.put(j, buildGeoWeight(latitude2weight, longitude2weight, j));
                }

                this.geoweightedregression = new GeographicallyWeightedRegression(predicted, this.xfeatures, weights2regress, (matrix2regress.size() - 2), min);//lat.length);
                this.geoWeightedRegressionOutput = "\n********" + prefix + "Multivariate Geographically Regression **********\n" +
                        fileStatus +
                        //"beta: " + Arrays.deepToString(geoweightedregression.beta.toArray()) +
                        "beta Max: " + Arrays.deepToString(geoweightedregression.betaSummaryMax.toArray()) +
                        "beta Mean: " + Arrays.deepToString(geoweightedregression.betaSummaryMean.toArray()) +
                        "beta Min: " + Arrays.deepToString(geoweightedregression.betaSummaryMin.toArray()) +
                        //"\nresiduals: " + Arrays.toString(multivariateregression.residuals) +
//                 "\nParameters Variance:" + geoweightedregression.parametersVariance.toString() +
                        "\nVariance: " + Arrays.toString(geoweightedregression.regressandVariance.toArray()) +
                        //  "\nF: " + geoweightedregression.fValue +
                        //"\nrSquared: " + geoweightedregression.rSquared +
                        //"\nAdjusted rSquared: " + geoweightedregression.AdjustedrSquared +
                        //"\n mean residuals: " + this.getMean(geoweightedregression.residuals) +
                        "\n N. Obs.: " + predicted.length;
                //"\n median residuals: " + this.getMedian(geoweightedregression.residuals) +
                //"\n std residuals: " + this.std(geoweightedregression.residuals);

            }
        }
    }


    /**
     * map coordinate
     * @param latitude latitude
     * @param longitude longitude
     * @param isLatitude boolean
     * @return hash map with positions
     */
    public HashMap<Integer, Double> getCoord(double[] latitude, double[] longitude,boolean isLatitude)
    {
        HashMap<Integer, Double> latitude2map = new HashMap<>();
        HashMap<Integer, Double> longitude2map = new HashMap<>();
        /*
        int j = 0;        int k = 0;
        for (int i = 0; i < latitude.length; i++) {
            if(i == 0){
                latitudeUnique.put(k, latitude[i]);
                longitudeUnique.put(k, latitude[i]);
            } else{
                Log.e(TAG, "latitude line ( " + i + " ) " + latitude[i] + " latitude line ( " + j + " ) " + latitude[j]);
                if ((latitude[j] != latitude[i]) || (longitude[j] != longitude[i])){
                    k++;
                    latitudeUnique.put(k, latitude[i]);
                    longitudeUnique.put(k, latitude[i]);
                    j = i;
                }            }
        }
        */
        for(int i = 0; i < latitude.length; i++) {
            latitude2map.put(i, latitude[i]);
            longitude2map.put(i, longitude[i]);
        }
        Log.e(TAG, "Size of unique values: " + latitude2map.size());
        if((latitude2map.size() == 0)||(longitude2map.size()==0))
        {
            Log.e(TAG, "No Variation on GPS data");
            flagLinearCase = false;
        }else{
            if(isLatitude)
                return latitude2map;
            else
                return longitude2map;
        }
        return null;
    }

    /**
     *
     * @param latitudeLocal latitude  of the device
     * @param longitudeLocal longitude of the device
     * @param latitudeRemote latitude of a remote device
     * @param longitudeRemote longitude of a remote device
     * @return distance feature
     */
    public double[] buildDistanceFeature (double[] latitudeLocal, double[] longitudeLocal, double[] latitudeRemote, double[] longitudeRemote ){
        double[] distanceFeature = new double[latitudeLocal.length];
        for(int i = 0; i < latitudeLocal.length; i++){
            if(i == 0)
                distanceFeature[i] = distGeo(latitudeLocal[i], longitudeLocal[i], latitudeRemote[i], longitudeRemote[i]);
            if(i > 0){
                double distOld = distGeo(latitudeLocal[i - 1], longitudeLocal[i - 1], latitudeRemote[i - 1], longitudeRemote[i - 1]);
                double distCurrent = distGeo(latitudeLocal[i], longitudeLocal[i], latitudeRemote[i], longitudeRemote[i]);
                Log.e(TAG, "latitude local: " + latitudeLocal[i] + " longitude local: " + longitudeLocal[i] + " latitude remote: " + latitudeRemote[i] + " longitude remote: " + longitudeRemote[i]);
                distanceFeature[i] = distGeo(latitudeLocal[i], longitudeLocal[i], latitudeRemote[i], longitudeRemote[i]);
                Log.e(TAG, "dist Value: " + distanceFeature[i]);
                if(distOld != distCurrent) {
                    this.flagEqualDistance = false;
                    Log.e(TAG, "Flag Distance: " + this.flagEqualDistance);
                }
            }
        }
        return distanceFeature;
    }




    // public double[][] buildGeoWeight(HashMap<Integer, Double> mapWeightsLat, HashMap<Integer, Double> mapWeightsLong, int line/*, boolean flagLinear*/){

    /**
     * weighting
     * @param mapWeightsLat latitude based weighting
     * @param mapWeightsLong longitude based weighting
     * @param line
     * @return geo weight
     */
    public double[][] buildGeoWeight(HashMap<Integer, Double> mapWeightsLat, HashMap<Integer, Double> mapWeightsLong, int line/*, boolean flagLinear*/){

        /*
        //double[][] weightTemp = new double[latitudeUnique.length][latitudeUnique.length];
        HashMap<Integer, Double> mapWeightsLat = new HashMap<>();
        HashMap<Integer, Double> mapWeightsLong = new HashMap<>();
        //ArrayList<double[][]> weigths = new ArrayList<>();

        //todo take the gps values from the device
        for(int i = 0; i < latitudeUnique.length; i++) {
            mapWeightsLat.put(i, latitudeUnique[i]);
            mapWeightsLong.put(i, longitudeUnique[i]);
        }
        */
        Log.e(TAG,"The size of the map's weight :" + mapWeightsLat.size());
        double[][] weights = new double[mapWeightsLat.size()][mapWeightsLong.size()];

        //Log.e(TAG,"KEYS ARRAY: " + Arrays.toString(mapWeightsLat.keySet().toArray()));
        //for(Integer key: mapWeightsLat.keySet()){
        for(Integer secondKey: mapWeightsLong.keySet()) {
            //int keyInt = key.intValue();
            //int secondkeyInt = secondKey.intValue();
            //if(!key.equals(secondKey)){
            // todo go to simple case when we only have one gps observation
            //todo create a function to define the bandwith
            //todo choose the best approach to calculate the weights
            //if (secondKey == key) {
            if(line == secondKey){
                weights[line][secondKey] = 1;
                bandwith.addValue(1);
            } else {
                Log.e(TAG, "latitude length: " + latitude.length);
                //Log.e(TAG, "latitude line ( " + line + " ) " + latitude[line] + " latitudeUnique line ( " + key + " ) " + mapWeightsLat.get(key));
                double dist = (distGeo(latitude[line], longitude[line], mapWeightsLat.get(secondKey), mapWeightsLong.get(secondKey)) == 0.0) ? 1.0 : distGeo(latitude[line], longitude[line], mapWeightsLat.get(secondKey), mapWeightsLong.get(secondKey));
                Log.e(TAG, "latitude line ( " + line + " ) " + latitude[line] + " latitudeUnique line ( " + secondKey + " ) " + mapWeightsLat.get(secondKey) + " Distance: " + dist);
                bandwith.addValue(dist);
            }
        }
        //}
        //for(Integer key: mapWeightsLat.keySet()){
        for(Integer secondKey: mapWeightsLong.keySet()){

            if((secondKey == line)) {
                weights[line][secondKey] = 1;
            }else{
                if(bandwith.getStandardDeviation() == 0.0)
                {
                    Log.e(TAG, "Inside Sd 0");
                    weights[secondKey][secondKey] = 1;
                }else {
                    Log.e(TAG, "latitude length: " + latitude.length);
                    //Log.e(TAG, "latitude line ( " + line + " ) " + latitude[line] + " latitudeUnique line ( " + key + " ) " + mapWeightsLat.get(key));
                    //Log.e(TAG, "latitude line ( " + line + " ) " + latitude[line] + " latitudeUnique line ( " + secondKey + " ) " + mapWeightsLat.get(secondKey));
                    double dist = (distGeo(latitude[line], longitude[line], mapWeightsLat.get(secondKey), mapWeightsLong.get(secondKey)) == 0.0) ? 1.0 : distGeo(latitude[line], longitude[line], mapWeightsLat.get(secondKey), mapWeightsLong.get(secondKey));
                    weights[secondKey][secondKey] = 1 / (Math.exp(dist / bandwith.getStandardDeviation()));
                    Log.e(TAG, "latitude line ( " + line + " ) " + latitude[line] + " latitudeUnique line ( " + secondKey + " ) " + mapWeightsLat.get(secondKey) + " weight: " + (Math.exp(dist / bandwith.getStandardDeviation())) + " dist: " + dist + " sd: " + bandwith.getStandardDeviation());
                }
            }
            //weights[key][secondKey] = Math.exp((((mapWeightsLong.get(line) - mapWeightsLong.get(secondKey)) * (mapWeightsLong.get(line) - mapWeightsLong.get(secondKey))) + ((mapWeightsLat.get(line) - mapWeightsLat.get(key)) * (mapWeightsLat.get(line) - mapWeightsLat.get(key))))/bandwith);
            //else
            //   weights[key][secondKey] = Math.exp(((mapWeightsLong.get(line) - mapWeightsLong.get(secondKey)) * (mapWeightsLong.get(line) - mapWeightsLong.get(secondKey))) + ((mapWeightsLat.get(line) - mapWeightsLat.get(secondKey)) * (mapWeightsLat.get(line) - mapWeightsLat.get(secondKey)))/100);
            //weights[key][secondKey] = 0;
            //}else{
            //  weights[key][secondKey] = 1;
            //}
        }
        //}
        Log.e(TAG,"The size of the weights is :" + weights.length);
/*
        for(int i = 0; i < latitudeUnique.length; i++)
        {
            for(int j = 0; j <latitudeUnique.length;j ++)
            {
                double difLat = (latitudeUnique[line] - latitudeUnique[j]);
                double difLong = (longitudeUnique[line] - longitudeUnique[j]);
                if(i == j)
                    if((difLat < 10 ) && (difLong < 10))
                        weightTemp[i][j] = (difLat * difLat) + (difLong * difLong);
                    else
                        weightTemp[i][j] = 0;
                //todo create a function of distance;
                else
                    weightTemp[i][j] = 0;
            }
        }
        */
        return weights;
    }

    /* Taking the single values in order to run a simple regression beetween the features */
    // Case when X2 = alfa0 + alfa1 X1
   /*
        if (features.size() > 1) {
            ArrayList featureObj1 = new ArrayList();
            featureObj1.add(0, features.get(0));
            double[][] featureX1 = features2regress(featureObj1);
            double[] predictedX2 = (double[]) features.get(1);

            // Simple Regression to explain X2 with X1 values.
            MultivariateRegression simpleregressionX2 = new MultivariateRegression(predictedX2, featureX1);


            // Case when X1 = alfa0 + alfa1 X2
            ArrayList featureObj2 = new ArrayList();
            featureObj2.add(0, features.get(1));
            double[][] featureX2 = features2regress(featureObj2);
            double[] predictedX1 = (double[]) features.get(0);

            //// Simple Regression to explain X1 with X2 values.
            MultivariateRegression simpleregressionX1 = new MultivariateRegression(predictedX1, featureX2);

            MultivariateRegressionoutput += "\n X2 =" + simpleregressionX2.beta[0] + "+" + simpleregressionX2.beta[1] + " * X1 " +
                    "\n with rsquared " + simpleregressionX2.rSquared +
                    "\n X1 = " + simpleregressionX1.beta[0] + simpleregressionX1.beta[1] + " * X2 " //+  Arrays.toString(simpleregressionX1.beta
                    + "\n with rsquared " + simpleregressionX1.rSquared;
        }

        //Calculating the difference between the variables before calibration
        for (int i = 0; i < features.size(); i++) {
            double diff = diff(predicted, (double[]) features.get(i));
            MultivariateRegressionoutput += "\n Y - X" + i + " = " + diff;

            for (int k = 0; k < features.size(); k++) {
                if (i != k) {
                    double diffx = diff((double[]) features.get(i), (double[]) features.get(k));
                    MultivariateRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                }
            }
        }
        double[] val = (double[]) features.get(0);
        // apply the calibration to the values and get the diff
        double calibrated[] = new double[predicted.length];
        double calibratedResidual[] = new double[predicted.length];
        double alternativeCalibratedResidual[] = new double[predicted.length];
        double alternativeCalibratedSimple[] = new double[predicted.length];

        //Exploring different approachs for calibrating
        for (int i = 0; i < predicted.length; i++) {
                /*approach for calibration including/excluding residuals*/
/*
            calibrated[i] = predicted[i] - multivariateregression.beta[0];
            calibratedResidual[i] = predicted[i] - multivariateregression.beta[0] - multivariateregression.residuals[i];

                /*approach for multivariate regression, when we consider the difference between the calibration
                 value and the weighted mean (regression parameters) of the features*/
/*
            alternativeCalibratedResidual[i] = predicted[i] - multivariateregression.beta[0] - multivariateregression.residuals[i];
            alternativeCalibratedSimple[i] = predicted[i] - multivariateregression.beta[0];

            double sum = 0;
            for (int j = 1; j < multivariateregression.beta.length; j++) {
                sum += multivariateregression.beta[j];
            }
            calibrated[i] = calibrated[i] / sum;
            calibratedResidual[i] = calibratedResidual[i] / sum;
        }
*/
    //Calculating the weighted mean between the regression parameters and the feature variables to test calibrated values
/*        if (features.size() > 1)
        {
            double[] sumFeatBeta = new double[((double[])features.get(0)).length];
            for(int i = 0; i < ((double[])features.get(0)).length; i++)
            {
                for (int j = 0; j < features.size(); j++)
                {
                    sumFeatBeta[j] =  multivariateregression.beta[j] * ((double[])features.get(j))[i];
                }
            }

            double diffResidualOttoCalib = diff(alternativeCalibratedResidual, sumFeatBeta);
            double diffSimpleOttoCalib = diff(alternativeCalibratedSimple, sumFeatBeta);
            MultivariateRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diffSimpleOttoCalib;
            MultivariateRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diffResidualOttoCalib;

        }

        for (int i = 0; i < features.size(); i++)
        {
            double diff = diff(calibrated, (double[]) features.get(i));
            double diffResidual = diff(calibratedResidual, (double[]) features.get(i));
            MultivariateRegressionoutput += "\n Ycalib - X" + i + " = " + diff;
            MultivariateRegressionoutput += "\n YcalibResidual - X" + i + " = " + diffResidual;
        }
        //text.append(MultivariateRegressionoutput);
        regressFile.write_txt(MultivariateRegressionoutput);

        multivariateregression.findBestMultipleRegression();
        multivariateregression.buildRLSRegression();

        //Testing the Geometric Mean Approach

        multivariateregression.geometricMeanRegression();

        Log.d(TAG, "End of regression");

        this.robustRegressionoutput = "\n********Multivariate Robust Regression **********\n" +
                "Parameters: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.beta) +
                //  "\nresiduals: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.residuals) +
                //       "\nParameters Variance:" + Arrays.deepToString(multivariateregression.cleanedMultivariateRegression.parametersVariance) +
                "\nVariance: " + multivariateregression.cleanedMultivariateRegression.regressandVariance +
                "\nrSquared: " + multivariateregression.cleanedMultivariateRegression.rSquared +
                "\nAdjusted rSquared:" + multivariateregression.cleanedMultivariateRegression.AdjustedrSquared +
                "\n mean residuals: " + this.getMean(multivariateregression.cleanedMultivariateRegression.residuals) +
                "\n N. Obs. Robust: " + multivariateregression.y2regressClean.length +
                "\n median residuals: " +  this.getMedian(multivariateregression.cleanedMultivariateRegression.residuals)  +
                "\n std residuals: " + this.std(multivariateregression.cleanedMultivariateRegression.residuals);


        // Taking the clean values from the robust regression

        Log.d(TAG, "Size of Y Cleaned: " + multivariateregression.y2regressClean.length);
        ArrayList<double[]> featuresCleaned = new ArrayList<>();
        for (int k = 0; k < features.size(); k++)
        {
            double[] featureTemp = new double[multivariateregression.x2regressClean.length];
            for (int j = 0; j < multivariateregression.x2regressClean.length; j++)
            {
                featureTemp[j] = multivariateregression.x2regressClean[j][k];
            }
            featuresCleaned.add(k, featureTemp);
        }
        Log.e(TAG, "Size of features Cleaned: " + featuresCleaned.size());
        Log.e(TAG, "Values of first feature Cleaned: " + Arrays.toString(featuresCleaned.get(0)));

        // Making the difference beetween the values in robust regression before calibration

        for (int i = 0; i < featuresCleaned.size(); i++)
        {
            //  Log.d(TAG, "Making the difference for each cleaned feature and each predicted variable");
            double diff = diff(multivariateregression.y2regressClean, featuresCleaned.get(i));
            robustRegressionoutput += "\n Y - X" + i + " = " + diff;
            for (int k = 0; k < featuresCleaned.size(); k++)
            {
                if (i != k)
                {
                    //    Log.d(TAG, "Making the difference for each cleaned feature");
                    double diffx = diff(featuresCleaned.get(i), featuresCleaned.get(k));
                    robustRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
                }
            }
        }


        //Defining the variables and plotting the evaluating values for the robust case

        double calibratedRobust[] = new double[multivariateregression.y2regressClean.length];
        double calibratedRobustResidual[] = new double[multivariateregression.y2regressClean.length];
        double alternativeCalibratedRobustResidual[] = new double[multivariateregression.y2regressClean.length];
        double alternativeCalibratedRobustSimple[] = new double[multivariateregression.y2regressClean.length];


        for (int i = 0; i < multivariateregression.y2regressClean.length; i++)
        {
            calibratedRobust[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0];

            calibratedRobustResidual[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0] -
                    multivariateregression.cleanedMultivariateRegression.residuals[i];

            alternativeCalibratedRobustResidual[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0] -
                    multivariateregression.cleanedMultivariateRegression.residuals[i];

            alternativeCalibratedRobustSimple[i] = multivariateregression.y2regressClean[i] - multivariateregression.cleanedMultivariateRegression.beta[0];
            double sum = 0;
            for (int j = 1; j < multivariateregression.cleanedMultivariateRegression.beta.length; j++)
            {
                sum += multivariateregression.cleanedMultivariateRegression.beta[j];
            }
            calibratedRobust[i] = calibratedRobust[i] / sum;
            calibratedRobustResidual[i] = calibratedRobustResidual[i] / sum;
        }


        if (featuresCleaned.size() > 1)
        {

            double[] sumFeatBetaRobust = new double[featuresCleaned.get(0).length];
            for(int i = 0; i < ((double[])featuresCleaned.get(0)).length; i++)
            {
                for (int j = 0; j < featuresCleaned.size(); j++)
                {
                    sumFeatBetaRobust[j] = multivariateregression.cleanedMultivariateRegression.beta[j] * featuresCleaned.get(j)[i];
                }
            }

            double diff2Robust = diff(alternativeCalibratedRobustResidual, sumFeatBetaRobust);
            robustRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diff2Robust;

            double diff3Robust = diff(alternativeCalibratedRobustSimple, sumFeatBetaRobust);
            robustRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diff3Robust;


        }

        Log.e(TAG, "Size of features Cleanded: " + featuresCleaned.size());

        for (int i = 0; i < featuresCleaned.size(); i++)
        {
            Log.d(TAG, "Calculating the diff for each calibrated value");
            double diff = diff(calibratedRobust, featuresCleaned.get(i));
            double diffResidualRobust = diff(calibratedRobustResidual, featuresCleaned.get(i));
            robustRegressionoutput += "\n Ycalibrobust - X" + i + " = " + diff;
            robustRegressionoutput += "\n Ycalibresidualrobust  - X" + i + " = " + diffResidualRobust;
        }

        //text.append(robustRegressionoutput);
        regressFile.write_txt(robustRegressionoutput);

        //Taking the parameters from the Geometric Mean Regression

        this.geoMeanRegressionoutput = "\n********Geometric Mean Regression **********\n" +
                "Parameters: " + Arrays.toString(multivariateregression.betaGeoMeanRegress) +
                "rSquared: " + multivariateregression.r2Geo;


        regressFile.write_txt(geoMeanRegressionoutput);
        regressFile.close();

        Log.d(TAG, "End of the calibration regression");
        Log.d(TAG, "Saving Data");

    }
    */
    // }

    /**
     * takes the remote noise record data from the file manager
     * @param meet meeting
     * @param deviceId device id
     * @param namePosition position of the device in the group
     * @param ctx2regress context
     * @return file
     */
    public FileManager device2data(Meet meet ,String deviceId, int namePosition, Context ctx2regress)
    {
        FileManager remoteFile4Noise;
        if (deviceId.startsWith("cross")) {
            remoteFile4Noise = new FileManager("cross" + meet.getRemoteFile4NoiseName(this.meet.meetwithS.get( namePosition)), true, ctx2regress);
            if(remoteFile4Noise.isEmpty())
                fileStatus = remoteFile4Noise.getFilename() + "is empty \n";
            Log.e(TAG, "CROOS CASE");
        } else {
            remoteFile4Noise = new FileManager(meet.getRemoteFile4NoiseName(this.meet.meetwithS.get( namePosition)), true, ctx2regress);
            if(remoteFile4Noise.isEmpty())
                fileStatus = remoteFile4Noise.getFilename() + "is empty \n";
            Log.e(TAG, "Name remote: Testing" + remoteFile4Noise.fileName);
            Log.e(TAG, "NOTCROOS CASE");
        }
        return remoteFile4Noise;
    }

    // These classes take Devices data from a list and put in a double double so as to run a regression.
    //We use the apache regression, which asks primitive data for input
    /* public double[][] features2regress(ArrayList featuresData)
   {
       double[][] data = new double[((double[])featuresData.get(0)).length][featuresData.size()];
       for(int i = 0; i < featuresData.size(); i++)
       {
           double [] columns = (double[]) featuresData.get(i);
           Log.e(TAG,"Features Variables: " + "n_lines:" + ((double[])featuresData.get(0)).length + "n_cols"+ featuresData.size());
           for(int j = 0; j < ((double[]) featuresData.get(0)).length; j++)
           {
               data[j][i] = columns[j];
           }
       }
       return data;
   }
*/

    /**
     * put in the good format the parameters before regression
     * @param featuresData
     * @param ctx context
     * @return matrix
     */
    public double[][] features2regress(ArrayList featuresData, ContextData ctx)
    {
        double[] accumulatedError = ContextData.accumulatedErrorData.getValues();
        double[] accumulatedErrorDistrib = new double[accumulatedError.length];
        if(accumulatedError.length == 0) {
            int sizeCol = featuresData.size();
            double[][] data = new double[((double[]) featuresData.get(0)).length][sizeCol];
            for (int i = 0; i < sizeCol; i++) {
                double[] columns = (double[]) featuresData.get(i);
                Log.e(TAG, "Features Variables: " + "n_lines:" + ((double[]) featuresData.get(0)).length + "n_cols" + featuresData.size());
                for (int j = 0; j < ((double[]) featuresData.get(0)).length; j++) {
                    data[j][i] = columns[j];
                }
            }
            return data;
        }else{
            int sizeCol = featuresData.size();
            ArrayList<Integer> indexDevicesAccurated = new ArrayList<Integer>();
            int k = 0;
            for(int j = 0; j < accumulatedError.length ; j++){
                if(accumulatedError.length < 3){
                    //Two devices case, which means that we just consider the more accurated and goes to the pairwise case
                    if(accumulatedError[j] == ContextData.accumulatedErrorData.getMax())
                        accumulatedErrorDistrib[j] = 1;
                    if(accumulatedError[j] == ContextData.accumulatedErrorData.getMin())
                        accumulatedErrorDistrib[j] = 0;
                    //Pairwise case, which means that we only have one accumulated error to consider
                    if((accumulatedError[j] == ContextData.accumulatedErrorData.getMin()) && (accumulatedError[j] == ContextData.accumulatedErrorData.getMax()))
                        accumulatedErrorDistrib[j] = 1;
                }else {
                    accumulatedErrorDistrib[j] = Math.abs(ContextData.accumulatedErrorData.getMin() - ContextData.accumulatedErrorData.getMax()) / Math.abs(accumulatedError[j] - ContextData.accumulatedErrorData.getMin());
                    Log.e(TAG, "accumulated Error Distrib Value: (" + j + ") " + accumulatedErrorDistrib[j]);
                    if (accumulatedErrorDistrib[j] < 0.5) {
                        sizeCol = sizeCol - 1;
                        Log.e(TAG, "accumulated Error Removed Value: (" + j + ") " + accumulatedErrorDistrib[j]);
                    } else {
                        Log.e(TAG, "accumulated Error Added Value: (" + j + ") " + accumulatedErrorDistrib[j]);
                        indexDevicesAccurated.add(k, j);
                        k++;
                    }
                }
            }
            double[][] data = new double[((double[]) featuresData.get(0)).length][sizeCol];
            for (int i = 0; i < sizeCol; i++) {
                double[] columns = (double[]) featuresData.get(indexDevicesAccurated.get(i));
                Log.e(TAG, "Features Variables: " + "n_lines:" + ((double[]) featuresData.get(0)).length + "n_cols" + sizeCol);
                for (int j = 0; j < ((double[]) featuresData.get(0)).length; j++) {
                    data[j][i] = columns[j];
                }
            }
            return data;
        }
    }

    /**
     * put in the good format the parameters before regression (min paramters are extracted
     * @param alist2regress feaures
     * @param min amount of feature to extract
     * @return regression parameters
     */
    public double[] cast_data2regress(ArrayList<Double> alist2regress , int min  ){
        int i;
        double[] local_y = new double[min];
        for (i = 0 ; i  < min/*alist2regress.size()*/;  i ++) {
            local_y[i] = alist2regress.get(i);
            //    Log.e(TAG, "add " +  local_y[i] ) ;
        }
        return local_y;
    }

    //These classes takes the results from the regression and calculates the interesting stastitics for calibration

    /**
     * return the mean of the measurements
     * @param doublelist measurements
     * @return mean of the measurements
     */
    public double getMean( double[] doublelist )
    {
        double meanresidual = 0;
        for (int i =0 ; i< doublelist.length ; i++)
        {
            meanresidual += doublelist[i] / ((double ) doublelist.length);
        }
        return meanresidual;
    }

    /**
     * return the median of the measurements
     * @param doublelist measurements
     * @return median of the measurements
     */
    public double getMedian( double[] doublelist )
    {
        //sort this list
        // take the element that is in the middle
        Arrays.sort(doublelist);
        double median;
        if (doublelist.length % 2 == 0)
            median = (doublelist[doublelist.length/2] + doublelist[doublelist.length/2 - 1])/2;
        else
            median = doublelist[doublelist.length/2];
        Log.e(TAG, "median is : " + median);
        return median;
    }

    /**
     * return the standard deviation
     * @param doublelist measurements
     * @return standard deviation
     */
    public double std(double[] doublelist)
    {
        double mean = getMean(doublelist);
        double sum =0 ;
        for (int i =0; i<doublelist.length ; i++)
        {
            sum += (doublelist[i] - mean ) *  (doublelist[i] - mean ) / doublelist.length;
        }
        return Math.sqrt(sum);
    }

    /**
     * return the summed difference between the two given measurements
     * @param y measurements
     * @param x measurements
     * @return summed difference
     */
    public double diff(double[] y , double[]x)
    {

        double diff =0;
        for (int i =0 ; i< y.length ; i++)
        {
            diff += Math.abs(y[i] - x[i])/y.length;
        }
        return diff;
    }

    /**
     * distance between two devices
      * @param lat1 latitude of device 1
     * @param lon1 longitude of device 1
     * @param lat2 latitude  of device 2
     * @param lon2 longitude of device 1
     * @return distance
     */

    public Double distGeo(double lat1, double lon1, double lat2, double lon2){
        double theta = lon1 - lon2;
        double distAng = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        double distAcos = Math.acos(distAng);
        double distRad = rad2deg(distAcos);
        dist = 100 * distRad * 60 * 1.1515;
        Log.e(TAG, "Dista in DistGeo: " + dist);
        //if((lat1 == lat2)&&(lon1 == lon2)) {
        //  dist = 0.0;
        //}
        return dist;
    }

    /**
     * return degree to radian
     * @param deg degree
     * @return radian
     */
    public double deg2rad(double deg){
        return (deg * Math.PI /180.0);
    }

    /**
     * return radian to degreee
     * @param rad radian
     * @return degree
     */
    public double rad2deg(double rad){
        return (rad * 180.0 / Math.PI);
    }

}
