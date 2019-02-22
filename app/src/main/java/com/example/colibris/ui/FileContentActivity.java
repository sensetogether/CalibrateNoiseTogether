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
import android.text.method.ScrollingMovementMethod;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.colibris.calib.FileManager;
import com.example.colibris.configuration.Configuration;
import com.example.colibris.configuration.Device;

/**
 * This FileContentActivity class permits to display the content of a file that
 * has been selected by the end user
 */
public class FileContentActivity extends AppCompatActivity {
    /**
     * text view
     */
    private TextView tv = null;

    /**
     * Activity creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        LinearLayout ll = new LinearLayout(this);
        ll.addView(tv);
        setContentView(ll);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String fileName = extras.getString(Configuration.FILE_MESSAGE);
            FileManager filemanager = new FileManager(fileName,true, this);
            tv.setText(filemanager.read_txt()  );
            filemanager.close();
        }
    }
}
