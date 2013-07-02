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

import java.util.ArrayList;

/**
 * Contains a list of ScoreLabels, returned as a result of a query.
 */
public class LabelList extends ArrayList<ScoreLabel> {

  private static final long serialVersionUID = 1L;

  public double getScore(String label) {
    for (ScoreLabel sl : this) {
      if (sl.getRecommendation().equals(label)) {
        return sl.getScore();
      }
    }
    return -1;
  }

  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (ScoreLabel l : this) {
      sb.append(l.getRecommendation()).append("\t").append(l.getScore()).append("\n");
    }
    return sb.toString();
  }

  public void retainTopk(int topk) {
    if (size() > topk) {
      // start from the end, so that there is no call to System.arraycopy
      for (int i = size() - 1; i >= topk; i--) {
        remove(i);
      }
    }
  }

}
