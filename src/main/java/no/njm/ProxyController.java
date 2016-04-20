package no.njm;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/proxy")
public class ProxyController {

    private static final String PATH_IDENTIFIER = "path";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "9000";
    private static final String CONTEXT_PATH = "/";
    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_JSON = "application/json";

    @GET
    @Path("/{path:.*}")
    @Produces({"application/xml", "application/json"})
    public Response proxy(@Context UriInfo uriInfo,
                          @HeaderParam("Accept") String acceptHeader) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl(uriInfo));
        Response response = target.request()
                                  .accept(defaultAcceptHeader(acceptHeader))
                                  .get();
        return Response.status(response.getStatus())
                       .entity(response.readEntity(String.class))
                       .build();
    }

    @POST
    @Path("/{path:.*}")
    @Produces({"application/xml", "application/json"})
    public Response proxy(@Context UriInfo uriInfo,
                          @HeaderParam("Accept") String acceptHeader,
                          @HeaderParam("Content-type") String contentType,
                          String body) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildUrl(uriInfo));
        Response response = target.request()
                                  .accept(defaultAcceptHeader(acceptHeader))
                                  .post(Entity.entity(body, defaultContentType(contentType)));
        return Response.status(response.getStatus())
                       .entity(response.readEntity(String.class))
                       .build();
    }

    private String buildUrl(UriInfo uriInfo) {
        return new StringBuilder()
                .append("http://")
                .append(defaultHost())
                .append(":")
                .append(defaultPort())
                .append(CONTEXT_PATH)
                .append(requestPath(uriInfo.getPathParameters()))
                .append(queryParameters(uriInfo.getQueryParameters()))
                .toString();
    }

    private String defaultHost() {
        String host = System.getenv("PROXY_HOST");
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return DEFAULT_HOST;
    }

    private String defaultPort() {
        String port = System.getenv("PROXY_PORT");
        if (port != null && !port.isEmpty()) {
            return port;
        }
        return DEFAULT_PORT;
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

    private String defaultAcceptHeader(String acceptHeader) {
        if (acceptHeader.equals(APPLICATION_XML) || acceptHeader.equals(APPLICATION_JSON)) {
            return acceptHeader;
        }
        return APPLICATION_XML;
    }

    private String defaultContentType(String contentType) {
        if (contentType.equals(APPLICATION_XML) || contentType.equals(APPLICATION_JSON)) {
            return contentType;
        }
        return APPLICATION_XML;
    }
}

