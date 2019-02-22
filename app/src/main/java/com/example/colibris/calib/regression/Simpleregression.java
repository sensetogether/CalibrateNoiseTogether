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

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * The Simpleregression class compute a pairwise, simple/robust regression
 */

public class Simpleregression {

    /**
     * simple regression
     */
    public SimpleRegression simpleRegression;
    /**
     * log-related information
     */
    private static final String TAG = "SimpleRegression";
    /**
     * regression intercept
     */
    public double intercept ;
    /**
     * regression slope
     */
    public double slope ;
    /**
     * regression standard error
     */
    public double slope_standard_error;
    /**
     *  sum of squared errors divided by the degrees of freedom, usually abbreviated MSE
     */
    public double means_square_error;//
    /**
     * Pearson's product moment correlation coefficient usually denoted r
     */
    public double R ;
    /**
     * sum of squared deviations of the predicted y values about their mean (which equals the mean of y)
     */
    public double sumSquarre ;
    /**
     * coefficient of determination, usually denoted r-square
     */
    public double RSquare ;
    /**
     * significance level of the slope (equiv) correlation
     */
    public double significance ;
    /**
     * Returns the half-width of a 95% confidence interval for the slope estimate.
     */
    public double slopeConfidenceInterval ;
    /**
     * standard error of the slope estimate, usually denoted s(b1).
     */
    public double slopeStdErr;
    /**
     * Returns the sum of crossproducts, xi*yi.
     */
    public double sumOfCrossProducts;
    /**
     * sum of squared errors (SSE)
     */
    public double sumSquaredErrors;
    /**
     * sum of squared deviations of the y values about their mean
     */
    public double totalSumSquares ;
    /**
     * sum of squared deviations of the x values about their mean.
     */
    public double xSumSquares; //
    /**
     * seed used when creating a random number
     */
    private long randomseed;
    /**
     * random number
     */
    private Random random;
    /**
     * regressand
     */
    public List<Double> list2regress ;
    /**
     * best median
     */
    public double bestMedian = Double.POSITIVE_INFINITY;
    /**
     * best regression as established by the robust regression
     */
    public Simpleregression bestRegression ; // obtained with veru data
    /**
     * best regression without outliers
     */
    public Simpleregression cleanedRegression;


    /*  */

    /**
     * instanciate a simple regression using the samples provided in the list
     * @param alist2regress
     */
    public Simpleregression (List<Double> alist2regress){
        this.simpleRegression = new SimpleRegression();
        cast_data2regress( alist2regress);
        list2regress = new ArrayList<Double>();
        list2regress.addAll(alist2regress);

        setRandomSeed(0); // seed could be better...
        setRandom();
    }

     /**
     * consider only simple regression to populate the regression with the two provided files
     * @param localFile file storing the sound recorded locally
     * @param remoteFile file sroting the sound given by the remote device
     */
    public Simpleregression (FileManager localFile , FileManager remoteFile) {
        this.simpleRegression = new SimpleRegression();
        //extract the data to plot X and Y from the file provided as input
        list2regress = new ArrayList<Double>();
        list2regress.addAll(localFile.getNewCommonTimeSeries(remoteFile, Integer.MAX_VALUE));
        cast_data2regress(list2regress);
        setRandomSeed(0); // todo set a good seed
        setRandom();
    }

     /**
     * populate the regression with the list provided vectgiven or as input
     * @param alist2regress regression input
     */
    private void cast_data2regress(List<Double> alist2regress){
        int i;
        for (i = 0 ; i +1 < alist2regress.size() ;  i = i+2) {
            double local_x = alist2regress.get(i);
            double local_y = alist2regress.get(i + 1);
            //Log.e(TAG, "add " + local_x + " " + local_y ) ;
            this.simpleRegression.addData(local_x,local_y);
        }


        Log.e(TAG, "Simple Regression complete");

        this.intercept  = this.simpleRegression.getIntercept();
        this.slope_standard_error = simpleRegression.getSlopeStdErr();
        this.slope = this.simpleRegression.getSlope();
        this.means_square_error=  simpleRegression.getMeanSquareError();
        this.R = simpleRegression.getR();
        this.sumSquarre = simpleRegression.getRegressionSumSquares();
        this.RSquare = simpleRegression.getRSquare();
        this.significance = simpleRegression.getSignificance() ;
        this.slopeConfidenceInterval = simpleRegression.getSlopeConfidenceInterval();
        this.slopeStdErr = simpleRegression.getSlopeStdErr() ;
        this.sumOfCrossProducts = simpleRegression.getSumOfCrossProducts() ;
        this.sumSquaredErrors = simpleRegression.getSumSquaredErrors() ;
        this.totalSumSquares = simpleRegression.getTotalSumSquares() ;
        this.xSumSquares = simpleRegression.getXSumSquares() ;
        // Log.e(TAG, "intercept" + this.intercept + " slope: " + this.slope + " std err: " + this.slope_standard_error);
    }



