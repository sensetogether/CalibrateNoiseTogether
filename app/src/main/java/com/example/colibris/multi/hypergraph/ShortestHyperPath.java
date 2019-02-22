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

import android.util.Log;

import com.example.colibris.configuration.Configuration;
import com.example.colibris.multi.Calibration;
import com.example.colibris.multi.graph.DirectedEdge;
import com.example.colibris.multi.graph.IndexMinPQ;
import  com.example.colibris.multi.graph.EdgeWeightedDigraph;
import java.util.ArrayList;
import java.util.List;

/**
 * The ShortestHyperPath class permits to compute theShortest hyperpath
 * in a hypergraph. in pratice, each hyperedge of the hypergraph
 * represents a calibration meeting that took place.
 */

public class ShortestHyperPath {
    /**
     * log related information
     */
    public static final String TAG = "ShortestHyperPath";
    /**
     * distance to other nodes
     */
    double[]  distTo ;
    /**
     * edges to others
     */
    DirectedEdge[] edgeTo;
    /**
     * heap to organise
     */
    IndexMinPQ<Double> heap;

    /**
     * best hyperpath that has been generated
     */
    public EdgeWeightedDigraph bestHyperpath ;

    /**
     * Computes a shortest-paths tree from the source vertex {@code s} to every other
     * vertex in the edge-weighted digraph {@code G}.
     *
     * @param  G hypergraph corresponding to a edge-weighted digraph
     * @param  s the source vertex
     * @throws IllegalArgumentException if an edge weight is negative
     * @throws IllegalArgumentException unless {@code 0 <= s < V}
     */
    public EdgeWeightedDigraph DijkstraSP(EdgeWeightedDigraph G, int s , int dest, boolean[] visited) {

        //initialisation
        bestHyperpath = new EdgeWeightedDigraph(G.V); // best hyperpath that will be ultimaly returned
        //establish the nodes that have been already visited
        boolean visitedSoFar [] = new boolean[G.V];
        for (int i=0; i< visited.length; i++){
            visitedSoFar[i] = visited[i];
        }

        boolean freezedVisitedSoFar [] = new boolean[G.V]; // freeze visited so far, i.e. the nodes that have been visisted should node be visited twice
        // distance to s is 0 and infinite for others
        distTo = new double[G.V];// distance to a node
        for (int v = 0; v < G.V; v++) // at start distance to node is infinit (except for starting point)
            distTo[v] = Double.POSITIVE_INFINITY;
        distTo[s] = 0.0;
        edgeTo = new DirectedEdge[G.V];  // edgeTo[v] = last edge on shortest s->v path
        // end of the initialisation


        // relax vertices in order of distance from s
        heap = new IndexMinPQ<Double>(G.V);
        if (visited[s]==false ) {
            heap.insert(s, distTo[s]);
        }

        while (!heap.isEmpty()) { //while there is some nodes to visit

            int v = heap.delMin(); // visit node v
            visitedSoFar[v] = true; // this node v is set as visited

            Log.d(TAG, "Visit node " + v );
            Log.d(TAG, "node visited so far is henceforth: " + toPrint(visitedSoFar));
            //save outgoing edges in a list (from wich we will remove all theedges we have treated)
            List<DirectedEdge> adj =  new ArrayList<DirectedEdge>();
            for (DirectedEdge e : G.adj(v)) {
                //Log.e(TAG, "extract edge for adj" + e);
                adj.add(e);
            }


            while( adj.size()>0) {//go through the outgoing eges
                DirectedEdge e = adj.get(0);
//                Log.e(TAG, " - Outgoing edge " + e.from() + "->" + e.to());
                //Log.e(TAG, " - Outgoing edge " + e);

                // check if e is part of an hyperedge
                boolean isHyperEdge = false;
                //is that an hyperedge ?
                for (DirectedEdge d : G.adj(v)) {
                    if (e.caracteristic().meetingStartTime == d.caracteristic.meetingStartTime  && e != d && isHyperEdge == false){
                        // that is an hyperedge (a meeting is taking place with at least two other nodes)
                        isHyperEdge = true;
                        System.arraycopy( visitedSoFar, 0, freezedVisitedSoFar, 0, visitedSoFar.length );
                    }
                }
                // e is not a hyperedge
                if (isHyperEdge == false ) {
                    Log.e(TAG, "   - Outgoing isolated edge " + e.from() + "->" + e.to());
                    relax(e, visitedSoFar);
                    adj =    this.removefromAdj(e, adj);// remove that outgoing edge which is threated now
                }
                else{// e is a hyperedge
                    // Log.e(TAG, "   - Outgoing inner edge " + e.from() + "->" + e.to());
                    // extract the overall hyperedge
                    HyperEdge E= this.getHyperEdge(e, G);
                    // remove each inneredge from the adjancy list to threat
                    adj =    this.removefromAdj(E.getInnerEdges().get(0), adj);

                    Log.e(TAG, " - outgoing inner edge " + e.from() + "->" + e.to() + " is included in " + E.toString());
                    // Log.d(TAG, "    remaining outgoing edge to treat: " + adj.toString());
                    // establish the shortest hyperpath for each inner edge constituing the hyperedge
                    List<EdgeWeightedDigraph> hyperpath4hyperedge = new ArrayList<EdgeWeightedDigraph>();

                    Double weight = new Double (0); //distTo[v] + E.weight() ;
                    //compute the shortest path from each inner edge
                    for (int i =0; i< E.getInnerEdges().size() ;  i++){
                        DirectedEdge ei = E.getInnerEdges().get(i);
                        Log.e(TAG, "  - compute SP from "+ ei.to() + " excluding nodes: " + toPrint(freezedVisitedSoFar));
                        //initialise the shortest path
                        ShortestHyperPath sp_ei= new ShortestHyperPath();

                        hyperpath4hyperedge.add( sp_ei.DijkstraSP(  G, ei.to() ,  dest, freezedVisitedSoFar));
                        // Log.e(TAG, "hyperpath so forth from :" + ei.to()+" is "+ hyperpath4hyperedge.toString());
                        //compute the weight of this hyperpath
                        weight += sp_ei.weight(ei.to(), hyperpath4hyperedge.get(i), dest);
                        Log.e(TAG, "acumulated weight is " + weight);
                    }
                    weight += distTo[v] + E.weight();

                    // if Hir = Hiv + weight(Ei) + sum_i H(ei, r) < bestone so far
                    if (weight <    this.weight(s, this.bestHyperpath , dest) )
                    {
                        Log.e(TAG, "weight (" +s + "through " + v + " to " + dest + " = "+ weight + "< best hyperpath");
                        bestHyperpath = new EdgeWeightedDigraph(G.V);
                        // add Hir to the best hyperpath
                        for (DirectedEdge edge = edgeTo[v]; edge != null; edge = edgeTo[edge.from()]) {
                            Log.e(TAG, "add edge "+edge.toString());
                            bestHyperpath.addEdge(edge.reverse());
                        }
                        // add the hyperedge Ei
                        // Log.e(TAG, "TIME TO ADD HYPEREDGE " + E.toString() + " TO BEST HYPERPATH" + bestHyperpath);
                        for (int i =0; i< E.getInnerEdges().size() ;  i++) {

                            DirectedEdge ei = E.getInnerEdges().get(i);
                            //     Log.e(TAG, "get inner edge" + ei );
                            //   Log.e(TAG, "get reverse edge" + ei.reverse());
                            bestHyperpath.addEdge(ei.reverse());
                            // Log.e (TAG, "HYPERPAHT AFTER " + bestHyperpath);
                        }
                        // add H(ei,r) for each ei in Ei
                        for (int i=0; i< hyperpath4hyperedge.size() ; i++){
                            bestHyperpath.merge(hyperpath4hyperedge.get(i));
                        }
                        Log.e (TAG, "Update the best hypergraph from " + s + "to" + bestHyperpath);
                    }else{
                        Log.e(TAG, "weight (" +s + "through" + v + "to" + dest + ">  best hyperpath");
                    }
                }                 //v is an hyperedge
            }
        }
        Log.e(TAG, "finish to compute Shortest hyperpath from " + s);
        Double best_weight =  this.weight(s, this.bestHyperpath , dest);
        Log.e(TAG, "Best hyperpath weight =  " + best_weight + "disto =  "  + "from " +s + " = "+  distTo[dest]);

        // check whether dikstra is the best
        if ( this.weight(s, this.bestHyperpath , dest) > distTo[dest]){
            Log.e(TAG, "Best path from " + s + " is the dikstra path: ");

            //best hyperpath is a dikjstra path from i to r
            this.bestHyperpath = new EdgeWeightedDigraph(G.V);
            for (DirectedEdge e = edgeTo[dest]; e != null; e = edgeTo[e.from()]) {
                Log.e(TAG, " "+e.toString());
                bestHyperpath.addEdge(e.reverse());
            }
        }
        Log.d(TAG, "Best hyperpath so far from " + s + " is " + bestHyperpath.toString());
        return bestHyperpath;
    }

