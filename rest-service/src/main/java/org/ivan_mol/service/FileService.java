package org.ivan_mol.service;

import org.ivan_mol.entity.AppDocument;
import org.ivan_mol.entity.AppPhoto;
import org.ivan_mol.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
}