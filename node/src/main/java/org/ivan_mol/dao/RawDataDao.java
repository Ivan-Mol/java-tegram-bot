package org.ivan_mol.dao;

import org.ivan_mol.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawDataDao extends JpaRepository<RawData, Long> {

}
