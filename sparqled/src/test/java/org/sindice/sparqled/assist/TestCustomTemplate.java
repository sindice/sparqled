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
package org.sindice.sparqled.assist;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.BACKEND;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.BACKEND_ARGS;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.CLASS_ATTRIBUTES;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.DATASET_LABEL_DEF;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.DOMAIN_URI_PREFIX;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.GRAPH_SUMMARY_GRAPH;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.LIMIT;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.PAGINATION;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.RECOMMENDER_WRAPPER;
import static org.sindice.sparqled.assist.AssistedSparqlEditorListener.TEMPLATE;
import info.aduna.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.rio.RDFFormat;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;

public class TestCustomTemplate {

  private static final String dgsInput = "./src/test/resources/testCustomTemplate/custom-summary.nt.gz";

  private ServletTester       aseTester;
  private String              aseBaseUrl;

  private HttpClient          client   = null;

  @Before
  public void setUp()
  throws Exception {
    // reset the vocab parameters
    DataGraphSummaryVocab.resetToDefaults();

    client = new HttpClient();

    aseTester = new ServletTester();
    aseTester.setContextPath("/");
    aseTester.setAttribute(MemorySesameServletHelper.FILE_STREAM, new GZIPInputStream(new FileInputStream(dgsInput)));
    aseTester.setAttribute(MemorySesameServletHelper.FORMAT, RDFFormat.NTRIPLES);
    aseTester.addServlet(MemorySesameServletHelper.class, "/DGS-repo");

    String url = aseTester.createSocketConnector(true);
    final String dgsRepoServletUrl = url + "/DGS-repo";

    aseTester.setAttribute(RECOMMENDER_WRAPPER + TEMPLATE, "./src/test/resources/testCustomTemplate/custom-template.mustache");
    aseTester.setAttribute(RECOMMENDER_WRAPPER + BACKEND, BackendType.HTTP.toString());
    aseTester.setAttribute(RECOMMENDER_WRAPPER + BACKEND_ARGS, new String[] { dgsRepoServletUrl });
    aseTester.addServlet(AssistedSparqlEditorServlet.class, "/SparqlEditorServlet");
    aseTester.setAttribute(RECOMMENDER_WRAPPER + DOMAIN_URI_PREFIX, DataGraphSummaryVocab.DOMAIN_URI_PREFIX);
    aseTester.setAttribute(RECOMMENDER_WRAPPER + PAGINATION, 1000);
    aseTester.setAttribute(RECOMMENDER_WRAPPER + DATASET_LABEL_DEF, DatasetLabel.SECOND_LEVEL_DOMAIN);
    aseTester.setAttribute(RECOMMENDER_WRAPPER + CLASS_ATTRIBUTES, new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });
    aseTester.setAttribute(RECOMMENDER_WRAPPER + GRAPH_SUMMARY_GRAPH, DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
    aseTester.setAttribute(RECOMMENDER_WRAPPER + LIMIT, 0);
    /*
     * Comment it to prevent it from creating the sindice.home_IS_UNDEFINED folder, or from writing into sindice.home/ROOT
     * TODO: Update the Listener to enable a Test mode -> the log files are created in a temp folder
     */
//    aseTester.setEventListeners(new EventListener[] { new AssistedSparqlEditorListener() });

    aseBaseUrl = url + "/SparqlEditorServlet";
    aseTester.start();
  }

  @After
  public void tearDown()
  throws Exception {
    if (aseTester != null) {
      aseTester.stop();
    }
    File repo = new File("/tmp/DGS-repo-test");
    if (repo.exists()) {
      FileUtil.deleteDir(repo);
    }
  }

  @Test
  public void testPredicateRecommendation()
  throws IllegalArgumentException, HttpException, IOException {
    final String query = "SELECT * { ?s < ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.di.unipi.it/#produce",
        "http://www.di.unipi.it/#livein"
      );
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testChangeDGSGraph()
  throws IllegalArgumentException, HttpException, IOException {
    final String query = "SELECT * { ?s < ?o }";

    PutMethod put = new PutMethod(aseBaseUrl);
    put.setQueryString(new NameValuePair[] { new NameValuePair(AssistedSparqlEditorServlet.DGS_GRAPH, "http://not.there.com")});
    client.executeMethod(put);

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    client.executeMethod(post);
    final String json = post.getResponseBodyAsString();
    final ObjectMapper mapper = new ObjectMapper();

    final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
    assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
    assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
  }