    /**
     *
     * @param freezedVisitedSoFar
     * @return
     */
    private String toPrint( boolean freezedVisitedSoFar[]){

        String toReturn = new String();
        for (int i=0; i<  freezedVisitedSoFar.length ; i++){
            if (freezedVisitedSoFar[i]==true)
                toReturn = toReturn.concat(" " +i );
        }
        return toReturn;
    }

    /**
     *
     * @param innerEdge
     * @param adj
     * @return
     */
    private List<DirectedEdge> removefromAdj(DirectedEdge innerEdge, List<DirectedEdge> adj ){

        int j = adj.size()-1;
        while (j >=0){
            DirectedEdge e = adj.get(j);
            if(e.caracteristic().meetingStartTime == innerEdge.caracteristic().meetingStartTime){ // one inner edge is found
                adj.remove(j);
                //  Log.d(TAG, "remove edge " + e + " from adj");
            }
            j= j-1;
        }

        return adj;
    }


    /**
     *
     * @param innerEdge
     * @param G
     * @return
     */
    private HyperEdge  getHyperEdge(DirectedEdge innerEdge, EdgeWeightedDigraph G ){
        HyperEdge hyperEdge2return =  new HyperEdge();
        for (DirectedEdge e : G.adj[ innerEdge.from()] ) { // goe through the graph to extract the hyperlink

            if(e.caracteristic().meetingStartTime == innerEdge.caracteristic().meetingStartTime){ // one inner edge is found
                hyperEdge2return.add(e); // add this inner  edge to the hyperedge to retrun
                //      Log.d(TAG, "add edge to hyperedge ");
            }
        }
        return hyperEdge2return;
    }