    /**
     * return the squared residuals
     * @param _intercept regression intercept
     * @param _slope regression slope
     * @return squared residuals
     */
    private List<Double> getSquaredResiduals(double _intercept , double _slope)  {
        List<Double> residuals  = new ArrayList<Double>();
        double sumofsquareresiduals = 0;
        Log.e(TAG, "find residuals");
        Log.e(TAG, "slope" + _slope + " intecept" + _intercept);
        for (int i = 0; i <  list2regress.size() ; i= i+2) {
            //compute  residual_i = y_i - (alpha + beta i) = y_ i - line

            double residual = list2regress.get(i+1) - ( _intercept + _slope * list2regress.get(i));
            // Log.e(TAG, "residual(" + i +")=" + list2regress.get(i+1)  + " - " +  ( _intercept + _slope * list2regress.get(i)) );
            residual *= residual;
            residuals.add(residual);
//            sumofsquareresiduals += residual * residual;
        }
        //      Log.e(TAG, "computed sum of squared residuals" + sumofsquareresiduals + "apache: " + this.sumSquaredErrors);
        return residuals;
    }

    /**
     * return the median
     * @return median
     */
    public double getMedian( ){
        // create a list containing only the sound data (without the time
        //sort this list
        // take the element that is in the middle

        double[] soundBuf = new double[list2regress.size()/2];
        for(int i =0 ; i< list2regress.size() ; i=i+2){
            soundBuf[ i/2 +1 ] = list2regress.get(i+1);
            // Log.e(TAG, "add " + list2regress.get(i+1) + "to sound(" + (i/2+1) + ")");
        }
        Arrays.sort(soundBuf);
        double median;
        if (soundBuf.length % 2 == 0)
            median = (soundBuf[soundBuf.length/2] + soundBuf[soundBuf.length/2 - 1])/2;
        else
            median = soundBuf[soundBuf.length/2];

        Log.e(TAG, "median is : " + median);
        return median;

    }

    /**
     * return the median
     * @param list measurements
     * @return median of the measurements
     */
    public double getMyMedian(List<Double> list ){
        // create a list containing only the sound data (without the time
        //sort this list
        // take the element that is in the middle

        double[] listBuf = new double[list.size()];
        for(int i =0 ; i< list.size() ; i++){
            listBuf[ i ] = list.get(i);
            //  Log.e(TAG, "add " + list.get(i) + "to squared residual(" + (i) + ")");
        }

        Arrays.sort(listBuf);

/*
        Log.e(TAG, "after sorting the array" );

        for(int i =0 ; i< list.size() ; i++){

            Log.e(TAG, "add " + listBuf[i] + " to squared sorted residual(" + (i) + ")");
        }
*/
        double median;
        if (listBuf.length % 2 == 0)
            median = (listBuf[listBuf.length/2] + listBuf[listBuf.length/2 - 1])/2;
        else
            median = listBuf[listBuf.length/2];

        Log.e(TAG, "median is : " + median);
        return median;

    }

