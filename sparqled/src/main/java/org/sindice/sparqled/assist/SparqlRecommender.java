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
package org.sindice.sparqled.assist;

import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.queryProcessor.DGSException;
import org.sindice.analytics.queryProcessor.DGSQueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.analytics.ranking.CardinalityRanking;
import org.sindice.analytics.ranking.Label;
import org.sindice.analytics.ranking.Label.LabelType;
import org.sindice.analytics.ranking.LabelsRanking;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * 
 */
public final class SparqlRecommender {

  private static final Logger logger  = LoggerFactory.getLogger(SparqlRecommender.class);

  private final Mustache template;

  public SparqlRecommender(String pathToTemplate) {
    if (pathToTemplate != null) {
      logger.info("Loading template at [{}]", pathToTemplate);
      MustacheFactory mf = new DefaultMustacheFactory();
      template = mf.compile(pathToTemplate);
    } else {
      logger.info("Loading default template");
      template = null;
    }
  }

  /**
   * Returns an object containing the list of recommendation for the given query
   * @param dgsBackend
   * @param query
   * @param rankings
   * @param pagination
   * @param limit
   * @return
   */
  public <C> C run(SesameBackend<Label> dgsBackend, ResponseWriter<C> response, String query, int pagination, int limit) {
    // TODO: Support queries with multiple FROM clauses
    RecommendationType recommendationType = RecommendationType.NONE;

    if (query == null || query.isEmpty()) {
      return response.createEmptyAnswer("The given query is null or empty...");
    }

    logger.debug("Input query: [{}]\n", query);
    final LabelsRanking lr = new CardinalityRanking();
    final POFMetadata meta;
    try {
      /*
       * Get the DataGraphSummary query
       */
      final QueryProcessor qp = new DGSQueryProcessor(template);
      qp.load(query);

      meta = qp.getPofASTMetadata();
      final String qname = (String) meta.pofNode.getMetadata(SyntaxTreeBuilder.Qname);
      recommendationType = qp.getRecommendationType();

      final String dgsQuery;
      if (limit == 0) {
        dgsQuery = qp.getDGSQuery();
      } else {
        dgsQuery = qp.getDGSQueryWithLimit(limit);
      }
      logger.debug("RecommendationType: {}\nDGS query: [{}]", recommendationType, dgsQuery);
      if (!recommendationType.equals(RecommendationType.NONE)) {
        /*
         * Get the list of candidates and rank them
         */
        final QueryIterator<Label> qrp = dgsBackend.submit(dgsQuery);
        qrp.setPagination(pagination);
        while (qrp.hasNext()) {
          final Label label = qrp.next();

          if (label == null) {
            logger.debug("No Recommendation for the label: {}", label);
            continue;
          }
          // QName filtering
          if (qname != null) {
            if (!label.getLabel().startsWith(qname)) {
              continue;
            }
            // recommend only the localname
            label.setType(LabelType.QNAME);
            label.setLabel(label.getLabel().replaceFirst(qname, ""));
          }
          logger.debug("Label=[{}]", label);
          lr.addLabel(label);
        }
        // Check if there are some recommendations
        if (lr.size() == 0) {
          return response.createEmptyAnswer("No recommendation from the given position");
        }
      } else {
        return response.createEmptyAnswer("Cannot compute recommendation from the given position");
      }
    } catch (DGSException e) {
      logger.info("DGSException: Unable to compute recommendations", e);
      return response.createErrorAnswer(recommendationType, e);
    } catch (Throwable e) {
      logger.info("Unable to compute recommendations", e);
      return response.createErrorAnswer(recommendationType, e);
    }
    return response.createSuccessAnswer(recommendationType, meta, lr);
  }

}