    // relax edge e and update heap if changed
    private void relax(DirectedEdge e, boolean[] visited) {
        int v = e.from(), w = e.to();
        if (distTo[w] > distTo[v] + e.weight()) {
            distTo[w] = distTo[v] + e.weight();
            edgeTo[w] = e;
            if (heap.contains(w)){
                heap.decreaseKey(w, distTo[w]);
            }
            else{ // if that node is not already visited ???
                if (visited[w] ==false)
                    heap.insert(w, distTo[w]);
            }
        }
    }



    private EdgeWeightedDigraph bestHyperedgeTo; // is equivalent to disto = best hyperpath so far


    public HyperConnection getShortestHypergraph (int src, HyperConnection local_connexion){
        //create an empty best hyperpath
        HyperConnection bestHypergraph = new HyperConnection();
        bestHyperedgeTo =  bestHypergraph.graph ;
        // compute the shortest hyerpath from src to R (i.e. consolidated node
        Log.d(TAG, "******************************\n look for the best hyperpath from " + src + " to " + local_connexion.getConsolidatedNode()+ "\n****************************** \n");
        boolean visitedSoFar [] = new boolean[local_connexion.graph.V];
        DijkstraSP(local_connexion.graph, src, local_connexion.getConsolidatedNode(),visitedSoFar );


        bestHypergraph.graph = this.bestHyperpath;
        return bestHypergraph;
    }

