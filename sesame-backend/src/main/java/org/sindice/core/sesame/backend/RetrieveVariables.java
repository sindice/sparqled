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
package org.sindice.core.sesame.backend;

import java.util.Set;

import org.openrdf.query.parser.sparql.ASTVisitorBase;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.VisitorException;

public final class RetrieveVariables {

  private RetrieveVariables() {
  }

  public static void process(ASTQueryContainer ast, Set<String> bindingNames)
  throws VisitorException {
    final RetrieveASTVar rav = new RetrieveASTVar();
    rav.visit(ast, bindingNames);
  }

  private static class RetrieveASTVar extends ASTVisitorBase {

    @Override
    public Object visit(ASTVar node, Object data)
    throws VisitorException {
      final Set<String> vars = (Set<String>) data;

      vars.add(node.getName());
      return super.visit(node, data);
    }

  }

}
