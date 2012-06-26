/**
 * @project analytics
 * @author Thomas Perry <thomas.perry@deri.org>
 * @copyright Copyright (C) 2011, All rights reserved.
 */
package org.sindice.servlet.sparqlqueryservlet;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
public class SparqlQueryServletListener
extends ServletConfigurationContextListener {

  private static final Logger    logger             = LoggerFactory
                                                    .getLogger(SparqlQueryServletListener.class);

  public static final String     SQS_WRAPPER        = "proxy";
  public static final String     BACKEND            = "backend";
  public static final String     BACKEND_ARGS       = "backendArgs";
  public static final String     PREPROCESSING      = "preprocessing";
  public static final String     PREPROCESSING_ARGS = "preprocessingArgs";

  private MemcachedClientWrapper wrapper;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    final ServletContext context = sce.getServletContext();

    logger.info("initializing SQS context");
    XMLConfiguration config = (XMLConfiguration) sce.getServletContext().getAttribute("config");

    // PreProcessing
    final String prep = config.getString(SQS_WRAPPER + "." + PREPROCESSING, "");
    context.setAttribute(SQS_WRAPPER + PREPROCESSING, prep);
    final String[] prepArgs = getParametersWithLogging(config, SQS_WRAPPER + "." + PREPROCESSING_ARGS, new String[] {});
    context.setAttribute(SQS_WRAPPER + PREPROCESSING_ARGS, prepArgs);

    final String backend = config.getString(SQS_WRAPPER + "." + BACKEND, BackendType.NATIVE.toString());
    context.setAttribute(SQS_WRAPPER + BACKEND, backend);
    // TODO handle HTTPmode
    final String[] backendArgs = getParametersWithLogging(config, SQS_WRAPPER + "." + BACKEND_ARGS, new String[] { "./native-repository" });
    context.setAttribute(SQS_WRAPPER + BACKEND_ARGS, backendArgs);

    logger.info("Backend={} BackendArgs={}", backend, Arrays.toString(backendArgs));

    final String useMemcached = getParameterWithLogging(config, "USE_MEMCACHED", "false");
    final String memcachedHost = getParameterWithLogging(config, "MEMCACHED_HOST", "localhost");
    final String memcachedPort = getParameterWithLogging(config, "MEMCACHED_PORT", "11211");

    if (Boolean.parseBoolean(useMemcached)) {
      try {
        final List<InetSocketAddress> addresses = AddrUtil.getAddresses(memcachedHost + ":" + memcachedPort);
        wrapper = new MemcachedClientWrapper(new MemcachedClient(addresses));
        sce.getServletContext().setAttribute(SQS_WRAPPER + MemcachedClientWrapper.class.getName(), wrapper);
      } catch (IOException e) {
        logger.error("Could not initialize memcached !!!", e);
      }
    }
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
