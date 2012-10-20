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
package org.sindice.core.analytics.commons.summary;

public class CollectionAnalyticsVocab {

  public static final String ANY23_PREFIX         = "http://vocab.sindice.net/";
  public static final String COL_ANALYTICS_PREFIX = "http://vocab.sindice.net/analytics/collection/";

  /**
   * Analytics Graphs Names
   */
  public static final String COL_ANALYTICS_GRAPH  = "http://sindice.com/analytics/collection";

  /**
   * Check mechanism
   */
  public static final String XSD_DATE             = "http://www.w3.org/2001/XMLSchema#date";
  public static final String DATE_PREDICATE       = "http://purl.org/dc/elements/1.1/date";

  /**
   * Vocaulary Terms
   */

  public static final String STATISTIC_NAME       = COL_ANALYTICS_PREFIX +
                                                    "statistic_name";
  public static final String COL_CLASS_STAT       = COL_ANALYTICS_PREFIX +
                                                    "statistic_col_class";
  public static final String COL_PREDICATE_STAT   = COL_ANALYTICS_PREFIX +
                                                    "statistic_col_predicate";
  public static final String COL_FORMAT_STAT      = COL_ANALYTICS_PREFIX +
                                                    "statistic_col_format";
  public static final String COL_URI_STAT         = COL_ANALYTICS_PREFIX +
                                                    "statistic_col_uri";
  public static final String COL_NS_STAT          = COL_ANALYTICS_PREFIX +
                                                    "statistic_col_ns";

  public static final String LABEL                = COL_ANALYTICS_PREFIX +
                                                    "label";
  public static final String N_REFS               = COL_ANALYTICS_PREFIX +
                                                    "n_refs";
  public static final String N_URLS               = COL_ANALYTICS_PREFIX +
                                                    "n_urls";
  public static final String N_DOMAINS            = COL_ANALYTICS_PREFIX +
                                                    "n_domains";
  public static final String N_SND_DOMAINS        = COL_ANALYTICS_PREFIX +
                                                    "n_snd_domains";

}
