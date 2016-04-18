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
 * This is the interface that a keyword spotting service has to implement.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface KSService {
    /**
     * Obtain the Locales available from this KSService
     *
     * @return The Locales available from this service
     */
    public Set<Locale> getSupportedLocales();

    /**
     * Obtain the audio formats supported by this KSService
     *
     * @return The audio formats supported by this service
     */
    public Set<AudioFormat> getSupportedFormats();

   /**
    * This method starts the process of keyword spotting
    *
    * The audio data of the passed {@link AudioSource} is passed to the keyword
    * spotting engine. The keyword spotting attempts to spot {@code keyword} as
    * being spoken in the passed {@code Locale}. Spotted keyword is indicated by
    * fired {@link KSEvent} events targeting the passed {@link KSListener}.
    *
    * The passed {@link AudioSource} must be of a supported {@link AudioFormat}.
    * In other words a {@link AudioFormat} compatable with one returned from
    * the {@code getSupportedFormats()} method.
    *
    * The passed {@code Locale} must be supported. That is to say it must be
    * a {@code Locale} returned from the {@code getSupportedLocales()} method.
    *
    * The passed {@code keyword} is the keyword which should be spotted.
    *
    * @param ksListener Non-null {@link KSListener} that {@link KSEvent} events target
    * @param audioSource The {@link AudioSource} from which keywords are spotted
    * @param locale The {@code Locale} in which the target keywords are spoken
    * @param keyword The keyword which to spot
    * @return A {@link KSServiceHandle} used to abort keyword spotting
    * @throws A {@link KSException} if any paramater is invalid or a problem occurs
    */
    public KSServiceHandle spot(KSListener ksListener, AudioSource audioSource, Locale locale, String keyword) throws KSException;
}
