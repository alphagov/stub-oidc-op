package uk.gov.ida.stuboidcop.resources;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import net.minidev.json.JSONObject;
import uk.gov.ida.stuboidcop.services.RegistrationService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;

@Path("/")
public class OidcRegistrationResource {

    private RegistrationService registrationService;

    public OidcRegistrationResource(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String requestBody) throws ParseException, com.nimbusds.oauth2.sdk.ParseException {
        JSONObject jwtObject = JSONObjectUtils.parse(requestBody);
        String signedJwt = jwtObject.get("signed-jwt").toString();
        SignedJWT signedJWT = SignedJWT.parse(signedJwt);
        String response = registrationService.processHTTPRequest(signedJWT);

        return Response.ok(response).build();
    }
}
