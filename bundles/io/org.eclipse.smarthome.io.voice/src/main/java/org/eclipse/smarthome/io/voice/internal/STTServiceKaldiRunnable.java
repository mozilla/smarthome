/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

import ee.ioc.phon.netspeechapi.duplex.WsDuplexRecognitionSession;

import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.voice.RecognitionStartEvent;
import org.eclipse.smarthome.io.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.io.voice.STTListener;

/**
 * A Runnable that sends AudioSource data in a WsDuplexRecognitionSession 
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class STTServiceKaldiRunnable implements Runnable {
   /**
    * Boolean indicating if the thread is aborting
    */
    private volatile boolean isAborting;

   /**
    * The source of audio data
    */
    private final AudioSource audioSource;
 
   /**
    * The STTListener notified of STTEvents
    */
    private final STTListener sttListener;

    /**
     * The WsDuplexRecognitionSession communication is over
     */
     private final WsDuplexRecognitionSession recognitionSession;

   /**
    * Constructs an instance targeting the passed WsDuplexRecognitionSession
    *
    * @param recognitionSession The WsDuplexRecognitionSession sesion
    * @param sttListener The STTListener targeted for STTEvents
    * @param audioSource The AudioSource data
    */
    public STTServiceKaldiRunnable(WsDuplexRecognitionSession recognitionSession, STTListener sttListener, AudioSource audioSource) {
        this.isAborting = false;
        this.audioSource = audioSource;
        this.sttListener = sttListener;
        this.recognitionSession = recognitionSession;
    }

   /**
    * This method sends AudioSource data in the WsDuplexRecognitionSession
    */
    public void run() {
        try {
            this.recognitionSession.connect();
            InputStream inputStream = this.audioSource.getInputStream();
            AudioFormat audioFormat = this.audioSource.getFormat();
            int bitRate = audioFormat.getBitRate().intValue();
            int byteRate = (bitRate / 8);
            int chunkRate = 4; // 4 <= chunkRate [See: http://bit.ly/1V4Ktw2]
            byte buffer[] = new byte[byteRate / chunkRate];
    
            sttListener.sttEventReceived(new RecognitionStartEvent());
    
            boolean sentLastChunk = false;
            while (!this.isAborting) {
                long millisWithinChunkSecond = System.currentTimeMillis() % (1000 / chunkRate);
                int size = inputStream.read(buffer);
                if (size < 0) {
                    sentLastChunk = true;
                    byte buffer2[] = new byte[0];
                    this.recognitionSession.sendChunk(buffer2, true);
                    break;
                }
                if (size == (byteRate / chunkRate)) {
                    this.recognitionSession.sendChunk(buffer, false);
                } else {
                    sentLastChunk = true;
                    byte buffer2[] = Arrays.copyOf(buffer, size);
                    this.recognitionSession.sendChunk(buffer2, true);
                    break;
                }
                Thread.sleep(1000/chunkRate - millisWithinChunkSecond);
            }
    
            if (this.isAborting && !sentLastChunk) {
                byte buffer2[] = new byte[0];
                this.recognitionSession.sendChunk(buffer2, true);
            }
        } catch(AudioException e) {
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Unable to obtain the audio input stream"));
        } catch(IOException e) {
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Unable to send audio data to the server"));
        } catch(InterruptedException e) {
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Unable to send data to the server at the proper rate"));
        }
    }

   /**
    * This method initiates the process of aborting this thread
    */
    public void abort() {
        this.isAborting = true;
    }
}
