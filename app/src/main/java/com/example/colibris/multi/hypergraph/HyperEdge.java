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

import android.support.annotation.NonNull;

import com.example.colibris.multi.graph.DirectedEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * An (directed) Hyperedge correspond to a set of inneredges
 * All theses ineredges  have the same source
 *
 */

public class HyperEdge implements Comparable<HyperEdge> {
    //
    public static final String TAG = "hyperedge";
    // and
    /**
     * an hyperedge is caracterised by a src and a set of dest and an id
     * hyperedge is a set of labeled edges having the same label and same source
     */
    List<DirectedEdge> innerEdges ;

    /**
     * Create an empty hyperedge
     */
    public  HyperEdge(){
        innerEdges =  new ArrayList<DirectedEdge>();
    }

    /**
     * create a hyper edge with the given inner edges
     * @param _innerEdges set of inner edges that compose the hyperedge
     */
    public  HyperEdge(List<DirectedEdge> _innerEdges){
        this.innerEdges =  _innerEdges;
    }
    /*

     */

    /**
     * return if an hyper edges is valid (i.e.
     *
     * @return return if all the src and all the id of the inner edges are the same (true) or not (false))
     */
    public boolean is_valid(){
        if (this.innerEdges.size() == 0)
            return false;

        // chech if we have the same src and the same id for all the inneredges
        //constituing the hyperedge
        int src =0;
        double id =0;

        // check if all the src and all the id of the inner edges are the same
        for (int i=0; i< this.innerEdges.size(); i++){
            if (i==0){
                src = this.innerEdges.get(i).from();
                id = this.innerEdges.get(i).caracteristic().meetingStartTime;
            }
            if (src != this.innerEdges.get(i).from() || id != this.innerEdges.get(i).caracteristic().meetingStartTime)
                return false;
        }

        return true;
    }


    /**
     * determine wheter a given edge belongs to the hyperedge
     * @param e edge
     * @return if the given edge belongs to the hyperedge
     */
     public boolean isIn (DirectedEdge e){
        for (int i=0; i< this.innerEdges.size(); i++) {
            if (this.innerEdges.get(i).from() == e.from()&& this.innerEdges.get(i).to() == e.to() )
                return true;
        }

        return false;
    }


    /**
     * return the weight of the hyper edge
     * @return hyperedge weight
     */
    public double weight(){
        double weight2return=0;
        for (int i=0; i< innerEdges.size(); i++){
            weight2return+= innerEdges.get(i).weight();
        }
        return       weight2return;
    }

    /**
     * returns the source of the hyperedge
     * @return source of the hyperedge
     */
    public int from(){
        if(innerEdges.size() ==0)
            return -1;
        //else
        return innerEdges.get(0).from();
    }

    /**
     * returns the id of the hyperedge (that corresponds
     * to the date when was created the hyperedge)
     * @return hyperedge id
     */
    public double id(){
        if(innerEdges.size() ==0)
            return -1;
        return innerEdges.get(0).caracteristic().meetingStartTime;
    }

    /**
     * add a inner edge to the hyperedge
     * @param innerEdge inner edge that should be added
     */
    public void add(DirectedEdge innerEdge){
        this.innerEdges.add(innerEdge);
    }

    /**
     * extract the inner edges that compose the hyper edge
     * @return inner edges
     */
    public  List<DirectedEdge> getInnerEdges(){
        return this.innerEdges;
    }

    /**
     * return a string with the caracteristics of the hyper egde
     * @return string displaying the properties of the hyperedge
     */
    public String toString(){
        String output = new String("Hyperedge starting at " + this.from() + " with inneredges: ");

        for (int j=0; j< this.innerEdges.size(); j++){
            output =  output.concat(this.innerEdges.get(j).toString());
        }
        return output;
    }


    /**
     * Compare the weight of the  given hyper egde
     * @param hyperEdge hyper edge to compare
     * @return 0 if the two hyper edges have the same weight , 1 if the given hyper edge has a higher weight, -1 otherwise
     */
    @Override
    public int compareTo(@NonNull HyperEdge hyperEdge) {
        if (this.weight() == hyperEdge.weight())
            return 0;

        if (this.weight() > hyperEdge.weight())
            return 1;

        return -1 ;
//return  0 if equal ; < 0 if this Integer is numerically less than the argument Integer; and a value greater than 0 if this Integer is numerically greater than the argument Integer (signed comparison).
    }
}
