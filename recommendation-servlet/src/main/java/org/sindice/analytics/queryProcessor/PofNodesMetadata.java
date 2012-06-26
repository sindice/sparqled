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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathAlternative;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPrefixDecl;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQName;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;

public final class PofNodesMetadata {

  private PofNodesMetadata() {
  }

  /**
   * Retrieve the POF metadata, gathered at the AST creation time.
   * To be run after denormalizing the ast
   * @param ast
   * @return
   * @throws VisitorException 
   * @throws MalformedQueryException 
   */
  public static POFMetadata retrieve(ASTQueryContainer ast)
  throws VisitorException, MalformedQueryException {
    final POFMetadata meta = new POFMetadata();
    RetrievePofASTMetadata retrieve = new RetrievePofASTMetadata();

    List<ASTPrefixDecl> prefixDeclList = ast.getPrefixDeclList();

    // Build a prefix --> IRI map
    final Map<String, String> prefixes = new LinkedHashMap<String, String>();
    for (ASTPrefixDecl prefixDecl : prefixDeclList) {
      String prefix = prefixDecl.getPrefix();
      String iri = prefixDecl.getIRI().getValue();

      prefixes.put(prefix, iri);
    }

    retrieve.visit(ast, prefixes);
    meta.pofNode = retrieve.pofNode;
    meta.pofClassAttribute = retrieve.pofClassAttribute;
    return meta;
  }

  private static class RetrievePofASTMetadata extends ASTVisitorBase {

    private final String pof = "?" + SyntaxTreeBuilder.PointOfFocus;
    private SimpleNode pofNode;
    private SimpleNode pofClassAttribute;

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      if (node.getName().equals(pof)) {
        // POF node metadata
        pofNode = node;
        if (pofNode != null && pofNode.getMetadata().containsKey(SyntaxTreeBuilder.Qname)) {
          Map<String, String> prefixes = (Map<String, String>) data;
          if (prefixes != null) {
            final ArrayList<Object> array = pofNode.getMetadata(SyntaxTreeBuilder.Qname);
            final ArrayList<Object> newArray = new ArrayList<Object>();

            for (Object o : array) {
              String prefix = (String) o;
              if (prefixes.containsKey(prefix)) {
                newArray.add(prefixes.get(prefix));
              } else {
                newArray.add(prefix);
              }
            }
            pofNode.getMetadata().put(SyntaxTreeBuilder.Qname, newArray);
          }
        }

        /*
         * Class attribute of the POF
         */
        // propertylistpath
        if (node.jjtGetParent() instanceof ASTObjectList &&
            node.jjtGetParent().jjtGetParent() instanceof ASTPropertyListPath &&
            node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTPathAlternative) {
          ASTPathAlternative pa = (ASTPathAlternative) node.jjtGetParent().jjtGetParent().jjtGetChild(0);
          if (pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) instanceof ASTIRI) {
            ASTIRI verb = (ASTIRI) pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            pofClassAttribute = verb;
          } else if (pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) instanceof ASTQName) {
            ASTQName verb = (ASTQName) pa.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
            pofClassAttribute = verb;
          }
        }
        // property list (bnode collection)
        if (node.jjtGetParent() instanceof ASTObjectList &&
            node.jjtGetParent().jjtGetParent() instanceof ASTPropertyList) {
          if (node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTIRI) {
            ASTIRI verb = (ASTIRI) node.jjtGetParent().jjtGetParent().jjtGetChild(0);
            pofClassAttribute = verb;
          } else if (node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTQName) {
            ASTQName verb = (ASTQName) node.jjtGetParent().jjtGetParent().jjtGetChild(0);
            pofClassAttribute = verb;
          }
        }
      }
      return super.visit(node, data);
    }

  }

}
