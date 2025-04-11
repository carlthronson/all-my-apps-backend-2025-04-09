package personal.carl.thronson.security.data.repo;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.core.BaseRepository;
import personal.carl.thronson.security.data.entity.RoleEntity;

@Repository
@Transactional
public interface RoleRepository extends BaseRepository<RoleEntity> {

  Optional<RoleEntity> findByName(String name);
}
