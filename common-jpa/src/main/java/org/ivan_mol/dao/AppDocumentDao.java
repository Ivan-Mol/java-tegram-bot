package org.ivan_mol.dao;

import org.ivan_mol.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDocumentDao extends JpaRepository<AppDocument,Long> {
}
