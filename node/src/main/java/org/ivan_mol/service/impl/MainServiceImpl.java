package org.ivan_mol.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ivan_mol.dao.AppUserDao;
import org.ivan_mol.dao.RawDataDao;
import org.ivan_mol.entity.AppDocument;
import org.ivan_mol.entity.AppPhoto;
import org.ivan_mol.entity.AppUser;
import org.ivan_mol.entity.RawData;
import org.ivan_mol.entity.UserState;
import org.ivan_mol.exception.UploadFileException;
import org.ivan_mol.service.FileService;
import org.ivan_mol.service.MainService;
import org.ivan_mol.service.ProducerService;
import org.ivan_mol.service.enums.LinkType;
import org.ivan_mol.service.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.ivan_mol.entity.UserState.BASIC_STATE;
import static org.ivan_mol.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static org.ivan_mol.service.enums.ServiceCommand.CANCEL;
import static org.ivan_mol.service.enums.ServiceCommand.HELP;
import static org.ivan_mol.service.enums.ServiceCommand.REGISTRATION;
import static org.ivan_mol.service.enums.ServiceCommand.START;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final AppUserDao appUserDao;
    private final ProducerService producerService;
    private final FileService fileService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getState();
        String text = update.getMessage().getText();
        String output = "";
        var serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
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
        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            String answer = "Документ успешно загружен! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            String error = "File is not loaded.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            String answer = "Фото успешно загружено! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "Photo is not loaded.";
            sendAnswer(error, chatId);
        }
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
        ServiceCommand serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            //TODO добавить регистрацию
            return "Now is unavailable.";
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Greatings! Use help /help";
        } else {
            return "Unknown command /help";
        }
    }

    private String help() {
        return "Commands:\n"
                + "/cancel - Cancel current command;\n"
                + "/registration - User registration.";
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
