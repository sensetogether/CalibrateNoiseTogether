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
package com.example.colibris.calib.regression;

import android.util.Log;

import com.example.colibris.calib.FileManager;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import Jama.Matrix;

/**
 * The MultiRegression class compute a multivariate regression
 */

public class MultiRegression  {
    /**
     * multivariate regression
     */
    public OLSMultipleLinearRegression regression ;
    /**
     * log-related information
     */
    private static final String TAG = "MultipleRegression";

    /**
     * initialize the multivariate regression
     * @param localFile file containing the sound that has been recorded locally
     * @param remoteFilelist file containing the sound that has been recorded by another remote device
     */
    public MultiRegression (FileManager localFile , List<FileManager> remoteFilelist) {
        Log.e(TAG, "Create a multiple regression");
        this.regression = new OLSMultipleLinearRegression();
        // extract y from the sample obtained locally
        Log.e(TAG, "get samples  to compute Y and X");
        // get  samples the common time series ...

        // extract the samples that have been taken at the same time
        ArrayList <ArrayList<Double>> matrix = localFile.extractCommontimeSeries(remoteFilelist);

        // matrix = (time Y X1 X2 X3 ....)

        //extract y from matrix
        if (matrix.size() > 2){
            // time is the first row and y is the second raw

            // extract Y from the matrix
            Log.e(TAG, " extract Y  (size: " +  matrix.get(1).size() +")");
            double[] y = new double[matrix.get(1).size()];

            Object[] obj =   matrix.get(1).toArray();
//            Log.e(TAG, "obj size" + obj.length);

            for(int i =0 ; i< y.length ; i++){
                y[i] = (double)obj[i];
                // Log.e(TAG, "y(" +i+")=" + y[i]);
            }

            Log.e(TAG, "matrix size:" + matrix.size());

            // extract X1 , X2, ect....
            double[][] x = new double[ matrix.get(0).size()][matrix.size()-2];
            //extract x
            for (int i = 2 ; i< matrix.size() ; i++){
                Log.e(TAG, "allocated x" +(i -2));
                Object[] tmp =  matrix.get(i).toArray();
                Log.e(TAG, "get obj for x" +(i -2));
                for(int j =0 ; j< matrix.get(i).size() ; j++){
                    x[j][i-2] = (double) tmp[j];
                    // Log.e(TAG, "tmp(" +j +")" + x[i-2][j] );
                }
                //x[i] = ArrayUtils.toPrimitive(tmp);
            }

            //regress
            Log.e(TAG, "Start Multivariate Regress y size: " + y.length + " x size: " + x.length);
            regression.newSampleData(y, x);
        }
    }

    /**
     * return soem regression parameters
     * @return characteristics of the regression
     */
    public String toString(){
        String toReturn = new String();

        double[] beta = regression.estimateRegressionParameters();
        for (int i = 0 ; i< beta.length ; i++){
            Log.e(TAG, "\n beta(" + i +")=" + beta[i]);
            toReturn += " beta(" + i +")=" + beta[i] + "\n";
        }

        toReturn += "\n Var(y)=" + 	regression.estimateRegressandVariance();
        Log.e(TAG,  "\n Var(y)=" + 	regression.estimateRegressandVariance());

        toReturn += "\nR-Squared=" + regression.calculateRSquared();
        Log.e(TAG,  "\nR-Squared=" + regression.calculateRSquared());

        toReturn += "\nEstimate std error=" +regression.estimateRegressionStandardError();
        Log.e(TAG,"\nEstimate Std error=" +regression.estimateRegressionStandardError());

        toReturn += "Adjusted R-squared=" + regression.calculateAdjustedRSquared();
        Log.e(TAG,  "Adjusted R-squared=" + regression.calculateAdjustedRSquared());

        toReturn += "\nResidual sum of squares: " + regression.calculateResidualSumOfSquares();
        Log.e(TAG,   "\nResidual sum of squares: " + regression.calculateResidualSumOfSquares());

        double[][] parametersVariance = regression.estimateRegressionParametersVariance();
        for (int i = 0 ; i< parametersVariance.length ; i++){
            toReturn += "\nvar(" + i + ")=" ;
            String  aline = new String();
            for (int j = 0 ; j< parametersVariance[i].length ;j++){
                toReturn += " " + parametersVariance[i][j];
                aline += " " +parametersVariance[i][j];
            }
            Log.e(TAG, "var(" + i + ")=" + aline);
        }

        toReturn += "\nSum of squared deviations of Y from its mean: " + regression.calculateTotalSumOfSquares();
        Log.e(TAG,  "\nSum of squared deviations of Y from its mean: " + regression.calculateTotalSumOfSquares());
        toReturn +="\nestimate error variance: " + regression.estimateErrorVariance();
        Log.e(TAG,  "\nestimate error variance: " + regression.estimateErrorVariance());

        toReturn += "\nstd errors of the regression param: " ;
        double[] regressstd=   regression.estimateRegressionParametersStandardErrors();
        for (int i = 0 ; i< regressstd.length ; i++){
            toReturn += "\nstd(" + i +")=" + regressstd[i];
            Log.e(TAG, "\nstd(" + i +")=" + regressstd[i]);
        }


        double[] residuals = regression.estimateResiduals();
        toReturn += "\nResiduals:" ;
       /* for (int i = 0 ; i< residuals.length ; i++){
            toReturn += "\nresiduals(" + i +")=" + residuals[i];
            Log.e(TAG, "\nresiduals(" + i +")=" + residuals[i]);
        }*/


        return toReturn;
    }

