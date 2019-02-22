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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * The TransferManager class handles the packets that are received (and provided by Wifi direct)
 */
public class TransferManager implements Runnable  {
    /**
     * socket
     */
    private DatagramSocket socket = null;
    /**
     *handler
     */

    private Handler handler;

    /**
     * determine if the transfer manager is running
     */
    volatile boolean isrunning = true;
    /**
     * context
     */
    private Context ctx;
    /**
     * log related information
     */
    private static final String TAG = "TransferManager";

    /**
     *  initialise the transfer manager
     * @param socket socket
     * @param handler handler
     * @param ctx context
     */
     public TransferManager (DatagramSocket socket, Handler handler, Context ctx) {
        this.socket = socket;
        this.handler = handler;
        this.ctx = ctx;
    }

    /**
     * return the local host address that is used on by the wifi interface
     * @return local address used by Wifi direct
     */
    public String getLocalHostaddr(){
        Enumeration e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    //  Log.e(TAG, i.getHostAddress());
                    if (i.getHostAddress().startsWith(Configuration.prefix)){
                        return i.getHostAddress();
                    }
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * run a UDP server that is listening for the packet that are provided by Wifidirect
     * the packets are provided to a handler that deal with it
     */
    @Override
    public void run() {
        Log.e(TAG, "Running the udp transfer");
        handler.obtainMessage(Configuration.MY_BROADCAST_HANDLE, this).sendToTarget();

        try {
            while (this.isrunning==true) {
                int bufferSize = socket.getReceiveBufferSize();
                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                //extract the address from the packet
                InetAddress pktAddr =  packet.getAddress();
                Log.e(TAG, "udp pkt received from " +  pktAddr.getHostAddress());

                // check that we are not listening our own packet
                if(pktAddr.getHostAddress().compareTo(getLocalHostaddr())!=0){
                    // display response
                    String received = new String(packet.getData(), 0, packet.getLength());
                    Log.e(TAG, "udp pkt contains" + received);

                    int bytes = packet.getLength();
                    if (bytes == -1)
                        break;
                    // Send the obtained bytes to the wifiDiscovery for a treatment of the message

                    //extract the computer id of the pkt
                    String id =  pktAddr.getHostAddress().substring(Configuration.prefix.length()+1);
                    int v = Integer.parseInt(id);
                    handler.obtainMessage(Configuration.FILE_READ, bytes, v, packet.getData()).sendToTarget();
                }else{
                    Log.e(TAG, "do not consider the udp pkt received" );
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "problem socket ", e);
            e.printStackTrace();
            if (socket.isClosed() == false)
                socket.close();//just added by francoise
            isrunning = false;
        } finally {
            Log.e(TAG, "CLOSE SOCKET ");
            if (socket.isClosed() == false)
                socket.close();
            isrunning = false;
        }
    }


    public void write(byte[] buffer) {
        if (this.isrunning = true) {
            try {
                InetAddress address = InetAddress.getByName(Configuration.broadcast_addr);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Configuration.BROADCAST_SERVER_PORT); //todo check if that is the good port
                socket.send(packet);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                if (socket.isClosed() == false)
                    socket.close();
                isrunning = false;
            }
        }
        else{
            Log.e(TAG, "do not write");
        }
    }


    public void writeChunkFile(String action, String fileName, String endFileDelimiter) {
        Log.d(TAG, "Start sendind file: "+ fileName);
        byte[] chunk = new byte[1024];//create a chunk of 1024 char
        byte[]  endFileDelimiterBuf =  endFileDelimiter.getBytes();

        String md5 = toString( createChecksum(fileName));
        String actionAndMD5 = action +Configuration.MSG_MD5 +  md5 + ",";
        byte[] actionAndMD5Buf = actionAndMD5.getBytes();

        if (this.isrunning = true) {
            try {
                InetAddress address = InetAddress.getByName(Configuration.broadcast_addr);
                //send the action to perform (i.e. message type) and the md5 of the file
                try {
                    DatagramPacket packet = new DatagramPacket(actionAndMD5Buf, actionAndMD5Buf.length, address, Configuration.BROADCAST_SERVER_PORT);
                    socket.send(packet);     Log.e(TAG, " sent the file header");
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                    if (socket.isClosed() == false)
                        socket.close();
                    isrunning = false;
                }
                //open the file to send
                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                boolean isPresent = true;
                if (!docsFolder.exists()) {
                    isPresent = docsFolder.mkdir();
                }
                File file = new File(docsFolder.getAbsolutePath(), fileName);
                FileInputStream fin = new FileInputStream(file);

                int chunkLen = 0; //length of the chunk that is sent
                while ((chunkLen = fin.read(chunk)) != -1) {
                    Log.e(TAG, "chunklen: " + chunkLen);
                    byte[] sentbuf = new byte[ chunkLen];
                    System.arraycopy(chunk /*src*/, 0 /*from src position*/ , sentbuf /*dest*/, 0 /*destfrom*/, chunkLen);
                    try {
                        DatagramPacket packet  = new DatagramPacket(sentbuf, sentbuf.length, address, Configuration.BROADCAST_SERVER_PORT);
                        socket.send(packet);
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                        if (socket.isClosed() == false)
                            socket.close();
                        isrunning = false;
                    }
                }
                fin.close();
                // send the end file delimiter
                byte[] sent_end_delimiter_buf = new byte[endFileDelimiterBuf.length ];
                System.arraycopy(endFileDelimiterBuf/*src*/, 0/*from src position*/, sent_end_delimiter_buf/*dest*/, 0/*dest from */, endFileDelimiterBuf.length /*length*/);
                try {
                    DatagramPacket packet  = new DatagramPacket(sent_end_delimiter_buf, sent_end_delimiter_buf.length, address, Configuration.BROADCAST_SERVER_PORT);
                    socket.send(packet);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                    if (socket.isClosed() == false)
                        socket.close();
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
            Log.e(TAG, "do not write, socket is already closed");
        }
        Log.d (TAG, "end sending the file");
    }




    static  public String toString(byte[] byteData){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }

    public  byte[] createChecksum(String filename)  {
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {
            // open the given file
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

    public void close(){
        isrunning = false;
        Log.e(TAG, "close is called");

        if(socket.isClosed() == false)
            socket.close();


    }

}


