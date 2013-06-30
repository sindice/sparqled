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
package org.sindice.analytics.ranking;

import java.util.ArrayList;
import java.util.List;

import org.sindice.analytics.ranking.Label.LabelType;

/**
 * Holds an URI to recommend and additional informations:
 * <ul>
 * <li>The elements of the DGS that contain the URI</li>
 * <li>The score of the URI</li>
 * <li>Explanation of the score, if requested</li>
 * </ul>
 * ASE-18
 */
public class ScoreLabel
implements Comparable<ScoreLabel> {

  private final LabelType    recommendationType; // whether an URI or a Literal
  private final String       recommendation;    // the element that will be recommended for the POF
  private final List<Label>  labels;            // the element node where that URI is from
  private double             score;             // the score associated with toRecommend
  private final List<String> explanation;       // the score explanation

  public ScoreLabel(LabelType type, String recommendation) {
    this.recommendation = recommendation;
    this.recommendationType = type;
    labels = new ArrayList<Label>();
    explanation = new ArrayList<String>();
    score = 0;
  }

  public void addLabel(Label l) {
    labels.add(l);
  }

  public void addExplanation(String e) {
    explanation.add(e);
  }

  /**
   * @return the element
   */
  public List<Label> getLabels() {
    return labels;
  }

  /**
   * @param element
   *          the element to set
   */
  public void setElement(List<Label> labels) {
    this.labels.clear();
    this.labels.addAll(labels);
  }

  /**
   * @return the score
   */
  public double getScore() {
    return score;
  }

  /**
   * @param score
   *          the score to set
   */
  public void setScore(double score) {
    this.score = score;
  }

  /**
   * @return the explanation
   */
  public List<String> getExplanation() {
    return explanation;
  }

  /**
   * @param explanation
   *          the explanation to set
   */
  public void setExplanation(List<String> explanation) {
    this.explanation.clear();
    this.explanation.addAll(explanation);
  }

  /**
   * @return the URI to recommend
   */
  public String getRecommendation() {
    return recommendation;
  }

  public LabelType getRecommendationType() {
    return recommendationType;
  }

  @Override
  public int compareTo(ScoreLabel obj) {
    if (!(obj instanceof ScoreLabel))
      return 0;
    ScoreLabel sl = (ScoreLabel) obj;
    if (score > sl.score)
      return -1;
    if (sl.score > score)
      return 1;
    return 0;
  }

  @Override
  public String toString() {
    return "ScoreLabel [Uri=" + recommendation + ", score=" + score + "]";
  }

}
