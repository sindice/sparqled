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

import org.openrdf.http.client.SparqlSession;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

public class HTTPSesameBackend<VALUE>
extends AbstractSesameBackend<VALUE> {

  private final String endpointURL;

  public HTTPSesameBackend(String endpoingURL) {
    this(null, endpoingURL);
  }

  public HTTPSesameBackend(QueryResultProcessor<VALUE> qit, String endpoingURL) {
    super(qit);
    endpointURL = endpoingURL;
  }

  private class TimeoutHttpRepository extends SPARQLRepository {

    public TimeoutHttpRepository(String repositoryURL) {
      super(repositoryURL);
    }

    @Override
    protected SparqlSession createHTTPClient() {
      SparqlSession session = super.createHTTPClient();
      session.setConnectionTimeout(300000);
      return session;
    }

  }

  @Override
  protected Repository getRepository() {
    return new TimeoutHttpRepository(endpointURL);
  }

}
