/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.Voice;
import org.eclipse.smarthome.io.voice.TTSService;
import org.eclipse.smarthome.io.voice.TTSException;

/**
 * This is a TTS service implementation for MacOS, which simply uses
 * the "say" command from MacOS.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Pauli Anttila
 * @author Kelly Davis
 *
 */
public class TTSServiceMacOS implements TTSService {

    private final Logger logger = LoggerFactory.getLogger(TTSServiceMacOS.class);

   /**
    * Set of supported voices
    */
    private final HashSet<Voice> voices = initVoices();

    /**
     * Set of supported audio formats
     */
     private final HashSet<AudioFormat> audioFormats = initAudioFormats();

    /**
     * {@inheritDoc}
     */
     public Set<Voice> getAvailableVoices() {
         return this.voices;
     }

    /**
     * {@inheritDoc}
     */
     public Set<AudioFormat> getSupportedFormats() {
         return this.audioFormats;
     }

    /**
     * {@inheritDoc}
     */
     public AudioSource synthesize(String text,
                                   Voice voice,
                                   AudioFormat requestedFormat) throws TTSException {
         // Validate arguments
         if ((null == text) || text.isEmpty()) {
             throw new TTSException("The passed text is null or empty");
         }
         if (!this.voices.contains(voice)) {
             throw new TTSException("The passed voice is unsupported");
         }
         boolean isAudioFormatSupported = false;
         for (AudioFormat currentAudioFormat : this.audioFormats) {
             if (currentAudioFormat.isCompatible(requestedFormat)) {
                 isAudioFormatSupported = true;
                 break;
             }
         }
         if (!isAudioFormatSupported) {
             throw new TTSException("The passed AudioFormat is unsupported");
         }

         return new  AudioSourceMacOS(text, voice, requestedFormat);
     }

    /**
     * Initializes this.voices
     *
     * @return The voices of this instance
     */
     private final HashSet<Voice> initVoices() {
        HashSet<Voice> voices = new HashSet<Voice>();
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                voices.add(new VoiceMacOS(nextLine));
            }
        } catch (IOException e) {
            logger.error("Error while executing the 'say -v ?' command: " + e.getMessage());
        }
        return voices;
     }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
     private final HashSet<AudioFormat> initAudioFormats() {
         HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
         String command = "say --file-format=?";
         try {
             Process process = Runtime.getRuntime().exec(command);
             InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

             String nextLine;
             while ((nextLine = bufferedReader.readLine()) != null) {
                 audioFormats.addAll(getAudioFormats(nextLine));
             }
         } catch (IOException e) {
             logger.error("Error while executing the '" + command + "' command: " + e.getMessage());
         }
         return audioFormats;
     }

    /**
     * Obtain AudioFormats for say --file-format=? line
     *
     * @param line A say --file-format=? line
     * @return The audio formats of the passed say --file-format=? line
     */
     private final HashSet<AudioFormat> getAudioFormats(String line) {
         StringTokenizer stringTokenizer = new StringTokenizer(line);
         String container = stringTokenizer.nextToken();

         HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
         String command = "say --file-format=" + container + " --data-format=?";
         try {
             Process process = Runtime.getRuntime().exec(command);
             InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

             String nextLine;
             while ((nextLine = bufferedReader.readLine()) != null) {
                 audioFormats.addAll(getAudioFormats(container, nextLine));
             }
         } catch (IOException e) {
             logger.error("Error while executing the '" + command + "' command: " + e.getMessage());
         }
         return audioFormats;
     }

    /**
     * Obtain AudioFormats for say --file-format=<file-format> --data-format=? line
     *
     * @param container The container format
     * @param line A say --file-format=<file-format> --data-format=? line
     * @return The audio formats of the passed say --file-format=<file-format> --data-format=? line
     */
     private final HashSet<AudioFormat> getAudioFormats(String container, String line) {
         StringTokenizer stringTokenizer = new StringTokenizer(line);
         String codec = stringTokenizer.nextToken();

         HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
         // Ignore lpcm codec as it is impossible to query for bit-rate (See say man page)
         if ("lpcm" != codec) {
             String command = "say --file-format=" + container + " --data-format=" + codec + " --bit-rate=?";
             try {
                 Process process = Runtime.getRuntime().exec(command);
                 InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                 String nextLine;
                 while ((nextLine = bufferedReader.readLine()) != null) {
                     audioFormats.addAll(getAudioFormats(container, codec, nextLine));
                 }
             } catch (IOException e) {
                 logger.error("Error while executing the '" + command + "' command: " + e.getMessage());
             }
         }
         return audioFormats;
     }

    /**
     * Obtain AudioFormats for say --file-format=<file-format> --data-format=<data-format> --bit-rate=? line
     *
     * @param container The container format
     * @param codec The codec format
     * @param line A say --file-format=<file-format> --data-format=<data-format> --bit-rate=? line
     * @return The audio formats of the passed say --file-format=<file-format> --data-format=<data-format> --bit-rate=? line
     */
     private final HashSet<AudioFormat> getAudioFormats(String container, String codec, String line) {
         HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();

         Integer bitRate = new Integer(line.trim());
         AudioFormat audioFormat = new AudioFormat(container, codec, null, null, bitRate, null);

         audioFormats.add(audioFormat);

         return audioFormats;
     }
}
