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
package org.sindice.sparqled;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This {@link ServletContextListener} loads {@value #CONFIG_XML} and {@value #LOGGING_XML} configuration files,
 * located in the {@value #SPARQLED_HOME} folder.
 */
public class SparqledContextListener
implements ServletContextListener {

  private static final Logger logger        = LoggerFactory.getLogger(SparqledContextListener.class);

  public static final String  LOGGING_XML   = "logback.xml";
  public static final String  CONFIG_XML    = "config.xml";
  public static final String  SPARQLED_HOME = "sparqled/home";

  protected File sparqledHome;

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    logger.info("destroying context");
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final ServletContext context = sce.getServletContext();

    addEnvToContext(context);
    final String home = (String) context.getInitParameter(SPARQLED_HOME);
    sparqledHome = home == null ? null : new File(home);

    logger.info("Looking for configuration in [{}]", sparqledHome);
    if (sparqledHome == null || !sparqledHome.exists()) {
      throw new RuntimeException("Missing sparqled home: " + sparqledHome);
    }

    configureLogging(context, sparqledHome);
    final XMLConfiguration config = createXMLConfiguration(context);
    final File applicationConfigFile = new File(sparqledHome, CONFIG_XML);

    try {
      config.load(applicationConfigFile);
    } catch (ConfigurationException e) {
      throw new RuntimeException("Invalid configuration file: " + applicationConfigFile, e);
    }
    // important to set this as it is used later in logback.xml
    context.setAttribute("sparqled.home", home);

    context.setAttribute("config", config);
    logger.info("config now availabe via the following line of code\n"
          + "    XMLConfiguration appConfig = (XMLConfiguration) servletContext.getAttribute(\"config\");");
  }

  private XMLConfiguration createXMLConfiguration(final ServletContext context) {
    final XMLConfiguration config = new XMLConfiguration();
    final ConfigurationInterpolator interpolator = config.getInterpolator();
    final StrLookup defaultLookup = interpolator.getDefaultLookup();

    interpolator.setDefaultLookup(new StrLookup() {
      @Override
      public String lookup(String key) {
        if (context.getAttribute(key) != null) {
          return context.getAttribute(key).toString();
        }
        if (context.getInitParameter(key) != null) {
          return context.getInitParameter(key);
        }
        return defaultLookup.lookup(key);
      }
    });
    return config;
  }

  private void addEnvToContext(ServletContext context) {
    addToContext(context, System.getenv());
    addToContext(context, System.getProperties());
  }

  private void addToContext(ServletContext context, Map<?, ?> map) {
    for (Object key : map.keySet()) {
      if (context.getAttribute(key.toString()) == null) {
        context.setAttribute(key.toString(), map.get(key));
      }
    }
  }

  private void configureLogging(ServletContext context, final File configFolder) {
    final InputStream in = openLoggingConfig(configFolder);

    if (in == null) {
      throw new RuntimeException("Missing " + configFolder);
    }
    try {
      final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      final JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      try {
        final Enumeration<String> attributeNames = context.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
          String key = attributeNames.nextElement();
          Object value = context.getAttribute(key);
          if (value != null) {
            configurator.getContext().putProperty(key, value.toString());
          }
        }
        configurator.doConfigure(in);
      } catch (JoranException e) {
        throw new RuntimeException("logging configuration failed", e);
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        logger.warn("problem closing stream", e);
      }
    }
  }

  /**
   * Opens the logging configuration file logback.xml. If that doesn't exist try
   * to create it by copying default-logback.xml which should be provided in the
   * classes folder with the application.
   * 
   * @param configFolder the sparqled home folder
   * @return the {@link InputStream} to the logging configuration file file
   */
  private InputStream openLoggingConfig(File configFolder) {
    final File logConfigFile = new File(configFolder, LOGGING_XML);

    if (!logConfigFile.exists()) {
      throw new RuntimeException("Missing logging configuration file " + logConfigFile);
    } else {
      logger.info("Will try to configure logging from: {}", logConfigFile.getAbsolutePath());
    }
    try {
      return new FileInputStream(logConfigFile);
    } catch (Exception e) {
      throw new RuntimeException("Unable to get logging file", e);
    }
  }

  /**
   * Returns the value associated with the parameter "name" in the given config.
   * If the parameter is not found, the defaultValue is returned.
   * @param config the {@link XMLConfiguration configuration} of the application
   * @param name the parameter's name
   * @param defaultValue the default value of the parameter
   * @return the value associated with the given parameter's name
   */
  protected static final String getParameter(XMLConfiguration config, String name, String defaultValue) {
    if (config == null) {
      return defaultValue;
    }

    String value = config.getString(name);
    if (value == null) {
      logger.info("missing init parameter " + name + ", using default value");
      value = defaultValue;
    }
    logger.info("using " + name + "=[" + value + "]");
    return value;
  }

  /**
   * Returns the values associated with the parameter "name" in the given config.
   * If the parameter is not found, the defaultValues are returned.
   * @param config the {@link XMLConfiguration configuration} of the application
   * @param name the parameter's name
   * @param defaultValues the default set of values for the parameter
   * @return the values associated with the given parameter's name
   */
  protected static final String[] getParameters(XMLConfiguration config, String name, String[] defaultValues) {
    if (config == null) {
      return defaultValues;
    }

    String[] value = config.getStringArray(name);
    if (value.length == 0) {
      logger.info("missing init parameter {}, using default value", name);
      value = defaultValues;
    }
    logger.info("using {}={}", name, Arrays.toString(value));
    return value;
  }

}
