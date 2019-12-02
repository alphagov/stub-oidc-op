package uk.gov.ida.stuboidcop.configuration;

import io.dropwizard.Configuration;

public class StubOidcOpConfiguration extends Configuration {

    private String redisURI;

    private String directoryURI;

    private String verifiableCredentialURI;

    public String getRedisURI() {
        return redisURI;
    }

    public String getDirectoryURI() {
        return directoryURI;
    }

    public String getVerifiableCredentialURI() {
        return verifiableCredentialURI;
    }
}
