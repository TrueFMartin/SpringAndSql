package com.github.truefmartin.api;

import com.github.truefmartin.JpaModel;
import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.models.Game;
import com.github.truefmartin.models.Player;
import com.github.truefmartin.models.PositionType;
import com.github.truefmartin.models.Team;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.*;

/**
 * Controller class for handling HTTP requests related to queries.
 */
@Controller
public class QueryController {
    private boolean justAdded = false;

    /**
     * Handles GET requests to the home page.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

    /**
     * Handles GET requests to display the top teams.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/top-teams")
    public String topTeams(Model model) {
        var teamAndWins = JpaModel.getTeamsAndWins();
        List<Map<String, Object>> tableData = new ArrayList<>();
        teamAndWins.forEach((team, wins) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Team", team.getNickname());
            row.put("Conference", team.getConference());
            row.put("Total Wins", wins[0]);
            row.put("Conference Wins", wins[1]);
            tableData.add(row);
        });

        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("title", "Top Teams by:");
        model.addAttribute("subtitle", "Conference > Total Wins > Conference Wins");
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "table";
    }

    /**
     * Handles GET requests to display the form for adding a player.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/addPlayer")
    public String addPlayerDisplay(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);
        var teamIds = new TeamIds();
        model.addAttribute("teams", teams);
        model.addAttribute("player", new Player());
        model.addAttribute("teamIds", teamIds);
        return "add-player";
    }

    /**
     * Handles POST requests to add a player.
     * @param player the player to add
     * @param teamIds the IDs of the teams the player is associated with
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @PostMapping("/addPlayer")
    public String addPlayer(@ModelAttribute Player player, @ModelAttribute TeamIds teamIds, Model model) {
        if (teamIds.getHomeTeamId() == 0) {
            model.addAttribute("error", new EmptyResultsException("No team selected"));
            return "errors";
        }
        JpaModel.addPlayer(player, teamIds.getHomeTeamId());
        justAdded = true;
        return "redirect:/showPlayers";
    }

    /**
     * Handles GET requests to display the players.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/showPlayers")
    public String showPlayers(Model model) {
        List<Player> players = JpaModel.getPlayersIncludeTeams();
        if (players.isEmpty()) {
            model.addAttribute("error", new EmptyResultsException("No players found"));
            return "errors";
        }
        List<Map<String, Object>> tableData = new ArrayList<>();
        players.stream().sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList().forEach(player -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", player.getName());
            row.put("Position", player.getPosition().getDisplayValue());
            row.put("Team", player.getTeam().getNickname());
            tableData.add(row);
        });

        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("title", "Players");
        model.addAttribute("subtitle", "Sorted by Name");
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);

        if (justAdded) {
            model.addAttribute("justAdded", true);
            justAdded = false;
        }
        return "table";
    }

    /**
     * Handles GET requests to display the form for getting players of a specific position.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/playersOfPosition")
    public String getPlayerPositions(Model model) {
        model.addAttribute("player", new Player());
        return "player-positions";
    }

    /**
     * Handles POST requests to display players of a specific position.
     * @param player the player whose position to use
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
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
        players.stream().sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).toList().forEach(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", p.getName());
            row.put("Position", p.getPosition().getDisplayValue());
            row.put("Team", p.getTeam().getNickname());
            tableData.add(row);
        });
        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("title", "Players of Position");
        model.addAttribute("subtitle", position);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "player-positions";
    }

    /**
     * Handles GET requests to display the form for adding a game.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/addGame")
    public String showAddGameForm(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);
        var game = new Game();
        var teamIds = new TeamIds();
        model.addAttribute("teams", teams);
        model.addAttribute("game", game);
        model.addAttribute("teamIds", teamIds);
        if (justAdded) {
            model.addAttribute("justAdded", true);
            justAdded = false;
        }
        return "add-game";
    }

    /**
     * Handles POST requests to add a game.
     * @param game the game to add
     * @param teamIds the IDs of the teams the game is associated with
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @PostMapping("/addGame")
    public String addGame(@ModelAttribute Game game, @ModelAttribute TeamIds teamIds, Model model) {

        JpaModel.addGame(game, teamIds.getHomeTeamId(), teamIds.getAwayTeamId());
        model.addAttribute("teamId", teamIds.homeTeamId);
        justAdded = true;
        return "redirect:/addGame";
    }

    /**
     * Handles GET requests to display games by date.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/byDate")
    public String gamesByDate(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("notFound", false);
        return "games-by-date";
    }

    /**
     * Handles POST requests to display games by date.
     * @param game the game whose date to use
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @PostMapping("/byDate")
    public String gamesByDate(@ModelAttribute() Game game, Model model) {
        List<Map<String, Object>> tableData = JpaModel.getGamesOfDate(game.getDate());
        if (tableData.isEmpty()) {
            model.addAttribute("notFound", true);
            return "games-by-date";
        }
        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "games-by-date";
    }

    /**
     * Handles GET requests to display the teams.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/teams")
    public String showTeams(Model model) {
        List<Team> teams = (List<Team>) JpaModel.listRelation("team", Team.class);
        model.addAttribute("teams", teams);
        model.addAttribute("title", "Select team to retrieve information about.");
        if (justAdded) {
            model.addAttribute("justAdded", true);
            justAdded = false;
        }
        return "teams-list";
    }

    /**
     * Handles GET requests to display the players of a specific team.
     * @param id the ID of the team
     * @param teamName the name of the team
     * @param teamLocation the location of the team
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
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
        Set<String> headers = new LinkedHashSet<>(tableData.get(0).keySet());
        model.addAttribute("title", "Players of");
        model.addAttribute("subtitle", teamName + " From " + teamLocation);
        model.addAttribute("headers", headers);
        model.addAttribute("tableData", tableData);
        return "table";
    }

    /**
     * Handles GET requests to display the games of a specific team.
     * @param id the ID of the team
     * @param teamName the name of the team
     * @param teamLocation the location of the team
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
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

    /**
     * Handles GET requests to display the form for adding a team.
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @GetMapping("/addTeam")
    public String showAddTeamForm(Model model) {
        var team = new Team();
        model.addAttribute("team", team);
        return "add-team";
    }

    /**
     * Handles POST requests to add a team.
     * @param team the team to add
     * @param model the model to add attributes to for rendering in the view
     * @return the name of the view to render
     */
    @PostMapping("/addTeam")
    public String addTeam(@ModelAttribute Team team, Model model) {
        Long id = JpaModel.addTeam(team, true);
        justAdded = true;
        return "redirect:/teams#" + id;
    }
}