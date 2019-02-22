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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The GroupOwnerSocketHandler class implements a socket server.
 * The class  creates a new socket server listening
 * on the port (as provided in the Configuration class).
 * The implementation of a ServerSocket handler  is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerSocketHandler extends Thread {
    /**
     * socket
     */
    ServerSocket socket = null;
    /**
     * number of threads
     */
    private final int THREAD_COUNT = 10;
    /**
     * handler
     */
    private Handler handler;
    /**
     * context
     */
    private Context ctx;
    /**
     * log related information
     */
    private static final String TAG = "GroupOwnerSocketHandler";

    /**
     * create a handler
     * @param handler handler
     * @param ctx context
     * @throws IOException
     */
    public GroupOwnerSocketHandler(Handler handler, Context ctx) throws IOException {
        Log.e(TAG,"Create GroupOwnerSocketHandler");
        this.ctx = ctx;
        try {
            socket = new ServerSocket(); // <-- create an unbound socket first
            Log.e(TAG,"Created a Server SOCKET");
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(Configuration.SERVER_PORT));
            this.handler = handler;
            Log.e(TAG, "Server socket Started");
        } catch (IOException e) {
            Log.e(TAG, "Server socket NOT Started");
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }
    }

    /**
     * close the socket server
     */
    /**
     * close the server
     */
    public void close()  {
        if (pool.isShutdown() == false)
            pool.shutdownNow();
        if(socket.isClosed() == false) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * start a thread pool for client sockets
     */
    @Override
    public void run() {
        while (true) {
            try {
                //  accept a new connection from a client
                Socket asocket = socket.accept();
                Log.e(TAG, "Server accept a novel socket");
                //pass the socket to the filetransfermanager
                pool.execute(new FileTransferManager(asocket, handler, ctx, true /* is a server socket*/, false /*not a client connecting to ap*/ ));
                Log.e(TAG, "Launching the GROUP handler as SERVER ");
            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {
                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }
}