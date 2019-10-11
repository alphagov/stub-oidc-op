package uk.gov.ida.verifystubop.resources;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import uk.gov.ida.verifystubop.services.TokenService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class OidcResource {

    private TokenService tokenService;

    public OidcResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    //TODO: The spec states there should be a post method for this endpoint as well
    @GET
    @Path("/authorize")
    public Response authorize(@Context UriInfo uriInfo) {
        URI uri = uriInfo.getRequestUri();

        try {
            AuthenticationRequest authenticationRequest = AuthenticationRequest.parse(uri);

            AuthorizationCode authorizationCode = tokenService.generateTokensAndGetAuthCode();

            AuthenticationSuccessResponse successResponse =
                    new AuthenticationSuccessResponse(
                            authenticationRequest.getRedirectionURI(),
                            authorizationCode,
                            null,
                            null,
                            authenticationRequest.getState(),
                            null,
                            null
                    );
            return Response.status(302).location(successResponse.toURI()).build();
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse URI: " + uri.toString() + " to authentication request", e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Response getProviderTokens(
            @FormParam("code") @NotNull AuthorizationCode authCode) {

        OIDCTokenResponse response = new OIDCTokenResponse(tokenService.getTokens(authCode));
        return Response.ok(response.toJSONObject()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userinfo")
    public Response getUserInfo(@HeaderParam("Authorization") @NotNull String authorizationHeader) {
        try {
            //This will need to be used to get the user info but we're not using it for now
            AccessToken accessToken = AccessToken.parse(authorizationHeader);

            UserInfo userInfo = new UserInfo(new Subject("subject"));

            return Response.ok(userInfo.toJSONObject()).build();
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse authorization header: " + authorizationHeader + " to access token", e);
        }
    }
}
