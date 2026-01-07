package PROG3.service;

import PROG3.DB.DBConnection;
import PROG3.model.ContinentEnum;
import PROG3.model.Player;
import PROG3.model.PlayerPositionEnum;
import PROG3.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();

    public Team findTeamById(Integer id) throws SQLException {

        Team team = null;

        String teamQuery = """
                SELECT id, name , continent
                FROM Team
                WHERE id = ?
                """;

        String playerQuery = """
                SELECT id, name, age, position , goal_nb
                FROM Player
                WHERE id_team = ?
                """;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement teamStmt = connection.prepareStatement(teamQuery)) {

            teamStmt.setInt(1, id);
            ResultSet teamRs = teamStmt.executeQuery();

            if (teamRs.next()) {
                team = new Team(
                        teamRs.getInt("id"),
                        teamRs.getString("name"),
                        ContinentEnum.valueOf(teamRs.getString("continent"))
                );

                try (PreparedStatement playerStmt = connection.prepareStatement(playerQuery)) {
                    playerStmt.setInt(1, id);
                    ResultSet playerRs = playerStmt.executeQuery();

                    while (playerRs.next()) {
                        Integer goalNb = playerRs.getObject("goal_nb", Integer.class);

                        Player player = new Player(
                                playerRs.getInt("id"),
                                playerRs.getString("name"),
                                playerRs.getInt("age"),
                                PlayerPositionEnum.valueOf(playerRs.getString("position")),
                                goalNb,
                                team
                        );
                        team.getPlayers().add(player);
                    }
                }
            }
        }

        return team;
    }


    public List<Player> findPlayers(int page, int size) throws SQLException {

        List<Player> players = new ArrayList<>();

        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("page et size doivent être > 0");
        }

        String query = """
            SELECT p.id, p.name, p.age, p.position, p.goal_nb,
                   t.id AS team_id, t.name AS team_name, t.continent
            FROM Player p
            LEFT JOIN Team t ON p.id_team = t.id
            ORDER BY p.id
            LIMIT ? OFFSET ?
            """;

        int offset = (page - 1) * size;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Team team = null;
                if (rs.getInt("team_id") != 0) {
                    team = new Team(
                            rs.getInt("team_id"),
                            rs.getString("team_name"),
                            ContinentEnum.valueOf(rs.getString("continent"))
                    );
                }
                Integer goalNb = rs.getObject("goal_nb", Integer.class);

                Player player = new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        PlayerPositionEnum.valueOf(rs.getString("position")),
                        goalNb,
                        team
                );

                players.add(player);
            }
        }

        return players;
    }

    public List<Player> createPlayers(List<Player> newPlayers) {

        if (newPlayers == null || newPlayers.isEmpty()) {
            throw new IllegalArgumentException("liste vide");
        }

        try (Connection connection = dbConnection.getDBConnection()) {

            connection.setAutoCommit(false);

            String insertQuery = """
                    INSERT INTO Player(id, name, age, position, goal_nb, id_team)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

            for (Player p : newPlayers) {
                if (playerExists(connection, p.getName())) {
                    connection.rollback();
                    throw new IllegalStateException("deja existant : " + p.getName());
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                for (Player p : newPlayers) {
                    stmt.setInt(1, p.getId());
                    stmt.setString(2, p.getName());
                    stmt.setInt(3, p.getAge());
                    stmt.setObject(4, p.getPosition().name(), java.sql.Types.OTHER);
                    if (p.getGoalNb() != null) {
                        stmt.setInt(5, p.getGoalNb());
                    } else {
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    }
                    if (p.getTeam() != null) {
                        stmt.setInt(5, p.getTeam().getId());
                    } else {
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    }
                    stmt.executeUpdate();
                }
            }

            connection.commit();
            return newPlayers;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean playerExists(Connection connection, String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM Player WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public Team saveTeam(Team teamToSave) {

        if (teamToSave == null) {
            throw new IllegalArgumentException("team null");
        }

        try (Connection connection = dbConnection.getDBConnection()) {

            connection.setAutoCommit(false);

            String existsQuery = "SELECT COUNT(*) FROM Team WHERE id = ?";
            String insertQuery = "INSERT INTO Team(id, name, continent) VALUES (?, ?, ?::continent_enum)";
            String updateQuery = "UPDATE Team SET name = ?, continent = ?::continent_enum WHERE id = ?";
            String removePlayersTeam = "UPDATE Player SET id_team = NULL WHERE id_team = ?";
            String updatePlayerTeam = "UPDATE Player SET id_team = ? WHERE id = ?";

            boolean exists;

            try (PreparedStatement stmt = connection.prepareStatement(existsQuery)) {
                stmt.setInt(1, teamToSave.getId());
                ResultSet rs = stmt.executeQuery();
                exists = rs.next() && rs.getInt(1) > 0;
            }

            if (!exists) {
                try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                    stmt.setInt(1, teamToSave.getId());
                    stmt.setString(2, teamToSave.getName());
                    stmt.setString(3, teamToSave.getContinent().name());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                    stmt.setString(1, teamToSave.getName());
                    stmt.setString(2, teamToSave.getContinent().name());
                    stmt.setInt(3, teamToSave.getId());
                    stmt.executeUpdate();
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement(removePlayersTeam)) {
                stmt.setInt(1, teamToSave.getId());
                stmt.executeUpdate();
            }

            if (teamToSave.getPlayers() != null) {
                try (PreparedStatement stmt = connection.prepareStatement(updatePlayerTeam)) {
                    for (Player p : teamToSave.getPlayers()) {
                        stmt.setInt(1, teamToSave.getId());
                        stmt.setInt(2, p.getId());
                        stmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            return teamToSave;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Team> findTeamsByPlayerName(String playerName) {

        List<Team> teams = new ArrayList<>();

        String query = """
        SELECT t.id AS team_id, t.name AS team_name, t.continent AS team_continent
        FROM Team t
        JOIN Player p ON p.id_team = t.id
        WHERE LOWER(p.name) LIKE LOWER(?)
        GROUP BY t.id, t.name, t.continent
        """;

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, "%" + playerName + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Team team = new Team(
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        ContinentEnum.valueOf(rs.getString("team_continent"))
                );
                teams.add(team);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return teams;
    }

    public List<Player> findPlayersByCriteria(String playerName, PlayerPositionEnum position, String teamName, ContinentEnum continent, int page, int size) {
        List<Player> players = new ArrayList<>();

        String baseQuery = """
       SELECT p.id AS player_id, p.name AS player_name, p.age AS player_age,
               p.position AS player_position, p.goal_nb,
               t.id AS team_id, t.name AS team_name, t.continent AS team_continent
        FROM Player p
        JOIN Team t ON p.id_team = t.id
        WHERE 1=1
        """;

        List<Object> params = new ArrayList<>();

        if (playerName != null && !playerName.isBlank()) {
            baseQuery += " AND LOWER(p.name) LIKE LOWER(?)";
            params.add("%" + playerName + "%");
        }

        if (position != null) {
            baseQuery += " AND p.position = CAST(? AS position_enum)";
            params.add(position.name());
        }
        if (teamName != null && !teamName.isBlank()) {
            baseQuery += " AND t.name ILIKE ?";
            params.add("%" + teamName + "%");
        }


        if (continent != null) {
            baseQuery += " AND t.continent = CAST(? AS continent_enum)";
            params.add(continent.name());
        }

        baseQuery += " LIMIT ? OFFSET ?";
        params.add(size);
        params.add((page - 1) * size);

        try (Connection connection = dbConnection.getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(baseQuery)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Team team = new Team(
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        ContinentEnum.valueOf(rs.getString("team_continent"))
                );
                Integer goalNb = rs.getObject("goal_nb", Integer.class);

                Player player = new Player(
                        rs.getInt("player_id"),
                        rs.getString("player_name"),
                        rs.getInt("player_age"),
                        PlayerPositionEnum.valueOf(rs.getString("player_position")),
                        goalNb,
                        team
                );

                players.add(player);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return players;
    }



}
