package personal.carl.thronson.security.data.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.core.BaseRepository;
import personal.carl.thronson.security.data.entity.ResetPasswordTokenEntity;

@Repository
@Transactional
public interface ResetPasswordTokenRepository
    extends BaseRepository<ResetPasswordTokenEntity> {
}
