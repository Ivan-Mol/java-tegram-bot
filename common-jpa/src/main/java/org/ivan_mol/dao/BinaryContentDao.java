package org.ivan_mol.dao;

import org.ivan_mol.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentDao extends JpaRepository<BinaryContent,Long> {
}
