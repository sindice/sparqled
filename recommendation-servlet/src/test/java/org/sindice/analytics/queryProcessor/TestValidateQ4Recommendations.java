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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

public class TestValidateQ4Recommendations {

  private ASTQueryContainer   ast;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    ASTVarGenerator.reset();
  }

  @Test(expected=DGSException.class)
  public void testPOFGraph()
  throws Exception {
    final String q = "SELECT ?s WHERE { GRAPH <http://sindice.com>  }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test
  public void testPOFGraph2()
  throws Exception {
    final String q = "SELECT ?s WHERE { GRAPH <  }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test(expected=DGSException.class)
  public void testDuplicatePOF()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s < ?o ; a <  }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test(expected=DGSException.class)
  public void testDuplicateFillVar()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s <  ; ?p }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test(expected=DGSException.class)
  public void testWrongFillVar()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s ?p }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test(expected=DGSException.class)
  public void testWrongFillVar2()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s <name> }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

  @Test
  public void testGoodFillVar()
  throws Exception {
    final String q = "SELECT ?s WHERE { ?s < }";

    ast = SyntaxTreeBuilder.parseQuery(q);
    ValidateQ4Recommendations.process(ast);
  }

}
