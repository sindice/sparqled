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


public class DatasetsAnalyticsVocab {

  public static final String ANY23_PREFIX                    = "http://vocab.sindice.net/";
  public static final String ANALYTICS_PREFIX                = "http://vocab.sindice.net/analytics#";
  public static String       DOMAIN_URI_PREFIX               = "http://sindice.com/dataspace/default/domain/";

  /*
   * Analytics Graphs Names
   */
  public static final String DOMAIN_ANALYTICS_GRAPH          = "http://sindice.com/analytics/domain";

  /*
   * Data Graph Summary Check mechanism
   */
  public static final String XSD_DATE                        = "http://www.w3.org/2001/XMLSchema#date";
  public static final String DATE_PREDICATE                  = "http://purl.org/dc/elements/1.1/date";

  /**
   * Vocaulary Terms
   */
  public static final String DOMAIN_URI                      = ANY23_PREFIX + "domain_uri";
  public static final String DOMAIN_NAME                     = ANY23_PREFIX + "domain";

  public static final String LABEL                           = ANALYTICS_PREFIX + "label";
  public static final String CARDINALITY                     = ANALYTICS_PREFIX + "cardinality";

  public static final String NAMESPACE                       = ANALYTICS_PREFIX + "namespace";

  public static final String DOCUMENT_CARD                   = ANALYTICS_PREFIX + "document_cardinality";

  public static final String TOTAL_EXPLICIT_TRIPLES          = ANALYTICS_PREFIX + "total_explicit_triples";
  public static final String TOTAL_ENTITIES                  = ANALYTICS_PREFIX + "total_entities";
  public static final String TOTAL_DOCUMENTS                 = ANALYTICS_PREFIX + "total_documents";

  public static final String PATTERN_UUU                     = ANALYTICS_PREFIX + "patternUUU";
  public static final String PATTERN_UUB                     = ANALYTICS_PREFIX + "patternUUB";
  public static final String PATTERN_UUL                     = ANALYTICS_PREFIX + "patternUUL";
  public static final String PATTERN_BUU                     = ANALYTICS_PREFIX + "patternBUU";
  public static final String PATTERN_BUB                     = ANALYTICS_PREFIX + "patternBUB";
  public static final String PATTERN_BUL                     = ANALYTICS_PREFIX + "patternBUL";

  public static final String MAX_PROPERTY_CARD               = ANALYTICS_PREFIX + "max_property_cardinality";
  public static final String TOTAL_PROPERTY_CARD             = ANALYTICS_PREFIX + "total_property_cardinality";

  public static final String MAX_RELATION_CARD               = ANALYTICS_PREFIX + "max_relation_cardinality";
  public static final String TOTAL_RELATION_CARD             = ANALYTICS_PREFIX + "total_relation_cardinality";

  public static final String MAX_RELATION_SOURCE_CARD        = ANALYTICS_PREFIX + "max_relation_source_cardinality";
  public static final String TOTAL_RELATION_SOURCE_CARD      = ANALYTICS_PREFIX + "total_relation_source_cardinality";
  public static final String MAX_RELATION_DESTINATION_CARD   = ANALYTICS_PREFIX + "max_relation_destination_cardinality";
  public static final String TOTAL_RELATION_DESTINATION_CARD = ANALYTICS_PREFIX + "total_relation_destination_cardinality";

  public static final String STATISTIC_NAME                  = ANALYTICS_PREFIX + "statistic_name";
  public static final String DOMAIN_CLASS_STAT               = ANALYTICS_PREFIX + "statistic_domain_class";
  public static final String DOMAIN_PREDICATE_STAT           = ANALYTICS_PREFIX + "statistic_domain_predicate";
  public static final String DOMAIN_NAMESPACE_STAT           = ANALYTICS_PREFIX + "statistic_domain_namespace";

}
