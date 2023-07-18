package ivan_mol.dao;

import ivan_mol.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawDataDao extends JpaRepository<RawData, Long> {

}
