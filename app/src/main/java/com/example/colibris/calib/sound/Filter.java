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
package com.example.colibris.calib.sound;

import android.util.Log;

import com.example.colibris.calib.sound.Record;
import com.example.colibris.configuration.Configuration;

import java.nio.DoubleBuffer;
import java.util.Arrays;

import static org.apache.commons.math3.stat.StatUtils.sum;

/**
 * This Filter class is used to correlate two sounds
 */

public class Filter {
    /**
     * buffer storing the sound that has been collected locally
     */
    public DoubleBuffer localNoiseBuffer;
    /**
     * log related information
     */
    private static final String TAG = "Filter";
    /**
     * buffer storing the noise recorded by a remote device
     */
    public DoubleBuffer remoteNoiseBuffer;
    /**
     * array storing the sound that has been collected locally
     */
    private double[] remoteNoiseArray;
    /**
     * array storing the sound that has been collected by a remote device
     */
    private double[] localNoiseArray;

    /**
     * time offset
     */
    public int offset =0;
    public double[] cross_array ;
    /**
     * normalized offset
     */
    public int norm_offset =0 ;
    /**
     * normalised array
     */
    public double[] norm_array;
    /**
     * are we windowing
     */
    public boolean isWindowing = false;
    /**
     * max offset
     */
    public long masOffset;
    /**
     * maxDelay foreseen exepressed as a number of samples
     */
    public int maxDelayassumed_NB_SAMPLE = 0; //

    /**
     * we extract a chunk of size windowing_size
     *   this chunk is localed between 2 * WINDOWING_SIZE and 3 * WINDOWING SIZE
     */
    private  int WINDOWING_SIZE_SAMPLE = 0 ;

    /**
     * noise filtering
     * @param alocalNoiseBuffer buffer containing the noise collected locally
     * @param aremoteNoiseBuffer buffer containing the noise collected by a remote buffer
     * @param maxOffset_milli_sec max offset
     */
    public Filter (DoubleBuffer alocalNoiseBuffer , DoubleBuffer aremoteNoiseBuffer, long maxOffset_milli_sec){

        Log.e(TAG, "call filter with max offset (ms): "+maxOffset_milli_sec +" on " +  alocalNoiseBuffer.toString() + "\n and \n " + aremoteNoiseBuffer);
        Log.e(TAG, "call filter with"+ alocalNoiseBuffer.toString() + "\n and \n " + aremoteNoiseBuffer);


        masOffset = maxOffset_milli_sec;

        // extract the local and remote sound
        this.localNoiseBuffer = alocalNoiseBuffer;
        this.remoteNoiseBuffer = aremoteNoiseBuffer;

        Log.e(TAG, "local buffer limit " + alocalNoiseBuffer.limit() + " has remaining " +  alocalNoiseBuffer.hasRemaining() + "capacity" + alocalNoiseBuffer.capacity());
        Log.e(TAG, "remote buffer limit " + aremoteNoiseBuffer.limit() + " has remaining " +  aremoteNoiseBuffer.hasRemaining() + "capacity" + aremoteNoiseBuffer.capacity());


        // compute the maximum assumed time offset
        if(Configuration.isAveraging = true){
            this.maxDelayassumed_NB_SAMPLE = (int) (  maxOffset_milli_sec / (Configuration.samplingDurationSec * 1000));
            this.WINDOWING_SIZE_SAMPLE = (int) (Configuration.chunksize_Sec / Configuration.samplingDurationSec);

            //this.maxDelayassumed_NB_SAMPLE = (int) (  maxOffset_milli_sec * (Configuration.samplingDurationSec * 1000));
            //   this.WINDOWING_SIZE_SAMPLE = (int) (Configuration.chunksize_Sec *1000);
        }else{
            this.maxDelayassumed_NB_SAMPLE = (int) (Record._rate * 1000* maxOffset_milli_sec / Configuration.recordDuration_ms );
            //             this.maxDelayassumed_NB_SAMPLE = (int) (Record._rate *  Configuration.samplingDurationSec * maxOffset_milli_sec);
            this.WINDOWING_SIZE_SAMPLE = (int) (Record._rate * Configuration.chunksize_Sec);
        }

        Log.e(TAG, "max delay assumed (#sample):  "  + maxDelayassumed_NB_SAMPLE + "\n work on " +  WINDOWING_SIZE_SAMPLE );
        if (maxDelayassumed_NB_SAMPLE ==0){
            this.norm_offset = 0;
            this.masOffset = 0;
            return ; // nothing to do
        }

        // increase the windows size if the ntp round trip delay is too big
        while (this.WINDOWING_SIZE_SAMPLE < this.maxDelayassumed_NB_SAMPLE){
            this.WINDOWING_SIZE_SAMPLE = this.WINDOWING_SIZE_SAMPLE *2;
        }


        Log.e(TAG, "max delay assumed (#sample):  "  + maxDelayassumed_NB_SAMPLE + "\n work on " +  WINDOWING_SIZE_SAMPLE );


        if(aremoteNoiseBuffer.capacity() > WINDOWING_SIZE_SAMPLE *3  && alocalNoiseBuffer.capacity() > WINDOWING_SIZE_SAMPLE *3) {
            Log.e(TAG, "Trunk the local noise and remote noise");
            double [] aremoteNoiseArray = this.remoteNoiseBuffer.array();
            double [] alocalNoiseArray = this.localNoiseBuffer.array();

            this.localNoiseArray   = new double[ 3*WINDOWING_SIZE_SAMPLE];
            this.remoteNoiseArray = new double[WINDOWING_SIZE_SAMPLE];

            Log.e(TAG, "Trunk the local noise and remote noise");
            int j =0;

            for (int i = WINDOWING_SIZE_SAMPLE * 2; i < WINDOWING_SIZE_SAMPLE * 3; i++) {
                this.isWindowing = true;
                this.remoteNoiseArray[j] = aremoteNoiseArray[i];
                j++;
            }
            j = 0;
            for (int i = WINDOWING_SIZE_SAMPLE; i < WINDOWING_SIZE_SAMPLE * 4; i++) {
                this.localNoiseArray[j] = alocalNoiseArray[i];
                j++;
            }
        }else{
            this.isWindowing = false;
            this.remoteNoiseArray = this.remoteNoiseBuffer.array();
            this.localNoiseArray = this.localNoiseBuffer.array();
        }

        xcorr(this.localNoiseArray, this.remoteNoiseArray);
    }



