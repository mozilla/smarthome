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
import javax.sound.sampled.TargetDataLine;

/**
 * This is an InputStream from a Microphone 
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class MicrophoneInputStream extends InputStream {
   /**
    * TargetDataLine for the mic
    */
    private final TargetDataLine microphone;

   /**
    * Constructs a MicrophoneInputStream with the passed microphone
    *
    * @param microphone The mic which data is pulled from
    */
    public MicrophoneInputStream(TargetDataLine microphone) {
        this.microphone = microphone;
        this.microphone.start();
    }

   /**
    * {@inheritDoc}
    */
    public int read() throws IOException {
        byte[] b = new byte[1];

        int bytesRead = read(b);

        if (-1 == bytesRead) {
            return bytesRead;
        }

        Byte bb = new Byte(b[0]);
        return bb.intValue();     // TODO: Is this the best way to go from byte to int?
    }

   /**
    * {@inheritDoc}
    */
    public int read(byte[] b) throws IOException {
        return this.microphone.read(b, 0, b.length);
    }

   /**
    * {@inheritDoc}
    */
    public int read(byte[] b, int off, int len) throws IOException {
        return this.microphone.read(b, off, len);
    }

   /**
    * {@inheritDoc}
    */
    public void close() throws IOException {
        this.microphone.close();
    }
}
