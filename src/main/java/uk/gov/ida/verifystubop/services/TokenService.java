package uk.gov.ida.verifystubop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.AccessTokenHash;
import com.nimbusds.openid.connect.sdk.claims.CodeHash;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private static final String ISSUER = "verify-stub-op";
    private RedisService redisService;

    public TokenService(RedisService redisService) {
        this.redisService = redisService;
    }

    public JWT generateAndGetIdToken(AuthorizationCode authCode, AuthenticationRequest authRequest, AccessToken accessToken) {

        CodeHash cHash = CodeHash.compute(authCode, JWSAlgorithm.RS256);
        AccessTokenHash aHash = AccessTokenHash.compute(accessToken, JWSAlgorithm.RS256);
        IDTokenClaimsSet idTokenClaimsSet = new IDTokenClaimsSet(
                new Issuer(ISSUER),
                new Subject(),
                Arrays.asList(new Audience(authRequest.getClientID())),
                new Date(),
                new Date());
        idTokenClaimsSet.setCodeHash(cHash);
        idTokenClaimsSet.setNonce(authRequest.getNonce());
        idTokenClaimsSet.setAccessTokenHash(aHash);
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = idTokenClaimsSet.toJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse IDTokenClaimsSet to JWTClaimsSet", e);
        }

        RSAKey signingKey = createSigningKey();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build();
        SignedJWT idToken;

        try {
            JWSSigner signer = new RSASSASigner(signingKey);
            idToken = new SignedJWT(jwsHeader, jwtClaimsSet);
            idToken.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException();
        }

        storeTokens(idToken, accessToken, authCode);
        createUserInfo(accessToken);

        return idToken;
    }

    public OIDCTokens getTokens(AuthorizationCode authCode) {

        String tokens = redisService.get(authCode.getValue());

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(JSONObjectUtils.parse(tokens));
            return OIDCTokens.parse(jsonObject);
        } catch (java.text.ParseException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthorizationCode getAuthorizationCode() {

        AuthorizationCode authCode = new AuthorizationCode();
        return authCode;
    }

    public UserInfo getUserInfo(AccessToken accessToken) {
        String serialisedUserInfo = redisService.get(accessToken.getValue());
        try {
            LOG.info("Have successfully retrieved user info from redis using access token");
            return new UserInfo(new JSONObject(JSONObjectUtils.parse(serialisedUserInfo)));
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeTokens(JWT idToken, AccessToken accessToken, AuthorizationCode authCode) {

        OIDCTokens oidcTokens = new OIDCTokens(idToken, accessToken, null);

        redisService.set(authCode.getValue(), oidcTokens.toJSONObject().toJSONString());
    }

    private RSAKey createSigningKey() {
        try {
            return new RSAKeyGenerator(2048).keyID("123").generate();
        } catch (JOSEException e) {
            throw new RuntimeException("Unable to create RSA key");
        }
    }

    private void createUserInfo(AccessToken accessToken) {
        UserInfo userInfo = new UserInfo(new Subject());
        userInfo.setGender(new Gender("male"));
        userInfo.setFamilyName("Smith");
        userInfo.setGivenName("John");
        userInfo.setName("John Smith");
        userInfo.setPhoneNumber("01234567890");
        userInfo.setPhoneNumberVerified(false);
        redisService.set(accessToken.getValue(), userInfo.toJSONObject().toJSONString());
    }
}
