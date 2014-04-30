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


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.http.protocol.Protocol;
import org.sindice.analytics.ranking.DGSQueryResultProcessor;
import org.sindice.analytics.ranking.Label;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class AssistedSparqlEditorServlet
extends HttpServlet {

  private static final long    serialVersionUID = 4137296200305461786L;

  private static final Logger  logger           = LoggerFactory.getLogger(AssistedSparqlEditorServlet.class);

  public static final String   DGS_GRAPH        = "dg";

  private SesameBackend<Label> dgsBackend       = null;
  private int                  pagination;
  private int                  limit;

  @Override
  public void init(ServletConfig config)
  throws ServletException {
    super.init(config);

    logger.info("Intialized ASE Servlet");

    // SPARQL endpoint with graph summary
    final BackendType backend = BackendType.valueOf((String) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.BACKEND));
    final String[] backendArgs = (String[]) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.BACKEND_ARGS);
    // The pagination value
    pagination = (Integer) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.PAGINATION);
    // The Limit of results to be retrieved
    limit = (Integer) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.LIMIT);
    // The ClassAttributes
    final String[] classAttributes = (String[]) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.CLASS_ATTRIBUTES);
    // Set the domain URI prefix
    DataGraphSummaryVocab.setDomainUriPrefix((String) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.DOMAIN_URI_PREFIX));
    // Set the dataset label definition
    DataGraphSummaryVocab.setDatasetLabelDefinition(DatasetLabel.valueOf((String) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.DATASET_LABEL_DEF)));
    // Set the summary named graph
    DataGraphSummaryVocab.setGraphSummaryGraph((String) config.getServletContext().getAttribute(AssistedSparqlEditorListener.RECOMMENDER_WRAPPER + AssistedSparqlEditorListener.GRAPH_SUMMARY_GRAPH));

    AnalyticsClassAttributes.initClassAttributes(classAttributes);
    try {
      final DGSQueryResultProcessor qrp = new DGSQueryResultProcessor();
      dgsBackend = SesameBackendFactory.getDgsBackend(backend, qrp, backendArgs);
      dgsBackend.initConnection();

      logger.info("Backend={} BackendArgs={} ClassAttributes={} Pagination={} DomainUriPrefix={} DatasetLabelDef={} GraphSummaryGraph={} LIMIT={}",
        new Object[] { backend, Arrays.toString(backendArgs), Arrays.toString(classAttributes),
      pagination, DataGraphSummaryVocab.DOMAIN_URI_PREFIX, DataGraphSummaryVocab.DATASET_LABEL_DEF, DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH, limit});
    } catch (Exception e) {
      logger.error("Failed to start the DGS backend", e);
    }
  }

  @Override
  public void destroy() {
    logger.info("Destroy ASE Servlet");
    try {
      if (dgsBackend != null) {
        dgsBackend.closeConnection();
      }
    } catch (SesameBackendException e) {
      logger.error("", e);
    } finally {
      super.destroy();
    }
  }

  /**
   * Update the Graph name of the summary, where the data summary was
   * saved in
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse resp)
  throws ServletException, IOException {
    if (request.getParameter(DGS_GRAPH) != null) {
      DataGraphSummaryVocab.setGraphSummaryGraph(request.getParameter(DGS_GRAPH));
    } else {
      DataGraphSummaryVocab.setGraphSummaryGraph(DataGraphSummaryVocab.DEFAULT_GSG);
    }
    logger.info("Using the summary: {}", DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
  }

  /**
   * Process request from editor, provides recommendations
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    getRecommendationAsJson(request, response);
  }

  /**
   * Process request from editor, provides recommendations
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    getRecommendationAsJson(request, response);
  }

  /**
   * Process auto-recommendation request
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  private void getRecommendationAsJson(HttpServletRequest request, HttpServletResponse response)
  throws IOException {
    logger.info("\nBegin processing request");
    final PrintWriter out = response.getWriter();

    response.setContentType("application/json");
    out.print(computeResponse(request));
    out.flush();
    out.close();
  }

  private String computeResponse(HttpServletRequest request)
  throws IOException {
    String response = "{}";

    // Check if user wants a context aware recommendation
    if (request.getParameter(Protocol.QUERY_PARAM_NAME) != null) {
      final ResponseWriter<String> responseWriter = new JsonResponseWriter();
      final String query = URLDecoder.decode(request.getParameter(Protocol.QUERY_PARAM_NAME), "UTF-8");
      // Get recommendation
      response = (String) SparqlRecommender.run(dgsBackend, responseWriter, query, pagination, limit);
    }
    return response;
  }

}
