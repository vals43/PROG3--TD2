package PROG3.model;

import PROG3.exception.NullGoalException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private int id;
    private String name;
    private ContinentEnum continent;
    private List<Player> players;

    public Team(int id, String name, ContinentEnum continent) {
        this.id = id;
        this.name = name;
        this.continent = continent;
        players = new ArrayList<>();
    }

    public int getPlayerCount() {
        return players.size();
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContinentEnum getContinent() {
        return continent;
    }

    public void setContinent(ContinentEnum continent) {
        this.continent = continent;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Integer getPlayersGoals() {
        if (this.players == null || this.players.isEmpty()) {
            return null;
        }

        Integer total = 0;
        for (Player p : this.getPlayers()) {
            if (p.getGoalNb() == null) {
                System.out.println(p.getName() + " has no goal");
                throw new NullGoalException(
                        "Le nombre de buts du joueur " + p.getName() +
                                " est inconnu. Impossible de calculer le total de l'équipe."
                );
            }
            total += p.getGoalNb();
        }
        return total;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id && Objects.equals(name, team.name) && continent == team.continent && Objects.equals(players, team.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, continent, players);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", Continent=" + continent +
                ", Players=" + players +
                '}';
    }
}
