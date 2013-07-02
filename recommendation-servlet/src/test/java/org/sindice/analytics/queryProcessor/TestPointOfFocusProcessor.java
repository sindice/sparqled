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

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

/**
 * 
 */
public class TestPointOfFocusProcessor {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    ASTVarGenerator.reset();
  }

  @After
  public void tearDown()
  throws Exception {
    checkCreatedNodes(ast);
  }

  private void checkCreatedNodes(SimpleNode n)
  throws InstantiationException, IllegalAccessException {
    final String str = n.toString("org.openrdf.sindice.query.parser.sparql.ast.AST");
    assertEquals(n.getClass().getName(), str.substring(0, str.indexOf(' ') == -1 ? str.length() : str.indexOf(' ')));
    for (int i = 0; i < n.jjtGetNumChildren(); i++) {
      checkCreatedNodes((SimpleNode) n.jjtGetChild(i));
    }
  }

  @Test
  public void testMaterializePOF()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s < ?o }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select\n" +
                                "   ProjectionElem\n" +
                                "    Var (POF)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testPOFOnObject()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s a < }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select\n" +
                                "   ProjectionElem\n" +
                                "    Var (POF)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        Var (POF)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test(expected=DGSException.class)
  public void testPOFOnObject2()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s <name> < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
  }

  @Test(expected=DGSException.class)
  public void testPOFOnObject3()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s ?p < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
  }

  public void testAdditionalProjectionVars()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s < ?o }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select\n" +
                                "   ProjectionElem\n" +
                                "    Var (POF)\n" +
                                "   ProjectionElem\n" +
                                "    Var (var2)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, Arrays.asList("var2"));
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testReplaceWithSelectQueryPOF()
  throws Exception {
    final String qConstruct = "CONSTRUCT { ?s a ?o } WHERE { ?s < ?o }";
    final String qAsk = "ASK WHERE { ?s < ?o }";
    final String qDescribe = "DESCRIBE ?s WHERE { ?s < ?o }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select\n" +
                                "   ProjectionElem\n" +
                                "    Var (POF)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    ast = SyntaxTreeBuilder.parseQuery(qAsk);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));

    ast = SyntaxTreeBuilder.parseQuery(qConstruct);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));

    ast = SyntaxTreeBuilder.parseQuery(qDescribe);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testConstructQueryPOFWithSolutionsMod()
  throws Exception {
    final String q = "CONSTRUCT { ?s a ?o } WHERE { ?s < ?o } GROUP BY(?s)";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select\n" +
                                "   ProjectionElem\n" +
                                "    Var (POF)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)\n" +
                                "  GroupClause\n" +
                                "   GroupCondition\n" +
                                "    Var (s)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testRecommendationTypeClass()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s a < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final RecommendationType type = PointOfFocusProcessor.process(ast, null);
    assertEquals(RecommendationType.CLASS, type);
  }

  @Test
  public void testRecommendationTypePredicate()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s < ?o }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final RecommendationType type = PointOfFocusProcessor.process(ast, null);
    assertEquals(RecommendationType.PREDICATE, type);
  }

  @Test
  public void testRecommendationTypeGraph()
  throws Exception {
    final String q = "SELECT ?s WHERE { GRAPH < { ?s ?p ?o } }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    final RecommendationType type = PointOfFocusProcessor.process(ast, null);
    assertEquals(RecommendationType.GRAPH, type);
  }

  @Test(expected=DGSException.class)
  public void testWrongRecommendationType()
  throws Exception {
    final String q = "SELECT ?s WHERE { GRAPH ?g { < ?p ?o } }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PointOfFocusProcessor.process(ast, null);
  }

}
