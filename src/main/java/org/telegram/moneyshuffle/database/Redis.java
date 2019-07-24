package org.telegram.moneyshuffle.database;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import org.telegram.moneyshuffle.config.BotConfig;

public final class Redis {
    private static volatile Redis instance;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    private Redis(String url) {
        client = RedisClient.create(url);
        connection = client.connect();
    }

    public static Redis getInstance() {
        if (instance == null) {
            synchronized (Redis.class) {
                if (instance == null) {
                    instance = new Redis(BotConfig.REDIS);
                }
            }
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        connection.close();
        client.shutdown();
    }

    public boolean Exists(Long id) {
        RedisCommands<String, String> syncCommands = connection.sync();
        return syncCommands.exists(id.toString()) == 1;
    }

    public void Deposit(Long id, Integer deposit) {
        RedisCommands<String, String> syncCommands = connection.sync();
        if (deposit != 0) {
            String balance = syncCommands.get(id.toString());
            deposit += Integer.parseInt(balance);
        }

        syncCommands.set(id.toString(), deposit.toString());
    }

    public int Balance(Long id) {
        RedisCommands<String, String> syncCommands = connection.sync();
        if (Exists(id)) {
            return Integer.parseInt(syncCommands.get(id.toString()));
        }
        Deposit(id, 0);
        return 0;
    }
}