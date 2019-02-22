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

import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Bundle;

import com.example.colibris.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;

import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.net.wifi.WifiManager;

import com.example.colibris.calib.sound.Filter;
import com.example.colibris.comtool.ContextData;
import com.example.colibris.comtool.SleepListener;
import com.example.colibris.configuration.Me;

import com.example.colibris.configuration.Configuration;
 import com.example.colibris.configuration.Device;
import com.example.colibris.calib.FileManager;
 import com.example.colibris.multi.hypergraph.HyperConnection;
import com.example.colibris.calib.Meet;
import com.example.colibris.calib.Meeting;
import com.example.colibris.calib.regression.MultiRegression;
import com.example.colibris.calib.sound.Record;

import com.example.colibris.nsd.ClientSocketHandler;
import com.example.colibris.nsd.FileTransferManager;
import com.example.colibris.nsd.GroupOwnerSocketHandler;
import com.example.colibris.nsd.NtpTimeConfigurationParam;
import com.example.colibris.nsd.ProtocolState;
import com.example.colibris.nsd.SimpleNTPClient;
import com.example.colibris.nsd.SimpleNTPServer;
import com.example.colibris.nsd.TransferManager;
import com.example.colibris.nsd.WiFiDirectBroadcastReceiver;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.DoubleBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.example.colibris.configuration.Configuration.AVG_LOCAL_FILE_PREFIX;
import static com.example.colibris.configuration.Configuration.AVG_REMOTE_FILE_PREFIX;
import static com.example.colibris.configuration.Configuration.FILE_READ;
import static com.example.colibris.configuration.Configuration.LOCAL_FILE_PREFIX;
import static com.example.colibris.configuration.Configuration.MY_BROADCAST_HANDLE;
import static com.example.colibris.configuration.Configuration.REMOTE_FILE_PREFIX;

/**
 * This activity registers a local (calibration) service and
 * perform discovery over Wi-Fi p2p network. It also hosts a couple of fragments
 * to manage calibration operations. When the app is launched, the device publishes a
 * calibration service and also tries to discover services published by other peers. On
 * selecting a peer published service, the app initiates a Wi-Fi P2P (Direct)
 * connection with the peer. On successful connection with a peer advertising
 * the same service, the app opens up sockets to initiate a calibration process.
 * this fragment is then added to the the main activity which manages
 * the interface and messaging needs for a  session.
 */
