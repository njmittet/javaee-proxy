package no.njm;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/proxy")
public class ProxyController {

    private static final Logger log = Logger.getLogger(ProxyController.class);

    private static final String PATH_IDENTIFIER = "path";
    private static final String BACKEND_HOST = "localhost";
    private static final String BACKEND_PORT = "8080";
    private static final String BACKEND_PATH = "/";

    @GET
    @Path("/{path:.*}")
    @Produces(MediaType.APPLICATION_XML)
    public Response proxy(@Context UriInfo uriInfo,
                          @HeaderParam("Accept") String acceptHeader) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl(uriInfo));
        Response backendResponse = target.request()
                                         .accept(acceptHeader)
                                         .get();
        return createReponse(backendResponse);
    }

    @POST
    @Path("/{path:.*}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response proxy(@Context UriInfo uriInfo,
                          @HeaderParam("Accept") String acceptHeader,
                          @HeaderParam("Content-type") String contentType,
                          String body) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl(uriInfo));
        Response backendResponse = target.request()
                                         .accept(acceptHeader)
                                         .post(Entity.entity(body, contentType));
        return createReponse(backendResponse);
    }

    private Response createReponse(Response response) {
        return Response.status(response.getStatus())
                       .entity(response.readEntity(String.class))
                       .build();
    }

    private String buildUrl(UriInfo uriInfo) {
        String url = new StringBuilder()
                .append("http://")
                .append(backendHost())
                .append(":")
                .append(backendPort())
                .append(backendPath())
                .append(requestPath(uriInfo.getPathParameters()))
                .append(queryParameters(uriInfo.getQueryParameters()))
                .toString();
        log.debug("Backend url: " + url);
        return url;
    }

    private String backendHost() {
        String host = System.getenv("PROXY_HOST");
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return BACKEND_HOST;
    }

    private String backendPath() {
        String contextPath = System.getenv("CONTEXT_PATH");
        if (contextPath != null && !contextPath.isEmpty()) {
            return contextPath;
        }
        return BACKEND_PATH;
    }

    private String backendPort() {
        String port = System.getenv("PROXY_PORT");
        if (port != null && !port.isEmpty()) {
            return port;
        }
        return BACKEND_PORT;
    }

    private String requestPath(MultivaluedMap<String, String> pathParameters) {
        List<String> params = pathParameters.get(PATH_IDENTIFIER);
        return params.get(0);
    }

    private String queryParameters(MultivaluedMap<String, String> queryParameterMap) {
        List<String> parameterList = new ArrayList<>();
        queryParameterMap.forEach((key, value) -> parameterList.add(explodeValues(key, value)));
        if (parameterList.isEmpty()) {
            return "";
        }
        return "?" + String.join("&", parameterList);
    }

    // Transforms: key = {value1, value2} to: {key=value1, key=value2}
    private String explodeValues(String key, List<String> values) {
        List<String> keyValues = new ArrayList<>();
        values.forEach(value -> keyValues.add(key + "=" + value));
        return String.join("&", keyValues);
    }
}

