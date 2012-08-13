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
    final BackendType type = getRecommenderType();
    String response = getJson(Status.ERROR, "");
    SesameBackend<?, ?> backend = null;

    try {
      backend = SesameBackendFactory.getDgsBackend(type, getRecommenderArgs());
      backend.initConnection();
      final QueryIterator<?, ?> it = backend.submit(
        "SELECT ?summary FROM <" + SUMMARIES_GRAPH + "> {" +
        "  <" + AnalyticsVocab.DEFAULT_GSG + "> " +
        HAS_PART_URI + " ?summary " +
        "}"
      );
      final ArrayList<String> summaries = new ArrayList<String>();
      while (it.hasNext()) {
        final BindingSet b = (BindingSet) it.next();
        summaries.add("\"" + b.getValue("summary").stringValue() + "\"");
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
   * Delete the given graph from the endpoint, and unregister it from the list
   * of available summaries.
   */

  @DELETE
  @Path("/delete")
  @Produces(MediaType.APPLICATION_JSON)
  public String deleteSummary(@QueryParam("graph") String graph) {
    final BackendType type = getRecommenderType();
    String response = getJson(Status.ERROR, "");
    SesameBackend<?, ?> backend = null;

    if (graph == null) {
      response = getJson(Status.ERROR, "You need to specify the graph summary to delete");
      return response;
    }
    try {
      backend = SesameBackendFactory.getDgsBackend(type, getRecommenderArgs());
      backend.initConnection();

      final ValueFactoryImpl factory = new ValueFactoryImpl();
      // Delete the graph
      backend.getConnection().clear(NTriplesUtil.parseURI("<" + graph  + ">", factory));
      // Unregister the graph
      final URI context = NTriplesUtil.parseURI("<" + SUMMARIES_GRAPH  + ">", factory);
      final URI s = NTriplesUtil.parseURI("<" + AnalyticsVocab.DEFAULT_GSG  + ">", factory);
      final URI p = NTriplesUtil.parseURI(HAS_PART_URI, factory);
      final URI o = NTriplesUtil.parseURI("<" + graph  + ">", factory);
      backend.getConnection().remove(s, p, o, context);
      backend.getConnection().commit();

      response = getJson(Status.SUCCESS, "Deleted the Summary Graph: " + graph);
    } catch (SesameBackendException e) {
      response = getJson(Status.ERROR, e.getLocalizedMessage());
    } catch (RepositoryException e) {
      response = getJson(Status.ERROR, e.getLocalizedMessage());
    } catch (IllegalArgumentException e) {
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
  public String computeSummary(@FormParam("input-graph") String inputGraph,
                               @FormParam("output-graph")
                               @DefaultValue(AnalyticsVocab.DEFAULT_GSG)
                               String outputGraph) {
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
          " --outputfile " + summaryPath.getAbsolutePath() +
          (getProxyType() == BackendType.VIRTUOSO ? " --user " + getProxyArgs()[1] + " --pass " + getProxyArgs()[2] : "")
        ).split(" ");
      } else {
        create = (
          "--type " + getProxyType() + " --repository " + getProxyArgs()[0] +
          " --outputfile " + summaryPath.getAbsolutePath() + " --domain " + inputGraph +
          (getProxyType() == BackendType.VIRTUOSO ? " --user " + getProxyArgs()[1] + " --pass " + getProxyArgs()[2] : "")
        ).split(" ");
      }
      Pipeline.main(create);
      /*
       * Load the summary into the repository
       */
      final String[] ingest = (
        "--feed --type " + getRecommenderType() + " --repository " + getRecommenderArgs()[0] +
        " --add " + summaryPath.getAbsolutePath() + " --domain " + outputGraph +
        (getRecommenderType() == BackendType.VIRTUOSO ? " --user " + getRecommenderArgs()[1] + " --pass " + getRecommenderArgs()[2] : "")
      ).split(" ");
      Pipeline.main(ingest);
      /*
       * Register this summary
       */
      final String triple = "<" + AnalyticsVocab.DEFAULT_GSG + "> " +
      HAS_PART_URI + " <" + outputGraph + "> .\n";
      final File regPath = new File(tmpPath, "reg-path");
      try {
        BufferedWriter w = new BufferedWriter(new FileWriter(regPath));
        w.append(triple);
        w.close();
        final String[] register = (
          "--feed --type " + getRecommenderType() + " --repository " + getRecommenderArgs()[0] +
          " --add " + regPath + " --domain " + SUMMARIES_GRAPH +
          (getRecommenderType() == BackendType.VIRTUOSO ? " --user " + getRecommenderArgs()[1] + " --pass " + getRecommenderArgs()[2] : "")
        ).split(" ");
        Pipeline.main(register);
        response = getJson(Status.SUCCESS, "Created summary from " +
          getProxyArgs()[0] + ", ingested it in " + getRecommenderArgs()[0]);
      } catch (Exception e) {
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
