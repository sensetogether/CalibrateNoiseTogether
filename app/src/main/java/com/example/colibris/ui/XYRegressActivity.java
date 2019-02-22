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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;
import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.Meet;
import com.example.colibris.calib.regression.MultiRegression;

import java.util.ArrayList;
import java.util.List;

/**
 * XYRegressActivity class is an activity used to plot regeression related information
 */
public class XYRegressActivity extends AppCompatActivity {
    /**
     * textview
     */
    private TextView text ;
    /**
     * meeting related information
     */
    private Meet meet;

    /**
     * activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xyregress);
        text =  new TextView(this);
        text = findViewById(R.id.xyregress_message);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean canRegress = true;
            String vertexIdList = extras.getString(Configuration.VERTEX_MESSAGE); //  provide the list of vertex, e.g., 7-56-
            String device_id = extras.getString(Configuration.DEVICE_ID_MESSAGE); //determine if we should add  cross at the beginning of the file name
            Device device2meet;
            List<Device> deviceList = new ArrayList<Device>();
            List<FileManager> fileList = new ArrayList<FileManager>();
            int firstitem= 0;
            int begin = 0;
            while(vertexIdList.substring(begin).indexOf("-") >0 ){//while there is a vertexid to extract
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

            FileManager localfiletoplot;
            if(device_id.startsWith("cross")){
                localfiletoplot = new FileManager("cross" + meet.getlocalFile4NoiseName(), true, this);
                FileManager  remotefiletoplot = new FileManager("cross" + meet.getRemoteFile4NoiseName(deviceList.get(0)) , true, this);
                //  if the file is empty we cannot regress
                if( remotefiletoplot.getFileSize() == 0){
                    canRegress = false;
                }
            }
            else {
                localfiletoplot = new FileManager( meet.getlocalFile4NoiseName(), true, this);
                if( localfiletoplot.getFileSize() == 0){
                    canRegress = false;
                }
            }

            if (canRegress == true){
                Log.e("XYRegress", "we can regress");
                for (int i = 0; i< meet.meetwithS.size(); i++){
                    fileList.add( meet.files4noise.get(1+i) );
                    //todo check if the file is empty
                }

                MultiRegression multiRegress = new MultiRegression(localfiletoplot, fileList);
                String toprint = multiRegress.toString();
                text.append("\n********Multiple Regression:\n" + toprint);
                //todo print the robust multiple regression
            }

            //            Simpleregression simpleregression = new Simpleregression(meet.getLocalFileManager4Noise(),  meet.files4noise.get(meet.getPositionfromVertex(device2meet.getVertexId())) /*meet.remoteFile4Noise*/);
  /*          text.append("**** Simple Regression: ");
            text.append("\nSLOPE: " + simpleregression.slope +"\nintercept" + simpleregression.intercept +
                    "\nmeans sqr err: " + simpleregression.means_square_error +
                    "\nR:" + simpleregression.R );

            text.append(                    "\nRsquare: " + simpleregression.RSquare +
                    "\nslope std err: " + simpleregression.slope_standard_error +
                    "\nsignificance: " + simpleregression.significance +
                    "\nslope confidence interval:" + simpleregression.slopeConfidenceInterval +
                    "\nslope standard error: " + simpleregression.slopeStdErr +
                    "\nsum of cross products:" + simpleregression.sumOfCrossProducts +
                    "\nsum squared errors: " + simpleregression.sumSquaredErrors +
                    "\nsum square: " + simpleregression.sumSquarre +
                    "\ntotal sum square: " + simpleregression.totalSumSquares +
                    "\nxsumsqr: " + simpleregression.xSumSquares);*/


/*            DoubleBuffer localSound = meet.localFile4Noise.getSound();
            DoubleBuffer remoteSound = meet.remoteFile4Noise.getSound();
            Filter filter = new Filter(localSound, remoteSound);
            localSound.clear();
            remoteSound.clear();
            text.append("\n\noffset: " + filter.offset + "\n normalised offset:" + filter.norm_offset);
*/
            /*
           simpleregression.findBestRegression();
            simpleregression.buildRLSRegression();
            text.append("**** Robust Simple Regression:\n ");
            text.append("SLOPE: " + simpleregression.cleanedRegression.slope);
            text.append("\nintercept" + simpleregression.cleanedRegression.intercept +
                    "\nmeans sqr err: " + simpleregression.cleanedRegression.means_square_error +
                    "\nR:" + simpleregression.cleanedRegression.R +
                    "\nRsquare: " + simpleregression.cleanedRegression.RSquare +
                    "\nslope std err: " + simpleregression.cleanedRegression.slope_standard_error +
                    "\nsignificance: " + simpleregression.cleanedRegression.significance +
                    "\nslope confidence interval:" + simpleregression.cleanedRegression.slopeConfidenceInterval +
                    "\nslope standard error: " + simpleregression.cleanedRegression.slopeStdErr +
                    "\nsum of cross products:" + simpleregression.cleanedRegression.sumOfCrossProducts +
                    "\nsum squared errors: " + simpleregression.cleanedRegression.sumSquaredErrors +
                    "\nsum square: " + simpleregression.cleanedRegression.sumSquarre +
                    "\ntotal sum square: " + simpleregression.cleanedRegression.totalSumSquares +
                    "\nxsumsqr: " + simpleregression.cleanedRegression.xSumSquares +
                    "\ntotal sum square: " + simpleregression.cleanedRegression.totalSumSquares +
                    "\nxsumsqr: " + simpleregression.cleanedRegression.xSumSquares);*/

        }
    }
}

//////////



