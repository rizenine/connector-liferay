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

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * @author Justin Stanczak
 *
 */
public class AbstractRestConfiguration extends AbstractConfiguration {

	public enum AuthMethod {
        NONE, BASIC, TOKEN
	}

	private String serviceAddress = null;

	private String username = null;

	private GuardedString password = null;

	private String authMethod = AuthMethod.NONE.name();

	private String tokenName = null;

	private GuardedString tokenValue = null;

	private Boolean trustAllCertificates = false;

	private String proxy = null;

	private Integer proxyPort = 8080;

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public String getTokenName() {
		return tokenName;
	}

	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}

	public GuardedString getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(GuardedString tokenValue) {
		this.tokenValue = tokenValue;
	}

	@ConfigurationProperty(displayMessageKey = "rest.config.trustAllCertificates",
			helpMessageKey = "rest.config.trustAllCertificates.help")
	public Boolean getTrustAllCertificates() {
		return trustAllCertificates;
	}

	public void setTrustAllCertificates(Boolean trustAllCertificates) {
		this.trustAllCertificates = trustAllCertificates;
	}


    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
	public void validate() {
		// TODO Auto-generated method stub

	}

}
