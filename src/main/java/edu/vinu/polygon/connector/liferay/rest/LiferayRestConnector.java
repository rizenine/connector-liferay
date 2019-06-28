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

import java.net.URI;

import java.net.URISyntaxException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import java.nio.charset.Charset;

import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.ConnectorClass;

import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;

import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.common.objects.*;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;

import edu.vinu.polygon.connector.liferay.rest.AbstractRestConnector;

import java.util.Set;
import java.util.EnumSet;

import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * @author Justin Stanczak
 *
 */
@ConnectorClass(displayNameKey = "connector.liferay.rest.display", configurationClass = LiferayRestConfiguration.class)
public class LiferayRestConnector extends AbstractRestConnector<LiferayRestConfiguration> implements TestOp, SchemaOp, SearchOp<LiferayFilter>, CreateOp, UpdateOp, DeleteOp {

  private static final Log LOG = Log.getLog(LiferayRestConnector.class);

  @Override
  public void test() {
    try {
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration(), getURIBuilder().build(), getHttpClient());
      usersHandler.test();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public Schema schema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder(LiferayRestConnector.class);
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();

    ocBuilder.setType("Account");
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("autoPassword", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("autoScreenName", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("screenName", String.class, EnumSet.of(Flags.REQUIRED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("emailAddress", String.class, EnumSet.of(Flags.REQUIRED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("facebookId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("openId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("locale", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("firstName", String.class, EnumSet.of(Flags.REQUIRED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("middleName", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lastName", String.class, EnumSet.of(Flags.REQUIRED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("prefixId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("suffixId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("male", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("birthdayMonth", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("birthdayDay", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("birthdayYear", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("jobTitle", String.class));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("groupIds", String.class, EnumSet.of(Flags.MULTIVALUED, Flags.REQUIRED, Flags.NOT_READABLE, Flags.NOT_RETURNED_BY_DEFAULT)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("organizationIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("roleIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupRoles", String.class, EnumSet.of(Flags.MULTIVALUED)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("sendEmail", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("reminderQueryQuestion", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("reminderQueryAnswer", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("languageId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("timeZoneId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("greeting", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("comments", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("smsSn", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("facebookSn", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("jabberSn", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("skypeSn", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("twitterSn", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("agreedToTermsOfUse", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("companyId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("contactId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("createDate", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("defaultUser", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("emailAddressVerified", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("externalReferenceCode", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("failedLoginAttempts", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("googleUserId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("graceLoginCount", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lastFailedLoginDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lastLoginDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lastLoginIP", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("ldapServerId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lockout", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("lockoutDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("loginDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("loginIP", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("modifiedDate", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("mvccVersion", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("portraitId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("portraitBytes", byte[].class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("portrait", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("status", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("uuid", String.class));
    schemaBuilder.defineObjectClass(ocBuilder.build());

    LOG.ok(">>> schema finished");
    return schemaBuilder.build();
  }

  @Override
  public void executeQuery(ObjectClass oc, LiferayFilter filter, ResultsHandler handler, OperationOptions oo) {
    if (ObjectClass.ACCOUNT.is(oc.ACCOUNT_NAME)) {
      try{
        LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration(), getURIBuilder().build(), getHttpClient());
        usersHandler.executeQuery(oc, filter, handler, oo);
        LOG.ok(">>> executeQuery finished");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
  }

  @Override
  public FilterTranslator<LiferayFilter> createFilterTranslator(ObjectClass oc, OperationOptions oo) {
      LOG.ok(">>> createFilterTranslator {0} {1}", oc, oo);
      LOG.ok(">>> createFilterTranslator finished");
  		return new LiferayFilterTranslator();
  }

  @Override
  public Uid create(ObjectClass oc, Set<Attribute> set, OperationOptions oo) {
    try {
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration(), getURIBuilder().build(), getHttpClient());
      LOG.ok(">>> create {0} / {1} / {2}", oc, set, oo);
      Uid uid = usersHandler.addUser(oc, set, oo);
      // usersHandler.updatePassword(oc, uid, set, oo);
      // usersHandler.updatePortrait(oc, uid, set, oo);
      usersHandler.updateAll(oc, uid, set, oo);
      // usersHandler.updateStatus(oc, uid, set, oo);
      LOG.ok(">>> create finished");
      return uid;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public Uid update(ObjectClass oc, Uid uid, Set<Attribute> set, OperationOptions oo) {
    try {
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration(), getURIBuilder().build(), getHttpClient());
      LOG.ok(">>> create {0} / {1} / {2}/ {3}", oc, uid, set, oo);
      usersHandler.updateAll(oc, uid, set, oo);
      // usersHandler.updatePassword(oc, uid, set, oo);
      // usersHandler.updateStatus(oc, uid, set, oo);
      // usersHandler.updatePortrait(oc, uid, set, oo);
      usersHandler.updateUser(oc, uid, set, oo);
      LOG.ok(">>> create finished");
      return uid;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public void delete(ObjectClass oc, Uid uid, OperationOptions oo) {
    try {
        LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration(), getURIBuilder().build(), getHttpClient());
      LOG.ok(">>> delete {0} / {1} / {2}", oc, uid, oo);
      usersHandler.deleteUser(oc, uid, oo);
      LOG.ok(">>> delete finished");
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
