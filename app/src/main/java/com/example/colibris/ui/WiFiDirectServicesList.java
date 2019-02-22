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
 import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.colibris.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple ListFragment that shows the available services as published by the
 * peers
 */
public class WiFiDirectServicesList extends ListFragment {
    /**
     * List adapter
     */
    WiFiDevicesAdapter listAdapter = null;

    /**
     * listener deterining which device is selected
     */
    interface DeviceClickListener {
        void connectP2p(WiFiP2pService wifiP2pService);
    }

    /**
     * view creation
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState instance state
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.devices_list, container, false);
    }

    /**
     * activity creation
     * @param savedInstanceState instance state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new WiFiDevicesAdapter(this.getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<WiFiP2pService>());
        setListAdapter(listAdapter);
    }



     /**
     *  When the user clicks on a line, the device connects to the other device (or to the group formed by this device)
     *
     * @param l list view
     * @param v view
     * @param position position where we click
     * @param id id
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((DeviceClickListener) getActivity()).connectP2p((WiFiP2pService) l.getItemAtPosition(position));
        ((TextView) v.findViewById(android.R.id.text2)).setText("Connecting");
    }


    /**
     * Adapter
     */

    public class WiFiDevicesAdapter extends ArrayAdapter<WiFiP2pService> {
        /**
         * information are displayed concerning the devices
         */
        private List<WiFiP2pService> items;

        /**
         *
         * @param context context
         * @param resource resource
         * @param textViewResourceId id
         * @param items items (devices)
         */
         public WiFiDevicesAdapter(Context context, int resource, int textViewResourceId, List<WiFiP2pService> items) {
            super(context, resource, textViewResourceId, items);
            this.items = items;
        }

        /**
         *
         * @param i
         * @param service
         * @return if the item has been set
         */
        public boolean setAtAGivenPosition(int i, WiFiP2pService service){
            items.set(i, service);

          return true;
        }

        /**
         *
         * @param position position
         * @param convertView convert view
         * @param parent view group
         * @return view
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
             if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }
            WiFiP2pService service = items.get(position);
            if (service != null) {
                TextView nameText = v.findViewById(android.R.id.text1);
                if (nameText != null) {
                    nameText.setText(service.device.deviceName + " - " + service.instanceName);
                }
                TextView statusText = v.findViewById(android.R.id.text2);

                if (service.isGroupOwner == true){
                    statusText.setText(getDeviceStatus(service.device.status) + " as a group");
                }else{
                    statusText.setText(getDeviceStatus(service.device.status));
                }
            }
            return v;
        }
    }

    /**
     * get the devide status
     * @param statusCode  status of the device
     * @return string defining the device status
     */
    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}