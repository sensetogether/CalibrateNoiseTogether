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

import android.content.Context;
import android.os.Build;
import android.util.Log;


import com.example.colibris.calib.FileManager;
import com.example.colibris.multi.Calibration;
import com.example.colibris.multi.hypergraph.HyperConnection;
import com.example.colibris.multi.hypergraph.ShortestHyperPath;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Date;


/**
 * The Me class encompasses the information (mostly configuration information)
 * concerning the device itself. Such information include the connexion graph,
 * the calibration parameters (that are herein established)
 */

public class Me  {
    /**
     * log related information
     */
    private static final String Tag = "Me"; // for debugging purpose
    /**
     * local device
     */
    public Device localDevice; //who am i
    /**
     * connexion graph that is stored on the device
     */
    public HyperConnection localconnectionGraph ; // connexion graph stored
    /**
     * is there a need to extract again the connection graph from the local file
     */
    public boolean isConnectionGraphExtracted = false;//
    private Context ctx; // for displaying purpose

    /**
     * Create Locale device
     * @param ctx context
     */
    public Me ( Context ctx) {
        this.ctx = ctx;
        //extract the local id
        this.localDevice = new Device(getVertexId(), getDeviceName(), 0);
    }


    /**
     * Extract the connection graph from the local file where it is stored.
     * If there is not file, then create an empty connexion graph
     */
    public void setConnectionHyperGraph(){
        //create an empty hypergraph
        this.localconnectionGraph = new HyperConnection();
        FileManager filem = new FileManager(Configuration.connectFileName, true, ctx);

        int fileSize =0;

        if (filem != null){
         fileSize = filem.getFileSize();
        filem.close();
        }

        if(fileSize == 0){
            // create a new connection graph and save it
            Log.e(Tag, "create a new connection hyper graph ");
            // set in the connection graph if i am calibrated or not
            if(Configuration.IS_CALIBRATED == true){
                Log.e(Tag, "set local node as calibrated");
                this.localconnectionGraph.setCalibrated(this.localDevice);
            }

            this.localconnectionGraph.toFile(ctx);
            Log.e(Tag, "connection graph is saved");
        } else {//connection graph is already existing
            Log.e(Tag, "extract the connection graph ");
            // the file containing the connection graph is not empty -> extract from it the connection graph

            //if cannot extract from file
            if (this.localconnectionGraph.getFromFile(ctx) == false){
            }
        }
        this.isConnectionGraphExtracted = true; // we have extracted the connection graph
    }

    public HyperConnection getLocalconnectionGraph(){
        if(this.isConnectionGraphExtracted == false)
            setConnectionHyperGraph();
        return this.localconnectionGraph;
    }


    /**
     * return the time
     * @return time
     */
    public long getLocalTimeShifterALongTimeAgo(){
        Date now = new Date();
        Date very_long_time_ago = new Date(now.getTime() -Configuration.VERY_LONG_TIME_AGO);
        return very_long_time_ago.getTime();
    }







    /*this function gets the shortest

    /**
     * extract the shorted hyperpath from the given source to the
     * consolidated node.
     * if the local device is calibrated then it return itself
    // if there is no device to calibrate, then the size of the list is 0
     * @param src source of the shortest hyperpaht
     * @return shortest hyperpath
     */
    public HyperConnection getShortestPath( int src) {
        // extract the hypergraph from the file where it is stored
        HyperConnection hyperConnection = new HyperConnection();
        hyperConnection.getFromFile(ctx);
        //compute shortest path from the local device
        ShortestHyperPath shortestHyperPath = new ShortestHyperPath();
        Log.e(Tag, "Look for the shortest hyperpath from " + src);

        HyperConnection bestHyperConnexion = shortestHyperPath.getShortestHypergraph(localDevice.getVertexId(), hyperConnection );

        Log.e(Tag, "\n Best Shortest path: \n");
        Log.e(Tag,         bestHyperConnexion.toString());
        return bestHyperConnexion;


    }

    /**
     * return the multi hop calibration parameters for the given node
     * @param Vertex_id vertex id of the node that is considered
     * @return calibration parameters
     */
    public Calibration getMultiHopCalibration(int Vertex_id) {
        Log.e(Tag, "get multi hop calibration");
        // extract the hypergraph from the file where it is stored
        HyperConnection hyperConnection = new HyperConnection();
        hyperConnection.getFromFile(ctx);
        //compute shortest path from the local device
        ShortestHyperPath shortestHyperPath = new ShortestHyperPath();
        Log.e(Tag, "Look for the shortest hyperpath from " + localDevice.getVertexId());

        HyperConnection bestHyperConnexion = shortestHyperPath.getShortestHypergraph(localDevice.getVertexId(), hyperConnection );
        Log.e(Tag, "\n In get multi hop Best Shortest path: \n "+ bestHyperConnexion.toString());
        Log.e(Tag, "start computing the best multi hops calibration");
        Calibration calibration = shortestHyperPath.multi_hop_calibrate( this.localDevice.getVertexId(),bestHyperConnexion);
        Log.e(Tag, "FLORENCE MULTI HOPS" +  calibration.toString() );
        return calibration;
    }


    /**
     * return the multi hop calibration parameters for the local device
      * @return calibration parameters
     */
    public Calibration getMultiHopCalibration() {
        return getMultiHopCalibration(localDevice.getVertexId());
    }


    /**
     * provide the calibration parameters back to last best regression
     * @return  calibration parameters back to last best regression
     */
    public Calibration getSingleHopCalibration() {
        // extract the hypergraph from the file where it is stored
        HyperConnection hyperConnection = new HyperConnection();
        hyperConnection.getFromFile(ctx);
        //compute shortest path from the local device
        ShortestHyperPath shortestHyperPath = new ShortestHyperPath();
        Log.e(Tag, "Look for the shortest hyperpath from " + localDevice.getVertexId());

        HyperConnection bestHyperConnexion = shortestHyperPath.getShortestHypergraph(localDevice.getVertexId(),
                hyperConnection );
        Log.e(Tag, "\n Best Shortest path: \n "+ bestHyperConnexion.toString());
        Calibration calibration = shortestHyperPath.single_hop_calibrate( this.localDevice.getVertexId(),bestHyperConnexion);
        Log.e(Tag, calibration.toString() );
        return calibration;
    }


    /**
     * returns the device name
     * @return device name
     */
    public static String getDeviceName(){
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "";
        }
    }


    /**
     * return the vertex id which is generated using the device name
     * @return vertex id
     */
    public static int getVertexId(){
        String deviceHash  = md5(getDeviceName());
        String deviceHash16 =  deviceHash.substring(0, 4);
        return Integer.parseInt(deviceHash16,16) % Configuration.VERTEXNB ;
    }

    /**
     * return the md5 of the given string
     * @param toEncrypt string
     * @return md5 of the provided string
     */
    private static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return "";
        }
    }





}



