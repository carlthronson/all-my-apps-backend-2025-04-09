package personal.carl.thronson.flashcards.ctrl;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import personal.carl.thronson.flashcards.data.core.Card;
import personal.carl.thronson.flashcards.data.entity.CardEntity;
import personal.carl.thronson.flashcards.svc.CardService;

@RestController
@EnableWebMvc
@Transactional
public class CardController {

  @Autowired
  CardService service;
  Logger logger = Logger.getLogger(CardController.class.getName());
  ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @RequestMapping(path = "/card/{id}", method = RequestMethod.DELETE)
  public void delete(@PathVariable(name = "id") Long id) {
    service.delete(id);
  }

  @RequestMapping(path = "/card", method = RequestMethod.POST)
  public CardEntity save(@RequestBody Card requestBody) {
    logger.info("Request body: " + requestBody);
    CardEntity cardEntity1 = new CardEntity();
    cardEntity1.setId(requestBody.getId());
    cardEntity1.setName(requestBody.getName());
    cardEntity1.setLabel(requestBody.getLabel());
    cardEntity1.setQuestion(requestBody.getQuestion());
    cardEntity1.setAnswer(requestBody.getAnswer());
    CardEntity cardEntity = cardEntity1;
    cardEntity = service.save(cardEntity);
    return cardEntity;
  }

  @RequestMapping(path = "/card/getbyid/{id}", method = RequestMethod.GET)
  public Card getById(@PathVariable("id") Long id) {
    logger.info("Path variable: " + id);
    CardEntity cardEntity = service.getById(id);
    return cardEntity;
  }

  @RequestMapping(path = "/card/findbyid/{id}", method = RequestMethod.GET)
  public CardEntity findById(@PathVariable("id") Long id) {
    logger.info("Path variable: " + id);
    return service.findById(id);
  }

  @RequestMapping(path = "/card/findallbyid/{id}", method = RequestMethod.GET)
  public List<CardEntity> findAllById(@PathVariable("id") Long id) {
    logger.info("Path variable: " + id);
    return service.findAllById(id);
  }

  @RequestMapping(path = "/card/findbyname/{name}", method = RequestMethod.GET)
  public Optional<CardEntity> findByName(@PathVariable("name") String name) {
    logger.info("Path variable: " + name);
    return service.findByName(name);
  }

  @RequestMapping(path = "/card/findallbyname/{name}", method = RequestMethod.GET)
  public List<CardEntity> findAllByName(@PathVariable("name") String name) {
    logger.info("Path variable: " + name);
    return service.findAllByName(name);
  }

  @RequestMapping(path = "/card/findbylabel/{label}", method = RequestMethod.GET)
  public CardEntity findByLabel(@PathVariable("label") String label) {
    logger.info("Path variable: " + label);
    return service.findByLabel(label);
  }

  @RequestMapping(path = "/card/findallbylabel/{label}", method = RequestMethod.GET)
  public List<CardEntity> findAllByLabel(@PathVariable("label") String label) {
    logger.info("Path variable: " + label);
    return service.findAllByLabel(label);
  }

  @RequestMapping(path = "/card/findall", method = RequestMethod.GET)
  public List<CardEntity> findAll(@RequestParam("limit") Optional<Integer> limit, Principal principal) {
    logger.info("Request param limit: " + limit);
    return service.findAll(limit).getContent();
  }
}