public class WiFiServiceDiscoveryActivity extends Activity implements
        WiFiDirectServicesList.DeviceClickListener, Handler.Callback, WifiDirectCalibrationFragment.MessageTarget, SleepListener,
        ConnectionInfoListener {
    /**
     * log related information
     */
    public static final String TAG = "wifidirect";
    /**
     * date related information
     */
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    /**
     * is used to perform a periodical service discovery
     */
    private boolean continueServiceDiscovery = false; //
    /**
     * is used to determine if we should continue registering the service
     */
    private boolean continueregisterService = true;
    /**
     * transfer manager
     */
   private TransferManager broadcasttransferManager;
    /**
     * context
     */
    public Context ctx = this;
    /**
     * swith used to start/stop wifi
     */
    private Switch simpleSwitch ;
    /**
     * wifi direct manager
     */
    private WifiP2pManager manager;
    /**
     * thread to sleep before saving the sound into a file
     */
    private Thread _sleepingThread;
    /**
     * sleep event handler
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * intent filter used by wifi direct
     */
    private final IntentFilter intentFilter = new IntentFilter();
    /**
     * wifi direct channel
     */
    private Channel channel;
    /**
     * used to handle events related to wifi direct
     */
    private BroadcastReceiver receiver = null; //
    /**
     * service request used for service discovery
     */
    private WifiP2pDnsSdServiceRequest serviceRequest;
    /**
     * handler
     */
    private Handler handler = new Handler(this);
    /**
     * fragment
     */
    private WifiDirectCalibrationFragment calibrationFragment;
    /**
     * list of services
     */
    private WiFiDirectServicesList servicesList;
    /**
     * local device
     */
    Me me;
    /**
     * meeting related information
     */
    public Meet meet = null;
    /**
     * test number (identifiy the successive calibrations
     */
    private int testId = 1; // test that is currently running from 1 to max number of consecutive tests
    /**
     * context related data
     */
    public ContextData ctxData = new ContextData();
    /**
     * sleep listener
     */
    private List<SleepListener> listeners = new ArrayList<SleepListener>();

    /**
     * protocol state  : socket started, i.e. we launched the client / server thread
     */
    private boolean socket_started = false; //
    /**
     * protocol state that determines if the device is the AP   of the wifi direct group
     */
    protected boolean isGroupOwner = false; //
    /**
     * protocol state that determine that the group is formed
     */
    private boolean groupAlreadyCreated = false; // determine that
    /**
     * protocol state that we have all the informations (group size ect...) to start recording
     */
    private boolean isReadyToRecord = false ; //
    /**
     * define that we have launched the recording phase
     */
    private boolean isUnderSchedule = false;
    /**
     * define whether we prepared  next calibration
     */
    private boolean isprepared4nextCalibration = false; //
    /**
     * view
     */
    private TextView statusTxtView;
    /**
     * socket server
     */
    private GroupOwnerSocketHandler serverThread = null;
    /**
     * client
     */
    private GoogleApiClient client;

    /**
     * return the handler
     * @return handler
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * set the handler
     * @param handler handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }




    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        statusTxtView = findViewById(R.id.status_text); // is used to display status informations and warning at the bottom
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        me = new Me(this);
        me.setConnectionHyperGraph();
        this.meet = new Meet(this);
        this.meet.setTimeOffset(0);// set that there is no time offset

        // we need to delete all the sound that were recorded before
        File file = new File(Environment.getExternalStorageDirectory() + "/Documents");

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            Log.e(TAG, "check " + files[i].getName());

            if (files[i].getName().startsWith( LOCAL_FILE_PREFIX ) ){
                Log.e(TAG, "delete " + files[i].getName());

                files[i].delete();
            }
            if (files[i].getName().startsWith(REMOTE_FILE_PREFIX )){
                Log.e(TAG, "detele " + files[i].getName());

                files[i].delete();
            }

            if (files[i].getName().startsWith(AVG_LOCAL_FILE_PREFIX)){
                Log.e(TAG, "delete " + files[i].getName());

                files[i].delete();
            }
            if (files[i].getName().startsWith(AVG_REMOTE_FILE_PREFIX   )  ){
                files[i].delete();
            }
        }

        start();

        simpleSwitch = findViewById(R.id.simpleSwitch);// swith wifi on
        simpleSwitch.setChecked(true);
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(true);
                    // is used to trigger events related to wifi direct
                    intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
                    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

                    manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
                    channel = manager.initialize(ctx, getMainLooper(), null);
                    continueregisterService = true; // we need to register the service
                    startRegistrationAndDiscovery();
                } else {

                    manager.clearLocalServices(channel, new ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.e(TAG, "Clear Local Service");
                        }

                        @Override
                        public void onFailure(int error) {
                            Log.e(TAG, "Fail to clear local service");
                        }
                    });


                    manager.clearServiceRequests(channel, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG, "Clear Service Request");
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.e(TAG, "Clear Service Request");
                        }
                    });

                    manager.removeServiceRequest(channel, serviceRequest,
                            new ActionListener() {

                                @Override
                                public void onSuccess() {
                                    Log.e(TAG, " Remove service discovery request");
                                }

                                @Override
                                public void onFailure(int arg0) {
                                    Log.e(TAG, "Fail to remove service discovery request");
                                }
                            });

                    Map<String, String> record = new HashMap<String, String>();
                    WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(Configuration.SERVICE_INSTANCE, Configuration.SERVICE_REG_TYPE, record);
                    manager.removeLocalService(channel, service, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG, " Remove Local service ");
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.e(TAG, "Fail to Remove Local service ");

                        }
                    });

                    // switch down wifi
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                    statusTxtView.setTextColor(Color.parseColor("grey"));

                }
            }
        });


    }

    public void deleteGroups() {

final       WifiP2pManager  manager = (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
     final   Channel channel = manager.initialize(this, this.getMainLooper(), null);
        try {
            Class persistentInterface = null;

            //Iterate and get class PersistentGroupInfoListener
            for (Class<?> classR : WifiP2pManager.class.getDeclaredClasses()) {
                if (classR.getName().contains("PersistentGroupInfoListener")) {
                    persistentInterface = classR;
                    break;
                }

            }


            final Method deletePersistentGroupMethod = WifiP2pManager.class.getDeclaredMethod("deletePersistentGroup", Channel.class, int.class, WifiP2pManager.ActionListener.class);

            //anonymous class to implement PersistentGroupInfoListener which has a method, onPersistentGroupInfoAvailable
            Object persitentInterfaceObject = java.lang.reflect.Proxy.newProxyInstance(persistentInterface.getClassLoader(), new java.lang.Class[]{persistentInterface},
                    new java.lang.reflect.InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
                            String method_name = method.getName();

                            if (method_name.equals("onPersistentGroupInfoAvailable")) {
                                Class wifiP2pGroupListClass =  Class.forName("android.net.wifi.p2p.WifiP2pGroupList");
                                Object wifiP2pGroupListObject = wifiP2pGroupListClass.cast(args[0]);

                                Collection<WifiP2pGroup> wifiP2pGroupList = (Collection<WifiP2pGroup>) wifiP2pGroupListClass.getMethod("getGroupList", null).invoke(wifiP2pGroupListObject, null);
                                for (WifiP2pGroup group : wifiP2pGroupList) {
                                    deletePersistentGroupMethod.invoke(manager, channel, WifiP2pGroup.class.getMethod("getNetworkId").invoke(group, null), new WifiP2pManager.ActionListener() {
                                        @Override
                                        public void onSuccess() {

                                            Log.i(TAG, "Persistent Group deleted");
                                        }

                                        @Override
                                        public void onFailure(int i) {
                                            Log.e(TAG, "Persistent Group could not be deleted");
                                        }
                                    });
                                }
                            }

                            return null;
                        }
                    });

            Method requestPersistentGroupMethod =
                    WifiP2pManager.class.getDeclaredMethod("requestPersistentGroupInfo", Channel.class, persistentInterface);
            requestPersistentGroupMethod.invoke(manager, channel, persitentInterfaceObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * start the calibration
     */
    private void start (){
        //delete the persistent groups of wifi direct
         deleteGroups();

        Log.e(TAG, "service discovery launched");
        continueServiceDiscovery = true;

        // is used to trigger events related to wifi direct
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        // register the calibration service and start the discovery of the other calibration setvices
        startRegistrationAndDiscovery();
        // Create the list of calibration services that are available
        servicesList = new WiFiDirectServicesList();
        getFragmentManager().beginTransaction().add(R.id.container_root, servicesList, "services").commit();
    }

    /**
     * restart the activity
     */
    @Override
    protected void onRestart() {
        Log.e(TAG, "**********\n\n\nrestart.\n **********\n\n\n");
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    /**
     * stop the activity
     */
    @Override
    protected void onStop() {
        Log.e(TAG, "**********\n\n\n stop. \n**********\n\n\n ");
        continueServiceDiscovery = false ; //stop sending periodically some service discovery requests

        manager.cancelConnect(channel,  new ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "Cancel Connect ");
            }

            @Override
            public void onFailure(int error) {
                Log.e(TAG, "Fail cancel connect ");
            }
        });



        manager.removeGroup(channel,   new ActionListener() {

            @Override
            public void onSuccess() {
                Log.e(TAG, "Remove group ");
            }

            @Override
            public void onFailure(int error) {
                Log.e(TAG, "Fail removing group ");
            }
        });


        if (broadcasttransferManager != null)
            broadcasttransferManager.close();

        unregisterReceiver(receiver);

        manager.clearLocalServices(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.e(TAG, "Clear Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.e(TAG, "Fail to clear local service");
            }
        });

        manager.clearServiceRequests(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "Clear Service Request");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Clear Service Request");
            }
        });

        manager.removeServiceRequest(channel, serviceRequest,
                new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.e(TAG, " Remove service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.e(TAG, "Fail to remove service discovery request");
                    }
                });






        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(Configuration.SERVICE_INSTANCE, Configuration.SERVICE_REG_TYPE, record);
        manager.removeLocalService(channel, service, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, " Remove Local service ");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Fail to Remove Local service ");

            }
        });

        // manager.re


        //remove group
        deleteGroups();



        //if a server thread is running, close it (and the related socket)
        if (serverThread != null) {
            serverThread.close();
            Log.e(TAG, "SERVER thread is CLOSED. ");
        }

        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    /**
     * pause the activity
     */
    @Override
    public void onPause() {
        Log.e(TAG, "**********\n\n\n pause. \n**********\n\n\n");
       super.onPause();
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        appendStatus("Restart the local calibration service");


        ///////////////////////////////////

        final Handler handler = new Handler();

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread

                if (continueregisterService == true) {
//                    Log.d("Handlers", "Called on main thread");
                    Map<String, String> record = new HashMap<String, String>();
                    record.put(TXTRECORD_PROP_AVAILABLE, "visible");
                    WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(Configuration.SERVICE_INSTANCE, Configuration.SERVICE_REG_TYPE, record);
                    manager.addLocalService(channel, service, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            appendStatus("Register successfully the local calibration service");
                            continueregisterService = false;
                        }

                        @Override
                        public void onFailure(int error) {
                            appendStatus("Failed to register the local calibration service");
                            continueregisterService = true;
                        }
                    });

                }
                // Repeat this the same runnable code block again another 2 seconds
                // 'this' is referencing the Runnable object

                if (continueServiceDiscovery == true) {
                    handler.postDelayed(this, 10000);
                }

            }
        };
