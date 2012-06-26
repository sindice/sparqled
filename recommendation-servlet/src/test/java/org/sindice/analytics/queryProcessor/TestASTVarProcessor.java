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
 * @author Campinas Stephane [ 19 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;


import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;

/**
 * 
 */
public class TestASTVarProcessor {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {}

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown()
  throws Exception {}

  @Test
  public void testASTVar()
  throws Exception {
    final String query = "SELECT * { ?s ?p [ <name> ?n ] }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);

    final String[] expectedVars = { "n", "p", "s" };
    final String[] actualVars = ASTVarProcessor.process(ast).toArray(new String[0]);
    Arrays.sort(actualVars);
    assertArrayEquals(expectedVars, actualVars);
  }

  @Test
  public void testPOF()
  throws Exception {
    final String query = "SELECT * { ?s < [ <name> ?n ] }";
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);

    final String[] expectedVars = { "?" + SyntaxTreeBuilder.PointOfFocus, "n", "s" };
    final String[] actualVars = ASTVarProcessor.process(ast).toArray(new String[0]);
    Arrays.sort(actualVars);
    assertArrayEquals(expectedVars, actualVars);
  }

}
