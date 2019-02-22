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
package com.example.colibris.multi.hypergraph;

import android.content.Context;
import android.util.Log;


import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.Meeting;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.multi.graph.StdRandom;
import com.example.colibris.multi.graph.DirectedEdge;
import com.example.colibris.multi.graph.EdgeWeightedDigraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 *  The {@code HyperConnection} class represents a edge-weighted
 *  hypergraph composed of  vertices named 0 through <em>V</em> -  where each
 *  directed hypereedge is of type {@link HyperEdge} and is composed of a set of
 *  inner-edges.
 */


public class HyperConnection {
    /**
     * log information
     */
    public static final String TAG = "HyperConnection";
    /**
     * graph representing the meetings
     */
    public EdgeWeightedDigraph graph ;//
    private List<Boolean> calibratedNodesList; //nodes that are already calibrated
    /**
     * is used when generating a random graph
     */
    private  List<Integer> leavesList = null;
    /**
     * ramining edges to add when generating a random hypergraph
     */
    private int remainingHyperEdge2add =0;
    /**
     * stop generating the hypergraph
     */
    private boolean stop = false;
    /*create an empty hypergraph containing configuration.vertexNB nodes without any hyperlink
     * create also an empty list of calibrated nodes
     * input: metric/criteria used to determine the shortest path
     * */
    public HyperConnection() {
        //create an empty graph
        this.graph = new EdgeWeightedDigraph(Configuration.HYPERGRAPH_SIZE);
        calibratedNodesList = new ArrayList<Boolean>();
        // add the devices that are not calibrated
        for (int i =0 ; i< Configuration.VERTEXNB ; i++)
            calibratedNodesList.add(false);


        //add the references nodes and the single consolidated node (i.e. r)
        // set that they are calibrated as well as the consolidated node
        for (int i =Configuration.VERTEXNB ; i< Configuration.HYPERGRAPH_SIZE ; i++)
            calibratedNodesList.add(true);
    }

    /**
     * give the id of the consolidated node (in pratice the last one)
     * @return consolidated node
     */
    public int getConsolidatedNode(){
        return Configuration.HYPERGRAPH_SIZE -1;
    }

    /**
     * Connect the reference node to the consolidated node
     */
    private void connectReferenceNodesToConsolidatedNode(){
        //add a hyperedge between any reference node and the consolidated node
        //go through any reference node
        for (int j =Configuration.VERTEXNB ; j< Configuration.HYPERGRAPH_SIZE -1 ; j++){
            //create an hyper edge

            //populate this hyperedge with one inner edge of 0 weight
            double std_err = 0 ;
            double mean_sqr = 0 ;
            double R_squarred = 1  ;
            double residu_Sum_squarre  = 0;
            // assign a weight to the inner edge
            //slope is 1 and intercept is 0
            Meeting ameeting = new Meeting(/*intercept*/ 0 , /*slope*/ 1,
                    std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 0,   residu_Sum_squarre);

            DirectedEdge edge = new DirectedEdge(j,Configuration.HYPERGRAPH_SIZE-1, /*weight*/ 0,ameeting );
            Log.e(TAG,"Adding an edge "+edge.toString());

            Log.e(TAG, "Edge with caracteristic" + ameeting.toString());
            Log.e(TAG, "Edge with weight" + edge.weight() );

            this.graph.addEdge(edge);
        }
    }

    /*
     * create a hypergraph containing
     * nbVertexs vertexs
     * with nbLink links that are randomly assigned
     * create also a set of calibrated nodes (all set to uncalibrated
     * @param nbVertex number of vertex in the hypergraph
     * @param nbHyperLink number of hyperlinks in the hypergraph
     * @param max_meeting_Size maximum meeting size in the hypergraph (i.e., max nb of devices the device to calibrate in meeting with)
     */
    public void almost_connectSource2ConsolidateNode(int src ){

        connectReferenceNodesToConsolidatedNode();

        //connect with one hyperlink src to src+1, src+2, src+3
        double std_err = 0.1;
        double mean_sqr = 0.1;
        double R_squarred = 0.1;
        double residu_Sum_squarre  = 0.1;
        // assign a weight to the inner edge
        double weight=0;
        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        Meeting ameeting = new Meeting(/*intercept*/ 0.1 , /*slope*/ 0.1,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 1,   residu_Sum_squarre);

        DirectedEdge e = new DirectedEdge(src, src+1, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src, src+2, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src, src+3, weight,ameeting);
        this.graph.addEdge(e);


        // connect with one hyperlink src+1 to src+4, src+5
        std_err = 0.2;
        mean_sqr = 0.2;
        R_squarred = 0.2;
        residu_Sum_squarre  = 0.2;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.2, /*slope*/ 0.2,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 2,   residu_Sum_squarre);

        e = new DirectedEdge(src+1, src+4, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+1, src+5, weight,ameeting);
        this.graph.addEdge(e);


