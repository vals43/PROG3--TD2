import PROG3.model.*;
import PROG3.service.DataRetriever;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataRetriverTest {

    private static DataRetriever dataRetriever;

    @BeforeAll
    static void init() {
        dataRetriever = new DataRetriever();
    }

    @Test
    void a_findTeamById_1() throws SQLException {
        Team t = dataRetriever.findTeamById(1);
        System.out.println(t);
        assertNotNull(t);
        assertEquals("Real Madrid CF", t.getName());
        assertEquals(3, t.getPlayers().size());
        //assertEquals(4, t.getPlayers().size()); avec l'insertion de vini au test i_saveTeam_addPlayer

    }

    @Test
    void b_findTeamById_5() throws SQLException {
        Team t = dataRetriever.findTeamById(5);
        assertNotNull(t);
        assertEquals("Inter Miami CF", t.getName());
        assertTrue(t.getPlayers().isEmpty());
    }

    @Test
    void c_findPlayers_page1_size2() throws SQLException {
        List<Player> players = dataRetriever.findPlayers(1, 2);
        System.out.println(players);
        assertEquals(2, players.size());
        assertEquals("Thibaut Courtois", players.get(0).getName());
        assertEquals("Dani Carvajal", players.get(1).getName());
    }

    @Test
    void d_findPlayers_page3_size5() throws SQLException {
        List<Player> players = dataRetriever.findPlayers(3, 5);
        assertTrue(players.isEmpty());
    }

    @Test
    void e_findTeamsByPlayerName() {
        List<Team> teams = dataRetriever.findTeamsByPlayerName("an");
        assertEquals(2, teams.size());
        System.out.println(teams);
        assertTrue(teams.stream().anyMatch(t -> t.getName().equals("Real Madrid CF")));
        assertTrue(teams.stream().anyMatch(t -> t.getName().equals("Atl‚tico de Madrid")));
    }

    @Test
    void f_findPlayersByCriteria() {
        List<Player> players = dataRetriever.findPlayersByCriteria(
                "ud", PlayerPositionEnum.MIDF, "Madrid", ContinentEnum.EUROPA, 1, 10
        );
        assertEquals(1, players.size());
        assertEquals("Jude Bellingham", players.getFirst().getName());
    }

    @Test
    void g_createPlayers_duplicateId() {
        List<Player> input = List.of(
                new Player(6, "Jude Bellingham", 23, PlayerPositionEnum.STR, null),
                new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null)
        );
        assertThrows(RuntimeException.class, () -> dataRetriever.createPlayers(input));
    }

    @Test
    void h_createPlayers_valid() {
        List<Player> input = List.of(
                new Player(6, "Vini", 25, PlayerPositionEnum.STR, null),
                new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null)
        );
        List<Player> result = dataRetriever.createPlayers(input);
        assertEquals(2, result.size());
        assertEquals("Vini", result.get(0).getName());
        assertEquals("Pedri", result.get(1).getName());
    }
    @Test
    void i_saveTeam_addPlayer() throws SQLException {

        Player newPlayer = new Player(6, "Vini", 25, PlayerPositionEnum.STR, null);

        Team teamBefore = dataRetriever.findTeamById(1);
        int initialSize = teamBefore.getPlayers().size();

        teamBefore.getPlayers().add(newPlayer);

        Team updated = dataRetriever.saveTeam(teamBefore);

        assertEquals(initialSize + 1, updated.getPlayers().size());
        assertTrue(updated.getPlayers().stream()
                .anyMatch(p -> p.getName().equals("Vini")));
    }

    @Test
    void j_saveTeam_removeAllPlayers() throws SQLException {

        Team team = dataRetriever.findTeamById(2);
        team.getPlayers().clear();

        Team updated = dataRetriever.saveTeam(team);

        assertNotNull(updated);
        assertEquals("FC Barcelona", updated.getName());
        assertTrue(updated.getPlayers().isEmpty());
    }

    @Test
    void test_total_But_Equipe() throws SQLException {
        Team t = dataRetriever.findTeamById(1);
        assertNotNull(t);
        try {
            Integer total = t.getPlayersGoals();
            assertTrue(total >= 0);
        } catch (PROG3.exception.NullGoalException e) {
            System.out.println("Exception confirmée : " + e.getMessage());
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void but_apres_Update() throws SQLException {
        Team team = dataRetriever.findTeamById(1);
        Player p = team.getPlayers().getFirst();
        p.setGoalNb(10);

        Team updated = dataRetriever.saveTeam(team);

        try {
            assertEquals(10, updated.getPlayersGoals());
        } catch (PROG3.exception.NullGoalException e) {
            System.out.println("Exception confirmée : " + e.getMessage());
            assertNotNull(e.getMessage());
        }
    }

}
