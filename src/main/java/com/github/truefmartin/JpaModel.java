package com.github.truefmartin;

import com.github.truefmartin.models.*;
import com.github.truefmartin.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The JpaModel class is responsible for managing the database operations.
 * It uses Hibernate to interact with the database.
 */
public class JpaModel implements AutoCloseable{
    private static final Logger logger =
            LoggerFactory.getLogger(JpaModel.class);
    /**
     * Constructs a new JpaModel object.
     * If the sessionFactory is null, it builds a new one.
     */
    public JpaModel() {
        if (!HibernateUtil.isOpen()) {
            HibernateUtil.start();
        }
    }

    /**
     * Closes the HibernateUtil sessionFactory when the JpaModel object is closed.
     * @throws Exception if there is a problem closing the sessionFactory
     */
    @Override
    public void close() throws Exception {
        HibernateUtil.close();
    }


    public static void addGame(Game game, Long homeTeamId, Long awayTeamId) {
        try(var tx = HibernateUtil.openSession()) {
            addGame(tx, game, homeTeamId, awayTeamId);
        }
    }


    public static Long addTeam(Team team, boolean getID) {
        try(var tx = HibernateUtil.openSession()) {
            addTeam(tx, team);
            if (!getID) {
                return -1L;
            }
            return team.getId();
        }
    }

