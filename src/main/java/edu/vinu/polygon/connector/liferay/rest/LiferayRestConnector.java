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
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.common.objects.*;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;

import edu.vinu.polygon.connector.liferay.rest.AbstractRestConnector;

import java.util.Set;

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
    LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration());
    try {
      LOG.ok(">>> test finished: {0}", usersHandler.getCompanyUsersCount(new HttpPost(getURIBuilder().build()), getHttpClient()));
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public Schema schema() {
    SchemaBuilder schemaBuilder = new SchemaBuilder(LiferayRestConnector.class);
    ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();

    ocBuilder.setType("Account");

    AttributeInfoBuilder attr = new AttributeInfoBuilder("screenName", String.class);
    attr.setRequired(true);
    ocBuilder.addAttributeInfo(attr.build());

    attr = new AttributeInfoBuilder("firstName", String.class);
    attr.setRequired(true);
    ocBuilder.addAttributeInfo(attr.build());

    attr = new AttributeInfoBuilder("lastName", String.class);
    attr.setRequired(true);
    ocBuilder.addAttributeInfo(attr.build());

    attr = new AttributeInfoBuilder("emailAddress", String.class);
    attr.setRequired(true);
    ocBuilder.addAttributeInfo(attr.build());

    ocBuilder.addAttributeInfo(AttributeInfoBuilder.build("middleName", String.class));

    schemaBuilder.defineObjectClass(ocBuilder.build());
    attr = null;

    LOG.ok(">>> schema finished");
    return schemaBuilder.build();
  }

  @Override
  public void dispose() {
    LOG.ok(">>> dispose finished");
  }

  @Override
  public void executeQuery(ObjectClass oc, LiferayFilter filter, ResultsHandler handler, OperationOptions oo) {

    LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration());

    if (ObjectClass.ACCOUNT.is(oc.ACCOUNT_NAME)) {
      try{
        processResponseErrors(usersHandler.loadCompanyUsers(handler, new HttpPost(getURIBuilder().build()), getHttpClient()));
      }catch(IOException e){
        LOG.warn(e, "Error when trying to get entity to json: {0}", e.getMessage());
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    LOG.ok(">>> executeQuery finished");
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
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration());
    LOG.ok(">>> create {0} / {1} / {2}", oc, set, oo);
    LOG.ok(">>> create finished");
      return usersHandler.addUser(oc, set, oo, new HttpPost(getURIBuilder().build()), getHttpClient());
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public Uid update(ObjectClass oc, Uid uid, Set<Attribute> set, OperationOptions oo) {
    try {
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration());
    LOG.ok(">>> create {0} / {1} / {2}/ {3}", oc, uid, set, oo);
    LOG.ok(">>> create finished");
      return usersHandler.updateUser(oc, uid, set, oo, new HttpPost(getURIBuilder().build()), getHttpClient());
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  @Override
  public void delete(ObjectClass oc, Uid uid, OperationOptions oo) {
    try {
      LiferayUsersHandler usersHandler = new LiferayUsersHandler(getConfiguration());
    LOG.ok(">>> delete {0} / {1} / {2}", oc, uid, oo);
    LOG.ok(">>> delete finished");
    usersHandler.deleteUser(oc, uid, oo, new HttpPost(getURIBuilder().build()), getHttpClient());
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