  @Test
  public void testPredicateBNPL()
  throws IllegalArgumentException, HttpException, IOException {
    final String query = "SELECT * { ?s ?p [< ] }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.di.unipi.it/#produce"
      );
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testErrorMessage()
  throws IllegalArgumentException, HttpException, IOException {
    final String query = "SELECT * { ?s ?p < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.ERROR, jsonMap.get(ResponseStructure.STATUS));
      assertEquals("Recommendations on the object are only possible if the predicate is a class attribute",
        jsonMap.get(ResponseStructure.MESSAGE));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testUnclosedQuote()
  throws Exception {
    final String query = "SELECT * { ?s a \" < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.ERROR, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendation()
  throws Exception {
    final String query = "SELECT * { ?s a < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.countries.eu/beer",
        "http://www.countries.eu/drink",
        "http://www.countries.eu/person",
        "country"
      );
      checkResponse(jsonMap, expectedResults, true, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testPredicateRecommendationWithQName()
  throws Exception {
    final String query = "PREFIX unipi: <http://www.di.unipi.it/#> SELECT * { ?s unipi:< ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList("produce", "livein");
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendationWithQName()
  throws Exception {
    final String query = "PREFIX c: <http://www.countries.eu/> SELECT * { ?s a c:< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList("drink", "beer", "person");
      checkResponse(jsonMap, expectedResults, true, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendationWithKeyword()
  throws Exception {
    final String query = "SELECT * { ?s a count< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.countries.eu/drink",
        "http://www.countries.eu/beer",
        "http://www.countries.eu/person",
        "country"
      );
      checkResponse(jsonMap, expectedResults, true, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendationWithPrefix()
  throws Exception {
    final String query = "SELECT * { ?s a <http://fake.com/fake/< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendationWithQName2()
  throws Exception {
    final String query = "PREFIX rdf: <http://fake.com/fake/> SELECT * { ?s a rdf:< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassRecommendationWithKeyword2()
  throws Exception {
    final String query = "SELECT * { ?s a FAKEFAKE< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testPredicateRecommendationWithKeyword()
  throws Exception {
    final String query = "SELECT * { ?s produce< ?o }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList("http://www.di.unipi.it/#produce");
      checkResponse(jsonMap, expectedResults, false, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testDatatypeLiteral()
  throws Exception {
    final String query = "SELECT * { ?s <http://www.di< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.di.unipi.it/#produce", "http://www.di.unipi.it/#livein"
      );
      checkResponse(jsonMap, expectedResults, false, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testPredicateRecommendationWithPrefix()
  throws Exception {
    final String query = "SELECT * { ?s <http://www.di< }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.di.unipi.it/#produce",
        "http://www.di.unipi.it/#livein"
      );
      checkResponse(jsonMap, expectedResults, false, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testKeywordAndLiteral()
  throws Exception {
    /*
     * the literal is removed by the DGS processing as it is not a query
     * structural element
     */
    final String query = "SELECT * { ?s unipi< \"bla\" }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        "http://www.di.unipi.it/#produce",
        "http://www.di.unipi.it/#livein"
      );
      checkResponse(jsonMap, expectedResults, false, true);
    } else {
      fail("code=" + code);
    }
  }

  // FIXME: Correct handling of named graphs
  @Test
  @Ignore
  public void testPOFGraph()
  throws Exception {
    /*
     * the literal is removed by the DGS processing as it is not a query
     * structural element
     */
    final String query = "SELECT * { GRAPH <}";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList(
        DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "countries.eu",
        DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "unipi.it"
      );
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassLiteral()
  throws Exception {
    /*
     * the literal is NOT removed by the DGS processing since it is a class
     */
    final String query = "SELECT * { ?s a \"country\"; < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      final List<String> expectedResults = Arrays.asList("http://www.di.unipi.it/#produce");
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassLiteral2()
  throws Exception {
    /*
     * the literal is NOT removed by the DGS processing since it is a class
     */
    final String query = "SELECT * { ?s ?p [ a \"bla\" ]; < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  private void checkResponse(final Map<String, Object> jsonMap,
                             final List<String> expectedResults,
                             final boolean isCAsubsEnabled,
                             final boolean isRecSubsEnabled) {
    assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
    assertEquals(ResponseStructure.SUCCESS, jsonMap.get(ResponseStructure.STATUS));

    assertTrue(jsonMap.containsKey(ResponseStructure.RESULTS) &&
               jsonMap.get(ResponseStructure.RESULTS) instanceof Map);
    // check the number of results
    final Map results = (Map) jsonMap.get(ResponseStructure.RESULTS);
    assertTrue(results.containsKey(ResponseStructure.COUNT));
    assertEquals(expectedResults.size(), results.get(ResponseStructure.COUNT));

    // Check the substitution
    assertTrue(jsonMap.containsKey(ResponseStructure.CA_REPLACE));
    assertEquals(isCAsubsEnabled, jsonMap.get(ResponseStructure.CA_REPLACE));
    assertTrue(jsonMap.containsKey(ResponseStructure.REC_REPLACE));
    assertEquals(isRecSubsEnabled, jsonMap.get(ResponseStructure.REC_REPLACE));

    // Check the ranking results
    final List<Map<String, Object>> bindings = (List<Map<String, Object>>) results.get(ResponseStructure.BINDINGS);

    assertEquals(expectedResults.size(), bindings.size());

    final List<String> actualResults = new ArrayList<String>();
    for (Map<String, Object> r : bindings) {
      actualResults.add(r.get(ResponseStructure.VALUE).toString());
    }
    Collections.sort(actualResults);
    Collections.sort(expectedResults);
    assertArrayEquals(expectedResults.toArray(new String[expectedResults.size()]),
      actualResults.toArray(new String[actualResults.size()]));
  }

}
