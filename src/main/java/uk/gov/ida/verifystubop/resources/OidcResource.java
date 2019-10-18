package uk.gov.ida.verifystubop.resources;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifystubop.services.RequestValidationService;

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

            AuthenticationSuccessResponse successResponse = requestValidationService.handleAuthenticationRequest(authenticationRequest);

            LOG.info("Success Response URI: " + successResponse.toURI().toString());

            return Response.status(302).location(successResponse.toURI()).build();
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse URI: " + uri.toString() + " to authentication request", e);
        }
    }
}
