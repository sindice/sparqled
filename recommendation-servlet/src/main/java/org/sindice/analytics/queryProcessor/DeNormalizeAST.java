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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.BlankNodeVarProcessor;
import org.openrdf.sindice.query.parser.sparql.PrefixDeclProcessor;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.sindice.query.parser.sparql.ast.ASTOptionalGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * Denormalize the AST into triple patterns.
 * Expands QNames into a full URI
 */
public final class DeNormalizeAST {

  private DeNormalizeAST() {
  }

  public static void process(ASTQueryContainer ast)
  throws MalformedQueryException, VisitorException {
    final DeNormalizeASTVisitor deNorm = new DeNormalizeASTVisitor();

    PrefixDeclProcessor.process(ast);
    BlankNodeVarProcessor.process(ast);
    deNorm.visit(ast, null);
  }

  private static class DeNormalizeASTVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTGraphPatternGroup node, Object data)
    throws VisitorException {
      process(node);
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      if (node.jjtGetNumChildren() == 2) {
        process(node);
      }
      return super.visit(node, data);
    }

    /*
     * Special case of the OPTIONAL: because of the grammar, an optional graph pattern
     * is not built based on the GraphPatternGroup. See sparql.jjt.
     */
    @Override
    public Object visit(ASTOptionalGraphPattern node, Object data)
    throws VisitorException {
      process(node);
      return super.visit(node, data);
    }

    private void process(SimpleNode node)
    throws VisitorException {
      for (ASTBasicGraphPattern bgp : node.jjtGetChildren(ASTBasicGraphPattern.class)) {
        for (ASTTriplesSameSubjectPath tp : bgp.jjtGetChildren(ASTTriplesSameSubjectPath.class)) {
          // TriplesNode
          for (Node n : DeNormalizeTriplesNode.process(tp)) {
            bgp.jjtAppendChild(n);
          }
          // PropertyPath
          for (Node n : DeNormalizeTriplesPaths.process(tp)) {
            if (n instanceof ASTTriplesSameSubjectPath) {
              bgp.jjtAppendChild(n);
            } else { // GraphPatternNotTriples
              node.jjtAppendChild(n);
            }
          }
          bgp.removeChild(tp);
        }
      }
      // remove empty BGPs
      for (ASTBasicGraphPattern bgp : node.jjtGetChildren(ASTBasicGraphPattern.class)) {
        if (bgp.jjtGetNumChildren() == 0) { // all TP of that BGP were removed
          node.removeChild(bgp);
        }
      }
    }

  }

}
