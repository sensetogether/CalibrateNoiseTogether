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

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * The WiFiP2pService class is a structure to hold service information.
 */
public class WiFiP2pService {
    /**
     * device
     */
    WifiP2pDevice device;
    /**
     * instance name
     */
    String instanceName = null;
    /**
     * type of service offered
     */
    String serviceRegistrationType = null;
    /**
     * determine if the device is the wifi direct AP
     */
    Boolean isGroupOwner = false;
}