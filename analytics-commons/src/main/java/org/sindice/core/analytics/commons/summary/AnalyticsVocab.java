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

import org.sindice.core.analytics.commons.util.Hash;


/**
 * @author thomas
 */
public enum AnalyticsVocab {

  // Any23 namespace
  DOMAIN_URI("domain_uri"),
  DOMAIN_NAME("domain"),

  /*
   * Data Graph Summary
   */
  // Analytics namespace
  LABEL("analytics#label"),
  CARDINALITY("analytics#cardinality"),

  CLASS_RANK1("analytics#rank1"),
  CLASS_RANK2("analytics#rank2"),
  GLOBAL_ID("analytics#global_id"),
  TYPE("analytics#type"),

  EDGE_SOURCE("analytics#source"),
  EDGE_TARGET("analytics#target"),
  EDGE_PUBLISHED_IN("analytics#publishedIn"),

  /*
   * Domain Stats
   */
  NAMESPACE("analytics#namespace"),

  DOCUMENT_CARD("analytics#document_cardinality"),

  TOTAL_EXPLICIT_TRIPLES("analytics#total_explicit_triples"),
  TOTAL_ENTITIES("analytics#total_entities"),
  TOTAL_DOCUMENTS("analytics#total_documents"),

  PATTERN_UUU("analytics#patternUUU"),
  PATTERN_UUB("analytics#patternUUB"),
  PATTERN_UUL("analytics#patternUUL"),
  PATTERN_BUU("analytics#patternBUU"),
  PATTERN_BUB("analytics#patternBUB"),
  PATTERN_BUL("analytics#patternBUL"),

  MAX_PROPERTY_CARD("analytics#max_property_cardinality"),
  TOTAL_PROPERTY_CARD("analytics#total_property_cardinality"),

  MAX_RELATION_CARD("analytics#max_relation_cardinality"),
  TOTAL_RELATION_CARD("analytics#total_relation_cardinality"),

  MAX_RELATION_SOURCE_CARD("analytics#max_relation_source_cardinality"),
  TOTAL_RELATION_SOURCE_CARD("analytics#total_relation_source_cardinality"),
  MAX_RELATION_DESTINATION_CARD("analytics#max_relation_destination_cardinality"),
  TOTAL_RELATION_DESTINATION_CARD("analytics#total_relation_destination_cardinality"),

  STATISTIC_NAME("analytics#statistic_name"),
  DOMAIN_CLASS_STAT("analytics#statistic_domain_class"),
  DOMAIN_PREDICATE_STAT("analytics#statistic_domain_predicate"),
  DOMAIN_NAMESPACE_STAT("analytics#statistic_domain_namespace");

  /*
   * Data Graph Summary Check mechanism
   */
  public static final String  XSD_DATE                 = "http://www.w3.org/2001/XMLSchema#date";
  public static final String  DATE_PREDICATE           = "http://purl.org/dc/elements/1.1/date";

  /*
   * Analytics Graphs Names
   */
  public static final String  GRAPH_SUMMARY_GRAPH      = "http://sindice.com/analytics";
  public static final String  DOMAIN_ANALYTICS_GRAPH   = "http://sindice.com/analytics/domain";
  public static final String  EXTENDED_ANALYTICS_GRAPH = "http://sindice.com/analytics/extended";       // TODO: where is this used ?

  public static final String  PREFIX                   = "http://vocab.sindice.net/";
  public static final String  ANALYTICS_PREFIX         = "http://vocab.sindice.net/analytics#";
  private static final String DEFAULT_DUP              = "http://sindice.com/dataspace/default/domain/";
  public static String        DOMAIN_URI_PREFIX        = DEFAULT_DUP;

  public static final String  DUMMY_CLASS_HASH         = Long.toString(Hash.getLong("dummy class")).replace('-', 'n');
  public static final String  BLANK_NODE_COLLECTION    = "dummy class: " + DUMMY_CLASS_HASH;

  public static DatasetLabel  DATASET_LABEL_DEF        = DatasetLabel.SECOND_LEVEL_DOMAIN;
  private final String        term;

  private AnalyticsVocab(String _term) {
    this.term = PREFIX + _term;
  }

  public static void setDatasetLabelDefinition(DatasetLabel dlDef) {
    DATASET_LABEL_DEF = dlDef;
  }

  public static void setDomainUriPrefix(String dup) {
    DOMAIN_URI_PREFIX = dup;
  }

  public static void resetToDefaults() {
    DOMAIN_URI_PREFIX = DEFAULT_DUP;
    DATASET_LABEL_DEF = DatasetLabel.SECOND_LEVEL_DOMAIN;
  }

  @Override
  public String toString() {
    return term;
  }

}
