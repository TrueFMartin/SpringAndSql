package com.github.truefmartin.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ColumnDefault("unique_rowid()")
    @Column(name = "game_id", nullable = false)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id1")
    public Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id2")
    public Team awayTeam;

    @Column(name = "score1")
    public int score1;

    @Column(name = "score2")
    public int score2;

    @Column(name = "date")
    public Date date;

    public boolean homeTeamWon() {
        return score1 > score2;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getScore1() {
        return score1;
    }

    public void setScore1(int score1) {
        this.score1 = score1;
    }

    public int getScore2() {
        return score2;
    }

    public void setScore2(int score2) {
        this.score2 = score2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        if (id != 0 && game.id != 0) return id == game.id;
        return Objects.equals(score1, game.score1) && Objects.equals(score2, game.score2) && Objects.equals(date, game.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score1, score2, date);
    }
//
//    @Override
//    public Field[] getFields() {
//        return Game.class.getFields();
//    }
}