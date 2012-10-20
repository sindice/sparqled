package org.sindice.summary.multilabelled;

import org.openrdf.repository.RepositoryException;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.summary.Dump;

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
public class HTTPMultiLabelledQuery extends AbstractMultiLabelledQuery {
  /**
   * This constructor make a connection with a web SPARQL repository.
   * 
   * @param d
   *          Dump object.
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   */
  public HTTPMultiLabelledQuery(Dump d, String websiteURL)
      throws RepositoryException, SesameBackendException {
    super(d);
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();
  }

  /**
   * This constructor make a connection with a web SPARQL repository.
   * 
   * @param websiteURL
   *          URL of the web SPARQL repository
   * @throws RepositoryException
   */
  public HTTPMultiLabelledQuery(String websiteURL) throws RepositoryException,
      SesameBackendException {
    super();
    _repository = SesameBackendFactory.getDgsBackend(BackendType.HTTP,
        websiteURL);
    _repository.initConnection();
  }
}
