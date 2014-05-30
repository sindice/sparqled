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
package org.sindice.sparqled;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Statement;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySesameServletHelper
extends HttpServlet {

  private static final long   serialVersionUID = -3562458310115107697L;

  public static final String  FILE_STREAM      = "filename";
  public static final String  FORMAT           = "format";

  private SailRepository      memBackend;

  public MemorySesameServletHelper() {
  }

  @Override
  public void init(ServletConfig config)
  throws ServletException {
    memBackend = new SailRepository(new MemoryStore());

    final InputStream fileStream = (InputStream) config.getServletContext().getAttribute(FILE_STREAM);
    final RDFFormat format = (RDFFormat) config.getServletContext().getAttribute(FORMAT);

    RepositoryConnection con = null;
    try {
      memBackend.initialize();
      con = memBackend.getConnection();
      con.getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
      con.begin();
      con.add(fileStream, "", format);
      con.commit();
    } catch (Exception e) {
      try {
        if (con != null) {
          con.rollback();
        }
      } catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
      throw new RuntimeException(e);
    }
    super.init(config);
  }

  @Override
  public void destroy() {
    try {
      memBackend.getConnection().close();
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    super.destroy();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException {
    final String query = req.getParameter(Protocol.QUERY_PARAM_NAME);
    final String queryLc = query.toLowerCase();

    try {
      if (queryLc.contains("select")) {
        res.setContentType("application/sparql-results+xml");
        final SPARQLResultsXMLWriter sparqlRes = new SPARQLResultsXMLWriter(res.getOutputStream());
        final TupleQuery tupleQuery = memBackend.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
        tupleQuery.evaluate(sparqlRes);
      } else if (queryLc.contains("ask")) {
        res.setContentType("application/sparql-results+xml");
        final SPARQLBooleanXMLWriter sparqlRes = new SPARQLBooleanXMLWriter(res.getOutputStream());
        final BooleanQuery bq = memBackend.getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query);
        sparqlRes.write(bq.evaluate());
      } else if (queryLc.contains("describe") || queryLc.contains("construct")) {
        res.setContentType("text/plain");
        final RDFWriter sparqlRes = new NTriplesWriter(new OutputStreamWriter(res.getOutputStream()));
        final GraphQuery gq = memBackend.getConnection().prepareGraphQuery(QueryLanguage.SPARQL, query);
        gq.evaluate(new RDFHandlerBase() {

          @Override
          public void startRDF()
          throws RDFHandlerException {
            sparqlRes.startRDF();
          }

          @Override
          public void handleStatement(Statement st)
          throws RDFHandlerException {
            sparqlRes.handleStatement(st);
          }

          @Override
          public void endRDF()
          throws RDFHandlerException {
            sparqlRes.endRDF();
          }

        });
      }
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.getOutputStream().print(e.toString());
    } finally {
      res.getOutputStream().flush();
      res.getOutputStream().close();
    }
  }

}
