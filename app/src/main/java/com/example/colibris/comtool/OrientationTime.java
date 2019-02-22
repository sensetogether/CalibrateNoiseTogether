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
package com.example.colibris.comtool;
import java.io.Serializable;

/**
 * This class provides orientation and time related parameters
 */

public class OrientationTime implements Serializable {

    //location related infrmation
    /**
     * orientation with regards to X
     */
    public float orientationAxisX;
    /**
     * orientation with regards to Y
     */
    public float orientationAxisY;
    /**
     * orientation with regards to Z
     */
    public float orientationAxisZ;

    // time related information
    public long orientationOffset = (long) Double.POSITIVE_INFINITY;
    public long networkOffset = (long) Double.POSITIVE_INFINITY;

    public boolean isOrientationSynchronised = false;
    //public boolean isNetworkSynchronised = false;

    public long orientationTime = (long) Double.POSITIVE_INFINITY;
    public long localOrientationTime = (long) Double.POSITIVE_INFINITY;
    public long NetworkTime = (long) Double.POSITIVE_INFINITY;
    public long LocalNetworkTime = (long) Double.POSITIVE_INFINITY;

}

