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
 * @author Campinas Stephane [ 28 Feb 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.servlet;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.sindice.analytics.backend.DGSQueryResultProcessor;
import org.sindice.analytics.backend.DGSQueryResultProcessor.Context;
import org.sindice.analytics.queryProcessor.DGSException;
import org.sindice.analytics.queryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.RecommendationType;
import org.sindice.analytics.queryProcessor.SparqlToDGSQueryInterface;
import org.sindice.analytics.queryProcessor.SparqlToDGSQueryManager;
import org.sindice.analytics.ranking.Label;
import org.sindice.analytics.ranking.Label.LabelType;
import org.sindice.analytics.ranking.LabelsRanking;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stephane
 */
public final class SparqlRecommender {

  private static final Logger logger  = LoggerFactory.getLogger(SparqlRecommender.class);

  /** Rank by batch of labels */
  private static final int    BATCH   = 2000;

  private SparqlRecommender() {}

  /**
   * Returns an object containing the list of recommendation for the given query
   * @param dgsBackend
   * @param query
   * @param rankings
   * @param pagination
   * @return
   */
  public static <C> C run(SesameBackend<Label, DGSQueryResultProcessor.Context> dgsBackend,
                          ResponseWriter<C> response,
                          String query,
                          List<LabelsRanking> rankings,
                          int pagination) {
    // TODO: Support queries with multiple FROM clauses
    RecommendationType recommendationType = RecommendationType.NONE;

    if (query == null || query.isEmpty()) {
      return response.createEmptyAnswer("The given query is null or empty...");
    }

    logger.debug("Input query: [{}]\n", query);
    final POFMetadata meta;
    try {
      // initialize
      for (LabelsRanking lr: rankings) {
        lr.reset();
      }
      /*
       * Get the DataGraphSummary query
       */
      final SparqlToDGSQueryInterface qp = new SparqlToDGSQueryManager();
      qp.load(query);
      final String dgsQuery = qp.getDGSQuery();

      meta = qp.getPofASTMetadata();
      final List<Object> keyword = meta.getPofNode().getMetadata() == null ? null : meta.getPofNode()
      .getMetadata(SyntaxTreeBuilder.Keyword);
      final List<Object> prefix = meta.getPofNode().getMetadata() == null ? null : meta.getPofNode()
      .getMetadata(SyntaxTreeBuilder.Prefix);
      final List<Object> qname = meta.getPofNode().getMetadata() == null ? null : meta.getPofNode()
      .getMetadata(SyntaxTreeBuilder.Qname);
      recommendationType = qp.getRecommendationType();

      logger.debug("RecommendationType: {}\nDGS query: [{}]", recommendationType, dgsQuery);
      if (!recommendationType.equals(RecommendationType.NONE)) {
        /*
         * Get the list of candidates and rank them
         */
        final ArrayList<Label> labels = new ArrayList<Label>();
       
        final QueryIterator<Label, Context> qrp = dgsBackend.submit(dgsQuery);
        qrp.getContext().type = recommendationType;
        
        qrp.setPagination(pagination);
        while (qrp.hasNext()) {
          final Label label = qrp.next();

          // filter If the POF contains keyword or Prefix metadata 
          // toString(), but the Object is an instance of String, so it is OK
          if (keyword != null && !label.getLabel().toLowerCase().contains(keyword.get(0).toString())) { // case insensitive: keyword is lowercased
            continue;
          }
          if (prefix != null &&
              !label.getLabel().toLowerCase().startsWith(prefix.get(0).toString().substring(1))) { // case insensitive: prefix is lowercased
            // the first character is the opening <
            continue;
          }
          if (qname != null) {
            final String value = qname.get(0).toString();
            if (!label.getLabel().startsWith(value)) {
              continue;
            }
            // recommend only the localname
            label.setLabel(label.getLabel().replaceFirst(value, ""));
            label.setLabelType(LabelType.QNAME);
          }

          logger.debug("Label={}", label);
          if (label.getCardinality() != -1) {
            labels.add(label);
            if (labels.size() == BATCH) {
              for (LabelsRanking lr: rankings) {
                lr.rank(labels);
              }
              labels.clear();
            }
          } else {
            logger.debug("No Recommendation for the label: {}", label);
          }
        }
        // Rank the rest of the labels
        for (LabelsRanking lr: rankings) {
          lr.rank(labels);
        }
        labels.clear();
        // Check if there are some recommendations
        boolean isEmpty = true;
        for (LabelsRanking lr: rankings) {
          if (lr.getLabelList().size() != 0) {
            isEmpty = false;
            break;
          }
        }
        if (isEmpty) {
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
    return response.createSuccessAnswer(recommendationType, meta, rankings);
  }

}
