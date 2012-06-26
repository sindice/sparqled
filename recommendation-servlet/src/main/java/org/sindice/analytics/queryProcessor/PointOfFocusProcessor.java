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
 * @author Campinas Stephane [ 19 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.sindice.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.sindice.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPathElt;
import org.openrdf.sindice.query.parser.sparql.ast.ASTProjectionElem;
import org.openrdf.sindice.query.parser.sparql.ast.ASTPropertyListPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelect;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;

/**
 * - Check there is only one Point Of Focus (POF)
 * - Materialize the POF with a properly named ASTVar (without the '?' prefixed)
 * - Change if needed the query to be a Select Query
 * - Add the POF, and any other variables name passed in argument, to the SELECT clause
 * - Return the type of the recommendation, e.g., predicate or class or graph name.
 */
public final class PointOfFocusProcessor {

  private PointOfFocusProcessor() {
  }

  public static RecommendationType process(ASTQueryContainer ast, List<String> varsToProject)
  throws VisitorException {
    final List<String> v = new ArrayList<String>();

    if (varsToProject != null) {
      v.addAll(varsToProject);
    }
    if (!(ast.getQuery() instanceof ASTSelectQuery)) { // Change to a SelectQuery
      final ASTSelectQuery selectQuery = new ASTSelectQuery(SyntaxTreeBuilderTreeConstants.JJTSELECTQUERY);
      final ASTSelect select = new ASTSelect(SyntaxTreeBuilderTreeConstants.JJTSELECT);

      selectQuery.jjtAppendChild(select);
      for (ASTDatasetClause d : ast.getQuery().getDatasetClauseList()) {
        selectQuery.jjtAppendChild(d);
      }
      selectQuery.jjtAppendChild(ast.getQuery().getWhereClause());
      if (!(ast.getQuery() instanceof ASTAskQuery)) {
        if (ast.getQuery().getGroupClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getGroupClause());
        }
        if (ast.getQuery().getHavingClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getHavingClause());
        }
        if (ast.getQuery().getOrderClause() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getOrderClause());
        }
        if (ast.getQuery().getLimit() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getLimit());
        }
        if (ast.getQuery().getOffset() != null) {
          selectQuery.jjtAppendChild(ast.getQuery().getOffset());
        }
      }
      if (ast.getQuery().getBindingsClause() != null) {
        selectQuery.jjtAppendChild(ast.getQuery().getBindingsClause());
      }
      ast.getQuery().jjtReplaceWith(selectQuery);
    }
    final ASTMaterializePointOfFocus matPOF = new ASTMaterializePointOfFocus();
    matPOF.visit(ast, v);
    final POFRecType type = new POFRecType();
    return (RecommendationType) type.visit(ast, RecommendationType.NONE);
  }

  private static class POFRecType extends ASTVisitorBase {

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      if (node.getName().equals(SyntaxTreeBuilder.PointOfFocus)) { // Class recommendation
        if (node.jjtGetParent() instanceof ASTObjectList) {
          data = RecommendationType.CLASS;
        } else if (node.jjtGetParent() instanceof ASTPathElt ||
                   node.jjtGetParent() instanceof ASTPropertyListPath) { // Predicate Recommendation
          data = RecommendationType.PREDICATE;
        } else if (node.jjtGetParent() instanceof ASTGraphGraphPattern) { // Predicate Recommendation
          data = RecommendationType.GRAPH;
        } else if (!(node.jjtGetParent() instanceof ASTProjectionElem)) { // The POF is a solution of a select 
          throw new DGSException("Unsupported recommendation: the parent of the POF is " + node.jjtGetParent().toString());
        }
      }
      return super.visit(node, data);
    }

  }

  private static class ASTMaterializePointOfFocus extends ASTVisitorBase {

    @Override
    public Object visit(ASTSelect node, Object data)
    throws VisitorException {
      final List<String> varsToProject = (List<String>) data;
      final HashSet<String> hasPof = new HashSet<String>();

      final CheckSelectForPOF cpof = new CheckSelectForPOF();
      cpof.visit((ASTSelectQuery) node.jjtGetParent(), hasPof);
      if (hasPof.contains("?" + SyntaxTreeBuilder.PointOfFocus)) {
        node.removeChildren(ASTProjectionElem.class);
        node.setWildcard(false);
        if (!varsToProject.contains(SyntaxTreeBuilder.PointOfFocus)) {
          varsToProject.add(SyntaxTreeBuilder.PointOfFocus);
        }
        for (String varName : varsToProject) {
          final ASTProjectionElem p = new ASTProjectionElem(SyntaxTreeBuilderTreeConstants.JJTPROJECTIONELEM);
          final ASTVar var = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
          var.setName(varName);
          p.jjtAppendChild(var);
          node.jjtAppendChild(p);
        }
      }
      return super.visit(node, data);
    }

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      if (node.getName().equals("?" + SyntaxTreeBuilder.PointOfFocus)) {
        node.setName(SyntaxTreeBuilder.PointOfFocus);
        // if the POF is on the object, check that the predicate is a class attribute
        if ((node.jjtGetParent() instanceof ASTObjectList) &&
            !(node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTVar) &&
            !AnalyticsClassAttributes.isClass(((ASTIRI) node.jjtGetParent().jjtGetParent().jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).getValue())) {
          throw new DGSException("Recommendations on the object are only possible if the predicate is a class attribute");
        } else if ((node.jjtGetParent() instanceof ASTObjectList) &&
                   node.jjtGetParent().jjtGetParent().jjtGetChild(0) instanceof ASTVar) {
          throw new DGSException("Recommendations on the object are only possible if the predicate is a class attribute");
        }
      } else if (node.getName().equals("?" + SyntaxTreeBuilder.FillVar)) {
        node.setName(SyntaxTreeBuilder.FillVar);
      }
      return super.visit(node, data);
    }

  }

  private static class CheckSelectForPOF extends ASTVisitorBase {

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      ((HashSet<String>) data).add(node.getName());
      return super.visit(node, data);
    }

  }

}
