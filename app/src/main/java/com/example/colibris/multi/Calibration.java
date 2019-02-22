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
package com.example.colibris.multi;

/**
 * The calibration class includes all the parameters characterising a calibration i.e., regression
 */
public class Calibration {
    /**
     * calibration intercept
     */
    public double intercept =0;
    /**
     * calibration slope
     */
    public double slope =0;
    /**
     * weighted calibration error
     */
    public double weighted_cumulated_error =0;
    /**
     * cumulated error
     */
    public double cumulated_errror=0;

    /**
     *
     * @param slope slope of the regression
     * @param intercept intercept of the regression
     * @param cumulated_errror (multi-hops) errors
     * @param weighted_cumulated_error weighted (multi-hops) error
     */
    public Calibration(double slope, double intercept, double cumulated_errror, double weighted_cumulated_error)
    {this.intercept = intercept ; this.slope = slope; this.cumulated_errror = cumulated_errror; this.weighted_cumulated_error = weighted_cumulated_error;
    }

    /**
     * return if a node is calibrated relying on the calibration parameters
     * an uncalibrated node is characterised by a 0 intercept, a Slope =1,, error =0
     * @return if node is calibrated (i.e., if the intercept is not 0, if the slope is not 1 and if the cumultated
     */
    public boolean iscalibred (){
        if (intercept == 0 && slope == 1 && cumulated_errror ==0)
            return false;
        return true;

    }


    /**
     * return a string with the calibration parameters
     * @return string displaying the calibration parameters
     */
     public String toString(){
        return new String("calibration slope " + this.slope + " intercept: " + this.intercept +
                " cumulated error: " + cumulated_errror +  " weighted error: " + this.weighted_cumulated_error + " is calibrated " + iscalibred() );
    }

}
