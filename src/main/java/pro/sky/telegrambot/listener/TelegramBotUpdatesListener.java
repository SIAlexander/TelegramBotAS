package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final String START = "/start";

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            String msg = update.message().text();
            Long chatId = update.message().chat().id();

            if (START.equals(msg)) {
                String userName = update.message().from().firstName();
                commandStart(chatId, userName);
            } else {
                notificationTaskService.saveNotification(chatId, msg);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void commandStart(Long chatId, String userName) {
        String messageStart = "Добро пожаловать в бот планировщик, %s!" +
                "\n\nДля созадния напоминания введите дату/время/текст в следующем формате: DD.MM.YYYY HH:MM Текст" +
                "\n\nПример: 26.09.2023 20:02 Сделать домашнюю работ";
        String formatMessageStart = String.format(messageStart, userName);
        sendMessage(chatId, formatMessageStart);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        SendResponse response = telegramBot.execute(sendMessage);
        if (!response.isOk()){
            logger.info("Сообщение не отправлено");
        };
    }

}
