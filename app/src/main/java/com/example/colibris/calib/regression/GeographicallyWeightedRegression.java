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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
//import java.util.stream.StreamSupport;

import android.util.Log;

import com.example.colibris.calib.regression.MultiRegression;


public class GeographicallyWeightedRegression {
    /**
     * The GeographicallyWeightedRegression compute the georgraphically weighted, multivariate regression
     */
    public GLSMultipleLinearRegression GLSMultiReg;
    /**
     * log related information
     */
    private static final String TAG = "GLSMultiReg";
    /**
     * qr decomposition
     */
    private QRDecomposition qr;
    /**
     * regression related parameter: beta
     */
    public ArrayList<double[]> beta = new ArrayList<>();
    /**
     * regression related parameter:min
     */
    public ArrayList<Double>  betaSummaryMin = new ArrayList<Double>();
    /**
     * regression related parameter: max
     */
    public ArrayList<Double>  betaSummaryMax = new ArrayList<Double>();
    /**
     * regression related parameter: mean
     */
    public ArrayList<Double>  betaSummaryMean = new ArrayList<Double>();
    /**
     * column
     */
    public ArrayList<DescriptiveStatistics>  columnParameters = new ArrayList<DescriptiveStatistics>();


    /**
     * map
     */
    public ArrayList<ArrayList<Double>> mpMaps = new ArrayList<ArrayList<Double>>();

    /**
     * regression related parameter: beta  statistics
     */
    public ArrayList<DescriptiveStatistics>  betaStat = new ArrayList<DescriptiveStatistics>();
    /** vector of residuals
     *
     */
    public ArrayList<double[]> residuals = new ArrayList<>();
    /**
     * parameters' variance matrix
     */
    public ArrayList<double[][]> parametersVariance;

    /**
     * stadard error
     */
    public ArrayList<double[]> parametersStdErrors = new ArrayList<>();

    /**
     * regressand variance
     */
    public ArrayList<Double> regressandVariance = new ArrayList<>();
    /**
     * r squared of the entire regression
     */
    public double fValue;
    /**
     * Adjusted rSquared of the entire regression
     */
    public double AdjustedrSquared;
    /**
     *     get a matrix of residuals' covariance
     */
    public ArrayList<Double> sigma;
    /**
     * x to regress
     */
    public double[][] x2regress;
    /**
     * y to regress
     */
    public double[] y2regress;
    /**
     * weight
     */
    public double[][] weight;
    public double[][] x2regressClean;
    public double[] y2regressClean;


    /**
     *  geometric mean regression
     *  */
    public double[] betaGeoMeanRegress = new double[2]; // geometric mean regression alpha and beta parameter


    public double r2Geo;
    /**
     * x outliers
     */
    public ArrayList<double[]> outliersX = new ArrayList<>();
    /**
     * y outliers
     */
    public ArrayList<Double> outliersY = new ArrayList<>();

    //private Matrix sampleFeaturesMatrix;
    /**
     * seed used to generate a random number
     */
    private long randomseed;
    /**
     * random number
     */
    private Random random = new Random();
    /**
     * best median established by the robust regression
     */
    public double bestMedian = Double.POSITIVE_INFINITY;
    /**
     * best multi variate regression (as established by the robust regression
     */
    public MultiRegression.MultivariateRegression bestMultivariateRegression ;
    /**
     * multivariate regression without outliers
     */
    public MultiRegression.MultivariateRegression cleanedMultivariateRegression;
    /**
     * geographical aware multivariate regression
     */
    public MultiRegression.MultivariateRegression geoYtoX;
    /**
     * geographical aware multivariate regression
     */
    public MultiRegression.MultivariateRegression geoXtoY;
    /**
     * statistics on y
     */
    public DescriptiveStatistics statsY = new DescriptiveStatistics();
    /**
     *     statistics on x
     */
    public DescriptiveStatistics statsX = new DescriptiveStatistics();
    /**
     * features
     */
    Matrix Features;
    /**
     * weights
     */
    Matrix Weight0;

