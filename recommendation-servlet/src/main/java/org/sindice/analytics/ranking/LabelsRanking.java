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
 * @author Diego Ceccarelli [ 15 Apr 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.ranking;

import java.io.Closeable;
import java.util.List;

import org.sindice.analytics.ranking.LabelsRankingFactory.RankingType;

/**
 * Interface for ranking the labels provided by the Backend
 */
public interface LabelsRanking extends Closeable {

  public static final int TOPK = 50;

  /**
   * Reset the ranking to prepare for a new incoming list of labels
   */
  public void reset();

  /**
   * Rank the list of candidates.
   * It is ordered in descending order of the score.
   * @param elements
   * @return
   */
  public void rank(List<Label> elements);

  /**
   * 
   * @return
   */
  public RankingType getRankingType();

  /**
   * The name of the ranking implementation
   * @return
   */
  public String getName();

  /**
   * The ordered List of recommendations.
   */
  public LabelList getLabelList();

}
