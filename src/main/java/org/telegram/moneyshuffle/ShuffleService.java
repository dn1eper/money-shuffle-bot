package org.telegram.moneyshuffle;

import java.util.HashMap;
import java.util.Random;
import org.telegram.telegrambots.meta.logging.BotLogger;

public class ShuffleService implements Runnable {
    static final String LOGTAG = "Shuffle Service";
    static final long INTERVAL = 10l * 1000 * 60;

    private static Thread t;
    private static ShuffleService service;
    private static HashMap<Long, Integer> members = new HashMap<Long, Integer>();

    boolean stopFlag = false;
    Random random = new Random();

    public static void start() {        
        if (t == null) {
            BotLogger.info(LOGTAG, "started");
            long sleep = INTERVAL - System.currentTimeMillis() % INTERVAL;
            
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                BotLogger.info(LOGTAG, "interrupted");
                return;
            }
            
            service = new ShuffleService();
            t = new Thread(service, "ShuffleService");
            t.start();
        }
    }

    public static void stop() {
        if (service != null) {
            service.stopFlag = true;
        }
    }

    public void run() {
        while (!stopFlag) {
            BotLogger.info(LOGTAG, "New shuffle loop started");
            try {
                Thread.sleep(INTERVAL - System.currentTimeMillis() % INTERVAL);
            }
            catch (InterruptedException e) {
                BotLogger.info(LOGTAG, "interrupted");
                break;
            }

            if (!members.isEmpty()) {
                long winner = MakeShuffle();
                App.Bot.ShuffleEnds(members.keySet(), winner, sum());
                BotLogger.info(LOGTAG, "Another shuffle ends. Winner - " + winner);
                members.clear();
            }  
        }
        BotLogger.info(LOGTAG, "stoped");
    }

    private Long MakeShuffle() {
        int sum = sum();
        int win = random.nextInt(sum-1) + 1;
        for (HashMap.Entry<Long, Integer> entry : members.entrySet()) {
            win -= entry.getValue();
            if (win <= 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    // public static intervace
    public static void addMember(Long id, int sum) {
        if (members.containsKey(id)) {
            sum += members.get(id);
        }
        members.put(id, sum);
    }

    public static int participats() {
        return members.size();
    }

    public static int sum() {
        return members.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static int sum(Long id) {
        if (members.containsKey(id)) {
            return members.get(id);
        }
        else return 0;
    }

    public static int ends() {
        long ends =  (INTERVAL / 1000) - (System.currentTimeMillis() % INTERVAL) / 1000;
        if (service == null) 
        {
            ends += INTERVAL / 1000;
        }
        return (int) ends;
    }
}

