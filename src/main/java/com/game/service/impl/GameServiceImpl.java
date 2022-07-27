package com.game.service.impl;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.GameRepository;
import com.game.service.GameService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class GameServiceImpl implements GameService {

  public GameServiceImpl(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  private final GameRepository gameRepository;

  private int calculateLevel(Player player) {
    int exp = player.getExperience();
    return (int) ((Math.sqrt(2500 + 200 * exp) - 50) / 100);
  }

  private int calculateExpUntilNextLevel(Player player) {
    int exp = player.getExperience();
    int level = calculateLevel(player);
    return 50 * (level + 1) * (level + 2) - exp;
  }

  private void setLevelAndExpUntilNextLevel(Player player) {
    player.setLevel(calculateLevel(player));
    player.setUntilNextLevel(calculateExpUntilNextLevel(player));
  }

  private boolean invalidValue(Player player) {
    if (player.getName().length() < 1 || player.getName().length() > 12) return true;
    if (player.getTitle().length() > 30) return true;
    if (player.getExperience() < 0 || player.getExperience() > 10_000_000) return true;
    if (player.getBirthday().getTime() < 0) return true;
    Calendar date = Calendar.getInstance();
    date.setTime(player.getBirthday());
    return date.get(Calendar.YEAR) < 2_000 || date.get(Calendar.YEAR) > 3_000;
  }

  @Override
  public Specification<Player> nameFilter(String name) {
    return (root, query, builder) ->
        name == null ? null : builder.like(root.get("name"), "%" + name + "%");
  }

  @Override
  public Specification<Player> titleFilter(String title) {
    return (root, query, builder) ->
        title == null ? null : builder.like(root.get("title"), "%" + title + "%");
  }

  @Override
  public Specification<Player> raceFilter(Race race) {
    return (root, query, builder) -> race == null ? null : builder.equal(root.get("race"), race);
  }

  @Override
  public Specification<Player> professionFilter(Profession profession) {
    return (root, query, builder) ->
        profession == null ? null : builder.equal(root.get("profession"), profession);
  }

  @Override
  public Specification<Player> experienceFilter(Integer min, Integer max) {
    return (root, query, builder) -> {
      if (min == null && max == null) return null;
      if (min == null) return builder.lessThanOrEqualTo(root.get("experience"), max);
      if (max == null) return builder.greaterThanOrEqualTo(root.get("experience"), min);
      return builder.between(root.get("experience"), min, max);
    };
  }

  @Override
  public Specification<Player> levelFilter(Integer min, Integer max) {
    return (root, query, builder) -> {
      if (min == null && max == null) return null;
      if (min == null) return builder.lessThanOrEqualTo(root.get("level"), max);
      if (max == null) return builder.greaterThanOrEqualTo(root.get("level"), min);
      return builder.between(root.get("level"), min, max);
    };
  }

  @Override
  public Specification<Player> birthdayFilter(Long after, Long before) {
    return (root, query, builder) -> {
      if (before == null && after == null) return null;
      if (after == null) return builder.lessThanOrEqualTo(root.get("birthday"), new Date(before));
      if (before == null)
        return builder.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
      return builder.between(root.get("birthday"), new Date(after), new Date(before));
    };
  }

  @Override
  public Specification<Player> bannedFilter(Boolean banned) {
    return (root, query, builder) -> {
      if (banned == null) return null;
      if (banned) return builder.isTrue(root.get("banned"));
      else return builder.isFalse(root.get("banned"));
    };
  }

  @Override
  public List<Player> getAllPlayers(Specification<Player> specification) {
    return gameRepository.findAll(specification);
  }

  @Override
  public Page<Player> getAllPlayers(Specification<Player> specification, Pageable pageable) {
    return gameRepository.findAll(specification, pageable);
  }

  @Override
  public Player createPlayer(Player player) {
    if (player == null
        || player.getName() == null
        || player.getTitle() == null
        || player.getRace() == null
        || player.getProfession() == null
        || player.getBirthday() == null
        || player.getExperience() == null) {
      return null;
    }
    if (invalidValue(player)) {
      return null;
    }
    if (player.getBanned() == null) {
      player.setBanned(false);
    }
    setLevelAndExpUntilNextLevel(player);

    return gameRepository.saveAndFlush(player);
  }

  @Override
  public Player getPlayer(Long id) {
    if (gameRepository.findById(id).isPresent()) return gameRepository.findById(id).get();
    return null;
  }

  @Override
  public Player updatePlayer(Long id, Player player) {
    if (!gameRepository.findById(id).isPresent()) return null;

    Player updatePlayer = getPlayer(id);

    if (player.getName() != null) updatePlayer.setName(player.getName());
    if (player.getTitle() != null) updatePlayer.setTitle(player.getTitle());
    if (player.getRace() != null) updatePlayer.setRace(player.getRace());
    if (player.getProfession() != null) updatePlayer.setProfession(player.getProfession());
    if (player.getBirthday() != null) updatePlayer.setBirthday(player.getBirthday());
    if (player.getExperience() != null) updatePlayer.setExperience(player.getExperience());
    if (player.getBanned() != null) updatePlayer.setBanned(player.getBanned());

    if (invalidValue(updatePlayer)) return null;

    setLevelAndExpUntilNextLevel(updatePlayer);
    return gameRepository.save(updatePlayer);
  }

  @Override
  public boolean deletePlayer(Long id) {
    if (gameRepository.findById(id).isPresent()) {
      gameRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
