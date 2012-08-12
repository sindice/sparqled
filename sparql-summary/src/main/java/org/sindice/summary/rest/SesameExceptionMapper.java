package org.sindice.summary.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.sindice.core.sesame.backend.SesameBackendException;

@Provider
public class SesameExceptionMapper
implements ExceptionMapper<SesameBackendException> {

  @Override
  public Response toResponse(SesameBackendException e) {
    return new WebApplicationException(e, 400).getResponse();
  }

}
