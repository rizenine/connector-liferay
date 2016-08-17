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
package com.evolveum.polygon.connector.example.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import com.evolveum.polygon.rest.AbstractRestConnector;

/**
 * @author semancik
 *
 */
@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = ExampleRestConfiguration.class)
public class ExampleRestConnector extends AbstractRestConnector<ExampleRestConfiguration> implements TestOp, SchemaOp {

	@Override
	public void test() {
		URIBuilder uriBuilder = getURIBuilder();
		URI uri;
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		HttpGet request = new HttpGet(uri);
		
		HttpResponse response = execute(request);
		
		processResponseErrors(response);
	}

	@Override
	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(ExampleRestConnector.class);
		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		schemaBuilder.defineObjectClass(ocBuilder.build());
		return schemaBuilder.build();
	}

}