    /**
     * Multivariate regression
     */
    public static class MultivariateRegression
    {
        /**
         * multivariate regression
         */
        public OLSMultipleLinearRegression OLSMultiReg;
        /**
         * log related information
         */
        private static final String TAG = "OLSMultiReg";
        /**
         * vector of beta parameters
         */
        public double[] beta;
        /**
         * residuals
         */
        public double[] residuals; //vector of residuals
        /**
         * variance matrix
         */
        public double[][] parametersVariance;
        /**
         * standard errors
         */
        public double[] parametersStdErrors;
        /**
         * variance matrix
         */
        public double regressandVariance;
        /**
         * r squared of the entire regression
         */
        public double rSquared;
        /**
         * adjusted rSquared of the entire regression
         */
        public double AdjustedrSquared;
        /**
         * sigma
         */
        public double sigma;
        /**
         * x to regress
         */
        public double[][] x2regress;
        /**
         * y to regress
         */
        public double[] y2regress;
        /**
         * x without outliers
         */
        public double[][] x2regressClean;
        /**
         * y without outliers
         */
        public double[] y2regressClean;
        /**
         * x's outliers
         */
        public ArrayList<double[]> outliersX = new ArrayList<>();
        /**
         * y's outliers
         */
        public ArrayList<Double> outliersY = new ArrayList<>();

        /**
         * seed used to generate a random number
          */
        private long randomseed;
        /**
         * random number
         */
        private Random random = new Random();
        /**
         * best median, as established by the robust regression
         */
        public double bestMedian = Double.POSITIVE_INFINITY;
        /**
         * best multivariate regression
         */
        public MultivariateRegression bestMultivariateRegression ; // obtained with veru data
        /**
         * best multivariate regression without outliers
         */
        public MultivariateRegression cleanedMultivariateRegression;
        /**
         * matrix of features
         */
        Matrix Features;

        /**
         *  instanciate a multivariate regression using the samples provided in the routine */
        public MultivariateRegression (double[] localFile2regress, double[][] remoteFile2regress) {
            this.OLSMultiReg = new OLSMultipleLinearRegression();
            //  Log.d(TAG,"Beginning the OLS Method for a Multivariate Regression");

            x2regress = remoteFile2regress;
            y2regress = localFile2regress;

            //  Log.e(TAG, "y2regress LENGTH:" + y2regress.length);
            // Log.e(TAG, "x2regress LENGTH:" + x2regress.length);

            this.OLSMultiReg.newSampleData(y2regress, x2regress);
            this.beta = this.OLSMultiReg.estimateRegressionParameters();
            this.residuals = this.OLSMultiReg.estimateResiduals();
            this.parametersVariance = this.OLSMultiReg.estimateRegressionParametersVariance();
            this.parametersStdErrors = this.OLSMultiReg.estimateRegressionParametersStandardErrors();
            this.regressandVariance = this.OLSMultiReg.estimateRegressandVariance();
            this.rSquared = this.OLSMultiReg.calculateRSquared();
            this.AdjustedrSquared = this.OLSMultiReg.calculateAdjustedRSquared();
            this.sigma = this.OLSMultiReg.estimateRegressionStandardError();
            // Defining a features matrix to make sub-sample process easier, when running a robust regression.
            Features = new Matrix(x2regress);
            //  Log.d(TAG,"Finishing the OLS Method for a Multivariate Regression");
        }


