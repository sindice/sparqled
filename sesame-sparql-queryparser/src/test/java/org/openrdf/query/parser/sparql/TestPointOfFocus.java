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
package org.openrdf.query.parser.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.ASTWhereClause;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * 
 */
public class TestPointOfFocus {

  /**
   * The POF is on the graph
   * @throws Exception
   */
  @Test
  public void testPOFGraph()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  GRAPH < \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(0, fillFinder.nodes.size());
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTGraphGraphPattern.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
  }

  /**
   * The POF is on the graph
   * @throws Exception
   */
  @Test
  public void testPOFGraph2()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  GRAPH ctd< \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(0, fillFinder.nodes.size());
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTGraphGraphPattern.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    final Object meta = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Keyword);
    assertTrue(meta != null);
    assertEquals("ctd", meta); // lower cased

    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginLine));
    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndLine));
    assertEquals(9, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginColumn));
    assertEquals(11, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndColumn));
  }

  /**
   * The POF is on the qname ns
   * @throws Exception
   */
  @Test
  public void testPOFQName()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s foaf:< ?e \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(0, fillFinder.nodes.size());
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);

    final Object qname = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Qname);
    assertTrue(qname != null);
    assertEquals("foaf", qname);
  }

  @Test
  public void testPOFKeywordMetadata()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s Compound< \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(1, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    final Object meta = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Keyword);
    assertTrue(meta != null);
    assertEquals("compound", meta); // lower cased

    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginLine));
    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndLine));
    assertEquals(6, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginColumn));
    assertEquals(13, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndColumn));
  }

  /**
   * There is an object
   * @throws Exception
   */
  @Test
  public void testPOFKeywordMetadata2()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s Compound< ?e\n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(0, fillFinder.nodes.size());
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    final Object meta = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Keyword);
    assertTrue(meta != null);
    assertEquals("compound", meta); // lower cased

    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginLine));
    assertEquals(2, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndLine));
    assertEquals(6, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginColumn));
    assertEquals(13, ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndColumn));
  }

  @Test(expected=TokenMgrError.class)
  public void testPOFKeywordMetadata3()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s Compound < \n");
    qb.append(" } ");

    SyntaxTreeBuilder.parseQuery(qb.toString());
  }

  /**
   * The POF is on the qname ns
   * @throws Exception
   */
  @Test
  public void testPOFQName2()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s foaf:< \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(1, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    final Object meta = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Qname);
    assertTrue(meta != null);
    assertEquals("foaf", meta);
  }

  /**
   * The POF is one the object
   * @throws Exception
   */
  @Test
  public void testPOFQName3()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s foaf: < \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(0, fillFinder.nodes.size());
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    assertTrue(((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Qname) == null);
  }

  @Test
  public void testPOFWithPrefixMetadata()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s <http://acme.org/foaf< \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(1, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertTrue(pofFinder.nodes.get(0) instanceof ASTVar);
    final Object meta = ((SimpleNode) pofFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.Prefix);
    assertTrue(meta != null);
    assertEquals("<http://acme.org/foaf", meta);
  }

  /**
   * There cannot be a space between the POF and the associated Prefix
   * @throws Exception
   */
  @Test(expected=ParseException.class)
  public void testPOFWithPrefixMetadata2()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s <http://acme.org/foaf < \n");
    qb.append(" } ");

    SyntaxTreeBuilder.parseQuery(qb.toString());
  }

  @Test
  public void testPOFPropertyPath()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("  ?s <knows> / < \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(1, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
  }

  @Test
  public void testMissingObjectMaterialization()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("graph ?g { ?s < }\n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(1, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
  }

  @Test
  public void testMissingMultipleObjectsMaterialization()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("graph ?g { ?s < } ?a < \n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder fillFinder = new NodeFinder(SyntaxTreeBuilder.FillVar);
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);
    fillFinder.visit(qc, null);

    assertEquals(2, fillFinder.nodes.size());
    assertTrue(checkParent(fillFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
    assertEquals(2, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
  }

  @Test
  public void testTriplePatternPOFMaterialization()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("?s < ?o .\n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);

    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));
  }

  @Test
  public void testClassAttributePosition()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("?s a < .\n");
    qb.append(" } ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    final NodeFinder aFinder = new NodeFinder(RDF.TYPE.toString());
    pofFinder.visit(qc, null);
    aFinder.visit(qc, null);

    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTTriplesSameSubjectPath.class));

    assertEquals(1, aFinder.nodes.size());
    assertEquals(2, ((SimpleNode) aFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginLine));
    assertEquals(2, ((SimpleNode) aFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndLine));
    assertEquals(4, ((SimpleNode) aFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.BeginColumn));
    assertEquals(4, ((SimpleNode) aFinder.nodes.get(0)).getMetadata(SyntaxTreeBuilder.EndColumn));
  }

  @Test
  public void testGraphPOFMaterialization()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("GRAPH < { \n");
    qb.append("?s ?p ?o .\n");
    qb.append(" }} ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);

    assertEquals(1, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTGraphGraphPattern.class));
  }

  @Test
  public void testMultiplePOFMaterialization()
  throws Exception {
    StringBuilder qb = new StringBuilder();
    qb.append("SELECT * \n");
    qb.append("WHERE { \n");
    qb.append("GRAPH < { \n");
    qb.append("?s ?p < .\n");
    qb.append(" }} ");

    final ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
    final NodeFinder pofFinder = new NodeFinder(SyntaxTreeBuilder.PointOfFocus);
    pofFinder.visit(qc, null);

    assertEquals(2, pofFinder.nodes.size());
    assertTrue(checkParent(pofFinder.nodes.get(0), ASTGraphGraphPattern.class));
    assertTrue(checkParent(pofFinder.nodes.get(1), ASTTriplesSameSubjectPath.class));
  }

  private boolean checkParent(Node n, Class<? extends SimpleNode> expectedParent) {
    while (!(n instanceof ASTWhereClause)) {
      if (n.getClass() == expectedParent) {
        return true;
      }
      n = n.jjtGetParent();
    }
    return false;
  }

  private class NodeFinder extends ASTVisitorBase {

    private final List<Node> nodes = new ArrayList<Node>();
    private final String name;

    public NodeFinder(String name) {
      this.name = name;
    }

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      if (node.getName().equals("?" + name)) {
        nodes.add(node);
      }
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTIRI node, Object data)
    throws VisitorException {
      if (node.getValue().equals(name)) {
        nodes.add(node);
      }
      return super.visit(node, data);
    }

  }

}
