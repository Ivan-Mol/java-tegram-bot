package org.ivan_mol.controller;

import lombok.extern.log4j.Log4j;
import org.ivan_mol.service.UpdateProducer;
import org.ivan_mol.utils.MessageUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import static org.ivan_mol.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateProcessor {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateProcessor(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
        }
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported type of telegram message");
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);

        } else if (message.hasDocument()) {
            processDocumentMessage(update);

        }
        else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processDocumentMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }
    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "File processing...");
        setView(sendMessage);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = MessageUtils
                .generateSendMessageWithText(update,"Unsupported type of message");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }
}
