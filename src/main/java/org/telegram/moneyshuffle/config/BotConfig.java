package org.telegram.moneyshuffle.config;

import java.util.Date;
import java.text.SimpleDateFormat;

public final class BotConfig {
    public static final String TOKEN = System.getenv("BOT_TOKEN");
    public static final String NAME = "Money Shuffle Bot";

    public static final String REDIS = System.getenv("REDISCLOUD_URL");

    public static String DateTimeNow() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        return "[" + simpleDateFormat.format(new Date()) + "] ";
    }
}