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
package com.example.colibris.calib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.colibris.R;
import com.example.colibris.calib.regression.Simpleregression;
import com.example.colibris.calib.sound.Record;
import com.example.colibris.comtool.ContextData;
import com.example.colibris.configuration.Configuration;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import static com.example.root.myapplication.calib.Record.HAS_COMPLETED;


/**
 * The FileManager class is used to handle file. In particular, this class write/read
 * information in/from a file.
 *
 */

public class FileManager {
    /**
     * log related information
     */
    private static final String LOG_TAG = "FileManager";
    /**
     * file name
     */
    public String fileName;
    /***
     * determine whether we should append to the file or create an empty file
     */
    private boolean istoappend;
    /**
     * context
     */
    private Context context;
    /**
     * stream  used to write in the file
     */
    private FileOutputStream outputStream;
    /**
     * stream used to read the file
     */
    private FileInputStream inputStream = null; //to read the file
    /**
     * thread used to save the sound
     */
    private Thread _savingSoundThread;
    /**
     * file is closed (not usefull/used)
     */
    private boolean isclosed = true;
    /**
     *  state that the writting of the sound into the file is completed
     */
    public static  final int SAVING_SOUND_HAS_COMPLETED =0;
    /**
     * set of file listeners
     */
    private List<FileListener> listeners = new ArrayList<FileListener>();
    /**
     * handler
     */
    //  .
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * line to write in the file
     */
    private String line2write;
    /**
     * x value
     */
    public String axisX;
    /**
     * y value
     */
    public String axisY;
    /**
     * z value
     */
    public String axisZ;
/**
 * latitude
 */
    public String latitude;
    /**
     * longitude
     */
    public String longitude;
    /**
     * position id
     */
     private int iPosition = 0;
     /**
      * orientation
      */
    private int iOrientation = 0;
    /**
     *
     */
    private String ortIndex;
    /**
     *
     */
    private String pstIndex;
    public boolean ortLowerTreshold = true;
    public boolean pstLowerTreshold = true;
    public double minGPSboundarie = Integer.MAX_VALUE;
    private boolean flagGeoCase = false;
    private Double filterSD = (double) 1;
    private Double filterWeight = (double) 0;

    /**
     * for converting nanoseconds to milliseconds
     */
    private static final float NS2S = 1.0f / 1000000.0f; //




