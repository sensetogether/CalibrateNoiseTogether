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
package com.example.colibris.comtool;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Date;


/**
 * This Orientation class permits to obtain the device orientation
 */
public class Orientation{
    /**
     * context
     */
    private Context ctx; // for   purpose
    /**
     * sensor call back
     */
    private SensorCallBack callback;
    /**
     * log information
     */
    private static final String TAG = "Orientation"; // for debugging purpose
    /**
     * sensor manager
     */
    private SensorManager mSensorManager;
    /**
     * accelerometer manager
     */
    private Sensor mAccelerometer;
    /**
     * gyro manager
     */
    private Sensor mGyroSensor;
    /**
     * mag manager
     */
    private Sensor mMagSensor;
    /**
     * used to convert  nanoseconds to milliseconds
     */
     private static final float NS2S = 1.0f / 1000000.0f;

    /**
     * init the orientation sensor
     * @param ctx context
     * @param callback callback
     */
    public Orientation(Context ctx, SensorCallBack callback) {
        Log.d(TAG, "Orientation begins ctx");
        this.ctx = ctx;
        this.callback = callback;
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // mWindowManager = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG, "Orientation ends ctx");
    }

    /**
     * init the orientation listener
     */
    final SensorEventListener mGyroListener = new SensorEventListener() {

        //public SensorEventListener callback;
        private static final float MIN_TIME_STEP = (1f / 40f);
        private long mLastTime = System.currentTimeMillis();
        private float mRotationX, mRotationY, mRotationZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float angularVelocity = z * 0.96f; // Minor adjustment to avoid drift on Nexus S

            // Calculate time diff
            long now = System.currentTimeMillis();
            float timeDiff = (now - mLastTime) / 1000f;
            mLastTime = now;
            if (timeDiff > 1) {
                // Make sure we don't go bananas after pause/resume
                timeDiff = MIN_TIME_STEP;
            }

            mRotationX += x * timeDiff;
            if (mRotationX > 0.5f)
                mRotationX = 0.5f;
            else if (mRotationX < -0.5f)
                mRotationX = -0.5f;

            mRotationY += y * timeDiff;
            if (mRotationY > 0.5f)
                mRotationY = 0.5f;
            else if (mRotationY < -0.5f)
                mRotationY = -0.5f;

            mRotationZ += angularVelocity * timeDiff;

            Date nowDate = new Date();
            long timeOrt = nowDate.getTime();
            callback.onSensorOrientationChanged(mRotationX, mRotationY, mRotationZ, timeOrt);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.d(TAG, "AccuracyChanged running");
        }

    };


     /**
     * we save orientation
     */
    final SensorEventListener mAccListener = new SensorEventListener() {
        @Override
        /**
         * accuracy changed
         */
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        /**
         * orientation changed
         */
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            // float z = values[2];

            // Ignoring orientation since the activity is using screenOrientation "nosensor"
        }
    };


    /**
     * magnetic field is changed
     */
    final SensorEventListener mMagListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        /**
         * sensor changed
         */
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            // float z = values[2];

        }
    };

    /**
     * start to store information
     */
    public void start() {
        Log.d(TAG, "Start is running");
        mSensorManager.registerListener(mAccListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mGyroListener, mGyroSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mMagListener, mMagSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * stop trasking orientation
     */
    public void stop() {
        mSensorManager.unregisterListener(mAccListener, mAccelerometer);
        mSensorManager.unregisterListener(mGyroListener, mGyroSensor);
        mSensorManager.unregisterListener(mMagListener, mMagSensor);
    }

/*
    public void onSensorChanged(SensorEvent event) {

        //locationManager = (android.location.LocationManager)
        //      ctx.getSystemService(android.content.Context.LOCATION_SERVICE);

        // Log.d(TAG, "SensorChanged running");
        //testOrientation.onSensorChanged(eventOrientation);
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.

        //Date now = new Date();// get local time
        //long offset =  now.getTime() - event ; // compute the time offset
        //Log.e("Location", "Location changed offset: " + now.getTime() + "-"  + location.getTime() + "= " + offset);
        //String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());



        if ((timestamp != 0)) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            // Axis of the rotation sample, not normalized yet.
            //  Log.d(TAG, "dt Value: " + dT);
            // orientationTime.LocalNetworkTime = now.getTime();
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            Log.d(TAG, "azimuth: " + axisX + "pitch: " + axisY + "roll: " + axisZ);

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            int EPSILON = 1;
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
            Date now = new Date();
            long timeOrt = now.getTime();
            this.callback.onSensorOrientationChanged(deltaRotationVector[0], deltaRotationVector[1], deltaRotationVector[2], timeOrt);
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;

        ;
    }

   /* private DoubleBuffer getOrientationInterval(Double interval)
    {

        return orientationInterval;
    }*/

    /**
     * call back
     */


    public interface SensorCallBack {
        void onSensorOrientationChanged(float axisX, float axisY, float axisZ, long timestamp);
    }
}
