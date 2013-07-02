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

import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * 
 */
public interface QueryProcessor {

  public static final String POF_RESOURCE              = "POFresource";
  public static final String CARDINALITY_VAR           = SyntaxTreeBuilder.PointOfFocus + "cardinality";
  public static final String CLASS_ATTRIBUTE_CARD_VAR  = "CAcardinality";
  public static final String CLASS_ATTRIBUTE_LABEL_VAR = "CAlabel";

  public static enum RecommendationType {
    NONE, PREDICATE, CLASS, GRAPH
  }

  public class POFMetadata {
    // the POF ast node
    public SimpleNode pofNode;
    /*
     * The class attribute of the POF, in case of CLASS recommendation
     */
    public SimpleNode pofClassAttribute;
  }

  /**
   * Parse a query, perform the mapping to a Data Graph Summary query and reduce
   * its scope.
   * 
   * @param query
   * @throws VisitorException
   * @throws MalformedQueryException
   * @throws ParseException
   * @throws VisitorException
   * @throws MalformedQueryException
   * @throws ParseException
   * @throws TokenMgrError
   */
  public void load(String query)
  throws DGSException;

  /**
   * Parse a query, perform the mapping to a Data Graph Summary query and reduce
   * its scope.
   * 
   * @param query
   * @param varsToProject The variables to project in the DataGraphSummary query (By Default, it is the POF)
   * @throws MalformedQueryException
   * @throws VisitorException
   * @throws TokenMgrError
   * @throws ParseException
   */
  public void load(String query, List<String> varsToProject)
  throws DGSException;

  /**
   * Return the Data Graph Summary query from the one passed in {@link AbstractQueryProcessor#load(String)}.
   * Only valid after the call to load.
   * @return
   * @throws VisitorException 
   */
  public String getDGSQuery(ASTConstraint... contraints)
  throws DGSException;

  /**
   * Return the Data Graph Summary query from the one passed in {@link AbstractQueryProcessor#load(String)}.
   * Only valid after the call to load. Add a limit clause to the query.
   * @return
   * @throws VisitorException 
   */
  public String getDGSQueryWithLimit(int limit, ASTConstraint... contraints)
  throws DGSException;

  /**
   * returns a list of metadata for the given field,
   * associated to the POF while building the AST.
   * @return
   */
  public POFMetadata getPofASTMetadata();

  public RecommendationType getRecommendationType();

}
