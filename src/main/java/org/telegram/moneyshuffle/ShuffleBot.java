package org.telegram.moneyshuffle;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.moneyshuffle.config.BotConfig;
import org.telegram.moneyshuffle.database.Redis;;

public class ShuffleBot extends TelegramLongPollingBot {
    public ShuffleBot() {
        BotLogger.info("Bot", "started");
    }

    @Override
    public void onUpdateReceived(Update update) {
        Redis db = Redis.getInstance();
        String answer;

        if(update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            Long user_id = message.getChatId();

            ReplyKeyboard keyboard = null;

            if (ReplyTypes.START.getKey().equals(text)) {
                if (db.Exists(user_id)) {
                    answer = "I don't understand you. Use main menu.";
                }
                else {
                    answer = "Welcome to Shuffle Bot!";
                    db.Deposit(user_id, 0);
                }
                keyboard = getMainKeyboard();
            }
            else if (ReplyTypes.SHUFFLE.toString().equals(text)) {
                answer = "Choose a sum for shuffle";
                keyboard = getShuffleSumInlineKeyboard();
            }
            else if (ReplyTypes.BALANCE.toString().equals(text)) {
                int balance = db.Balance(user_id);
                answer = String.format("Your id:       %d.\nYour balance: %d rub.", + user_id, balance);
                keyboard = getBalanceInlineKeyboard();
            }
            else if (ReplyTypes.ABOUT.toString().equals(text)) {
                answer = "Shuffling money every 10 minutes.";
            }
            else if (ReplyTypes.STATUS.toString().equals(text)) {
                int ends = ShuffleService.ends();
                int total = ShuffleService.sum();
                int members = ShuffleService.participats();
                int sum = ShuffleService.sum(user_id);
                if (sum > 0) {
                    answer = String.format("Shuffling money every 10 min. \nCurrent draw ends in %dm %ds\nTotal members: %d\nTotal sum is %d rub\nYour sum: %d rub.\n Win rate: %d%", ends/60, ends%60, members, total, sum, total/sum*100);
                }
                else {
                    answer = String.format("Shuffling money every 10 min. \nCurrent draw ends in %dm %ds\nTotal members: %d\nTotal sum is %d rub\nYour sum: %d rub.", ends/60, ends%60, members, total, sum);
                }
            }
            else {
                answer = "I don't understand you.";
            }
            
            sendRequestMessage(message.getChatId(), answer, keyboard);
        } 
        else  if(update.hasCallbackQuery()) {
            // todo
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String text = callbackQuery.getData();
            Long user_id = callbackQuery.getMessage().getChatId();
            
            InlineKeyboardMarkup keyboard = null;

            if (BalanceInlineTypes.REPLENISH.getKey().equals(text)) {
                answer = "Select replenish type.";
                keyboard = getReplenishInlineKeyboard();
            }
            else if (BalanceInlineTypes.WITHDRAW.getKey().equals(text)) {
                answer = "Soon...";
            }
            else if (BalanceInlineTypes.YANDEX.getKey().equals(text)) {
                db.Deposit(user_id, 1000);
                answer = "Your balance was successfully replenished by 1000 rub.";
            }
            else if (BalanceInlineTypes.QIWI.getKey().equals(text)) {
                db.Deposit(user_id, 1000);
                answer = "Your balance was successfully replenished by 1000 rub.";
            }
            else if (ShuffleTypes.SUM10.getKey().equals(text))
            {
                int balance = db.Balance(user_id);
                int sum = 10;
                if (sum <= balance) {
                    answer = "Now you are a member of shuffling money.";
                    db.Deposit(user_id, sum * -1);
                    ShuffleService.addMember(user_id, sum);
                }
                else {
                    answer = "Not enough money on balance";
                }
            }
            else if (ShuffleTypes.SUM100.getKey().equals(text))
            {
                int balance = db.Balance(user_id);
                int sum = 100;
                if (sum <= balance) {
                    answer = "Now you are a member of shuffling money.";
                    db.Deposit(user_id, sum * -1);
                    ShuffleService.addMember(user_id, sum);
                }
                else {
                    answer = "Not enough money on balance";
                }
            }
            else if (ShuffleTypes.SUM1000.getKey().equals(text))
            {
                int balance = db.Balance(user_id);
                int sum = 1000;
                if (sum <= balance) {
                    answer = "Now you are a member of shuffling money.";
                    db.Deposit(user_id, sum * -1);
                    ShuffleService.addMember(user_id, sum);
                }
                else {
                    answer = "Not enough money on balance";
                }
            }
            else if (ShuffleTypes.SUM10000.getKey().equals(text))
            {
                int balance = db.Balance(user_id);
                int sum = 10000;
                if (sum <= balance) {
                    answer = "Now you are a member of shuffling money.";
                    db.Deposit(user_id, sum * -1);
                    ShuffleService.addMember(user_id, sum);
                }
                else {
                    answer = "Not enough money on balance";
                }   
            }
            else {
                answer = "Error!";
            }

            sendAnswerCallbackQuery(callbackQuery, answer, keyboard);
        }
    }
    @Override
    public String getBotUsername() {
        return BotConfig.NAME;
    }
    @Override
    public String getBotToken() {
        return BotConfig.TOKEN;
    }

