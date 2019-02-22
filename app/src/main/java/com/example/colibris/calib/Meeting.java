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

/**
 * This Meeting class store all the parameters describing a calibration  that  takes place
 * when various devices meet
 */
public class Meeting {

    /**
     * intercept of the regression  that takes place during the meeting
     */
    public double intercept ;
    /**
     * slope of the regression that takes place during the meeting
     */
    public double slope ;
    /**
     * standard error of the regression that takes place during the meeting
     */
    public double standard_error;
    /**
     * mean squarred error of the regression that takes place during the meeting
     */
    public double means_square_error;

    //meeting characteristic
    /**
     * meeting duration
     */
    public double meetingDuration ;

    /**
     * R squared of the regression that takes place during the meeting
     */
    public double Rsquared ; // determination coeff
    /**
     * Metting start time
     */
    public double meetingStartTime ;
    /**
     * sum of the squared residual that takes place during the meeting
     */
    public double ResidualSumOfSquares;

    /**
     * Initialise the meeting
     * @param regressIntercept regression intercept
     * @param regressSlope regression slope
     * @param regressStandard_error regression standard error
     * @param regressMeans_square_error mean squared error of the regression that takes place during the meeting
     * @param aMeetingDuration meeting duration
     * @param _Rsquared r squared of the regression that takes place during the meeting
     * @param _meetingStartTime meeting start time
     * @param _ResidualSumOfSquares  sum of the squared residuals that takes place during the meeting
     */
    public Meeting(  double regressIntercept, double regressSlope,
                     double regressStandard_error, double regressMeans_square_error, double aMeetingDuration
            , double _Rsquared, double _meetingStartTime, double _ResidualSumOfSquares){

        this.intercept = regressIntercept;
        this.slope = regressSlope;
        this.standard_error = regressStandard_error;
        this.means_square_error = regressMeans_square_error;
        this.meetingDuration = aMeetingDuration ;

        this.Rsquared = _Rsquared;
        this.meetingStartTime = _meetingStartTime;
        this.ResidualSumOfSquares = _ResidualSumOfSquares;
    }

    /**
     * returns a string with the meeting parameters
     * @return string
     */
    public String toString (){
        StringBuilder s = new StringBuilder();

        s.append(this.intercept+" ");
        s.append(this.slope+" ");
        s.append(this.standard_error+" ");
        s.append(this.means_square_error+" ");
        s.append(this.meetingDuration+" ");
        s.append(this.Rsquared + " ");
        s.append(this.meetingStartTime + " ");
        s.append(this.ResidualSumOfSquares + " ");
        return s.toString();
    }
}
