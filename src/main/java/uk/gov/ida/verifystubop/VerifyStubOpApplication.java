package uk.gov.ida.verifystubop;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import uk.gov.ida.verifystubop.configuration.VerifyStubOpConfiguration;
import uk.gov.ida.verifystubop.resources.OidcFormPostResource;
import uk.gov.ida.verifystubop.resources.OidcResource;
import uk.gov.ida.verifystubop.resources.TokenResource;
import uk.gov.ida.verifystubop.services.RedisService;
import uk.gov.ida.verifystubop.services.RequestValidationService;
import uk.gov.ida.verifystubop.services.TokenService;

public class VerifyStubOpApplication extends Application<VerifyStubOpConfiguration> {

    public static void main(String[] args) throws Exception {
        new VerifyStubOpApplication().run(args);
    }

    @Override
    public void run(VerifyStubOpConfiguration configuration, Environment environment) {
        RedisService redisService = new RedisService(configuration);

        TokenService tokenService = new TokenService(redisService);

        RequestValidationService requestValidationService = new RequestValidationService(tokenService);

        environment.jersey().register(new OidcResource(requestValidationService));
        environment.jersey().register(new TokenResource(tokenService));
        environment.jersey().register(new OidcFormPostResource(requestValidationService));
    }

    @Override
    public void initialize(final Bootstrap<VerifyStubOpConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
    }
}
