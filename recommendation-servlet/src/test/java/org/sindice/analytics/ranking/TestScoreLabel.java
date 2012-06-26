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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.ranking.Label.LabelType;

public class TestScoreLabel {

  public List<Label> getFakeData() {
    List<Label> results = new ArrayList<Label>() {
      {
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 618090937));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 612638304));
        add(new Label(LabelType.URI, "http://xmlns.com/foaf/0.1/Person", 1039644372));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Address", 141735369));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Organization", 63809166));
        add(new Label(LabelType.LITERAL, "article", 40179723));
        add(new Label(LabelType.LITERAL, "website", 8915647));
        add(new Label(LabelType.URI, "http://www.w3.org/2003/01/geo/wgs84_pos#Point", 23771266));
        add(new Label(LabelType.LITERAL, "blog", 1463395));
        add(new Label(LabelType.URI, "http://www.w3.org/2002/12/cal/icaltzd#Vevent", 17853076));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Location", 16653076));
      }
    };
    addRandomPOFresourceURI(results);
    return results;
  }

  public List<Label> getFakeData2() {
    // location 7
    // point 6
    // name 6
    // vcard 5
    // person 3
    // event 3
    // website 3
    // address 2

    List<Label> results = new ArrayList<Label>() {
      {
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));
        add(new Label(LabelType.URI, "http://xmlns.com/foaf/0.1/Person", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));
        add(new Label(LabelType.URI, "http://xmlns.com/foaf/0.1/Person", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Address", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));
        add(new Label(LabelType.URI, "http://xmlns.com/foaf/0.1/Person", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Address", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#VCard", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));
        add(new Label(LabelType.LITERAL, "website", 1));

        add(new Label(LabelType.LITERAL, "website", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2003/01/geo/wgs84_pos#Point", 2));

        add(new Label(LabelType.URI, "http://www.w3.org/2003/01/geo/wgs84_pos#Point", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Name", 1));
        add(new Label(LabelType.LITERAL, "website", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Location", 1));

        add(new Label(LabelType.URI, "http://www.w3.org/2003/01/geo/wgs84_pos#Point", 1));
        add(new Label(LabelType.URI, "http://www.w3.org/2002/12/cal/icaltzd#Vevent", 3));

        add(new Label(LabelType.URI, "http://www.w3.org/2006/vcard/ns#Location", 6));
      }
    };
    addRandomPOFresourceURI(results);
    return results;
  }

  @Test
  public void testCardinalityScorer() {
    BaseLabelRanking ranker = new BaseLabelRanking("", new CardinalityScorer(), null);
    ranker.rank(getFakeData());
    final LabelList labels = ranker.getLabelList();

    assertEquals("http://xmlns.com/foaf/0.1/Person", labels.get(0).getRecommendation());
    assertEquals("blog", labels.get(labels.size() - 1).getRecommendation());
    assertEquals(40179723, labels.getScore("article"), 0.01);
  }

  @Test
  public void testCardinalityScorer2() {
    BaseLabelRanking ranker = new BaseLabelRanking("", new CardinalityScorer(), null);
    ranker.rank(getFakeData2());
    final LabelList labels = ranker.getLabelList();

    assertEquals("http://www.w3.org/2006/vcard/ns#Location", labels.get(0).getRecommendation());
    assertEquals(7, labels.getScore("http://www.w3.org/2006/vcard/ns#Location"), 0.01);
    assertEquals("http://www.w3.org/2006/vcard/ns#Address", labels.get(labels.size() - 1).getRecommendation());
    assertEquals(2, labels.getScore("http://www.w3.org/2006/vcard/ns#Address"), 0.01);
    assertEquals(6, labels.getScore("http://www.w3.org/2006/vcard/ns#Name"), 0.01);
  }

  private void addRandomPOFresourceURI(List<Label> results) {
    int cnt = 0;

    for (Label label: results) {
      label.addContext(QueryProcessor.POF_RESOURCE, QueryProcessor.POF_RESOURCE + (cnt++));
    }
  }

}