    /**
     * determine the best regression using robust regression
     */
    public void findBestRegression() {
        Log.e(TAG, "**** find best regression to build a robust regression ");
        this.bestMedian = Double.POSITIVE_INFINITY;

        int stuf[] = new int[] { 500, 50, 22, 17, 15, 14 };
        int sample_size = 4 ;
        int sample_Nb = 1;

        if (sample_size < 7) {
            if ( this.list2regress.size() / 2 < stuf[sample_size - 1]) {
                if (this.list2regress.size() / 2 <= sample_size) {
                    sample_size = this.list2regress.size()/2;
                    sample_Nb = 1;
                } else{
                    sample_Nb = combinations(this.list2regress.size() / 2, sample_size);
                }
            } else {//todo set the sample Nb
                sample_Nb = sample_size * 500;
            }
        } else {
            sample_Nb = 3000;
        }
        Log.e(TAG, "Number of samples to generate: "  + sample_Nb);

        //todo we should make only one regression
        sample_Nb = 1;
        // extract a number of sample ; each sample
        for (int s = 0 ; s < sample_Nb; s++ ) {
            // extract in a random manner 4 points from the data set
            Log.e(TAG, "Extract a subset of " +sample_size +" samples");
            List<Double> subsample =  this.getSubSample(sample_size) ;
            //   for (int i =0 ; i<sample_size ; i++){
            //     Log.e(TAG, " sample("+ i + ") : " + subsample.get(i));
            //}


            //regress the 4 points
            Simpleregression currentRegression = new Simpleregression(subsample);
            Log.e(TAG, "regression of this sample: slope =" + currentRegression.slope
                    +" intercept = " + currentRegression.intercept);
            // find the residuals considering the line established using the 4 samples
            List<Double> squarredResiduals = this.getSquaredResiduals(currentRegression.intercept, currentRegression.slope);

            // get the median of all the data points ?
            double median = getMyMedian(squarredResiduals);
            Log.e(TAG, "MEDIAN of current regression is " + median);
            if (median  < this.bestMedian) {
                Log.e(TAG, "this regression is the best current median = " + median +"<"+ this.bestMedian);
                this.bestMedian = median;
                this.bestRegression = currentRegression;
            }
        }

    }

    //
    /**
     * number of combinaision of r elements among n elements
     * @param n total amount of elements
     * @param r amount of some subelements
     * @return number of combinaisons
     */
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
     * Builds a weight function removing instances with an abnormally high scaled
     * residual
     *
     * @throws Exception if weight building fails
     */
    private double[] buildWeight() {
        List<Double> residuals =  this.getSquaredResiduals( this.bestRegression.intercept , this.bestRegression.slope);
        Log.e(TAG, "best median" + this.bestMedian + " with slope" + this.bestRegression.slope + " and intercept " + this.bestRegression.intercept);
        double scalefactor;
        if(this.bestMedian == 0)
            scalefactor = 1.4826 * (1 + 5 / ( this.list2regress.size()/2 - 2 /* m_Data.numAttributes()*/)) * Math.sqrt(this.bestMedian);
        else
            scalefactor = 1.4826 * (1 + 5 / ( this.list2regress.size()/2 - 2 /* m_Data.numAttributes()*/)) * Math.sqrt(this.bestMedian);
        Log.e(TAG, "scaled factor:" + scalefactor);
        double[] weight  = new double[ residuals.size()];
        for (int i = 0; i < residuals.size(); i++) {
            weight[i] = ((Math.sqrt( residuals.get(i)) <= 2.5 * scalefactor) ? 1.0 : 0.0);
            Log.e(TAG, "weigth(" +i +")=" +"=" + weight[i]);
        }
        Log.e(TAG, "End building weights");
        return weight;
    }



