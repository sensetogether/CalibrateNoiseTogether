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

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.colibris.R;
import com.example.colibris.configuration.Configuration;

import java.io.File;
import java.util.ArrayList;
import static com.example.colibris.ui.FileActivity.*;
import static com.example.colibris.ui.FileActivity.RADIO_FILE;
import static com.example.colibris.ui.FileActivity.RADIO_PLOT;
import static com.example.colibris.ui.FileActivity.RADIO_REGRESS;
import static com.example.colibris.ui.FileActivity.RADIO_XYPLOT;
import static com.example.colibris.configuration.Configuration.DEVICE_ID_MESSAGE;
import static com.example.colibris.configuration.Configuration.FILE_MESSAGE;
import static com.example.colibris.configuration.Configuration.VERTEX_MESSAGE;

import com.example.colibris.R;

/**
 * The FileList class refers to an activity used to display to the end user some calibration related files
 */
public class FileList   extends ListFragment implements OnItemClickListener {
    /**
     * log related information
     */
    public static final String TAG = "filelist";
    /**
     * context
     */
    public Context ctx;
    /**
     * radio button options
     */
    int radio_button;

    /**
     * init the context
     * @param ctx context
     */
    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * set the radio button
     * @param radioButton radio button
     */
    public void setRadioButton(int radioButton) {
        radio_button = radioButton;
    }

    /**
     * View creation
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        return view;
    }

    /**
     * Activity creation
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Construct the data source
        ArrayList<String> arrayOfFiles = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, arrayOfFiles);

        File file = new File(Environment.getExternalStorageDirectory() + "/Documents");
        //   File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString());

        Log.d(TAG, "Application path:  " + file.getAbsolutePath() + "\n");
        File[] files = file.listFiles();

        Log.d(TAG, "Files" + "Size: " + files.length + "\n"); // nb of files in the directory
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "FileName:" + files[i].getName() + "\n");
            //do not print on the screen the file corresponding to the connexion and the file called instant-run
            if (files[i].getName().compareTo(Configuration.connectFileName) == 0 || files[i].getName().compareTo("instant-run") == 0) {
            } else {
                adapter.add(files[i].getName());
                adapter.notifyDataSetChanged();
            }
        }

        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    /**
     * deal with the various options of radio
     * @param parent parent view
     * @param view view
     * @param position position
     * @param id id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = ((TextView) view).getText().toString();
        // Toast.makeText(getActivity(), "Item: " + item +" , " +position, Toast.LENGTH_SHORT).show();
        //extract the device id for the filename
        String vertex_id_string = "";
        String prefix = "";
        if (item.startsWith(Configuration.LOCAL_FILE_PREFIX) == true)
            vertex_id_string = item.substring(Configuration.LOCAL_FILE_PREFIX.length());
        if (item.startsWith(Configuration.REMOTE_FILE_PREFIX) == true)
            vertex_id_string = item.substring(Configuration.REMOTE_FILE_PREFIX.length());
        if (item.startsWith(Configuration.AVG_LOCAL_FILE_PREFIX) == true)
            vertex_id_string = item.substring(Configuration.AVG_LOCAL_FILE_PREFIX.length());
        if (item.startsWith(Configuration.AVG_REMOTE_FILE_PREFIX) == true)
            vertex_id_string = item.substring(Configuration.AVG_REMOTE_FILE_PREFIX.length());

        if (item.startsWith("cross" + Configuration.LOCAL_FILE_PREFIX) == true) {
            vertex_id_string = item.substring("cross".length() + Configuration.LOCAL_FILE_PREFIX.length());
            prefix = "cross";
        }
        if (item.startsWith("cross" + Configuration.REMOTE_FILE_PREFIX) == true) {
            vertex_id_string = item.substring("cross".length() + Configuration.REMOTE_FILE_PREFIX.length());
            prefix = "cross";
        }
        if (item.startsWith("cross" + Configuration.AVG_LOCAL_FILE_PREFIX) == true) {
            vertex_id_string = item.substring("cross".length() + Configuration.AVG_LOCAL_FILE_PREFIX.length());
            prefix = "cross";
        }
        if (item.startsWith("cross" + Configuration.AVG_REMOTE_FILE_PREFIX) == true) {
            vertex_id_string = item.substring("cross".length() + Configuration.AVG_REMOTE_FILE_PREFIX.length());
            prefix = "cross";
        }

        switch (radio_button) {
            case RADIO_PLOT:
                this.plotMessage(vertex_id_string, prefix);
                break;
            case RADIO_XYPLOT:
                this.popMessage(vertex_id_string, prefix);
                break;
            case RADIO_FILE:// print the file content
                this.fileMessage(item);
                break;
            case RADIO_XY_REGRESS: // display textual information concerning the regression
                this.xyRegress(vertex_id_string, prefix);
                break;
            case RADIO_REGRESS:
                this.regressMessage();
                break;
            case RADIO_SEE_REGRESS: // print information concerning regression
                this.seeRegress(vertex_id_string, prefix);
                break;
            case RADIO_SEE_CORR: //calibrate

                this.calibrate(vertex_id_string, prefix);


                //  this.xyRegress(vertex_id_string, device_name);
                //   Intent intent = new Intent(ctx, CalibrateActivity.class);
                //  startActivity(intent);
                break;
        }
    }

    /**
     * click on plot
     * @param vertex_id vertex id
     * @param prefix prefix
     */
    public void plotMessage(String vertex_id, String prefix) {
        Intent intent = new Intent(ctx, PlotMessageActivity.class);
        intent.putExtra(VERTEX_MESSAGE, vertex_id);
        intent.putExtra(DEVICE_ID_MESSAGE, prefix);
        startActivity(intent);
    }