    /*
return a slope = 1, an intercept =0 and an error = 0 if we cannot calibrate
 */
    public Calibration single_hop_calibrate (int src, HyperConnection shortestHyperpath ){
        if (shortestHyperpath == null ){
            Log.e(TAG, "shortest path is null");
            return  new Calibration(1,0, 0, 0);
        }

        Log.d(TAG, "compute the shortest hyperpath from r to " + src);
        Log.d(TAG,"based on the following hypergraph" + shortestHyperpath.graph.toString());
        int r = Configuration.HYPERGRAPH_SIZE-1;

        Calibration finalCalibration =  single_hop_calibrate (   src,   shortestHyperpath, r , new Calibration(1,0, 0, 0));

        Log.e(TAG, "final single calibration: " + finalCalibration.toString());
        return finalCalibration;
    }




    /*
return a slope = 1, an intercept =0 and an error = 0 if we cannot calibrate
 */
    public Calibration multi_hop_calibrate (int src, HyperConnection shortestHyperpath ){
        if (shortestHyperpath == null ){
            Log.e(TAG, "shortest path is null");
            return  new Calibration(1,0, 0, 0);
        }

        Log.d(TAG, "compute the shortest multi hyperpath from r to " + src);
        Log.d(TAG,"based on the following multi hypergraph" + shortestHyperpath.graph.toString());
        int r = Configuration.HYPERGRAPH_SIZE-1;

        Calibration fincalibration =  multi_hop_calibrate (   src,   shortestHyperpath, r , new Calibration(1,0, 0, 0));

        Log.e(TAG, "final muti hops calibration: " + fincalibration.toString());
        return fincalibration;
    }


    public Double weight(int src, EdgeWeightedDigraph graph, int dest ) {
        boolean visited [] = new boolean[graph.V];
        //no node has been visited so far
        for (int i=0; i< visited.length; i++){
            visited[i] = visited[i];
        }
        return this.weight( src,  graph, dest, visited );
    }


    // compute the weight from node src to dest
    // typically src is r and dest is the source (i)
    public Double weight(int src, EdgeWeightedDigraph graph, int dest, boolean visited[] ){
        if (graph == null){
            Log.d(TAG, "  ** Compute weight from node " + src + " to " + dest + "= " + Double.POSITIVE_INFINITY);//Log.d(TAG, "** no hyperpath found");
            return Double.POSITIVE_INFINITY;
        }

        visited[dest] = true;
        //stop condition : src is reach or there is no incoming (hyper)edge, in wich case
        // there is an empty hyperpath between s and r
        if (dest == src ){//stop here, the calibration to apply is already performed
            Log.d(TAG, "  ** Compute weight from node " + src + " to " + dest + "= " + 0);//Log.d(TAG, "** src " + src + " is also the destination -> weight =0 " );
            return  0.0;
        }
        // Log.d(TAG, "** adjancy lenght of node "+ dest +": " + graph.adj[dest].size());
        // if there is no (hyper)edge -> we are not connected to src -> return
        if(graph.adj[dest].size() == 0){
            //  Log.d(TAG, "** node " + dest + "is not connected to " + src + "-> weigth infinite ");
            Log.d(TAG, "  ** Compute weight from " + src + " to " + dest + "= " + Double.POSITIVE_INFINITY);
            return  Double.POSITIVE_INFINITY;
        }
        Double weight2return =0.0;
        for (DirectedEdge e :   graph.adj[dest]){
            //  Log.d(TAG, "** (inner) edge " + e.from() + "->" + e.to() + "with edge weight " + e.weight() );
            // extract the incoming ingress link
            //  Y = a x + b -> X = Y/a - b/a
            //  double s = 1/ e.caracteristic().slope;
            //Log.d(TAG, "  s = 1 / edge.slope = 1 /" + e.caracteristic().slope + " = "+s );
            //double i = - e.caracteristic().intercept/e.caracteristic().slope;
            //Log.d(TAG, "  i = - edge intercept / edge slope = " + e.caracteristic().intercept + " / " +   e.caracteristic().slope  + " = " + i );
            //Calibration calibrationSoFar = new Calibration(0,0);
            // calibrationSoFar.slope = whereX.slope * s ;
            // calibrationSoFar.intercept = whereX.intercept + whereX.slope * i;

            weight2return += e.weight() + weight(src, graph, e.to(), visited );
        }
        Log.d(TAG, "  ** Compute weight from node " + src + " to " + dest + "= " + weight2return);
        //   Log.e(TAG, "returned weight " + weight2return);
        return weight2return;
    }




