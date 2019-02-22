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
package com.example.colibris.calib.sound;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The MeasurementsHelper permits to get sound related measurements
 * In particular, it permets to obtain the sound level over a period of time
 * that can be parametrized
 */
public class MeasurementsHelper {
    /**
     * log-related information
     */
    private static final String Tag = "MeasurementsHelper";

    public enum NoiseChangeType {
        NEW_VALUES, COMPLETED, CANCELLED
    }

    /**
     * averaged noise
     */
    private float avgNoise;
    /**
     * noise unit
     */
   private String noiseUnit;
    /**
     *
     */
    private long subwindow_sumPCM;
    /**
     * summed PCM
     */
    private long sumPCM;
    /**
     * parameter that is not used
     */
   private int anzPCM;
    /**
     * recording period
     */
    private long realRecordingTime;
    /**
     * time when we start recording
     */
    private Calendar startRecordingTime;
    /**
     * time when we stop recording
     */
    private Calendar endRecordingTime;
    /**
     * recording is complete
     */
    private boolean completed = false;
    /**
     * stop recording
     */
    private boolean stopped = true;
    /**
     * subwindowing values
     */
    private float[] subwindowing_values;
    /**
     * duration of the subwindowing
     */
    private double subwindowDuration;
    //It is mainly used if needed in the future to access each PCM value (not just averages),
    // e.g. for frequency weighting.
    /**
     * to be used in the furture
     */
    private ShortBuffer totalBuffer;

     /**
     * handler
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * audio record
     */
    private AudioRecord _recorder;
    /**
     * min buffer sizz
     */
    private int _minBufferSize;
    /**
     * mono stero
     */
    private int _channel = AudioFormat.CHANNEL_IN_MONO;
    /**
     * sound encoding format
     */
    private int _format = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * sampling frequency
     */
    private int _rate = 44100;
    /**
     * encoding
     */
    private int _source = MediaRecorder.AudioSource.MIC;


    //List of "to be supplied" with audio solution helpers, CopyOnWriteArrayList provides thread safety
    private List<MeasurementsHelper> _measurementsHelperList = new CopyOnWriteArrayList<>();
    /**
     * recording thread
     */
    private Thread _recordingThread;
    /**
     * used to synchronise
     */
    private Object stopSync = new Object(); //Used for syncing threads


    /**
     * Constructor
     *
     * @param sensingDuration sensing duration
     */
    public MeasurementsHelper(long sensingDuration) {
        Log.d(Tag, "sensingDuration=" + sensingDuration);
        int bufferSize = (int) (_rate * (sensingDuration / 1000f));
        Log.d(Tag, "bufferSize=" + bufferSize);
        totalBuffer = ShortBuffer.allocate(bufferSize);
    }

     /**
     * applies A-weighting to audio samples and returns weighted values
     *
     * @param samples: array of unweighted noise values
     * @return weightVals: array of A-weighted noise values
     */
    private double[] applyAWeightingFilter(double[] samples) {
        double[] weightedVals = new double[samples.length];
        double[] A = new double[]{1.0D, -4.0195761811158315D, 6.1894064429206921D, -4.4531989035441155D, 1.4208429496218764D, -0.14182547383030436D, 0.0043511772334950787D};
        double[] B = new double[]{0.2557411252042574D, -0.51148225040851436D, -0.25574112520425807D, 1.0229645008170318D, -0.25574112520425918D, -0.51148225040851414D, 0.25574112520425729D};

        for (int i = 0; i < samples.length; i++) {
            weightedVals[i] = convolutionSum(B, samples, i, 0) - convolutionSum(A, weightedVals, i, 1);
        }
        return weightedVals;
    }

    /**
     * return convolution sum
     * @param factors
     * @param vals
     * @param n
     * @param startIndex
     * @return convolution sum
     */
    public double convolutionSum(double[] factors, double[] vals, int n, int startIndex) {
        double result = 0;
        for (int k = startIndex; k < factors.length; k++) {
            if (n >= k) {
                //this should always work at least once for B sums (for n=0, k=0)
                result += factors[k] * vals[n - k];
            }
        }
        return result;
    }


    /**
     * Terminates current measurement.
     * If a minimum number of microphone-values ​​was achieved then the measurement was successful.
     */
    public void stop() {
        removeMeasurementsHelper(this);
        stopped = true;
    }

    /**
     * Starts measurement (new), resets variables and calls for new values to MeasurementsService
     *
     * @throws Exception
     */
    public void start() throws Exception {
        Log.d(Tag, "start recording");
        sumPCM = anzPCM = 0;
        completed = false;
        totalBuffer.clear();
        stopped = false;
        loadAudioSettings();
        subwindowDuration = 0.125;
        addMeasurementsHelper(this);
    }




