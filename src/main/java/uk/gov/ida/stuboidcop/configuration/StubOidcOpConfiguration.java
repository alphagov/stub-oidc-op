package uk.gov.ida.stuboidcop.configuration;

import io.dropwizard.Configuration;

public class StubOidcOpConfiguration extends Configuration {

    private String redisURI;

    public String getRedisURI() {
        return redisURI;
    }
}
