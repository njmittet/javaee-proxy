package no.njm;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/proxy")
public class ProxyController {

    private static final Logger log = Logger.getLogger(ProxyController.class.getName());

    private static final String QUERY_PARAM_IDENTIFIER = "?";
    private static final String QUERY_PARAM_DELIMITER = "&";
    private static final String KEY_VALUE_DELIMITER = "=";
    private static final String PATH_IDENTIFIER = "path";
    private static final String EMPTY_STRING = "";
    private static final String CREDENTIALS = "Basic username:password";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "8080";

    @GET
    @Path("/{path: .*}")
    @Produces("application/json")
    public Response proxyGetRequest(@Context UriInfo uriInfo) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(buildProxyUrl(uriInfo));
        Response response = target.request()
                                  .header("Authorization", CREDENTIALS)
                                  .accept("application/json")
                                  .get();
        if (responseStatusOK(response)) {
            return Response.ok(response.readEntity(String.class)).build();
        }
        return Response.status(response.getStatus()).build();
    }

    // @POST
    //    Response response = target.request().header("Authorization", "Basic test123")
    //                              .acceptEncoding("gzip, deflate")
    //                              .post(Entity.entity("requestBody, "application/x-www-form-urlencoded"));

    private boolean responseStatusOK(Response response) {
        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    private String buildProxyUrl(UriInfo uriInfo) {
        return new StringBuilder()
                .append("http://")
                .append(resolveProxyHost())
                .append(":")
                .append(resolveProxyPort())
                .append("/")
                .append(requestPath(uriInfo.getPathParameters()))
                .append(requestQueryParameters(uriInfo.getQueryParameters()))
                .toString();
    }

    private String resolveProxyHost() {
        String host = System.getenv("PROXY_HOST");
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return DEFAULT_HOST;
    }

    private String resolveProxyPort() {
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

    private String requestQueryParameters(MultivaluedMap<String, String> queryParameterMap) {
        List<String> parameterList = new ArrayList<>();
        queryParameterMap.forEach((key, value) -> parameterList.add(explodeParameterArray(key, value)));
        if (!parameterList.isEmpty()) {
            return QUERY_PARAM_IDENTIFIER + String.join(QUERY_PARAM_DELIMITER, parameterList);
        }
        return EMPTY_STRING;
    }

    private String explodeParameterArray(String key, List<String> values) {
        List<String> keyValues = new ArrayList<>();
        values.forEach(value -> keyValues.add(key + KEY_VALUE_DELIMITER + value));
        return String.join(QUERY_PARAM_DELIMITER, keyValues);
    }
}

