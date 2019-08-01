/**
 * Copyright (c) 2016 Evolveum
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

import edu.vinu.polygon.connector.liferay.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * @author Justin Stanczak
 *
 */
public class LiferayRestConfiguration extends AbstractRestConfiguration {

  private String companyId;
  private String parentOrganizationId;
  private Integer defaultPageSize = 10;
  
  @ConfigurationProperty(displayMessageKey = "rest.config.companyId", helpMessageKey = "rest.config.companyId.help")
  public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

  @ConfigurationProperty(displayMessageKey = "rest.config.parentOrganizationId", helpMessageKey = "rest.config.parentOrganizationId.help")
  public String getParentOrganizationId() {
        return parentOrganizationId;
    }

    public void setParentOrganizationId(String parentOrganizationId) {
        this.parentOrganizationId = parentOrganizationId;
    }



  @ConfigurationProperty(order = 10, displayMessageKey = "defaultPageSize.display",
  groupMessageKey = "basic.group", helpMessageKey = "defaultPageSize.help", required = true, confidential = false)
  public Integer getDefaultPageSize() {
  return defaultPageSize;
  }

  public void setDefaultPageSize(Integer defaultPageSize) {
  this.defaultPageSize = defaultPageSize;
  }


}
