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
package org.sindice.sparqled.assist;

import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;
import org.sindice.analytics.ranking.LabelsRanking;

public interface ResponseWriter {

  /**
   * Returns an object storing the list of recommendations
   * @param type
   * @param pofMetadata TODO
   * @param recommendations
   * @param rankingName
   * @return
   */
  public String createSuccessAnswer(RecommendationType type, POFMetadata pofMetadata, LabelsRanking recommendations);

  /**
   * Returns an object with an explanation of the failure
   * @param type
   * @return
   */
  public String createErrorAnswer(RecommendationType type, Throwable e);

  /**
   * Returns an object with an explanation of the failure
   * @param type
   * @return
   */
  public String createEmptyAnswer(String msg);

}
