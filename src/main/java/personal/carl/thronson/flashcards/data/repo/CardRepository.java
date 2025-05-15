package personal.carl.thronson.flashcards.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import personal.carl.thronson.core.SimpleRepository;
import personal.carl.thronson.flashcards.data.entity.CardEntity;

@Repository
@Transactional
public interface CardRepository extends JpaRepository<CardEntity, Long>, SimpleRepository<CardEntity> {
}