    /**
     * instanciate a multivariate regression using the samples provided in the routine
     * @param localFile2regress measurements colected locally
     * @param remoteFile2regress measurements provided by other devices
     * @param geographicallyWeights weights to apply
     * @param sizeCol number of columns
     * @param sizeLine number of lines
     */

    public GeographicallyWeightedRegression (double[] localFile2regress, double[][] remoteFile2regress,/*double[][] geographicallyWeights*/ HashMap<Integer,double[][]> geographicallyWeights , int sizeCol , int sizeLine) {
        this.GLSMultiReg = new GLSMultipleLinearRegression();
        //  Log.d(TAG,"Beginning the OLS Method for a Multivariate Regression");
/*
        this.beta = new double[sizeLine][sizeCol];
        this.residuals = new double[sizeLine][sizeCol];
        this.parametersStdErrors = new double[sizeLine][sizeCol];
        this.regressandVariance = new double[sizeLine];
        this.sigma = new double[sizeLine];
*/

        for(int line = 0; line < sizeLine; line ++) {

            //int line = 0;
            Log.e(TAG, "I am in the line : (" + line + ")" );

            x2regress = remoteFile2regress;
            y2regress = localFile2regress;
            //weight = geographicallyWeights.get(line);
            weight = geographicallyWeights.get(line);

            Log.e(TAG, "weights : " + Arrays.deepToString(weight));
            Log.e(TAG, "weight LENGTH: " + weight.length);
            // Log.e(TAG, "x2regress LENGTH:" + x2regress.length);

            this.GLSMultiReg.newSampleData(y2regress, x2regress, weight);
            //this.beta[line] = this.GLSMultiReg.estimateRegressionParameters();
            Log.e(TAG, "Regress is done");
            this.beta.add(line, this.GLSMultiReg.estimateRegressionParameters());
            this.residuals.add(line, this.GLSMultiReg.estimateResiduals());
//            this.parametersVariance.add(line, this.GLSMultiReg.estimateRegressionParametersVariance());
            this.parametersStdErrors.add(line, this.GLSMultiReg.estimateRegressionParametersStandardErrors());
            this.regressandVariance.add(line, this.GLSMultiReg.estimateRegressandVariance());
            Features = new Matrix(x2regress);
            // Defining a weighted matrix to calculate the hat matrix.
            if(line == 0)
                Weight0 = new Matrix(weight);

            double[] parametersLine = this.GLSMultiReg.estimateRegressionParameters();

            double globalRSS = (sizeLine - sizeCol) * this.GLSMultiReg.estimateErrorVariance();

          /*  Matrix hatWeighted = calculateGLSHat();
            Matrix hatWeightedSquared = hatWeighted.transpose().times(hatWeighted);
            double spatialRSS = (sizeLine - (2 * hatWeighted.trace() - hatWeightedSquared.trace())) * this.GLSMultiReg.estimateErrorVariance();

            this.fValue = spatialRSS / globalRSS;
            */

            ArrayList<Double> mp = new ArrayList<Double>();
            for(int j = 0; j < parametersLine.length; j++) {
                mp.add(j,parametersLine[j]);
            }
            mpMaps.add(line, mp);
            //this.rSquared = this.GLSMultiReg;
            //this.AdjustedrSquared = this.OLSMultiReg.calculateAdjustedRSquared();
            //this.sigma.add(line, this.GLSMultiReg.estimateRegressionStandardError());
            // Defining a features matrix to make sub-sample process easier, when running a robust regression.
            //Features = new Matrix(x2regress);
            //  Log.d(TAG,"Finishing the OLS Method for a Multivariate Regression");
            Log.d(TAG, "Size Map :" + " (" + line + ") " + mpMaps.size());
        }

        for(int columnKey = 0; columnKey < mpMaps.get(0).size(); columnKey ++)
        {
            DescriptiveStatistics columnStat = new DescriptiveStatistics();
            for(int lineKey = 0; lineKey < mpMaps.size(); lineKey ++)
            {
                Log.d(TAG, "Value Line :" + " (" + lineKey + ") " + mpMaps.get(lineKey).get(columnKey));
                columnStat.addValue(mpMaps.get(lineKey).get(columnKey));
            }
            Log.e(TAG,"size stat :" + columnStat.getN());
            columnParameters.add(columnKey, columnStat);
            //double minPar = (b) -> columnTemp.stream().reduce(b -> b.min().getAsDouble());
        }

        //Calculating the min value of the parameters
        for(int i = 0; i < columnParameters.size(); i ++)
        {
            this.betaSummaryMin.add(i, columnParameters.get(i).getMin());
        }
        //Calculating the max value of the parameters
        for(int i = 0; i < columnParameters.size(); i ++)
        {
            this.betaSummaryMax.add(i, columnParameters.get(i).getMax());
        }
        //Calculating the mean value of the parameters
        for(int i = 0; i < columnParameters.size(); i ++)
        {
            this.betaSummaryMean.add(i, columnParameters.get(i).getMean());
        }
        // Defining a features matrix to make sub-sample process easier, when running a robust regression. We use the Features to calculate the Hat matrix as well.

    }

