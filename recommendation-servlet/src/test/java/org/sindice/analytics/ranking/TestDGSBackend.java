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
package org.sindice.analytics.ranking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sindice.analytics.RDFTestHelper.literal;
import static org.sindice.analytics.RDFTestHelper.uri;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.sindice.analytics.queryProcessor.DGSQueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.ranking.Label;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;

/**
 * 
 */
public class TestDGSBackend {

  private static final SesameBackend<Label> backend = SesameBackendFactory.getDgsBackend(BackendType.MEMORY, new DGSQueryResultProcessor());
  private static final String dgsInput = "./src/test/resources/DGSBackend/test-data-graph-summary_cascade.nt.gz";
  private final DGSQueryProcessor dgsQProcessor = new DGSQueryProcessor();
  private final List<Label> actualLabels = new ArrayList<Label>();

  private final Comparator<Label> cmpLabels = new Comparator<Label>() {
    @Override
    public int compare(Label l0, Label l1) {
      final int c = l0.getLabel().compareTo(l1.getLabel());
      if (c == 0) {
        return l0.getContext().toString().compareTo(l1.getContext().toString());
      }
      return c;
    }
  };

  @BeforeClass
  public static void init()
  throws FileNotFoundException, IOException,
         RDFParseException, RepositoryException, SesameBackendException {
    final BufferedReader dgsInputReader = new BufferedReader(new InputStreamReader(
      new GZIPInputStream(new FileInputStream(dgsInput))));
    backend.initConnection();

    AnalyticsClassAttributes.initClassAttributes(new String[] {AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE});
    DataGraphSummaryVocab.resetToDefaults();

    try {
      final Resource c = NTriplesUtil.parseURI("<" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + ">", new MemValueFactory());
      backend.getConnection().add(dgsInputReader, "", RDFFormat.NTRIPLES, c);
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
    final List<Label> expected = new ArrayList<Label>(){{
      add(new Label(uri("http://www.di.unipi.it/#produce"), 1));
      add(new Label(uri("http://www.di.unipi.it/#produce"), 1));
      add(new Label(uri("http://www.di.unipi.it/#livein"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testClassRecommendation()
  throws Exception {
    final String query = "SELECT * { ?s a < }";
    final List<Label> expected = new ArrayList<Label>(){{
      add(new Label(literal("country"), 1));
      // it appears in two nodes and one has two class attributes definition
      final Label l1 = new Label(uri("http://www.countries.eu/drink"), 1);
      l1.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      add(l1);
      final Label l2 = new Label(uri("http://www.countries.eu/drink"), 1);
      l2.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
      add(l2);
      final Label l3 = new Label(uri("http://www.countries.eu/drink"), 1);
      l3.addContext(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR, "http://ogp.me/ns#type");
      add(l3);
      // it appears in two nodes
      add(new Label(uri("http://www.countries.eu/beer"), 1));
      add(new Label(uri("http://www.countries.eu/beer"), 1));
      add(new Label(uri("http://www.countries.eu/person"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testGraphRecommendation()
  throws Exception {
    final String query = "SELECT * { GRAPH < { ?s a <http://www.countries.eu/person> ; ?p ?o } }";
    final List<Label> expected = new ArrayList<Label>() {{
      add(new Label(uri("http://sindice.com/dataspace/default/domain/unipi.it"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; ?p ?o } GRAPH < { ?o a ?c } }";
    final List<Label> expected = new ArrayList<Label>() {{
      add(new Label(uri("http://sindice.com/dataspace/default/domain/countries.eu"), 1));
      add(new Label(uri("http://sindice.com/dataspace/default/domain/unipi.it"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation2()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o } GRAPH <countries.eu> { ?o a ?c } }";
    final List<Label> expected = new ArrayList<Label>() {{
      add(new Label(uri("http://www.di.unipi.it/#livein"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation3()
  throws Exception {
    final String query = "SELECT * { GRAPH <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o . ?o a ?c } }";
    final List<Label> expected = new ArrayList<Label>() {{
      add(new Label(uri("http://www.di.unipi.it/#produce"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testAcrossGraphsRecommendation4()
  throws Exception {
    final String query = "SELECT * FROM <http://pisa.unipi.it> { ?s a <http://www.countries.eu/person>; < ?o . ?o a ?c }";
    final List<Label> expected = new ArrayList<Label>() {{
      add(new Label(uri("http://www.di.unipi.it/#produce"), 1));
    }};
    executeQuery(query, expected);
  }

  @Test
  public void testLimitWithKeyword()
  throws Exception {
    final String query = "SELECT * { ?s produce< ?o }";
    final List<Label> expected = new ArrayList<Label>(){{
      add(new Label(uri("http://www.di.unipi.it/#produce"), 1));
    }};
    executeQuery(query, expected, 1, 20);
  }

  private void executeQuery(String query, List<Label> expected)
  throws Exception {
    executeQuery(query, expected, 0, 0);
  }

  private void executeQuery(String query, List<Label> expected, int limit, int pagination)
  throws Exception {
    dgsQProcessor.load(query);
    final String dgsQuery = limit == 0 ? dgsQProcessor.getDGSQuery() : dgsQProcessor.getDGSQueryWithLimit(limit);
    final QueryIterator<Label> qit = backend.submit(dgsQuery);
    qit.setPagination(pagination);
    while (qit.hasNext()) {
      actualLabels.add(qit.next());
    }
    Collections.sort(actualLabels, cmpLabels);
    Collections.sort(expected, cmpLabels);

    assertArrayEquals(actualLabels.toString(), expected.toArray(), actualLabels.toArray());
    // Check that the expected context elements are present
    for (int i = 0; i < expected.size(); i++) {
      final Map<String, Object> actualContext = actualLabels.get(i).getContext();
      for (Entry<String, Object> c : expected.get(i).getContext().entrySet()) {
        assertTrue(actualContext.containsKey(c.getKey()));
        assertEquals(c.getValue(), actualContext.get(c.getKey()));
      }
    }
  }

}
