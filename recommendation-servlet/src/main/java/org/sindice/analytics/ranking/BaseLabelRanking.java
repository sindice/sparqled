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
package org.sindice.analytics.ranking;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.ranking.LabelsRankingFactory.RankingType;

/**
 * Implements the logic for ranking: given a list of labels, for each Uri
 * contained in a label creates a new ScoreLabel and updates its score (Computed
 * by a Scorer). <br/>
 * Since an Uri can belong to more than one label, if this happens scores are
 * summed.
 * 
 * @see Scorer
 */
public class BaseLabelRanking
extends AbstractLabelsRanking {

  public BaseLabelRanking(String name,
                          Scorer scorer,
                          Properties rankingProperties) {
    super(name, scorer, rankingProperties);
  }

  @Override
  public RankingType getRankingType() {
    return RankingType.BASE;
  }

  /**
   * Given the list of the label returned by the backend, extract the set of Uri
   * contained in each Labels and score them (using a Scorer). Returns a <i>
   * unsorted </i> list of ScoreLabel
   * 
   * @param labels
   * @return a collections of scored Uri
   */
  @Override
  protected Collection<ScoreLabel> scoreLabels(final List<Label> labels,
                                               final Map<String, ScoreLabel> scores) {
    for (Label l : labels) {
      final String label = l.getLabel();

      scorer.setLabel(l);
      scorer.setUri(label);
      ScoreLabel scoreLabel;
      if (scores.containsKey(label)) {
        scoreLabel = scores.get(label);
      } else {
        scoreLabel = new ScoreLabel(l.getLabelType(), label);
      }
      // an Uri can belong to more than one label, if this happens scores are summed
      /*
       * Check that this label doesn't come from a resource previously seen, e.g.,
       * it can happen when a class label is defined with multiple class attributes
       */
      if (isNew(scoreLabel, l)) {
        scoreLabel.setScore(scoreLabel.getScore() + scorer.score());
      }
      scoreLabel.addLabel(l);
      if (EXPLAIN) {
        scoreLabel.addExplanation(scorer.explain());
      }
      scores.put(label, scoreLabel);
    }
    return scores.values();
  }

  private boolean isNew(ScoreLabel sl, Label toAdd) {
    final String pofResourceToAdd = (String) toAdd.getContext().get(QueryProcessor.POF_RESOURCE).get(0);

    for (Label label: sl.getLabels()) {
      String pofResource = (String) label.getContext().get(QueryProcessor.POF_RESOURCE).get(0);
      if (pofResource.equals(pofResourceToAdd)) {
        return false;
      }
    }
    return true;
  }

}
