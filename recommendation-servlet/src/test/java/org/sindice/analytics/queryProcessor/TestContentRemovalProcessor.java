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
public class TestContentRemovalProcessor {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    SparqlVarGenerator.reset();
  }

  @Test
  public void testObjectRemoval()
  throws Exception {
    final String q = "SELECT * { ?s ?p \"test\". ?s a <Person>. ?s ?p True. ?s ?p False }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    ContentRemovalProcessor.process(ast);

    final String[] vars = SparqlVarGenerator.getCurrentVarNames();
    assertEquals(3, vars.length);
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       Var (p)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[0] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        IRI (Person)\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       Var (p)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[1] + ")\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       Var (p)\n" +
                                "       ObjectList\n" +
                                "        Var (" + vars[2] + ")";


    assertEquals(expectedAst, ast.dump(""));
  }

  @Test
  public void testSubjectAndIRIRemoval()
  throws Exception {
    final String q = "SELECT * { ?s <name> <test>. ?s <label> <test>. <moi> a <Person>. <moi> ?p <Person> }";
    final String expectedAst = "QueryContainer\n" +
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
                                "          IRI (name)\n" +
                                "       ObjectList\n" +
                                "        Var (IRIn1010444415016862037)\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (label)\n" +
                                "       ObjectList\n" +
                                "        Var (IRIn1010444415016862037)\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (IRIn8119792335708867021)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          IRI (http://www.w3.org/1999/02/22-rdf-syntax-ns#type)\n" +
                                "       ObjectList\n" +
                                "        IRI (Person)\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (IRIn8119792335708867021)\n" +
                                "      PropertyListPath\n" +
                                "       Var (p)\n" +
                                "       ObjectList\n" +
                                "        Var (IRI4869599639157439053)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ContentRemovalProcessor.process(ast);
    assertEquals(expectedAst, ast.dump(""));
  }

}
