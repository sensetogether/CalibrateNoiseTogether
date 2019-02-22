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

import com.example.colibris.comtool.ContextData;
import com.example.colibris.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.colibris.calib.regression.MultivariateRegression;
import com.example.colibris.configuration.Device;

/**
 * This Meet2regress class is used when a meeting/calibration takes place.
 * In particular, this class permits to handle the calibration. For this
 * purpose, various type of regressions can be  performed
 */
public class Meet2regress {
    /**
     * log related information
     */
    private static final String TAG = "Meet2Regress";
    /**
     * regression features
     */
    public ArrayList features = new ArrayList();
    /**
     * predicted fatures
     */
    public double[] predicted = new double[]{};
    /**
     * x features
     */
    public double[][] xfeatures;
    /**
     * parameters related to the meeting
     */
    public Meet meet = null;
    /**
     * string displaying the characteristics of the multivariate regression
     */
    public String MultivariateRegressionoutput;
    /**
     * string displaying the characteristics of the multivariate  and robust regression
     */

    public String robustRegressionoutput;
    /**
     * string displaying the cahracteristics of the geometric regression
     */

    public String geoMeanRegressionoutput;
    /**
     * simple multivariate regression
     */
    public MultivariateRegression multivariateregression;

    /**
     * rmultivariate regression initialisation
     * @param extras2regress  devices to regress
     * @param ctx2regress context
     */
    public Meet2regress(Bundle extras2regress, Context ctx2regress) {
        /* Taking each devices in a meeting, extracting the noise data, so as to call a calibration regression
         */
        Log.d(TAG, "Beginning Meet 2 Regress ");

        // new meeting is created in order to call proceed the calibration beetween the devices
        //   Log.d(TAG, "Running meet class and taking record data from File Manager");
        this.meet = new Meet(ctx2regress);
        Device device2meet;
        if (extras2regress != null) {
            String vertexId = extras2regress.getString(Configuration.VERTEX_MESSAGE);
            //     Log.e(TAG, "Indexes :" + Arrays.toString(vertexId.split("-")));
            String[] index = vertexId.split("-");
            //  Log.e(TAG, "Vertex id Meet :" + vertexId);
            String device_id = extras2regress.getString(Configuration.DEVICE_ID_MESSAGE);
            List<FileManager> remoteFilelist = new ArrayList<FileManager>();
            for (int i = 0; i < index.length; i++) {
                Integer index_i = new Integer(index[i]);
                device2meet = new Device(index_i, device_id, 0);
                this.meet.add(device2meet, ctx2regress /*cyx*/, false /*append*/); // add the device if the device is not already in
                // display
                for (int j = 0; j < this.meet.meetwithS.size(); j++) {
                    //   Log.e(TAG, "Devices in a meeting calibration: " + Arrays.toString(this.meet.meetwithS.toArray()));
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
            localFile4Noise.close();
            regressMatrix(localFile4Noise, remoteFilelist, ctx2regress);
        }
    }

    /**
     * init the multivariate regression
     * @param localFile4Noise file name where is stored the sound recorded locally
     * @param remoteFilelist file names where are stored the noise recorded by the devices we are meeting with
     * @param ctx2regress context
     */
    public Meet2regress(FileManager localFile4Noise, List<FileManager> remoteFilelist, Context ctx2regress){
        regressMatrix(localFile4Noise, remoteFilelist, ctx2regress);
    }

    /**
     * init the multivariate regression
     * @param localFile4Noise file name where is stored the sound recorded locally
     * @param remoteFilelist  file names where are stored the noise recorded by the devices we are meeting with
     * @param ctx2regress context
     */
    public void regressMatrix(FileManager localFile4Noise, List<FileManager> remoteFilelist, Context ctx2regress) {
        //Creating a matrix with data recorded. This matrix will be used to run a calibrate regression
        ContextData ctxData = new ContextData();
        ArrayList<ArrayList<Double>> matrix = localFile4Noise.extractCommontimeSeries(remoteFilelist, ctxData);
        regressAction(matrix, ctx2regress);
    }

    /**
     * regression performed
     * @param matrix matrix with parameters
     * @param ctx2regress context
     */
    public void regressAction(ArrayList<ArrayList<Double>> matrix, Context ctx2regress){

        //We take the minimum size of the noise vectors. With this value, we create a matrix for regression.
        int min = Integer.MAX_VALUE;
        for (int i = 1; i < matrix.size(); i++) {
            Log.d(TAG, "**********\n ***********\n matrix(" + i + ") = " + matrix.get(i));
            if ((matrix.get(i).size() < min) && (0 < matrix.get(i).size()))
                min = matrix.get(i).size();
        }

        Log.e(TAG, "Size of the regression is" + min);
        Log.d(TAG, "Taking the data recorded and saving in a features list, so as to regress");
        //Here we save each feature data
        //We print each column of the regression here to be sure that we dont have a singular matrix
        for (int i = 1; i < matrix.size(); i++) {
            Log.d(TAG, "**********\n ***********\n matrix(" + i + ") = " + matrix.get(i));
            if(matrix.get(i).size() != 0) {
                if (i == 1) {
                    predicted = cast_data2regress(matrix.get(i), min);
                    Log.e(TAG, "predicted: " + Arrays.toString(predicted));
                    Log.e(TAG, "predicted LENGTH:" + predicted.length);
                } else {
                    double[] c = cast_data2regress(matrix.get(i), min);
                    features.add(i - 2, c);
                }
            }
        }

        /* Begin the multivariate regression approach for more than two devices
         */

        Log.d(TAG, "beginning of the regression");

        this.xfeatures = features2regress(features);
        Log.e(TAG, "xfeatures LENGTH:" + xfeatures.length);
        FileManager regressFile = new FileManager("Regressoutput", false, ctx2regress);

        this.multivariateregression = new MultivariateRegression(predicted, this.xfeatures);
        this.MultivariateRegressionoutput = "\n********Multivariate Simple Regression **********\n" +
                "beta: " + Arrays.toString(multivariateregression.beta) +
                //"\nresiduals: " + Arrays.toString(multivariateregression.residuals) +
                // "\nParameters Variance:" + Arrays.deepToString(multivariateregression.parametersVariance) +
                "\nVariance: " + multivariateregression.regressandVariance +
                "Nb. Obs.: " + predicted.length +

                "\nR^2: " + multivariateregression.rSquared +
                " adjusted R²: " + multivariateregression.AdjustedrSquared +
                "\nResidual: \nmean: " + this.getMean(multivariateregression.residuals) +
                "  median: " +  this.getMedian(multivariateregression.residuals)  +
                "\n std residuals: " + this.std(multivariateregression.residuals);


        /* Taking the single values in order to run a simple regression beetween the features */
        // Case when X2 = alfa0 + alfa1 X1
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

               /* MultivariateRegressionoutput += "\n X2 =" + simpleregressionX2.beta[0] + "+" + simpleregressionX2.beta[1] + " * X1 " +
                        "\n with rsquared " + simpleregressionX2.rSquared +
                        "\n X1 = " + simpleregressionX1.beta[0] + simpleregressionX1.beta[1] + " * X2 " //+  Arrays.toString(simpleregressionX1.beta
                        + "\n with rsquared " + simpleregressionX1.rSquared;
                        */
        }

        //Calculating the difference between the variables before calibration
        for (int i = 0; i < features.size(); i++) {
            double diff = diff(predicted, (double[]) features.get(i));
            //   MultivariateRegressionoutput += "\n Y - X" + i + " = " + diff;

            for (int k = 0; k < features.size(); k++) {
                if (i != k) {
                    double diffx = diff((double[]) features.get(i), (double[]) features.get(k));
                    //         MultivariateRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
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
            //    MultivariateRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diffSimpleOttoCalib;
            //  MultivariateRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diffResidualOttoCalib;

        }

        for (int i = 0; i < features.size(); i++)
        {
            double diff = diff(calibrated, (double[]) features.get(i));
            double diffResidual = diff(calibratedResidual, (double[]) features.get(i));
            //    MultivariateRegressionoutput += "\n Ycalib - X" + i + " = " + diff;
            //   MultivariateRegressionoutput += "\n YcalibResidual - X" + i + " = " + diffResidual;
        }
        //text.append(MultivariateRegressionoutput);
        regressFile.write_txt(MultivariateRegressionoutput);

        multivariateregression.findBestMultipleRegression();
        multivariateregression.buildRLSRegression();

        //Testing the Geometric Mean Approach

        multivariateregression.geometricMeanRegression();

        Log.d(TAG, "End of regression");

        this.robustRegressionoutput = "\n********Multivariate Robust Regression **********\n" +
                "beta: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.beta) +
                //  "\nresiduals: " + Arrays.toString(multivariateregression.cleanedMultivariateRegression.residuals) +
                //       "\nParameters Variance:" + Arrays.deepToString(multivariateregression.cleanedMultivariateRegression.parametersVariance) +
                "\nVariance: " + multivariateregression.cleanedMultivariateRegression.regressandVariance +
                " Nb. Obs.: " + multivariateregression.y2regressClean.length +
                "\nR²: " + multivariateregression.cleanedMultivariateRegression.rSquared +
                "  Adjusted R²:" + multivariateregression.cleanedMultivariateRegression.AdjustedrSquared +
                "\n Residuals: \n mean:" + this.getMean(multivariateregression.cleanedMultivariateRegression.residuals) +
                "\n median: " +  this.getMedian(multivariateregression.cleanedMultivariateRegression.residuals)  +
                " std: " + this.std(multivariateregression.cleanedMultivariateRegression.residuals);


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
            // robustRegressionoutput += "\n Y - X" + i + " = " + diff;
            for (int k = 0; k < featuresCleaned.size(); k++)
            {
                if (i != k)
                {
                    //    Log.d(TAG, "Making the difference for each cleaned feature");
                    double diffx = diff(featuresCleaned.get(i), featuresCleaned.get(k));
                    // robustRegressionoutput += "\nX" + i + "-" + "X" + k + " = " + diffx;
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
            // robustRegressionoutput += "\n (Y - Beta0 - e) - (Beta1X1 + Beta2X2) = " + diff2Robust;

            double diff3Robust = diff(alternativeCalibratedRobustSimple, sumFeatBetaRobust);
            // robustRegressionoutput += "\n (Y - Beta0) - (Beta1X1 + Beta2X2) = " + diff3Robust;


        }

        Log.e(TAG, "Size of features Cleanded: " + featuresCleaned.size());

        for (int i = 0; i < featuresCleaned.size(); i++)
        {
            Log.d(TAG, "Calculating the diff for each calibrated value");
            double diff = diff(calibratedRobust, featuresCleaned.get(i));
            double diffResidualRobust = diff(calibratedRobustResidual, featuresCleaned.get(i));
            //  robustRegressionoutput += "\n Ycalibrobust - X" + i + " = " + diff;
            //  robustRegressionoutput += "\n Ycalibresidualrobust  - X" + i + " = " + diffResidualRobust;
        }

        //text.append(robustRegressionoutput);
        regressFile.write_txt(robustRegressionoutput);

        //Taking the parameters from the Geometric Mean Regression

        this.geoMeanRegressionoutput = "\n********Geometric Mean Regression **********\n" +
                "Parameters: " + Arrays.toString(multivariateregression.betaGeoMeanRegress) +
                "\nmean residuals: " + multivariateregression.residualMeanGeometric +
                "\nrSquared: " + multivariateregression.r2Geo

                + " \nAdjusted Squared:";


        regressFile.write_txt(geoMeanRegressionoutput);
        regressFile.close();

        Log.d(TAG, "End of the calibration regression");
        Log.d(TAG, "Saving Data");

    }
    // }

    /**
     * provide the file manager used to store sound related measurements
     * @param meet meeting
     * @param deviceId  deice id
     * @param namePosition position in the group
     * @param ctx2regress context
     * @return file
     */
    public FileManager device2data(Meet meet ,String deviceId, int namePosition, Context ctx2regress)
    {
        FileManager remoteFile4Noise;
        if (deviceId.startsWith("cross")) {
            remoteFile4Noise = new FileManager("cross" + meet.getRemoteFile4NoiseName(this.meet.meetwithS.get( namePosition)), true, ctx2regress);
            Log.e(TAG, "CROOS CASE");
        } else {
            remoteFile4Noise = new FileManager(meet.getRemoteFile4NoiseName(this.meet.meetwithS.get( namePosition)), true, ctx2regress);
            Log.e(TAG, "Name remote: Testing" + remoteFile4Noise.fileName);
            Log.e(TAG, "NOTCROOS CASE");
        }
        return remoteFile4Noise;
    }


    /**
     * convert a list into a matrix that is further used in the regression
     * @param featuresData
     * @return matrix
     */
    public double[][] features2regress(ArrayList featuresData)
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


    /**
     * extract a certain amount of measurements
     * @param alist2regress measurements
     * @param min amount of measurement to extract
     * @return list of a certain amount of measurements
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


     /**
     * compute the mean of the measurements
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
     * return the median
     * @param doublelist easurements
     * @return median
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
     * sum of the measurements
     * @param doublelist measurements
     * @return sums
     */
    public double sum(double[] doublelist)
    {
        double sum  = 0 ;
        for (int i =0; i<doublelist.length ; i++)
        {
            sum += doublelist[i]*doublelist[i];
        }
        return sum;
    }


    /**
     * return sum of the differences between the provided samples
     * @param y samples
     * @param x other samples
     * @return sum of the differences between the provided samples
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
}

