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

import com.example.colibris.configuration.Configuration;

import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This Record class is used to record sound
 */
public class Record {
    /**
     * log related information
     */
    private static final String Tag = "Record";
    /**
     * recording state
     */
    public static  final int STILL_ADDING =0;
    /**
     * recording state
     */

    public static final int HAS_COMPLETED = 1 ;
    /**
     * recording state
     */

    public static final int HAS_CANCELLED = 2;

    /**
     * average noise obtained during the recording period
     */
    private float avgNoise;
    /**
     * avg rw noise
     */
    private float avgrawNoise;
    /**
     * accumulated sound
     */
    private long subwindow_sumPCM;
    /**
     * raw accumulated sound
     */
    private long subwindow_sumRAW;
    /**
     * summed pcm
     */
    private long sumPCM;
    /**
     * summed raw pcm
     */
    private long sumrawPCM;
    /**
     * anz pcm
     */
    private int anzPCM;
    private long realRecordingTime;
    /**
     *
     */
    public long startRecord;
    /**
     * time at which the recording is started
     */
    public Calendar startRecordingTime;
    /**
     * end the recording
     */
    public Calendar endRecordingTime;
    /**
     * recording is completed
     */
    private boolean completed = false;
    /**
     * recording is stoped
     */
    private boolean stopped = true;
    /**
     *  values obtained during the subwindowing
     */

    public float[] subwindowing_values;
    /**
     * raw values obtained during the subwindowing
     */

    public float[] subwindowing_raw_values;
    /**
     * duration of the subwindow during which the sound is averaged
     */
     double subwindowDuration ;// = 0.001;  //0.125 = 125 ms
     /**
     * to be used in the future to access each PCM value (not just averages),
     */
    public ShortBuffer totalBuffer;

    /**
     * noise buffer
     */
     public DoubleBuffer noiseBuffer;
    /**
     * raw noise buffer
     */
    public DoubleBuffer rawNoiseBuffer;
    /**
     * listeners
     */
    private List<NoiseListener> listeners = new ArrayList<NoiseListener>();
    /**
     * averaged sound level
     */
    public DoubleBuffer avgNoiseBuffer;


    /**
     * handlers
      */
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * audio record
     */
    private AudioRecord _recorder;
    /**
     * min buffersize
     */
    private int _minBufferSize;
    /**
     * mono stereo
     */
    private int _channel = AudioFormat.CHANNEL_IN_MONO;
    /**
     * encording format
     */
    private int _format = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * sampling frequency
     */
    static public int _rate = 44100;
    /**
     * Mic encoding
     */
    private int _source = MediaRecorder.AudioSource.MIC;


    //List of "to be supplied" with audio solution helpers, CopyOnWriteArrayList provides thread safety
    private List<Record> _measurementsHelperList = new CopyOnWriteArrayList<>();
    /**
     * recording thread
     */
    private Thread _recordingThread;


    private Object stopSync = new Object(); //Used for syncing threads


    /**
     * Constructor
     *
     * @param sensingDuration sensing duration
     */
    public Record(long sensingDuration) {
        Log.d(Tag, "sensingDuration=" + sensingDuration);
        int bufferSize = (int) (_rate * (sensingDuration / 1000f));
        Log.e(Tag, "buffer Size= " + bufferSize);
        totalBuffer = ShortBuffer.allocate(bufferSize);

        this.noiseBuffer = DoubleBuffer.allocate(bufferSize);
        this.rawNoiseBuffer = DoubleBuffer.allocate(bufferSize);
    }

    /**
     * add a noise listener
     * @param toadd listener that is added
     */
    public void addNoiseListener(NoiseListener toadd){
        listeners.add(toadd);
    }

    /**
     * listener that is removed
     * @param toremove removed listener
     */
    public void removeNoiseListener(NoiseListener toremove){
        listeners.remove(toremove);
    }

