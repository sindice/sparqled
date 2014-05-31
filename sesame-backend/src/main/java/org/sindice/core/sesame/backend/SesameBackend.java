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
package org.sindice.core.sesame.backend;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.openrdf.model.Resource;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

/**
 * Provides access method to Sesame repositories: query, adding data.
 */
public interface SesameBackend {

  public static final int LIMIT = 1000;

  /**
   * @param <VALUE>
   *          The type of the query's results
   */
  public abstract class QueryIterator<VALUE>
  implements Iterator<VALUE> {

    public interface QueryResultProcessor<V> {

      public V process(Object bs);

    }

    /**
     * Get the results through pagination. If 0, there will be no pagination, i.e., all results are received with a
     * single query.
     * 
     * @param l
     */
    public abstract void setPagination(int l);

    /**
     * get the binding names in case of a select Query. Empty array otherwise.
     * 
     * @return
     */
    public abstract Set<String> getBindingNames();

    /**
     * Return the AST of the submitted query
     * 
     * @return
     */
    public abstract ASTQueryContainer getQueryAst();

    @Override
    public void remove() {
      throw new NotImplementedException();
    }

  }

  /**
   * Close the underlying repository
   * 
   * @throws SesameBackendException
   */
  public void closeConnection()
  throws SesameBackendException;

  /**
   * Starts the underlying repository
   * 
   * @throws SesameBackendException
   */
  public void initConnection()
  throws SesameBackendException;

  /**
   * Get a direct access to the repository
   * 
   * @return
   */
  public RepositoryConnection getConnection();

  /**
   * Add data to the repository
   * 
   * @param path
   * @param format
   * @param contexts
   * @throws SesameBackendException
   */
  public void addToRepository(File path, RDFFormat format, Resource... contexts)
  throws SesameBackendException;

  /**
   * Returns an iterator over the results of the submitted query
   * 
   * @param query
   * @return
   * @throws SesameBackendException
   */
  public <VALUE> QueryIterator<VALUE> submit(String query)
  throws SesameBackendException;

  /**
   * Returns an iterator over the results of the submitted query. Process results using the given
   * {@link QueryResultProcessor}
   * 
   * @param qrp
   * @param query
   * @return
   * @throws SesameBackendException
   */
  public <VALUE> QueryIterator<VALUE> submit(QueryResultProcessor<VALUE> qrp, String query)
  throws SesameBackendException;

}
