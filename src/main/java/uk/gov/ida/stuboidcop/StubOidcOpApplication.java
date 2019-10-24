package uk.gov.ida.stuboidcop;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import uk.gov.ida.stuboidcop.configuration.StubOidcOpConfiguration;
import uk.gov.ida.stuboidcop.resources.OidcFormPostResource;
import uk.gov.ida.stuboidcop.resources.OidcResource;
import uk.gov.ida.stuboidcop.resources.TokenResource;
import uk.gov.ida.stuboidcop.services.RedisService;
import uk.gov.ida.stuboidcop.services.RequestValidationService;
import uk.gov.ida.stuboidcop.services.TokenService;

public class StubOidcOpApplication extends Application<StubOidcOpConfiguration> {

    public static void main(String[] args) throws Exception {
        new StubOidcOpApplication().run(args);
    }

    @Override
    public void run(StubOidcOpConfiguration configuration, Environment environment) {
        RedisService redisService = new RedisService(configuration);

        TokenService tokenService = new TokenService(redisService);

        RequestValidationService requestValidationService = new RequestValidationService(tokenService);

        environment.jersey().register(new OidcResource(requestValidationService));
        environment.jersey().register(new TokenResource(tokenService));
        environment.jersey().register(new OidcFormPostResource(requestValidationService));
    }

    @Override
    public void initialize(final Bootstrap<StubOidcOpConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
    }
}
