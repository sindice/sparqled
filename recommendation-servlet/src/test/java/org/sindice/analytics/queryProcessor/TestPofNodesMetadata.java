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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.ASTVarGenerator;
import org.sindice.analytics.queryProcessor.PipelineObject;
import org.sindice.analytics.queryProcessor.PofNodesMetadata;
import org.sindice.analytics.queryProcessor.RecommendationType;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

public class TestPofNodesMetadata {

  private ASTQueryContainer ast;

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

  @Test
  public void testClassAttribute()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * { ?s a < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(0, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(0, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(15, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(15, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * { ?s ?p ?o ; rdf:type < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(0, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(0, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(54, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(61, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);
    
    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(10, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(17, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute4()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(10, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(17, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testClassAttribute5()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s ?p [ ?p1 <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofClassAttribute() == null);
  }

  @Test
  public void testPOFQName()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofNode() != null);
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.Qname));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Qname).size());
    assertEquals("http://type.com/", po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Qname).get(0));

    assertFalse(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
  }

  @Test
  public void testPOFQName2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s a rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofNode() != null);
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.Qname));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Qname).size());
    assertEquals("http://type.com/", po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Qname).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(5, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(5, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofNode() != null);
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(7, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(9, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(5, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(5, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a [ <type> rdf< ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofNode() != null);
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(16, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(18, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(9, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(14, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

  @Test
  public void testPOFkeyword3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a <Person> ; <type> rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    PipelineObject po = new PipelineObject(ast, null, RecommendationType.NONE, null, 0, null);
    new PofNodesMetadata().process(po);

    assertTrue(po.getMeta().getPofNode() != null);
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.Keyword));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).size());
    assertEquals("rdf", po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.Keyword).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(25, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofNode().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(27, po.getMeta().getPofNode().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));


    assertTrue(po.getMeta().getPofClassAttribute() != null);
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).size());
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndLine).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).size());
    assertEquals(18, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.BeginColumn).get(0));

    assertTrue(po.getMeta().getPofClassAttribute().getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(1, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).size());
    assertEquals(23, po.getMeta().getPofClassAttribute().getMetadata().get(SyntaxTreeBuilder.EndColumn).get(0));
  }

}
