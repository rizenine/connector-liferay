
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
import javax.xml.bind.DatatypeConverter;
import java.util.Base64;
import java.util.Base64.Encoder;

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
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("companyId", config.getCompanyId());
    cmd.put("/user/get-company-users-count", params);
    LOG.ok(">>> getCompanyUsersCount JSON {0}", cmd.toString());
    request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
    CloseableHttpResponse response = client.execute(request);
    LOG.ok(">>> getCompanyUsersCount {0}", response.getEntity());
    processResponseErrors(response);
    return EntityUtils.toString(response.getEntity(), "UTF-8");
  }

  private JSONObject getUserById(Uid uid) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("userId", uid.getUidValue());
    cmd.put("/user/get-user-by-id", params);
    LOG.ok(">>> getUserById JSON {0}", cmd.toString());
    request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
    CloseableHttpResponse response = client.execute(request);
    LOG.ok(">>> getUserById {0}", response.getEntity());
    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    processResponseErrors(response);
    return new JSONObject(result);
  }





  public Uid updateAll(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, URISyntaxException {

  HttpEntityEnclosingRequestBase request = new HttpPost(uri);
      JSONArray cmds = new JSONArray();




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

      Integer status = getAttr(attrs, "status", Integer.class);
    if(status != null) {
        JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("status", status);
      cmd.put("/user/update-status", params);
        cmds.put(cmd);
    }



    byte[] portrait = getAttr(attrs, "portraitBytes", byte[].class);

    if(portrait != null) {
        JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("bytes", portrait);
      cmd.put("/user/update-portrait", params);
        cmds.put(cmd);
    }




      LOG.ok(">>> updateAll JSON {0}",cmds.toString());
      request.setEntity(new StringEntity(cmds.toString(), "UTF-8"));
      CloseableHttpResponse response = client.execute(request);
      processResponseErrors(response);


      return uid;


  }

  public Uid updatePassword(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
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
      LOG.ok(">>> updatePassword JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = client.execute(request);
      LOG.ok(">>> updatePassword {0}", response.getEntity());
      processResponseErrors(response);
    }
    return uid;
  }

  public Uid updateStatus(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    Integer status = getAttr(attrs, "status", Integer.class);

    if(status != null) {
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("status", status);
      cmd.put("/user/update-status", params);
      LOG.ok(">>> updateStatus JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = client.execute(request);
      LOG.ok(">>> updateStatus {0}", response.getEntity());
      processResponseErrors(response);
    }
    return uid;
  }

  public Uid updatePortrait(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    byte[] portrait = getAttr(attrs, "portraitBytes", byte[].class);

    if(portrait != null) {
      JSONObject cmd = new JSONObject();
      JSONObject params = new JSONObject();
      params.put("userId", uid.getUidValue());
      params.put("bytes", portrait);
      cmd.put("/user/update-portrait", params);
      LOG.ok(">>> updatePortrait JSON {0}", cmd.toString());
      request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
      CloseableHttpResponse response = client.execute(request);
      LOG.ok(">>> updatePortrait {0}", response.getEntity());
      processResponseErrors(response);
    }
    return uid;
  }

  public void executeQuery(ObjectClass oc, LiferayFilter filter, ResultsHandler handler, OperationOptions oo) throws IOException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("companyId", config.getCompanyId());
    params.put("start", 0);
    params.put("end", getCompanyUsersCount());
    cmd.put("/user/get-company-users", params);
    LOG.ok(">>> executeQuery JSON {0}", cmd.toString());
    request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
    CloseableHttpResponse response = client.execute(request);
    JSONArray users = new JSONArray(EntityUtils.toString(response.getEntity()));

    LOG.ok(">>> executeQuery users {0}", users);

    for (Object o : users) {
      ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
      JSONObject u = (JSONObject) o;
      builder.setUid(u.getString("userId"));
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
      handler.handle(builder.build());
    }
    processResponseErrors(response);
  }

  public Uid addUser(ObjectClass oc, Set<Attribute> attrs, OperationOptions oo) throws IOException, UnsupportedEncodingException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(addUserEntity(attrs));
    CloseableHttpResponse response = client.execute(request);
    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    LOG.ok(">>> addUser response {0}", response);
    LOG.ok(">>> addUser result {0}", result);
    JSONObject jors = new JSONObject(result);
    LOG.ok(">>> addUser json {0}", jors);
    String newUid = jors.getString("userId");
    processResponseErrors(response);
    return new Uid(newUid);
  }

  public Uid updateUser(ObjectClass oc, Uid uid, Set<Attribute> attrs, OperationOptions oo) throws IOException, UnsupportedEncodingException, URISyntaxException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    request.setEntity(updateUserEntity(attrs, uid, getUserById(uid)));
    CloseableHttpResponse response = client.execute(request);
    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    LOG.ok(">>> updateUser response {0}", response);
    LOG.ok(">>> updateUser response body {0}", result);
    processResponseErrors(response);
    return uid;
  }

  private HttpEntity addUserEntity(Set<Attribute> attrs) throws UnsupportedEncodingException{
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("companyId", config.getCompanyId());
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
    params.put("roleIds", JSONObject.NULL);
    params.put("userGroupIds", JSONObject.NULL);
    params.put("sendEmail", getAttr(attrs, "sendEmail", Boolean.class, false));
    cmd.put("/user/add-user", params);
    LOG.ok(">>> addUser JSON {0}", cmd.toString());
    return new StringEntity(cmd.toString(), "UTF-8");
  }

  private HttpEntity updateUserEntity(Set<Attribute> attrs, Uid uid, JSONObject user) throws UnsupportedEncodingException{
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();

    LOG.ok(">>> updateUserEntity user {0}", user);
    LOG.ok(">>> updateUserEntity UID {0}", uid);
    LOG.ok(">>> updateUserEntity AttributeDelta {0}", attrs);
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
    params.put("roleIds", JSONObject.NULL);
    params.put("userGroupRoles", JSONObject.NULL);
    params.put("userGroupIds", JSONObject.NULL);
    cmd.put("/user/update-user", params);
    LOG.ok(">>> updateUserEntity JSON {0}", cmd.toString());
    return new StringEntity(cmd.toString(), "UTF-8");
  }

  public void deleteUser(ObjectClass oc, Uid uid, OperationOptions oo) throws IOException, UnsupportedEncodingException {
    HttpEntityEnclosingRequestBase request = new HttpPost(uri);
    LOG.ok(">>> deleteUser UID {0}", uid.getUidValue());
    JSONObject cmd = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("userId", new Integer(uid.getUidValue()));
    cmd.put("/user/delete-user", params);
    request.setEntity(new StringEntity(cmd.toString(), "UTF-8"));
    CloseableHttpResponse response = client.execute(request);
    String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    processResponseErrors(response);
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

  protected Object getJSONAddr(JSONObject obj, String attrName, Object defaultVal) {
    if(obj.isNull(attrName)) {
      return defaultVal;
    } else {
      return obj.get(attrName);
    }
  }

  protected void addJSONAddr(ConnectorObjectBuilder builder, JSONObject obj, String attrName) {
    if(!obj.isNull(attrName)) {
      builder.addAttribute(attrName, obj.get(attrName));
    }
  }

  protected void putJSONAttr(JSONObject obj, String attrName, JSONObject user, Set<Attribute> attr, Object defaultVal) {
    Object o = getAttr(attr, attrName, Object.class);
    if(o == null) {
      if(user.isNull(attrName)){
        obj.put(attrName, defaultVal);
      } else {
        obj.put(attrName, user.get(attrName));
      }
    } else {
      obj.put(attrName, o);
    }
  }

  /**
   * Checks HTTP response for errors. If the response is an error then the method
   * throws the ConnId exception that is the most appropriate match for the error.
   */
  public void processResponseErrors(CloseableHttpResponse response) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode <= 299) {
          return;
      }
      String responseBody = null;
      try {
          responseBody = EntityUtils.toString(response.getEntity());
      } catch (IOException e) {
          LOG.warn("cannot read response body: " + e, e);
      }

      String message = "HTTP error " + statusCode + " " + response.getStatusLine().getReasonPhrase() + " : " + responseBody;
      LOG.error("{0}", message);
      if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
          closeResponse(response);
          throw new ConnectorIOException(message);
      }
      if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
          closeResponse(response);
          throw new PermissionDeniedException(message);
      }
      if (statusCode == 404 || statusCode == 410) {
          closeResponse(response);
          throw new UnknownUidException(message);
      }
      if (statusCode == 408) {
          closeResponse(response);
          throw new OperationTimeoutException(message);
      }
      if (statusCode == 412) {
          closeResponse(response);
          throw new PreconditionFailedException(message);
      }
      if (statusCode == 418) {
          closeResponse(response);
          throw new UnsupportedOperationException("Sorry, no cofee: " + message);
      }
      // TODO: other codes
      closeResponse(response);
      throw new ConnectorException(message);
  }

  protected void closeResponse(CloseableHttpResponse response) {
      // to avoid pool waiting
      try {
          response.close();
      } catch (IOException e) {
          LOG.warn(e, "Error when trying to close response: " + response);
      }
  }
}