        // connect with one hyperlink src+2 to src+6
        std_err = 0.3;
        mean_sqr = 0.3;
        R_squarred = 0.3;
        residu_Sum_squarre  = 0.3;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.3, /*slope*/ 0.3,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 3,   residu_Sum_squarre);

        e = new DirectedEdge(src+2, src+6, weight,ameeting);
        this.graph.addEdge(e);

        // connect with one hyperlink src+3 to src+6, src+7
        std_err = 0.4;
        mean_sqr = 0.4;
        R_squarred = 0.4;
        residu_Sum_squarre  = 0.4;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.4, /*slope*/ 0.4,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 4,   residu_Sum_squarre);

        e = new DirectedEdge(src+3, src+6, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+3, src+7, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+4 to R+0
        std_err = 0.5;
        mean_sqr = 0.5;
        R_squarred = 0.5;
        residu_Sum_squarre  = 0.5;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.5, /*slope*/ 0.5,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 5,   residu_Sum_squarre);

        e = new DirectedEdge(src+4, Configuration.VERTEXNB, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+5 to R+0
        std_err = 0.6;
        mean_sqr = 0.6;
        R_squarred = 0.6;
        residu_Sum_squarre  = 0.6;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.6, /*slope*/0.6,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 6,   residu_Sum_squarre);

        e = new DirectedEdge(src+5, Configuration.VERTEXNB, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+6 to R+0 and R+1
        std_err = 0.7;
        mean_sqr = 0.7;
        R_squarred = 0.7;
        residu_Sum_squarre  = 0.7;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case    Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.7, /*slope*/ 0.7,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 7,   residu_Sum_squarre);

        e = new DirectedEdge(src+6, Configuration.VERTEXNB , weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+6, Configuration.VERTEXNB+1, weight,ameeting);
        this.graph.addEdge(e);
        //connect with one hyperlink src+7 to R+2
        std_err = 0.8;
        mean_sqr = 0.8;
        R_squarred = 0.8;
        residu_Sum_squarre  = 0.8;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.8, /*slope*/ 0.8,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 8,   residu_Sum_squarre);

        e = new DirectedEdge(src+7, Configuration.VERTEXNB+1, weight,ameeting);
        //this.graph.addEdge(e);

    }



    /*
     * create a hypergraph containing
     * nbVertexs vertexs
     * with nbLink links that are randomly assigned
     * create also a set of calibrated nodes (all set to uncalibrated
     * @param nbVertex number of vertex in the hypergraph
     * @param nbHyperLink number of hyperlinks in the hypergraph
     * @param max_meeting_Size maximum meeting size in the hypergraph (i.e., max nb of devices the device to calibrate in meeting with)
     */
    public void connectSource2ConsolidateNode(int src ){

        connectReferenceNodesToConsolidatedNode();


        Meeting fmeeting = new Meeting(/*intercept*/ 0.01 , /*slope*/ 0.01,
                0.01    /*std_err*/  ,  0.01 /*mean_sqr*/ , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ 0.01 , /*time*/ 0, 0.01);

        DirectedEdge e = new DirectedEdge(src, src-1, 0.01,fmeeting);
        this.graph.addEdge(e);

        e = new DirectedEdge(src-1, Configuration.VERTEXNB , 0.01,fmeeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src to src+1, src+2, src+3
        double std_err = 0.1;
        double mean_sqr = 0.1;
        double R_squarred = 0.1;
        double residu_Sum_squarre  = 0.1;
        // assign a weight to the inner edge
        double weight=0;
        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }

        Meeting ameeting = new Meeting(/*intercept*/ 0.1 , /*slope*/ 0.1,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 1,   residu_Sum_squarre);

        e = new DirectedEdge(src, src+1, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src, src+2, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src, src+3, weight,ameeting);
        this.graph.addEdge(e);


        // connect with one hyperlink src+1 to src+4, src+5
        std_err = 0.2;
        mean_sqr = 0.2;
        R_squarred = 0.2;
        residu_Sum_squarre  = 0.2;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.2, /*slope*/ 0.2,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 2,   residu_Sum_squarre);

        e = new DirectedEdge(src+1, src+4, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+1, src+5, weight,ameeting);
        this.graph.addEdge(e);


        // connect with one hyperlink src+2 to src+6
        std_err = 0.3;
        mean_sqr = 0.3;
        R_squarred = 0.3;
        residu_Sum_squarre  = 0.3;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.3, /*slope*/ 0.3,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 3,   residu_Sum_squarre);

        e = new DirectedEdge(src+2, src+6, weight,ameeting);
        this.graph.addEdge(e);

        // connect with one hyperlink src+3 to src+6, src+7
        std_err = 0.4;
        mean_sqr = 0.4;
        R_squarred = 0.4;
        residu_Sum_squarre  = 0.4;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.4, /*slope*/ 0.4,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 4,   residu_Sum_squarre);

        e = new DirectedEdge(src+3, src+6, weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+3, src+7, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+4 to R+0
        std_err = 0.5;
        mean_sqr = 0.5;
        R_squarred = 0.5;
        residu_Sum_squarre  = 0.5;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.5, /*slope*/ 0.5,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred ,
                /*time*/ 5,   residu_Sum_squarre);

        e = new DirectedEdge(src+4, Configuration.VERTEXNB, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+5 to R+0
        std_err = 0.6;
        mean_sqr = 0.6;
        R_squarred = 0.6;
        residu_Sum_squarre  = 0.6;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.6, /*slope*/0.6,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 6,   residu_Sum_squarre);

        e = new DirectedEdge(src+5, Configuration.VERTEXNB, weight,ameeting);
        this.graph.addEdge(e);

        //connect with one hyperlink src+6 to R+0 and R+1
        std_err = 0.7;
        mean_sqr = 0.7;
        R_squarred = 0.7;
        residu_Sum_squarre  = 0.7;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.7, /*slope*/ 0.7,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 7,   residu_Sum_squarre);

        e = new DirectedEdge(src+6, Configuration.VERTEXNB , weight,ameeting);
        this.graph.addEdge(e);
        e = new DirectedEdge(src+6, Configuration.VERTEXNB+1, weight,ameeting);
        this.graph.addEdge(e);
        //connect with one hyperlink src+7 to R+2
        std_err = 0.8;
        mean_sqr = 0.8;
        R_squarred = 0.8;
        residu_Sum_squarre  = 0.8;
        // assign a weight to the inner edge

        switch (Configuration.shortest_path_criteria){
            case Configuration.STD_ERROR_CRITERIA:
                weight = std_err;
            case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                weight = mean_sqr;
            case Configuration.MEETING_DURATION_CRITERIA :
                weight = Configuration.recordDuration_ms;
            case Configuration.MEETING_RSQUARED :
                weight = R_squarred ;
            case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                weight = residu_Sum_squarre;
        }
        ameeting = new Meeting(/*intercept*/ 0.8, /*slope*/ 0.8,
                std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ 8,   residu_Sum_squarre);

        e = new DirectedEdge(src+7, Configuration.VERTEXNB+1, weight,ameeting);
        this.graph.addEdge(e);

    }



    /* create a hypergraph containing
     * with nbLink links that are randomly assigned
     * create also a set of calibrated nodes (all set to uncalibrated
     * @param nbVertex number of vertex in the hypergraph
     * @param nbHyperLink number of hyperlinks in the hypergraph
     * @param max_meeting_Size maximum meeting size in the hypergraph (i.e., max nb of devices the device to calibrate in meeting with)
     */
    // node id is assigned consecutively (0 1 2 3 ... nbVertex-1)
    public void populateWithRandomConnectedHyperEdges(int src,  int nbHyperLink , int max_meeting_Size){

        //populate the hypergraph with the hyperedges between devices
        for ( int i = 0; i < nbHyperLink; i++) {
            int source = StdRandom.uniform(Configuration.VERTEXNB); // source
            Log.d(TAG, "creation of hyperlink  E " + i);
            // meeting size = random, non zero number <= max_meeting_Size
            int actual_meeting_size = 0;
            while (actual_meeting_size ==0)
                actual_meeting_size = StdRandom.uniform(max_meeting_Size+1)  ;

            Log.d(TAG, "meeting size: " + actual_meeting_size + "for E" +i);
            // generate a link for each inner-edge
            for (int j =1 ; j<= actual_meeting_size ; j++ ){
                //destination no equal to source
                int dest = source;
                while (dest == source)
                    dest= StdRandom.uniform(Configuration.HYPERGRAPH_SIZE ); // dest
                //todo (RE) define the structure of a meeting to have the propoerty of a inner link that is represented
                double std_err = StdRandom.uniform(100);
                double mean_sqr = StdRandom.uniform(100);
                double R_squarred = (double) StdRandom.uniform(100) /100 ;
                double residu_Sum_squarre  = StdRandom.uniform(100);
                // assign a weight to the inner edge

                double weight=0;
                switch (Configuration.shortest_path_criteria){
                    case Configuration.STD_ERROR_CRITERIA:
                        weight = std_err;
                    case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                        weight = mean_sqr;
                    case Configuration.MEETING_DURATION_CRITERIA :
                        weight = Configuration.recordDuration_ms;
                    case Configuration.MEETING_RSQUARED :
                        weight = R_squarred ;
                    case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                        weight = residu_Sum_squarre;
                }

                Meeting ameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ StdRandom.uniform(100),
                        std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ i,   residu_Sum_squarre);

                DirectedEdge e = new DirectedEdge(source, dest, weight,ameeting);
                this.graph.addEdge(e);
            } // end adding this hyperedge
        }

    }








    /* create a hypergraph containing
     * with nbLink links that are randomly assigned
     * create also a set of calibrated nodes (all set to uncalibrated
     * @param nbVertex number of vertex in the hypergraph
     * @param nbHyperLink number of hyperlinks in the hypergraph
     * @param max_meeting_Size maximum meeting size in the hypergraph (i.e., max nb of devices the device to calibrate in meeting with)
     */
    // node id is assigned consecutively (0 1 2 3 ... nbVertex-1)
    public void populateWithRandomHyperEdges(  int nbHyperLink , int max_meeting_Size){
        // connectReferenceNodesToConsolidatedNode();
//populate the hypergraph with the hyperedges between devices
        for ( int i = 0; i < nbHyperLink; i++) {
            int source = StdRandom.uniform(Configuration.VERTEXNB); // source

            // meeting size = random, non zero number <= max_meeting_Size
            int actual_meeting_size = 0;
            while (actual_meeting_size ==0)
                actual_meeting_size = StdRandom.uniform(max_meeting_Size+1)  ;

            Log.d(TAG, "** creation of hyperlink  E " + i +" with " +actual_meeting_size + " inner edges");
            // generate a link for each inner-edge

            List<DirectedEdge> hyperedge_list = new ArrayList<DirectedEdge>();
            for (int j =1 ; j<= actual_meeting_size ; j++ ){
                //destination no equal to source
                int dest = source;
                while (dest == source)
                    dest= StdRandom.uniform(Configuration.HYPERGRAPH_SIZE -1); // dest
                //todo (RE) define the structure of a meeting to have the propoerty of a inner link that is represented
                double std_err = StdRandom.uniform(100);
                double mean_sqr = StdRandom.uniform(100);
                double R_squarred = (double) StdRandom.uniform(100) /100 ;
                double residu_Sum_squarre  = StdRandom.uniform(100);
                // assign a weight to the inner edge

                double weight=0;
                switch (Configuration.shortest_path_criteria){
                    case Configuration.STD_ERROR_CRITERIA:
                        weight = std_err;
                    case    Configuration.MEAN_SQUARE_ERROR_CRITERIA :
                        weight = mean_sqr;
                    case Configuration.MEETING_DURATION_CRITERIA :
                        weight = Configuration.recordDuration_ms;
                    case Configuration.MEETING_RSQUARED :
                        weight = R_squarred ;
                    case     Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE:
                        weight = residu_Sum_squarre;
                }

                Meeting ameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ StdRandom.uniform(100),
                        std_err  ,  mean_sqr , /*meeting duration */ Configuration.recordDuration_ms,/* R²*/ R_squarred , /*time*/ StdRandom.uniform(1000000000),   residu_Sum_squarre);

                DirectedEdge e = new DirectedEdge(source, dest, weight,ameeting);
                Log.d(TAG, "   add inner edges " + j + ": " + e.toString() );

                hyperedge_list.add(e);
//                this.graph.addEdge(e);
            } // end adding this edge

            Log.d (TAG, "update the hypergraph adding " + hyperedge_list.toString());
            HyperEdge hyper = new HyperEdge(hyperedge_list);
            this.addHyperEdge(hyper);
        }
        Log.e(TAG, " end populating the graph with random edges");
    }

    //function that  merges 2 connection hypergraphs
    public void add(HyperConnection remote_connection){
        //go through the 2 connection graphs
        Log.e(TAG,"Merge the connexion graphs with " + "\n" + remote_connection.toString());

        // investigate all the vertex to determine if there is a new node that is calibrated
        for (int v = 0; v < Configuration.HYPERGRAPH_SIZE ; v++) {

            // if the node is calibrated , then set it in the local connexion file also
            if(remote_connection.calibratedNodesList.get(v) == true){
                Device n = new Device(v, "",0);
                this.setCalibrated(n);
            }
        }

        //investigate all the vertex contained in the  graph to insert
        for (int v = 0; v <Configuration.HYPERGRAPH_SIZE ; v++) {
            // create a list called adj2add which is easier to handle
            List<DirectedEdge>  adj2add = new ArrayList<DirectedEdge>();
            for (DirectedEdge e : remote_connection.graph.adj[v]) {
                adj2add.add(e);
            }
            while (adj2add.size()>0){
                //extract the first inneredge
                DirectedEdge inneredge = adj2add.get(0);
                // extract from the remote connection the hyperedge corresponding to this inneredge
                HyperEdge remotehyperedge = new HyperEdge(remote_connection.getHyperEdge(inneredge.from(),  inneredge.caracteristic().meetingStartTime));
                adj2add = this.removeHyperEdgefromList(inneredge.from(), inneredge.caracteristic().meetingStartTime, adj2add);


                // extract the hyperedge, if any, from the local hypergraph with wich we are merging
                // if an hyperedge is already present then update it with the minimal weight
                List<DirectedEdge>  local_inner_edges = this.getHyperEdge(inneredge.from(), inneredge.caracteristic().meetingStartTime);

                if (local_inner_edges.size() !=0){// the remote hyperedge is already in the local connection
                    HyperEdge local_hyperedge =  new HyperEdge(local_inner_edges);
                    if (local_hyperedge.weight()< remotehyperedge.weight()){ // update the local hyperedge that is too costly
                        for (int i=0 ; i< remotehyperedge.getInnerEdges().size(); i++){ // update all the constituing inner edges
                            this.addInnerEdge(remotehyperedge.getInnerEdges().get(i));
                        }
                    }
                }else{ // this hyperedge has to be added to the local connection graph
                    // and to be removed from the connection graph to merge
                    for (int i=0 ; i< remotehyperedge.getInnerEdges().size(); i++){ // update all the constituing inner edges
                        this.addInnerEdge(remotehyperedge.getInnerEdges().get(i));
                    }
                }
            }
        }
    }

    private List<DirectedEdge> removeHyperEdgefromList(int src /*queue of the hyperedge to remove*/ , double id /*id of the hyperedge 2 remove*/,
                                                       List<DirectedEdge> HyperEdgefromList  ){
        List<DirectedEdge> list2return  =new ArrayList<DirectedEdge>();

        for (int i = 0 ; i< HyperEdgefromList.size() ; i++){
            if (HyperEdgefromList.get(i).from() == src &&HyperEdgefromList.get(i).caracteristic().meetingStartTime == id ){
                // nothing to do
            }else{
                list2return.add(HyperEdgefromList.get(i));
            }
        }
        return list2return;
    }



    public List<DirectedEdge> getHyperEdge(int src /*queue*/ , double id ){
        List<DirectedEdge> hyperEdge2return  = new ArrayList<DirectedEdge>();
        // find the src in the labeled graph
        for (DirectedEdge e : this.graph.adj[src]){
            // try to find the inner edge composing the required hyperedge (the one with the same id
            if (e.caracteristic().meetingStartTime == id ){
                //add the inneredge
                hyperEdge2return.add(e);
            }
        }
        return hyperEdge2return;
    }







     /**
     * Set that the given node is calibrated
     * @param node that need to be calibrated
     */
    public void setCalibrated(Device node){
        this.calibratedNodesList.set(node.vertexId, true);
    }


    /**
     * Set that the given node is not calibrated
     * @param node that need to be set as uncalibrated
     */
    public void unsetCalibrated(Device node){

        Log.e(TAG, "unset node" + node.getVertexId());
        this.calibratedNodesList.set(node.vertexId, false);
    }


    /**
     * Display some information related to the hypergraph
     * @return information related to the hypergraph
     */
    public String toString(){
        StringBuilder s = new StringBuilder();//string to return
        s.append("\nContact graph:\n");
        s.append(this.graph.toString());
        s.append("\nCalibrated nodes:");
        for (int i =0; i< Configuration.HYPERGRAPH_SIZE ; i++){
            if(this.calibratedNodesList.get(i))
                s.append(i + " ");
        }
        return s.toString();
    }

    //
    /**
     * save the connection graph/hypergraph in a file used for this purpose (
     * the name of the file is set in the Configuration class
     * @param ctx context
     */
    public synchronized void toFile(Context ctx ){
        Log.d(TAG, "Save the CONNEXION GRAPH INTO file");
        FileManager file = new FileManager( Configuration.connectFileName, false /*do not append*/,ctx);

        //write in the file the connection graph
        //write the number of nodes
        Integer anInt = this.graph.V;
        file.write_txt(anInt.toString()+"\n");
        Log.d(TAG, "write" + anInt.toString());
        //write the number of edges
        anInt = this.graph.E;
        file.write_txt(anInt.toString()+"\n");
        // write if the nodes are calibrated
        Log.d(TAG, "node number : " + anInt.toString() + " edges number. " + anInt.toString());
        Log.d(TAG, "Calibrated nodes:");
        for (int i =0 ; i< this.graph.V ; i++) {
            if (i== this.graph.V -1 )
                file.write_txt(this.calibratedNodesList.get(i).toString() +"\n");
            else
                file.write_txt(this.calibratedNodesList.get(i).toString() +" ");
            //  Log.d(TAG, this.calibratedNodesList.get(i).toString() );

        }
        String tosave = new String () ;
        // write the connection graph, i.e., write the edges (src,dest,weight)
        for (int v =0; v < this.graph.V ; v++) {//go through each  node
            //   Log.d(TAG, "node " + v);
            for (DirectedEdge e :  this.graph.adj(v)) { // analyse outgoing edges for that node
                Log.d(TAG, "write edge from " + e.from() + " to " + e.to());
                //print the edge format
                tosave += new Integer(e.from()).toString()+" " + new Integer( e.to()).toString()+" "
                        +new Double(e.weight()).toString()+" "+ new Double(e.caracteristic().intercept).toString()+" "+
                        new Double(e.caracteristic().slope).toString()+" "+ new Double(e.caracteristic().standard_error).toString()+" " +
                        new Double(e.caracteristic().means_square_error).toString()+" " + new Double( e.caracteristic().meetingDuration).toString()+
                        " " + new Double( e.caracteristic().Rsquared).toString()+" " +new Double( e.caracteristic().meetingStartTime).toString() +" "
                        + new Double(e.caracteristic().ResidualSumOfSquares).toString() +"\n";

/*                file.write_txt(new Integer(e.from()).toString()+" " + new Integer( e.to()).toString()+" "
                +new Double(e.weight()).toString()+" "+ new Double(e.caracteristic().intercept).toString()+" "+
                new Double(e.caracteristic().slope).toString()+" "+ new Double(e.caracteristic().standard_error).toString()+" " +
                        new Double(e.caracteristic().means_square_error).toString()+" " + new Double( e.caracteristic().meetingDuration).toString()+
                                " " + new Double( e.caracteristic().Rsquared).toString()+" " +new Double( e.caracteristic().meetingStartTime).toString() +" "
                        + new Double(e.caracteristic().ResidualSumOfSquares).toString() +"\n");*/
            }


        }
        file.write_txt(tosave);

        Log.d(TAG, "end connexion save in file \n " );
        Log.d(TAG, "close file  \n " );

        file.close();
    }
    // extract the connection graph from the String that is provided

    /**
     *
     * @param connexionGraphString  string including the connection graph (i.e. hypergraph)
     */
    public void getfromString( String connexionGraphString){
        Log.e(TAG, "Connexion graph to extract from string:" + connexionGraphString);
        //extract line by line
        Scanner s = new Scanner(connexionGraphString);//.useDelimiter("\n");
        //extract the number of node
        Integer nodeNumber = new Integer(0);
        Integer edgeNumber= new Integer(0);

        if (s.hasNextInt()==true)//read number of nodes
            nodeNumber = new Integer(s.nextInt());
        if (s.hasNextInt()==true) // read number of edges
            edgeNumber = new Integer(s.nextInt());
        Log.e(TAG, "nb of nodes: " + nodeNumber.toString() +"\n nb of edges: " + edgeNumber.toString());

        // read the calibrated nodes
        String toplot = new String ();
        for (int i =0 ; i< this.graph.V ; i++) {
            if(s.hasNext()) {
                Boolean iscalibrated = Boolean.parseBoolean(s.next());
                toplot += "" + iscalibrated ;
                this.calibratedNodesList.set(i, iscalibrated);
            }
        }


        // scan each edge information
        Integer from = new Integer(0);
        Integer to = new Integer(0);
        Double weight = new Double(0);
        Double correlation = new Double(0);
        Double intercept = new Double(0);
        Double slope = new Double(0);
        Double standarderror = new Double(0);
        Double meadnsquareerror = new Double(0);
        Double metingduration = new Double(0);

        double Rsquared = new Double(0) ; // determination coeff
        double meetingStartTime = new Double(0); // when the meeting is starting
        Double ResidualSumOfSquares = new Double(0);

        while (s.hasNextInt()){//while
            Device fromD = new Device(0,"device0",0);
            Device toD = new Device (0,"device0",0);
            if ( s.hasNextInt()) {
                from = s.nextInt();
                fromD = new Device(from, "from", 0);
            }

            if ( s.hasNextInt()) {
                to = s.nextInt();
                toD = new Device (to,"to", 0);
            }

            if(s.hasNext()) {
                weight = Double.parseDouble(s.next());
                // Log.e(TAG, "weight is " + weight);
            }

            if(s.hasNext()) {intercept = Double.parseDouble(s.next()); /*Log.e(TAG, " intercept:"+ intercept);*/}
            if(s.hasNext()) {slope =  Double.parseDouble(s.next()); /*Log.e(TAG, " slope:"+ slope);*/}
            if(s.hasNext()) {standarderror  = Double.parseDouble(s.next());/* Log.e(TAG, " std err:"+ standarderror);*/}
            if(s.hasNext()) {meadnsquareerror = Double.parseDouble(s.next()); /*Log.e(TAG, "c mean:"+meadnsquareerror);*/}
            if(s.hasNext()) {metingduration  = Double.parseDouble(s.next());
                // Log.e(TAG, "duration is" + metingduration);
            }

            if(s.hasNext()) {Rsquared = Double.parseDouble(s.next()); /*Log.e(TAG, " R squarred"+ Rsquared);*/}
            if(s.hasNext()) {meetingStartTime = Double.parseDouble(s.next()); /*Log.e(TAG, " meeting start"+ meetingStartTime);*/}
            if(s.hasNext()) {ResidualSumOfSquares = Double.parseDouble(s.next()); /*Log.e(TAG, " residual sum of square "+ ResidualSumOfSquares);*/}

            Meeting ameeting = new Meeting(intercept,slope,standarderror,
                    meadnsquareerror,metingduration,Rsquared,meetingStartTime, ResidualSumOfSquares);
            addInnerEdge(fromD,toD,ameeting);
            Log.e( TAG,from + "->" + to + " weight " + weight + " corr "+correlation + " inter " + intercept + " slope " + slope + " stderr " + standarderror + "" +
                    " mean " + meadnsquareerror+ " dur: " +metingduration + "Rsquared:" + Rsquared + "meet start" + meetingStartTime + "residual sum of square" + ResidualSumOfSquares );
        }
    }


    /**
     * Extract the connexion file from a file (whose name is parametred in Configuration file)
     * @param ctx context
     * @return whether the connexion graph could be extracted from a file
     */

    public  boolean getFromFile( Context ctx){
        //extract the connection graph from the file
        Log.e(TAG, " begin Extract Connection from file, content: ");
        FileManager file = new FileManager(  Configuration.connectFileName, true /* append*/,ctx);

        String fileContent =  file.read_txt().toString();
        Log.e(TAG, "Extract Connection from file, content: " + fileContent);
        //extract line by line
        Scanner s = new Scanner(fileContent);//.useDelimiter("\n");
        //extract the number of node
        Integer nodeNumber = new Integer(0);
        Integer edgeNumber= new Integer(0);

        if (s.hasNextInt()==true)//first line: read number of nodes
            nodeNumber = new Integer(s.nextInt());
        if (s.hasNextInt()==true) // second line //read number of edge
            edgeNumber = new Integer(s.nextInt());

        Log.e(TAG, "nb of nodes: " + nodeNumber.toString() +" nb of edges: " + edgeNumber.toString()+ "\n");

        // if the node number is not the same as the one in the configuration file, we should stop it
        if (nodeNumber != Configuration.HYPERGRAPH_SIZE){
            file.close();
            return  false;
        }


        String is_calibrated = new String();
        // read the calibrated nodes
        for (int i =0 ; i< this.graph.V ; i++) {
            if(s.hasNext()) {
                Boolean iscalibrated = Boolean.parseBoolean(s.next());
                is_calibrated += i +iscalibrated.toString() + " ";
                this.calibratedNodesList.set(i, iscalibrated);
            }
        }
        Log.e(TAG, " " +  is_calibrated);

        // scan each edge information
        while (s.hasNext()){//while
            Integer from = new Integer(0);
            Integer to = new Integer(0);
            Double weight = new Double(0);
            Double intercept = new Double(0);
            Double slope = new Double(0);
            Double standarderror = new Double(0);
            Double meadnsquareerror = new Double(0);
            Double metingduration = new Double(0);
            double Rsquared = new Double(0) ; // determination coeff
            double meetingStartTime = new Double(0); // when the meeting is starting
            double ResidualSumOfSquares = new Double(0);

            Device fromD = new Device(0,"device0",0);
            Device toD = new Device (0,"device0",0);
            if ( s.hasNext()) {
                String d = s.next();
                from = Integer.parseInt(d);
                Log.e(TAG, "from " + from);
                fromD = new Device(from, "from",0);
            }

            if ( s.hasNext()) {
                to = Integer.parseInt(s.next());
                Log.e(TAG, "to " + to);
                toD = new Device (to,"to",0);
            }
            if(s.hasNext()) {
                weight = Double.parseDouble(s.next());
                /*Log.e(TAG, "weight is " + weight);*/
            }

            if(s.hasNext()) {intercept = Double.parseDouble(s.next()); /*Log.e(TAG, " intercept:"+ intercept);*/}
            if(s.hasNext()) {slope =  Double.parseDouble(s.next()); /*Log.e(TAG, " slope:"+ slope);*/}
            if(s.hasNext()) {standarderror  = Double.parseDouble(s.next()); /*Log.e(TAG, " std err:"+ standarderror);*/}
            if(s.hasNext()) {meadnsquareerror = Double.parseDouble(s.next()); /*Log.e(TAG, "c mean:"+meadnsquareerror);*/}
            if(s.hasNext()) {metingduration  = Double.parseDouble(s.next());
                Log.e(TAG, "duration is" + metingduration);
            }

            if(s.hasNext()) {Rsquared = Double.parseDouble(s.next()); /*Log.e(TAG, " Rsquared:"+ Rsquared);*/}

            if(s.hasNext()) {meetingStartTime = Double.parseDouble(s.next()); /*Log.e(TAG, " meetingStartTime"+ meetingStartTime);*/}
            if(s.hasNext()) {ResidualSumOfSquares = Double.parseDouble(s.next());  /*Log.e(TAG, "Resisual sim of square :"+ ResidualSumOfSquares);*/}

            Log.e (TAG, "from: "+ from + " to: " + to+ " weight:"+ weight +   ", intercept" +
                    " " + intercept + " slope: " + slope + " standard error " + standarderror + "" +
                    "mean sqr :" + meadnsquareerror + ", meeting duration " + metingduration
                    + " Rsquared" + Rsquared + "meeting start" + meetingStartTime + "residu sum of square" + ResidualSumOfSquares);
            Meeting ameeting = new Meeting( intercept,slope,standarderror, meadnsquareerror,metingduration,  Rsquared, meetingStartTime, ResidualSumOfSquares);
            addInnerEdge(fromD,toD,ameeting);
        }
        file.close();
        s.close();
        Log.e(TAG, " end Extract Connection from file ");
        return true;
    }


    /**
     * Check is the provided hyperedge is valid
     * if yes, it returns the hyperedge. Otherwise,an empty
     * hyperedge is returned
     * @param h2get hyper edge
     * @return hyperedge
     */
    public HyperEdge  getHyperEdge(HyperEdge h2get){
        HyperEdge hyperEdge2return =  new HyperEdge();

        if(h2get.is_valid() ){
            for (DirectedEdge e : this.graph.adj[ h2get.from()] ) { // goes through the graph to extract the hyperlinks
                if(e.caracteristic().meetingStartTime == h2get.id()){ // one inner edge is found
                    hyperEdge2return.add(e); // add this inner  edge to the hyperedge to return
                }
            }
        }
        return hyperEdge2return;
    }



    //

    /**
     * Add the provided hyperedge.
     * if this hyperedge does not exist in the hypergraph, then add it
     * else if this hyperedge already exists, then replace it with the one with minimal weight
     * @param h2add hyperedge to add
     *
     */
    public void addHyperEdge(HyperEdge h2add){
        Log.e(TAG, "add hyperedge" + h2add);
        // if the hyperedge does not have inner edges -> do nothing
        if (h2add.is_valid() ==false ){
            Log.e(TAG, "Hyperedge " + h2add.toString()  +  "cannot be added because it is invalid ");
            return ;
        }
        // if such same hyperedge (with same id) is found with higher weight, replace it
        // extract the corresponding hyperlink, if any, in the graph
        HyperEdge h = this.getHyperEdge(h2add); //extract hyper edge with the same id
        Log.e(TAG, "Look for the same hyper edge");
        // check if we found the corresponding hyperedge in the hypergraph
        if (h.is_valid() ==true) {
            // we found the hyeredge2add in the hypergraph
            Log.e(TAG, "we found the same hyperedge (with same id " + h.id() + ")" );
            if (h.weight() > h2add.weight()) {
                Log.e(TAG, "we need to replace the hyper egde in the graph");
                // update the weight of all the inneredges
                for (int i = 0; i < h2add.getInnerEdges().size(); i++) {//update all the inneredges
                    updateInnerEdge(h2add.getInnerEdges().get(i));
                }
                return ;
            }
        }


        //check if we can find an hyper edge head included or equal with higher height-> delete and add the new one
        //create a new hyper edge and remove obsolete hyper edges
        double  last_id  =-1;
        double id =-1;
        double hyperedge_weigh = 0;
        boolean to_update = true;

        int end =0;
        //is used to store an hyper edge of the graph, this hyperedge is analysed
        List<DirectedEdge> edge_list =  new ArrayList<DirectedEdge>();
        List<DirectedEdge> edge_list2remove =  new ArrayList<DirectedEdge>();


        for (DirectedEdge e : this.graph.adj[ h2add.from()] ) { // go through the graph to extract the hyperlinks
            end ++; //
            Log.d(TAG, " -- inspect " + e.from() + "-> " + e.to() + " id " + e.caracteristic().meetingStartTime + "\n   " + e.toString());
            //hyperedges are added consecutively and are not mixed

            // extract the id of the hyper edge
            id =  e.caracteristic().meetingStartTime ; // get the id of the hyper edge
            if (last_id == -1) // we start, that is the first edge
                last_id = id;

            // if we are still analysing the same hyper edge
            if (last_id == id ){
                //continu extracting the inner egde of the hyper edge
                edge_list.add(e);
                hyperedge_weigh += e.weight();
                Log.e(TAG, " continue with inner edge " + e.from() + "-> " + e.to() + " id "+ e.caracteristic().meetingStartTime + " weight " + hyperedge_weigh );

                if(  h2add.isIn(e) == false){ // if the
                    to_update = false; // state that we do not have to update that hyper edge
                    Log.e(TAG, " This analysed hyperedge is not included in " + h2add.toString());
                }
            }

            //if we have finished with this hyper edge of if we are at the end of the list of hyperedge
            if(last_id != id || this.graph.adj[h2add.from()].size() == end){
                // if the weigh of the hyper edge to add <

                if (h2add.weight() >= hyperedge_weigh && to_update == true){
                    Log.e(TAG, " This hyperedge to add is ignored " );
                    return ;// there is a better edge in the graph, there is not need to continue
                }

                if (h2add.weight() < hyperedge_weigh && to_update == true){
                    Log.e(TAG, " This hyperedge is not ignored and should be removed from the hypergraph " );
                    // remove from hypergraph
                    for (int k= 0; k< edge_list.size(); k++){
                        edge_list2remove.add(edge_list.get(k));
                    }
                }





                if (h2add.weight() >= hyperedge_weigh && to_update == true){
                    Log.e(TAG, " This hyperedge to add is ignored " );
                }else{
                    Log.e(TAG, " This hyperedge is not ignored and should be removed from the hypergraph " );
                    // remove from hypergraph
                    for (int k= 0; k< edge_list.size(); k++)
                        edge_list2remove.add(edge_list.get(k));
                }

                //init and continue
                edge_list =  new ArrayList<DirectedEdge>();
                hyperedge_weigh=0; // reinit 4 next hyperedge
                to_update = true;
                last_id = id;
                edge_list.add(e);//add the inner edge
                hyperedge_weigh += e.weight();
                if(  h2add.isIn(e) == false){
                    to_update = false; // state that we do not have to update that hyper edge
                    Log.e(TAG, " This hyperedge is not included in " + h2add.toString());
                }
            }
        }

        int size = h2add.getInnerEdges().size();
        for (int i=0 ; i< size; i++){//add each inner edge
            this.graph.addEdge( h2add.getInnerEdges().get(i));
            Log.e(TAG, " add inner edge " + h2add.getInnerEdges().get(i).from() + "->" + h2add.getInnerEdges().get(i).to());
        }


        size = edge_list2remove.size();
        //remove now starting at the end and
        while (size > 0){
            Log.e(TAG, "remove " + edge_list2remove.get(0).toString());
            graph.removeEdges(edge_list2remove.get(0));
            //     edge_list.remove(0);
            ///      edge_list.remove(size()-1);
            //  size = edge_list2remove.size();
            size--;
        }
    }

     /**
     * Update the provided hyperedge in  the connexion graph (i.e. hypergraph)
      * If the hyperedge does not belong to the hypergraph, nothing is done
     * @param edge2add
     */
    private void updateInnerEdge(DirectedEdge edge2add){
        Log.d(TAG , "********* Add inner edge "+ edge2add.from() + "->" + edge2add.to() );
        int i =0;
        // goe through the adjacency list
        Log.d(TAG, "# number of nodes in  hypergraph: " + this.graph.V );
        Log.d(TAG, "want to add " + edge2add.from());

        for (DirectedEdge e : this.graph.adj[edge2add.from()] ) {// check if that edge is already contained in the graph
            if(edge2add.from() == e.from() && edge2add.to() == e.to() &&  edge2add.caracteristic().meetingStartTime ==  e.caracteristic().meetingStartTime   ){//edge found in the graph
                this.graph.adj[edge2add.from()].set(i,edge2add);
                Log.e(TAG, " "+ edge2add.toString() +"updated"+ e.weight() + "<"+ edge2add.weight() + "with position"+i);
                // stop looking : the edge has been found
                return ;
            }
            i++;
        }
    }

    /**
     * add the provided edge to the hypergraph
     * @param e edge to add to the hypergraph
     */
    private void addInnerEdge(DirectedEdge e ){
        this.addInnerEdge(new Device (e.from(), " ", e.from()), new Device (e.to(), " ", e.to()), e.caracteristic() );
    }


    /**
     * add an edge with the provided edge characteristics
     * @param src source of the edge
     * @param dest destination of the edge
     * @param meeting_characteristics caracteristics of the edge
     */
    private void addInnerEdge(Device src, Device dest, Meeting meeting_characteristics){

        Log.d(TAG , "********* Add edge "+  src.getVertexId() + "->" + dest.getVertexId() + "with meeting char" +  meeting_characteristics );
        //  determine the parameter that should be used to compute the shortest path
        DirectedEdge edge2add  = new DirectedEdge(src.vertexId, dest.vertexId,  0, meeting_characteristics);
        // Log.d(TAG , "********* edge created "+  src.getVertexId() + "->" + dest.getVertexId()  );
        if(Configuration.shortest_path_criteria == Configuration.STD_ERROR_CRITERIA)
            edge2add = new DirectedEdge(src.vertexId, dest.vertexId,  meeting_characteristics.standard_error, meeting_characteristics);

        if(Configuration.shortest_path_criteria == Configuration.MEAN_SQUARE_ERROR_CRITERIA)
            edge2add = new DirectedEdge(src.vertexId, dest.vertexId,  meeting_characteristics.means_square_error, meeting_characteristics);

        if(Configuration.shortest_path_criteria == Configuration.MEETING_DURATION_CRITERIA) {
            if (meeting_characteristics.meetingDuration ==0)
                edge2add = new DirectedEdge(src.vertexId, dest.vertexId, 1 / 0.0001, meeting_characteristics);
            else
                edge2add = new DirectedEdge(src.vertexId, dest.vertexId, 1 / meeting_characteristics.meetingDuration, meeting_characteristics);
        }
        if(Configuration.shortest_path_criteria == Configuration.MEETING_RSQUARED)
            edge2add = new DirectedEdge(src.vertexId, dest.vertexId,  1 -meeting_characteristics.Rsquared, meeting_characteristics);

        if(Configuration.shortest_path_criteria ==Configuration. MEETING_RESIDUAL_SUM_OF_SQUARRE)
            edge2add = new DirectedEdge(src.vertexId, dest.vertexId,  meeting_characteristics.ResidualSumOfSquares, meeting_characteristics);

        //  if that edge does not exist then add the edge
        int i =0;

        //  Log.e(TAG, "before adding edge "+edge2add.toString() + "with characteristics" + meeting_characteristics.toString());

        // go through the adjacency list
        for (DirectedEdge e : this.graph.adj[src.vertexId] ) {// check if that edge is already contained in the graph
            if(src.vertexId == e.from() && dest.vertexId == e.to() &&  meeting_characteristics.meetingStartTime ==  e.caracteristic().meetingStartTime   ){//edge found in the graph
                // keep the edge with the lowest weight

                if (e.weight() < edge2add.weight()){
                    //if (e.caracteristic().meetingStartTime < meeting_characteristics.meetingStartTime){
                    this.graph.adj[src.vertexId].set(i,edge2add);
                    Log.e(TAG, " "+ edge2add.toString() +"updated"+ e.weight() + "<"+ edge2add.weight() + "with position"+i);
                }
                else{//todo check meeting date
                    Log.e(TAG, " "+ edge2add.toString() +"not updated " + + e.weight() + ">="+ edge2add.weight() + "with position"+i);
                }
                // stop looking : the edge has been found
                return ;
            }
            i++;
        }
        // the new edge should be added to the graph
        this.graph.addEdge(edge2add);

        // Log.e(TAG, "+++++++++++++++++++++++++++++++++++++");
        //  Log.e(TAG, "after adding edge CONNECTION GRAPH IS" + this.toString());
    }


    /**
     * create a spanning hypergraph
     * @param hyperedgeNB number of hyper edges in the hypergraph
     * @param hyperedgeCard number of edges in the hypergraph
     * @param from source of the spaning hypergraph
     */
    public void spanning2ref(int hyperedgeNB, int hyperedgeCard, int from){
        this.remainingHyperEdge2add = hyperedgeNB;
        this.leavesList= null;
        this.stop = false;


        //connect the reference nodes to the consolidated ones
        for (int i=0; i< Configuration.REFERENCENB; i++){

            Meeting ameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ 1 ,  0  ,  StdRandom.uniform(100) , /*meeting duration */
                    Configuration.recordDuration_ms,/* R²*/ 1  ,/*time*/0 ,  StdRandom.uniform(100));
            DirectedEdge e = new DirectedEdge(i  + Configuration.VERTEXNB, Configuration.VERTEXNB + Configuration.REFERENCENB ,  0 /*weight*/,ameeting);

            //add an edge
            this.graph.addEdge(e);

        }

        this.spanning2ref(hyperedgeCard, from);
    }

    /**
     * create a spanning hypergraph
     * @param hyperedgeCard number of hyper edges in the hypergraph
     * @param from source of the spanning hypergraph
     */
    private void spanning2ref( int hyperedgeCard, int from){
        if (stop == true)
            return;

        if(leavesList == null){
            leavesList = new ArrayList<>();
            //create a hyperegde from
            Log.e(TAG,  "**************"+ "Create a spanning hypergraph from " + from + " **************"  );
        }else{
            Log.e(TAG, "continue creating a spanning hypergraph from " + from  + " leaves " + leavesList.toString() );
            //remove from from the set of leaves
            boolean b =   leavesList.remove(new Integer(from));
            Log.e(TAG, "   * remove " + from + " from " + leavesList.toString() + " is done " + b);
        }


        //create a hyperedge including a random number of inner edges
        int hyperedge_cardinality = StdRandom.uniform(hyperedgeCard) +1; // source

        //if there is enought space to add this hyperedge
        if( hyperedge_cardinality < remainingHyperEdge2add && remainingHyperEdge2add > 0 ){
            Log.e(TAG, "   * add an hyperedge with " + hyperedge_cardinality + " inner edge(s): ");
            //is used to create a hyper edge (with the same tag)
            double atime= (double)  System.currentTimeMillis(); //when the hyperedge is added
            //list of destination
            List<Integer> tempLeaveList = new ArrayList<>();


            // create the hyper edge
            for (int i=0 ; i< hyperedge_cardinality ; i++){
                int dest = -1;
                //select randomly a destination (which is not = source and which has not yet being selected yet as a destination
                while ((dest == -1 ) || dest == from  || isIn(new Integer(dest), tempLeaveList) == true){
                    dest = StdRandom.uniform(Configuration.VERTEXNB + Configuration.REFERENCENB);
                }


                //if the destination is not a reference node, we can proceed. Otherwise we stop because we attained a reference node
                if( dest <= Configuration.VERTEXNB  ){
                    //add the destination to the list of destination
                    tempLeaveList.add(new Integer(dest));
                    //add the destination to the list of leaves
                    leavesList.add(new Integer(dest));
                }

                //add the inner edge
                Log.e(TAG, "   - add inner edge " + from + " -> " + dest );
                Meeting ameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ StdRandom.uniform(100), StdRandom.uniform(100)  ,  StdRandom.uniform(100) , /*meeting duration */
                        Configuration.recordDuration_ms,/* R²*/ StdRandom.uniform(100)/100 ,/*time*/atime ,  StdRandom.uniform(100));
                DirectedEdge e = new DirectedEdge(from, dest, StdRandom.uniform(100) /*weight*/,ameeting);
                this.graph.addEdge(e);
            }//end for

            remainingHyperEdge2add -= hyperedge_cardinality;

            for (int i = 0 ; i< leavesList.size() ; i++){
                Log.e(TAG,"*** continue from " + leavesList.get(i) + " leaves " + leavesList.toString() + " # remaining hyperedges to add " + remainingHyperEdge2add );
                spanning2ref(  hyperedgeCard, leavesList.get(i));
            }
        }else{//stop condition
            this.stop = true;
            Log.e(TAG,"   - stop condition at " + from + " because remaining hyper edges to add = " + remainingHyperEdge2add );
            //todo draw an edge from all the leaves to a reference node
            // select randomly a reference node
            int ref = StdRandom.uniform( Configuration.REFERENCENB);

            Log.e(TAG, "add an edge " + from +" -> " + (ref+Configuration.VERTEXNB));
            Meeting aameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ StdRandom.uniform(100), StdRandom.uniform(100)  ,  StdRandom.uniform(100) , /*meeting duration */
                    Configuration.recordDuration_ms,/* R²*/ StdRandom.uniform(100)/100 ,/*time*/0 ,  StdRandom.uniform(100));
            DirectedEdge ea = new DirectedEdge(from, ref + Configuration.VERTEXNB, StdRandom.uniform(100) /*weight*/,aameeting);
            //add an edge
            this.graph.addEdge(ea);




            ref = StdRandom.uniform( Configuration.REFERENCENB);


            for (int i=0; i< leavesList.size(); i++){
                Log.e(TAG, "add an edge " + leavesList.get(i) +" -> " + (ref+Configuration.VERTEXNB));
                Meeting ameeting = new Meeting(/*intercept*/ StdRandom.uniform(100), /*slope*/ StdRandom.uniform(100), StdRandom.uniform(100)  ,  StdRandom.uniform(100) , /*meeting duration */
                        Configuration.recordDuration_ms,/* R²*/ StdRandom.uniform(100)/100 ,/*time*/0 ,  StdRandom.uniform(100));
                DirectedEdge e = new DirectedEdge(leavesList.get(i), ref + Configuration.VERTEXNB, StdRandom.uniform(100) /*weight*/,ameeting);
                //add an edge
                this.graph.addEdge(e);
            }

        }


    }


    /**
     * returns if a given elements belongs to a given list
     * @param element element
     * @param list liste of elements
     * @return returns if an element belongs to the given list
     */
    private boolean isIn(Integer element, List<Integer> list){
        for (int i=0; i< list.size(); i++ ){
            if(element == list.get(i))
                return true;
        }
        return false ;
    }

}
