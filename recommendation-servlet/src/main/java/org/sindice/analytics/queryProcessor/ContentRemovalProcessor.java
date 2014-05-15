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
import org.openrdf.sindice.query.parser.sparql.ast.ASTFalse;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTNumericLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathAlternative;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTrue;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.util.Hash;

/**
 * Replace any content information by wildcard variables
 */
public final class ContentRemovalProcessor {

  private ContentRemovalProcessor() {
  }

  public static void process(ASTQueryContainer ast)
  throws VisitorException {
    final ContentRemovalVisitor c = new ContentRemovalVisitor();
    c.visit(ast, null);
  }

  private static class ContentRemovalVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTTriplesSameSubjectPath node, Object data)
    throws VisitorException {
      if (!(node.jjtGetChild(0) instanceof ASTVar)) {
        final SimpleNode subject = (SimpleNode) node.jjtGetChild(0);
        if (subject instanceof ASTIRI) {
          replaceIRI(subject, Hash.getLong(((ASTIRI) subject).getValue()));
        } else {
          replace(subject);
        }
      }
      final Node verb = node.jjtGetChild(1).jjtGetChild(0);
      final SimpleNode object = (SimpleNode) node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
      if (!(verb instanceof ASTVar)) { // replace object if not ClassTriplePattern
        final ASTIRI verbIRI = (ASTIRI) verb.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        if (!AnalyticsClassAttributes.isClass(verbIRI.getValue())) {
          if (object instanceof ASTIRI) {
            replaceIRI(object, Hash.getLong(((ASTIRI) object).getValue()));
          } else if (!(object instanceof ASTVar)) {
            replace(object);
          }
        }
      } else if (!(object instanceof ASTVar)) { // verb is variable, the content can be removed
        if (object instanceof ASTIRI) {
          replaceIRI(object, Hash.getLong(((ASTIRI) object).getValue()));
        } else if (!(object instanceof ASTVar)) {
          replace(object);
        }
      }
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTNumericLiteral node, Object data)
    throws VisitorException {
      replace(node);
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTRDFLiteral node, Object data)
    throws VisitorException {
      final Node parent = node.jjtGetParent().jjtGetParent();
      if (parent instanceof ASTPropertyListPath &&
          ((ASTPropertyListPath) parent).getVerb() instanceof ASTPathAlternative) {
        final ASTPathAlternative pa = (ASTPathAlternative) ((ASTPropertyListPath) parent).getVerb();
        if (pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) instanceof ASTIRI) {
          final ASTIRI verb = ((ASTIRI) pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0));
          if (AnalyticsClassAttributes.isClass(verb.getValue())) {
            return super.visit(node, data);
          }
        }
      } else if (parent instanceof ASTPropertyList && parent.jjtGetChild(0) instanceof ASTIRI &&
                 AnalyticsClassAttributes.isClass(((ASTIRI) parent.jjtGetChild(0)).getValue())) {
        return super.visit(node, data);
      }
      replace(node);
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTTrue node, Object data)
    throws VisitorException {
      replace(node);
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFalse node, Object data)
    throws VisitorException {
      replace(node);
      return super.visit(node, data);
    }

    private void replace(SimpleNode node) {
      node.jjtReplaceWith(SparqlVarGenerator.getASTVar("content"));
    }

    private void replaceIRI(SimpleNode node, long valueHash) {
      final ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
      final String varName = "IRI" + Long.toString(valueHash).replace('-', 'n');

      var.setName(varName);
      SparqlVarGenerator.addVar(varName);
      node.jjtReplaceWith(var);
    }

  }

}
