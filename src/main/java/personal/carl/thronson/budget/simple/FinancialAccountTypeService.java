package personal.carl.thronson.budget.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import personal.carl.thronson.budget.data.entity.FinancialAccountTypeEntity;
import personal.carl.thronson.budget.data.repo.FinancialAccountTypeRepository;
import personal.carl.thronson.core.SimpleRepository;
import personal.carl.thronson.core.SimpleService;

@Service
@Transactional
public class FinancialAccountTypeService extends SimpleService<FinancialAccountTypeEntity> {

  @Autowired
  FinancialAccountTypeRepository repository;

  @Override
  public SimpleRepository<FinancialAccountTypeEntity> getSimpleRepository() {
    return this.repository;
  }

  @Override
  public JpaRepository<FinancialAccountTypeEntity, Long> getJpaRepository() {
    return this.repository;
  }
}