    //todo Calculate the value of F, in order to do some hypothesis tests



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
            MultiRegression.MultivariateRegression currentMultivariateRegression = new MultiRegression.MultivariateRegression(SubSampleY, SubSampleX);

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

        this.cleanedMultivariateRegression = new MultiRegression.MultivariateRegression(this.y2regressClean,this.x2regressClean);


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
        double[] xGeoSimple = new double[rowDim];
        double[] yGeoResult = new double[rowDim];
        double[] SSresVec = new double[rowDim];
        double[] SSresHat = new double[rowDim];
        double SSres;
        double SStot;
        double beta1;
        double beta0;
        double yHat;
        DescriptiveStatistics yGeoResulStat = new DescriptiveStatistics();
        //DescriptiveStatistics statsX = new DescriptiveStatistics();
        Log.e(TAG, "Row Dimension :" + Features.getRowDimension());
        for(int i = 0; i < y2regress.length; i++)
        {
            xGeoMulti[i][0] = Features.get(i,0);
            //yGeo[i][1] = y2regress[i];
            //xGeoSimple[i] = Features.get(i,1);
            this.statsY.addValue(y2regress[i]);
            this.statsX.addValue(Features.get(i,0));
        }
        this.geoYtoX = new MultiRegression.MultivariateRegression(y2regress, xGeoMulti);
        //this.geoXtoY = new MultivariateRegression(xGeoSimple, yGeo);

        // The sinal of the covariance between the Y and X give the sinal of the geoMeanRegression

        if(this.geoYtoX.beta[1] < 0)
            beta1 = -(this.statsY.getStandardDeviation()/ this.statsX.getStandardDeviation());
        else
            beta1 = this.statsY.getStandardDeviation()/ this.statsX.getStandardDeviation();
        beta0 = (makeCumul(y2regress) - beta1* makeCumul(xGeoSimple))/y2regress.length;

        this.betaGeoMeanRegress[0] = beta0;
        this.betaGeoMeanRegress[1] = beta1;


        for(int i = 0; i < Features.getRowDimension(); i++)
        {
            yGeoResult[i] = this.betaGeoMeanRegress[0] + this.betaGeoMeanRegress[1] * xGeoMulti[i][0];
            SSresVec[i] = (y2regress[i] - yGeoResult[i])*(y2regress[i] - yGeoResult[i]);
            yGeoResulStat.addValue(SSresVec[i]);
        }
        SSres = makeCumul(SSresVec);
        yHat = yGeoResulStat.getMean();
        for(int i = 0; i < Features.getRowDimension(); i++)
        {
            SSresHat[i] = (y2regress[i] - yHat)*(y2regress[i] - yHat);
        }
        SStot = makeCumul(SSresHat);
        this.r2Geo = 1 - (SSres/SStot);

    }
/*
    public Matrix calculateGLSHat() {
        Matrix weightInverse = Weight0.inverse();
        Matrix test = Features.times(weightInverse).times(Features.transpose());
        Matrix testInverse = test.inverse();
        return Features.times(testInverse).times(Features.transpose()).times(weightInverse);
    }
    */
}

