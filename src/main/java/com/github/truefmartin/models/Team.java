package com.github.truefmartin.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.*;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @ColumnDefault("unique_rowid()")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id", nullable = false)
    public Long id;

    @Column(name = "location", nullable = false, length = 50)
    public String location;

    @Column(name = "nickname", nullable = false, length = 50)
    public String nickname;

    @Column(name = "conference")
    @Enumerated(EnumType.STRING)
    public ConferenceType conference;

    public enum ConferenceType {
        afc,
        nfc;
        private final String displayValue;
        ConferenceType() {
            this.displayValue = DivisionType.inner.displayNames.get(this.ordinal());
        }
        public String getDisplayValue() {
            return displayValue;
        }
        private static class inner {
            static ArrayList<String> displayNames = new ArrayList<>(){
                {
                    add("AFC");
                    add("NFC");
                }
            };
        }
    }

    @Column(name = "division")
    @Enumerated(EnumType.STRING)
    public DivisionType division;
    public enum DivisionType {
        east,
        north,
        south,
        west;
        private final String displayValue;
        DivisionType() {
            this.displayValue = inner.displayNames.get(this.ordinal());
        }
        public String getDisplayValue() {
            return displayValue;
        }
        private static class inner {
            static ArrayList<String> displayNames = new ArrayList<>(){
                {
                    add("East");
                    add("North");
                    add("South");
                    add("West");
                }
            };
        }
    }


    @OneToMany(mappedBy = "homeTeam")
    public Set<Game> gamesHome = new LinkedHashSet<>();
    @OneToMany(mappedBy = "awayTeam")
    public List<Game> gamesAway = new ArrayList<>();



    @OneToMany(mappedBy = "team")
    public Set<Player> players = new LinkedHashSet<>();



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

    public Set<Game> getGamesHome() {
        return gamesHome;
    }

    public void setGamesHome(Set<Game> gamesHome) {
        this.gamesHome = gamesHome;
    }

    public List<Game> getGamesAway() {
        return gamesAway;
    }

    public void setGamesAway(List<Game> gamesAway) {
        this.gamesAway = gamesAway;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

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