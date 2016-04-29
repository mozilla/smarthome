/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.KSEvent;
import org.eclipse.smarthome.io.voice.KSException;
import org.eclipse.smarthome.io.voice.KSListener;
import org.eclipse.smarthome.io.voice.KeywordSpottingErrorEvent;
import org.eclipse.smarthome.io.voice.KeywordSpottingEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test KSServicePocketsphinx
 *
 * @author Andre Natal
 */
public class KSServicePocketsphinxTest {

    /**
     * Test KSServicePocketsphinx.getSupportedLocales()
     */
    @Test
    public void getSupportedLocalesTest() {
        KSServicePocketsphinx ksServicePs = new KSServicePocketsphinx();
        Assert.assertFalse("getSupportedLocales() failed", ksServicePs.getSupportedLocales().isEmpty());
    }

    /**
     * Test KSServicePocketsphinx.getSupportedFormats()
     */
    @Test
    public void getSupportedFormatsTest() {
        KSServicePocketsphinx ksServicePs = new KSServicePocketsphinx();
        Assert.assertFalse("getSupportedFormats() failed", ksServicePs.getSupportedFormats().isEmpty());
    }

    /**
     * Utility KSListener for testing
     */
    class KSListenerUtility implements KSListener {
        String keyword;
        boolean isSpot;
        KeywordSpottingErrorEvent e;

        public KSListenerUtility(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public void ksEventReceived(KSEvent ksEvent) {
            if (ksEvent instanceof KeywordSpottingEvent) {
                isSpot = true;
            }
            if (ksEvent instanceof KeywordSpottingErrorEvent) {
                e = (KeywordSpottingErrorEvent) ksEvent;
            }

            synchronized (this) {
                this.notify();
            }
        }
    }

    /**
     * Utility AudioSource for testing
     */
    class FileAudioSource implements AudioSource {
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
        @Override
        public AudioFormat getFormat() {
            String container = "NONE";
            String codec = "RAW";
            Boolean bigEndian = new Boolean(false);
            Integer bitDepth = new Integer(16);
            Integer bitRate = new Integer(16000 * 2 * 8);
            Long frequency = new Long(16000);

            return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream() throws AudioException {
            FileInputStream fileInputStream;
            try {
                String filePath = this.resourcePath + File.separator + this.fileName;
                fileInputStream = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new AudioException("Unable to file audio file", e);
            }

            return fileInputStream;
        }
    }

    /**
     * Test ksServicePocketsphinx.recognize()
     */
    @Test
    public void recognizeTest() {
        String keyword = "marieta";
        KSServicePocketsphinx ksServicePocketsphinx = new KSServicePocketsphinx();
        KSListenerUtility ksListener = new KSListenerUtility(keyword);
        FileAudioSource fsrc = new FileAudioSource("marieta.raw");
        try {
            ksServicePocketsphinx.spot(ksListener, fsrc, null, keyword);

            synchronized (ksListener) {
                ksListener.wait(3000);
            }

            if (!ksListener.isSpot) {
                Assert.fail("KSServicePocketsphinx.spot() failed to spot the word:" + keyword);
            }
        } catch (KSException e) {
            // TODO Auto-generated catch block
            Assert.fail("KSServicePocketsphinx.spot() failed with message:" + e.getMessage());
        } catch (InterruptedException e) {
            Assert.fail("KSListenerUtility.wait() failed with InterruptedException: " + e.getMessage());
        }
    }
}
