package personal.carl.thronson.flashcards.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import personal.carl.thronson.flashcards.data.core.Card;

@Entity(name = "card")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class CardEntity extends Card {

}
