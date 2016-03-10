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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Test VoiceMacOS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class VoiceMacOSTest {
    /**
     * Test VoiceMacOS(String) constructor
     */
    @Test
    public void testConstructor() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            VoiceMacOS voiceMacOS = new VoiceMacOS(nextLine);
            Assert.assertNotNull("The VoiceMacOS(String) constructor failed", voiceMacOS);
        } catch (IOException e) {
            Assert.fail("testConstructor() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test VoiceMacOS.getUID()
     */
    @Test
    public void getUIDTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            VoiceMacOS voiceMacOS = new VoiceMacOS(nextLine);
            Assert.assertTrue("The VoiceMacOS UID has an incorrect format", (0 == voiceMacOS.getUID().indexOf("com.apple.voice:")));
        } catch (IOException e) {
            Assert.fail("getUIDTest() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test VoiceMacOS.getLabel()
     */
    @Test
    public void getLabelTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            VoiceMacOS voiceMacOS = new VoiceMacOS(nextLine);
            Assert.assertNotNull("The VoiceMacOS label has an incorrect format", voiceMacOS.getLabel());
        } catch (IOException e) {
            Assert.fail("getLabelTest() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test VoiceMacOS.getLocale()
     */
    @Test
    public void getLocaleTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            VoiceMacOS voiceMacOS = new VoiceMacOS(nextLine);
            Assert.assertNotNull("The VoiceMacOS locale has an incorrect format", voiceMacOS.getLocale());
        } catch (IOException e) {
            Assert.fail("getLocaleTest() failed with IOException: " + e.getMessage());
        }
    }
}
