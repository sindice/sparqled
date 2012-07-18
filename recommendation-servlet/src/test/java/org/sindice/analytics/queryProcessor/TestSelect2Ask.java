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
import org.sindice.analytics.queryProcessor.Select2Ask;

/**
 * @author bibhas [Jul 2, 2012]
 * @email bibhas.das@deri.org
 *
 */
public class TestSelect2Ask {

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
  public void testSelect2Ask() throws Exception{
    final String query = "SELECT DISTINCT * WHERE {?s ?p ?o} LIMIT 10";
    Select2Ask obj = new Select2Ask();
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);
    final ASTQueryContainer newAst = obj.convert(ast);
    String expected = "QueryContainer\n" +
                          " AskQuery\n"+
                          "  WhereClause\n"+
                          "   GraphPatternGroup\n"+
                          "    BasicGraphPattern\n"+
                          "     TriplesSameSubjectPath\n"+
                          "      Var (s)\n"+
                          "      PropertyListPath\n"+
                          "       Var (p)\n"+
                          "       ObjectList\n"+
                          "        Var (o)";

    assertEquals(expected, newAst.dump(""));
  }

}
