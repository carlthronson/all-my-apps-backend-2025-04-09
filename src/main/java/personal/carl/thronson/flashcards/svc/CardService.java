package personal.carl.thronson.flashcards.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import personal.carl.thronson.core.SimpleRepository;
import personal.carl.thronson.core.SimpleService;
import personal.carl.thronson.flashcards.data.entity.CardEntity;
import personal.carl.thronson.flashcards.data.repo.CardRepository;

@Service
@Transactional
public class CardService extends SimpleService<CardEntity> {

  @Autowired
  CardRepository repository;

  @Override
  public SimpleRepository<CardEntity> getSimpleRepository() {
    return this.repository;
  }

  @Override
  public JpaRepository<CardEntity, Long> getJpaRepository() {
    return this.repository;
  }
}
