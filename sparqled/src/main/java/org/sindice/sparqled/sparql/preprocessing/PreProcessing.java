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
package org.sindice.sparqled.sparql.preprocessing;

public interface PreProcessing {

  /**
   * The arguments needed to initialise the preprocessing
   * @param args
   */
  public void init(String... args);

  /**
   * Process the query somehow, e.g., adding patterns,
   * and returns the updated query
   * @param query
   * @return
   * @throws Exception 
   */
  public String process(String query) throws Exception;

  /**
   * Any additional SPARQL variables must be prefixed with it and cannot be empty or null
   * @return
   */
  public String getVarPrefix();

  /**
   * Any additional SPARQL variables must be suffixed with it and cannot be empty or null
   * @return
   */
  public String getVarSuffix();

}
