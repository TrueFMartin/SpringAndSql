package com.github.truefmartin.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class GameTest {

    @Test
    void varArgPrintFields() {
        var game = new Game();
        game.setId(1L);
        game.setDate(java.time.LocalDate.now());
        game.setScore1(10L);
        game.setScore2(20L);
        Team team1 = new Team();
        team1.setId(1L);
        team1.setLocation("home and stuff");
//        team1.setVarArgFields(0,1,5);
        Team team2 = new Team();
        team2.setId(2L);
        Player player1 = new Player();
        player1.setId(1L);
        player1.setName("player1");
        player1.setPosition(Player.PositionType.QUARTERBACK);
//        player1.setVarArgFields(0,1,2);
        player1.setTeam(team1);
//        team1.setGamesHome(Set.of(game));
        game.setHomeTeam(team1);
        game.setAwayTeam(team2);
//        game.setVarArgFields(0,2,3,4);
//        var res = player1.printPreSetFields();
//        System.out.println(res);


        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(player1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(json);
    }

    @Test
    void setVarArgFields() {
    }

    @Test
    void printPreSetFields() {
    }
}