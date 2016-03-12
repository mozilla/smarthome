/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.InputStream;

import org.eclipse.smarthome.io.audio.AudioException;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * Implementation of the {@link AudioSource} interface for the {@link TTSServiceMaryTTS}
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
class AudioSourceMaryTTS implements AudioSource {
   /**
    * {@link AudioFormat} of this {@link AudioSource}
    */
    private final AudioFormat audioFormat;

   /**
    * {@link InputStream} of this {@link AudioSource}
    */
    private final InputStream inputStream; 

   /**
    * Constructs an instance with the passed properties 
    *
    * @param inputStream The InputStream of this instance
    * @param audioFormat The AudioFormat of this instance
    */
    public AudioSourceMaryTTS(InputStream inputStream, AudioFormat audioFormat) {
        this.inputStream = inputStream;
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
        return this.inputStream;
    }
}
