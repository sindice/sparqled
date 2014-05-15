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
public class TestSparqlToDGSQuery {

  private final SparqlTranslationHelper helper = new SparqlTranslationHelper();

  @Before
  public void setUp()
  throws Exception {
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    SparqlVarGenerator.reset();
  }

  @Test
  public void testPOFWithLanguageTag()
  throws Exception {
    final String query = "SELECT * WHERE { ?s a \"test\"@en; < ?o }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "\"test\""));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFWithDatatypeTag()
  throws Exception {
    final String query = "SELECT * WHERE { ?s a \"test\"^^<xsd:int>; < ?o }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "\"test\""));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFTargetIsLeaf()
  throws Exception {
    final String query = "SELECT * WHERE { ?s ?p ?o. ?s2 < ?o }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "?p", "o"));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s2", "?" + SyntaxTreeBuilder.PointOfFocus, "o"));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  @Ignore
  public void testPOFGraph()
  throws Exception {
    final String query = "SELECT * WHERE { GRAPH < }";
  }

  @Test
  public void testPOFPropertyPath()
  throws Exception {
    final String query = "SELECT * WHERE { ?s <knows> / < }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "<knows>", "v2"));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "v2", "?" + SyntaxTreeBuilder.PointOfFocus, null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFBlankNodePropertyList()
  throws Exception {
    final String query = "SELECT * WHERE { ?s <knows> [< ] }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "v2", "?" + SyntaxTreeBuilder.PointOfFocus, null));
    rq.edge().add(new Edge(null, "v1", "s", "<knows>", "v2"));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFpredicateQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?o. }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "<Person>"));
    rq.edge().add(new Edge(null, "v1", "s", "<name>", null));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFpredicateMissingObjectQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ; <age> ?age }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "<Person>"));
    rq.edge().add(new Edge(null, "v1", "s", "<name>", null));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, null));
    rq.edge().add(new Edge(null, "v2", "s", "<age>", null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFpredicateTargetReused()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?n }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "<Person>"));
    rq.edge().add(new Edge(null, "v1", "s", "<name>", "n"));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, "n"));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFpredicateTargetReused2()
  throws Exception {
    final String query = "SELECT * { ?s ?p ?n. ?o < ?p }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.edge().add(new Edge(null, "v1", "s", "?p", null));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "o", "?" + SyntaxTreeBuilder.PointOfFocus, "p"));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFpredicateRelationQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?o. ?o <is_a> ?type }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type(null, "s", "<Person>"));
    rq.edge().add(new Edge(null, "v1", "s", "<name>", null));
    rq.edge().add(new Edge(null, QueryProcessor.POF_RESOURCE, "s", "?" + SyntaxTreeBuilder.PointOfFocus, "o"));
    rq.edge().add(new Edge(null, "v2", "o", "<is_a>", null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testPOFClassQuery()
  throws Exception {
    final String query = "SELECT * { ?s a < ; <name> ?n; <age> ?o. }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR);
    rq.addProjection(QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR);
    rq.type().add(new Type(null, "s", "?" + SyntaxTreeBuilder.PointOfFocus));
    rq.edge().add(new Edge(null, "v1", "s", "<name>", null));
    rq.edge().add(new Edge(null, "v2", "s", "<age>", null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  public void testGraphQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH <http://stecam.net> { ?s a <Person> ; <name> ?n; < ?o.} }";
    final SparqlToDGSQuery dgs = new SparqlToDGSQuery();
    final RecommendationQuery rq = new RecommendationQuery(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, null);

    rq.addProjection(SyntaxTreeBuilder.PointOfFocus);
    rq.addProjection(QueryProcessor.POF_RESOURCE);
    rq.addProjection(QueryProcessor.CARDINALITY_VAR);
    rq.type().add(new Type("<" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net>", "s", "<Person>"));
    rq.edge().add(new Edge("<" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net>", "v1", "s", "<name>", null));
    rq.edge().add(new Edge("<" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net>", "v2", "s", "?" + SyntaxTreeBuilder.PointOfFocus, null));

    ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    dgs.process(ast);

    helper.assertDGSQuery(rq, dgs.getRecommendationQuery());
  }

  @Test
  @Ignore
  public void testPOFGraphQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH < { ?s a <Person> } }";
  }

  // TODO: support nested select queries
  @Ignore
  @Test
  public void testNestedGraphAndSelectQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH <http://stecam.net> { ?s a <Person> ; <name> ?n; < ?o." +
                         "GRAPH <http://renaud.net> { ?a2 ?b ?c } {SELECT ?e FROM <http://example.org> {?e ?p ?e }} } }";
  }

}
