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
package com.example.colibris.nsd;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;


import com.example.colibris.configuration.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This FileTransferManager class implements the transfer of files
 * (e.g., the connexion graph or the sound)
 */

public class FileTransferManager implements Runnable {
    /**
     * socket
     */
    public Socket socket = null;
    /**
     * handler
     */
    private Handler handler;
    /**
     * determines if the manager is running
     */
    volatile boolean isrunning = true;
    /**
     * context
     */
    private Context ctx;
    /**
     * input stream
     */
    private InputStream iStream;
    /**
     * output stream
     */
    private OutputStream oStream;
    /**
     * log related information
     */
    private static final String TAG = "FileTransfer";
  //  public String prefix = "192.168.49"; // network prefix of wifidirect
    /**
     * is the ap
     */
    public  boolean isProvidedByServer = false;
    /**
     * determine if the device is the client ap
     */
    public boolean isAPclient = false;

    /**
     * initialisation
     * @param socket socket
     * @param handler handler
     * @param ctx context
     * @param is_server_socket is starting a server
      * @param is_ap_client is starting a client
     */
    public FileTransferManager (Socket socket, Handler handler, Context ctx, boolean is_server_socket, boolean is_ap_client) {
        this.socket = socket;
        this.handler = handler;
        this.ctx = ctx;
        this.isProvidedByServer = is_server_socket;
        this.isAPclient = is_ap_client;
    }

    /**
     * start the server/cleint
     */
    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            int bufferSize  =socket.getReceiveBufferSize();

            Log.e(TAG, "Start File transfer manager");
            // provide an handler to wifiservicediscovery that provides it to the wifichatfragment
            //that use it to send messages
            Log.e(TAG, "Send a tcp handler to wifidiscovery that forwards it to wifi fragment");
            handler.obtainMessage(Configuration.MY_TCP_HANDLE, this).sendToTarget();

            // listen for the paket sent only  if i am a server or i am a client connected to the ap
            // otherwise, (if i am a client connected to another device (excluding the ap
            // i should not use the socket client to listen for incoming messages that will
            // never come ....

