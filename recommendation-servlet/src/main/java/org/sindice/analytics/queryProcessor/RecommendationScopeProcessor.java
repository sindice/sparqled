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
/**
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 28 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.Node;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.util.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reduce the recommended query down to its effective scope
 * @author Stephane Campinas [17 Jun 2012]
 * @email stephane.campinas@deri.org
 *
 */
public final class RecommendationScopeProcessor {

  private static final Logger logger = LoggerFactory.getLogger(RecommendationScopeProcessor.class);

  private RecommendationScopeProcessor() {
  }

  public static void process(ASTQueryContainer ast)
  throws VisitorException {
    final ScopedVariables scopedVariables = new ScopedVariables();
    final POFConnected scope = new POFConnected();

    // Initialize the seed
    scopedVariables.newConnectedVars.add(Hash.get(SyntaxTreeBuilder.PointOfFocus));
    // Define the recommendation scope
    do {
      scopedVariables.updateVars();
      scope.visit(ast, scopedVariables);
    } while (!scopedVariables.newConnectedVars.isEmpty());
    // Prune disconnected parts of the AST
    pruneToScope(ast);
  }

  private static class ScopedVariables {

    final Set<Integer> connectedVars    = new HashSet<Integer>(); // The seeds of variables to look for
    final Set<Integer> newConnectedVars = new HashSet<Integer>(); // variables connected to the seeds
    final Set<Integer> flagedVars       = new HashSet<Integer>(); // all variables seen so far

    public void updateVars() {
      flagedVars.addAll(newConnectedVars);
      flagedVars.addAll(connectedVars);
      connectedVars.clear();
      connectedVars.addAll(newConnectedVars);
      newConnectedVars.clear();
    }
  }

  private static class POFConnected extends ASTVisitorBase {

    // recommendation on the GRAPH name
    @Override
    public Object visit(ASTGraphGraphPattern node, Object data)
    throws VisitorException {
      final ScopedVariables sc = (ScopedVariables) data;

      // content is expected to have been removed/changed to variables
      if (node.jjtGetChild(0) instanceof ASTVar) {
        final int graphVar = Hash.get(((ASTVar) node.jjtGetChild(0)).getName());
        for (int hc : sc.connectedVars) {
          if (graphVar == hc) {
            if (!sc.flagedVars.contains(graphVar)) {
              sc.newConnectedVars.add(graphVar);
            }
            updateScope(node);
            if (node.jjtGetNumChildren() == 2) {
              /*
               * Special case of the GRAPH: all its variables have to be part of the next seed iteration
               */
              processGraphVariables(node.jjtGetChild(1), sc.newConnectedVars);
            }
          }
        }
      }
      return super.visit(node, data);
    }

    private void processGraphVariables(Node node, Set<Integer> newConnectedVars) {
      if (node instanceof ASTVar) {
        final int graphNestedVar = Hash.get(((ASTVar) node).getName());
        newConnectedVars.add(graphNestedVar);
      }
      for (int i = 0; i < node.jjtGetNumChildren(); i++) {
        processGraphVariables(node.jjtGetChild(i), newConnectedVars);
      }
    }

    // recommendation on the triple pattern
    @Override
    public Object visit(ASTTriplesSameSubjectPath node, Object data)
    throws VisitorException {
      final ScopedVariables sc = (ScopedVariables) data;

      // content is expected to have been removed/changed to variables
      final int subjectVar = Hash.get(((ASTVar) node.jjtGetChild(0)).getName());
      final int objectVar;
      final Integer predicateVar;

      final Node o = node.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0);
      if (o instanceof ASTVar) {
        objectVar = Hash.get(((ASTVar) o).getName());
      } else if (o instanceof ASTIRI) { // the object is a class URI
        objectVar = -1;
      } else if (o instanceof ASTRDFLiteral) { // the object is a class LITERAL
        objectVar = -1;
      } else {
        logger.error("The triple={} has a wrong object type, expected Var or IRI: got {}", node, o);
        return super.visit(node, data);
      }

      if (node.jjtGetChild(1).jjtGetChild(0) instanceof ASTVar) {
        predicateVar = Hash.get(((ASTVar) node.jjtGetChild(1).jjtGetChild(0)).getName());
      } else {
        predicateVar = null;
      }

      for (int hc : sc.connectedVars) {
//        if ((subjectVar == hc && (objectVar == -1 || !sc.flagedVars.contains(objectVar))) ||
//            (predicateVar != null && predicateVar == hc && !sc.flagedVars.contains(objectVar))) {
//          if (objectVar != -1) {
//            sc.newConnectedVars.add(objectVar);
//          }
//          updateScope(node);
//        }
//        if (objectVar == hc && !sc.flagedVars.contains(subjectVar) ||
//            predicateVar != null && predicateVar == hc && !sc.flagedVars.contains(subjectVar)) {
//          sc.newConnectedVars.add(subjectVar);
//          updateScope(node);
//        }
        /*
         * the object variable is connected. In case where the seed variable
         * is the predicate, add the object.
         */
        if (subjectVar == hc || (predicateVar != null && predicateVar == hc)) {
          if (objectVar != -1 && !sc.flagedVars.contains(objectVar)) {
            sc.newConnectedVars.add(objectVar);
          }
          updateScope(node);
        }
        /*
         * the subject variable is connected. In case where the seed variable
         * is the predicate, add the subject.
         */
        if (objectVar == hc  || (predicateVar != null && predicateVar == hc)) {
          if (!sc.flagedVars.contains(subjectVar)) {
            sc.newConnectedVars.add(subjectVar);
          }
          updateScope(node);
        }
      }
      return super.visit(node, data);
    }

    private void updateScope(SimpleNode node) {
      updateChildrenScope(node);
      do {
        node.setWithinRecommendationScope();
        // Visit the children of the Select
        if (node instanceof ASTSelectQuery) {
          // Keep the select
          updateChildrenScope(((ASTSelectQuery) node).getSelect());
          // Keep the Dataset clauses
          for (ASTDatasetClause d: ((ASTSelectQuery) node).getDatasetClauseList()) {
            updateChildrenScope(d);
          }
        } else if (node instanceof ASTGraphGraphPattern) {
          updateChildrenScope((SimpleNode) node.jjtGetChild(0));
        }
        node = (SimpleNode) node.jjtGetParent();
      } while (node != null);
    }

    private void updateChildrenScope(SimpleNode node) {
      if (node == null) {
        return;
      }

      node.setWithinRecommendationScope();
      for (Node child : node.jjtGetChildren()) {
        ((SimpleNode) child).setWithinRecommendationScope();
        updateChildrenScope((SimpleNode) child);
      }
    }

  }

  private static void pruneToScope(SimpleNode node) {
    final ArrayList<Node> toRemove = new ArrayList<Node>();

    for (Node child : node.jjtGetChildren()) {
      if (!((SimpleNode) child).isWithinRecommendationScope()) {
        toRemove.add(child);
      } else {
        pruneToScope((SimpleNode) child);
      }
    }
    node.removeChildren(toRemove);
  }

}
