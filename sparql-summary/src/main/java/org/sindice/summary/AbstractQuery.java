/**
 * @project Graph Summary SPARQL
 * @author Pierre Bailly <pierre.bailly@deri.org>
 * @copyright Copyright (C) 2012, All rights reserved.
 */

package org.sindice.summary;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailException;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor.Context;
import org.sindice.core.sesame.backend.SesameBackendException;

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
/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
abstract public class AbstractQuery {
  // protected static RepositoryConnection _con;
  protected Stack<TupleQueryResult> _queriesResults;
  protected String _graphFrom;
  protected static SesameBackend<BindingSet, Context> _repository;
  protected Logger _logger;
  private Dump _dump;
  private String _domain;
  private boolean _setGraph;
  private boolean _initDump;
  private int _pagination;

  public static enum SummaryAlgorithm {
    SINGLE_LABELLED, MULTI_LABELLED
  }

  /**
   * Initialize the queries launcher.
   * 
   * @param d
   *          The Dump object, allows the use to modify the output easily.
   */
  public AbstractQuery(Dump d) {
    _logger = Logger.getLogger("org.sindice.summary.query");
    _graphFrom = "";
    _dump = d;
    _domain = "";
    _setGraph = false;
    _initDump = false;
    _pagination = -1;
  }

  /**
   * Initialize the queries launcher.
   */
  public AbstractQuery() {
    _logger = Logger.getLogger("org.sindice.summary.query");
    _graphFrom = "";
    _dump = new Dump();
    _domain = "";
    _setGraph = false;
    _initDump = false;
    _pagination = -1;
  }

  /**
   * Use the connection to the local repository or the web repository to launch
   * a SPARQL query and get the node.
   * 
   * @param query
   *          A string with a SPARQL query
   * @throws Exception
   */
  protected void launchQueryNode(String query) throws Exception {
    if (!_initDump) {
      Random rand = new Random();
      int r = rand.nextInt();
      initDump("/tmp/Graph-Summary-out/out" + r);
      _logger.info("Dump initializes by default at /tmp/Graph-Summary-out/out"
          + r);
    }
    _logger.info("LAUNCH QUERY");
    QueryIterator<BindingSet, Context> queryIt = _repository.submit(query);
    if (_pagination >= 0)
      queryIt.setPagination(_pagination);
    while (queryIt.hasNext()) {
      _dump.dumpRDFNode(queryIt.next());
    }
    _logger.info("END QUERY");
  }

  /**
   * Use the connection to the local repository or the web repository to launch
   * a SPARQL query and get the edge.
   * 
   * @param query
   *          A string with a SPARQL query
   * @throws Exception
   */
  protected void launchQueryPred(String query) throws Exception {
    if (!_initDump) {
      Random rand = new Random();
      int r = rand.nextInt();
      initDump("/tmp/Graph-Summary-out/out" + r);
      _logger.info("Dump initializes by default at /tmp/Graph-Summary-out/out"
          + r);
    }

    _logger.info("LAUNCH QUERY");
    QueryIterator<BindingSet, Context> queryIt = _repository.submit(query);
    if (_pagination >= 0)
      queryIt.setPagination(_pagination);
    while (queryIt.hasNext()) {
      _dump.dumpRDFPred(queryIt.next());
    }
    _logger.info("END QUERY");
  }

  /**
   * Set the pagination of the queries
   * 
   * @param pagination
   */
  public void setPagination(Integer pagination) {
    _pagination = pagination;
  }

  /**
   * Add an RDF file to the local repository.
   * 
   * @param RDFFile
   *          The path of the new RDF file
   * @param Ressource
   *          Optional argument for the file (example : The domain).
   * @throws RDFParseException
   * @throws RepositoryException
   * @throws IOException
   * @throws SesameBackendException
   */
  public void addFileToRepository(String RDFFile, RDFFormat format,
      Resource... contexts) throws RDFParseException, RepositoryException,
      IOException, SesameBackendException {
    _repository.addToRepository(new File(RDFFile), format, contexts);
  }

  /**
   * Set the "graph" for the next queries.
   * 
   * @param domain
   *          the website of the graph (example:
   *          "http://www.rottentomatoes.com")
   */

  public void setGraph(String domain) {
    if (domain.equals("")) {
      _domain = "";
    } else {
      _domain = domain;
    }
    _setGraph = true;
    if (domain.startsWith("<") && domain.endsWith(">")) {
      _graphFrom = "FROM " + domain + " ";
    } else {
      _graphFrom = "FROM <" + domain + "> ";
    }
  }

  /**
   * Unset the "graph" for the next queries.
   */
  public void unsetGraph() {
    _graphFrom = "";
    _domain = "";
    _setGraph = false;
  }

  /**
   * Initialize the dump
   * 
   * @param output
   *          Location of the output file.
   * @throws Exception
   */
  public void initDump(String output) throws Exception {
    if (!_setGraph || _domain.equals("")) {
      _logger.error("Dump initialization without a graph initialised.\n"
          + "Second domain = sindice.com");
      _domain = "sindice.com";
    }
    _initDump = true;
    _dump.openRDF(output, _domain);
  }

  /**
   * Get the name and the cardinality of the nodes.
   * 
   * @throws Exception
   */
  public abstract void computeName() throws Exception;

  /**
   * Get the name and the cardinality of the predicate/the edge for each node
   * and get the name of the node son/the target if it is possible.
   * 
   * @throws Exception
   */
  public abstract void computePredicate() throws Exception;

  /**
   * Stop the connection with the web repository or the local repository.
   * 
   * @throws RepositoryException
   * @throws SailException
   */
  public void stopConnexion() throws RepositoryException, SailException {
    try {
      _repository.closeConnection();
    } catch (SesameBackendException e) {
      _logger.error(e.getMessage());
    }
    _dump.closeRDF();
  }
}
