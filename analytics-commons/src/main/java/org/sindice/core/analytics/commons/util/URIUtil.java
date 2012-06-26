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
package org.sindice.core.analytics.commons.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sindice.core.analytics.commons.summary.AnalyticsClassAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InternetDomainName;

/**
 * @author diego
 */
public class URIUtil {

  private static final Logger logger = LoggerFactory.getLogger(URIUtil.class);

  public static boolean isClassAttribute(String predicate) {
    return AnalyticsClassAttributes.isClass(predicate);
  }

  /**
   * Extract the domain name from the URL, then extract its second-level domain
   * name sing {@link URIUtil#getSndDomain(String)}.
   * @param url
   * @return
   */
  public static String getSndDomainFromUrl(String url) {
    if (url == null) {
      return null;
    }
    try {
      final String domain = URI.create(url).getHost();
      if (domain != null) {
        return getSndDomain(domain);
      }
    } catch (IllegalArgumentException e) {
    }
    logger.debug("The url={} isn't valid, skipping it", url);
    return null;
  }

  /**
   * Return the second-level domain name. Returns null if the domain is not valid.
   * This method normalises domain names by removing the leading www sub-domain,
   * if present.
   * @param domain
   * @return
   */
  public static String getSndDomain(String domain) {
    if (domain == null) {
      return null;
    }
    // Remove www subdomain if it exists
    if (domain.startsWith("www.")) {
      domain = domain.substring(4);
    }
    if (InternetDomainName.isValid(domain)) { // the domain is valid according to the RFC3490
      final InternetDomainName idn = InternetDomainName.from(domain);
      if (idn.hasPublicSuffix()) { // the domain has a public suffix
        if (idn.isUnderPublicSuffix()) {
          return idn.topPrivateDomain().name();
        } else if (idn.hasParent()) {
          final List<String> parts = idn.parts();
          return parts.get(parts.size() - 2).concat(".").concat(parts.get(parts.size() - 1));
        }
      }
    }
    return null;
  }

  public static String normalizePrefix(String url) {
    final String[] prefixes = new String[] { "http://", "https://", "ftp://", "www." };
    for (String p : prefixes) {
      if (url.startsWith(p)) {
        url = url.substring(p.length());
      }
    }
    return url;
  }

  public static String normalize(String urlString)
  throws URISyntaxException {
    if ("".equals(urlString)) { // disallow empty URL
      throw new URISyntaxException(urlString, "empty URL");
    }

    urlString = urlString.trim(); // remove extra spaces

    final URI url = new URI(urlString);

    String scheme = url.getScheme();
    String userInfo = url.getUserInfo();
    String host = url.getHost();
    int port = url.getPort();
    String path = url.getPath();
    final String query = url.getQuery();
    String fragment = url.getFragment();

    scheme = normalizeScheme(scheme);
    userInfo = normalizeUserInfo(userInfo);
    host = normalizeHost(host);
    port = normalizePort(scheme, port);
    path = normalizePath(path);
    // fragment = normalizeFragment(fragment);

    return new URI(scheme, userInfo, host, port, path, query, fragment).normalize().toString();
  }

  private static String normalizePath(String path) {
    // if path empty or path is a slash, add a slash
    if (path == null || path.isEmpty() || path.equals("/")) {
      return "/";
    }

    String directory = extractPathDirectory(path);
    String localname = extractLocalname(path);

    directory = normalizeDirectory(directory, localname);
    localname = normalizeLocalname(localname);

    path = directory + localname;
    path = normalizeDotSegment(path);
    return path;
  }

  private static String normalizeLocalname(final String localname) {
    // If localname is a default page, return empty string
    if (DEFAULT_PAGES.contains(localname.toLowerCase())) {
      return "";
    }
    return localname;
  }

  private static String normalizeDirectory(String directory,
                                           final String localname) {
    // lowercase directory
    directory = directory.toLowerCase();
    // remove slash if localname empty
    if (localname.isEmpty()) {
      directory = directory.replaceFirst("/$", "");
    }
    return directory;
  }

  private static String extractPathDirectory(final String path) {
    final int offset = path.lastIndexOf('/');
    return path.substring(0, offset + 1);
  }

  private static String extractLocalname(final String path) {
    final int offset = path.lastIndexOf('/');
    return path.substring(offset + 1, path.length());
  }

  private static String normalizeDotSegment(String path) {
    // remove dot-segment prefix (not removed by URI#normalize)
    while (path.startsWith("/./") || path.startsWith("/../")) {
      path = path.replaceFirst("/[\\.]+/", "/");
    }
    return path;
  }

  private static int normalizePort(final String scheme, int port) {
    if (port == getDefaultPort(scheme)) { // uses default port
      port = -1; // so don't specify it
    }
    return port;
  }

  private static String normalizeHost(String host) {
    if (host != null) {
      host = host.toLowerCase(); // lowercase host
    }
    return host;
  }

  private static String normalizeUserInfo(final String userInfo) {
    return null; // remove user info
  }

  private static String normalizeScheme(String scheme) {
    if (scheme != null) {
      scheme = scheme.toLowerCase(); // lowercase scheme
    }
    return scheme;
  }

  private static int getDefaultPort(final String scheme) {
    if (DEFAULT_PORTS.containsKey(scheme)) {
      return DEFAULT_PORTS.get(scheme);
    }
    return -1;
  }

  private static final Map<String, Integer> DEFAULT_PORTS;
  private static final Set<String>          DEFAULT_PAGES;

  static {
    DEFAULT_PORTS = new HashMap<String, Integer>();
    DEFAULT_PORTS.put("http", 80);
    DEFAULT_PORTS.put("https", 443);
    DEFAULT_PORTS.put("ftp", 21);

    DEFAULT_PAGES = new HashSet<String>();
    DEFAULT_PAGES.add("index.htm");
    DEFAULT_PAGES.add("index.html");
    DEFAULT_PAGES.add("default.htm");
    DEFAULT_PAGES.add("default.html");
  }

}