    /**
     * get the subwindowing duration
     * @return subwindow
     */
    public double getSubwindowDuration() {
        return subwindowDuration;
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
     * compute the convuolution sum
     * @param factors
     * @param vals measurements
     * @param n
     * @param startIndex index pointing where to start
     * @return sum
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
     * sound isrecorded
     * @param type
     */
     private void noiseChangedSendEvent(final int type) {
        if (stopped) {
            return;
        }
        if (type ==  HAS_COMPLETED) {
            completed = true;
        }
        mainHandler.post(new Runnable() {
            public void run() {
                for (NoiseListener listener : listeners) {
                    if (!stopped) {
                        listener.someoneReportedNoiseChange(type);
                    }
                }
            }
        });
    }

    /**
     * Terminates current measurement.
     * If a minimum number of microphone-values ​​was achieved then the measurement was successful.
     */
    public void stop() {
        removeMeasurementsHelper(this);
        stopped = true;
        //Todo francoise
        if (!completed) {
            if (anzPCM >= 2000) {
                noiseChangedSendEvent(  HAS_COMPLETED);
            } else {
                noiseChangedSendEvent( HAS_CANCELLED);
            }
        }

    }

    /**
     * Starts measurement (new), resets variables and calls for new values to MeasurementsService
     *
     * @throws Exception recording impossible
     */
    public void start() throws Exception {
        Log.d(Tag, "start recording");
        sumPCM = anzPCM = 0;
        completed = false;
        totalBuffer.clear();
        stopped = false;
        loadAudioSettings();

        subwindowDuration = Configuration.samplingDurationSec;
        addMeasurementsHelper(this);
    }

    /**
     * Measurement for predefined duration
     *
     * @param duration The sensing duration
     * @return measurement helper
     */
    public MeasurementsHelper getMeasurement(int duration) {
        return new MeasurementsHelper((int) (_rate * (duration / 1000f)));
    }

    /**
     * add a measurement helper
     * @param helper measurement helper
     * @throws Exception
     */
    private void addMeasurementsHelper(Record helper) throws Exception {
        _measurementsHelperList.add(helper);
        if (_measurementsHelperList.size() != 0 && !isRecording()) {
            Log.d(Tag, "record");
            record();
        }
    }

    /**
     * determinate if the device is recording
     * @return if the device is recording
     */
    private boolean isRecording() {
        return (_recordingThread != null && _recordingThread.isAlive());
    }

    /**
     * remove a measurement helper
     * @param helper measurementhelper to remove
     */
    public void removeMeasurementsHelper(Record helper) {
        _measurementsHelperList.remove(helper);
    }

    /**
     * Loads the specified audio settings
     */
    public void loadAudioSettings() {
        //  Log.e(Tag, "min buffer size:" + _minBufferSize );
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

        short[] tempBuffer = new short[_minBufferSize];

        while (_measurementsHelperList.size() != 0) {
            if (_recorder == null) {
                break;
            }
            Date now = new Date();
            this.startRecord = now.getTime();
            startRecordingTime = Calendar.getInstance();
            int anz = _recorder.read(tempBuffer, 0, _minBufferSize);
            // Log.e(Tag, "Take measure, anz:" + anz + "minbuffer" + _minBufferSize);
            if (anz > 0) {
                for (Record helper : _measurementsHelperList) {
                    helper.newPCM(tempBuffer, anz);
                }
            } else if (anz < 0) {
                // read operation failed, exit loop and report problem to callback
                for (Record helper : _measurementsHelperList) {
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
     * handle recording errors
     */
    private void recordingError() {

        for (NoiseListener listener : listeners) {
            if (!stopped) {
                listener.someoneReportedNoiseError();
            }
        }
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
        //Log.e(Tag, "anz:" + anz);
        anz = Math.min(totalBuffer.remaining(), anz);
        totalBuffer.put(pcm, 0, anz);


        if (totalBuffer.remaining() == 0) {
            // If required number of values ​​has been reached, no further promoted by the service
            endRecordingTime = Calendar.getInstance();
            realRecordingTime = endRecordingTime.getTimeInMillis() - startRecordingTime.getTimeInMillis();
            //           Log.e(Tag, "recording duration:" + realRecordingTime);
            // convert buffers to array
            short[] pcmBuffer = totalBuffer.array();
            // convert array of short to array of double
            double[] pcmValsInit = new double[pcmBuffer.length];
            for (int i = 0; i < pcmBuffer.length; i++) {
                pcmValsInit[i] = (double) pcmBuffer[i];
                //this.rawNoiseBuffer.put((double) pcmBuffer[i]);
                //         Log.e(Tag, "pcmbuf[" + i +"]" + pcmValsInit[i] );
            }
            Log.e(Tag, "Raw array size: " + this.rawNoiseBuffer.array().length);

            //apply A-weighting filter to PCM values
            double[] pcmVals = applyAWeightingFilter(pcmValsInit);
            sumPCM = 0;
            for (double pcmVal : pcmVals) {
                double squarePCM = pcmVal * pcmVal;
                sumPCM += squarePCM;
                //Log.d(Tag, "newPCM: " + pcmVal);
            }
            //todo fix the next line
            //     this.noiseBuffer.put(pcmVals);

            this.noiseBuffer.put(Noise.pcmToDB( pcmVals));

            //todo plot it

            // Calculation of the (previous) overall average
            avgNoise = (float) Math.sqrt(sumPCM / (pcmVals.length * 1f));
            avgNoise = Noise.pcmToDB(avgNoise);
            //todo replace
            // avgNoise = Noise.pcmToDB((float) Math.sqrt(sumPCM / (pcmVals.length * 1f)));
            Log.e(Tag, "Test: Global Avg A-Weighted Noise: " + avgNoise);

            for (double pcmrawVal : pcmBuffer) {
                double squarerawPCM = pcmrawVal * pcmrawVal;
                sumrawPCM += squarerawPCM;
                //Log.d(Tag, "newPCM: " + pcmVal);
            }
            // Calculation of the (previous) overall raw average
            avgrawNoise = (float) Math.sqrt(sumrawPCM / (pcmBuffer.length * 1f));
            avgrawNoise = Noise.pcmToDB(avgrawNoise);

            Log.e(Tag, "Test: Global Avg Raw Noise: " + avgrawNoise);


            // compute subwindows noise
            int n_samples_subwindow = (int) (_rate * subwindowDuration); //window size in term of nb of samples
            int window_nb = totalBuffer.capacity() / n_samples_subwindow; //windows number
            subwindowing_values = new float[window_nb]; // Array of windows noise values
            subwindowing_raw_values = new float[window_nb]; // Array of windows noise raw values


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
                    double pcmRawValue = pcmBuffer[index];
                    double windowSquareRawPCM = pcmRawValue * pcmRawValue;
                    subwindow_sumRAW += windowSquareRawPCM;
                    windowSizeCounter++;
                }
                //              Log.e(Tag, "subwindow_sumPCM:" + subwindow_sumPCM);
                //  Log.e(Tag, "n_samples_subwindow:" + n_samples_subwindow);

                // Log.e(Tag, "Value without pcm to DB " + (float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f)) + " Value with pcm to DB " +  Noise.pcmToDB((float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f))));
                subwindowing_values[windowsCounter] = (float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f));
                subwindowing_raw_values[windowsCounter] = Noise.pcmToDB((float) Math.sqrt(subwindow_sumRAW / (n_samples_subwindow * 1f)));

                // subwindowing_values[windowsCounter] = pcmToDB((float) Math.sqrt(subwindow_sumPCM / (n_samples_subwindow * 1f)));
                //  Log.e(Tag, "subwindow_sumPCM in db" + subwindowing_values[windowsCounter]);
                if(subwindowing_values[windowsCounter] == Double.NEGATIVE_INFINITY){
                    subwindowing_values[windowsCounter] = 0;
                }
//                    Log.e(Tag, "infinity" );
                subwindow_sumPCM = 0;
                subwindow_sumRAW = 0;
                windowsCounter++;
            }

            this. avgNoiseBuffer = DoubleBuffer.allocate(window_nb);
            for(int i =0 ; i< window_nb ; i++) {
                this.avgNoiseBuffer.put(subwindowing_values[i]);
                this.rawNoiseBuffer.put(subwindowing_raw_values[i]);
                // Log.e(Tag, "averaged sound:" + subwindowing_values[i]);
            }


            // clear tmp buffers
            pcmVals = null;
            pcmValsInit = null;
            pcmBuffer = null;

            totalBuffer.clear();
            totalBuffer = null;

            removeMeasurementsHelper(this);
             noiseChangedSendEvent(  HAS_COMPLETED);
             // noiseChanged(MeasurementsService.NoiseChangeType.COMPLETED);
        } else {
             noiseChangedSendEvent( STILL_ADDING);
            //todo
            //         noiseChanged(MeasurementsService.NoiseChangeType.NEW_VALUES);
        }
    }

    /***
     * start the continuous recording
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
     * add a measurement helper to continuously record
     * @param helper measurement helper
     * @throws Exception
     */
    private void addMeasurementsHelperContinous(Record helper) throws Exception {
        _measurementsHelperList.add(helper);
        if (_measurementsHelperList.size() != 0 && !isRecording()) {
            recordContinuous();
        }
    }

    /**
     * start to continuously record
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
     * continuous record
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
                for (Record helper : _measurementsHelperList) {
                    helper.newPCMContinuous(tempBuffer, anz);
                }
            } else if (anz < 0) {
                // read operation failed, exit loop and report problem to callback
                for (Record helper : _measurementsHelperList) {
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
     * set the continous recording
     * @param pcm measurement
     * @param anz anz
     */
    private void newPCMContinuous(short[] pcm, int anz) {

        anz = Math.min(totalBuffer.remaining(), anz);
        Log.e(Tag ,"anz:" + anz);
        totalBuffer.put(pcm, 0, anz);
        int subwindowIndex=0;
        if (totalBuffer.remaining() == 0) {
            // If required number of values ​​has been reached, no further promoted by the service
            endRecordingTime = Calendar.getInstance();
            realRecordingTime = endRecordingTime.getTimeInMillis() - startRecordingTime.getTimeInMillis();
            Log.e(Tag,"realRecordingTime= " + realRecordingTime);
            Log.e(Tag, "start recording time" + startRecordingTime);
            Log.e(Tag , "end recording time" + endRecordingTime.getTimeInMillis());

            // convert buffers to array
            short[] pcmBuffer = totalBuffer.array();
            // convert array of short to array of double
            double[] pcmValsInit = new double[pcmBuffer.length];
            for (int i = 0; i < pcmBuffer.length; i++) {
                pcmValsInit[i] = (double) pcmBuffer[i];
                //  Log.e(Tag, "pcm[" + i + "]" + pcmValsInit[i]);
            }



            //apply A-weighting filter to PCM values
            double[] pcmVals = applyAWeightingFilter(pcmValsInit);
            sumPCM = 0;
            for (int i = 0; i < pcmVals.length; i++) {
                double squarePCM = pcmVals[i] * pcmVals[i];
                sumPCM += squarePCM;
            }

            this.noiseBuffer.put(pcmVals);//added by francois
            // Calculation of the (previous) overall average

            avgNoise = Noise.pcmToDB((float) Math.sqrt(sumPCM / (pcmVals.length * 1f)));

            totalBuffer.clear();
            //todo
            noiseChangedSendEvent(HAS_COMPLETED);
            //noiseChanged(MeasurementsService.NoiseChangeType.COMPLETED);

        }
    }
}