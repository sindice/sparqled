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
package org.sindice.analytics.queryProcessor;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.RecommendationQuery.Edge;
import org.sindice.analytics.queryProcessor.RecommendationQuery.Type;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;


/**
 * 
 */
public class TestSparqlTranslationProcessor {

  private final SparqlTranslationHelper helper = new SparqlTranslationHelper();

  @Before
  public void setUp()
  throws Exception {
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    SparqlVarGenerator.reset();
  }

  @Test
  public void testTranslateSimplePredicate()
  throws Exception {
    final String query = "SELECT * { ?s <name> ?o1 . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.edge().add(new Edge(null, "v1", "s", "<name>", null));

    helper.assertDGSQuery(rq, actual);
  }

  @Test
  public void testTranslateClassTriplePattern()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.type().add(new Type(null, "s", "<Person>"));

    helper.assertDGSQuery(rq, actual);
  }

  // FIXME: Correct handling of named graphs
  @Test
  @Ignore
  public void testTranslateDataset()
  throws Exception {
    final String query = "SELECT * FROM <http://ste.live.com> { Graph <http://sindice.com> { ?s a <Person> . ?s ?p ?o } ." +
                                                              " ?s a <Person> . ?s ?p ?o }";
  }

  // FIXME: Correct handling of named graphs
  @Test
  @Ignore
  public void testTranslateDataset2()
  throws Exception {
    final String query = "SELECT * { Graph <http://sindice.com> { ?s a <Person> . OPTIONAL {?s ?p ?o } } }";
  }

  // FIXME: Correct handling of named graphs
  @Test
  @Ignore
  public void testTranslateDatasetPOF()
  throws Exception {
    final String query = "SELECT * { GRAPH ?POF { ?s a <Person> . ?s <age> ?age } }";
  }

  @Test
  public void testPOFonClass() throws Exception {
    final String query = "SELECT * { ?a a ?POF . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
    rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR);
    rq.type().add(new Type(null, "s", "?" + SyntaxTreeBuilder.PointOfFocus));

    helper.assertDGSQuery(rq, actual);
  }

  @Test
  public void testConnectedObject1() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?s . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "?p", null));
    rq.edge().add(new Edge(null, "v2", "a", "?" + SyntaxTreeBuilder.PointOfFocus, "s"));

    helper.assertDGSQuery(rq, actual);
  }

  @Test
  public void testConnectedObject2() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?o . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "?p", "o"));
    rq.edge().add(new Edge(null, "v2", "a", "?" + SyntaxTreeBuilder.PointOfFocus, "o"));

    helper.assertDGSQuery(rq, actual);
  }

  @Test
  public void testConnectedObject3() throws Exception {
    final String query = "SELECT * { ?s ?p ?o . ?a ?POF ?p . }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final RecommendationQuery actual = SparqlTranslationProcessor.process(null, ast);
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "?p", null));
    rq.edge().add(new Edge(null, "v2", "a", "?" + SyntaxTreeBuilder.PointOfFocus, "p"));

    helper.assertDGSQuery(rq, actual);
  }

}