    /**
     * add a measurement helper
     * @param helper measurement helper to add
     * @throws Exception cannot add it
     */
    private void addMeasurementsHelper(MeasurementsHelper helper) throws Exception {
        _measurementsHelperList.add(helper);
        if (_measurementsHelperList.size() != 0 && !isRecording()) {
            Log.d(Tag, "record");
            record();
        }
    }

    /**
     *determine whether the device records the sound
     * @return if the device is recording
     */
    private boolean isRecording() {
        return (_recordingThread != null && _recordingThread.isAlive());
    }

    /**
     * remove a measurement helper
     * @param helper measurement helper to remove
     */
    public void removeMeasurementsHelper(MeasurementsHelper helper) {
        _measurementsHelperList.remove(helper);
    }

    /**
     * Loads the specified audio settings
     */
    public void loadAudioSettings() {
        _minBufferSize = AudioRecord.getMinBufferSize(_rate, _channel, _format);
        if (_minBufferSize == AudioRecord.ERROR_BAD_VALUE || _minBufferSize == AudioRecord.ERROR) {
            Log.e(Tag, "Bad value error");
        }
    }

    /**
     * Set up the recorder
     *
     * @throws Exception
     */
    private void setUpRecorder() throws Exception {
        synchronized (stopSync) {
            if (_recorder == null || _recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                _recorder = new AudioRecord(_source, _rate, _channel, _format, _minBufferSize * 2);
                if (_recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    throw new Exception();
                }
            }
            if (_recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                _recorder.startRecording();
            }
        }
    }

    /**
     * Stop the recorder
     */
    private void stopRecorder() {
        synchronized (stopSync) {
            if (_recorder != null) {
                if (_recorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
                    _recorder.stop();
                }
                _recorder.release();
                _recorder = null;
            }
        }
    }

    /**
     * Performs collection and distribution of the audio
     *
     * @throws Exception
     */
    private void record() throws Exception {

        if (!isRecordingThreadAlive()) {
            // setup recorder prior to launching thread
            setUpRecorder();

            //Create a new thread only if it does not exist or has been terminated.
            _recordingThread = new Thread() {
                @Override
                public void run() {
                    recording();
                }
            };
            _recordingThread.start();
        }
    }

    /**
     * This Method queries the audio resources and distributes the data to all.
     * When no more data are needed, the audio resources are released again.
     * It Should not be called directly but only on new Threads!
     */
    private void recording() {
        startRecordingTime = Calendar.getInstance();

        short[] tempBuffer = new short[_minBufferSize];

        while (_measurementsHelperList.size() != 0) {
            if (_recorder == null) {
                break;
            }
            int anz = _recorder.read(tempBuffer, 0, _minBufferSize);
            if (anz > 0) {
                for (MeasurementsHelper helper : _measurementsHelperList) {
                    helper.newPCM(tempBuffer, anz);
                }
            } else if (anz < 0) {
                // read operation failed, exit loop and report problem to callback
                for (MeasurementsHelper helper : _measurementsHelperList) {
                    helper.recordingError();
                }
                break;
            }
        }
        // clear buffer memory
        tempBuffer = null;
        if (_measurementsHelperList.size() == 0) {
            stopRecorder();
        }
    }

    private void recordingError() {
    }

    private boolean isRecordingThreadAlive() {
        return (_recordingThread != null && _recordingThread.isAlive());
    }

