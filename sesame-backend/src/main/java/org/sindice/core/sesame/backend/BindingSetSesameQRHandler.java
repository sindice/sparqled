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
package org.sindice.core.sesame.backend;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * Select query result
 * @author Stephane Campinas [17 Jun 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class BindingSetSesameQRHandler
implements SesameQRHandler<BindingSet> {

  private TupleQueryResult tqr;

  @Override
  public void set(Object res) {
    tqr = (TupleQueryResult) res;
  }

  @Override
  public boolean hasNext()
  throws QueryEvaluationException {
    return tqr.hasNext();
  }

  @Override
  public BindingSet next()
  throws QueryEvaluationException {
    return tqr.next();
  }

  @Override
  public void close()
  throws QueryEvaluationException {
    tqr.close();
  }

}
