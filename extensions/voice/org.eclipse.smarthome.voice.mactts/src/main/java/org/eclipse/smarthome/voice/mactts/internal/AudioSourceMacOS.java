/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.voice.mactts.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.Voice;

/**
 * Implementation of the {@link AudioSource} interface for the {@link TTSServiceMacOS}
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
class AudioSourceMacOS implements AudioSource {
   /**
    * {@link Voice} this {@link AudioSource} speaks in
    */
    private final Voice voice;

   /**
    * Text spoken in this {@link AudioSource}
    */
    private final String text;

   /**
    * {@link AudioFormat} of this {@link AudioSource}
    */
    private final AudioFormat audioFormat;

   /**
    * Constructs an instance with the passed properties.
    *
    * It is assumed that the passed properties have been validated.
    *
    * @param text The text spoken in this {@link AudioSource}
    * @param voice The {@link Voice} used to speak this instance's text
    * @param audioFormat The {@link AudioFormat} of this {@link AudioSource}
    */
    public AudioSourceMacOS(String text, Voice voice, AudioFormat audioFormat) {
        this.text = text;
        this.voice = voice;
        this.audioFormat = audioFormat;
    }

   /**
    * {@inheritDoc}
    */
    public AudioFormat getFormat() {
        return audioFormat;
    }

   /**
    * {@inheritDoc}
    */
    public InputStream getInputStream() throws AudioException {
        FileInputStream fileInputStream;
        String outputFile = generateOutputFilename();
        String command = getCommand(outputFile);

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            fileInputStream = new FileInputStream(outputFile);
        } catch (IOException e) {
            throw new AudioException("Error while executing '" + command + "'", e);
        } catch (InterruptedException e) {
            throw new AudioException("The '" + command + "' has been interrupted", e);
        }

        return fileInputStream;
    }

   /**
    * Generates a unique, absolute output filename
    *
    * @return Unique, absolute output filename
    */
    private String generateOutputFilename() throws AudioException {
        File tempFile;
        try {
            tempFile = File.createTempFile(voice.getLabel(), audioFormat.getContainer());
            tempFile.deleteOnExit();
        } catch(IOException e) {
            throw new AudioException("Unable to create temp file.", e);
        }
        return tempFile.getAbsolutePath();
    }

   /**
    * Gets the command used to generate an audio file {@code outputFile}
    *
    * @param outputFile The absolute filename of the command's output
    * @return The command used to generate the audio file {@code outputFile}
    */
    private String getCommand(String outputFile) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("say");

        stringBuffer.append(" --voice=" + this.voice.getLabel());
        stringBuffer.append(" --output-file=" + outputFile);
        stringBuffer.append(" --file-format=" + this.audioFormat.getContainer());
        stringBuffer.append(" --data-format=" + this.audioFormat.getCodec());
        stringBuffer.append(" --channels=1"); // Mono
        stringBuffer.append(" --bit-rate=" + this.audioFormat.getBitRate());
        stringBuffer.append(" --quality=127"); // Highest quality
        stringBuffer.append(" " + this.text);

        return stringBuffer.toString();
    }
}