    /**
     * Called by the microphone when new PCM data are available.
     *
     * @param pcm Array of values
     * @param anz The number of indexes to be used therein, from index 0
     */
    private void newPCM(short[] pcm, int anz) {
        //Log.d(Tag, "newPCM: anz=" + anz);

        // Determining the remaining required number of samples
        anz = Math.min(totalBuffer.remaining(), anz);
        totalBuffer.put(pcm, 0, anz);

        if (totalBuffer.remaining() == 0) {
            // If required number of values ​​has been reached, no further promoted by the service
            endRecordingTime = Calendar.getInstance();
            realRecordingTime = endRecordingTime.getTimeInMillis() - startRecordingTime.getTimeInMillis();

            // convert buffers to array
            short[] pcmBuffer = totalBuffer.array();
            // convert array of short to array of double
            double[] pcmValsInit = new double[pcmBuffer.length];
            for (int i = 0; i < pcmBuffer.length; i++) {
                pcmValsInit[i] = (double) pcmBuffer[i];
            }

            //apply A-weighting filter to PCM values
            double[] pcmVals = applyAWeightingFilter(pcmValsInit);
            sumPCM = 0;
            for (double pcmVal : pcmVals) {
                double squarePCM = pcmVal * pcmVal;
                sumPCM += squarePCM;
            }

            // Calculation of the (previous) overall average
            avgNoise = (float) Math.sqrt(sumPCM / (pcmVals.length * 1f));
            //todo replace
            // avgNoise = Noise.pcmToDB((float) Math.sqrt(sumPCM / (pcmVals.length * 1f)));

            // compute subwindows noise
            int n_samples_subwindow = (int) (_rate * subwindowDuration); //window size
            int window_nb = totalBuffer.capacity() / n_samples_subwindow; //windows number
            subwindowing_values = new float[window_nb]; // Array of windows noise values

            int windowsCounter = 0;
            //for each subBuffer in the final buffer list
            while (windowsCounter < window_nb) {
                int windowSizeCounter = 0;
                // for each sample in the current subBuffer
                while (windowSizeCounter < n_samples_subwindow) {
                    int index = n_samples_subwindow * windowsCounter + windowSizeCounter;
                    double pcmValue = pcmVals[index];
                    double windowSquarePCM = pcmValue * pcmValue;
                    subwindow_sumPCM += windowSquarePCM;
                    windowSizeCounter++;
                }
                subwindowing_values[windowsCounter] = (float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f));
                //todo replace
                //subwindowing_values[windowsCounter] = Noise.pcmToDB((float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f)));
                subwindow_sumPCM = 0;
                windowsCounter++;
            }
            // clear tmp buffers
            pcmVals = null;
            pcmValsInit = null;
            pcmBuffer = null;
            totalBuffer.clear();
            totalBuffer = null;

            removeMeasurementsHelper(this);
            //
            // noiseChanged(MeasurementsService.NoiseChangeType.COMPLETED);
        } else {
            //
            //         noiseChanged(MeasurementsService.NoiseChangeType.NEW_VALUES);
        }
    }

    /**
     * set up the recording and start recording
     * @throws Exception
     */
    public void startContinuous() throws Exception {
        sumPCM = anzPCM = 0;
        completed = false;
        totalBuffer.clear();
        stopped = false;
        loadAudioSettings();
        addMeasurementsHelperContinous(this);
    }

    /**
     * add a measurement helper to record the sound
     * @param helper measurement helper
     * @throws Exception
     */
    private void addMeasurementsHelperContinous(MeasurementsHelper helper) throws Exception {
        _measurementsHelperList.add(helper);
        if (_measurementsHelperList.size() != 0 && !isRecording()) {
            recordContinuous();
        }
    }

    /**
     * start the recording and configure it
     * @throws Exception
     */
    private void recordContinuous() throws Exception {

        if (!isRecordingThreadAlive()) {
            // setup recorder prior to launching thread
            setUpRecorder();

            //Create a new thread only if it does not exist or has been terminated.
            _recordingThread = new Thread() {
                @Override
                public void run() {
                    recordingContinous();
                }
            };
            _recordingThread.start();
        }
    }

    /**
     * sound recording using thepcm format
     */
    private void recordingContinous() {
        startRecordingTime = Calendar.getInstance();

        short[] tempBuffer = new short[_minBufferSize];

        while (_measurementsHelperList.size() != 0) {
            if (_recorder == null) {
                break;
            }
            int anz = _recorder.read(tempBuffer, 0, _minBufferSize);
            if (anz > 0) {
                for (MeasurementsHelper helper : _measurementsHelperList) {
                    helper.newPCMContinuous(tempBuffer, anz);
                }
            } else if (anz < 0) {
                // read operation failed, exit loop and report problem to callback
                for (MeasurementsHelper helper : _measurementsHelperList) {
                    helper.recordingError();
                }
                break;
            }
        }
        // clear buffer memory
        tempBuffer = null;
        if (_measurementsHelperList.size() == 0) {
            stopRecorder();
        }
    }

    /**
     *  record continuously the sound
     * @param pcm sound recorded using the pcm format
     * @param anz
     */
    private void newPCMContinuous(short[] pcm, int anz) {

        anz = Math.min(totalBuffer.remaining(), anz);
        totalBuffer.put(pcm, 0, anz);
        int subwindowIndex=0;
        if (totalBuffer.remaining() == 0) {
            // If required number of values ​​has been reached, no further promoted by the service
            endRecordingTime = Calendar.getInstance();
            realRecordingTime = endRecordingTime.getTimeInMillis() - startRecordingTime.getTimeInMillis();

            // convert buffers to array
            short[] pcmBuffer = totalBuffer.array();
            // convert array of short to array of double
            double[] pcmValsInit = new double[pcmBuffer.length];
            for (int i = 0; i < pcmBuffer.length; i++) {
                pcmValsInit[i] = (double) pcmBuffer[i];
            }

            //apply A-weighting filter to PCM values
            double[] pcmVals = applyAWeightingFilter(pcmValsInit);
            sumPCM = 0;
            for (int i = 0; i < pcmVals.length; i++) {
                double squarePCM = pcmVals[i] * pcmVals[i];
                sumPCM += squarePCM;
            }

            // Calculation of the (previous) overall average
            avgNoise = (float) Math.sqrt(sumPCM / (pcmVals.length * 1f));
             // avgNoise = Noise.pcmToDB((float) Math.sqrt(sumPCM / (pcmVals.length * 1f)));

            totalBuffer.clear();
             //noiseChanged(MeasurementsService.NoiseChangeType.COMPLETED);

        }
    }
}