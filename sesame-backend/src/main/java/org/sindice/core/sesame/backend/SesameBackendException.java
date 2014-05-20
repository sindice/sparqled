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

public class SesameBackendException
extends RuntimeException {

  private static final long serialVersionUID = -8813306750515755266L;

  /**
   * Constructs an <code>SesameBackendException</code> with no detail  message.
   */
  public SesameBackendException() {
    super();
  }

  /**
   * Constructs an <code>SesameBackendException</code> with the
   * specified detail message.
   *
   * @param message The detail message.
   */
  public SesameBackendException(final String message) {
    super(message);
  }

  /**
   * Constructs an <code>SesameBackendException</code> with the
   * specified cause.
   *
   * @param cause The cause.
   */
  public SesameBackendException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs an <code>SesameBackendException</code> with the
   * specified detail message and cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public SesameBackendException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
