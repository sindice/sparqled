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
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemorySesameServletHelper
extends HttpServlet {

  private static final long serialVersionUID = -3562458310115107697L;

  private static final Logger  logger      = LoggerFactory.getLogger(MemorySesameServletHelper.class);

  public static final String   FILE_STREAM = "filename";
  public static final String   FORMAT      = "format";

  private File                 repository;

  private SailRepository memBackend;

  public MemorySesameServletHelper() {
  }

  @Override
  public void init(ServletConfig config)
  throws ServletException {
    repository = new File("/tmp/DGS-repo-test");
    memBackend = new SailRepository(new MemoryStore(repository));

    final InputStream fileStream = (InputStream) config.getServletContext().getAttribute(FILE_STREAM);
    final RDFFormat format = (RDFFormat) config.getServletContext().getAttribute(FORMAT);

    try {
      final BufferedInputStream dgsInputStream = new BufferedInputStream(fileStream);
      AnalyticsClassAttributes.initClassAttributes(new String[] {AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE});

      memBackend.initialize();
      memBackend.getConnection().setAutoCommit(true);
      memBackend.getConnection().add(dgsInputStream, "", format, memBackend.getValueFactory().createURI(DataGraphSummaryVocab.DEFAULT_GSG));
    } catch (RDFParseException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    super.init(config);
  }

  @Override
  public void destroy() {
    logger.info("Destroy DGS Servlet");
    try {
      memBackend.getConnection().close();
    } catch (RepositoryException e) {
      logger.error("", e);
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
      final TupleQuery tupleQuery = memBackend.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.evaluate(sparqlRes);
    } catch (RepositoryException e) {
      e.printStackTrace();
    } catch (MalformedQueryException e) {
      e.printStackTrace();
    } catch (QueryEvaluationException e) {
      e.printStackTrace();
    } catch (TupleQueryResultHandlerException e) {
      e.printStackTrace();
    } finally {
      res.getOutputStream().flush();
      res.getOutputStream().close();
    }
  }

}
