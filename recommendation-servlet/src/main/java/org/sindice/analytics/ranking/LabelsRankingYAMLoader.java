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
 * @project siren-ranking
 * @author Campinas Stephane [ 14 Jul 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.ranking;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.sindice.analytics.ranking.LabelsRankingFactory.RankingType;
import org.sindice.analytics.ranking.LabelsRankingFactory.ScorerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * 
 */
public class LabelsRankingYAMLoader {

  private static final Logger       logger      = LoggerFactory.getLogger(LabelsRankingYAMLoader.class);

  private final Yaml                yaml        = new Yaml();
  private final InputStream         yamlStream;
  private final List<LabelsRanking> labelsRankings      = new ArrayList<LabelsRanking>();
  private final Set<String>         labelsRankingsNames = new HashSet<String>();

  public static enum Vocab {
    NAME("name"), RANKING("ranking"), SCORER("scorer"),
    RANKING_PARAMETERS("ranking-parameters"), SCORER_PARAMETERS("scorer-parameters");

    private final String label;

    private Vocab(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }

  }

  public LabelsRankingYAMLoader(final InputStream stream)
  throws IOException, InstantiationException, IllegalAccessException {
    yamlStream = stream;
  }

  public void load() {
    for (Object o : yaml.loadAll(yamlStream)) { // get each ranking
      final Properties rankingProperties = new Properties();
      final Properties scorerProperties = new Properties();
      final Map<String, Object> scorerConf = (Map) o;
      RankingType rankingType = null;
      ScorerType scorerType = null;
      String name = null;

      for (Entry<String, Object> c : scorerConf.entrySet()) { // get that ranking config
        if (c.getKey().equals(Vocab.NAME.toString())) {
          name = (String) c.getValue();
        } else if (c.getKey().equals(Vocab.RANKING.toString())) {
          rankingType = RankingType.valueOf((String) c.getValue());
        } else if (c.getKey().equals(Vocab.SCORER.toString())) {
          scorerType = ScorerType.valueOf((String) c.getValue());
        } else if (c.getKey().equals(Vocab.RANKING_PARAMETERS.toString())) {
          for (Map<String, Object> param : (List<Map<String, Object>>) c.getValue()) {
            rankingProperties.putAll(param);
          }
        } else if (c.getKey().equals(Vocab.SCORER_PARAMETERS.toString())) {
          for (Map<String, Object> param : (List<Map<String, Object>>) c.getValue()) {
            scorerProperties.putAll(param);
          }
        } else {
          throw new IllegalArgumentException("Unknown scorer configuration entry: " + c.getKey());
        }
      }
      if (scorerType == null || rankingType == null || name == null) {
        throw new IllegalArgumentException("Missing mandatory configuration: name, the ranking type or the scorer type");
      }
      /*
       * Create the scorer
       */
      final LabelsRanking lr = LabelsRankingFactory.getLabelsRanking(name, rankingType, rankingProperties, scorerType, scorerProperties);
      if (labelsRankingsNames.contains(name)) {
        throw new IllegalArgumentException("A scorer with the name " + name + " already exists");
      }
      logger.info("Added ranking: {}", lr);
      labelsRankings.add(lr);
      labelsRankingsNames.add(name);
    }
  }

  public List<LabelsRanking> getConfigurations() {
    return labelsRankings;
  }

}
