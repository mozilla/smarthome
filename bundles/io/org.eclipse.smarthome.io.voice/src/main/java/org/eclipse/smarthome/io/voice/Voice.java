/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

import java.util.Locale;

/**
 * This is the interface that a text-to-speech voice has to implement.
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public interface Voice {
    /**
     * Globally unique identifier of the voice, must have the format
     * "prefix:voicename", where "prefix" is unique to the organization
     * creating the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
     public String getUID();

    /**
     * The voice label, usually used for GUI's or VUI's
     *
     * @return The voice label, may not be globally unique
     */
     public String getLabel();

    /**
     * Locale of the voice
     *
     * @return Locale of the voice
     */
     public Locale getLocale();
}
