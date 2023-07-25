package org.ivan_mol.dao;

import org.ivan_mol.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPhotoDao extends JpaRepository<AppPhoto,Long> {
}