    private static void addTeam(Session tx, Team team) {
        tx.beginTransaction();
        tx.persist(team);
        tx.getTransaction().commit();
    }
    public static List<Player> getPlayersOfTeam(Long teamId) {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayersOfTeam(tx, teamId);
        }
    }

    private static List<Player> getPlayersOfTeam(Session tx, Long teamId) {
        return tx.createQuery(
                "from Player p where p.team.id = :teamId",
                Player.class
        ).setParameter("teamId", teamId).getResultList();
    }
    public static void addPlayer(Player player, Long teamId) {
        try(var tx = HibernateUtil.openSession()) {
            addPlayer(tx, player, teamId);
        }
    }

    private static void addPlayer(Session tx, Player player, Long teamId) {
        tx.beginTransaction();
        player.setTeam(tx.get(Team.class, teamId));
        tx.persist(player);
        tx.getTransaction().commit();
    }


    public static List<Player> getPlayersOfTeam(Team team) {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayersOfTeam(tx, team);
        }
    }

    private static List<Player> getPlayersOfTeam(Session tx, Team team) {
        return tx.createQuery(
                "from Player p where p.team = :team",
                Player.class
        ).setParameter("team", team).getResultList();
    }

    public static List<Player> getPlayerOfPosition(PositionType position) {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayerOfPosition(tx, position);
        }
    }

    private static List<Player> getPlayerOfPosition(Session tx, PositionType position) {
        return tx.createQuery(
                "from Player p join fetch p.team where p.position = :position",
                Player.class
        ).setParameter("position", position).getResultList();
    }

    public static LinkedHashMap<Team, Integer[]> getTeamsAndWins() {
        try(var tx = HibernateUtil.openSession()) {
            var games = getTeamsAndGames(tx);

            if (games.isEmpty()) {
                logger.error("No games found in database");
                return new LinkedHashMap<>();
            }
            //  View all teams arranged by conference (sorted alphabetically); within each conference, sort by number of wins overall and then number of wins within the same conference
            LinkedHashMap<Team, Integer[]> teamScoresMap = new LinkedHashMap<>();
            for (Game game :
                    games
            ) {

                Team homeTeam = game.getHomeTeam();
                Team awayTeam = game.getAwayTeam();
                if (homeTeam == null || awayTeam == null) {
                    logger.error("Game with null home or away team: {}", game);
                    continue;
                }

                if (!teamScoresMap.containsKey(homeTeam)) {
                    teamScoresMap.put(homeTeam, new Integer[]{0, 0});
                }
                if (!teamScoresMap.containsKey(awayTeam)) {
                    teamScoresMap.put(awayTeam, new Integer[]{0, 0});
                }
                if (Objects.equals(game.getScore1(), game.getScore2())) {
                    continue;
                }
                boolean conferenceIsSame = homeTeam.getConference().equals(awayTeam.getConference());
                // We want to add the team to the map even if they have not yet won a game, but don't want to increment on tie
                Team winningTeam = game.getScore1() > game.getScore2() ? homeTeam : awayTeam;
                var scoreArr = teamScoresMap.get(winningTeam);
                scoreArr[0]++;
                if (conferenceIsSame) {
                    scoreArr[1]++;
                }
            }
            List<Long> teamIds = teamScoresMap.keySet().stream().map(Team::getId).toList();
            tx.createQuery(
                    "from Team t where t.id not in :teamIds",
                    Team.class
            ).setParameter("teamIds", teamIds)
                    .getResultList()
                    // Add teams with no games to the map
                    .forEach(team -> teamScoresMap.put(team, new Integer[]{0, 0}));

            return teamScoresMap.entrySet().stream()
                    .sorted((e1, e2) -> {
                        // First compare on conference name spelling
                        int confCompare = e1.getKey().getConference().toString().compareTo(e2.getKey().getConference().toString());
                        if (confCompare != 0) {
                            return confCompare;
                        }
                        // If same conference, compare on total wins
                        int winsCompare = e2.getValue()[0].compareTo(e1.getValue()[0]);
                        if (winsCompare != 0) {
                            return winsCompare;
                        }
                        return e2.getValue()[1].compareTo(e1.getValue()[1]);
                    })
                    .collect(LinkedHashMap::new, (LinkedHashMap<Team, Integer[]> map, Map.Entry<Team, Integer[]> entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
        }
    }

    private static List<Game> getTeamsAndGames(Session tx) {
        return tx.createQuery(
                "from Game g join fetch g.homeTeam",
                Game.class
        ).getResultList();
    }

    public static List<Game> getGamesOfTeam(Long id) {
        try(var tx = HibernateUtil.openSession()) {
            return getGamesOfTeam(tx, id);
        }
    }

    private static List<Game> getGamesOfTeam(Session tx, Long id) {
        return tx.createQuery(
                "from Game g where g.homeTeam.id = :id or g.awayTeam.id = :id",
                Game.class
        ).setParameter("id", id).getResultList();
    }

    public static Team getGamesOfTeam(Team team, String ... teamName) {
        try(var tx = HibernateUtil.openSession()) {
            Team loadedTeam;
            // If the user passed in a string team name, use that. Otherwise, use the team object.
            if (teamName.length > 0) {
                loadedTeam = getGamesOfTeamName(tx, teamName[0]);
            } else {
                loadedTeam = getGamesOfTeam(tx, team);
            }


            loadedTeam.getGamesAway().forEach(Game::getHomeTeam);
            loadedTeam.getGamesHome().forEach(Game::getAwayTeam);
            return loadedTeam;
        }
    }

    public static List<Map<String, Object>> getGamesOfTeamId(Long teamId) {
        try(var tx = HibernateUtil.openSession()) {

            Team team = tx.get(Team.class, teamId);
            List<Map<String, Object>> tableData = new ArrayList<>();
            ArrayList<Game> games = new ArrayList<>(team.getGamesHome());
            games.addAll(team.getGamesAway());
            games.sort(Comparator.comparing(Game::getDate));
            games.forEach((game) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Date", game.getDate());
                if (game.homeTeam.equals(team)) {
                    row.put("Result", game.score1 > game.score2 ? "Won" : "Lost");
                    row.put("Playing As", "Home");
                    row.put("Against", game.awayTeam.getNickname());
                    row.put("Of", game.awayTeam.getLocation());
                    row.put("Score", game.score1 + " - " + game.score2);
                } else {
                    row.put("Result", game.score1 < game.score2 ? "Won" : "Lost");
                    row.put("Playing As", "Away");
                    row.put("Against", game.homeTeam.getNickname());
                    row.put("Of", game.homeTeam.getLocation());
                    row.put("Score", game.score2 + " - " + game.score1);
                }
                tableData.add(row);
            });
            return tableData;
        }
    }


    private static Team getGamesOfTeamName(Session tx, String teamName) {
        return tx.createQuery(
                "from Team t where t.nickname = :teamName ",
                Team.class
        ).setParameter("teamName", teamName).getSingleResult();
    }

    private static Team getGamesOfTeam(Session tx, Team team) {
        return tx.createQuery(
                "from Team t where t = :team ",
                Team.class
        ).setParameter("team", team).getSingleResult();
    }

    public static List<Player> getPlayersIncludeTeams() {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayersIncludeTeams(tx);
        }
    }

    private static List<Player> getPlayersIncludeTeams(Session tx) {
        return tx.createQuery("from Player p join fetch p.team", Player.class).getResultList();
    }

    //7) View all results on a given date. Display the Team name, nicknames, location, and scores for the teams involved. Clearly indicate the winner.
    public static List<Map<String, Object>> getGamesOfDate(Date date) {
        try(var tx = HibernateUtil.openSession()) {
            List<Game> gamesHome = getGamesOfDate(tx, date, true);
            List<Game> gamesAway = getGamesOfDate(tx, date, false);
            tx.close();
            if (gamesHome.isEmpty() && gamesAway.isEmpty()) {
                logger.error("No games found in database for date: {}", date);
                return new ArrayList<>();
            }
            if (gamesHome.size() != gamesAway.size()) {
                logger.error("Mismatched number of home and away games for date: {}", date);
                return new ArrayList<>();
            }
            List<Map<String, Object>> response = new ArrayList<>();
            // Should already be sorted, but just in case
            gamesHome.sort(Comparator.comparing(Game::getId));
            gamesAway.sort(Comparator.comparing(Game::getId));

            for (int i = 0; i < gamesHome.size(); i++) {
                Map<String, Object> row = formatRows(gamesHome, i, gamesAway);
                response.add(row);
            }
            return response;
         }
    }

    private static Map<String, Object> formatRows(List<Game> gamesHome, int i, List<Game> gamesAway) {
        Game homeGame = gamesHome.get(i);
        Game awayGame = gamesAway.get(i);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Home Team", homeGame.getHomeTeam().getNickname());
        row.put("Home Team Location", homeGame.getHomeTeam().getLocation());
        row.put("Home Team Score", homeGame.getScore1());
        row.put("Away Team", awayGame.getAwayTeam().getNickname());
        row.put("Away Team Location", awayGame.getAwayTeam().getLocation());
        row.put("Away Team Score", awayGame.getScore2());
        if (homeGame.getScore1() > awayGame.getScore2()) {
            row.put("Winner", homeGame.getHomeTeam().getNickname());
        } else if (homeGame.getScore1() < awayGame.getScore2()) {
            row.put("Winner", awayGame.getAwayTeam().getNickname());
        } else {
            row.put("Winner", "???");
        }
        return row;
    }

    private static List<Game> getGamesOfDate(Session tx, Date date, boolean homeTeam) {
        String team = "g.homeTeam";
        if (!homeTeam) {
            team = "g.awayTeam";
        }
        return tx.createQuery(
                "from Game g join fetch " + team + " where g.date = :date",
                Game.class
        ).setParameter("date", date).getResultList();
    }


    // Add the itemNo, current time, and current date to the FoodOrder table.
    private static void addGame(Session tx, Game game, Long homeTeamId, Long awayTeamId) {
        tx.beginTransaction();
        game.setHomeTeam(tx.get(Team.class, homeTeamId));
        game.setAwayTeam(tx.get(Team.class, awayTeamId));
        tx.persist(game);
        tx.getTransaction().commit();
    }

    public static List<?> listRelation(String relationName, Class<?> className) {
        try(var tx = HibernateUtil.openSession()) {
            return listRelation(tx, relationName, className);
        }
    }

    private static List<?> listRelation(Session tx, String relationName, Class<?> className) {
        var entityName = "";
        switch (relationName){
            case "game":
                entityName = "Game";
                break;
            case "team":
                entityName = "Team";
                break;
            case "player":
                entityName = "Player";
                break;
        }
        return tx.createQuery(
                "FROM " + entityName + " e",
                className
                ).getResultList();
    }
}
