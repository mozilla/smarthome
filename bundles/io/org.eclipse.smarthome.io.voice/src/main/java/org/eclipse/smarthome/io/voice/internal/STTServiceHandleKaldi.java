/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import org.eclipse.smarthome.io.voice.STTServiceHandle;

/**
 * Kaldi implementation of a STTServiceHandle
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class STTServiceHandleKaldi implements STTServiceHandle {
   /**
    * STTServiceKaldiRunnable managed by this instance
    */
    private final STTServiceKaldiRunnable sttServiceKaldiRunnable;

   /**
    * Creates an instance to manage the passed STTServiceKaldiRunnable
    *
    * @param sttServiceKaldiRunnable The managed STTServiceKaldiRunnable
    */
    public STTServiceHandleKaldi(STTServiceKaldiRunnable sttServiceKaldiRunnable) {
        this.sttServiceKaldiRunnable = sttServiceKaldiRunnable;
    }

    /**
     * {@inheritDoc}
     */
    public void abort() {
        this.sttServiceKaldiRunnable.abort();
    }
}
