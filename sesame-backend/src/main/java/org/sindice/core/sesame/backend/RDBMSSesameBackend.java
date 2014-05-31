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

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

public class RDBMSSesameBackend
extends AbstractSesameBackend {

  private final String url;
  private final String database;
  private final String user;
  private final String password;

  public RDBMSSesameBackend(String url, String database, String user, String password) {
    this(null, url, database, user, password);
  }

  public RDBMSSesameBackend(QueryResultProcessor qit, String url, String database, String user, String password) {
    super(qit);
    this.database = database;
    this.password = password;
    this.user = user;
    this.url = url;
  }

  @Override
  protected Repository getRepository() {
    final MySqlStore rs = new MySqlStore(database);
    rs.setServerName(url);
    rs.setPassword(password);
    rs.setUser(user);
    return new SailRepository(rs);
  }

}
