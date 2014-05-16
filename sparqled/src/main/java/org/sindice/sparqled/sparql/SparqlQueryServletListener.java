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
package org.sindice.sparqled.sparql;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.configuration.XMLConfiguration;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.sindice.sparqled.MemcachedClientWrapper;
import org.sindice.sparqled.SparqledContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class SparqlQueryServletListener
extends SparqledContextListener {

  private static final Logger    logger             = LoggerFactory.getLogger(SparqlQueryServletListener.class);

  public static final String     SQS_WRAPPER        = "proxy";
  public static final String     BACKEND            = "backend";
  public static final String     BACKEND_ARGS       = "backendArgs";
  public static final String     PREPROCESSING      = "preprocessing";
  public static final String     PREPROCESSING_ARGS = "preprocessingArgs";

  private MemcachedClientWrapper wrapper;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    logger.info("initializing SQS context");
    ServletContext c = sce.getServletContext();
    XMLConfiguration config = (XMLConfiguration) c.getAttribute("config");

    // PreProcessing
    final String prep = doGetParameter(config, PREPROCESSING, "");
    setParameter(c, PREPROCESSING, prep);
    final String[] prepArgs = doGetParameters(config, PREPROCESSING_ARGS);
    setParameter(c, PREPROCESSING_ARGS, prepArgs);

    final String backend = doGetParameter(config, BACKEND, BackendType.NATIVE.toString());
    setParameter(c, BACKEND, backend);
    final String[] backendArgs = doGetParameters(config, BACKEND_ARGS, "./native-repository");
    setParameter(c, BACKEND_ARGS, backendArgs);

    logger.info("Backend={} BackendArgs={}", backend, Arrays.toString(backendArgs));

    final String useMemcached = getParameter(config, "USE_MEMCACHED", "false");

    if (Boolean.parseBoolean(useMemcached)) {
      final String memcachedHost = getParameter(config, "MEMCACHED_HOST", "localhost");
      final String memcachedPort = getParameter(config, "MEMCACHED_PORT", "11211");

      try {
        final List<InetSocketAddress> addresses = AddrUtil.getAddresses(memcachedHost + ":" + memcachedPort);
        wrapper = new MemcachedClientWrapper(new MemcachedClient(addresses));
        setParameter(c, SQS_WRAPPER + MemcachedClientWrapper.class.getName(), wrapper);
      } catch (IOException e) {
        logger.error("Could not initialize memcached !!!", e);
      }
    }
  }

  private String doGetParameter(XMLConfiguration config, String param, String defaultValue) {
    return super.getParameter(config, SQS_WRAPPER + "." + param, defaultValue);
  }

  private String[] doGetParameters(XMLConfiguration config, String param, String... defaultValues) {
    return super.getParameters(config, SQS_WRAPPER + "." + param, defaultValues);
  }

  private void setParameter(ServletContext context, String param, Object value) {
    context.setAttribute(SQS_WRAPPER + param, value);
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
