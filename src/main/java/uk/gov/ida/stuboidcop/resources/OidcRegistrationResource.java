package uk.gov.ida.stuboidcop.resources;

import com.nimbusds.jwt.SignedJWT;
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
import java.text.ParseException;
import java.util.stream.Collectors;

@Path("/")
public class OidcRegistrationResource {

    private RegistrationService registrationService;

    public OidcRegistrationResource(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpServletRequest request) throws IOException, ParseException {
        MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);
        final String stringJWT = multiReadHttpServletRequest.getReader().lines().collect(Collectors.joining());
        SignedJWT signedJWT = SignedJWT.parse(stringJWT);
        String response = registrationService.processHTTPRequest(signedJWT);

        return Response.ok(response).build();
    }
}
