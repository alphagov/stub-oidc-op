package uk.gov.ida.stuboidcop.resources;

import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.ServletUtils;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationRequest;
import uk.gov.ida.stuboidcop.http.MultiReadHttpServletRequest;
import uk.gov.ida.stuboidcop.services.RegistrationService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
public class OidcRegistrationResource {

    private RegistrationService registrationService;

    public OidcRegistrationResource(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request) {
        OIDCClientRegistrationRequest registrationRequest;
        MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);
        try {
            HTTPRequest httpRequest = ServletUtils.createHTTPRequest(multiReadHttpServletRequest);
            registrationRequest = registrationService.processHTTPRequest(httpRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.ok(registrationRequest.getOIDCClientMetadata().toJSONObject()).build();
    }
}
