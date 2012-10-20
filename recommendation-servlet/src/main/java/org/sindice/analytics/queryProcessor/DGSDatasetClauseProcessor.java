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
 * @author Campinas Stephane [ 20 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.List;

import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.DatasetDeclProcessor;
import org.openrdf.sindice.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.sindice.query.parser.sparql.ast.ASTIRI;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;

/**
 * Set the analytics graph name in the DGS query
 * @author Stephane Campinas [17 Jun 2012]
 * @email stephane.campinas@deri.org
 *
 */
public final class DGSDatasetClauseProcessor {

  private DGSDatasetClauseProcessor() {
  }

  public static Dataset process(ASTQueryContainer ast)
  throws MalformedQueryException, VisitorException {
    final Dataset datasets = DatasetDeclProcessor.process(ast);
    final DGSDatasetClauseVisitor d = new DGSDatasetClauseVisitor();
    d.visit(ast, null);
    return datasets;
  }

  private static class DGSDatasetClauseVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTSelectQuery node, Object data)
    throws VisitorException {
      final List<ASTDatasetClause> datasets = node.jjtGetChildren(ASTDatasetClause.class);

      final ASTDatasetClause dDGS = new ASTDatasetClause(SyntaxTreeBuilderTreeConstants.JJTDATASETCLAUSE);
      final ASTIRI dgsGraph = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);
      dDGS.jjtAppendChild(dgsGraph);
      dgsGraph.setValue(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
      if (datasets.isEmpty()) {
        node.jjtInsertChild(dDGS, 1);
      } else {
        node.jjtReplaceChild(datasets.get(0), dDGS);
        for (int i = 1; i < datasets.size(); i++) {
          node.removeChild(datasets.get(i));
        }
      }
      return super.visit(node, data);
    }

  }

}
