/*
 * Copyright (c) 2019 Vincennes University
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

import java.util.List;

import org.identityconnectors.common.logging.Log;

import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Uid;

/**
* @author Justin Stanczak
*/

public class LiferayFilterTranslator extends AbstractFilterTranslator<String> {
  private static final Log LOG = Log.getLog(LiferayFilterTranslator.class);

  @Override
  protected String createEqualsExpression(EqualsFilter filter, boolean not) {
    LOG.ok(">>> createEqualsExpression {0} ({1})", filter, not);
    List<Object> value = filter.getAttribute().getValue();
    if (!not && isUidAttribute(filter.getAttribute())) {
      return (String) value.get(0);
    } else {
      return super.createEqualsExpression(filter, not);
    }
  }

  @Override
  protected String createContainsExpression(ContainsFilter filter, boolean not) {
    LOG.ok(">>> createContainsExpression {0} ({1})", filter, not);
    if (!not && isUidAttribute(filter.getAttribute())) {
      return (String) filter.getValue();
    } else {
      return super.createContainsExpression(filter, not);
    }
  }

  private boolean isUidAttribute(Attribute attribute) {
    return attribute.is(Uid.NAME);
  }
}