// Start the initial runnable task by posting through the handler
        handler.post(runnableCode);







        /////////////////////////////////////
        discoverService();
    }

    /**
     *
     * Register listeners for DNS-SD services. These are callbacks invoked
     *  by the system when a service is actually discovered.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void discoverService() {

        manager.setDnsSdResponseListeners(channel, new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

                Log.e(TAG, "The following device is offering a service " + srcDevice.deviceName);
                Log.e(TAG, "registration type: " + registrationType );
                Log.e(TAG, "The following service is available " + instanceName );


                // update the UI and add a new  item that include the information related to the discovered device.
                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager().findFragmentByTag("services");

                if (fragment != null) {
                    WiFiDirectServicesList.WiFiDevicesAdapter adapter = ((WiFiDirectServicesList.WiFiDevicesAdapter) fragment.getListAdapter());
                    WiFiP2pService service = new WiFiP2pService();



                    service.device = srcDevice;
                    service.instanceName = instanceName;
                    service.serviceRegistrationType = registrationType;

                    // check if there is a need for adding this service

                    // go through
                    Log.e(TAG, "number of services discovered so far" + adapter.getCount());

                    boolean foundSameServiceAlready = false;
                    for (int i = 0; i< adapter.getCount() ; i++){
                        WiFiP2pService s =  adapter.getItem(i);
                        Log.e(TAG, "service so far " + s.device.deviceName + " " + s.device.isGroupOwner());
                        // check is the device is becoming a group owner, which implies that a group is under creation
                        Log.e(TAG, s.device.deviceName + " - " + service.device.deviceName + " = " +
                                s.device.deviceName.equals(service.device.deviceName) + "\n " +  s.device.isGroupOwner() + "-"+
                                service.device.isGroupOwner() + "\n"+
                                s.device.deviceAddress + "-" + service.device.deviceAddress);



                        if (s.device.deviceName.equals(service.device.deviceName)  && s.device.isGroupOwner() == service.device.isGroupOwner()
                                && s.device.deviceAddress.equals( service.device.deviceAddress) &&
                                service.serviceRegistrationType.equals(s.serviceRegistrationType) && service.instanceName.equals(s.instanceName)
                        ){
                            Log.e(TAG, "device " + s.device.deviceName + " ALREADY KNOWN as " + s.device.isGroupOwner());
                            foundSameServiceAlready = true;
                        }
                        else{


                            //device is known but forms a new group
                            if (s.device.deviceName.equals(service.device.deviceName)  && s.device.deviceAddress.equals( service.device.deviceAddress) &&
                                    service.serviceRegistrationType.equals(s.serviceRegistrationType) && service.instanceName.equals(s.instanceName)
                            ){
                                Log.e("**********************", "new group owner" );
                                service.isGroupOwner = true;
                                adapter.setAtAGivenPosition(i,service);

                                adapter.notifyDataSetChanged();

                                foundSameServiceAlready =true;
                            }

                        }
                    }

                    if (foundSameServiceAlready == false){
                        // add the discovered service to the list of displayed services
                        adapter.add(service);
                        adapter.notifyDataSetChanged();
                        Log.e(TAG, "display the new Service" + service.device.deviceName);
                    }else{
                        Log.e(TAG, " No need to display the new Service" + service.device.deviceName);

                    }

                }
            }
        }, new DnsSdTxtRecordListener() {
            /**
             * A new TXT record is available. Pick up the advertised one
             */
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomainName, Map<String, String> record,
                    WifiP2pDevice device) {
                Log.e(TAG, device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
            }
        });
        // After attaching listeners, create a service request and initiate discovery.
        // this request should be performed periodically
        // Create the Handler object
        final Handler handler = new Handler();

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread

                if (continueServiceDiscovery == true) {
//                    Log.d("Handlers", "Called on main thread");

                    serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                    manager.addServiceRequest(channel, serviceRequest,
                            new ActionListener() {
                                @Override
                                public void onSuccess() {

                                    appendStatus("Added service discovery request");
                                }

                                @Override
                                public void onFailure(int arg0) {
                                    appendError("\nFailed adding service discovery request");
                                }
                            });
                    manager.discoverServices(channel, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            //appendStatus("Service discovery initiated");

                        }

                        @Override
                        public void onFailure(int arg0) {
                            appendError("\nService discovery failed. Please enable Wifi Direct");
                        }
                    });
                }
                // Repeat this the same runnable code block again another 2 seconds
                // 'this' is referencing the Runnable object

                if (continueServiceDiscovery == true) {
                    handler.postDelayed(this, 10000);
                }

            }
        };
