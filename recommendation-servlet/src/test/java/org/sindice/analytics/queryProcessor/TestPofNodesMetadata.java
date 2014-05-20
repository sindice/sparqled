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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

public class TestPofNodesMetadata {

  private ASTQueryContainer ast;

  @Before
  public void setUp()
  throws Exception {
    ast = null;
    AnalyticsClassAttributes.initClassAttributes(Arrays.asList(AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE));
    SparqlVarGenerator.reset();
  }

  @Test
  public void testClassAttribute()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * { ?s a < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertCursor(meta.pofClassAttribute, 0, 0, 15, 15);
  }

  @Test
  public void testClassAttribute2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * { ?s ?p ?o ; rdf:type < }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertCursor(meta.pofClassAttribute, 0, 0, 54, 61);
  }

  @Test
  public void testClassAttribute3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertCursor(meta.pofClassAttribute, 1, 1, 10, 17);
  }

  @Test
  public void testClassAttribute4()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s ?p [ rdf:type <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertCursor(meta.pofClassAttribute, 1, 1, 10, 17);
  }

  @Test
  public void testClassAttribute5()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s ?p [ ?p1 <Person>, < ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertTrue(meta.pofClassAttribute == null);
  }

  @Test
  public void testPOFQName()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertMetadata(meta.pofNode, SyntaxTreeBuilder.Qname, "http://type.com/");
    assertFalse(meta.pofNode.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
  }

  @Test
  public void testPOFQName2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "prefix rdf: <http://type.com/> SELECT * {\n ?s a rdf:< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertMetadata(meta.pofNode, SyntaxTreeBuilder.Qname, "http://type.com/");
    assertCursor(meta.pofClassAttribute, 1, 1, 5, 5);
  }

  @Test
  public void testPOFkeyword()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertMetadata(meta.pofNode, SyntaxTreeBuilder.Keyword, "rdf");
    assertCursor(meta.pofNode, 1, 1, 7, 9);
    assertCursor(meta.pofClassAttribute, 1, 1, 5, 5);
  }

  @Test
  public void testPOFkeyword2()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a [ <type> rdf< ] }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertMetadata(meta.pofNode, SyntaxTreeBuilder.Keyword, "rdf");
    assertCursor(meta.pofNode, 1, 1, 16, 18);
    assertCursor(meta.pofClassAttribute, 1, 1, 9, 14);
  }

  @Test
  public void testPOFkeyword3()
  throws TokenMgrError, ParseException, MalformedQueryException,
  VisitorException {
    final String q = "SELECT * {\n ?s a <Person> ; <type> rdf< }";
    ast = SyntaxTreeBuilder.parseQuery(q);
    POFMetadata meta = PofNodesMetadata.retrieve(ast);

    assertMetadata(meta.pofNode, SyntaxTreeBuilder.Keyword, "rdf");
    assertCursor(meta.pofNode, 1, 1, 25, 27);
    assertCursor(meta.pofClassAttribute, 1, 1, 18, 23);
  }

  /**
   * Asserts the {@link SimpleNode#getMetadata() metadata} of the {@link SimpleNode} contains the given field and
   * associated value.
   * @param node the {@link SimpleNode}
   * @param field the field
   * @param value the associated value
   */
  private void assertMetadata(SimpleNode node, String field, Object value) {
    assertTrue(node != null);
    assertTrue(node.getMetadata().containsKey(field));
    assertEquals(value, node.getMetadata().get(field));
  }

  /**
   * Asserts the cursor metadata that is associated with a {@link SyntaxTreeBuilder#PointOfFocus}
   * @param node the {@link SimpleNode}
   * @param beginLine the start of the line
   * @param endLine the end of the line
   * @param beginColumn the start of the column
   * @param endColumn the end of the column
   */
  private void assertCursor(final SimpleNode node, int beginLine, int endLine, int beginColumn, int endColumn) {
    assertTrue(node != null);
    assertTrue(node.getMetadata().containsKey(SyntaxTreeBuilder.BeginLine));
    assertEquals(beginLine, node.getMetadata().get(SyntaxTreeBuilder.BeginLine));

    assertTrue(node.getMetadata().containsKey(SyntaxTreeBuilder.EndLine));
    assertEquals(endLine, node.getMetadata().get(SyntaxTreeBuilder.EndLine));

    assertTrue(node.getMetadata().containsKey(SyntaxTreeBuilder.BeginColumn));
    assertEquals(beginColumn, node.getMetadata().get(SyntaxTreeBuilder.BeginColumn));

    assertTrue(node.getMetadata().containsKey(SyntaxTreeBuilder.EndColumn));
    assertEquals(endColumn, node.getMetadata().get(SyntaxTreeBuilder.EndColumn));
  }

}
