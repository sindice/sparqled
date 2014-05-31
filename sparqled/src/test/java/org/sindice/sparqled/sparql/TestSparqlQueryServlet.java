/**
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
 */
package org.sindice.sparqled.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.BACKEND;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.BACKEND_ARGS;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.SQS_WRAPPER;
import static org.sindice.sparqled.sparql.SparqlResultsHelper.add;
import static org.sindice.sparqled.sparql.SparqlResultsHelper.binding;
import static org.sindice.sparqled.sparql.SparqlResultsHelper.bindings;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.rio.RDFFormat;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.sparqled.MemorySesameServletHelper;

/**
 * 
 */
public class TestSparqlQueryServlet {

  private final ServletTester aseTester = new ServletTester();
  private String              aseBaseUrl;
  private final HttpClient    client    = new HttpClient();

  @Before
  public void setUp()
  throws Exception {
    aseTester.setContextPath("/");

    String input = "./src/test/resources/testSparqlQueryServlet/test.nt.gz";
    aseTester.setAttribute(MemorySesameServletHelper.FILE_STREAM, new GZIPInputStream(new FileInputStream(input)));
    aseTester.setAttribute(MemorySesameServletHelper.FORMAT, RDFFormat.NTRIPLES);
    aseTester.addServlet(MemorySesameServletHelper.class, "/repo");

    String url = aseTester.createSocketConnector(true);
    final String repoUrl = url + "/repo";

    aseTester.setAttribute(SQS_WRAPPER + BACKEND, BackendType.HTTP.toString());
    aseTester.setAttribute(SQS_WRAPPER + BACKEND_ARGS, new String[] { repoUrl });
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

  @Test
  public void testQuery()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s ?p ?o . }LIMIT 4";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/like"))
      ));
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryOrdered()
  throws Exception {
    final String query = "SELECT ?s ?p WHERE { ?s ?p ?o . } ORDER BY ?s ?p";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/like"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/b")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/b")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/b")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/think_at"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/b")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/c")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/c")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/test"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/c")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/d")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/fail"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/e")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/f")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/do"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/f")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));

      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/g")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/test"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/g")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/g")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type"))
      ));
      assertResults(post, expected, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryPagination()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName"))
      ));
      expected.add(bindings(
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/like"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQuerySelectStar()
  throws Exception {
    final String query = "SELECT * WHERE { ?s ?p ?o . } LIMIT 1";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));
    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName")),
        binding("o", add("type", "literal"), add("value", "Richard"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryFrom()
  throws Exception {
    final String query = "SELECT ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 4";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryFromAndPagination()
  throws Exception {
    final String query = "SELECT ?s ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryEmptySelectStar()
  throws Exception {
    final String query = "SELECT * WHERE { ?s <http://purl.org/dc/terms/title> ?o }LIMIT 10";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryInvalidSelect()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s <http://purl.org/dc/terms/title> ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"results\":{\"distinct\":\"false\","
              + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":\"p\"},"
              + "\"status\":\"SUCCESS\",\"message\":\"\"}";

      assertEquals(ref, json.toString());
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testQueryInvalid()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
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
  public void testSubmitQueryInvalid()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s <INVALID> ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"status\":\"ERROR\","
              + "\"message\":\"org.openrdf.repository.http.HTTPQueryEvaluationException: " +
              "org.openrdf.query.MalformedQueryException: Not a valid (absolute) URI: INVALID\"}";

      assertEquals(ref, json.toString());
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testASK()
  throws Exception {
    final String query = "ASK WHERE { ?s <http://opengraphprotocol.org/schema/firstName> ?o . }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"boolean\":true,\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";

      assertEquals(ref, json.toString());
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testASK2()
  throws Exception {
    final String query = "ASK WHERE { ?s <http://purl.org/dc/terms/NOTHING> ?o . }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"boolean\":false,\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";
      assertEquals(ref, json.toString());
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testConstruct()
  throws Exception {
    final String query = "CONSTRUCT { ?product ?p ?o }  WHERE { "
            + "?product ?p ?o ." + "} LIMIT 2";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName")),
        binding("o", add("type", "literal"), add("value", "Richard"))
      ));
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/type")),
        binding("o", add("type", "literal"), add("value", "Thing"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testDescribe()
  throws Exception {
    final String query = "DESCRIBE ?s WHERE { "
            + "?s <http://opengraphprotocol.org/schema/firstName> ?o "
            + "} LIMIT 1";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("s", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/a")),
        binding("p", add("type", "uri"), add("value", "http://opengraphprotocol.org/schema/firstName")),
        binding("o", add("type", "literal"), add("value", "Richard"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testVariableType()
  throws Exception {
    String input = "src/test/resources/testSparqlQueryServlet/testVariableType/test.nt.gz";

    aseTester.stop();
    String url = aseTester.createSocketConnector(true);
    final String repoUrl = url + "/repo";

    aseTester.setAttribute(SQS_WRAPPER + BACKEND, BackendType.HTTP.toString());
    aseTester.setAttribute(SQS_WRAPPER + BACKEND_ARGS, new String[] { repoUrl });
    aseTester.addServlet(SparqlQueryServlet.class, "/SparqlEditorServlet");

    aseBaseUrl = url + "/SparqlEditorServlet";
    aseTester.setAttribute(MemorySesameServletHelper.FILE_STREAM, new GZIPInputStream(new FileInputStream(input)));
    aseTester.start();

    final String query = "select ?o { ?s ?p ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final List<Map<String, Object>> expected = new ArrayList<Map<String,Object>>();
      expected.add(bindings(
        binding("o", add("type", "literal"), add("value", "123"))
      ));
      expected.add(bindings(
        binding("o", add("type", "literal"), add("value", "Coeur"), add("xml:lang", "fr"))
      ));
      expected.add(bindings(
        binding("o", add("type", "typed-literal"), add("value", "123"), add("datatype", ":long"))
      ));
      expected.add(bindings(
        binding("o", add("type", "bnode"), add("value", "b1"))
      ));
      assertResults(post, expected, false);
    } else {
      fail("code=" + code);
    }
  }

  /**
   * Asserts the results returned by {@link SparqlQueryServlet}
   * @param post the {@link PostMethod} with the response returned by {@link SparqlQueryServlet}
   * @param expected the expected list of bindings
   * @param inOrder <code>true</code> if the order of bindings in the response should be asserted. If <code>false</code>,
   * the order of bindings do not matter.
   * @throws Exception
   * @see {@link SparqlResultsHelper}
   */
  private void assertResults(PostMethod post, List<Map<String, Object>> expected, boolean inOrder)
  throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final Map<String, Object> map = mapper.readValue(post.getResponseBodyAsStream(), Map.class);

    assertEquals("SUCCESS", map.get("status"));

    final Object actual = ((Map) map.get("results")).get("bindings");

    if (actual instanceof List) {
      final List<Map<String, Object>> list = (List<Map<String,Object>>) actual;
      if (inOrder) {
        assertEquals(expected, list);
      } else {
        for (Map<String, Object> l : expected) {
          assertThat(list, hasItem(l));
        }
      }
    } else {
      assertEquals(expected.get(0), actual);
    }
  }

}
