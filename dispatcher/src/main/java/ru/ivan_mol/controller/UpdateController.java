package ru.ivan_mol.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ivan_mol.utils.MessageUtils;

@Component
@Log4j

public class UpdateController {
    private final TelegramBot telegramBot;
    private final MessageUtils messageUtils;

    public UpdateController(@Lazy TelegramBot telegramBot, MessageUtils messageUtils) {
        this.telegramBot = telegramBot;
        this.messageUtils = messageUtils;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
        }
        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported type of telegram message");
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        } else if (message.getPhoto() != null) {
            processPhotoMessage(update);

        } else if (message.getDocument() != null) {
            processDocumentMessage(update);

        }
        else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processTextMessage(Update update) {

    }

    private void processPhotoMessage(Update update) {

    }

    private void processDocumentMessage(Update update) {
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = MessageUtils
                .generateSendMessageWithText(update,"Unsupported type of message");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerToBot(sendMessage);
    }
}
