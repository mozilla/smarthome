/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

import org.eclipse.smarthome.io.audio.AudioSource;

/**
 * A {@link KSEvent} fired when the {@link KSService} spots a keyword.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class KeywordSpottingEvent  implements KSEvent {
   /**
    * AudioSource from which the keyword was spotted 
    */
    private final AudioSource audioSource;

   /**
    * Constructs an instance with the passed {@code audioSource}
    *
    * @param audioSource The AudioSource of the spotted keyword 
    */
    public KeywordSpottingEvent(AudioSource audioSource) {
        if (null == audioSource) {
            throw new IllegalArgumentException("The passed audioSource is null");
        }

        this.audioSource = audioSource;
    }

   /**
    * Returns the audioSource of the spotted keyword
    *
    * @return The audioSource of the spotted keyword
    */
    public AudioSource getAudioSource() {
        return this.audioSource;
    }
}
