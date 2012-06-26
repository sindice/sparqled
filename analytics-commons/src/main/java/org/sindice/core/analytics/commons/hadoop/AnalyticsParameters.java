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

import java.lang.reflect.Field;
import java.util.HashSet;

import org.sindice.core.analytics.commons.hadoop.ConfigurationKey.Reset;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DatasetLabel;
import org.sindice.core.analytics.commons.summary.DocumentFormat;
import org.sindice.core.analytics.commons.summary.SummaryModel;

/**
 * Configuration parameters used in Hadoop analytics jobs
 * @author Stephane Campinas
 * @email stephane.campinas@deri.org
 *
 */
public class AnalyticsParameters {

  /**
   * The date the Summary Graph was computed
   */
  public final static ConfigurationKey<String>          DATE                   = ConfigurationKey
                                                                               .newInstance("date", new Reset<String>() {
                                                                                 @Override
                                                                                 public String reset() {
                                                                                   return null;
                                                                                 }
                                                                               });

  /**
   * The Key comparator to be used in the HFile default: the HBase key
   * comparator KeyValue.KEY_COMPARATOR
   */
  public final static ConfigurationKey<String>          KEY_COMPARATOR         = ConfigurationKey
                                                                               .newInstance("hbase.hfileoutputformat.keycomparator", new Reset<String>() {
                                                                                 @Override
                                                                                 public String reset() {
                                                                                   return "org.apache.hadoop.hbase.KeyValue.KeyComparator";
                                                                                 }
                                                                               });

  /**
   * The filename extension for the scheme ExtensionTextLine default: nquads
   * extension
   */
  public static final ConfigurationKey<String>          EXTENSION              = ConfigurationKey
                                                                               .newInstance("extension", new Reset<String>() {
                                                                                 @Override
                                                                                 public String reset() {
                                                                                   return "nq";
                                                                                 }
                                                                               });

  /**
   * Check the type authoritativeness default: true
   */
  public static final ConfigurationKey<Boolean>         CHECK_AUTH_TYPE        = ConfigurationKey
                                                                               .newInstance("check-auth-type", new Reset<Boolean>() {
                                                                                 @Override
                                                                                 public Boolean reset() {
                                                                                   return true;
                                                                                 }
                                                                               });

  /**
   * Unwanted list of predicates default: no black list
   */
  public static final ConfigurationKey<HashSet<String>> PREDICATES_BLACKLIST   = ConfigurationKey
                                                                               .newInstance("predicate-blacklist", new Reset<HashSet<String>>() {
                                                                                 @Override
                                                                                 public HashSet<String> reset() {
                                                                                   return new HashSet<String>();
                                                                                 }
                                                                               });

  /**
   * Let through only the domains from that list default: let all domains
   * through
   */
  public static final ConfigurationKey<HashSet<String>> DOMAINS_WHITELIST      = ConfigurationKey
                                                                               .newInstance("domains-whitelist", new Reset<HashSet<String>>() {
                                                                                 @Override
                                                                                 public HashSet<String> reset() {
                                                                                   return new HashSet<String>();
                                                                                 }
                                                                               });

  /**
   * The dataset label definition default: take the second-level domain name of
   * the document containing the RDF data
   */
  public static final ConfigurationKey<DatasetLabel>    DATASET_LABEL          = ConfigurationKey
                                                                               .newInstance("dataset-label", new Reset<DatasetLabel>() {
                                                                                 @Override
                                                                                 public DatasetLabel reset() {
                                                                                   return DatasetLabel.SECOND_LEVEL_DOMAIN;
                                                                                 }
                                                                               });

  /**
   * The format of the input documents default: the Sindice export format
   */
  public static final ConfigurationKey<DocumentFormat>  DOCUMENT_FORMAT        = ConfigurationKey
                                                                               .newInstance("document-format", new Reset<DocumentFormat>() {
                                                                                 @Override
                                                                                 public DocumentFormat reset() {
                                                                                   return DocumentFormat.SINDICE_EXPORT;
                                                                                 }
                                                                               });

  /**
   * the default second-level domain to use in case of
   * {@link DocumentFormat#NTRIPLES} default: not set, the user must specify
   * something
   */
  public static final ConfigurationKey<String>          DEFAULT_DOMAIN         = ConfigurationKey
                                                                               .newInstance("default-domain", new Reset<String>() {
                                                                                 @Override
                                                                                 public String reset() {
                                                                                   return null;
                                                                                 }
                                                                               });

  /**
   * Normalise Literal Type default: don't process literal types in any way
   */
  public static final ConfigurationKey<Boolean>         NORM_LITERAL_TYPE      = ConfigurationKey
                                                                               .newInstance("normalize-literal-type", new Reset<Boolean>() {
                                                                                 @Override
                                                                                 public Boolean reset() {
                                                                                   return false;
                                                                                 }
                                                                               });

  /**
   * Defines the set of class attributes default: rdf:type
   */
  public static final ConfigurationKey<String[]>        CLASS_ATTRIBUTES_FIELD = ConfigurationKey
                                                                               .newInstance("class-attributes", new Reset<String[]>() {
                                                                                 @Override
                                                                                 public String[] reset() {
                                                                                   return new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE };
                                                                                 }
                                                                               });

  /**
   * The model used to compute the Data Graph Summary default: the
   * EntityNodeCollection is the set of classes
   */
  public static final ConfigurationKey<SummaryModel>    SUMMARY_MODEL          = ConfigurationKey
                                                                               .newInstance("summary-model", new Reset<SummaryModel>() {
                                                                                 @Override
                                                                                 public SummaryModel reset() {
                                                                                   return SummaryModel.CLASS_SET;
                                                                                 }
                                                                               });

  public static void reset() {
    for (Field field : AnalyticsParameters.class.getDeclaredFields()) {
      try {
        if (field.get(null) instanceof ConfigurationKey) {
          ConfigurationKey<?> c = (ConfigurationKey<?>) field.get(null);
          c.reset();
        }
      } catch (IllegalArgumentException e) {} catch (IllegalAccessException e) {}
    }
  }

}
