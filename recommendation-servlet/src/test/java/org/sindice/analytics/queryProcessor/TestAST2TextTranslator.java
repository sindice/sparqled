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
package org.sindice.analytics.queryProcessor;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;

/**
 * @author bibhas [Jul 19, 2012]
 * @email bibhas.das@deri.org
 *
 */
public class TestAST2TextTranslator {

  private ASTQueryContainer ast;
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testPOF() 
   throws Exception {
    final String query = "SELECT * WHERE { ?s < ?o .}";
    ast = SyntaxTreeBuilder.parseQuery(query);
    String translatedQuery = AST2TextTranslator.translate(ast);
    String expected = "SELECT *\n" +
                      "WHERE {\n" +
                      "  ?s < ?o .\n" +
                      "}\n";
    assertEquals(expected, translatedQuery);
  }
  
  @Test
  public void testGraphPOF()
   throws Exception {
    final String query = "SELECT * { GRAPH < { ?s ?p ?o .}}";
    ast = SyntaxTreeBuilder.parseQuery(query);
    String translatedQuery = AST2TextTranslator.translate(ast);
    String expected = "SELECT *\n" +
                      "WHERE {\n" +
                      "GRAPH < {\n" +
                      "  ?s ?p ?o .\n" +
                      "}\n" +
                      "}\n";
    assertEquals(expected, translatedQuery);
  }

}
