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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;

/**
 * 
 */
public final class SparqlToDGSQuery {

  private RecommendationType  type;
  private POFMetadata         meta;
  private RecommendationQuery rq;

  public void process(ASTQueryContainer ast)
  throws MalformedQueryException, VisitorException {
    meta = null;
    rq = null;
    type = RecommendationType.NONE;
    if (!ast.containsQuery()) {
      return;
    }

    SparqlVarGenerator.reset();
    // Retrieve the POF metadata
    meta = PofNodesMetadata.retrieve(ast);
    // Remove RDF tags
    RDFTagRemover.remove(ast);
    // expand each TP into simple one: denormalize syntax sugar constructions
    DeNormalizeAST.process(ast);

    // Ensure the query is valid for recommendation
    ValidateQ4Recommendations.process(ast);

    SparqlVarGenerator.addVars(ASTVarProcessor.process(ast));
    /*
     * Map SPARQL query to a Data Graph Summary query
     */
    // 1. Materialize the POF
    type = PointOfFocusProcessor.process(ast);
    // 2. Remove Content Elements
    ContentRemovalProcessor.process(ast);
    // Define Recommendation Scope
    RecommendationScopeProcessor.process(ast);
    // TODO: Optimize the query by removing unnecessary parts, e.g., optional, unions
    // 3. Map to the DGS query
    rq = SparqlTranslationProcessor.process(meta, ast);
  }

  public RecommendationType getRecommendationType() {
    return type;
  }

  public POFMetadata getPOFMetadata() {
    return meta;
  }

  public RecommendationQuery getRecommendationQuery() {
    return rq;
  }

}