        /**
         * created to run sub-samples of the data and find the regression with least median residuals squares */
        public void findBestMultipleRegression()
        {
            //  Log.d(TAG, "Find regression with least median residuals squared to build a multiple robust regression");
            this.bestMedian = Double.POSITIVE_INFINITY;

            //Defining the number of sub-sets two run, based upon the data provided.
            //Inspired in the book Robust Regression and Outlier Detection - Rousseeuw and Leroy
            int stuf[] = new int[]{500, 50, 22, 17, 15, 14};


            //Number of "good" points choose
            int sample_size = 4;
            //Number of subsamples choose to run the regressions
            int sample_Nb = 1;

            if (sample_size < 7)
            {
                if (this.Features.getRowDimension() < stuf[sample_size - 1])
                {
                    if (this.Features.getRowDimension() <= sample_size)
                    {
                        sample_size = this.Features.getRowDimension() / 2;
                        sample_Nb = 1;
                        //we should call simple regression in that case
                    } else
                    {
                        sample_Nb = combinations(this.Features.getRowDimension(), sample_size);
                    }
                } else {//todo set the sample Nb
                    sample_Nb = sample_size * 500;
                }
            } else
            {
                sample_Nb = 3000;
            }


            // Log.e(TAG, "Number of samples to generate: " + sample_Nb);

            for (int s = 0; s < sample_Nb; s++)
            {
                //Selecting the subsample to run the regression and test if it has the least median squares
                //  Log.e(TAG, "Extract a subset of " + sample_size + " samples");
                List<Integer> sample_index = getSampleIndex(sample_size);
                //    Log.e(TAG, "Extract a subset of the current indexes: " + Arrays.toString(sample_index.toArray()));
                double[][] SubSampleX = this.getSampleFeatures(sample_index, sample_size);
                double[] SubSampleY = this.getSamplePredictedVariables(sample_index, sample_size);

                //Running a regression in the sub-sample
                MultivariateRegression currentMultivariateRegression = new MultivariateRegression(SubSampleY, SubSampleX);

                //  Log.e(TAG, "regression of this sample: slope =" + currentMultivariateRegression.beta);

                // find the residuals considering the line established using the 4 samples
                List<Double> squaredResiduals = getSquaredResiduals(currentMultivariateRegression.beta);

                //Verifying if the current regression has the least median of squared residuals
                double median = getMyMedian(squaredResiduals);
                //Log.e(TAG, "MEDIAN of current regression is " + median);
                if(median == 0)
                {
                    //    Log.e(TAG, "Overfitting");
                }
                if (median < this.bestMedian && 0 < median)
                {
                    // Log.e(TAG, "this regression is the best current median = " + median + "<" + this.bestMedian);
                    this.bestMedian = median;
                    this.bestMultivariateRegression = currentMultivariateRegression;
                }
            }
            //  Log.d(TAG, "Finishing the find Best Median Class");
        }


        /**
         * Builds a weight function removing instances with an abnormally high scaled
         * residual, in other words, outliers.
         *
         * @throws Exception if weight building fails
         */

        private double[] buildWeight()
        {
            //   Log.d(TAG, "Creating weights so as to clean the outliers, that contamines the data");
            List<Double> residuals =  this.getSquaredResiduals( this.bestMultivariateRegression.beta);
            double scalefactor;
            if(this.bestMedian == 0)
                scalefactor = 1.4826 * (1 + 5 / ( this.Features.getRowDimension() - this.Features.getColumnDimension() /* m_Data.numAttributes()*/)) * Math.sqrt(this.bestMedian);
            else
                scalefactor = 1.4826 * (1 + 5 / ( this.Features.getRowDimension() - this.Features.getColumnDimension() /* m_Data.numAttributes()*/)) * Math.sqrt(this.bestMedian);
            //  Log.e(TAG, "scaled factor:" + scalefactor);
            double[] weight  = new double[ residuals.size()];

            for (int i = 0; i < residuals.size(); i++) {
                weight[i] = ((Math.sqrt( residuals.get(i)) <= 2.5 * scalefactor) ? 1.0 : 0.0);
            }
            //Log.d(TAG, "End building weights");
            return weight;
        }



