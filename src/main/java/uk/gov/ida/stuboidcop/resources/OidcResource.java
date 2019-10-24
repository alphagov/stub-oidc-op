package uk.gov.ida.stuboidcop.resources;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.stuboidcop.services.RequestValidationService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class OidcResource {

    private final RequestValidationService requestValidationService;
    private static final Logger LOG = LoggerFactory.getLogger(OidcResource.class);


    public OidcResource(RequestValidationService requestValidationService) {
        this.requestValidationService = requestValidationService;
    }

    //TODO: The spec states there should be a post method for this endpoint as well
    @GET
    @Path("/authorize")
    public Response authorize(@Context UriInfo uriInfo) {
        URI uri = uriInfo.getRequestUri();

        try {
            AuthenticationRequest authenticationRequest = AuthenticationRequest.parse(uri);

            AuthenticationResponse response = requestValidationService.handleAuthenticationRequest(authenticationRequest);

            if (!response.indicatesSuccess()) {
                return Response.status(302).location(response.toErrorResponse().toURI()).build();
            } else {
                return Response.status(302).location(response.toSuccessResponse().toURI()).build();
            }

        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse URI: " + uri.toString() + " to authentication request", e);
        }
    }
}
