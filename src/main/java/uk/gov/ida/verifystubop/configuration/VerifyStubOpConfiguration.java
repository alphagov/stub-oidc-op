package uk.gov.ida.verifystubop.configuration;

import io.dropwizard.Configuration;

public class VerifyStubOpConfiguration extends Configuration {

    private String redisURI;

    public String getRedisURI() {
        return redisURI;
    }
}
