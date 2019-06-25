
package edu.vinu.polygon.connector.liferay.rest;

import java.net.URI;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.common.security.GuardedString;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;

import java.io.IOException;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;

public class LiferayUsersHandler {
  private static final Log LOG = Log.getLog(LiferayUsersHandler.class);

  private LiferayRestConfiguration config;
  private CloseableHttpClient client;
  private URI uri;

  private static final String companyUsersCountEntity = "{\"/user/get-company-users-count\":{\"companyId\":%s}}";
  private static final String companyUsersEntity = "{\"/user/get-company-users\":{\"companyId\":%s,\"start\":%s,\"end\":%s}}";
  private static final String userByIdEntity = "{\"/user/get-user-by-id\":{\"userId\":%s}}";
  private static final String updatePasswordEntity = "{\"/user/update-password\":{\"userId\":%s, \"password1\":\"%s\", \"password2\":\"%s\", \"passwordReset\": false}}";

  public LiferayUsersHandler(LiferayRestConfiguration config, URI uri, CloseableHttpClient client) {
    super();
    this.config = config;
    this.uri = uri;
    this.client = client;
  }

  public void dispose() {
    LOG.ok(">>> dispose finished");
  }

  public void test() throws IOException, URISyntaxException {
    LOG.ok(">>> test {0}", getCompanyUsersCount());
  }

