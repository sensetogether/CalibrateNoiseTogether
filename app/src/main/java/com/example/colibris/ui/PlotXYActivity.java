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
package com.example.colibris.ui;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidplot.xy.*;
import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.Meet;

import java.util.ArrayList;
import java.util.List;

/**
 * The PlotXYActivity class is an activity used to plot information
 */
public class PlotXYActivity extends AppCompatActivity {
    /**
     * plot
     */
    private XYPlot plot;
    /**
     * meeting related parameters
     */
    Meet meet ;

    /**
     * creation of the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot_xy);
        plot = findViewById(R.id.plotxy); // initialize our XYPlot reference:
        Log.e("XY activity", "Creation");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String vertexIdList = extras.getString(Configuration.VERTEX_MESSAGE); //  provide the list of vertex, e.g., 7-56-
            String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE); //determine if we should add  cross at the beginning of the file name

            Device device2meet;
            List<Device> deviceList = new ArrayList<Device>();
            int firstitem= 0;
            int begin = 0;
            Log.e("XY activity", "HI");
            while(vertexIdList.substring(begin).indexOf("-") >0 ){//while there is a vertexid to extract
                String  vertexId = vertexIdList.substring(begin, begin+ vertexIdList.substring(begin).indexOf("-") ); // extract the vertex id
                begin += vertexIdList.indexOf("-") +1;
                Integer i = new Integer(vertexId);
                device2meet  = new Device(i,device_id, -1);
                deviceList.add(device2meet);

                if(firstitem==0)
                    meet = new Meet(device2meet,this, true  );
                else
                    meet.add(device2meet, this, true  );
            }
            FileManager localfiletoplot, remotefiletoplot;
            if(device_id.startsWith("cross")){
                localfiletoplot = new FileManager("cross" + meet.getlocalFile4NoiseName(), true, this);
                remotefiletoplot = new FileManager("cross" + meet.getRemoteFile4NoiseName(deviceList.get(0)) , true, this);
                localfiletoplot.plot_calibration(plot, this, remotefiletoplot, Integer.MAX_VALUE);
            }
            else {

                ////////////////////////////////////
                String fic = meet.getlocalFile4NoiseName(); // take the prefix of the file
                String local_file = new String (fic.substring(0, fic.indexOf("noise")+5)) ;//take the prefix

                for (int i = 0; i< deviceList.size(); i++ ){//add to the prefix the list of the name of the device that meet
                    local_file = local_file+ deviceList.get(i).getVertexId() + "-";
                }
                Log.e("XY activity", "LOCAL FILE NAME IS " + local_file);

                localfiletoplot = new FileManager( local_file , true, this);


                for (int j =0 ; j< deviceList.size() ; j++){
                    String r_file = new String (fic.substring(0, fic.indexOf("local_noise"))) + "remote_noise" + deviceList.get(j).getVertexId() + "-";

                    for (int i = 0; i< deviceList.size(); i++ ){
                        if (i!= j){
                            r_file = r_file     + deviceList.get(i).getVertexId() + "-";
                        }
                    }
//                    Log.e (TAG, "ADD FILE" + r_file);
                    //                  alltheremotefiletoplot.add( new FileManager(r_file, true, this)  );
                    remotefiletoplot = new FileManager(r_file, true, this) ;
                    localfiletoplot.plot_calibration(plot, this, remotefiletoplot, Integer.MAX_VALUE);

                }

                ///////////////////////////////////

                //localfiletoplot = new FileManager( meet.getlocalFile4NoiseName(), true, this);
                //  remotefiletoplot = new FileManager( meet.getRemoteFile4NoiseName(deviceList.get(0)) , true, this);
                // localfiletoplot.plot_calibration(plot,this ,meet.files4noise.get(1+meet.getPositionfromVertex(deviceList.get(0).getVertexId())), Integer.MAX_VALUE);
            }

        }
    }
}

//////////////////////

// time series
/*
        // create a couple arrays of y-values to plot:
        final Number[] domainLabels = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14};
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
        Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
       // R.xml.line_point_formatter_with_labels

        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(this,
                R.xml.line_point_formatter_with_labels);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(this,
                R.xml.line_point_formatter_with_labels_2);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {

                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series2Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);


        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);

  */

