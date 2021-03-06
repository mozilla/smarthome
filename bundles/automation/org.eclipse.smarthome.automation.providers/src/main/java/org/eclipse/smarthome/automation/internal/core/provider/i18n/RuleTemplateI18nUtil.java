/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider.i18n;

import java.util.Locale;

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * This class is used as utility for resolving the localized {@link RuleTemplate}s. It automatically infers the key if
 * the default text is not a constant with the assistance of {@link I18nProvider}.
 * 
 * @author Ana Dimova - Initial Contribution
 *
 */
public class RuleTemplateI18nUtil {

    public static final String RULE_TEMPLATE = "rule-template.";

    public static String getLocalizedRuleTemplateLabel(I18nProvider i18nProvider, Bundle bundle, String ruleTemplateUID,
            String defaultLabel, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel)
                : inferRuleTemplateKey(ruleTemplateUID, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    public static String getLocalizedRuleTemplateDescription(I18nProvider i18nProvider, Bundle bundle,
            String ruleTemplateUID, String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferRuleTemplateKey(ruleTemplateUID, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    private static String inferRuleTemplateKey(String ruleTemplateUID, String lastSegment) {
        return RULE_TEMPLATE + ruleTemplateUID + "." + lastSegment;
    }

}
