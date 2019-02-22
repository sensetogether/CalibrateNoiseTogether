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

import java.util.Arrays;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import android.util.Log;

/**
 * The MultivariateRegression class compute a simple/robust multivariate regession
 */
public class MultivariateRegression
{
    public OLSMultipleLinearRegression OLSMultiReg; //apache`s regression multivariate
    private static final String TAG = "OLSMultiReg";
    public double[] beta; //vector of parameters
    public double[] residuals; //vector of residuals
    public double[][] parametersVariance; //parameters' variance matrix
    public double[] parametersStdErrors; //parameters' variance matrix
    public double regressandVariance; //variance of the entire regression
    public double rSquared; //rSquared of the entire regression
    public double AdjustedrSquared; //get Adjusted rSquared of the entire regression
    public double sigma; //get a matrix of residuals' covariance
    public double[][] x2regress;
    public double[] y2regress;
    public double[][] x2regressClean;
    public double[] y2regressClean;
    /* Geometric regression parameters*/
    public double[] betaGeoMeanRegress = new double[2];
    public double r2Geo;// R² of the geometric regression
    public double residualMeanGeometric; // mean of the squared residuals
    public double adjustedRSquarredGeometric;



    public ArrayList<double[]> outliersX = new ArrayList<>();
    public ArrayList<Double> outliersY = new ArrayList<>();

    //private Matrix sampleFeaturesMatrix;

    private long randomseed;
    private Random random = new Random();
    public double bestMedian = Double.POSITIVE_INFINITY;
    public MultivariateRegression bestMultivariateRegression ; // obtained with veru data
    public MultivariateRegression cleanedMultivariateRegression;
    public MultivariateRegression geoYtoX;
    public MultivariateRegression geoXtoY;
    public DescriptiveStatistics statsY = new DescriptiveStatistics();
    public DescriptiveStatistics statsX = new DescriptiveStatistics();

    Matrix Features;

    /* instanciate a multivariate regression using the samples provided in the routine */
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


    /* Class created to run sub-samples of the data and find the regression with least median residuals squares */
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

    private void setRandom() {
        random = new Random(getRandomSeed());
    }

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

    public int makeCumul(double[] in)
    {
        int total = 0;
        for (int i = 0; i < in.length; i++)
        {
            total += in[i];
        }
        return total;
    }

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

    // number of combinaision of r elements among n elements
    public static int combinations(int n, int r)  {

        int c = 1, denom = 1, num = 1, i ;

        r = Math.min(r, n - r);

        for (i = 1; i <= r; i++) {

            num *= n - i + 1;
            denom *= i;
        }
        return num / denom;
    }

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


    //todo and discuss the Multivariate case

    //First of all doing the simple approach.

    public void geometricMeanRegression()
    {


        //We want to regress y in x
        int rowDim = Features.getRowDimension();
        double[][] xGeoMulti = new double[rowDim][1];
        double[] xGeoSimple = new double[rowDim]; // store X values
        double[] yGeoResult = new double[rowDim];
        double[] SSresVec = new double[rowDim];
        double[] SSresHat = new double[rowDim];
        double SSres;
        double SStot; //sum of squared deviation of y from its mean
        double beta1; //slope
        double beta0;//intercept
        double yHat;
        double residual;
        DescriptiveStatistics residualsGeo = new DescriptiveStatistics();
        DescriptiveStatistics yGeoResulStat = new DescriptiveStatistics();
        //DescriptiveStatistics statsX = new DescriptiveStatistics();
        Log.e(TAG, "Row Dimension :" + Features.getRowDimension());



        for(int i = 0; i < y2regress.length; i++)
        {
            xGeoMulti[i][0] = Features.get(i,0);
            //yGeo[i][1] = y2regress[i];

            xGeoSimple[i] = Features.get(i,0);
            //xGeoSimple[i] = Features.get(i,1);
            this.statsY.addValue(y2regress[i]);
            this.statsX.addValue(Features.get(i,0));
        }
        this.geoYtoX = new MultivariateRegression(y2regress, xGeoMulti);
        //this.geoXtoY = new MultivariateRegression(xGeoSimple, yGeo);

        // The sinal of the covariance between the Y and X give the sinal of the geoMeanRegressio
        if(this.geoYtoX.beta[1] < 0)
            beta1 = -(this.statsY.getStandardDeviation()/ this.statsX.getStandardDeviation());
        else
            beta1 = this.statsY.getStandardDeviation()/ this.statsX.getStandardDeviation();

        //beta0 = intercept = Y - slope * X
        // beta0 = (makeCumul(y2regress) - beta1* makeCumul(xGeoSimple))/y2regress.length;

//        beta0 = (makeCumul(y2regress)/y2regress.length - beta1* makeCumul(xGeoSimple)/y2regress.length);

        beta0 = (makeCumul(y2regress)/y2regress.length - beta1* makeCumul(xGeoSimple)/y2regress.length);



        this.betaGeoMeanRegress[0] = beta0;
        this.betaGeoMeanRegress[1] = beta1;


        for(int i = 0; i < Features.getRowDimension(); i++)
        {
            //estimated y  = beta0 + beta1 * x
            yGeoResult[i] = this.betaGeoMeanRegress[0] + this.betaGeoMeanRegress[1] * xGeoMulti[i][0];
            residual = (y2regress[i] - yGeoResult[i]); // residual = y _ estimated y
            residualsGeo.addValue(residual);
            SSresVec[i] = residual*residual; // squared of the residual
            yGeoResulStat.addValue(SSresVec[i]);
        }
        SSres = makeCumul(SSresVec);
        yHat = yGeoResulStat.getMean();
        for(int i = 0; i < Features.getRowDimension(); i++)
        {
            SSresHat[i] = (y2regress[i] - yHat)*(y2regress[i] - yHat);
        }
        SStot = makeCumul(SSresHat);
        this.r2Geo = 1 - (SSres/SStot);//calculate R²
        this.residualMeanGeometric = residualsGeo.getMean(); // sum of the  residuals

        //adjusted r squarre
        this.adjustedRSquarredGeometric = 1 - (rowDim -1) / (rowDim -3) * ( 1 - this.r2Geo);

    }
}

