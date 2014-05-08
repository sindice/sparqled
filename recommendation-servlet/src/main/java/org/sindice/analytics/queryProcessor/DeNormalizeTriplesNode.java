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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.BlankNodeVarProcessor;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * Transforms collections and blank node property lists
 * into basic triple patterns
 */
public final class DeNormalizeTriplesNode {

  private DeNormalizeTriplesNode() {
  }

  /**
   * Only valid after this AST called {@link BlankNodeVarProcessor}
   * @param node
   * @return
   * @throws VisitorException
   */
  public static List<ASTTriplesSameSubjectPath> process(ASTTriplesSameSubjectPath node)
  throws VisitorException {
    final List<ASTTriplesSameSubjectPath> group = new ArrayList<ASTTriplesSameSubjectPath>();

    // expand BlankNodePropertyList
    node.jjtAccept(new ExpandBlankNodePropertyListVisitor(), group);
    // replace BlankNodePropertyList with variables
    node.jjtAccept(new ReplaceBlankNodePropertyListVisitor(), null);
    // TODO: expand collections
    return group;
  }

  /**
   * Create the triples patterns of a {@link ASTBlankNodePropertyList}
   */
  private static class ExpandBlankNodePropertyListVisitor extends ASTVisitorBase {

    private ASTVar anonSubject;

    @Override
    public Object visit(ASTBlankNodePropertyList node, Object data)
    throws VisitorException {
      anonSubject = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
      anonSubject.setName(node.getVarName());
      anonSubject.setAnonymous(true);
      data = super.visit(node, data);
      anonSubject = null;
      return data;
    }

    @Override
    public Object visit(ASTPropertyListPath node, Object data)
    throws VisitorException {
      if (anonSubject != null) { // Check if this ASTPropertyListPath node is a descendant of ASTBlankNodePropertyList
        final List<Node> group = (List<Node>) data;
        final ASTObjectList objList = node.getObjectList();

        for (int i = 0; i < objList.jjtGetNumChildren(); i++) {
          if (objList.jjtGetChild(i) instanceof ASTBlankNodePropertyList) {
            final ASTBlankNodePropertyList bn = (ASTBlankNodePropertyList) objList.jjtGetChild(i);
            final ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
            var.setName(bn.getVarName());
            var.setAnonymous(true);
            group.add(ASTProcessorUtil.createTriple(anonSubject, node.getVerb(), var));
          } else {
            group.add(ASTProcessorUtil.createTriple(anonSubject, node.getVerb(), objList.jjtGetChild(i)));
          }
        }
      }
      return super.visit(node, data);
    }

  }

  private static class ReplaceBlankNodePropertyListVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTBlankNodePropertyList node, Object data)
    throws VisitorException {
      final ASTVar anonSubject = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
      anonSubject.setName(node.getVarName());
      anonSubject.setAnonymous(true);
      node.jjtReplaceWith(anonSubject);
      return super.visit(node, data);
    }

  }

}
