/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage;

/**
 * The {@link ConfigDescriptionParameterValidator} for the pattern attribute of a
 * {@link ConfigDescriptionParameter}.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class PatternValidator implements ConfigDescriptionParameterValidator {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.config.core.validation.internal.ConfigDescriptionParameterValidator#validate(org.eclipse.
     * smarthome.config.core.ConfigDescriptionParameter, java.lang.Object)
     */
    @Override
    public ConfigValidationMessage validate(ConfigDescriptionParameter parameter, Object value) {
        if (value == null || parameter.getType() != Type.TEXT || parameter.getPattern() == null) {
            return null;
        }

        if (!((String) value).matches(parameter.getPattern())) {
            MessageKey messageKey = MessageKey.PATTERN_VIOLATED;
            return new ConfigValidationMessage(parameter.getName(), messageKey.defaultMessage, messageKey.key, value,
                    parameter.getPattern());
        }

        return null;
    }
}
