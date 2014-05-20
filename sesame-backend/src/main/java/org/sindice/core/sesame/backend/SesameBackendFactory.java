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

import org.openrdf.query.BindingSet;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

/**
 * 
 */
public final class SesameBackendFactory {

  public static enum BackendType {
    HTTP, MEMORY, RDBMS, NATIVE, VIRTUOSO
  }

  private SesameBackendFactory() {}

  public static <VALUE> SesameBackend<VALUE> getDgsBackend(BackendType type,
                                                           QueryResultProcessor<VALUE> qrp,
                                                           String... args) {
    switch (type) {
      case MEMORY:
        if (args != null && args.length > 1) {
          throw new IllegalArgumentException("The Memory backend takes zero or one argument: <data-dir>?");
        }
        return new MemorySesameBackend<VALUE>(qrp, (args == null || args.length == 0) ? null : args[0]);
      case VIRTUOSO:
      case HTTP:
        if (args.length != 1) {
          throw new IllegalArgumentException("The HTTP backend only takes one argument: <enpoint-url>");
        }
        return new HTTPSesameBackend<VALUE>(qrp, args[0]);
      case NATIVE:
        if (args.length != 1) {
          throw new IllegalArgumentException("The Native backend only takes one argument: <data-dir>");
        }
        return new NativeSesameBackend<VALUE>(qrp, args[0]);
      case RDBMS:
        if (args.length != 4) {
          throw new IllegalArgumentException("The RDBMS backend only takes 4 argument: <url> <database> <user> <password>");
        }
        return new RDBMSSesameBackend<VALUE>(qrp, args[0], args[1], args[2], args[3]);
      default:
        throw new EnumConstantNotPresentException(BackendType.class, type.toString());
    }
  }

  public static SesameBackend<BindingSet> getDgsBackend(BackendType type, String... args) {
    return getDgsBackend(type, null, args);
  }

}
