/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * This is a class that plays an AudioSource
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
class AudioPlayer extends Thread {
    /**
     * The AudioSource to play
     */
    private final AudioSource audioSource;

    /**
     * Constructs an AudioPlayer to play the passed AudioSource
     *
     * @param audioSource The AudioSource to play
     */
    public AudioPlayer(AudioSource audioSource) {
        this.audioSource = audioSource;
    }

    /**
     * This method plays the contained AudioSource
     */
    @Override
    public void run() {
        SourceDataLine line;
        AudioFormat audioFormat = convertAudioFormat(this.audioSource.getFormat());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
        line.start();
        int nRead = 0;
        byte[] abData = new byte[65532]; // needs to be a multiple of 4 and 6, to support both 16 and 24 bit stereo
        while (-1 != nRead) {
            try {
                InputStream inputStream = this.audioSource.getInputStream();
                nRead = inputStream.read(abData, 0, abData.length);
            } catch (AudioException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (nRead >= 0) {
                line.write(abData, 0, nRead);
            }
        }
        line.drain();
        line.close();
    }

    /**
     * Converts a org.eclipse.smarthome.io.audio.AudioFormat
     * to a javax.sound.sampled.AudioFormat
     *
     * @param audioFormat The AudioFormat to convert
     * @return The corresponding AudioFormat
     */
    protected AudioFormat convertAudioFormat(org.eclipse.smarthome.io.audio.AudioFormat audioFormat) {
        AudioFormat.Encoding encoding = new AudioFormat.Encoding(audioFormat.getCodec());
        float sampleRate = audioFormat.getFrequency().floatValue();
        int sampleSizeInBits = audioFormat.getBitDepth().intValue();
        int channels = 1; // TODO: Is thia always true?
        int frameSize = audioFormat.getBitDepth().intValue() / 8; // As channels == 1, thus sampleSizeInBits ==
                                                                  // frameSize
        float frameRate = audioFormat.getFrequency().floatValue();
        boolean bigEndian = audioFormat.isBigEndian().booleanValue();

        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }
}
