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
package com.example.colibris.calib.sound;

/**
 * This noise class provide various methods to deal with noise
 */
public class Noise {
    /**
     * cumpte the noise DB
     * @param pcm noise level
     * @return DB
     */
    public static float pcmToDB(float pcm) {
        return 20 * (float) Math.log10(pcm / 1f);
    }

    /**
     * compute the noise DB
     * @param pcm set of noise level
     * @return set of DB
     */
    public static double[] pcmToDB(double[] pcm) {
        for (int i = 0; i < pcm.length; i++) {
            if (pcm[i] == 0) {
                pcm[i] = 1;
            }
            pcm[i] = 20 * Math.log10(Math.abs(pcm[i]));
            //Log.d(Tag, "dB value: " + String.valueOf(pcm[i]));
        }
        return pcm;
    }



    /**
     * Decibel to PCM
     *
     * @param dB
     * @return sound power
     */
    public static float DBToPcm(float dB) {
        return (float) Math.pow(10, dB / 20);
    }

    /**
     * Distance between two noises
     *
     * @param noise1
     * @param noise2
     * @return distance between the two sounds
     */
    public static float getDistBetweenNoise(float noise1, float noise2) {
        return (float) Math.pow(10, (noise1 - noise2) / 20);
    }
}
