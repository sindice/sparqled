package org.sindice.summary.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import org.junit.Test;
import org.openrdf.query.BindingSet;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Pipeline;
import org.sindice.summary.rest.SummaryRest.Status;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public class TestSummaryRest
extends JerseyTest {

  public TestSummaryRest() throws Exception {
    super("org.sindice.summary.rest");
  }

  @Test
  public void testCreateSummaryNative()
  throws IOException, SesameBackendException {
    try {
      final String repo = "./src/test/resources/rest/native";
      final String[] input;
      input = ("--feed --type NATIVE --repository " + repo
               + " --add ./src/test/resources/rest/data.nt").split(" ");
      Pipeline.main(input);

      final String repoEncoded = URLEncoder.encode(repo, "UTF-8"); // encoding is not necessary
      WebResource webResource = resource();
      webResource.path("summaries/create/native").queryParam("input-repo", repoEncoded).post(String.class);

      checkSummary(SesameBackendFactory.getDgsBackend(BackendType.NATIVE, repo), AnalyticsVocab.GRAPH_SUMMARY_GRAPH);
    } finally {
      FileUtil.deleteDir(new File("./src/test/resources/rest/native"));
    }
  }

  @Test
  public void testListNative()
  throws IOException, SesameBackendException {
    try {
      final String repo = "./src/test/resources/rest/native";
      final String[] input;
      input = ("--feed --type NATIVE --repository " + repo
               + " --add ./src/test/resources/rest/data.nt").split(" ");
      Pipeline.main(input);

      final String graph = "http://www.acme.org/cs";
      final String repoEncoded = URLEncoder.encode(repo, "UTF-8");
      WebResource webResource = resource();
      webResource.path("summaries/create/native").queryParam("input-repo", repo).queryParam("output-graph", graph).post(String.class);
      String responseMsg = webResource.path("summaries/list/native").queryParam("input-repo", repoEncoded).get(String.class);

      assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
      assertTrue(responseMsg.contains(graph));
    } finally {
      FileUtil.deleteDir(new File("./src/test/resources/rest/native"));
    }
  }

  private void checkSummary(SesameBackend<?, ?> backend, String graphName)
  throws SesameBackendException {
    backend.initConnection();

    QueryIterator<?, ?> it = backend.submit(
      "PREFIX an: <" + AnalyticsVocab.ANALYTICS_PREFIX + ">\n" +
      "SELECT DISTINCT ?Label ?pLabel\n" +
      "FROM <" + graphName + "> {\n" +
      "  ?node an:label [ an:label ?Label ] .\n" +
      "  ?edge an:source ?node ;\n" +
      "        an:label ?pLabel .\n" +
      "} ORDER BY ?pLabel"
    );

    assertTrue(it.hasNext());
    BindingSet b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://data.semanticweb.org/ns/swc/ontology#givesKeynoteTalk", b.getValue("pLabel").stringValue());
    assertTrue(it.hasNext());
    b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://data.semanticweb.org/ns/swc/ontology#holdsRole", b.getValue("pLabel").stringValue());
    assertTrue(it.hasNext());
    b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://swrc.ontoware.org/ontology#affiliation", b.getValue("pLabel").stringValue());
    assertTrue(it.hasNext());
    b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://www.w3.org/2000/01/rdf-schema#label", b.getValue("pLabel").stringValue());
    assertTrue(it.hasNext());
    b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://xmlns.com/foaf/0.1/made", b.getValue("pLabel").stringValue());
    assertTrue(it.hasNext());
    b = (BindingSet) it.next();
    assertEquals("http://xmlns.com/foaf/0.1/Person", b.getValue("Label").stringValue());
    assertEquals("http://xmlns.com/foaf/0.1/name", b.getValue("pLabel").stringValue());
    assertFalse(it.hasNext());

    backend.closeConnection();
  }

}
