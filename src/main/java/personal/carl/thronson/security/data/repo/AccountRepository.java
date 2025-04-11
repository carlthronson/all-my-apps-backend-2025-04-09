package personal.carl.thronson.security.data.repo;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.core.BaseRepository;
import personal.carl.thronson.security.data.entity.AccountEntity;

@Repository
@Transactional
public interface AccountRepository extends BaseRepository<AccountEntity> {

  Optional<AccountEntity> findByEmail(String email);
}