  private String getCompanyUsersCount() throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(new StringEntity(String.format(companyUsersCountEntity, config.getCompanyId())));
    CloseableHttpResponse response = client.execute(request);
    LOG.ok(">>> getCompanyUsersCount {0}", response.getEntity());
    return EntityUtils.toString(response.getEntity(), "UTF-8");
  }

  private JSONObject getUserById(Uid uid) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(new StringEntity(String.format(userByIdEntity, uid.getUidValue())));
    CloseableHttpResponse response = client.execute(request);
    LOG.ok(">>> getUserById {0}", response.getEntity());
    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    return new JSONObject(result);
  }

  public Uid updatePassword(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    GuardedString gpass = getAttr(attrs, "__PASSWORD__", GuardedString.class);
    StringAccessor acc = new StringAccessor();

    if(gpass != null) {
      gpass.access(acc);
      request.setEntity(new StringEntity(String.format(updatePasswordEntity, uid.getUidValue(), acc.getValue(), acc.getValue())));
      CloseableHttpResponse response = client.execute(request);
      LOG.ok(">>> updatePassword {0}", response.getEntity());
    }
    return uid;
  }

  public CloseableHttpResponse executeQuery(ObjectClass oc, LiferayFilter filter, ResultsHandler handler, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(new StringEntity(String.format(companyUsersEntity, config.getCompanyId(), 0, getCompanyUsersCount())));
    CloseableHttpResponse response = client.execute(request);
    JSONArray users = new JSONArray(EntityUtils.toString(response.getEntity()));

    LOG.ok(">>> executeQuery users {0}", users);

    for (Object o : users) {
      ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
      JSONObject u = (JSONObject) o;
      builder.setUid(u.getString("userId"));
      builder.setName(u.getString("screenName"));
      builder.addAttribute(AttributeBuilder.build("screenName", u.getString("screenName")));
      builder.addAttribute(AttributeBuilder.build("emailAddress", u.getString("emailAddress")));
      builder.addAttribute(AttributeBuilder.build("firstName", u.getString("firstName")));
      builder.addAttribute(AttributeBuilder.build("middleName", u.getString("middleName")));
      builder.addAttribute(AttributeBuilder.build("lastName", u.getString("lastName")));
      handler.handle(builder.build());
    }

    return response;
  }

  public Uid addUser(ObjectClass oc, Set<Attribute> attrs, OperationOptions oo) throws IOException, UnsupportedEncodingException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(addUserEntity(attrs));
    LOG.ok(">>> addUser Attribute {0}", attrs);

    CloseableHttpResponse response = client.execute(request);

    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    LOG.ok(">>> addUser response {0}", response);
    LOG.ok(">>> addUser result {0}", result);
    JSONObject jors = new JSONObject(result);
    LOG.ok(">>> addUser json {0}", jors);
    String newUid = jors.getString("userId");

    return new Uid(newUid);
  }

  public Uid updateUser(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, UnsupportedEncodingException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(updateUserEntity(attrs, uid, getUserById(uid)));

    CloseableHttpResponse response = client.execute(request);

    String result = EntityUtils.toString(response.getEntity(), "UTF-8");

    LOG.ok(">>> updateUser response {0}", response);
    LOG.ok(">>> updateUser response body {0}", result);
    return uid;
  }

  private HttpEntity addUserEntity(Set<Attribute> attrs) throws UnsupportedEncodingException{
    JSONObject root = new JSONObject();
    JSONObject users = new JSONObject();
    users.put("companyId", config.getCompanyId());
    users.put("autoPassword", true);
    users.put("password1", "");
    users.put("password2", "");
    users.put("autoScreenName", false);
    users.put("screenName", getStringAttr(attrs, "screenName", ""));
    users.put("emailAddress", getStringAttr(attrs, "emailAddress", ""));
    users.put("facebookId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("openId", getStringAttr(attrs, "openId", ""));
    users.put("locale", "");
    users.put("firstName", getStringAttr(attrs, "firstName", ""));
    users.put("middleName", getStringAttr(attrs, "middleName", ""));
    users.put("lastName", getStringAttr(attrs, "lastName", ""));
    users.put("prefixId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("suffixId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("male", true);
    users.put("birthdayMonth", getAttr(attrs, "facebookId", Integer.class, 1));
    users.put("birthdayDay", getAttr(attrs, "facebookId", Integer.class, 1));
    users.put("birthdayYear", getAttr(attrs, "facebookId", Integer.class, 1970));
    users.put("jobTitle", getStringAttr(attrs, "jobTitle", ""));
    users.put("groupIds", JSONObject.NULL);
    users.put("organizationIds", JSONObject.NULL);
    users.put("roleIds", JSONObject.NULL);
    users.put("userGroupIds", JSONObject.NULL);
    users.put("sendEmail", false);
    root.put("/user/add-user", users);
    LOG.ok(">>> addUser JSON {0}", root.toString());
    return new StringEntity(root.toString(), "UTF-8");
  }

  private HttpEntity updateUserEntity(Set<Attribute> attrs, Uid uid, JSONObject user) throws UnsupportedEncodingException{
    JSONObject root = new JSONObject();
    JSONObject users = new JSONObject();

    LOG.ok(">>> buildUserEntity user {0}", user);
    LOG.ok(">>> buildUserEntity UID {0}", uid);
    LOG.ok(">>> buildUserEntity AttributeDelta {0}", attrs);
    users.put("userId", new Integer(uid.getUidValue()));
    users.put("oldPassword", "");
    users.put("newPassword1", "");
    users.put("newPassword2", "");
    users.put("passwordReset", false);
    users.put("reminderQueryQuestion", getStringAttr(attrs, "reminderQueryQuestion", ""));
    users.put("reminderQueryAnswer", getStringAttr(attrs, "reminderQueryAnswer", ""));
    users.put("screenName", getStringAttr(attrs, "screenName", "a206ford"));
    users.put("emailAddress", getStringAttr(attrs, "emailAddress", user.getString("emailAddress")));
    users.put("facebookId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("openId", getStringAttr(attrs, "openId", ""));
    users.put("languageId", getStringAttr(attrs, "", ""));
    users.put("timeZoneId", getStringAttr(attrs, "", ""));
    users.put("greeting", getStringAttr(attrs, "", ""));
    users.put("comments", getStringAttr(attrs, "", ""));
    users.put("firstName", getStringAttr(attrs, "firstName", user.getString("firstName")));
    users.put("middleName", getStringAttr(attrs, "middleName", user.getString("middleName")));
    users.put("lastName", getStringAttr(attrs, "lastName", user.getString("lastName")));
    users.put("prefixId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("suffixId", getAttr(attrs, "facebookId", Integer.class, 0));
    users.put("male", false);
    users.put("birthdayMonth", getAttr(attrs, "facebookId", Integer.class, 1));
    users.put("birthdayDay", getAttr(attrs, "facebookId", Integer.class, 1));
    users.put("birthdayYear", getAttr(attrs, "facebookId", Integer.class, 1970));
    users.put("smsSn", "");
    users.put("facebookSn", "");
    users.put("jabberSn", "");
    users.put("skypeSn", "");
    users.put("twitterSn", "");
    users.put("jobTitle", "");
    users.put("groupIds", JSONObject.NULL);
    users.put("organizationIds", JSONObject.NULL);
    users.put("roleIds", JSONObject.NULL);
    users.put("userGroupRoles", JSONObject.NULL);
    users.put("userGroupIds", JSONObject.NULL);
    root.put("/user/update-user", users);
    LOG.ok(">>> buildUserEntity JSON {0}", root.toString());
    return new StringEntity(root.toString(), "UTF-8");
  }

  public void deleteUser(ObjectClass oc, Uid uid, OperationOptions oo) throws IOException, UnsupportedEncodingException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    LOG.ok(">>> deleteUser UID {0}", uid.getUidValue());
    JSONObject root = new JSONObject();
    JSONObject user = new JSONObject();
    user.put("userId", new Integer(uid.getUidValue()));
    root.put("/user/delete-user", user);

    request.setEntity(new StringEntity(root.toString(), "UTF-8"));

    CloseableHttpResponse response = client.execute(request);

    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    LOG.ok(">>> deleteUser response {0}", response);
    LOG.ok(">>> deleteUser response body {0}", result);
  }

  private String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
      return getAttr(attributes, attrName, String.class);
  }

  private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal) throws InvalidAttributeValueException {
      return getAttr(attributes, attrName, String.class, defaultVal);
  }

  private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2, boolean notNull) throws InvalidAttributeValueException {
      String ret = getAttr(attributes, attrName, String.class, defaultVal);
      if (notNull && ret == null) {
          if (notNull && defaultVal == null)
              return defaultVal2;
          return defaultVal;
      }
      return ret;
  }

  private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull) throws InvalidAttributeValueException {
      String ret = getAttr(attributes, attrName, String.class, defaultVal);
      if (notNull && ret == null)
          return defaultVal;
      return ret;
  }

  private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type) throws InvalidAttributeValueException {
      return getAttr(attributes, attrName, type, null);
  }

  private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull) throws InvalidAttributeValueException {
      T ret = getAttr(attributes, attrName, type, defaultVal);
      if (notNull && ret == null)
          return defaultVal;
      return ret;
  }

  @SuppressWarnings("unchecked")
  private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal) throws InvalidAttributeValueException {
      for (Attribute attr : attributes) {
          if (attrName.equals(attr.getName())) {
              List<Object> vals = attr.getValue();
              if (vals == null || vals.isEmpty()) {
                  // set empty value
                  return null;
              }
              if (vals.size() == 1) {
                  Object val = vals.get(0);
                  if (val == null) {
                      // set empty value
                      return null;
                  }
                  if (type.isAssignableFrom(val.getClass())) {
                      return (T) val;
                  }
                  throw new InvalidAttributeValueException("Unsupported type " + val.getClass() + " for attribute " + attrName + ", value: ");
              }
              throw new InvalidAttributeValueException("More than one value for attribute " + attrName + ", values: " + vals);
          }
      }
      // set default value when attrName not in changed attributes
      return defaultVal;
  }

  protected String[] getMultiValAttr(Set<Attribute> attributes, String attrName, String[] defaultVal) {
      for (Attribute attr : attributes) {
          if (attrName.equals(attr.getName())) {
              List<Object> vals = attr.getValue();
              if (vals == null || vals.isEmpty()) {
                  // set empty value
                  return new String[0];
              }
              String[] ret = new String[vals.size()];
              for (int i = 0; i < vals.size(); i++) {
                  Object valAsObject = vals.get(i);
                  if (valAsObject == null)
                      throw new InvalidAttributeValueException("Value " + null + " must be not null for attribute " + attrName);

                  String val = (String) valAsObject;
                  ret[i] = val;
              }
              return ret;
          }
      }
      // set default value when attrName not in changed attributes
      return defaultVal;
  }


  protected <T> T addAttr(ConnectorObjectBuilder builder, String attrName, T attrVal) {
      if (attrVal != null) {
          builder.addAttribute(attrName, attrVal);
      }
      return attrVal;
  }
}
