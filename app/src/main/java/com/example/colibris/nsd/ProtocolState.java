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

/**
 * The ProtocolState class details the states of the protocol that is used to exchange sound and calibrate.
 * States include e.g. if one of the sound used to calibrate is received received,
 * if one of the received sound is corrupted, if the md5 of the sound that
 * is received has already been extracted, if all the sounds that are necessary
 * to calibrate have been receved.
 * In addition to the above, various parameters (e.g. connection hypergraph that is
 * received, the sound and the md5 of the received sound are stored
 */

public class ProtocolState {
    /**
     * protocole state: the device is actually receiving sound
     */
    public boolean isReceivingSound = false; // we are currently received the sound
    /**
     * protocole state: the device is actually receiving the connexion graph
     */

    public boolean isReceivingConnexion = false; // we are currently receiving the connexion graph
    /**
     * protocole state: the device has already extracted the md5 of the sound
     */
    public boolean ismd5extractedFromSound = false; // we already extracted the md5 from the sound file
    /**
     * protocole state: the device received a sound that is corrupted
     */
    public boolean isSoundCorrupted = false;
    /**
     * protocole state: the device has received all the sound
     */
    public boolean receivingSoundCompleted = false ;
    /**
     * protocole state: the device already received the connexion graph
     */
    public String receivedConnexionGraph = null;// is used to record the received connexion graph
    /**
     * sound that is actually received
     */
    public String receivedSound = null; // is used and record the beginning of the sound that is received
    /**
     * md5 of the sound file that has been received
     */
    public String receivedSoundMd5String = null; //
    /**
     * init the protocol state
     */

    public void init(){
        isReceivingSound = false;
        isReceivingConnexion = false;
        isSoundCorrupted = false;
        receivingSoundCompleted = false ;
        ismd5extractedFromSound = false;

        receivedConnexionGraph = null;
        receivedSound = null;
        receivedSoundMd5String = null;

    }
}
