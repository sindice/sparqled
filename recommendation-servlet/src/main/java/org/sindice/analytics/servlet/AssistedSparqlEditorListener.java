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
package org.sindice.analytics.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.configuration.XMLConfiguration;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.AnalyticsVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephane Campinas
 */
public class AssistedSparqlEditorListener
extends ServletConfigurationContextListener {

  private static final Logger    logger                = LoggerFactory.getLogger(AssistedSparqlEditorListener.class);

  public static final String     RECOMMENDER_WRAPPER   = "recommender";

  public static final String     CLASS_ATTRIBUTES      = "classAttributes";
  public static final String     BACKEND               = "backend";
  public static final String     BACKEND_ARGS          = "backendArgs";
  public static final String     PAGINATION            = "pagination";
  public static final String     LIMIT                 = "limit";
  public static final String     DOMAIN_URI_PREFIX     = "domainUriPrefix";
  public static final String     DATASET_LABEL_DEF     = "datasetLabelDef";
  public static final String     GRAPH_SUMMARY_GRAPH   = "graphSummaryGraph";

  public static final String     RANKING_CONFIGURATION = "rankingConfig";
  private static final String    DEFAULT_RANKING       = "default-ranking.yaml";

  private MemcachedClientWrapper wrapper;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    final ServletContext context = sce.getServletContext();

    logger.info("initializing ASE context");
    XMLConfiguration config = (XMLConfiguration) sce.getServletContext().getAttribute("config");

    context.setAttribute(RECOMMENDER_WRAPPER + RANKING_CONFIGURATION, createRankingConfigFile());

    final String datasetLabelDef = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + DATASET_LABEL_DEF, AnalyticsVocab.DATASET_LABEL_DEF.toString());
    context.setAttribute(RECOMMENDER_WRAPPER + DATASET_LABEL_DEF, datasetLabelDef);

    final String domainUriPrefix = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + DOMAIN_URI_PREFIX, AnalyticsVocab.DOMAIN_URI_PREFIX);
    context.setAttribute(RECOMMENDER_WRAPPER + DOMAIN_URI_PREFIX, domainUriPrefix);

    final String gsg = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + GRAPH_SUMMARY_GRAPH, AnalyticsVocab.GRAPH_SUMMARY_GRAPH);
    context.setAttribute(RECOMMENDER_WRAPPER + GRAPH_SUMMARY_GRAPH, gsg);

    final String backend = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + BACKEND, BackendType.HTTP.toString());
    context.setAttribute(RECOMMENDER_WRAPPER + BACKEND, backend);
    final String[] backendArgs = getParametersWithLogging(config, RECOMMENDER_WRAPPER + "." + BACKEND_ARGS, new String[] { "http://sparql.sindice.com/sparql" });
    context.setAttribute(RECOMMENDER_WRAPPER + BACKEND_ARGS, backendArgs);

    final String pagination = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + PAGINATION, Integer.toString(SesameBackend.LIMIT));
    context.setAttribute(RECOMMENDER_WRAPPER + PAGINATION, Integer.valueOf(pagination));

    final String limit = getParameterWithLogging(config, RECOMMENDER_WRAPPER + "." + LIMIT, "0"); // No limit by default
    context.setAttribute(RECOMMENDER_WRAPPER + LIMIT, Integer.valueOf(limit));

    final String[] classAttributes = getParametersWithLogging(config, RECOMMENDER_WRAPPER + "." + CLASS_ATTRIBUTES, new String[] { AnalyticsClassAttributes.DEFAULT_CLASS_ATTRIBUTE });
    context.setAttribute(RECOMMENDER_WRAPPER + CLASS_ATTRIBUTES, classAttributes);

    final String useMemcached = getParameterWithLogging(config, "USE_MEMCACHED", "false");
    final String memcachedHost = getParameterWithLogging(config, "MEMCACHED_HOST", "localhost");
    final String memcachedPort = getParameterWithLogging(config, "MEMCACHED_PORT", "11211");

    if (Boolean.parseBoolean(useMemcached)) {
      try {
        final List<InetSocketAddress> addresses = AddrUtil.getAddresses(memcachedHost + ":" + memcachedPort);
        wrapper = new MemcachedClientWrapper(new MemcachedClient(addresses));
        sce.getServletContext().setAttribute(MemcachedClientWrapper.class.getName(), wrapper);
      } catch (IOException e) {
        logger.error("Could not initialize memcached !!!", e);
      }
    }
  }

  /**
   * Opens the ranking configuration file ranking.yaml. If that doesn't exist try
   * to create it by copying default-ranking.yaml which should be provided in the
   * classes folder with the application.
   * 
   * @return the path to the ranking configuration file
   */
  private String createRankingConfigFile() {
    File rankingConfigFile = null;
    try {
      final String rankingConfigFilename = configFolder.getAbsolutePath() + File.separatorChar + "ranking.yaml";
      rankingConfigFile = new File(rankingConfigFilename);

      if (!rankingConfigFile.exists()) {
        // use the default ranking configuration file
        final InputStream in = ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream(DEFAULT_RANKING);
        if (in == null) {
          logger.warn("missing default-ranking.yaml from classpath");
        } else {
          try {
            rankingConfigFile.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(rankingConfigFile);
            try {
              byte[] buff = new byte[1024];
              int read = 0;
              while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
              }
            }
            finally {
              out.close();
            }
          }
          finally {
            in.close();
          }
        }
      }
    } catch (IOException e) {
      logger.warn("couldn't write rankingConfigFile {}", rankingConfigFile, e);
    }
    return rankingConfigFile == null ? "" : rankingConfigFile.toString();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    logger.info("destroying context");
    if (wrapper != null) {
      wrapper.shutdown();
    }
    super.contextDestroyed(sce);
  }

}
