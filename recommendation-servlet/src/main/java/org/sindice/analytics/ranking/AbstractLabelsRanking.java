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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sindice.analytics.ranking.Parameters.Vocab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLabelsRanking
implements LabelsRanking {

  protected static final Logger           logger     = LoggerFactory.getLogger(AbstractLabelsRanking.class);

  private final int                       topk;

  protected final Map<String, ScoreLabel> scores     = new HashMap<String, ScoreLabel>();
  protected final LabelList               labels     = new LabelList();
  protected Scorer                        scorer;
  protected final boolean                 EXPLAIN;
  protected final Properties              parameters;

  private final String                    name;

  public AbstractLabelsRanking(String name,
                               Scorer scorer,
                               Properties rankingProperties) {
    this.name = name;
    parameters = Parameters.setParameters(rankingProperties);
    this.scorer = scorer;
    EXPLAIN = Parameters.getParamValue(parameters, Vocab.EXPLAIN, false);
    topk = Parameters.getParamValue(parameters, Vocab.TOPK, TOPK);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void reset() {
    labels.clear();
    scores.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void rank(List<Label> elements) {
    Collection<ScoreLabel> csl = scoreLabels(elements, scores);
    labels.addAll(csl);
    Collections.sort(labels);
    labels.retainTopk(topk);
  }

  @Override
  public String toString() {
    return getName() + ": Ranking=" + getRankingType() + "(" + parameters +
           ") Scorer=" + scorer.getScorerType() + "(" + scorer.getParameters() + ")";
  }

  protected abstract Collection<ScoreLabel> scoreLabels(final List<Label> labels,
                                                        final Map<String, ScoreLabel> scores);

  @Override
  public void close()
  throws IOException {
  }

  @Override
  public LabelList getLabelList() {
    return labels;
  }

}
