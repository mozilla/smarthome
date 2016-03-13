/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.RecognitionStopEvent;
import org.eclipse.smarthome.io.voice.SpeechRecognitionEvent;
import org.eclipse.smarthome.io.voice.STTEvent;
import org.eclipse.smarthome.io.voice.STTException;
import org.eclipse.smarthome.io.voice.STTListener;
import org.eclipse.smarthome.io.voice.STTServiceHandle;

/**
 * Test STTServiceKaldi
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class STTServiceKaldiTest {
    /**
     * Test STTServiceKaldi.getSupportedLocales()
     */
    @Test
    public void getSupportedLocalesTest() {
        STTServiceKaldi sttServiceKaldi = new STTServiceKaldi();
        Assert.assertFalse("getSupportedLocales() failed", sttServiceKaldi.getSupportedLocales().isEmpty());
    }

    /**
     * Test STTServiceKaldi.getSupportedFormats()
     */
    @Test
    public void getSupportedFormatsTest() {
        STTServiceKaldi sttServiceKaldi = new STTServiceKaldi();
        Assert.assertFalse("getSupportedFormats() failed", sttServiceKaldi.getSupportedFormats().isEmpty());
    }

   /**
    * Utility STTListener for testing 
    */
    static class STTListenerUtility implements STTListener {
       /**
        * Boolean indicating if session is closed
        */
        private boolean isClosed = false;

       /**
        * Final transcript
        */
        private String transcript;

       /**
        * {@inheritDoc}
        */
        public void sttEventReceived(STTEvent  sttEvent) {
            if (sttEvent instanceof SpeechRecognitionEvent) {
                SpeechRecognitionEvent speechRecognitionEvent = (SpeechRecognitionEvent) sttEvent;
                this.transcript = speechRecognitionEvent.getTranscript();
                return;
            }
            if (sttEvent instanceof RecognitionStopEvent) {
                this.isClosed = true;
            }
        }

       /**
        * Gets if the session is closed
        *
        * @return If the session is closed
        */
        public boolean isClosed() {
            return this.isClosed;
        }

       /**
        * Gets the transcript
        *
        * @return The transcript
        */
        public String getTranscript() {
            return this.transcript;
        }
    }

   /**
    * Utility AudioSource for testing
    */
    static class FileAudioSource implements AudioSource {
       /**
        * The name of the wrapped file
        */
        private final String fileName;

       /**
        * The name of the resource directory 
        */
        private final String resourcePath = "src/test/resources/org/eclipse/smarthome/io/voice/internal";

       /**
        * Constructs an instance wrapping the passed file 
        *
        * @param fileName The name of the wrapped file
        */
        public FileAudioSource(String fileName) {
            this.fileName = fileName;
        }

       /**
        * {@inheritDoc}
        */
        public AudioFormat getFormat() {
            String container = "NONE";
            String codec = "RAW";
            Boolean bigEndian = new Boolean(false); 
            Integer bitDepth = new Integer(16);
            Integer bitRate = new Integer(16000*2*8);
            Long frequency = new Long(16000);

            return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency); 
        }
    
       /**
        * {@inheritDoc}
        */
        public InputStream getInputStream() throws AudioException {
            FileInputStream fileInputStream; 
            try {
                String filePath = this.resourcePath + File.separator + this.fileName;
                fileInputStream = new FileInputStream(filePath);
            } catch(FileNotFoundException e) {
                throw new AudioException("Unable to file audio file", e);
            }

            return fileInputStream;
        }
    }

   /**
    * Test STTServiceKaldi.recognize()
    */
   @Test
   public void recognizeTest() {
       STTServiceKaldi sttServiceKaldi = new STTServiceKaldi();

       STTListenerUtility sttListenerUtility = new STTListenerUtility();
       FileAudioSource fileAudioSource = new FileAudioSource("hello_world.raw");
       Locale locale = new Locale("en", "US");
       HashSet<String> grammars = new HashSet<String>();

       try {
           STTServiceHandle STTServiceHandle = sttServiceKaldi.recognize(sttListenerUtility, fileAudioSource, locale, grammars);
           while (!sttListenerUtility.isClosed()) {
               synchronized (sttListenerUtility) {
                   sttListenerUtility.wait(1000);
               }
           }
           Assert.assertEquals("The decoded text doesn't match the spoken test", "hello world.", sttListenerUtility.getTranscript());
       } catch(STTException e) {
           Assert.fail("STTServiceKaldi.recognize() failed with STTException: " + e.getMessage());
       } catch(InterruptedException e) {
           Assert.fail("STTListenerUtility.wait() failed with InterruptedException: " + e.getMessage());
       }
   }
}