    /**
     * Builds a new LinearRegression without the 'bad' data found by buildWeight
     *
     */
    public void buildRLSRegression()   {
        Log.e(TAG, "Build RLS Regression for " + this.list2regress.toString());
        double [] weight =  buildWeight();
        // create a new regression and feed it with all the data point
        cleanedRegression = new Simpleregression(this.list2regress);
        Log.e(TAG, "Build RLS Regression for " + this.list2regress.toString());
        Log.e(TAG, "  weigth lenght " + weight.length);
        for (int x = 0 ; x < weight.length ; x++) {

            Log.e(TAG, "analyse weigth(" +x +") = " +weight[x] + " for data: " + this.list2regress.get(x*2) + ", " +this.list2regress.get(x*2+1) );

            if (weight[x] == 0) {
                //remove the element at position x
                // given that each element z is a paire : z0 z1 z2 =  x0 y0 x1 y1 x2 y2
                Log.e(TAG, " remove outlier:" );
                double xi = this.list2regress.get(x*2);
                Log.e(TAG, " remove outlier at " + x*2 );
                double yi = this.list2regress.get(x*2+1);
                Log.e(TAG, " remove outlier with " + x*2 +1);
                cleanedRegression.list2regress.remove(xi);
                cleanedRegression.list2regress.remove(yi);
                Log.e(TAG, "remove outlier:" + xi +"," + yi);
                cleanedRegression.simpleRegression.removeData(xi,yi);
            }
        }

        if (cleanedRegression.list2regress.size() == 0) {
            Log.e(TAG, "RLS REGRESSION UNBUILT");
        }

        Log.e(TAG, "we are done");
        Log.e(TAG, "list:" + this.list2regress.toString());
        Log.e(TAG, "cleaned list" + cleanedRegression.list2regress.toString());
        Log.e(TAG, "intercept" + this.intercept + " slope: " + this.slope + " std err: " + this.slope_standard_error);

        cleanedRegression.intercept  = cleanedRegression.simpleRegression.getIntercept();
        cleanedRegression.slope_standard_error = cleanedRegression.simpleRegression.getSlopeStdErr();
        cleanedRegression.slope = cleanedRegression.simpleRegression.getSlope();
        cleanedRegression.means_square_error=  cleanedRegression.simpleRegression.getMeanSquareError();
        cleanedRegression.R = cleanedRegression.simpleRegression.getR();
        cleanedRegression.sumSquarre = cleanedRegression.simpleRegression.getRegressionSumSquares();
        cleanedRegression.RSquare = cleanedRegression.simpleRegression.getRSquare();
        cleanedRegression.significance = cleanedRegression.simpleRegression.getSignificance() ;
        cleanedRegression.slopeConfidenceInterval = cleanedRegression.simpleRegression.getSlopeConfidenceInterval();
        cleanedRegression.slopeStdErr = cleanedRegression.simpleRegression.getSlopeStdErr() ;
        cleanedRegression.sumOfCrossProducts = cleanedRegression.simpleRegression.getSumOfCrossProducts() ;
        cleanedRegression.sumSquaredErrors = cleanedRegression.simpleRegression.getSumSquaredErrors() ;
        cleanedRegression.totalSumSquares = cleanedRegression.simpleRegression.getTotalSumSquares() ;
        cleanedRegression.xSumSquares = cleanedRegression.simpleRegression.getXSumSquares() ;
        Log.e(TAG, "CLEANED intercept" + cleanedRegression.intercept + " slope: " + cleanedRegression.slope + " std err: " + cleanedRegression.slope_standard_error);

        //we are done
    }



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
     * set the random parameter
     */
    private void setRandom() {
        random = new Random(getRandomSeed());
    }


    /**
     * Produces a random sample from m_Data in m_SubSample
     *
     * @param sampleSize size of the sample to extract
     */
    public List<Double> getSubSample( int sampleSize)  {

        //create a sample
        List<Double> sample = new ArrayList<Double>();
        Log.e(TAG, "sample size: " + sampleSize + " data size: " + list2regress.size());

        //Log.e(TAG, "random " + random.nextDouble());

        for (int i = 0, j = 0; i < sampleSize; i++) {
            do {
                // take a random number
                //    Log.e(TAG, "random " + random.nextDouble());
                j = (int) (random.nextDouble() * list2regress.size());
                Log.d(TAG, "j: " + j);
            } while (j == 0);

            // if j est pair , ajouter j , j +1
            // sinon ajouter j-1 , j
            if( j  % 2 == 0){
//            Log.e(TAG, "pair =(" + list2regress.get(j)+"," + list2regress.get(j+1) );
                sample.add(list2regress.get(j));
                sample.add(list2regress.get(j+1));
            }else {
                //          Log.e(TAG, "impair =(" + list2regress.get(j)+"," + list2regress.get(j+1) );
                sample.add(list2regress.get(j-1));
                sample.add(list2regress.get(j));
            }

        }
        Log.e(TAG, "Data: " + list2regress.toString());
        Log.e(TAG, "Samples:" + sample.toString());
        return sample;
    }

}