    public void ShuffleEnds(Set<Long> chatIds, Long winnerId, int sum) {
        String answer;
        for (Long chatId: chatIds) {
            if (chatId.equals(winnerId)) {
                answer = String.format("You win %d rub.", sum);
                Redis.getInstance().Deposit(winnerId, sum);
            }
            else {
                answer = "You not win, sorry.";
            }

            sendRequestMessage(chatId, answer, null);
        }
    }

    // Send message
    public synchronized void sendRequestMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error("ShuffleBot TelegramApiException", e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized void sendAnswerCallbackQuery(CallbackQuery callbackQuery, String text, boolean alert) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
		answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
		answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);

        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            BotLogger.error("ShuffleBot TelegramApiException", e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void sendAnswerCallbackQuery(CallbackQuery callbackQuery, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText answerEditMessage = new EditMessageText();
        answerEditMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
        answerEditMessage.setInlineMessageId(callbackQuery.getInlineMessageId());
        answerEditMessage.setText(text);
        answerEditMessage.enableMarkdown(true);
        answerEditMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        answerEditMessage.setReplyMarkup(keyboard);

        try {
            execute(answerEditMessage);
        } catch (TelegramApiException e) {
            BotLogger.error("ShuffleBot TelegramApiException", e.getMessage());
            e.printStackTrace();
        }
    }


    // Keyboards
    private ReplyKeyboardMarkup getMainKeyboard() {
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(ReplyTypes.STATUS.toString()));
        keyboardFirstRow.add(new KeyboardButton(ReplyTypes.BALANCE.toString()));
        
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton(ReplyTypes.SHUFFLE.toString()));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    // - balance
    private InlineKeyboardMarkup getBalanceInlineKeyboard() {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(BalanceInlineTypes.REPLENISH.toString())
            .setCallbackData(BalanceInlineTypes.REPLENISH.getKey()));
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(BalanceInlineTypes.WITHDRAW.toString())
            .setCallbackData(BalanceInlineTypes.WITHDRAW.getKey()));

        rowsInline.add(rowInline1);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(rowsInline);
        return markupKeyboard;
    }

    private InlineKeyboardMarkup getReplenishInlineKeyboard() {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(BalanceInlineTypes.QIWI.toString())
            .setCallbackData(BalanceInlineTypes.QIWI.getKey()));
        rowInline2.add(
            new InlineKeyboardButton()
            .setText(BalanceInlineTypes.YANDEX.toString())
            .setCallbackData(BalanceInlineTypes.YANDEX.getKey()));

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(rowsInline);
        return markupKeyboard;
    }

    // - shuffle
    private InlineKeyboardMarkup getShuffleSumInlineKeyboard() {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(ShuffleTypes.SUM10.toString())
            .setCallbackData(ShuffleTypes.SUM10.getKey()));
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(ShuffleTypes.SUM100.toString())
            .setCallbackData(ShuffleTypes.SUM100.getKey()));
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(ShuffleTypes.SUM1000.toString())
            .setCallbackData(ShuffleTypes.SUM1000.getKey()));
        rowInline1.add(
            new InlineKeyboardButton()
            .setText(ShuffleTypes.SUM10000.toString())
            .setCallbackData(ShuffleTypes.SUM10000.getKey()));

        rowsInline.add(rowInline1);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(rowsInline);
        return markupKeyboard;
    }


    // Response types
    enum ShuffleTypes {
        SUM10("10"),
        SUM100("100"),
        SUM1000("1000"),
        SUM10000("10000");
    
        private String value;
    
        ShuffleTypes(String value) {
            this.value = value;
        }

        public String getKey() {
            return Integer.toString(value.hashCode());
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum BalanceInlineTypes {
        QIWI("Qiwi"),
        YANDEX("Yandex Money/Card"),
        REPLENISH("Replenish"),
        WITHDRAW("Withdraw");

        private String value;

        BalanceInlineTypes(String value) {
            this.value = value;
        }

        public String getKey() {
            return Integer.toString(value.hashCode());
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum ReplyTypes {
        START("/start", "Start"), 
        STATUS("/status", "📝 Info"), 
        BALANCE("/balance", "💰 Balance"), 
        SHUFFLE("/shuffle", "🎲 Shuffle"), 
        SETTINGS("/settings", "⚙️ Settings"),
        ABOUT("/about", "About");

        private String key;
        private String value;

        ReplyTypes(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}