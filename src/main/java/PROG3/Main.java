package PROG3;

import PROG3.DB.DBConnection;
import PROG3.model.Team;
import PROG3.service.DataRetriever;

public class Main {
    public static void main(String[] args) {
        DataRetriever data = new DataRetriever();
        DBConnection db = new DBConnection();

        try {
            db.getDBConnection();
            Team team = data.findTeamById(1);
            System.out.println("=== TEST findTeamById ===");
            try {
                System.out.print("Total buts équipe: ");
                System.out.print(team.getPlayersGoals());
            } catch (RuntimeException e) {
                System.out.print("Erreur: " + e.getMessage());
            }

            System.out.println("\n=== TEST saveTeam ===");
            try{
                if (team != null) {
                    Team saved = data.saveTeam(team);

                    System.out.println("Équipe après sauvegarde : " + saved.getName());
                    System.out.println("Total buts après sauvegarde : " + saved.getPlayersGoals());
                }
            } catch (RuntimeException e) {
                System.out.print("Erreur: " + e.getMessage());
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
