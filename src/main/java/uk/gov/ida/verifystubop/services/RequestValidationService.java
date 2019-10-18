package uk.gov.ida.verifystubop.services;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;

public class RequestValidationService {

    private final TokenService tokenService;

    public RequestValidationService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public AuthenticationSuccessResponse handleAuthenticationRequest(AuthenticationRequest authenticationRequest) {

        validateRequestType(authenticationRequest);

        AuthorizationCode authorizationCode = tokenService.getAuthorizationCode();

        AccessToken accessToken = new BearerAccessToken();

        AccessToken returnedAccessToken = null;
        if (authenticationRequest.getResponseType().contains("token")) {
            returnedAccessToken = accessToken;
        }

        JWT idToken = tokenService.generateAndGetIdToken(authorizationCode, authenticationRequest, accessToken);

        return new AuthenticationSuccessResponse(
                authenticationRequest.getRedirectionURI(),
                authorizationCode,
                idToken,
                returnedAccessToken,
                authenticationRequest.getState(),
                null,
                null
        );
    }

    private static void validateRequestType(AuthenticationRequest authenticationRequest) {

        if (!authenticationRequest.getResponseType().contains("code") && !authenticationRequest.getResponseType().contains("id_token")) {
            throw new RuntimeException("Stub OP only supports response types which include code and id_token");
        }
    }
}
