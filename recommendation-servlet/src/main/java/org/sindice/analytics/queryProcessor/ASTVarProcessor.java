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

import java.util.HashSet;
import java.util.Set;

import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * Returns a set of variables occurring in the ast
 * @author Stephane Campinas [17 Jun 2012]
 * @email stephane.campinas@deri.org
 *
 */
public final class ASTVarProcessor {

  private ASTVarProcessor() {
  }

  public static Set<String> process(ASTQueryContainer ast)
  throws VisitorException {
    final Set<String> vars = new HashSet<String>();

    final ASTVarProcessorVisitor v = new ASTVarProcessorVisitor();
    v.visit(ast, vars);
    return vars;
  }

  private static class ASTVarProcessorVisitor extends ASTVisitorBase {

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      final Set<String> vars = (Set<String>) data;
      vars.add(node.getName());
      return super.visit(node, data);
    }

  }

}
