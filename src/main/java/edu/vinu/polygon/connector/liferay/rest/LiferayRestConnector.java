/**
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
import org.identityconnectors.framework.common.exceptions.*;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;

import edu.vinu.polygon.connector.liferay.rest.AbstractRestConnector;

import java.util.Set;
import java.util.EnumSet;
import java.util.ArrayList;

import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * @author Justin Stanczak
 *
 */
@ConnectorClass(displayNameKey = "connector.liferay.rest.display", configurationClass = LiferayRestConfiguration.class)
public class LiferayRestConnector extends AbstractRestConnector<LiferayRestConfiguration> implements TestOp, SchemaOp, SearchOp<LiferayFilter>, CreateOp, UpdateOp, DeleteOp {

  private static final Log LOG = Log.getLog(LiferayRestConnector.class);

	public static final String ACCOUNT_OBJECT_CLASS = "Account";
	public static final String ROLE_OBJECT_CLASS = "Role";
	public static final String USERGROUP_OBJECT_CLASS = "UserGroup";
	public static final String WEBSITE_OBJECT_CLASS = "Website";
	public static final String ORGANIZATION_OBJECT_CLASS = "Organization";

  @Override
  public void test() {
    try {
      LOG.ok(">>> test {0}", getCompanyUsersCount());
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public Schema schema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder(LiferayRestConnector.class);
		schemaBuilder.defineObjectClass(prepareAccountClass().build());
		schemaBuilder.defineObjectClass(prepareRoleClass().build());
		schemaBuilder.defineObjectClass(prepareUserGroupClass().build());
		schemaBuilder.defineObjectClass(prepareWebsiteClass().build());
		schemaBuilder.defineObjectClass(prepareOrganizationClass().build());
    LOG.ok(">>> schema finished");
    return schemaBuilder.build();
  }

	private ObjectClassInfoBuilder prepareAccountClass(){
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();

    ocBuilder.setType(ACCOUNT_OBJECT_CLASS);
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
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("organizationIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("roleIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
    // ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupRoles", String.class, EnumSet.of(Flags.MULTIVALUED)));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupIds", String.class, EnumSet.of(Flags.MULTIVALUED)));
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
    return ocBuilder;
	}

	private ObjectClassInfoBuilder prepareRoleClass(){
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
    ocBuilder.setType(ROLE_OBJECT_CLASS);
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("name", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("type", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("roleId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("classPK", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("description", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("descriptionCurrentValue", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("classNameId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("title", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userName", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("uuid", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("companyId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("subtype", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("modifiedDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("mvccVersion", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("titleCurrentValue", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("createDate", Integer.class));
    return ocBuilder;
	}

	private ObjectClassInfoBuilder prepareUserGroupClass(){
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
    ocBuilder.setType(USERGROUP_OBJECT_CLASS);
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("name", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("description", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userGroupId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("addedByLDAPImport", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("companyId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("createDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("externalReferenceCode", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("modifiedDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("mvccVersion", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("parentUserGroupId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userName", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("uuid", String.class));
    return ocBuilder;
	}

	private ObjectClassInfoBuilder prepareWebsiteClass(){
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
    ocBuilder.setType(WEBSITE_OBJECT_CLASS);
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("className", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("classPK", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("url", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("typeId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("primary", Boolean.class));
    return ocBuilder;
	}

	private ObjectClassInfoBuilder prepareOrganizationClass(){
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
    ocBuilder.setType(ORGANIZATION_OBJECT_CLASS);
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("parentOrganizationId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("name", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("type", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("regionId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("countryId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("statusId", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("comments", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("site", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("companyId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("createDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("externalReferenceCode", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("logoId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("modifiedDate", Integer.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("mvccVersion", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("organizationId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("recursable", Boolean.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("treePath", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userId", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("userName", String.class));
    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("uuid", String.class));
    return ocBuilder;
	}

  @Override
  public void executeQuery(ObjectClass oc, LiferayFilter filter, ResultsHandler handler, OperationOptions oo) {
    LOG.ok(">>> executeQuery ObjectClass {0}", oc);
    LOG.ok(">>> executeQuery filter {0}", filter);
    LOG.ok(">>> executeQuery OperationOptions {0}", oo);

    if (oc.is(ACCOUNT_OBJECT_CLASS)) {
      try{
        HttpPost request = new HttpPost(getURIBuilder().build());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("companyId", getConfiguration().getCompanyId());
        params.put("start", 0);
        params.put("end", getCompanyUsersCount());
        cmd.put("/user/get-company-users", params);
        LOG.ok(">>> executeQuery account JSON {0}", cmd.toString());
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        CloseableHttpResponse response = getHttpClient().execute(request);
        JSONArray users = new JSONArray(EntityUtils.toString(response.getEntity()));

        LOG.ok(">>> executeQuery users {0}", users);

        for (Object o : users) {
          ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
          JSONObject u = (JSONObject) o;
          Uid uid = new Uid(u.getString("userId"));
          builder.setUid(uid);
          builder.setName(u.getString("screenName"));
          addJSONAddr(builder, u, "agreedToTermsOfUse");
          addJSONAddr(builder, u, "comments");
          addJSONAddr(builder, u, "companyId");
          addJSONAddr(builder, u, "contactId");
          addJSONAddr(builder, u, "createDate");
          addJSONAddr(builder, u, "defaultUser");
          addJSONAddr(builder, u, "emailAddress");
          addJSONAddr(builder, u, "emailAddressVerified");
          addJSONAddr(builder, u, "externalReferenceCode");
          addJSONAddr(builder, u, "facebookId");
          addJSONAddr(builder, u, "failedLoginAttempts");
          addJSONAddr(builder, u, "firstName");
          addJSONAddr(builder, u, "googleUserId");
          addJSONAddr(builder, u, "graceLoginCount");
          addJSONAddr(builder, u, "greeting");
          addJSONAddr(builder, u, "jobTitle");
          addJSONAddr(builder, u, "languageId");
          addJSONAddr(builder, u, "lastFailedLoginDate");
          addJSONAddr(builder, u, "lastLoginDate");
          addJSONAddr(builder, u, "lastLoginIP");
          addJSONAddr(builder, u, "lastName");
          addJSONAddr(builder, u, "ldapServerId");
          addJSONAddr(builder, u, "lockout");
          addJSONAddr(builder, u, "lockoutDate");
          addJSONAddr(builder, u, "loginDate");
          addJSONAddr(builder, u, "loginIP");
          addJSONAddr(builder, u, "middleName");
          addJSONAddr(builder, u, "modifiedDate");
          addJSONAddr(builder, u, "mvccVersion");
          addJSONAddr(builder, u, "openId");
          addJSONAddr(builder, u, "portraitId");
          addJSONAddr(builder, u, "reminderQueryQuestion");
          addJSONAddr(builder, u, "reminderQueryAnswer");
          addJSONAddr(builder, u, "screenName");
          addJSONAddr(builder, u, "status");
          addJSONAddr(builder, u, "timeZoneId");
          addJSONAddr(builder, u, "userId");
          addJSONAddr(builder, u, "uuid");
          builder.addAttribute("roleIds", getUserRoleIds(uid));
          builder.addAttribute("userGroupIds", getUserGroupIds(uid));
          handler.handle(builder.build());
        }
        processResponseErrors(response);
        LOG.ok(">>> executeQuery finished");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(ROLE_OBJECT_CLASS)) {
      try{
        HttpPost request = new HttpPost(getURIBuilder().build());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("companyId", getConfiguration().getCompanyId());
        params.put("types", new JSONArray());
        cmd.put("/role/get-roles", params);
        LOG.ok(">>> executeQuery roles JSON {0}", cmd.toString());
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        CloseableHttpResponse response = getHttpClient().execute(request);
        JSONArray roles = new JSONArray(EntityUtils.toString(response.getEntity()));

        LOG.ok(">>> executeQuery roles {0}", roles);

        for (Object o : roles) {
          ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
          JSONObject u = (JSONObject) o;
          builder.setUid(u.getString("roleId"));
          builder.setName(u.getString("name"));
          addJSONAddr(builder, u, "name");
          addJSONAddr(builder, u, "roleId");
          addJSONAddr(builder, u, "type");
          addJSONAddr(builder, u, "classPK");
          addJSONAddr(builder, u, "description");
          addJSONAddr(builder, u, "descriptionCurrentValue");
          addJSONAddr(builder, u, "classNameId");
          addJSONAddr(builder, u, "title");
          addJSONAddr(builder, u, "userName");
          addJSONAddr(builder, u, "userId");
          addJSONAddr(builder, u, "uuid");
          addJSONAddr(builder, u, "companyId");
          addJSONAddr(builder, u, "subtype");
          addJSONAddr(builder, u, "modifiedDate");
          addJSONAddr(builder, u, "mvccVersion");
          addJSONAddr(builder, u, "titleCurrentValue");
          addJSONAddr(builder, u, "createDate");
          handler.handle(builder.build());
        }
        processResponseErrors(response);
        LOG.ok(">>> executeQuery finished");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(USERGROUP_OBJECT_CLASS)) {
      try{
        HttpPost request = new HttpPost(getURIBuilder().build());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("companyId", getConfiguration().getCompanyId());
        cmd.put("/usergroup/get-user-groups", params);
        LOG.ok(">>> executeQuery usergroup JSON {0}", cmd.toString());
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        CloseableHttpResponse response = getHttpClient().execute(request);
        LOG.ok(">>> executeQuery usergroup response.getEntity {0}", response.getEntity());
        JSONArray usergroups = new JSONArray(EntityUtils.toString(response.getEntity()));

        LOG.ok(">>> executeQuery usergroups {0}", usergroups);

        for (Object o : usergroups) {
          ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
          JSONObject u = (JSONObject) o;
          builder.setUid(u.getString("userGroupId"));
          builder.setName(u.getString("name"));
          addJSONAddr(builder, u, "name");
          addJSONAddr(builder, u, "userGroupId");
          addJSONAddr(builder, u, "addedByLDAPImport");
          addJSONAddr(builder, u, "companyId");
          addJSONAddr(builder, u, "createDate");
          addJSONAddr(builder, u, "description");
          addJSONAddr(builder, u, "externalReferenceCode");
          addJSONAddr(builder, u, "modifiedDate");
          addJSONAddr(builder, u, "mvccVersion");
          addJSONAddr(builder, u, "parentUserGroupId");
          addJSONAddr(builder, u, "userId");
          addJSONAddr(builder, u, "userName");
          addJSONAddr(builder, u, "uuid");
          handler.handle(builder.build());
        }
        processResponseErrors(response);
        LOG.ok(">>> executeQuery finished");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(WEBSITE_OBJECT_CLASS)) {
      try{
        HttpPost request = new HttpPost(getURIBuilder().build());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("className", "com.liferay.portal.kernel.model.Website");
        params.put("classPK", 0);
        cmd.put("/website/get-websites", params);
        LOG.ok(">>> executeQuery website JSON {0}", cmd.toString());
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        CloseableHttpResponse response = getHttpClient().execute(request);
        LOG.ok(">>> executeQuery website response.getEntity {0}", response.getEntity());
        JSONArray websites = new JSONArray(EntityUtils.toString(response.getEntity()));

        LOG.ok(">>> executeQuery websites {0}", websites);

        for (Object o : websites) {
          ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
          JSONObject u = (JSONObject) o;
          builder.setUid(u.getString("websiteId"));
          builder.setName(u.getString("websiteId"));
          addJSONAddr(builder, u, "websiteId");
          handler.handle(builder.build());
        }
        processResponseErrors(response);
        LOG.ok(">>> executeQuery finished");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(ORGANIZATION_OBJECT_CLASS)) {
      try{
        HttpPost request = new HttpPost(getURIBuilder().build());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("companyId", getConfiguration().getCompanyId());
        params.put("parentOrganizationId", getConfiguration().getParentOrganizationId());
        cmd.put("/organization/get-organizations", params);
        LOG.ok(">>> executeQuery organization JSON {0}", cmd.toString());
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        CloseableHttpResponse response = getHttpClient().execute(request);
        LOG.ok(">>> executeQuery organization response.getEntity {0}", response.getEntity());
        JSONArray organizations = new JSONArray(EntityUtils.toString(response.getEntity()));

        LOG.ok(">>> executeQuery organizations {0}", organizations);

        for (Object o : organizations) {
          ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
          JSONObject u = (JSONObject) o;
          builder.setUid(u.getString("organizationId"));
          builder.setName(u.getString("name"));
          addJSONAddr(builder, u, "comments");
          addJSONAddr(builder, u, "companyId");
          addJSONAddr(builder, u, "countryId");
          addJSONAddr(builder, u, "createDate");
          addJSONAddr(builder, u, "externalReferenceCode");
          addJSONAddr(builder, u, "logoId");
          addJSONAddr(builder, u, "modifiedDate");
          addJSONAddr(builder, u, "mvccVersion");
          addJSONAddr(builder, u, "name");
          addJSONAddr(builder, u, "organizationId");
          addJSONAddr(builder, u, "parentOrganizationId");
          addJSONAddr(builder, u, "recursable");
          addJSONAddr(builder, u, "regionId");
          addJSONAddr(builder, u, "statusId");
          addJSONAddr(builder, u, "treePath");
          addJSONAddr(builder, u, "type");
          addJSONAddr(builder, u, "userId");
          addJSONAddr(builder, u, "userName");
          addJSONAddr(builder, u, "uuid");
          handler.handle(builder.build());
        }
        processResponseErrors(response);
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
  public Uid create(ObjectClass oc, Set<Attribute> attrs, OperationOptions oo) {
    LOG.ok(">>> create ObjectClass {0}", oc);
    LOG.ok(">>> create Attribute {0}", attrs);
    LOG.ok(">>> create OperationOptions {0}", oo);

    if (oc.is(ACCOUNT_OBJECT_CLASS)) {
      return createOrUpdateAccount(oc, null, attrs, oo);
    }

    if (oc.is(ROLE_OBJECT_CLASS)) {
      return createOrUpdateRole(oc, null, attrs, oo);
    }

    if (oc.is(USERGROUP_OBJECT_CLASS)) {
      return createOrUpdateUserGroup(oc, null, attrs, oo);
    }

    if (oc.is(WEBSITE_OBJECT_CLASS)) {
      return createOrUpdateWebsite(oc, null, attrs, oo);
    }

    if (oc.is(ORGANIZATION_OBJECT_CLASS)) {
      return createOrUpdateOrganization(oc, null, attrs, oo);
    }
    return null;
  }

  @Override
  public Uid update(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    LOG.ok(">>> update ObjectClass {0}", oc);
    LOG.ok(">>> update Uid {0}", uid);
    LOG.ok(">>> update Attribute {0}", attrs);
    LOG.ok(">>> update OperationOptions {0}", oo);

    if (oc.is(ACCOUNT_OBJECT_CLASS)) {
      return createOrUpdateAccount(oc, uid, attrs, oo);
    }

    if (oc.is(ROLE_OBJECT_CLASS)) {
      return createOrUpdateRole(oc, uid, attrs, oo);
    }

    if (oc.is(USERGROUP_OBJECT_CLASS)) {
      return createOrUpdateUserGroup(oc, uid, attrs, oo);
    }

    if (oc.is(WEBSITE_OBJECT_CLASS)) {
      return createOrUpdateWebsite(oc, null, attrs, oo);
    }

    if (oc.is(ORGANIZATION_OBJECT_CLASS)) {
      return createOrUpdateOrganization(oc, null, attrs, oo);
    }
    return null;
  }

  public Uid createOrUpdateAccount(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONArray cmds = new JSONArray();
      if(uid == null) {
        JSONObject user = addUserJSON(attrs);
        LOG.ok(">>> create JSON {0}",user.toString());
        request.setEntity(new StringEntity(user.toString(), "UTF-8"));

        CloseableHttpResponse response = getHttpClient().execute(request);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jors = new JSONObject(result);
        LOG.ok(">>> create json {0}", jors);
        uid = new Uid(jors.getString("userId"));
        processResponseErrors(response);
        request = new HttpPost(getURIBuilder().build());
      } else {
        updateUserJSON(cmds, attrs, uid);
        LOG.ok(">>> update JSON {0}", cmds.toString());
      }

      updatePasswordJSON(cmds, attrs, uid);
      updateStatusJSON(cmds, attrs, uid);
      updatePortraitJSON(cmds, attrs, uid);
      request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
      LOG.ok(">>> update extra JSON {0}", cmds.toString());
      processResponseErrors(getHttpClient().execute(request));
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return uid;
  }

  public Uid createOrUpdateRole(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONArray cmds = new JSONArray();
      if(uid == null) {
        JSONObject role = addRoleJSON(attrs);
        LOG.ok(">>> create JSON {0}",role.toString());
        request.setEntity(new StringEntity(role.toString(), "UTF-8"));

        CloseableHttpResponse response = getHttpClient().execute(request);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jors = new JSONObject(result);
        LOG.ok(">>> create json result {0}", jors);
        uid = new Uid(jors.getString("roleId"));
        processResponseErrors(response);
      } else {
        updateRoleJSON(cmds, attrs, uid);
        LOG.ok(">>> update JSON {0}", cmds.toString());
        request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return uid;
  }

  public Uid createOrUpdateUserGroup(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONArray cmds = new JSONArray();
      if(uid == null) {
        JSONObject usergroup = addUserGroupJSON(attrs);
        LOG.ok(">>> create JSON {0}",usergroup.toString());
        request.setEntity(new StringEntity(usergroup.toString(), "UTF-8"));

        CloseableHttpResponse response = getHttpClient().execute(request);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jors = new JSONObject(result);
        LOG.ok(">>> create json result {0}", jors);
        uid = new Uid(jors.getString("userGroupId"));
        processResponseErrors(response);
      } else {
        updateUserGroupJSON(cmds, attrs, uid);
        LOG.ok(">>> update JSON {0}", cmds.toString());
        request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return uid;
  }

  public Uid createOrUpdateWebsite(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONArray cmds = new JSONArray();
      if(uid == null) {
        JSONObject website = addWebsiteJSON(attrs);
        LOG.ok(">>> create JSON {0}",website.toString());
        request.setEntity(new StringEntity(website.toString(), "UTF-8"));

        CloseableHttpResponse response = getHttpClient().execute(request);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jors = new JSONObject(result);
        LOG.ok(">>> create json result {0}", jors);
        uid = new Uid(jors.getString("userGroupId"));
        processResponseErrors(response);
      } else {
        updateWebsiteJSON(cmds, attrs, uid);
        LOG.ok(">>> update JSON {0}", cmds.toString());
        request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return uid;
  }

  public Uid createOrUpdateOrganization(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONArray cmds = new JSONArray();
      if(uid == null) {
        JSONObject organization = addOrganizationJSON(attrs);
        LOG.ok(">>> create JSON {0}",organization.toString());
        request.setEntity(new StringEntity(organization.toString(), "UTF-8"));

        CloseableHttpResponse response = getHttpClient().execute(request);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jors = new JSONObject(result);
        LOG.ok(">>> create json result {0}", jors);
        uid = new Uid(jors.getString("organizationId"));
        processResponseErrors(response);
      } else {
        updateOrganizationJSON(cmds, attrs, uid);
        LOG.ok(">>> update JSON {0}", cmds.toString());
        request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return uid;
  }

  @Override
  public void delete(ObjectClass oc, Uid uid, OperationOptions oo) {
    LOG.ok(">>> update ObjectClass {0}", oc);
    LOG.ok(">>> update Uid {0}", uid);
    LOG.ok(">>> update OperationOptions {0}", oo);
    if (oc.is(ACCOUNT_OBJECT_CLASS)) {
      try {
        HttpPost request = new HttpPost(getURIBuilder().build());
        LOG.ok(">>> deleteUser UID {0}", uid.getUidValue());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("userId", new Integer(uid.getUidValue()));
        cmd.put("/user/delete-user", params);
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(ROLE_OBJECT_CLASS)) {
      try {
        HttpPost request = new HttpPost(getURIBuilder().build());
        LOG.ok(">>> deleteRole UID {0}", uid.getUidValue());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("roleId", new Integer(uid.getUidValue()));
        cmd.put("/role/delete-role", params);
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    if (oc.is(USERGROUP_OBJECT_CLASS)) {
      try {
        HttpPost request = new HttpPost(getURIBuilder().build());
        LOG.ok(">>> deleteUserGroup UID {0}", uid.getUidValue());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("userGroupId", new Integer(uid.getUidValue()));
        cmd.put("/usergroup/delete-user-group", params);
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
      LOG.ok(">>> delete finished");
    }

    if (oc.is(WEBSITE_OBJECT_CLASS)) {
      try {
        HttpPost request = new HttpPost(getURIBuilder().build());
        LOG.ok(">>> deleteWebsite UID {0}", uid.getUidValue());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("websiteId", new Integer(uid.getUidValue()));
        cmd.put("/website/delete-website", params);
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
      LOG.ok(">>> delete finished");
    }

    if (oc.is(ORGANIZATION_OBJECT_CLASS)) {
      try {
        HttpPost request = new HttpPost(getURIBuilder().build());
        LOG.ok(">>> deleteOrganization UID {0}", uid.getUidValue());
        JSONObject cmd = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("organizationId", new Integer(uid.getUidValue()));
        cmd.put("/organization/delete-organization", params);
        request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
        processResponseErrors(getHttpClient().execute(request));
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
      LOG.ok(">>> delete finished");
    }
  }

  private JSONObject addUserJSON(Set<Attribute> attrs) {
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("companyId", getConfiguration().getCompanyId());
    params.put("autoPassword", true); // Don't set password here
    params.put("password1", ""); // Don't set password here
    params.put("password2", ""); // Don't set password here
    params.put("autoScreenName", getAttr(attrs, "autoScreenName", Boolean.class, false));
    params.put("screenName", getStringAttr(attrs, "screenName", ""));
    params.put("emailAddress", getStringAttr(attrs, "emailAddress", ""));
    params.put("facebookId", getAttr(attrs, "facebookId", Integer.class, 0));
    params.put("openId", getStringAttr(attrs, "openId", ""));
    params.put("locale", getStringAttr(attrs, "locale", ""));
    params.put("firstName", getStringAttr(attrs, "firstName", ""));
    params.put("middleName", getStringAttr(attrs, "middleName", ""));
    params.put("lastName", getStringAttr(attrs, "lastName", ""));
    params.put("prefixId", getAttr(attrs, "prefixId", Integer.class, 0));
    params.put("suffixId", getAttr(attrs, "suffixId", Integer.class, 0));
    params.put("male", getAttr(attrs, "male", Boolean.class, false));
    params.put("birthdayMonth", getAttr(attrs, "birthdayMonth", Integer.class, 1));
    params.put("birthdayDay", getAttr(attrs, "birthdayDay", Integer.class, 1));
    params.put("birthdayYear", getAttr(attrs, "birthdayYear", Integer.class, 1970));
    params.put("jobTitle", getStringAttr(attrs, "jobTitle", ""));
    params.put("groupIds", JSONObject.NULL);
    params.put("organizationIds", JSONObject.NULL);
    params.put("roleIds",  getMultiValAttrJSON(attrs, "roleIds", null));
    params.put("userGroupIds",  getMultiValAttrJSON(attrs, "userGroupIds", null));
    params.put("sendEmail", getAttr(attrs, "sendEmail", Boolean.class, false));
    cmd.put("/user/add-user", params);
    LOG.ok(">>> addUser JSON {0}", cmd.toString());
    return cmd;
  }

  private void updateUserJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    JSONObject user = getUserById(uid);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("userId", new Integer(uid.getUidValue()));
    params.put("oldPassword", ""); // Don't set password here
    params.put("newPassword1", ""); // Don't set password here
    params.put("newPassword2", ""); // Don't set password here
    params.put("passwordReset", false); // Don't set password here
    putJSONAttr(params, "reminderQueryQuestion", user, attrs, "");
    putJSONAttr(params, "reminderQueryAnswer", user, attrs, "");
    putJSONAttr(params, "screenName", user, attrs, "");
    putJSONAttr(params, "emailAddress", user, attrs, "");
    putJSONAttr(params, "facebookId", user, attrs, 0);
    putJSONAttr(params, "openId", user, attrs, "");
    putJSONAttr(params, "languageId", user, attrs, "");
    putJSONAttr(params, "timeZoneId", user, attrs, "");
    putJSONAttr(params, "greeting", user, attrs, "");
    putJSONAttr(params, "comments", user, attrs, "");
    putJSONAttr(params, "firstName", user, attrs, "");
    putJSONAttr(params, "middleName", user, attrs, "");
    putJSONAttr(params, "lastName", user, attrs, "");
    putJSONAttr(params, "prefixId", user, attrs, 0);
    putJSONAttr(params, "suffixId", user, attrs, 0);
    putJSONAttr(params, "male", user, attrs, false);
    putJSONAttr(params, "birthdayMonth", user, attrs, 1);
    putJSONAttr(params, "birthdayDay", user, attrs, 1);
    putJSONAttr(params, "birthdayYear", user, attrs, 1970);
    putJSONAttr(params, "smsSn", user, attrs, "");
    putJSONAttr(params, "facebookSn", user, attrs, "");
    putJSONAttr(params, "jabberSn", user, attrs, "");
    putJSONAttr(params, "skypeSn", user, attrs, "");
    putJSONAttr(params, "twitterSn", user, attrs, "");
    putJSONAttr(params, "jobTitle", user, attrs, "");
    params.put("groupIds", JSONObject.NULL);
    params.put("organizationIds", JSONObject.NULL);
    params.put("roleIds",  getMultiValAttrJSON(attrs, "roleIds", user.getJSONArray("roleIds")));
    params.put("userGroupRoles", JSONObject.NULL);
    params.put("userGroupIds",  getMultiValAttrJSON(attrs, "userGroupIds", user.getJSONArray("userGroupIds")));
    cmd.put("/user/update-user", params);
    LOG.ok(">>> updateUserEntity JSON {0}", cmd.toString());
    cmds.put(cmd);
  }

  private JSONObject addRoleJSON(Set<Attribute> attrs) {
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("className", getStringAttr(attrs, "className", "com.liferay.portal.kernel.model.Role"));
    params.put("classPK", getAttr(attrs, "classPK", Integer.class, 0));
    params.put("name", getStringAttr(attrs, "name", ""));
    params.put("titleMap", JSONObject.NULL);
    params.put("descriptionMap", JSONObject.NULL);
    params.put("type", getAttr(attrs, "type", Integer.class, 0));
    params.put("subtype", getStringAttr(attrs, "subtype", ""));
    cmd.put("/role/add-role", params);
    LOG.ok(">>> addRole JSON {0}", cmd.toString());
    return cmd;
  }

  private void updateRoleJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    JSONObject role = getRoleById(uid);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("roleId", new Integer(uid.getUidValue()));
    putJSONAttr(params, "name", role, attrs, "");
    params.put("titleMap", JSONObject.NULL);
    params.put("descriptionMap", JSONObject.NULL);
    putJSONAttr(params, "subtype", role, attrs, 0);
    cmd.put("/role/update-role", params);
    LOG.ok(">>> updateUserEntity JSON {0}", cmd.toString());
    cmds.put(cmd);
  }

  private JSONObject addUserGroupJSON(Set<Attribute> attrs) {
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("name", getStringAttr(attrs, "name", ""));
    params.put("description", getStringAttr(attrs, "description", ""));
    cmd.put("/usergroup/add-user-group", params);
    LOG.ok(">>> addUserGroupJSON JSON {0}", cmd.toString());
    return cmd;
  }

  private void updateUserGroupJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    JSONObject usergroup = getUserGroupById(uid);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("userGroupId", new Integer(uid.getUidValue()));
    putJSONAttr(params, "name", usergroup, attrs, "");
    putJSONAttr(params, "description", usergroup, attrs, "");
    cmd.put("/usergroup/update-user-group", params);
    LOG.ok(">>> updateUserGroupJSON JSON {0}", cmd.toString());
    cmds.put(cmd);
  }

  private JSONObject addWebsiteJSON(Set<Attribute> attrs) {
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("className", getStringAttr(attrs, "className", "com.liferay.portal.kernel.model.Website"));
    params.put("classPK", getAttr(attrs, "classPK", Integer.class, 0));
    params.put("url", getStringAttr(attrs, "url", ""));
    params.put("typeId", getAttr(attrs, "typeId", Integer.class, 0));
    params.put("primary", getAttr(attrs, "primary", Boolean.class, false));
    cmd.put("/website/add-website", params);
    LOG.ok(">>> addWebsiteJSON JSON {0}", cmd.toString());
    return cmd;
  }

  private void updateWebsiteJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    JSONObject website = getWebsiteById(uid);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("websiteId", new Integer(uid.getUidValue()));
    putJSONAttr(params, "url", website, attrs, "");
    putJSONAttr(params, "typeId", website, attrs, 0);
    putJSONAttr(params, "primary", website, attrs, false);
    cmd.put("/website/update-website", params);
    LOG.ok(">>> updateWebsiteJSON JSON {0}", cmd.toString());
    cmds.put(cmd);
  }

  private JSONObject addOrganizationJSON(Set<Attribute> attrs) {
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("parentOrganizationId", getAttr(attrs, "parentOrganizationId", Integer.class, 0));
    params.put("name", getAttr(attrs, "name", String.class, ""));
    params.put("type", getAttr(attrs, "type", String.class, ""));
    params.put("regionId", getAttr(attrs, "regionId", Integer.class, 0));
    params.put("countryId", getAttr(attrs, "countryId", Integer.class, 0));
    params.put("statusId", getAttr(attrs, "statusId", Integer.class, 0));
    params.put("comments", getAttr(attrs, "comments", String.class, ""));
    params.put("site", getAttr(attrs, "site", Boolean.class, false));
    cmd.put("/organization/add-organization", params);
    LOG.ok(">>> addOrganizationJSON JSON {0}", cmd.toString());
    return cmd;
  }

  private void updateOrganizationJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    JSONObject organization = getOrganizationById(uid);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("organizationId", new Integer(uid.getUidValue()));
    putJSONAttr(params, "parentOrganizationId", organization, attrs, 0);
    putJSONAttr(params, "name", organization, attrs, "");
    putJSONAttr(params, "type", organization, attrs, "");
    putJSONAttr(params, "regionId", organization, attrs, 0);
    putJSONAttr(params, "countryId", organization, attrs, 0);
    putJSONAttr(params, "statusId", organization, attrs, 0);
    putJSONAttr(params, "comments", organization, attrs, "");
    putJSONAttr(params, "site", organization, attrs, false);
    cmd.put("/organization/update-organization", params);
    LOG.ok(">>> updateOrganizationJSON JSON {0}", cmd.toString());
    cmds.put(cmd);
  }

  private JSONObject getUserById(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      cmd.put("/user/get-user-by-id", params);
      LOG.ok(">>> getUserById JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getUserById {0}", response.getEntity());
      String result = EntityUtils.toString(response.getEntity(), "UTF-8");
      processResponseErrors(response);
      JSONObject user = new JSONObject(result);
      user.put("roleIds", getUserRoleIds(uid));
      user.put("userGroupIds", getUserGroupIds(uid));
      return user;
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private JSONObject getRoleById(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("roleId", uid.getUidValue());
      cmd.put("/role/get-role", params);
      LOG.ok(">>> getRoleById JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getRoleById {0}", response.getEntity());
      String result = EntityUtils.toString(response.getEntity(), "UTF-8");
      processResponseErrors(response);
      return new JSONObject(result);
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private JSONObject getUserGroupById(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userGroupId", uid.getUidValue());
      cmd.put("/usergroup/get-user-group", params);
      LOG.ok(">>> getUserGroupById JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getUserGroupById {0}", response.getEntity());
      String result = EntityUtils.toString(response.getEntity(), "UTF-8");
      processResponseErrors(response);
      return new JSONObject(result);
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private JSONObject getWebsiteById(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("websiteId", uid.getUidValue());
      cmd.put("/website/get-website", params);
      LOG.ok(">>> getWebsiteById JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getWebsiteById {0}", response.getEntity());
      String result = EntityUtils.toString(response.getEntity(), "UTF-8");
      processResponseErrors(response);
      return new JSONObject(result);
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private JSONObject getOrganizationById(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("organizationId", uid.getUidValue());
      cmd.put("/organization/get-organization", params);
      LOG.ok(">>> getOrganizationById JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getOrganizationById {0}", response.getEntity());
      String result = EntityUtils.toString(response.getEntity(), "UTF-8");
      processResponseErrors(response);
      return new JSONObject(result);
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private ArrayList<String> getUserRoleIds(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      cmd.put("/role/get-user-roles", params);
      LOG.ok(">>> getUserRoles JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      JSONArray result = new JSONArray(EntityUtils.toString(response.getEntity(), "UTF-8"));
      processResponseErrors(response);

      ArrayList<String> roleIds = new ArrayList<String>();
      for (Object o : result) {
        JSONObject role = (JSONObject) o;
        roleIds.add(role.getString("roleId"));
      }
      LOG.ok(">>> getUserRoles roleIds {0}", roleIds);
      return roleIds;
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private ArrayList<String> getUserGroupIds(Uid uid) {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      cmd.put("/usergroup/get-user-user-groups", params);
      LOG.ok(">>> getUserGroupIds JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      JSONArray result = new JSONArray(EntityUtils.toString(response.getEntity(), "UTF-8"));
      processResponseErrors(response);

      ArrayList<String> usergroupIds = new ArrayList<String>();
      for (Object o : result) {
        JSONObject usergroup = (JSONObject) o;
        usergroupIds.add(usergroup.getString("userGroupId"));
      }
      LOG.ok(">>> getUserGroupIds usergroupIds {0}", usergroupIds);
      return usergroupIds;
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private String getCompanyUsersCount() throws IOException, URISyntaxException {
    try {
      HttpPost request = new HttpPost(getURIBuilder().build());
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("companyId", getConfiguration().getCompanyId());
      cmd.put("/user/get-company-users-count", params);
      LOG.ok(">>> getCompanyUsersCount JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = getHttpClient().execute(request);
      LOG.ok(">>> getCompanyUsersCount {0}", response.getEntity());
      processResponseErrors(response);
      return EntityUtils.toString(response.getEntity(), "UTF-8");
    } catch (Exception e) {
      throw new ConnectorIOException(e.getMessage(), e);
    }
  }

  private void updatePasswordJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    GuardedString gpass = getAttr(attrs, "__PASSWORD__", GuardedString.class);
    StringAccessor acc = new StringAccessor();
    if(gpass != null) {
      gpass.access(acc);
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("password1", acc.getValue());
      params.put("password2", acc.getValue());
      params.put("passwordReset", false);
      cmd.put("/user/update-password", params);
      cmds.put(cmd);
    }
  }

  private void updateStatusJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    Integer status = getAttr(attrs, "status", Integer.class);
    if(status != null) {
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("status", status);
      cmd.put("/user/update-status", params);
      cmds.put(cmd);
    }
  }

  private void updatePortraitJSON(JSONArray cmds, Set<Attribute> attrs, Uid uid) {
    byte[] portrait = getAttr(attrs, "portraitBytes", byte[].class);
    if(portrait != null) {
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("bytes", portrait);
      cmd.put("/user/update-portrait", params);
      cmds.put(cmd);
    }
  }

}
