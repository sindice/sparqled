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
package org.sindice.servlet.sparqlqueryservlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class SparqlSesameServletHelper
extends HttpServlet {

  private static final long   serialVersionUID = -3562458310115107697L;

  private static final Logger logger           = LoggerFactory.getLogger(SparqlSesameServletHelper.class);

  public static final String  FILE_STREAM      = "filename";
  public static final String  INPUT_FORMAT     = "input-format";
  public static final String  BACKEND_FORMAT   = "backend-format";
  public static final String  BACKEND_ARGS     = "backend-args";

  private File                repository;

  private SailRepository      _backend;

  public SparqlSesameServletHelper() {}

  @Override
  public void init(ServletConfig config)
  throws ServletException {

    final InputStream fileStream = (InputStream) config.getServletContext()
    .getAttribute(FILE_STREAM);
    final RDFFormat format = (RDFFormat) config.getServletContext()
    .getAttribute(INPUT_FORMAT);
    final BackendType backend = (BackendType) config.getServletContext()
    .getAttribute(BACKEND_FORMAT);
    final String backendArgs = (String) config.getServletContext()
    .getAttribute(BACKEND_ARGS);

    Logger logger = LoggerFactory.getLogger(SparqlSesameServletHelper.class);
    logger.debug(backend + ": " + backendArgs);
    repository = new File(backendArgs);
    if (backend == BackendType.MEMORY) {
      _backend = new SailRepository(new MemoryStore(repository));
    } else if (backend == BackendType.MEMORY) {
      _backend = new SailRepository(new NativeStore(repository));
    } // else HTTP => nothing

    try {
      final BufferedInputStream dgsInputStream = new BufferedInputStream(fileStream);
      AnalyticsClassAttributes.initClassAttributes(new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });

      _backend.initialize();
      _backend.getConnection().add(dgsInputStream, "", format, _backend
      .getValueFactory().createURI(DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    super.init(config);
  }

  @Override
  public void destroy() {
    logger.info("Destroy QUERY Servlet");
    try {
      _backend.getConnection().close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    super.destroy();
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
  throws ServletException, IOException {
    res.setContentType("application/sparql-results+xml");

    final String query = (String) req.getParameter(Protocol.QUERY_PARAM_NAME);
    final SPARQLResultsXMLWriter sparqlRes = new SPARQLResultsXMLWriter(res.getOutputStream());

    try {
      final TupleQuery tupleQuery = _backend.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.evaluate(sparqlRes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      res.getOutputStream().flush();
      res.getOutputStream().close();
    }
  }

}
