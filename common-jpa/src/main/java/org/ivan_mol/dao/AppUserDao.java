package org.ivan_mol.dao;

import org.ivan_mol.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserDao extends JpaRepository<AppUser,Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