        /**
         * Builds a new LinearRegression without outliers found by buildWeight
         */

        public void buildRLSRegression()
        {
            //Log.d(TAG, "Beginning the Robust Regression");
            double [] weight =  buildWeight();
            // create a new regression and filling it with all the data point
            int cleanLines = makeCumul(weight);
            //   Log.e(TAG, "The number of clean lines is: " + cleanLines);
            y2regressClean = new double[cleanLines];
            x2regressClean = new double[cleanLines][x2regress[0].length];
            int i = 0;
            int j = 0;
            // Log.e(TAG, "Cleaning the data");
            for (int x = 0 ; x < weight.length ; x++)
            {
                if (!(weight[x] == 0))
                {
                    //remove the element at position x
                    //  Log.e(TAG, "not outlier: (" + i+ " line " + x + " with y :" + y2regress[x] + "");
                    //creating a cleaned vector of preficted variables
                    y2regressClean[i] = y2regress[x];
                    for (int k =0 ; k< x2regress[0].length ; k++)
                    {
                        //creating a cleaned "matrix" of feature variables
                        x2regressClean[i][k] = x2regress[x][k];
                    }
                    i = i + 1;
                }else
                // Log.e(TAG, "Data is cleaned");
                {
                    // Log.e(TAG, "Saving the outliers");
                    //  Log.e(TAG, "outlier: line =" + x + " value:" + y2regress[x]);
                    outliersX.add(j, x2regress[x]);
                    outliersY.add(j, y2regress[x]);
                    j = j + 1;
                }
            }

            if (x2regressClean.length == 0 &&  y2regressClean.length == 0) {
                //Log.e(TAG, "Rls Regression Unbuilt - All the data is contaminated");
            }

            this.cleanedMultivariateRegression = new MultivariateRegression(this.y2regressClean,this.x2regressClean);


            this.cleanedMultivariateRegression.beta  = cleanedMultivariateRegression.OLSMultiReg.estimateRegressionParameters();
            this.cleanedMultivariateRegression.parametersVariance = cleanedMultivariateRegression.OLSMultiReg.estimateRegressionParametersVariance();
            this.cleanedMultivariateRegression.parametersStdErrors = cleanedMultivariateRegression.OLSMultiReg.estimateRegressionParametersStandardErrors();
            this.cleanedMultivariateRegression.residuals =  cleanedMultivariateRegression.OLSMultiReg.estimateResiduals();
            this.cleanedMultivariateRegression.regressandVariance = cleanedMultivariateRegression.OLSMultiReg.estimateRegressandVariance();
            this.cleanedMultivariateRegression.rSquared = cleanedMultivariateRegression.OLSMultiReg.calculateRSquared();
            this.cleanedMultivariateRegression.AdjustedrSquared = cleanedMultivariateRegression.OLSMultiReg.calculateAdjustedRSquared();
            this.cleanedMultivariateRegression.sigma = cleanedMultivariateRegression.OLSMultiReg.estimateRegressionStandardError();

        }

        //Function that creates subsamples so as to running Robust Regression

        /**
         * Produces a random sample from m_Data in m_SubSample
         *
         * @param sampleSize size of the sample to extract
         */

        /**
         * Set the seed for the random number generator
         * @param randomseed the seed
         */
        public void setRandomSeed(long randomseed) {
            this.randomseed = randomseed;
        }

        /**
         * get the seed for the random number generator
         *
         * @return the seed value
         */
        public long getRandomSeed() {
            return this.randomseed;
        }

        /**
         * set a random variable
         */
        private void setRandom() {
            random = new Random(getRandomSeed());
        }

        /**
         * return samples
         * @param sampleSize amount of sample
         * @return samples
         */
        public List<Integer> getSampleIndex(int sampleSize)
        {
            //create a sample
            int colDim = Features.getColumnDimension();
            List<Integer> sampleIndex2regress = new ArrayList<>();
            if (colDim > sampleSize)
            {
                return null;
            }
            else
            {
                while(sampleIndex2regress.size() != sampleSize)
                {
                    int j = (int) (random.nextDouble() * Features.getRowDimension());
                    if (!sampleIndex2regress.contains(j))
                    {
                        sampleIndex2regress.add(j);
                    }
                    //  Log.d(TAG, sampleIndex2regress.toString());
                }
            }
            return sampleIndex2regress;
        }

