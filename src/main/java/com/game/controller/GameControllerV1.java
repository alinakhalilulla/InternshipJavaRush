package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.GameService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/rest")
public class GameControllerV1 {

  public GameControllerV1(GameService gameService) {
    this.gameService = gameService;
  }

  private final GameService gameService;

  @GetMapping("/players")
  @ResponseStatus(HttpStatus.OK)
  public List<Player> getAllPlayers(
          @RequestParam(value = "name", required = false) String name,
          @RequestParam(value = "title", required = false) String title,
          @RequestParam(value = "race", required = false) Race race,
          @RequestParam(value = "profession", required = false) Profession profession,
          @RequestParam(value = "after", required = false) Long after,
          @RequestParam(value = "before", required = false) Long before,
          @RequestParam(value = "banned", required = false) Boolean banned,
          @RequestParam(value = "minExperience", required = false) Integer minExperience,
          @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
          @RequestParam(value = "minLevel", required = false) Integer minLevel,
          @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
          @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
          @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer  pageNumber,
          @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

    return gameService.getAllPlayers(
            Specification.where(gameService.nameFilter(name)
                                    .and(gameService.titleFilter(title))
                            .and(gameService.raceFilter(race))
                            .and(gameService.professionFilter(profession))
                            .and(gameService.birthdayFilter(after, before))
                            .and(gameService.bannedFilter(banned))
                            .and(gameService.experienceFilter(minExperience, maxExperience))
                            .and(gameService.levelFilter(minLevel, maxLevel))), pageable)
            .getContent();
  }


  @GetMapping("/players/count")
  @ResponseStatus(HttpStatus.OK)
  Integer getPlayersCount(
          @RequestParam(value = "name", required = false) String name,
          @RequestParam(value = "title", required = false) String title,
          @RequestParam(value = "race", required = false) Race race,
          @RequestParam(value = "profession", required = false) Profession profession,
          @RequestParam(value = "after", required = false) Long after,
          @RequestParam(value = "before", required = false) Long before,
          @RequestParam(value = "banned", required = false) Boolean banned,
          @RequestParam(value = "minExperience", required = false) Integer minExperience,
          @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
          @RequestParam(value = "minLevel", required = false) Integer minLevel,
          @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

    return gameService.getAllPlayers(Specification.where(gameService.nameFilter(name)
            .and(gameService.titleFilter(title))
            .and(gameService.raceFilter(race))
            .and(gameService.professionFilter(profession))
            .and(gameService.birthdayFilter(after, before))
            .and(gameService.bannedFilter(banned))
            .and(gameService.experienceFilter(minExperience, maxExperience))
            .and(gameService.levelFilter(minLevel, maxLevel)))).size();
  }

  @PostMapping("/players")
  ResponseEntity<?> createPlayer(@RequestBody Player player ) {
    if (gameService.createPlayer(player) == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } else return new ResponseEntity<>(player, HttpStatus.OK);
  }

  @GetMapping("/players/{id}")
  ResponseEntity<?> getPlayer(@PathVariable("id") Long id){
    if (id <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    if(gameService.getPlayer(id) == null) return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
    else {
      return new ResponseEntity<>(gameService.getPlayer(id), HttpStatus.OK);
    }
  }

  @PostMapping("/players/{id}")
  ResponseEntity<?> updatePlayer(@PathVariable("id") Long id, @RequestBody Player player){
    if (id <= 0 || (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000))
        || (player.getBirthday() != null && player.getBirthday().getTime() < 0))
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    if(gameService.updatePlayer(id, player) == null) return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
    else return new ResponseEntity<>(gameService.updatePlayer(id, player), HttpStatus.OK);
  }

  @DeleteMapping("/players/{id}")
  ResponseEntity<?> deletePlayer(@PathVariable("id") Long id) {
    if(id <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    if (gameService.deletePlayer(id)) return new ResponseEntity<>(HttpStatus.OK);
    else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


}
