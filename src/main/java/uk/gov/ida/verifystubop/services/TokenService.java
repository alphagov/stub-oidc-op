package uk.gov.ida.verifystubop.services;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.langtag.LangTag;
import com.nimbusds.langtag.LangTagException;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifystubop.configuration.VerifyStubOpConfiguration;

import java.util.Arrays;
import java.util.Date;

public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    private RedisService redisService;
    private VerifyStubOpConfiguration configuration;

    public TokenService(RedisService redisService, VerifyStubOpConfiguration configuration) {
        this.redisService = redisService;
        this.configuration = configuration;
    }

    public AuthorizationCode generateTokensAndGetAuthCode() {

        IDTokenClaimsSet idTokenClaimsSet = new IDTokenClaimsSet(
                new Issuer("iss"),
                new Subject("sub"),
                Arrays.asList(new Audience("aud")),
                new Date(),
                new Date());

        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = idTokenClaimsSet.toJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse IDTokenClaimsSet to JWTClaimsSet", e);
        }
        //The ID Token will probably need to be signed, although this is fine for now
        JWT idToken = new PlainJWT(jwtClaimsSet);
        AuthorizationCode authCode = new AuthorizationCode();
        AccessToken accessToken = new BearerAccessToken();
        storeTokens(idToken, accessToken, authCode);
        createUserInfo(accessToken);
        return authCode;
    }

    private void storeTokens(JWT idToken, AccessToken accessToken, AuthorizationCode authCode) {

        OIDCTokens oidcTokens = new OIDCTokens(idToken, accessToken, null);

        redisService.set(authCode.getValue(), oidcTokens.toJSONObject().toJSONString());
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
}
