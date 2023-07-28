package org.ivan_mol.service;

import org.ivan_mol.entity.AppDocument;
import org.ivan_mol.entity.AppPhoto;
import org.ivan_mol.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
