package personal.carl.thronson.budget.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.budget.data.entity.FinancialAccountTypeEntity;
import personal.carl.thronson.core.SimpleRepository;

@Repository
@Transactional
public interface FinancialAccountTypeRepository extends JpaRepository<FinancialAccountTypeEntity, Long>, SimpleRepository<FinancialAccountTypeEntity> {
}
