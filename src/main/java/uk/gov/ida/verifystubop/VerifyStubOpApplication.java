package uk.gov.ida.verifystubop;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import uk.gov.ida.verifystubop.configuration.VerifyStubOpConfiguration;
import uk.gov.ida.verifystubop.resources.OidcResource;
import uk.gov.ida.verifystubop.services.RedisService;
import uk.gov.ida.verifystubop.services.TokenService;

public class VerifyStubOpApplication extends Application<VerifyStubOpConfiguration> {

    public static void main(String[] args) throws Exception {
        new VerifyStubOpApplication().run(args);
    }

    @Override
    public void run(VerifyStubOpConfiguration configuration, Environment environment) {
        RedisService redisService = new RedisService(configuration);

        TokenService tokenService = new TokenService(redisService);

        environment.jersey().register(new OidcResource(tokenService));
    }
}
