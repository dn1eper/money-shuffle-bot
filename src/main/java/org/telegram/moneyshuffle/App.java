package org.telegram.moneyshuffle;

import java.util.logging.Level;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.logging.BotLogger;

public final class App {
    final static String LOGTAG = "MAIN";
    public static ShuffleBot Bot;

    public static void main(String[] args) {
        BotLogger.setLevel(Level.CONFIG);
        //BotLogger.registerLogger(new ConsoleHandler());

        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            Bot = new ShuffleBot();
            telegramBotsApi.registerBot(Bot);
            ShuffleService.start();
        } 
        catch (Exception e) {
            BotLogger.error(LOGTAG, e.toString());
        }
    }
}
