package org.ivan_mol.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ivan_mol.dao.AppUserDao;
import org.ivan_mol.dao.RawDataDao;
import org.ivan_mol.entity.AppUser;
import org.ivan_mol.entity.RawData;
import org.ivan_mol.entity.UserState;
import org.ivan_mol.service.MainService;
import org.ivan_mol.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.ivan_mol.entity.UserState.BASIC_STATE;
import static org.ivan_mol.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.ivan_mol.service.enums.ServiceCommands.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final AppUserDao appUserDao;
    private final ProducerService producerService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getState();
        String text = update.getMessage().getText();
        String output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить обработку емейла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Error! Use /cancel and try again!";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO add document save :)
        var answer = "Doc is loaded successful";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранения фото :)
        var answer = "Photo is loaded successful";
        sendAnswer(answer, chatId);
    }
    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDao.save(rawData);
    }
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO добавить регистрацию
            return "Now is not working";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Greetings! /help";
        } else {
            return "Unknown command /help";
        }
    }
    private String help() {
        return "Command list:\n"
                + "/cancel - cancel current command;\n"
                + "/registration - registration of user.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Command is canceled!";
    }
    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Register or activate your account";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Cancel the current command /cancel";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }
    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser appUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (appUser == null) {
            appUser = new AppUser();
            appUser.setTelegramUserId(telegramUser.getId());
            appUser.setUserName(telegramUser.getUserName());
            appUser.setFirstName(telegramUser.getFirstName());
            appUser.setLastName(telegramUser.getLastName());
            //TODO
            appUser.setIsActive(true);
            appUser.setState(BASIC_STATE);
            return appUserDao.save(appUser);
        }
        return appUser;
    }

}
