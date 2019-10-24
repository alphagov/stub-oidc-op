package uk.gov.ida.stuboidcop.services;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.stuboidcop.configuration.StubOidcOpConfiguration;

public class RedisService {

    private RedisCommands<String, String> commands;

    public RedisService(StubOidcOpConfiguration config) {
        startup(config);
    }

    public void startup(StubOidcOpConfiguration config) {
        RedisClient client = RedisClient.create("redis://" + config.getRedisURI());
        commands = client.connect().sync();
    }

    public void set(String key, String value) {
        commands.set(key, value);
    }

    public String get(String key) {
        return commands.get(key);
    }
}
