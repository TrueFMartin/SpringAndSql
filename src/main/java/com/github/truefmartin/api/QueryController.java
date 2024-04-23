package com.github.truefmartin.api;

import com.github.truefmartin.JpaModel;
import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.models.Game;
import com.github.truefmartin.models.Player;
import com.github.truefmartin.models.PositionType;
import com.github.truefmartin.models.Team;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;


@Controller
public class QueryController {

    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
    @GetMapping("/top-teams")
    public String topTeams(Model model) {
        var teamAndWins = JpaModel.getTeamsAndWins();
        List<Map<String, Object>> tableData = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();  // Maintains insertion order
        teamAndWins.forEach((team, wins) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Team", team.getNickname());
            row.put("Conference", team.getConference());
            row.put("Total Wins", wins[0]);
            row.put("Conference Wins", wins[1]);
            tableData.add(row);
            if (headers.isEmpty()) {
                row.keySet().forEach(headers::add);
            }
        });
        model.addAttribute("title", "Top Teams by:");
        model.addAttribute("subtitle", "Conference > Total Wins > Conference Wins");
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "table";
    }

    @GetMapping("/addPlayer")
    public String addPlayerDisplay(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);
        var teamIds = new TeamIds();
        model.addAttribute("teams", teams);
        model.addAttribute("player", new Player());
        model.addAttribute("teamIds", teamIds);
        return "add-player";
    }

    @PostMapping("/addPlayer")
    public String addPlayer(@ModelAttribute Player player, @ModelAttribute TeamIds teamIds, Model model) {
        if (teamIds.getHomeTeamId() == 0) {
            model.addAttribute("error", new EmptyResultsException("No team selected"));
            return "errors";
        }
        JpaModel.addPlayer(player, teamIds.getHomeTeamId());
        return "redirect:/showPlayers";
    }

    @GetMapping("/showPlayers")
    public String showPlayers(Model model) {
        List<Player> players = JpaModel.getPlayersIncludeTeams();
        if (players.isEmpty()) {
            model.addAttribute("error", new EmptyResultsException("No players found"));
            return "errors";
        }
        List<Map<String, Object>> tableData = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();  // Maintains insertion order
        players.stream().sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList().forEach(player -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", player.getName());
            row.put("Position", player.getPosition().getDisplayValue());
            row.put("Team", player.getTeam().getNickname());
            tableData.add(row);
            if (headers.isEmpty()) {
                row.keySet().forEach(headers::add);
            }
        });
        model.addAttribute("title", "Players");
        model.addAttribute("subtitle", "Sorted by Name");
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "table";
    }
    @GetMapping("/playersOfPosition")
    public String getPlayerPositions(Model model) {
        model.addAttribute("player", new Player());
        return "player-positions";
    }

    @PostMapping("/playersOfPosition")
    public String showPlayersOfPosition(@ModelAttribute Player player, Model model) {
        if (player == null) {
            return "redirect:playersOfPosition";
        }
        PositionType position = player.getPosition();
        List<Player> players = JpaModel.getPlayerOfPosition(position);
        if (players.isEmpty()) {
            model.addAttribute("error", new EmptyResultsException("No players found"));
            return "errors";
        }
        List<Map<String, Object>> tableData = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();  // Maintains insertion order
        players.stream().sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList().forEach(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", p.getName());
            row.put("Position", p.getPosition().getDisplayValue());
            row.put("Team", p.getTeam().getNickname());
            tableData.add(row);
            if (headers.isEmpty()) {
                row.keySet().forEach(headers::add);
            }
        });
        model.addAttribute("title", "Players of Position");
        model.addAttribute("subtitle", position);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "player-positions";
    }

    @GetMapping("/addGame")
    public String showAddGameForm(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);

        var game = new Game();
        var teamIds = new TeamIds();
        model.addAttribute("teams", teams);
        model.addAttribute("game", game);
        model.addAttribute("teamIds", teamIds);
        return "add-game";
    }

    @PostMapping("/addGame")
    public String addGame(@ModelAttribute Game game, @ModelAttribute TeamIds teamIds, Model model) {

        JpaModel.addGame(game, teamIds.getHomeTeamId(), teamIds.getAwayTeamId());
        model.addAttribute("teamId", teamIds.homeTeamId);
        return "redirect:/addGame";
//        return showGamesOfTeam(teamIds.getHomeTeamId(), model);
    }

    @GetMapping("/byDate")
    public String gamesByDate(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("notFound", false);
        return "games-by-date";
    }

    @PostMapping("/byDate")
    public String gamesByDate(@ModelAttribute() Game game, Model model) {
        List<Map<String, Object>> tableData = JpaModel.getGamesOfDate(game.getDate());
        if (tableData.isEmpty()) {
            model.addAttribute("notFound", true);
            return "games-by-date";
        }
        Set<String> headers = new LinkedHashSet<>();  // Maintains insertion order
        tableData.get(0).keySet().forEach(headers::add);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "games-by-date";
    }


    @GetMapping("/teams")
    public String showTeams(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);
        model.addAttribute("teams", teams);
        return "teams-list";
    }

    @GetMapping("/playersOf")
    public String showPlayersOfTeam(
            @RequestParam("teamId") Long id,
            @RequestParam("teamName") String teamName,
            @RequestParam("teamLocation") String teamLocation,
            Model model) {
        if (id == 0 ) {
            model.addAttribute("error", new EmptyResultsException("Selected Team ID is zero"));
            return "errors";
        }
        List<Player> playerData = JpaModel.getPlayersOfTeam(id);
        if (playerData.isEmpty()) {
            model.addAttribute("title", "No players found for: ");
            model.addAttribute("subtitle", teamName + " from " + teamLocation);
            return "table";
        }
        List<Map<String, Object>> tableData = new ArrayList<>();
        playerData.forEach((player) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", player.getName());
            row.put("Position", player.getPosition().getDisplayValue());
            tableData.add(row);
        });
        Set<String> headers = new LinkedHashSet<>();  // Maintains insertion order
        tableData.get(0).keySet().forEach(headers::add);
        model.addAttribute("title", "Players of");
        model.addAttribute("subtitle", teamName + " From " + teamLocation);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "table";
    }

    @GetMapping("/gamesOf")
    public String showGames(
            @RequestParam(value = "teamId", required = false) Long id,
            @RequestParam(value = "teamName", required = false) String teamName,
            @RequestParam(value = "teamLocation", required = false) String teamLocation,
            Model model) {

        if (id == 0) {
            if (model.getAttribute("teamId") instanceof Long i)
                id = i;
            else
                return "errors";
            teamName = (String)model.getAttribute("teamName");
            teamLocation = (String)model.getAttribute("teamLocation");
            if (id == 0 || teamName == null || teamLocation == null)
                return "errors";
        }

        var tableData =  JpaModel.getGamesOfTeamId(id);
        if (tableData.isEmpty()) {
            model.addAttribute("title", "No games found for:");
            model.addAttribute("subtitle", teamName + " from " + teamLocation);
            return "table";
        }
        // Maintains insertion order
        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("title", "Games For");
        model.addAttribute("subtitle", teamName + " From " + teamLocation);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);

        return "table";
    }

    @GetMapping("/addTeam")
    public String showAddTeamForm(Model model) {
        var team = new Team();
        model.addAttribute("team", team);
        return "add-team";
    }

    @PostMapping("/addTeam")
    public String addTeam(@ModelAttribute Team team, Model model) {
        Long id = JpaModel.addTeam(team, true);
        return "redirect:/teams#" + id;
    }
}