    /*.
     */
    /*      ----------------------------
     *       |         array a          |
     *       ----------------------------
     *       <-------------->
     *     lag = -10          -------------------------
     *                       |              array b  |
     *                       --------------------------
     *
     *
     *
     */

    /**
     *
     * return an array containing the cross correlation between two given sequence
     * @param a sequence
     * @param b sequence
     */
    public  void xcorr(double[] a, double[] b)
    {
        Log.e(TAG, "call xor with" + a.toString() + " and " + b.toString());
        int len = a.length;
        if(b.length > a.length)
            len = b.length;
        Log.e(TAG, "XCORR");
        xcorr(a, b, len-1 /*at least one element in common*/);
    }

     /**
     * Computes the cross correlation between sequences a and b.
     *
     * @param a sequence
     * @param b sequence
     * @param maxlag max lag
     */
    public  void xcorr(double[] a, double[] b, int maxlag)
    {

        Log.e(TAG, "XCORR");
        Log.e(TAG,"max lag:" + maxlag);
        this.cross_array  = new double[2*maxlag+1]; // array used to
        Arrays.fill(cross_array , 0); // fill the array cross_array  with O

        this.norm_array = new double[2*maxlag+1]; // array used to
        Arrays.fill(norm_array , 0); // fill the array norm_array  with O

        double variance_a = 1 ; //variance(a);
        double variance_b = 1 ; //variance(b);
        Log.e(TAG,"variance a:" + variance_a);
        Log.e(TAG,"var b:" + variance_b);

        // go through any lag
        for(int lag = b.length-1, idx = maxlag-b.length+1; lag > -a.length; lag--, idx++)
        {
            //   Log.e(TAG, "lag: " + lag);
            //           Log.e(TAG,"lag:" + lag + " idx:" + idx);

            if(idx < 0)
                continue;

            if(idx >= cross_array.length)
                break;

            // where do the two signals overlap?
            int start = 0;
            // we can't start past the left end of b
            if(lag < 0)
            {
                start = -lag;
                //  Log.d(TAG, "lag<0, start:" + start);
            }

            int end = a.length-1;
            // we can't go past the right end of b
            if(end > b.length-lag-1)
            {
                end = b.length-lag-1;
                //    Log.d(TAG,"end: "+end);
            }

            //   Log.d(TAG,"lag = " + lag +": "+ start+" to " + end+"   idx = "+idx);
            for(int n = start; n <= end; n++)
            {
                //Log.d(TAG,"  bi = " + (lag+n) + ", ai = " + n);
                cross_array[idx] += a[n]*b[lag+n];
                //         Log.d(TAG, "cross(" + idx +") += a("+ n+ ") + b(" + (lag+n) +")=" + a[n] + "*" + b[lag+n]);

                double mean_a = this.mean(a, start, end ); // a(start) -> a(end)
                double mean_b = this.mean(b, lag+start , lag+ end ); //b(lag+start) -> b(lag + end)
                //          Log.d(TAG, "mean " + mean_a + " mean" + mean_b + "\n");

                norm_array[idx] += (a[n] - mean_a) * (b[lag+n] - mean_b);

                if (n == end ){
                    double value =  a[n]*b[lag+n];
                    // Log.d(TAG, "lag" + lag + "    value=" +  value );
                    double normalised_value = 1/ (end - start+1) *  (a[n] - mean_a) *(b[lag+n] - mean_b);
                    //                  Log.d(TAG, "start:" + start + "end" + end + "lag" + lag);
                    //                 Log.d(TAG, "meanA(" + start +"->" + (end-start +1) + ") meanB(" + (lag +start) +"->" + ( lag+end-start +1) +")" );
                    // Log.d(TAG, "lag" + lag +"normvalue=" + normalised_value );
                }
            }
            norm_array[idx] = norm_array[idx] / ((end-start +1) * variance_a * variance_b   ); // cross variance
            //    Log.e(TAG, "lag" + lag +",  cross("+ idx + ")=" + String.valueOf(cross_array[idx]) +  " Ny("+ idx + ")=" + String.valueOf(norm_array [idx])+ "\n");
        }

        // get the offsets
        Log.e(TAG, "try to find the OFFSET");
        Log.e(TAG, "windoww size : " + WINDOWING_SIZE_SAMPLE);

        Log.e(TAG, "max offset  : " + masOffset);
        Log.e(TAG, "chunk s size : " + Configuration.chunksize_Sec);

        Log.e(TAG, "max Delay assumed:" + maxDelayassumed_NB_SAMPLE);
        Log.e(TAG, "start :" + ( b.length *3 - this.maxDelayassumed_NB_SAMPLE));

        // variables used to get the best offset, i.e., the one with a maximum correlation
        double max = Double.NEGATIVE_INFINITY;
        double norm_max = Double.NEGATIVE_INFINITY;

        int offset =0;
        int norm_offset =0;

        //  for (int i = 0 ; i < this.cross_array.length; i++ ){
/*  DO Not consider this case
            ----------------------------
    *       |         array a          |
    *       ----------------------------
    *<----->
    *lag=+b.lenght
    * ------
    *    b  |
    *-------
    *
    *
    * /*    consider this case
            ----------------------------
    *       |         array a          |
    *       ----------------------------
    * <------------->
    * lag=-3*b-decalmax
    *                ------
    *                | b
    *                -------
    *
    */

        //  for (int i = 0 ; i < this.cross_array.length; i++ ){
        for (int i = b.length *3 - this.maxDelayassumed_NB_SAMPLE ; i <  b.length *3 + this.maxDelayassumed_NB_SAMPLE ; i++ ){
            if(this.cross_array[i] > max ) {
                max = this.cross_array[i];
                offset = i;
            }
            if(this.norm_array[i] > norm_max ) {
                norm_max = this.norm_array[i];
                norm_offset = i;
            }
            Log.d(TAG, "array (" + i+ ")=" + this.cross_array[i] + " normarray("+i+")= " + this.norm_array[i] );
        }

        Log.e(TAG, "offset: "+ offset+ "=" + this.offset + " highest correlation: "+ max +  " norm offset: "+
                norm_offset+"=" + this.norm_offset + "highest norm correlation" + norm_max);

        // if i = 0 => we have an offet = - 3 *b : b is shifted tree time on the left
        // i.e., i = position - 3 * b
        // if i = 3 b then the offset is = 0
        this.offset = offset - b.length * 3;
        this.norm_offset = norm_offset - b.length * 3;

//        this.offset = offset - maxlag - 1;
        //      this.norm_offset = norm_offset - maxlag - 1;
        Log.e(TAG, "offset: "+ offset+ "=" + this.offset + " highest correlation: "+ max +  " norm offset: "+
                norm_offset+"=" + this.norm_offset + "highest norm correlation" + norm_max);

        if (isWindowing == true) {
            Log.e(TAG, "we are windowing");
            //  |     a                                                  |
            //   <- WINDOWS SIZE  -><- WINDOWS SIZE  -><- WINDOWS SIZE  ->
            //                      | b                |
            //
            //  this.offset +=   WINDOWING_SIZE;
            //  this.norm_offset +=  WINDOWING_SIZE;
        }
        Log.e(TAG, "offset"+ offset+ "=" + this.offset + "highest correlation"+ max +  "norm offset"+ norm_offset+"=" + this.norm_offset + "highest norm correlation" + norm_max);
//    offset = i - max (a.length, b.lenght);
    }

