/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.voice.mactts.internal;

import java.util.Set;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import  org.eclipse.smarthome.io.audio.AudioException;
import  org.eclipse.smarthome.io.audio.AudioFormat;
import  org.eclipse.smarthome.io.audio.AudioSource;
import  org.eclipse.smarthome.io.voice.TTSException;
import  org.eclipse.smarthome.io.voice.Voice;

/**
 * Test TTSServiceMacOS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class TTSServiceMacOSTest {
    /**
     * Test TTSServiceMacOS.getAvailableVoices()
     */
    @Test
    public void getAvailableVoicesTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        TTSServiceMacOS ttsServiceMacOS = new TTSServiceMacOS();
        Assert.assertFalse("The getAvailableVoicesTest() failed", ttsServiceMacOS.getAvailableVoices().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.getSupportedFormats()
     */
    @Test
    public void getSupportedFormatsTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        TTSServiceMacOS ttsServiceMacOS = new TTSServiceMacOS();
        Assert.assertFalse("The getSupportedFormatsTest() failed", ttsServiceMacOS.getSupportedFormats().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.synthesize(String,Voice,AudioFormat)
     */
    @Test
    public void synthesizeTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        TTSServiceMacOS ttsServiceMacOS = new TTSServiceMacOS();
        Set<Voice> voices = ttsServiceMacOS.getAvailableVoices();
        Set<AudioFormat> audioFormats = ttsServiceMacOS.getSupportedFormats();
        try {
            AudioSource audioSource = ttsServiceMacOS.synthesize("Hello", voices.iterator().next(), audioFormats.iterator().next());
            Assert.assertNotNull("The test synthesizeTest() created null AudioSource", audioSource);
            Assert.assertNotNull("The test synthesizeTest() created an AudioSource w/o AudioFormat", audioSource.getFormat());
            InputStream inputStream = audioSource.getInputStream();
            Assert.assertNotNull("The test synthesizeTest() created an AudioSource w/o InputStream", inputStream);
            Assert.assertTrue("The test synthesizeTest() returned an AudioSource with no data", (-1 != inputStream.read()));
            inputStream.close();
        } catch (TTSException e) {
            Assert.fail("synthesizeTest() failed with TTSException: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("synthesizeTest() failed with IOException: " + e.getMessage());
        } catch (AudioException e) {
            Assert.fail("synthesizeTest() failed with AudioException: " + e.getMessage());
        }
    }
}
