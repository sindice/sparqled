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

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Holds the label of an element which is a solution to the DGS query, and other
 * contextual information.
 */
public class Label {

  public enum LabelType {
    /** a {@link URI} */
    URI,
    /** a {@link Literal} */
    LITERAL,
    /** A qualified {@link URI} */
    QNAME
  }

  /**
   * map containing contextual information about that element. It can be the solutions
   * to SPARQL variables other than the POF (e.g., the linked Entity Node Collections).
   * 
   * The key is the name of the variable in the DGSquery, the array contains
   * the list of values the variable maps to.
   */
  private final Map<String, Object> context = new HashMap<String, Object>();
  private String                    label;
  private final long                cardinality;
  private LabelType type;

  public Label(Value label, long cardinality) {
    this.cardinality = cardinality;
    this.label = label.stringValue();
    type = (label instanceof Literal) ? LabelType.LITERAL : LabelType.URI;
  }

  public void addContext(String field, Object o) {
    context.put(field, o);
  }

  /**
   * @return the context
   */
  public Map<String, Object> getContext() {
    return context;
  }

  public LabelType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(LabelType type) {
    this.type = type;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the label
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
    return "label=[" + label + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Label) {
      return ((Label) obj).label.equals(label);
    }
    return false;
  }

  public int hashCode() {
    int hash = 31 + label.hashCode();
    return hash * 31 + type.hashCode();
  }

}