    // hyperpath   A -- B ---C ----D----where --- E already created --- r
    // return the calibration to perform
    // if no calibration can be performed, then it return a slope = 1  and a intercept = 0 and error = 0 and isalibred = false
    public Calibration single_hop_calibrate ( int src, HyperConnection shortestHyperpath, int where /*ancestor */, Calibration whereX  ){
        Log.d(TAG, "analyse node " + where);
        //stop condition : src is reach or there is no incoming (hyper)edge, in which case there is no hyperpath between s and r
        if (where == src ){//stop here, the calibration to apply is already performed
            Log.d(TAG, "src " + src + " is reach, single calibration is completed");
            Log.d(TAG, "single calibration is before return " + whereX.toString());
            return  whereX;
        }
        Log.d(TAG, "number of edges in graph: " + shortestHyperpath.graph.E);
        // if there is no (hyper)edge -> we are not connected to src -> return
        if(shortestHyperpath.graph.E == 0){
            Log.d(TAG, "node " + where + "is not connected to " + src + "-> calibration cannot be performed");
            return  new Calibration(1, 0, 0, 0);
        }

        Calibration fromsrc_on_all_branchs = new Calibration(0,0, 0, 0);
        for (DirectedEdge e :              shortestHyperpath.graph.adj[where]){
            Log.d(TAG, "(inner) edge " + e.from() + "->" + e.to());Log.d(TAG, "(inner) edge " + e.caracteristic().toString());
            Calibration calibration2return = new Calibration(0,0, 0, 0);
            // extract the incoming ingress link : Y = a x + b + error -> X = Y/a - b/a - err/a
            calibration2return.slope += 1/ e.caracteristic().slope;
            Log.d(TAG, "  s = 1 / edge.slope = 1 /" + e.caracteristic().slope + " = "+1/ e.caracteristic().slope );

            calibration2return.intercept += - e.caracteristic().intercept/e.caracteristic().slope;
            Log.d(TAG, "  i = - edge intercept / edge slope = " + e.caracteristic().intercept + " / " +   e.caracteristic().slope  + " = " + - e.caracteristic().intercept/e.caracteristic().slope );

            calibration2return.weighted_cumulated_error += - e.weight() /e.caracteristic().slope;
            Log.d(TAG, "  weightederr = - edge error / edge slope = " + - e.weight() + " / " +   e.caracteristic().slope  + " = " + - e.weight() /e.caracteristic().slope );
            calibration2return.cumulated_errror += -e.weight();
            //continue the traversal providing the good calibration so far
           Calibration  fromsrc_on_this_branch = single_hop_calibrate(src, shortestHyperpath, e.to(),  calibration2return );
            fromsrc_on_all_branchs.slope += fromsrc_on_this_branch.slope;
            fromsrc_on_all_branchs.intercept += fromsrc_on_this_branch.intercept;
            fromsrc_on_all_branchs.cumulated_errror += fromsrc_on_this_branch.cumulated_errror;
            fromsrc_on_all_branchs.weighted_cumulated_error +=fromsrc_on_this_branch.weighted_cumulated_error;

        }
        Log.e(TAG, "calibration 2 return at  "+where  + "= " + fromsrc_on_all_branchs.toString());
        return  fromsrc_on_all_branchs ;
    }

