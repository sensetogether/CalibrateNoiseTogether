package com.example.colibris.multi.graph;



import android.util.Log;

import com.example.colibris.configuration.Configuration;
import com.example.colibris.calib.Meeting;

/**
 *  The {@code DirectedEdge} class represents a weighted edge in an
 *  {@link EdgeWeightedDigraph}. Each edge consists of two integers
 *  (naming the two vertices) and a real-value weight. The data type
 *  provides methods for accessing the two endpoints of the directed edge and
 *  the weight.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/44sp">Section 4.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */

public class DirectedEdge {
    private final int v;
    private final int w;
    private double weight;
    public Meeting caracteristic ;

    /**
     * Initializes a directed edge from vertex {@code v} to vertex {@code w} with
     * the given {@code weight}.
     * @param v the tail vertex
     * @param w the head vertex
     * @param weight the weight of the directed edge
     * @throws IndexOutOfBoundsException if either {@code v} or {@code w}
     *    is a negative integer
     * @throws IllegalArgumentException if {@code weight} is {@code NaN}
     */

    public DirectedEdge(int v, int w, double weight) {
        if (v < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (w < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.weight = weight;
        this.caracteristic = new Meeting(-1,-1,-1,-1,-1, -1, -1, -1);
    }


    public double id(){
        return this.caracteristic().meetingStartTime;
    }

    public DirectedEdge(int v, int w, Meeting edge_caracteristics) {
        if (v < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (w < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.caracteristic = edge_caracteristics;

        if(Configuration.shortest_path_criteria == Configuration.STD_ERROR_CRITERIA)
            weight = edge_caracteristics.standard_error;
        if(Configuration.shortest_path_criteria == Configuration.MEAN_SQUARE_ERROR_CRITERIA){
            weight = edge_caracteristics.means_square_error;
            Log.e("edge", "weight is set to " + weight);
        }
        if(Configuration.shortest_path_criteria == Configuration.MEETING_DURATION_CRITERIA)
            weight = edge_caracteristics.meetingDuration;
        if(Configuration.shortest_path_criteria == Configuration.MEETING_RSQUARED)
            weight = edge_caracteristics.Rsquared;
        if(Configuration.shortest_path_criteria == Configuration.MEETING_RESIDUAL_SUM_OF_SQUARRE)
            weight =  edge_caracteristics.ResidualSumOfSquares;


    }





    public DirectedEdge(int v, int w, double weight, Meeting edge_caracteristics) {
        if (v < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (w < 0) throw new IndexOutOfBoundsException("Vertex names must be nonnegative integers");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.weight = weight;
        this.caracteristic = edge_caracteristics;
    }

    /**
     * Returns the tail vertex of the directed edge.
     * @return the tail vertex of the directed edge
     */
    public int from() {
        return v;
    }

    /**
     * Returns the head vertex of the directed edge.
     * @return the head vertex of the directed edge
     */
    public int to() {
        return w;
    }

    /**
     * Returns the weight of the directed edge.
     * @return the weight of the directed edge
     */
    public double weight() {
        return weight;
    }

    public void setweight(double aweight){ this.weight = aweight; }

    public void setEdge_caracteristic(Meeting edge_caracteristic){
        this.caracteristic = edge_caracteristic;
    }

    public Meeting caracteristic(){return this.caracteristic;}

    /**
     * Returns a string representation of the directed edge.
     * @return a string representation of the directed edge
     */
    public String toString() {
        return v + "->" + w + " " + weight + " " + caracteristic.toString();
    }


    public DirectedEdge reverse(){

        return new DirectedEdge(this.to(), this.from(), this.weight(), this.caracteristic());
    }


}