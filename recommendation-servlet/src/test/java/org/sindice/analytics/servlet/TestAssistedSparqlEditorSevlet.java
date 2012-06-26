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
package org.sindice.analytics.servlet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.sindice.analytics.servlet.ResponseWriterFactory.ResponseType;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.core.sesame.backend.testHelper.SesameNxParser;

public class TestAssistedSparqlEditorSevlet {

  private static final String  dgsInput = "./src/test/resources/DGSBackend/test-data-graph-summary_cascade.nt.gz";

  private static ServletTester aseTester;
  private static String        aseBaseUrl;

  private static HttpClient    client   = null;

  class Results implements Comparable<Results> {

    public final int count;
    public final String label;

    public Results(int count, String label) {
      this.count = count;
      this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Results) {
        Results r = (Results) obj;
        return this.count == r.count && this.label.equals(r.label);
      }
      return false;
    }

    @Override
    public int compareTo(Results r) {
      if (this.count < r.count) {
        return -1;
      } else if (this.count > r.count) {
        return 1;
      }
      return this.label.compareTo(r.label);
    }

    @Override
    public String toString() {
      return "count=" + count + " label=" + label;
    }

  }

  @BeforeClass
  public static void setUpBeforeClass()
  throws Exception {
    // Add the nquads parser
    RDFParserRegistry.getInstance().add(new RDFParserFactory() {
  
      @Override
      public RDFFormat getRDFFormat() {
        return SesameNxParser.nquadsFormat;
      }
  
      @Override
      public RDFParser getParser() {
        return new SesameNxParser();
      }
  
    });

    client = new HttpClient();

    aseTester = new ServletTester();
    aseTester.setContextPath("/");
    aseTester.setAttribute(MemorySesameServletHelper.FILE_STREAM, new GZIPInputStream(new FileInputStream(dgsInput)));
    aseTester.setAttribute(MemorySesameServletHelper.FORMAT, SesameNxParser.nquadsFormat);
    aseTester.addServlet(MemorySesameServletHelper.class, "/DGS-repo");

    String url = aseTester.createSocketConnector(true);
    final String dgsRepoServletUrl = url + "/DGS-repo";

    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.BACKEND, BackendType.HTTP.toString());
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.BACKEND_ARGS, new String[] { dgsRepoServletUrl });
    aseTester.addServlet(AssistedSparqlEditorServlet.class, "/SparqlEditorServlet");
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.RANKING_CONFIGURATION, "src/main/resources/default-ranking.yaml");
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.DOMAIN_URI_PREFIX, AnalyticsVocab.DOMAIN_URI_PREFIX);
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.PAGINATION, 1000);
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.DATASET_LABEL_DEF, DatasetLabel.SECOND_LEVEL_DOMAIN.toString());
    aseTester.setAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.CLASS_ATTRIBUTES, new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });
    /*
     * Comment it to prevent it from creating the sindice.home_IS_UNDEFINED folder, or from writing into sindice.home/ROOT
     * TODO: Update the Listener to enable a Test mode -> the log files are created in a temp folder
     */
