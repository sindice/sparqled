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
package org.sindice.core.analytics.commons.summary;

import org.sindice.core.analytics.commons.util.Hash;

public class DataGraphSummaryVocab {

  public static final String  ANY23_PREFIX          = "http://vocab.sindice.net/";
  public static final String  DGS_PREFIX            = "http://vocab.sindice.net/analytics#";
  private static final String DEFAULT_DUP           = "http://sindice.com/dataspace/default/domain/";
  public static String        DOMAIN_URI_PREFIX     = DEFAULT_DUP;

  /*
   * Analytics Graphs Names
   */
  public static final String  DEFAULT_GSG           = "http://sindice.com/analytics";
  public static String        GRAPH_SUMMARY_GRAPH   = DEFAULT_GSG;

  /*
   * Data Graph Summary Check mechanism
   */
  public static final String  XSD_DATE              = "http://www.w3.org/2001/XMLSchema#date";
  public static final String  DATE_PREDICATE        = "http://purl.org/dc/elements/1.1/date";

  public static final String  DUMMY_CLASS_HASH      = Long
                                                    .toString(Hash.getLong("dummy class"))
                                                    .replace('-', 'n');
  public static final String  BLANK_NODE_COLLECTION = "dummy class: " +
                                                      DUMMY_CLASS_HASH;

  public static DatasetLabel  DATASET_LABEL_DEF     = DatasetLabel.SECOND_LEVEL_DOMAIN;

  /**
   * Vocaulary Terms
   */
  public static final String  DOMAIN_URI            = ANY23_PREFIX + "domain_uri";
  public static final String  DOMAIN_NAME           = ANY23_PREFIX + "domain";
  public static final String  LABEL                 = DGS_PREFIX + "label";
  public static final String  CARDINALITY           = DGS_PREFIX + "cardinality";
  public static final String  CLASS_RANK1           = DGS_PREFIX + "rank1";
  public static final String  CLASS_RANK2           = DGS_PREFIX + "rank2";
  public static final String  GLOBAL_ID             = DGS_PREFIX + "global_id";
  public static final String  TYPE                  = DGS_PREFIX + "type";
  public static final String  EDGE_SOURCE           = DGS_PREFIX + "source";
  public static final String  EDGE_TARGET           = DGS_PREFIX + "target";
  public static final String  EDGE_PUBLISHED_IN     = DGS_PREFIX + "publishedIn";

  public static void setDatasetLabelDefinition(DatasetLabel dlDef) {
    DATASET_LABEL_DEF = dlDef;
  }

  public static void setDomainUriPrefix(String dup) {
    DOMAIN_URI_PREFIX = dup;
  }

  public static void setGraphSummaryGraph(String gsg) {
    GRAPH_SUMMARY_GRAPH = gsg;
  }

  public static void resetToDefaults() {
    DOMAIN_URI_PREFIX = DEFAULT_DUP;
    DATASET_LABEL_DEF = DatasetLabel.SECOND_LEVEL_DOMAIN;
    GRAPH_SUMMARY_GRAPH = DEFAULT_GSG;
  }

}
