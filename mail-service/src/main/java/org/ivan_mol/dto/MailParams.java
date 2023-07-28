package org.ivan_mol.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MailParams {
    private String id;
    private String emailTo;
}