package uk.gov.ida.verifystubop.services;

import com.nimbusds.openid.connect.sdk.AuthenticationRequest;

public class RequestValidationService {

    public static void validateRequestType(AuthenticationRequest authenticationRequest) {

        if (!authenticationRequest.getResponseType().contains("code")) {
            throw new RuntimeException("Stub OP only supports response types which include code");
        }

        if (!authenticationRequest.getResponseType().contains("id_token")) {
            throw new RuntimeException("Stub OP only supports response types which include id_token");
        }

        if (authenticationRequest.getResponseType().contains("token")) {
            throw new RuntimeException("Stub OP doesn't support response types which include token");
        }
    }
}
