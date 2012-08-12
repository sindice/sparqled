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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

  public static enum Status {
    SUCCESS, ERROR
  }

  /*
   * List the current Data Graph Summaries
   */

  @GET
  @Path("/list/native")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSummariesNative(@QueryParam("input-repo") String repo) {
    return getSummaries(BackendType.NATIVE, repo);
  }

  @GET
  @Path("/list/http")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSummariesHttp(@QueryParam("input-repo") String repo) {
    return getSummaries(BackendType.HTTP, repo);
  }

  @GET
  @Path("/list/virtuoso")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSummariesVirtuoso(@QueryParam("input-repo") String repo) {
    // NOTE: to query a VIRTUOSO endpoint, using a HTTP backend is enough
    return getSummaries(BackendType.HTTP, repo);
  }

  private String getSummaries(BackendType repoType,
                              String repo) {
    if (repo == null) {
      throw new IllegalArgumentException("You must specify the input repository");
    }

    String response = getJson(Status.ERROR, "");
    SesameBackend<?, ?> backend = null;
    try {
      backend = SesameBackendFactory.getDgsBackend(repoType, repo);
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
  @Path("/create/native")
  @Produces(MediaType.APPLICATION_JSON)
  public String computeSummaryNative(@QueryParam("input-repo") String inputRepo,
                                     @QueryParam("outut-repo") String outputRepo,
                                     @QueryParam("input-graph") String inputGraph,
                                     @QueryParam("output-graph")
                                     @DefaultValue(AnalyticsVocab.DEFAULT_GSG)
                                     String outputGraph) {
    return doComputeSummary(BackendType.NATIVE, inputRepo, outputRepo, inputGraph, outputGraph);
  }

  @POST
  @Path("/create/http")
  @Produces(MediaType.APPLICATION_JSON)
  public String computeSummaryHttp(@QueryParam("input-repo") String inputRepo,
                                   @QueryParam("outut-repo") String outputRepo,
                                   @QueryParam("input-graph") String inputGraph,
                                   @QueryParam("output-graph")
                                   @DefaultValue(AnalyticsVocab.DEFAULT_GSG)
                                   String outputGraph) {
    return doComputeSummary(BackendType.HTTP, inputRepo, outputRepo, inputGraph, outputGraph);
  }

  @POST
  @Path("/create/virtuoso")
  @Produces(MediaType.APPLICATION_JSON)
  public String computeSummaryVirtuoso(@QueryParam("input-repo") String repoPath,
                                       @QueryParam("outut-repo") String outputRepo,
                                       @QueryParam("input-graph") String inputGraph,
                                       @QueryParam("output-graph")
                                       @DefaultValue(AnalyticsVocab.DEFAULT_GSG)
                                       String outputGraph) {
    return doComputeSummary(BackendType.VIRTUOSO, repoPath, outputRepo, inputGraph, outputGraph);
  }

  private String doComputeSummary(BackendType repoType,
                                  String inputRepo,
                                  String outputRepo,
                                  String inputGraph,
                                  String outputGraph) {
    if (inputRepo == null) {
      throw new IllegalArgumentException("You must specify the input repository");
    }

    final File summaryPath = new File(tmpPath, SUMMARY_NAME);
    String response = getJson(Status.ERROR, "");
    try {
      // Compute the summary
      final String[] create;
      if (inputGraph == null) {
        create = (
          "--type " + repoType + " --repository " + inputRepo +
          " --outputfile " + summaryPath.getAbsolutePath()
        ).split(" ");
      } else {
        create = (
          "--type " + repoType + " --repository " + inputRepo +
          " --outputfile " + summaryPath.getAbsolutePath() + " --domain " + inputGraph
        ).split(" ");
      }
      Pipeline.main(create);
      // Load the summary into the repository
      final String oRepo = (outputRepo == null ? inputRepo : outputRepo);
      final String[] ingest = (
        "--feed --type " + repoType + " --repository " + oRepo +
        " --add " + summaryPath.getAbsolutePath() + " --domain " + outputGraph
      ).split(" ");
      Pipeline.main(ingest);
      // Register this summary
      final String triple = "<" + AnalyticsVocab.GRAPH_SUMMARY_GRAPH + "> " +
      HAS_PART_URI + " <" + outputGraph + "> .\n";
      final File regPath = new File(tmpPath, "reg-path");
      try {
        BufferedWriter w = new BufferedWriter(new FileWriter(regPath));
        w.append(triple);
        w.close();
        final String[] register = (
          "--feed --type " + repoType + " --repository " + oRepo +
          " --add " + regPath + " --domain " + SUMMARIES_GRAPH
        ).split(" ");
        Pipeline.main(register);
        response = getJson(Status.SUCCESS, "Created summary from " +
        (inputGraph == null ? inputRepo : inputGraph) + ", ingested it in " +
        oRepo
        );
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
