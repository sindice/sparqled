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

import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public final class Parameters {

  private static final Logger logger = LoggerFactory.getLogger(Parameters.class);

  public static enum Vocab {
    EXPLAIN("explain"), TOPK("topk"), OUTPUT_FORMAT("output-format");

    private final String label;

    private Vocab(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private Parameters() {}

  /**
   * Set the different parameters values to be used by this scorer.
   * 
   * @param p
   */
  public static final Properties setParameters(Properties p) {
    final Properties parameters = new Properties();

    Parameters.setDefaults(parameters);
    if (p != null) {
      for (Entry<Object, Object> prop : p.entrySet()) {
        if (parameters.containsKey(prop.getKey())) { // overwrite the default value
          parameters.put(prop.getKey(), prop.getValue());
        } else {
          throw new IllegalArgumentException("The parameter " + prop.getKey() + " doesn't exist");
        }
      }
    }
    return parameters;
  }

  public static <C> C getParamValue(Properties parameters, Vocab name, Class<C> type) {
    if (!parameters.containsKey(name.toString())) {
      logger.info("No value assigned for the parameter: " + name);
      throw new IllegalArgumentException("No value assigned for the parameter: " + name);
    }
    return (C) parameters.get(name.toString());
  }

  @SuppressWarnings("unchecked")
  public static <C> C getParamValue(Properties parameters, Vocab name, C defaultValue) {
    if (!parameters.containsKey(name.toString())) {
      logger.info("No value assigned for the parameter: " + name);
      return defaultValue;
    }
    return (C) parameters.get(name.toString());
  }

  private static void setDefaults(Properties parameters) {
    /*
     * Enable ranking explanation
     */
    parameters.put(Vocab.EXPLAIN.toString(), false);
    /*
     * Set the number of top results to be returned
     */
    parameters.put(Vocab.TOPK.toString(), 50);
    parameters.put(Vocab.OUTPUT_FORMAT.toString(), "html");
  }

}
