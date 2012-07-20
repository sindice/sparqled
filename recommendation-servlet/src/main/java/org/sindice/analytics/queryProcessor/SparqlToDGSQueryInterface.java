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

import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ParseException;
import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.sindice.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;

/**
 * @author bibhas [Jul 12, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public interface SparqlToDGSQueryInterface {
  public static final String POF_RESOURCE = "POFresource";
  public static final String CARDINALITY_VAR = SyntaxTreeBuilder.PointOfFocus
      + "cardinality";
  public static final String CLASS_ATTRIBUTE_CARD_VAR = "CAcardinality";
  public static final String CLASS_ATTRIBUTE_LABEL_VAR = "CAlabel";

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
  public void load(String query) throws Exception;

  /**
   * Parse a query, perform the mapping to a Data Graph Summary query and reduce
   * its scope.
   * 
   * @param query
   * @param varsToProject
   *          The variables to project in the DataGraphSummary query (By
   *          Default, it is the POF)
   * @throws MalformedQueryException
   * @throws VisitorException
   * @throws TokenMgrError
   * @throws ParseException
   */
  public void load(String query, List<String> varsToProject) throws Exception;

  /**
   * Return the Data Graph Summary query from the one passed in
   * {@link AbstractQueryProcessor#load(String)}. Only valid after the call to
   * load.
   * 
   * @return
   * @throws VisitorException
   */
  public String getDGSQuery() throws DGSException;

  /**
   * returns a list of metadata for the given field, associated to the POF while
   * building the AST.
   * 
   * @return
   */
  public POFMetadata getPofASTMetadata();

  /**
   * Returns a query for getting the set of properties from the specified domain
   * 
   * @param domain
   *          if empty string, returns all the properties available
   * @param limit
   *          limit the response to the first "limit" solutions
   * @return
   * @throws DGSException
   */
  public String getPropertiesQuery(String domain, int limit)
      throws DGSException;

  /**
   * Returns a query for getting the set of classes from the specified domain
   * 
   * @param domain
   *          if empty string, returns all the classes available
   * @param limit
   *          limit the response to the first "limit" solutions
   * @return
   * @throws DGSException
   */
  public String getClassesQuery(String domain, int limit) throws DGSException;

  /**
   * Returns a query for getting the set of domains
   * 
   * @param domain
   *          if empty string, returns all the domains available
   * @param limit
   *          limit the response to the first "limit" solutions
   * @return
   * @throws DGSException
   */
  public String getDomainsQuery(String domain, int limit) throws DGSException;

  public RecommendationType getRecommendationType();
}
