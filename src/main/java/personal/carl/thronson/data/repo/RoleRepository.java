package personal.carl.thronson.data.repo;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.data.entity.RoleEntity;

@Repository
@Transactional
public interface RoleRepository extends BaseRepository<RoleEntity> {

  Optional<RoleEntity> findByName(String name);
}
