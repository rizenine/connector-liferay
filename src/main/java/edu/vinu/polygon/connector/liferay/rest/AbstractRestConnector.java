/**
* Copyright (c) 2019 Vincennes University
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.vinu.polygon.connector.liferay.rest;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * @author Justin Stanczak
 */
public abstract class AbstractRestConnector<C extends AbstractRestConfiguration> implements Connector {

    private static final Log LOG = Log.getLog(AbstractRestConnector.class);

    private C configuration;
    private CloseableHttpClient httpClient = null;
    private Collection<Header> headers = null;

    public AbstractRestConnector() {
        super();
        LOG.info("Creating {0} connector instance {1}", this.getClass().getSimpleName(), this);
    }

    @Override
    public C getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        LOG.info("Initializing {0} connector instance {1}", this.getClass().getSimpleName(), this);
        this.configuration = (C) configuration;
        this.httpClient = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        headers = new ArrayList();

        headers.add(new BasicHeader("Content-Type", "application/json"));

        URI serviceAddress = URI.create(configuration.getServiceAddress());
        final HttpHost httpHost = new HttpHost(serviceAddress.getHost(),
                serviceAddress.getPort(), serviceAddress.getScheme());

        switch (AbstractRestConfiguration.AuthMethod.valueOf(getConfiguration().getAuthMethod())) {
            case BASIC:
              configuration.getPassword().access(new GuardedString.Accessor() {
                @Override
                public void access(char[] clearChars) {
                  try{
                    Base64.Encoder enc= Base64.getEncoder();
                    String auth_code = configuration.getUsername() + ":" + new String(clearChars);
                    auth_code = new String(enc.encode(auth_code.getBytes("UTF-8")));
                    headers.add(new BasicHeader("Authorization", "Basic " + auth_code));
                  }catch(java.io.UnsupportedEncodingException e){
                      throw new IllegalArgumentException(e.getMessage(), e);
                  }
                }
              });

              LOG.ok(">>> executeQuery Headers {0}", headers);
              httpClientBuilder.setDefaultHeaders(headers);
              break;

            case NONE:
                break;

            case TOKEN:
                break;

            default:

                throw new IllegalArgumentException("Unknown authentication method " + getConfiguration().getAuthMethod());

        }

        if (configuration.getTrustAllCertificates()) {
            try {
                final SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
                            @Override
                            public boolean isTrusted(X509Certificate[] x509CertChain, String authType) throws CertificateException {
                                return true;
                            }
                        })
                        .build();
                httpClientBuilder.setSSLContext(sslContext);
                httpClientBuilder.setConnectionManager(
                        new PoolingHttpClientConnectionManager(
                                RegistryBuilder.<ConnectionSocketFactory>create()
                                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                                        .register("https", new SSLConnectionSocketFactory(sslContext,
                                                NoopHostnameVerifier.INSTANCE))
                                        .build()
                        ));
            } catch (Exception e) {
                throw new ConnectorIOException(e.getMessage(), e);
            }
        }
        if (StringUtil.isNotEmpty(getConfiguration().getProxy())) {
            HttpHost proxy = new HttpHost(getConfiguration().getProxy(), getConfiguration().getProxyPort());
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }

        CloseableHttpClient httpClient = httpClientBuilder.build();

        return httpClient;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Returns URIBuilder that is pre-configured with the service address that
     * is defined in the connector configuration.
     */
    protected URIBuilder getURIBuilder() {
        URI serviceAddress = URI.create(configuration.getServiceAddress());
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(serviceAddress.getScheme());
        uriBuilder.setHost(serviceAddress.getHost());
        uriBuilder.setPort(serviceAddress.getPort());
        uriBuilder.setPath(serviceAddress.getPath());
        return uriBuilder;
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


    @Override
    public void dispose() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOG.error("Error closing HTTP client: {0}", e.getMessage(), e);
            }
        }
    }




    protected String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class);
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class, defaultVal);
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null) {
            if (notNull && defaultVal == null)
                return defaultVal2;
            return defaultVal;
        }
        return ret;
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, type, null);
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull) throws InvalidAttributeValueException {
        T ret = getAttr(attributes, attrName, type, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal) throws InvalidAttributeValueException {
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

    protected JSONArray getJSONArray(Set<Attribute> attributes, String attrName) {
      return new JSONArray(getStringAttr(attributes, attrName));
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

    protected JSONArray getMultiValAttrJSON(Set<Attribute> attributes, String attrName, JSONArray defaultVal) {
        for (Attribute attr : attributes) {
            if (attrName.equals(attr.getName())) {
                List<Object> vals = attr.getValue();
                if (vals == null || vals.isEmpty()) {
                    // set empty value
                    return new JSONArray();
                }
                String[] ret = new String[vals.size()];
                for (int i = 0; i < vals.size(); i++) {
                    Object valAsObject = vals.get(i);
                    if (valAsObject == null)
                        throw new InvalidAttributeValueException("Value " + null + " must be not null for attribute " + attrName);

                    String val = (String) valAsObject;
                    ret[i] = val;
                }
                return new JSONArray(ret);
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

    protected void putJSONAttr(JSONObject obj, String attrName, JSONObject user, Set<Attribute> attributes, Object defaultVal) {
      Object o = getAttr(attributes, attrName, Object.class);
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

    protected void addJSONAddr(ConnectorObjectBuilder builder, JSONObject obj, String attrName) {
      if(!obj.isNull(attrName)) {
        builder.addAttribute(attrName, obj.get(attrName));
      }
    }

    protected boolean allowPartialAttribute(OperationOptions options) {
      if (options == null) {
        return false;
      }
      return options.getAllowPartialAttributeValues() == Boolean.TRUE;
    }
}
