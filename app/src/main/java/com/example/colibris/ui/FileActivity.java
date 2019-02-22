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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.colibris.R;

import java.io.File;

import static com.example.colibris.ui.FileList.*;

import java.io.File;


/**
 * The FileActivity class permits to plot some data that are related to
 * the calibrations that took place. In particular, the files generated
 * by a calibration can be selected and the related information can be ploted
 */


public class FileActivity extends AppCompatActivity {
    public static final String TAG = "file activity";
    /**
     * radio plot by default
     */
    public static final int RADIO_PLOT =0;
    /**
     * radio plot option 1
     */
    public static final int RADIO_XYPLOT = 1;
    /**
     * radio plot option 2
     */
    public static final int RADIO_FILE = 2;
    /**
     * radio plot option 3
     */
    public static final int RADIO_XY_REGRESS =3;
    /**
     * radio plot option 4
     */
    public static final int RADIO_REGRESS =4;
    /**
     * radio plot option 5
     */
    public static  final int RADIO_SEE_REGRESS = 5;
    /**
     * radio plot option 6
     */
    public static final  int RADIO_SEE_CORR = 6;
    /**
     * radio plot option7
     */
    public int last_radio = RADIO_PLOT;
    /**
     * fragment
     */
    private FileList fragment;


    /**
     * Activity creation
     * @param savedInstanceState state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        fragment = (FileList) getFragmentManager().findFragmentById(R.id.fragment1);
        fragment.setContext(this);
    }

    /**
     * activity resume
     */
    protected void onResume(){
        super.onResume();
        fragment.setRadioButton(last_radio);
        RadioGroup rg;
        rg = findViewById(R.id.RG);

        switch (rg.getCheckedRadioButtonId()) {
            case R.id.radio_plot:
                fragment.setRadioButton(RADIO_PLOT);
                this.last_radio = RADIO_PLOT;
                break;
            case R.id.radio_xyplot:
                fragment.setRadioButton(RADIO_XYPLOT);
                this.last_radio = RADIO_XYPLOT;
                break;
            case R.id.radio_file:
                fragment.setRadioButton(RADIO_FILE);
                this.last_radio = RADIO_FILE;

                break;
            case R.id.radio_regress:
                fragment.setRadioButton(RADIO_REGRESS);
                this.last_radio = RADIO_REGRESS;
                break;
            case R.id.radio_see_regress:
                fragment.setRadioButton(RADIO_SEE_REGRESS);
                this.last_radio = RADIO_SEE_REGRESS;
                break;
            case R.id.radio_xyregress:
                fragment.setRadioButton(RADIO_XY_REGRESS);
                this.last_radio = RADIO_XY_REGRESS;
                break;
            case R.id.radio_see_corr:
                fragment.setRadioButton(RADIO_SEE_CORR);
                this.last_radio = RADIO_SEE_CORR;
                break;
        }
    }

    /**
     * Display the files in the directory
     */
    private void displayFiles(){
        FileList fragment = (FileList) getFragmentManager().findFragmentById(R.id.fragment1);

        if (fragment!=null) {
            ArrayAdapter adapter = ((ArrayAdapter) fragment
                    .getListAdapter());
            //display the file that are contained in the directory of the application
            File file = getFilesDir();
            Log.d(TAG, "Application path:  " + file.getAbsolutePath() + "\n");
            File[] files = file.listFiles();

            Log.d(TAG, "Files" + "Size: " + files.length + "\n"); // nb of files in the directory
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "FileName:" + files[i].getName() + "\n");
                adapter.add("files[i].getName()");
            }
        }
    }

    /**
     * Click a radio button
     * @param view view
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_plot:
                if (checked){
                    fragment.setRadioButton(RADIO_PLOT);
                    this.last_radio = RADIO_PLOT;
                }
                break;
            case R.id.radio_xyplot:
                if (checked){
                    fragment.setRadioButton(RADIO_XYPLOT);
                    this.last_radio = RADIO_XYPLOT;
                }
                break;
            case R.id.radio_file:
                if (checked){
                    fragment.setRadioButton(RADIO_FILE);
                    this.last_radio = RADIO_FILE;
                }

                break;
            case R.id.radio_regress:
                if (checked){
                    fragment.setRadioButton(RADIO_REGRESS);
                    this.last_radio = RADIO_REGRESS;
                }
                break;

            case R.id.radio_see_regress:
                if (checked) {
                    fragment.setRadioButton(RADIO_SEE_REGRESS);
                    this.last_radio = RADIO_SEE_REGRESS;
                }
                break;

            case R.id.radio_xyregress:
                if (checked) {
                    fragment.setRadioButton(RADIO_XY_REGRESS);
                    this.last_radio = RADIO_XYPLOT;
                }
                break;

            case R.id.radio_see_corr:
                if (checked) {
                    fragment.setRadioButton(RADIO_SEE_CORR);
                    this.last_radio = RADIO_SEE_CORR;
                }
                break;
        }
    }
}
