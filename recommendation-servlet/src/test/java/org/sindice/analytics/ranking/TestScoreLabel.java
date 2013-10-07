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


import static org.junit.Assert.fail;
import static org.sindice.analytics.RDFTestHelper.literal;
import static org.sindice.analytics.RDFTestHelper.uri;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sindice.analytics.queryProcessor.QueryProcessor;
import org.sindice.analytics.ranking.CardinalityRanking.Recommendation;

public class TestScoreLabel {

  public List<Label> getFakeData() {
    final List<Label> results = new ArrayList<Label>() {
      {
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 618090937));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 612638304));
        add(new Label(uri("http://xmlns.com/foaf/0.1/Person"), 1039644372));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Address"), 141735369));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Organization"), 63809166));
        add(new Label(literal("article"), 40179723));
        add(new Label(literal("website"), 8915647));
        add(new Label(uri("http://www.w3.org/2003/01/geo/wgs84_pos#Point"), 23771266));
        add(new Label(literal("blog"), 1463395));
        add(new Label(uri("http://www.w3.org/2002/12/cal/icaltzd#Vevent"), 17853076));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Location"), 16653076));
      }
    };
    addRandomPOFresourceURI(results);
    return results;
  }

  public List<Label> getFakeData2() {
    List<Label> results = new ArrayList<Label>() {
      {
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));
        add(new Label(uri("http://xmlns.com/foaf/0.1/Person"), 1));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));
        add(new Label(uri("http://xmlns.com/foaf/0.1/Person"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Address"), 1));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));
        add(new Label(uri("http://xmlns.com/foaf/0.1/Person"), 1));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Address"), 1));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#VCard"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));
        add(new Label(literal("website"), 1));

        add(new Label(literal("website"), 1));

        add(new Label(uri("http://www.w3.org/2003/01/geo/wgs84_pos#Point"), 2));

        add(new Label(uri("http://www.w3.org/2003/01/geo/wgs84_pos#Point"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Name"), 1));
        add(new Label(literal("website"), 1));
        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Location"), 1));

        add(new Label(uri("http://www.w3.org/2003/01/geo/wgs84_pos#Point"), 1));
        add(new Label(uri("http://www.w3.org/2002/12/cal/icaltzd#Vevent"), 3));

        add(new Label(uri("http://www.w3.org/2006/vcard/ns#Location"), 6));
      }
    };
    addRandomPOFresourceURI(results);
    return results;
  }

  @Test
  public void testCardinalityRanking()
  throws Exception {
    final CardinalityRanking ranker = new CardinalityRanking();

    for (Label data : getFakeData()) {
      ranker.addLabel(data);
    }

    Long prevCard = null;
    for (Recommendation data : ranker.getLabels()) {
      if (prevCard != null && data.getCardinality() > prevCard) {
        fail("Received labels out of order");
      }
      prevCard = data.getCardinality();
    }
  }

  @Test
  public void testCardinalityRanking2()
  throws Exception {
    final CardinalityRanking ranker = new CardinalityRanking();

    for (Label data : getFakeData2()) {
      ranker.addLabel(data);
    }

    Long prevCard = null;
    for (Recommendation data : ranker.getLabels()) {
      if (prevCard != null && data.getCardinality() > prevCard) {
        fail("Received labels out of order");
      }
      prevCard = data.getCardinality();
    }
  }

  private void addRandomPOFresourceURI(List<Label> results) {
    int cnt = 0;

    for (Label label: results) {
      label.addContext(QueryProcessor.POF_RESOURCE, QueryProcessor.POF_RESOURCE + (cnt++));
    }
  }

}