// Start the initial runnable task by posting through the handler
        handler.post(runnableCode);

    }

    /**
     * is called wehn the end user select a device on the device list
     * @param service service (calibration service)
     */

    @Override
    public void connectP2p(final WiFiP2pService service) {
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                appendStatus("Connecting to calibration service " + service.device.deviceName);
            }

            @Override
            public void onFailure(int errorCode) {
                appendStatus("Failed connecting to calibration service");
            }
        });
    }

     /**
     * deal with the message that have been received
     * that is the core of the protocol that handle a meeting
     * @param msg message
     * @return whether the message could be handled
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MY_BROADCAST_HANDLE:
                Log.e(TAG, "set broadcast handler");
                break;

            case Configuration.MY_TCP_HANDLE:
                continueServiceDiscovery = false; //set that we do not need anymore a service discovery
                (calibrationFragment).setdiscoveryActivity(this);
                Object file_obj = msg.obj;
                Log.e(TAG, "set the tcp handler");
                (calibrationFragment).setTCPTransferManager((FileTransferManager) file_obj);
                Log.e(TAG, "tcp handler has been set");
                break;
            case FILE_READ: //received a message that has to be handled
                byte[] readFileBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String message = new String(readFileBuf, 0, msg.arg1);
                Log.e(TAG, "handle message" + message);
                computeMessage(message, msg.arg2);
        }
        return true;
    }

     /**
     * handle the messages that have been received starting by analysing the message type
     * @param message message
     * @param from_who message src
     * @return work is done
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private int computeMessage(final String message, int from_who) {
        Log.e(TAG, "Treat  message: " + message);

        // see if the message is aggregated with another one in such a case, split the message
        // check if the message is not only the end of the message = MSG_DELIMIT

        // find where is the message delimiter (i.e., MSG_DELIMITER
        int messageEnd = message.substring(Configuration.MSG_DELIMIT.length()).indexOf(Configuration.MSG_DELIMIT);
        Log.d(TAG, "Message end:  " + messageEnd);
        //find the delimiter of the message
        if (messageEnd != -1) {// the message is aggregated
            Log.d(TAG, "received message is aggregated");
            Log.d(TAG, "Analyse the first received message: " + message.substring(0, messageEnd + Configuration.MSG_DELIMIT.length()));
            Log.d(TAG, "Analyse the second part of the received message: " + message.substring(messageEnd + Configuration.MSG_DELIMIT.length()));
            computeMessage(message.substring(0, messageEnd + Configuration.MSG_DELIMIT.length()), from_who);
            computeMessage(message.substring(messageEnd + Configuration.MSG_DELIMIT.length()), from_who);
            return 0;
        }

        //try to analyse the received message (which is not anymore aggregated
        // if the message start with an order, then analyse the message
        //if the message does not start with an order, it correspond to a remaining of the
        //file that is sent
        if (message.startsWith(Configuration.MSG_ID) == true || message.startsWith(Configuration.MSG_CONNEXION) == true ||
                message.startsWith(Configuration.MSG_RECORD) == true || message.startsWith(Configuration.MSG_TIME) == true || message.startsWith(Configuration.MSG_TEST)==true ) {
            //Although the messages provided by the socket may be aggregated//we need to handle them independently
            //Thus, we need to find where is the delimiter of the next packet
            // while avoiding the beginning of the message which is = delimiter

            if (message.startsWith(Configuration.MSG_TEST) == true) { // we receive a message that say that another calibration should be carried
                // send our connexion file to anyone
                calibrationFragment.pushMessage("Test: receive a calibration order");

                // if we could not prepare the next calibration, then we should do it now
                if (isprepared4nextCalibration ==false)
                {
                    prepareNextCalibration();
                }

                isprepared4nextCalibration = false;
            }

            if (message.startsWith(Configuration.MSG_ID) == true) { // we receive a message that identifies the device we are communicating with
                //  String whoIam = "ID=" + VertexId + "," + DeviceName + "," + deviceId + "#" + list of devices we know;
                int delimit = message.indexOf(",");//search for the first "," which delimit the vertex id, the device name and the computer id
                int delimit_device = message.indexOf("#"); // search of # which delimit the list of known devices
                //extract the vertex id
                String vertexString = message.substring(Configuration.MSG_ID.length(), delimit);// Log.d(TAG, "Vertex id : " + vertexString);
                //extract device name
                int second_delimit = message.indexOf(",", delimit + 1);
                String deviceName = message.substring(delimit + 1, second_delimit);
                //extract the computer id (last digit of the ip address
                String id = message.substring(second_delimit + 1, delimit_device);
                // add the device that sent this messages to the list of meeting devices
                Device device2meet = new Device(Integer.valueOf(vertexString), deviceName, Integer.valueOf(id));
                this.meet.add(device2meet, this /*ctx*/, false /*append*/); // add the device if the device is not already in

                Log.e(TAG, "IDENTIFICATION MESSAGE : " + "I am " + vertexString + "," + deviceName + "," + id);
                (calibrationFragment).pushMessage("discover " + deviceName + " ip: " + id + " vertex: " + vertexString);




                //extract the other known devices
                while (message.indexOf("#", delimit_device + 1) != -1) {
                    int next_delimit = message.indexOf("#", delimit_device + 1);// delimit the next known device
                    String pdevice = message.substring(delimit_device + 1, next_delimit); // informations related to the known device
                    delimit_device = next_delimit;
                    Log.e(TAG, "  know device: " + pdevice);

                    // extract the vertex id , device name, id of the device
                    delimit = pdevice.indexOf(",");
                    vertexString = pdevice.substring(0, delimit);

                    second_delimit = pdevice.indexOf(",", delimit + 1);
                    deviceName = pdevice.substring(delimit + 1, second_delimit);
                    id = pdevice.substring(second_delimit + 1);
                    (calibrationFragment).pushMessage("Know " + deviceName + " ip " +  id+  " vertex: " + vertexString);
                    Log.e(TAG, "  know vertex: " + vertexString + "  know device name: " + id);

                    //add this advertised device to the list of devices I am meeting with if not already done
                    Device knowndevice2meet = new Device(Integer.valueOf(vertexString), deviceName, Integer.valueOf(id));
                    if (Me.getVertexId() != knowndevice2meet.getVertexId()) {
                        Log.e(TAG, "add to the meeting group this yet unknown device " + knowndevice2meet.getVertexId());
                        this.meet.add(knowndevice2meet, this /*ctx*/, false /*append*/); // add the device if the device is not already in

                        // if i am not the ap, I start a client to communicate with this device latter on
                        if (this.isGroupOwner == false) {
                            //  start the socket clients to send the sound files to all the other group members
                            final String s = Configuration.prefix + "." + knowndevice2meet.deviceId;

                            if (knowndevice2meet.deviceId != 1) {
                                (calibrationFragment).pushMessage("- " + "start a client to communicate with " + knowndevice2meet.deviceId);
                                Log.e(TAG,  "start a client to communicate with " + knowndevice2meet.deviceId);
                                try {
                                    InetAddress destAddr = InetAddress.getByName(s);
                                    Thread clientThread = new ClientSocketHandler(((WifiDirectCalibrationFragment.MessageTarget) this).getHandler(), destAddr, this);
                                    clientThread.start();
                                } catch (UnknownHostException e) {
                                    Log.e(TAG, "cannot create an address for " + s);
                                    e.printStackTrace();
                                }
                            }
                        }

                    } else {
                        Log.e(TAG, "do not add myself = the known device" + knowndevice2meet.getVertexId());
                    }
                }
                isReadyToRecord = true; // i know at least one device => we can proceed with calibration and start recording when asked
            }
            //receive a connexion file
            if (message.startsWith(Configuration.MSG_CONNEXION) == true) {
                if (this.meet.getPosition(from_who) == -1) {
                    // that is not fine because, we did not receive the message "who I am" from that device
                    (calibrationFragment).pushMessage("Warning: start receiving connexion graph from an unknown device: " + from_who);
                } else {
                    //set that we are receiving the connexion for that device
                    // get the protocol state for that device

                    ProtocolState p = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));
                    p.isReceivingConnexion = true; // protocol state = is receiving connexion
                    this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), p); // set that we received the
                    // that is fine, we found the device (we received the message who I am
                    (calibrationFragment).pushMessage("start receiving connexion graph from " + this.meet.meetwithS.get(this.meet.getPosition(from_who)).name);
                    Log.e(TAG, "start receiving remote connexion graph from" + this.meet.meetwithS.get(this.meet.getPosition(from_who)).name + " ip:" + from_who);

                    // get the protocol state for that device
                    ProtocolState pr = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));
                    //create a new string for the connexion
                    pr.receivedConnexionGraph = new String();
                    this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), pr);// update the protocole state
                    this.handleReceivedConnexion(message.substring(Configuration.MSG_CONNEXION.length()), from_who);// handle the recevived connexion file
                }

                if (this.isGroupOwner == true && isUnderSchedule == false) {
                    //schedule the recording  and send this info to the other device)
                    //check if we have already send our name and connexion file, if not, send it before scheduling the record time
                    isUnderSchedule = true;
                    Log.e(TAG, "time to schedule recording, sleep for  " + Configuration.SLEEPDURBEFORESCHEDULERECORD_MS + " ms");

                    this.addSleepListener(this);
                    try {
                        this.sleepingThread(Configuration.SLEEPDURBEFORESCHEDULERECORD_MS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //I receive the time when is scheduled the recording
            if (message.startsWith(Configuration.MSG_TIME) == true) {
                //extract from the message the time when the sound is recorded
                String time2schedule_S = message.substring(Configuration.MSG_TIME.length());
                double received_time2schedule = Double.parseDouble(time2schedule_S);

                // consider time offset => synchronised scheduled time = received_scheduled_timetime2wait - time offset
                double localTimeToSchedule = received_time2schedule - meet.getTimeOffset();

                (calibrationFragment).pushMessage("Time offset: " + meet.getTimeOffset()  ); Log.e(TAG, "Time Offset: " + meet.getTimeOffset() );
                (calibrationFragment).pushMessage("Receive scheduling order (wake up in "
                        + received_time2schedule + " ms = wake up at the following local time: " + localTimeToSchedule);
                Log.e(TAG, "Time Offset: " + "Receive scheduling order (wake up at "
                        + received_time2schedule + " ms = wake up local time:" + localTimeToSchedule);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getDefault());

                (calibrationFragment).pushMessage("Schedule  sound record at " + sdf.format(new Date((long) received_time2schedule)) + "=" +
                        "" + sdf.format(new Date((long) localTimeToSchedule)));

                Log.e(TAG, "receive a scheduling order : record at" + sdf.format(new Date((long) received_time2schedule)) + "=local synchronised time: "
                        + sdf.format(new Date((long) localTimeToSchedule)));
                //schedule the recording as asked by remote device

                if (isReadyToRecord == true){
                    (calibrationFragment).scheduleRecord(this.meet, (long) localTimeToSchedule /*synchronised time*/);
                }
                else{
                    (calibrationFragment).pushMessage("Warning:  sound recording cannot be sheduled, device joined the group to let ");
                    Log.e(TAG, "Warning: The sound cannot be sheduled, device joined the group to let ");
                    //schedule the recording as asked by remote device
                    //avoid recording, calibrating, just wait next calibration
                }
            }

            // I received the recorded sound
            if (message.startsWith(Configuration.MSG_RECORD) == true) {
                //get the protocol state for that device so as to state that we are recieving the sound
                ProtocolState pr = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));
                pr.isReceivingSound = true; // protocol state = we are receiving sound
                pr.ismd5extractedFromSound = false; // we did not yet extracted md5 and size
                pr.receivedSound = new String(); // create a string to receive the sound
                this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), pr);

                String recordedSound = message.substring(Configuration.MSG_RECORD.length());
                Log.e(TAG, "Start receiving sound:" + recordedSound);
                (calibrationFragment).pushMessage("Start receiving sound from " + this.meet.meetwithS.get(this.meet.getPosition(from_who)).name /*+ message*/);

                handleReceivedSound(recordedSound, from_who);// save the sound into a file
            }
        } else { // the remaining of a file is being transmitted it can correspond to a remaining sound or to a remaining graph
            //todo deal with an error because packet is unknown
            Log.e(TAG, "check the protocol state" + this.meet.getPosition(from_who));
            ProtocolState pr = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));

            if (pr.isReceivingSound == true) {
                Log.e(TAG, "Still receiving sound... "/* + message*/);

                (calibrationFragment).pushMessage("still receiving sound .... from " + meet.meetwithS.get(meet.getPosition(from_who)).name);
                handleReceivedSound(message, from_who);// save the sound into a file
            } else {
                if (pr.isReceivingConnexion == true) {
                    (calibrationFragment).pushMessage("still receiving connexion file ...from " + meet.meetwithS.get(meet.getPosition(from_who)).name);
                    handleReceivedConnexion(message, from_who);
                }
            }
        }
        return 1;
    }

    /**
     * create a checksum for the content that is provided
     * @param buffer content
     * @return chacksum
     */
    public byte[] createChecksum(byte[] buffer) {
        try {
            if (buffer != null) {
                MessageDigest complete = MessageDigest.getInstance("MD5");
                complete.update(buffer, 0, buffer.length);
                return complete.digest();
            } else {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * actions to perform when a connexion graph is received
     * @param receivedConnexion connexion graph
     * @param from_who source of the connexion graph
     */
    private void handleReceivedConnexion(String receivedConnexion, int from_who) {
        Log.e(TAG, "handle received connexion: " + receivedConnexion);
        ProtocolState pr = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));
        // check if we received all the connexion graph
        if (receivedConnexion.startsWith(Configuration.MSG_FILE_END, receivedConnexion.length() - Configuration.MSG_FILE_END.length() /*offset */) == true) {
            pr.isReceivingSound = false; // we are no longer receiving the connexion
            this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), pr);

            //isReceivingConnexion = false; // we are no longer receiving the connexion
            //append the received part of the connexion graph to the already received parts

            pr.receivedConnexionGraph = pr.receivedConnexionGraph.concat(receivedConnexion.substring(0, receivedConnexion.length() - Configuration.MSG_FILE_END.length()));
            Log.e(TAG, "all the connection graph including md5" + pr.receivedConnexionGraph);
            Log.e(TAG, "append");
            // extract the md5 of the connexion graph
            String receivedMd5String = pr.receivedConnexionGraph.substring(Configuration.MSG_MD5.length(), pr.receivedConnexionGraph.indexOf(","));
            Log.e(TAG, "Received md5: " + receivedMd5String);

            String connexionGraph = pr.receivedConnexionGraph.substring(pr.receivedConnexionGraph.indexOf(",") + 1);
            Log.e(TAG, "Received connexion graph: " + connexionGraph);
            //compute the check sum of the received connexion graph
            byte[] computedCheckSum = createChecksum(connexionGraph.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < computedCheckSum.length; i++)
                sb.append(Integer.toString((computedCheckSum[i] & 0xff) + 0x100, 16).substring(1));
            String computedChecksum = sb.toString();
            Log.e(TAG, "computed md5: " + sb.toString());
            Log.e(TAG, "comparison between checksum:" + computedChecksum.compareTo(receivedMd5String));

            // check if the received checksum = novelly computed checksum and if received size = computed size
            if (computedChecksum.compareTo(receivedMd5String) == 0) {//
                Log.e(TAG, "Integrity of the received connexion graph is ok");
                HyperConnection receivedConnection = new HyperConnection();
                receivedConnection.getfromString(connexionGraph);

                Log.e(TAG, "Before receiving connexion graph" + me.localconnectionGraph.toString());
                me.localconnectionGraph.add(receivedConnection);//merge the two connection graphs
                Log.e(TAG, "after adding remote connexion graph ************************* " + me.localconnectionGraph.toString());
                me.localconnectionGraph.toFile(this);     //save the novel connection graph
                Log.e(TAG, "after saving connexion graph and getting it" + me.localconnectionGraph.toString());
                (calibrationFragment).pushMessage("Newly merged connexion graph: " + me.getLocalconnectionGraph().toString());

                // if i am the AP, I should broadcast a novel hello message including my updated connexion file
                // if I am not the AP, send the message to the ap
                if (this.isGroupOwner == true)
                    (calibrationFragment).sendHelloandConnexion();
            } else {
                (calibrationFragment).pushMessage(" Corruption of the connexion graph provided by " + meet.meetwithS.get(meet.getPosition(from_who)).name);
            }
        } else {
            Log.e(TAG, "connexion graph not completely received");
            pr.receivedConnexionGraph = pr.receivedConnexionGraph.concat(receivedConnexion);
        }
    }

    /**
     * the sound provided by another device that is in sensing and communication range, has been received
     * @param recordedSound sound
     * @param from_who source of the sound
     */
    private void handleReceivedSound(String recordedSound, int from_who) {
        // if the md5 has not been extracted yet, extract it
        ProtocolState pr = this.meet.meetwithProtocolState.get(this.meet.getPosition(from_who));
        if (pr.ismd5extractedFromSound == false) {
            pr.receivedSound = pr.receivedSound.concat(recordedSound);

            // check if we received all the md5
            if (pr.receivedSound.startsWith(Configuration.MSG_MD5) && pr.receivedSound.indexOf(",") != -1) {
                // extract md5
                Log.e(TAG, ", found at " + pr.receivedSound.indexOf(","));
                pr.receivedSoundMd5String = pr.receivedSound.substring(Configuration.MSG_MD5.length(), pr.receivedSound.indexOf(","));
                Log.e(TAG, "Received md5: " + pr.receivedSoundMd5String);

                recordedSound = pr.receivedSound.substring(pr.receivedSound.indexOf(",") + 1);
                pr.ismd5extractedFromSound = true;
                pr.receivedSound = new String();
            }    //else we did not receive yet the md5 -> we cannot extract yet this information
            // we will wait for the next packet to be send and then we will extract it
        }



        //check if we received the end of the sound file
        if (recordedSound.startsWith(Configuration.MSG_FILE_END, recordedSound.length() - Configuration.MSG_FILE_END.length() /*offset */) == true) {
            (calibrationFragment).pushMessage("Sound has been completely received from " + meet.meetwithS.get(meet.getPosition(from_who)).name);
            pr.isReceivingSound = false;

            Device adevice = this.meet.meetwithS.get(this.meet.getPosition(from_who));

            (calibrationFragment).pushMessage("save sound provided by   device " + adevice.getVertexId() + " name: " + adevice.name + " id: " + adevice.deviceId + " position: " + this.meet.getPosition(from_who));
            Log.e(TAG, "save the remote sound for device " + adevice.toString() + "position is given by " + this.meet.getPosition(from_who));
            //check   the received md5

            //remove the end delimiter before saving it
            meet.saveRemoteRecord(recordedSound.substring(0, recordedSound.length() - Configuration.MSG_FILE_END.length()), /*which device*/ this.meet.meetwithS.get(this.meet.getPosition(from_who)));
            //check   the received md5
            //compute the check sum of the received sound
            Log.e(TAG, "Compute the md5 of sound ");

            byte[] computedCheckSum = createChecksum(this.meet.getRemoteFile4NoiseName(this.meet.meetwithS.get(this.meet.getPosition(from_who))));

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < computedCheckSum.length; i++)
                sb.append(Integer.toString((computedCheckSum[i] & 0xff) + 0x100, 16).substring(1));
            String computedChecksum = sb.toString();
            (calibrationFragment).pushMessage("computed md5: " + computedChecksum);

            (calibrationFragment).pushMessage("received md5: " + pr.receivedSoundMd5String);
            Log.e(TAG, "received md5 (of sound): " + pr.receivedSoundMd5String);
            pr.receivingSoundCompleted = true; // we receive the sound
            /////////////////////
            // check if  checksum is ok
            if (computedChecksum.compareTo(pr.receivedSoundMd5String) == 0) {
                (calibrationFragment).pushMessage("No corruption of the sound provided by " +
                        meet.meetwithS.get(meet.getPosition(from_who)).name);
                Log.e(TAG, "Received sound is not corrupted");
                pr.ismd5extractedFromSound = false;
                this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), pr);
            }
            else
            {// received sound is corrupted
                calibrationFragment.pushMessage("Warning: Corruption of the sound sent by " + meet.meetwithS.get(meet.getPosition(from_who)).name);

                pr.isSoundCorrupted = true; // state that the sound is corrupted
                this.meet.meetwithProtocolState.set(this.meet.getPosition(from_who), pr);
                // we cannot calibrate because the sound file is corrupted
                calibrationFragment.canCalibrate = false;
                //  this.prepareNextCalibration();
            }




            //todo if the sound is received from the AP and if i already record my local sound then synchronise my local sound
            if (calibrationFragment.hasAlreadyFinishedRecording == true && from_who == 1) {
                Log.e(TAG, "We synchronise and send the local sound (recording is termined)");
                (calibrationFragment).pushMessage("Send the synchronised and local sound (recording is termined and AP sound is received)");
                // extract local sound
                DoubleBuffer localSound = meet.getLocalFileManager4Noise().getSound();
                for (int i = 0; i < meet.meetwithS.size(); i++) {
                    if (meet.meetwithS.get(i).deviceId == 1) {//  i found  the ap
                        calibrationFragment.pushMessage("for device " + meet.meetwithS.get(i).getVertexId()
                                + " id: " + meet.meetwithS.get(i).deviceId + ", round trip delay: " +
                                meet.roundTripDelay + "ms" + ", offset: " + meet.getTimeOffset());
                        Log.e(TAG, "for device " + meet.meetwithS.get(i).getVertexId()
                                + " id: " + meet.meetwithS.get(i).deviceId + ", round trip delay: " + meet.roundTripDelay + "ms" +
                                ", offset: " + meet.getTimeOffset());

                        //get the sound for the AP
                        DoubleBuffer remoteSound = meet.files4noise.get(i + 1).getSound();

                        Log.e(TAG, "Create a filter delaying at max with " + meet.roundTripDelay);
                        calibrationFragment.pushMessage("Create a filter delaying at max with " + meet.roundTripDelay);

                        Filter filter = new Filter(localSound, remoteSound, meet.roundTripDelay);
                        calibrationFragment.pushMessage("for " + meet.meetwithS.get(i).getVertexId() +
                                " offset (#samples): " + filter.offset + "\n normalised offset (#samples):" + filter.norm_offset);
                        Log.e(TAG, "for " + meet.meetwithS.get(i).getVertexId() +
                                " offset (#samples): " + filter.offset + "\n normalised offset (#samples):" + filter.norm_offset);

                        // shift the starting time
                        double delay;
                        if (Configuration.isAveraging = true) {
                            delay = filter.norm_offset * Configuration.samplingDurationSec * 1000;
                        } else {
                            delay = Record._rate * Configuration.samplingDurationSec * filter.norm_offset;
                        }

                        calibrationFragment.pushMessage("for " + meet.meetwithS.get(i).getVertexId() + "normalised offset (ms) : " + delay);
                        // rewrite local sound file using the normalised delay compute the delay in milisecond
                        double slot_millisec;
                        if (Configuration.isAveraging == false)
                            slot_millisec = (double) 1000 / (double) Record._rate; // compute the delay between two recording
                        else // save the avg recording into a file
                            slot_millisec = Configuration.samplingDurationSec * 1000; // 0.125 second = 0.125 *1000 = 125 ms

                        double startTime = meet.getLocalFileManager4Noise().getStartTime();
                        Log.e(TAG, "before delaying, local start time is : " + startTime);
                        calibrationFragment.pushMessage("before delaying local start time is : " + startTime);
                        Log.e(TAG, "before delaying, local start time should be after : " + startTime + " + " + delay + "=" + (startTime + delay));
                        calibrationFragment.pushMessage("before delaying local start time should be after: " + startTime + " + " + delay + "=" + (startTime + delay));

                        FileManager localFileManager = new FileManager(meet.getlocalFile4NoiseName(), false, this);
                        localFileManager.write_calibration_buffer(localSound, startTime + delay, slot_millisec);

                        Log.e(TAG, "after delaying, local start time is : " + meet.getLocalFileManager4Noise().getStartTime());
                        calibrationFragment.pushMessage("before delaying local start time is : " + meet.getLocalFileManager4Noise().getStartTime());
                        localSound.clear();
                    }
                }


                calibrationFragment.clientSendSound();


            }




            // check if I have enough information to calibrate (i.e., I get local and remote file)
            int nb_of_remote_not_corrupted_sound = 0;
            int nb_of_remote_corrupted_sound = 0;

            // count the number of  remote sounds that have been received and that are either corrupted or not corrupted
            for (int i = 0; i < meet.meetwithS.size(); i++) {
                if (meet.meetwithProtocolState.get(i).receivingSoundCompleted && !meet.meetwithProtocolState.get(i).isSoundCorrupted)
                    nb_of_remote_not_corrupted_sound++;

                if (meet.meetwithProtocolState.get(i).receivingSoundCompleted && meet.meetwithProtocolState.get(i).isSoundCorrupted)
                    nb_of_remote_corrupted_sound++;
            }


            // if we have the local recording and we obtained  all the remote recording => we can calibrate
            if (    calibrationFragment.hasAlreadyFinishedRecording == true) {
                if (nb_of_remote_not_corrupted_sound == meet.meetwithS.size()) {

                    String printing = meet.calibrate();
                    calibrationFragment.pushMessage("- " + printing);

                    meet.add_uncalibrated_devices();


                    //draw a button that ask if we save this calibration
                    // determine if the device should calibrate or not, or if the calibration should be save
                    if (Configuration.SHOULD_SIMPLY_CALIBRATE || Configuration.SHOULD_ROBUSTLY_CALIBRATE || Configuration.SHOULD_BE_SAVED)
                    {
                        //display a button : calibrate/proceed and a cancel button
                        calibrationFragment.cancelButton.setVisibility(View.VISIBLE);
                        calibrationFragment.calibrateButton.setVisibility(View.VISIBLE);

                        // the device should calibrate (using robust calibration)
                        if (Configuration.SHOULD_ROBUSTLY_CALIBRATE || Configuration.SHOULD_SIMPLY_CALIBRATE ) {
                            calibrationFragment.calibrateButton.setText("Calibrate");
                            if (Configuration.SHOULD_ROBUSTLY_CALIBRATE){
                                //radio button set to robust calibration
                                calibrationFragment.simple_calibrate_radio.setChecked(false);
                                calibrationFragment.simple_calibrate_radio.setVisibility(View.VISIBLE);
                                calibrationFragment.robust_calibrate_radio.setVisibility(View.VISIBLE);

                                calibrationFragment.robust_calibrate_radio.setChecked(true);
                            }
                            if (Configuration.SHOULD_SIMPLY_CALIBRATE){
                                calibrationFragment.simple_calibrate_radio.setChecked(true);
                                calibrationFragment.robust_calibrate_radio.setChecked(false);
                                calibrationFragment.simple_calibrate_radio.setVisibility(View.VISIBLE);
                                calibrationFragment.robust_calibrate_radio.setVisibility(View.VISIBLE);
                            }
                        }else{
                            if (Configuration.SHOULD_BE_SAVED == true){
                                calibrationFragment.calibrateButton.setText("Proceed");
                            }
                        }
                    }

                    Configuration.SHOULD_SIMPLY_CALIBRATE = false;
                    Configuration.SHOULD_ROBUSTLY_CALIBRATE = false;
                    Configuration.SHOULD_BE_SAVED = false;

                    prepareNextCalibration();
                }
                else{ // we cannot calibrate because there is a corrupted file or we did not received yet all   the files
                    if ( nb_of_remote_corrupted_sound ==0 ){
                        calibrationFragment.pushMessage("Cannot calibrate yet, there is not corrupted sound but ...");
                        calibrationFragment.pushMessage("Cannot calibrate yet, number of sound received  " +
                                nb_of_remote_not_corrupted_sound  +   " <  meeting size: " + meet.meetwithS.size() );
                    }else{// we will never calibrate but I go to next calibration only if I received all the file (corrupted or not)
                        calibrationFragment.canCalibrate = false;
                        if ( nb_of_remote_corrupted_sound + nb_of_remote_not_corrupted_sound ==  meet.meetwithS.size()){
                            calibrationFragment.pushMessage("- " + "Calibration is aborted because "+ nb_of_remote_corrupted_sound +
                                    "  file(s) is/are corrupted");
                            prepareNextCalibration();
                        }
                    }
                }
                isUnderSchedule = false;
            } else {//we cannot calibrate yet, because recording is not finished
                Log.e(TAG, "Local record is needed for calibration");
                calibrationFragment.pushMessage("Sound is provided by " + from_who + " but local sound is not yet recorded");
                if (nb_of_remote_not_corrupted_sound == meet.meetwithS.size()) {
                    calibrationFragment.canCalibrate = true;// we state that we can calibrate, we 'll calibrate only when the local sound recording will be completed
                }
            }







        } else {//we are not at the end of the file, simply save the block into the file
            meet.saveRemoteRecord(recordedSound, this.meet.meetwithS.get(this.meet.getPosition(from_who)));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault());
    }






    // ISNOT USED ANYMORE meet.calibrate is called instead

    /**
     * time to calibrate
     */
    public void calibrate() {

        List<FileManager> fileList = new ArrayList<FileManager>();

        List<FileManager> crossfileList = new ArrayList<FileManager>();

        // correlate the sample to determine the time offset between the two sounds

        DoubleBuffer localSound = meet.getLocalFileManager4Noise().getSound();

        for (int i = 0; i < meet.meetwithS.size(); i++) {
            Log.e(TAG, "get the offset for device " + meet.meetwithS.get(i).getVertexId());
            //get the sound for the remove device
            DoubleBuffer remoteSound = meet.files4noise.get(i + 1).getSound();

            Filter filter = new Filter(localSound, remoteSound, meet.roundTripDelay);
            (calibrationFragment).pushMessage("for " + meet.meetwithS.get(i).getVertexId() +
                    " offset : " + filter.offset + "\n normalised offset:" + filter.norm_offset);
            // shift the starting time
            double delay;
            if (Configuration.isAveraging = true) {
                delay = filter.norm_offset * Configuration.samplingDurationSec * 1000;
            } else {
                delay = Record._rate * Configuration.samplingDurationSec * filter.norm_offset;
            }
            // rewrite remote sound file
            double slot;
            if (Configuration.isAveraging == false)
                slot = (double) 1000 / (double) Record._rate; // compute the delay between two recording
            else // save the avg recording into a file
                slot = Configuration.samplingDurationSec * 1000; // 0.125 second = 0.125 *1000 = 125 ms

            double startTime = meet.files4noise.get(i + 1).getStartTime();
            Log.e(TAG, "START TIME IS : " + startTime);
            // add an offset to the remote file

            //create a file manager
            FileManager remoteFileManager = new FileManager("cross" + meet.getRemoteFile4NoiseName(meet.meetwithS.get(i)), false, this);
            remoteFileManager.write_calibration_buffer(remoteSound, ((long) startTime + filter.norm_offset), slot);

            // copy the local file
            startTime = meet.getLocalFileManager4Noise().getStartTime();
            FileManager localFileManager = new FileManager("cross" + meet.getlocalFile4NoiseName(), false, this);
            localFileManager.write_calibration_buffer(localSound, (long) startTime, slot);

            localSound.clear();
            remoteSound.clear();

            fileList.add(meet.files4noise.get(1 + meet.getPositionfromVertex(meet.meetwithS.get(i).getVertexId())));
            // fileList.add(meet.remoteFile4Noise);
            crossfileList.add(remoteFileManager);
        }

        MultiRegression multiRegress = new MultiRegression(meet.getLocalFileManager4Noise(), fileList);

        String toprint = multiRegress.toString();
        // Log.e(TAG, "multiple Regress" + toprint);
        (calibrationFragment).pushMessage("n********Multiple Regression:\n" + toprint);

        MultiRegression crossmultiRegress = new MultiRegression(meet.getLocalFileManager4Noise(), crossfileList);
        String tocrossprint = crossmultiRegress.toString();
        (calibrationFragment).pushMessage("n********Cross Multiple Regression:\n" + tocrossprint);

        //todo   we should  use correlated multipe robust regression instead ?


        for (int i = 0; i < meet.meetwithS.size(); i++) {
            double startTime = meet.files4noise.get(i + 1).getStartTime();
            double[] beta = multiRegress.regression.estimateRegressionParameters();
            double[] regressstd = multiRegress.regression.estimateRegressionParametersStandardErrors();
            Meeting meetingcahracteristics = new Meeting(beta[0], beta[i + 1] /*slope*/,
                    regressstd[i] /*simpleregression.slopeStdErr*/, multiRegress.regression.calculateTotalSumOfSquares(), (double) Configuration.recordDuration_ms,
                    multiRegress.regression.calculateRSquared()      /*RSquare*/, startTime, multiRegress.regression.calculateResidualSumOfSquares()/*sumSquaredErrors*/);

            (calibrationFragment).pushMessage("add edge between " + this.me.localDevice.getVertexId() + "and " + this.meet.meetwithS.get(i).getVertexId());
            //TODO  me.localconnectionGraph.addEdge(this.me.localDevice, this.meet.meetwithS.get(i), meetingcahracteristics);
            //save the connexion graph into a file
            me.localconnectionGraph.toFile(this);
            //TODO me.setConnectionGraph();
        }




    }

    /**
     * compute the checksum of a file
     * @param filename file name
     * @return check sum
     */
    public byte[] createChecksum(String filename) {
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {
            // open the given file
            File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");

            File file = new File(docsFolder.getAbsolutePath(), filename);
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
            return null;
        }
    }

    /**
     * the activity is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

    }

    //

    /**
     * is called when a connection is established
     * @param p2pInfo information related to the p2p connexion
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

        /*deal with the connection to establish (socket and so one)*/
        Thread clientThread = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}*/
        // if we did not yet started the socket
        Log.e(TAG, "group already created:" + socket_started + " is group owner now: " + p2pInfo.isGroupOwner
                + " was group owner" +this.isGroupOwner);


        if (socket_started == false) {
            Log.e(TAG, "The group is created");
            socket_started = true;
            calibrationFragment = new WifiDirectCalibrationFragment(); // display the fragment with the messages
            getFragmentManager().beginTransaction().replace(R.id.container_root, calibrationFragment).commit();
            statusTxtView.setVisibility(View.GONE);
            simpleSwitch.setVisibility(View.GONE);



            if (p2pInfo.isGroupOwner) {

                this.isGroupOwner = true;

                Log.e(TAG, "************************************");
                Log.e(TAG, "************************************");
                Log.e(TAG, "CONNECTED AS GROUP OWNER");
                Log.e(TAG, "************************************");
                Log.e(TAG, "************************************");

                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);

                meet.isTimeServer = true;
                meet.setTimeOffset(0); //reinitialise the time offset (local time of the group leader - my local time
                meet.roundTripDelay =0; // no round trip delay with myself
                meet.isSynchronised = true; // i am synchronised with myself
                ContextData.timeOffsetCtxData = 0;

                //start tcp server and udp server only if they have not be started before (i.e., started only when the group is formed
                // not everytime a new memmber is added to the group
                if (this.groupAlreadyCreated == false) {
                    this.groupAlreadyCreated = true; // next time, no need to start the udp/tcp servers
                    try {
                        Log.e(TAG, "launch tcp server");
                        serverThread = new GroupOwnerSocketHandler(((WifiDirectCalibrationFragment.MessageTarget) this).getHandler(), this);
                        serverThread.start();

                        // launch the ntp server
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    SimpleNTPServer timeServer = new SimpleNTPServer(Configuration.ntp_ports);
                                    timeServer.start( );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        new Thread(runnable).start();
                        // timeServer.run();
                    } catch (IOException e) {
                        Log.e(TAG, "PROBLEM: AP Failed to create a server thread - " + e.getMessage());
                        return;
                    }
                }
            } else {// start as a member of the group (not the ap)
                Log.e(TAG, "************************************");
                Log.e(TAG, "************************************");
                Log.e(TAG, "Connected as peer");
                Log.e(TAG, "************************************");
                Log.e(TAG, "************************************");
                // start tcp client to communicate with the AP
                Log.e(TAG, "client launchs the tcp client to communicate with the AP");

                clientThread = new ClientSocketHandler(((WifiDirectCalibrationFragment.MessageTarget) this).getHandler(), p2pInfo.groupOwnerAddress, this);
                clientThread.start();
                // start a tcp server to receive the sounds from other group members (excluding the ap)
                meet.isTimeServer = false;

                Log.e(TAG, "Client start ntp client to synchronise with AP");
                // I send an ntp request so as to get synchronised my the AP
                SimpleNTPClient ntpclient = new SimpleNTPClient();
                NtpTimeConfigurationParam newconfiguration =  ntpclient.sendNTPRequest( Configuration.ntp_ports);

                if(NtpTimeConfigurationParam.isSynchronised){
                    meet.isSynchronised = true; // i am synchronised now (I receive an ntp answer
                    meet.setTimeOffset(NtpTimeConfigurationParam.timeOffset);
                    meet.roundTripDelay = NtpTimeConfigurationParam.roundTripDelay;
                    ContextData.timeOffsetCtxData = NtpTimeConfigurationParam.timeOffset;
                    Log.e(TAG,  "ntp time offset: " + NtpTimeConfigurationParam.timeOffset + " round trip: "  + NtpTimeConfigurationParam.roundTripDelay +
                            " is synchronised: " + NtpTimeConfigurationParam.isSynchronised);
                }else{
                    Log.e(TAG, "problem, I cannot synchronise with the client ");
                }

                try {
                    Log.e(TAG, "launch the tcp server on the client ");
                    serverThread = new GroupOwnerSocketHandler(((WifiDirectCalibrationFragment.MessageTarget) this).getHandler(), this);
                    serverThread.start();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create a server thread on the client- " + e.getMessage());
                    return;
                }

                Map<String, String> record = new HashMap<String, String>();
                record.put(TXTRECORD_PROP_AVAILABLE, "visible");
                WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(Configuration.SERVICE_INSTANCE, Configuration.SERVICE_REG_TYPE, record);
                manager.removeLocalService(channel, service, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, " Remove Local service ");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(TAG, "Failed to Remove Local service ");
                    }
                });
            }
        }
    }





    /**
     *display an error message on the screen
     * @param status
     */
    public void appendError(String status) {
        String current = statusTxtView.getText().toString();
        statusTxtView.setTextColor(Color.parseColor("red"));

        //  statusTxtView.setText(current + "\n" + status);
        statusTxtView.append(status);
    }

    /**/

    /**
     *  display a message on the screen
     * @param status message
     */
    public void appendStatus(String status) {
        statusTxtView.setTextColor(Color.parseColor("grey"));

        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("WiFiServiceDiscovery Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    /**
     * activity is started
     */
    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    /**
     * init the location listener
     */
    public void InitialiseLocationListener() {

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(android.location.Location location) {

                String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
                    Log.d("Location", "Time GPS: " + time); // This is what we want!
                else
                    Log.d("Location", "Time Device (" + location.getProvider() + "): " + time);
            }

            public void onStatusChanged(String provider, int status, android.os.Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    /**
     * thread used to sleep during a certain period of time
     * @return
     */
    private boolean sleepingThreadAlive() {
        return (_sleepingThread != null && _sleepingThread.isAlive());
    }

    /**
     * start a thread to sleep during a certain period of time
     * @param millisec sleep duration (ms)
     * @throws Exception cannot sleep
     */


    public synchronized void sleepingThread(final long millisec) throws Exception {
        Log.d(TAG, "launch sleeping thread for " + millisec + "ms");
        if (!sleepingThreadAlive()) {
            //Create a new thread only if it does not exist or has been terminated.
            _sleepingThread = new Thread() {
                @Override
                public void run() {
                    Log.d(TAG, "sleeping thread launched");
                    sleepFor(millisec);
                }
            };
            _sleepingThread.start();
        } else {
            Log.d(TAG, "cannot launch a sleeping thread ");
        }
    }

    //

    /**
     * sleep during the amount of millisecs that is provided
     * @param millisec sleep duration
     */
    public void sleepFor(long millisec) { // 5000 = 5 sec
        Log.d(TAG, "delay the scheduling order \n");
        Date now = new Date();// get local time
        SystemClock.sleep(millisec);
        sleepChangedSendEvent();
    }

    /**
     * wake up time
     */
    private void sleepChangedSendEvent() {
        Log.d(TAG, "Event to start scheduling:");
        mainHandler.post(new Runnable() {
            public void run() {
                for (SleepListener listener : listeners) {
                    Log.d(TAG, "report to listener ");
                    listener.someoneReportedAwake();
                }
            }
        });
    }

    /**
     * add a sleep listener
     * @param toadd sleep listener to add
     */
    public void addSleepListener(SleepListener toadd) {
        listeners.add(toadd);
    }

    /**
     * remove a sleep listener
     * @param toremove removed sleep listener
     */
    public void removeSleepListener(SleepListener toremove) {
        listeners.remove(toremove);
    }

    /**
     * that is time to schedule the recording (i.e. to send an order
     * to the other devices
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void someoneReportedAwake() {
        Log.e(TAG, "that is time to schedule the recording -> send an order");

        // send the message that schedule the sound recording
        // the sound should be recorded by the remote device in SCHEDULE_TIME second
        // taking into account the time offset
        // i am the ap so the time offset is nul anyway
        long synchronised_scheduled_time = (calibrationFragment).sendSchedulingInfo(Configuration.SCHEDULE_TIME, 0 );

        // start recording the sound locally at scheduled_time, taking into account the time offset
        // long synchronisedTimeToSchedule = scheduled_time + me.configuration.timeOffset;
        (calibrationFragment).scheduleRecord(this.meet, synchronised_scheduled_time);
        removeSleepListener(this);
    }




////////////////////////////

    /**
     * the consecutive calibration is started
     */
    public void prepareNextCalibration(){
        isprepared4nextCalibration = true;
        //   calibrationFragment.pushMessage("Test: *************************"  );

        //   calibrationFragment.pushMessage("Test: test " + this.testId + " is over"  );
        Log.d(TAG, "Beginning Zip Activity");
        String nameData2zip = "test" + this.testId;

        // zip the file if there is more than one calibration

        if(Configuration.SoundFileAreZipped == true || Configuration.numberofconsecutivecalibrationtocarry > 1)
        {
            meet.end(); //close all the files and clean the related file managers
            FileManager data2zip = new FileManager(nameData2zip, false, this);
            data2zip.zipFileAtPath(data2zip.getFilename(), String.valueOf(Configuration.numberofconsecutivecalibrationtocarry));
            data2zip.close();
            //     calibrationFragment.pushMessage("Test: Zip files"  );
        }
        else{
            //   calibrationFragment.pushMessage("Test: Do not zip files"  );
        }

        // initialise the protocol state
        this.isUnderSchedule = false;
        (calibrationFragment).canCalibrate = false;
        (calibrationFragment).hasAlreadyFinishedRecording = false;

        // go through the protocol state of any partner participating to the meeting
        // init the related protocol state
        for (int i= 0; i< this.meet.meetwithProtocolState.size() ; i++){
            ProtocolState p = this.meet.meetwithProtocolState.get(i);
            p.init();
            this.meet.meetwithProtocolState.set(i, p); // set that we received the
        }
        // we carried one calibration

        this.testId ++;

        // should we start another calibration
        if ( this.testId <= Configuration.numberofconsecutivecalibrationtocarry){        // launch next calibration
            calibrationFragment.pushMessage("Test:Finish calibration number " + this.testId + " over " + Configuration.numberofconsecutivecalibrationtocarry + " calibrations to perform");
            meet.init(this);


            if (this.isGroupOwner == true && isUnderSchedule == false) {
                //schedule the recording  and send this info to the other device)
                //check if we have already send our name and connexion file, if not, send it before scheduling the record time
                isUnderSchedule = true;
                Log.e(TAG, "Schedule another recording, sleep for  " + Configuration.SLEEPDURBEFORESCHEDULERECORD_MS);
                calibrationFragment.pushMessage("Test:Next calibration: start sleeping for"  + Configuration.SLEEPDURBEFORESCHEDULERECORD_MS );

                this.addSleepListener(this);
                try {
                    this.sleepingThread(Configuration.SLEEPDURBEFORESCHEDULERECORD_MS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else{
            calibrationFragment.pushMessage("Test: Calibration is over; #tests performed: " +  Configuration.numberofconsecutivecalibrationtocarry   );
        }
    }
}



