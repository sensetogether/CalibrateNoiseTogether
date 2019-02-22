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
import android.util.Log;

import com.example.colibris.configuration.Configuration;
import com.example.colibris.ui.MainActivity;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * The SimpleNTPClient class corresponds to a NTP client used to synchronise with a NTP server that operate as the Wifi AP
 * The wifi - direct AP serves as ntp server.
 * Clients try to synchronise with this server.
 */

public class SimpleNTPClient {

    /**
     * log related information
     */
    public static final String TAG = "NTP client";
    /**
     * ntp client
     */
    private   NTPUDPClient ntpclient;

    /**
     * this method sends an NTP request
     * @param port port number on which operated NTP
     * @return the time related configuration parameters that are provided by NTP
     */

    public NtpTimeConfigurationParam sendNTPRequest( int port){

        Log.e(TAG, "ntp client start sending a request");
        NTPUDPClient client = new NTPUDPClient();

        Log.e(TAG, "ntp client has creates an ntp client ");

        // We want to timeout if a response takes longer than 5 seconds
        client.setDefaultTimeout(4000);
        NtpTimeConfigurationParam configuration2return = new NtpTimeConfigurationParam();
        try {
            Log.e(TAG, "ntp client open()");
            client.open();

            try {
                configuration2return =  sendNtpRequest(  client,  port)  ;
                this.ntpclient = client;
            } catch (IOException ioe) {

                Log.e(TAG, "ERROR NO NTP ");
                ioe.printStackTrace();
            }
            client.close();

        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR NO NTP ");
        }
        return configuration2return ;

    }

    /**
     * Create and send an NTP request to the wifi direct leader
     * @param client NTP client used to send the NTP request
     * @param port port on which is operating NTP
     * @return the time-related parameters that are provided by NTP
     * @throws IOException
     */
     private NtpTimeConfigurationParam sendNtpRequest(NTPUDPClient client, int port) throws IOException {

        InetAddress hostAddr = InetAddress.getByName( Configuration.calibration_leader/*"192.168.49.1"*//*"ntp.cnam.fr"*//*"ntp1.jussieu.fr"*/ /*"time.nist.gov"*/);


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSSS");
        sdf.setTimeZone(TimeZone.getDefault());
        Log.e(TAG, "> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
        String currentDateandTime = sdf.format(new Date());
        TimeInfo info = client.getTime(hostAddr, port);
        MainActivity.processResponse(info);

        NtpV3Packet message = info.getMessage();
        TimeStamp refNtpTime = message.getReferenceTimeStamp();
        Log.e(TAG, "  Reference Timestamp:\t" + refNtpTime.toDateString());
        String ferdate = sdf.format(refNtpTime.getDate());

        // Origin Timestamp : Time at the client when the request departed
        // for the server,
        TimeStamp origNtpTime = message.getOriginateTimeStamp();

        Log.e(TAG, " Originate Time:\t" + origNtpTime.toDateString());
        String ordate = sdf.format(origNtpTime.getDate());

        long destTime = info.getReturnTime();

        //     Receive Timestamp (rec): Time at the server when the request arrived
        // from the client,
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        Log.e(TAG, "****\n \n \n  Receive Time:\t" + rcvNtpTime.toDateString());
        String rcvdate = sdf.format(rcvNtpTime.getDate());

        //       Transmit Timestamp (xmt): Time at the server when the response left
        // for the client,
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        Log.e(TAG, " ****\n \n \n Transmit Time:\t" + xmitNtpTime.toDateString());
        String xmitdate = sdf.format(xmitNtpTime.getDate());

        // Destination Timestamp (dst): Time at the client when the reply
        // arrived from the server, in NTP timestamp format.
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
        Log.e(TAG, " ****\n \n \n Destination Time:\t" + destNtpTime.toDateString());
        String destdate = sdf.format(destNtpTime.getDate());

        info.computeDetails(); // compute offset/delay if not already done
        Long offsetValue = info.getOffset();
        Long delayValue = info.getDelay();

        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

        NtpTimeConfigurationParam config = new NtpTimeConfigurationParam();

        if (offsetValue != null) {

            NtpTimeConfigurationParam.isSynchronised = true;
            NtpTimeConfigurationParam.roundTripDelay = delayValue;
            NtpTimeConfigurationParam.timeOffset = offsetValue;

            Log.d(TAG, "  " + offset + "ms\n");
            Log.d(TAG, "  " + delay + "ms\n");
        }
        return config;
    }


}
