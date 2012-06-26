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
package org.sindice.core.analytics.commons.hadoop;

/**
 * 
 * @author Stephane Campinas
 * @email stephane.campinas@deri.org
 * @param <T>
 */
public class ConfigurationKey<T> {

  private final String   name;
  private T              value;
  private final Reset<T> reset;

  interface Reset<T> {
    public T reset();
  }

  private ConfigurationKey(String name, Reset<T> reset) {
    this.name = name;
    this.reset = reset;
    reset(); // initialise the default value
  }

  /**
   * Creates a new instance.
   * 
   * @param <T>
   *          the value's type
   * @return a new instance
   */
  public static <T> ConfigurationKey<T> newInstance(String name, Reset<T> reset) {
    return new ConfigurationKey<T>(name, reset);
  }

  public void reset() {
    set(reset.reset());
  }

  public T get() {
    return value;
  }

  public void set(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return name;
  }

}
