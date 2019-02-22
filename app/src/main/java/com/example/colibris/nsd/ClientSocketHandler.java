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
import android.os.Handler;
import android.util.Log;


import com.example.colibris.configuration.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class implements a socket client. in partice, it
 * correspond to a client thread.
 */
public class ClientSocketHandler extends Thread {
    /**
     * log-related information
     */
    private static final String TAG = "ClientSocketHandler";
    /**
     * handler
     */
    private Handler handler;
    /**
     * context
     */
    private Context ctx;
    /**
     * file transfer manager
     */
    private FileTransferManager fileTransferManager;
    /**
     * adress
     */
    private InetAddress mAddress;
    /**
     *
     */
    private volatile boolean isrunning = true;

    // open a socket with the server

    /**
     *
     * @param handler handler of of the packet that are received
     * @param groupOwnerAddress adress of the owner/leader of the wifi direct group
     * @param ctx context
     */
      public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, Context ctx) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        this.ctx = ctx;
    }

    /**
     * Start a client that operate on the port (as configured in Configuration.port)
     * and start a thread to receive/send files
     */
    @Override
    public void run() {
        if(isrunning = true) {
            Socket socket = new Socket();
            try {
                socket.bind(null);
                Log.e(TAG, "Connecting to"+ mAddress.getHostAddress() + " as CLIENT ");
                socket.connect(new InetSocketAddress(mAddress.getHostAddress(), Configuration.SERVER_PORT), 5000);

                // if we connect to the ap
                if (Integer.parseInt(mAddress.getHostAddress().substring(Configuration.prefix.length()+1)) == 1 )
                    fileTransferManager = new FileTransferManager(socket,handler,this.ctx, false /*is a client socket*/ , true /* conect to ap */);
                else
                    fileTransferManager = new FileTransferManager(socket,handler,this.ctx, false /*is a client socket*/ , false /* conect to server */);

                new Thread(fileTransferManager).start();

            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to "+ mAddress.getHostAddress() + "  as  client");
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.e(TAG, "Launching the I/O handler socket exception");
                }
                return;
            }
        }
    }
}