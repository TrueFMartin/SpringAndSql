package com.github.truefmartin.models;

import com.github.truefmartin.views.VarArgPrintFields;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @ColumnDefault("unique_rowid()")
    @Column(name = "team_id", nullable = false)
    public Long id;

    @Column(name = "location", nullable = false, length = 50)
    public String location;

    @Column(name = "nickname", nullable = false, length = 50)
    public String nickname;

    @Column(name = "conference")
    @Enumerated(EnumType.STRING)
    public ConferenceType conference;
//
//    @Override
//    public Field[] getFields() {
//        return Game.class.getFields();
//    }

    public enum ConferenceType {
        AFC,
        NFC
    }

    @Column(name = "division")
    @Enumerated(EnumType.STRING)
    public DivisionType division;
    public enum DivisionType {
        EAST,
        NORTH,
        SOUTH,
        WEST
    }

//
//    @OneToMany(mappedBy = "teamId1")
//    public Set<Game> gamesHome = new LinkedHashSet<>();
//    @OneToMany(mappedBy = "teamId2")
//    public Set<Game> gamesAway = new LinkedHashSet<>();
//    @OneToMany(mappedBy = "team")
//    public Set<Player> players = new LinkedHashSet<>();



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ConferenceType getConference() {
        return conference;
    }

    public void setConference(ConferenceType conference) {
        this.conference = conference;
    }

    public DivisionType getDivision() {
        return division;
    }

    public void setDivision(DivisionType division) {
        this.division = division;
    }
//
//    public Set<Game> getGamesHome() {
//        return gamesHome;
//    }
//
//    public void setGamesHome(Set<Game> gamesHome) {
//        this.gamesHome = gamesHome;
//    }
//
//    public Set<Game> getGamesAway() {
//        return gamesAway;
//    }
//
//    public void setGamesAway(Set<Game> gamesAway) {
//        this.gamesAway = gamesAway;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(location, team.location) && Objects.equals(nickname, team.nickname) && conference == team.conference && division == team.division;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, nickname, conference, division);
    }
}