/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.util.Locale;

import org.eclipse.smarthome.io.voice.Voice;

/**
 * Implementation of the Voice interface for MaryTTS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class VoiceMaryTTS implements Voice {
    /**
     * Voice locale
     */
     private final Locale locale;

    /**
     * Voice label
     */
     private final String label;


    /**
     * Constructs a MaryTTS Voice for the passed data
     *
     * @param locale The Locale of the voice
     * @param label The label of the voice 
     */
     public VoiceMaryTTS(Locale locale, String label) {
         this.locale = locale;
         this.label = label;
     }

    /**
     * Globally unique identifier of the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
     public String getUID() {
         return "de.mary.dfki.voice:" + this.label;
     }

    /**
     * The voice label, used for GUI's or VUI's
     *
     * @return The voice label, may not be globally unique
     */
     public String getLabel() {
         return this.label;
     }

    /**
     * @inheritDoc
     */
     public Locale getLocale() {
        return this.locale;
     }
}
