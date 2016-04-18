/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

import java.util.Set;
import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * This is the interface that a text-to-speech service has to implement.
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public interface TTSService {
    /**
     * Obtain the voices available from this TTSService
     *
     * @return The voices available from this service
     */
     public Set<Voice> getAvailableVoices();

    /**
     * Obtain the audio formats supported by this TTSService
     *
     * @return The audio formats supported by this service
     */
     public Set<AudioFormat> getSupportedFormats();

    /**
     * Returns an {@link AudioSource} containing the TTS results. Note, one
     * can only request a supported {@code Voice} and {@link AudioSource} or
     * an exception is thrown.
     *
     * @param text The text to convert to speech
     * @param voice The voice to use for speech
     * @param requestedFormat The audio format to return the results in
     * @return AudioSource containing the TTS results
     * @throws TTSException If {@code voice} and/or {@code requestedFormat}
     *         are not supported or annother error occurs while creating an
     *         {@link AudioSource}
     */
     public AudioSource synthesize(String text,
                                   Voice voice,
                                   AudioFormat requestedFormat) throws TTSException;
}
