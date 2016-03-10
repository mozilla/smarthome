/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.InputStream;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * This is an AudioSource from a Microphone 
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class MicrophoneAudioSource implements AudioSource {
   /**
    * TargetDataLine for the mic
    */
    private final TargetDataLine microphone;

   /**
    * AudioFormat of the MicrophoneAudioSource 
    */
    private final AudioFormat audioFormat;

   /**
    * Constructs a MicrophoneAudioSource with the passed microphone and AudioFormat
    *
    * @param microphone The mic which data is pulled from
    * @param audioFormat The AudioFormat of this MicrophoneAudioSource
    */
    public MicrophoneAudioSource(TargetDataLine microphone, AudioFormat audioFormat) {
        this.microphone = microphone;
        this.audioFormat = audioFormat;
    }

   /**
    * {@inheritDoc}
    */
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

   /**
    * {@inheritDoc}
    */
    public InputStream getInputStream() throws AudioException {
        return new MicrophoneInputStream(this.microphone);
    }
}
