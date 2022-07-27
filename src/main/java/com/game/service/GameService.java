package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface GameService {

  Page<Player> getAllPlayers(Specification<Player> specification, Pageable pageable);

  List<Player> getAllPlayers(Specification<Player> specification);

  Player createPlayer(Player player);

  Player getPlayer(Long id);

  Player updatePlayer(Long id, Player player);

  boolean deletePlayer(Long id);

  Specification<Player> nameFilter(String name);

  Specification<Player> titleFilter(String title);

  Specification<Player> raceFilter(Race race);

  Specification<Player> professionFilter(Profession profession);

  Specification<Player> experienceFilter(Integer min, Integer max);

  Specification<Player> levelFilter(Integer min, Integer max);

  Specification<Player> birthdayFilter(Long before, Long after);

  Specification<Player> bannedFilter(Boolean banned);
}
