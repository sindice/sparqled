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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the label of an element which is a solution to the DGS query, and other
 * contextual information.
 */
public class Label {

  public static enum LabelType {
    LITERAL, URI, QNAME,
    NONE // only when there is an error getting the value
  }

  /**
   * map containing contextual information about that element. It can be the solutions
   * to SPARQL variables other than the POF (e.g., the linked Entity Node Collections).
   * 
   * The key is the name of the variable in the DGSquery, the array contains
   * the list of values the variable maps to.
   */
  private final Map<String, List<Object>> context = new HashMap<String, List<Object>>();
  private String                          label;
  private final long                      cardinality;
  private LabelType                       type;

  public Label(LabelType labelType, String label, long cardinality) {
    this.cardinality = cardinality;
    this.label = label;
    this.type = labelType;
  }

  public void addContext(String field, Object o) {
    if (!context.containsKey(field)) {
      context.put(field, new ArrayList<Object>());
    }
    context.get(field).add(o);
  }

  public LabelType getLabelType() {
    return type;
  }

  public void setLabelType(LabelType type) {
    this.type = type;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the context
   */
  public Map<String, List<Object>> getContext() {
    return context;
  }

  /**
   * @return the labels
   */
  public String getLabel() {
    return label;
  }

  /**
   * @return the cardinality
   */
  public long getCardinality() {
    return cardinality;
  }

  @Override
  public String toString() {
    return "Label [ label=[" + label + "], cardinality=" + cardinality + ", context=" + context + " ]\n";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Label) {
      final Label lbl = (Label) obj;
      if (lbl.cardinality == cardinality && lbl.label.equals(label) && lbl.type.equals(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
