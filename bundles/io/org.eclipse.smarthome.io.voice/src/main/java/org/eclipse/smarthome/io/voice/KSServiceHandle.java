/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

/**
 * An handle to a {@link KSService}
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface KSServiceHandle {
   /**
    * Aborts keyword spotting in the associated {@link KSService}
    */
    public void abort();
}
