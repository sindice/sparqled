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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;


/**
 * 
 */
public class TestSparqlToDGSQuery {

  private ASTQueryContainer ast;


  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    ASTVarGenerator.reset();
  }

  @Test
  public void testPOFWithLanguageTag()
  throws Exception {
    final String query = "SELECT * WHERE { ?s a \"test\"@en; < ?o }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "o");
    assertEquals(2, vars.length);
    vars[1] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected =
    "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[1] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
    "WHERE {\n" +
    "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
    "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> \"test\" .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
    "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFWithDatatypeTag()
  throws Exception {
    final String query = "SELECT * WHERE { ?s a \"test\"^^<xsd:int>; < ?o }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "o");
    assertEquals(2, vars.length);
    vars[1] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected =
    "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[1] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
    "WHERE {\n" +
    "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
    "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> \"test\" .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
    "  ?" + vars[1] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
    "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFTargetIsLeaf()
  throws Exception {
    final String query = "SELECT * WHERE { ?s ?p ?o. ?s2 < ?o }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "p", "o", "s2");
    assertEquals(2, vars.length);
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + QueryProcessor.POF_RESOURCE + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "FILTER (!isLITERAL(?o ))  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?o .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> ?p .\n" +
                            "FILTER (!isLITERAL(?o ))  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s2 .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?o .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFGraph()
  throws Exception {
    final String query = "SELECT * WHERE { GRAPH < }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s");
    assertEquals(0, vars.length);
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + QueryProcessor.POF_RESOURCE + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n{\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> ?POF .\n" +
                            "}\n}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFPropertyPath()
  throws Exception {
    final String query = "SELECT * WHERE { ?s <knows> / < }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s");
    assertEquals(3, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "FILTER (!isLITERAL(?" + vars[0] + " ))  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <knows> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFBlankNodePropertyList()
  throws Exception {
    final String query = "SELECT * WHERE { ?s <knows> [< ] }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s");
    assertEquals(3, vars.length);
    vars[1] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[1] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "FILTER (!isLITERAL(?" + vars[0] + " ))  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> <knows> .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFpredicateQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?o. }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n", "o");
    assertEquals(3, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFpredicateMissingObjectQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ; <age> ?age }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n", "age");
    assertEquals(4, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.LABEL + "> <age> .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFpredicateTargetReused()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?n }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n");
    assertEquals(3, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "FILTER (!isLITERAL(?n ))  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?n .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "FILTER (!isLITERAL(?n ))  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?n .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFpredicateTargetReused2()
  throws Exception {
    final String query = "SELECT * { ?s ?p ?n. ?o < ?p }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "p", "o", "s", "n");
    assertEquals(2, vars.length);
    vars[1] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[1] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> ?p .\n" +
                            "FILTER (!isLITERAL(?p ))  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?o .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?p .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFpredicateRelationQuery()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?o. ?o <is_a> ?type }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n", "o", "type");
    assertEquals(4, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "FILTER (!isLITERAL(?o ))  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?o .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?o .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.LABEL + "> <is_a> .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFClassQuery()
  throws Exception {
    final String query = "SELECT * { ?s a < ; <name> ?n; <age> ?o. }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n", "o");
    assertEquals(4, vars.length);
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR + " ?" + QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR + " ?" + QueryProcessor.CARDINALITY_VAR + " ?" + QueryProcessor.POF_RESOURCE + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.TYPE + "> ?" + vars[1] + " .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CLASS_ATTRIBUTE_CARD_VAR + " .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> ?" + QueryProcessor.CLASS_ATTRIBUTE_LABEL_VAR + " .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?" + QueryProcessor.POF_RESOURCE + " .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?" + QueryProcessor.POF_RESOURCE + " .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.LABEL + "> <age> .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testSimpleQueryVarsToProject()
  throws Exception {
    final String query = "SELECT * { ?s a <Person> ; <name> ?n; < ?o. }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, Arrays.asList("n"));

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "s", "n", "o");
    assertEquals(3, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?n ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> \n" +
                            "WHERE {\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testGraphQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH <http://stecam.net> { ?s a <Person> ; <name> ?n; < ?o.} }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "a", "b", "c", "s", "n", "o");
    assertEquals(3, vars.length);
    vars[2] = QueryProcessor.POF_RESOURCE; // This is changed by the class SparqlTranslationProcessor.ChangeToPofRessource
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + vars[2] + " FROM <http://sindice.com/analytics> \n" +
                            "WHERE {\n" +
                            "{\n  ?s <" + DataGraphSummaryVocab.DOMAIN_URI + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> \"" + SparqlTranslationProcessor.BLANK_NODE_COLLECTION + "\" .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "}\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  @Test
  public void testPOFGraphQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH < { ?s a <Person> } }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "a", "b", "c", "s");
    assertEquals(1, vars.length);
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT DISTINCT ?POF ?" + QueryProcessor.CARDINALITY_VAR + " ?" + QueryProcessor.POF_RESOURCE + " FROM <http://sindice.com/analytics> \n" +
                            "WHERE {\n" +
                            "{\n  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.DOMAIN_URI + "> ?" + SyntaxTreeBuilder.PointOfFocus + " .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.CARDINALITY + "> ?" + QueryProcessor.CARDINALITY_VAR + " .\n" +
                            "  ?" + QueryProcessor.POF_RESOURCE + " <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[0] + " .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "}\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  // TODO: support nested select queries
  @Ignore
  @Test
  public void testNestedGraphAndSelectQuery()
  throws Exception {
    final String query = "SELECT * FROM <http://sindice.com> { ?a ?b ?c GRAPH <http://stecam.net> { ?s a <Person> ; <name> ?n; < ?o." +
                         "GRAPH <http://renaud.net> { ?a2 ?b ?c } {SELECT ?e FROM <http://example.org> {?e ?p ?e }} } }";

    ast = SyntaxTreeBuilder.parseQuery(query);
    SparqlToDGSQuery.process(ast, null);

    final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(), "a", "b", "c", "s", "n", "o",
      "a2", "e", "p");
    assertEquals(6, vars.length);
    final String dgsQuery = AST2TextTranslator.translate(ast);
    final String expected = "SELECT ?POF FROM <http://sindice.com/analytics> \n" +
                            "WHERE {\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "sindice.com" + "> .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?a .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?c .\n" +
                            "  ?" + vars[0] + " <" + DataGraphSummaryVocab.LABEL + "> ?b .\n" +
                            "GRAPH <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> {\n" +
                            "  ?s <" + DataGraphSummaryVocab.DOMAIN_URI + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "  ?s <" + DataGraphSummaryVocab.LABEL + "> ?" + vars[1] + " .\n" +
                            "  ?" + vars[1] + " <" + DataGraphSummaryVocab.LABEL + "> <Person> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?n .\n" +
                            "  ?" + vars[2] + " <" + DataGraphSummaryVocab.LABEL + "> <name> .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "stecam.net" + "> .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?s .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?o .\n" +
                            "  ?" + vars[3] + " <" + DataGraphSummaryVocab.LABEL + "> ?POF .\n" +
                            "GRAPH <" + DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH + "> {\n" +
                            "  ?" + vars[4] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "renaud.net" + "> .\n" +
                            "  ?" + vars[4] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?a2 .\n" +
                            "  ?" + vars[4] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?c .\n" +
                            "  ?" + vars[4] + " <" + DataGraphSummaryVocab.LABEL + "> ?b .\n" +
                            "}\n" +
                            "{\n" +
                            "SELECT ?e FROM <http://sindice.com/analytics> \n" +
                            "WHERE {\n" +
                            "  ?" + vars[5] + " <" + DataGraphSummaryVocab.EDGE_PUBLISHED_IN + "> <" + DataGraphSummaryVocab.DOMAIN_URI_PREFIX + "example.org" + "> .\n" +
                            "  ?" + vars[5] + " <" + DataGraphSummaryVocab.EDGE_SOURCE + "> ?e .\n" +
                            "  ?" + vars[5] + " <" + DataGraphSummaryVocab.EDGE_TARGET + "> ?e .\n" +
                            "  ?" + vars[5] + " <" + DataGraphSummaryVocab.LABEL + "> ?p .\n" +
                            "}\n" +
                            "}\n" +
                            "}\n" +
                            "}\n";
    assertEquals(expected, dgsQuery);
  }

  private final String[] filter(String[] vars, String... toFilter) {
    final ArrayList<String> v = new ArrayList<String>();
    final HashSet<String> f = new HashSet<String>(Arrays.asList(toFilter));

    // variables added by the modified sesame SPARQL grammar
    f.add("?" + SyntaxTreeBuilder.PointOfFocus);
    f.add("?" + SyntaxTreeBuilder.FillVar);
    for (String var : vars) {
      if (!f.contains(var)) {
        v.add(var);
      }
    }
    return v.toArray(new String[0]);
  }

}
