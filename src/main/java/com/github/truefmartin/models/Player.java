package com.github.truefmartin.models;

import com.github.truefmartin.views.VarArgPrintFields;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;

@Entity
@Table(name = "player")
public class Player {
    @Id
    @ColumnDefault("unique_rowid()")
    @Column(name = "player_id", nullable = false)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    public Team team;

    @Column(name = "name", nullable = false, length = 50)
    public String name;


    @Column(name = "position")
    @Enumerated(EnumType.STRING)
    public PositionType position;
    public enum PositionType {
        QUARTERBACK,
        RUNNING_BACK,
        WIDE_RECEIVER,
        TIGHT_END,
        DEFENSIVE_END,
        LINEBACKER,
        CORNERBACK,
        SAFETY
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PositionType getPosition() {
        return position;
    }

    public void setPosition(PositionType position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) && position == player.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, name, position);
    }
}