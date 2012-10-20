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
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 27 Apr 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.sindice.analytics.backend.DGSQueryResultProcessor.Context;
import org.sindice.analytics.queryProcessor.DGSException;
import org.sindice.analytics.queryProcessor.DGSQueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.ranking.Label;
import org.sindice.analytics.ranking.Label.LabelType;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.core.sesame.backend.testHelper.SesameNxParser;

/**
 * 
 */
public class TestDGSBackend {

  private static final SesameBackend<Label, Context> backend = SesameBackendFactory.getDgsBackend(BackendType.MEMORY, new DGSQueryResultProcessor());
  private static final String dgsInput = "./src/test/resources/DGSBackend/test-data-graph-summary_cascade.nt.gz";
  private final DGSQueryProcessor dgsQProcessor = new DGSQueryProcessor();
  private final ArrayList<Label> actualLabels = new ArrayList<Label>();

  private final Comparator<Label> cmpLabels = new Comparator<Label>() {
    @Override
    public int compare(Label l0, Label l1) {
      return l0.toString().compareTo(l1.toString());
    }
  };

  @BeforeClass
  public static void init()
  throws FileNotFoundException, IOException,
         RDFParseException, RepositoryException, SesameBackendException {
    final BufferedReader dgsInputReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dgsInput))));
    backend.initConnection();

    AnalyticsClassAttributes.initClassAttributes(new String[] {AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE});
    DataGraphSummaryVocab.resetToDefaults();

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
    try {
      addNQuads(dgsInputReader);
    } finally {
      dgsInputReader.close();
    }
  }

  @AfterClass
  public static void release()
      throws IOException, SesameBackendException {
    backend.closeConnection();
  }

  @Before
  public void setUp()
      throws RepositoryException {
    actualLabels.clear();
  }

  @Test
  public void testPredicateRecommendation()
  throws Exception {
    final String query = "SELECT * { ?s < ?o }";
    final ArrayList<Label> expected = new ArrayList<Label>(){{
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#produce", 1));
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#produce", 1));
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#livein", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testClassRecommendation()
  throws Exception {
    final String query = "SELECT * { ?s a < }";
    final ArrayList<Label> expected = new ArrayList<Label>(){{
      add(new Label(LabelType.LITERAL, "country", 1));
      // it appears in two nodes and one has two class attributes definition
      final Label l1 = new Label(LabelType.URI, "http://www.countries.eu/drink" , 1);
      l1.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      add(l1);
      final Label l2 = new Label(LabelType.URI, "http://www.countries.eu/drink" , 1);
      l2.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      add(l2);
      final Label l3 = new Label(LabelType.URI, "http://www.countries.eu/drink" , 1);
      l3.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://ogp.me/ns#type");
      add(l3);
      // it appears in two nodes
      add(new Label(LabelType.URI, "http://www.countries.eu/beer", 1));
      add(new Label(LabelType.URI, "http://www.countries.eu/beer", 1));
      add(new Label(LabelType.URI, "http://www.countries.eu/person", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testGraphRecommendation()
  throws Exception {
    final String query = "SELECT * { GRAPH < { ?s a <http://www.countries.eu/person> ; ?p ?o } }";
    final ArrayList<Label> expected = new ArrayList<Label>() {{
      add(new Label(LabelType.URI, "http://sindice.com/dataspace/default/domain/unipi.it", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; ?p ?o } GRAPH < { ?o a ?c } }";
    final ArrayList<Label> expected = new ArrayList<Label>() {{
      add(new Label(LabelType.URI, "http://sindice.com/dataspace/default/domain/countries.eu", 1));
      add(new Label(LabelType.URI, "http://sindice.com/dataspace/default/domain/unipi.it", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation2()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o } GRAPH <countries.eu> { ?o a ?c } }";
    final ArrayList<Label> expected = new ArrayList<Label>() {{
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#livein", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation3()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o . ?o a ?c } }";
    final ArrayList<Label> expected = new ArrayList<Label>() {{
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#produce", 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation4()
  throws Exception {
    final String query = "SELECT * FROM <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o . ?o a ?c }";
    final ArrayList<Label> expected = new ArrayList<Label>() {{
      add(new Label(LabelType.URI, "http://www.di.unipi.it/#produce", 1));
    }};
    executeQuery(query, expected);
  }

  private void executeQuery(String query, ArrayList<Label> expected)
  throws DGSException, IllegalArgumentException, IllegalAccessException,
  SecurityException, NoSuchFieldException, SesameBackendException {
    dgsQProcessor.load(query);
    final QueryIterator<Label, Context> qit = backend.submit(dgsQProcessor.getDGSQuery());
    Field f = qit.getContext().getClass().getDeclaredField("type");
    f.setAccessible(true);
    f.set(qit.getContext(), dgsQProcessor.getRecommendationType());
    while (qit.hasNext()) {
      actualLabels.add(qit.next());
    }
    Collections.sort(actualLabels, cmpLabels);
    Collections.sort(expected, cmpLabels);

    assertEquals(expected.size(), actualLabels.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actualLabels.get(i));
    }
    // Check that the expected context elements are present
    for (int i = 0; i < expected.size(); i++) {
      final Map<String, ArrayList<Object>> actualContext = actualLabels.get(i).getContext();
      for (Entry<String, ArrayList<Object>> c : expected.get(i).getContext().entrySet()) {
        assertTrue(actualContext.containsKey(c.getKey()));
        for (Object o : c.getValue()) {
          assertTrue(actualContext.get(c.getKey()).contains(o));
        }
      }
    }
  }

  private static void addNQuads(BufferedReader ntriples)
      throws RDFParseException, RepositoryException, IOException {
    backend.getConnection().add(ntriples, "", SesameNxParser.nquadsFormat);
  }

}