    /**
     * click on one option
     * @param vertex_id vertex id
     * @param prefix prefix
     */
    public void popMessage(String vertex_id, String prefix) {
        Intent intent = new Intent(ctx, PlotXYActivity.class);
        intent.putExtra(VERTEX_MESSAGE, vertex_id);
        intent.putExtra(DEVICE_ID_MESSAGE, prefix);
        startActivity(intent);
    }

    //

    /**
     * display the content of the file in text format
     * @param fileName file name
     */
    public void fileMessage(String fileName) {
        Intent intent = new Intent(ctx, FileContentActivity.class);
        intent.putExtra(FILE_MESSAGE, fileName);
        startActivity(intent);
    }

    /**
     * Click on regression option
     * @param vertex_id
     * @param prefix
     */
    public void xyRegress(String vertex_id, String prefix) {
        Intent intent = new Intent(ctx, XYRegressActivity.class);
        intent.putExtra(VERTEX_MESSAGE, vertex_id);
        intent.putExtra(DEVICE_ID_MESSAGE, prefix);
        startActivity(intent);
    }

    /**
     * click to see regression options
     * @param vertex_id veretex id
     * @param prefix prefix
     */
    public void seeRegress(String vertex_id, String prefix) {
        Intent intent = new Intent(ctx, SeeRegressActivity.class);
        intent.putExtra(VERTEX_MESSAGE, vertex_id);
        intent.putExtra(DEVICE_ID_MESSAGE, prefix);
        startActivity(intent);
    }

    /**
     * click to see calibration option
     * @param vertex_id vertex id
     * @param prefix prefix
     */
    public void calibrate(String vertex_id, String prefix) {
        Intent intent = new Intent(ctx, CalibrateActivity.class);
        intent.putExtra(VERTEX_MESSAGE, vertex_id);
        intent.putExtra(DEVICE_ID_MESSAGE, prefix);
        startActivity(intent);
    }

    /**
     * click
     */
    public void regressMessage() {
        Intent intent = new Intent(ctx, RegressActivity.class);
        startActivity(intent);
    }
}