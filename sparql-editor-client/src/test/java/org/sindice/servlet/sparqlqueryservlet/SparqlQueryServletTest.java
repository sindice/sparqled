package org.sindice.servlet.sparqlqueryservlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mortbay.jetty.testing.ServletTester;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
@RunWith(value = Parameterized.class)
public class SparqlQueryServletTest {
	@Parameters
	public static Collection<Object[]> data() {
		// Object[][] data = new Object[][] { { BackendType.NATIVE, "./native"
		// },
		// { BackendType.MEMORY, "./memory" } };
		Object[][] data = new Object[][] {
		        { BackendType.NATIVE, "/tmp/test-unit-native/", "", "" },
		        { BackendType.MEMORY, "/tmp/test-unit-memory/", "", "" }
		// , { BackendType.HTTP, "test2", "", "" }
		};

		return Arrays.asList(data);
	}

	private static final String dgsInput = "./src/test/resources/QueryBackend/test.nt";

	private static ServletTester aseTester;
	private static String aseBaseUrl;
	private static HttpClient client = null;

	public SparqlQueryServletTest(BackendType backend, String backendArgs,
	        String backendArgsHTTP, String backendArgsHTTP2) throws Exception {
		// if (this.backend.equals("HTTP")) {
		// this.backendArgs = new String[] { backendArgs, backendArgsHTTP,
		// backendArgsHTTP2 };
		// } else {
		// this.backendArgs = new String[] { backendArgs };
		// }

		client = new HttpClient();

		aseTester = new ServletTester();
		aseTester.setContextPath("/");
		String url = aseTester.createSocketConnector(true);
		final String queryRepoServletUrl = url + "/" + backendArgs;
		aseTester.setAttribute(SparqlQueryServletListener.SQS_WRAPPER
		        + SparqlQueryServletListener.BACKEND, backend.toString());
		aseTester.setAttribute(SparqlQueryServletListener.SQS_WRAPPER
		        + SparqlQueryServletListener.BACKEND_ARGS,
		        new String[] { backendArgs });
		aseTester.addServlet(SparqlQueryServlet.class, "/SparqlEditorServlet");

		aseBaseUrl = url + "/SparqlEditorServlet";
		aseTester.start();
	}

	@After
	public void tearDownAfter() throws Exception {
		if (aseTester != null) {
			aseTester.stop();
		}
	}

	@AfterClass
	public static void clean() {
		File path = new File("/tmp/test-unit-memory/");
		deleteDirectory(path);
		path = new File("/tmp/test-unit-native/");
		deleteDirectory(path);
	}

