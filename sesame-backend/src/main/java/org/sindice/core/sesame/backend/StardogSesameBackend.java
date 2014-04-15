/**
 * Copyright (c) 2014 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.core.sesame.backend;

import org.openrdf.repository.Repository;
import org.sindice.core.sesame.backend.AbstractSesameBackend;
import org.sindice.core.sesame.backend.SesameBackend.QueryIterator.QueryResultProcessor;

import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.sesame.StardogRepository;

/**
 * 
 */
public class StardogSesameBackend<VALUE>
extends AbstractSesameBackend<VALUE> {

  private final String theDBName;
  private final String username;
  private final String password;

  public StardogSesameBackend(String theDBName, String username, String password) {
    this(null, theDBName, username, password);
  }

  public StardogSesameBackend(QueryResultProcessor<VALUE> qit, String theDBName, String username, String password) {
    super(qit);
    this.theDBName = theDBName;
    this.username = username;
    this.password = password;
  }

  @Override
  protected Repository getRepository() {
    return new StardogRepository(ConnectionConfiguration.to(theDBName).credentials(username, password));
  }

}
