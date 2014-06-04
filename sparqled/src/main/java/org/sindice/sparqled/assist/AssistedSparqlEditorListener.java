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
package org.sindice.sparqled.assist;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.configuration.XMLConfiguration;
import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.sindice.core.analytics.commons.summary.DataGraphSummaryVocab;
import org.sindice.core.sesame.backend.SesameBackend;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.sparqled.MemcachedClientWrapper;
import org.sindice.sparqled.SparqledContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class AssistedSparqlEditorListener
extends SparqledContextListener {

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
  public static final String     TEMPLATE              = "template";

  private MemcachedClientWrapper wrapper;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    final ServletContext context = sce.getServletContext();

    logger.info("initializing ASE context");
    XMLConfiguration config = (XMLConfiguration) sce.getServletContext().getAttribute("config");

    String template = doGetParameter(config, TEMPLATE, null);
    try {
      template = template == null ? null : new File(sparqledHome, template).getCanonicalPath();
    } catch (IOException e1) {
      throw new IllegalArgumentException("Unable to load template=[" + template + "]");
    }
    setParameter(context, TEMPLATE, template);

    final String datasetLabelDef = doGetParameter(config, DATASET_LABEL_DEF, DataGraphSummaryVocab.DATASET_LABEL_DEF.toString());
    setParameter(context, DATASET_LABEL_DEF, datasetLabelDef);

    final String domainUriPrefix = doGetParameter(config, DOMAIN_URI_PREFIX, DataGraphSummaryVocab.DOMAIN_URI_PREFIX);
    setParameter(context, DOMAIN_URI_PREFIX, domainUriPrefix);

    final String gsg = doGetParameter(config, GRAPH_SUMMARY_GRAPH, DataGraphSummaryVocab.GRAPH_SUMMARY_GRAPH);
    setParameter(context, GRAPH_SUMMARY_GRAPH, gsg);

    final String backend = doGetParameter(config, BACKEND, BackendType.HTTP.toString());
    setParameter(context, BACKEND, backend);
    final String[] backendArgs = doGetParameters(config, BACKEND_ARGS, "http://sparql.sindice.com/sparql");
    setParameter(context, BACKEND_ARGS, backendArgs);

    final String pagination = doGetParameter(config, PAGINATION, Integer.toString(SesameBackend.LIMIT));
    setParameter(context, PAGINATION, Integer.valueOf(pagination));

    final String limit = doGetParameter(config, LIMIT, "1000");
    setParameter(context, LIMIT, Integer.valueOf(limit));

    final String[] classAttributes = doGetParameters(config, CLASS_ATTRIBUTES, AnalyticsClassAttributes.DEFAULT);
    setParameter(context, CLASS_ATTRIBUTES, classAttributes);

    final String useMemcached = doGetParameter(config, "USE_MEMCACHED", "false");

    if (Boolean.parseBoolean(useMemcached)) {
      final String memcachedHost = doGetParameter(config, "MEMCACHED_HOST", "localhost");
      final String memcachedPort = doGetParameter(config, "MEMCACHED_PORT", "11211");
      try {
        final List<InetSocketAddress> addresses = AddrUtil.getAddresses(memcachedHost + ":" + memcachedPort);
        wrapper = new MemcachedClientWrapper(new MemcachedClient(addresses));
        sce.getServletContext().setAttribute(MemcachedClientWrapper.class.getName(), wrapper);
      } catch (IOException e) {
        logger.error("Could not initialize memcached !!!", e);
      }
    }
  }

  private String doGetParameter(XMLConfiguration config, String param, String defaultValue) {
    return super.getParameter(config, RECOMMENDER_WRAPPER + "." + param, defaultValue);
  }

  private String[] doGetParameters(XMLConfiguration config, String param, String... defaultValues) {
    return super.getParameters(config, RECOMMENDER_WRAPPER + "." + param, defaultValues);
  }

  private void setParameter(ServletContext context, String param, Object value) {
    context.setAttribute(RECOMMENDER_WRAPPER + param, value);
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
