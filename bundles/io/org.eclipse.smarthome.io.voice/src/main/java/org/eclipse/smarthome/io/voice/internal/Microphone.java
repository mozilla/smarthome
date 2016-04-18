/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * This is a class that creates an AudioSource form the default mic 
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class Microphone {

   /**
    * Obtains an AudioSource for this mic
    *
    * @return An AudioSource for this mic
    * @throws A AudioException is an error occurs
    */
    public AudioSource getAudioSource() throws AudioException {
        TargetDataLine microphone;
        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);

            microphone.open(format);

            return new MicrophoneAudioSource(microphone, convertAudioFormat(format));
        } catch(Exception e) {
            throw new AudioException("Error obtaining the AudioSource", e);
        }
    }

   /**
    * Converts a javax.sound.sampled.AudioFormat to a org.eclipse.smarthome.io.audio.AudioFormat
    *
    * @param audioFormat the AudioFormat to convert
    * @return The converted AudioFormat
    */
    org.eclipse.smarthome.io.audio.AudioFormat convertAudioFormat(AudioFormat audioFormat) {
        String container = "WAVE"; // TODO: Is this right?

        String codec = audioFormat.getEncoding().toString();

        Boolean bigEndian = new Boolean(audioFormat.isBigEndian());

        int frameSize = audioFormat.getFrameSize(); // In bytes
        int bitsPerFrame = frameSize * 8;
        Integer bitDepth = ((AudioSystem.NOT_SPECIFIED == frameSize) ? null : new Integer(bitsPerFrame));

        float frameRate = audioFormat.getFrameRate();
        Integer bitRate = ((AudioSystem.NOT_SPECIFIED == frameRate) ? null : new Integer((int) (frameRate * bitsPerFrame)));

        float sampleRate = audioFormat.getSampleRate();
        Long frequency = ((AudioSystem.NOT_SPECIFIED == sampleRate) ? null : new Long((long) sampleRate));

        return new org.eclipse.smarthome.io.audio.AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
    }
}
