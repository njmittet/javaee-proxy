package no.njm.util;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    @Inject
    Logger log;

    @Override
    public Response toResponse(Exception e) {
        log.error(e);
        return Response.serverError().build();
    }
}
