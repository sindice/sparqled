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
 * @author Campinas Stephane [ 20 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;


/**
 * 
 */
public class TestDGSDatasetClauseProcessor {

  private ASTQueryContainer   ast;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {
    ast = null;
    ASTVarGenerator.reset();
  }

  @Test
  public void testNoDatasetClause()
  throws Exception {
    final String q = "SELECT * { ?s < ?o }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  DatasetClause (named=false)\n" +
                                "   IRI (http://sindice.com/analytics)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (?POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    final Dataset datasets = DGSDatasetClauseProcessor.process(ast);
    assertEquals(expectedAst, ast.dump(""));
    assertEquals(null, datasets);
  }

  @Test
  public void testDatasetClauses()
  throws Exception {
    final String q = "SELECT * " +
                     "FROM <http://sindice.com> " +
                     "FROM NAMED <http://stephane.net> " +
                     "FROM <http://dgs.com> { ?s < ?o }";
    final String expectedAst = "QueryContainer\n" +
                                " SelectQuery\n" +
                                "  Select ( * )\n" +
                                "  DatasetClause (named=false)\n" +
                                "   IRI (http://sindice.com/analytics)\n" +
                                "  WhereClause\n" +
                                "   GraphPatternGroup\n" +
                                "    BasicGraphPattern\n" +
                                "     TriplesSameSubjectPath\n" +
                                "      Var (s)\n" +
                                "      PropertyListPath\n" +
                                "       PathAlternative\n" +
                                "        PathSequence\n" +
                                "         PathElt\n" +
                                "          Var (?POF)\n" +
                                "       ObjectList\n" +
                                "        Var (o)";

    ast = SyntaxTreeBuilder.parseQuery(q);
    final Dataset datasets = DGSDatasetClauseProcessor.process(ast);
    assertEquals(expectedAst, ast.dump(""));

    final URI[] defaults = datasets.getDefaultGraphs().toArray(new URI[2]);
    assertEquals(2, datasets.getDefaultGraphs().size());
    assertEquals("http://sindice.com", defaults[0].toString());
    assertEquals("http://dgs.com", defaults[1].toString());
    final URI[] named = datasets.getNamedGraphs().toArray(new URI[1]);
    assertEquals(1, datasets.getNamedGraphs().size());
    assertEquals("http://stephane.net", named[0].toString());
  }

}
