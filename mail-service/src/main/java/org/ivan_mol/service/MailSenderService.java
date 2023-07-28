package org.ivan_mol.service;

import org.ivan_mol.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}