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

import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

/**
 * 
 */
public class TestRecommendationScopeProcessor {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    SparqlVarGenerator.reset();
  }

  @Test
  public void testSimpleBGP()
  throws Exception {
    final String q = "SELECT * { ?s ?POF ?o . ?o a ?Person }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = ast.dump("");
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testDisjointBGPs()
  throws Exception {
    final String q = "SELECT * { ?s ?POF ?o . ?s2 a ?Person }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       Var (POF)\n" +
                            "       ObjectList\n" +
                            "        Var (o)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testDisjointGGPs()
  throws Exception {
    final String q = "SELECT * { ?s ?POF ?o . { SELECT * { ?s2 a ?Person } } }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       Var (POF)\n" +
                            "       ObjectList\n" +
                            "        Var (o)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testDisjointGGPs2()
  throws Exception {
    final String q = "SELECT * { ?s <knows> ?o . { SELECT * { ?o ?p ?o2. ?s2 a ?Person OPTIONAL {?s a ?POF} } } }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s)\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (knows)\n" +
                            "       ObjectList\n" +
                            "        Var (o)\n" +
                            "    GraphPatternGroup\n" +
                            "     SelectQuery\n" +
                            "      Select ( * )\n" +
                            "      WhereClause\n" +
                            "       GraphPatternGroup\n" +
                            "        BasicGraphPattern\n" +
                            "         TriplesSameSubjectPath\n" +
                            "          Var (o)\n" +
                            "          PropertyListPath\n" +
                            "           Var (p)\n" +
                            "           ObjectList\n" +
                            "            Var (o2)\n" +
                            "        OptionalGraphPattern\n" +
                            "         BasicGraphPattern\n" +
                            "          TriplesSameSubjectPath\n" +
                            "           Var (s)\n" +
                            "           PropertyListPath\n" +
                            "            PathAlternative\n" +
                            "             PathSequence\n" +
                            "              PathElt\n" +
                            "               IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                            "            ObjectList\n" +
                            "             Var (POF)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testRemovePrecedingBGP()
  throws Exception {
    final String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                         "SELECT * WHERE {\n" + 
                         "   ?movie a <http://www.schema.org/Movie>;\n" +
                         "          <http://www.schema.org/Movie/director> ?director.\n" +
                         "   ?n a ?POF " +
                         "}LIMIT 10\n";
    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (n)\n" +
                            "      PropertyListPath\n" +
                            "       PathAlternative\n" +
                            "        PathSequence\n" +
                            "         PathElt\n" +
                            "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                            "       ObjectList\n" +
                            "        Var (POF)";
    ast = SyntaxTreeBuilder.parseQuery(query);
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  /**
   * Here we have two GraphPatternGroup one after the other
   * but it is ok, the BNF is still correct
   */
  public void testDisjointGGPs3()
  throws Exception {
    final String q = "SELECT * { ?s1 <knows> ?o1 . { SELECT * { ?o ?p ?o2. ?s2 a ?Person OPTIONAL {?s a ?POF} } } }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    GraphPatternGroup\n" +
                            "     SelectQuery\n" +
                            "      Select ( * )\n" +
                            "      WhereClause\n" +
                            "       GraphPatternGroup\n" +
                            "        OptionalGraphPattern\n" +
                            "         BasicGraphPattern\n" +
                            "          TriplesSameSubjectPath\n" +
                            "           Var (s)\n" +
                            "           PropertyListPath\n" +
                            "            PathAlternative\n" +
                            "             PathSequence\n" +
                            "              PathElt\n" +
                            "               IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                            "            ObjectList\n" +
                            "             Var (POF)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testGraphGraphPattern()
  throws Exception {
    final String q = "SELECT * { ?s1 <knows> ?o1 . GRAPH ?POF { ?o ?p ?s } ?a ?b ?p }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    GraphGraphPattern\n" +
                            "     Var (POF)\n" +
                            "     GraphPatternGroup\n" +
                            "      BasicGraphPattern\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (o)\n" +
                            "        PropertyListPath\n" +
                            "         Var (p)\n" +
                            "         ObjectList\n" +
                            "          Var (s)\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (a)\n" +
                            "      PropertyListPath\n" +
                            "       Var (b)\n" +
                            "       ObjectList\n" +
                            "        Var (p)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

  @Test
  public void testGraphGraphPattern2()
  throws Exception {
    final String q = "SELECT * { ?s1 ?POF ?g . GRAPH ?g { ?o ?p ?s. } ?a ?b ?c }";
    ast = SyntaxTreeBuilder.parseQuery(q);

    final String expected = "QueryContainer\n" +
                            " SelectQuery\n" +
                            "  Select ( * )\n" +
                            "  WhereClause\n" +
                            "   GraphPatternGroup\n" +
                            "    BasicGraphPattern\n" +
                            "     TriplesSameSubjectPath\n" +
                            "      Var (s1)\n" +
                            "      PropertyListPath\n" +
                            "       Var (POF)\n" +
                            "       ObjectList\n" +
                            "        Var (g)\n" +
                            "    GraphGraphPattern\n" +
                            "     Var (g)\n" +
                            "     GraphPatternGroup\n" +
                            "      BasicGraphPattern\n" +
                            "       TriplesSameSubjectPath\n" +
                            "        Var (o)\n" +
                            "        PropertyListPath\n" +
                            "         Var (p)\n" +
                            "         ObjectList\n" +
                            "          Var (s)";
    RecommendationScopeProcessor.process(ast);
    assertEquals(expected, ast.dump(""));
  }

}
