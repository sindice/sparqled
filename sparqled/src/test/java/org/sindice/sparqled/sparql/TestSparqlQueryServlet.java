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
import static org.junit.Assert.fail;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.BACKEND;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.BACKEND_ARGS;
import static org.sindice.sparqled.sparql.SparqlQueryServletListener.SQS_WRAPPER;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
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

    String input = "./src/test/resources/QueryBackend/test.nt.gz";
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
      final String json = post.getResponseBodyAsString();
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
  public void testQueryOrdered()
  throws Exception {
    final String query = "SELECT ?s ?p WHERE { ?s ?p ?o . }ORDER BY ?s ?p";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
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
  public void testQueryPagination()
  throws Exception {
    final String query = "SELECT ?p WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
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
  public void testQuerySelectStar()
  throws Exception {
    final String query = "SELECT * WHERE { ?s ?p ?o . } LIMIT 1";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));
    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
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
  public void testQueryFrom()
  throws Exception {
    final String query = "SELECT ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 4";

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
  public void testQueryFromAndPagination()
  throws Exception {
    final String query = "SELECT ?s ?p FROM <http://rottentomatoes.com> WHERE { ?s ?p ?o . } LIMIT 2 OFFSET 2";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"results\":{\"distinct\":\"false\","
              + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":[\"s\",\"p\"]},"
              + "\"status\":\"SUCCESS\",\"message\":\"\"}";

      assertEquals(ref, json.toString());
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
      final String json = post.getResponseBodyAsString();
      String ref = "{\"results\":{\"distinct\":\"false\","
              + "\"ordered\":\"true\",\"bindings\":[]},\"head\":{\"link\":[],\"vars\":[\"s\",\"o\"]},"
              + "\"status\":\"SUCCESS\",\"message\":\"\"}";

      assertEquals(ref, json.toString());
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
      String ref = "{\"boolean\":\"true\",\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";

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
      String ref = "{\"boolean\":\"false\",\"head\":{\"link\":[]},\"status\":\"SUCCESS\",\"message\":\"\"}";

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
      final String json = post.getResponseBodyAsString();
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
  public void testDescribe()
  throws Exception {
    final String query = "DESCRIBE ?s WHERE { "
            + "?s <http://opengraphprotocol.org/schema/firstName> ?o "
            + "} LIMIT 1";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      String ref = "{\"results\":{\"distinct\":\"false\",\"ordered\":\"true\",\"bindings\":" +
          "{\"s\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/a\"}," +
          "\"p\":{\"type\":\"uri\",\"value\":\"http://opengraphprotocol.org/schema/firstName\"}," +
          "\"o\":{\"type\":\"uri\",\"value\":\"\\\"Richard\\\"\"}}}," +
          "\"head\":{\"link\":[],\"vars\":[\"s\",\"p\",\"o\"]}," +
          "\"status\":\"SUCCESS\",\"message\":\"\"}";
      assertEquals(ref, json.toString());
    } else {
      fail("code=" + code);
    }
  }

}
