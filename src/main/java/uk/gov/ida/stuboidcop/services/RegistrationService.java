package uk.gov.ida.stuboidcop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationRequest;
import org.glassfish.jersey.internal.util.Base64;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RegistrationService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .build();

    public OIDCClientRegistrationRequest processHTTPRequest(HTTPRequest httpRequest) {
        boolean passedValidation;
        OIDCClientRegistrationRequest registrationRequest;
        try {
           registrationRequest = OIDCClientRegistrationRequest.parse(httpRequest);
             passedValidation = validateRegistrationRequest(registrationRequest);
        } catch (java.text.ParseException | ParseException e) {
            throw new RuntimeException(e);
        }
        if (passedValidation) {
            return registrationRequest;
        } else {
            throw new RuntimeException("Failed Registation Request validation");
        }
    }

    private boolean validateRegistrationRequest(OIDCClientRegistrationRequest registrationRequest) throws java.text.ParseException {
        SignedJWT softwareStatement = registrationRequest.getSoftwareStatement();
        String softwareJwksEndpoint = registrationRequest.getSoftwareStatement().getJWTClaimsSet().getClaim("software_jwks_endpoint").toString();
        PublicKey publicKey = getSsaPublicKeyFromDirectory(softwareJwksEndpoint);
        return validateSsaSignatureAndAlgorithm(publicKey, softwareStatement);
    }

    private PublicKey getSsaPublicKeyFromDirectory(String softwareJwksEndpoint) {
        HttpResponse<String> response;
        try {
            URI ssaURI = new URI(softwareJwksEndpoint);
            response = sendHttpRequest(ssaURI);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String publicKeyString = response.body();
        publicKeyString = publicKeyString.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
        byte[] encodedPublicKey = Base64.decode(publicKeyString.getBytes());

        try {
            X509EncodedKeySpec x509publicKey = new X509EncodedKeySpec(encodedPublicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(x509publicKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateSsaSignatureAndAlgorithm(PublicKey publicKey, SignedJWT softwareStatement) {
        JWSAlgorithm algorithm1 = softwareStatement.getHeader().getAlgorithm();

        if (!algorithm1.equals(JWSAlgorithm.RS256)) {
            throw new RuntimeException("Wrong algorithm");
        }

        RSASSAVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        boolean isVerified;

        try {
             isVerified = softwareStatement.verify(verifier);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        return isVerified;
    }

    private HttpResponse<String> sendHttpRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("This could be 1 out of 2 exceptions. Take your pick", e);
        }
    }
}
