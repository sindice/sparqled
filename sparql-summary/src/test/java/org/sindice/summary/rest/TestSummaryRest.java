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

  private static final String proxyRepo       = "./src/test/resources/rest/native/repo";
  private static final String recommenderRepo = "./src/test/resources/rest/native/repo-summary";

  public TestSummaryRest() throws Exception {
    super(new WebAppDescriptor.Builder("org.sindice.summary.rest")
    .contextListenerClass(SummaryRestContextListener.class)
    .build());
  }

  @BeforeClass
  public static void beforeClass()
  throws SesameBackendException {
    System.setProperty("sindice.home", "./src/test/resources/sindice.home");
    final String[] input;
    input = ("--feed --type NATIVE --repository " + proxyRepo
             + " --add ./src/test/resources/rest/data.nt").split(" ");
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

    checkSummary(SesameBackendFactory.getDgsBackend(BackendType.NATIVE, recommenderRepo), DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
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

  private void checkSummary(SesameBackend<?, ?> backend, String graphName)
  throws SesameBackendException {
    backend.initConnection();

    QueryIterator<?, ?> it = backend.submit(
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
