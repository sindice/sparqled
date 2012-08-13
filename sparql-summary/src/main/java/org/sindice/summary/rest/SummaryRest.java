/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
package org.sindice.summary.rest;

import info.aduna.io.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.openrdf.query.BindingSet;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("summaries")
public class SummaryRest {

  private static final Logger  logger          = LoggerFactory.getLogger(SummaryRest.class);

  private final transient File tmpPath         = new File(System.getProperty("java.io.tmpdir"),
                                                          "summary" + Math.random());
  private final static String  SUMMARY_NAME    = "summary.nt.gz";
  private final static String  HAS_PART_URI    = "<http://purl.org/dc/terms/hasPart>";
  private final static String  SUMMARIES_GRAPH = "http://sindice.com/analytics/summaries";

  @Context
  private ServletContext       context;

  public static enum Status {
    SUCCESS, ERROR
  }

  private BackendType getRecommenderType() {
    final String type = (String) context.getAttribute(SummaryRestContextListener.RECOMMENDER_BACKEND);
    return type == null ? null : BackendType.valueOf(type);
  }

  private String[] getRecommenderArgs() {
    return (String[]) context.getAttribute(SummaryRestContextListener.RECOMMENDER_BACKEND_ARGS);
  }

  private BackendType getProxyType() {
    final String type = (String) context.getAttribute(SummaryRestContextListener.PROXY_BACKEND);
    return type == null ? null : BackendType.valueOf(type);
  }

  private String[] getProxyArgs() {
    return (String[]) context.getAttribute(SummaryRestContextListener.PROXY_BACKEND_ARGS);
  }

  /*
   * List the current Data Graph Summaries
   */

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSummaries() {
    // For VIRTUOSO, it is enough to use a HTTP endpoint for only querying
    final BackendType type = getRecommenderType() == BackendType.VIRTUOSO ? BackendType.HTTP : getRecommenderType();
    String response = getJson(Status.ERROR, "");
    SesameBackend<?, ?> backend = null;

    try {
      backend = SesameBackendFactory.getDgsBackend(type, getRecommenderArgs());
      backend.initConnection();
      final QueryIterator<?, ?> it = backend.submit(
        "SELECT ?summary FROM <" + SUMMARIES_GRAPH + "> {" +
        "  <" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + "> " +
        HAS_PART_URI + " ?summary " +
        "}"
      );
      final ArrayList<String> summaries = new ArrayList<String>();
      while (it.hasNext()) {
        final BindingSet b = (BindingSet) it.next();
        summaries.add(b.getValue("summary").stringValue());
      }
      final int size = summaries.size();
      response = getJson(Status.SUCCESS, "There " + (size <= 1 ? "is " : "are ") + size + " registered summaries", summaries.toString());
    } catch (SesameBackendException e) {
      response = getJson(Status.ERROR, e.getLocalizedMessage());
    } finally {
      if (backend != null) {
        try {
          backend.closeConnection();
        } catch (SesameBackendException e) {
          logger.error("Unable to close the connection to the SPARQL endpoint", e);
        }
      }
    }
    return response;
  }

  /*
   * Create a new Data Graph Summary
   */

  @POST
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  public String computeSummary(@QueryParam("input-graph") String inputGraph,
                               @QueryParam("output-graph")
                               @DefaultValue(AnalyticsVocab.DEFAULT_GSG)
                               String outputGraph) {
    System.out.println(context.getAttribute("recommender.backend"));
    final File summaryPath = new File(tmpPath, SUMMARY_NAME);
    String response = getJson(Status.ERROR, "");
    try {
      /*
       * Compute the summary
       */
      final String[] create;
      if (inputGraph == null) {
        create = (
          "--type " + getProxyType() + " --repository " + getProxyArgs()[0] +
          " --outputfile " + summaryPath.getAbsolutePath()
        ).split(" ");
      } else {
        create = (
          "--type " + getProxyType() + " --repository " + getProxyArgs()[0] +
          " --outputfile " + summaryPath.getAbsolutePath() + " --domain " + inputGraph
        ).split(" ");
      }
      Pipeline.main(create);
      /*
       * Load the summary into the repository
       */
      final String[] ingest = (
        "--feed --type " + getRecommenderType() + " --repository " + getRecommenderArgs()[0] +
        " --add " + summaryPath.getAbsolutePath() + " --domain " + outputGraph
      ).split(" ");
      Pipeline.main(ingest);
      /*
       * Register this summary
       */
      final String triple = "<" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + "> " +
      HAS_PART_URI + " <" + outputGraph + "> .\n";
      final File regPath = new File(tmpPath, "reg-path");
      try {
        BufferedWriter w = new BufferedWriter(new FileWriter(regPath));
        w.append(triple);
        w.close();
        final String[] register = (
          "--feed --type " + getRecommenderType() + " --repository " + getRecommenderArgs()[0] +
          " --add " + regPath + " --domain " + SUMMARIES_GRAPH
        ).split(" ");
        Pipeline.main(register);
        response = getJson(Status.SUCCESS, "Created summary from " +
          getProxyArgs()[0] + ", ingested it in " + getRecommenderArgs()[0]);
      } catch (IOException e) {
        logger.debug("Unable to register the summary");
        response = getJson(Status.ERROR, "Created the summary, but unable to register it.");
      }
    } catch(SesameBackendException e) {
      response = getJson(Status.ERROR, e.getLocalizedMessage());
    } finally {
      try {
        FileUtil.deleteDir(tmpPath);
      } catch (IOException e) {
        logger.error("", e);
      }
    }
    return response;
  }

  private String getJson(Status status,
                         String message,
                         String data) {
    return "{\"status\":\"" + status + "\",\"message\":\"" + message + "\"," +
           "\"data\":" + data + "}";
  }

  private String getJson(Status status,
                         String message) {
    return "{\"status\":\"" + status + "\",\"message\":\"" + message + "\"}";
  }

}