        /**
         * return the features
         * @param sampleIndex samples
         * @param sampleSize sample size
         * @return features
         */
        public double[][] getSampleFeatures(List<Integer> sampleIndex, int sampleSize)
        {
            //   Log.e(TAG, "sample size: " + sampleSize + " features size: " + Features.getRowDimension() + " predicted variable size: " + y2regress.length);

            int colDim = Features.getColumnDimension();
            double[][] sampleFeatures = new double[sampleSize][colDim];
            double[] samplePredictedVariable2 = new double [sampleSize];
            // Log.e(TAG, "sampleSize " + String.valueOf(sampleFeatures == null));

            if(colDim > sampleSize)
                return null;
            else
                for(int i = 0; i < sampleIndex.size(); i++)
                {
                    for (int k = 0; k < colDim; k++)
                    {
                        sampleFeatures[i][k] = Features.get(sampleIndex.get(i), k);
                    }
                    samplePredictedVariable2[i] = y2regress[sampleIndex.get(i)];
                }
            return sampleFeatures;
        }

        /**
         * predicted variables
         * @param sampleIndex samples
         * @param sampleSize sample size
         * @return predicted variables
         */
        public double[] getSamplePredictedVariables(List<Integer> sampleIndex, int sampleSize)
        {

            //create a sample

            //  Log.e(TAG, "sample size: " + sampleSize + " features size: " + Features.getRowDimension() + " predicted variable size: " + y2regress.length);

            int colDim = Features.getColumnDimension();
            double[] samplePredictedVariable2 = new double [sampleSize];

            if(colDim > sampleSize)
                return null;
            else
                for(int i = 0; i< sampleIndex.size(); i++)
                {
                    samplePredictedVariable2[i] = y2regress[sampleIndex.get(i)];
                }
            return samplePredictedVariable2;
        }

        /**
         * sum the provided samples
         * @param in samples
         * @return sum of the samples
         */
        public int makeCumul(double[] in)
        {
            int total = 0;
            for (int i = 0; i < in.length; i++)
            {
                total += in[i];
            }
            return total;
        }

        /**
         * return the median
         * @param list samples
         * @return median
         */
        public double getMyMedian(List<Double> list ){
            // create a list containing only the sound data (without the time
            //sort this list
            // take the element that is in the middle

            double[] listBuf = new double[list.size()];
            for(int i =0 ; i< list.size() ; i++){
                listBuf[ i ] = list.get(i);
            }

            Arrays.sort(listBuf);

            double median;
            if (listBuf.length % 2 == 0)
                median = (listBuf[listBuf.length/2] + listBuf[listBuf.length/2 - 1])/2;
            else
                median = listBuf[listBuf.length/2];

            // Log.e(TAG, "median is : " + median);
            return median;

        }

        /**
         * number of combination of r elements among n elements
         * @param n total amount of elements
         * @param r amount of subsamples
         * @return number of combination
         */
        //
        public static int combinations(int n, int r)  {

            int c = 1, denom = 1, num = 1, i ;

            r = Math.min(r, n - r);

            for (i = 1; i <= r; i++) {

                num *= n - i + 1;
                denom *= i;
            }
            return num / denom;
        }

        /**
         * returns the squared residuals
         * @param parameters parameters
         * @return sauqred residuals
         */
        private List<Double> getSquaredResiduals(double[] parameters)  {
            List<Double> residuals  = new ArrayList<Double>();
            double[] residualTemp = new double[Features.getRowDimension()];
            for (int i = 0; i <  Features.getRowDimension() ; i ++) {
                if(Features.getColumnDimension() > 1)
                {
                    //Multiavariate Case
                    for(int j = 1; j < Features.getColumnDimension(); j++) {
                        residualTemp[i] = parameters[j] * Features.get(i, j);
                    }
                    double residual = y2regress[i] - ( parameters[0] + residualTemp[i]);
                    residual *= residual;
                    residuals.add(residual);
                }else{
                    //Simple Case
                    double residual = y2regress[i] - (parameters[0] + (parameters[1] * Features.get(i, 0)));
                    residual *= residual;
                    residuals.add(residual);
                }
                //
            }
            return residuals;
        }
    }
}
