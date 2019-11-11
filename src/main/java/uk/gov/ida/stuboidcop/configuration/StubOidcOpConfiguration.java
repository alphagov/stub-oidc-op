package uk.gov.ida.stuboidcop.configuration;

import io.dropwizard.Configuration;

public class StubOidcOpConfiguration extends Configuration {

    private String redisURI;

    private String brokerURI;

    public String getRedisURI() {
        return redisURI;
    }

    public String getBrokerURI() {
        return brokerURI;
    }
}
