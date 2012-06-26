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
package org.sindice.servlet.sparqlqueryservlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * --- Changed for the ASE use case ---</br></br>
 * 
 * Configures the servlet based upon configuration in sindice.home/{appname}
 * folder, or if that folder is not defined then ~/sindice/{appname}. Creates
 * the folder if needed using default logging and configuration provided with
 * the web application. This context listener should be moved into a shared
 * sindice package.
 * 
 * @author robful modified by szymon danielczyk (made it platform independent)
 *         If application deployed in ROOT context tries get the applicationName
 *         from web.xml <context-param> <param-name>applicationName</param-name>
 *         <param-value>xxx</param-value> </context-param>
 * 
 *         if present will use ROOT_xxx in not will use ROOT as contextPath
 * 
 */
public class ServletConfigurationContextListener
implements ServletContextListener {

  private static final Logger logger          = LoggerFactory.getLogger(ServletConfigurationContextListener.class);
  private static final String DEFAULT_LOGGING = "default-logback.xml";

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    logger.info("destroying context");
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final ServletContext context = sce.getServletContext();
    String contextPath = context.getContextPath().replaceFirst("^/", "");
    if (contextPath.equals("")) {
      // application deployed in ROOT context
      String applicationName = context.getInitParameter("applicationName");
      if (applicationName != null && !applicationName.equals("")) {
        contextPath = "ROOT_" + applicationName;
      } else {
        contextPath = "ROOT";
      }
      logger.warn("Application deployed in ROOT context.");
    } else {
      logger.info("Application deployed in [" + contextPath + "] context.");
    }
    logger.info("Will use [" + contextPath + "] as a part of a path for reading/storing configuration");
    
    String servletContextName = context.getServletContextName();
    addEnvToContext(context);
    
    String sindiceHome = null;
    // FOR DEBIAN PACKAGE LOD2 PROJECT INTEGRATION
    // check that following directory exists
    // if yes please use it
    if (new File("/etc/" + contextPath + "/config.xml").exists()) {
      sindiceHome = "/etc";
      logger.info("Found config.xml at [/etc/" + contextPath
          + "/config.xml]. Setting sindiceHome to : [" + sindiceHome + "]");
    } else {
      logger.info("File /etc/" + contextPath
          + "/config.xml does not exists will try to determine sindiceHome");
    }
    // END DEBIAN PACKAGE LOD2 PROJECT INTEGRATION
    if (sindiceHome == null && context.getAttribute("sindice.home") != null) {
      sindiceHome = (String) context.getAttribute("sindice.home");
      logger.info("Setting sindiceHome from sindice.home env variable to ["
          + sindiceHome + "]");
    }
    if (sindiceHome == null && context.getAttribute("SINDICE_HOME") != null) {
      sindiceHome = (String) context.getAttribute("SINDICE_HOME");
      logger.info("Setting sindiceHome from SINDICE_HOME env variable to ["
          + sindiceHome + "]");
    }
    if (sindiceHome == null || "".equals(sindiceHome.trim())) {
      String userHome = (String) context.getAttribute("user.home");
      sindiceHome = (userHome == null ? "" : userHome) + File.separatorChar
          + "sindice";
      logger.warn(
          "Neither sindice.home nor SINDICE_HOME are not defined, assuming {}",
          sindiceHome);
    }
    
    logger.info("Looking for configuration in [" + sindiceHome
        + File.separatorChar + contextPath + "]");
    
    // important to set these two as they are used later in logback.xml
    context.setAttribute("sindice.home", sindiceHome);
    context.setAttribute("app.name", contextPath);
    
    File configFolder = new File(sindiceHome + File.separatorChar + contextPath);
    if (!(configFolder.exists() && configFolder.isDirectory())) {
      logger.warn("Missing configuration folder {}", configFolder);
      if (configFolder.mkdirs()) {
        logger.warn("Creating default configuration at " + configFolder);
        
      } else {
        // set logging level to INFO
        configureLogging(context, configFolder);
        return;
      }
    }
    // does a specific folder exist for this servlet context?
    if (servletContextName == null) {
      logger.error("specify display-name element in web.xml !!! ");
    } else {
      File specificFolder = new File(configFolder, servletContextName);
      if (specificFolder.exists() && specificFolder.isDirectory()) {
        configFolder = specificFolder;
      }
    }
    logger.info("loading configuration from folder {}", configFolder);
    configureLogging(context, configFolder);
    final XMLConfiguration config = createXMLConfiguration(context);
    File applicationConfigFile = new File(configFolder, "config.xml");
    if (!applicationConfigFile.exists()) {
      logger.warn("missing application config file {}", applicationConfigFile);
      loadDefaultConfiguration(config, applicationConfigFile);
    } else {
      try {
        config.load(applicationConfigFile);
        logger.info("parsed {}", applicationConfigFile);
      } catch (ConfigurationException e) {
        logger.error("Could not load configuration from {}",
            applicationConfigFile, e);
        loadDefaultConfiguration(config, null);
      }
    }
    context.setAttribute("config", config);
    logger.info("config now availabe via the following line of code\n"
                + "    XMLConfiguration appConfig = (XMLConfiguration) servletContext.getAttribute(\"config\");");
    
    logger.info("Starting up {}", servletContextName);
  }
  
  private XMLConfiguration createXMLConfiguration(final ServletContext context) {
    final XMLConfiguration config = new XMLConfiguration();
    ConfigurationInterpolator interpolator = config.getInterpolator();
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
  
  private void loadDefaultConfiguration(final XMLConfiguration config,
      File saveAs) {
    logger.info("loading default configuration");
    InputStream in = ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream("default-config.xml");
    if (in == null) {
      logger.error("application is missing default-config.xml from classpath");
    } else {
      try {
        config.load(in);
        if (saveAs != null) {
          try {
            saveAs.getParentFile().mkdirs();
            config.save(saveAs);
            logger.info("wrote default configuration to {}", saveAs);
          } catch (ConfigurationException e) {
            logger.warn("Could not write configuration to {}", saveAs, e);
          }
        }
      } catch (ConfigurationException e) {
        logger.error("could not load default-config.xml", e);
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("problem closing stream", e);
        }
      }
    }
  }
  
  private void addEnvToContext(ServletContext context) {
    addToContext(context, System.getenv());
    addToContext(context, System.getProperties());
  }
  
  private void addToContext(ServletContext context, Map map) {
    for (Object key : map.keySet()) {
      if (context.getAttribute(key.toString()) == null) {
        context.setAttribute(key.toString(), map.get(key));
      }
    }
  }
  
  private void configureLogging(ServletContext context, final File configFolder) {
    InputStream in = openLoggingConfig(configFolder);
    if (in == null) {
      setDefaultLogging();
      return;
    }
    try {
      final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      final JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      try {
        Enumeration<String> attributeNames = context.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
          String key = attributeNames.nextElement();
          Object value = context.getAttribute(key);
          if (value != null) {
            configurator.getContext().putProperty(key, value.toString());
          }
        }
        configurator.doConfigure(in);
      } catch (JoranException e) {
        lc.reset();
        setDefaultLogging();
        logger.error("logging configuration failed", e);
        logger.warn("using servlet container logging configuration");
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
   * @param configFolder
   * @return
   */
  private InputStream openLoggingConfig(File configFolder) {
    String loggerConfigFileName = configFolder.getAbsolutePath()
        + File.separatorChar + "logback.xml";
    File logConfigFile = new File(loggerConfigFileName);
    if (!logConfigFile.exists()) {
      logger.warn("Missing logging configuration file " + loggerConfigFileName);
      createLogConfigFile(logConfigFile);
    } else {
      logger.info("Will try to configure logging from:"
          + logConfigFile.getAbsolutePath());
    }
    if (logConfigFile.exists()) {
      try {
        return new FileInputStream(logConfigFile);
      } catch (Exception e) {
        logger.warn("problem reading log file", e);
      }
    }
    return ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream(DEFAULT_LOGGING);
  }
  
  private void createLogConfigFile(File logConfigFile) {
    try {
      InputStream in = ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream(DEFAULT_LOGGING);
      if (in == null) {
        logger.warn("missing default-logging.xml from classpath");
      } else {
        // try to write the application logging file so admin can change it.
        try {
          logConfigFile.getParentFile().mkdirs();
          FileOutputStream out = new FileOutputStream(logConfigFile);
          try {
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
              out.write(buff, 0, read);
            }
          } finally {
            out.close();
          }
        } finally {
          in.close();
        }
      }
    } catch (IOException e) {
      logger.warn("couldn't write logConfigFile {}", logConfigFile, e);
    }
  }
  
  private void setDefaultLogging() {
    final Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    if (rootLogger instanceof ch.qos.logback.classic.Logger) {
      ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
    }
    logger.warn("Log level set to INFO, create the logging configuration file to change this.");
  }
  
  protected static final String getParameterWithLogging(XMLConfiguration config, String name, String defaultValue) {
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
  
  protected static final String[] getParametersWithLogging(XMLConfiguration config, String name, String[] defalt) {
    if (config == null) {
      return defalt;
    }
    
    String[] value = config.getStringArray(name);
    if (value.length == 0) {
      logger.info("missing init parameter " + name + ", using default value");
      value = defalt;
    }
    StringBuilder sb = new StringBuilder();
    for (String s : value) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(s);
    }
    logger.info("using " + name + "=[" + sb + "]");
    return value;
  }
  
}
