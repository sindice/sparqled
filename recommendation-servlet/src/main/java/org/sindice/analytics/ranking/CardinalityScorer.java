/**
 * Copyright (c) 2009-2012 Sindice Limited. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project sparql-editor-servlet
 * @author Diego Ceccarelli [ 15/apr/2012 ] 
 * @link diego.ceccarelli@deri.org
 * @copyright Copyright (C) 2012, All rights reserved.
 */
package org.sindice.analytics.ranking;

import java.util.Properties;

import org.sindice.analytics.ranking.LabelsRankingFactory.ScorerType;

/**
 * Simple scorer based on the cardinality of label containing the uri.
 */
public class CardinalityScorer extends Scorer {

  public CardinalityScorer() {
    super();
  }

  public CardinalityScorer(Properties scorerProperties) {
    super(scorerProperties);
  }

  @Override
  public ScorerType getScorerType() {
    return ScorerType.CARDINALITY;
  }

  @Override
  public double score() {
    return label.getCardinality();
  }

}
