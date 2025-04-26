package personal.carl.thronson.budget.data.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.core.BaseRepository;

@Repository
@Transactional
public interface TransactionRepository extends BaseRepository<TransactionEntity> {

}
