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
package org.sindice.servlet.sparqlqueryservlet.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.query.parser.sparql.ASTVisitorBase;
import org.openrdf.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.Node;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.sparql.ast.VisitorException;

public class SindicePreProcessing
implements PreProcessing {

  private final static String PREFIX = "GRAPH";
  private final static String SUFFIX = "END";

  public final static ASTIRI isPartOf = new ASTIRI(SyntaxTreeBuilderTreeConstants.JJTIRI);

  @Override
  public void init(String... args) {
    if (args == null || args.length != 1) {
      throw new IllegalArgumentException("Missing SindicePreProcessing argument");
    }
    isPartOf.setValue(args[0]);
  };

  @Override
  public String getVarPrefix() {
    return PREFIX;
  }

  @Override
  public String getVarSuffix() {
    return SUFFIX;
  }

  @Override
  public String process(String query)
  throws Exception {
    final ASTQueryContainer ast = SyntaxTreeBuilder.parseQuery(query);

    // Dataset clauses are not allowed
    if (ast.getQuery().getDatasetClauseList().size() != 0) {
      throw new IllegalArgumentException("FROM and FROM NAMED clauses are not allowed. Use GRAPH instead.");
    }
    // Extract entities into their separate graph
    // Add the triple describing the dataset
    ASTVarGenerator.reset();
    final AddDatasetStatement addDSst = new AddDatasetStatement();
    addDSst.visit(ast, null);
    // Translate query back to string
    return AST2TextTranslator.translate(ast);
  }

  private class AddDatasetStatement extends ASTVisitorBase {

    @Override
    public Object visit(ASTGraphPatternGroup node, Object data)
    throws VisitorException {
      final List<ASTGraphGraphPattern> graphs = node.jjtGetChildren(ASTGraphGraphPattern.class);

      for (ASTGraphGraphPattern graph : graphs) { // for each graph in the group
        final Node graphName = graph.jjtGetChild(0);

        final ASTVar graphVar = ASTVarGenerator.getASTVar(PREFIX, SUFFIX);
        final ASTTriplesSameSubjectPath ds = ASTProcessorUtil.createTriple(graphVar, isPartOf, graphName);
        node.jjtAppendChild(ds);
        graph.jjtReplaceChild(graphName, graphVar);
      }
      return super.visit(node, data);
    }

  }

  private class EntityGraph extends ASTVisitorBase {

    @Override
    public Object visit(ASTGraphPatternGroup node, Object data)
    throws VisitorException {
      for (Node child : node.jjtGetChildren()) {
        if (child instanceof ASTGraphGraphPattern) {
          final HashMap<String, ArrayList<Node>> entities = new HashMap<String, ArrayList<Node>>();
          super.visit(node, entities);
          if (entities.size() > 1) {
            
          }
        }
      }
      return data;
    }

    @Override
    public Object visit(ASTTriplesSameSubjectPath node, Object data)
    throws VisitorException {
      if (data != null) {
        final HashMap<String, ArrayList<Node>> entities = (HashMap<String, ArrayList<Node>>) data;
        final Node e = node.jjtGetChild(0);
        final String entity;

        if (e instanceof ASTIRI) {
          entity = ((ASTIRI) e).getValue();
        } else if (e instanceof ASTVar) {
          entity = ((ASTVar) e).getName();
        } else {
          throw new IllegalStateException("Found a subject which is neither a URI nor a Var: " + e);
        }
        if (!entities.containsKey(entity)) {
          entities.put(entity, new ArrayList<Node>());
        }
        entities.get(entity).add(node);
      }
      return super.visit(node, data);
    }

  }

}