    /**
     * allocate outputStream and inputstream so as to write or read a file
     *     * during the creation of the file, there are two option : create an empty
     *     * file or append always to the end of the file
     *     isToappend defines if we will append the text at the end of the file
     *     or if instead we erase the file to write the text
     * @param provided_file_name file name
     * @param isToappend determine if the information should be placed at the end of the file or should replace
     * @param ctx context
     */
    public FileManager(String provided_file_name , Boolean isToappend, Context ctx){
        setFilename(provided_file_name);
        this.isclosed = false;
        this.istoappend = isToappend;
        this.context = ctx;
        // Get the directory for the user's public pictures directory.
//        File file = new File(Environment.getExternalStoragePublicDirectory(
        //                  Environment.DIRECTORY_DOCUMENTS), provided_file_name);

        // File file = new File(Environment.getExternalStoragePublicDirectory(
        //                  Environment.DIRECTORY_DOCUMENTS), provided_file_name);


        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        File file = new File(docsFolder.getAbsolutePath(),provided_file_name);

     /*   if(file.isDirectory()){            Log.e(LOG_TAG, "this is a directory"); }
        else            Log.e(LOG_TAG, "this is not a directory");*/

        try {
            outputStream = new FileOutputStream(file, isToappend);
            inputStream = new FileInputStream(file);
            //  Log.e(LOG_TAG, "after outputstram");
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.e(LOG_TAG, "Error writing " + file, e);
        }
          /*   if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created");
            }
*/


   /*     ctx.getExternalFilesDir()
        File dcimDir = ctx.getExternal getExternalStoragePublicDirectory(ctx.DIRECTORY_DCIM);
                ctx.getDataDir();//
        File picsDir = new File(dcimDir, "Calibration");
        picsDir.mkdirs(); //make if not exist
        File newFile = new File(picsDir, provided_file_name);

        try {
            outputStream = new FileOutputStream(newFile, isToappend);
            inputStream = new FileInputStream(newFile);//   ctx.openFileInput(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/



/*

         if (isToappend == true) {
             try {
                 //File dir = ctx.getDir(directory, Context.MODE_APPEND);
                // File myFile = new File(dir, this.fileName);
                // outputStream = new FileOutputStream(myFile);

                outputStream = ctx.openFileOutput(this.fileName, Context.MODE_APPEND); //MODE_APPEND   MODE_PRIVATE
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
         }
        else
             try {
                 //File dir = ctx.getDir(directory, Context.MODE_PRIVATE);
                // File myFile = new File(dir, this.fileName);
                // outputStream = new FileOutputStream(myFile);

                 outputStream = ctx.openFileOutput(this.fileName, Context.MODE_PRIVATE);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }

        try {
         //   File dir = ctx.getDir(directory, Context.MODE_PRIVATE);
           // File myFile = new File(dir, this.fileName);
           // inputStream = new FileInputStream(myFile);

            inputStream = ctx.openFileInput(this.fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }


    /**
     *   Create a file
     *   allocate outputStream and inputstream so as to write or read a file
     *      during the creation of the file, there are two option : create an empty
     *      file or append always to the end of the file
     *
     *       @param provided_file_name file name
     *       @param isToappend determine if the information should be placed at the end of the file or should replace
     *       @param ctx context
     * @param where
     */
    public FileManager(String provided_file_name , Boolean isToappend, Context ctx, String where){
        setFilename(provided_file_name);
        this.isclosed = false;
        this.istoappend = isToappend;
        this.context = ctx;
        // Get the directory for the user's public pictures directory.
//        File file = new File(Environment.getExternalStoragePublicDirectory(
        //                  Environment.DIRECTORY_DOCUMENTS), provided_file_name);

        // File file = new File(Environment.getExternalStoragePublicDirectory(
        //                  Environment.DIRECTORY_DOCUMENTS), provided_file_name);


        File docsFolder = new File(Environment.getExternalStorageDirectory() + where);
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        File file = new File(docsFolder.getAbsolutePath(),provided_file_name);

     /*   if(file.isDirectory()){            Log.e(LOG_TAG, "this is a directory"); }
        else            Log.e(LOG_TAG, "this is not a directory");*/

        try {
            outputStream = new FileOutputStream(file, isToappend);
            inputStream = new FileInputStream(file);
            //  Log.e(LOG_TAG, "after outputstram");
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.e(LOG_TAG, "Error writing " + file, e);
        }
          /*   if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created");
            }
*/


   /*     ctx.getExternalFilesDir()
        File dcimDir = ctx.getExternal getExternalStoragePublicDirectory(ctx.DIRECTORY_DCIM);
                ctx.getDataDir();//
        File picsDir = new File(dcimDir, "Calibration");
        picsDir.mkdirs(); //make if not exist
        File newFile = new File(picsDir, provided_file_name);

        try {
            outputStream = new FileOutputStream(newFile, isToappend);
            inputStream = new FileInputStream(newFile);//   ctx.openFileInput(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/



/*

         if (isToappend == true) {
             try {
                 //File dir = ctx.getDir(directory, Context.MODE_APPEND);
                // File myFile = new File(dir, this.fileName);
                // outputStream = new FileOutputStream(myFile);

                outputStream = ctx.openFileOutput(this.fileName, Context.MODE_APPEND); //MODE_APPEND   MODE_PRIVATE
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
         }
        else
             try {
                 //File dir = ctx.getDir(directory, Context.MODE_PRIVATE);
                // File myFile = new File(dir, this.fileName);
                // outputStream = new FileOutputStream(myFile);

                 outputStream = ctx.openFileOutput(this.fileName, Context.MODE_PRIVATE);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }

        try {
         //   File dir = ctx.getDir(directory, Context.MODE_PRIVATE);
           // File myFile = new File(dir, this.fileName);
           // inputStream = new FileInputStream(myFile);

            inputStream = ctx.openFileInput(this.fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }


    /**
     * used to reset the streams that are useful to read/write at a given position if a file.
     * The cursor goes at the begining of the file
     */
     private void reset (){
        try {
            inputStream.close();
            outputStream.close();
            isclosed = true;

            File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
            boolean isPresent = true;
            if (!docsFolder.exists()) {
                isPresent = docsFolder.mkdir();
            }
            File file = new File(docsFolder.getAbsolutePath(),this.fileName);

            try {
                outputStream = new FileOutputStream(file, true /*append*/);
                inputStream = new FileInputStream(file);
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Log.e(LOG_TAG, "Error writing " + file, e);
            }
            // inputStream = this.context.openFileInput(this.fileName);
            isclosed = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * extract the samples included in the list of files and the local file that is calling the
     *     function whenever they have common time stamps
     * @param remoteFilelist list of files from which the samples are extracted
     * @param ctx context
     * @return list of samples
     */
    public ArrayList <ArrayList<Double>> extractCommontimeSeries( List<FileManager> remoteFilelist, ContextData ctx){
        // extract the local samples and put it into the matrix to form the Y vector
        ArrayList <ArrayList<Double>> matrix = new ArrayList <ArrayList<Double>>();
        ArrayList <ArrayList<Double>> matrixRaw = new ArrayList <ArrayList<Double>>();
        if(Configuration.isLocationAwareCalibration == false)
            matrix =  this.extract(null);
        if(Configuration.isLocationAwareCalibration == true) {
            if(Configuration.isFilteredData2LinearRegression == true) {
                if(Configuration.isFilteredData2GeoRegression == false) {
                    Log.d(LOG_TAG, "I'm in the filtered Case");
                    matrix = this.extractFilterMatrix(null, ctx);
                }
                if(Configuration.isFilteredData2GeoRegression == true){
                    Log.d(LOG_TAG, "I'm in the Geo filtered Case");
                    matrix = this.extractFilterGPSorientation(null, ctx);
                }
                //matrixRaw = this.extractFilterMatrix(null, true);
            }
            else{
                Log.d(LOG_TAG, "I'm not in the filtered Case");
                matrix = this.extractGPSorientation(null);
            }
        }

        // extract the remote samples X1 X2 X3 ... and put it into the matrix
        for(int i = 0 ; i<remoteFilelist.size() ; i++){
            Log.e(LOG_TAG, "*******\n *******\n Analyse remote file number " +i);
            if(Configuration.isLocationAwareCalibration == false) {
                matrix = remoteFilelist.get(i).extract(matrix); // extract the sample X that have been taken at the same time
            }
            if(Configuration.isLocationAwareCalibration == true) {
                if(Configuration.isFilteredData2LinearRegression == true) {
                    if(Configuration.isFilteredData2GeoRegression == false) {
                        //Filtering the data based upon the distance and accumulated error between the devices
                        matrix = remoteFilelist.get(i).extractFilterMatrix(matrix, ctx); // extract the sample X that have been taken at the same time
                        //matrixRaw = remoteFilelist.get(i).extractFilterMatrix(matrix, true); // extract the sample X that have been taken at the same time
                    }
                    if(Configuration.isFilteredData2GeoRegression == false){
                        //Filtering the data based upon the distance and accumulated error between the devices
                        matrix = remoteFilelist.get(i).extractFilterGPSorientation(matrix, ctx); // extract the sample X that have been taken at the same time
                    }
                }
                else {
                    //Not filtered data and getting history location
                    matrix = remoteFilelist.get(i).extractGPSorientation(matrix); // extract the sample X that have been taken at the same time
                }
            }
            // and put it into the matrix
        }
        return matrix ;
    }


    /**
     * zip a folder
     * @param name2zipFile path of the folder that need to be zipped
     * @param lastTest id the define the test id and will be used to generate the name of the zip file
     * @return if the zip is generated
     */
    public boolean zipFileAtPath(String name2zipFile, String lastTest) {
        final int BUFFER = 2048;
        ContextData ctx = new ContextData();
        String sourcePath = Environment.getExternalStorageDirectory() + "/Documents";
        String toLocation = Environment.getExternalStorageDirectory() + "/" + name2zipFile + Configuration.nameRecordTest +".zip";
        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
            boolean recordTest = Integer.parseInt(name2zipFile.split("test")[1]) == Integer.parseInt(lastTest);
            Log.e(LOG_TAG, "Tests to run: " + name2zipFile);
            Log.e(LOG_TAG, "Current test: " + Integer.parseInt(name2zipFile.split("test")[1]) + " last test: " + Integer.parseInt(lastTest) + " record test: " + recordTest);
            if((Integer.parseInt(name2zipFile.split("test")[1]) != Integer.parseInt(lastTest))){
                File[] fileList = sourceFile.listFiles();
                deleteFilesAftZip(fileList);
                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                //boolean isPresent = true;
                //if (!docsFolder.exists()) {
                //  isPresent = docsFolder.mkdir();
                //}

                File file = new File(docsFolder.getAbsolutePath(),this.fileName);
                try {
                    outputStream = new FileOutputStream(file, true /*append*/);
                    inputStream = new FileInputStream(file);
                } catch (IOException e) {
                    // Unable to create file, likely because external storage is
                    // not currently mounted.
                    Log.e(LOG_TAG, "Error writing " + file, e);
                }
            }else{
                Log.e(LOG_TAG, "I am in the last case");
                //todo write in the Record Activity - Record tests finished.
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * zip all the folders located in the provided folder
     * @param out stream of the zip
     * @param folder folder path
     * @param basePathLength path lenght
     * @throws IOException
     */
    private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }


    /**
     * return the file name of the given path
     * @param filePath file path
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     * @return filename
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }


    /**
     * delete all the files
     * @param files
     */
    public void deleteFilesAftZip(File[] files)
    {
        for (int i = 0; i < files.length; i++)
        {
            if(!files[i].getName().contains("local_connection_graphs.txt")) {
                files[i].delete();
            }
        }
    }


    /**
     * returns if there are some files
     * @return if there are some files
     */
    public int  getFileSize()   {

        try {
            return inputStream.available();
        } catch (IOException e) {

            e.printStackTrace();
            return 0;
        }
    }

    /**
     * returns the file name
     * @return file name
     */
    public String getFilename(){
        return this.fileName;
    }

    /**
     * set the file name
     * @param provided_name file name
     */
    public void setFilename(String provided_name){this.fileName =  new String(provided_name);}

    /**
     * add a file listener
     * @param toadd file listener
     */
    public void addFileListener(FileListener toadd){ listeners.add(toadd);}

    /**
     * return false
     * @return false
     */
    public boolean isEmpty(){
        return false;
    }

    /**
     * remove a file listener
     * @param toremove file listener to remove
     */
    public void removeFileListener(FileListener toremove){
        listeners.remove(toremove);
    }

    /**
     * send a notification (typically when the file is written
     * @param type type
     */
    private void fileChangedSendEvent(final int type) {
        Log.d(LOG_TAG , "file changed event :" );
        if (isclosed) {
            Log.d(LOG_TAG , "is closed :" );
            return;
        }

        mainHandler.post(new Runnable() {
            public void run() {
                for (FileListener listener : listeners) {
                    if (!isclosed) {
                        Log.d(LOG_TAG , "report to listener " );
                        listener.someoneReportedFileChange(type);
                    }
                }
            }
        });
    }

    /*deprecated method*/
    /* add the string "time,value,\n" to the calibration file*/
 /*   public int write_calibration_txt(String aValue)
    {
    try {
        String space = ",";

        //write local time
        Date actualDate = new Date();
        long timeis = actualDate.getTime();
        String timeStr = String.valueOf(timeis);
        outputStream.write(timeStr.getBytes());

        //write ","
        outputStream.write(space.getBytes());

        //write the value
        outputStream.write(aValue.getBytes());

        //write ",\n"
        space = ",\n";
        outputStream.write(space.getBytes());
    } catch (Exception e) {
        e.printStackTrace();
        return 0;
    }
        return 1;
    }*/

    /**
     * returns if the sound is actually saves
     * @return if were are actually saving the sound
     */
    private boolean write_calibration_bufferThreadAlive() {
        return (_savingSoundThread != null && _savingSoundThread.isAlive());
    }

    /*
    * */

    /**
     * write in a file the content of the sound (given buffer), adding time stamps starting from the (synchronised) time
     * that determines when the recording started
     *         and given the time slot between two noise recorded
     * @param buffer buffer
     * @param startTime  time wen the recording started
     * @param slot delay between two measurements
     * @throws Exception we could not write the sound in the file
     */
    public synchronized void write_calibration_bufferThread(final DoubleBuffer buffer, final double startTime , final double slot  ) throws Exception {

        Log.d(LOG_TAG , "launching the thread writing the sound recorded locally with synchronised start  time: " +  startTime );
        if (!write_calibration_bufferThreadAlive()) {
            //Create a new thread only if it does not exist or has been terminated.
            _savingSoundThread = new Thread() {
                @Override
                public void run() {
                    Log.d(LOG_TAG , "thread launched");
                    write_calibration_buffer( buffer,  startTime /* synchronised started time*/, slot);
                }
            };
            _savingSoundThread.start();
        }else{
            Log.d(LOG_TAG , "cannot launch a writing thread, End of write ");
        }
    }






    /**
     * write in a file the content of the sound (given buffer), adding time stamps starting from the (synchronised) time
     * that determines when the recording started
     *         and given the time slot between two noise recorded
     * @param buffer buffer
     * @param startTime  time wen the recording started
     * @param slot delay between two measurements
     * @param ctx context
     */
    public synchronized void write_calibration_bufferThread_gps_orientation(final DoubleBuffer buffer, final double startTime, final double slot, final ContextData ctx/*, final ArrayList<String> positionTime*/) {

        Log.d(LOG_TAG , "launching the thread writing the sound recorded locally with synchronised start  time: " +  startTime );
        if (!write_calibration_bufferThreadAlive()) {
            //Create a new thread only if it does not exist or has been terminated.
            _savingSoundThread = new Thread() {
                @Override
                public void run() {
                    Log.d(LOG_TAG , "thread launched");
                    synchronized (ctx) {
                        if ((Configuration.isLocationAwareCalibration == false) && (Configuration.isRawSoundData == true)) {
                            Log.d(LOG_TAG, "*********** GEO button is not pressed " + Configuration.isLocationAwareCalibration);
                            Log.d(LOG_TAG, "*********** RAW button is pressed " + Configuration.isRawSoundData);
                            //Log.d(LOG_TAG, "*********** RAW data size " + ctx.rawBuffer.size());
                            Log.d(LOG_TAG, "*********** RAW data size " + ContextData.rawBuffer.length);
                            write_calibration_buffer_recordActiv(buffer, startTime /* synchronised started time*/, slot, ctx);
                        }
                        if ((Configuration.isLocationAwareCalibration == false) && (Configuration.isRawSoundData == false)) {
                            Log.d(LOG_TAG, "*********** GEO button is not pressed " + Configuration.isLocationAwareCalibration);
                            Log.d(LOG_TAG, "*********** RAW button is not pressed " + Configuration.isRawSoundData);
                            write_calibration_buffer(buffer, startTime /* synchronised started time*/, slot);
                        }
                        if ((Configuration.isLocationAwareCalibration == true) && (Configuration.isRawSoundData == false)) {
                            Log.d(LOG_TAG, "*********** GEO button is pressed " + Configuration.isLocationAwareCalibration);
                            Log.d(LOG_TAG, "*********** RAW button is not pressed " + Configuration.isRawSoundData);
                            //write_calibration_buffer_gps_orientation(buffer, startTime  /* synchronised started time*/, slot, ctx);//, positionTime);
                            write_calibration_buffer_gps(buffer, startTime  /* synchronised started time*/, slot, ctx);//, positionTime);
                        }
                    }
                }
            };
            _savingSoundThread.start();
        }else{
            Log.d(LOG_TAG , "cannot launch a writing thread, End of write ");
        }
    }





    /**
     * write in the file the raw sound provided by buffer  starting at synchronised time starttime and with a time slot
     *      * between two sound record given by slot
      @param buffer buffer
     @param startTime  time wen the recording started
     @param slot delay between two measurements
     * @param ctx context
     * @return 0 or -1 is returned if a problem occured, otherwise, 1 is returned
     */
    public int write_calibration_buffer_gps_orientation(DoubleBuffer buffer, double startTime ,double slot  , ContextData ctx)//, ArrayList<String> positionTime)
    {
        Log.d(LOG_TAG , "into the write calibration buffer thread ");
        double[] pcmBuffer = buffer.array();// convert array of short to array of double
        long samplyFrequency = Record._rate ; // extract the recording rate

        Log.d(LOG_TAG,"recording start time is " + startTime);
        Log.d(LOG_TAG, "recording rate is " + Record._rate);
        Log.d(LOG_TAG,"slot is "+ slot);

        long cteNormTemp = 1000000000000L;
        double timeStart =  startTime;
        double timeis =  startTime;
        //Double timeOrt = (double) ctx.startOrtTime;
        //Double timePst = (double) ctx.startGPSTime;

        // todo add the cleaning of the beginning
        boolean isStillblanck = false; // is unsed to see if we are still at the beginning of the audio record
        // and there is 0 noise recorded
        // start to write into a file  line format is time,noise,\n
        try {

            //ctx.orientationData = ctx.creatingArrs(startTime - (long) ((pcmBuffer.length) * slot) , pcmBuffer.length + 3, slot, true, true);
            //ctx.positionData = ctx.creatingArrs(startTime - (long) ((pcmBuffer.length + 1) * slot) , pcmBuffer.length + 1, slot, false, true);
            if(ContextData.positionData.size() == 0)
            {
                //todo go to the simple case
                return -1;
            }
            double highestOrtTime = (double) Long.parseLong(ContextData.orientationData.get(ContextData.orientationData.size() - 1).split(",")[0]);
            double highestPstTime = (double) Long.parseLong(ContextData.positionData.get(ContextData.positionData.size() - 1).split(",")[0]);
            Long lowestDBTime = (long) startTime;
            if((lowestDBTime < highestOrtTime) || lowestDBTime < highestPstTime) {
                if(lowestDBTime > highestOrtTime)
                    this.ortLowerTreshold = false;
                if(lowestDBTime > highestOrtTime)
                    this.pstLowerTreshold = false;
                ortIndex = findIntersectionTime(lowestDBTime, ContextData.orientationData, true, this.ortLowerTreshold);
                pstIndex = findIntersectionTime(lowestDBTime, ContextData.positionData, true, this.pstLowerTreshold);
                Log.e(LOG_TAG, "this is the ortIndex: " + ortIndex);
            }else {
                Log.e(LOG_TAG, "Protocols times does not match");
                //todo in this case, go to linear regression
                return -1;
            }



            for (int i = 0; i < pcmBuffer.length; i++) {

                /*Configuration config = new Configuration();
                double margin ;
                if (config.isAveraging) {
                    margin =   config.subwindowDuration * 1000; // 10 ; // margin allowed between the samples
                }else{
                    margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
                }
                */

                // Log.e(LOG_TAG, " DB Size: " + pcmBuffer.length + " Ort Size: " + ctx.orientationData.size() + " GPS Size: " + ctx.positionData.size());

                String space = ",";
                String avalue = String.valueOf(pcmBuffer[i] );//extract the sound

                //todo remove the if condition to stop cleaning the data
                if (isStillblanck && avalue.compareTo("0.0") == 0 ){
                    //nothing to do
                    Log.e(LOG_TAG, avalue +  "is equal to 0");
                }else{
                    isStillblanck = false;
                    Log.e(LOG_TAG, "Beginning the stream");
                    //write local time

                    Log.e(LOG_TAG, "This device is: " + ctx.FLAG);


                    //ArrayList<String> orientationDataDuration = new ArrayList<>();
                    //ArrayList<String> positionDataDuration = new ArrayList<>();
                    if(i != 0) {
                        if (iOrientation >= (ContextData.orientationData.size() - 1) ) {
                            //iOrientation = ctx.orientationData.size() - 1;
                            Log.e(LOG_TAG, "We don't change the iOrientation anymore");
                        } else {
                            Log.e(LOG_TAG, "Taking the orientation time position for Sound Line: " + i);
                            String ortIndexIs = findIntersectionTime((long) timeis, ContextData.orientationData, false, this.ortLowerTreshold);
                            int iOrientationIs = new Integer(ortIndexIs.split(",")[0]);
                            iOrientation = iOrientationIs;
                        }
                    }else{
                        if(ortIndex != null) {
                            int iOrientation0 = new Integer(ortIndex.split(",")[0]);
                            iOrientation = iOrientation0;
                        }else {
                            Log.e(LOG_TAG, "No GPS data until know. Let's take the last observation");
                            //Log.e(LOG_TAG, "Position Array is: " + Arrays.deepToString(ctx.positionData.toArray()));
                            String ostIndexIs = findIntersectionTime((long) timeis, ContextData.orientationData, false, this.ortLowerTreshold);
                            int iOrientationIs = new Integer(ostIndexIs.split(",")[0]);
                            iOrientation = iOrientationIs;
                        }
                    }


                    //In case we have less data of changing gps then changing noise level


                    if(i != 0) {
                        if (iPosition >= (ContextData.positionData.size() - 1) ) {
                            Log.e(LOG_TAG, "We don't change the iPosition anymore");
                        } else {
                            Log.e(LOG_TAG, "Taking the position(GPS) time position");
                            String pstIndexIs = findIntersectionTime((long) timeis, ContextData.positionData, false, this.pstLowerTreshold);
                            int iPositionIs = new Integer(pstIndexIs.split(",")[0]);
                            iPosition = iPositionIs;
                        }
                    }else{
                        if(pstIndex != null) {
                            int iPosition0 = new Integer(pstIndex.split(",")[0]);
                            iPosition = iPosition0;
                        }else {
                            Log.e(LOG_TAG, "No GPS data until know. Let's take the last observation");
                            //Log.e(LOG_TAG, "Position Array is: " + Arrays.deepToString(ctx.positionData.toArray()));
                            String pstIndexIs = findIntersectionTime((long) timeis, ContextData.positionData, false, this.pstLowerTreshold);
                            int iPositionIs = new Integer(pstIndexIs.split(",")[0]);
                            iPosition = iPositionIs;
                        }
                    }

                    /*
                    if(ctx.positionData.size() == 0)
                        iPosition = 0;


                    if (i >= ctx.positionData.size())
                        iPosition = ctx.positionData.size() - 1;
                    else
                        iPosition = i;
                    */
                    String timeOrientation = ContextData.orientationData.get(iOrientation).split(",")[0];
                    String timePosition = ContextData.positionData.get(iPosition).split(",")[0];
                    double timeOrt = (double) Long.parseLong(timeOrientation);
                    double timePst = (double) Long.parseLong(timePosition);
                    //double timePst = timeStart;


                    Log.e(LOG_TAG, " Time DB Norm: " + timeStart + " Time Orientation: " + timeOrt + " Time GPS: " + timePst);
                    Log.e(LOG_TAG, " Time DB Norm: " + timeis + " Time Orientation: " + timeOrt + " Time Ort0: " + ortIndex.split(",")[1]);
                    //Restriction created to be sure that the data saved is actually as close as possible, considering the time.

                    // Log.e(LOG_TAG, "timeStart: " + timeStart + " timePST: " + timePst + " timeORT: " + timeOrt);
                    if ((timeStart <= timeOrt) || (timePst >= timeStart)){// && (timePst >= timeStart)){// && ((timePst >= timeStart) && (timePst <= timeEnd))) {
                        Log.d(LOG_TAG, "time Ort: " + timeOrt + " time Pst: " + timePst );
                        //  Log.e(LOG_TAG, "Writing the data from differente protocol sources");

                        //double timeOffsetOrt = Math.abs(timeOrt - timeis);
                        double timeOffsetPst = Math.abs(timePst - timeis);
                        // Log.e(LOG_TAG, "timeOffset of Orientation :" + timeOffsetOrt + " Time DB Norm: " + timeis + " Time Orientation: " + timeOrt + " Time GPS: " + timePst);


                        axisX = ContextData.orientationData.get(iOrientation).split(",")[1];
                        axisY = ContextData.orientationData.get(iOrientation).split(",")[2];
                        axisZ = ContextData.orientationData.get(iOrientation).split(",")[3];

                        Log.e(LOG_TAG, "Position time Position: " + iPosition);
                        latitude = ContextData.positionData.get(iPosition).split(",")[1];
                        longitude = ContextData.positionData.get(iPosition).split(",")[2];
                        //if((iOrientation%2) == 0) {
                        //latitude = ctx.positionData.get(iPosition).split(",")[1];
                        //longitude = ctx.positionData.get(iPosition).split(",")[2];
                        //latitude = "2.5";
                        //longitude = "2";
                        //latitude = "2.75";
                        //longitude = "1.8";
                        //}else{
                        //latitude = "1.5";
                        //longitude = "2.5";
                        //latitude = "1.35";
                        //longitude = "2.15";
                        //}


                        /*if(timeOrt - timeis > 0)
                        {
                            //todo verify if the range of orientation is not so sparce
                            if(timeOffsetOrtFuture < timeOffsetOrt){
                                Log.e(LOG_TAG, "Orientation time closer to the future Noise time");
                                axisX = ctx.orientationData.get(iOrientation - 1).split(",")[1];
                                axisY = ctx.orientationData.get(iOrientation - 1).split(",")[2];
                                axisZ = ctx.orientationData.get(iOrientation - 1).split(",")[3];
                            }else{
                                Log.e(LOG_TAG, "Orientation time closer to the current Noise time");
                                axisX = ctx.orientationData.get(iOrientation).split(",")[1];
                                axisY = ctx.orientationData.get(iOrientation).split(",")[2];
                                axisZ = ctx.orientationData.get(iOrientation).split(",")[3];
                            }
                        }else{
                            if(timeOffsetOrtOld < timeOffsetOrt){
                                Log.e(LOG_TAG, "Orientation time closer to the past Noise time");
                                axisX = ctx.orientationData.get(iOrientation + 1).split(",")[1];
                                axisY = ctx.orientationData.get(iOrientation + 1).split(",")[2];
                                axisZ = ctx.orientationData.get(iOrientation + 1).split(",")[3];
                            }else{
                                Log.e(LOG_TAG, "Orientation time closer to the current Noise time");
                                axisX = ctx.orientationData.get(iOrientation).split(",")[1];
                                axisY = ctx.orientationData.get(iOrientation).split(",")[2];
                                axisZ = ctx.orientationData.get(iOrientation).split(",")[3];
                            }
                        }

                        */

                        /*
                        //Time verification for orientation. In a case that a new deltatime is found, we atualize the deltatime and the orientation data.
                        if (timeOffsetOrt <= this.DeltaOrientationTime) {
                            Log.e(LOG_TAG, "NEW DELTA ORIENTATION");
                            this.DeltaOrientationTime = (int) timeOffsetOrtOld;
                            iMinOrt = i;

                            axisX = ctx.orientationData.get(iOrientation).split(",")[1];
                            axisY = ctx.orientationData.get(iOrientation).split(",")[2];
                            axisZ = ctx.orientationData.get(iOrientation).split(",")[3];

                            /*String axisXcrtTime = ctx.orientationData.get(iOrientation).split(",")[1];
                            String axisYcrtTime = ctx.orientationData.get(iOrientation).split(",")[2];
                            String axisZcrtTime = ctx.orientationData.get(iOrientation).split(",")[3];
                            orientationDataDuration.add(i, timeOrientation + "," + axisXcrtTime + "," + axisYcrtTime + "," + axisZcrtTime);
                            */
                        /*
                        } else {
                            Log.e(LOG_TAG, "KEEP DELTA ORIENTATION");
                            axisX = ctx.orientationData.get(iMinOrt).split(",")[1];
                            axisY = ctx.orientationData.get(iMinOrt).split(",")[2];
                            axisZ = ctx.orientationData.get(iMinOrt).split(",")[3];
                        }
                        */
                        //Time verification for position. In a case that a new deltatime is found, we atualize the deltatime and the position data.
                        /*if (timeOffsetPst < (double) (this.DeltaPositionTime)) {
                            Log.e(LOG_TAG, "NEW DELTA POSITION");
                            this.DeltaPositionTime = (int) timeOffsetPst;
                            iMinPst = iPosition;

                            latitude = ctx.positionData.get(iPosition).split(",")[1];
                            longitude = ctx.positionData.get(iPosition).split(",")[2];

                            /*String latitudecrtTime = ctx.positionData.get(iPosition).split(",")[1];
                            String longitudecrtTime = ctx.positionData.get(iPosition).split(",")[2];
                            positionDataDuration.add(i, timeis + "," + latitudecrtTime + "," + longitudecrtTime);
                            */
                        /*
                        } else {
                            Log.e(LOG_TAG, "KEEP DELTA POSITION");
                            latitude = ctx.positionData.get(iMinPst).split(",")[1];
                            longitude = ctx.positionData.get(iMinPst).split(",")[2];;
                        }
                        */
                        // Log.e(LOG_TAG, "X_example " + axisX + " Y_example " + axisY + " Z_example " + axisZ + " latitude " + latitude + " longitude " + longitude);
                        String timeStr = String.valueOf(timeis);
                        line2write = timeStr + space + avalue + space + axisX + space + axisY + space + axisZ + space + latitude + space + longitude;
                        //Log.e(LOG_TAG, "Line 2 write : (" + i + ") " + line2write);
                        outputStream.write(line2write.getBytes());
                        space = ",\n";
                        outputStream.write(space.getBytes());
                    }
                }
                //timeOrt = timeOrt + slot;
                //timePst = timePst + slot;
                timeis = timeis + slot;
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        Log.d(LOG_TAG , "End of write ");
        this.fileChangedSendEvent(SAVING_SOUND_HAS_COMPLETED);
        return 1;
    }


    /**
     * write in the file the raw sound provided by buffer  starting at synchronised time starttime and with a time slot
     *      * between two sound record given by slot
     @param buffer buffer
     @param startTime  time wen the recording started
     @param slot delay between two measurements
      * @param ctx context
     * @return  contextualised sound   */
    public int write_calibration_buffer_gps(DoubleBuffer buffer, double startTime ,double slot  , ContextData ctx)//, ArrayList<String> positionTime)
    {
        Log.d(LOG_TAG , "into the write calibration buffer thread ");
        double[] pcmBuffer = buffer.array();// convert array of short to array of double
        long samplyFrequency = Record._rate ; // extract the recording rate

        Log.d(LOG_TAG,"recording start time is " + startTime);
        Log.d(LOG_TAG, "recording rate is " + Record._rate);
        Log.d(LOG_TAG,"slot is "+ slot);

        long cteNormTemp = 1000000000000L;
        double timeStart =  startTime;
        double timeis =  startTime;

        // todo add the cleaning of the beginning
        boolean isStillblanck = false; // is unsed to see if we are still at the beginning of the audio record
        try {
            if(ContextData.positionData.size() == 0)
            {
                write_calibration_buffer(buffer, startTime, slot);
                return 0;
            }
            double highestPstTime = (double) Long.parseLong(ContextData.positionData.get(ContextData.positionData.size() - 1).split(",")[0]);
            Long lowestDBTime = (long) startTime;
            if(lowestDBTime > highestPstTime) {
                pstIndex = findIntersectionTime(lowestDBTime, ContextData.positionData, true, this.pstLowerTreshold);
                //this.pstLowerTreshold = false;
            }else {
                Log.e(LOG_TAG, "Protocols times does not match");
                this.pstLowerTreshold = false;
                pstIndex = findIntersectionTime(lowestDBTime, ContextData.positionData, true, this.pstLowerTreshold);
            }



            for (int i = 0; i < pcmBuffer.length; i++) {

                String space = ",";
                String avalue = String.valueOf(pcmBuffer[i] );//extract the sound

                //todo remove the if condition to stop cleaning the data
                if (isStillblanck && avalue.compareTo("0.0") == 0 ){
                    //nothing to do
                    Log.e(LOG_TAG, avalue +  "is equal to 0");
                }else{
                    isStillblanck = false;
                    Log.e(LOG_TAG, "Beginning the stream");
                    //write local time

                    Log.e(LOG_TAG, "This device is: " + ctx.FLAG);

                    //In case we have less data of changing gps then changing noise level


                    if(i != 0) {
                        if (iPosition >= (ContextData.positionData.size() - 1) ) {
                            Log.e(LOG_TAG, "We don't change the iPosition anymore");
                        } else {
                            Log.e(LOG_TAG, "Taking the position(GPS) time position");
                            String pstIndexIs = findIntersectionTime((long) timeis, ContextData.positionData, false, this.pstLowerTreshold);
                            int iPositionIs = new Integer(pstIndexIs.split(",")[0]);
                            iPosition = iPositionIs;
                        }
                    }else{
                        if(pstIndex != null) {
                            int iPosition0 = new Integer(pstIndex.split(",")[0]);
                            iPosition = iPosition0;
                        }else {
                            Log.e(LOG_TAG, "No GPS data until know. Let's take the last observation");
                            //Log.e(LOG_TAG, "Position Array is: " + Arrays.deepToString(ctx.positionData.toArray()));
                            String pstIndexIs = findIntersectionTime((long) timeis, ContextData.positionData, false, this.pstLowerTreshold);
                            int iPositionIs = new Integer(pstIndexIs.split(",")[0]);
                            iPosition = iPositionIs;
                        }
                    }

                    String timePosition = ContextData.positionData.get(iPosition).split(",")[0];
                    double timePst = (double) Long.parseLong(timePosition);
                    //double timePst = timeStart;


                    Log.e(LOG_TAG, " Time DB Norm: " + timeStart + " Time GPS: " + timePst);
                    //Restriction created to be sure that the data saved is actually as close as possible, considering the time.

                    // Log.e(LOG_TAG, "timeStart: " + timeStart + " timePST: " + timePst + " timeORT: " + timeOrt);
                    // if ((timePst >= timeStart)){// && (timePst >= timeStart)){// && ((timePst >= timeStart) && (timePst <= timeEnd))) {
                    Log.d(LOG_TAG, "time Pst: " + timePst );

                    double timeOffsetPst = Math.abs(timePst - timeis);
                    Log.e(LOG_TAG, "Position time Position: " + iPosition);
                    latitude = ContextData.positionData.get(iPosition).split(",")[1];
                    longitude = ContextData.positionData.get(iPosition).split(",")[2];

                    String timeStr = String.valueOf(timeis);
                    line2write = timeStr + space + avalue + space + latitude + space + longitude;
                    //Log.e(LOG_TAG, "Line 2 write : (" + i + ") " + line2write);
                    outputStream.write(line2write.getBytes());
                    space = ",\n";
                    outputStream.write(space.getBytes());
                    //}
                }
                timeis = timeis + slot;
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        Log.d(LOG_TAG , "End of write ");
        this.fileChangedSendEvent(SAVING_SOUND_HAS_COMPLETED);
        return 1;
    }


    /**
     * finds intersecion time
     * @param timeBaseLine
     * @param time2compare
     * @param isFirstStep
     * @param isLowerThenTreshold
     * @return time-related information
     */
    public String findIntersectionTime(Long timeBaseLine, ArrayList<String> time2compare, boolean isFirstStep, boolean isLowerThenTreshold) {
        if(isLowerThenTreshold)
        {
            Log.e(LOG_TAG, "Array time lower then treshold is: " + Arrays.deepToString(time2compare.toArray()));
            int index = (time2compare.size() - 1);
            return index + "," + time2compare.get(index);
        }else {
            for (int i = 0; i < time2compare.size(); i++) {
                Long timeProtocolIs = Long.parseLong(time2compare.get(i).split(",")[0]);
                if (isFirstStep) {
                    Log.e(LOG_TAG, "time is: " + timeProtocolIs);
                    if (timeProtocolIs >= timeBaseLine) {
                        return i + "," + time2compare.get(i);
                    }
                } else {
                    if (timeProtocolIs >= timeBaseLine) {
                        Log.e(LOG_TAG, "time Protocol is: " + timeProtocolIs);
                        Long timeOld = Long.parseLong(time2compare.get(i - 1).split(",")[0]);
                        Log.e(LOG_TAG, "time Protocol was: " + timeOld);
                        Long timeOffsetCurrent = Math.abs(timeProtocolIs - timeBaseLine);
                        Long timeOffsetOld = Math.abs(timeOld - timeBaseLine);
                        if (i + 1 == time2compare.size()) {
                            if (timeOffsetCurrent < timeOffsetOld)
                                return i + "," + time2compare.get(i);
                            else
                                return (i - 1) + "," + time2compare.get(i - 1);
                        } else {
                            Long timeFuture = Long.parseLong(time2compare.get(i + 1).split(",")[0]);
                            Log.e(LOG_TAG, "time Protocol will be: " + timeFuture);
                            Long timeOffsetFuture = Math.abs(timeFuture - timeBaseLine);
                            if (timeOffsetCurrent < timeOffsetOld)
                                if (timeOffsetCurrent < timeOffsetFuture)
                                    return i + "," + time2compare.get(i);
                                else
                                    return (i + 1) + "," + time2compare.get(i);
                            else if (timeOffsetOld < timeOffsetFuture)
                                return (i - 1) + "," + time2compare.get(i - 1);
                            else
                                return (i + 1) + "," + time2compare.get(i + 1);
                        }
                    }
                }
            }
        }
        return null;
    }





    /**
     * write in the file the raw sound provided by buffer  starting at synchronised time starttime and with a time slot
     * between two sound record given by slot
     * @param buffer sound measurement (i.e. buffer)
     * @param startTime time when we started recording
     * @param slot delay between two measurements
     * @param remotebuffer other sound we need to synchronise with
     * @param delay bias
     * @return if the temporal sound is stored in a file
     */
    public boolean re_write_delay_calibration_buffer(DoubleBuffer buffer, double startTime , double slot,DoubleBuffer remotebuffer, long delay)
    {
        Log.d(LOG_TAG , "write calibration buffer thread, start at " + startTime);
        double[] pcmBuffer = buffer.array();// convert array of short to array of double

        Log.d(LOG_TAG,"recording start time is " + startTime);
        Log.d(LOG_TAG, "recording rate is " + Record._rate);
        Log.d(LOG_TAG,"slot is "+ slot);

        double timeis =  startTime;

        boolean isStillblanck = false; // is unsed to see if we are still at the beginning of the audio record
        // and there is 0 noise recorded
        // start to write into a file  line format is time,noise,\n
        try {
            for (int i = 0; i < pcmBuffer.length; i++) {
                String space = ",";
                String avalue = String.valueOf(pcmBuffer[i] );//extract the sound

                if (isStillblanck && avalue.compareTo("0.0") == 0 ){
                    //nothing to do
                    Log.e(LOG_TAG, avalue +  "is equal to 0");
                }else{
                    isStillblanck = false;

                    //write local time
                    String timeStr = String.valueOf(timeis);

                    Log.e(LOG_TAG, "write " + timeStr );

                    outputStream.write(timeStr.getBytes());

                    //write ","
                    outputStream.write(space.getBytes()); //  Log.d(LOG_TAG , "write :"+timeis + ","+pcmBuffer[i] );
                    outputStream.write(avalue.getBytes());

                    //write ",\n"
                    space = ",\n";
                    outputStream.write(space.getBytes());
                }
                timeis = timeis + slot;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Log.d(LOG_TAG , "End of write ");
        this.fileChangedSendEvent(SAVING_SOUND_HAS_COMPLETED);
        return true;
    }







    /**
     * write in the file the raw sound provided by buffer  starting at synchronised time starttime and with a time slot
     *      * between two sound record given by slot
     * @param buffer sound measurements
     * @param startTime time when we start recording
     * @param slot detaly between two measruements
     * @return is equal to true if the spatial-sound could be stored. Otherwise, is equal to 0
     */
    public boolean write_calibration_buffer(DoubleBuffer buffer, double startTime , double slot  )
    {
        Log.d(LOG_TAG , "into the write calibration buffer thread, start at " + startTime);
        double[] pcmBuffer = buffer.array();// convert array of short to array of double

        Log.d(LOG_TAG,"recording start time is " + startTime);
        Log.d(LOG_TAG, "recording rate is " + Record._rate);
        Log.d(LOG_TAG,"slot is "+ slot);


        double timeis =  startTime;

        boolean isStillblanck = false; // is unsed to see if we are still at the beginning of the audio record
        // and there is 0 noise recorded
        // start to write into a file  line format is time,noise,\n
        try {
            for (int i = 0; i < pcmBuffer.length; i++) {
                String space = ",";
                String avalue = String.valueOf(pcmBuffer[i] );//extract the sound

                //todo remove the if condition to stop cleaning the data
                if (isStillblanck && avalue.compareTo("0.0") == 0 ){
                    //nothing to do
                    Log.e(LOG_TAG, avalue +  "is equal to 0");
                }else{
                    isStillblanck = false;

                    //write local time
                    String timeStr = String.valueOf(timeis);

                    Log.e(LOG_TAG, "write " + timeStr );

                    outputStream.write(timeStr.getBytes());

                    //write ","
                    outputStream.write(space.getBytes()); //  Log.d(LOG_TAG , "write :"+timeis + ","+pcmBuffer[i] );
                    outputStream.write(avalue.getBytes());

                    //write ",\n"
                    space = ",\n";
                    outputStream.write(space.getBytes());
                }
                timeis = timeis + slot;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Log.d(LOG_TAG , "End of write ");
        this.fileChangedSendEvent(SAVING_SOUND_HAS_COMPLETED);
        return true;
    }





    /**
     * write in the file the raw sound provided by buffer  starting at synchronised time starttime and with a time slot
     *  between two sound record given by slot
     * @param buffer sound measurements
     * @param startTime time at which we begin recording
     * @param slot delay between two measurements
     * @param ctx context
     * @return is equal to true if the spatial-sound could be stored. Otherwise, is equal to 0
     */
    public boolean write_calibration_buffer_recordActiv(DoubleBuffer buffer,double startTime , double slot, ContextData ctx  )
    {
        Log.d(LOG_TAG , "into the write calibration buffer thread ");
        double[] pcmBuffer = buffer.array();// convert array of short to array of double
        //double[] rawPcmBuffer = new double[ctx.rawBuffer.size()];// convert array of short to array of double
        double[] rawPcmBuffer = ContextData.rawBuffer;

        /*for(int k = 0; k < ctx.rawBuffer.size(); k++)
        {
            rawPcmBuffer[k] = ctx.rawBuffer.get(k);
        }*/
        long samplyFrequency = Record._rate ; // extract the recording rate

        Log.d(LOG_TAG,"recording start time is " + startTime);
        Log.d(LOG_TAG, "recording rate is " + Record._rate);
        Log.d(LOG_TAG,"slot is "+ slot);


        double timeis = startTime;

        // todo add the cleaning of the beginning
        boolean isStillblanck = false; // is unsed to see if we are still at the beginning of the audio record
        // and there is 0 noise recorded
        // start to write into a file  line format is time,noise,\n
        try {
            for (int i = 0; i < pcmBuffer.length; i++) {
                String space = ",";
                String avalue = String.valueOf(pcmBuffer[i] );//extract the sound
                String arawvalue = String.valueOf(rawPcmBuffer[i] );//extract the raw sound

                //todo remove the if condition to stop cleaning the data
                if (isStillblanck && avalue.compareTo("0.0") == 0 ){
                    //nothing to do
                    Log.e(LOG_TAG, avalue +  "is equal to 0");
                }else{
                    isStillblanck = false;

                    //write local time
                    String timeStr = String.valueOf(timeis);
                    outputStream.write(timeStr.getBytes());

                    //write ","
                    outputStream.write(space.getBytes()); //  Log.d(LOG_TAG , "write :"+timeis + ","+pcmBuffer[i] );
                    outputStream.write(avalue.getBytes());
                    outputStream.write(space.getBytes());
                    outputStream.write(arawvalue.getBytes());
                    //write ",\n"
                    space = ",\n";
                    outputStream.write(space.getBytes());
                }
                timeis = timeis + slot;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Log.d(LOG_TAG , "End of write ");
        this.fileChangedSendEvent(SAVING_SOUND_HAS_COMPLETED);
        return true;
    }


    /**
     * add the sting at the end of the file
     * @param aTextToWrite string to add at the end of the file
     * @return is equal to true if the spatial-sound could be stored. Otherwise, is equal to 0
     */
    public boolean write_txt(String aTextToWrite)
    {
        try {
            //write the provided text
            outputStream.write(aTextToWrite.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            this.reset();
            return false;
        }
        this.reset();
        return true;
    }


    /**
     * return the content of the file
     * @return content of the file
     */
    public StringBuilder read_txt(){
        // Log.e(LOG_TAG,"begin read file");
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                // Log.e(LOG_TAG,"readline" + line);
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.reset();//place at the beginning of the file for the next read
            return null;
        }
        // Log.e(LOG_TAG,"before reset");
        this.reset();//place at the beginning of the file for the next read
        // Log.e(LOG_TAG,"after");
        return sb;
    }

    /**
     * scan a file
     * @return content of the scanned file
     */
    public StringBuilder scan(){
        List<Double> words = new ArrayList<Double>();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        int i =1;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append("line ");
                sb.append(i);
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
                i=i+1;
                Scanner s = new Scanner(line).useDelimiter(",");
                //  String x;
                Double x,y;
                sb.append("X:");
                if (s.hasNextDouble ()== true)
                {
                    x = new Double(s.nextDouble());
                    sb.append(x);
                    sb.append("Y:");
                    if (s.hasNextDouble ()== true)
                    {
                        y = new Double(s.nextDouble());
                        sb.append(y);
                        words.add(x);
                        words.add(y);
                    }
                }
            }
        } catch (IOException e) {
            this.reset();//place at the beginning of the file for the next read
            e.printStackTrace();
        }
        this.reset();//place at the beginning of the file for the next read
        return sb;
    }

    /**
     * extract the sound
     * @return sound
     */
    public  DoubleBuffer getSound(){
        Log.e(LOG_TAG, "begin get sound");
        this.reset();
//        LineNumberReader  lnr = new LineNumberReader(new FileReader(new File("File1")));
        try {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
            reader.skip(Long.MAX_VALUE);
//            System.out.println(reader.getLineNumber() + 1); //Add 1 because line index starts at 0
            Log.e(LOG_TAG, "size:" + reader.getLineNumber());
            DoubleBuffer noise =  DoubleBuffer.allocate(reader.getLineNumber() + 1);
            reader.close();

            Log.e(LOG_TAG , "extract sound " + this.getFilename());
            this.reset();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            int i =1;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    // Log.e(LOG_TAG , "SCAN2LIST explore line " + line);
                    i=i+1;
                    Scanner s = new Scanner(line).useDelimiter(",");

                    Double x,y= new Double(0);

                    if (s.hasNext ()== true)
                    {
                        x = new Double(s.next());

                        if (s.hasNext ()== true)// extract next positive number
                        {
                            y = new Double(s.next());
                            //words.add(x);

                            noise.put(y);
                        }
                        //   Log.e(LOG_TAG , "x: " + x + ", y:" + y);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.reset();//place at the beginning of the file for the next read
                return noise;
            }
            this.reset();//place at the beginning of the file for the next read
            Log.e(LOG_TAG,"end of get sound");
            return noise;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Weight the sound with provided parameters (slope and intercept) and store it
     * @param slope  slope that serve as weight
     * @param intercept intercept that serve to weight
     * @param provided_file_name name of the file containing the sound that will be weighted
     * @param ctx  context
     * @return file manager
     */
    public FileManager getWeightedSound(double slope, double intercept, String provided_file_name, Context ctx){

  //  List<Double> words = new ArrayList<Double>();
    InputStreamReader isr = new InputStreamReader(inputStream);
    BufferedReader bufferedReader = new BufferedReader(isr);
    StringBuilder sb = new StringBuilder();
    String line;
     try {
        while ((line = bufferedReader.readLine()) != null) {
            Log.e("RR", "line" + line);//sb.append("line "); sb.append(i); // sb.append(": "); //  sb.append(line);
           // sb.append("\n");           // i=i+1;
            Scanner s = new Scanner(line).useDelimiter(",");
            Double x,y;


         if (s.hasNext ()== true)
            {

                x = new Double(s.next());
                Log.e(LOG_TAG , "x: " + x );

                sb.append(x);
                Log.e("RR",x.toString());
                sb.append(",");
                if (s.hasNext ()== true)
                {
                    y = new Double(s.next()) * slope + intercept;
                    sb.append(y);
                    Log.e("RR", String.valueOf(y));
                    sb.append("\n");
                 }
            }
        }
    } catch (IOException e) {
        this.reset();//place at the beginning of the file for the next read
        e.printStackTrace();
    }


    this.reset();//place at the beginning of the file for the next read
    FileManager f = new FileManager( provided_file_name, false, ctx);
    f.write_txt(sb.toString());

    return f;
}

    /**
     * return when the sound started being recorded
     * @return time
     */
    public double  getStartTime (){
        Log.e(LOG_TAG, "extract the start time");

        this.reset();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;

        try {
            if ((line = bufferedReader.readLine()) != null) {
                Log.e(LOG_TAG , "get time explore line " + line);

                Scanner s = new Scanner(line).useDelimiter(",");
                Double x ;

                if (s.hasNext ()== true)
                {
                    x = new Double(s.next());
                    Log.e(LOG_TAG , "x: " + x );
                    return x;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.reset();//place at the beginning of the file for the next read
            return 0;
        }
        this.reset();//place at the beginning of the file for the next read
        Log.e(LOG_TAG,"end of get time");
        return 0;
    }

    /**/

    /**
     *extract from the file the content and provide the related list
     * @return content
     */
    public List<Double> scan2list(){
        List<Double> words = new ArrayList<Double>();
        Log.e(LOG_TAG , "SCAN2LIST " + this.getFilename());
        this.reset();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        int i =1;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                // Log.e(LOG_TAG , "SCAN2LIST explore line " + line);
                i=i+1;
                Scanner s = new Scanner(line).useDelimiter(",");

                Double x,y= new Double(0);

                if (s.hasNext ()== true)
                {
                    x = new Double(s.next());

                    if (s.hasNext ()== true)// extract next positive number
                    {
                        y = new Double(s.next());
                        words.add(x);
                        words.add(y);
                    }
                    //   Log.e(LOG_TAG , "x: " + x + ", y:" + y);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.reset();//place at the beginning of the file for the next read
            return words;
        }
        this.reset();//place at the beginning of the file for the next read
        Log.e(LOG_TAG,"end of scantolist");
        return words;
    }

    /**
     * close the file
     * @return the file could be closed
     */
    public int close (){
        try {
            outputStream.close();
            inputStream.close();
            isclosed = true;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    /**
     * plot the time series (local and remote)
     * @param plot plot
     * @param ctx context
     * @param aremoteFileManager file when the time series are stored
     * @param upto up to how many elements should be ploted
     */
    public void plot_old_calibration(XYPlot plot, Context ctx, FileManager aremoteFileManager, int upto){
        //extract the data to plot X and Y
        List<Double> xy_calibration = new ArrayList<Double>();
        xy_calibration.addAll(this.getNewCommonTimeSeries(aremoteFileManager, upto ));
        Log.e( LOG_TAG,"Size to the sample to plot "+ xy_calibration.size() );
        Log.e( LOG_TAG, xy_calibration.toString());

        List<Double> xy_plot = new ArrayList<Double>();
        int i;
        for(i=0; i<xy_calibration.size() ; i++) {
            Double d = new Double (i);
            xy_plot.add(xy_calibration.get(i));

            Log.e(LOG_TAG, "get: " + xy_calibration.get(i));
        }
        plot.setRangeBoundaries(-500, 500, BoundaryMode.FIXED);

        plot.setDomainBoundaries(-1000, 1000, BoundaryMode.FIXED );
        Log.e(LOG_TAG, "domain step value "+ 	plot.getDomainStepValue());

        // extract local calibration so as to plot it
        List<Double> local_calibration = new ArrayList<Double>();
        local_calibration.addAll(this.scan2list());

        // extract remote calibration so as to plot it
        List<Double> remote_calibration = new ArrayList<Double>();
        remote_calibration.addAll( aremoteFileManager.scan2list());

        // turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                xy_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED , ctx.getString( R.string.plot_series1_name));
        Number maxX = plot.getCalculatedMaxX();
        Log.e(LOG_TAG, "max X: " +  maxX.toString() );
        //Log.e(LOG_TAG, "top max : " + plot.getRangeTopMax().toString());
        XYSeries series2 = new SimpleXYSeries(
                remote_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, ctx.getString( R.string.plot_series2_name) );

        Log.e(LOG_TAG, "series 1 size " + series1.size() );
        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        // series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(ctx, R.xml.point_formatter);
        //series1Format.configure(ctx, R.xml.line_point_formatter_with_labels);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        //series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(ctx, R.xml.point_formatter_2);
        //series2Format.configure(ctx, R.xml.line_point_formatter_with_labels_2);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {

                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));

        //  for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series2Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series2, series2Format);
        plot.addSeries(series1, series1Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        Log.e(LOG_TAG, "domain label: "+ plot.getDomainLabel());
        // Log.e(LOG_TAG, "domain left max" + 	plot.getDomainLeftMax().toString());
        //  Log.e(LOG_TAG, " domain left min" +	plot.getDomainLeftMin().toString());
        //    Log.e(LOG_TAG, "domain orgigin" + 	plot.getDomainOrigin().toString());
        //    Log.e(LOG_TAG, " domain right max" +  	plot.getDomainRightMax().toString());
        //    Log.e(LOG_TAG, " domain right min"+ 	plot.getDomainRightMin());
        Log.e(LOG_TAG, "getRangeBottomMax()" + plot.getRangeBottomMax());

        //  Number 	getRangeBottomMin()

    }



    /**
     * plot the measurements and the regression line
     * @param plot plot
     * @param ctx context
     * @param aremoteFileManager file name where measurements are provided
     * @param upto up to how many elements should be ploted
     */
    public void plot_calibration(XYPlot plot, Context ctx, FileManager aremoteFileManager, int upto) {
        //extract the data to plot X and Y
        // ArrayList<ArrayList<Double>> matrix = localFile4Noise.extractCommontimeSeries(remoteFilelist, ctxData);

        List<Double> xy_calibration = new ArrayList<Double>();
        Log.e( LOG_TAG,"Local  file to extract "+ this.getFilename());

        Log.e( LOG_TAG,"Remote file to extract "+ aremoteFileManager.getFilename() );

        xy_calibration.addAll(this.getNewCommonTimeSeries(aremoteFileManager, upto));
        Log.e( LOG_TAG,"Size of the sample to plot "+ xy_calibration.size() );
        Log.e( LOG_TAG, xy_calibration.toString());

        double maxX= getMaxXFromTimeSeries(xy_calibration);// double maxY =getMaxYFromTimeSeries(xy_calibration);
        double minX = getMinXFromTimeSeries(xy_calibration);// double minY = getMinYFromTimeSeries(xy_calibration);

        Log.e(LOG_TAG , "getMaxX" + getMaxXFromTimeSeries(xy_calibration));//        Log.e(LOG_TAG , "getMAxY" + getMaxYFromTimeSeries(xy_calibration));
        Log.e(LOG_TAG , "get Min X"+ getMinXFromTimeSeries(xy_calibration));//      Log.e(LOG_TAG ,"get min Y" + getMinYFromTimeSeries(xy_calibration));

        plot.setRangeBoundaries(getMinYFromTimeSeries(xy_calibration) -5 , getMaxYFromTimeSeries(xy_calibration) +5, BoundaryMode.FIXED);
        plot.setDomainBoundaries(minX-5, maxX+5, BoundaryMode.FIXED );// X

        // plot the simple regression function
        //todo impossible to deal with multiple regression
        Simpleregression myRegression = new Simpleregression(xy_calibration);
        // compute f(minX) = slope * minX + intercept //compute f(maxX)  =  slope * minX + intercept
        double minY = myRegression.slope * minX + myRegression.intercept;
        double maxY =  myRegression.slope * maxX + myRegression.intercept;
        double middleX =   (minX + maxX)/2;
        double middleY =  myRegression.slope * middleX + myRegression.intercept;

        Log.e(LOG_TAG, "minX: " + minX +" minY: "+ minY+" middleX: "+middleX+" middleY: " + middleY);
        Log.e(LOG_TAG, "maxX: " + maxX+ " maxY: " + maxY);
        Log.e(LOG_TAG, "slope: " + myRegression.slope +" intercept: " + myRegression.intercept);

        Number[] series2Numbers = {minX, minY, middleX,middleY ,maxX, maxY};

        // plot the robust simple regression
        // sr.getSubSample(4) ; // extract a random sample of 4 elements
        myRegression.findBestRegression();
        myRegression.buildRLSRegression();

        double cleaned_minY = myRegression.cleanedRegression.slope * minX + myRegression.cleanedRegression.intercept;
        double cleaned_maxY = myRegression.cleanedRegression.slope * maxX + myRegression.cleanedRegression.intercept;
        double cleaned_middleY = myRegression.cleanedRegression.slope * middleX + myRegression.cleanedRegression.intercept;

        Log.e(LOG_TAG, "minX: " + minX +"cleanedminY: "+ cleaned_minY+"middleX: "+ middleX+"cleaned middleY: " + cleaned_middleY);
        Log.e(LOG_TAG, "maxX: " + maxX+ "maxY: " + cleaned_maxY);
        Log.e(LOG_TAG, "slope: " + myRegression.cleanedRegression.slope +"intercept: " + myRegression.cleanedRegression.intercept);

        Number[] series3Numbers = {minX, cleaned_minY, middleX, cleaned_middleY ,maxX, cleaned_maxY};

        ////////////////////////////////////
        Log.e(LOG_TAG, "size to plot" + xy_calibration.size() );
        XYSeries series1 = new SimpleXYSeries(xy_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED , "Samples");
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Line fit");
        XYSeries series3 = new SimpleXYSeries(Arrays.asList(series3Numbers), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Robust Line fit");

        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        //  series1Format.configure(ctx, R.xml.point_formatter);



        series1Format.getLinePaint().setColor(Color.BLACK); // lines between points
        series1Format.getLinePaint().setStrokeWidth(0.05f);
        series1Format.getFillPaint().setColor(Color.TRANSPARENT);
        series1Format.getVertexPaint().setStrokeWidth(4f);
        series1Format.getVertexPaint().setColor(Color.GREEN ); // color of the vertex
        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        ///////////////////


        LineAndPointFormatter series2Format = new LineAndPointFormatter();
//        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        //  series2Format.configure(ctx, R.xml.line_point_formatter_with_labels_2);

        series2Format.getVertexPaint().setColor( Color.BLUE/*Color.rgb(255,204,102)*/); // color of the vertex Color.rgb(255,204,102)
        series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        series2Format.getLinePaint().setColor(Color.BLUE); // lines between points
        series2Format.getLinePaint().setStrokeWidth(5f);
        series2Format.getFillPaint().setColor(Color.TRANSPARENT);
        series2Format.getVertexPaint().setStrokeWidth(5f);
        series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        //format for serie 3 (robust
        LineAndPointFormatter series3Format = new LineAndPointFormatter();
        // series3Format.setPointLabelFormatter(new PointLabelFormatter());
        //series3Format.configure(ctx, R.xml.line_point_formatter_with_labels);
        series3Format.getVertexPaint().setColor( Color.RED/*Color.rgb(255,204,102)*/); // color of the vertex Color.rgb(255,204,102)
        series3Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        series3Format.getLinePaint().setColor(Color.RED); // lines between points
        series3Format.getLinePaint().setStrokeWidth(5f);
        series3Format.getFillPaint().setColor(Color.TRANSPARENT);
        series3Format.getVertexPaint().setStrokeWidth(5f);
        // add an "dash" effect to the series2 line:
       /* series3Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));*/



        // reduce the number of range labels
        plot.setTicksPerRangeLabel(2);
        // add each series to the xyplot:
        plot.addSeries(series1, series1Format);//series1Format
        plot.addSeries(series2, series2Format); // line fit
        plot.addSeries(series3, series3Format); // robust line fit

        xy_calibration.clear();
    }


    /*
        // plot XY and the regression line
    public void plot_calibration(XYPlot plot, Context ctx, FileManager aremoteFileManager, int upto) {

        //extract the data to plot X and Y
        List<Double> xy_calibration = new ArrayList<Double>();
        xy_calibration.addAll(this.getNewCommonTimeSeries(aremoteFileManager, upto));
        Log.e( LOG_TAG,"Size to the sample to plot "+ xy_calibration.size() );
        Log.e( LOG_TAG, xy_calibration.toString());

        double maxX= getMaxXFromTimeSeries(xy_calibration);// double maxY =getMaxYFromTimeSeries(xy_calibration);
        double minX = getMinXFromTimeSeries(xy_calibration);// double minY = getMinYFromTimeSeries(xy_calibration);

       Log.e(LOG_TAG , "getMaxX" + getMaxXFromTimeSeries(xy_calibration));
//        Log.e(LOG_TAG , "getMAxY" + getMaxYFromTimeSeries(xy_calibration));
        Log.e(LOG_TAG , "get Min X"+ getMinXFromTimeSeries(xy_calibration));
  //      Log.e(LOG_TAG ,"get min Y" + getMinYFromTimeSeries(xy_calibration));

        plot.setRangeBoundaries(getMinYFromTimeSeries(xy_calibration) -5 , getMaxYFromTimeSeries(xy_calibration) +5, BoundaryMode.FIXED);
        plot.setDomainBoundaries(minX-5, maxX+5, BoundaryMode.FIXED );// X

        // plot the simple regression function
        //todo deal with multiple regression
        Simpleregression myRegression = new Simpleregression(xy_calibration);
        // compute f(minX) = slope * minX + intercept //compute f(maxX)  =  slope * minX + intercept
        double minY = myRegression.slope * minX + myRegression.intercept;
        double maxY =  myRegression.slope * maxX + myRegression.intercept;
        double middleX =   (minX + maxX)/2;
        double middleY =  myRegression.slope * middleX + myRegression.intercept;

        Log.e(LOG_TAG, "minX: " + minX +"minY: "+ minY+"middleX: "+middleX+"middleY: " + middleY);
        Log.e(LOG_TAG, "maxX: " + maxX+ "maxY: " + maxY);
        Log.e(LOG_TAG, "slope: " + myRegression.slope +"intercept: " + myRegression.intercept);

        Number[] series2Numbers = {minX, minY, middleX,middleY ,maxX, maxY};
        XYSeries series1 = new SimpleXYSeries(xy_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED , "Samples");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Line fit");

        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.configure(ctx, R.xml.point_formatter);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(ctx, R.xml.line_point_formatter_with_labels_2);

        series2Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        // add each series to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format); // line fit
        xy_calibration.clear();
    }
    */




    /* */

    /**
     * plot the two time series (local and remote)
     * @param plot plot
     * @param ctx context
     * @param remoteFileManager file name where information is stored
     */
    public void plot_several_calibration(XYPlot plot, Context ctx, List<FileManager>  remoteFileManager){
        // extract local calibration so as to plot it
        List<Double> local_calibration = new ArrayList<Double>();
        local_calibration.addAll(this.scan2list());
        XYSeries series1 = new SimpleXYSeries(local_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED , "Local Samples"/* ctx.getString( R.string.plot_series1_name)*/);
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.getLinePaint().setColor(Color.BLACK); // lines between points
        series1Format.getLinePaint().setStrokeWidth(0.05f);
        series1Format.getFillPaint().setColor(Color.TRANSPARENT);
        series1Format.getVertexPaint().setStrokeWidth(4f);
        series1Format.getVertexPaint().setColor(Color.YELLOW); // color of the vertex
        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        plot.addSeries(series1, series1Format);

        //series1Format.configure(ctx, R.xml.point_formatter);
        //        series2Format.configure(ctx, R.xml.point_formatter_2);

        List<Integer> colorList = new ArrayList<Integer>();
        colorList.add(Color.MAGENTA);// Color.rgb(255,204,102) );
        //
        //colorList.add( Color.rgb(255,204,102) );
        //   colorList.add(Color.rgb(247, 139, 106));
        colorList.add(Color.RED);//rgb(245, 106, 247));
        colorList.add(Color.CYAN); //.rgb(247, 247, 106));
        colorList.add(Color.GREEN);//rgb(172,247,106));

        // create formatters to use for drawing a series using LineAndPointRenderer

        // extract remote calibration so as to plot it
        for (int i = 0 ; i< remoteFileManager.size() ; i++){
            List<Double> remote_calibration = new ArrayList<Double>();
            remote_calibration.addAll( remoteFileManager.get(i).scan2list());
            LineAndPointFormatter series2Format = new LineAndPointFormatter();
            series2Format.getLinePaint().setColor(Color.BLACK); // lines between points
            series2Format.getLinePaint().setStrokeWidth(0.1f);
            series2Format.getFillPaint().setColor(Color.TRANSPARENT);
            series2Format.getVertexPaint().setStrokeWidth(4f);
            // add an "dash" effect to the series2 line:
            series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {
                    PixelUtils.dpToPix(20),
                    PixelUtils.dpToPix(15)}, 0));
            //  for fun, add some smoothing to the lines:
            // see: http://androidplot.com/smooth-curves-and-androidplot/
            series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

            series2Format.getVertexPaint().setColor(colorList.get(i)); // color of the vertex = yellow
            XYSeries series2 = new SimpleXYSeries(remote_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, ctx.getString( R.string.plot_series2_name) );

            // add a new series' to the xyplot:
            plot.addSeries(series2, series2Format);
            remote_calibration.clear();
        }
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        local_calibration.clear();
    }


     /**
     * plot the two time series (local and remote)
     * @param plot plot
     * @param ctx context
     * @param aremoteFileManager file name where information is stored
     */

    public void plot_both_old_calibration(XYPlot plot, Context ctx, FileManager aremoteFileManager){
        // extract local calibration so as to plot it
        List<Double> local_calibration = new ArrayList<Double>();
        local_calibration.addAll(this.scan2list());

        // extract remote calibration so as to plot it
        List<Double> remote_calibration = new ArrayList<Double>();
        remote_calibration.addAll( aremoteFileManager.scan2list());

        // turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                local_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED , ctx.getString( R.string.plot_series1_name));

        XYSeries series2 = new SimpleXYSeries(
                remote_calibration , SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, ctx.getString( R.string.plot_series2_name) );

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        LineAndPointFormatter series2Format = new LineAndPointFormatter();

        series1Format.getLinePaint().setColor(Color.BLACK); // lines between points
        series1Format.getLinePaint().setStrokeWidth(0.05f);
        series1Format.getFillPaint().setColor(Color.TRANSPARENT);
        series1Format.getVertexPaint().setStrokeWidth(4f);

        series2Format.getLinePaint().setColor(Color.BLACK); // lines between points
        series2Format.getLinePaint().setStrokeWidth(0.1f);
        series2Format.getFillPaint().setColor(Color.TRANSPARENT);
        series2Format.getVertexPaint().setStrokeWidth(4f);

        series1Format.getVertexPaint().setColor(Color.RED);//BLUE); // color of the vertex

        series2Format.getVertexPaint().setColor(Color.rgb(255,204,102)); // color of the vertex = yellow

        //series1Format.configure(ctx, R.xml.point_formatter);
        //        series2Format.configure(ctx, R.xml.point_formatter_2);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {
                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));

        //  for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        local_calibration.clear();
        remote_calibration.clear();
    }

    /*
     *scan the local and remote calibration file
     * so as to extract the samples that have been taken at the same time
     */
    public StringBuilder printCommonTimeSeries(FileManager aremoteFileManager) {
        //returned string
        StringBuilder sb = new StringBuilder();
        // extract local samples
        List<Double> local = new ArrayList<Double>();
        local.addAll(this.scan2list());
        //extract remote samples
        List<Double> remote = new ArrayList<Double>();
        remote.addAll(aremoteFileManager.scan2list());

        //iterator on the local and remote list
        int local_i, remote_i;
        remote_i =0 ;
        //go through the local list
        sb.append("local size:" + local.size());
        sb.append("  remote size:" + remote.size()+ "\n");

        for (local_i = 0 ; local_i +1 < local.size() ; local_i = local_i+2){
            sb.append("local line: "+local_i + " remote line: "+remote_i+"\n");
            // extract x and y from the local file
            double local_x = local.get(local_i);
            double local_y = local.get(local_i +1);
            sb.append ("local  -    X: " + local_x + "   Y: " + local_y + "\n");

            boolean stop = false; // stop looking for next remote sample

            // go through the remote list
            while (remote_i +1 < remote.size() && stop == false ){
                //extract x and y from the remote file
                double remote_x = remote.get(remote_i);
                double remote_y = remote.get(remote_i +1);
                sb.append ("remote - X: " + remote_x+ " Y: " + remote_y + "\n");

                if (local_x < remote_x){
                    sb.append ("break x<y\n\n");
                    stop = true; // stop looking for remote sample taken at the same time
                }
                // check if the sample have the same x (i.e. local_x = remote_x
                if (local_x == remote_x) {
                    sb.append("egality (" + local_y +", "+remote_y+")\n\n");
                    stop = true; // stop looking for remote sample taken at the same time
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
                if (local_x > remote_x){
                    sb.append ("next remote sample \n");
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
            }
        }
        sb.append("Stop extracting time series ");
        return sb;
    }


    /**
     * scan a line
     *
     * @param line to scan
     * @return the scanned list
     */
    public List<Double> scanLine( String line ){
        List<Double> words = new ArrayList<Double>();

        if (line  != null) {
            // Log.e(LOG_TAG , " line: " + line);
            Scanner s = new Scanner(line).useDelimiter(",");
            Double x,y= new Double(0);

            if (s.hasNext ()== true)
            {
                x = new Double(s.next());

                if (s.hasNext ()== true)// extract next positive number
                {
                    y = new Double(s.next());
                    words.add(x);
                    words.add(y);
                }
                //     Log.e(LOG_TAG , "x: " + x + ", y:" + y);
            }
            else{
                Log.e(LOG_TAG , "No line ");
            }
        }
        return words;
    }


    /**
     * return the scanned parameters
     * @param line line to scan
     * @return scanned parameters
     */
    public List<Double> scanLineOrtGps( String line ){
        List<Double> words = new ArrayList<Double>();

        if (line  != null) {
            Log.e(LOG_TAG , " line: " + line);
            Scanner s = new Scanner(line).useDelimiter(",");
            Double x,y,ortX,ortY,ortZ,lat,longi = new Double(0);

            if (s.hasNext ()== true)
            {
                x = new Double(s.next());

                if (s.hasNext ()== true)// extract next positive number
                {
                    y = new Double(s.next());

                    if(s.hasNext() == true){

                        ortX = new Double(s.next());

                        if(s.hasNext() == true){

                            ortY = new Double(s.next());

                            if(s.hasNext() == true){

                                ortZ = new Double(s.next());

                                if(s.hasNext() == true){

                                    lat = new Double(s.next());

                                    if(s.hasNext() == true){

                                        longi = new Double(s.next());

                                        words.add(x);
                                        words.add(y);
                                        words.add(ortX);
                                        words.add(ortY);
                                        words.add(ortZ);
                                        words.add(lat);
                                        words.add(longi);
                                        Log.e(LOG_TAG , "x: " + x + ", y:" + y + ", ortX:" + ortX + ", ortY:" + ortY + ", ortZ:" + ortZ + ", lat:" + lat + ", longi:" + longi);
                                    }

                                }


                            }

                        }


                    }

                }

            }
            else{
                Log.e(LOG_TAG , "No line ");
            }
        }
        return words;
    }

    /**
     * scan the parameters
     * @param line line 2 scan
     * @return scanned parameters
     */
    public List<Double> scanLineGps( String line ){
        List<Double> words = new ArrayList<Double>();

        if (line  != null) {
            Log.e(LOG_TAG , " line: " + line);
            Scanner s = new Scanner(line).useDelimiter(",");
            Double x,y,ortX,ortY,ortZ,lat,longi = new Double(0);

            if (s.hasNext ()== true)
            {
                x = new Double(s.next());

                if (s.hasNext ()== true)// extract next positive number
                {
                    y = new Double(s.next());

                    if(s.hasNext() == true){

                        lat = new Double(s.next());

                        if(s.hasNext() == true){
                            longi = new Double(s.next());
                            words.add(x);
                            words.add(y);
                            words.add(lat);
                            words.add(longi);
                            Log.e(LOG_TAG , "x: " + x + ", y:" + y + ", lat:" + lat + ", longi:" + longi);
                        }

                    }


                }

            }
            else{
                Log.e(LOG_TAG , "No line ");
            }
        }
        return words;
    }




    /**
     *  scan a matrix
     * @param _matrix mastric to scan
     * @return scanned matric
     */
    public ArrayList <ArrayList<Double>> extract(ArrayList <ArrayList<Double>> _matrix) {
        // put the provided file into a list and return that list
        if( _matrix == null) {
            List<Double> time= new ArrayList<Double>(); // time vector
            List<Double> y = new ArrayList<Double>();   // value vector
            ArrayList <ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(); // matrix vector
            // open local file and place at the beginning
            this.reset();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);

            List<Double> local = new ArrayList<Double>(); // tuple containing time, sound
            //read one line in local samples
            String localLine = null;// read one line in the local  file
            try {
                localLine = bufferedReader.readLine();

                if (localLine != null) {
                    local.addAll(this.scanLine(localLine));
                }

                //go through the local list
                while (local.isEmpty() == false ) {
                    // extract time and sound from the local file
                    // Log.e(LOG_TAG, "local line" + localLine);
                    double local_x = local.get(0); // get the time
                    double local_y = local.get(1); // get the sound
                    time.add(local_x); // add time
                    y.add(local_y); // add sound

                    // analyse nex local sample
                    local.clear();
                    localLine = bufferedReader.readLine();// read one line in the local  file
                    if (localLine != null) {
                        local.addAll(this.scanLine(localLine));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.e(LOG_TAG, "***********Local time of size: " + time.size()/* + time.toString()*/);
            for (int i=0; i< time.size(); i++ )
                Log.d(LOG_TAG, ""+ time.get(i));
            //  Log.e(LOG_TAG, "***********end Local time " /* + time.toString()*/);

            //   Log.e(LOG_TAG, "**************Local y of size" + y.size()/* + y.toString()*/);
            for (int i=0 ; i< y.size();i++)
                Log.e(LOG_TAG, ""+ y.get(i));
            matrix.add((ArrayList<Double>) time); // add the time to the matrix
            matrix.add((ArrayList<Double>) y);  // add the sound to the matrix

            return matrix;
        }
        else{// we are extracting remote file which is calling extract
            ///////////////////////////////////

            ArrayList<Double> x = new ArrayList<Double>();

            // compute the margin
            Configuration config = new Configuration();
            double margin ;
            if (Configuration.isAveraging) {
                margin =   Configuration.samplingDurationSec * 1000; // 10 ; // margin allowed between the samples
            }else{
                margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
            }
            //   Log.e(LOG_TAG, "margin:" + margin );


            //open remote file and place at the beginning
            this.reset();
            InputStreamReader remote_isr = new InputStreamReader(this.inputStream);
            BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

            List<Double> remote = new ArrayList<Double>(); // tuple storing time,sound

            try {//read one line in the remote sample
                String remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                // Log.e(LOG_TAG,"remote LINE:" +remoteLine);
                if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine));}

                //go through the time list stored in the matrix
                for(int k =0 ; k< _matrix.get(0).size() ; k++){
                    // extract time  from the list
                    double local_x = _matrix.get(0).get(k); // get time
                    boolean stop = false; // stop looking for next remote sample

                    // go through the remote list
                    while (remote.isEmpty() == false && stop == false ){
                        //extract x and y from the remote file
                        double remote_x = remote.get(0); // extract time from remote file
                        double remote_y = remote.get(1);// extract sound from remote file
                        //    Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                        if (local_x < remote_x - margin ){ //local_x < remote_x - margin
                            stop = true; // stop looking for remote sample and look into the local a little bit
                            //     Log.e(LOG_TAG, "local " + local_x +"<<" + remote_x + " = stop looking remote: " + remote_x);

                            //remove the element in the matrix
                            for(int m =0 ; m < _matrix.size(); m++){
                                _matrix.get(m).remove(k);
                            }
                            k = k - 1 ; // because the element has been remove
                        }
                        // check if the sample have the same x (i.e. local_x = remote_x
                        if (remote_x - margin <=local_x && local_x<= remote_x + margin ) {
                            //  Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);
                            x.add(remote_y); // add this element to the X vector in the matrix
                            stop = true; // stop looking for remote sample taken at the same time

                            remote.clear();// next remote
                            remoteLine = remote_bufferedReader.readLine();//read next line in the remote file
                            if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine));}
                        }

                        if (local_x > remote_x + margin ){
                            remote.clear();//next remote
                            remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                            //   Log.e(LOG_TAG, "local " + local_x +">>" + remote_x + " =  go to next remote:" + remoteLine );
                            if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine)); }
                        }
                    }// analyse next local sample
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(LOG_TAG, "x size: " + x.size());
            _matrix.add(x);
            remote.clear();
            this.reset();//place at the beginning of the file for the next read
            return _matrix ;
        }
    }


    /**
     *  scan a matrix
     * @param _matrix mastric to scan
     * @return scanned matric
     */

    public ArrayList <ArrayList<Double>> extractGPSorientation(ArrayList <ArrayList<Double>> _matrix) {
        // put the provided file into a list and return that list
        if( _matrix == null) {
            List<Double> time= new ArrayList<Double>(); // time vector
            List<Double> y = new ArrayList<Double>();   // value vector
            List<Double> latitude = new ArrayList<Double>();   // latitude vector
            List<Double> longitude = new ArrayList<Double>();   // longitude vector
            ArrayList <ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(); // matrix vector
            // open local file and place at the beginning
            this.reset();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);

            List<Double> local = new ArrayList<Double>(); // tuple containing time, sound
            //read one line in local samples
            String localLine = null;// read one line in the local  file
            try {
                localLine = bufferedReader.readLine();

                if (localLine != null) {
                    //local.addAll(this.scanLineOrtGps(localLine));
                    local.addAll(this.scanLineGps(localLine));
                }

                //go through the local list
                while (local.isEmpty() == false ) {
                    // extract time and sound from the local file
                    // Log.e(LOG_TAG, "local line" + localLine);
                    int local_size = local.size();
                    double local_x = local.get(0); // get the time
                    double local_y = local.get(1); // get the sound
                    double local_lat = local.get(local_size - 1);
                    double local_longi = local.get(local_size - 2);


                    time.add(local_x); // add time
                    y.add(local_y); // add sound
                    latitude.add(local_lat); // add latitude
                    longitude.add(local_longi); // add longitude

                    // analyse nex local sample
                    local.clear();
                    localLine = bufferedReader.readLine();// read one line in the local  file
                    if (localLine != null) {
                        //local.addAll(this.scanLineOrtGps(localLine));
                        local.addAll(this.scanLineGps(localLine));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.e(LOG_TAG, "***********Local time of size: " + time.size()/* + time.toString()*/);
            for (int i=0; i< time.size(); i++ )
                Log.d(LOG_TAG, ""+ time.get(i));
            //  Log.e(LOG_TAG, "***********end Local time " /* + time.toString()*/);

            //   Log.e(LOG_TAG, "**************Local y of size" + y.size()/* + y.toString()*/);
            for (int i=0 ; i< y.size();i++)
                Log.e(LOG_TAG, ""+ y.get(i));

            for (int i=0 ; i< latitude.size();i++)
                Log.e(LOG_TAG, ""+ latitude.get(i));

            for (int i=0 ; i< longitude.size();i++)
                Log.e(LOG_TAG, ""+ longitude.get(i));

            int j = 0;
            for (int i = 0; i < latitude.size(); i++) {
                if (i > 0) {
                    flagGeoCase = (latitude.get(j) != latitude.get(i)) || (longitude.get(j) != longitude.get(i));
                }
                j++;
            }
            if(flagGeoCase == true){
                for (int k = 0; k < latitude.size(); k++) {
                    for (int p = 0; p < latitude.size(); p++) {
                        double boundarie = distZones(latitude.get(k), longitude.get(k), latitude.get(p), longitude.get(p));
                        if (boundarie < this.minGPSboundarie) {
                            this.minGPSboundarie = boundarie;
                        }
                    }
                }
            }else{
                //todo go to linear case
            }

            matrix.add((ArrayList<Double>) latitude); // add the latitude to the matrix
            matrix.add((ArrayList<Double>) longitude);  // add the longitude to the matrix
            matrix.add((ArrayList<Double>) time); // add the time to the matrix
            matrix.add((ArrayList<Double>) y);  // add the sound to the matrix

            return matrix;
        }
        else{// we are extracting remote file which is calling extract
            ///////////////////////////////////

            ArrayList<Double> x = new ArrayList<Double>();

            // compute the margin
            Configuration config = new Configuration();
            double margin ;
            if (Configuration.isAveraging) {
                margin =   Configuration.samplingDurationSec * 1000; // 10 ; // margin allowed between the samples
            }else{
                margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
            }
            //   Log.e(LOG_TAG, "margin:" + margin );


            //open remote file and place at the beginning
            this.reset();
            InputStreamReader remote_isr = new InputStreamReader(this.inputStream);
            BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

            List<Double> remote = new ArrayList<Double>(); // tuple storing time,sound

            try {//read one line in the remote sample
                String remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                // Log.e(LOG_TAG,"remote LINE:" +remoteLine);

                // if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}

                //go through the time list stored in the matrix
                for(int k =0 ; k< _matrix.get(2).size() ; k++){
                    // extract time  from the list
                    double local_x = _matrix.get(2).get(k); // get time
                    //int size_matrix = _matrix.size(); // get size
                    double local_lat = _matrix.get(0).get(k); // get latitude
                    double local_longi = _matrix.get(1).get(k); // get longitude
                    boolean stop = false; // stop looking for next remote sample

                    // go through the remote list
                    while (remote.isEmpty() == false && stop == false ){
                        //extract x and y from the remote file
                        double remote_x = remote.get(0); // extract time from remote file
                        double remote_y = remote.get(1);// extract sound from remote file
                        int remote_size = remote.size();
                        double remote_lat = remote.get(remote_size - 1);
                        double remote_longi = remote.get(remote_size - 2);



                        Log.e(LOG_TAG, "remote file extract: " + Arrays.toString(remote.toArray()));
                        Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                        double difIC = remote_x - margin;
                        Log.e(LOG_TAG, "margin: " + margin + " difIC: " + difIC);
                        if ((local_x < remote_x - margin) &&  (distZones(local_lat, local_longi, remote_lat, remote_longi) > minGPSboundarie) ){ //local_x < remote_x - margin
                            stop = true; // stop looking for remote sample and look into the local a little bit
                            //     Log.e(LOG_TAG, "local " + local_x +"<<" + remote_x + " = stop looking remote: " + remote_x);
                            Log.e(LOG_TAG, "1st margin ");
                            //remove the element in the matrix
                            for(int m =0 ; m < _matrix.size(); m++){
                                _matrix.get(m).remove(k);
                            }
                            k = k - 1 ; // because the element has been remove
                        }
                        // check if the sample have the same x (i.e. local_x = remote_x
                        if ((remote_x - margin <=local_x && local_x<= remote_x + margin) && (distZones(local_lat, local_longi, remote_lat, remote_longi) < minGPSboundarie)) {
                            //  Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);
                            x.add(remote_y); // add this element to the X vector in the matrix
                            stop = true; // stop looking for remote sample taken at the same time

                            Log.e(LOG_TAG, "2nd margin ");
                            remote.clear();// next remote
                            remoteLine = remote_bufferedReader.readLine();//read next line in the remote file
                            //if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }

                        if ((local_x > remote_x + margin) && (distZones(local_lat, local_longi, remote_lat, remote_longi) < minGPSboundarie) ){
                            remote.clear();//next remote
                            remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                            Log.e(LOG_TAG, "3nd margin ");
                            //   Log.e(LOG_TAG, "local " + local_x +">>" + remote_x + " =  go to next remote:" + remoteLine );
                            //if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }
                    }// analyse next local sample
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(LOG_TAG, "x size: " + x.size());
            _matrix.add(x);
            remote.clear();
            this.reset();//place at the beginning of the file for the next read
            return _matrix ;
        }
    }

    /**
     *
     *  scan a matrix
     * @param _matrix mastric to scan
     * @param ctx context
     * @return scanned matric
      */

    public ArrayList <ArrayList<Double>> extractFilterMatrix(ArrayList <ArrayList<Double>> _matrix, ContextData ctx){//, boolean isRaw) {
        // put the provided file into a list and return that list
        Log.d(LOG_TAG, "Begging the Error and Distance Filtering");
        if( _matrix == null) {
            List<Double> time= new ArrayList<Double>(); // time vector
            List<Double> y = new ArrayList<Double>();   // value vector
            List<Double> latitude = new ArrayList<Double>();   // latitude vector
            List<Double> longitude = new ArrayList<Double>();   // longitude vector
            ArrayList <ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(); // matrix vector
            // open local file and place at the beginning
            this.reset();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);

            List<Double> local = new ArrayList<Double>(); // tuple containing time, sound
            //read one line in local samples
            String localLine = null;// read one line in the local  file
            try {
                localLine = bufferedReader.readLine();

                if (localLine != null) {
                    //local.addAll(this.scanLineOrtGps(localLine));
                    local.addAll(this.scanLineGps(localLine));
                }

                //go through the local list
                while (local.isEmpty() == false ) {
                    // extract time and sound from the local file
                    // Log.e(LOG_TAG, "local line" + localLine);
                    int local_size = local.size();
                    double local_x = local.get(0); // get the time
                    double local_y = local.get(1); // get the sound
                    double local_lat = local.get(local_size - 1);
                    double local_longi = local.get(local_size - 2);


                    time.add(local_x); // add time
                    y.add(local_y); // add sound
                    latitude.add(local_lat); // add latitude
                    longitude.add(local_longi); // add longitude

                    // analyse nex local sample
                    local.clear();
                    localLine = bufferedReader.readLine();// read one line in the local  file
                    if (localLine != null) {
                        //local.addAll(this.scanLineOrtGps(localLine));
                        local.addAll(this.scanLineGps(localLine));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.e(LOG_TAG, "***********Local time of size: " + time.size()/* + time.toString()*/);
            for (int i=0; i< time.size(); i++ )
                Log.d(LOG_TAG, ""+ time.get(i));
            //  Log.e(LOG_TAG, "***********end Local time " /* + time.toString()*/);

            //   Log.e(LOG_TAG, "**************Local y of size" + y.size()/* + y.toString()*/);
            for (int i=0 ; i< y.size();i++)
                Log.e(LOG_TAG, ""+ y.get(i));

            for (int i=0 ; i< latitude.size();i++)
                Log.e(LOG_TAG, ""+ latitude.get(i));

            for (int i=0 ; i< longitude.size();i++)
                Log.e(LOG_TAG, ""+ longitude.get(i));

            matrix.add((ArrayList<Double>) latitude); // add the latitude to the matrix
            matrix.add((ArrayList<Double>) longitude);  // add the longitude to the matrix
            matrix.add((ArrayList<Double>) time); // add the time to the matrix
            matrix.add((ArrayList<Double>) y);  // add the sound to the matrix

            return matrix;
        }
        else{// we are extracting remote file which is calling extract
            ///////////////////////////////////
            ArrayList<Double> x = new ArrayList<Double>();
            DescriptiveStatistics filter = new DescriptiveStatistics();


            // compute the margin
            Configuration config = new Configuration();
            double margin ;
            if (Configuration.isAveraging) {
                margin =   Configuration.samplingDurationSec * 1000; // 10 ; // margin allowed between the samples
            }else{
                margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
            }
            //   Log.e(LOG_TAG, "margin:" + margin );


            //open remote file and place at the beginning
            this.reset();
            InputStreamReader remote_isr = new InputStreamReader(this.inputStream);
            BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

            List<Double> remote = new ArrayList<Double>(); // tuple storing time,sound

            try {//read one line in the remote sample
                String remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                // Log.e(LOG_TAG,"remote LINE:" +remoteLine);

                // if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}



                //go through the time list stored in the matrix
                for(int k =0 ; k< _matrix.get(2).size() ; k++){
                    // extract time from the list
                    double local_x = _matrix.get(2).get(k); // get time
                    //int size_matrix = _matrix.size(); // get size
                    double local_lat = _matrix.get(0).get(k); // get latitude
                    double local_longi = _matrix.get(1).get(k); // get longitude

                    boolean stop = false; // stop looking for next remote sample

                    // go through the remote list
                    while (remote.isEmpty() == false && stop == false ){
                        //extract x and y from the remote file
                        double remote_x = remote.get(0); // extract time from remote file
                        double remote_y = remote.get(1);// extract sound from remote file
                        int remote_size = remote.size();
                        double remote_lat = remote.get(remote_size - 1);
                        double remote_longi = remote.get(remote_size - 2);

                        Log.e(LOG_TAG, "remote file extract: " + Arrays.toString(remote.toArray()));
                        Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                        // todo calculate the standard deviation of the distances
                        filter.addValue(distZones(local_lat, local_longi, remote_lat, remote_longi));
                        double difIC = remote_x - margin;
                        Log.e(LOG_TAG, "margin: " + margin + " difIC: " + difIC);
                        if (local_x < remote_x - margin){ //local_x < remote_x - margin
                            stop = true; // stop looking for remote sample and look into the local a little bit
                            //     Log.e(LOG_TAG, "local " + local_x +"<<" + remote_x + " = stop looking remote: " + remote_x);
                            Log.e(LOG_TAG, "1st margin ");
                            //remove the element in the matrix
                            for(int m = 0 ; m < _matrix.size(); m++){
                                _matrix.get(m).remove(k);
                            }
                            k = k - 1 ; // because the element has been remove
                        }
                        // check if the sample have the same x (i.e. local_x = remote_x
                        if (remote_x - margin <=local_x && local_x<= remote_x + margin) {
                            //  Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);

                            filter.addValue(distZones(local_lat, local_longi, remote_lat, remote_longi));
                            if((0.0 <= (1000 * distZones(local_lat, local_longi, remote_lat, remote_longi))&&((1000 *distZones(local_lat, local_longi, remote_lat, remote_longi) < 0.1)))) {
                                filterWeight = ((double) 0);
                            }else {
                                Log.e(LOG_TAG, "Dist: " + distZones(local_lat, local_longi, remote_lat, remote_longi) + " weight: " + Math.log10(distZones(local_lat, local_longi, remote_lat, remote_longi)));
                                if(Configuration.weightingFunction == 1)
                                    filterWeight = 20 * Math.log10((distZones(local_lat, local_longi, remote_lat, remote_longi))/100);
                                if(Configuration.weightingFunction == 0)
                                    filterWeight = Math.exp(-(distZones(local_lat, local_longi, remote_lat, remote_longi)/100));
                            }
                            double remote_weight = filterWeight + remote_y;
                            Log.e(LOG_TAG, "Distance Filter: " + filterWeight + " x filtered value: " + remote_weight);
                            if(Configuration.isNotFilteredMatrix) {
                                x.add(remote_y); // add this element to the X vector in the matrix
                            } else {
                                if(Configuration.weightingFunction == 1)
                                    x.add(filterWeight + remote_y); // add this element to the X vector in the matrix
                                if(Configuration.weightingFunction == 0)
                                    x.add(filterWeight * remote_y); // add this element to the X vector in the matrix
                            }
                            stop = true; // stop looking for remote sample taken at the same time

                            Log.e(LOG_TAG, "2nd margin ");
                            remote.clear();// next remote
                            remoteLine = remote_bufferedReader.readLine();//read next line in the remote file
                            //if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }

                        if ((local_x > remote_x + margin)){
                            filter.addValue(distZones(local_lat, local_longi, remote_lat, remote_longi));
                            remote.clear();//next remote
                            remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                            Log.e(LOG_TAG, "3nd margin ");
                            //   Log.e(LOG_TAG, "local " + local_x +">>" + remote_x + " =  go to next remote:" + remoteLine );
                            // if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }
                    }// analyse next local sample
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ContextData.filterSD = filter.getStandardDeviation();
            Log.e(LOG_TAG, "Standard Deviation: " + filterSD);
            ArrayList<Double> xTemp = new ArrayList<Double>();
            if(Configuration.isNotFilteredMatrix)
                xTemp = x;
            else {
                for (int i = 0; i < x.size(); i++) {
                    Log.e(LOG_TAG, " X Data: " + x.get(i));
                    //xTemp.add(x.get(i) * Math.exp(-filterSD));
                    xTemp.add(x.get(i));
                }
            }
            Log.e(LOG_TAG, "xTemp size: " + xTemp.size());
            _matrix.add(xTemp);
            //_matrixRaw.add(x);
            remote.clear();
            this.reset();//place at the beginning of the file for the next read
            //if(isRaw)
            //  return _matrixRaw;
            //else
            return _matrix ;
        }
    }

    /**
     * scan a matric
     * @param _matrix matric to scan
     * @param ctx context
     * @return scanned matrix
     */
    public ArrayList <ArrayList<Double>> extractFilterGPSorientation(ArrayList <ArrayList<Double>> _matrix, ContextData ctx) {
        // put the provided file into a list and return that list
        if( _matrix == null) {
            List<Double> time= new ArrayList<Double>(); // time vector
            List<Double> y = new ArrayList<Double>();   // value vector
            List<Double> latitude = new ArrayList<Double>();   // latitude vector
            List<Double> longitude = new ArrayList<Double>();   // longitude vector
            ArrayList <ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(); // matrix vector
            // open local file and place at the beginning
            this.reset();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);

            List<Double> local = new ArrayList<Double>(); // tuple containing time, sound
            //read one line in local samples
            String localLine = null;// read one line in the local  file
            try {
                localLine = bufferedReader.readLine();

                if (localLine != null) {
                    //local.addAll(this.scanLineOrtGps(localLine));
                    local.addAll(this.scanLineGps(localLine));
                }

                //go through the local list
                while (local.isEmpty() == false ) {
                    // extract time and sound from the local file
                    // Log.e(LOG_TAG, "local line" + localLine);
                    int local_size = local.size();
                    double local_x = local.get(0); // get the time
                    double local_y = local.get(1); // get the sound
                    double local_lat = local.get(local_size - 1);
                    double local_longi = local.get(local_size - 2);


                    time.add(local_x); // add time
                    y.add(local_y); // add sound
                    latitude.add(local_lat); // add latitude
                    longitude.add(local_longi); // add longitude

                    // analyse nex local sample
                    local.clear();
                    localLine = bufferedReader.readLine();// read one line in the local  file
                    if (localLine != null) {
                        //local.addAll(this.scanLineOrtGps(localLine));
                        local.addAll(this.scanLineGps(localLine));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.e(LOG_TAG, "***********Local time of size: " + time.size()/* + time.toString()*/);
            for (int i=0; i< time.size(); i++ )
                Log.d(LOG_TAG, ""+ time.get(i));
            //  Log.e(LOG_TAG, "***********end Local time " /* + time.toString()*/);

            //   Log.e(LOG_TAG, "**************Local y of size" + y.size()/* + y.toString()*/);
            for (int i=0 ; i< y.size();i++)
                Log.e(LOG_TAG, ""+ y.get(i));

            for (int i=0 ; i< latitude.size();i++)
                Log.e(LOG_TAG, ""+ latitude.get(i));

            for (int i=0 ; i< longitude.size();i++)
                Log.e(LOG_TAG, ""+ longitude.get(i));

            int j = 0;
            for (int i = 0; i < latitude.size(); i++) {
                if (i > 0) {
                    flagGeoCase = (latitude.get(j) != latitude.get(i)) || (longitude.get(j) != longitude.get(i));
                }
                j++;
            }
            if(flagGeoCase == true){
                for (int k = 0; k < latitude.size(); k++) {
                    for (int p = 0; p < latitude.size(); p++) {
                        double boundarie = distZones(latitude.get(k), longitude.get(k), latitude.get(p), longitude.get(p));
                        if (boundarie < this.minGPSboundarie) {
                            this.minGPSboundarie = boundarie;
                        }
                    }
                }
            }else{
                //todo go to linear case
            }

            matrix.add((ArrayList<Double>) latitude); // add the latitude to the matrix
            matrix.add((ArrayList<Double>) longitude);  // add the longitude to the matrix
            matrix.add((ArrayList<Double>) time); // add the time to the matrix
            matrix.add((ArrayList<Double>) y);  // add the sound to the matrix

            return matrix;
        }
        else{// we are extracting remote file which is calling extract
            ///////////////////////////////////

            ArrayList<Double> x = new ArrayList<Double>();
            DescriptiveStatistics filter = new DescriptiveStatistics();

            // compute the margin
            Configuration config = new Configuration();
            double margin ;
            if (Configuration.isAveraging) {
                margin =   Configuration.samplingDurationSec * 1000; // 10 ; // margin allowed between the samples
            }else{
                margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
            }
            //   Log.e(LOG_TAG, "margin:" + margin );


            //open remote file and place at the beginning
            this.reset();
            InputStreamReader remote_isr = new InputStreamReader(this.inputStream);
            BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

            List<Double> remote = new ArrayList<Double>(); // tuple storing time,sound

            try {//read one line in the remote sample
                String remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                // Log.e(LOG_TAG,"remote LINE:" +remoteLine);

                //if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}

                //go through the time list stored in the matrix
                for(int k =0 ; k< _matrix.get(2).size() ; k++){
                    // extract time  from the list
                    double local_x = _matrix.get(2).get(k); // get time
                    //int size_matrix = _matrix.size(); // get size
                    double local_lat = _matrix.get(0).get(k); // get latitude
                    double local_longi = _matrix.get(1).get(k); // get longitude
                    boolean stop = false; // stop looking for next remote sample

                    // go through the remote list
                    while (remote.isEmpty() == false && stop == false ){
                        //extract x and y from the remote file
                        double remote_x = remote.get(0); // extract time from remote file
                        double remote_y = remote.get(1);// extract sound from remote file
                        int remote_size = remote.size();
                        double remote_lat = remote.get(remote_size - 1);
                        double remote_longi = remote.get(remote_size - 2);



                        Log.e(LOG_TAG, "remote file extract: " + Arrays.toString(remote.toArray()));
                        Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                        double difIC = remote_x - margin;
                        Log.e(LOG_TAG, "margin: " + margin + " difIC: " + difIC);
                        if ((local_x < remote_x - margin) &&  (distZones(local_lat, local_longi, remote_lat, remote_longi) > minGPSboundarie) ){ //local_x < remote_x - margin
                            stop = true; // stop looking for remote sample and look into the local a little bit
                            //     Log.e(LOG_TAG, "local " + local_x +"<<" + remote_x + " = stop looking remote: " + remote_x);
                            Log.e(LOG_TAG, "1st margin ");
                            //remove the element in the matrix
                            for(int m =0 ; m < _matrix.size(); m++){
                                _matrix.get(m).remove(k);
                            }
                            k = k - 1 ; // because the element has been remove
                        }
                        // check if the sample have the same x (i.e. local_x = remote_x
                        if ((remote_x - margin <=local_x && local_x<= remote_x + margin) && (distZones(local_lat, local_longi, remote_lat, remote_longi) < minGPSboundarie)) {
                            //  Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);

                            filter.addValue(distZones(local_lat, local_longi, remote_lat, remote_longi));
                            if((0.0 <= distZones(local_lat, local_longi, remote_lat, remote_longi))&&(distZones(local_lat, local_longi, remote_lat, remote_longi) < 0.1)) {
                                filterWeight = (double) 1;
                            }else {
                                Log.e(LOG_TAG, "Dist: " + distZones(local_lat, local_longi, remote_lat, remote_longi) + " weight: " + Math.log10(distZones(local_lat, local_longi, remote_lat, remote_longi)));
                                if(Configuration.weightingFunction == 1)
                                    filterWeight = 20 * Math.log10((distZones(local_lat, local_longi, remote_lat, remote_longi))/100);
                                if(Configuration.weightingFunction == 0)
                                    filterWeight = Math.exp(-(distZones(local_lat, local_longi, remote_lat, remote_longi)/100));
                            }
                            Log.e(LOG_TAG, "Distance Filter: " + filterWeight + " x filtered value: " + filterWeight * remote_y);
                            if(Configuration.isNotFilteredMatrix)
                                x.add(remote_y); // add this element to the X vector in the matrix
                            else
                            if(Configuration.weightingFunction == 1)
                                x.add(filterWeight + remote_y); // add this element to the X vector in the matrix
                            if(Configuration.weightingFunction == 0)
                                x.add(filterWeight * remote_y); // add this element to the X vector in the matrix
                            stop = true; // stop looking for remote sample taken at the same time


                            Log.e(LOG_TAG, "2nd margin ");
                            remote.clear();// next remote
                            remoteLine = remote_bufferedReader.readLine();//read next line in the remote file
                            // if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }

                        if ((local_x > remote_x + margin) && (distZones(local_lat, local_longi, remote_lat, remote_longi) < minGPSboundarie) ){
                            filter.addValue(distZones(local_lat, local_longi, remote_lat, remote_longi));
                            remote.clear();//next remote
                            remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                            Log.e(LOG_TAG, "3nd margin ");
                            //   Log.e(LOG_TAG, "local " + local_x +">>" + remote_x + " =  go to next remote:" + remoteLine );
                            //if (remoteLine != null) {remote.addAll(this.scanLineOrtGps(remoteLine));}
                            if (remoteLine != null) {remote.addAll(this.scanLineGps(remoteLine));}
                        }
                    }// analyse next local sample
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            filterSD = filter.getStandardDeviation();
            Log.e(LOG_TAG, "Standard Deviation: " + filterSD);
            ArrayList<Double> xTemp = new ArrayList<Double>();
            if(Configuration.isNotFilteredMatrix)
                xTemp = x;
            else {
                for (int i = 0; i < x.size(); i++) {
                    Log.e(LOG_TAG, " X Data: " + x.get(i));
                    //xTemp.add(x.get(i) * Math.exp(-filterSD));
                    xTemp.add(x.get(i));
                }
            }
            Log.e(LOG_TAG, "xTemp size: " + xTemp.size());
            _matrix.add(xTemp);
            remote.clear();
            this.reset();//place at the beginning of the file for the next read
            return _matrix ;
        }
    }


    /**
     * distance
     * @param lat1 latitude src
     * @param lon1 longitude src
     * @param lat2 latitude dest
     * @param lon2 longiture dest
     * @return distance
     */
    public Double distZones(double lat1, double lon1, double lat2, double lon2){
        double theta = lon1 - lon2;
        double distAng = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        double distAcos = Math.acos(distAng);
        double distRad = rad2deg(distAcos);
        double dist = distRad * 60 * 1.1515;
        return(dist);
    }

    /**
     * degree to radian
     * @param deg degree
     * @return radian
     */
    public double deg2rad(double deg){
        return (deg * Math.PI /180.0);
    }

    /**
     * radian to degree
     * @param rad radian
     * @return degree
     */
    public double rad2deg(double rad){
        return (rad * 180.0 / Math.PI);
    }




    /**
     * extract the samples included in the list of files and the local file that is calling the
     *  function whenever they have common time stamps
     * @param remoteFilelist file list
     * @return sample included in the provided files
     */
    public ArrayList <ArrayList<Double>> extractCommontimeSeries( List<FileManager> remoteFilelist){
        // extract the local samples and put it into the matrix to form the Y vector
        ArrayList <ArrayList<Double>> matrix =  this.extract(null);


        // extract the remote samples X1 X2 X3 ... and put it into the matrix
        for(int i = 0 ; i<remoteFilelist.size() ; i++){
            Log.e(LOG_TAG, "*******\n *******\n Analyse remote file number " +i);
            matrix = remoteFilelist.get(i).extract(matrix); // extract the sample X that have been taken at the same time
            // and put it into the matrix
        }
        return matrix ;
    }


    /**
     * extract the samples that have been taken at the same time from the two provided files
     * @param aremoteFileManager file where the sound is included
     * @param upto up to how many samples should be extracted
     * @return common time series (after synchronisation)
     */
    public List<Double> getNewCommonTimeSeries(FileManager aremoteFileManager, int upto) {
        // output the samples that have been taken at the same time
        List<Double> output = new ArrayList<Double>();

        // define the margin between two samples
        // consider wether noise is averaged to compute the margin
        Configuration config = new Configuration();
        double margin ;
        if (Configuration.isAveraging) {
            margin =   Configuration.samplingDurationSec * 1000; // 10 ; // margin allowed between the samples
        }else{
            margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples
        }
        Log.e(LOG_TAG, "margin:" + margin );


        // open local file and place at the beginning
        this.reset();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        //open remote file and place at the beginning


        FileManager help = new    FileManager(aremoteFileManager.getFilename() , true, this.context) ;
        help.reset();

        InputStreamReader remote_isr = new InputStreamReader(help.inputStream);
        BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

//            aremoteFileManager.reset();
        //      InputStreamReader remote_isr = new InputStreamReader(aremoteFileManager.inputStream);
        //    BufferedReader remote_bufferedReader = new BufferedReader(remote_isr);

        List<Double> local = new ArrayList<Double>();
        List<Double> remote = new ArrayList<Double>();

        try {        //read one line in local samples
            String localLine = bufferedReader.readLine();// read one line in the local  file
            if (localLine != null) {local.addAll(this.scanLine(localLine));}
            Log.e(LOG_TAG, "local line" + localLine);
            //read one line in the remote sample
            String remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
            Log.e(LOG_TAG,"remote LINE:" +remoteLine);
            if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine));}

            int i =0;
            int j= 0;
            //go through the local list
            while (local.isEmpty() == false && i<upto ){
                // extract x and y from the local file
                double local_x = local.get(0);
                double local_y = local.get(1);
                boolean stop = false; // stop looking for next remote sample

                // go through the remote list
                while (remote.isEmpty() == false && stop == false && j <upto){
                    //extract x and y from the remote file
                    double remote_x = remote.get(0);
                    double remote_y = remote.get(1);
                    //    Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                    if (local_x < remote_x - margin ){ //local_x < remote_x - margin
                        stop = true; // stop looking for remote sample and look into the local a little bit
                        Log.e(LOG_TAG, "local " + local_x +"<<" + remote_x + " = stop looking remote: " + remote_x);
                    }
                    // check if the sample have the same x (i.e. local_x = remote_x
                    if (remote_x - margin <=local_x && local_x<= remote_x + margin ) {
                        Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);
                        //todo add the local time
                        // output.add(local_x); or output.add(remote_x) ;

                        output.add(local_y);
                        output.add(remote_y);
                        stop = true; // stop looking for remote sample taken at the same time

                        remote.clear();// next remote
                        remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                        if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine)); j++;}
                        // remote_i = remote_i +2 ; // analyse next remote sample to find a
                        // sample taken at the same time
                    }
                    if (local_x > remote_x + margin ){
                        remote.clear();//next remote
                        remoteLine = remote_bufferedReader.readLine();//read one line in the remote file
                        Log.e(LOG_TAG, "local " + local_x +">>" + remote_x + " =  go to next remote:" + remoteLine );
                        if (remoteLine != null) {remote.addAll(this.scanLine(remoteLine)); j++;}
                        //remote_i = remote_i +2 ; // analyse next remote sample to find a
                    }
                }
                // analyse nex local sample
                local.clear();
                localLine = bufferedReader.readLine();// read one line in the local  file
                if (localLine != null) {local.addAll(this.scanLine(localLine)); i++;}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        local.clear();
        remote.clear();
        help.close();
        this.reset();//place at the beginning of the file for the next read
        return  output;
    }

    /*
     *scan the local and remote calibration file
     * so as to extract the samples that have been taken at the same time
     */
    /*

    public List<Double> getCommonTimeSeries(FileManager aremoteFileManager) {
        //extract local samples
        List<Double> local = new ArrayList<Double>();
        local.addAll(this.scan2list());
        //extract remote samples
        List<Double> remote = new ArrayList<Double>();
        remote.addAll(aremoteFileManager.scan2list());
        // output the samples that have been taken at the same time
        List<Double> output = new ArrayList<Double>();
       //
        double margin = (double) 1000 / (double) Record._rate;// 10 ; // margin allowed between the samples

        Log.e(LOG_TAG, "rate:" + Record._rate + "margin:" + margin );
        int local_i, remote_i;//iterator on the local and remote list
        remote_i =0 ;

        //go through the local list
        for (local_i = 0 ; local_i +1 < local.size() ; local_i = local_i+2){
            // extract x and y from the local file
            double local_x = local.get(local_i);
            double local_y = local.get(local_i +1);
            boolean stop = false; // stop looking for next remote sample

            // go through the remote list
            while (remote_i +1 < remote.size() && stop == false ){
                //extract x and y from the remote file
                double remote_x = remote.get(remote_i);
                double remote_y = remote.get(remote_i +1);
            //    Log.e(LOG_TAG, "analyse remote: " + remote_x + " local: " + local_x );
                if (local_x < remote_x - margin ){ //local_x < remote_x - margin
                    stop = true; // stop looking for remote sample
              //  Log.e(LOG_TAG, "stop looking: " + remote_x);
                }
                // check if the sample have the same x (i.e. local_x = remote_x
                if (remote_x - margin <=local_x && local_x<= remote_x + margin ) {
                   // Log.e(LOG_TAG, " local " + local_x + " near " + remote_x);
                    output.add(local_y);
                    output.add(remote_y);
                    stop = true; // stop looking for remote sample taken at the same time
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
                if (local_x > remote_x + margin ){
                 //   Log.e(LOG_TAG, "next remote" );
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
            }
        }
        local.clear();
        remote.clear();
        return  output;
    }
*/
/*    public List<Double> getCommonTimeSeries(FileManager aremoteFileManager) {
        //extract local samples
        List<Double> local = new ArrayList<Double>();
        local.addAll(this.scan2list());
        //extract remote sample
        List<Double> remote = new ArrayList<Double>();
        remote.addAll(aremoteFileManager.scan2list());
        // output samples that have been taken at the same time
        List<Double> output = new ArrayList<Double>();

        //iterator on the local and remote list
        int local_i, remote_i;
        remote_i =0 ;

        //go through the local list
        for (local_i = 0 ; local_i +1 < local.size() ; local_i = local_i+2){
            // extract x and y from the local file
            double local_x = local.get(local_i);
            double local_y = local.get(local_i +1);
            boolean stop = false; // stop looking for next remote sample

            // go through the remote list
            while (remote_i +1 < remote.size() && stop == false ){
                //extract x and y from the remote file
                double remote_x = remote.get(remote_i);
                double remote_y = remote.get(remote_i +1);

                if (local_x < remote_x){
                    stop = true; // stop looking for remote sample taken at the same time
                }
                // check if the sample have the same x (i.e. local_x = remote_x
                if (local_x == remote_x) {
                    output.add(local_y);
                    output.add(remote_y);
                    stop = true; // stop looking for remote sample taken at the same time
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
                if (local_x > remote_x){
                    remote_i = remote_i +2 ; // analyse next remote sample to find a
                    // sample taken at the same time
                }
            }
        }
        return  output;
    }
*/

    /**
     * extract the maximul value of X
     * @param sampleList list of samples
     * @return maximum value of X
     */
    public double getMaxXFromTimeSeries(List<Double> sampleList){
        int i;
        double max2return =0;
        for (i = 0 ; i +1 < sampleList.size() ;  i = i+2) {
            Log.e(LOG_TAG, "analyse" +  sampleList.get(i));
            // extract x and y from the local file
            double local_x = sampleList.get(i);
            if( i ==0)
                max2return = local_x;
            //double local_y = sampleList.get(i + 1);
            if( max2return < local_x){
                max2return = local_x;
                Log.e(LOG_TAG,"max+=" + local_x);
            }

        }
        return max2return;
    }

    /**
     * extarct the maximum value of Y
     * @param sampleList sample list
     * @return minimum value of X
     */
    public double getMinXFromTimeSeries(List<Double> sampleList){
        int i;
        double min2return =0;
        for (i = 0 ; i +1 < sampleList.size() ;  i = i+2) {
            // extract x and y from the local file
            double local_x = sampleList.get(i);
            if( i ==0)
                min2return = local_x;

            //double local_y = sampleList.get(i + 1);
            if( min2return > local_x){
                min2return = local_x;
            }

        }
        return min2return;
    }

    /**
     * return the max Y value from a list of measurements
     * @param sampleList measurements
     * @return max value of y
     */
    public double getMaxYFromTimeSeries(List<Double> sampleList){
        int i;
        double max2return =0;
        for (i = 0 ; i +1 < sampleList.size() ;  i = i+2) {
            // extract x and y from the local file
            //double local_x = sampleList.get(i);
            double local_y = sampleList.get(i + 1);
            if( max2return < local_y){
                max2return = local_y;
            }

        }
        return max2return;
    }

    /**
     * get the min value of Y
     * @param sampleList measurements
     * @return minimum value of Y
     */
    public double getMinYFromTimeSeries(List<Double> sampleList){
        int i;
        double min2return =0;
        for (i = 0 ; i +1 < sampleList.size() ;  i = i+2) {
            // extract x and y from the local file
            // double local_x = sampleList.get(i);
            double local_y = sampleList.get(i + 1);
            if( i ==0)
                min2return = local_y;

            if( min2return > local_y){
                min2return = local_y;
            }
        }
        return min2return;
    }



}


