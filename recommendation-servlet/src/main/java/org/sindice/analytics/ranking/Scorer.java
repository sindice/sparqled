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
package org.sindice.analytics.ranking;

import java.util.Properties;

import org.sindice.analytics.ranking.LabelsRankingFactory.ScorerType;

/**
 * Common scoring functionality for different types of queries. Scorer computes
 * for each label returned by the back-end a score
 */
public abstract class Scorer {

  protected Label            label;
  protected String           uri;
  protected final Properties parameters;

  public Scorer() {
    this(null);
  }

  public Scorer(Properties properties) {
    parameters = Parameters.setParameters(properties);
  }

  /**
   * Set the current label containing the uri for which to produce the score
   * 
   * @param label
   */
  public void setLabel(Label label) {
    this.label = label;
  }

  Properties getParameters() {
    return parameters;
  }

  /**
   * Set the current uri for which to produce the score
   * 
   * @param uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  public abstract double score();

  public abstract ScorerType getScorerType();

  public String explain() {
    return "score: " + score() + " \t label:" + label.toString();
  }

}
