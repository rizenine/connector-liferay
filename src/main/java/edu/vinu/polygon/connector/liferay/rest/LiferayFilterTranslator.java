/*
 * Copyright (c) 2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.vinu.polygon.connector.liferay.rest;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

/**
 * @author Justin Stanczak
 */
public class LiferayFilterTranslator extends AbstractFilterTranslator<LiferayFilter> {
    private static final Log LOG = Log.getLog(LiferayFilterTranslator.class);

    @Override
    protected LiferayFilter createEqualsExpression(EqualsFilter filter, boolean not) {
        LOG.ok("createEqualsExpression, filter: {0}, not: {1}", filter, not);

        LiferayFilter lf = new LiferayFilter();

        if (not) {
          LOG.ok("createEqualsExpression, filter NOT supported");
          return null;            // not supported
        }

        Attribute attr = filter.getAttribute();

        if (Uid.NAME.equals(attr.getName())) {
          lf.byUid = String.valueOf(attr.getValue().get(0));
        }

        return lf;            // not supported
    }
}
