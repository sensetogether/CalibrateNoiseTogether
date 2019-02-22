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
package com.example.colibris.configuration;


import com.example.colibris.multi.Calibration;

/**
 * The device class contain the information related to a device, such
 * as the device id, device name,
 */

public class Device {
    /**
     * vertex id
     */
    public  int vertexId;
    /**
     * device name
     */
    public  String name ;
    /**
     * device id
     */
    public int deviceId;
    /**
     * calibration parameters of the device
     */
    public Calibration calibration;

    /**
     * Set the device parameters
     * @param avertexId vertex id
     * @param aname device name
     * @param adeviceid device if
     */
    public Device(int avertexId, String aname, int adeviceid)
    {
        this.name = aname;
        this.vertexId = avertexId;
        this.deviceId = adeviceid;
    }

    /**
     * Set the device parameters
     * @param avertexId vertex id
     * @param aname device name
     * @param adeviceid device id
     * @param acalibration calibration parameters
     */
    public Device(int avertexId, String aname, int adeviceid, Calibration acalibration)
    {
        this.name = aname;
        this.vertexId = avertexId;
        this.deviceId = adeviceid;
        this.calibration = acalibration;
    }


    /**
     * rerturn the vextex id
     * @return vertex id
     */
    public int getVertexId(){ return this.vertexId;
    }

    /**
     * set the vertex id
     * @param avertexId vertex id
     */
    public void setVertexId(int avertexId){
        this.vertexId = avertexId;
    }

    /**
     * return the vertex id
     * @return vertex id
     */
    public String toString(){
        StringBuilder s = new StringBuilder();//string to return
        //print the vertex list
        s.append(vertexId + " " +vertexId+ " \n");
        return s.toString();
    }
    //compare the deviceID

    /**
     * compare two devices based on their vertex id
     * @param device2compare device to compare with
     * @return if the two device have the same vertex id, return true, else returns falses
     */
    public boolean hasSameDeviceId(Device device2compare){
        return this.vertexId == device2compare.vertexId;
    }



}
