package com.example.colibris.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.os.Bundle;
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
 * PlotMessageActivity class refers to an activity used to plot measurements
 */
public class PlotMessageActivity extends Activity
{
    /**
     * plot
     */
    private XYPlot plot;
    /**
     * meeting parameters
     */
    private Meet meet;
    /**
     * log-related information
     */
    private static final String TAG = "PLOT";

    /**
     * the activity is created
     * @param savedInstanceState state of the instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot_message);
        // initialize our XYPlot reference:
        plot = findViewById(R.id.plot);

        //get the list of vertex id and if it is a file that starts with "cross"
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String vertexIdList = extras.getString(Configuration.VERTEX_MESSAGE); //  provide the list of vertex, e.g., 7-56-
            String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE); //determine if we should add  cross at the beginning of the file name

            Device device2meet;
            List<Device> deviceList = new ArrayList<Device>();
            int firstitem= 0;
            int begin = 0;
            while(vertexIdList.substring(begin).indexOf("-") >0 ){//while there is a vertex id to extract
                String  vertexId = vertexIdList.substring(begin, begin+ vertexIdList.substring(begin).indexOf("-") ); // extract the vertex id

                begin += vertexIdList.indexOf("-") +1;
                Integer i = new Integer(vertexId);

                device2meet  = new Device(i,device_id, -1);
                deviceList.add(device2meet);

                if(firstitem==0)
                    meet = new Meet(device2meet,this, true /*append */);
                else
                    meet.add(device2meet, this, true /*append*/);
            }


            Log.e(TAG, "PLOT " + vertexIdList);

            for (int i =0 ; i< deviceList.size(); i++){
                Log.e(TAG, "DEVICE IS " +  deviceList.get(i).getVertexId());
            }



            FileManager localfiletoplot, remotefiletoplot;

            List<FileManager> alltheremotefiletoplot = new ArrayList<FileManager>();
            // Handlers to the events to lead to the UI thread to the listener.

            if(device_id.startsWith("cross")){
                localfiletoplot = new FileManager("cross" + meet.getlocalFile4NoiseName(), true, this);
                for (int i = 0; i< deviceList.size(); i++ ){
                    alltheremotefiletoplot.add(new FileManager("cross" + meet.getRemoteFile4NoiseName(deviceList.get(i)) , true, this));
                }
//                 remotefiletoplot = new FileManager("cross" + meet.getRemoteFile4NoiseName(deviceList.get(0)) , true, this);
            }
            else {

                String fic = meet.getlocalFile4NoiseName();
                // take the prefix of the
                String local_file = new String (fic.substring(0, fic.indexOf("noise")+5)) ;

                for (int i = 0; i< deviceList.size(); i++ ){
                    local_file = local_file+ deviceList.get(i).getVertexId() + "-";
                }
                Log.e(TAG, "LOCAL FILE NAME IS " + local_file);

                localfiletoplot = new FileManager( local_file , true, this);


                for (int j =0 ; j< deviceList.size() ; j++){
                    String r_file = new String (fic.substring(0, fic.indexOf("local_noise"))) + "remote_noise" + deviceList.get(j).getVertexId() + "-";

                    for (int i = 0; i< deviceList.size(); i++ ){
                        if (i!= j){
                            r_file = r_file     + deviceList.get(i).getVertexId() + "-";
                        }
                    }
                    Log.e (TAG, "ADD FILE" + r_file);
                    alltheremotefiletoplot.add( new FileManager(r_file, true, this)  );

                }


                //  remotefiletoplot = new FileManager(meet.getRemoteFile4NoiseName(deviceList.get(0)) , true, this);
            }
            localfiletoplot.plot_several_calibration(plot, this, alltheremotefiletoplot);
        }








    }
}
/*
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        XYSeries series1 = generateScatter("series1", 80, new RectRegion(10, 50, 10, 50));
        XYSeries series2 = generateScatter("series2", 80, new RectRegion(30, 70, 30, 70));

        plot.setDomainBoundaries(0, 80, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, 80, BoundaryMode.FIXED);

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter();//this, R.xml.point_formatter);

        LineAndPointFormatter series2Format =
                new LineAndPointFormatter();
                      //  this, R.xml.point_formatter_2);

        // add each series to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        //plot.setLinesPerRangeLabel(3);
*/
        /*
        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
        Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels_2);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(
                new DashPathEffect(new float[] {

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
