package org.sindice.summary.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.BindingSet;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.webapps.SparqledContextListener;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Pipeline;
import org.sindice.summary.rest.SummaryRest.Status;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class TestSummaryRest
extends JerseyTest {

  private static final String INPUT_GRAPH      = "http://acme.ste.org/tech";
  private static final String PROXY_REPO       = "./src/test/resources/rest/native/repo";
  private static final String RECOMMENDER_REPO = "./src/test/resources/rest/native/repo-summary";

  public TestSummaryRest() throws Exception {
    super(new WebAppDescriptor.Builder("org.sindice.summary.rest")
    .contextListenerClass(SummaryRestContextListener.class)
    .contextParam(SparqledContextListener.SPARQLED_HOME, "./src/test/resources/sindice.home")
    .build());
  }

  @BeforeClass
  public static void beforeClass()
  throws SesameBackendException {
    String[] input;
    input = ("--feed --type NATIVE --repository " + PROXY_REPO
             + " --add ./src/test/resources/rest/data.nt"
            ).split(" ");
    Pipeline.main(input);

    input = ("--feed --type NATIVE --repository " + PROXY_REPO
             + " --add ./src/test/resources/rest/data.nt --domain " + INPUT_GRAPH
            ).split(" ");
    Pipeline.main(input);
  }

  @AfterClass
  public static void afterClass()
  throws IOException {
    FileUtil.deleteDir(new File("./src/test/resources/rest/native"));
  }

  @Test
  public void testCreate()
  throws IOException, SesameBackendException {
    WebResource webResource = resource();
    webResource.path("summaries/create").post(String.class);
    final SesameBackend<?> backend = SesameBackendFactory.getDgsBackend(BackendType.NATIVE, RECOMMENDER_REPO);
    checkSummary(backend, DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
  }

  @Test
  public void testUpdate()
  throws IOException, SesameBackendException {
    final String graph = INPUT_GRAPH + "/summary";
    final WebResource webResource = resource();
    final Form form = new Form();

    // Create the summary
    form.add("input-graph", INPUT_GRAPH);
    form.add("output-graph", graph);
    webResource.path("summaries/create").post(String.class, form);
    // Update it
    form.add("graph", graph);
    final String responseMsg = webResource.path("summaries/update").post(String.class, form);

    assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
    final SesameBackend<?> backend = SesameBackendFactory.getDgsBackend(BackendType.NATIVE, RECOMMENDER_REPO);
    checkSummary(backend, graph);
  }

  @Test
  public void testList()
  throws IOException, SesameBackendException {
    final String graph = "http://www.acme.org/cs";
    WebResource webResource = resource();
    Form form = new Form();
    form.add("output-graph", graph);
    webResource.path("summaries/create").post(String.class, form);
    String responseMsg = webResource.path("summaries/list").get(String.class);

    assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
    assertTrue(responseMsg.contains(graph));
  }

  @Test
  public void testDelete()
  throws IOException, SesameBackendException {
    final String graph = "http://www.acme.org/cs";
    WebResource webResource = resource();
    Form form = new Form();
    form.add("output-graph", graph);
    webResource.path("summaries/create").post(String.class, form);

    // Check that the summary is there
    String responseMsg = webResource.path("summaries/list").get(String.class);
    assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
    assertTrue(responseMsg.contains(graph));
    // Check that it is removed
    responseMsg = webResource.path("summaries/delete").queryParam("graph", graph).delete(String.class);
    assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
    assertTrue(responseMsg.contains(graph));
    // Check that the summary is not there anymore
    responseMsg = webResource.path("summaries/list").get(String.class);
    assertTrue(responseMsg.contains(Status.SUCCESS.toString()));
    assertFalse(responseMsg.contains(graph));
  }

  private void checkSummary(SesameBackend<?> backend, String graphName)
  throws SesameBackendException {
    backend.initConnection();

    QueryIterator<?> it = backend.submit(
      "PREFIX an: <" + DataGraphSummaryVocab.DGS_PREFIX + ">\n" +
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