    // hyperpath   A -- B ---C ----D----where --- E already created --- r // return the calibration to perform
    // if no calibration can be performed, then it return a slope = 1  and a intercept = 0 and error = 0 and isalibred = false
    public Calibration multi_hop_calibrate ( int src, HyperConnection shortestHyperpath, int where /*ancestor */, Calibration whereX  ){
        Log.d(TAG, "MUTLI HOP analyse node " + where);
        //stop condition : src is reach or there is no incoming (hyper)edge, in which case there is no hyperpath between s and r
        if (where == src ){//stop here, the calibration to apply is already performed
            Log.d(TAG, "src " + src + " is reach, calibration is completed");
            Log.d(TAG, "multi calibration returned is " + whereX.toString());
            return  whereX;
        }
        Log.d(TAG, "number of edges in graph: " + shortestHyperpath.graph.E);
        // if there is no (hyper)edge -> we are not connected to src -> return
        if(shortestHyperpath.graph.E == 0){
            Log.d(TAG, "node " + where + "is not connected to " + src + "-> calibration cannot be performed");
            return  new Calibration(1, 0, 0, 0);
        }

        Calibration overallCalibration2return = new Calibration(0,0, 0, 0);

        for (DirectedEdge e :              shortestHyperpath.graph.adj[where]){
            Calibration calibration2returnforOneBranch = new Calibration(0,0, 0, 0);
            Log.d(TAG, "(inner) edge " + e.from() + "->" + e.to());
            Log.d(TAG, "(inner) edge " + e.caracteristic().toString());

            // extract the incoming ingress link : Y = a x + b + error -> X = Y/a - b/a - err/a
            double s = 1/ e.caracteristic().slope;
            Log.d(TAG, "  s = 1 / edge.slope = 1 /" + e.caracteristic().slope + " = "+s );

            double i = - e.caracteristic().intercept/e.caracteristic().slope;
            Log.d(TAG, "  i = - edge intercept / edge slope = " + e.caracteristic().intercept + " / " +   e.caracteristic().slope  + " = " + i );

            double err = - e.weight() /e.caracteristic().slope;
            Log.d(TAG, "  weightederr = - edge error / edge slope = " + - e.weight() + " / " +   e.caracteristic().slope  + " = " + err );

            Calibration calibrationSoFar = new Calibration(0,0, 0, 0);
            calibrationSoFar.slope                    += whereX.slope * s ;
            calibrationSoFar.intercept                += whereX.intercept + whereX.slope * i;
            calibrationSoFar.weighted_cumulated_error += whereX.weighted_cumulated_error + whereX.slope * err; //whereX.weighted_cumulated_error + whereX.slope * i
            calibrationSoFar.cumulated_errror         += whereX.cumulated_errror - e.weight();

            Log.e(TAG, "slope so far = slopesofar * s = " + whereX.slope + " * " + s + " = " + calibrationSoFar.slope);
            Log.e(TAG, "intercept so far = intercept so far + slope so far * i = " +  whereX.intercept+ " + " + whereX.slope + "*"  +  i +" =" + calibrationSoFar.intercept);
            Log.e(TAG, "weighted err so far = err so far + slope so far * error = " +  whereX.weighted_cumulated_error + " + " + whereX.slope + "*"  +  err +" =" + calibrationSoFar.weighted_cumulated_error);
            Log.e(TAG, "err so far = err so far + error = " +  whereX.cumulated_errror + " - "+ e.weight() +  " = " + calibrationSoFar.cumulated_errror       );

            //continue the traversal providing the good calibration so far
            calibration2returnforOneBranch =  multi_hop_calibrate(src, shortestHyperpath, e.to(),  calibrationSoFar );


            overallCalibration2return.slope += calibration2returnforOneBranch.slope;
            overallCalibration2return.intercept += calibration2returnforOneBranch.intercept;
            overallCalibration2return.cumulated_errror += calibration2returnforOneBranch.cumulated_errror;
            overallCalibration2return.weighted_cumulated_error += calibration2returnforOneBranch.weighted_cumulated_error;
        }
        Log.e(TAG, "calibration 2 return at  "+where  + "= " + overallCalibration2return.toString());
        return  overallCalibration2return ;
    }




}
