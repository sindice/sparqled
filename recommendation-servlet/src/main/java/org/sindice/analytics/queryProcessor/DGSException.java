/*******************************************************************************
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
 *******************************************************************************/
/**
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 28 Feb 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;


/**
 * Outputs error messages relative to the Data Graph Summary query generation
 */
public class DGSException
extends VisitorException {

  private static final long serialVersionUID = -5918111678964653870L;

  public DGSException() {
    super();
  }

  public DGSException(String msg) {
    super(msg);
  }

  public DGSException(String msg, Throwable t) {
    super(msg, t);
  }

  public DGSException(Throwable t) {
    super(t);
  }

}