            while (this.isrunning==true && (this.isProvidedByServer == true || this.isAPclient == true) ) {
                try {
                    if(this.isAPclient == true)
                        Log.e(TAG, "client is receiving a message");

                    if (this.isProvidedByServer)
                        Log.e(TAG, "Server is receiving a message");
                    byte[] buffer = new byte[bufferSize];
                    int bytes;
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }


                    // Send the obtained bytes to the UI Activity

                    Log.e(TAG, "Socket Address is" + socket.getRemoteSocketAddress().toString()); // 192.168.49.136:59152

                    String id =  socket.getRemoteSocketAddress().toString().substring(Configuration.prefix.length()+2,socket.getRemoteSocketAddress().toString().indexOf(":"));
                    Log.e(TAG, "EXTRACTED ID:" + id);
                    int v = Integer.parseInt(id);

                    handler.obtainMessage(Configuration.FILE_READ, bytes, v, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "problem socket ", e);
            e.printStackTrace();
        } finally {

            if( this.isAPclient == true) {

                try {

                    Log.e(TAG, "CLOSE SOCKET ");
                    if (socket.isClosed() == false)
                        socket.close();
                    isrunning = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{ // nothing to do : I am a client, I should wait
                Log.e(TAG, "is a client not receiving");
            }
        }
    }

    /**
     * send the buffer
     * @param buffer content of the message
     */
    public void write(byte[] buffer) {
        if (this.isrunning = true) {
            try {
                Log.e(TAG, "write message to " +  socket.getInetAddress().toString());
                oStream.write(buffer);
                // oStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);

                if (socket.isClosed() == false)
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                isrunning = false;
            }
        }
        else{
            Log.e(TAG, "do not write");
        }
    }


    /**
     * send the file content chunk by chunk, with a md5
     * @param action message type
     * @param fileName file name
     * @param endFileDelimiter delimiter
     * @return some information to display
     */
    public String writeChunkFile(String action, String fileName, String endFileDelimiter) {
        String toReturn = new String();
        toReturn += "Start sendind file: "+ fileName + "md5: " ;

        ///////////////////
        byte[] computedCheckSum = createChecksum(fileName);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < computedCheckSum.length; i++)
            sb.append(Integer.toString((computedCheckSum[i] & 0xff) + 0x100, 16).substring(1));
        String computedChecksum = sb.toString();
        toReturn += computedChecksum;


        Log.d(TAG, "Start sendind file: "+ fileName + "md5: " + createChecksum(fileName).toString());
        byte[]  actionbuf =  action.getBytes();
        byte[]  md5buf = toString(createChecksum(fileName)).getBytes();
        byte[] chunk = new byte[1024];//create a chunk of 1024 char
        byte[]  endFileDelimiterBuf =  endFileDelimiter.getBytes();

        if (this.isrunning = true) {
            try {
                toReturn+= "proceed: file transfer manager is running";
                //send the action to perform (i.e. message type) and the md5 of the file
                byte[] sent_action_buf = new byte[actionbuf.length ];
                System.arraycopy(actionbuf/*src*/, 0/*from src position*/, sent_action_buf/*dest*/, 0/*dest from */, actionbuf.length /*length*/);
                try {
                    oStream.write(sent_action_buf);//send the action
                    oStream.write(Configuration.MSG_MD5.getBytes());//send MD5=
                    if(md5buf!= null) // send md5
                        oStream.write(md5buf);

                    oStream.write(",".getBytes());// send ","
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                    toReturn += "Exception during write";
                    if (socket.isClosed() == false)
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    isrunning = false;
                }
                //do some printing
                String msgtmp = new String(sent_action_buf, 0,sent_action_buf.length);
                Log.d(TAG, "has sent " + msgtmp);
                toReturn+= "has sent " + msgtmp;
                Log.e(TAG, "begins sending file:" + fileName);
                toReturn+= "begin sending file " + fileName;

                //open the file to send

                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                boolean isPresent = true;
                if (!docsFolder.exists()) {
                    isPresent = docsFolder.mkdir();
                }
                File file = new File(docsFolder.getAbsolutePath(), fileName);
                FileInputStream fin = new FileInputStream(file);

                int chunkLen = 0; //lenght of the chunk that is sent

                while ((chunkLen = fin.read(chunk)) != -1) {
                    byte[] sentbuf = new byte[ chunkLen];
                    System.arraycopy(chunk /*src*/, 0 /*from src position*/ , sentbuf /*dest*/
                            , 0 /*destfrom*/, chunkLen);
                    try {
                        oStream.write(sentbuf);

                        //  String chunktmp = new String(sentbuf, 0,chunkLen);
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                        if (socket.isClosed() == false)
                            try {
                                socket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        isrunning = false;
                    }
                }
                fin.close();


                // send the end file delimiter
                byte[] sent_end_delimiter_buf = new byte[endFileDelimiterBuf.length ];
                System.arraycopy(endFileDelimiterBuf/*src*/, 0/*from src position*/, sent_end_delimiter_buf/*dest*/, 0/*dest from */, endFileDelimiterBuf.length /*length*/);

                try {
                    oStream.write(sent_end_delimiter_buf);
                    //   Log.d(TAG, "has written the end delimiter");
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                    toReturn+= "Exception during write";

                    if (socket.isClosed() == false)
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    isrunning = false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();//file not found
                Log.e(TAG, "Exception File not found", e);
            } catch (IOException e) {
                e.printStackTrace();//available
                Log.e(TAG, "Exception file not available", e);
            }
        }
        else{
            toReturn+= "do not write, socket is already closed\n";
            Log.e(TAG, "do not write, socket is already closed");
        }
        Log.d (TAG, "end sending the file");
        toReturn+= "end sending the file";
        return toReturn;
    }

    /**
     * for displaying purpose
     * @param byteData content to send
     * @return byte converted into string
     */
    static  public String toString(byte[] byteData){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        //System.out.println("Digest(in hex format):: " + sb.toString());
        return sb.toString();
    }

    /**
     * return the checksum of the file
     * @param filename file name
     * @return checksum
     */
    private  byte[] createChecksum(String filename)  {
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {

            File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
            File file = new File(docsFolder.getAbsolutePath(),filename);
            fis = new FileInputStream(file);

            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            fis.close();
            return complete.digest();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * close the file manager
     */
    public void close(){
        try {
            this.socket.close();
        } catch (IOException e) {
            Log.e(TAG, "cannot close the socket in file transfer manager");
            e.printStackTrace();

        }
    }

}
