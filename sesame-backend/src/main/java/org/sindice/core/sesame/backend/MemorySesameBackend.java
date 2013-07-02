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

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

/**
 * 
 */
public class MemorySesameBackend<VALUE, CONTEXT>
extends AbstractSesameBackend<VALUE, CONTEXT> {

  private final File dataDir;

  public MemorySesameBackend() {
    super();
    this.dataDir = null;
  }

  public MemorySesameBackend(String dataDir) {
    this(null, dataDir);
  }

  public MemorySesameBackend(QueryResultProcessor<VALUE, CONTEXT> qit) {
    this(qit, null);
  }

  public MemorySesameBackend(QueryResultProcessor<VALUE, CONTEXT> qit, String dataDir) {
    super(qit);
    this.dataDir = dataDir == null ? null : new File(dataDir);
  }

  @Override
  protected Repository getRepository() {
    final MemoryStore memStore = new MemoryStore();

    if (dataDir != null) {
      memStore.setDataDir(dataDir);
      memStore.setPersist(true);
    } else {
      memStore.setPersist(false);
    }
    return new SailRepository(memStore);
  }

}
