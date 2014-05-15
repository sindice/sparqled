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

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathAlternative;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathElt;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathSequence;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

public final class ValidateQ4Recommendations {

  private ValidateQ4Recommendations() {
  }

  public static void process(ASTQueryContainer ast)
  throws VisitorException {
    final ASTValidateAST validate = new ASTValidateAST();
    if ((Integer) validate.visit(ast, 0) == 0) {
      throw new DGSException("No Point Of Focus found");
    }
  }

  private static class ASTValidateAST extends ASTVisitorBase {

    private final static String msg = "A missing object can only appear with the predicate as the Point Of Focus";
    private int nPOF = 0;
    private int nFillVar = 0;

    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      if (node.jjtGetNumChildren() != 2) { // Incomplete graph name: POF
        ASTVar pof = node.jjtGetChild(ASTVar.class);
        if (pof == null || !pof.getName().equals("?" + SyntaxTreeBuilder.PointOfFocus)) {
          throw new DGSException("Incomplete GraphGraphPattern: if the graph pattern is missing, the graph name must be the POF");
        }
      }
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      if (node.getName().equals("?" + SyntaxTreeBuilder.PointOfFocus)) {
        if (++nPOF > 1) {
          throw new DGSException("There can be only one Point Of Focus");
        }
        data = nPOF;
      }
      if (node.getName().equals("?" + SyntaxTreeBuilder.FillVar)) {
        if (++nFillVar > 1) {
          throw new DGSException("There can be only one missing object. Check your triple patterns");
        }
        if (node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTPathAlternative) { // property list path
          final ASTPathSequence ps = (ASTPathSequence) node.jjtGetParent().jjtGetParent().jjtGetChild(0).jjtGetChild(0);
          boolean isCorrect = false;
          for (ASTPathElt elt : ps.getPathElements()) {
            if (elt.jjtGetChild(0) instanceof ASTVar) {
              final ASTVar var = (ASTVar) elt.jjtGetChild(0);
              if (var.getName().equals("?" + SyntaxTreeBuilder.PointOfFocus)) {
                isCorrect = true;
                break;
              }
            }
          }
          if (!isCorrect) {
            throw new DGSException(msg);
          }
        } else if (node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTVar) { // property list
          final ASTVar var = (ASTVar) node.jjtGetParent().jjtGetParent().jjtGetChild(0);
          if (!var.getName().equals("?" + SyntaxTreeBuilder.PointOfFocus)) {
            throw new DGSException(msg);
          }
        } else {
          throw new DGSException(msg);
        }
      }
      return data;
    }

  }

}
