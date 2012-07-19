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
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ASTVisitorBase;
import org.openrdf.sindice.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.ASTTriplesSameSubjectPath;
import org.openrdf.sindice.query.parser.sparql.ast.ASTVar;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.backend.DGSQueryResultProcessor.Context;
import org.sindice.analytics.ranking.Label;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module keeps on adding a property path between two nodes until either
 * the maximum limit is reached or a valid recommendation is obtained.
 * @author bibhas [Jul 9, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class AddOneHopPropertyPath implements BasicOperation {

  private static final Logger logger = LoggerFactory.getLogger(AddOneHopPropertyPath.class);

  private final SparqlTranslationProcessor sparql2dgs;
  private final Select2Ask selectToAsk = new Select2Ask();

  public AddOneHopPropertyPath(SparqlTranslationProcessor sparql2dgs) {
    this.sparql2dgs = sparql2dgs;
  }

  /* The type of object to be sent to the visitor method.
   * It stores the path length from one node to another(hops).
   * It also stores all the variables to be projected for recommendation (varsToProject).
   * */
  private class HopsVarsPair {
    int hops;
    final ArrayList<String> varsToProject = new ArrayList<String>();

    public void reset(int hops) {
      this.hops = hops;
      varsToProject.clear();
    }
  }

  public PipelineObject process(PipelineObject po)
  throws VisitorException, MalformedQueryException, ParseException, SesameBackendException {
    if (!po.getType().equals(RecommendationType.PREDICATE)) {
      return po;
    }

    String query = AST2TextTranslator.translate(po.getAst());
    final AddHopVisitor obj = new AddHopVisitor();
    int hops = 1;
    final HopsVarsPair hopsVars = new HopsVarsPair();
    while (hops <= po.getMAX_HOPS()) {
      po = sparql2dgs.process(po);
      
      /* convert the DGS query to an ASK query to check if recommendations
       * are obtained with the current property paths.
       */
      ASTQueryContainer ast = selectToAsk.convert(po.getAst());
      QueryIterator<Label, Context> qit = po.getBackend().submit(
          AST2TextTranslator.translate(ast));
      qit.hasNext();
      if (qit.next().getLabel().equals("true")) {
        logger.debug("Property Path Recommendation: Breaking out....");
        break;
      }
      if (++hops > po.getMAX_HOPS())
        break;
      hopsVars.reset(hops);
      po.setAst(SyntaxTreeBuilder.parseQuery(query));
      // add another hop to the existing query
      obj.visit(po.getAst(), hopsVars);
      query = AST2TextTranslator.translate(po.getAst());
      po.getVarsToProject().addAll(hopsVars.varsToProject);
    }
    po.setAst(SyntaxTreeBuilder.parseQuery(query));
    return po;
  }

  /*The visitor searches for the triple pattern containing the POF
   *and adds the requisite number of triples(as mentioned by hops)
   *after the found triple.
   * */
  public class AddHopVisitor extends ASTVisitorBase {
    public Object visit(ASTBasicGraphPattern node, Object data)
        throws VisitorException {
      final HopsVarsPair hopsVars = (HopsVarsPair) data;
      final int hops = hopsVars.hops;
      final ArrayList<String> set = hopsVars.varsToProject;
      boolean foundPOF = false;

      final ASTBasicGraphPattern bgpDGS = new ASTBasicGraphPattern(SyntaxTreeBuilderTreeConstants.JJTBASICGRAPHPATTERN);
      ASTTriplesSameSubjectPath triple = null;

      // find the ?POF variable in the triple pattern
      final List<ASTTriplesSameSubjectPath> tpList = node
          .jjtGetChildren(ASTTriplesSameSubjectPath.class);
      while (true) {
        triple = tpList.remove(0);
        if (triple.jjtGetChild(1).jjtGetChild(0) instanceof ASTVar) {
          if (((ASTVar) triple.jjtGetChild(1).jjtGetChild(0)).getName().equals(SyntaxTreeBuilder.PointOfFocus)) {
            foundPOF = true;
            break;
          }
        }
        bgpDGS.jjtAppendChild(triple);
      }

      // add hops number of triples after the triple containing POF
      if (foundPOF) {
        for (int i = 2; i < hops; i++) {
          bgpDGS.jjtAppendChild(triple);
          triple = tpList.remove(0);
        }
        final ASTVar targetNode = (ASTVar) triple.jjtGetChild(1).jjtGetChild(1)
            .jjtGetChild(0);

        // change the existing triple
        final ASTVar newObject = ASTVarGenerator.getASTVar("ob");
        set.add(newObject.getName());
        final ASTVar newPOF = ASTVarGenerator.getASTVar(SyntaxTreeBuilder.PointOfFocus);
        set.add(newPOF.getName());
        final ASTTriplesSameSubjectPath t1 = ASTProcessorUtil.createTriple(triple
            .jjtGetChild(0), triple.jjtGetChild(1).jjtGetChild(0), newObject);
        final ASTTriplesSameSubjectPath t2 = ASTProcessorUtil.createTriple(newObject,
            newPOF, targetNode);

        bgpDGS.jjtAppendChild(t1);
        bgpDGS.jjtAppendChild(t2);

        // add rest of the triples in the BGP
        for (ASTTriplesSameSubjectPath tp: tpList) {
          bgpDGS.jjtAppendChild(tp);
        }
        node.jjtReplaceWith(bgpDGS);
      }

      return super.visit(node, data);
    }
  }

}
