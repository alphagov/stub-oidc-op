package uk.gov.ida.verifystubop.services;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.verifystubop.configuration.VerifyStubOpConfiguration;

public class RedisService {

    private RedisCommands<String, String> commands;

    public RedisService(VerifyStubOpConfiguration config) {
        startup(config);
    }

    public void startup(VerifyStubOpConfiguration config) {
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
