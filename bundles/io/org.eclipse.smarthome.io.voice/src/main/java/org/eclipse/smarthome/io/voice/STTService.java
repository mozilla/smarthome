/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * This is the interface that a speech-to-text service has to implement.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface STTService {
    /**
     * Obtain the Locales available from this STTService
     *
     * @return The Locales available from this service
     */
    public Set<Locale> getSupportedLocales();

    /**
     * Obtain the audio formats supported by this STTService
     *
     * @return The audio formats supported by this service
     */
    public Set<AudioFormat> getSupportedFormats();

   /**
    * This method starts the process of speech recognition.
    *
    * The audio data of the passed {@link AudioSource} is passed to the speech
    * recognition engine. The recognition engine attempts to recognize speech
    * as being spoken in the passed {@code Locale} and containing statements
    * specified in the passed {@code grammars}. Recognition is indicated by
    * fired {@link STTEvent} events targeting the passed {@link STTListener}.
    *
    * The passed {@link AudioSource} must be of a supported {@link AudioFormat}.
    * In other words a {@link AudioFormat} compatable with one returned from
    * the {@code getSupportedFormats()} method.
    *
    * The passed {@code Locale} must be supported. That is to say it must be
    * a {@code Locale} returned from the {@code getSupportedLocales()} method.
    *
    * The passed {@code grammars} must consist of a syntatically valid grammar
    * as specified by the JSpeech Grammar Format. If {@code grammars} is null
    * or empty, large vocabulary continuous speech recognition is attempted.
    *
    * @see <a href="https://www.w3.org/TR/jsgf/">JSpeech Grammar Format.</a>
    * @param sttListener Non-null {@link STTListener} that {@link STTEvent} events target
    * @param audioSource The {@link AudioSource} from which speech is recognized
    * @param locale The {@code Locale} in which the target speech is spoken
    * @param grammars The JSpeech Grammar Format grammar specifying allowed statements
    * @return A {@link STTServiceHandle} used to abort recognition
    * @throws A {@link SSTException} if any paramater is invalid or a STT problem occurs
    */
    public STTServiceHandle recognize(STTListener sttListener, AudioSource audioSource, Locale locale, Set<String> grammars) throws STTException;
}