    /**
     * return the variance
     * @param a sequence
     * @return variance
     */
    public double variance(double[] a){
        // variance = A/ N sum (xi-mean
        double variance = 0;
        double mean = this.mean(a);
        // variance = 1 / a.length ;
        for (int i = 0 ; i< a.length ;  i++){

            variance += a[i] - mean ;
            //  Log.e(TAG,"a[" + i + ")=" + a[i]  +"- mean" + mean + " var:" + variance);
        }
        variance = variance / (double ) a.length;
        //    Log.e(TAG, "variance total: " + variance);
        return variance;
    }

    /**
     * returns the mean
     * @param a sequence of measurements
     * @return mean
     */
    public  double mean(double[] a)
    {
        return sum(a)/a.length;
    }

    /**
     * return the mean of a sequence
     * @param a sequence
     * @param start start of the sequence to consider
     * @param end end of the sequence to consider
     * @return mean
     */
    public  double mean(double[] a, int start, int end)
    {
        double sum = 0;

        for (int i = start ; i<= end ; i++)
            sum += a[i];
//        Log.d(TAG, "sum (" + start +"->" + end + ")/" + "(" + end +"-" + start + ")=" + sum +"/" + (end-start+1)  + "=" + (sum / (double) (end - start +1)));
        sum = sum / (double) (end - start +1);
        return sum;
    }




}