//    aseTester.setEventListeners(new EventListener[] { new AssistedSparqlEditorListener() });

    aseBaseUrl = url + "/SparqlEditorServlet";

    System.out.println("dgsRepoURL: [" + dgsRepoServletUrl + "]");
    System.out.println("aseURL: [" + aseBaseUrl + "]");

    aseTester.start();
  }

  @AfterClass
  public static void tearDownAfterClass()
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.di.unipi.it/#produce"));
        add(new Results(1, "http://www.di.unipi.it/#livein"));
      }};
      checkResponse(jsonMap, expectedResults, false, false);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testPredicateBNPL()
  throws IllegalArgumentException, HttpException, IOException {
    final String query = "SELECT * { ?s ?p [< ] }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(1, "http://www.di.unipi.it/#produce"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.ERROR, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testClassAttributeRecommendation()
  throws Exception {
    final String query = "SELECT * { ?s a < }";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.countries.eu/beer"));
        add(new Results(2, "http://www.countries.eu/drink"));
        add(new Results(1, "http://www.countries.eu/person"));
        add(new Results(1, "country"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "produce"));
        add(new Results(1, "livein"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "drink"));
        add(new Results(2, "beer"));
        add(new Results(1, "person"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.countries.eu/drink"));
        add(new Results(2, "http://www.countries.eu/beer"));
        add(new Results(1, "http://www.countries.eu/person"));
        add(new Results(1, "country"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();

      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.di.unipi.it/#produce"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.di.unipi.it/#produce"));
        add(new Results(1, "http://www.di.unipi.it/#livein"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.di.unipi.it/#produce"));
        add(new Results(1, "http://www.di.unipi.it/#livein"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, "http://www.di.unipi.it/#produce"));
        add(new Results(1, "http://www.di.unipi.it/#livein"));
      }};
      checkResponse(jsonMap, expectedResults, false, true);
    } else {
      fail("code=" + code);
    }
  }

  @Test
  public void testPOFGraph()
  throws Exception {
    /*
     * the literal is removed by the DGS processing as it is not a query
     * structural element
     */
    final String query = "SELECT * { GRAPH <}";

    PostMethod post = new PostMethod(aseBaseUrl);
    post.addParameter(Protocol.QUERY_PARAM_NAME, URLEncoder.encode(query, "UTF-8"));
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(2, AnalyticsVocab.DOMAIN_URI_PREFIX + "countries.eu"));
        add(new Results(4, AnalyticsVocab.DOMAIN_URI_PREFIX + "unipi.it"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      final ArrayList<Results> expectedResults = new ArrayList<Results>() {{
        add(new Results(1, "http://www.di.unipi.it/#produce"));
      }};
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
    post.addParameter(AssistedSparqlEditorServlet.DATA_REQUEST, "autocomplete");
    post.addParameter(AssistedSparqlEditorServlet.RESPONSE, ResponseType.JSON.toString());

    final int code = client.executeMethod(post);
    if (code == HttpStatus.SC_OK) {
      final String json = post.getResponseBodyAsString();
      final ObjectMapper mapper = new ObjectMapper();
      final HashMap<String, Object> jsonMap = mapper.readValue(json, HashMap.class);
      assertTrue(jsonMap.containsKey(ResponseStructure.STATUS));
      assertEquals(ResponseStructure.NONE, jsonMap.get(ResponseStructure.STATUS));
    } else {
      fail("code=" + code);
    }
  }

  private void checkResponse(final HashMap<String, Object> jsonMap,
                             final ArrayList<Results> expectedResults,
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
    assertTrue(results.containsKey(ResponseStructure.RANKINGS) &&
               results.get(ResponseStructure.RANKINGS) instanceof List);
    assertEquals(1, ((List<Map>) results.get(ResponseStructure.RANKINGS)).size()); // only one ranking, the default one
    assertTrue(((List<Map>) results.get(ResponseStructure.RANKINGS)).get(0)
    .containsKey(ResponseStructure.NAME));
    assertEquals("DEFAULT", ((List<Map>) results.get(ResponseStructure.RANKINGS))
    .get(0).get(ResponseStructure.NAME)); // only one ranking, the default one
    assertTrue(((List<Map>) results.get(ResponseStructure.RANKINGS)).get(0)
    .containsKey(ResponseStructure.BINDINGS));
    assertTrue(((List<Map>) results.get(ResponseStructure.RANKINGS)).get(0)
    .get(ResponseStructure.BINDINGS) instanceof List);
    final List<Map<String, Object>> bindings = (List<Map<String, Object>>) ((List<Map>) results
    .get(ResponseStructure.RANKINGS)).get(0).get(ResponseStructure.BINDINGS);

    assertEquals(expectedResults.size(), bindings.size());

    final ArrayList<Results> actualResults = new ArrayList<Results>();
    for (Map<String, Object> r : bindings) {
      actualResults.add(new Results(Float.valueOf(r.get(ResponseStructure.COUNT).toString())
      .intValue(), r.get(ResponseStructure.VALUE).toString()));
    }
    Collections.sort(actualResults);
    Collections.sort(expectedResults);
    assertArrayEquals(expectedResults.toArray(new Results[0]), actualResults.toArray(new Results[0]));
  }

}
