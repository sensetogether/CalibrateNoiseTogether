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

import com.androidplot.xy.XYPlot;
import com.example.colibris.R;
import com.example.colibris.calib.FileManager;
import com.example.colibris.calib.sound.Record;

import java.nio.DoubleBuffer;

/**
 * The RegressActivity is an activity used to plot regression related information
 */
public class RegressActivity extends AppCompatActivity {
    /**
     * plot
     */
    private XYPlot plot;

    /**
     * activity creation
     * @param savedInstanceState state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regress);


        // initialize our XYPlot reference:
        plot = findViewById(R.id.plotregress);

        Log.e("Regress activity", "start");
        // create two files
        String localFileName4Noise = "tmp_local" ;
        FileManager localFile4Noise = new FileManager(localFileName4Noise, false /*append*/,this);

        String remoteFileName4Noise = "tmp_remote";
        FileManager remoteFile4Noise = new FileManager(remoteFileName4Noise, false  /*append*/,this);

        //write data in the local file
        DoubleBuffer localBuffer, remoteBuffer;
        int localBufferSize  = 10;
        localBuffer = DoubleBuffer.allocate(localBufferSize);
        remoteBuffer = DoubleBuffer.allocate(localBufferSize);
        int i;
        double pcmVal = 2;
        for (i=0; i<localBufferSize;i++) {
            localBuffer.put((double) i * pcmVal *1 );
            // localBuffer.put(0);
            remoteBuffer.put((double) i * pcmVal *10000);
        }

        //write data in the local file
        double slot = (double) 1000 / (double) Record._rate ; // compute the delay between two recording

        localFile4Noise.write_calibration_buffer(localBuffer,0, slot);
        remoteFile4Noise.write_calibration_buffer(remoteBuffer,0, slot);

        //    localFile4Noise.plot_both_calibration(plot, this,remoteFile4Noise);
        localFile4Noise.plot_calibration(plot,this,remoteFile4Noise, 1000);
        //get vertex id and device name

        // meet.localFile4Noise.plot_both_calibration(plot,this, meet.remoteFile4Noise);

    }

}

