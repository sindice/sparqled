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
package org.sindice.summary.rest;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.configuration.XMLConfiguration;
import org.sindice.core.analytics.commons.webapps.SparqledContextListener;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * --- Changed for the ASE use case ---</br></br> Configures the servlet based
 * upon configuration in sindice.home/{appname} folder, or if that folder is not
 * defined then ~/sindice/{appname}. Creates the folder if needed using default
 * logging and configuration provided with the web application. This context
 * listener should be moved into a shared sindice package.
 * 
 * @author robful modified by szymon danielczyk (made it platform independent)
 *         If application deployed in ROOT context tries get the applicationName
 *         from web.xml <context-param> <param-name>applicationName</param-name>
 *         <param-value>xxx</param-value> </context-param> if present will use
 *         ROOT_xxx in not will use ROOT as contextPath
 */
public class SummaryRestContextListener
extends SparqledContextListener {

  private static final Logger logger = LoggerFactory
                                     .getLogger(SummaryRestContextListener.class);

  public static final String  RECOMMENDER_BACKEND      = "recommender.backend";
  public static final String  RECOMMENDER_BACKEND_ARGS = "recommender.backendArgs";
  public static final String  PROXY_BACKEND            = "proxy.backend";
  public static final String  PROXY_BACKEND_ARGS       = "proxy.backendArgs";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    logger.info("initializing SummaryRest context");
    final ServletContext context = sce.getServletContext();
    final XMLConfiguration config = (XMLConfiguration) context.getAttribute("config");

    /*
     * TODO: Find a better way to get these backend configurations
     */
    final String recommenderBackend = getParameterWithLogging(config, RECOMMENDER_BACKEND, BackendType.HTTP.toString());
    final String[] recommenderBackendArgs = getParametersWithLogging(config, RECOMMENDER_BACKEND_ARGS, new String[] { "" });
    final String proxyBackend = getParameterWithLogging(config, PROXY_BACKEND, BackendType.HTTP.toString());
    final String[] proxyBackendArgs = getParametersWithLogging(config, PROXY_BACKEND_ARGS, new String[] { "" });

    context.setAttribute(RECOMMENDER_BACKEND, recommenderBackend);
    context.setAttribute(RECOMMENDER_BACKEND_ARGS, recommenderBackendArgs);
    context.setAttribute(PROXY_BACKEND, proxyBackend);
    context.setAttribute(PROXY_BACKEND_ARGS, proxyBackendArgs);

    logger.info("{}={} {}={} {}={} {}={}", new Object[] {
      PROXY_BACKEND, proxyBackend, PROXY_BACKEND_ARGS, Arrays.toString(proxyBackendArgs),
      RECOMMENDER_BACKEND, recommenderBackend, RECOMMENDER_BACKEND_ARGS, Arrays.toString(recommenderBackendArgs)
    });
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    logger.info("destroying context");
    super.contextDestroyed(sce);
  }

}