	private static Boolean deleteDirectory(File path) {
		Boolean resultat = true;
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					resultat &= deleteDirectory(files[i]);
				} else {
					resultat &= files[i].delete();
				}
			}
		}
		resultat &= path.delete();
		return (resultat);
	}

	@BeforeClass
	public static void initRepository() throws ServletException {
		Logger logger = LoggerFactory.getLogger(SparqlQueryServletTest.class);
		logger.debug("CREATE MEMORY");
		final SailRepository memoryRepository = new SailRepository(
		        new MemoryStore(new File("/tmp/test-unit-memory/")));
		try {
			final BufferedInputStream dgsInputStream = new BufferedInputStream(
			        new FileInputStream(dgsInput));
			AnalyticsClassAttributes
			        .initClassAttributes(new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });

			memoryRepository.initialize();
			memoryRepository.getConnection().add(dgsInputStream, "",
			        RDFFormat.NTRIPLES);
			memoryRepository.shutDown();
			logger.debug(" MEMORY CLOSE");
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.debug("CREATE Native");
		final SailRepository nativeRepository = new SailRepository(
		        new NativeStore(new File("/tmp/test-unit-native/")));
		try {
			final BufferedInputStream dgsInputStream = new BufferedInputStream(
			        new FileInputStream(dgsInput));
			AnalyticsClassAttributes
			        .initClassAttributes(new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });

			nativeRepository.initialize();
			nativeRepository.getConnection().add(dgsInputStream, "",
			        RDFFormat.NTRIPLES);
			nativeRepository.shutDown();
			logger.debug("NATIVE CLOSE");
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testQuery() throws IllegalArgumentException, HttpException,
	        IOException {
		final String query = "SELECT ?p WHERE { ?s ?p ?o . }LIMIT 4";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[{\"p\":{\"type\""
			        + ":\"uri\",\"value\":\"http://opengraphprotocol.org/sch"
			        + "ema/firstName\"}},{\"p\":{\"type\":\"uri\",\"value\":"
			        + "\"http://opengraphprotocol.org/schema/type\"}},{\"p\":"
			        + "{\"type\":\"uri\",\"value\":\"http://opengraphprotocol."
			        + "org/schema/like\"}},{\"p\":{\"type\":\"uri\",\"value\":"
			        + "\"http://opengraphprotocol.org/schema/firstName\"}}"
			        + "]},\"head\":{\"link\":[],\"vars\":\"p\"},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryOrdered() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?s ?p WHERE { ?s ?p ?o . }ORDER BY ?s ?p";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\",\"ordered\":\"true\",\"bindings\":["
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/like\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/b\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/b\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/b\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/think_at\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/b\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/c\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/c\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/test\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/c\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/d\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/fail\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/e\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/f\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/do\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/f\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/g\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/test\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/g\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/g\"},\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"}}]},"
			        + "\"head\":{\"link\":[],\"vars\":[\"s\",\"p\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";
			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryPagination() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?p WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[{\"p\":"
			        + "{\"type\":\"uri\",\"value\":\"http://opengraphprotocol."
			        + "org/schema/like\"}},{\"p\":{\"type\":\"uri\",\"value\":"
			        + "\"http://opengraphprotocol.org/schema/firstName\"}}"
			        + "]},\"head\":{\"link\":[],\"vars\":\"p\"},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQuerySelectStar() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT * WHERE { ?s ?p ?o . } LIMIT 1";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));
		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":{"
			        + "\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Richard\\\"\"}}},"
			        + "\"head\":{\"link\":[],\"vars\":[\"s\",\"p\",\"o\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryFrom() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 4";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":\"p\"},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryFromAndPagination() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?s ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":[\"s\",\"p\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryEmptySelectStar() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT * WHERE { ?s <http://purl.org/dc/terms/title> ?o }LIMIT 10";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":[\"s\",\"o\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryInvalidSelect() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?p WHERE { ?s <http://purl.org/dc/terms/title> ?o }";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\","
			        + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":\"p\"},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testQueryInvalid() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?p WHERE { ?s ?o }";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"status\":\"ERROR\","
			        + "\"message\":\"org.openrdf.query.parser.sparql.ast.ParseException: "
			        + "Encountered \\\" \\\"}\\\" \\\"} \\\"\\\" "
			        + "at line 1, column 25.\\nWas expecting one of:\\n    \\\"("
			        + "\\\" ...\\n    \\\"[\\\" ...\\n    <NIL> ...\\n    <ANON> ..."
			        + "\\n    \\\"true\\\" ...\\n    \\\"false\\\" ...\\n    "
			        + "<Q_IRI_REF> ...\\n    <PNAME_NS> ...\\n    <PNAME_LN> ..."
			        + "\\n    <BLANK_NODE_LABEL> ...\\n    <VAR1> ...\\n    "
			        + "<VAR2> ...\\n    <INTEGER> ...\\n    <INTEGER_POSITIVE> "
			        + "...\\n    <INTEGER_NEGATIVE> ...\\n    <DECIMAL> ...\\n"
			        + "    <DECIMAL_POSITIVE> ...\\n    <DECIMAL_NEGATIVE> ..."
			        + "\\n    <DOUBLE> ...\\n    <DOUBLE_POSITIVE> ...\\n    "
			        + "<DOUBLE_NEGATIVE> ...\\n    <STRING_LITERAL1> ...\\n    "
			        + "<STRING_LITERAL2> ...\\n    <STRING_LITERAL_LONG1> ...\\n"
			        + "    <STRING_LITERAL_LONG2> ...\\n    \"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testSubmitQueryInvalid() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "SELECT ?p WHERE { ?s <INVALID> ?o }";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"status\":\"ERROR\","
			        + "\"message\":\"org.openrdf.query.MalformedQueryException: "
			        + "Not a valid (absolute) URI: INVALID\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testASK() throws IllegalArgumentException, HttpException,
	        IOException {
		final String query = "ASK WHERE { ?s <http://opengraphprotocol.org/schema/firstName> ?o . }";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"boolean\":\"true\",\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testASK2() throws IllegalArgumentException, HttpException,
	        IOException {
		final String query = "ASK WHERE { ?s <http://purl.org/dc/terms/NOTHING> ?o . }";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"boolean\":\"false\",\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testConstruct() throws IllegalArgumentException,
	        HttpException, IOException {
		final String query = "CONSTRUCT { ?product ?p ?o }  WHERE { "
		        + "?product ?p ?o ." + "} LIMIT 2";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\",\"ordered\":\"true\","
			        + "\"bindings\":["
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Richard\\\"\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Thing\\\"\"}}]}"
			        + ",\"head\":{\"link\":[],\"vars\":[\"s\",\"p\",\"o\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";

			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

	@Test
	public void testDescribe() throws IllegalArgumentException, HttpException,
	        IOException {
		final String query = "DESCRIBE ?s WHERE { "
		        + "?s <http://opengraphprotocol.org/schema/firstName> ?o "
		        + "} LIMIT 1";

		PostMethod post = new PostMethod(aseBaseUrl);
		post.addParameter(Protocol.QUERY_PARAM_NAME,
		        URLEncoder.encode(query, "UTF-8"));

		final int code = client.executeMethod(post);
		if (code == HttpStatus.SC_OK) {
			final String json = post.getResponseBodyAsString();
			final ObjectMapper mapper = new ObjectMapper();
			final HashMap<String, Object> jsonMap = mapper.readValue(json,
			        HashMap.class);
			String ref = "{\"results\":{\"distinct\":\"false\",\"ordered\":\"true\","
			        + "\"bindings\":["
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Richard\\\"\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Thing\\\"\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/like\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/c\"}},"
			        + "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/e\"},"
			        + "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/type\"},"
			        + "\"o\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"}}]},"
			        + "\"head\":{\"link\":[],\"vars\":[\"s\",\"p\",\"o\"]},"
			        + "\"status\":\"SUCCESS\",\"message\":\"\"}";
			assertEquals(ref, json.toString());
		} else {
			fail("code=" + code);
		}
	}

}
