package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {
    private final Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final DateTimeFormatter DateTimeFormatted = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void showNotification() {
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> notificationTasks = notificationTaskRepository.findByNotificationTasks(localDateTime);

        for (NotificationTask notificationTask : notificationTasks) {
            sendMessage(notificationTask.getIdChat(), notificationTask.getNotification());
        }
    }

    public void saveNotification(Long chatId, String text) {


        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            try {
                String dateTime = matcher.group(1);
                String notification = matcher.group(3);
                LocalDateTime timestamp = LocalDateTime.parse(dateTime, DateTimeFormatted);
                NotificationTask notificationTask = new NotificationTask(chatId, notification, timestamp);
                notificationTaskRepository.save(notificationTask);
            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Неверно указан формат времени или даты");
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        SendResponse response = telegramBot.execute(sendMessage);
        if (!response.isOk()){
           logger.info("Сообщение не отправлено");
        };
    }
}
